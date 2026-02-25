package me.purplertp.plugin.managers;

import me.purplertp.plugin.PurpleRTP;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {

    private final PurpleRTP plugin;
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();
    private File cooldownFile;
    private FileConfiguration cooldownData;

    public CooldownManager(PurpleRTP plugin) {
        this.plugin = plugin;
        loadCooldowns();
    }

    public boolean isOnCooldown(UUID uuid, String world) {
        if (!cooldowns.containsKey(uuid)) return false;
        Map<String, Long> worldCooldowns = cooldowns.get(uuid);
        if (!worldCooldowns.containsKey(world)) return false;
        long expiry = worldCooldowns.get(world);
        if (System.currentTimeMillis() >= expiry) {
            worldCooldowns.remove(world);
            return false;
        }
        return true;
    }

    public long getRemainingCooldown(UUID uuid, String world) {
        if (!cooldowns.containsKey(uuid)) return 0;
        Map<String, Long> worldCooldowns = cooldowns.get(uuid);
        if (!worldCooldowns.containsKey(world)) return 0;
        return Math.max(0, (worldCooldowns.get(world) - System.currentTimeMillis()) / 1000);
    }

    public void setCooldown(UUID uuid, String world, int seconds) {
        if (seconds <= 0) return;
        cooldowns.computeIfAbsent(uuid, k -> new HashMap<>())
                 .put(world, System.currentTimeMillis() + (seconds * 1000L));
    }

    public void removeCooldown(UUID uuid, String world) {
        if (cooldowns.containsKey(uuid)) cooldowns.get(uuid).remove(world);
    }

    public void removeCooldown(UUID uuid) {
        cooldowns.remove(uuid);
    }

    public void saveCooldowns() {
        cooldownData.set("cooldowns", null);
        for (Map.Entry<UUID, Map<String, Long>> entry : cooldowns.entrySet()) {
            for (Map.Entry<String, Long> worldEntry : entry.getValue().entrySet()) {
                cooldownData.set("cooldowns." + entry.getKey() + "." + worldEntry.getKey(), worldEntry.getValue());
            }
        }
        try { cooldownData.save(cooldownFile); }
        catch (IOException e) { plugin.getLogger().severe("Failed to save cooldowns: " + e.getMessage()); }
    }

    private void loadCooldowns() {
        cooldownFile = new File(plugin.getDataFolder(), "cooldowns.yml");
        if (!cooldownFile.exists()) {
            try { cooldownFile.getParentFile().mkdirs(); cooldownFile.createNewFile(); }
            catch (IOException e) { plugin.getLogger().severe("Failed to create cooldowns.yml"); return; }
        }
        cooldownData = YamlConfiguration.loadConfiguration(cooldownFile);
        if (cooldownData.getConfigurationSection("cooldowns") == null) return;
        for (String uuidStr : cooldownData.getConfigurationSection("cooldowns").getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                for (String world : cooldownData.getConfigurationSection("cooldowns." + uuidStr).getKeys(false)) {
                    long expiry = cooldownData.getLong("cooldowns." + uuidStr + "." + world);
                    if (System.currentTimeMillis() < expiry) {
                        cooldowns.computeIfAbsent(uuid, k -> new HashMap<>()).put(world, expiry);
                    }
                }
            } catch (IllegalArgumentException ignored) {}
        }
    }
}
