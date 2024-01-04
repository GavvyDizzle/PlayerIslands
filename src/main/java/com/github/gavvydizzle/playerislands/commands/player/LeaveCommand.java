package com.github.gavvydizzle.playerislands.commands.player;

import com.github.gavvydizzle.playerislands.PlayerIslands;
import com.github.gavvydizzle.playerislands.commands.IslandSelectionCommand;
import com.github.gavvydizzle.playerislands.commands.PlayerCommandManager;
import com.github.gavvydizzle.playerislands.island.Island;
import com.github.gavvydizzle.playerislands.island.IslandManager;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class LeaveCommand extends SubCommand implements IslandSelectionCommand {

    private final IslandManager islandManager;

    public LeaveCommand(PlayerCommandManager playerCommandManager, IslandManager islandManager) {
        this.islandManager = islandManager;

        setName("leave");
        setDescription("Leave an island");
        setSyntax("/" + playerCommandManager.getCommandDisplayName() + " leave");
        setColoredSyntax(ChatColor.YELLOW + getSyntax());
        setPermission(playerCommandManager.getPermissionPrefix() + getName().toLowerCase());
    }

    @Override
    public ArrayList<Island> getIslandsForSelection(OfflinePlayer islandMember, String[] args) {
        return PlayerIslands.getInstance().getIslandManager().getPlayerMemberIslands(islandMember);
    }

    @Override
    public boolean isCommandValid(CommandSender sender, String[] args) {
        return sender instanceof Player;
    }

    @Override
    public OfflinePlayer getIslandMember(CommandSender sender, String[] args) {
        return (OfflinePlayer) sender;
    }

    @Override
    public void runSelectionCommand(Player player, String[] args, @NotNull Island island) {
        islandManager.leaveIsland(island, player);
    }

    @Override
    public void perform(CommandSender sender, String[] args) {

    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}