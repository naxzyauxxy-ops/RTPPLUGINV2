package me.purplertp.plugin.commands;

import me.purplertp.plugin.PurpleRTP;
import me.purplertp.plugin.gui.RTPMenu;
import me.purplertp.plugin.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RTPCommand implements CommandExecutor {

    private final PurpleRTP plugin;
    private final RTPMenu menu;

    public RTPCommand(PurpleRTP plugin) {
        this.plugin = plugin;
        this.menu = new RTPMenu(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (!player.hasPermission("purplertp.use")) {
            player.sendMessage(MessageUtils.format("&cYou don't have permission to use RTP."));
            return true;
        }

        if (!plugin.getConfig().getBoolean("ENABLED", true)) {
            player.sendMessage(MessageUtils.format(plugin.getConfig().getString("MESSAGES.DISABLED")));
            return true;
        }

        if (plugin.getConfig().getStringList("DENIED-WORLDS").contains(player.getWorld().getName())) {
            player.sendMessage(MessageUtils.format("&cRTP is not allowed in this world."));
            return true;
        }

        menu.open(player);
        return true;
    }
}
