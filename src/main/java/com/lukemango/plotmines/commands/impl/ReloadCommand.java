package com.lukemango.plotmines.commands.impl;

import com.lukemango.plotmines.PlotMines;
import com.lukemango.plotmines.config.ConfigManager;
import com.lukemango.plotmines.listener.PlayerInteractListener;
import com.lukemango.plotmines.storage.JsonMineStorage;
import com.lukemango.plotmines.storage.JsonMinesToGiveStorage;
import com.lukemango.plotmines.util.Colourify;
import com.sk89q.worldedit.command.util.CommandPermissions;
import net.kyori.adventure.audience.Audience;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.annotations.Command;

public class ReloadCommand extends AbstractCommand {

    @Command("plotmines|plotmine|pmine|pm admin reload")
    @CommandPermissions("mangoplotmines.admin")
    public void onReload(CommandSender sender) {
        ConfigManager.get().reload(); // Reload the config
        JsonMineStorage.get().load(); // Reload the mines
        JsonMinesToGiveStorage.get().load(); // Reload the mines to give back
        PlayerInteractListener.updateDuration(); // Update the preview duration

        // Reload the holograms if they are enabled
        if (ConfigManager.get().getConfig().areHologramsEnabled()) {
            PlotMines.getInstance().getHolograms().reloadHolograms();
        }

        final Audience senderAudience = PlotMines.getInstance().getAdventure().sender(sender);
        senderAudience.sendMessage(Colourify.colour(ConfigManager.get().getMessages().getAdminReloaded()));
    }

}
