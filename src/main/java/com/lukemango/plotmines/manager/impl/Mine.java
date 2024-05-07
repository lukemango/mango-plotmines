package com.lukemango.plotmines.manager.impl;

import com.lukemango.plotmines.PlotMines;
import com.lukemango.plotmines.config.ConfigManager;
import com.lukemango.plotmines.util.Colourify;
import com.lukemango.plotmines.util.FinePosition;
import com.lukemango.plotmines.util.LocationUtil;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.pattern.RandomPattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class Mine {

    private UUID uuid;
    private UUID owner;
    private String mineType;
    private String displayName;
    private FinePosition minimum;
    private FinePosition maximum;
    private double resetPercentage;
    private FinePosition resetTeleportLocation; // Also used as the interaction block
    private Map<Material, Double> composition;
    private int totalBlocks;

    private transient boolean isResetting = false;
    private transient int blocksRemaining;

    public Mine(
            UUID owner,
            String mineType,
            String displayName,
            FinePosition minimum,
            FinePosition maximum,
            double resetPercentage,
            FinePosition resetTeleportLocation,
            Map<Material, Double> composition,
            int totalBlocks
    ) {
        this.uuid = UUID.randomUUID();
        this.owner = owner;
        this.mineType = mineType;
        this.displayName = displayName;
        this.minimum = minimum;
        this.maximum = maximum;
        this.resetPercentage = resetPercentage;
        this.resetTeleportLocation = resetTeleportLocation;
        this.composition = composition;
        this.totalBlocks = totalBlocks;
    }

    public void decrementBlocksRemaining() {
        blocksRemaining--;

        // Reset Percentage check
        double minedPercentage = (double) (totalBlocks - blocksRemaining) / totalBlocks * 100;
        if (minedPercentage >= resetPercentage) {
            if (this.isResetting) return;
            this.isResetting = true;
            Bukkit.getScheduler().runTaskLaterAsynchronously(PlotMines.getInstance(), this::reset, 20); // 1 second delay
        }
    }

    public void reset() {
        // Is the chunk loaded?
        if (!this.minimum.toLocation().getChunk().isLoaded() || !this.maximum.toLocation().getChunk().isLoaded()) {
            this.isResetting = false;
            return;
        }

        // Teleport any players in the mine to the reset location
        for (final Player player : Bukkit.getOnlinePlayers()) {
            final Mine mine = LocationUtil.isLocationInMine(player.getLocation());
            if (mine == null || !mine.getUuid().equals(this.uuid)) continue;

            Bukkit.getScheduler().runTask(PlotMines.getInstance(), () -> player.teleport(this.resetTeleportLocation.toLocation()));

            final Audience playerAudience = PlotMines.getInstance().getAdventure().player(player);
            playerAudience.sendMessage(Colourify.colour(ConfigManager.get().getMessages().getPlayerMineResetTeleportedOut()));
        }

        // WorldEdit Section
        this.setBlocks(this.composition, false);

        // Reset blocks remaining
        this.resetBlocksRemaining();
        this.isResetting = false;
    }

    /**
     * Set the blocks of the mine
     *
     * @param composition  Blocks to set
     * @param expand1Block Expand one block or not (for Mine border)
     */
    public void setBlocks(Map<Material, Double> composition, boolean expand1Block) {
        // Reset the blocks
        final World world = Bukkit.getWorld(this.minimum.world());
        if (world == null) {
            return;
        }

        // Prepare WorldEdit
        final com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(world);
        final EditSession editSession = WorldEdit.getInstance().newEditSession(weWorld);

        Region region = getBlockVector3s(expand1Block, weWorld);

        // Add the blocks to the pattern
        final RandomPattern randomPattern = new RandomPattern();
        if (composition.isEmpty()) {
            return;
        }
        for (Map.Entry<Material, Double> entry : composition.entrySet()) {
            BlockType blockType = BlockTypes.parse(entry.getKey().name());
            BlockState blockState = blockType.getDefaultState();
            randomPattern.add(blockState, entry.getValue() * 100);
        }

        // Set the blocks
        editSession.setBlocks(region, randomPattern);
        editSession.close();
    }

    /**
     * Returns the region of the mine with or without expanding 1 block (for Mine border)
     *
     * @param expand1Block Expand one block or not
     * @param weWorld      WorldEdit World
     * @return Region
     */
    private Region getBlockVector3s(boolean expand1Block, com.sk89q.worldedit.world.World weWorld) {
        Region region;
        if (!expand1Block) {
            region = new CuboidRegion(
                    weWorld,
                    BlockVector3.at(this.minimum.x(), this.minimum.y(), this.minimum.z()),
                    BlockVector3.at(this.maximum.x(), this.maximum.y(), this.maximum.z())
            );
        } else {
            region = new CuboidRegion(
                    weWorld,
                    BlockVector3.at(this.minimum.x() - 1, this.minimum.y() - 1, this.minimum.z() - 1),
                    BlockVector3.at(this.maximum.x() + 1, this.maximum.y(), this.maximum.z() + 1)
            );
        }
        return region;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public String getMineType() {
        return mineType;
    }

    public void setMineType(String mineType) {
        this.mineType = mineType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public FinePosition getMinimum() {
        return minimum;
    }

    public void setMinimum(FinePosition minimum) {
        this.minimum = minimum;
    }

    public FinePosition getMaximum() {
        return maximum;
    }

    public void setMaximum(FinePosition maximum) {
        this.maximum = maximum;
    }

    public double getResetPercentage() {
        return resetPercentage;
    }

    public void setResetPercentage(double resetPercentage) {
        this.resetPercentage = resetPercentage;
    }

    public FinePosition getResetTeleportLocation() {
        return resetTeleportLocation;
    }

    public void setResetTeleportLocation(FinePosition resetTeleportLocation) {
        this.resetTeleportLocation = resetTeleportLocation;
    }

    public Map<Material, Double> getComposition() {
        return composition;
    }

    public void setComposition(Map<Material, Double> composition) {
        this.composition = composition;
    }

    public int getTotalBlocks() {
        return totalBlocks;
    }

    public void setTotalBlocks(int totalBlocks) {
        this.totalBlocks = totalBlocks;
    }

    public int getBlocksRemaining() {
        return blocksRemaining;
    }

    public void setBlocksRemaining(int blocksRemaining) {
        this.blocksRemaining = blocksRemaining;
    }

    public void resetBlocksRemaining() {
        blocksRemaining = totalBlocks;
    }

}