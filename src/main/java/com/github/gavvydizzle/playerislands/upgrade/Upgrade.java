package com.github.gavvydizzle.playerislands.upgrade;

import com.github.gavvydizzle.playerislands.PlayerIslands;
import com.github.gavvydizzle.playerislands.utils.Messages;
import org.bukkit.entity.Player;

public class Upgrade {

    private final long price;
    private final int upgradeLevel;
    private final String permission;

    public Upgrade (long price, int level, String permission) {
        this.price = Math.max(0, price);
        upgradeLevel = level;
        this.permission = permission;
    }

    public boolean cannotAffordUpgrade(Player player) {
        return !PlayerIslands.getEconomy().has(player, price);
    }

    public void purchaseUpgrade(Player player) {
        PlayerIslands.getEconomy().withdrawPlayer(player, price);
        player.sendMessage(Messages.successfulPurchase.replace("{price}", "" + price));
    }

    /**
     * Determines if the player does not have permission to purchase this upgrade.
     * If the player is an admin of the plugin, then they have permission by default.
     * @param player The player to check
     * @return True if the player does not meet this upgrade's permissions
     */
    public boolean doesNotHavePermission(Player player) {
        if (permission == null || permission.trim().isEmpty()) return false;
        if (PlayerIslands.isPluginAdmin(player)) return false;
        return !player.hasPermission(permission);
    }

    /**
     * Determines if the player does not have permission to purchase this upgrade.
     * If the player is an admin of the plugin, then they still need to have this upgrade's permission explicitly to purchase.
     * @param player The player to check
     * @return True if the player does not meet this upgrade's permissions
     */
    public boolean doesNotHavePermissionIgnoreAdmin(Player player) {
        if (permission == null || permission.trim().isEmpty()) return false;
        return !player.hasPermission(permission);
    }

    public int getUpgradeLevel() {
        return upgradeLevel;
    }

    public long getPrice() {
        return price;
    }

}
