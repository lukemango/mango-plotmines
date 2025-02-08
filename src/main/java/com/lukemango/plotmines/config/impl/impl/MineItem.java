package com.lukemango.plotmines.config.impl.impl;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public record MineItem(String name, int width, int depth, double resetPercent, int resetDelay, Material border, Material interactionBlock, ItemStack creationItem, Map<Material, Double> composition) {
}
