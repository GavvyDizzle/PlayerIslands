package com.github.gavvydizzle.playerislands.utils;

import com.github.gavvydizzle.playerislands.configs.SoundsConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Objects;

public class Sounds {

    public static Sounds generalClickSound, generalFailSound;
    public static Sounds togglePrivacySound, upgradeSuccessful;
    public static Sounds promoteMemberSound, demoteMemberSound, kickMemberSound;

    static {
        generalClickSound = new Sounds(Sound.UI_BUTTON_CLICK, 1, 1);
        generalFailSound = new Sounds(Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 1, 1);
        togglePrivacySound = new Sounds(Sound.ITEM_BOOK_PAGE_TURN, 1, 1.5f);
        upgradeSuccessful = new Sounds(Sound.BLOCK_NOTE_BLOCK_CHIME, 1, 1.4f);
        promoteMemberSound = new Sounds(Sound.BLOCK_NOTE_BLOCK_BIT, 1, 2);
        demoteMemberSound = new Sounds(Sound.BLOCK_NOTE_BLOCK_BIT, 1, 1.6f);
        kickMemberSound = new Sounds(Sound.BLOCK_NOTE_BLOCK_BIT, 1, 0.5f);
    }

    public static void reload() {
        FileConfiguration config = SoundsConfig.get();
        config.options().copyDefaults(true);

        addDefault(config, "generalClickSound", generalClickSound);
        addDefault(config, "generalFailSound", generalFailSound);
        addDefault(config, "togglePrivacySound", togglePrivacySound);
        addDefault(config, "upgradeSuccessful", upgradeSuccessful);
        addDefault(config, "promoteMemberSound", promoteMemberSound);
        addDefault(config, "demoteMemberSound", demoteMemberSound);
        addDefault(config, "kickMemberSound", kickMemberSound);

        SoundsConfig.save();

        generalClickSound = getSound(config, "generalClickSound");
        generalFailSound = getSound(config, "generalFailSound");
        togglePrivacySound = getSound(config, "togglePrivacySound");
        upgradeSuccessful = getSound(config, "upgradeSuccessful");
        promoteMemberSound = getSound(config, "promoteMemberSound");
        demoteMemberSound = getSound(config, "demoteMemberSound");
        kickMemberSound = getSound(config, "kickMemberSound");
    }

    private static void addDefault(FileConfiguration config, String root, Sounds sound) {
        config.addDefault(root + ".enabled", true);
        config.addDefault(root + ".sound", sound.sound.toString().toUpperCase());
        config.addDefault(root + ".volume", sound.volume);
        config.addDefault(root + ".pitch", sound.pitch);
    }

    private static Sounds getSound(FileConfiguration config, String root) {
        if (!config.getBoolean(root + ".enabled")) return new Sounds(false);

        try {
            return new Sounds(Sound.valueOf(Objects.requireNonNull(config.getString(root + ".sound")).toUpperCase()),
                    (float) config.getDouble(root + ".volume"),
                    (float) config.getDouble(root + ".pitch"));
        } catch (Exception e) {
            Bukkit.getLogger().severe("Failed to load the sound: " + root + ". This sound will be muted until this error is fixed.");
            return new Sounds(false);
        }
    }

    private final boolean isEnabled;
    private final Sound sound;
    private final float volume;
    private final float pitch;

    public Sounds(boolean isEnabled) {
        this.isEnabled = isEnabled;
        this.sound = Sound.UI_BUTTON_CLICK;
        this.volume = 0;
        this.pitch = 0;
    }

    public Sounds(Sound sound, float volume, float pitch) {
        this.isEnabled = true;
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }

    /**
     * Plays the sound at the location for all players to hear.
     * @param loc The location to play the sound.
     */
    public void playSound(Location loc) {
        if (isEnabled && loc.getWorld() != null) loc.getWorld().playSound(loc, sound, volume, pitch);
    }

    /**
     * Plays the sound for only the player to hear.
     * @param p The player to play the sound for.
     */
    public void playSound(Player p) {
        if (isEnabled) p.playSound(p.getLocation(), sound, volume, pitch);
    }
}