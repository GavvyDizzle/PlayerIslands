package com.github.gavvydizzle.playerislands.commands.player;

import com.github.gavvydizzle.playerislands.commands.IslandSelectionCommand;
import com.github.gavvydizzle.playerislands.commands.PlayerCommandManager;
import com.github.gavvydizzle.playerislands.commands.RankedCommand;
import com.github.gavvydizzle.playerislands.configs.CommandsConfig;
import com.github.gavvydizzle.playerislands.island.Island;
import com.github.gavvydizzle.playerislands.island.IslandManager;
import com.github.gavvydizzle.playerislands.island.MemberType;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TogglePrivacyCommand extends SubCommand implements IslandSelectionCommand, RankedCommand {

    private final IslandManager islandManager;

    public TogglePrivacyCommand(PlayerCommandManager playerCommandManager, IslandManager islandManager) {
        this.islandManager = islandManager;

        setName("togglePrivacy");
        setDescription("Toggle your island's privacy");
        setSyntax("/" + playerCommandManager.getCommandDisplayName() + " togglePrivacy");
        setColoredSyntax(ChatColor.YELLOW + getSyntax());
        setPermission(playerCommandManager.getPermissionPrefix() + getName().toLowerCase());
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
    public ArrayList<Island> getIslandsForSelection(OfflinePlayer islandMember, String[] args) {
        List<Island> islands = islandManager.getPlayerIslands(islandMember);
        return (ArrayList<Island>) islands.stream().filter(island -> island.getPlayerMemberType(islandMember).weight >= getRequiredRank().weight).collect(Collectors.toList());
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
        island.toggleIslandPrivacy(player);
    }

    @Override
    public void perform(CommandSender sender, String[] args) {

    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}