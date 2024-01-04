package com.github.gavvydizzle.playerislands.commands.admin;

import com.github.gavvydizzle.playerislands.PlayerIslands;
import com.github.gavvydizzle.playerislands.commands.AdminCommandManager;
import com.github.gavvydizzle.playerislands.upgrade.SizeUpgrade;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PasteSizeUpgradeSchematic extends SubCommand {

    public PasteSizeUpgradeSchematic(AdminCommandManager adminCommandManager) {
        setName("pasteSchematic");
        setDescription("Pastes the schematic for the given size level");
        setSyntax("/" + adminCommandManager.getCommandDisplayName() + " pasteSchematic <upgradeLevel>");
        setColoredSyntax(ChatColor.YELLOW + getSyntax());
        setPermission(adminCommandManager.getPermissionPrefix() + getName().toLowerCase());
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return;

        if (args.length < 2) {
            sender.sendMessage(getColoredSyntax());
            return;
        }

        int level;
        try {
            level = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid number");
            return;
        }

        SizeUpgrade sizeUpgrade = PlayerIslands.getInstance().getUpgradeManager().getSizeUpgrade(level);
        if (sizeUpgrade == null) {
            sender.sendMessage(ChatColor.RED + "Invalid size upgrade level");
            return;
        }

        PlayerIslands.getInstance().getUpgradeManager().pasteSchematic(((Player) sender).getLocation(), PlayerIslands.getInstance().getUpgradeManager().getSizeUpgrade(level-1), sizeUpgrade, true);
        sender.sendMessage(ChatColor.GREEN + "Successfully pasted schematic from size upgrade " + sizeUpgrade.getUpgradeLevel());
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}