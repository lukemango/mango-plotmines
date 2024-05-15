package com.lukemango.plotmines;

import com.lukemango.plotmines.commands.CommandManager;
import com.lukemango.plotmines.commands.impl.AdminDeleteMineCommand;
import com.lukemango.plotmines.commands.impl.AdminGiveMineCommand;
import com.lukemango.plotmines.commands.impl.AdminReloadCommand;
import com.lukemango.plotmines.commands.impl.PlayerDeleteMineCommand;
import com.lukemango.plotmines.config.ConfigManager;
import com.lukemango.plotmines.listener.BlockBreakListener;
import com.lukemango.plotmines.listener.PlayerChatListener;
import com.lukemango.plotmines.listener.PlayerInteractListener;
import com.lukemango.plotmines.listener.PlayerJoinListener;
import com.lukemango.plotmines.listener.PlotListener;
import com.lukemango.plotmines.manager.MineManager;
import com.lukemango.plotmines.storage.JsonMineStorage;
import com.lukemango.plotmines.storage.JsonMinesToGiveStorage;
import com.lukemango.plotmines.util.Holograms;
import com.plotsquared.core.PlotAPI;
import com.rylinaux.plugman.api.PlugManAPI;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CompletableFuture;

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

    // PlotSquared API
    private PlotAPI plotAPI;

    // Storage
    private JsonMineStorage jsonMineStorage;
    private JsonMinesToGiveStorage jsonMinesToGiveStorage;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize adventure
        this.adventure = BukkitAudiences.create(this);

        // Initialize PlotSquared API
        this.plotAPI = new PlotAPI();
        new PlotListener(plotAPI); // Register the PlotClear event listener

        // Initialize the config manager & configs with it
        configManager = new ConfigManager();

        // Init storage
        jsonMineStorage = new JsonMineStorage();
        jsonMinesToGiveStorage = new JsonMinesToGiveStorage();

        // Initialize the mine manager
        mineManager = new MineManager(this);

        // Initialize the command manager
        commandManager = new CommandManager();
        commandManager.registerCommand(
                new AdminGiveMineCommand(),
                new AdminReloadCommand(),
                new AdminDeleteMineCommand(),
                new PlayerDeleteMineCommand()
        );

        // Initialize the holograms
        if (configManager.getConfig().areHologramsEnabled()
                && getServer().getPluginManager().getPlugin("DecentHolograms") != null) {
            holograms = new Holograms();
        } else {
            holograms = null;
            this.getLogger().warning("Holograms are disabled or DecentHolograms is not installed. Holograms will not be displayed.");
        }

        // Anti-PlugMan (breaks because of PlotSquared events - cannot unregister them onDisable)
        if (getServer().getPluginManager().getPlugin("PlugManX") != null) {
            PlugManAPI.iDoNotWantToBeUnOrReloaded("mango-plotmines");
        }

        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerChatListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
    }

    @Override
    public void onDisable() {
        // Save all mines and mines to give
        CompletableFuture<Void> saveMines = this.jsonMineStorage.saveAll();
        CompletableFuture<Void> saveMinesToGive = saveMines.thenCompose(result -> this.jsonMinesToGiveStorage.saveAll());
        saveMinesToGive.join(); // Wait for the completion

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

    public PlotAPI getPlotAPI() {
        return plotAPI;
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
