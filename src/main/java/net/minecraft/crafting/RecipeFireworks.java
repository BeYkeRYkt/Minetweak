package net.minecraft.crafting;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;

import java.util.ArrayList;

public class RecipeFireworks implements IRecipe {
    private ItemStack stack;

    /**
     * Used to check if a crafting matches current crafting inventory
     */
    @Override
    public boolean matches(InventoryCrafting par1InventoryCrafting, World par2World) {
        this.stack = null;
        int var3 = 0;
        int var4 = 0;
        int var5 = 0;
        int var6 = 0;
        int var7 = 0;
        int var8 = 0;

        for (int var9 = 0; var9 < par1InventoryCrafting.getSizeInventory(); ++var9) {
            ItemStack var10 = par1InventoryCrafting.getStackInSlot(var9);

            if (var10 != null) {
                if (var10.itemID == Item.gunpowder.itemID) {
                    ++var4;
                } else if (var10.itemID == Item.fireworkCharge.itemID) {
                    ++var6;
                } else if (var10.itemID == Item.dyePowder.itemID) {
                    ++var5;
                } else if (var10.itemID == Item.paper.itemID) {
                    ++var3;
                } else if (var10.itemID == Item.glowstone.itemID) {
                    ++var7;
                } else if (var10.itemID == Item.diamond.itemID) {
                    ++var7;
                } else if (var10.itemID == Item.fireballCharge.itemID) {
                    ++var8;
                } else if (var10.itemID == Item.feather.itemID) {
                    ++var8;
                } else if (var10.itemID == Item.goldNugget.itemID) {
                    ++var8;
                } else {
                    if (var10.itemID != Item.skull.itemID) {
                        return false;
                    }

                    ++var8;
                }
            }
        }

        var7 += var5 + var8;

        if (var4 <= 3 && var3 <= 1) {
            NBTTagCompound var15;
            NBTTagCompound var18;

            if (var4 >= 1 && var3 == 1 && var7 == 0) {
                this.stack = new ItemStack(Item.firework);

                if (var6 > 0) {
                    var15 = new NBTTagCompound();
                    var18 = new NBTTagCompound("Fireworks");
                    NBTTagList var25 = new NBTTagList("Explosions");

                    for (int var22 = 0; var22 < par1InventoryCrafting.getSizeInventory(); ++var22) {
                        ItemStack var26 = par1InventoryCrafting.getStackInSlot(var22);

                        if (var26 != null && var26.itemID == Item.fireworkCharge.itemID && var26.hasTagCompound() && var26.getTagCompound().hasKey("Explosion")) {
                            var25.appendTag(var26.getTagCompound().getCompoundTag("Explosion"));
                        }
                    }

                    var18.setTag("Explosions", var25);
                    var18.setByte("Flight", (byte) var4);
                    var15.setTag("Fireworks", var18);
                    this.stack.setTagCompound(var15);
                }

                return true;
            } else if (var4 == 1 && var3 == 0 && var6 == 0 && var5 > 0 && var8 <= 1) {
                this.stack = new ItemStack(Item.fireworkCharge);
                var15 = new NBTTagCompound();
                var18 = new NBTTagCompound("Explosion");
                byte var21 = 0;
                ArrayList<Integer> var12 = new ArrayList<Integer>();

                for (int var13 = 0; var13 < par1InventoryCrafting.getSizeInventory(); ++var13) {
                    ItemStack var14 = par1InventoryCrafting.getStackInSlot(var13);

                    if (var14 != null) {
                        if (var14.itemID == Item.dyePowder.itemID) {
                            var12.add(ItemDye.dyeColors[var14.getItemDamage()]);
                        } else if (var14.itemID == Item.glowstone.itemID) {
                            var18.setBoolean("Flicker", true);
                        } else if (var14.itemID == Item.diamond.itemID) {
                            var18.setBoolean("Trail", true);
                        } else if (var14.itemID == Item.fireballCharge.itemID) {
                            var21 = 1;
                        } else if (var14.itemID == Item.feather.itemID) {
                            var21 = 4;
                        } else if (var14.itemID == Item.goldNugget.itemID) {
                            var21 = 2;
                        } else if (var14.itemID == Item.skull.itemID) {
                            var21 = 3;
                        }
                    }
                }

                int[] var24 = new int[var12.size()];

                for (int var27 = 0; var27 < var24.length; ++var27) {
                    var24[var27] = var12.get(var27);
                }

                var18.setIntArray("Colors", var24);
                var18.setByte("Type", var21);
                var15.setTag("Explosion", var18);
                this.stack.setTagCompound(var15);
                return true;
            } else if (var4 == 0 && var3 == 0 && var6 == 1 && var5 > 0 && var5 == var7) {
                ArrayList<Integer> var16 = new ArrayList<Integer>();

                for (int var20 = 0; var20 < par1InventoryCrafting.getSizeInventory(); ++var20) {
                    ItemStack var11 = par1InventoryCrafting.getStackInSlot(var20);

                    if (var11 != null) {
                        if (var11.itemID == Item.dyePowder.itemID) {
                            var16.add(ItemDye.dyeColors[var11.getItemDamage()]);
                        } else if (var11.itemID == Item.fireworkCharge.itemID) {
                            this.stack = var11.copy();
                            this.stack.stackSize = 1;
                        }
                    }
                }

                int[] var17 = new int[var16.size()];

                for (int var19 = 0; var19 < var17.length; ++var19) {
                    var17[var19] = var16.get(var19);
                }

                if (this.stack != null && this.stack.hasTagCompound()) {
                    NBTTagCompound var23 = this.stack.getTagCompound().getCompoundTag("Explosion");

                    if (var23 == null) {
                        return false;
                    } else {
                        var23.setIntArray("FadeColors", var17);
                        return true;
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Returns an Item that is the result of this crafting
     */
    @Override
    public ItemStack getCraftingResult(InventoryCrafting par1InventoryCrafting) {
        return this.stack.copy();
    }

    /**
     * Returns the size of the crafting area
     */
    @Override
    public int getRecipeSize() {
        return 10;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return this.stack;
    }
}
