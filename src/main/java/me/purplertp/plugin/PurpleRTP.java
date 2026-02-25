package me.purplertp.plugin;

import me.purplertp.plugin.commands.RTPAdminCommand;
import me.purplertp.plugin.commands.RTPCommand;
import me.purplertp.plugin.managers.CooldownManager;
import me.purplertp.plugin.managers.RTPManager;
import me.purplertp.plugin.gui.RTPMenuListener;
import org.bukkit.plugin.java.JavaPlugin;

public class PurpleRTP extends JavaPlugin {

    private static PurpleRTP instance;
    private CooldownManager cooldownManager;
    private RTPManager rtpManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.cooldownManager = new CooldownManager(this);
        this.rtpManager = new RTPManager(this);

        getCommand("rtp").setExecutor(new RTPCommand(this));
        getCommand("rtpadmin").setExecutor(new RTPAdminCommand(this));

        getServer().getPluginManager().registerEvents(new RTPMenuListener(this), this);

        getLogger().info("PurpleRTP enabled!");
    }

    @Override
    public void onDisable() {
        cooldownManager.saveCooldowns();
        getLogger().info("PurpleRTP disabled.");
    }

    public static PurpleRTP getInstance() { return instance; }
    public CooldownManager getCooldownManager() { return cooldownManager; }
    public RTPManager getRtpManager() { return rtpManager; }
}
