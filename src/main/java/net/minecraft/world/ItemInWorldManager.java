package net.minecraft.world;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityPlayer;
import net.minecraft.entity.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.server.network.packet.Packet53BlockChange;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.utils.enums.EnumGameType;
import org.minetweak.Minetweak;
import org.minetweak.block.Block;
import org.minetweak.event.block.BlockBreakEvent;
import org.minetweak.event.block.BlockUseEvent;

public class ItemInWorldManager {
    /**
     * The world object that this object is connected to.
     */
    public World theWorld;

    /**
     * The EntityPlayerMP object that this object is connected to.
     */
    public EntityPlayerMP thisPlayerMP;
    private EnumGameType gameType;

    /**
     * True if the player is destroying a block
     */
    private boolean isDestroyingBlock;
    private int initialDamage;
    private int curBlockX;
    private int curBlockY;
    private int curBlockZ;
    private int curblockDamage;

    /**
     * Set to true when the "finished destroying block" packet is received but the block wasn't fully damaged yet. The
     * block will not be destroyed while this is false.
     */
    private boolean receivedFinishDiggingPacket;
    private int posX;
    private int posY;
    private int posZ;
    private int field_73093_n;
    private int durabilityRemainingOnBlock;

    public ItemInWorldManager(World par1World) {
        this.gameType = EnumGameType.NOT_SET;
        this.durabilityRemainingOnBlock = -1;
        this.theWorld = par1World;
    }

    public void setGameType(EnumGameType par1EnumGameType) {
        this.gameType = par1EnumGameType;
        par1EnumGameType.configurePlayerCapabilities(this.thisPlayerMP.capabilities);
        this.thisPlayerMP.sendPlayerAbilities();
    }

    public EnumGameType getGameType() {
        return this.gameType;
    }

    /**
     * Get if we are in creative game mode.
     */
    public boolean isCreative() {
        return this.gameType.isCreative();
    }

    /**
     * if the gameType is currently NOT_SET then change it to par1
     */
    public void initializeGameType(EnumGameType par1EnumGameType) {
        if (this.gameType == EnumGameType.NOT_SET) {
            this.gameType = par1EnumGameType;
        }

        this.setGameType(this.gameType);
    }

    public void updateBlockRemoving() {
        ++this.curblockDamage;
        int var1;
        float var4;
        int var5;

        if (this.receivedFinishDiggingPacket) {
            var1 = this.curblockDamage - this.field_73093_n;
            int var2 = this.theWorld.getBlockId(this.posX, this.posY, this.posZ);

            if (var2 == 0) {
                this.receivedFinishDiggingPacket = false;
            } else {
                net.minecraft.block.Block var3 = net.minecraft.block.Block.blocksList[var2];
                var4 = var3.getPlayerRelativeBlockHardness(this.thisPlayerMP, this.thisPlayerMP.worldObj, this.posX, this.posY, this.posZ) * (float) (var1 + 1);
                var5 = (int) (var4 * 10.0F);

                if (var5 != this.durabilityRemainingOnBlock) {
                    this.theWorld.destroyBlockInWorldPartially(this.thisPlayerMP.entityId, this.posX, this.posY, this.posZ, var5);
                    this.durabilityRemainingOnBlock = var5;
                }

                if (var4 >= 1.0F) {
                    this.receivedFinishDiggingPacket = false;
                    this.tryHarvestBlock(this.posX, this.posY, this.posZ);
                }
            }
        } else if (this.isDestroyingBlock) {
            var1 = this.theWorld.getBlockId(this.curBlockX, this.curBlockY, this.curBlockZ);
            net.minecraft.block.Block var6 = net.minecraft.block.Block.blocksList[var1];

            if (var6 == null) {
                this.theWorld.destroyBlockInWorldPartially(this.thisPlayerMP.entityId, this.curBlockX, this.curBlockY, this.curBlockZ, -1);
                this.durabilityRemainingOnBlock = -1;
                this.isDestroyingBlock = false;
            } else {
                int var7 = this.curblockDamage - this.initialDamage;
                var4 = var6.getPlayerRelativeBlockHardness(this.thisPlayerMP, this.thisPlayerMP.worldObj, this.curBlockX, this.curBlockY, this.curBlockZ) * (float) (var7 + 1);
                var5 = (int) (var4 * 10.0F);

                if (var5 != this.durabilityRemainingOnBlock) {
                    this.theWorld.destroyBlockInWorldPartially(this.thisPlayerMP.entityId, this.curBlockX, this.curBlockY, this.curBlockZ, var5);
                    this.durabilityRemainingOnBlock = var5;
                }
            }
        }
    }

    /**
     * if not creative, it calls destroyBlockInWorldPartially until the block is broken first. par4 is the specific
     * side. tryHarvestBlock can also be the result of this call
     */
    public void onBlockClicked(int par1, int par2, int par3, int par4) {
        if (this.isCreative()) {
            if (!this.theWorld.extinguishFire(null, par1, par2, par3, par4)) {
                this.tryHarvestBlock(par1, par2, par3);
            }
            return;
        }
        if (!this.gameType.isAdventure() || this.thisPlayerMP.canHarvestBlock(net.minecraft.block.Block.blocksList[this.theWorld.getBlockId(par1, par2, par3)])) {
            this.theWorld.extinguishFire(null, par1, par2, par3, par4);
            this.initialDamage = this.curblockDamage;
            float var5 = 1.0F;
            int var6 = this.theWorld.getBlockId(par1, par2, par3);

            if (var6 > 0) {
                net.minecraft.block.Block.blocksList[var6].onBlockClicked(this.theWorld, par1, par2, par3, this.thisPlayerMP);
                var5 = net.minecraft.block.Block.blocksList[var6].getPlayerRelativeBlockHardness(this.thisPlayerMP, this.thisPlayerMP.worldObj, par1, par2, par3);
            }

            if (var6 > 0 && var5 >= 1.0F) {
                this.tryHarvestBlock(par1, par2, par3);
            } else {
                this.isDestroyingBlock = true;
                this.curBlockX = par1;
                this.curBlockY = par2;
                this.curBlockZ = par3;
                int var7 = (int) (var5 * 10.0F);
                this.theWorld.destroyBlockInWorldPartially(this.thisPlayerMP.entityId, par1, par2, par3, var7);
                this.durabilityRemainingOnBlock = var7;
            }
        }
    }

    public void blockRemoving(int par1, int par2, int par3) {
        if (par1 == this.curBlockX && par2 == this.curBlockY && par3 == this.curBlockZ) {
            int var4 = this.curblockDamage - this.initialDamage;
            int var5 = this.theWorld.getBlockId(par1, par2, par3);

            if (var5 != 0) {
                net.minecraft.block.Block var6 = net.minecraft.block.Block.blocksList[var5];
                float var7 = var6.getPlayerRelativeBlockHardness(this.thisPlayerMP, this.thisPlayerMP.worldObj, par1, par2, par3) * (float) (var4 + 1);

                if (var7 >= 0.7F) {
                    this.isDestroyingBlock = false;
                    this.theWorld.destroyBlockInWorldPartially(this.thisPlayerMP.entityId, par1, par2, par3, -1);
                    this.tryHarvestBlock(par1, par2, par3);
                } else if (!this.receivedFinishDiggingPacket) {
                    this.isDestroyingBlock = false;
                    this.receivedFinishDiggingPacket = true;
                    this.posX = par1;
                    this.posY = par2;
                    this.posZ = par3;
                    this.field_73093_n = this.initialDamage;
                }
            }
        }
    }

    /**
     * note: this ignores the pars passed in and continues to destroy the onClickedBlock
     */
    public void cancelDestroyingBlock(int par1, int par2, int par3) {
        this.isDestroyingBlock = false;
        this.theWorld.destroyBlockInWorldPartially(this.thisPlayerMP.entityId, this.curBlockX, this.curBlockY, this.curBlockZ, -1);
    }

    /**
     * Removes a block and triggers the appropriate events
     */
    private boolean removeBlock(int par1, int par2, int par3) {
        net.minecraft.block.Block var4 = net.minecraft.block.Block.blocksList[this.theWorld.getBlockId(par1, par2, par3)];
        int var5 = this.theWorld.getBlockMetadata(par1, par2, par3);

        if (var4 != null) {
            var4.onBlockHarvested(this.theWorld, par1, par2, par3, var5, this.thisPlayerMP);
        }

        boolean var6 = this.theWorld.setBlockToAir(par1, par2, par3);

        if (var4 != null && var6) {
            var4.onBlockDestroyedByPlayer(this.theWorld, par1, par2, par3, var5);
        }

        return var6;
    }

    /**
     * Attempts to harvest a block at the given coordinate
     */
    public boolean tryHarvestBlock(int par1, int par2, int par3) {
        BlockBreakEvent event = null;

        if (this.thisPlayerMP != null) {
            Block tweakBlock = this.theWorld.getWorld().getBlockAt(par1, par2, par3);

            if (theWorld.getBlockTileEntity(par1, par2, par3) == null) {
                Packet53BlockChange packet = new Packet53BlockChange(par1, par2, par3, this.theWorld);
                packet.type = 0;
                packet.metadata = 0;
                this.thisPlayerMP.playerNetServerHandler.sendPacket(packet);
            }

            event = new BlockBreakEvent(tweakBlock, Minetweak.getPlayerByName(this.thisPlayerMP.username.toLowerCase()));
            if (!isCreative() && !getGameType().isAdventure() && !this.thisPlayerMP.canHarvestBlock(net.minecraft.block.Block.blocksList[this.theWorld.getBlockId(par1, par2, par3)]))
                event.setCancelled(true);

            if (getGameType().isAdventure())
                event.setCancelled(true);
            Item currentItem = null;
            if (thisPlayerMP.getCurrentEquippedItem() != null)
                currentItem = thisPlayerMP.getCurrentEquippedItem().getItem();

            if (isCreative() && currentItem != null && currentItem instanceof ItemSword)
                event.setCancelled(true);

            net.minecraft.block.Block nmsBlock = net.minecraft.block.Block.blocksList[this.theWorld.getBlockId(par1, par2, par3)];

            if (nmsBlock != null && !event.isCancelled() && !this.isCreative() && !this.thisPlayerMP.canHarvestBlock(nmsBlock)) {
                if (!(nmsBlock.canSilkHarvest() && EnchantmentHelper.getSilkTouchModifier(this.thisPlayerMP))) {
                    int data = tweakBlock.getData();
                    int bonusLevel = EnchantmentHelper.getFortuneModifier(this.thisPlayerMP);
                    event.setExpToDrop(nmsBlock.getExpDrop(this.theWorld, data, bonusLevel));
                }
            }

            if (thisPlayerMP.getCurrentEquippedItem() == null)
                // Used to fix bug MINETWEAK-16
                event.setCancelled(false);

            Minetweak.getEventBus().post(event);

            if (event.isCancelled()) {
                // Let the client know the Block still exists
                this.thisPlayerMP.playerNetServerHandler.sendPacket(new Packet53BlockChange(par1, par2, par3, this.theWorld));
                // Update any tile entity data for this Block
                TileEntity tileentity = this.theWorld.getBlockTileEntity(par1, par2, par3);

                if (tileentity != null) {
                    this.thisPlayerMP.playerNetServerHandler.sendPacket(tileentity.getDescriptionPacket());
                }

                return false;
            }
        }

        int l = this.theWorld.getBlockId(par1, par2, par3);

        if (net.minecraft.block.Block.blocksList[l] == null) {
            return false;
        }

        int i1 = this.theWorld.getBlockMetadata(par1, par2, par3);

        if (l == net.minecraft.block.Block.skull.blockID && !this.isCreative()) {
            net.minecraft.block.Block.skull.dropBlockAsItemWithChance(theWorld, par1, par2, par3, i1, 1.0F, 0);
            return this.removeBlock(par1, par2, par3);
        }

        this.theWorld.playAuxSFXAtEntity(this.thisPlayerMP, 2001, par1, par2, par3, l + (this.theWorld.getBlockMetadata(par1, par2, par3) << 12));
        boolean flag;

        if (this.isCreative()) {
            flag = this.removeBlock(par1, par2, par3);
            this.thisPlayerMP.playerNetServerHandler.sendPacket(new Packet53BlockChange(par1, par2, par3, this.theWorld));
        } else {
            ItemStack itemstack = this.thisPlayerMP.getCurrentEquippedItem();
            int var4 = this.theWorld.getBlockId(par1, par2, par3);

            if (itemstack != null) {
                itemstack.onBlockDestroyed(this.theWorld, l, par1, par2, par3, this.thisPlayerMP);

                if (itemstack.stackSize == 0) {
                    this.thisPlayerMP.destroyCurrentEquippedItem();
                }
            }
            if (this.thisPlayerMP.canHarvestBlock(net.minecraft.block.Block.blocksList[var4])) {
                net.minecraft.block.Block.blocksList[var4].harvestBlock(this.theWorld, this.thisPlayerMP, par1, par2, par3, i1);
            }
            flag = this.removeBlock(par1, par2, par3);
        }

        if (flag && event != null) {
            net.minecraft.block.Block.blocksList[l].func_71923_g_CodeFix_Public(this.theWorld, par1, par2, par3, event.getExpToDrop());
        }

        return flag;
    }

    /**
     * Attempts to right-click use an item by the given EntityPlayer in the given World
     */
    public boolean tryUseItem(EntityPlayer par1EntityPlayer, World par2World, ItemStack par3ItemStack) {
        int var4 = par3ItemStack.stackSize;
        int var5 = par3ItemStack.getItemDamage();
        ItemStack var6 = par3ItemStack.useItemRightClick(par2World, par1EntityPlayer);

        if (var6 == par3ItemStack && (var6.stackSize == var4 && var6.getMaxItemUseDuration() <= 0 && var6.getItemDamage() == var5)) {
            return false;
        } else {
            par1EntityPlayer.inventory.mainInventory[par1EntityPlayer.inventory.currentItem] = var6;

            if (this.isCreative()) {
                var6.stackSize = var4;

                if (var6.isItemStackDamageable()) {
                    var6.setItemDamage(var5);
                }
            }

            if (var6.stackSize == 0) {
                par1EntityPlayer.inventory.mainInventory[par1EntityPlayer.inventory.currentItem] = null;
            }

            if (!par1EntityPlayer.isUsingItem()) {
                ((EntityPlayerMP) par1EntityPlayer).sendContainerToPlayer(par1EntityPlayer.inventoryContainer);
            }

            return true;
        }
    }

    /**
     * Activate the clicked on block, otherwise use the held item. Args: player, world, itemStack, x, y, z, side,
     * xOffset, yOffset, zOffset
     */
    public boolean activateBlockOrUseItem(EntityPlayer par1EntityPlayer, World par2World, ItemStack par3ItemStack, int par4, int par5, int par6, int par7, float par8, float par9, float par10) {
        int var11;

        if (!par1EntityPlayer.isSneaking() || par1EntityPlayer.getHeldItem() == null) {
            var11 = par2World.getBlockId(par4, par5, par6);

            BlockUseEvent event = new BlockUseEvent(Minetweak.getPlayerByName(par1EntityPlayer.getCommandSenderName()), new Block(new org.minetweak.world.Chunk(par2World.getChunkFromBlockCoords(par4, par5)), par4, par5, par6));
            Minetweak.getEventBus().post(event);
            if (event.isCancelled()) {
                return false;
            }
            if (var11 > 0 && net.minecraft.block.Block.blocksList[var11].onBlockActivated(par2World, par4, par5, par6, par1EntityPlayer, par7, par8, par9, par10)) {
                return true;
            }
        }

        if (par3ItemStack == null) {
            return false;
        } else if (this.isCreative()) {
            var11 = par3ItemStack.getItemDamage();
            int var12 = par3ItemStack.stackSize;
            boolean var13 = par3ItemStack.tryPlaceItemIntoWorld(par1EntityPlayer, par2World, par4, par5, par6, par7, par8, par9, par10);
            par3ItemStack.setItemDamage(var11);
            par3ItemStack.stackSize = var12;
            return var13;
        } else {
            return par3ItemStack.tryPlaceItemIntoWorld(par1EntityPlayer, par2World, par4, par5, par6, par7, par8, par9, par10);
        }
    }

    /**
     * Sets the world instance.
     */
    public void setWorld(WorldServer par1WorldServer) {
        this.theWorld = par1WorldServer;
    }
}