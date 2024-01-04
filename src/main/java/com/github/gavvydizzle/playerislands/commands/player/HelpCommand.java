package com.github.gavvydizzle.playerislands.commands.player;

import com.github.gavvydizzle.playerislands.commands.PlayerCommandManager;
import com.github.gavvydizzle.playerislands.configs.CommandsConfig;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class HelpCommand extends SubCommand {

    private final PlayerCommandManager playerCommandManager;

    public HelpCommand(PlayerCommandManager playerCommandManager) {
        this.playerCommandManager = playerCommandManager;

        setName("help");
        setDescription("Display these help messages");
        setSyntax("/" + playerCommandManager.getCommandDisplayName() + " help");
        setColoredSyntax(ChatColor.YELLOW + getSyntax());
        setPermission(playerCommandManager.getPermissionPrefix() + getName().toLowerCase());
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        String padding = playerCommandManager.getHelpCommandPadding();

        if (!padding.isEmpty()) sender.sendMessage(padding);
        ArrayList<SubCommand> subCommands = playerCommandManager.getSubcommands();
        for (SubCommand subCommand : subCommands) {
             sender.sendMessage(ChatColor.GOLD + subCommand.getSyntax() + " - " + ChatColor.YELLOW + CommandsConfig.getDescription(subCommand));
        }
        if (!padding.isEmpty()) sender.sendMessage(padding);
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}