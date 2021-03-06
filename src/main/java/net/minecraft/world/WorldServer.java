package net.minecraft.world;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEventData;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.exception.MinecraftException;
import net.minecraft.entity.*;
import net.minecraft.item.Item;
import net.minecraft.logging.ILogAgent;
import net.minecraft.player.PlayerManager;
import net.minecraft.player.score.ScoreboardSaveData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerBlockEventList;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.network.packet.*;
import net.minecraft.src.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.utils.enums.EnumCreatureType;
import net.minecraft.utils.weighted.WeightedRandom;
import net.minecraft.utils.weighted.WeightedRandomChestContent;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.*;
import net.minecraft.world.gen.world.WorldGeneratorBonusChest;
import net.minecraft.world.provider.WorldProvider;
import net.minecraft.world.storage.ISaveHandler;

import java.util.*;

public class WorldServer extends World {
    private final MinecraftServer mcServer;
    private final EntityTracker theEntityTracker;
    private final PlayerManager thePlayerManager;
    private Set<NextTickListEntry> pendingTickListEntriesHashSet;

    /**
     * All work to do in future ticks.
     */
    private TreeSet<NextTickListEntry> pendingTickListEntriesTreeSet;
    public ChunkProviderServer theChunkProviderServer;

    /**
     * Whether or not level saving is enabled
     */
    public boolean levelSaving;

    /**
     * is false if there are no players
     */
    private boolean allPlayersSleeping;
    private int updateEntityTick;
    private final Teleporter field_85177_Q;
    private final SpawnerAnimals field_135059_Q = new SpawnerAnimals();

    /**
     * Double buffer of ServerBlockEventList[] for holding pending BlockEventData's
     */
    private ServerBlockEventList[] blockEventCache = new ServerBlockEventList[]{new ServerBlockEventList(null), new ServerBlockEventList(null)};

    /**
     * The index into the blockEventCache; either 0, or 1, toggled in sendBlockEventPackets  where all BlockEvent are
     * applied locally and send to clients.
     */
    private int blockEventCacheIndex;
    private static final WeightedRandomChestContent[] bonusChestContent = new WeightedRandomChestContent[]{new WeightedRandomChestContent(Item.stick.itemID, 0, 1, 3, 10), new WeightedRandomChestContent(Block.planks.blockID, 0, 1, 3, 10), new WeightedRandomChestContent(Block.wood.blockID, 0, 1, 3, 10), new WeightedRandomChestContent(Item.axeStone.itemID, 0, 1, 1, 3), new WeightedRandomChestContent(Item.axeWood.itemID, 0, 1, 1, 5), new WeightedRandomChestContent(Item.pickaxeStone.itemID, 0, 1, 1, 3), new WeightedRandomChestContent(Item.pickaxeWood.itemID, 0, 1, 1, 5), new WeightedRandomChestContent(Item.appleRed.itemID, 0, 2, 3, 5), new WeightedRandomChestContent(Item.bread.itemID, 0, 2, 3, 3), new WeightedRandomChestContent(Item.bakedPotato.itemID, 0, 3, 3, 4)};
    private List<NextTickListEntry> pendingTickListEntriesThisTick = new ArrayList<NextTickListEntry>();

    /**
     * An IntHashMap of entity IDs (integers) to their Entity objects.
     */
    private IntHashMap entityIdMap;

    public WorldServer(MinecraftServer par1MinecraftServer, ISaveHandler par2ISaveHandler, String par3Str, int par4, WorldSettings par5WorldSettings, Profiler par6Profiler, ILogAgent par7ILogAgent) {
        super(par2ISaveHandler, par3Str, par5WorldSettings, WorldProvider.getProviderForDimension(par4), par6Profiler, par7ILogAgent);
        this.mcServer = par1MinecraftServer;
        this.theEntityTracker = new EntityTracker(this);
        this.thePlayerManager = new PlayerManager(this, par1MinecraftServer.getConfigurationManager().getViewDistance());

        if (this.entityIdMap == null) {
            this.entityIdMap = new IntHashMap();
        }

        if (this.pendingTickListEntriesHashSet == null) {
            this.pendingTickListEntriesHashSet = new HashSet<NextTickListEntry>();
        }

        if (this.pendingTickListEntriesTreeSet == null) {
            this.pendingTickListEntriesTreeSet = new TreeSet<NextTickListEntry>();
        }

        this.field_85177_Q = new Teleporter(this);
        this.worldScoreboard = new ServerScoreboard(par1MinecraftServer);
        ScoreboardSaveData var8 = (ScoreboardSaveData) this.mapStorage.loadData(ScoreboardSaveData.class, "scoreboard");

        if (var8 == null) {
            var8 = new ScoreboardSaveData();
            this.mapStorage.setData("scoreboard", var8);
        }

        var8.func_96499_a(this.worldScoreboard);
        ((ServerScoreboard) this.worldScoreboard).func_96547_a(var8);
    }

    /**
     * Runs a single tick for the world
     */
    @Override
    public void tick() {
        super.tick();

        if (this.getWorldInfo().isHardcoreModeEnabled() && this.difficultySetting < 3) {
            this.difficultySetting = 3;
        }

        this.provider.worldChunkMgr.cleanupCache();

        if (this.areAllPlayersAsleep()) {
            if (this.getGameRules().getGameRuleBooleanValue("doDaylightCycle")) {
                long var1 = this.worldInfo.getWorldTime() + 24000L;
                this.worldInfo.setWorldTime(var1 - var1 % 24000L);
            }

            this.wakeAllPlayers();
        }

        if (this.getGameRules().getGameRuleBooleanValue("doMobSpawning")) {
            this.field_135059_Q.findChunksForSpawning(this, this.spawnHostileMobs, this.spawnPeacefulMobs, this.worldInfo.getWorldTotalTime() % 400L == 0L);
        }

        this.chunkProvider.unloadQueuedChunks();
        int var3 = this.calculateSkylightSubtracted(1.0F);

        if (var3 != this.skylightSubtracted) {
            this.skylightSubtracted = var3;
        }

        this.worldInfo.incrementTotalWorldTime(this.worldInfo.getWorldTotalTime() + 1L);

        if (this.getGameRules().getGameRuleBooleanValue("doDaylightCycle")) {
            this.worldInfo.setWorldTime(this.worldInfo.getWorldTime() + 1L);
        }

        this.tickUpdates(false);
        this.tickBlocksAndAmbiance();
        this.thePlayerManager.updatePlayerInstances();
        this.villageCollectionObj.tick();
        this.villageSiegeObj.tick();
        this.field_85177_Q.removeStalePortalLocations(this.getTotalWorldTime());
        this.sendAndApplyBlockEvents();
    }

    /**
     * only spawns creatures allowed by the chunkProvider
     */
    public SpawnListEntry spawnRandomCreature(EnumCreatureType par1EnumCreatureType, int par2, int par3, int par4) {
        List var5 = this.getChunkProvider().getPossibleCreatures(par1EnumCreatureType, par2, par3, par4);
        return var5 != null && !var5.isEmpty() ? (SpawnListEntry) WeightedRandom.getRandomItem(this.rand, var5) : null;
    }

    /**
     * Updates the flag that indicates whether or not all players in the world are sleeping.
     */
    @Override
    public void updateAllPlayersSleepingFlag() {
        this.allPlayersSleeping = !this.playerEntities.isEmpty();

        for (EntityPlayer var2 : this.playerEntities) {
            if (!var2.isPlayerSleeping()) {
                this.allPlayersSleeping = false;
                break;
            }
        }
    }

    protected void wakeAllPlayers() {
        this.allPlayersSleeping = false;

        for (EntityPlayer var2 : this.playerEntities) {
            if (var2.isPlayerSleeping()) {
                var2.wakeUpPlayer(false, false, true);
            }
        }

        this.resetRainAndThunder();
    }

    private void resetRainAndThunder() {
        this.worldInfo.setRainTime(0);
        this.worldInfo.setRaining(false);
        this.worldInfo.setThunderTime(0);
        this.worldInfo.setThundering(false);
    }

    public boolean areAllPlayersAsleep() {
        if (this.allPlayersSleeping && !this.isRemote) {
            Iterator<EntityPlayer> var1 = this.playerEntities.iterator();
            EntityPlayer var2;

            do {
                if (!var1.hasNext()) {
                    return true;
                }

                var2 = var1.next();
            }
            while (var2.isPlayerFullyAsleep());

            return false;
        } else {
            return false;
        }
    }

    /**
     * plays random cave ambient sounds and runs updateTick on random blocks within each chunk in the vacinity of a
     * player
     */
    @Override
    protected void tickBlocksAndAmbiance() {
        super.tickBlocksAndAmbiance();

        for (ChunkCoordIntPair var4 : this.activeChunkSet) {
            int var5 = var4.chunkXPos * 16;
            int var6 = var4.chunkZPos * 16;
            Chunk var7 = this.getChunkFromChunkCoords(var4.chunkXPos, var4.chunkZPos);
            this.moodSoundAndLightCheck(var5, var6, var7);
            var7.updateSkylight();
            int var8;
            int var9;
            int var10;
            int var11;

            if (this.rand.nextInt(100000) == 0 && this.isRaining() && this.isThundering()) {
                this.updateLCG = this.updateLCG * 3 + 1013904223;
                var8 = this.updateLCG >> 2;
                var9 = var5 + (var8 & 15);
                var10 = var6 + (var8 >> 8 & 15);
                var11 = this.getPrecipitationHeight(var9, var10);

                if (this.canLightningStrikeAt(var9, var11, var10)) {
                    this.addWeatherEffect(new EntityLightningBolt(this, (double) var9, (double) var11, (double) var10));
                }
            }

            int var13;

            if (this.rand.nextInt(16) == 0) {
                this.updateLCG = this.updateLCG * 3 + 1013904223;
                var8 = this.updateLCG >> 2;
                var9 = var8 & 15;
                var10 = var8 >> 8 & 15;
                var11 = this.getPrecipitationHeight(var9 + var5, var10 + var6);

                if (this.isBlockFreezableNaturally(var9 + var5, var11 - 1, var10 + var6)) {
                    this.setBlock(var9 + var5, var11 - 1, var10 + var6, Block.ice.blockID);
                }

                if (this.isRaining() && this.canSnowAt(var9 + var5, var11, var10 + var6)) {
                    this.setBlock(var9 + var5, var11, var10 + var6, Block.snow.blockID);
                }

                if (this.isRaining()) {
                    BiomeGenBase var12 = this.getBiomeGenForCoords(var9 + var5, var10 + var6);

                    if (var12.canSpawnLightningBolt()) {
                        var13 = this.getBlockId(var9 + var5, var11 - 1, var10 + var6);

                        if (var13 != 0) {
                            Block.blocksList[var13].fillWithRain(this, var9 + var5, var11 - 1, var10 + var6);
                        }
                    }
                }
            }

            ExtendedBlockStorage[] var19 = var7.getBlockStorageArray();
            var9 = var19.length;

            for (var10 = 0; var10 < var9; ++var10) {
                ExtendedBlockStorage var21 = var19[var10];

                if (var21 != null && var21.getNeedsRandomTick()) {
                    for (int var20 = 0; var20 < 3; ++var20) {
                        this.updateLCG = this.updateLCG * 3 + 1013904223;
                        var13 = this.updateLCG >> 2;
                        int var14 = var13 & 15;
                        int var15 = var13 >> 8 & 15;
                        int var16 = var13 >> 16 & 15;
                        int var17 = var21.getExtBlockID(var14, var16, var15);
                        Block var18 = Block.blocksList[var17];

                        if (var18 != null && var18.getTickRandomly()) {
                            var18.updateTick(this, var14 + var5, var16 + var21.getYLocation(), var15 + var6, this.rand);
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns true if the given block will receive a scheduled tick in this tick. Args: X, Y, Z, blockID
     */
    @Override
    public boolean isBlockTickScheduledThisTick(int par1, int par2, int par3, int par4) {
        NextTickListEntry var5 = new NextTickListEntry(par1, par2, par3, par4);
        return this.pendingTickListEntriesThisTick.contains(var5);
    }

    /**
     * Used to schedule a call to the updateTick method on the specified block.
     */
    @Override
    public void scheduleBlockUpdate(int par1, int par2, int par3, int par4, int par5) {
        this.scheduleBlockUpdateWithPriority(par1, par2, par3, par4, par5, 0);
    }

    @Override
    public void scheduleBlockUpdateWithPriority(int par1, int par2, int par3, int par4, int par5, int par6) {
        NextTickListEntry var7 = new NextTickListEntry(par1, par2, par3, par4);
        byte var8 = 0;

        if (this.scheduledUpdatesAreImmediate && par4 > 0) {
            if (Block.blocksList[par4].func_82506_l()) {
                var8 = 8;

                if (this.checkChunksExist(var7.xCoord - var8, var7.yCoord - var8, var7.zCoord - var8, var7.xCoord + var8, var7.yCoord + var8, var7.zCoord + var8)) {
                    int var9 = this.getBlockId(var7.xCoord, var7.yCoord, var7.zCoord);

                    if (var9 == var7.blockID && var9 > 0) {
                        Block.blocksList[var9].updateTick(this, var7.xCoord, var7.yCoord, var7.zCoord, this.rand);
                    }
                }

                return;
            }

            par5 = 1;
        }

        if (this.checkChunksExist(par1 - var8, par2 - var8, par3 - var8, par1 + var8, par2 + var8, par3 + var8)) {
            if (par4 > 0) {
                var7.setScheduledTime((long) par5 + this.worldInfo.getWorldTotalTime());
                var7.setPriority(par6);
            }

            if (!this.pendingTickListEntriesHashSet.contains(var7)) {
                this.pendingTickListEntriesHashSet.add(var7);
                this.pendingTickListEntriesTreeSet.add(var7);
            }
        }
    }

    /**
     * Schedules a block update from the saved information in a chunk. Called when the chunk is loaded.
     */
    @Override
    public void scheduleBlockUpdateFromLoad(int par1, int par2, int par3, int par4, int par5, int par6) {
        NextTickListEntry var7 = new NextTickListEntry(par1, par2, par3, par4);
        var7.setPriority(par6);

        if (par4 > 0) {
            var7.setScheduledTime((long) par5 + this.worldInfo.getWorldTotalTime());
        }

        if (!this.pendingTickListEntriesHashSet.contains(var7)) {
            this.pendingTickListEntriesHashSet.add(var7);
            this.pendingTickListEntriesTreeSet.add(var7);
        }
    }

    /**
     * Updates (and cleans up) entities and tile entities
     */
    @Override
    public void updateEntities() {
        if (this.playerEntities.isEmpty()) {
            if (this.updateEntityTick++ >= 1200) {
                return;
            }
        } else {
            this.resetUpdateEntityTick();
        }

        super.updateEntities();
    }

    /**
     * Resets the updateEntityTick field to 0
     */
    public void resetUpdateEntityTick() {
        this.updateEntityTick = 0;
    }

    /**
     * Runs through the list of updates to run and ticks them
     */
    @Override
    public boolean tickUpdates(boolean par1) {
        int var2 = this.pendingTickListEntriesTreeSet.size();

        if (var2 != this.pendingTickListEntriesHashSet.size()) {
            throw new IllegalStateException("TickNextTick list out of synch");
        } else {
            if (var2 > 1000) {
                var2 = 1000;
            }

            NextTickListEntry var4;

            for (int var3 = 0; var3 < var2; ++var3) {
                var4 = this.pendingTickListEntriesTreeSet.first();

                if (!par1 && var4.scheduledTime > this.worldInfo.getWorldTotalTime()) {
                    break;
                }

                this.pendingTickListEntriesTreeSet.remove(var4);
                this.pendingTickListEntriesHashSet.remove(var4);
                this.pendingTickListEntriesThisTick.add(var4);
            }

            Iterator<NextTickListEntry> var14 = this.pendingTickListEntriesThisTick.iterator();

            while (var14.hasNext()) {
                var4 = var14.next();
                var14.remove();
                byte var5 = 0;

                if (this.checkChunksExist(var4.xCoord - var5, var4.yCoord - var5, var4.zCoord - var5, var4.xCoord + var5, var4.yCoord + var5, var4.zCoord + var5)) {
                    int var6 = this.getBlockId(var4.xCoord, var4.yCoord, var4.zCoord);

                    if (var6 > 0 && Block.isAssociatedBlockID(var6, var4.blockID)) {
                        try {
                            Block.blocksList[var6].updateTick(this, var4.xCoord, var4.yCoord, var4.zCoord, this.rand);
                        } catch (Throwable var13) {
                            CrashReport var8 = CrashReport.makeCrashReport(var13, "Exception while ticking a block");
                            CrashReportCategory var9 = var8.makeCategory("TweakBlock being ticked");
                            int var10;

                            try {
                                var10 = this.getBlockMetadata(var4.xCoord, var4.yCoord, var4.zCoord);
                            } catch (Throwable var12) {
                                var10 = -1;
                            }

                            CrashReportCategory.func_85068_a(var9, var4.xCoord, var4.yCoord, var4.zCoord, var6, var10);
                            throw new ReportedException(var8);
                        }
                    }
                } else {
                    this.scheduleBlockUpdate(var4.xCoord, var4.yCoord, var4.zCoord, var4.blockID, 0);
                }
            }

            this.pendingTickListEntriesThisTick.clear();
            return !this.pendingTickListEntriesTreeSet.isEmpty();
        }
    }

    @Override
    public List getPendingBlockUpdates(Chunk par1Chunk, boolean par2) {
        ArrayList<NextTickListEntry> var3 = null;
        ChunkCoordIntPair var4 = par1Chunk.getChunkCoordIntPair();
        int var5 = (var4.chunkXPos << 4) - 2;
        int var6 = var5 + 16 + 2;
        int var7 = (var4.chunkZPos << 4) - 2;
        int var8 = var7 + 16 + 2;

        for (int var9 = 0; var9 < 2; ++var9) {
            Iterator<NextTickListEntry> var10;

            if (var9 == 0) {
                var10 = this.pendingTickListEntriesTreeSet.iterator();
            } else {
                var10 = this.pendingTickListEntriesThisTick.iterator();

                if (!this.pendingTickListEntriesThisTick.isEmpty()) {
                    System.out.println(this.pendingTickListEntriesThisTick.size());
                }
            }

            while (var10.hasNext()) {
                NextTickListEntry var11 = var10.next();

                if (var11.xCoord >= var5 && var11.xCoord < var6 && var11.zCoord >= var7 && var11.zCoord < var8) {
                    if (par2) {
                        this.pendingTickListEntriesHashSet.remove(var11);
                        var10.remove();
                    }

                    if (var3 == null) {
                        var3 = new ArrayList<NextTickListEntry>();
                    }

                    var3.add(var11);
                }
            }
        }

        return var3;
    }

    /**
     * Will update the entity in the world if the chunk the entity is in is currently loaded or its forced to update.
     * Args: entity, forceUpdate
     */
    @Override
    public void updateEntityWithOptionalForce(Entity par1Entity, boolean par2) {
        if (!this.mcServer.getCanSpawnAnimals() && (par1Entity instanceof EntityAnimal || par1Entity instanceof EntityWaterMob)) {
            par1Entity.setDead();
        }

        if (!this.mcServer.getCanSpawnNPCs() && par1Entity instanceof INpc) {
            par1Entity.setDead();
        }

        super.updateEntityWithOptionalForce(par1Entity, par2);
    }

    /**
     * Creates the chunk provider for this world. Called in the constructor. Retrieves provider from worldProvider?
     */
    @Override
    protected IChunkProvider createChunkProvider() {
        IChunkLoader var1 = this.saveHandler.getChunkLoader(this.provider);
        this.theChunkProviderServer = new ChunkProviderServer(this, var1, this.provider.createChunkGenerator());
        return this.theChunkProviderServer;
    }

    /**
     * get a list of tileEntity's
     */
    public List<TileEntity> getTileEntityList(int par1, int par2, int par3, int par4, int par5, int par6) {
        ArrayList<TileEntity> var7 = new ArrayList<TileEntity>();

        for (TileEntity var9 : this.loadedTileEntityList) {
            if (var9.xCoord >= par1 && var9.yCoord >= par2 && var9.zCoord >= par3 && var9.xCoord < par4 && var9.yCoord < par5 && var9.zCoord < par6) {
                var7.add(var9);
            }
        }

        return var7;
    }

    /**
     * Called when checking if a certain block can be mined or not. The 'spawn safe zone' check is located here.
     */
    @Override
    public boolean canMineBlock(EntityPlayer par1EntityPlayer, int par2, int par3, int par4) {
        return !this.mcServer.func_96290_a(this, par2, par3, par4, par1EntityPlayer);
    }

    @Override
    protected void initialize(WorldSettings par1WorldSettings) {
        if (this.entityIdMap == null) {
            this.entityIdMap = new IntHashMap();
        }

        if (this.pendingTickListEntriesHashSet == null) {
            this.pendingTickListEntriesHashSet = new HashSet<NextTickListEntry>();
        }

        if (this.pendingTickListEntriesTreeSet == null) {
            this.pendingTickListEntriesTreeSet = new TreeSet<NextTickListEntry>();
        }

        this.createSpawnPosition(par1WorldSettings);
        super.initialize(par1WorldSettings);
    }

    /**
     * creates a spawn position at random within 256 blocks of 0,0
     */
    protected void createSpawnPosition(WorldSettings par1WorldSettings) {
        if (!this.provider.canRespawnHere()) {
            this.worldInfo.setSpawnPosition(0, this.provider.getAverageGroundLevel(), 0);
        } else {
            this.findingSpawnPoint = true;
            WorldChunkManager var2 = this.provider.worldChunkMgr;
            List<BiomeGenBase> var3 = var2.getBiomesToSpawnIn();
            Random var4 = new Random(this.getSeed());
            ChunkPosition var5 = var2.findBiomePosition(0, 0, 256, var3, var4);
            int var6 = 0;
            int var7 = this.provider.getAverageGroundLevel();
            int var8 = 0;

            if (var5 != null) {
                var6 = var5.x;
                var8 = var5.z;
            } else {
                this.getWorldLogAgent().logWarning("Unable to find spawn biome");
            }

            int var9 = 0;

            while (!this.provider.canCoordinateBeSpawn(var6, var8)) {
                var6 += var4.nextInt(64) - var4.nextInt(64);
                var8 += var4.nextInt(64) - var4.nextInt(64);
                ++var9;

                if (var9 == 1000) {
                    break;
                }
            }

            this.worldInfo.setSpawnPosition(var6, var7, var8);
            this.findingSpawnPoint = false;

            if (par1WorldSettings.isBonusChestEnabled()) {
                this.createBonusChest();
            }
        }
    }

    /**
     * Creates the bonus chest in the world.
     */
    protected void createBonusChest() {
        WorldGeneratorBonusChest var1 = new WorldGeneratorBonusChest(bonusChestContent, 10);

        for (int var2 = 0; var2 < 10; ++var2) {
            int var3 = this.worldInfo.getSpawnX() + this.rand.nextInt(6) - this.rand.nextInt(6);
            int var4 = this.worldInfo.getSpawnZ() + this.rand.nextInt(6) - this.rand.nextInt(6);
            int var5 = this.getTopSolidOrLiquidBlock(var3, var4) + 1;

            if (var1.generate(this, this.rand, var3, var5, var4)) {
                break;
            }
        }
    }

    /**
     * Gets the hard-coded portal location to use when entering this dimension.
     */
    public ChunkCoordinates getEntrancePortalLocation() {
        return this.provider.getEntrancePortalLocation();
    }

    /**
     * Saves all chunks to disk while updating progress bar.
     */
    public void saveAllChunks(boolean par1, IProgressUpdate par2IProgressUpdate) throws MinecraftException {
        if (this.chunkProvider.canSave()) {
            if (par2IProgressUpdate != null) {
                par2IProgressUpdate.displaySavingString("Saving level");
            }

            this.saveLevel();

            if (par2IProgressUpdate != null) {
                par2IProgressUpdate.displayLoadingString("Saving chunks");
            }

            this.chunkProvider.saveChunks(par1, par2IProgressUpdate);
        }
    }

    public void func_104140_m() {
        if (this.chunkProvider.canSave()) {
            this.chunkProvider.func_104112_b();
        }
    }

    /**
     * Saves the chunks to disk.
     */
    protected void saveLevel() throws MinecraftException {
        this.checkSessionLock();
        this.saveHandler.saveWorldInfoWithPlayer(this.worldInfo, this.mcServer.getConfigurationManager().getHostPlayerData());
        this.mapStorage.saveAllData();
    }

    @Override
    protected void onEntityAdded(Entity par1Entity) {
        super.onEntityAdded(par1Entity);
        this.entityIdMap.addKey(par1Entity.entityId, par1Entity);
        Entity[] var2 = par1Entity.getParts();

        if (var2 != null) {
            for (Entity aVar2 : var2) {
                this.entityIdMap.addKey(aVar2.entityId, aVar2);
            }
        }
    }

    @Override
    protected void onEntityRemoved(Entity par1Entity) {
        super.onEntityRemoved(par1Entity);
        this.entityIdMap.removeObject(par1Entity.entityId);
        Entity[] var2 = par1Entity.getParts();

        if (var2 != null) {
            for (Entity aVar2 : var2) {
                this.entityIdMap.removeObject(aVar2.entityId);
            }
        }
    }

    /**
     * Returns the Entity with the given ID, or null if it doesn't exist in this World.
     */
    @Override
    public Entity getEntityByID(int par1) {
        return (Entity) this.entityIdMap.lookup(par1);
    }

    /**
     * adds a lightning bolt to the list of lightning bolts in this world.
     */
    @Override
    public boolean addWeatherEffect(Entity par1Entity) {
        if (super.addWeatherEffect(par1Entity)) {
            this.mcServer.getConfigurationManager().sendPacketToPlayersAroundPoint(par1Entity.posX, par1Entity.posY, par1Entity.posZ, 512.0D, this.provider.dimensionId, new Packet71Weather(par1Entity));
            return true;
        } else {
            return false;
        }
    }

    /**
     * sends a Packet 38 (Entity Status) to all tracked players of that entity
     */
    @Override
    public void setEntityState(Entity par1Entity, byte par2) {
        Packet38EntityStatus var3 = new Packet38EntityStatus(par1Entity.entityId, par2);
        this.getEntityTracker().sendPacketToTrackedPlayersAndTrackedEntity(par1Entity, var3);
    }

    /**
     * returns a new explosion. Does initiation (at time of writing Explosion is not finished)
     */
    @Override
    public Explosion newExplosion(Entity par1Entity, double par2, double par4, double par6, float par8, boolean par9, boolean par10) {
        Explosion var11 = new Explosion(this, par1Entity, par2, par4, par6, par8);
        var11.isFlaming = par9;
        var11.isSmoking = par10;
        var11.doExplosionA();
        var11.doExplosionB(false);

        if (!par10) {
            var11.affectedBlockPositions.clear();
        }

        for (EntityPlayer var13 : this.playerEntities) {
            if (var13.getDistanceSq(par2, par4, par6) < 4096.0D) {
                ((EntityPlayerMP) var13).playerNetServerHandler.sendPacket(new Packet60Explosion(par2, par4, par6, par8, var11.affectedBlockPositions, var11.func_77277_b().get(var13)));
            }
        }

        return var11;
    }

    /**
     * Adds a block event with the given Args to the blockEventCache. During the next tick(), the block specified will
     * have its onBlockEvent handler called with the given parameters. Args: X,Y,Z, BlockID, EventID, EventParameter
     */
    @Override
    public void addBlockEvent(int par1, int par2, int par3, int par4, int par5, int par6) {
        BlockEventData var7 = new BlockEventData(par1, par2, par3, par4, par5, par6);
        Iterator var8 = this.blockEventCache[this.blockEventCacheIndex].iterator();
        BlockEventData var9;

        do {
            if (!var8.hasNext()) {
                this.blockEventCache[this.blockEventCacheIndex].add(var7);
                return;
            }

            var9 = (BlockEventData) var8.next();
        }
        while (!var9.equals(var7));
    }

    /**
     * Send and apply locally all pending BlockEvents to each player with 64m radius of the event.
     */
    private void sendAndApplyBlockEvents() {
        while (!this.blockEventCache[this.blockEventCacheIndex].isEmpty()) {
            int var1 = this.blockEventCacheIndex;
            this.blockEventCacheIndex ^= 1;

            for (Object o : this.blockEventCache[var1]) {

                if (this.onBlockEventReceived((BlockEventData) o)) {
                    this.mcServer.getConfigurationManager().sendPacketToPlayersAroundPoint((double) ((BlockEventData) o).getX(), (double) ((BlockEventData) o).getY(), (double) ((BlockEventData) o).getZ(), 64.0D, this.provider.dimensionId, new Packet54PlayNoteBlock(((BlockEventData) o).getX(), ((BlockEventData) o).getY(), ((BlockEventData) o).getZ(), ((BlockEventData) o).getBlockID(), ((BlockEventData) o).getEventID(), ((BlockEventData) o).getEventParameter()));
                }
            }

            this.blockEventCache[var1].clear();
        }
    }

    /**
     * Called to apply a pending BlockEvent to apply to the current world.
     */
    private boolean onBlockEventReceived(BlockEventData par1BlockEventData) {
        int var2 = this.getBlockId(par1BlockEventData.getX(), par1BlockEventData.getY(), par1BlockEventData.getZ());
        return var2 == par1BlockEventData.getBlockID() && Block.blocksList[var2].onBlockEventReceived(this, par1BlockEventData.getX(), par1BlockEventData.getY(), par1BlockEventData.getZ(), par1BlockEventData.getEventID(), par1BlockEventData.getEventParameter());
    }

    /**
     * Syncs all changes to disk and wait for completion.
     */
    public void flush() {
        this.saveHandler.flush();
    }

    /**
     * Updates all weather states.
     */
    @Override
    protected void updateWeather() {
        boolean var1 = this.isRaining();
        super.updateWeather();

        if (var1 != this.isRaining()) {
            if (var1) {
                this.mcServer.getConfigurationManager().sendPacketToAllPlayers(new Packet70GameEvent(2, 0));
            } else {
                this.mcServer.getConfigurationManager().sendPacketToAllPlayers(new Packet70GameEvent(1, 0));
            }
        }
    }

    /**
     * Gets the MinecraftServer.
     */
    public MinecraftServer getMinecraftServer() {
        return this.mcServer;
    }

    /**
     * Gets the EntityTracker
     */
    public EntityTracker getEntityTracker() {
        return this.theEntityTracker;
    }

    public PlayerManager getPlayerManager() {
        return this.thePlayerManager;
    }

    public Teleporter getDefaultTeleporter() {
        return this.field_85177_Q;
    }
}
