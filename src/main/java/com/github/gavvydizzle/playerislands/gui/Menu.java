package com.github.gavvydizzle.playerislands.gui;

import com.github.gavvydizzle.playerislands.island.Island;
import com.github.gavvydizzle.playerislands.island.MemberType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public abstract class Menu {

    private final Island island;
    protected Inventory menu;
    private boolean isReloadPending;

    public Menu(Island island) {
        this.island = island;
        this.isReloadPending = false;
    }

    public Island getIsland() {
        return island;
    }

    public MemberType getMemberType(Player player) {
        return island.getPlayerMemberType(player);
    }

    /**
     * Generates the inventory for this menu and removes the reload pending status.
     * Subclasses are expected call to super then handle their own menu creation.
     */
    protected void createInventory() {
        isReloadPending = false;
    }

    /**
     * Creates the inventory and any placeholder items where applicable
     * @return The inventory
     */
    public Inventory getInventory() {
        if (menu == null || isReloadPending) createInventory();
        return menu;
    }

    /**
     * Handles what to do when the back button is clicked from this inventory
     * @param player The player who clicked
     * @param isAdmin If the player who clicked is in admin mode
     */
    public abstract void onBackButtonClick(Player player, boolean isAdmin);

    /**
     * Handles a click done in this menu. Calls the player or admin handleClick method
     * @param e The click event
     */
    public void handleClick(InventoryClickEvent e, Player player, boolean isAdminMode) {
        if (isAdminMode) handleAdminClick(e, player);
        else handlePlayerClick(e, player);
    }

    protected abstract void handlePlayerClick(InventoryClickEvent e, Player player);

    protected abstract void handleAdminClick(InventoryClickEvent e, Player player);

    /**
     * Tells this inventory to recreate itself the next time it is requested
     */
    public void setReloadPending() {
        isReloadPending = true;
    }
}