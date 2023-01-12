package com.github.gavvydizzle.playerislands.configs;

import com.github.gavvydizzle.playerislands.PlayerIslands;
import com.github.gavvydizzle.playerislands.commands.RankedCommand;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class CommandsConfig {

    private static File file;
    private static FileConfiguration fileConfiguration;

    static {
        setup();
        save();
    }

    //Finds or generates the config file
    public static void setup() {
        file = new File(PlayerIslands.getInstance().getDataFolder(), "commands.yml");
        fileConfiguration = YamlConfiguration.loadConfiguration(file);
    }

    public static FileConfiguration get(){
        return fileConfiguration;
    }

    public static void save() {
        try {
            fileConfiguration.save(file);
        }
        catch (IOException e) {
            System.out.println("Could not save file");
        }
    }

    public static void reload() {
        fileConfiguration = YamlConfiguration.loadConfiguration(file);
    }


    public static void setDescriptionDefault(SubCommand subCommand) {
        fileConfiguration.addDefault("descriptions.player." + subCommand.getName(), subCommand.getDescription());
    }

    /**
     * @param subCommand The SubCommand
     * @return The description of this SubCommand as defined in this config file
     */
    public static String getDescription(SubCommand subCommand) {
        return fileConfiguration.getString("descriptions.player." + subCommand.getName());
    }

    public static void setAdminDescriptionDefault(SubCommand subCommand) {
        fileConfiguration.addDefault("descriptions.admin." + subCommand.getName(), subCommand.getDescription());
    }

    /**
     * @param subCommand The SubCommand
     * @return The description of this SubCommand as defined in this config file
     */
    public static String getAdminDescription(SubCommand subCommand) {
        return fileConfiguration.getString("descriptions.admin." + subCommand.getName());
    }

    public static void setRequiredRankDefault(SubCommand subCommand) {
        if (!(subCommand instanceof RankedCommand)) return;
        fileConfiguration.addDefault("requiredRank." + subCommand.getName(), ((RankedCommand) subCommand).getDefaultRequiredRank().weight);
    }

    /**
     * @param subCommand The SubCommand
     * @return The weight value of the required rank
     */
    public static int getRequiredRank(SubCommand subCommand) {
        return fileConfiguration.getInt("requiredRank." + subCommand.getName());
    }

}
