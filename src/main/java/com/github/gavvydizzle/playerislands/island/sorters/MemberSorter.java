package com.github.gavvydizzle.playerislands.island.sorters;

import com.github.gavvydizzle.playerislands.island.IslandMember;

import java.util.Comparator;

public class MemberSorter implements Comparator<IslandMember> {
    @Override
    public int compare(IslandMember o1, IslandMember o2) {
        if (o1.getMemberType() != o2.getMemberType()) {
            return Integer.compare(o2.getMemberType().weight, o1.getMemberType().weight);
        }
        return o1.getPlayerName().compareTo(o2.getPlayerName());
    }
}
