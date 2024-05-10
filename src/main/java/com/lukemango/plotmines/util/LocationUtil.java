package com.lukemango.plotmines.util;

import com.lukemango.plotmines.PlotMines;
import com.lukemango.plotmines.config.ConfigManager;
import com.lukemango.plotmines.manager.MineManager;
import com.lukemango.plotmines.manager.impl.CreationResult;
import com.lukemango.plotmines.manager.impl.Mine;
import com.plotsquared.core.PlotAPI;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class LocationUtil {

    /**
     * Check if a location is within a mine
     *
     * @param location The location to check
     * @return The mine if the location is within one, otherwise null
     */
    public static @Nullable Mine isLocationInMine(Location location) {
        // Get a list of the mines in the same world as the location
        final List<Mine> mineList = new ArrayList<>();
        MineManager.getMines().stream().filter(mine -> mine.getMaximum().world().equals(location.getWorld().getName())).forEach(mineList::add);
        if (MineManager.getMines().isEmpty()) return null;

        // Check if the location is within a mine
        for (Mine mine : mineList) {
            double x = Math.floor(location.getX());
            double y = Math.floor(location.getY());
            double z = Math.floor(location.getZ());
            if (x >= mine.getMinimum().x() && x <= mine.getMaximum().x()) {
                if (y>= mine.getMinimum().y() && y <= mine.getMaximum().y()) {
                    if (z >= mine.getMinimum().z() && z <= mine.getMaximum().z()) {
                        return mine;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Check if a location is within a PlotSquared plot
     *
     * @param player   The player to check
     * @param location The location to check
     * @return True if the location is within a plot, otherwise false
     */
    public static CreationResult isLocationInAnyPlot(Player player, Location location) {
        final PlotAPI api = new PlotAPI();
        final com.plotsquared.core.location.Location loc = com.plotsquared.core.location.Location.at(location.getWorld().getName(),
                (int) Math.floor(location.getX()),
                (int) Math.floor(location.getY()),
                (int) Math.floor(location.getZ()),
                0,
                0);

        final PlotArea plotArea = api.getPlotSquared().getPlotAreaManager().getPlotArea(loc);
        if (plotArea == null) {
            return CreationResult.NO_PLOT_FOUND;
        }

        final Plot plot = plotArea.getPlot(loc);
        if (plot == null) {
            return CreationResult.NOT_YOUR_PLOT;
        }

        // If the config is set to only create on your own plot, check if the player is the owner, else check if the player is added or the owner
        if (ConfigManager.get().getConfig().getOnlyCreateOnOwnPlot()) {
            return plot.isOwner(player.getUniqueId()) ? CreationResult.SUCCESS : CreationResult.NOT_YOUR_PLOT;
        } else {
            return plot.isAdded(player.getUniqueId()) || plot.isOwner(player.getUniqueId()) ? CreationResult.SUCCESS : CreationResult.NOT_YOUR_PLOT;
        }
    }

    public static boolean isLocationInPlot(Location location, Plot plot) {
        final com.plotsquared.core.location.Location loc = com.plotsquared.core.location.Location.at(location.getWorld().getName(),
                (int) Math.floor(location.getX()),
                (int) Math.floor(location.getY()),
                (int) Math.floor(location.getZ()),
                0,
                0);

        final PlotArea plotArea = PlotMines.getInstance().getPlotAPI().getPlotSquared().getPlotAreaManager().getPlotArea(loc);
        if (plotArea == null) {
            return false;
        }

        if (plot == null || plotArea.getPlot(loc) == null) {
            return false;
        }

        return plotArea.getPlot(loc).equals(plot);
    }

    public static CreationResult checkMineBoundary(Player player, Location createdAt, int width, int depth) {
        width = width + 1; // Add 1 to get the correct width

        final Location max = createdAt.clone();
        final Location min = createdAt.clone().add(-width, -depth, -width);

        final CreationResult resultMin = isLocationInAnyPlot(player, min);
        final CreationResult resultMax = isLocationInAnyPlot(player, max);

        // Is the min Y level below -63?
        if (min.getBlockY() < -63) {
            return CreationResult.NOT_YOUR_PLOT;
        }

        if (resultMin != CreationResult.SUCCESS) {
            return resultMin;
        }

        return resultMax;
    }

    /**
     * Turns LocationUtil#getBorderLocationFromCreationPoint into a list of locations for the outline
     *
     * @param location The location of the mine
     * @param width    The width of the mine
     * @return The outline of the mine
     */
    public static List<Location> getOutline(Location location, int width) {
        final List<Location> outline = new ArrayList<>();

        final Location max = LocationUtil.getBorderLocationFromCreationPoint(location, width, width)[0][1]; // Get the max location (top right corner)
        final Location min = LocationUtil.getBorderLocationFromCreationPoint(location, width, width)[0][0]; // Get the min location (bottom-left corner)

        // Get the outline of the top layer
        for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
            outline.add(new Location(location.getWorld(), x, location.getBlockY(), min.getBlockZ()));
            outline.add(new Location(location.getWorld(), x, location.getBlockY(), max.getBlockZ()));
        }

        for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
            outline.add(new Location(location.getWorld(), min.getBlockX(), location.getBlockY(), z));
            outline.add(new Location(location.getWorld(), max.getBlockX(), location.getBlockY(), z));
        }

        return outline;
    }

    /**
     * Get the mine max and min locations from the creation point
     *
     * @param location The location where the mine was created
     * @param width    The width of the mine
     * @param depth    The depth of the mine
     * @return The max and min locations of the mine
     */
    public static Location[][] getMineLocationFromCreationPoint(Location location, int width, int depth) {
        depth = depth - 1; // Subtract 1 to get the correct depth

        final Location maxLocation = location.clone().add(-1, 0, -1); // Mine starts 1 block back (to the left and back) for the border
        final Location minLocation = location.clone().add(-width, -depth, -width);

        return new Location[][]{
                {minLocation, maxLocation}
        };
    }

    /**
     * Get the display border locations from the creation point
     * Has only 1 Y level
     *
     * @param location The location where the mine was created
     * @param width    The width of the mine
     * @param depth    The depth of the mine
     * @return The max and min locations of the border
     */
    public static Location[][] getBorderLocationFromCreationPoint(Location location, int width, int depth) {
        width = width + 1; // Add 1 to get the correct width
        depth = depth + 1; // Add 1 to get the correct depth

        final Location maxLocation = location.clone();
        final Location minLocation = location.clone().add(-width, 0, -depth);

        return new Location[][]{
                {minLocation, maxLocation}
        };
    }

    public static Mine getMineInteractionBlockFromLocation(Player player, Location location) {
        final FinePosition finePosition = new FinePosition(
                location.getBlockX(),
                location.getBlockY() + 1,
                location.getBlockZ() ,
                location.getWorld().getName()
        );

        return MineManager.getMines().stream()
                .filter(mine -> mine.getOwner().equals(player.getUniqueId())
                        || player.hasPermission("mangoplotmines.admin")) // Check if the player is the owner or has the admin permission
                .filter(mine -> Math.floor(mine.getResetTeleportLocation().x()) == Math.floor(finePosition.x()))
                .filter(mine -> Math.floor(mine.getResetTeleportLocation().y()) == Math.floor(finePosition.y()))
                .filter(mine -> Math.floor(mine.getResetTeleportLocation().z()) == Math.floor(finePosition.z()))
                .filter(mine -> mine.getResetTeleportLocation().world().equals(finePosition.world()))
                .findFirst()
                .orElse(null);
    }
}
