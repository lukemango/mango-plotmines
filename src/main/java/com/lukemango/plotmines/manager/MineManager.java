package com.lukemango.plotmines.manager;

import com.lukemango.plotmines.PlotMines;
import com.lukemango.plotmines.config.ConfigManager;
import com.lukemango.plotmines.config.impl.impl.MineItem;
import com.lukemango.plotmines.manager.impl.Mine;
import com.lukemango.plotmines.storage.JsonMineStorage;
import com.lukemango.plotmines.util.Colourify;
import com.lukemango.plotmines.util.FinePosition;
import com.lukemango.plotmines.util.LocationUtil;
import com.lukemango.plotmines.util.StringUtil;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MineManager {

    private static Set<Mine> mines = new HashSet<>();
    private final PlotMines plugin;

    public MineManager(PlotMines plugin) {
        this.plugin = plugin;

        // Load all mines from the JSON file and reset them
        JsonMineStorage.get().load().thenRun(() -> {
            plugin.getLogger().info("Loaded " + mines.size() + " plot mine(s) from storage");
            mines.forEach(Mine::reset);
        });
    }

    public void createMine(Location init, MineItem mineItem, Player player) {
        final Location[][] corners = LocationUtil.getMineLocationFromCreationPoint(init, mineItem.width(), mineItem.depth());
        final Audience playerAudience = plugin.getAdventure().player(player);

        final FinePosition minimum = new FinePosition(
                corners[0][0].getX(),
                corners[0][0].getY(),
                corners[0][0].getZ(),
                corners[0][0].getWorld().getName()
        );
        final FinePosition maximum = new FinePosition(
                corners[0][1].getX(),
                corners[0][1].getY(),
                corners[0][1].getZ(),
                corners[0][1].getWorld().getName()
        );

        // Where the block was placed to trigger the mine creation
        final FinePosition resetTeleportLocation = new FinePosition(
                init.getX() + 0.5,
                init.getY() + 2,
                init.getZ() + 0.5,
                init.getWorld().getName()
        );

        // Display Name Check
        String baseName = player.getName() + "'s " + StringUtil.formatString(mineItem.name());
        String displayName = baseName;
        long duplicateMineCount = mines.stream().filter(mine -> mine.getDisplayName().contains(baseName)).count();

        if (duplicateMineCount > 0) {
            displayName = baseName + " (" + duplicateMineCount + ")";
        }

        // Create the mine object
        final Mine mine = new Mine(
                player.getUniqueId(),
                mineItem.name(),
                displayName,
                minimum,
                maximum,
                mineItem.resetPercent(),
                mineItem.resetDelay(),
                resetTeleportLocation,
                mineItem.composition(),
                this.calculateTotalBlocks(minimum, maximum)
        );

        mines.add(mine);
        JsonMineStorage.get().saveAll();

        // Create the border and fill the mine with the composition
        mine.setBlocks(Map.of(mineItem.border(), 100.0), true); // Create the border
        mine.reset(); // Reset the mine to fill it with the composition

        // Create the interaction block to manage the mine
        final Location interactionBlock = new Location(
                init.getWorld(),
                init.getX(),
                init.getY() + 1,
                init.getZ()
        );
        interactionBlock.getBlock().setType(mineItem.interactionBlock());
        interactionBlock.getBlock().getState().update();

        // Create the hologram for the mine
        if (ConfigManager.get().getConfig().areHologramsEnabled()
                && PlotMines.getInstance().getHolograms() != null) {
            plugin.getHolograms().createHologram(mine);
        }

        // Inform the player that the mine was created
        playerAudience.sendMessage(Colourify.colour(ConfigManager.get().getMessages().getPlayerCreatedMine()
                .replace("<mine>", mine.getDisplayName()))
        );
    }

    public static void deleteMine(Mine mine) {
        mine.setBlocks(Map.of(Material.AIR, 100.0), true); // Remove the mine

        Bukkit.getScheduler().runTask(PlotMines.getInstance(), () -> {
            final Location interactionBlock = mine.getResetTeleportLocation().toLocation();
            interactionBlock.add(0, -1, 0) // Add 1 to get the correct location
                    .getBlock().setType(Material.AIR); // Remove the interaction block
            mine.getResetTeleportLocation().toLocation().getBlock().getState().update();
        });

        // Remove the hologram
        if (ConfigManager.get().getConfig().areHologramsEnabled()
                && PlotMines.getInstance().getHolograms() != null) {
            PlotMines.getInstance().getHolograms().removeHologram(mine);
        }

        // Remove the mine from the list and save the changes
        mines.remove(mine);
        JsonMineStorage.get().saveAll();
    }

    /**
     * Calculate the total blocks in a mine
     *
     * @param pos1 The first position
     * @param pos2 The second position
     * @return The total blocks in the mine
     */
    private int calculateTotalBlocks(FinePosition pos1, FinePosition pos2) {
        // Check which position is the minimum and which is the maximum
        final FinePosition min = new FinePosition(
                Math.min(pos1.x(), pos2.x()),
                Math.min(pos1.y(), pos2.y()),
                Math.min(pos1.z(), pos2.z()),
                pos1.world()
        );

        final FinePosition max = new FinePosition(
                Math.max(pos1.x(), pos2.x()),
                Math.max(pos1.y(), pos2.y()),
                Math.max(pos1.z(), pos2.z()),
                pos1.world()
        );

        // Calculate the volume of the mine
        final int x = (int) (max.x() - min.x()) + 1; // +1 to include the last block
        final int y = (int) (max.y() - min.y()) + 1;
        final int z = (int) (max.z() - min.z()) + 1;

        return x * y * z;
    }

    public static Set<Mine> getMines() {
        return mines;
    }

    public static void setMines(Set<Mine> mineSet) {
        mines = mineSet;
    }
}
