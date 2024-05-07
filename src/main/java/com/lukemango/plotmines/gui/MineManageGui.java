package com.lukemango.plotmines.gui;

import com.lukemango.plotmines.PlotMines;
import com.lukemango.plotmines.config.ConfigManager;
import com.lukemango.plotmines.config.impl.impl.MineItem;
import com.lukemango.plotmines.manager.MineManager;
import com.lukemango.plotmines.manager.impl.ChangeDisplayNameHandler;
import com.lukemango.plotmines.manager.impl.Mine;
import com.lukemango.plotmines.util.Colourify;
import com.lukemango.plotmines.util.StringUtil;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.SlotPos;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MineManageGui implements InventoryProvider {

    private static final String ID = "mangoplotmines:manage";
    private final Map<ClickableItem, List<Integer>> clickables = new HashMap<>();

    public MineManageGui(Player player, Mine mine) {
        ConfigManager.get().getManageGuiConfig().getItems().forEach((item, slots) -> {
            final ClickableItem clickable = ClickableItem.empty(item);
            clickables.put(clickable, slots);
        });

        ConfigManager.get().getManageGuiConfig().getButtons().forEach((key, buttons) ->
                buttons.forEach((item, slot) -> {
                    final ClickableItem clickable = ClickableItem.of(item, e -> {
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
                    clickables.put(clickable, List.of(slot));
                })
        );
    }

    public static void open(Player player, Mine mine) {
        SmartInventory inventory = SmartInventory.builder()
                .id(ID)
                .manager(InventoryManager.get())
                .provider(new MineManageGui(player, mine))
                .size(ConfigManager.get().getManageGuiConfig().getRows(), 9)
                .title(LegacyComponentSerializer.legacySection().serialize(
                        Colourify.colour(
                                ConfigManager.get().getManageGuiConfig().getTitle()
                                        .replace("<mine>", mine.getDisplayName())
                        )))
                .build();

        inventory.open(player);
    }

    @Override
    public void init(Player player, InventoryContents inventoryContents) {
        for (Map.Entry<ClickableItem, List<Integer>> entry : clickables.entrySet()) {
            for (int slot : entry.getValue()) {
                inventoryContents.set(SlotPos.of(slot / 9, slot % 9), entry.getKey());
            }
        }
    }

    @Override // This method is not used in the plugin
    public void update(Player player, InventoryContents inventoryContents) {

    }
}
