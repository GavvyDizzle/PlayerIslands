package com.github.gavvydizzle.playerislands.commands.admin;

import com.github.gavvydizzle.playerislands.commands.AdminCommandManager;
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

import java.util.ArrayList;
import java.util.List;

public class ResetPlayerOwnedIslands extends SubCommand implements PermissionCommand {

    private final AdminCommandManager adminCommandManager;
    private final IslandManager islandManager;

    public ResetPlayerOwnedIslands(AdminCommandManager adminCommandManager, IslandManager islandManager) {
        this.adminCommandManager = adminCommandManager;
        this.islandManager = islandManager;
    }

    @Override
    public String getPermission() {
        return "playerislands.islandadmin." + getName().toLowerCase();
    }

    @Override
    public String getName() {
        return "resetAllIslands";
    }

    @Override
    public String getDescription() {
        return "Resets all islands that this player owns (removes player placed blocks)";
    }

    @Override
    public String getSyntax() {
        return "/" + adminCommandManager.getCommandDisplayName() + " resetAllIslands <player>";
    }

    @Override
    public String getColoredSyntax() {
        return ChatColor.YELLOW + "Usage: " + getSyntax();
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(getColoredSyntax());
            return;
        }

        OfflinePlayer owner = islandManager.getPlayerByName(args[1]);
        if (owner == null) {
            sender.sendMessage(Messages.playerNotFound.replace("{player_name}", args[1]));
            return;
        }

        if (islandManager.getNumPlayerOwnedIslands(owner) == 0) {
            sender.sendMessage(Messages.belongsToNoIslands.replace("{player_name}", args[1]));
            return;
        }

        for (Island island : islandManager.getPlayerOwnedIslands(owner)) {
            islandManager.removePlayersFromIsland(island);
            island.setMemberUpgrade(0);
            island.setSizeUpgrade(0, false);
            sender.sendMessage(ChatColor.GREEN + "Successfully reset island " + island.getId());
        }
    }

    @Override
    public List<String> getSubcommandArguments(Player player, String[] args) {
        ArrayList<String> list = new ArrayList<>();

        if (args.length == 2) {
            StringUtil.copyPartialMatches(args[1], islandManager.getCachedPlayerNames(), list);
        }

        return list;
    }
}