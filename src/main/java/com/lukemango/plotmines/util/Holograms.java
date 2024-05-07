package com.lukemango.plotmines.util;

import com.lukemango.plotmines.PlotMines;
import com.lukemango.plotmines.config.ConfigManager;
import com.lukemango.plotmines.config.impl.Config;
import com.lukemango.plotmines.manager.MineManager;
import com.lukemango.plotmines.manager.impl.Mine;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Holograms {

    private final Map<UUID, Hologram> holograms = new HashMap<>();
    private BukkitTask task; // Task to refresh the holograms

    public Holograms() {
        for (Mine mine : MineManager.getMines()) {
            this.createHologram(mine);
        }

        this.refreshHolograms();
    }

    public void reloadHolograms() {
        this.removeAllHolograms();
        for (Mine mine : MineManager.getMines()) {
            this.createHologram(mine);
        }

        // Cancel the task and restart the task in case the update interval has changed
        task.cancel();
        this.refreshHolograms();
    }

    /**
     * Refresh the holograms every second if they are loaded
     */
    public void refreshHolograms() {
        task = Bukkit.getScheduler().runTaskTimerAsynchronously(PlotMines.getInstance(), () -> {

            for (Mine mine : MineManager.getMines()) {
                if (!mine.getResetTeleportLocation().toLocation().getChunk().isLoaded()) continue;

                final Hologram hologram = holograms.get(mine.getUuid());
                final List<String> loreLines = this.getLoreLines(mine, ConfigManager.get().getConfig());

                DHAPI.setHologramLines(hologram, loreLines);
            }
        }, 0, ConfigManager.get().getConfig().getHologramUpdateInterval() * 20L);
    }

    /**
     * Create a hologram for a mine
     */
    public void createHologram(Mine mine) {
        final Config config = ConfigManager.get().getConfig();
        final FinePosition location = mine.getResetTeleportLocation();
        final List<String> lines = this.getLoreLines(mine, config);

        final double height = lines.size() * 0.25;
        final FinePosition hologramLocation = new FinePosition(
                location.x(),
                location.y() + height + config.getHologramOffset(),
                location.z(),
                location.world());

        holograms.put(
                mine.getUuid(),
                DHAPI.createHologram(
                        "mangoplotmines_" + mine.getUuid(),
                        hologramLocation.toLocation(),
                        false,
                        lines
                )
        );
    }

    private List<String> getLoreLines(Mine mine, Config config) {
        final List<String> lines = new ArrayList<>();

        for (final String line : config.getHologramText()) {
            if (line == null) continue;

            // Loops through the hologram lines and updates them, parsing our placeholders and colours (MiniMessage -> LegacyComponent)
            lines.add(LegacyComponentSerializer.legacySection().serialize(
                    Colourify.colour(line.replace("<name>", mine.getDisplayName())
                            .replace("<owner>", Bukkit.getOfflinePlayer(mine.getOwner()).getName())
                            .replace("<mined-blocks>", String.valueOf(mine.getBlocksRemaining()))
                            .replace("<total-blocks>", String.valueOf(mine.getTotalBlocks()))
                            .replace("<reset-percent>", String.valueOf(mine.getResetPercentage()))
                            .replace("<mined-percent>", String.valueOf(100 - Math.round(((float) mine.getBlocksRemaining() / (float) mine.getTotalBlocks()) * 100)))
                    )));
        }
        return lines;
    }

    public void removeHologram(Mine mine) {
        Hologram hologram = holograms.remove(mine.getUuid());
        if (hologram != null) {
            hologram.delete();
        }
    }

    public void removeAllHolograms() {
        holograms.values().forEach(Hologram::delete);
        holograms.clear();
    }

}
