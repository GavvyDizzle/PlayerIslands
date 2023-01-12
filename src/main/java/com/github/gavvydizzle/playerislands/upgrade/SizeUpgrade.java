package com.github.gavvydizzle.playerislands.upgrade;

import com.github.gavvydizzle.playerislands.PlayerIslands;
import com.github.gavvydizzle.playerislands.utils.Dimension;
import com.github.mittenmc.serverutils.Numbers;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Files;

public class SizeUpgrade extends Upgrade {


    private final String schematicName;
    private final Dimension dimension, regionDimension, regionOffset, spawnOffset;
    private final float pitch, yaw;

    public SizeUpgrade(long price, int level, String permission, @Nullable String schematicName, Dimension dimension, Dimension regionOffset, Dimension spawnOffset, float pitch, float yaw) {
        super(price, level, permission);
        this.dimension = dimension;
        this.regionDimension = new Dimension(dimension.getX()-1, dimension.getY()-1, dimension.getZ()-1);
        this.regionOffset = regionOffset;
        this.spawnOffset = spawnOffset;
        this.schematicName = schematicName;
        this.pitch = pitch;
        this.yaw = yaw;
    }

    public Dimension getDimension() {
        return dimension;
    }

    public Dimension getRegionDimension() {
        return regionDimension;
    }

    public Dimension getRegionOffset() {
        return regionOffset;
    }

    public Dimension getSpawnOffset() {
        return spawnOffset;
    }

    public float getPitch() {
        return pitch;
    }

    public float getYaw() {
        return yaw;
    }

    /**
     * Gets the Clipboard object associated with this saves schematic file
     * @return The Clipboard or null if an error occurred or the schematicName is empty
     */
    public Clipboard getClipboard() {
        if (schematicName == null || schematicName.trim().isEmpty()) return null;

        Clipboard clipboard;
        File file = new File(PlayerIslands.getInstance().getDataFolder(), "schematics/" + schematicName);

        ClipboardFormat format = ClipboardFormats.findByFile(file);
        if (format == null) {
            PlayerIslands.getInstance().getLogger().severe("Error turning " + schematicName + " into a file! Make sure it is in the PlayerIslands/schematics folder");
            return null;
        }

        try (ClipboardReader reader = format.getReader(Files.newInputStream(file.toPath()))) {
            clipboard = reader.read();
        } catch (Exception e) {
            PlayerIslands.getInstance().getLogger().severe(e.getMessage());
            return null;
        }

        return clipboard;
    }

    @Override
    public String toString() {
        return "SizeUpgrade " + getUpgradeLevel() + "\n" +
                "Price: $" + Numbers.withSuffix(getPrice()) + "\n" +
                "Schematic: " + schematicName + "\n" +
                "Dimensions: " + dimension + "\n" +
                "Offset: " + spawnOffset + "  Pitch: " + pitch + "  Yaw: " + yaw;
    }
}
