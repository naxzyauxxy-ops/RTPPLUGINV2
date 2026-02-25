package me.purplertp.plugin.managers;

import me.purplertp.plugin.PurpleRTP;
import me.purplertp.plugin.utils.MessageUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class RTPManager {

    private final PurpleRTP plugin;
    private final Set<UUID> inRtp = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public RTPManager(PurpleRTP plugin) {
        this.plugin = plugin;
    }

    public int getPlayersInRtp() {
        return inRtp.size();
    }

    public void randomTeleport(Player player, String worldName) {
        // Plugin enabled check
        if (!plugin.getConfig().getBoolean("ENABLED", true)) {
            player.sendMessage(MessageUtils.format(plugin.getConfig().getString("MESSAGES.DISABLED")));
            return;
        }

        // Max concurrent players check
        int maxPlayers = plugin.getConfig().getInt("SETTINGS.PLAYERS-IN-RTP", 150);
        if (inRtp.size() >= maxPlayers) {
            player.sendMessage(MessageUtils.format(plugin.getConfig().getString("MESSAGES.MAX-PLAYERS")));
            return;
        }

        // World exists check
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            player.sendMessage(MessageUtils.format(plugin.getConfig().getString("MESSAGES.WORLD-NOT-EXIST")));
            return;
        }

        // Cooldown check
        if (!player.hasPermission("purplertp.bypass.cooldown") &&
                plugin.getCooldownManager().isOnCooldown(player.getUniqueId(), worldName)) {
            long remaining = plugin.getCooldownManager().getRemainingCooldown(player.getUniqueId(), worldName);
            String msg = plugin.getConfig().getString("MESSAGES.COOLDOWN", "")
                    .replace("{remaining}", String.valueOf(remaining));
            player.sendMessage(MessageUtils.format(msg));
            return;
        }

        // World settings
        String path = "WORLD-SETTINGS." + worldName + ".";
        int maxRadius = plugin.getConfig().getInt(path + "MAX-RADIUS", 10000);
        int minRadius = plugin.getConfig().getInt(path + "MIN-RADIUS", 1000);
        int centerX   = plugin.getConfig().getInt(path + "CENTER-X", 0);
        int centerZ   = plugin.getConfig().getInt(path + "CENTER-Z", 0);
        int cooldown  = plugin.getConfig().getInt(path + "COOLDOWN", 0);
        int maxAttempts = plugin.getConfig().getInt("SETTINGS.MAX-ATTEMPTS", 5);

        // Searching message
        String searching = plugin.getConfig().getString("MESSAGES.SEARCHING", "");
        if (!searching.isEmpty()) player.sendMessage(MessageUtils.format(searching));

        inRtp.add(player.getUniqueId());

        new BukkitRunnable() {
            @Override
            public void run() {
                Location safeLoc = findSafeLocation(world, centerX, centerZ, minRadius, maxRadius, maxAttempts);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        inRtp.remove(player.getUniqueId());

                        if (!player.isOnline()) return;

                        if (safeLoc == null) {
                            String msg = plugin.getConfig().getString("MESSAGES.MAX-ATTEMPTS", "")
                                    .replace("{attempts}", String.valueOf(maxAttempts));
                            if (!msg.isEmpty()) player.sendMessage(MessageUtils.format(msg));
                            return;
                        }

                        // Pre-teleport effects
                        player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation(), 50, 0.5, 1, 0.5, 0.1);
                        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0.8f);

                        player.teleport(safeLoc);

                        // Post-teleport effects
                        safeLoc.getWorld().spawnParticle(Particle.PORTAL, safeLoc, 80, 0.5, 1, 0.5, 0.1);
                        safeLoc.getWorld().spawnParticle(Particle.WITCH, safeLoc, 30, 0.5, 1, 0.5, 0.05);
                        player.playSound(safeLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1.2f);
                        player.playSound(safeLoc, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1f, 0.8f);

                        // Safe location found message
                        String foundMsg = plugin.getConfig().getString("MESSAGES.SAFE-LOCATION-FOUND", "");
                        if (!foundMsg.isEmpty()) player.sendMessage(MessageUtils.format(foundMsg));

                        // Title
                        player.sendTitle(
                            MessageUtils.format("&5&l✦ &dWILDERNESS &5&l✦"),
                            MessageUtils.format("&7" + safeLoc.getBlockX() + "&8, &7" + safeLoc.getBlockZ()),
                            10, 60, 20
                        );

                        // Set cooldown
                        plugin.getCooldownManager().setCooldown(player.getUniqueId(), worldName, cooldown);
                    }
                }.runTask(plugin);
            }
        }.runTaskAsynchronously(plugin);
    }

    private Location findSafeLocation(World world, int centerX, int centerZ,
                                       int minRadius, int maxRadius, int maxAttempts) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < maxAttempts; i++) {
            int x = centerX + (random.nextInt(minRadius, maxRadius + 1) * (random.nextBoolean() ? 1 : -1));
            int z = centerZ + (random.nextInt(minRadius, maxRadius + 1) * (random.nextBoolean() ? 1 : -1));

            Chunk chunk = world.getChunkAt(x >> 4, z >> 4);
            if (!chunk.isLoaded()) chunk.load();

            int y = world.getHighestBlockYAt(x, z);
            Location loc = new Location(world, x + 0.5, y + 1, z + 0.5);
            loc.setYaw(random.nextFloat() * 360);

            if (isSafe(loc)) return loc;
        }
        return null;
    }

    private boolean isSafe(Location loc) {
        Block feet   = loc.getBlock();
        Block head   = feet.getRelative(0, 1, 0);
        Block ground = feet.getRelative(0, -1, 0);

        if (!feet.getType().isAir() || !head.getType().isAir()) return false;
        if (!ground.getType().isSolid()) return false;

        Material g = ground.getType();
        if (g == Material.WATER || g == Material.LAVA ||
            g == Material.FIRE  || g == Material.CACTUS) return false;

        if (loc.getY() <= loc.getWorld().getMinHeight()) return false;

        return true;
    }
}
