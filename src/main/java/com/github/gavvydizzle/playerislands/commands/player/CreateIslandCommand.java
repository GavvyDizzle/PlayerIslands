package com.github.gavvydizzle.playerislands.commands.player;

import com.github.gavvydizzle.playerislands.commands.PlayerCommandManager;
import com.github.gavvydizzle.playerislands.island.IslandManager;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class CreateIslandCommand extends SubCommand {

    private final PlayerCommandManager playerCommandManager;
    private final IslandManager islandManager;

    public CreateIslandCommand(PlayerCommandManager playerCommandManager, IslandManager islandManager) {
        this.playerCommandManager = playerCommandManager;
        this.islandManager = islandManager;

        setName("create");
        setDescription("Create a new island");
        setSyntax("/" + playerCommandManager.getCommandDisplayName() + " create");
        setColoredSyntax(ChatColor.YELLOW + getSyntax());
        setPermission(playerCommandManager.getPermissionPrefix() + getName().toLowerCase());
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) return;

        islandManager.createNewIsland(player);
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}