package com.lukemango.plotmines;

import com.lukemango.plotmines.commands.CommandManager;
import com.lukemango.plotmines.commands.impl.DeleteMineCommand;
import com.lukemango.plotmines.commands.impl.GiveMineCommand;
import com.lukemango.plotmines.commands.impl.ReloadCommand;
import com.lukemango.plotmines.config.ConfigManager;
import com.lukemango.plotmines.listener.OnBlockBreak;
import com.lukemango.plotmines.listener.OnChatEvent;
import com.lukemango.plotmines.listener.OnPlayerInteract;
import com.lukemango.plotmines.manager.MineManager;
import com.lukemango.plotmines.storage.JsonStorageManager;
import com.lukemango.plotmines.util.Holograms;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.plugin.java.JavaPlugin;

public final class PlotMines extends JavaPlugin {

    // Singleton instance
    private static PlotMines instance;

    // Managers
    private ConfigManager configManager;
    private MineManager mineManager;
    private CommandManager commandManager;

    // Adventure
    private BukkitAudiences adventure;

    // Holograms
    private Holograms holograms;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize adventure
        this.adventure = BukkitAudiences.create(this);

        // Initialize the config manager & configs with it
        configManager = new ConfigManager();

        // Initialize the mine manager
        mineManager = new MineManager(this);

        // Initialize the command manager
        commandManager = new CommandManager();
        commandManager.registerCommand(
                new GiveMineCommand(),
                new ReloadCommand(),
                new DeleteMineCommand()
        );

        // Initialize the holograms
        if (configManager.getConfig().areHologramsEnabled()
                && getServer().getPluginManager().getPlugin("DecentHolograms") != null) {
            holograms = new Holograms();
        } else {
            holograms = null;
            this.getLogger().warning("Holograms are disabled or DecentHolograms is not installed. Holograms will not be displayed.");
        }

        // Register listeners
        getServer().getPluginManager().registerEvents(new OnPlayerInteract(), this);
        getServer().getPluginManager().registerEvents(new OnBlockBreak(), this);
        getServer().getPluginManager().registerEvents(new OnChatEvent(), this);
    }

    @Override
    public void onDisable() {
        // Save all mines to the JSON file
        JsonStorageManager.saveAll();

        // Close the adventure
        this.adventure.close();

        // Delete all holograms
        holograms.removeAllHolograms();
    }

    public static PlotMines getInstance() {
        return instance;
    }

    public BukkitAudiences getAdventure() {
        return adventure;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MineManager getMineManager() {
        return mineManager;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public Holograms getHolograms() {
        return holograms;
    }
}
