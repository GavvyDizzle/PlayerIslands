package com.github.gavvydizzle.playerislands.commands.admin;

import com.github.gavvydizzle.playerislands.commands.AdminCommandManager;
import com.github.gavvydizzle.playerislands.island.Island;
import com.github.gavvydizzle.playerislands.island.IslandManager;
import com.github.gavvydizzle.playerislands.utils.Messages;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class ResetIsland extends SubCommand {

    private final IslandManager islandManager;

    public ResetIsland(AdminCommandManager adminCommandManager, IslandManager islandManager) {
        this.islandManager = islandManager;

        setName("resetIsland");
        setDescription("Reset an island's upgrades (removes all placed blocks)");
        setSyntax("/" + adminCommandManager.getCommandDisplayName() + " resetIsland <id>");
        setColoredSyntax(ChatColor.YELLOW + getSyntax());
        setPermission(adminCommandManager.getPermissionPrefix() + getName().toLowerCase());
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
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

        islandManager.removePlayersFromIsland(island);
        island.setMemberUpgrade(0);
        island.setSizeUpgrade(0, false);
        sender.sendMessage(ChatColor.GREEN + "Successfully reset island " + id);
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}