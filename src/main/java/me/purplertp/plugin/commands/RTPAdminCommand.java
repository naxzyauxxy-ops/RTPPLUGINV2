package me.purplertp.plugin.commands;

import me.purplertp.plugin.PurpleRTP;
import me.purplertp.plugin.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RTPAdminCommand implements CommandExecutor {

    private final PurpleRTP plugin;
    private final String prefix = "&5&l[&dRTP Admin&5&l] &r";

    public RTPAdminCommand(PurpleRTP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("purplertp.admin")) {
            sender.sendMessage(MessageUtils.format(prefix + "&cNo permission."));
            return true;
        }

        if (args.length == 0) { sendHelp(sender); return true; }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                plugin.reloadConfig();
                sender.sendMessage(MessageUtils.format(prefix + "&dConfig reloaded!"));
            }
            case "clearcooldown" -> {
                if (args.length < 2) { sender.sendMessage(MessageUtils.format(prefix + "&cUsage: /rtpadmin clearcooldown <player>")); return true; }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) { sender.sendMessage(MessageUtils.format(prefix + "&cPlayer not found.")); return true; }
                plugin.getCooldownManager().removeCooldown(target.getUniqueId());
                sender.sendMessage(MessageUtils.format(prefix + "&dCleared all cooldowns for &5" + target.getName()));
            }
            case "forcertp" -> {
                if (args.length < 2) { sender.sendMessage(MessageUtils.format(prefix + "&cUsage: /rtpadmin forcertp <player> [world]")); return true; }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) { sender.sendMessage(MessageUtils.format(prefix + "&cPlayer not found.")); return true; }
                String world = args.length >= 3 ? args[2] : target.getWorld().getName();
                plugin.getRtpManager().randomTeleport(target, world);
                sender.sendMessage(MessageUtils.format(prefix + "&dForce-RTPing &5" + target.getName() + " &din &5" + world));
            }
            default -> sendHelp(sender);
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(MessageUtils.format("&5&l━━━ &dPurpleRTP Admin &5&l━━━"));
        sender.sendMessage(MessageUtils.format("&5/rtpadmin reload &d- Reload config"));
        sender.sendMessage(MessageUtils.format("&5/rtpadmin clearcooldown <player> &d- Clear all cooldowns"));
        sender.sendMessage(MessageUtils.format("&5/rtpadmin forcertp <player> [world] &d- Force RTP"));
        sender.sendMessage(MessageUtils.format("&5&l━━━━━━━━━━━━━━━━━━━━━━━━━"));
    }
}
