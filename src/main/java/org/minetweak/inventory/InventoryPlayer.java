package org.minetweak.inventory;

import org.minetweak.item.ItemStack;

public class InventoryPlayer implements Inventory {
    private net.minecraft.src.InventoryPlayer playerInventory;

    /**
     * Creates an Inventory from MC's Inventory Player
     * @param playerInventory Minetweak Inventory
     */
    public InventoryPlayer(net.minecraft.src.InventoryPlayer playerInventory) {
        this.playerInventory = playerInventory;
    }

    @Override
    public ItemStack getStackInSlot(Integer slotID) {
        net.minecraft.src.ItemStack stack = playerInventory.getStackInSlot(slotID);
        if (stack==null)
            return null;
        else
            return new ItemStack(stack);
    }

    @Override
    public int getSize() {
        return playerInventory.getSizeInventory();
    }


    @Override
    public void setStackInSlot(Integer slotId, ItemStack stack) {
        if (stack==null) {
            playerInventory.setInventorySlotContents(slotId, null);
        } else {
            playerInventory.setInventorySlotContents(slotId, stack.getItemStack());
        }
    }

    @Override
    public void clear() {
        for (int i = 0; i<getSize(); i++) {
            ItemStack stack = getStackInSlot(i);
            if (stack!=null) {
                setStackInSlot(i, null);
            }
        }
    }
}