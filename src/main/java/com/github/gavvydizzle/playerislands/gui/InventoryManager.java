package com.github.gavvydizzle.playerislands.gui;

import com.github.gavvydizzle.playerislands.PlayerIslands;
import com.github.gavvydizzle.playerislands.configs.GUIConfig;
import com.github.gavvydizzle.playerislands.island.Island;
import com.github.gavvydizzle.playerislands.utils.Sounds;
import com.github.mittenmc.serverutils.Colors;
import com.github.mittenmc.serverutils.ConfigUtils;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class InventoryManager implements Listener {

    private final HashMap<UUID, Menu> playersInGUI;
    private final ArrayList<UUID> adminsInGUI;
    private final HashMap<UUID, IslandSelectionMenu> selectingPlayers; // Players current in the selection menu
    private ItemStack backButton;

    public InventoryManager() {
        playersInGUI = new HashMap<>();
        adminsInGUI = new ArrayList<>();
        selectingPlayers = new HashMap<>();
    }

    public void reload() {
        FileConfiguration config = GUIConfig.get();
        config.options().copyDefaults(true);

        config.addDefault("backButton.material", "RED_STAINED_GLASS_PANE");
        config.addDefault("backButton.name", "&cBack");
        config.addDefault("backButton.lore", new ArrayList<>());
        config.addDefault("backButton.glow", false);

        backButton = new ItemStack(ConfigUtils.getMaterial(config.getString("backButton.material"), Material.RED_STAINED_GLASS_PANE));
        ItemMeta meta = backButton.getItemMeta();
        assert meta != null;
        meta.setDisplayName(Colors.conv(config.getString("backButton.name")));
        meta.setLore(Colors.conv(config.getStringList("backButton.lore")));
        if (config.getBoolean("backButton.glow")) {
            meta.addEnchant(Enchantment.WATER_WORKER, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        backButton.setItemMeta(meta);

        IslandMenu.reload();
        MemberListMenu.reload();
        UpgradeMenu.reload();
        IslandSelectionMenu.reload();

        GUIConfig.save();
    }

    /**
     * Opens the menu for the player and adds them to the open GUI map
     * @param player The player
     * @param menu The menu they opened
     * @param isAdmin If the player opened the admin inventory
     */
    public void handleMenuOpen(Player player, Menu menu, boolean isAdmin) {
        player.openInventory(menu.getInventory());
        playersInGUI.put(player.getUniqueId(), menu);

        if (isAdmin) adminsInGUI.add(player.getUniqueId());
    }

    @EventHandler
    private void onMenuClick(InventoryClickEvent e) {
        if (e.getClickedInventory() == null) return;

        if (playersInGUI.containsKey(e.getWhoClicked().getUniqueId())) {
            e.setCancelled(true);

            if (e.getClickedInventory() == e.getView().getTopInventory()) {
                playersInGUI.get(e.getWhoClicked().getUniqueId()).handleClick(e, (Player) e.getWhoClicked(), adminsInGUI.contains(e.getWhoClicked().getUniqueId()));
            }
        }
        else if (selectingPlayers.containsKey(e.getWhoClicked().getUniqueId())) {
            e.setCancelled(true);

            if (e.getClickedInventory() == e.getView().getTopInventory()) {
                Island clickedIsland = selectingPlayers.get(e.getWhoClicked().getUniqueId()).getIslandFromSlot(e.getSlot());

                if (clickedIsland != null) {
                    selectingPlayers.remove(e.getWhoClicked().getUniqueId());
                    PlayerIslands.getInstance().getIslandSelectionManager().onIslandSelect((Player) e.getWhoClicked(), clickedIsland);
                    Sounds.generalClickSound.playSound((Player) e.getWhoClicked());
                }
            }
        }
    }

    @EventHandler
    private void onMenuClose(InventoryCloseEvent e) {
        if (playersInGUI.containsKey(e.getPlayer().getUniqueId())) {
            playersInGUI.remove(e.getPlayer().getUniqueId());
            adminsInGUI.remove(e.getPlayer().getUniqueId());
        }
        else if (selectingPlayers.containsKey(e.getPlayer().getUniqueId())) {
            selectingPlayers.remove(e.getPlayer().getUniqueId());
            PlayerIslands.getInstance().getIslandSelectionManager().onIslandSelect((Player) e.getPlayer(), null);
        }
    }

    @EventHandler
    private void onPlayerLeave(PlayerQuitEvent e) {
        playersInGUI.remove(e.getPlayer().getUniqueId());
        adminsInGUI.remove(e.getPlayer().getUniqueId());
        selectingPlayers.remove(e.getPlayer().getUniqueId());
    }

    public void openSelectionMenu(Player player, OfflinePlayer islandMember, ArrayList<Island> islands) {
        IslandSelectionMenu menu = new IslandSelectionMenu(player, islandMember, islands);
        player.openInventory(menu.getInventory());
        selectingPlayers.put(player.getUniqueId(), menu);
    }


    public ItemStack getBackButton() {
        return backButton;
    }

}
