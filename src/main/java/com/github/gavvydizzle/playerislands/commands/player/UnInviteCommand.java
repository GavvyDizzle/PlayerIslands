package com.github.gavvydizzle.playerislands.commands.player;

import com.github.gavvydizzle.playerislands.commands.PlayerCommandManager;
import com.github.gavvydizzle.playerislands.island.IslandManager;
import com.github.gavvydizzle.playerislands.utils.Messages;
import com.github.mittenmc.serverutils.PermissionCommand;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class UnInviteCommand extends SubCommand implements PermissionCommand {

    private final PlayerCommandManager playerCommandManager;
    private final IslandManager islandManager;

    public UnInviteCommand(PlayerCommandManager playerCommandManager, IslandManager islandManager) {
        this.playerCommandManager = playerCommandManager;
        this.islandManager = islandManager;
    }

    @Override
    public String getPermission() {
        return "playerislands.island." + getName().toLowerCase();
    }

    @Override
    public String getName() {
        return "unInvite";
    }

    @Override
    public String getDescription() {
        return "Cancel a pending invite";
    }

    @Override
    public String getSyntax() {
        return "/" + playerCommandManager.getCommandDisplayName() + " unInvite";
    }

    @Override
    public String getColoredSyntax() {
        return ChatColor.YELLOW + "Usage: " + getSyntax();
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return;

        if (!islandManager.isPlayerCreatorOfInvite((Player) sender)) {
            sender.sendMessage(Messages.noPendingInvite);
            return;
        }

        islandManager.onIslandInviteCancel((Player) sender);
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        return null;
    }
}