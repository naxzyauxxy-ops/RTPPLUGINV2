package me.purplertp.plugin.gui;

import me.purplertp.plugin.PurpleRTP;
import me.purplertp.plugin.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class RTPMenu {

    private final PurpleRTP plugin;

    public RTPMenu(PurpleRTP plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        ConfigurationSection menuCfg = plugin.getConfig().getConfigurationSection("RTP-MENU");
        if (menuCfg == null) return;

        String title = MessageUtils.format(menuCfg.getString("TITLE", "&8Random Teleport"));
        int size = menuCfg.getInt("SIZE", 27);
        boolean placeholder = menuCfg.getBoolean("PLACEHOLDER", true);

        Inventory inv = Bukkit.createInventory(null, size, title);

        // Fill with purple glass panes as placeholders
        if (placeholder) {
            ItemStack pane = buildItem(Material.PURPLE_STAINED_GLASS_PANE, " ", null);
            for (int i = 0; i < size; i++) {
                inv.setItem(i, pane);
            }
        }

        // Place buttons
        ConfigurationSection buttons = menuCfg.getConfigurationSection("BUTTONS");
        if (buttons != null) {
            for (String key : buttons.getKeys(false)) {
                ConfigurationSection btn = buttons.getConfigurationSection(key);
                if (btn == null) continue;

                String worldName = btn.getString("WORLD", "world");
                int slot = btn.getInt("SLOT", 0);
                String displayName = MessageUtils.format(btn.getString("DISPLAY-NAME", key));
                Material material = parseMaterial(btn.getString("MATERIAL", "GRASS_BLOCK"));
                List<String> rawLore = btn.getStringList("LORE");

                // Build lore with replacements
                int worldPlayers = Bukkit.getWorld(worldName) != null
                        ? Bukkit.getWorld(worldName).getPlayerCount() : 0;
                int ping = player.getPing();

                List<String> lore = new ArrayList<>();
                for (String line : rawLore) {
                    lore.add(MessageUtils.format(
                        line.replace("{players}", String.valueOf(worldPlayers))
                            .replace("{ping}", String.valueOf(ping))
                    ));
                }

                if (slot >= 0 && slot < size) {
                    inv.setItem(slot, buildItem(material, displayName, lore));
                }
            }
        }

        player.openInventory(inv);
    }

    private ItemStack buildItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        meta.setDisplayName(name);
        if (lore != null) meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private Material parseMaterial(String name) {
        try { return Material.valueOf(name.toUpperCase()); }
        catch (IllegalArgumentException e) { return Material.GRASS_BLOCK; }
    }
}
