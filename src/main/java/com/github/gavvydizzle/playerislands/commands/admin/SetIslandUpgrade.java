package com.github.gavvydizzle.playerislands.commands.admin;

import com.github.gavvydizzle.playerislands.PlayerIslands;
import com.github.gavvydizzle.playerislands.commands.AdminCommandManager;
import com.github.gavvydizzle.playerislands.island.Island;
import com.github.gavvydizzle.playerislands.island.IslandManager;
import com.github.gavvydizzle.playerislands.upgrade.MemberUpgrade;
import com.github.gavvydizzle.playerislands.upgrade.SizeUpgrade;
import com.github.gavvydizzle.playerislands.upgrade.UpgradeManager;
import com.github.gavvydizzle.playerislands.utils.Messages;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class SetIslandUpgrade extends SubCommand {

    private final IslandManager islandManager;
    private final ArrayList<String> upgradeTypes;

    public SetIslandUpgrade(AdminCommandManager adminCommandManager, IslandManager islandManager) {
        this.islandManager = islandManager;

        setName("setIslandUpgrade");
        setDescription("Change an island's upgrade level");
        setSyntax("/" + adminCommandManager.getCommandDisplayName() + " setIslandUpgrade <id> <upgradeType> <level>");
        setColoredSyntax(ChatColor.YELLOW + getSyntax());
        setPermission(adminCommandManager.getPermissionPrefix() + getName().toLowerCase());

        upgradeTypes = new ArrayList<>(2);
        upgradeTypes.add("member");
        upgradeTypes.add("size");
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return;

        if (args.length < 4) {
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

        int level;
        try {
            level = Integer.parseInt(args[3]);
        } catch (NumberFormatException ignored) {
            sender.sendMessage(ChatColor.RED + "Invalid level: " + args[3]);
            return;
        }

        UpgradeManager upgradeManager = PlayerIslands.getInstance().getUpgradeManager();
        if (args[2].equalsIgnoreCase("member")) {
            MemberUpgrade memberUpgrade = upgradeManager.getMemberUpgrade(level);
            if (memberUpgrade == null) {
                sender.sendMessage(ChatColor.RED + "No member upgrade found for level " + level);
            }
            island.setMemberUpgrade(level);
            sender.sendMessage(ChatColor.GREEN + "Successfully set island " + id + "'s member upgrade level to " + level);
        }
        else if (args[2].equalsIgnoreCase("size")) {
            SizeUpgrade sizeUpgrade = upgradeManager.getSizeUpgrade(level);
            if (sizeUpgrade == null) {
                sender.sendMessage(ChatColor.RED + "No size upgrade found for level " + level);
            }

            if (level < island.getSizeUpgrade().getUpgradeLevel()) {
                sender.sendMessage(ChatColor.RED + "Reducing the size of an island is not supported. If you wish to do this, reset it then enlarge it to the desired size");
                return;
            }

            island.setSizeUpgrade(level, true);
            sender.sendMessage(ChatColor.GREEN + "Successfully set island " + id + "'s size upgrade level to " + level);

            if (island.getNumMembers() > island.getMaxMembers()) {
                sender.sendMessage(ChatColor.YELLOW + "This island now has more members than it's size upgrade allows");
            }
        }
        else {
            sender.sendMessage(ChatColor.RED + args[2] + " is not a valid upgrade type");
        }
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        ArrayList<String> list = new ArrayList<>();

        if (args.length == 3) {
            StringUtil.copyPartialMatches(args[2], upgradeTypes, list);
        }

        return list;
    }
}