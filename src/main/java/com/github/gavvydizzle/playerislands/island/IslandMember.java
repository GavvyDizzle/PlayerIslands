package com.github.gavvydizzle.playerislands.island;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class IslandMember {

    private final OfflinePlayer offlinePlayer;
    private final String playerName;
    private final int islandID;
    private MemberType memberType;

    public IslandMember(UUID uuid, int islandID, int memberWeight) {
        this.offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        this.islandID = islandID;
        this.memberType = MemberType.getTypeByWeight(memberWeight);

        if (offlinePlayer.getName() != null) {
            playerName = offlinePlayer.getName();
        }
        else {
            playerName = "null";
        }
    }

    public IslandMember(UUID uuid, int islandID, MemberType memberType) {
        this.offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        this.islandID = islandID;
        this.memberType = memberType;

        if (offlinePlayer.getName() != null) {
            playerName = offlinePlayer.getName();
        }
        else {
            playerName = "null";
        }
    }

    /**
     * Sends a message to this cell member if they are online
     * @param messages The message(s) to send
     */
    public void sendMessage(String... messages) {
        if (offlinePlayer.isOnline()) {
            Player player = Bukkit.getPlayer(offlinePlayer.getUniqueId());
            if (player == null) return;

            player.sendMessage(messages);
        }
    }


    public OfflinePlayer getOfflinePlayer() {
        return offlinePlayer;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getIslandID() {
        return islandID;
    }

    public MemberType getMemberType() {
        return memberType;
    }

    public void setMemberType(MemberType memberType) {
        this.memberType = memberType;
    }
}
