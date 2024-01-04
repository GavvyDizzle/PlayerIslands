package com.github.gavvydizzle.playerislands.commands;

import com.github.gavvydizzle.playerislands.PlayerIslands;
import com.github.gavvydizzle.playerislands.commands.admin.*;
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

public class AdminCommandManager extends CommandManager {

    private final PluginCommand command;
    private final IslandManager islandManager;
    private String commandDisplayName, helpCommandPadding;

    public AdminCommandManager(PluginCommand command, IslandManager islandManager) {
        super(command);
        this.command = command;
        this.islandManager = islandManager;

        registerCommand(new AddMemberCommand(this, islandManager));
        registerCommand(new AdminHelpCommand(this));
        registerCommand(new OpenAdminMenuCommand(this, islandManager));
        registerCommand(new PasteSizeUpgradeSchematic(this));
        registerCommand(new ReloadCommand(this));
        registerCommand(new ResetPlayerOwnedIslands(this, islandManager));
        registerCommand(new ResetIsland(this, islandManager));
        registerCommand(new SetIslandUpgrade(this, islandManager));

        sortCommands();

        reload();
    }

    // Call after PlayerCommandManager's reload
    public void reload() {
        FileConfiguration config = CommandsConfig.get();
        config.addDefault("commandDisplayName.admin", command.getName());
        config.addDefault("helpCommandPadding.admin", "&6-----(" + PlayerIslands.getInstance().getName() + " Admin Commands)-----");

        for (SubCommand subCommand : getSubcommands()) {
            CommandsConfig.setAdminDescriptionDefault(subCommand);
        }
        CommandsConfig.save();

        commandDisplayName = config.getString("commandDisplayName.admin");
        helpCommandPadding = Colors.conv(config.getString("helpCommandPadding.admin"));
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
}