package com.lukemango.plotmines.commands.impl;

import com.lukemango.plotmines.PlotMines;
import com.lukemango.plotmines.config.ConfigManager;
import com.lukemango.plotmines.storage.JsonStorageManager;
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
        JsonStorageManager.load(); // Reload the mines

        // Reload the holograms if they are enabled
        if (ConfigManager.get().getConfig().areHologramsEnabled()) {
            PlotMines.getInstance().getHolograms().reloadHolograms();
        }

        final Audience senderAudience = PlotMines.getInstance().getAdventure().sender(sender);
        senderAudience.sendMessage(Colourify.colour(ConfigManager.get().getMessages().getAdminReloaded()));
    }

}
