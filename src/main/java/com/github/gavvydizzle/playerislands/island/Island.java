package com.github.gavvydizzle.playerislands.island;

import com.github.gavvydizzle.playerislands.PlayerIslands;
import com.github.gavvydizzle.playerislands.gui.IslandMenu;
import com.github.gavvydizzle.playerislands.gui.MemberListMenu;
import com.github.gavvydizzle.playerislands.gui.UpgradeMenu;
import com.github.gavvydizzle.playerislands.island.sorters.MemberSorter;
import com.github.gavvydizzle.playerislands.upgrade.MemberUpgrade;
import com.github.gavvydizzle.playerislands.upgrade.SizeUpgrade;
import com.github.gavvydizzle.playerislands.utils.Messages;
import com.github.gavvydizzle.playerislands.utils.Sounds;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

public class Island {

    private static final int PRIVACY_TOGGLE_COOLDOWN_SECONDS = 3;
    private final int id;
    private int memberUpgradeLevel, sizeUpgradeLevel;
    private boolean isPrivate;
    private long lastPrivacyToggleTime;
    private final IslandMember owner;
    private ArrayList<IslandMember> members;
    private final Location originLocation;
    private Location spawnLocation;
    private final String regionName;
    private ProtectedRegion region;
    private final IslandMenu islandMenu;
    private final MemberListMenu memberListMenu;
    private final UpgradeMenu upgradeMenu;

    public Island(int id, UUID uuid, String regionName, boolean isPrivate, int memberUpgradeLevel, int sizeUpgradeLevel) {
        this.id = id;
        this.isPrivate = isPrivate;
        this.owner = new IslandMember(uuid, id, MemberType.OWNER.weight);
        this.memberUpgradeLevel = memberUpgradeLevel;
        this.sizeUpgradeLevel = sizeUpgradeLevel;

        originLocation = PlayerIslands.getInstance().getIslandManager().getCellLocation(id);
        updateSpawnLocation();

        this.regionName = regionName;
        PlayerIslands.getInstance().getIslandManager().createRegionIfNotExists(this);
        region = PlayerIslands.getInstance().getIslandManager().getRegionByName(regionName);

        islandMenu = new IslandMenu(this);
        memberListMenu = new MemberListMenu(this);
        upgradeMenu = new UpgradeMenu(this);
    }

    protected void loadMembers() {
        members = PlayerIslands.getInstance().getDatabase().getIslandMembers(id);
        members.sort(new MemberSorter());
    }

    /**
     * Updates the spawn location for this island. To be called when the size upgrade level is changed or upgrades are reloaded.
     */
    public void updateSpawnLocation() {
        SizeUpgrade sizeUpgrade = getSizeUpgrade();
        if (sizeUpgrade == null) {
            spawnLocation = originLocation.clone().add(0.5, 0, 0.5);
            return;
        }

        spawnLocation = originLocation.clone().add(sizeUpgrade.getSpawnOffset().getX() + 0.5, sizeUpgrade.getSpawnOffset().getY(), sizeUpgrade.getSpawnOffset().getZ() + 0.5);
        spawnLocation.setPitch(sizeUpgrade.getPitch());
        spawnLocation.setYaw(sizeUpgrade.getYaw());
    }

    /**
     * Inverts the privacy setting of this island and pushes the change to the database
     */
    public void toggleIslandPrivacy(Player member) {
        if (!canTogglePrivacy()) {
            member.sendMessage(Messages.pleaseWait);
            return;
        }

        isPrivate = !isPrivate;

        if (isPrivate) member.sendMessage(Messages.islandNowPrivate);
        else member.sendMessage(Messages.islandNowPublic);

        lastPrivacyToggleTime = System.currentTimeMillis();

        islandMenu.updatePrivacyItem();
        PlayerIslands.getInstance().getDatabase().updateIslandPrivacy(this);
        Sounds.togglePrivacySound.playSound(member);

        PlayerIslands.getInstance().getIslandManager().removeNonMembers(this);
    }

    /**
     * Determines if this island's privacy can be toggles
     * @return True if over a minute has passed since the last toggle
     */
    private boolean canTogglePrivacy() {
        return System.currentTimeMillis() - lastPrivacyToggleTime > 1000 * PRIVACY_TOGGLE_COOLDOWN_SECONDS;
    }

    /**
     * Adds a new member to this island and sorts the member list and adds this member to the database
     * @param newMember The player to add
     */
    public void addIslandMember(OfflinePlayer newMember) {
        members.add(new IslandMember(newMember.getUniqueId(), id, MemberType.MEMBER));
        members.sort(new MemberSorter());

        region.getMembers().addPlayer(newMember.getUniqueId());

        memberListMenu.updateMemberList();
        PlayerIslands.getInstance().getDatabase().createMember(newMember.getUniqueId(), id);
    }

    /**
     * Increases the member's rank by one and updates the database
     * @param member The member
     * @return If the member's rank actually changed
     */
    public boolean promoteMember(IslandMember member) {
        MemberType old = member.getMemberType();
        member.setMemberType(MemberType.getHigherRank(old));

        // Don't allow promotions to owner
        if (member.getMemberType() == MemberType.OWNER) member.setMemberType(old);

        if (member.getMemberType() != old) {
            members.sort(new MemberSorter());
            memberListMenu.updateMemberList();
            PlayerIslands.getInstance().getDatabase().updateMember(member);
            return true;
        }
        return false;
    }

    /**
     * Decreases the member's rank by one and updates the database
     * @param member The member
     * @return If the member's rank actually changed
     */
    public boolean demoteMember(IslandMember member) {
        MemberType old = member.getMemberType();
        member.setMemberType(MemberType.getLowerRank(old));

        if (member.getMemberType() != old) {
            members.sort(new MemberSorter());
            memberListMenu.updateMemberList();
            PlayerIslands.getInstance().getDatabase().updateMember(member);
            return true;
        }

        return false;
    }

    /**
     * Removes a player from this island and removes it from the database
     * @param kicked The kicked IslandMember
     */
    public void removeIslandMember(IslandMember kicked) {
        region.getMembers().removePlayer(kicked.getOfflinePlayer().getUniqueId());
        if (members.remove(kicked)) {
            memberListMenu.updateMemberList();
            PlayerIslands.getInstance().getDatabase().deleteMember(kicked);
        }
    }

    /**
     * Teleports this player to the spawn of this island.
     * Gives the player an error message if the island is private
     * @param player The player
     */
    public void teleportToSpawn(Player player) {
        if (isPrivate && !isMember(player) && !PlayerIslands.isPluginAdmin(player) && !PlayerIslands.hasPrivateIslandBypassPermission(player)) {
            player.sendMessage(Messages.onPrivateIslandTeleport);
            return;
        }
        player.teleport(spawnLocation);
    }

    public int getMaxMembers() {
        MemberUpgrade memberUpgrade = getMemberUpgrade();
        if (memberUpgrade == null) return 0;
        return memberUpgrade.getMaxMembers();
    }

    public boolean isMemberUpgradeMax() {
        return getNextMemberUpgrade() == null;
    }

    public boolean isSizeUpgradeMax() {
        return getNextSizeUpgrade() == null;
    }

    public MemberUpgrade getMemberUpgrade() {
        return PlayerIslands.getInstance().getUpgradeManager().getMemberUpgrade(memberUpgradeLevel);
    }

    public MemberUpgrade getNextMemberUpgrade() {
        return PlayerIslands.getInstance().getUpgradeManager().getMemberUpgrade(memberUpgradeLevel+1);
    }

    public void upgradeMemberUpgrade() {
        if (getNextMemberUpgrade() != null) {
            memberUpgradeLevel = getNextMemberUpgrade().getUpgradeLevel();
            upgradeMenu.updateMemberUpgradeItem();
            PlayerIslands.getInstance().getDatabase().updateIslandMemberUpgrade(this);
        }
    }

    public void setMemberUpgrade(int level) {
        if (memberUpgradeLevel == level) return;

        memberUpgradeLevel = level;
        upgradeMenu.updateMemberUpgradeItem();
        PlayerIslands.getInstance().getDatabase().updateIslandMemberUpgrade(this);
    }

    public SizeUpgrade getSizeUpgrade() {
        return PlayerIslands.getInstance().getUpgradeManager().getSizeUpgrade(sizeUpgradeLevel);
    }

    public SizeUpgrade getNextSizeUpgrade() {
        return PlayerIslands.getInstance().getUpgradeManager().getSizeUpgrade(sizeUpgradeLevel+1);
    }

    public void upgradeSizeUpgrade() {
        if (getNextSizeUpgrade() != null) {
            SizeUpgrade oldSizeUpgrade = getSizeUpgrade();
            sizeUpgradeLevel = getNextSizeUpgrade().getUpgradeLevel();
            updateSpawnLocation();
            upgradeMenu.updateSizeUpgradeItem();
            PlayerIslands.getInstance().getDatabase().updateIslandSizeUpgrade(this);
            PlayerIslands.getInstance().getIslandManager().updateRegionSize(this, getSizeUpgrade());
            PlayerIslands.getInstance().getUpgradeManager().pasteSchematic(originLocation, oldSizeUpgrade, getSizeUpgrade(), true);
        }
    }

    public void setSizeUpgrade(int level, boolean keepExistingBlocks) {
        if (level != 0 && sizeUpgradeLevel == level) return;

        SizeUpgrade oldSizeUpgrade = getSizeUpgrade();
        sizeUpgradeLevel = level;
        updateSpawnLocation();
        upgradeMenu.updateSizeUpgradeItem();
        PlayerIslands.getInstance().getDatabase().updateIslandSizeUpgrade(this);
        PlayerIslands.getInstance().getIslandManager().updateRegionSize(this, getSizeUpgrade());

        PlayerIslands.getInstance().getUpgradeManager().pasteSchematic(originLocation, oldSizeUpgrade, getSizeUpgrade(), keepExistingBlocks);
    }

    /**
     * Determines the player's MemberType for this island
     * @param offlinePlayer The player
     * @return The player's MemberType or null if they are not a member/owner
     */
    public MemberType getPlayerMemberType(OfflinePlayer offlinePlayer) {
        if (isOwner(offlinePlayer)) {
            return MemberType.OWNER;
        }

        for (IslandMember islandMember : members) {
            if (offlinePlayer.getUniqueId().equals(islandMember.getOfflinePlayer().getUniqueId())) return islandMember.getMemberType();
        }

        return null;
    }

    /**
     * Determines if this player is a member of the island
     * @param offlinePlayer The player
     * @return True if the player is the owner or a member, false otherwise
     */
    public boolean isMember(OfflinePlayer offlinePlayer) {
        return getPlayerMemberType(offlinePlayer) != null;
    }

    /**
     *
     * @param offlinePlayer The OfflinePlayer
     * @return If this player is the owner of the island
     */
    public boolean isOwner(OfflinePlayer offlinePlayer) {
        return owner.getOfflinePlayer().getUniqueId().equals(offlinePlayer.getUniqueId());
    }

    /**
     * Gets the IslandMember for this player
     * @param offlinePlayer The player
     * @return The associated IslandMember or null if they are not a member
     */
    public IslandMember getMemberFromPlayer(OfflinePlayer offlinePlayer) {
        if (isOwner(offlinePlayer)) return owner;

        for (IslandMember islandMember : members) {
            if (offlinePlayer.getUniqueId().equals(islandMember.getOfflinePlayer().getUniqueId())) return islandMember;
        }
        return null;
    }


    public int getId() {
        return id;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public IslandMember getOwner() {
        return owner;
    }

    public ArrayList<IslandMember> getMembers() {
        return members;
    }

    public Location getOriginLocation() {
        return originLocation;
    }

    public String getRegionName() {
        return regionName;
    }

    public ProtectedRegion getRegion() {
        return region;
    }

    public void setRegion(ProtectedRegion region) {
        this.region = region;
    }

    public IslandMenu getIslandMenu() {
        return islandMenu;
    }

    public MemberListMenu getMemberListMenu() {
        return memberListMenu;
    }

    public UpgradeMenu getUpgradeMenu() {
        return upgradeMenu;
    }

    /**
     * Gets the number of members of this island including the owner
     * @return The number of members + 1
     */
    public int getNumMembersWithOwner() {
        return members.size() + 1;
    }

    /**
     * Gets the number of members of this island including the owner
     * @return The number of members + 1
     */
    public int getNumMembers() {
        return members.size();
    }

    public ArrayList<IslandMember> getIslandMembers() {
        return members;
    }

    public boolean isAtMaxCapacity() {
        return members.size() >= getMaxMembers();
    }

    @Override
    public String toString() {
        return "Island(" + id + " - " + owner.getPlayerName() + ")";
    }
}
