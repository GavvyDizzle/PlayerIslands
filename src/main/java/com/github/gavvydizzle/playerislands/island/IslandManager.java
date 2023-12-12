package com.github.gavvydizzle.playerislands.island;

import com.github.gavvydizzle.playerislands.PlayerIslands;
import com.github.gavvydizzle.playerislands.upgrade.SizeUpgrade;
import com.github.gavvydizzle.playerislands.utils.Dimension;
import com.github.gavvydizzle.playerislands.utils.Messages;
import com.github.gavvydizzle.playerislands.utils.Pair;
import com.github.mittenmc.serverutils.Numbers;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class IslandManager {

    private int ISLAND_SPREAD;
    private int ISLAND_Y_COORDINATE;
    private ProtectedRegion templateRegion;
    private String onPrivateCommand;

    private boolean isDatabaseDataLoaded;
    private int highestIslandID;
    private World islandWorld;
    private RegionManager regionManager;
    private RegionQuery query;
    private ArrayList<Island> islandArrayList;
    private final HashMap<UUID, ArrayList<Island>> playerIslandOwnerMap;
    private final HashMap<UUID, ArrayList<Island>> playerIslandMemberMap; // Keep track of it on join/quit and edit on invite/leave
    private final HashMap<String, UUID> uuidByName;
    private final ArrayList<IslandInvite> islandInvites;

    public IslandManager() {
        reload();

        islandArrayList = new ArrayList<>();
        playerIslandOwnerMap = new HashMap<>();
        playerIslandMemberMap = new HashMap<>();
        uuidByName = new HashMap<>();
        islandInvites = new ArrayList<>();

        Bukkit.getScheduler().runTaskAsynchronously(PlayerIslands.getInstance(), this::initIslands);
    }

    private void initIslands() {
        if (isDatabaseDataLoaded) return;

        highestIslandID = PlayerIslands.getInstance().getDatabase().getHighestIslandID();
        if (highestIslandID == -1) {
            Bukkit.getLogger().severe("Unable to determine the highest ID in use. No islands can be created when this error occurs. Please restart the server to try again!");
        }

        islandArrayList = PlayerIslands.getInstance().getDatabase().loadAllIslands();
        for (Island island : islandArrayList) {
            island.loadMembers();

            addToOwnerMap(island.getOwner().getOfflinePlayer(), island);

            for (IslandMember member : island.getIslandMembers()) {
                addToMemberMap(member.getOfflinePlayer(), island);
            }
        }

        isDatabaseDataLoaded = true;
        PlayerIslands.getInstance().getLogger().info("Islands loaded from database");
    }

    private void addToOwnerMap(OfflinePlayer offlinePlayer, Island island) {
        if (playerIslandOwnerMap.containsKey(offlinePlayer.getUniqueId())) {
            playerIslandOwnerMap.get(offlinePlayer.getUniqueId()).add(island);
        }
        else {
            playerIslandOwnerMap.put(offlinePlayer.getUniqueId(), new ArrayList<>(Collections.singleton(island)));
        }

        if (offlinePlayer.getName() != null) uuidByName.put(offlinePlayer.getName().toLowerCase(), offlinePlayer.getUniqueId());
    }

    private void addToMemberMap(OfflinePlayer offlinePlayer, Island island) {
        if (playerIslandMemberMap.containsKey(offlinePlayer.getUniqueId())) {
            playerIslandMemberMap.get(offlinePlayer.getUniqueId()).add(island);
        }
        else {
            playerIslandMemberMap.put(offlinePlayer.getUniqueId(), new ArrayList<>(Collections.singleton(island)));
        }

        if (offlinePlayer.getName() != null) uuidByName.put(offlinePlayer.getName().toLowerCase(), offlinePlayer.getUniqueId());
    }

    private void removeFromMemberMap(UUID uuid, Island island) {
        ArrayList<Island> islands = playerIslandMemberMap.get(uuid);
        islands.remove(island);

        if (islands.isEmpty()) playerIslandMemberMap.remove(uuid);
    }

    public void reload() {
        FileConfiguration config = PlayerIslands.getInstance().getConfig();
        config.options().copyDefaults(true);
        config.addDefault("islandWorld", "todo");
        config.addDefault("onPrivateCommand", "warp spawn {player_name}");
        config.addDefault("islandWorld", "todo");
        config.addDefault("templateRegion", "todo");
        config.addDefault("islandSpread", 250);
        config.addDefault("island_y_coordinate", 70);
        PlayerIslands.getInstance().saveConfig();

        islandWorld = Bukkit.getWorld(Objects.requireNonNull(config.getString("islandWorld")));
        if (islandWorld == null) {
            PlayerIslands.getInstance().getLogger().warning("The island world is invalid in config.yml. Please input a valid world and reload this plugin with /isadmin reload");
            return;
        }

        onPrivateCommand = config.getString("onPrivateCommand");

        regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(islandWorld));

        if (regionManager != null) {
            try {
                templateRegion = regionManager.getRegion(Objects.requireNonNull(config.getString("templateRegion")));
                if (templateRegion == null) {
                    throw new Exception("Template region cannot be null");
                }
            }
            catch (Exception e) {
                PlayerIslands.getInstance().getLogger().warning("Failed to load the template region. Please make a region in the island world then put its name in config.yml");
            }
        }

        ISLAND_SPREAD = config.getInt("islandSpread");
        if (ISLAND_SPREAD < 100) {
            PlayerIslands.getInstance().getLogger().warning("An island spread of less than 100 is not allowed. It has been set to 100 but consider raising it");
        }
        else if (ISLAND_SPREAD <= 200) {
            PlayerIslands.getInstance().getLogger().warning("An island spread of less than 200 may be too small. Players may be able to load other islands from their island, so consider raising it");
        }
        else if (ISLAND_SPREAD >= 1000) {
            PlayerIslands.getInstance().getLogger().warning("An island spread of over 1000 is not allowed. It has been set to 1000 but consider lowering it to save disc space");
        }
        else if (ISLAND_SPREAD >= 500) {
            PlayerIslands.getInstance().getLogger().warning("An island spread of over 500 may be too large. Unless your islands are very large, this size is unnecessary, so consider lowering it");
        }
        ISLAND_SPREAD = Numbers.constrain(ISLAND_SPREAD, 100, 1000);

        ISLAND_Y_COORDINATE = config.getInt("island_y_coordinate");
        if (!Numbers.isWithinRange(ISLAND_Y_COORDINATE, -64, 319)) {
            PlayerIslands.getInstance().getLogger().warning("Blocks can only be placed from y -64 to 319. Please update the value at 'island_y_coordinate' in config.yml");
            ISLAND_Y_COORDINATE = Numbers.constrain(ISLAND_Y_COORDINATE, -64, 319);
        }
    }

    /**
     * Handles when a player creates a new island
     * @param player The player
     */
    public void createNewIsland(Player player) {
        if (highestIslandID == -1) {
            Bukkit.getLogger().severe("Island attempted to be created with an invalid island ID. No islands can be created when this error occurs. Please restart the server to resolve this issue");
            player.sendMessage(ChatColor.RED + "Unable to create island (INVALID_ISLAND_ID). Please alert an administrator!");
            return;
        }

        if (templateRegion == null) {
            Bukkit.getLogger().severe("Island attempted to be created with an invalid template region. No islands can be created when this error occurs. Please give a valid template region");
            player.sendMessage(ChatColor.RED + "Unable to create island (INVALID_ISLAND_TEMPLATE). Please alert an administrator!");
            return;
        }

        if (getNumPlayerOwnedIslands(player) >= getMaxIslands(player)) {
            player.sendMessage(Messages.maxIslandsReached);
            return;
        }

        PlayerIslands.getInstance().getDatabase().createIsland(player.getUniqueId());
        highestIslandID++;
        Island island = new Island(highestIslandID, player.getUniqueId(), "island_" + highestIslandID, false, 0, 0);
        island.loadMembers();
        islandArrayList.add(island);

        addToOwnerMap(island.getOwner().getOfflinePlayer(), island);

        player.sendMessage(Messages.islandCreated);
    }

    private int getMaxIslands(Player player) {
        for (int i = 1; i <= 50; i++) {
            String maxIslandsPrefix = "playerislands.islands.max.";
            if (player.hasPermission(maxIslandsPrefix + i)) return i;
        }

        return 1; // Default number of islands
    }

    /**
     * Handles when a player invites someone to their island
     * @param island The island
     * @param member The member who invited
     * @param newMember The newly invited player
     */
    public void invitePlayerToIsland(Island island, Player member, Player newMember) {
        //If the sender has another pending invite
        if (doesPlayerHaveOutstandingInvite(member)) {
            member.sendMessage(Messages.outstandingInvite);
            return;
        }

        if (island.isAtMaxCapacity()) {
            member.sendMessage(Messages.maxMembersReached);
            return;
        }

        //If the invited player is already a part of this island
        if (island.isMember(newMember)) {
            member.sendMessage(Messages.playerAlreadyMember.replace("{player_name}", newMember.getName()));
            return;
        }

        //If the player they invited has a pending invite
        if (doesPlayerHaveOutstandingInvite(newMember)) {
            member.sendMessage(Messages.otherOutstandingInvite);
            return;
        }

        onInvite(member, newMember, island);
    }

    /**
     * Adds a player to the island as a new member
     * @param newMember The player
     * @param island The island
     */
    public void onPlayerJoin(OfflinePlayer newMember, Island island) {
        island.addIslandMember(newMember);
        addToMemberMap(newMember, island);
    }

    /**
     * Handles when a player leaves an island
     * @param island The island
     * @param player The player who left
     */
    public void leaveIsland(Island island, Player player) {
        IslandMember islandMember = island.getMemberFromPlayer(player);
        if (islandMember == null) return;

        island.removeIslandMember(islandMember);
        removeFromMemberMap(player.getUniqueId(), island);

        islandMember.sendMessage(Messages.successfulLeave.replace("{owner}", island.getOwner().getPlayerName()));
        island.getOwner().sendMessage(Messages.playerLeftIsland.replace("{player_name}", islandMember.getPlayerName()).replace("{id}", "" + island.getId()));
    }

    /**
     * Handles when an admin forcefully adds a player as a member of an island
     * @param island the island
     * @param admin The admin
     * @param newMember The player who is the new member
     * @param overrideMemberCap If this method should ignore the island player cap
     */
    public void adminAddMember(Island island, Player admin, OfflinePlayer newMember, boolean overrideMemberCap) {
        IslandMember member = new IslandMember(newMember.getUniqueId(), island.getId(), MemberType.MEMBER);

        //If the invited player is already a part of this island
        if (island.isMember(newMember)) {
            admin.sendMessage(Messages.playerAlreadyMember.replace("{player_name}", member.getPlayerName()));
            return;
        }

        if (!overrideMemberCap && island.isAtMaxCapacity()) {
            admin.sendMessage(Messages.maxIslandsReached);
            return;
        }

        admin.sendMessage(Messages.successfulAdminAdd.replace("{player_name}", member.getPlayerName()).replace("{id}", "" + island.getId()));
        member.sendMessage(Messages.addedByAdmin.replace("{id}", "" + island.getId()));

        onPlayerJoin(newMember, island);
    }


    /**
     * Gets the region with the given name
     * @param name The name of the region
     * @return The region with this name or null if none exists
     */
    public ProtectedRegion getRegionByName(String name) {
        return regionManager.getRegion(name);
    }

    /**
     * Creates the WorldGuard region for this island
     * @param island The island
     */
    public void createRegionIfNotExists(Island island) {
        if (regionManager.hasRegion(island.getRegionName())) return;

        if (templateRegion == null) {
            PlayerIslands.getInstance().getLogger().warning("The template region is not defined so no island regions can be created!");
            return;
        }

        SizeUpgrade sizeUpgrade = PlayerIslands.getInstance().getUpgradeManager().getSizeUpgrade(0);
        Dimension regionDimension = sizeUpgrade.getRegionDimension();
        Dimension regionOffset = sizeUpgrade.getRegionOffset();

        BlockVector3 origin = BlockVector3.at(island.getOriginLocation().getBlockX(), island.getOriginLocation().getBlockY(), island.getOriginLocation().getBlockZ());
        BlockVector3 corner2 = BlockVector3.at(regionDimension.getX(), regionDimension.getY(), regionDimension.getZ());
        origin = origin.add(regionOffset.getX(), regionOffset.getY(), regionOffset.getZ()); // Move origin by the offset amount

        ProtectedCuboidRegion region = new ProtectedCuboidRegion(island.getRegionName(), origin, origin.add(corner2));
        try {
            region.setParent(templateRegion);
        } catch (ProtectedRegion.CircularInheritanceException e) {
            e.printStackTrace();
        }
        region.getOwners().addPlayer(island.getOwner().getOfflinePlayer().getUniqueId());

        regionManager.addRegion(region);

        PlayerIslands.getInstance().getUpgradeManager().pasteSchematic(island.getOriginLocation(), null, PlayerIslands.getInstance().getUpgradeManager().getSizeUpgrade(0), false);
    }

    /**
     * Set's the island region to the new, resized region.
     * Since there is no way to resize a region, a new one is created and the properties are carried over.
     *
     * @param island The island
     * @param sizeUpgrade The SizeUpgrade
     */
    public void updateRegionSize(Island island, SizeUpgrade sizeUpgrade) {
        ProtectedRegion old = island.getRegion();

        Dimension regionDimension = sizeUpgrade.getRegionDimension();
        Dimension regionOffset = sizeUpgrade.getRegionOffset();

        BlockVector3 origin = BlockVector3.at(island.getOriginLocation().getBlockX(), island.getOriginLocation().getBlockY(), island.getOriginLocation().getBlockZ());
        BlockVector3 corner2 = BlockVector3.at(regionDimension.getX(), regionDimension.getY(), regionDimension.getZ());
        origin = origin.add(regionOffset.getX(), regionOffset.getY(), regionOffset.getZ()); // Move origin by the offset amount

        ProtectedCuboidRegion region = new ProtectedCuboidRegion(island.getRegionName(), origin, origin.add(corner2));
        region.copyFrom(old);

        regionManager.removeRegion(island.getRegionName());
        regionManager.addRegion(region);
        island.setRegion(region);
    }

    /**
     * Gets the island region if this location is inside of one.
     * If the world is not the island world this will return null.
     * It is assumed that no island regions will overlap so the first region found with the query will be returned.
     * @param location The location
     * @return The Island or null
     */
    @Nullable
    public Island getIslandByLocation(Location location) {
        if (query == null) query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();

        try {
            if (!Objects.requireNonNull(location.getWorld()).getUID().equals(islandWorld.getUID())) return null;

            ProtectedRegion region = query.getApplicableRegions(BukkitAdapter.adapt(location)).getRegions().iterator().next();
            int id = Integer.parseInt(region.getId().split("_")[1]);
            return islandArrayList.get(id-1);
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * @see #getIslandByLocation(Location)
     * @param player The player
     * @return The Island this player is in or null
     */
    public Island getIslandByPlayerLocation(Player player) {
        return getIslandByLocation(player.getLocation());
    }

    /**
     * Runs a command for all non-members of this island. It is intended for this command to remove then from the island
     * @param island the island
     */
    public void removeNonMembers(Island island) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld().getUID().equals(islandWorld.getUID()) && island.getRegion().contains(BukkitAdapter.asBlockVector(player.getLocation()))) {
                if (!island.isMember(player) && !PlayerIslands.isPluginAdmin(player)) {
                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), onPrivateCommand.replace("{player_name}", player.getName()));
                }
            }
        }
    }

    /**
     * Runs a command for all of this island. It is intended for this command to remove then from the island during an island reset
     * @param island the island
     */
    public void removePlayersFromIsland(Island island) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (PlayerIslands.isPluginAdmin(player)) continue;

            if (player.getWorld().getUID().equals(islandWorld.getUID()) && island.getRegion().contains(BukkitAdapter.asBlockVector(player.getLocation()))) {
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), onPrivateCommand.replace("{player_name}", player.getName()));
            }
        }
    }

    /**
     * Calculates the location of this cell
     * @param id The id of the cell
     * @return The location of this cell's spawn given the cell spread
     */
    public Location getCellLocation(int id) {
        Pair<Integer, Integer> pair = getCoordinates(id);
        return new Location(islandWorld, pair.getA() * ISLAND_SPREAD, ISLAND_Y_COORDINATE, pair.getB() * ISLAND_SPREAD);
    }

    /**
     * Takes in an id and computes the coordinates. A layer begins in the top left corner. An id of 0 returns (0,0)
     * @param id The input
     * @return A pair of Integers representing the location
     */
    private Pair<Integer,Integer> getCoordinates(int id) {
        int l = (int) Math.sqrt(id); // The "layer" this id is found on if picturing the spiral as a square onion where 0 is the center point/layer
        if (l % 2 == 0) l = (l+1)/2;
        else l = (l+2)/2;

        if (l == 0) {
            return new Pair<>(0,0);
        }

        int sideLength = 2*l+1;
        int quarterID = id - (sideLength-2)*(sideLength-2);

        // Set the coordinates as if it were in the top left quarter
        int x = -l + (quarterID % (sideLength-1));
        int y = l;

        // Rotate coordinates if necessary
        switch (quarterID / (sideLength-1)) {
            case 0: return new Pair<>(x,y);
            case 1: return new Pair<>(y,-x);
            case 2: return new Pair<>(-x,-y);
            default: return new Pair<>(-y,x);
        }
    }

    public boolean isDatabaseDataNotLoaded() {
        return !isDatabaseDataLoaded;
    }

    public int getHighestIslandID() {
        return highestIslandID;
    }

    /**
     * Determines the number of islands this player owns
     * @param offlinePlayer The player
     * @return The number of islands owned by this player
     */
    public int getNumPlayerOwnedIslands(OfflinePlayer offlinePlayer) {
        return playerIslandOwnerMap.containsKey(offlinePlayer.getUniqueId()) ? playerIslandOwnerMap.get(offlinePlayer.getUniqueId()).size() : 0;
    }

    /**
     * Determines the number of islands this player is a member of
     * @param offlinePlayer The player
     * @return The number of islands this player is a member of
     */
    public int getNumPlayerMemberIslands(OfflinePlayer offlinePlayer) {
        return getPlayerMemberIslands(offlinePlayer).size();
    }

    /**
     * Determines the number of islands this player is the owner and a member of
     * @param offlinePlayer The player
     * @return The number of islands this player is the owner and a member of
     */
    public int getNumPlayerIslands(OfflinePlayer offlinePlayer) {
        return getNumPlayerOwnedIslands(offlinePlayer) + getNumPlayerMemberIslands(offlinePlayer);
    }

    /**
     * Gets the islands that the player is a member of
     * @param offlinePlayer The player
     * @return An ArrayList of Islands
     */
    public ArrayList<Island> getPlayerMemberIslands(OfflinePlayer offlinePlayer) {
        ArrayList<Island> arr = new ArrayList<>();

        for (Island island : islandArrayList) {
            if (!island.isOwner(offlinePlayer) && island.isMember(offlinePlayer)) arr.add(island);
        }
        return arr;
    }

    /**
     * Gets the islands that the player is a member and owner of
     * @param offlinePlayer The player
     * @return An ArrayList of Islands
     */
    public ArrayList<Island> getPlayerIslands(OfflinePlayer offlinePlayer) {
        ArrayList<Island> arr = new ArrayList<>();

        for (Island island : islandArrayList) {
            if (island.isOwner(offlinePlayer) || island.isMember(offlinePlayer)) arr.add(island);
        }
        return arr;
    }

    /**
     * Gets the islands that the player is the owner of
     * @param offlinePlayer The player
     * @return An ArrayList of Islands
     */
    public ArrayList<Island> getPlayerOwnedIslands(OfflinePlayer offlinePlayer) {
        return playerIslandOwnerMap.get(offlinePlayer.getUniqueId());
    }


    //*** ISLAND INVITES ***//

    /**
     * Determines if this player has an outstanding invite.
     * @param player The player to check
     * @return True if the player is the creator or invitedPlayer of any current outstanding invite, false otherwise.
     */
    public boolean doesPlayerHaveOutstandingInvite(Player player) {
        for (IslandInvite invite : islandInvites) {
            if (player.getUniqueId().equals(invite.getCreator().getUniqueId()) || player.getUniqueId().equals(invite.getInvitedPlayer().getUniqueId())) return true;
        }
        return false;
    }

    /**
     * Determines if the player is the creator of an outstanding invite.
     * @param player The player to check
     * @return True if the player is the creator of any current outstanding invite, false otherwise.
     */
    public boolean isPlayerCreatorOfInvite(Player player) {
        for (IslandInvite invite : islandInvites) {
            if (player.getUniqueId().equals(invite.getCreator().getUniqueId())) return true;
        }
        return false;
    }

    /**
     * Initiates an island invite for the given players.
     * @param creator The creator of the invite
     * @param invitedPlayer The player who the creator invited
     * @param island The island
     */
    public void onInvite(Player creator, Player invitedPlayer, Island island) {
        islandInvites.add(new IslandInvite(creator, invitedPlayer, island));
    }

    public void onIslandInviteExpire(IslandInvite islandInvite) {
        islandInvites.remove(islandInvite);
    }

    /**
     * To be called when the player accepts an outstanding invite.
     * @param invitedPlayer The player who accepted the invite
     */
    public void onIslandJoinFromInvite(Player invitedPlayer) {
        IslandInvite islandInvite = null;
        for (IslandInvite invite : islandInvites) {
            if (invitedPlayer.getUniqueId().equals(invite.getInvitedPlayer().getUniqueId())) {
                invite.onInviteAccept();
                islandInvite = invite;
                break;
            }
        }
        if (islandInvite != null) islandInvites.remove(islandInvite);
    }

    /**
     * To be called when the player cancels an outstanding invite.
     * @param creator The creator who cancelled the invite
     */
    public void onIslandInviteCancel(Player creator) {
        IslandInvite islandInvite = null;
        for (IslandInvite invite : islandInvites) {
            if (creator.getUniqueId().equals(invite.getCreator().getUniqueId())) {
                invite.onInviteCancel();
                islandInvite = invite;
                break;
            }
        }
        if (islandInvite != null) islandInvites.remove(islandInvite);
    }

    @Nullable
    public OfflinePlayer getPlayerByName(String name) {
        UUID uuid = uuidByName.get(name.toLowerCase());
        if (uuid == null) return null;

        return Bukkit.getOfflinePlayer(uuid);
    }

    public Set<String> getCachedPlayerNames() {
        return uuidByName.keySet();
    }

    public void updateIslandSpawnPoints() {
        for (Island island : islandArrayList) {
            island.updateSpawnLocation();
        }
    }


    public boolean isIslandWorldInvalid() {
        return islandWorld == null;
    }

    @Nullable
    public Island getRandomPublicIsland() {
        List<Island> islands = islandArrayList.stream().filter(island -> !island.isPrivate()).collect(Collectors.toList());
        if (islands.isEmpty()) return null;

        return islands.get(Numbers.randomNumber(0, islands.size()-1));
    }

    /**
     * @param id The island id
     * @return The island with this ID or null if none exists
     */
    @Nullable
    public Island getIslandByID(int id) {
        if (Numbers.isWithinRange(id, 1, islandArrayList.size())) return islandArrayList.get(id-1);
        return null;
    }

    /**
     * To be called when the GUIs of this plugin are reloaded
     */
    public void handleGUIReload() {
        for (Island island : islandArrayList) {
            island.getIslandMenu().setReloadPending();
            island.getMemberListMenu().setReloadPending();
            island.getUpgradeMenu().setReloadPending();
        }
    }

    /**
     * To be called when the upgrades are reloaded to refresh upgrade menus
     */
    public void handleUpgradeReload() {
        for (Island island : islandArrayList) {
            island.getUpgradeMenu().setReloadPending();
        }
    }
}
