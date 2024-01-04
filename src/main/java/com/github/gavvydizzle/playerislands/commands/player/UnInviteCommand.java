package com.github.gavvydizzle.playerislands.commands.player;

import com.github.gavvydizzle.playerislands.commands.PlayerCommandManager;
import com.github.gavvydizzle.playerislands.island.IslandManager;
import com.github.gavvydizzle.playerislands.utils.Messages;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class UnInviteCommand extends SubCommand {

    private final IslandManager islandManager;

    public UnInviteCommand(PlayerCommandManager playerCommandManager, IslandManager islandManager) {
        this.islandManager = islandManager;

        setName("unInvite");
        setDescription("Cancel a pending invite");
        setSyntax("/" + playerCommandManager.getCommandDisplayName() + " unInvite");
        setColoredSyntax(ChatColor.YELLOW + getSyntax());
        setPermission(playerCommandManager.getPermissionPrefix() + getName().toLowerCase());
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