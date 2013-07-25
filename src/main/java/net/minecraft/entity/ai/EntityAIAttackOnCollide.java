package net.minecraft.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.pathfinding.PathEntity;
import net.minecraft.utils.MathHelper;
import net.minecraft.world.World;

public class EntityAIAttackOnCollide extends EntityAIBase {
    World worldObj;
    EntityCreature attacker;

    /**
     * An amount of decrementing ticks that allows the entity to attack once the tick reaches 0.
     */
    int attackTick;
    double field_75440_e;
    boolean field_75437_f;

    /**
     * The PathEntity of our entity.
     */
    PathEntity entityPathEntity;
    Class classTarget;
    private int field_75445_i;

    public EntityAIAttackOnCollide(EntityCreature par1EntityCreature, Class par2Class, double par3, boolean par5) {
        this(par1EntityCreature, par3, par5);
        this.classTarget = par2Class;
    }

    public EntityAIAttackOnCollide(EntityCreature par1EntityCreature, double par2, boolean par4) {
        this.attacker = par1EntityCreature;
        this.worldObj = par1EntityCreature.worldObj;
        this.field_75440_e = par2;
        this.field_75437_f = par4;
        this.setMutexBits(3);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute() {
        EntityLivingBase var1 = this.attacker.getAttackTarget();

        if (var1 == null) {
            return false;
        } else if (!var1.isEntityAlive()) {
            return false;
        } else if (this.classTarget != null && !this.classTarget.isAssignableFrom(var1.getClass())) {
            return false;
        } else {
            this.entityPathEntity = this.attacker.getNavigator().getPathToEntityLiving(var1);
            return this.entityPathEntity != null;
        }
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean continueExecuting() {
        EntityLivingBase var1 = this.attacker.getAttackTarget();
        return var1 == null ? false : (!var1.isEntityAlive() ? false : (!this.field_75437_f ? !this.attacker.getNavigator().noPath() : this.attacker.func_110176_b(MathHelper.floor_double(var1.posX), MathHelper.floor_double(var1.posY), MathHelper.floor_double(var1.posZ))));
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting() {
        this.attacker.getNavigator().setPath(this.entityPathEntity, this.field_75440_e);
        this.field_75445_i = 0;
    }

    /**
     * Resets the task
     */
    public void resetTask() {
        this.attacker.getNavigator().clearPathEntity();
    }

    /**
     * Updates the task
     */
    public void updateTask() {
        EntityLivingBase var1 = this.attacker.getAttackTarget();
        this.attacker.getLookHelper().setLookPositionWithEntity(var1, 30.0F, 30.0F);

        if ((this.field_75437_f || this.attacker.getEntitySenses().canSee(var1)) && --this.field_75445_i <= 0) {
            this.field_75445_i = 4 + this.attacker.getRNG().nextInt(7);
            this.attacker.getNavigator().tryMoveToEntityLiving(var1, this.field_75440_e);
        }

        this.attackTick = Math.max(this.attackTick - 1, 0);
        double var2 = (double) (this.attacker.width * 2.0F * this.attacker.width * 2.0F + var1.width);

        if (this.attacker.getDistanceSq(var1.posX, var1.boundingBox.minY, var1.posZ) <= var2) {
            if (this.attackTick <= 0) {
                this.attackTick = 20;

                if (this.attacker.getHeldItem() != null) {
                    this.attacker.swingItem();
                }

                this.attacker.attackEntityAsMob(var1);
            }
        }
    }
}
