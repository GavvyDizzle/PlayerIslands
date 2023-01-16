package com.github.gavvydizzle.playerislands.gui;

import com.github.gavvydizzle.playerislands.PlayerIslands;
import com.github.gavvydizzle.playerislands.configs.GUIConfig;
import com.github.gavvydizzle.playerislands.island.Island;
import com.github.gavvydizzle.playerislands.island.IslandMember;
import com.github.gavvydizzle.playerislands.island.MemberType;
import com.github.gavvydizzle.playerislands.utils.Messages;
import com.github.gavvydizzle.playerislands.utils.Sounds;
import com.github.mittenmc.serverutils.ColoredItems;
import com.github.mittenmc.serverutils.Colors;
import com.github.mittenmc.serverutils.Numbers;
import com.github.mittenmc.serverutils.PlayerHeads;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;

public class MemberListMenu extends Menu {

    private static Inventory inventory;
    private static int backButtonSlot;
    private static String inventoryName, itemNameFormat;
    private static ArrayList<String> lore;
    private static int actionDelaySeconds;

    private long lastActionTime;

    public MemberListMenu(Island island) {
        super(island);
        lastActionTime = 0;
    }

    public static void reload() {
        FileConfiguration config = GUIConfig.get();
        config.addDefault("memberListMenu.actionDelaySeconds", 3);
        config.addDefault("memberListMenu.name", "Island Members");
        config.addDefault("memberListMenu.rows", 1);
        config.addDefault("memberListMenu.filler", "gray");
        config.addDefault("memberListMenu.backButtonSlot", 8);
        config.addDefault("memberListMenu.item.nameFormat", "<SOLID:FFAA00>[{rank}] <SOLID:E5FF00>{player_name}");
        config.addDefault("memberListMenu.item.lore", Arrays.asList("&7   --------------", "<SOLID:00FF11>Left-click to promote", "<SOLID:FF5252>Right-click to demote", "<SOLID:FF1C1C>Shift right-click to kick"));

        actionDelaySeconds = config.getInt("memberListMenu.actionDelaySeconds");
        inventoryName = Colors.conv(config.getString("memberListMenu.name"));
        int inventorySize = Numbers.constrain(config.getInt("memberListMenu.rows") * 9, 9, 54);
        ItemStack filler = ColoredItems.getGlassByName(config.getString("memberListMenu.filler"));
        backButtonSlot = config.getInt("memberListMenu.backButtonSlot");
        itemNameFormat = Colors.conv(config.getString("memberListMenu.item.nameFormat"));
        lore = (ArrayList<String>) Colors.conv(config.getStringList("memberListMenu.item.lore"));

        inventory = Bukkit.createInventory(null, inventorySize, inventoryName);
        for (int i = 0; i < inventorySize; i++) {
            inventory.setItem(i, filler);
        }

        if (Numbers.isWithinRange(backButtonSlot, 0, inventorySize - 1)) {
            inventory.setItem(backButtonSlot, PlayerIslands.getInstance().getInventoryManager().getBackButton());
        }
        else {
            int forceSlot = inventorySize - 9;
            if (forceSlot == 0) forceSlot = 8;
            inventory.setItem(forceSlot, PlayerIslands.getInstance().getInventoryManager().getBackButton());
        }
    }

    @Override
    protected void createInventory() {
        super.createInventory();
        menu = Bukkit.createInventory(null, inventory.getSize(), inventoryName);
        updateMemberList();
    }

    public void updateMemberList() {
        if (menu == null) return;

        menu.setContents(inventory.getContents()); // Set back to filler and back button

        menu.setItem(0, PlayerHeads.getHead(getIsland().getOwner().getOfflinePlayer().getUniqueId(),
                itemNameFormat.replace("{rank}", MemberType.OWNER.uppercaseName).replace("{player_name}", getIsland().getOwner().getPlayerName()))); // No lore on owner head

        ArrayList<IslandMember> members = getIsland().getMembers();
        for (int i = 0; i < members.size(); i++) {
            menu.setItem(i+1, PlayerHeads.getHead(members.get(i).getOfflinePlayer().getUniqueId(),
                    itemNameFormat.replace("{rank}", members.get(i).getMemberType().uppercaseName).replace("{player_name}", members.get(i).getPlayerName()), lore));
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
        else if (e.getSlot() < getIsland().getNumMembersWithOwner() && e.getSlot() != 0) {
            handleMemberClick(player, e.getSlot(), e.getClick());
        }
    }

    @Override
    protected void handleAdminClick(InventoryClickEvent e, Player player) {
        if (e.getSlot() == backButtonSlot) {
            onBackButtonClick(player, true);
        }
        else if (e.getSlot() < getIsland().getNumMembersWithOwner() && e.getSlot() != 0) {
            handleAdminClick(player, e.getSlot(), e.getClick());
        }
    }

    /**
     * Handles a click on a member head
     * @param member The island member
     */
    private void handleMemberClick(Player member, int slot, ClickType clickType) {
        if (!(clickType == ClickType.RIGHT || clickType == ClickType.LEFT || clickType == ClickType.SHIFT_RIGHT)) return;

        if (!getIsland().isMember(member)) return;

        MemberType memberType = getMemberType(member);

        IslandMember clickedMember = getIsland().getIslandMembers().get(slot-1);
        // Self click
        if (clickedMember.getOfflinePlayer().getUniqueId().equals(member.getUniqueId())) {
            Sounds.generalFailSound.playSound(member);
            member.sendMessage(ChatColor.YELLOW + "You feel a tickle on your face...");
            return;
        }

        // Promote action
        if (clickType == ClickType.LEFT) {
            if (memberType != MemberType.OWNER && memberType.weight <= clickedMember.getMemberType().weight) { // If the clicker is lower or equal rank cancel
                Sounds.generalFailSound.playSound(member);
                member.sendMessage(Messages.tooLowRankToPromote.replace("{rank}", clickedMember.getMemberType().lowercaseName));
                return;
            }

            if (isLocked()) { // Spam prevention
                Sounds.generalFailSound.playSound(member);
                member.sendMessage(Messages.pleaseWait);
                return;
            }

            if (getIsland().promoteMember(clickedMember)) {
                Sounds.promoteMemberSound.playSound(member);
                member.sendMessage(Messages.successfulPromote.replace("{player_name}", clickedMember.getPlayerName()).replace("{rank}", clickedMember.getMemberType().lowercaseName));
                clickedMember.sendMessage(Messages.promoted.replace("{owner}", getIsland().getOwner().getPlayerName()).replace("{rank}", clickedMember.getMemberType().lowercaseName));
                lastActionTime = System.currentTimeMillis();
            }
            else {
                Sounds.generalFailSound.playSound(member);
                member.sendMessage(Messages.alreadyThisRank.replace("{player_name}", clickedMember.getPlayerName()).replace("{rank}", clickedMember.getMemberType().lowercaseName));
            }
        }
        // Demote action
        else if (clickType == ClickType.RIGHT) {
            if (memberType != MemberType.OWNER && memberType.weight <= clickedMember.getMemberType().weight) { // If the clicker is lower or equal rank cancel
                Sounds.generalFailSound.playSound(member);
                member.sendMessage(Messages.tooLowRankToDemote.replace("{rank}", clickedMember.getMemberType().lowercaseName));
                return;
            }

            if (isLocked()) { // Spam prevention
                Sounds.generalFailSound.playSound(member);
                member.sendMessage(Messages.pleaseWait);
                return;
            }

            if (getIsland().demoteMember(clickedMember)) {
                Sounds.demoteMemberSound.playSound(member);
                member.sendMessage(Messages.successfulDemote.replace("{player_name}", clickedMember.getPlayerName()).replace("{rank}", clickedMember.getMemberType().lowercaseName));
                clickedMember.sendMessage(Messages.demoted.replace("{owner}", getIsland().getOwner().getPlayerName()).replace("{rank}", clickedMember.getMemberType().lowercaseName));
                lastActionTime = System.currentTimeMillis();
            }
            else {
                Sounds.generalFailSound.playSound(member);
                member.sendMessage(Messages.alreadyThisRank.replace("{player_name}", clickedMember.getPlayerName()).replace("{rank}", clickedMember.getMemberType().lowercaseName));
            }
        }
        // Kick action
        else {
            if (memberType != MemberType.OWNER && memberType.weight <= clickedMember.getMemberType().weight) { // If the clicker is lower or equal rank cancel
                Sounds.generalFailSound.playSound(member);
                member.sendMessage(Messages.tooLowRankToKick.replace("{rank}", clickedMember.getMemberType().lowercaseName));
                return;
            }

            getIsland().removeIslandMember(clickedMember);
            Sounds.kickMemberSound.playSound(member);
            member.sendMessage(Messages.successfulKick.replace("{player_name}", clickedMember.getPlayerName()).replace("{owner}", getIsland().getOwner().getPlayerName()));
            clickedMember.sendMessage(Messages.kicked.replace("{owner}", getIsland().getOwner().getPlayerName()));
            lastActionTime = System.currentTimeMillis();
        }
    }

    /**
     * Handles an admin click on a member head
     * @param admin The admin
     */
    private void handleAdminClick(Player admin, int slot, ClickType clickType) {
        if (!(clickType == ClickType.RIGHT || clickType == ClickType.LEFT || clickType == ClickType.SHIFT_RIGHT)) return;

        IslandMember clickedMember = getIsland().getIslandMembers().get(slot-1);

        // Promote action
        if (clickType == ClickType.LEFT) {
            if (getIsland().promoteMember(clickedMember)) {
                Sounds.promoteMemberSound.playSound(admin);
                admin.sendMessage(Messages.successfulPromote.replace("{player_name}", clickedMember.getPlayerName()).replace("{rank}", clickedMember.getMemberType().lowercaseName));
                clickedMember.sendMessage(Messages.promoted.replace("{owner}", getIsland().getOwner().getPlayerName()).replace("{rank}", clickedMember.getMemberType().lowercaseName));
                lastActionTime = System.currentTimeMillis();
            }
            else {
                Sounds.generalFailSound.playSound(admin);
                admin.sendMessage(Messages.alreadyThisRank.replace("{player_name}", clickedMember.getPlayerName()).replace("{rank}", clickedMember.getMemberType().lowercaseName));
            }
        }
        // Demote action
        else if (clickType == ClickType.RIGHT) {
            if (getIsland().demoteMember(clickedMember)) {
                Sounds.demoteMemberSound.playSound(admin);
                admin.sendMessage(Messages.successfulDemote.replace("{player_name}", clickedMember.getPlayerName()).replace("{rank}", clickedMember.getMemberType().lowercaseName));
                clickedMember.sendMessage(Messages.demoted.replace("{owner}", getIsland().getOwner().getPlayerName()).replace("{rank}", clickedMember.getMemberType().lowercaseName));
                lastActionTime = System.currentTimeMillis();
            }
            else {
                Sounds.generalFailSound.playSound(admin);
                admin.sendMessage(Messages.alreadyThisRank.replace("{player_name}", clickedMember.getPlayerName()).replace("{rank}", clickedMember.getMemberType().lowercaseName));
            }
        }
        // Kick action
        else {
            getIsland().removeIslandMember(clickedMember);
            Sounds.kickMemberSound.playSound(admin);
            admin.sendMessage(Messages.successfulKick.replace("{player_name}", clickedMember.getPlayerName()).replace("{owner}", getIsland().getOwner().getPlayerName()));
            clickedMember.sendMessage(Messages.kicked.replace("{owner}", getIsland().getOwner().getPlayerName()));
            lastActionTime = System.currentTimeMillis();
        }
    }

    private boolean isLocked() {
        return lastActionTime + actionDelaySeconds * 1000L > System.currentTimeMillis();
    }
}