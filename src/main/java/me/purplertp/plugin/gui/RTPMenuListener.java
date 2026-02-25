package me.purplertp.plugin.gui;

import me.purplertp.plugin.PurpleRTP;
import me.purplertp.plugin.utils.MessageUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class RTPMenuListener implements Listener {

    private final PurpleRTP plugin;

    public RTPMenuListener(PurpleRTP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // Check if this is our GUI by matching the title
        ConfigurationSection menuCfg = plugin.getConfig().getConfigurationSection("RTP-MENU");
        if (menuCfg == null) return;

        String expectedTitle = MessageUtils.format(menuCfg.getString("TITLE", ""));
        String inventoryTitle = event.getView().getTitle();

        if (!inventoryTitle.equals(expectedTitle)) return;

        event.setCancelled(true);

        if (event.getCurrentItem() == null) return;

        int clickedSlot = event.getSlot();

        // Find which button was clicked
        ConfigurationSection buttons = menuCfg.getConfigurationSection("BUTTONS");
        if (buttons == null) return;

        for (String key : buttons.getKeys(false)) {
            ConfigurationSection btn = buttons.getConfigurationSection(key);
            if (btn == null) continue;

            int slot = btn.getInt("SLOT", -1);
            if (slot != clickedSlot) continue;

            // Check if button is enabled
            if (!btn.getBoolean("ENABLED", true)) {
                player.sendMessage(MessageUtils.format(
                    plugin.getConfig().getString("MESSAGES.DESTINATION-DISABLED", "&cThis destination is disabled.")
                ));
                player.closeInventory();
                return;
            }

            String worldName = btn.getString("WORLD", "world");

            // Check denied worlds
            if (plugin.getConfig().getStringList("DENIED-WORLDS").contains(worldName)) {
                player.sendMessage(MessageUtils.format("&cThis world is not available for RTP."));
                player.closeInventory();
                return;
            }

            player.closeInventory();
            plugin.getRtpManager().randomTeleport(player, worldName);
            return;
        }
    }
}
