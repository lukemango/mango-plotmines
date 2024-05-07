package com.lukemango.plotmines.gui;

import org.bukkit.plugin.java.JavaPlugin;

public class InventoryManager extends fr.minuskube.inv.InventoryManager {

    private static InventoryManager instance;

    public InventoryManager(JavaPlugin plugin) {
        super(plugin);
        instance = this;

        this.init();
    }

    public static InventoryManager get() {
        return instance;
    }
}
