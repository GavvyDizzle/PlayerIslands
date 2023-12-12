package com.github.gavvydizzle.playerislands.island;

import com.github.gavvydizzle.playerislands.PlayerIslands;
import com.github.gavvydizzle.playerislands.utils.Messages;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class IslandInvite {

    private final Player creator, invitedPlayer;
    private final Island island;
    private int taskID;

    public IslandInvite(Player creator, Player invitedPlayer, Island island) {
        this.creator = creator;
        this.invitedPlayer = invitedPlayer;
        this.island = island;

        sendInvite();
    }

    private void sendInvite() {
        creator.sendMessage(Messages.inviteSent.replace("{player_name}", invitedPlayer.getName()));

        TextComponent component = new TextComponent(TextComponent.fromLegacyText(Messages.inviteReceived
                .replace("{player_name}", creator.getName()).replace("{id}", "" + island.getId())));
        component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + PlayerIslands.getInstance().getPlayerCommandManager().getCommandDisplayName() + " join"));
        invitedPlayer.spigot().sendMessage(component);

        taskID = Bukkit.getScheduler().scheduleSyncDelayedTask(PlayerIslands.getInstance(), this::onInviteExpire, 300);
    }

    public void onInviteAccept() {
        if (!island.isMember(creator)) {
            invitedPlayer.sendMessage(ChatColor.RED + "The invite became invalid because " + creator.getName() + " is no longer part of island " + island.getId() + " !");
            onInviteExpire();
            return;
        }

        if (island.isAtMaxCapacity()) {
            invitedPlayer.sendMessage(Messages.maxMembersReached);
            onInviteExpire();
            return;
        }

        if (creator.isOnline()) {
            creator.sendMessage(Messages.sentInviteAccepted);
        }
        invitedPlayer.sendMessage(Messages.receivedInviteAccepted.replace("{id}", "" + island.getId()));

        PlayerIslands.getInstance().getIslandManager().onPlayerJoin(invitedPlayer, island);

        Bukkit.getScheduler().cancelTask(taskID);
    }

    public void onInviteCancel() {
        if (creator.isOnline()) {
            creator.sendMessage(Messages.sentInviteCancelled.replace("{player_name}", invitedPlayer.getName()));
        }
        if (invitedPlayer.isOnline()) {
            invitedPlayer.sendMessage(Messages.receivedInviteCancelled.replace("{player_name}", creator.getName()));
        }

        Bukkit.getScheduler().cancelTask(taskID);
    }

    private void onInviteExpire() {
        if (creator.isOnline()) {
            creator.sendMessage(Messages.sentInviteExpired.replace("{player_name}", invitedPlayer.getName()));
        }
        if (invitedPlayer.isOnline()) {
            invitedPlayer.sendMessage(Messages.receivedInviteExpired.replace("{player_name}", creator.getName()));
        }

        PlayerIslands.getInstance().getIslandManager().onIslandInviteExpire(this);
    }


    public Player getCreator() {
        return creator;
    }

    public Player getInvitedPlayer() {
        return invitedPlayer;
    }
}