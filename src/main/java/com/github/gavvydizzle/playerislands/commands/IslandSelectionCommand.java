package com.github.gavvydizzle.playerislands.commands;

import com.github.gavvydizzle.playerislands.island.Island;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * Represents a command where an island must be selected by the sender
 */
public interface IslandSelectionCommand {

    /**
     * Determines if this command should execute.
     * This method will output any error messages before returning a value.
     *
     * @param sender The sender of the command
     * @param args The arguments of the command
     * @return True if the command's parameters are valid, false otherwise.
     */
    boolean isCommandValid(CommandSender sender, String[] args);

    /**
     * Gets the player that used when looking for islands
     * @param sender The sender of the command
     * @param args The arguments of the command
     * @return A OfflinePlayer
     */
    OfflinePlayer getIslandMember(CommandSender sender, String[] args);

    /**
     * Gets the islands that the player will select from.
     * This allows each command to choose which islands show up in the selection.
     *
     * @param islandMember The member of the islands when searching
     * @param args The arguments of the command
     * @return An ArrayList of Islands
     */
    ArrayList<Island> getIslandsForSelection(OfflinePlayer islandMember, String[] args);

    /**
     * Handles completing this command after the player selects an island.
     * @param player The player who ran the command
     * @param args The arguments of the command
     * @param island The island the player selected
     */
    void runSelectionCommand(Player player, String[] args, @NotNull Island island);

}
