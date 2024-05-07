package com.lukemango.plotmines.listener;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.lukemango.plotmines.PlotMines;
import com.lukemango.plotmines.config.ConfigManager;
import com.lukemango.plotmines.config.impl.Config;
import com.lukemango.plotmines.config.impl.impl.MineItem;
import com.lukemango.plotmines.gui.ManageGui;
import com.lukemango.plotmines.manager.impl.CreationResult;
import com.lukemango.plotmines.manager.impl.Mine;
import com.lukemango.plotmines.util.Colourify;
import com.lukemango.plotmines.util.LocationUtil;
import com.lukemango.plotmines.util.StringUtil;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class OnPlayerInteract implements Listener {

    private final Cache<Player, Block> mineItemCache = CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.SECONDS)
            .build();

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final ItemStack item = player.getInventory().getItemInMainHand();
        final Audience playerAudience = PlotMines.getInstance().getAdventure().player(player);

        // Only check for the main hand
        if (event.getHand() != EquipmentSlot.HAND) return;

        // Did the player click a block?
        if (event.getClickedBlock() == null) return;

        // Check if the player is trying to manage a mine
        final Block clickedBlock = event.getClickedBlock();
        final Mine manageMine = LocationUtil.getMineInteractionBlockFromLocation(player, clickedBlock.getLocation());
        if (LocationUtil.getMineInteractionBlockFromLocation(player, clickedBlock.getLocation()) != null) {
            event.setCancelled(true);
            ManageGui.open(player, manageMine);
            return;
        }

        // Check if the player is holding a mine item
        final ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        if (meta.getPersistentDataContainer().isEmpty()) return;

        // They are holding a mine item
        if (meta.getPersistentDataContainer().getKeys().contains(Config.getMineKey())) {
            this.mineCreateAttempt(event, playerAudience, player, meta, item);
        }
    }

    private void mineCreateAttempt(PlayerInteractEvent event, Audience playerAudience, Player player, ItemMeta meta, ItemStack item) {
        // It is a mine item, cancel the event
        event.setCancelled(true);

        final ConfigManager configManager = ConfigManager.get();

        // Get the mine item
        final MineItem mineItem = ConfigManager.get().getConfig().getMineItem(
                player, meta.getPersistentDataContainer().get(Config.getMineKey(), PersistentDataType.STRING));
        if (mineItem == null) return;

        // Check if the mine fits inside the plot
        if (LocationUtil.checkMineBoundary(player,
                event.getClickedBlock().getLocation(),
                mineItem.width(),
                mineItem.depth()) != CreationResult.SUCCESS) {
            playerAudience.sendMessage(Colourify.colour(configManager.getMessages().getPlayerInvalidLocation()));
            return;
        }

        // Get the outline of the mine
        final List<Location> topOutline = LocationUtil.getOutline(
                event.getClickedBlock().getLocation(),
                mineItem.width());

        // Check if there's already a mine in the outline of the top and bottom layers
        for (Location loc : topOutline) {
            if (LocationUtil.isLocationInMine(loc) != null
                    || LocationUtil.isLocationInMine(loc.clone().add(0, -mineItem.depth(), 0)) != null) {
                playerAudience.sendMessage(Colourify.colour(configManager.getMessages().getPlayerMineAlreadyThere()));
                return;
            }
        }

        // Cache the attempt, if the player doesn't click the block again within 10 seconds, the cache will expire
        final Block cacheBlock = mineItemCache.getIfPresent(player);
        if (cacheBlock == null) {
            mineItemCache.put(player, event.getClickedBlock());
        } else {
            // The player clicked the same block
            if (cacheBlock.equals(event.getClickedBlock())) {
                mineItemCache.invalidate(player);
                item.setAmount(item.getAmount() - 1);
                PlotMines.getInstance().getMineManager().createMine(cacheBlock.getLocation(), mineItem, player);
                return;
            }
            // The player clicked a different block
            playerAudience.sendMessage(Colourify.colour(ConfigManager.get().getMessages().getPlayerAlreadyHaveRequest()));
            return;
        }

        // Display the outline of the mine
        final Material previewBlock = ConfigManager.get().getConfig().getPreviewBlock(player);
        for (Location loc : topOutline) { // TODO: Replace deprecated method
            Bukkit.getScheduler().runTaskLaterAsynchronously(PlotMines.getInstance(), () ->
                    player.sendBlockChange(loc, previewBlock, (byte) 0), 1L); // Sends 1 tick later
        }

        // Bukkit Task to undo the outline after 10 seconds
        Bukkit.getScheduler().runTaskLaterAsynchronously(PlotMines.getInstance(), () -> {
            // If the player isn't in the cache, don't update the blocks
            if (mineItemCache.getIfPresent(player) == null) return;
            playerAudience.sendMessage(Colourify.colour(configManager.getMessages().getPlayerRequestTimedOut()));

            for (Location loc : topOutline) {
                player.sendBlockChange(loc, loc.getBlock().getBlockData());
            }
        }, 200L); // 10 seconds

        playerAudience.sendMessage(Colourify.colour(configManager.getMessages().getPlayerClickAgainToConfirm()
                .replace("<mine>", StringUtil.formatString(mineItem.name()))));
    }
}
