package com.lukemango.plotmines.listener;

import com.google.common.eventbus.Subscribe;
import com.lukemango.plotmines.PlotMines;
import com.lukemango.plotmines.config.ConfigManager;
import com.lukemango.plotmines.config.impl.impl.MineItem;
import com.lukemango.plotmines.manager.MineManager;
import com.lukemango.plotmines.manager.impl.Mine;
import com.lukemango.plotmines.storage.JsonMinesToGiveStorage;
import com.lukemango.plotmines.util.Colourify;
import com.lukemango.plotmines.util.LocationUtil;
import com.plotsquared.core.PlotAPI;
import com.plotsquared.core.events.PlotClearEvent;
import com.plotsquared.core.events.PlotDeleteEvent;
import com.plotsquared.core.events.PlotEvent;
import com.plotsquared.core.events.PlotUnlinkEvent;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlotListener {

    public PlotListener(PlotAPI api) {
        api.registerListener(this);
    }

    @Subscribe
    private void onPlotClear(PlotClearEvent event) {
        this.giveMinesBackFromPlot(event);
    }

    @Subscribe
    private void onPlotDelete(PlotDeleteEvent event) {
        this.giveMinesBackFromPlot(event);
    }

    @Subscribe
    private void onPlotUnmerge(PlotUnlinkEvent event) {
        this.giveMinesBackFromPlot(event);
    }

    private void giveMinesBackFromPlot(PlotEvent event) {
        // Get all the members of the plot, including the owner
        final List<UUID> members = new ArrayList<>(event.getPlot().getMembers());
        members.add(event.getPlot().getOwner());

        // Get all of their mines
        final List<Mine> mines = new ArrayList<>();
        members.forEach(uuid -> mines.addAll(MineManager.getMines().stream()
                .filter(mine -> mine.getOwner().equals(uuid))
                .toList()));

        // Check if any of the mines are in the plot
        mines.forEach(mine -> {
            if (!LocationUtil.isLocationInPlot(mine.getMinimum().toLocation(), event.getPlot())) return;
            MineManager.deleteMine(mine);

            final Player owner = Bukkit.getPlayer(mine.getOwner());
            if (owner == null) {
                JsonMinesToGiveStorage.get().addMineToGiveBack(mine.getOwner(), mine.getMineType());
            } else {
                this.giveOnlinePlayerMineBack(mine, owner);
            }
        });
    }

    private void giveOnlinePlayerMineBack(Mine mine, Player owner) {
        final Audience targetAudience = PlotMines.getInstance().getAdventure().player(owner);

        // Check if the player has an empty inventory slot
        if (owner.getInventory().firstEmpty() == -1) { // If the player has a full inventory
            targetAudience.sendMessage(Colourify.colour(ConfigManager.get().getMessages().getPlayerPlotClearedFullInventory()
                    .replace("<mine>", mine.getDisplayName())
                    .replace("<amount>", "1")));

            final MineItem mineItem = ConfigManager.get().getConfig().getMineItem(owner, mine.getMineType());
            if (mineItem == null) { // If the mine item is null, the mine does not exist
                PlotMines.getInstance().getLogger().warning("Plot Clear: Mine item is null for mine type " + mine.getMineType());
                return;
            }
            owner.getWorld().dropItem(owner.getLocation(), ConfigManager.get().getConfig().getMineItem(owner, mine.getMineType()).creationItem());
        } else { // If the player has an empty inventory slot
            targetAudience.sendMessage(Colourify.colour(ConfigManager.get().getMessages().getPlayerPlotCleared()
                    .replace("<mine>", mine.getDisplayName())
                    .replace("<amount>", "1")));

            final MineItem mineItem = ConfigManager.get().getConfig().getMineItem(owner, mine.getMineType());
            if (mineItem == null) { // If the mine item is null, the mine does not exist
                PlotMines.getInstance().getLogger().warning("Plot Clear: Mine item is null for mine type " + mine.getMineType());
                return;
            }
            owner.getInventory().addItem(mineItem.creationItem());
        }
    }

}
