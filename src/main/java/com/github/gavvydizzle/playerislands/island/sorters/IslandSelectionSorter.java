package com.github.gavvydizzle.playerislands.island.sorters;

import com.github.gavvydizzle.playerislands.island.Island;
import org.bukkit.OfflinePlayer;

import java.util.Comparator;

public class IslandSelectionSorter implements Comparator<Island> {

    private final OfflinePlayer member;

    public IslandSelectionSorter(OfflinePlayer member) {
        this.member = member;
    }

    @Override
    public int compare(Island o1, Island o2) {
         if (o1.isOwner(member) && !o2.isOwner(member)) {
            return -1;
        }
        else if (!o1.isOwner(member) && o2.isOwner(member)) {
            return 1;
        }
        return Integer.compare(o1.getId(), o2.getId());
    }
}
