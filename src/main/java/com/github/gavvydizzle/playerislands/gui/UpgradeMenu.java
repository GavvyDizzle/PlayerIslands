package com.github.gavvydizzle.playerislands.gui;

import com.github.gavvydizzle.playerislands.PlayerIslands;
import com.github.gavvydizzle.playerislands.configs.GUIConfig;
import com.github.gavvydizzle.playerislands.island.Island;
import com.github.gavvydizzle.playerislands.island.MemberType;
import com.github.gavvydizzle.playerislands.upgrade.MemberUpgrade;
import com.github.gavvydizzle.playerislands.upgrade.SizeUpgrade;
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
import java.util.Arrays;
import java.util.Objects;

public class UpgradeMenu extends Menu {

    private static Inventory inventory;
    private static String inventoryName;
    private static InventoryItem memberUpgradeItem, memberUpgradeItemMaxed, sizeUpgradeItem, sizeUpgradeItemMaxed;
    private static int backButtonSlot;

    public UpgradeMenu(Island island) {
        super(island);
    }

    public static void reload() {
        FileConfiguration config = GUIConfig.get();
        config.addDefault("upgradeMenu.name", "Island Upgrades");
        config.addDefault("upgradeMenu.rows", 3);
        config.addDefault("upgradeMenu.filler", "gray");
        config.addDefault("upgradeMenu.backButtonSlot", 18);
        InventoryItem.addDefaults(config, "upgradeMenu.items.memberUpgrade",
                11,
                Material.ARMOR_STAND,
                "&eUpgrade Member Cap",
                Arrays.asList("", "<SOLID:0AD13C>Current Size: {current_numMembers}", "<SOLID:45E6CD>➥ Next Size: {numMembers}", "", "&aUpgrade Price: ${price}"),
                false);
        InventoryItem.addDefaults(config, "upgradeMenu.items.memberUpgradeMaxed",
                11,
                Material.ARMOR_STAND,
                "&eUpgrade Member Cap",
                Arrays.asList("", "<SOLID:0AD13C>Current Size: {current_numMembers}", "<SOLID:E64570>➥ Upgrade Maxed"),
                false);
        InventoryItem.addDefaults(config, "upgradeMenu.items.sizeUpgrade",
                15,
                Material.GRASS_BLOCK,
                "&eUpgrade Island Size",
                Arrays.asList("", "<SOLID:0AD13C>Current Size: {current_dx}x{current_dy}x{current_dz}", "<SOLID:45E6CD>➥ Next Size: {dx}x{dy}x{dz}", "", "&aUpgrade Price: ${price}"),
                false);
        InventoryItem.addDefaults(config, "upgradeMenu.items.sizeUpgradeMaxed",
                15,
                Material.GRASS_BLOCK,
                "&eUpgrade Island Size",
                Arrays.asList("", "<SOLID:0AD13C>Current Size: {current_dx}x{current_dy}x{current_dz}", "<SOLID:E64570>➥ Upgrade Maxed"),
                false);

        inventoryName = Colors.conv(config.getString("upgradeMenu.name"));
        int inventorySize = Numbers.constrain(config.getInt("upgradeMenu.rows") * 9, 9, 54);
        ItemStack filler = ColoredItems.getGlassByName(config.getString("upgradeMenu.filler"));
        backButtonSlot = config.getInt("upgradeMenu.backButtonSlot");
        memberUpgradeItem = new InventoryItem(config, "upgradeMenu.items.memberUpgrade");
        memberUpgradeItemMaxed = new InventoryItem(config, "upgradeMenu.items.memberUpgradeMaxed");
        sizeUpgradeItem = new InventoryItem(config, "upgradeMenu.items.sizeUpgrade");
        sizeUpgradeItemMaxed = new InventoryItem(config, "upgradeMenu.items.sizeUpgradeMaxed");

        inventory = Bukkit.createInventory(null, inventorySize, inventoryName);
        for (int i = 0; i < inventorySize; i++) {
            inventory.setItem(i, filler);
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
        menu = Bukkit.createInventory(null, inventory.getSize(), inventoryName);
        menu.setContents(inventory.getContents());
        updateMemberUpgradeItem();
        updateSizeUpgradeItem();
    }

    public void updateMemberUpgradeItem() {
        if (menu == null) return;

        if (Numbers.isWithinRange(memberUpgradeItem.getSlot(), 0, menu.getSize() - 1)) {
            if (getIsland().isMemberUpgradeMax()) {
                ItemStack item = memberUpgradeItemMaxed.getItemStack().clone();
                ItemMeta meta = item.getItemMeta();
                assert meta != null;

                MemberUpgrade currentMemberUpgrade = getIsland().getMemberUpgrade();

                if (currentMemberUpgrade != null) {
                    meta.setDisplayName(meta.getDisplayName()
                            .replace("{current_numMembers}", "" + currentMemberUpgrade.getMaxMembers())
                    );

                    if (meta.hasLore()) {
                        ArrayList<String> newLore = new ArrayList<>();
                        for (String str : Objects.requireNonNull(meta.getLore())) {
                            newLore.add(str
                                    .replace("{current_numMembers}", "" + currentMemberUpgrade.getMaxMembers())
                            );
                        }
                        meta.setLore(newLore);
                    }
                    item.setItemMeta(meta);
                }

                menu.setItem(memberUpgradeItem.getSlot(), item);
            }
            else {
                ItemStack item = memberUpgradeItem.getItemStack().clone();
                ItemMeta meta = item.getItemMeta();
                assert meta != null;

                MemberUpgrade currentMemberUpgrade = getIsland().getMemberUpgrade();
                MemberUpgrade memberUpgrade = getIsland().getNextMemberUpgrade();

                if (currentMemberUpgrade != null && memberUpgrade != null) {
                    meta.setDisplayName(meta.getDisplayName()
                            .replace("{price}", Numbers.withSuffix(memberUpgrade.getPrice()))
                            .replace("{numMembers}", "" + memberUpgrade.getMaxMembers())
                            .replace("{current_numMembers}", "" + currentMemberUpgrade.getMaxMembers())
                    );

                    if (meta.hasLore()) {
                        ArrayList<String> newLore = new ArrayList<>();
                        for (String str : Objects.requireNonNull(meta.getLore())) {
                            newLore.add(str
                                    .replace("{price}", Numbers.withSuffix(memberUpgrade.getPrice()))
                                    .replace("{numMembers}", "" + memberUpgrade.getMaxMembers())
                                    .replace("{current_numMembers}", "" + currentMemberUpgrade.getMaxMembers())
                            );
                        }
                        meta.setLore(newLore);
                    }
                    item.setItemMeta(meta);
                }

                menu.setItem(memberUpgradeItem.getSlot(), item);
            }
        }
    }

    public void updateSizeUpgradeItem() {
        if (menu == null) return;

        if (Numbers.isWithinRange(sizeUpgradeItem.getSlot(), 0, menu.getSize() - 1)) {
            if (getIsland().isSizeUpgradeMax()) {
                ItemStack item = sizeUpgradeItemMaxed.getItemStack().clone();
                ItemMeta meta = item.getItemMeta();
                assert meta != null;

                SizeUpgrade currentSizeUpgrade = getIsland().getSizeUpgrade();

                if (currentSizeUpgrade != null) {
                    meta.setDisplayName(meta.getDisplayName()
                            .replace("{current_dx}", "" + currentSizeUpgrade.getDimension().getX())
                            .replace("{current_dy}", "" + currentSizeUpgrade.getDimension().getY())
                            .replace("{current_dz}", "" + currentSizeUpgrade.getDimension().getZ())
                    );

                    if (meta.hasLore()) {
                        ArrayList<String> newLore = new ArrayList<>();
                        for (String str : Objects.requireNonNull(meta.getLore())) {
                            newLore.add(str
                                    .replace("{current_dx}", "" + currentSizeUpgrade.getDimension().getX())
                                    .replace("{current_dy}", "" + currentSizeUpgrade.getDimension().getY())
                                    .replace("{current_dz}", "" + currentSizeUpgrade.getDimension().getZ())
                            );
                        }
                        meta.setLore(newLore);
                    }
                    item.setItemMeta(meta);
                }

                menu.setItem(sizeUpgradeItem.getSlot(), item);
            }
            else {
                ItemStack item = sizeUpgradeItem.getItemStack().clone();
                ItemMeta meta = item.getItemMeta();
                assert meta != null;

                SizeUpgrade currentSizeUpgrade = getIsland().getSizeUpgrade();
                SizeUpgrade sizeUpgrade = getIsland().getNextSizeUpgrade();

                if (currentSizeUpgrade != null && sizeUpgrade != null) {
                    meta.setDisplayName(meta.getDisplayName()
                            .replace("{price}", Numbers.withSuffix(sizeUpgrade.getPrice()))
                            .replace("{dx}", "" + sizeUpgrade.getDimension().getX())
                            .replace("{dy}", "" + sizeUpgrade.getDimension().getY())
                            .replace("{dz}", "" + sizeUpgrade.getDimension().getZ())
                            .replace("{current_dx}", "" + currentSizeUpgrade.getDimension().getX())
                            .replace("{current_dy}", "" + currentSizeUpgrade.getDimension().getY())
                            .replace("{current_dz}", "" + currentSizeUpgrade.getDimension().getZ())
                    );

                    if (meta.hasLore()) {
                        ArrayList<String> newLore = new ArrayList<>();
                        for (String str : Objects.requireNonNull(meta.getLore())) {
                            newLore.add(str
                                    .replace("{price}", Numbers.withSuffix(sizeUpgrade.getPrice()))
                                    .replace("{dx}", "" + sizeUpgrade.getDimension().getX())
                                    .replace("{dy}", "" + sizeUpgrade.getDimension().getY())
                                    .replace("{dz}", "" + sizeUpgrade.getDimension().getZ())
                                    .replace("{current_dx}", "" + currentSizeUpgrade.getDimension().getX())
                                    .replace("{current_dy}", "" + currentSizeUpgrade.getDimension().getY())
                                    .replace("{current_dz}", "" + currentSizeUpgrade.getDimension().getZ())
                            );
                        }
                        meta.setLore(newLore);
                    }
                    item.setItemMeta(meta);
                }

                menu.setItem(sizeUpgradeItem.getSlot(), item);
            }
        }
    }

    @Override
    public void onBackButtonClick(Player player, boolean isAdmin) {
        Sounds.generalClickSound.playSound(player);
        PlayerIslands.getInstance().getInventoryManager().handleMenuOpen(player, getIsland().getIslandMenu(), isAdmin);
    }

    @Override
    protected void handlePlayerClick(InventoryClickEvent e, Player player) {
        if (e.getSlot() == backButtonSlot) {
            onBackButtonClick(player, false);
        }
        else if (e.getSlot() == sizeUpgradeItem.getSlot()) {
            if (getIsland().getPlayerMemberType(player) != MemberType.OWNER) {
                player.sendMessage(Messages.mustBeIslandOwner);
                return;
            }

            handleSizeUpgrade(player);
        }
        else if (e.getSlot() == memberUpgradeItem.getSlot()) {
            if (getIsland().getPlayerMemberType(player) != MemberType.OWNER) {
                player.sendMessage(Messages.mustBeIslandOwner);
                return;
            }

            handleMemberUpgrade(player);
        }
    }

    @Override
    protected void handleAdminClick(InventoryClickEvent e, Player player) {
        if (e.getSlot() == backButtonSlot) {
            onBackButtonClick(player, true);
        }
        else if (e.getSlot() == sizeUpgradeItem.getSlot()) {
            handleAdminSizeUpgrade(player);
        }
        else if (e.getSlot() == memberUpgradeItem.getSlot()) {
            handleAdminMemberUpgrade(player);
        }
    }

    /**
     * Handles an attempt to upgrade the size
     * @param owner The island owner
     */
    private void handleSizeUpgrade(Player owner) {
        SizeUpgrade sizeUpgrade = getIsland().getNextSizeUpgrade();
        if (sizeUpgrade == null) {
            Sounds.generalFailSound.playSound(owner);
            owner.sendMessage(Messages.upgradeMaxLevel);
            return;
        }

        if (sizeUpgrade.doesNotHavePermissionIgnoreAdmin(owner.getPlayer())) {
            Sounds.generalFailSound.playSound(owner);
            owner.sendMessage(Messages.insufficientUpgradePermission);
            return;
        }

        if (sizeUpgrade.cannotAffordUpgrade(owner)) {
            Sounds.generalFailSound.playSound(owner);
            owner.sendMessage(Messages.playerCannotAffordUpgrade);
            return;
        }

        Sounds.upgradeSuccessful.playSound(owner);
        sizeUpgrade.purchaseUpgrade(owner);
        getIsland().upgradeSizeUpgrade();
    }

    /**
     * Handles an attempt to upgrade the member cap
     * @param owner The island owner
     */
    private void handleMemberUpgrade(Player owner) {
        MemberUpgrade memberUpgrade = getIsland().getNextMemberUpgrade();
        if (memberUpgrade == null) {
            Sounds.generalFailSound.playSound(owner);
            owner.sendMessage(Messages.upgradeMaxLevel);
            return;
        }

        if (memberUpgrade.doesNotHavePermissionIgnoreAdmin(owner.getPlayer())) {
            Sounds.generalFailSound.playSound(owner);
            owner.sendMessage(Messages.insufficientUpgradePermission);
            return;
        }

        if (memberUpgrade.cannotAffordUpgrade(owner)) {
            Sounds.generalFailSound.playSound(owner);
            owner.sendMessage(Messages.playerCannotAffordUpgrade);
            return;
        }

        Sounds.upgradeSuccessful.playSound(owner);
        memberUpgrade.purchaseUpgrade(owner);
        getIsland().upgradeMemberUpgrade();
    }

    /**
     * Handles when an admin upgrades the size
     * @param admin The admin
     */
    private void handleAdminSizeUpgrade(Player admin) {
        SizeUpgrade sizeUpgrade = getIsland().getNextSizeUpgrade();
        if (sizeUpgrade == null) {
            Sounds.generalFailSound.playSound(admin);
            admin.sendMessage(Messages.upgradeMaxLevel);
            return;
        }

        Sounds.upgradeSuccessful.playSound(admin);
        admin.sendMessage(Messages.successfulPurchase.replace("{price}", "0"));
        getIsland().upgradeSizeUpgrade();
    }

    /**
     * Handles when an admin upgrades the member cap
     * @param admin The admin
     */
    private void handleAdminMemberUpgrade(Player admin) {
        MemberUpgrade memberUpgrade = getIsland().getNextMemberUpgrade();
        if (memberUpgrade == null) {
            Sounds.generalFailSound.playSound(admin);
            admin.sendMessage(Messages.upgradeMaxLevel);
            return;
        }

        Sounds.upgradeSuccessful.playSound(admin);
        admin.sendMessage(Messages.successfulPurchase.replace("{price}", "0"));
        getIsland().upgradeMemberUpgrade();
    }
}