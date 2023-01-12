package com.github.gavvydizzle.playerislands.storage;

import com.github.gavvydizzle.playerislands.PlayerIslands;
import com.github.gavvydizzle.playerislands.island.Island;
import com.github.gavvydizzle.playerislands.island.IslandMember;
import com.github.gavvydizzle.playerislands.island.MemberType;
import com.github.mittenmc.serverutils.UUIDConverter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Level;

public abstract class Database {

    private final String CREATE_ISLANDS_TABLE = "CREATE TABLE IF NOT EXISTS islands(" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "uuid BINARY(16)        NOT NULL," +
            "regionName VARCHAR(16) NOT NULL," +
            "isPrivate TINYINT      DEFAULT 0 NOT NULL," +
            "memberUpgradeLevel INT DEFAULT 0 NOT NULL," +
            "sizeUpgradeLevel INT   DEFAULT 0 NOT NULL" +
            ");";

    private final String CREATE_MEMBER_TABLE = "CREATE TABLE IF NOT EXISTS members(" +
            "uuid BINARY(16) NOT NULL," +
            "islandID INT    NOT NULL," +
            "memberType INT  NOT NULL," +
            "PRIMARY KEY (uuid, islandID)" +
            ");";

    private final String LOAD_ISLANDS = "SELECT * FROM islands;";

    private final String LOAD_PLAYER_ISLANDS = "SELECT * FROM islands WHERE uuid=?;";

    private final String LOAD_ISLAND_MEMBERS = "SELECT * FROM members WHERE islandID=?;";

    private final String INSERT_ISLAND = "INSERT INTO islands(uuid, regionName) VALUES(?,?);";

    private final String UPDATE_ISLAND_MEMBER_UPGRADE = "UPDATE islands SET memberUpgradeLevel=? WHERE id=?;";
    private final String UPDATE_ISLAND_SIZE_UPGRADE = "UPDATE islands SET sizeUpgradeLevel=? WHERE id=?;";
    private final String UPDATE_ISLAND_PRIVACY = "UPDATE islands SET isPrivate=? WHERE id=?;";

    private final String UPSERT_MEMBER = "INSERT OR REPLACE INTO members(uuid, islandID, memberType) VALUES(?,?,?);";

    private final String DELETE_ISLAND = "DELETE FROM islands WHERE id=?;";

    private final String DELETE_MEMBER = "DELETE FROM members WHERE uuid=? AND islandID=?;";

    private final String GET_MAX_ISLAND_ID = "SELECT MAX(id) FROM islands";


    PlayerIslands plugin;
    Connection connection;

    public Database(PlayerIslands instance){
        plugin = instance;
    }

    public abstract Connection getSQLConnection();

    /**
     * Creates the tables if they do not already exist
     */
    public void createTables() {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement(CREATE_ISLANDS_TABLE);
            ps.execute();
            ps = conn.prepareStatement(CREATE_MEMBER_TABLE);
            ps.execute();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null) ps.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
    }

    /**
     * Adds a new island to the database
     * @param owner The owner of this island
     */
    public void createIsland(UUID owner) {
        int id = PlayerIslands.getInstance().getIslandManager().getHighestIslandID();
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement(INSERT_ISLAND);
            ps.setBytes(1, UUIDConverter.convert(owner));
            ps.setString(2, "island_" + (id+1));
            ps.execute();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null) ps.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
    }

    /**
     * Loads all islands from the database
     * @return A list of islands
     */
    public ArrayList<Island> loadAllIslands() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        ArrayList<Island> islands = new ArrayList<>();
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement(LOAD_ISLANDS);
            resultSet = ps.executeQuery();

            while (resultSet.next()) {
                islands.add(new Island(
                        resultSet.getInt(1),
                        UUIDConverter.convert(resultSet.getBytes(2)),
                        resultSet.getString(3),
                        resultSet.getBoolean(4),
                        resultSet.getInt(5),
                        resultSet.getInt(6)
                ));
            }

        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (ps != null) ps.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return islands;
    }

    /**
     * Gets all the islands that this player is the owner of
     * @param uuid The uuid of the player
     * @return A list of islands
     */
    public ArrayList<Island> getPlayerIslands(UUID uuid) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        ArrayList<Island> islands = new ArrayList<>();
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement(LOAD_PLAYER_ISLANDS);
            ps.setBytes(1, UUIDConverter.convert(uuid));
            resultSet = ps.executeQuery();

            while (resultSet.next()) {
                islands.add(new Island(
                        resultSet.getInt(1),
                        UUIDConverter.convert(resultSet.getBytes(2)),
                        resultSet.getString(3),
                        resultSet.getBoolean(4),
                        resultSet.getInt(5),
                        resultSet.getInt(6)
                ));
            }

        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (ps != null) ps.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return islands;
    }

    public void updateIslandMemberUpgrade(Island island) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement(UPDATE_ISLAND_MEMBER_UPGRADE);
            ps.setInt(1, island.getMemberUpgrade().getUpgradeLevel());
            ps.setInt(2, island.getId());
            ps.execute();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null) ps.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
    }

    public void updateIslandSizeUpgrade(Island island) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement(UPDATE_ISLAND_SIZE_UPGRADE);
            ps.setInt(1, island.getSizeUpgrade().getUpgradeLevel());
            ps.setInt(2, island.getId());
            ps.execute();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null) ps.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
    }

    public void updateIslandPrivacy(Island island) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement(UPDATE_ISLAND_PRIVACY);
            ps.setBoolean(1, island.isPrivate());
            ps.setInt(2, island.getId());
            ps.execute();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null) ps.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
    }

    /**
     * Removes an island from the database
     * @param island The island
     */
    public void deleteIsland(Island island) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement(DELETE_ISLAND);
            ps.setInt(1, island.getId());
            ps.execute();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null) ps.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
    }

    /**
     * Creates a new member entry in the database
     * @param member The uuid of the member
     * @param islandID The islandID they were invited to
     */
    public void createMember(UUID member, int islandID) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement(UPSERT_MEMBER);
            ps.setBytes(1, UUIDConverter.convert(member));
            ps.setInt(2, islandID);
            ps.setInt(3, MemberType.getLowestWeight());
            ps.execute();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null) ps.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
    }

    /**
     * Gets all members of this island excluding the owner
     * @param islandID The id of the island
     * @return A list of IslandMembers
     */
    public ArrayList<IslandMember> getIslandMembers(int islandID) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        ArrayList<IslandMember> members = new ArrayList<>();
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement(LOAD_ISLAND_MEMBERS);
            ps.setInt(1, islandID);
            resultSet = ps.executeQuery();

            while (resultSet.next()) {
                members.add(new IslandMember(
                        UUIDConverter.convert(resultSet.getBytes(1)),
                        resultSet.getInt(2),
                        resultSet.getInt(3)
                ));
            }

        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (ps != null) ps.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return members;
    }

    /**
     * Updates this member's MemberType in the database
     * @param member The member
     */
    public void updateMember(IslandMember member) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement(UPSERT_MEMBER);
            ps.setBytes(1, UUIDConverter.convert(member.getOfflinePlayer().getUniqueId()));
            ps.setInt(2, member.getIslandID());
            ps.setInt(3, member.getMemberType().weight);
            ps.execute();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null) ps.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
    }

    /**
     * Removes an island member from the database
     * @param member The member
     */
    public void deleteMember(IslandMember member) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement(DELETE_MEMBER);
            ps.setBytes(1, UUIDConverter.convert(member.getOfflinePlayer().getUniqueId()));
            ps.setInt(2, member.getIslandID());
            ps.execute();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null) ps.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
    }

    /**
     * Gets the max 'id' value in the database
     * @return The max number, 0 if no rows exist, or -1 if an error occured
     */
    public int getHighestIslandID() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        int n = -1;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement(GET_MAX_ISLAND_ID);
            resultSet = ps.executeQuery();

            if (resultSet.next()) {
                n = resultSet.getInt(1);
            }
            else {
                n = 0; // Assuming the first id in the table will be 1
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (ps != null) ps.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }

        return n;
    }
}