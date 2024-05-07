package com.lukemango.plotmines.listener;

import com.lukemango.plotmines.config.ConfigManager;
import com.lukemango.plotmines.config.impl.impl.MineItem;
import com.lukemango.plotmines.storage.JsonMinesToGiveStorage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;

public class PlayerJoinListener implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        final List<String> minesToGive = JsonMinesToGiveStorage.get().getMinesToGiveBack(player.getUniqueId());
        if (minesToGive.isEmpty()) return;

        minesToGive.forEach(mine -> {
            MineItem mineItem = ConfigManager.get().getConfig().getMineItem(player, mine);
            if (mineItem == null) return;
            if (player.getInventory().firstEmpty() == -1) {
                player.getWorld().dropItem(player.getLocation(), mineItem.creationItem());
                return;
            }
            player.getInventory().addItem(mineItem.creationItem());
        });

        JsonMinesToGiveStorage.get().removeMinesToGiveBack(player.getUniqueId());
    }
}
