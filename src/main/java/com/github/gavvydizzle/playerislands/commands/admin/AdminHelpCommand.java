package com.github.gavvydizzle.playerislands.commands.admin;

import com.github.gavvydizzle.playerislands.commands.AdminCommandManager;
import com.github.gavvydizzle.playerislands.configs.CommandsConfig;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class AdminHelpCommand extends SubCommand {

    private final AdminCommandManager adminCommandManager;

    public AdminHelpCommand(AdminCommandManager adminCommandManager) {
        this.adminCommandManager = adminCommandManager;

        setName("help");
        setDescription("Opens this help menu");
        setSyntax("/" + adminCommandManager.getCommandDisplayName() + " help");
        setColoredSyntax(ChatColor.YELLOW + getSyntax());
        setPermission(adminCommandManager.getPermissionPrefix() + getName().toLowerCase());
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        String padding = adminCommandManager.getHelpCommandPadding();

        if (!padding.isEmpty()) sender.sendMessage(padding);
        ArrayList<SubCommand> subCommands = adminCommandManager.getSubcommands();
        for (SubCommand subCommand : subCommands) {
            sender.sendMessage(ChatColor.GOLD + subCommand.getSyntax() + " - " + ChatColor.YELLOW + CommandsConfig.getAdminDescription(subCommand));
        }
        if (!padding.isEmpty()) sender.sendMessage(padding);
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}