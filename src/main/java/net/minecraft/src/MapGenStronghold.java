package net.minecraft.src;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

public class MapGenStronghold extends MapGenStructure
{
    private BiomeGenBase[] allowedBiomeGenBases;

    /**
     * is spawned false and set true once the defined BiomeGenBases were compared with the present ones
     */
    private boolean ranBiomeCheck;
    private ChunkCoordIntPair[] structureCoords;
    private double field_82671_h;
    private int field_82672_i;

    public MapGenStronghold()
    {
        this.allowedBiomeGenBases = new BiomeGenBase[] {BiomeGenBase.desert, BiomeGenBase.forest, BiomeGenBase.extremeHills, BiomeGenBase.swampland, BiomeGenBase.taiga, BiomeGenBase.icePlains, BiomeGenBase.iceMountains, BiomeGenBase.desertHills, BiomeGenBase.forestHills, BiomeGenBase.extremeHillsEdge, BiomeGenBase.jungle, BiomeGenBase.jungleHills};
        this.structureCoords = new ChunkCoordIntPair[3];
        this.field_82671_h = 32.0D;
        this.field_82672_i = 3;
    }

    public MapGenStronghold(Map par1Map)
    {
        this.allowedBiomeGenBases = new BiomeGenBase[] {BiomeGenBase.desert, BiomeGenBase.forest, BiomeGenBase.extremeHills, BiomeGenBase.swampland, BiomeGenBase.taiga, BiomeGenBase.icePlains, BiomeGenBase.iceMountains, BiomeGenBase.desertHills, BiomeGenBase.forestHills, BiomeGenBase.extremeHillsEdge, BiomeGenBase.jungle, BiomeGenBase.jungleHills};
        this.structureCoords = new ChunkCoordIntPair[3];
        this.field_82671_h = 32.0D;
        this.field_82672_i = 3;

        for (Object o : par1Map.entrySet()) {
            Entry var3 = (Entry) o;

            if (var3.getKey().equals("distance")) {
                this.field_82671_h = MathHelper.func_82713_a((String) var3.getValue(), this.field_82671_h, 1.0D);
            } else if (var3.getKey().equals("count")) {
                this.structureCoords = new ChunkCoordIntPair[MathHelper.parseIntWithDefaultAndMax((String) var3.getValue(), this.structureCoords.length, 1)];
            } else if (var3.getKey().equals("spread")) {
                this.field_82672_i = MathHelper.parseIntWithDefaultAndMax((String) var3.getValue(), this.field_82672_i, 1);
            }
        }
    }

    protected boolean canSpawnStructureAtCoords(int par1, int par2)
    {
        if (!this.ranBiomeCheck)
        {
            Random var3 = new Random();
            var3.setSeed(this.worldObj.getSeed());
            double var4 = var3.nextDouble() * Math.PI * 2.0D;
            int var6 = 1;

            for (int var7 = 0; var7 < this.structureCoords.length; ++var7)
            {
                double var8 = (1.25D * (double)var6 + var3.nextDouble()) * this.field_82671_h * (double)var6;
                int var10 = (int)Math.round(Math.cos(var4) * var8);
                int var11 = (int)Math.round(Math.sin(var4) * var8);
                ArrayList<BiomeGenBase> var12 = new ArrayList<BiomeGenBase>();
                Collections.addAll(var12, this.allowedBiomeGenBases);
                ChunkPosition var13 = this.worldObj.getWorldChunkManager().findBiomePosition((var10 << 4) + 8, (var11 << 4) + 8, 112, var12, var3);

                if (var13 != null)
                {
                    var10 = var13.x >> 4;
                    var11 = var13.z >> 4;
                }

                this.structureCoords[var7] = new ChunkCoordIntPair(var10, var11);
                var4 += (Math.PI * 2D) * (double)var6 / (double)this.field_82672_i;

                if (var7 == this.field_82672_i)
                {
                    var6 += 2 + var3.nextInt(5);
                    this.field_82672_i += 1 + var3.nextInt(2);
                }
            }

            this.ranBiomeCheck = true;
        }

        ChunkCoordIntPair[] var14 = this.structureCoords;

        for (ChunkCoordIntPair var16 : var14) {
            if (par1 == var16.chunkXPos && par2 == var16.chunkZPos) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns a list of other locations at which the structure generation has been run, or null if not relevant to this
     * structure generator.
     */
    protected List getCoordList()
    {
        ArrayList<ChunkPosition> var1 = new ArrayList<ChunkPosition>();
        ChunkCoordIntPair[] var2 = this.structureCoords;

        for (ChunkCoordIntPair var5 : var2) {
            if (var5 != null) {
                var1.add(var5.getChunkPosition(64));
            }
        }

        return var1;
    }

    protected StructureStart getStructureStart(int par1, int par2)
    {
        StructureStrongholdStart var3;

        //noinspection StatementWithEmptyBody
        for (var3 = new StructureStrongholdStart(this.worldObj, this.rand, par1, par2); var3.getComponents().isEmpty() || ((ComponentStrongholdStairs2)var3.getComponents().get(0)).strongholdPortalRoom == null; var3 = new StructureStrongholdStart(this.worldObj, this.rand, par1, par2))
        {

        }

        return var3;
    }
}
