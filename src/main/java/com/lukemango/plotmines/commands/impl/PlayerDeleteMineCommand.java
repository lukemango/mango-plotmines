package com.lukemango.plotmines.commands.impl;

import com.lukemango.plotmines.PlotMines;
import com.lukemango.plotmines.config.ConfigManager;
import com.lukemango.plotmines.manager.MineManager;
import com.lukemango.plotmines.manager.impl.Mine;
import com.lukemango.plotmines.util.Colourify;
import com.sk89q.worldedit.command.util.CommandPermissions;
import net.kyori.adventure.audience.Audience;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotation.specifier.Greedy;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandContext;

import java.util.List;

public class PlayerDeleteMineCommand extends AbstractCommand {

    @Command("plotmines|plotmine|pmine|pm delete|remove <mine>")
    private void onRemove(CommandSender sender, @Argument(value = "mine", suggestions = "my-mines") @Greedy String mine) {
        final Mine mineObject = MineManager.getMines().stream()
                .filter(mineObject1 -> mineObject1.getOwner().equals(((Player) sender).getUniqueId()))
                .filter(mineObject1 -> mineObject1.getDisplayName().equals(mine))
                .findFirst()
                .orElse(null);

        final Audience senderAudience = PlotMines.getInstance().getAdventure().sender(sender);

        if (mineObject == null) {
            senderAudience.sendMessage(Colourify.colour(ConfigManager.get().getMessages().getAdminMineNotFound()
                    .replace("<mine>", mine)));
            return;
        }

        MineManager.deleteMine(mineObject);

        senderAudience.sendMessage(Colourify.colour(ConfigManager.get().getMessages().getPlayerDeletedMine()
                .replace("<mine>", mine))
        );
    }

    @Suggestions("my-mines")
    public List<String> suggestPlayerMines(final CommandContext<CommandSender> commandContext, final String input) {
        final Player sender = (Player) commandContext.sender();

        return MineManager.getMines().stream()
                .filter(mine -> mine.getOwner().equals(sender.getUniqueId()))
                .map(Mine::getDisplayName)
                .toList();
    }
}
