package com.lukemango.plotmines.listener;

import com.lukemango.plotmines.manager.impl.Mine;
import com.lukemango.plotmines.util.LocationUtil;
import com.vk2gpz.tokenenchant.event.TEBlockExplodeEvent;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class TokenEnchantListener implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockExplode(TEBlockExplodeEvent event) {
        final Block block = event.getBlock();
        final Location location = block.getLocation();

        // Check if the broken block is in a plot mine
        final Mine mine = LocationUtil.isLocationInMine(location);
        if (mine == null) {
            return;
        }

        mine.decrementBlocksRemaining();
    }

}
