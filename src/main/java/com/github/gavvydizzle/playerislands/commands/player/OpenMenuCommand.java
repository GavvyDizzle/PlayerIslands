package com.github.gavvydizzle.playerislands.commands.player;

import com.github.gavvydizzle.playerislands.PlayerIslands;
import com.github.gavvydizzle.playerislands.commands.IslandSelectionCommand;
import com.github.gavvydizzle.playerislands.commands.PlayerCommandManager;
import com.github.gavvydizzle.playerislands.island.Island;
import com.github.gavvydizzle.playerislands.island.IslandManager;
import com.github.gavvydizzle.playerislands.utils.Messages;
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

public class OpenMenuCommand extends SubCommand implements IslandSelectionCommand {

    private final IslandManager islandManager;

    public OpenMenuCommand(PlayerCommandManager playerCommandManager, IslandManager islandManager) {
        this.islandManager = islandManager;

        setName("menu");
        setDescription("Open an island's menu (optional: include member islands)");
        setSyntax("/" + playerCommandManager.getCommandDisplayName() + " menu [player] [include_member_islands]");
        setColoredSyntax(ChatColor.YELLOW + getSyntax());
        setPermission(playerCommandManager.getPermissionPrefix() + getName().toLowerCase());
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

        OfflinePlayer owner = args.length < 2 ? (OfflinePlayer) sender : islandManager.getPlayerByName(args[1]);
        String ownerName = args.length < 2 ? sender.getName() : args[1];

        if (owner == null) {
            sender.sendMessage(Messages.playerNotFound.replace("{player_name}", ownerName));
            return false;
        }

        if (args.length < 2) {
            Island island = islandManager.getIslandByPlayerLocation((Player) sender);
            if (island != null) {
                PlayerIslands.getInstance().getInventoryManager().handleMenuOpen((Player) sender, island.getIslandMenu(), false);
                return false;
            }
        }

        boolean showMemberIslands = args.length >= 3 && args[2].equalsIgnoreCase("true");

        if (showMemberIslands) {
            if (islandManager.getNumPlayerIslands(owner) == 0) {
                sender.sendMessage(Messages.belongsToNoIslands.replace("{player_name}", ownerName));
            }
        }
        else {
            if (islandManager.getNumPlayerOwnedIslands(owner) == 0) {
                sender.sendMessage(Messages.ownsNoIslands.replace("{player_name}", ownerName));
            }
        }

        return true;
    }

    @Override
    public OfflinePlayer getIslandMember(CommandSender sender, String[] args) {
        return args.length < 2 ? (OfflinePlayer) sender : islandManager.getPlayerByName(args[1]);
    }

    @Override
    public void runSelectionCommand(Player player, String[] args, @NotNull Island island) {
        PlayerIslands.getInstance().getInventoryManager().handleMenuOpen(player, island.getIslandMenu(), false);
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