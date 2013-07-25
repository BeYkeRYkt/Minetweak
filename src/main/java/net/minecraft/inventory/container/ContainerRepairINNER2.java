package net.minecraft.inventory.container;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.slot.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

class ContainerRepairINNER2 extends Slot {
    final World field_135071_a;

    final int field_135069_b;

    final int field_135070_c;

    final int field_135067_d;

    final ContainerRepair field_135068_e;

    ContainerRepairINNER2(ContainerRepair par1ContainerRepair, IInventory par2IInventory, int par3, int par4, int par5, World par6World, int par7, int par8, int par9) {
        super(par2IInventory, par3, par4, par5);
        this.field_135068_e = par1ContainerRepair;
        this.field_135071_a = par6World;
        this.field_135069_b = par7;
        this.field_135070_c = par8;
        this.field_135067_d = par9;
    }

    /**
     * Check if the stack is a valid item for this slot. Always true beside for the armor slots.
     */
    public boolean isItemValid(ItemStack par1ItemStack) {
        return false;
    }

    /**
     * Return whether this slot's stack can be taken from this slot.
     */
    public boolean canTakeStack(EntityPlayer par1EntityPlayer) {
        return (par1EntityPlayer.capabilities.isCreativeMode || par1EntityPlayer.experienceLevel >= this.field_135068_e.maximumCost) && this.field_135068_e.maximumCost > 0 && this.getHasStack();
    }

    public void onPickupFromSlot(EntityPlayer par1EntityPlayer, ItemStack par2ItemStack) {
        if (!par1EntityPlayer.capabilities.isCreativeMode) {
            par1EntityPlayer.addExperienceLevel(-this.field_135068_e.maximumCost);
        }

        ContainerRepair.func_135073_a(this.field_135068_e).setInventorySlotContents(0, (ItemStack) null);

        if (ContainerRepair.func_135072_b(this.field_135068_e) > 0) {
            ItemStack var3 = ContainerRepair.func_135073_a(this.field_135068_e).getStackInSlot(1);

            if (var3 != null && var3.stackSize > ContainerRepair.func_135072_b(this.field_135068_e)) {
                var3.stackSize -= ContainerRepair.func_135072_b(this.field_135068_e);
                ContainerRepair.func_135073_a(this.field_135068_e).setInventorySlotContents(1, var3);
            } else {
                ContainerRepair.func_135073_a(this.field_135068_e).setInventorySlotContents(1, (ItemStack) null);
            }
        } else {
            ContainerRepair.func_135073_a(this.field_135068_e).setInventorySlotContents(1, (ItemStack) null);
        }

        this.field_135068_e.maximumCost = 0;

        if (!par1EntityPlayer.capabilities.isCreativeMode && !this.field_135071_a.isRemote && this.field_135071_a.getBlockId(this.field_135069_b, this.field_135070_c, this.field_135067_d) == Block.anvil.blockID && par1EntityPlayer.getRNG().nextFloat() < 0.12F) {
            int var6 = this.field_135071_a.getBlockMetadata(this.field_135069_b, this.field_135070_c, this.field_135067_d);
            int var4 = var6 & 3;
            int var5 = var6 >> 2;
            ++var5;

            if (var5 > 2) {
                this.field_135071_a.setBlockToAir(this.field_135069_b, this.field_135070_c, this.field_135067_d);
                this.field_135071_a.playAuxSFX(1020, this.field_135069_b, this.field_135070_c, this.field_135067_d, 0);
            } else {
                this.field_135071_a.setBlockMetadata(this.field_135069_b, this.field_135070_c, this.field_135067_d, var4 | var5 << 2, 2);
                this.field_135071_a.playAuxSFX(1021, this.field_135069_b, this.field_135070_c, this.field_135067_d, 0);
            }
        } else if (!this.field_135071_a.isRemote) {
            this.field_135071_a.playAuxSFX(1021, this.field_135069_b, this.field_135070_c, this.field_135067_d, 0);
        }
    }
}
