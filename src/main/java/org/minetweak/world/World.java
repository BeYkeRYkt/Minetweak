package org.minetweak.world;

import net.minecraft.world.WorldInfo;
import net.minecraft.world.WorldServer;
import org.minetweak.Minetweak;
import org.minetweak.block.TweakBlock;
import org.minetweak.chat.ChatColors;
import org.minetweak.entity.Player;
import org.minetweak.server.Difficulty;

public class World {

    private final WorldServer world;

    public World(WorldServer world) {
        this.world = world;
    }

    /**
     * Gets the Block at the specified position
     *
     * @param x x coord
     * @param y y coord
     * @param z z coord
     * @return Block at position
     */
    public TweakBlock getBlockAt(int x, int y, int z) {
        return getChunkAt(x >> 4, z >> 4).getBlock(x & 0xF, y & 0xFF, z & 0xF);
    }

    /**
     * Gets the Block ID of the block at a position
     *
     * @param x X Position
     * @param y Y Position
     * @param z Z Position
     * @return block id of position of block
     */
    public int getBlockTypeIdAt(int x, int y, int z) {
        return world.getBlockId(x, y, z);
    }

    /**
     * Gets a Chunk at the specified Location
     *
     * @param x X Position
     * @param z Z Position
     * @return Chunk at position
     */
    public Chunk getChunkAt(int x, int z) {
        return this.world.theChunkProviderServer.loadChunk(x, z).MineTweakChunk;
    }

    /**
     * Gets the Chunk  a block is in
     *
     * @param tweakBlock the block
     * @return the chunk
     */
    public Chunk getChunkAt(TweakBlock tweakBlock) {
        return getChunkAt(tweakBlock.getX() >> 4, tweakBlock.getZ() >> 4);
    }

    /**
     * Gets the WorldServer
     *
     * @return WorldServer instance for world
     */
    public WorldServer getWorldServer() {
        return world;
    }

    /**
     * Can peaceful mobs spawn in this world?
     *
     * @return True if peaceful mobs can spawn
     */
    public boolean canPeacefulMobsSpawn() {
        return getWorldServer().spawnPeacefulMobs();
    }

    /**
     * Can hostile mobs spawn in this world?
     *
     * @return True if hostile mobs can spawn
     */
    public boolean canHostileMobsSpawn() {
        return getWorldServer().spawnHostileMobs();
    }

    /**
     * Set the ability of mob spawns by type, peaceful or hostile.
     *
     * @param peacefulSpawnAbility Peaceful mobs spawn ability
     * @param hostileSpawnAbility  Hostile mobs spawn ability
     */
    public void setMobTypeSpawnAbility(boolean peacefulSpawnAbility, boolean hostileSpawnAbility) {
        getWorldServer().setAllowedSpawnTypes(hostileSpawnAbility, peacefulSpawnAbility);
    }

    /**
     * Sets the Spawn Location
     *
     * @param location Location
     */
    public void setSpawn(Location location) {
        setSpawn(location.getPosX(), location.getPosY(), location.getPosZ());
    }

    /**
     * Sets the Spawn Location
     *
     * @param x x
     * @param y y
     * @param z z
     */
    public void setSpawn(int x, int y, int z) {
        getWorldServer().getWorldInfo().setSpawnPosition(x, y, z);
    }

    /**
     * Gets the Spawn Location
     *
     * @return spawn location
     */
    public Location getSpawn() {
        WorldInfo i = getWorldServer().getWorldInfo();
        return new Location(i.getSpawnX(), i.getSpawnY(), i.getSpawnZ(), world);
    }

    /**
     * Gets the World's seed
     *
     * @return seed
     */
    public long getSeed() {
        return getWorldServer().getSeed();
    }

    /**
     * Gets the Difficulty of the World
     *
     * @return difficulty
     */
    public Difficulty getDifficulty() {
        return Difficulty.getByID(getWorldServer().difficultySetting);
    }

    /**
     * Sets the difficulty of the World
     *
     * @param difficulty difficulty
     */
    public void setDifficulty(Difficulty difficulty) {
        getWorldServer().difficultySetting = difficulty.getID();
    }

    /**
     * Broadcasts a Message to All Players in the World
     *
     * @param message message to broadcast
     */
    public void broadcastMessage(String message) {
        for (Player player : Minetweak.getPlayers().values()) {
            if (player.getCurrentWorld().getWorldServer() == getWorldServer()) {
                player.sendMessage(String.format("[%s%s%s] %s", ChatColors.GOLD, "Server", ChatColors.RESET, message));
            }
        }
    }

    /**
     * Gets the World's time
     *
     * @return world time
     */
    public long getWorldTime() {
        return getWorldServer().getWorldTime();
    }

    /**
     * Sets the World's time
     *
     * @param time time to set
     */
    public void setWorldTime(long time) {
        getWorldServer().setWorldTime(time);
    }

    /**
     * Turns rain on and off
     *
     * @param isRaining if it should rain
     */
    public void setRaining(boolean isRaining) {
        getWorldServer().getWorldInfo().setRaining(isRaining);
    }

    /**
     * Gets whether it is raining in the world
     *
     * @return if it is raining
     */
    public boolean isRaining() {
        return getWorldServer().getWorldInfo().isRaining();
    }

    /**
     * Sets the time it should rain in the world
     *
     * @param time time to rain
     */
    public void setRainTime(int time) {
        getWorldServer().getWorldInfo().setRainTime(time);
    }

    /**
     * Turns Thundering on and off
     *
     * @param isThundering if it should thunder
     */
    public void setThundering(boolean isThundering) {
        getWorldServer().getWorldInfo().setThundering(isThundering);
    }

    /**
     * Gets whether it is thundering in the world
     *
     * @return if it is thundering
     */
    public boolean isThundering() {
        return getWorldServer().getWorldInfo().isThundering();
    }

    /**
     * Sets the thunder time in the world
     *
     * @param time time to thunder
     */
    public void setThunderTime(int time) {
        getWorldServer().getWorldInfo().setThunderTime(time);
    }
}
