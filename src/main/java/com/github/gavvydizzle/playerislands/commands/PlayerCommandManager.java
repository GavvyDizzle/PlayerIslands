package com.github.gavvydizzle.playerislands.commands;

import com.github.gavvydizzle.playerislands.PlayerIslands;
import com.github.gavvydizzle.playerislands.commands.player.*;
import com.github.gavvydizzle.playerislands.configs.CommandsConfig;
import com.github.gavvydizzle.playerislands.island.IslandManager;
import com.github.gavvydizzle.playerislands.utils.Messages;
import com.github.mittenmc.serverutils.Colors;
import com.github.mittenmc.serverutils.CommandManager;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

public class PlayerCommandManager extends CommandManager {

    private final PluginCommand command;
    private final IslandManager islandManager;
    private String commandDisplayName, helpCommandPadding;

    private final TogglePrivacyCommand togglePrivacyCommand;

    public PlayerCommandManager(PluginCommand command, IslandManager islandManager) {
        super(command);
        this.command = command;
        this.islandManager = islandManager;

        registerCommand(new CreateIslandCommand(this, islandManager));
        registerCommand(new HelpCommand(this));
        registerCommand(new HomeCommand(this));
        registerCommand(new InviteCommand(this, islandManager));
        registerCommand(new JoinCommand(this, islandManager));
        registerCommand(new LeaveCommand(this, islandManager));
        registerCommand(new OpenMenuCommand(this, islandManager));
        togglePrivacyCommand = new TogglePrivacyCommand(this, islandManager);
        registerCommand(togglePrivacyCommand);
        registerCommand(new UnInviteCommand(this, islandManager));
        registerCommand(new VisitCommand(this, islandManager));
        registerCommand(new VisitByIDCommand(this, islandManager));
        registerCommand(new VisitRandomIslandCommand(this, islandManager));

        sortCommands();

        reload();
    }

    // Call before AdminCommandManager's reload
    public void reload() {
        FileConfiguration config = CommandsConfig.get();
        config.options().copyDefaults(true);

        config.addDefault("commandDisplayName.player", command.getName());
        config.addDefault("helpCommandPadding.player", "&6-----(" + PlayerIslands.getInstance().getName() + " Commands)-----");

        for (SubCommand subCommand : getSubcommands()) {
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
            for (SubCommand subCommand : getSubcommands()) {
                if (args[0].equalsIgnoreCase(subCommand.getName())) {

                    if (!subCommand.hasPermission(sender)) {
                        onNoPermission(sender, args);
                        return true;
                    }

                    if (islandManager.isIslandWorldInvalid() && !subCommand.getName().equalsIgnoreCase("reload")) { // Allow reload command through invalid world check
                        sender.sendMessage(Messages.invalidIslandWorld);
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
            onInvalidSubcommand(sender, args);
            return true;
        }
        onNoSubcommand(sender, args);
        return true;
    }

    public TogglePrivacyCommand getTogglePrivacyCommand() {
        return togglePrivacyCommand;
    }
}