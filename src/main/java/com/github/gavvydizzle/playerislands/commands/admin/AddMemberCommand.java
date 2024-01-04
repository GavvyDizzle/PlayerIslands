package com.github.gavvydizzle.playerislands.commands.admin;

import com.github.gavvydizzle.playerislands.commands.AdminCommandManager;
import com.github.gavvydizzle.playerislands.commands.IslandSelectionCommand;
import com.github.gavvydizzle.playerislands.island.Island;
import com.github.gavvydizzle.playerislands.island.IslandManager;
import com.github.gavvydizzle.playerislands.utils.Messages;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class AddMemberCommand extends SubCommand implements IslandSelectionCommand {

    private final IslandManager islandManager;

    public AddMemberCommand(AdminCommandManager adminCommandManager, IslandManager islandManager) {
        this.islandManager = islandManager;

        setName("addMember");
        setDescription("Forcefully makes the player a member of an island");
        setSyntax("/" + adminCommandManager.getCommandDisplayName() + " addMember <owner> <player> [bypass_member_cap]");
        setColoredSyntax(ChatColor.YELLOW + getSyntax());
        setPermission(adminCommandManager.getPermissionPrefix() + getName().toLowerCase());
    }

    @Override
    public boolean isCommandValid(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return false;

        if (args.length < 3) {
            sender.sendMessage(getColoredSyntax());
            return false;
        }

        OfflinePlayer owner = islandManager.getPlayerByName(args[1]);
        if (owner == null) {
            sender.sendMessage(Messages.playerNotFound.replace("{player_name}", args[1]));
            return false;
        }

        OfflinePlayer newMember = islandManager.getPlayerByName(args[2]);
        if (newMember == null) newMember = Bukkit.getPlayer(args[2]);
        if (newMember == null) {
            sender.sendMessage(Messages.playerNotFound.replace("{player_name}", args[2]));
            return false;
        }

        if (owner.getUniqueId().equals(newMember.getUniqueId())) {
            sender.sendMessage(ChatColor.RED + "Bad, very bad. Don't put the same player twice. Good thing I caught that or you could have broken something...");
            return false;
        }

        return true;
    }

    @Override
    public OfflinePlayer getIslandMember(CommandSender sender, String[] args) {
        return islandManager.getPlayerByName(args[1]);
    }

    @Override
    public ArrayList<Island> getIslandsForSelection(OfflinePlayer islandMember, String[] args) {
        OfflinePlayer newMember = islandManager.getPlayerByName(args[2]);
        if (newMember == null) newMember = Bukkit.getPlayer(args[2]);

        OfflinePlayer finalNewMember = newMember;
        return (ArrayList<Island>) islandManager.getPlayerOwnedIslands(Objects.requireNonNull(islandManager.getPlayerByName(args[1])))
                .stream().filter(island -> !island.isMember(finalNewMember)).collect(Collectors.toList());
    }

    @Override
    public void runSelectionCommand(Player player, String[] args, @NotNull Island island) {
        OfflinePlayer newMember = islandManager.getPlayerByName(args[2]);
        if (newMember == null) newMember = Bukkit.getPlayer(args[2]);
        if (newMember == null) return;

        islandManager.adminAddMember(island, player, newMember, args.length >= 4 && args[3].equalsIgnoreCase("true"));
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
        else if (args.length <= 3) {
            return null;
        }
        else if (args.length == 4) {
            StringUtil.copyPartialMatches(args[3], Collections.singleton("true"), list);
            return list;
        }

       return list;
    }

}