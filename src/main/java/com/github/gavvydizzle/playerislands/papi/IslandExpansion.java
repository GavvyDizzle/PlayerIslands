package com.github.gavvydizzle.playerislands.papi;

import com.github.gavvydizzle.playerislands.island.Island;
import com.github.gavvydizzle.playerislands.island.IslandManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class IslandExpansion extends PlaceholderExpansion {

    private final IslandManager islandManager;

    public IslandExpansion(IslandManager islandManager) {
        this.islandManager = islandManager;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "playerislands";
    }

    @Override
    public @NotNull String getAuthor() {
        return "GavvyDizzle";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (!player.isOnline()) return null;

        Island island = null;
        if (params.startsWith("current_island_")) {
            island = islandManager.getIslandByPlayerLocation(Objects.requireNonNull(player.getPlayer()));
        }
        else if (params.startsWith("island_")) {
            int id;
            try {
                id = Integer.parseInt(params.split("_")[1]);
            } catch (Exception ignored) {
                return null;
            }
            island = islandManager.getIslandByID(id);
        }

        if (island == null) return "null";

        switch (params.split("_")[2]) {
            case "owner":
                return island.getOwner().getPlayerName();
            case "id":
                return "" + island.getId();
            case "privacy":
                return island.isPrivate() ? "private" : "public";
            case "privacyUppercase":
                return island.isPrivate() ? "Private" : "Public";
            case "isMember":
                return island.isMember(player) ? "true" : "false";
            case "isOwner":
                return island.isOwner(player) ? "true" : "false";
        }

        return null;
    }
}