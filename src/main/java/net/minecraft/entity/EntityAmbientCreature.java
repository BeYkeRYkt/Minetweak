package net.minecraft.entity;

import net.minecraft.src.IAnimals;
import net.minecraft.world.World;

public abstract class EntityAmbientCreature extends EntityLiving implements IAnimals {
    public EntityAmbientCreature(World par1World) {
        super(par1World);
    }

    @Override
    public boolean func_110164_bC() {
        return false;
    }

    /**
     * Called when a player interacts with a mob. e.g. gets milk from a cow, gets into the saddle on a pig.
     */
    @Override
    protected boolean interact(EntityPlayer par1EntityPlayer) {
        return false;
    }
}
