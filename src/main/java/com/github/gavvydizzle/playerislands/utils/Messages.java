package com.github.gavvydizzle.playerislands.utils;

import com.github.gavvydizzle.playerislands.configs.MessagesConfig;
import com.github.mittenmc.serverutils.Colors;
import org.bukkit.configuration.file.FileConfiguration;

public class Messages {

    // Load errors
    public static String islandsNotLoadedYet, invalidIslandWorld;

    // General errors
    public static String pleaseWait, playerNotFound, playerAlreadyMember, invalidIslandId, noValidIslands, noPublicIslandsAvailable, ownsNoIslands, belongsToNoIslands, doesNotBelongToThisIsland;

    // Island Actions
    public static String mustBeIslandOwner, tooLowRank, maxIslandsReached, islandCreated, successfulLeave, playerLeftIsland, successfulDemote, demoted, tooLowRankToDemote,
            successfulPromote, tooLowRankToPromote, alreadyThisRank, promoted, successfulKick, kicked, tooLowRankToKick, successfulAdminAdd, addedByAdmin;

    // Upgrades
    public static String upgradeMaxLevel, playerCannotAffordUpgrade, successfulPurchase, insufficientUpgradePermission;

    // Invites
    public static String inviteSent, inviteReceived, sentInviteAccepted, receivedInviteAccepted, sentInviteExpired, receivedInviteExpired, sentInviteCancelled, receivedInviteCancelled, noPendingInvite, maxMembersReached;
    public static String noOutstandingInvite, outstandingInvite, otherOutstandingInvite;

    // Selection
    public static String selectionMessage, selectionPending, selectionExpire, selectionCancelled;

    // Privacy
    public static String islandNowPrivate, islandNowPublic, onPrivateIslandTeleport;

    public static void reloadMessages() {
        FileConfiguration config = MessagesConfig.get();
        config.options().copyDefaults(true);

        // Load errors
        config.addDefault("islandsNotLoadedYet", "&cPlease wait. This plugin is still loading");
        config.addDefault("invalidIslandWorld", "&cCommands are disabled until a valid island world is given");

        // General errors
        config.addDefault("pleaseWait", "&cPlease wait before trying again");
        config.addDefault("playerNotFound", "&c{player_name} is not a valid player");
        config.addDefault("playerAlreadyMember", "&c{player_name} is already a member of your island");
        config.addDefault("invalidIslandId", "&cNo island exists with the id '{id}'");
        config.addDefault("noValidIslands", "&cYou have no islands to execute this command on");
        config.addDefault("noPublicIslandsAvailable", "&cThere are no public islands to teleport to");
        config.addDefault("ownsNoIslands", "&c{player_name} does not own to any islands");
        config.addDefault("belongsToNoIslands", "&c{player_name} does not belong to any islands");
        config.addDefault("doesNotBelongToThisIsland", "&cYou don't belong to this island");

        // Island Actions
        config.addDefault("mustBeIslandOwner", "&cOnly the island owner can access this");
        config.addDefault("tooLowRank", "&cYour rank is too low to complete this action");
        config.addDefault("maxIslandsReached", "&cYou have created the maximum number of islands");
        config.addDefault("islandCreated", "&aSuccessfully created your new island. Use '/is home' to visit it");
        config.addDefault("successfulLeave", "&aYou have successfully left {owner}'s island");
        config.addDefault("playerLeftIsland", "&e{player_name} has left your island ({id})");
        config.addDefault("successfulDemote", "&aSuccessfully demoted {player_name} to {rank}");
        config.addDefault("demoted", "&eYou have been demoted to {rank} on {owner}'s island");
        config.addDefault("tooLowRankToDemote", "&cYou are not a high enough rank to demote {rank}s");
        config.addDefault("successfulPromote", "&aSuccessfully promoted {player_name} to {rank}");
        config.addDefault("promoted", "&eYou have been promoted to {rank} on {owner}'s island");
        config.addDefault("tooLowRankToPromote", "&cYou are not a high enough rank to promote {rank}s");
        config.addDefault("alreadyThisRank", "&c{player_name} is already a {rank}");
        config.addDefault("successfulKick", "&aSuccessfully kicked {player_name} from {owner}'s island");
        config.addDefault("kicked", "&eYou have been kicked from {owner}'s island");
        config.addDefault("tooLowRankToKick", "&cYou are not a high enough rank to kick {rank}s");
        config.addDefault("successfulAdminAdd", "&aSuccessfully added {player_name} to island {id}");
        config.addDefault("addedByAdmin", "&aYou were forcefully made a member to island {id} by an admin");

        // Upgrades
        config.addDefault("upgradeMaxLevel", "&cThis upgrade is at max level");
        config.addDefault("playerCannotAffordUpgrade", "&cYou cannot afford this upgrade");
        config.addDefault("successfulPurchase", "&aSuccessfully purchased the upgrade for ${price}");
        config.addDefault("insufficientUpgradePermission", "&cYou don't have permission to purchase this upgrade");

        // Invites
        config.addDefault("inviteSent", "&aSuccessfully sent an invite to {player_name}");
        config.addDefault("inviteReceived", "&e(!) {player_name} is inviting you to join island {id}. Click here to join");
        config.addDefault("sentInviteAccepted", "&aYour invite has been accepted");
        config.addDefault("receivedInviteAccepted", "&aInvite accepted. You are now a member of island {id}");
        config.addDefault("sentInviteExpired", "&cYour invite to {player_name} has expired");
        config.addDefault("receivedInviteExpired", "&cYour invite from {player_name} has expired");
        config.addDefault("sentInviteCancelled", "&cCancelled your invite to {player_name}");
        config.addDefault("receivedInviteCancelled", "&cYour invite from {player_name} has been cancelled");
        config.addDefault("noPendingInvite", "&cYou do not have a pending invite");
        config.addDefault("maxMembersReached", "&cThe island is at maximum player capacity");
        config.addDefault("noOutstandingInvite", "&cYou do not have an outstanding invite");
        config.addDefault("outstandingInvite", "&cYou cannot do this with an outstanding invite");
        config.addDefault("otherOutstandingInvite", "&cYou cannot invite this player because they have an outstanding invite");

        // Selection
        config.addDefault("selectionMessage", "&eSelect an island to continue");
        config.addDefault("selectionPending", "&cYou cannot execute this command with a selection confirmation");
        config.addDefault("selectionExpire", "&cSelection expired");
        config.addDefault("selectionCancelled", "&cSelection cancelled");

        // Privacy
        config.addDefault("islandNowPrivate", "&eYour island is now private");
        config.addDefault("islandNowPublic", "&eYour island is now public");
        config.addDefault("onPrivateIslandTeleport", "&cYou cannot visit this island because it is private");

        MessagesConfig.save();

        // Load errors
        islandsNotLoadedYet = Colors.conv(config.getString("islandsNotLoadedYet"));
        invalidIslandWorld = Colors.conv(config.getString("invalidIslandWorld"));

        // General errors
        pleaseWait = Colors.conv(config.getString("pleaseWait"));
        playerNotFound = Colors.conv(config.getString("playerNotFound"));
        playerAlreadyMember = Colors.conv(config.getString("playerAlreadyMember"));
        invalidIslandId = Colors.conv(config.getString("invalidIslandId"));
        noValidIslands = Colors.conv(config.getString("noValidIslands"));
        noPublicIslandsAvailable = Colors.conv(config.getString("noPublicIslandsAvailable"));
        ownsNoIslands = Colors.conv(config.getString("ownsNoIslands"));
        belongsToNoIslands = Colors.conv(config.getString("belongsToNoIslands"));
        doesNotBelongToThisIsland = Colors.conv(config.getString("doesNotBelongToThisIsland"));

        // Island Actions
        mustBeIslandOwner = Colors.conv(config.getString("mustBeIslandOwner"));
        tooLowRank = Colors.conv(config.getString("tooLowRank"));
        maxIslandsReached = Colors.conv(config.getString("maxIslandsReached"));
        islandCreated = Colors.conv(config.getString("islandCreated"));
        successfulLeave = Colors.conv(config.getString("successfulLeave"));
        playerLeftIsland = Colors.conv(config.getString("playerLeftIsland"));
        successfulDemote = Colors.conv(config.getString("successfulDemote"));
        demoted = Colors.conv(config.getString("demoted"));
        tooLowRankToDemote = Colors.conv(config.getString("tooLowRankToDemote"));
        successfulPromote = Colors.conv(config.getString("successfulPromote"));
        promoted = Colors.conv(config.getString("promoted"));
        tooLowRankToPromote = Colors.conv(config.getString("tooLowRankToPromote"));
        alreadyThisRank = Colors.conv(config.getString("alreadyThisRank"));
        successfulKick = Colors.conv(config.getString("successfulKick"));
        kicked = Colors.conv(config.getString("kicked"));
        tooLowRankToKick = Colors.conv(config.getString("tooLowRankToKick"));
        successfulAdminAdd = Colors.conv(config.getString("successfulAdminAdd"));
        addedByAdmin = Colors.conv(config.getString("addedByAdmin"));

        // Upgrades
        upgradeMaxLevel = Colors.conv(config.getString("upgradeMaxLevel"));
        playerCannotAffordUpgrade = Colors.conv(config.getString("playerCannotAffordUpgrade"));
        successfulPurchase = Colors.conv(config.getString("successfulPurchase"));
        insufficientUpgradePermission = Colors.conv(config.getString("insufficientUpgradePermission"));

        // Invites
        inviteSent = Colors.conv(config.getString("inviteSent"));
        inviteReceived = Colors.conv(config.getString("inviteReceived"));
        sentInviteAccepted = Colors.conv(config.getString("sentInviteAccepted"));
        receivedInviteAccepted = Colors.conv(config.getString("receivedInviteAccepted"));
        sentInviteExpired = Colors.conv(config.getString("sentInviteExpired"));
        receivedInviteExpired = Colors.conv(config.getString("receivedInviteExpired"));
        sentInviteCancelled = Colors.conv(config.getString("sentInviteCancelled"));
        receivedInviteCancelled = Colors.conv(config.getString("receivedInviteCancelled"));
        noPendingInvite = Colors.conv(config.getString("noPendingInvite"));
        maxMembersReached = Colors.conv(config.getString("maxMembersReached"));
        noOutstandingInvite = Colors.conv(config.getString("noOutstandingInvite"));
        outstandingInvite = Colors.conv(config.getString("outstandingInvite"));
        otherOutstandingInvite = Colors.conv(config.getString("otherOutstandingInvite"));

        // Selection
        selectionMessage = Colors.conv(config.getString("selectionMessage"));
        selectionPending = Colors.conv(config.getString("selectionPending"));
        selectionExpire = Colors.conv(config.getString("selectionExpire"));
        selectionCancelled = Colors.conv(config.getString("selectionCancelled"));

        // Privacy
        islandNowPrivate = Colors.conv(config.getString("islandNowPrivate"));
        islandNowPublic = Colors.conv(config.getString("islandNowPublic"));
        onPrivateIslandTeleport = Colors.conv(config.getString("onPrivateIslandTeleport"));
    }
}
