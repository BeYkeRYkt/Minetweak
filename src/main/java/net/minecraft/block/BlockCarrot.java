package net.minecraft.block;

import net.minecraft.item.Item;

public class BlockCarrot extends BlockCrops {
    public BlockCarrot(int par1) {
        super(par1);
    }

    /**
     * Generate a seed ItemStack for this crop.
     */
    @Override
    protected int getSeedItem() {
        return Item.carrot.itemID;
    }

    /**
     * Generate a crop produce ItemStack for this crop.
     */
    @Override
    protected int getCropItem() {
        return Item.carrot.itemID;
    }
}
