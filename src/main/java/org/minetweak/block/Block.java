package org.minetweak.block;

import org.minetweak.material.Material;
import org.minetweak.world.Chunk;
import org.minetweak.world.Location;
import org.minetweak.world.World;

public class Block implements IBlock {
    private final Chunk chunk;
    private final int x;
    private final int y;
    private final int z;
    private boolean powered;
    private boolean isInWorld = true;
    private net.minecraft.block.Block block;

    public Block(Chunk chunk, int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.chunk = chunk;
    }

    public Block(net.minecraft.block.Block block) {
        this(null, 0, 0, 0);
        this.isInWorld = false;
        this.block = block;
    }

    @Override
    public World getWorld() {
        return chunk.getWorld();
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int getZ() {
        return z;
    }

    public int getBlockID() {
        return getWorld().getWorldServer().getBlockId(x, y, z);
    }

    @Override
    public Chunk getChunk() {
        return chunk;
    }

    @Override
    public void setData(final byte data) {
        chunk.getHandle().worldObj.setBlockMetadata(x, y, z, data, 3);
    }

    public BlockState getState() {
        Material material = getType();
        if (material == null)
            return new BlockState(this);
        switch (material) {
            default:
                return new BlockState(this);
        }
    }

    @Override
    public Material getType() {
        return Material.getMaterial(getTypeId());
    }

    @Override
    public void setData(final byte data, boolean applyPhysics) {
        if (applyPhysics) {
            chunk.getHandle().worldObj.setBlockMetadata(x, y, z, data, 3);
        } else {
            chunk.getHandle().worldObj.setBlockMetadata(x, y, z, data, 2);
        }
    }

    @Override
    public byte getData() {
        return (byte) chunk.getHandle().getBlockMetadata(this.x & 0xF, this.y & 0xFF, this.z & 0xF);
    }

    @Override
    public boolean setTypeId(final int type) {
        return chunk.getHandle().worldObj.setBlock(x, y, z, type, getData(), 3);
    }

    @Override
    public boolean setTypeId(final int type, final boolean applyPhysics) {
        if (applyPhysics) {
            return setTypeId(type);
        } else {
            return chunk.getHandle().worldObj.setBlock(x, y, z, type, getData(), 2);
        }
    }

    @Override
    public boolean setTypeIdAndData(final int type, final byte data, final boolean applyPhysics) {
        if (applyPhysics) {
            return chunk.getHandle().worldObj.setBlock(x, y, z, type, data, 3);
        } else {
            boolean success = chunk.getHandle().worldObj.setBlock(x, y, z, type, data, 2);
            if (success) {
                chunk.getHandle().worldObj.markBlockForUpdate(x, y, z);
            }
            return success;
        }
    }

    @Override
    public int getTypeId() {
        return chunk.getHandle().getBlockID(this.x & 0xF, this.y & 0xFF, this.z & 0xF);
    }

    @Override
    public void setPowered(boolean powered) {
        getWorld().getWorldServer().scheduleBlockUpdate(x, y, z, getBlockID(), 4);
        this.powered = true;
    }

    @Override
    public boolean isPowered() {
        return powered;
    }

    @Override
    public net.minecraft.block.Block getMCBlock() {
        if (!isInWorld) {
            return block;
        }
        return net.minecraft.block.Block.blocksList[getBlockID()];
    }

    @Override
    public void breakBlock() {
        getWorld().setBlock(getLocation(), 0);
    }

    @Override
    public Location getLocation() {
        return new Location(x, y, z, getWorld());
    }
}