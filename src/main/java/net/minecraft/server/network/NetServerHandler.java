package net.minecraft.server.network;

import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.*;
import net.minecraft.inventory.InventoryPlayer;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerBeacon;
import net.minecraft.inventory.container.ContainerMerchant;
import net.minecraft.inventory.container.ContainerRepair;
import net.minecraft.inventory.slot.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemEditableBook;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemWritableBook;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ban.BanEntry;
import net.minecraft.server.network.packet.*;
import net.minecraft.src.IntHashMap;
import net.minecraft.src.ReportedException;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.utils.AxisAlignedBB;
import net.minecraft.utils.callable.CallablePacketClass;
import net.minecraft.utils.callable.CallablePacketID;
import net.minecraft.utils.chat.ChatAllowedCharacters;
import net.minecraft.utils.chat.ChatMessageComponent;
import net.minecraft.utils.enums.EnumChatFormatting;
import net.minecraft.world.WorldServer;
import org.minetweak.Minetweak;
import org.minetweak.chat.TabCompletion;
import org.minetweak.event.player.PlayerChatEvent;
import org.minetweak.server.Server;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class NetServerHandler extends NetHandler {
    /**
     * The underlying network manager for this server handler.
     */
    public final INetworkManager netManager;

    /**
     * Reference to the MinecraftServer object.
     */
    private final MinecraftServer mcServer;

    /**
     * This is set to true whenever a player disconnects from the server.
     */
    public boolean connectionClosed;

    /**
     * Reference to the EntityPlayerMP object.
     */
    public EntityPlayerMP playerEntity;

    /**
     * incremented each tick
     */
    private int currentTicks;

    /**
     * holds the amount of tick the player is floating
     */
    private int playerInAirTime;
    private int keepAliveRandomID;
    private long keepAliveTimeSent;

    /**
     * The Java Random object.
     */
    private static Random random = new Random();
    private long ticksOfLastKeepAlive;
    private int chatSpamThresholdCount;
    private int creativeItemCreationSpamThresholdTally;

    /**
     * The last known x position for this connection.
     */
    private double lastPosX;

    /**
     * The last known y position for this connection.
     */
    private double lastPosY;

    /**
     * The last known z position for this connection.
     */
    private double lastPosZ;

    /**
     * is true when the player has moved since his last movement packet
     */
    private boolean hasMoved = true;
    private IntHashMap ints = new IntHashMap();

    public NetServerHandler(MinecraftServer par1MinecraftServer, INetworkManager networkManager, EntityPlayerMP entityPlayer) {
        this.mcServer = par1MinecraftServer;
        this.netManager = networkManager;
        networkManager.setNetHandler(this);
        this.playerEntity = entityPlayer;
        entityPlayer.playerNetServerHandler = this;
    }

    /**
     * handle all the packets for the connection
     */
    public void handlePackets() {
        ++this.currentTicks;
        this.mcServer.profiler.startSection("packetflow");
        this.netManager.processReadPackets();
        this.mcServer.profiler.endStartSection("keepAlive");

        if ((long) this.currentTicks - this.ticksOfLastKeepAlive > 20L) {
            this.ticksOfLastKeepAlive = (long) this.currentTicks;
            this.keepAliveTimeSent = System.nanoTime() / 1000000L;
            this.keepAliveRandomID = random.nextInt();
            this.sendPacket(new Packet0KeepAlive(this.keepAliveRandomID));
        }

        if (this.chatSpamThresholdCount > 0) {
            --this.chatSpamThresholdCount;
        }

        if (this.creativeItemCreationSpamThresholdTally > 0) {
            --this.creativeItemCreationSpamThresholdTally;
        }
    }

    /**
     * Kick the offending player and give a reason why
     */
    public void kickPlayer(String par1Str) {
        if (!this.connectionClosed) {
            this.playerEntity.mountEntityAndWakeUp();
            this.sendPacket(new Packet255KickDisconnect(par1Str));
            this.netManager.serverShutdown();
            this.mcServer.getConfigurationManager().sendChatMessageToAll(ChatMessageComponent.createWithType("multiplayer.player.left", this.playerEntity.getTranslatedEntityName()).func_111059_a(EnumChatFormatting.YELLOW));
            this.mcServer.getConfigurationManager().playerLoggedOut(this.playerEntity);
            this.connectionClosed = true;
        }
    }

    @Override
    public void handlePlayerInput(Packet27PlayerInput par1Packet27PlayerInput) {
        this.playerEntity.func_110430_a(par1Packet27PlayerInput.func_111010_d(), par1Packet27PlayerInput.func_111012_f(), par1Packet27PlayerInput.func_111013_g(), par1Packet27PlayerInput.func_111011_h());
    }

    @Override
    public void handleFlying(Packet10Flying par1Packet10Flying) {
        WorldServer worldServer = this.mcServer.worldServerForDimension(this.playerEntity.dimension);

        if (!this.playerEntity.playerConqueredTheEnd) {
            double var3;

            if (!this.hasMoved) {
                var3 = par1Packet10Flying.yPosition - this.lastPosY;

                if (par1Packet10Flying.xPosition == this.lastPosX && var3 * var3 < 0.01D && par1Packet10Flying.zPosition == this.lastPosZ) {
                    this.hasMoved = true;
                }
            }

            if (this.hasMoved) {
                double var5;
                double var7;
                double var9;

                if (this.playerEntity.ridingEntity != null) {
                    float var34 = this.playerEntity.rotationYaw;
                    float var4 = this.playerEntity.rotationPitch;
                    this.playerEntity.ridingEntity.updateRiderPosition();
                    var5 = this.playerEntity.posX;
                    var7 = this.playerEntity.posY;
                    var9 = this.playerEntity.posZ;

                    if (par1Packet10Flying.rotating) {
                        var34 = par1Packet10Flying.yaw;
                        var4 = par1Packet10Flying.pitch;
                    }

                    this.playerEntity.onGround = par1Packet10Flying.onGround;
                    this.playerEntity.onUpdateEntity();
                    this.playerEntity.ySize = 0.0F;
                    this.playerEntity.setPositionAndRotation(var5, var7, var9, var34, var4);

                    if (this.playerEntity.ridingEntity != null) {
                        this.playerEntity.ridingEntity.updateRiderPosition();
                    }

                    this.mcServer.getConfigurationManager().serverUpdateMountedMovingPlayer(this.playerEntity);

                    if (this.hasMoved) {
                        this.lastPosX = this.playerEntity.posX;
                        this.lastPosY = this.playerEntity.posY;
                        this.lastPosZ = this.playerEntity.posZ;
                    }

                    worldServer.updateEntity(this.playerEntity);
                    return;
                }

                if (this.playerEntity.isPlayerSleeping()) {
                    this.playerEntity.onUpdateEntity();
                    this.playerEntity.setPositionAndRotation(this.lastPosX, this.lastPosY, this.lastPosZ, this.playerEntity.rotationYaw, this.playerEntity.rotationPitch);
                    worldServer.updateEntity(this.playerEntity);
                    return;
                }

                var3 = this.playerEntity.posY;
                this.lastPosX = this.playerEntity.posX;
                this.lastPosY = this.playerEntity.posY;
                this.lastPosZ = this.playerEntity.posZ;
                var5 = this.playerEntity.posX;
                var7 = this.playerEntity.posY;
                var9 = this.playerEntity.posZ;
                float var11 = this.playerEntity.rotationYaw;
                float var12 = this.playerEntity.rotationPitch;

                if (par1Packet10Flying.moving && par1Packet10Flying.yPosition == -999.0D && par1Packet10Flying.stance == -999.0D) {
                    par1Packet10Flying.moving = false;
                }

                double var13;

                if (par1Packet10Flying.moving) {
                    var5 = par1Packet10Flying.xPosition;
                    var7 = par1Packet10Flying.yPosition;
                    var9 = par1Packet10Flying.zPosition;
                    var13 = par1Packet10Flying.stance - par1Packet10Flying.yPosition;

                    if (!this.playerEntity.isPlayerSleeping() && (var13 > 1.65D || var13 < 0.1D)) {
                        this.kickPlayer("Illegal stance");
                        this.mcServer.getLogAgent().logWarning(this.playerEntity.getCommandSenderName() + " had an illegal stance: " + var13);
                        return;
                    }

                    if (Math.abs(par1Packet10Flying.xPosition) > 3.2E7D || Math.abs(par1Packet10Flying.zPosition) > 3.2E7D) {
                        this.kickPlayer("Illegal position");
                        return;
                    }
                }

                if (par1Packet10Flying.rotating) {
                    var11 = par1Packet10Flying.yaw;
                    var12 = par1Packet10Flying.pitch;
                }

                this.playerEntity.onUpdateEntity();
                this.playerEntity.ySize = 0.0F;
                this.playerEntity.setPositionAndRotation(this.lastPosX, this.lastPosY, this.lastPosZ, var11, var12);

                if (!this.hasMoved) {
                    return;
                }

                var13 = var5 - this.playerEntity.posX;
                double var15 = var7 - this.playerEntity.posY;
                double var17 = var9 - this.playerEntity.posZ;
                double var25;

                float var27 = 0.0625F;
                boolean var28 = worldServer.getCollidingBoundingBoxes(this.playerEntity, this.playerEntity.boundingBox.copy().contract((double) var27, (double) var27, (double) var27)).isEmpty();

                if (this.playerEntity.onGround && !par1Packet10Flying.onGround && var15 > 0.0D) {
                    this.playerEntity.addExhaustion(0.2F);
                }

                this.playerEntity.moveEntity(var13, var15, var17);
                this.playerEntity.onGround = par1Packet10Flying.onGround;
                this.playerEntity.addMovementStat(var13, var15, var17);
                double var29 = var15;
                var13 = var5 - this.playerEntity.posX;
                var15 = var7 - this.playerEntity.posY;

                if (var15 > -0.5D || var15 < 0.5D) {
                    var15 = 0.0D;
                }

                var17 = var9 - this.playerEntity.posZ;
                var25 = var13 * var13 + var15 * var15 + var17 * var17;
                boolean var31 = false;

                if (var25 > 0.0625D && !this.playerEntity.isPlayerSleeping() && !this.playerEntity.theItemInWorldManager.isCreative()) {
                    var31 = true;
                    this.mcServer.getLogAgent().logWarning(this.playerEntity.getCommandSenderName() + " moved wrongly!");
                }

                this.playerEntity.setPositionAndRotation(var5, var7, var9, var11, var12);
                boolean var32 = worldServer.getCollidingBoundingBoxes(this.playerEntity, this.playerEntity.boundingBox.copy().contract((double) var27, (double) var27, (double) var27)).isEmpty();

                if (var28 && (var31 || !var32) && !this.playerEntity.isPlayerSleeping()) {
                    this.setPlayerLocation(this.lastPosX, this.lastPosY, this.lastPosZ, var11, var12);
                    return;
                }

                AxisAlignedBB var33 = this.playerEntity.boundingBox.copy().expand((double) var27, (double) var27, (double) var27).addCoord(0.0D, -0.55D, 0.0D);

                if (!this.mcServer.isFlightAllowed() && !this.playerEntity.theItemInWorldManager.isCreative() && !worldServer.checkBlockCollision(var33)) {
                    if (var29 >= -0.03125D) {
                        ++this.playerInAirTime;

                        if (this.playerInAirTime > 80) {
                            this.mcServer.getLogAgent().logWarning(this.playerEntity.getCommandSenderName() + " was kicked for floating too long!");
                            this.kickPlayer("Flying is not enabled on this server");
                            return;
                        }
                    }
                } else {
                    this.playerInAirTime = 0;
                }

                this.playerEntity.onGround = par1Packet10Flying.onGround;
                this.mcServer.getConfigurationManager().serverUpdateMountedMovingPlayer(this.playerEntity);
                this.playerEntity.handleFalling(this.playerEntity.posY - var3, par1Packet10Flying.onGround);
            } else if (this.currentTicks % 20 == 0) {
                this.setPlayerLocation(this.lastPosX, this.lastPosY, this.lastPosZ, this.playerEntity.rotationYaw, this.playerEntity.rotationPitch);
            }
        }
    }

    /**
     * Moves the player to the specified destination and rotation
     */
    public void setPlayerLocation(double par1, double par3, double par5, float par7, float par8) {
        this.hasMoved = false;
        this.lastPosX = par1;
        this.lastPosY = par3;
        this.lastPosZ = par5;
        this.playerEntity.setPositionAndRotation(par1, par3, par5, par7, par8);
        sendPacket(new Packet13PlayerLookMove(par1, par3 + 1.6200000047683716D, par3, par5, par7, par8, false));
    }

    @Override
    public void handleBlockDig(Packet14BlockDig par1Packet14BlockDig) {
        WorldServer var2 = this.mcServer.worldServerForDimension(this.playerEntity.dimension);

        if (par1Packet14BlockDig.status == 4) {
            this.playerEntity.dropOneItem(false);
        } else if (par1Packet14BlockDig.status == 3) {
            this.playerEntity.dropOneItem(true);
        } else if (par1Packet14BlockDig.status == 5) {
            this.playerEntity.stopUsingItem();
        } else {
            boolean var3 = false;

            if (par1Packet14BlockDig.status == 0) {
                var3 = true;
            }

            if (par1Packet14BlockDig.status == 1) {
                var3 = true;
            }

            if (par1Packet14BlockDig.status == 2) {
                var3 = true;
            }

            int var4 = par1Packet14BlockDig.xPosition;
            int var5 = par1Packet14BlockDig.yPosition;
            int var6 = par1Packet14BlockDig.zPosition;

            if (var3) {
                double var7 = this.playerEntity.posX - ((double) var4 + 0.5D);
                double var9 = this.playerEntity.posY - ((double) var5 + 0.5D) + 1.5D;
                double var11 = this.playerEntity.posZ - ((double) var6 + 0.5D);
                double var13 = var7 * var7 + var9 * var9 + var11 * var11;

                if (var13 > 36.0D) {
                    return;
                }

                if (var5 >= this.mcServer.getBuildLimit()) {
                    return;
                }
            }

            if (par1Packet14BlockDig.status == 0) {
                if (!this.mcServer.func_96290_a(var2, var4, var5, var6, this.playerEntity)) {
                    this.playerEntity.theItemInWorldManager.onBlockClicked(var4, var5, var6, par1Packet14BlockDig.face);
                } else {
                    this.playerEntity.playerNetServerHandler.sendPacket(new Packet53BlockChange(var4, var5, var6, var2));
                }
            } else if (par1Packet14BlockDig.status == 2) {
                this.playerEntity.theItemInWorldManager.blockRemoving(var4, var5, var6);

                if (var2.getBlockId(var4, var5, var6) != 0) {
                    this.playerEntity.playerNetServerHandler.sendPacket(new Packet53BlockChange(var4, var5, var6, var2));
                }
            } else if (par1Packet14BlockDig.status == 1) {
                this.playerEntity.theItemInWorldManager.cancelDestroyingBlock(var4, var5, var6);

                if (var2.getBlockId(var4, var5, var6) != 0) {
                    this.playerEntity.playerNetServerHandler.sendPacket(new Packet53BlockChange(var4, var5, var6, var2));
                }
            }
        }
    }

    @Override
    public void handlePlace(Packet15Place par1Packet15Place) {
        WorldServer var2 = this.mcServer.worldServerForDimension(this.playerEntity.dimension);
        ItemStack var3 = this.playerEntity.inventory.getCurrentItem();
        boolean var4 = false;
        int var5 = par1Packet15Place.getXPosition();
        int var6 = par1Packet15Place.getYPosition();
        int var7 = par1Packet15Place.getZPosition();
        int var8 = par1Packet15Place.getDirection();

        if (par1Packet15Place.getDirection() == 255) {
            if (var3 == null) {
                return;
            }

            this.playerEntity.theItemInWorldManager.tryUseItem(this.playerEntity, var2, var3);
        } else if (par1Packet15Place.getYPosition() >= this.mcServer.getBuildLimit() - 1 && (par1Packet15Place.getDirection() == 1 || par1Packet15Place.getYPosition() >= this.mcServer.getBuildLimit())) {
            this.playerEntity.playerNetServerHandler.sendPacket(new Packet3Chat(ChatMessageComponent.createWithType("build.tooHigh", this.mcServer.getBuildLimit()).func_111059_a(EnumChatFormatting.RED)));
            var4 = true;
        } else {
            if (this.hasMoved && this.playerEntity.getDistanceSq((double) var5 + 0.5D, (double) var6 + 0.5D, (double) var7 + 0.5D) < 64.0D && !this.mcServer.func_96290_a(var2, var5, var6, var7, this.playerEntity)) {
                this.playerEntity.theItemInWorldManager.activateBlockOrUseItem(this.playerEntity, var2, var3, var5, var6, var7, var8, par1Packet15Place.getXOffset(), par1Packet15Place.getYOffset(), par1Packet15Place.getZOffset());
            }

            var4 = true;
        }

        if (var4) {
            this.playerEntity.playerNetServerHandler.sendPacket(new Packet53BlockChange(var5, var6, var7, var2));

            if (var8 == 0) {
                --var6;
            }

            if (var8 == 1) {
                ++var6;
            }

            if (var8 == 2) {
                --var7;
            }

            if (var8 == 3) {
                ++var7;
            }

            if (var8 == 4) {
                --var5;
            }

            if (var8 == 5) {
                ++var5;
            }

            this.playerEntity.playerNetServerHandler.sendPacket(new Packet53BlockChange(var5, var6, var7, var2));
        }

        var3 = this.playerEntity.inventory.getCurrentItem();

        if (var3 != null && var3.stackSize == 0) {
            this.playerEntity.inventory.mainInventory[this.playerEntity.inventory.currentItem] = null;
            var3 = null;
        }

        if (var3 == null || var3.getMaxItemUseDuration() == 0) {
            this.playerEntity.isChangingQuantityOnly = true;
            this.playerEntity.inventory.mainInventory[this.playerEntity.inventory.currentItem] = ItemStack.copyItemStack(this.playerEntity.inventory.mainInventory[this.playerEntity.inventory.currentItem]);
            Slot var9 = this.playerEntity.openContainer.getSlotFromInventory(this.playerEntity.inventory, this.playerEntity.inventory.currentItem);
            this.playerEntity.openContainer.detectAndSendChanges();
            this.playerEntity.isChangingQuantityOnly = false;

            if (!ItemStack.areItemStacksEqual(this.playerEntity.inventory.getCurrentItem(), par1Packet15Place.getItemStack())) {
                this.sendPacket(new Packet103SetSlot(this.playerEntity.openContainer.windowId, var9.slotNumber, this.playerEntity.inventory.getCurrentItem()));
            }
        }
    }

    @Override
    public void handleErrorMessage(String par1Str, Object[] par2ArrayOfObj) {
        this.mcServer.getLogAgent().logInfo(this.playerEntity.getCommandSenderName() + " lost connection: " + par1Str);
        this.mcServer.getConfigurationManager().sendChatMessageToAll(ChatMessageComponent.createWithType("multiplayer.player.left", this.playerEntity.getTranslatedEntityName()).func_111059_a(EnumChatFormatting.YELLOW));
        this.mcServer.getConfigurationManager().playerLoggedOut(this.playerEntity);
        this.connectionClosed = true;
    }

    /**
     * Default handler called for packets that don't have their own handlers in NetServerHandler; kicks player from the
     * server.
     */
    @Override
    public void unexpectedPacket(Packet par1Packet) {
        this.mcServer.getLogAgent().logWarning(this.getClass() + " wasn\'t prepared to deal with a " + par1Packet.getClass());
        this.kickPlayer("Protocol error, unexpected packet");
    }

    /**
     * Adds the packet to the underlying network manager's send queue.
     */
    public void sendPacket(Packet par1Packet) {
        if (par1Packet instanceof Packet3Chat) {
            Packet3Chat var2 = (Packet3Chat) par1Packet;
            int var3 = this.playerEntity.getChatVisibility();

            if (var3 == 2) {
                return;
            }

            if (var3 == 1 && !var2.getIsServer()) {
                return;
            }
        }

        try {
            this.netManager.addToSendQueue(par1Packet);
        } catch (Throwable var5) {
            CrashReport var6 = CrashReport.makeCrashReport(var5, "Sending packet");
            CrashReportCategory var4 = var6.makeCategory("Packet being sent");
            var4.addCrashSectionCallable("Packet ID", new CallablePacketID(this, par1Packet));
            var4.addCrashSectionCallable("Packet class", new CallablePacketClass(this, par1Packet));
            throw new ReportedException(var6);
        }
    }

    @Override
    public void handleBlockItemSwitch(Packet16BlockItemSwitch par1Packet16BlockItemSwitch) {
        if (par1Packet16BlockItemSwitch.id >= 0 && par1Packet16BlockItemSwitch.id < InventoryPlayer.getHotbarSize()) {
            this.playerEntity.inventory.currentItem = par1Packet16BlockItemSwitch.id;
        } else {
            this.mcServer.getLogAgent().logWarning(this.playerEntity.getCommandSenderName() + " tried to set an invalid carried item");
        }
    }

    @Override
    public void handleChat(Packet3Chat par1Packet3Chat) {
        if (this.playerEntity.getChatVisibility() == 2) {
            this.sendPacket(new Packet3Chat(ChatMessageComponent.createPremade("chat.cannotSend").func_111059_a(EnumChatFormatting.RED)));
        } else {
            String var2 = par1Packet3Chat.message;

            if (var2.length() > 100) {
                this.kickPlayer("Chat message too long");
            } else {
                var2 = org.apache.commons.lang3.StringUtils.normalizeSpace(var2);

                for (int var3 = 0; var3 < var2.length(); ++var3) {
                    if (!ChatAllowedCharacters.isAllowedCharacter(var2.charAt(var3))) {
                        this.kickPlayer("Illegal characters in chat");
                        return;
                    }
                }

                if (var2.startsWith("/")) {
                    this.handleSlashCommand(var2);
                } else {
                    if (this.playerEntity.getChatVisibility() == 1) {
                        this.sendPacket(new Packet3Chat(ChatMessageComponent.createPremade("chat.cannotSend").func_111059_a(EnumChatFormatting.RED)));
                        return;
                    }
                    PlayerChatEvent event = new PlayerChatEvent(Minetweak.getPlayerByName(this.playerEntity.getEntityName()), var2);
                    Minetweak.getEventBus().post(event);

                    if (!event.isCancelled()) {
                        ChatMessageComponent var4 = ChatMessageComponent.createWithType("chat.type.text", this.playerEntity.getTranslatedEntityName(), event.getMessage());
                        this.mcServer.getConfigurationManager().sendChatMessageToAll(var4, false);
                    } else {
                        return;
                    }
                }

                this.chatSpamThresholdCount += 20;

                if (this.chatSpamThresholdCount > 200 && !this.mcServer.getConfigurationManager().areCommandsAllowed(this.playerEntity.getCommandSenderName())) {
                    this.kickPlayer("disconnect.spam");
                }
            }
        }
    }

    /**
     * Processes a / command
     */
    private void handleSlashCommand(String par1Str) {
        Server.handleCommand(this.playerEntity, par1Str);
    }

    @Override
    public void handleAnimation(Packet18Animation par1Packet18Animation) {
        if (par1Packet18Animation.animate == 1) {
            this.playerEntity.swingItem();
        }
    }

    /**
     * runs registerPacket on the given Packet19EntityAction
     */
    @Override
    public void handleEntityAction(Packet19EntityAction par1Packet19EntityAction) {
        if (par1Packet19EntityAction.state == 1) {
            this.playerEntity.setSneaking(true);
        } else if (par1Packet19EntityAction.state == 2) {
            this.playerEntity.setSneaking(false);
        } else if (par1Packet19EntityAction.state == 4) {
            this.playerEntity.setSprinting(true);
        } else if (par1Packet19EntityAction.state == 5) {
            this.playerEntity.setSprinting(false);
        } else if (par1Packet19EntityAction.state == 3) {
            this.playerEntity.wakeUpPlayer(false, true, true);
            this.hasMoved = false;
        } else if (par1Packet19EntityAction.state == 6) {
            if (this.playerEntity.ridingEntity != null && this.playerEntity.ridingEntity instanceof EntityHorse) {
                ((EntityHorse) this.playerEntity.ridingEntity).func_110206_u(par1Packet19EntityAction.field_111009_c);
            }
        } else if (par1Packet19EntityAction.state == 7 && this.playerEntity.ridingEntity != null && this.playerEntity.ridingEntity instanceof EntityHorse) {
            ((EntityHorse) this.playerEntity.ridingEntity).func_110199_f(this.playerEntity);
        }
    }

    @Override
    public void handleKickDisconnect(Packet255KickDisconnect par1Packet255KickDisconnect) {
        this.netManager.networkShutdown("disconnect.quitting");
    }

    /**
     * return the number of chuckDataPackets from the netManager
     */
    public int getNumChunkDataPackets() {
        return this.netManager.getNumChunkDataPackets();
    }

    @Override
    public void handleUseEntity(Packet7UseEntity par1Packet7UseEntity) {
        WorldServer var2 = this.mcServer.worldServerForDimension(this.playerEntity.dimension);
        Entity var3 = var2.getEntityByID(par1Packet7UseEntity.targetEntity);

        if (var3 != null) {
            boolean var4 = this.playerEntity.canEntityBeSeen(var3);
            double var5 = 36.0D;

            if (!var4) {
                var5 = 9.0D;
            }

            if (this.playerEntity.getDistanceSqToEntity(var3) < var5) {
                if (par1Packet7UseEntity.isLeftClick == 0) {
                    this.playerEntity.interactWith(var3);
                } else if (par1Packet7UseEntity.isLeftClick == 1) {
                    if (var3 instanceof EntityItem || var3 instanceof EntityXPOrb || var3 instanceof EntityArrow || var3 == this.playerEntity) {
                        this.kickPlayer("Attempting to attack an invalid entity");
                        this.mcServer.logWarning("Player " + this.playerEntity.getCommandSenderName() + " tried to attack an invalid entity");
                        return;
                    }

                    this.playerEntity.attackTargetEntityWithCurrentItem(var3);
                }
            }
        }
    }

    @Override
    public void handleClientCommand(Packet205ClientCommand par1Packet205ClientCommand) {
        if (par1Packet205ClientCommand.forceRespawn == 1) {
            if (this.playerEntity.playerConqueredTheEnd) {
                this.playerEntity = this.mcServer.getConfigurationManager().recreatePlayerEntity(this.playerEntity, 0, true);
            } else if (this.playerEntity.getServerForPlayer().getWorldInfo().isHardcoreModeEnabled()) {
                BanEntry var2 = new BanEntry(this.playerEntity.getCommandSenderName());
                var2.setBanReason("Death in Hardcore");
                this.mcServer.getConfigurationManager().getBannedPlayers().put(var2);
                this.playerEntity.playerNetServerHandler.kickPlayer("You have died. Game over, man, it\'s game over!");
            } else {
                if (this.playerEntity.func_110143_aJ() > 0.0F) {
                    return;
                }

                this.playerEntity = this.mcServer.getConfigurationManager().recreatePlayerEntity(this.playerEntity, 0, false);
            }
        }
    }

    /**
     * If this returns false, all packets will be queued for the main thread to handle, even if they would otherwise be
     * processed asynchronously. Used to avoid processing packets on the client before the world has been downloaded
     * (which happens on the main thread)
     */
    @Override
    public boolean canProcessPacketsAsync() {
        return true;
    }

    @Override
    public void handleUpdateAttributes(Packet44UpdateAttributes par1Packet44UpdateAttributes) {
        this.func_110773_a(par1Packet44UpdateAttributes);
    }

    @Override
    public void handleTileEditorOpen(Packet133TileEditorOpen par1Packet133TileEditorOpen) {
        this.func_142031_a(par1Packet133TileEditorOpen);
    }

    /**
     * respawns the player
     */
    @Override
    public void handleRespawn(Packet9Respawn par1Packet9Respawn) {
    }

    @Override
    public void handleCloseWindow(Packet101CloseWindow par1Packet101CloseWindow) {
        this.playerEntity.closeContainer();
    }

    @Override
    public void handleWindowClick(Packet102WindowClick par1Packet102WindowClick) {
        if (this.playerEntity.openContainer.windowId == par1Packet102WindowClick.window_Id && this.playerEntity.openContainer.getCanCraft(this.playerEntity)) {
            ItemStack var2 = this.playerEntity.openContainer.slotClick(par1Packet102WindowClick.inventorySlot, par1Packet102WindowClick.mouseClick, par1Packet102WindowClick.holdingShift, this.playerEntity);

            if (ItemStack.areItemStacksEqual(par1Packet102WindowClick.itemStack, var2)) {
                this.playerEntity.playerNetServerHandler.sendPacket(new Packet106Transaction(par1Packet102WindowClick.window_Id, par1Packet102WindowClick.action, true));
                this.playerEntity.isChangingQuantityOnly = true;
                this.playerEntity.openContainer.detectAndSendChanges();
                this.playerEntity.updateHeldItem();
                this.playerEntity.isChangingQuantityOnly = false;
            } else {
                this.ints.addKey(this.playerEntity.openContainer.windowId, par1Packet102WindowClick.action);
                this.playerEntity.playerNetServerHandler.sendPacket(new Packet106Transaction(par1Packet102WindowClick.window_Id, par1Packet102WindowClick.action, false));
                this.playerEntity.openContainer.setCanCraft(this.playerEntity, false);
                ArrayList<ItemStack> var3 = new ArrayList<ItemStack>();

                for (int var4 = 0; var4 < this.playerEntity.openContainer.inventorySlots.size(); ++var4) {
                    var3.add((this.playerEntity.openContainer.inventorySlots.get(var4)).getStack());
                }

                this.playerEntity.updateCraftingInventory(this.playerEntity.openContainer, var3);
            }
        }
    }

    @Override
    public void handleEnchantItem(Packet108EnchantItem par1Packet108EnchantItem) {
        if (this.playerEntity.openContainer.windowId == par1Packet108EnchantItem.windowId && this.playerEntity.openContainer.getCanCraft(this.playerEntity)) {
            this.playerEntity.openContainer.enchantItem(this.playerEntity, par1Packet108EnchantItem.enchantment);
            this.playerEntity.openContainer.detectAndSendChanges();
        }
    }

    /**
     * Handle a creative slot packet.
     */
    @Override
    public void handleCreativeSetSlot(Packet107CreativeSetSlot par1Packet107CreativeSetSlot) {
        if (this.playerEntity.theItemInWorldManager.isCreative()) {
            boolean var2 = par1Packet107CreativeSetSlot.slot < 0;
            ItemStack var3 = par1Packet107CreativeSetSlot.itemStack;
            boolean var4 = par1Packet107CreativeSetSlot.slot >= 1 && par1Packet107CreativeSetSlot.slot < 36 + InventoryPlayer.getHotbarSize();
            boolean var5 = var3 == null || var3.itemID < Item.itemsList.length && var3.itemID >= 0 && Item.itemsList[var3.itemID] != null;
            boolean var6 = var3 == null || var3.getItemDamage() >= 0 && var3.getItemDamage() >= 0 && var3.stackSize <= 64 && var3.stackSize > 0;

            if (var4 && var5 && var6) {
                if (var3 == null) {
                    this.playerEntity.inventoryContainer.putStackInSlot(par1Packet107CreativeSetSlot.slot, null);
                } else {
                    this.playerEntity.inventoryContainer.putStackInSlot(par1Packet107CreativeSetSlot.slot, var3);
                }

                this.playerEntity.inventoryContainer.setCanCraft(this.playerEntity, true);
            } else if (var2 && var5 && var6 && this.creativeItemCreationSpamThresholdTally < 200) {
                this.creativeItemCreationSpamThresholdTally += 20;
                EntityItem var7 = this.playerEntity.dropPlayerItem(var3);

                if (var7 != null) {
                    var7.setAgeToCreativeDespawnTime();
                }
            }
        }
    }

    @Override
    public void handleTransaction(Packet106Transaction par1Packet106Transaction) {
        Short var2 = (Short) this.ints.lookup(this.playerEntity.openContainer.windowId);

        if (var2 != null && par1Packet106Transaction.shortWindowId == var2 && this.playerEntity.openContainer.windowId == par1Packet106Transaction.windowId && !this.playerEntity.openContainer.getCanCraft(this.playerEntity)) {
            this.playerEntity.openContainer.setCanCraft(this.playerEntity, true);
        }
    }

    /**
     * Updates Client side signs
     */
    @Override
    public void handleUpdateSign(Packet130UpdateSign par1Packet130UpdateSign) {
        WorldServer var2 = this.mcServer.worldServerForDimension(this.playerEntity.dimension);

        if (var2.blockExists(par1Packet130UpdateSign.xPosition, par1Packet130UpdateSign.yPosition, par1Packet130UpdateSign.zPosition)) {
            TileEntity var3 = var2.getBlockTileEntity(par1Packet130UpdateSign.xPosition, par1Packet130UpdateSign.yPosition, par1Packet130UpdateSign.zPosition);

            if (var3 instanceof TileEntitySign) {
                TileEntitySign var4 = (TileEntitySign) var3;

                if (!var4.isEditable() || var4.func_142009_b() != this.playerEntity) {
                    this.mcServer.logWarning("Player " + this.playerEntity.getCommandSenderName() + " just tried to change non-editable sign");
                    return;
                }
            }

            int var6;
            int var8;

            if (var3 instanceof TileEntitySign) {
                var8 = par1Packet130UpdateSign.xPosition;
                int var9 = par1Packet130UpdateSign.yPosition;
                var6 = par1Packet130UpdateSign.zPosition;
                TileEntitySign var7 = (TileEntitySign) var3;
                System.arraycopy(par1Packet130UpdateSign.signLines, 0, var7.signText, 0, 4);
                var7.onInventoryChanged();
                var2.markBlockForUpdate(var8, var9, var6);
            }
        }
    }

    /**
     * Handle a keep alive packet.
     */
    @Override
    public void handleKeepAlive(Packet0KeepAlive par1Packet0KeepAlive) {
        if (par1Packet0KeepAlive.randomId == this.keepAliveRandomID) {
            int var2 = (int) (System.nanoTime() / 1000000L - this.keepAliveTimeSent);
            this.playerEntity.ping = (this.playerEntity.ping * 3 + var2) / 4;
        }
    }

    /**
     * determine if it is a server handler
     */
    @Override
    public boolean isServerHandler() {
        return true;
    }

    /**
     * Handle a player abilities packet.
     */
    @Override
    public void handlePlayerAbilities(Packet202PlayerAbilities par1Packet202PlayerAbilities) {
        if (!playerEntity.getGameType().isCreative() && par1Packet202PlayerAbilities.getFlying()) {
            Minetweak.getPlayerByName(playerEntity.getEntityName()).kickPlayer("Stop Cheating or you WILL be banned.");
            return;
        }
        this.playerEntity.capabilities.isFlying = par1Packet202PlayerAbilities.getFlying() && this.playerEntity.capabilities.allowFlying;
    }

    @Override
    public void handleAutoComplete(Packet203AutoComplete packet) {
        StringBuilder builder = new StringBuilder();
        int i = 0;

        for (String part : TabCompletion.getMatches(Minetweak.getPlayerByName(playerEntity.getCommandSenderName().toLowerCase()), packet.getText())) {
            if (i > 0) {
                builder.append("\u0000");
            }
            builder.append(part);
            i++;
        }

        this.playerEntity.playerNetServerHandler.sendPacket(new Packet203AutoComplete(builder.toString()));
    }

    @Override
    public void handleClientInfo(Packet204ClientInfo par1Packet204ClientInfo) {
        this.playerEntity.updateClientInfo(par1Packet204ClientInfo);
    }

    @Override
    public void handleCustomPayload(Packet250CustomPayload par1Packet250CustomPayload) {
        DataInputStream var2;
        ItemStack var3;
        ItemStack var4;

        if ("MC|BEdit".equals(par1Packet250CustomPayload.channel)) {
            try {
                var2 = new DataInputStream(new ByteArrayInputStream(par1Packet250CustomPayload.data));
                var3 = Packet.readItemStack(var2);

                if (!ItemWritableBook.validBookTagPages(var3.getTagCompound())) {
                    throw new IOException("Invalid book tag!");
                }

                var4 = this.playerEntity.inventory.getCurrentItem();

                if (var3.itemID == Item.writableBook.itemID && var3.itemID == var4.itemID) {
                    var4.setTagInfo("pages", var3.getTagCompound().getTagList("pages"));
                }
            } catch (Exception var12) {
                var12.printStackTrace();
            }
        } else if ("MC|BSign".equals(par1Packet250CustomPayload.channel)) {
            try {
                var2 = new DataInputStream(new ByteArrayInputStream(par1Packet250CustomPayload.data));
                var3 = Packet.readItemStack(var2);

                if (!ItemEditableBook.validBookTagContents(var3.getTagCompound())) {
                    throw new IOException("Invalid book tag!");
                }

                var4 = this.playerEntity.inventory.getCurrentItem();

                if (var3.itemID == Item.writtenBook.itemID && var4.itemID == Item.writableBook.itemID) {
                    var4.setTagInfo("author", new NBTTagString("author", this.playerEntity.getCommandSenderName()));
                    var4.setTagInfo("title", new NBTTagString("title", var3.getTagCompound().getString("title")));
                    var4.setTagInfo("pages", var3.getTagCompound().getTagList("pages"));
                    var4.itemID = Item.writtenBook.itemID;
                }
            } catch (Exception var11) {
                var11.printStackTrace();
            }
        } else {
            int var14;

            if ("MC|TrSel".equals(par1Packet250CustomPayload.channel)) {
                try {
                    var2 = new DataInputStream(new ByteArrayInputStream(par1Packet250CustomPayload.data));
                    var14 = var2.readInt();
                    Container var16 = this.playerEntity.openContainer;

                    if (var16 instanceof ContainerMerchant) {
                        ((ContainerMerchant) var16).setCurrentRecipeIndex(var14);
                    }
                } catch (Exception var10) {
                    var10.printStackTrace();
                }
            } else {
                int var18;

                if ("MC|AdvCdm".equals(par1Packet250CustomPayload.channel)) {
                    if (!this.mcServer.isCommandBlockEnabled()) {
                        this.playerEntity.sendChatToPlayer(ChatMessageComponent.createPremade("advMode.notEnabled"));
                    } else if (this.playerEntity.canCommandSenderUseCommand(2, "") && this.playerEntity.capabilities.isCreativeMode) {
                        try {
                            var2 = new DataInputStream(new ByteArrayInputStream(par1Packet250CustomPayload.data));
                            var14 = var2.readInt();
                            var18 = var2.readInt();
                            int var5 = var2.readInt();
                            String var6 = Packet.readString(var2, 256);
                            TileEntity var7 = this.playerEntity.worldObj.getBlockTileEntity(var14, var18, var5);

                            if (var7 != null && var7 instanceof TileEntityCommandBlock) {
                                ((TileEntityCommandBlock) var7).setCommand(var6);
                                this.playerEntity.worldObj.markBlockForUpdate(var14, var18, var5);
                                this.playerEntity.sendChatToPlayer(ChatMessageComponent.createWithType("advMode.setCommand.success", var6));
                            }
                        } catch (Exception var9) {
                            var9.printStackTrace();
                        }
                    } else {
                        this.playerEntity.sendChatToPlayer(ChatMessageComponent.createPremade("advMode.notAllowed"));
                    }
                } else if ("MC|Beacon".equals(par1Packet250CustomPayload.channel)) {
                    if (this.playerEntity.openContainer instanceof ContainerBeacon) {
                        try {
                            var2 = new DataInputStream(new ByteArrayInputStream(par1Packet250CustomPayload.data));
                            var14 = var2.readInt();
                            var18 = var2.readInt();
                            ContainerBeacon var17 = (ContainerBeacon) this.playerEntity.openContainer;
                            Slot var19 = var17.getSlot(0);

                            if (var19.getHasStack()) {
                                var19.decrStackSize(1);
                                TileEntityBeacon var20 = var17.getBeacon();
                                var20.setPrimaryEffect(var14);
                                var20.setSecondaryEffect(var18);
                                var20.onInventoryChanged();
                            }
                        } catch (Exception var8) {
                            var8.printStackTrace();
                        }
                    }
                } else if ("MC|ItemName".equals(par1Packet250CustomPayload.channel) && this.playerEntity.openContainer instanceof ContainerRepair) {
                    ContainerRepair var13 = (ContainerRepair) this.playerEntity.openContainer;

                    if (par1Packet250CustomPayload.data != null && par1Packet250CustomPayload.data.length >= 1) {
                        String var15 = ChatAllowedCharacters.filerAllowedCharacters(new String(par1Packet250CustomPayload.data));

                        if (var15.length() <= 30) {
                            var13.updateItemName(var15);
                        }
                    } else {
                        var13.updateItemName("");
                    }
                }
            }
        }
    }

    @Override
    public boolean func_142032_c() {
        return this.connectionClosed;
    }
}
