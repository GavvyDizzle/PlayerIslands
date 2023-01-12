package com.github.gavvydizzle.playerislands.gui;

import com.github.gavvydizzle.playerislands.PlayerIslands;
import com.github.gavvydizzle.playerislands.configs.GUIConfig;
import com.github.gavvydizzle.playerislands.island.Island;
import com.github.gavvydizzle.playerislands.island.IslandMember;
import com.github.gavvydizzle.playerislands.island.sorters.IslandSelectionSorter;
import com.github.mittenmc.serverutils.ColoredItems;
import com.github.mittenmc.serverutils.Colors;
import com.github.mittenmc.serverutils.ConfigUtils;
import com.github.mittenmc.serverutils.Numbers;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.logging.Logger;

public class IslandSelectionMenu {

    private static String inventoryName;
    private static ItemStack filler, templateMemberItem, templateSelfMemberItem, templateOwnerItem, templateSelfOwnerItem;

    private final Player player;
    private final OfflinePlayer islandMember;
    private final ArrayList<Island> islands;

    /**
     * Creates a new island selection menu
     * @param player The player who will view the inventory
     * @param islandMember The player whose islands are being selected
     * @param islands A list of islands to show
     */
    public IslandSelectionMenu(Player player, OfflinePlayer islandMember, ArrayList<Island> islands) {
        this.player = player;
        this.islandMember = islandMember;
        this.islands = islands;
        islands.sort(new IslandSelectionSorter(islandMember));
    }

    public static void reload() {
        FileConfiguration config = GUIConfig.get();
        config.addDefault("selectionMenu.name", "Island Selection");
        config.addDefault("selectionMenu.filler", "gray");
        config.addDefault("selectionMenu.items.member.material", "FEATHER");
        config.addDefault("selectionMenu.items.member.name", "&eIsland {id}");
        config.addDefault("selectionMenu.items.member.lore", Collections.singletonList("&7{player_name} is a {rank} of this island"));
        config.addDefault("selectionMenu.items.member.selfLore", Collections.singletonList("&7You are a {rank} of this island"));
        config.addDefault("selectionMenu.items.owner.material", "CHEST");
        config.addDefault("selectionMenu.items.owner.name", "&eIsland {id}");
        config.addDefault("selectionMenu.items.owner.lore", Collections.singletonList("&7{player_name} is the owner of this island"));
        config.addDefault("selectionMenu.items.owner.selfLore", Collections.singletonList("&7You are the owner of this island"));

        inventoryName = Colors.conv(config.getString("selectionMenu.name"));
        filler = ColoredItems.getGlassByName(config.getString("selectionMenu.filler"));

        templateMemberItem = new ItemStack(ConfigUtils.getMaterial(config.getString("selectionMenu.items.member.material"), Material.OAK_DOOR));
        ItemMeta meta = templateMemberItem.getItemMeta();
        assert(meta != null);
        meta.setDisplayName(Colors.conv(config.getString("selectionMenu.items.member.name")));
        meta.setLore(Colors.conv(config.getStringList("selectionMenu.items.member.lore")));
        templateMemberItem.setItemMeta(meta);

        templateSelfMemberItem = templateMemberItem.clone();
        meta.setLore(Colors.conv(config.getStringList("selectionMenu.items.member.selfLore")));
        templateSelfMemberItem.setItemMeta(meta);

        templateOwnerItem = new ItemStack(ConfigUtils.getMaterial(config.getString("selectionMenu.items.owner.material"), Material.OAK_DOOR));
        meta = templateOwnerItem.getItemMeta();
        assert(meta != null);
        meta.setDisplayName(Colors.conv(config.getString("selectionMenu.items.owner.name")));
        meta.setLore(Colors.conv(config.getStringList("selectionMenu.items.owner.lore")));
        templateOwnerItem.setItemMeta(meta);

        templateSelfOwnerItem = templateOwnerItem.clone();
        meta.setLore(Colors.conv(config.getStringList("selectionMenu.items.owner.selfLore")));
        templateSelfOwnerItem.setItemMeta(meta);
    }

    public Inventory getInventory() {
        Inventory inventory = Bukkit.createInventory(player, getSize(), inventoryName);
        for (int i = 0; i < islands.size(); i++) {
            Island island = islands.get(i);
            ItemStack item;
            ItemMeta meta;

            try {
                IslandMember member = island.getMemberFromPlayer(islandMember);

                if (island.isOwner(islandMember)) {
                    if (islandMember.getUniqueId().equals(player.getUniqueId())) {
                        item = templateSelfOwnerItem.clone();
                        meta = item.getItemMeta();
                        assert meta != null;
                        meta.setDisplayName(meta.getDisplayName().replace("{id}", "" + island.getId()));

                        if (meta.hasLore()) {
                            ArrayList<String> newLore = new ArrayList<>();
                            for (String str : Objects.requireNonNull(meta.getLore())) {
                                newLore.add(str.replace("{id}", "" + island.getId()));
                            }
                            meta.setLore(newLore);
                        }
                    } else {
                        item = templateOwnerItem.clone();
                        meta = item.getItemMeta();
                        assert meta != null;
                        meta.setDisplayName(meta.getDisplayName()
                                .replace("{id}", "" + island.getId())
                                .replace("{player_name}", member.getPlayerName()));

                        if (meta.hasLore()) {
                            ArrayList<String> newLore = new ArrayList<>();
                            for (String str : Objects.requireNonNull(meta.getLore())) {
                                newLore.add(str
                                        .replace("{id}", "" + island.getId())
                                        .replace("{player_name}", member.getPlayerName()));
                            }
                            meta.setLore(newLore);
                        }
                    }
                } else {
                    if (islandMember.getUniqueId().equals(player.getUniqueId())) {
                        item = templateSelfMemberItem.clone();
                        meta = item.getItemMeta();
                        assert meta != null;
                        meta.setDisplayName(meta.getDisplayName()
                                .replace("{id}", "" + island.getId())
                                .replace("{rank}", island.getPlayerMemberType(player).lowercaseName));

                        if (meta.hasLore()) {
                            ArrayList<String> newLore = new ArrayList<>();
                            for (String str : Objects.requireNonNull(meta.getLore())) {
                                newLore.add(str
                                        .replace("{id}", "" + island.getId())
                                        .replace("{rank}", island.getPlayerMemberType(player).lowercaseName));
                            }
                            meta.setLore(newLore);
                        }
                    } else {
                        item = templateMemberItem.clone();
                        meta = item.getItemMeta();
                        assert meta != null;
                        meta.setDisplayName(meta.getDisplayName()
                                .replace("{id}", "" + island.getId())
                                .replace("{rank}", member.getMemberType().lowercaseName)
                                .replace("{player_name}", member.getPlayerName()));

                        if (meta.hasLore()) {
                            ArrayList<String> newLore = new ArrayList<>();
                            for (String str : Objects.requireNonNull(meta.getLore())) {
                                newLore.add(str
                                        .replace("{id}", "" + island.getId())
                                        .replace("{rank}", member.getMemberType().lowercaseName)
                                        .replace("{player_name}", member.getPlayerName()));
                            }
                            meta.setLore(newLore);
                        }
                    }
                }

                item.setItemMeta(meta);
                item.setAmount(i + 1);
                inventory.setItem(i, item);
            }
            catch (Exception e) {
                e.printStackTrace();
                Logger logger = PlayerIslands.getInstance().getLogger();
                logger.info("Null member info:");
                logger.info("Player: " + player.getName());
                logger.info("IslandMember: " + islandMember.getName());
                logger.info(islands.get(i).toString());

                inventory.setItem(i, new ItemStack(Material.RED_DYE));
            }
        }
        for (int i = islands.size(); i < inventory.getSize(); i++) {
            inventory.setItem(i, filler);
        }

        return inventory;
    }

    private int getSize() {
        int rem = (islands.size() + 9) % 9;

        if (rem == 0) return islands.size();
        else return islands.size() + 9 - rem;
    }

    public Island getIslandFromSlot(int slot) {
        if (Numbers.isWithinRange(slot, 0, islands.size()-1)) {
            return islands.get(slot);
        }
        return null;
    }
}