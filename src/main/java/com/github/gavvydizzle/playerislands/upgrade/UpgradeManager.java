package com.github.gavvydizzle.playerislands.upgrade;

import com.github.gavvydizzle.playerislands.PlayerIslands;
import com.github.gavvydizzle.playerislands.configs.UpgradesConfig;
import com.github.gavvydizzle.playerislands.utils.Dimension;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Objects;

public class UpgradeManager {

    // The first upgrade in the list is the default (level=0). All other levels are purchasable (level=index)
    private final ArrayList<SizeUpgrade> sizeUpgrades;
    private final ArrayList<MemberUpgrade> memberUpgrades;

    public UpgradeManager() {
        sizeUpgrades = new ArrayList<>();
        memberUpgrades = new ArrayList<>();
        reload();
    }

    public void reload() {
        FileConfiguration config = UpgradesConfig.get();
        config.options().copyDefaults(true);

        config.addDefault("sizeUpgrades.default.price", 0);
        config.addDefault("sizeUpgrades.default.permission", "");
        config.addDefault("sizeUpgrades.default.schematicName", "");
        config.addDefault("sizeUpgrades.default.dimensions.x", 5);
        config.addDefault("sizeUpgrades.default.dimensions.y", 3);
        config.addDefault("sizeUpgrades.default.dimensions.z", 5);
        config.addDefault("sizeUpgrades.default.regionOffset.x", 0);
        config.addDefault("sizeUpgrades.default.regionOffset.y", 0);
        config.addDefault("sizeUpgrades.default.regionOffset.z", 0);
        config.addDefault("sizeUpgrades.default.spawnOffset.x", 0);
        config.addDefault("sizeUpgrades.default.spawnOffset.y", 0);
        config.addDefault("sizeUpgrades.default.spawnOffset.z", 0);
        config.addDefault("sizeUpgrades.default.spawnOffset.pitch", 0.0);
        config.addDefault("sizeUpgrades.default.spawnOffset.yaw", 0.0);

        config.addDefault("memberUpgrades.default.price", 0);
        config.addDefault("sizeUpgrades.default.permission", "");
        config.addDefault("memberUpgrades.default.numMembers", 3);

        UpgradesConfig.save();

        if (config.getConfigurationSection("sizeUpgrades") != null) {
            sizeUpgrades.clear();
            for (String key : Objects.requireNonNull(config.getConfigurationSection("sizeUpgrades")).getKeys(false)) {
                String path = "sizeUpgrades." + key;

                long price = config.getInt(path + ".price");
                if (price < 0) {
                    Bukkit.getLogger().warning("You cannot have an upgrade price be negative (" + path + ".price). The price has been set to 0");
                    price = 0;
                }

                sizeUpgrades.add(new SizeUpgrade(
                        price,
                        sizeUpgrades.size(),
                        config.getString(path + ".permission"),
                        config.getString(path + ".schematicName"),
                        new Dimension(config.getInt(path + ".dimensions.x"), config.getInt(path + ".dimensions.y"), config.getInt(path + ".dimensions.z")),
                        new Dimension(config.getInt(path + ".regionOffset.x"), config.getInt(path + ".regionOffset.y"), config.getInt(path + ".regionOffset.z")),
                        new Dimension(config.getInt(path + ".spawnOffset.x"), config.getInt(path + ".spawnOffset.y"), config.getInt(path + ".spawnOffset.z")),
                        (float) config.getDouble(path + ".spawnOffset.pitch"),
                        (float) config.getDouble(path + ".spawnOffset.yaw")
                ));
            }
        }

        if (config.getConfigurationSection("memberUpgrades") != null) {
            memberUpgrades.clear();
            for (String key : Objects.requireNonNull(config.getConfigurationSection("memberUpgrades")).getKeys(false)) {
                String path = "memberUpgrades." + key;

                long price = config.getInt(path + ".price");
                if (price < 0) {
                    Bukkit.getLogger().warning("You cannot have an upgrade price be negative (" + path + ".price). The price has been set to 0");
                    price = 0;
                }

                int numMembers = config.getInt(path + ".numMembers");
                if (numMembers < 0) {
                    Bukkit.getLogger().warning("You cannot have the number of members be negative (" + path + ".multiplier). The number of members has been set to 0");
                    numMembers = 0;
                }

                memberUpgrades.add(new MemberUpgrade(price, memberUpgrades.size(), config.getString(path + ".permission"), numMembers));
            }
        }

        // No schematic for default size upgrade
        if (sizeUpgrades.get(0).getClipboard() == null) {
            PlayerIslands.getInstance().getLogger().warning("You have not provided a schematic file for the default size upgrade in upgrades.yml. Newly created islands will not have any blocks!");
        }
    }

    /**
     * Pastes the schematic for this upgrade into the world at the given location if the schematic is successfully loaded.
     *
     * @param loc The location to paste at
     * @param lastUpgrade The last SizeUpgrade to use for area preservation
     * @param upgrade The SizeUpgrade
     * @param keepExistingBlocks True if the existing blocks should be pasted back after the schematic is pasted
     */
    public void pasteSchematic(Location loc, @Nullable SizeUpgrade lastUpgrade, @NotNull SizeUpgrade upgrade, boolean keepExistingBlocks) {
        Clipboard clipboard = upgrade.getClipboard();
        if (clipboard == null) return; // Don't paste if the clipboard is null

        try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(Objects.requireNonNull(loc.getWorld())))) {

            BlockVector3 origin = BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());

            if (lastUpgrade != null) {
                BlockVector3 corner2 = origin.add(lastUpgrade.getRegionDimension().getX(), lastUpgrade.getRegionDimension().getY(), lastUpgrade.getRegionDimension().getZ());

                // Copy existing island
                CuboidRegion region = new CuboidRegion(origin, corner2);
                BlockArrayClipboard bac = new BlockArrayClipboard(region);

                ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(BukkitAdapter.adapt(loc.getWorld()), region, bac, region.getMinimumPoint());
                forwardExtentCopy.setCopyingEntities(false);
                Operations.complete(forwardExtentCopy);

                // Paste new island schematic
                Operation operation = new ClipboardHolder(clipboard)
                        .createPaste(editSession)
                        .to(origin)
                        .copyBiomes(false)
                        .copyEntities(false)
                        .build();
                Operations.complete(operation);

                if (keepExistingBlocks) { // Paste old island blocks back in
                    operation = new ClipboardHolder(bac)
                            .createPaste(editSession)
                            .to(origin)
                            .copyBiomes(false)
                            .copyEntities(true)
                            .ignoreAirBlocks(false)
                            .build();
                    Operations.complete(operation);
                }
            }
            else {
                Operation operation = new ClipboardHolder(clipboard)
                        .createPaste(editSession)
                        .to(origin)
                        .copyBiomes(false)
                        .copyEntities(false)
                        .build();
                Operations.complete(operation);
            }
        } catch (Exception e) {
            PlayerIslands.getInstance().getLogger().severe(e.getMessage());
            e.printStackTrace();
        }
    }

    public MemberUpgrade getMemberUpgrade(int level) {
        if (level < 0 || level >= memberUpgrades.size()) return null;
        return memberUpgrades.get(level);
    }

    public SizeUpgrade getSizeUpgrade(int level) {
        if (level < 0 || level >= sizeUpgrades.size()) return null;
        return sizeUpgrades.get(level);
    }

}
