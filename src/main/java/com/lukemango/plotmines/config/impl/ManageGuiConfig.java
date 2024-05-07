package com.lukemango.plotmines.config.impl;

import com.lukemango.plotmines.util.AbstractConfig;
import com.lukemango.plotmines.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageGuiConfig extends AbstractConfig {

    public ManageGuiConfig(String path) {
        super(path, "manage-gui.yml");
    }

    public int getRows() {
        return getYamlConfiguration().getInt("settings.rows");
    }

    public String getTitle() {
        return getYamlConfiguration().getString("settings.title");
    }

    public Map<String, Map<ItemStack, Integer>> getButtons() {
        final Map<String, Map<ItemStack, Integer>> buttons = new HashMap<>();
        final ConfigurationSection section = getYamlConfiguration().getConfigurationSection("buttons");

        for (String key : section.getKeys(false)) {
            ItemStack item = new ItemBuilder(Material.valueOf(section.getString(key + ".material")))
                    .name(section.getString(key + ".name"))
                    .lore(section.getStringList(key + ".lore"))
                    .model(section.getInt(key + ".custom-model-data"))
                    .build();

            buttons.put(key, Map.of(item, section.getInt(key + ".slot")));
        }

        return buttons;
    }

    public Map<ItemStack, List<Integer>> getItems() {
        final Map<ItemStack, List<Integer>> items = new HashMap<>();
        final ConfigurationSection section = getYamlConfiguration().getConfigurationSection("design");

        for (String key : section.getKeys(false)) {
            ItemStack item = new ItemBuilder(Material.valueOf(section.getString(key + ".material")))
                    .name(section.getString(key + ".name"))
                    .lore(section.getStringList(key + ".lore"))
                    .model(section.getInt(key + ".custom-model-data"))
                    .build();

            items.put(item, section.getIntegerList(key + ".slots"));
        }

        return items;
    }
}
