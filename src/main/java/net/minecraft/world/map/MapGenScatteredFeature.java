package net.minecraft.world.map;

import net.minecraft.entity.EntityWitch;
import net.minecraft.src.SpawnListEntry;
import net.minecraft.utils.MathHelper;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.structure.StructureScatteredFeatureStart;
import net.minecraft.world.structure.StructureStart;

import java.util.*;
import java.util.Map.Entry;

public class MapGenScatteredFeature extends MapGenStructure {
    private static List<BiomeGenBase> biomelist = Arrays.asList(BiomeGenBase.desert, BiomeGenBase.desertHills, BiomeGenBase.jungle, BiomeGenBase.jungleHills, BiomeGenBase.swampland);

    /**
     * contains possible spawns for scattered features
     */
    private List<SpawnListEntry> scatteredFeatureSpawnList;

    /**
     * the maximum distance between scattered features
     */
    private int maxDistanceBetweenScatteredFeatures;

    /**
     * the minimum distance between scattered features
     */
    private int minDistanceBetweenScatteredFeatures;

    public MapGenScatteredFeature() {
        this.scatteredFeatureSpawnList = new ArrayList<SpawnListEntry>();
        this.maxDistanceBetweenScatteredFeatures = 32;
        this.minDistanceBetweenScatteredFeatures = 8;
        this.scatteredFeatureSpawnList.add(new SpawnListEntry(EntityWitch.class, 1, 1, 1));
    }

    public MapGenScatteredFeature(Map par1Map) {
        this();

        for (Object o : par1Map.entrySet()) {
            Entry var3 = (Entry) o;

            if ((var3.getKey()).equals("distance")) {
                this.maxDistanceBetweenScatteredFeatures = MathHelper.parseIntWithDefaultAndMax((String) var3.getValue(), this.maxDistanceBetweenScatteredFeatures, this.minDistanceBetweenScatteredFeatures + 1);
            }
        }
    }

    @Override
    public boolean canSpawnStructureAtCoords(int par1, int par2) {
        int var3 = par1;
        int var4 = par2;

        if (par1 < 0) {
            par1 -= this.maxDistanceBetweenScatteredFeatures - 1;
        }

        if (par2 < 0) {
            par2 -= this.maxDistanceBetweenScatteredFeatures - 1;
        }

        int var5 = par1 / this.maxDistanceBetweenScatteredFeatures;
        int var6 = par2 / this.maxDistanceBetweenScatteredFeatures;
        Random var7 = this.worldObj.setRandomSeed(var5, var6, 14357617);
        var5 *= this.maxDistanceBetweenScatteredFeatures;
        var6 *= this.maxDistanceBetweenScatteredFeatures;
        var5 += var7.nextInt(this.maxDistanceBetweenScatteredFeatures - this.minDistanceBetweenScatteredFeatures);
        var6 += var7.nextInt(this.maxDistanceBetweenScatteredFeatures - this.minDistanceBetweenScatteredFeatures);

        if (var3 == var5 && var4 == var6) {
            BiomeGenBase var8 = this.worldObj.getWorldChunkManager().getBiomeGenAt(var3 * 16 + 8, var4 * 16 + 8);

            for (BiomeGenBase var10 : biomelist) {
                if (var8 == var10) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public StructureStart getStructureStart(int par1, int par2) {
        return new StructureScatteredFeatureStart(this.worldObj, this.rand, par1, par2);
    }

    /**
     * returns possible spawns for scattered features
     */
    public List getScatteredFeatureSpawnList() {
        return this.scatteredFeatureSpawnList;
    }
}
