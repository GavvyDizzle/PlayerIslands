package com.github.gavvydizzle.playerislands.commands;

import com.github.gavvydizzle.playerislands.PlayerIslands;
import com.github.gavvydizzle.playerislands.commands.player.*;
import com.github.gavvydizzle.playerislands.configs.CommandsConfig;
import com.github.gavvydizzle.playerislands.island.IslandManager;
import com.github.gavvydizzle.playerislands.utils.Messages;
import com.github.mittenmc.serverutils.Colors;
import com.github.mittenmc.serverutils.PermissionCommand;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PlayerCommandManager implements TabExecutor {

    private final PluginCommand command;
    private final IslandManager islandManager;
    private final ArrayList<SubCommand> subcommands = new ArrayList<>();
    private final ArrayList<String> subcommandStrings = new ArrayList<>();
    private String commandDisplayName, helpCommandPadding;

    private final TogglePrivacyCommand togglePrivacyCommand;

    public PlayerCommandManager(PluginCommand command, IslandManager islandManager) {
        this.command = command;
        command.setExecutor(this);
        this.islandManager = islandManager;

        subcommands.add(new CreateIslandCommand(this, islandManager));
        subcommands.add(new HelpCommand(this));
        subcommands.add(new HomeCommand(this));
        subcommands.add(new InviteCommand(this, islandManager));
        subcommands.add(new JoinCommand(this, islandManager));
        subcommands.add(new LeaveCommand(this, islandManager));
        subcommands.add(new OpenMenuCommand(this, islandManager));
        togglePrivacyCommand = new TogglePrivacyCommand(this, islandManager);
        subcommands.add(togglePrivacyCommand);
        subcommands.add(new UnInviteCommand(this, islandManager));
        subcommands.add(new VisitCommand(this, islandManager));
        subcommands.add(new VisitByIDCommand(this, islandManager));
        subcommands.add(new VisitRandomIslandCommand(this, islandManager));

        for (SubCommand subCommand : subcommands) {
            subcommandStrings.add(subCommand.getName());
        }

        reload();
    }

    // Call before AdminCommandManager's reload
    public void reload() {
        FileConfiguration config = CommandsConfig.get();
        config.options().copyDefaults(true);

        config.addDefault("commandDisplayName.player", command.getName());
        config.addDefault("helpCommandPadding.player", "&6-----(" + PlayerIslands.getInstance().getName() + " Commands)-----");

        for (SubCommand subCommand : subcommands) {
            CommandsConfig.setDescriptionDefault(subCommand);

            if (subCommand instanceof RankedCommand) {
                CommandsConfig.setRequiredRankDefault(subCommand);
            }
        }

        commandDisplayName = config.getString("commandDisplayName.player");
        helpCommandPadding = Colors.conv(config.getString("helpCommandPadding.player"));
    }

    public String getCommandDisplayName() {
        return commandDisplayName;
    }

    public String getHelpCommandPadding() {
        return helpCommandPadding;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (islandManager.isDatabaseDataNotLoaded()) {
            sender.sendMessage(Messages.islandsNotLoadedYet);
            return true;
        }
        else if (islandManager.isIslandWorldInvalid()) {
            sender.sendMessage(Messages.invalidIslandWorld);
            return true;
        }

        if (args.length > 0) {
            for (int i = 0; i < getSubcommands().size(); i++) {
                if (args[0].equalsIgnoreCase(getSubcommands().get(i).getName())) {

                    SubCommand subCommand = subcommands.get(i);

                    if (subCommand instanceof PermissionCommand &&
                            !sender.hasPermission(((PermissionCommand) subCommand).getPermission())) {
                        sender.sendMessage(ChatColor.RED + "Insufficient permission");
                        return true;
                    }

                    if (subCommand instanceof IslandSelectionCommand) {
                        if (((IslandSelectionCommand) subCommand).isCommandValid(sender, args)) {
                            PlayerIslands.getInstance().getIslandSelectionManager().createSelection((IslandSelectionCommand) subCommand, sender, ((IslandSelectionCommand) subCommand).getIslandMember(sender, args), args);
                        }
                        return true;
                    }

                    subCommand.perform(sender, args);
                    return true;
                }
            }
            sender.sendMessage(ChatColor.RED + "Invalid command");
        }
        sender.sendMessage(ChatColor.YELLOW + "Use '/" + commandDisplayName + " help' to see a list of valid commands");

        return true;
    }

    public ArrayList<SubCommand> getSubcommands(){
        return subcommands;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length == 1) {
            ArrayList<String> subcommandsArguments = new ArrayList<>();

            StringUtil.copyPartialMatches(args[0], subcommandStrings, subcommandsArguments);

            return subcommandsArguments;
        }
        else if (args.length >= 2) {
            for (SubCommand subcommand : subcommands) {
                if (args[0].equalsIgnoreCase(subcommand.getName())) {
                    return subcommand.getSubcommandArguments((Player) sender, args);
                }
            }
        }

        return null;
    }

    public PluginCommand getCommand() {
        return command;
    }

    public TogglePrivacyCommand getTogglePrivacyCommand() {
        return togglePrivacyCommand;
    }
}