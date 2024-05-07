package com.lukemango.plotmines.commands.impl;

import com.lukemango.plotmines.PlotMines;
import com.lukemango.plotmines.config.ConfigManager;
import com.lukemango.plotmines.manager.MineManager;
import com.lukemango.plotmines.manager.impl.Mine;
import com.lukemango.plotmines.util.Colourify;
import com.sk89q.worldedit.command.util.CommandPermissions;
import net.kyori.adventure.audience.Audience;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotation.specifier.Greedy;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandContext;

import java.util.List;

public class DeleteMineCommand extends AbstractCommand {

    // TODO: Don't require the player to be online to delete a mine
    @Command("plotmines|plotmine|pmine|pm admin remove <target> <mine>")
    @CommandPermissions("mangoplotmines.admin")
    private void onRemove(CommandSender sender, @Argument("target") Player target, @Argument(value = "mine", suggestions = "players-mines") @Greedy String mine) {
        final Mine mineObject = MineManager.getMines().stream()
                .filter(mineObject1 -> mineObject1.getDisplayName().equals(mine))
                .findFirst()
                .orElse(null);

        final Audience senderAudience = PlotMines.getInstance().getAdventure().sender(sender);
        final Audience targetAudience = PlotMines.getInstance().getAdventure().player(target);

        if (mineObject == null) {
            senderAudience.sendMessage(Colourify.colour(ConfigManager.get().getMessages().getAdminMineNotFound()
                    .replace("<mine>", mine)));
            return;
        }

        MineManager.deleteMine(mineObject.getUuid(), target);

        senderAudience.sendMessage(Colourify.colour(ConfigManager.get().getMessages().getAdminDeletedMine()
                .replace("<player>", target.getName())
                .replace("<mine>", mine))
        );
        targetAudience.sendMessage(Colourify.colour(ConfigManager.get().getMessages().getPlayerDeletedByAdmin()
                .replace("<mine>", mine))
        );
    }

    @Suggestions("players-mines")
    public List<String> suggest(final CommandContext<CommandSender> commandContext, final String input) {
        final Player target = commandContext.get("target");

        return MineManager.getMines().stream()
                .filter(mine -> mine.getOwner().equals(target.getUniqueId()))
                .map(Mine::getDisplayName)
                .toList();
    }
}
