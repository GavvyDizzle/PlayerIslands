package com.github.gavvydizzle.playerislands.commands.player;

import com.github.gavvydizzle.playerislands.commands.PlayerCommandManager;
import com.github.gavvydizzle.playerislands.island.Island;
import com.github.gavvydizzle.playerislands.island.IslandManager;
import com.github.gavvydizzle.playerislands.utils.Messages;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class VisitRandomIslandCommand extends SubCommand {

    private final IslandManager islandManager;

    public VisitRandomIslandCommand(PlayerCommandManager playerCommandManager, IslandManager islandManager) {
        this.islandManager = islandManager;

        setName("visitRandom");
        setDescription("Teleports you to a random public island");
        setSyntax("/" + playerCommandManager.getCommandDisplayName() + " visitRandom");
        setColoredSyntax(ChatColor.YELLOW + getSyntax());
        setPermission(playerCommandManager.getPermissionPrefix() + getName().toLowerCase());
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return;

        Island island = islandManager.getRandomPublicIsland();
        if (island == null) {
            sender.sendMessage(Messages.noPublicIslandsAvailable);
            return;
        }
        island.teleportToSpawn(((Player) sender).getPlayer());
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}