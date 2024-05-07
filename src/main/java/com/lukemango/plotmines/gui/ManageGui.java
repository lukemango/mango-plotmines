package com.lukemango.plotmines.gui;

import com.lukemango.plotmines.PlotMines;
import com.lukemango.plotmines.config.ConfigManager;
import com.lukemango.plotmines.config.impl.impl.MineItem;
import com.lukemango.plotmines.manager.MineManager;
import com.lukemango.plotmines.manager.impl.ChangeDisplayNameHandler;
import com.lukemango.plotmines.manager.impl.Mine;
import com.lukemango.plotmines.util.Colourify;
import com.lukemango.plotmines.util.StringUtil;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

public class ManageGui {

    private final Gui gui = Gui.gui()
            .title(Component.text(""))
            .rows(ConfigManager.get().getManageGuiConfig().getRows())
            .create();

    public ManageGui(Mine mine) {
        // Set the title
        gui.updateTitle(LegacyComponentSerializer.legacySection().serialize(
                Colourify.colour(
                        ConfigManager.get().getManageGuiConfig().getTitle()
                                .replace("<mine>", mine.getDisplayName())
                )));

        // Add the decoration items
        ConfigManager.get().getManageGuiConfig().getItems().forEach((item, slots) -> {
            final GuiItem guiItem = new GuiItem(item);
            slots.forEach(slot -> gui.setItem(slot, guiItem));
        });

        // Add the buttons
        ConfigManager.get().getManageGuiConfig().getButtons().forEach((key, buttons) ->
                buttons.forEach((item, slot) -> {
                    final GuiItem guiItem = new GuiItem(item);

                    guiItem.setAction(event -> {
                        final Player player = (Player) event.getWhoClicked();
                        final Audience playerAudience = PlotMines.getInstance().getAdventure().player(player);

                        // Handle the reset button
                        if (key.equalsIgnoreCase("reset")) {
                            player.closeInventory();
                            mine.reset();
                            playerAudience.sendMessage(Colourify.colour(ConfigManager.get().getMessages().getPlayerResetMine()
                                    .replace("<mine>", mine.getDisplayName())));
                            return;
                        }

                        // Handle the remove button
                        if (key.equalsIgnoreCase("remove")) {
                            player.closeInventory();

                            final MineItem mineItem = ConfigManager.get().getConfig().getMineItem(player, mine.getMineType());
                            MineManager.deleteMine(mine.getUuid(), player);

                            if (player.getInventory().firstEmpty() == -1) {
                                player.getLocation().getWorld().dropItem(player.getLocation(), mineItem.creationItem());
                                playerAudience.sendMessage(Colourify.colour(ConfigManager.get().getMessages().getPlayerReceivedFullInventory()
                                        .replace("<mine>", StringUtil.formatString(mine.getMineType()))));
                                return;
                            }

                            player.getInventory().addItem(mineItem.creationItem());
                            playerAudience.sendMessage(Colourify.colour(ConfigManager.get().getMessages().getPlayerReceivedMine()
                                    .replace("<mine>", StringUtil.formatString(mine.getMineType())))
                            );
                            return;
                        }

                        // Handle the set display name button
                        if (key.equalsIgnoreCase("set_display_name")) {
                            player.closeInventory();
                            ChangeDisplayNameHandler.setChangingDisplayName(player.getUniqueId(), mine);
                            playerAudience.sendMessage(Colourify.colour(ConfigManager.get().getMessages().getPlayerDisplayNameChangeInitiated()));
                        }
                    });
                    gui.setItem(slot, guiItem);
                })
        );
    }

    public static void open(Player player, Mine mine) {
        new ManageGui(mine).gui.open(player);
    }
}
