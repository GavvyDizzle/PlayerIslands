package com.github.gavvydizzle.playerislands.commands.player;

import com.github.gavvydizzle.playerislands.commands.PlayerCommandManager;
import com.github.gavvydizzle.playerislands.island.Island;
import com.github.gavvydizzle.playerislands.island.IslandManager;
import com.github.gavvydizzle.playerislands.utils.Messages;
import com.github.mittenmc.serverutils.PermissionCommand;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class VisitByIDCommand extends SubCommand implements PermissionCommand {

    private final PlayerCommandManager playerCommandManager;
    private final IslandManager islandManager;

    public VisitByIDCommand(PlayerCommandManager playerCommandManager, IslandManager islandManager) {
        this.playerCommandManager = playerCommandManager;
        this.islandManager = islandManager;
    }

    @Override
    public String getPermission() {
        return "playerislands.island." + getName().toLowerCase();
    }

    @Override
    public String getName() {
        return "visitID";
    }

    @Override
    public String getDescription() {
        return "Visit an island with its ID";
    }

    @Override
    public String getSyntax() {
        return "/" + playerCommandManager.getCommandDisplayName() + " visitID <id>";
    }

    @Override
    public String getColoredSyntax() {
        return ChatColor.YELLOW + "Usage: " + getSyntax();
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return;

        if (args.length < 2) {
            sender.sendMessage(getColoredSyntax());
            return;
        }

        int id;
        try {
            id = Integer.parseInt(args[1]);
        } catch (NumberFormatException ignored) {
            sender.sendMessage(Messages.invalidIslandId.replace("{id}", args[1]));
            return;
        }

        Island island = islandManager.getIslandByID(id);
        if (island == null) {
            sender.sendMessage(Messages.invalidIslandId.replace("{id}", args[1]));
            return;
        }

        island.teleportToSpawn(((Player) sender).getPlayer());
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        return null;
    }
}