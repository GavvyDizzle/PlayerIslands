package com.github.gavvydizzle.playerislands.commands.admin;

import com.github.gavvydizzle.playerislands.PlayerIslands;
import com.github.gavvydizzle.playerislands.commands.AdminCommandManager;
import com.github.gavvydizzle.playerislands.configs.*;
import com.github.gavvydizzle.playerislands.utils.Messages;
import com.github.gavvydizzle.playerislands.utils.Sounds;
import com.github.mittenmc.serverutils.PermissionCommand;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class ReloadCommand extends SubCommand implements PermissionCommand {

    private final AdminCommandManager adminCommandManager;
    private final ArrayList<String> argsList;

    public ReloadCommand(AdminCommandManager adminCommandManager) {
        this.adminCommandManager = adminCommandManager;
        argsList = new ArrayList<>();
        argsList.add("commands");
        argsList.add("gui");
        argsList.add("messages");
        argsList.add("sounds");
        argsList.add("upgrades");
        argsList.add("world");
    }

    @Override
    public String getPermission() {
        return "playerislands.islandadmin." + getName().toLowerCase();
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getDescription() {
        return "Reloads this plugin or a specified portion";
    }

    @Override
    public String getSyntax() {
        return "/" + adminCommandManager.getCommandDisplayName() + " reload [arg]";
    }

    @Override
    public String getColoredSyntax() {
        return ChatColor.YELLOW + "Usage: " + getSyntax();
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length >= 2) {
            switch (args[1].toLowerCase()) {
                case "commands":
                    reloadCommands();
                    sender.sendMessage(ChatColor.GREEN + "[" + PlayerIslands.getInstance().getName() + "] " + "Successfully reloaded commands");
                    break;
                case "gui":
                    reloadGUI();
                    sender.sendMessage(ChatColor.GREEN + "[" + PlayerIslands.getInstance().getName() + "] " + "Successfully reloaded all island menus");
                    break;
                case "messages":
                    reloadMessages();
                    sender.sendMessage(ChatColor.GREEN + "[" + PlayerIslands.getInstance().getName() + "] " + "Successfully reloaded all messages");
                    break;
                case "sounds":
                    reloadSounds();
                    sender.sendMessage(ChatColor.GREEN + "[" + PlayerIslands.getInstance().getName() + "] " + "Successfully reloaded all sounds");
                    break;
                case "upgrades":
                    reloadUpgrades();
                    sender.sendMessage(ChatColor.GREEN + "[" + PlayerIslands.getInstance().getName() + "] " + "Successfully reloaded all island upgrades");
                    break;
                case "world":
                    reloadWorld();
                    sender.sendMessage(ChatColor.GREEN + "[" + PlayerIslands.getInstance().getName() + "] " + "Successfully reloaded island world and refreshed region manager");
                    break;
                default:
            }
        }
        else {
            reloadCommands();
            reloadGUI();
            reloadMessages();
            reloadSounds();
            reloadUpgrades();
            reloadWorld();
            sender.sendMessage(ChatColor.GREEN + "[" + PlayerIslands.getInstance().getName() + "] " + "Successfully reloaded");
        }
    }

    @Override
    public List<String> getSubcommandArguments(Player player, String[] args) {
        ArrayList<String> list = new ArrayList<>();

        if (args.length == 2) {
            StringUtil.copyPartialMatches(args[1], argsList, list);
        }

        return list;
    }

    private void reloadCommands() {
        CommandsConfig.reload();
        PlayerIslands.getInstance().getPlayerCommandManager().reload();
        PlayerIslands.getInstance().getAdminCommandManager().reload();
    }

    private void reloadGUI() {
        GUIConfig.reload();
        PlayerIslands.getInstance().getInventoryManager().reload();
        PlayerIslands.getInstance().getIslandManager().handleGUIReload();
    }

    private void reloadMessages() {
        MessagesConfig.reload();
        Messages.reloadMessages();
    }

    private void reloadSounds() {
        SoundsConfig.reload();
        Sounds.reload();
    }

    private void reloadUpgrades() {
        UpgradesConfig.reload();
        PlayerIslands.getInstance().getUpgradeManager().reload();
        PlayerIslands.getInstance().getIslandManager().updateIslandSpawnPoints();
        PlayerIslands.getInstance().getIslandManager().handleUpgradeReload();
    }

    private void reloadWorld() {
        PlayerIslands.getInstance().reloadConfig();
        PlayerIslands.getInstance().getIslandManager().reload();
    }
}