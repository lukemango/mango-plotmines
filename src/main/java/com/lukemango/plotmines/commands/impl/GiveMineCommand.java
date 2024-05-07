package com.lukemango.plotmines.commands.impl;

import com.lukemango.plotmines.PlotMines;
import com.lukemango.plotmines.config.ConfigManager;
import com.lukemango.plotmines.config.impl.impl.MineItem;
import com.lukemango.plotmines.util.Colourify;
import com.lukemango.plotmines.util.StringUtil;
import com.sk89q.worldedit.command.util.CommandPermissions;
import net.kyori.adventure.audience.Audience;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandContext;

import java.util.List;

public class GiveMineCommand extends AbstractCommand {

    @Command("plotmines|plotmine|pmine|pm admin give <target> <mine>")
    @CommandPermissions("mangoplotmines.admin")
    private void onGive(CommandSender sender, @Argument("target") Player target, @Argument(value = "mine", suggestions = "mine-items") String mine) {
        final MineItem mineItem = ConfigManager.get().getConfig().getMineItem(sender, mine);
        if (mineItem == null) return; // If the mine item is null, the mine does not exist

        final Audience senderAudience = PlotMines.getInstance().getAdventure().sender(sender);
        final Audience targetAudience = PlotMines.getInstance().getAdventure().player(target);

        // Check if the player has an empty inventory slot
        if (target.getInventory().firstEmpty() == -1) {
            senderAudience.sendMessage(Colourify.colour(ConfigManager.get().getMessages().getFullInventory()));
            return;
        }

        // Give the player the mine item
        target.getInventory().addItem(mineItem.creationItem());

        // Send the messages to the player and the sender
        targetAudience.sendMessage(Colourify.colour(ConfigManager.get().getMessages().getPlayerReceivedMine()
                .replace("<mine>", StringUtil.formatString(mine)))
        );
        senderAudience.sendMessage(Colourify.colour(ConfigManager.get().getMessages().getAdminMineGiven()
                .replace("<player>", target.getName())
                .replace("<mine>", StringUtil.formatString(mine)))
        );
    }

    @Suggestions("mine-items")
    public List<String> suggest(final CommandContext<CommandSender> commandContext, final String input) {
        return ConfigManager.get().getConfig().getMines();
    }

}
