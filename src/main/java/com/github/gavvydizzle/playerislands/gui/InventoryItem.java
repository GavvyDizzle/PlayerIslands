package com.github.gavvydizzle.playerislands.gui;

import com.github.mittenmc.serverutils.Colors;
import com.github.mittenmc.serverutils.ConfigUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class InventoryItem {

    private final ItemStack itemStack;
    private final int slot;

    public InventoryItem(FileConfiguration config, String prefix) {
        this.slot = config.getInt(prefix + ".slot");
        itemStack = new ItemStack(ConfigUtils.getMaterial(config.getString(prefix + ".material")));
        ItemMeta meta = itemStack.getItemMeta();
        assert meta != null;
        meta.setDisplayName(Colors.conv(config.getString(prefix + ".name")));
        meta.setLore(Colors.conv(config.getStringList(prefix + ".lore")));
        if (config.getBoolean(prefix + ".glow")) {
            meta.addEnchant(Enchantment.WATER_WORKER, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        itemStack.setItemMeta(meta);
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public int getSlot() {
        return slot;
    }

    /**
     * Creates a new entry in the config file for this prefix with default values
     * @param config The configuration file
     * @param prefix The prefix in the config file
     */
    public static void addDefaults(FileConfiguration config, String prefix) {
        config.addDefault(prefix + ".slot", -1);
        config.addDefault(prefix + ".material", "DIRT");
        config.addDefault(prefix + ".name", "&eName");
        config.addDefault(prefix + ".lore", new ArrayList<>());
        config.addDefault(prefix + ".glow", false);
    }

    /**
     * Creates a new entry in the config file for this prefix with specified values
     * @param config The configuration file
     * @param prefix The prefix in the config file
     * @param slot The slot
     * @param material The material
     * @param name The name
     * @param lore The lore
     * @param glow If to add enchant glint
     */
    public static void addDefaults(FileConfiguration config, String prefix, int slot, Material material, String name, List<String> lore, boolean glow) {
        config.addDefault(prefix + ".slot", slot);
        config.addDefault(prefix + ".material", material.toString().toUpperCase());
        config.addDefault(prefix + ".name", name);
        config.addDefault(prefix + ".lore", lore);
        config.addDefault(prefix + ".glow", glow);
    }
}
