package com.github.gavvydizzle.playerislands.commands;

import com.github.gavvydizzle.playerislands.island.MemberType;

public interface RankedCommand {

    /**
     * Gets the default rank for this command to put in the config
     * @return The default MemberType
     */
    MemberType getDefaultRequiredRank();

    /**
     * Gets the rank for this command as defined in the config
     * @return A MemberType
     */
    MemberType getRequiredRank();

}
