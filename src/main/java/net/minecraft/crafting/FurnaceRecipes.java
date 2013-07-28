package net.minecraft.crafting;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class FurnaceRecipes {
    private static final FurnaceRecipes smeltingBase = new FurnaceRecipes();

    /**
     * The list of smelting results.
     */
    private Map<Number, ItemStack> smeltingList = new HashMap<Number, ItemStack>();
    private Map<Number, Float> experienceList = new HashMap<Number, Float>();

    /**
     * Used to call methods addSmelting and getSmeltingResult.
     */
    public static FurnaceRecipes smelting() {
        return smeltingBase;
    }

    private FurnaceRecipes() {
        this.addSmelting(Block.oreIron.blockID, new ItemStack(Item.ingotIron), 0.7F);
        this.addSmelting(Block.oreGold.blockID, new ItemStack(Item.ingotGold), 1.0F);
        this.addSmelting(Block.oreDiamond.blockID, new ItemStack(Item.diamond), 1.0F);
        this.addSmelting(Block.sand.blockID, new ItemStack(Block.glass), 0.1F);
        this.addSmelting(Item.porkRaw.itemID, new ItemStack(Item.porkCooked), 0.35F);
        this.addSmelting(Item.beefRaw.itemID, new ItemStack(Item.beefCooked), 0.35F);
        this.addSmelting(Item.chickenRaw.itemID, new ItemStack(Item.chickenCooked), 0.35F);
        this.addSmelting(Item.fishRaw.itemID, new ItemStack(Item.fishCooked), 0.35F);
        this.addSmelting(Block.cobblestone.blockID, new ItemStack(Block.stone), 0.1F);
        this.addSmelting(Item.clay.itemID, new ItemStack(Item.brick), 0.3F);
        this.addSmelting(Block.blockClay.blockID, new ItemStack(Block.hardenedClay), 0.35F);
        this.addSmelting(Block.cactus.blockID, new ItemStack(Item.dyePowder, 1, 2), 0.2F);
        this.addSmelting(Block.wood.blockID, new ItemStack(Item.coal, 1, 1), 0.15F);
        this.addSmelting(Block.oreEmerald.blockID, new ItemStack(Item.emerald), 1.0F);
        this.addSmelting(Item.potato.itemID, new ItemStack(Item.bakedPotato), 0.35F);
        this.addSmelting(Block.netherrack.blockID, new ItemStack(Item.netherrackBrick), 0.1F);
        this.addSmelting(Block.oreCoal.blockID, new ItemStack(Item.coal), 0.1F);
        this.addSmelting(Block.oreRedstone.blockID, new ItemStack(Item.redstone), 0.7F);
        this.addSmelting(Block.oreLapis.blockID, new ItemStack(Item.dyePowder, 1, 4), 0.2F);
        this.addSmelting(Block.oreNetherQuartz.blockID, new ItemStack(Item.netherQuartz), 0.2F);
    }

    /**
     * Adds a smelting crafting.
     */
    public void addSmelting(int par1, ItemStack par2ItemStack, float par3) {
        this.smeltingList.put(par1, par2ItemStack);
        this.experienceList.put(par2ItemStack.itemID, par3);
    }

    /**
     * Returns the smelting result of an item.
     */
    public ItemStack getSmeltingResult(int par1) {
        return this.smeltingList.get(par1);
    }

    public Map<Number, ItemStack> getSmeltingList() {
        return this.smeltingList;
    }

    public float getExperience(int par1) {
        return this.experienceList.containsKey(par1) ? this.experienceList.get(par1) : 0.0F;
    }
}
