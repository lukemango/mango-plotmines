package com.lukemango.plotmines.config.impl;

import com.lukemango.plotmines.PlotMines;
import com.lukemango.plotmines.config.ConfigManager;
import com.lukemango.plotmines.config.impl.impl.MineItem;
import com.lukemango.plotmines.util.AbstractConfig;
import com.lukemango.plotmines.util.Colourify;
import com.lukemango.plotmines.util.ItemBuilder;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Config extends AbstractConfig {

    private static final NamespacedKey mineKey = new NamespacedKey(PlotMines.getInstance(), "mine-item");

    public Config(String path) {
        super(path, "config.yml");
    }

    /**
     * Get the preview material from the config
     * @param sender The command sender used for error messages
     * @return The preview material
     */
    public Material getPreviewBlock(CommandSender sender) {
        if (!this.checkMaterial(sender, getYamlConfiguration().getString("settings.preview-block"), "settings.preview-block")) {
            return Material.STONE;
        }
        return Material.valueOf(getYamlConfiguration().getString("settings.preview-block").toUpperCase());
    }

    public Material getPreviewClickBlock(CommandSender sender) {
        if (!this.checkMaterial(sender, getYamlConfiguration().getString("settings.preview-click-block"), "settings.preview-click-block")) {
            return Material.STONE;
        }
        return Material.valueOf(getYamlConfiguration().getString("settings.preview-click-block").toUpperCase());
    }

    public int getPreviewBlockDuration() {
        return getYamlConfiguration().getInt("settings.preview-block-duration");
    }

    public int getMaxDisplayNameLength() {
        return getYamlConfiguration().getInt("settings.max-display-name-length");
    }

    public boolean getOnlyCreateOnOwnPlot() {
        return getYamlConfiguration().getBoolean("settings.only-create-on-own-plot");
    }

    public int getHologramUpdateInterval() {
        return getYamlConfiguration().getInt("holograms.update-interval");
    }

    public boolean areHologramsEnabled() {
        return getYamlConfiguration().getBoolean("holograms.enabled");
    }

    public double getHologramOffset() {
        return getYamlConfiguration().getDouble("holograms.offset");
    }

    public List<String> getHologramText() {
        return getYamlConfiguration().getStringList("holograms.text");
    }

    public List<String> getMines() {
        return getYamlConfiguration().getConfigurationSection("mines").getKeys(false).stream().toList();
    }

    /**
     * Get a mine item from the config
     * @param sender The command sender used for error messages
     * @param mine The mine to get
     * @return The mine item
     */
    public MineItem getMineItem(CommandSender sender, String mine) {
        final Audience senderAudience = PlotMines.getInstance().getAdventure().sender(sender);
        final ConfigurationSection section = getYamlConfiguration().getConfigurationSection("mines." + mine);
        if (section == null) {
            senderAudience.sendMessage(Colourify.colour(
                    ConfigManager.get().getMessages().getAdminMineNotFound()
                            .replace("<mine>", mine)));
            return null;
        }

        // User error handling for materials
        if (!this.checkMaterial(sender, section.getString("border"), "mines." + mine + ".border") ||
                !this.checkMaterial(sender, section.getString("interaction-block"), "mines." + mine + ".interaction-block") ||
                !this.checkMaterial(sender, section.getString("creation_item.material"), "mines." + mine + ".creation_item.material")) {
            senderAudience.sendMessage(Colourify.colour(ConfigManager.get().getMessages().getAdminErrorOccurred()));
            return null;
        }

        // Get the mine item values
        final int width = section.getInt("width");
        final int depth = section.getInt("depth");
        final double resetPercent = section.getDouble("reset-percent");
        final int resetDelay = section.getInt("reset-delay");
        final Material border = Material.valueOf(section.getString("border").toUpperCase());
        final Material interactionBlock = Material.valueOf(section.getString("interaction-block").toUpperCase());

        // Create the creation item stack
        final ItemStack creationItem = new ItemBuilder(Material.valueOf(section.getString("creation_item.material").toUpperCase()))
                .name(section.getString("creation_item.display-name"))
                .lore(section.getStringList("creation_item.lore"))
                .model(section.getInt("creation_item.custom-model-data"))
                .glow(section.getBoolean("creation_item.glow"))
                .build();
        final ItemMeta meta = creationItem.getItemMeta();
        meta.getPersistentDataContainer().set(mineKey, PersistentDataType.STRING, mine);
        creationItem.setItemMeta(meta);

        // Get the composition values
        final Map<Material, Double> composition = new HashMap<>();
        for (String key : section.getConfigurationSection("composition").getKeys(false)) {
            composition.put(Material.valueOf(key.toUpperCase()), section.getDouble("composition." + key));
        }

        // Create and return the mine item object
        return new MineItem(mine, width, depth, resetPercent, resetDelay, border, interactionBlock, creationItem, composition);
    }

    /**
     * Check if the material is valid
     * @param sender The command sender used for error messages
     * @param value The value to check
     * @param path The path in the config
     * @return If the material is valid
     */
    private boolean checkMaterial(CommandSender sender, @Nullable String value, String path) {
        if (value == null) {
            this.sendMaterialError(sender, "null", path);
            return false;
        }

        value = value.toUpperCase();

        try {
            Material.valueOf(value);
            return true;
        } catch (Exception e) {
            this.sendMaterialError(sender, value, path);
            return false;
        }
    }

    private void sendMaterialError(CommandSender sender, String value, String path) {
        final Audience senderAudience = PlotMines.getInstance().getAdventure().sender(sender);
        senderAudience.sendMessage(Colourify.colour(
                        ConfigManager.get().getMessages().getAdminMaterialConfigError()
                                .replace("<value>", value)
                                .replace("<path>", path)
                )
        );
        PlotMines.getInstance().getLogger().warning("Invalid material in the config: " + value + " at path: " + path);
    }

    public static NamespacedKey getMineKey() {
        return mineKey;
    }
}
