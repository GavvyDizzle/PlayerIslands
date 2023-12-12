package com.github.gavvydizzle.playerislands.commands.player;

import com.github.gavvydizzle.playerislands.commands.IslandSelectionCommand;
import com.github.gavvydizzle.playerislands.commands.PlayerCommandManager;
import com.github.gavvydizzle.playerislands.island.Island;
import com.github.gavvydizzle.playerislands.island.IslandManager;
import com.github.gavvydizzle.playerislands.utils.Messages;
import com.github.mittenmc.serverutils.PermissionCommand;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VisitCommand extends SubCommand implements IslandSelectionCommand, PermissionCommand {

    private final PlayerCommandManager playerCommandManager;
    private final IslandManager islandManager;

    public VisitCommand(PlayerCommandManager playerCommandManager, IslandManager islandManager) {
        this.playerCommandManager = playerCommandManager;
        this.islandManager = islandManager;
    }

    @Override
    public String getPermission() {
        return "playerislands.island." + getName().toLowerCase();
    }

    @Override
    public String getName() {
        return "visit";
    }

    @Override
    public String getDescription() {
        return "Visit another player's island by name or id (optional: include member islands)";
    }

    @Override
    public String getSyntax() {
        return "/" + playerCommandManager.getCommandDisplayName() + " visit <player> [include_member_islands]";
    }

    @Override
    public String getColoredSyntax() {
        return ChatColor.YELLOW + "Usage: " + getSyntax();
    }

    @Override
    public ArrayList<Island> getIslandsForSelection(OfflinePlayer islandMember, String[] args) {
        boolean showMemberIslands = args.length >= 3 && args[2].equalsIgnoreCase("true");

        if (showMemberIslands) {
            return islandManager.getPlayerIslands(islandMember);
        }
        else {
            return islandManager.getPlayerOwnedIslands(islandMember);
        }
    }

    @Override
    public boolean isCommandValid(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return false;

        if (args.length < 2) {
            sender.sendMessage(getColoredSyntax());
            return false;
        }

        OfflinePlayer owner = islandManager.getPlayerByName(args[1]);
        if (owner == null) {
            sender.sendMessage(Messages.playerNotFound.replace("{player_name}", args[1]));
            return false;
        }

        boolean showMemberIslands = args.length >= 3 && args[2].equalsIgnoreCase("true");

        if (showMemberIslands) {
            if (islandManager.getNumPlayerIslands(owner) == 0) {
                sender.sendMessage(Messages.belongsToNoIslands.replace("{player_name}", args[1]));
                return false;
            }
        }
        else {
            if (islandManager.getNumPlayerOwnedIslands(owner) == 0) {
                sender.sendMessage(Messages.ownsNoIslands.replace("{player_name}", args[1]));
                return false;
            }
        }

        return true;
    }

    @Override
    public OfflinePlayer getIslandMember(CommandSender sender, String[] args) {
        return islandManager.getPlayerByName(args[1]);
    }

    @Override
    public void runSelectionCommand(Player player, String[] args, @NotNull Island island) {
        island.teleportToSpawn(player);
    }

    @Override
    public void perform(CommandSender sender, String[] args) {

    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        ArrayList<String> list = new ArrayList<>();

        if (args.length == 2) {
            StringUtil.copyPartialMatches(args[1], islandManager.getCachedPlayerNames(), list);
        }
        else if (args.length == 3) {
            StringUtil.copyPartialMatches(args[2], Collections.singleton("true"), list);
        }

        return list;
    }
}