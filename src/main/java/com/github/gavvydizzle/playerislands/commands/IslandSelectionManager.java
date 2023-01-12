package com.github.gavvydizzle.playerislands.commands;

import com.github.gavvydizzle.playerislands.PlayerIslands;
import com.github.gavvydizzle.playerislands.island.Island;
import com.github.gavvydizzle.playerislands.utils.Messages;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class IslandSelectionManager {

    private final HashMap<UUID, IslandSelection> selectionMap;

    public IslandSelectionManager() {
        selectionMap = new HashMap<>();
    }

    public void createSelection(IslandSelectionCommand command, CommandSender sender, OfflinePlayer islandMember, String[] args) {
        if (!(sender instanceof Player)) return;
        Player viewer = (Player) sender;

        if (selectionMap.containsKey(viewer.getUniqueId())) {
            viewer.sendMessage(Messages.selectionPending);
        }
        else {
            ArrayList<Island> islands = command.getIslandsForSelection(islandMember, args);
            if (islands == null || islands.size() == 0) {
                viewer.sendMessage(Messages.noValidIslands);
            }
            else if (islands.size() == 1) { // Don't open the selection inventory if there is only one valid island
                command.runSelectionCommand(viewer, args, islands.get(0));
            }
            else {
                viewer.sendMessage(Messages.selectionMessage);
                selectionMap.put(viewer.getUniqueId(), new IslandSelection(command, viewer, args));
                PlayerIslands.getInstance().getInventoryManager().openSelectionMenu(viewer, islandMember, islands);
            }
        }
    }

    /**
     * Handles when the player selects and island. If the island is null, this will fail and send the player a message
     * @param player The player
     * @param island The island they selected or null
     */
    public void onIslandSelect(Player player, @Nullable Island island) {
        if (!selectionMap.containsKey(player.getUniqueId())) return;
        player.closeInventory();

        IslandSelection selection = selectionMap.remove(player.getUniqueId());

        if (island == null) {
            if (player.isOnline()) player.sendMessage(Messages.selectionCancelled);
        }
        else {
            selection.getCommand().runSelectionCommand(player, selection.getArgs(), island);
        }

        if (selection != null) selection.cancelTask();
    }

    public void onConfirmExpire(Player player) {
        selectionMap.remove(player.getUniqueId());
        if (player.isOnline()) {
            player.sendMessage(Messages.selectionExpire);
            player.closeInventory();
        }
    }
}
