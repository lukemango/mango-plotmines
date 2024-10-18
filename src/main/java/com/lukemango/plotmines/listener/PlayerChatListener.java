package com.lukemango.plotmines.listener;

import com.lukemango.plotmines.PlotMines;
import com.lukemango.plotmines.config.ConfigManager;
import com.lukemango.plotmines.manager.impl.ChangeDisplayNameHandler;
import com.lukemango.plotmines.util.Colourify;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PlayerChatListener implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onChatEvent(AsyncPlayerChatEvent event) {
        final Player player = event.getPlayer();
        if (!ChangeDisplayNameHandler.isChangingDisplayName(player.getUniqueId())) return;

        event.setCancelled(true);

        int messageLength = event.getMessage().length();
        String message = ChatColor.stripColor(event.getMessage());
        final Audience playerAudience = PlotMines.getInstance().getAdventure().player(player);
        if (messageLength > ConfigManager.get().getConfig().getMaxDisplayNameLength()) {
            playerAudience.sendMessage(Colourify.colour(ConfigManager.get().getMessages().getPlayerDisplayNameTooLong()));
            return;
        }

        ChangeDisplayNameHandler.getChangingDisplayName(player.getUniqueId()).setDisplayName(message);
        ChangeDisplayNameHandler.removeChangingDisplayName(player.getUniqueId());

        playerAudience.sendMessage(Colourify.colour(ConfigManager.get().getMessages().getPlayerDisplayNameChanged()
                .replace("<name>", message)));
    }

}
