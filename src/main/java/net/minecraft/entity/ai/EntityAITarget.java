package net.minecraft.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityOwnable;
import net.minecraft.entity.EntityPlayer;
import net.minecraft.entity.attribute.AttributeInstance;
import net.minecraft.entity.attribute.SharedMonsterAttributes;
import net.minecraft.entity.pathfinding.PathEntity;
import net.minecraft.entity.pathfinding.PathPoint;
import net.minecraft.utils.MathHelper;

public abstract class EntityAITarget extends EntityAIBase {
    /**
     * The entity that this task belongs to
     */
    protected EntityCreature taskOwner;

    /**
     * If true, EntityAI targets must be able to be seen (cannot be blocked by walls) to be suitable targets.
     */
    protected boolean shouldCheckSight;
    private boolean field_75303_a;
    private int field_75301_b;
    private int field_75302_c;
    private int field_75298_g;

    public EntityAITarget(EntityCreature par1EntityCreature, boolean par2) {
        this(par1EntityCreature, par2, false);
    }

    public EntityAITarget(EntityCreature par1EntityCreature, boolean par2, boolean par3) {
        this.taskOwner = par1EntityCreature;
        this.shouldCheckSight = par2;
        this.field_75303_a = par3;
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean continueExecuting() {
        EntityLivingBase var1 = this.taskOwner.getAttackTarget();

        if (var1 == null) {
            return false;
        } else if (!var1.isEntityAlive()) {
            return false;
        } else {
            double var2 = this.func_111175_f();

            if (this.taskOwner.getDistanceSqToEntity(var1) > var2 * var2) {
                return false;
            } else {
                if (this.shouldCheckSight) {
                    if (this.taskOwner.getEntitySenses().canSee(var1)) {
                        this.field_75298_g = 0;
                    } else if (++this.field_75298_g > 60) {
                        return false;
                    }
                }

                return true;
            }
        }
    }

    protected double func_111175_f() {
        AttributeInstance var1 = this.taskOwner.func_110148_a(SharedMonsterAttributes.field_111265_b);
        return var1 == null ? 16.0D : var1.func_111126_e();
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting() {
        this.field_75301_b = 0;
        this.field_75302_c = 0;
        this.field_75298_g = 0;
    }

    /**
     * Resets the task
     */
    public void resetTask() {
        this.taskOwner.setAttackTarget(null);
    }

    /**
     * A method used to see if an entity is a suitable target through a number of checks.
     */
    protected boolean isSuitableTarget(EntityLivingBase par1EntityLivingBase, boolean par2) {
        if (par1EntityLivingBase == null) {
            return false;
        } else if (par1EntityLivingBase == this.taskOwner) {
            return false;
        } else if (!par1EntityLivingBase.isEntityAlive()) {
            return false;
        } else if (!this.taskOwner.canAttackClass(par1EntityLivingBase.getClass())) {
            return false;
        } else {
            if (this.taskOwner instanceof EntityOwnable && org.apache.commons.lang3.StringUtils.isNotEmpty(((EntityOwnable) this.taskOwner).getOwnerName())) {
                if (par1EntityLivingBase instanceof EntityOwnable && ((EntityOwnable) this.taskOwner).getOwnerName().equals(((EntityOwnable) par1EntityLivingBase).getOwnerName())) {
                    return false;
                }

                if (par1EntityLivingBase == ((EntityOwnable) this.taskOwner).getOwner()) {
                    return false;
                }
            } else if (par1EntityLivingBase instanceof EntityPlayer && !par2 && ((EntityPlayer) par1EntityLivingBase).capabilities.disableDamage) {
                return false;
            }

            if (!this.taskOwner.func_110176_b(MathHelper.floor_double(par1EntityLivingBase.posX), MathHelper.floor_double(par1EntityLivingBase.posY), MathHelper.floor_double(par1EntityLivingBase.posZ))) {
                return false;
            } else if (this.shouldCheckSight && !this.taskOwner.getEntitySenses().canSee(par1EntityLivingBase)) {
                return false;
            } else {
                if (this.field_75303_a) {
                    if (--this.field_75302_c <= 0) {
                        this.field_75301_b = 0;
                    }

                    if (this.field_75301_b == 0) {
                        this.field_75301_b = this.func_75295_a(par1EntityLivingBase) ? 1 : 2;
                    }

                    if (this.field_75301_b == 2) {
                        return false;
                    }
                }

                return true;
            }
        }
    }

    private boolean func_75295_a(EntityLivingBase par1EntityLivingBase) {
        this.field_75302_c = 10 + this.taskOwner.getRNG().nextInt(5);
        PathEntity var2 = this.taskOwner.getNavigator().getPathToEntityLiving(par1EntityLivingBase);

        if (var2 == null) {
            return false;
        } else {
            PathPoint var3 = var2.getFinalPathPoint();

            if (var3 == null) {
                return false;
            } else {
                int var4 = var3.xCoord - MathHelper.floor_double(par1EntityLivingBase.posX);
                int var5 = var3.zCoord - MathHelper.floor_double(par1EntityLivingBase.posZ);
                return (double) (var4 * var4 + var5 * var5) <= 2.25D;
            }
        }
    }
}
