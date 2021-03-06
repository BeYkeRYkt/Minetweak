package net.minecraft.world.chunk;

import net.minecraft.block.Block;
import net.minecraft.src.ExtendedBlockStorage;
import net.minecraft.src.IProgressUpdate;
import net.minecraft.utils.enums.EnumCreatureType;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.FlatGeneratorInfo;
import net.minecraft.world.gen.FlatLayerInfo;
import net.minecraft.world.gen.world.WorldGenDungeons;
import net.minecraft.world.gen.world.WorldGenLakes;
import net.minecraft.world.map.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ChunkProviderFlat implements IChunkProvider {
    private World worldObj;
    private Random random;
    private final byte[] field_82700_c = new byte[256];
    private final byte[] field_82698_d = new byte[256];
    private final FlatGeneratorInfo field_82699_e;
    private final List<MapGenStructure> structureGenerators = new ArrayList<MapGenStructure>();
    private final boolean field_82697_g;
    private final boolean field_82702_h;
    private WorldGenLakes waterLakeGenerator;
    private WorldGenLakes lavaLakeGenerator;

    public ChunkProviderFlat(World par1World, long par2, boolean par4, String par5Str) {
        this.worldObj = par1World;
        this.random = new Random(par2);
        this.field_82699_e = FlatGeneratorInfo.createFlatGeneratorFromString(par5Str);

        if (par4) {
            Map var6 = this.field_82699_e.getWorldFeatures();

            if (var6.containsKey("village")) {
                Map var7 = (Map) var6.get("village");

                if (!var7.containsKey("size")) {
                    var7.put("size", "1");
                }

                this.structureGenerators.add(new MapGenVillage(var7));
            }

            if (var6.containsKey("biome_1")) {
                this.structureGenerators.add(new MapGenScatteredFeature((Map) var6.get("biome_1")));
            }

            if (var6.containsKey("mineshaft")) {
                this.structureGenerators.add(new MapGenMineshaft((Map) var6.get("mineshaft")));
            }

            if (var6.containsKey("stronghold")) {
                this.structureGenerators.add(new MapGenStronghold((Map) var6.get("stronghold")));
            }
        }

        this.field_82697_g = this.field_82699_e.getWorldFeatures().containsKey("decoration");

        if (this.field_82699_e.getWorldFeatures().containsKey("lake")) {
            this.waterLakeGenerator = new WorldGenLakes(Block.waterStill.blockID);
        }

        if (this.field_82699_e.getWorldFeatures().containsKey("lava_lake")) {
            this.lavaLakeGenerator = new WorldGenLakes(Block.lavaStill.blockID);
        }

        this.field_82702_h = this.field_82699_e.getWorldFeatures().containsKey("dungeon");

        for (Object o : this.field_82699_e.getFlatLayers()) {
            FlatLayerInfo var10 = (FlatLayerInfo) o;

            for (int var8 = var10.getMinY(); var8 < var10.getMinY() + var10.getLayerCount(); ++var8) {
                this.field_82700_c[var8] = (byte) (var10.getFillBlock() & 255);
                this.field_82698_d[var8] = (byte) var10.getFillBlockMeta();
            }
        }
    }

    /**
     * loads or generates the chunk at the chunk location specified
     */
    @Override
    public Chunk loadChunk(int par1, int par2) {
        return this.provideChunk(par1, par2);
    }

    /**
     * Will return back a chunk, if it doesn't exist and its not a MP client it will generates all the blocks for the
     * specified chunk from the map seed and chunk seed
     */
    @Override
    public Chunk provideChunk(int par1, int par2) {
        Chunk var3 = new Chunk(this.worldObj, par1, par2);

        for (int var4 = 0; var4 < this.field_82700_c.length; ++var4) {
            int var5 = var4 >> 4;
            ExtendedBlockStorage var6 = var3.getBlockStorageArray()[var5];

            if (var6 == null) {
                var6 = new ExtendedBlockStorage(var4, !this.worldObj.provider.hasNoSky);
                var3.getBlockStorageArray()[var5] = var6;
            }

            for (int var7 = 0; var7 < 16; ++var7) {
                for (int var8 = 0; var8 < 16; ++var8) {
                    var6.setExtBlockID(var7, var4 & 15, var8, this.field_82700_c[var4] & 255);
                    var6.setExtBlockMetadata(var7, var4 & 15, var8, this.field_82698_d[var4]);
                }
            }
        }

        var3.generateSkylightMap();
        BiomeGenBase[] var9 = this.worldObj.getWorldChunkManager().loadBlockGeneratorData(null, par1 * 16, par2 * 16, 16, 16);
        byte[] var10 = var3.getBiomeArray();

        for (int var11 = 0; var11 < var10.length; ++var11) {
            var10[var11] = (byte) var9[var11].biomeID;
        }

        for (MapGenStructure structureGenerator : this.structureGenerators) {
            structureGenerator.generate(this, this.worldObj, par1, par2, null);
        }

        var3.generateSkylightMap();
        return var3;
    }

    /**
     * Checks to see if a chunk exists at x, y
     */
    @Override
    public boolean chunkExists(int par1, int par2) {
        return true;
    }

    /**
     * Populates chunk with ores etc etc
     */
    @Override
    public void populate(IChunkProvider par1IChunkProvider, int par2, int par3) {
        int var4 = par2 * 16;
        int var5 = par3 * 16;
        BiomeGenBase var6 = this.worldObj.getBiomeGenForCoords(var4 + 16, var5 + 16);
        boolean var7 = false;
        this.random.setSeed(this.worldObj.getSeed());
        long var8 = this.random.nextLong() / 2L * 2L + 1L;
        long var10 = this.random.nextLong() / 2L * 2L + 1L;
        this.random.setSeed((long) par2 * var8 + (long) par3 * var10 ^ this.worldObj.getSeed());

        for (MapGenStructure structureGenerator : this.structureGenerators) {
            boolean var14 = structureGenerator.generateStructuresInChunk(this.worldObj, this.random, par2, par3);

            if (structureGenerator instanceof MapGenVillage) {
                var7 |= var14;
            }
        }

        int var17;
        int var16;
        int var18;

        if (this.waterLakeGenerator != null && !var7 && this.random.nextInt(4) == 0) {
            var16 = var4 + this.random.nextInt(16) + 8;
            var17 = this.random.nextInt(128);
            var18 = var5 + this.random.nextInt(16) + 8;
            this.waterLakeGenerator.generate(this.worldObj, this.random, var16, var17, var18);
        }

        if (this.lavaLakeGenerator != null && !var7 && this.random.nextInt(8) == 0) {
            var16 = var4 + this.random.nextInt(16) + 8;
            var17 = this.random.nextInt(this.random.nextInt(120) + 8);
            var18 = var5 + this.random.nextInt(16) + 8;

            if (var17 < 63 || this.random.nextInt(10) == 0) {
                this.lavaLakeGenerator.generate(this.worldObj, this.random, var16, var17, var18);
            }
        }

        if (this.field_82702_h) {
            for (var16 = 0; var16 < 8; ++var16) {
                var17 = var4 + this.random.nextInt(16) + 8;
                var18 = this.random.nextInt(128);
                int var15 = var5 + this.random.nextInt(16) + 8;
                (new WorldGenDungeons()).generate(this.worldObj, this.random, var17, var18, var15);
            }
        }

        if (this.field_82697_g) {
            var6.decorate(this.worldObj, this.random, var4, var5);
        }
    }

    /**
     * Two modes of operation: if passed true, save all Chunks in one go.  If passed false, save up to two chunks.
     * Return true if all chunks have been saved.
     */
    @Override
    public boolean saveChunks(boolean par1, IProgressUpdate par2IProgressUpdate) {
        return true;
    }

    @Override
    public void func_104112_b() {
    }

    /**
     * Unloads chunks that are marked to be unloaded. This is not guaranteed to unload every such chunk.
     */
    @Override
    public boolean unloadQueuedChunks() {
        return false;
    }

    /**
     * Returns if the IChunkProvider supports saving.
     */
    @Override
    public boolean canSave() {
        return true;
    }

    /**
     * Converts the instance data to a readable string.
     */
    @Override
    public String makeString() {
        return "FlatLevelSource";
    }

    /**
     * Returns a list of creatures of the specified type that can spawn at the given location.
     */
    @Override
    public List getPossibleCreatures(EnumCreatureType par1EnumCreatureType, int par2, int par3, int par4) {
        BiomeGenBase var5 = this.worldObj.getBiomeGenForCoords(par2, par4);
        return var5 == null ? null : var5.getSpawnableList(par1EnumCreatureType);
    }

    /**
     * Returns the location of the closest structure of the specified type. If not found returns null.
     */
    @Override
    public ChunkPosition findClosestStructure(World par1World, String par2Str, int par3, int par4, int par5) {
        if ("Stronghold".equals(par2Str)) {

            for (MapGenStructure structureGenerator : this.structureGenerators) {

                if (structureGenerator instanceof MapGenStronghold) {
                    return structureGenerator.getNearestInstance(par1World, par3, par4, par5);
                }
            }
        }

        return null;
    }

    @Override
    public int getLoadedChunkCount() {
        return 0;
    }

    @Override
    public void recreateStructures(int par1, int par2) {

        for (MapGenStructure structureGenerator : this.structureGenerators) {
            structureGenerator.generate(this, this.worldObj, par1, par2, null);
        }
    }
}
