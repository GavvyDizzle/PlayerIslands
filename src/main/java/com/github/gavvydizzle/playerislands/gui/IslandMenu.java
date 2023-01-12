package com.github.gavvydizzle.playerislands.gui;

import com.github.gavvydizzle.playerislands.PlayerIslands;
import com.github.gavvydizzle.playerislands.configs.GUIConfig;
import com.github.gavvydizzle.playerislands.island.Island;
import com.github.gavvydizzle.playerislands.utils.Messages;
import com.github.gavvydizzle.playerislands.utils.Sounds;
import com.github.mittenmc.serverutils.ColoredItems;
import com.github.mittenmc.serverutils.Colors;
import com.github.mittenmc.serverutils.Numbers;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class IslandMenu extends Menu {

    private static Inventory inventory;
    private static String inventoryName;
    private static InventoryItem memberListItem, upgradeItem, teleportItem, privacyItem;
    private static int backButtonSlot;

    public IslandMenu(Island island) {
        super(island);
    }

    public static void reload() {
        FileConfiguration config = GUIConfig.get();
        config.addDefault("islandMenu.name", "{owner}'s Island Menu ({id})");
        config.addDefault("islandMenu.rows", 3);
        config.addDefault("islandMenu.filler", "gray");
        config.addDefault("islandMenu.backButtonSlot", 18);
        InventoryItem.addDefaults(config, "islandMenu.items.memberList",
                10,
                Material.ARMOR_STAND,
                "&eView Island Members",
                Collections.singletonList("&7Click here to view island members"),
                false);
        InventoryItem.addDefaults(config, "islandMenu.items.upgrade",
                12,
                Material.SUNFLOWER,
                "&eView Island Upgrades",
                Collections.singletonList("&7Click here to view island upgrades"),
                false);
        InventoryItem.addDefaults(config, "islandMenu.items.privacy",
                14,
                Material.INK_SAC,
                "&eIsland Privacy",
                Collections.singletonList("&7This island is currently <SOLID:FFAA00>{privacy}"),
                false);
        InventoryItem.addDefaults(config, "islandMenu.items.teleport",
                16,
                Material.END_PORTAL_FRAME,
                "&eVisit Island",
                Collections.singletonList("&7Click here to teleport to this island"),
                false);

        inventoryName = Colors.conv(config.getString("islandMenu.name"));
        int inventorySize = Numbers.constrain(config.getInt("islandMenu.rows") * 9, 9, 54);
        ItemStack filler = ColoredItems.getGlassByName(config.getString("islandMenu.filler"));
        backButtonSlot = config.getInt("islandMenu.backButtonSlot");
        memberListItem = new InventoryItem(config, "islandMenu.items.memberList");
        upgradeItem = new InventoryItem(config, "islandMenu.items.upgrade");
        teleportItem = new InventoryItem(config, "islandMenu.items.teleport");
        privacyItem = new InventoryItem(config, "islandMenu.items.privacy");

        inventory = Bukkit.createInventory(null, inventorySize, inventoryName);
        for (int i = 0; i < inventorySize; i++) {
            inventory.setItem(i, filler);
        }

        if (Numbers.isWithinRange(memberListItem.getSlot(), 0, inventorySize - 1)) {
            inventory.setItem(memberListItem.getSlot(), memberListItem.getItemStack());
        }
        if (Numbers.isWithinRange(upgradeItem.getSlot(), 0, inventorySize - 1)) {
            inventory.setItem(upgradeItem.getSlot(), upgradeItem.getItemStack());
        }
        if (Numbers.isWithinRange(teleportItem.getSlot(), 0, inventorySize - 1)) {
            inventory.setItem(teleportItem.getSlot(), teleportItem.getItemStack());
        }

        if (Numbers.isWithinRange(backButtonSlot, 0, inventorySize - 1)) {
            inventory.setItem(backButtonSlot, PlayerIslands.getInstance().getInventoryManager().getBackButton());
        }
        else {
            int forceSlot = inventorySize - 9;
            inventory.setItem(forceSlot, PlayerIslands.getInstance().getInventoryManager().getBackButton());
        }
    }

    @Override
    protected void createInventory() {
        super.createInventory();
        menu = Bukkit.createInventory(null, inventory.getSize(), inventoryName.replace("{owner}", getIsland().getOwner().getPlayerName()).replace("{id}", "" + getIsland().getId()));
        menu.setContents(inventory.getContents());
        updatePrivacyItem();
    }

    public void updatePrivacyItem() {
        if (menu == null) return;

        if (Numbers.isWithinRange(privacyItem.getSlot(), 0, menu.getSize() - 1)) {
            String privacyString = getIsland().isPrivate() ? "private" : "public";
            ItemStack item = privacyItem.getItemStack().clone();
            ItemMeta meta = item.getItemMeta();
            assert meta != null;

            meta.setDisplayName(meta.getDisplayName().replace("{privacy}", privacyString));

            if (meta.hasLore()) {
                ArrayList<String> newLore = new ArrayList<>();
                for (String str : Objects.requireNonNull(meta.getLore())) {
                    newLore.add(str.replace("{privacy}", privacyString));
                }
                meta.setLore(newLore);
            }

            item.setItemMeta(meta);
            menu.setItem(privacyItem.getSlot(), item);
        }
    }

    @Override
    public void onBackButtonClick(Player player, boolean isAdmin) {
        Sounds.generalClickSound.playSound(player);
        player.closeInventory();
    }

    @Override
    protected void handlePlayerClick(InventoryClickEvent e, Player player) {
        if (e.getSlot() == backButtonSlot) {
            onBackButtonClick(player, false);
        }
        else if (e.getSlot() == privacyItem.getSlot()) {

            if (!getIsland().isMember(player)) return;

            if (getIsland().getMemberFromPlayer(player).getMemberType().weight < PlayerIslands.getInstance().getPlayerCommandManager().getTogglePrivacyCommand().getRequiredRank().weight) {
                Sounds.generalFailSound.playSound(player);
                player.sendMessage(Messages.tooLowRank);
                return;
            }

            getIsland().toggleIslandPrivacy(player);
        }
        else if (e.getSlot() == teleportItem.getSlot()) {
            getIsland().teleportToSpawn(player);
        }
        else if (e.getSlot() == upgradeItem.getSlot()) {
            Sounds.generalClickSound.playSound(player);
            PlayerIslands.getInstance().getInventoryManager().handleMenuOpen(player, getIsland().getUpgradeMenu(), false);
                    }
        else if (e.getSlot() == memberListItem.getSlot()) {
            Sounds.generalClickSound.playSound(player);
            PlayerIslands.getInstance().getInventoryManager().handleMenuOpen(player, getIsland().getMemberListMenu(), false);
        }
    }

    @Override
    protected void handleAdminClick(InventoryClickEvent e, Player player) {
        if (e.getSlot() == backButtonSlot) {
            onBackButtonClick(player, true);
        }
        else if (e.getSlot() == privacyItem.getSlot()) {
            getIsland().toggleIslandPrivacy(player);
        }
        else if (e.getSlot() == teleportItem.getSlot()) {
            getIsland().teleportToSpawn(player);
        }
        else if (e.getSlot() == upgradeItem.getSlot()) {
            Sounds.generalClickSound.playSound(player);
            PlayerIslands.getInstance().getInventoryManager().handleMenuOpen(player, getIsland().getUpgradeMenu(), true);
        }
        else if (e.getSlot() == memberListItem.getSlot()) {
            Sounds.generalClickSound.playSound(player);
            PlayerIslands.getInstance().getInventoryManager().handleMenuOpen(player, getIsland().getMemberListMenu(), true);
        }
    }
}
