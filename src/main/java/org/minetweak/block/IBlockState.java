package org.minetweak.block;

import org.minetweak.material.Material;
import org.minetweak.world.Chunk;
import org.minetweak.world.World;

/**
 * Represents a captured state of a block, which will not change automatically.
 * Unlike Block, which only one object can exist per coordinate, BlockState can
 * exist multiple times for any given Block. Note that another plugin may change
 * the state of the block and you will not know, or they may change the block to
 * another type entirely, causing your BlockState to become invalid.
 */
public interface IBlockState {

    /**
     * Gets the block represented by this BlockState
     *
     * @return Block that this BlockState represents
     */
    IBlock getBlock();

    /**
     * Gets the type of this block
     *
     * @return block type
     */
    Material getType();

    /**
     * Gets the type-id of this block
     *
     * @return block type-id
     */
    int getTypeId();

    /**
     * Gets the world which contains this Block
     *
     * @return World containing this block
     */
    World getWorld();

    /**
     * Gets the x-coordinate of this block
     *
     * @return x-coordinate
     */
    int getX();

    /**
     * Gets the y-coordinate of this block
     *
     * @return y-coordinate
     */
    int getY();

    /**
     * Gets the z-coordinate of this block
     *
     * @return z-coordinate
     */
    int getZ();

    /**
     * Gets the chunk which contains this block
     *
     * @return Containing Chunk
     */
    Chunk getChunk();

    /**
     * Sets the type of this block
     *
     * @param type Material to change this block to
     */
    void setType(Material type);

    /**
     * Sets the type-id of this block
     *
     * @param type Type-Id to change this block to
     * @return Whether it worked?
     */
    boolean setTypeId(int type);

    /**
     * Attempts to update the block represented by this state, setting it to the
     * new values as defined by this state.
     * This has the same effect as calling update(false). That is to say,
     * this will not modify the state of a block if it is no longer the same
     * type as it was when this state was taken. It will return false in this
     * eventuality.
     *
     * @return true if the update was successful, otherwise false
     * @see #update(boolean)
     */
    boolean update();

    /**
     * Attempts to update the block represented by this state, setting it to the
     * new values as defined by this state.
     * This has the same effect as calling update(force, true). That is to say,
     * this will trigger a physics update to surrounding blocks.
     *
     * @param force true to forcefully set the state
     * @return true if the update was successful, otherwise false
     */
    boolean update(boolean force);

    /**
     * Attempts to update the block represented by this state, setting it to the
     * new values as defined by this state.
     * Unless force is true, this will not modify the state of a block if it is
     * no longer the same type as it was when this state was taken. It will return
     * false in this eventuality.
     * If force is true, it will set the type of the block to match the new state,
     * set the state data and then return true.
     * If applyPhysics is true, it will trigger a physics update on surrounding
     * blocks which could cause them to update or disappear.
     *
     * @param force        true to forcefully set the state
     * @param applyPhysics false to cancel updating physics on surrounding blocks
     * @return true if the update was successful, otherwise false
     */
    boolean update(boolean force, boolean applyPhysics);

    /**
     * @return The data as a raw byte.
     */
    public byte getRawData();

    /**
     * @param data The new data value for the block.
     */
    public void setRawData(byte data);
}