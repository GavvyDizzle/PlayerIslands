package com.github.gavvydizzle.playerislands.commands;

import com.github.gavvydizzle.playerislands.PlayerIslands;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class IslandSelection {

    private final IslandSelectionCommand command;
    private final Player player;
    private final String[] args;
    private final int taskID;

    public IslandSelection(IslandSelectionCommand command, Player player, String[] args) {
        this.command = command;
        this.player = player;
        this.args = args;

        taskID = Bukkit.getScheduler().scheduleSyncDelayedTask(PlayerIslands.getInstance(), () -> PlayerIslands.getInstance().getIslandSelectionManager().onConfirmExpire(player), 200);
    }

    public IslandSelectionCommand getCommand() {
        return command;
    }

    public CommandSender getCommandSender() {
        return player;
    }

    public String[] getArgs() {
        return args;
    }

    public void cancelTask() {
        Bukkit.getScheduler().cancelTask(taskID);
    }
}
