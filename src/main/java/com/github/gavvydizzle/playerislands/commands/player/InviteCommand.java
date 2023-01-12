package com.github.gavvydizzle.playerislands.commands.player;

import com.github.gavvydizzle.playerislands.PlayerIslands;
import com.github.gavvydizzle.playerislands.commands.IslandSelectionCommand;
import com.github.gavvydizzle.playerislands.commands.PlayerCommandManager;
import com.github.gavvydizzle.playerislands.commands.RankedCommand;
import com.github.gavvydizzle.playerislands.configs.CommandsConfig;
import com.github.gavvydizzle.playerislands.island.Island;
import com.github.gavvydizzle.playerislands.island.IslandManager;
import com.github.gavvydizzle.playerislands.island.MemberType;
import com.github.gavvydizzle.playerislands.utils.Messages;
import com.github.mittenmc.serverutils.PermissionCommand;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InviteCommand extends SubCommand implements IslandSelectionCommand, PermissionCommand, RankedCommand {

    private final PlayerCommandManager playerCommandManager;
    private final IslandManager islandManager;

    public InviteCommand(PlayerCommandManager playerCommandManager, IslandManager islandManager) {
        this.playerCommandManager = playerCommandManager;
        this.islandManager = islandManager;
    }

    @Override
    public MemberType getDefaultRequiredRank() {
        return MemberType.CO_OWNER;
    }

    @Override
    public MemberType getRequiredRank() {
        return MemberType.getTypeByWeight(CommandsConfig.getRequiredRank(this));
    }

    @Override
    public String getPermission() {
        return "playerislands.island." + getName().toLowerCase();
    }

    @Override
    public String getName() {
        return "invite";
    }

    @Override
    public String getDescription() {
        return "Invite a player to your island";
    }

    @Override
    public String getSyntax() {
        return "/" + playerCommandManager.getCommandDisplayName() + " invite <player>";
    }

    @Override
    public String getColoredSyntax() {
        return ChatColor.YELLOW + "Usage: " + getSyntax();
    }

    @Override
    public ArrayList<Island> getIslandsForSelection(OfflinePlayer islandMember, String[] args) {
        Player newMember = Bukkit.getPlayer(args[1]);

        List<Island> islands = PlayerIslands.getInstance().getIslandManager().getPlayerIslands(islandMember);
        return (ArrayList<Island>) islands.stream()
                .filter(island -> island.getPlayerMemberType(islandMember).weight >= getRequiredRank().weight && !island.isMember(newMember))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isCommandValid(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return false;

        if (args.length < 2) {
            sender.sendMessage(getColoredSyntax());
            return false;
        }

        Player newMember = Bukkit.getPlayer(args[1]);
        if (newMember == null) {
            sender.sendMessage(Messages.playerNotFound.replace("{player_name}", args[1]));
            return false;
        }

        if (((Player) sender).getUniqueId().equals(newMember.getUniqueId())) {
            sender.sendMessage(ChatColor.RED + "You can't invite yourself... silly goose");
            return false;
        }

        return true;
    }

    @Override
    public OfflinePlayer getIslandMember(CommandSender sender, String[] args) {
        return (OfflinePlayer) sender;
    }

    @Override
    public void runSelectionCommand(Player player, String[] args, @NotNull Island island) {
        Player newMember = Bukkit.getPlayer(args[1]);
        if (newMember == null) {
            player.sendMessage(Messages.playerNotFound.replace("{player_name}", args[1]));
            return;
        }

        islandManager.invitePlayerToIsland(island, player, newMember);
    }

    @Override
    public void perform(CommandSender sender, String[] args) {

    }

    @Override
    public List<String> getSubcommandArguments(Player player, String[] args) {
        if (args.length == 2) return null;
        return new ArrayList<>();
    }
}