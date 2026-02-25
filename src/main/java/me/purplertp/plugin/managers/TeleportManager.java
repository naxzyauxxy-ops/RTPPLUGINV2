package me.purplertp.plugin.managers;

import me.purplertp.plugin.PurpleRTP;
import me.purplertp.plugin.utils.MessageUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ThreadLocalRandom;

public class TeleportManager {

    private final PurpleRTP plugin;

    public TeleportManager(PurpleRTP plugin) {
        this.plugin = plugin;
    }

    public void randomTeleport(Player player) {
        int maxRange = plugin.getConfig().getInt("max-range", 10000);
        int minRange = plugin.getConfig().getInt("min-range", 500);
        int maxAttempts = plugin.getConfig().getInt("max-attempts", 50);
        World world = player.getWorld();

        // Send searching message
        player.sendMessage(MessageUtils.format(
                plugin.getConfig().getString("messages.prefix", "&5&l[&dRTP&5&l] &r") +
                plugin.getConfig().getString("messages.searching", "&dSearching for a safe location...")
        ));

        // Play start sound
        player.playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRIGGER, 0.5f, 1.5f);

        // Run async to not lag the server
        new BukkitRunnable() {
            @Override
            public void run() {
                Location safeLoc = findSafeLocation(world, minRange, maxRange, maxAttempts);

                // Run teleport back on main thread
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (safeLoc == null) {
                            player.sendMessage(MessageUtils.format(
                                    plugin.getConfig().getString("messages.prefix") +
                                    plugin.getConfig().getString("messages.unsafe",
                                            "&cCould not find a safe location. Please try again.")
                            ));
                            return;
                        }

                        // Teleport effects before teleport
                        player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation(), 50, 0.5, 1, 0.5, 0.1);
                        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0.8f);

                        player.teleport(safeLoc);

                        // Teleport effects after teleport
                        safeLoc.getWorld().spawnParticle(Particle.PORTAL, safeLoc, 80, 0.5, 1, 0.5, 0.1);
                        safeLoc.getWorld().spawnParticle(Particle.WITCH, safeLoc, 30, 0.5, 1, 0.5, 0.05);
                        player.playSound(safeLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1.2f);
                        player.playSound(safeLoc, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1f, 0.8f);

                        // Success message
                        String successMsg = plugin.getConfig().getString("messages.success",
                                "&dYou have been teleported to &5%x%&d, &5%z%&d!")
                                .replace("%x%", String.valueOf(safeLoc.getBlockX()))
                                .replace("%z%", String.valueOf(safeLoc.getBlockZ()));

                        player.sendMessage(MessageUtils.format(
                                plugin.getConfig().getString("messages.prefix") + successMsg
                        ));

                        // Title display (DonutSMP style)
                        player.sendTitle(
                                MessageUtils.format("&5&l✦ &dWILDERNESS &5&l✦"),
                                MessageUtils.format("&7Teleported to &5" + safeLoc.getBlockX() + "&7, &5" + safeLoc.getBlockZ()),
                                10, 60, 20
                        );

                        // Set cooldown after successful teleport
                        plugin.getCooldownManager().setCooldown(player.getUniqueId());
                    }
                }.runTask(plugin);
            }
        }.runTaskAsynchronously(plugin);
    }

    private Location findSafeLocation(World world, int minRange, int maxRange, int maxAttempts) {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            // Generate random coords
            int x = random.nextInt(minRange, maxRange + 1) * (random.nextBoolean() ? 1 : -1);
            int z = random.nextInt(minRange, maxRange + 1) * (random.nextBoolean() ? 1 : -1);

            // Load chunk if needed and get surface
            Chunk chunk = world.getChunkAt(x >> 4, z >> 4);
            if (!chunk.isLoaded()) {
                chunk.load();
            }

            int y = world.getHighestBlockYAt(x, z);
            Location loc = new Location(world, x + 0.5, y + 1, z + 0.5);
            loc.setYaw(random.nextFloat() * 360);

            if (isSafe(loc)) {
                return loc;
            }
        }
        return null;
    }

    private boolean isSafe(Location loc) {
        Block feet = loc.getBlock();
        Block head = feet.getRelative(0, 1, 0);
        Block ground = feet.getRelative(0, -1, 0);

        // Check for air at feet and head
        if (!feet.getType().isAir() || !head.getType().isAir()) return false;

        // Check ground is solid
        if (!ground.getType().isSolid()) return false;

        // Check not water or lava surface
        Material groundType = ground.getType();
        if (groundType == Material.WATER || groundType == Material.LAVA ||
                groundType == Material.FIRE || groundType == Material.CACTUS) return false;

        // Check not over void
        if (loc.getY() <= loc.getWorld().getMinHeight()) return false;

        return true;
    }
}
