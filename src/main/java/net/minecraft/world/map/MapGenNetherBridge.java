package net.minecraft.world.map;

import net.minecraft.entity.EntityBlaze;
import net.minecraft.entity.EntityMagmaCube;
import net.minecraft.entity.EntityPigZombie;
import net.minecraft.entity.EntitySkeleton;
import net.minecraft.src.SpawnListEntry;
import net.minecraft.world.structure.StructureNetherBridgeStart;
import net.minecraft.world.structure.StructureStart;

import java.util.ArrayList;
import java.util.List;

public class MapGenNetherBridge extends MapGenStructure {
    private List<SpawnListEntry> spawnList = new ArrayList<SpawnListEntry>();

    public MapGenNetherBridge() {
        this.spawnList.add(new SpawnListEntry(EntityBlaze.class, 10, 2, 3));
        this.spawnList.add(new SpawnListEntry(EntityPigZombie.class, 5, 4, 4));
        this.spawnList.add(new SpawnListEntry(EntitySkeleton.class, 10, 4, 4));
        this.spawnList.add(new SpawnListEntry(EntityMagmaCube.class, 3, 4, 4));
    }

    public List getSpawnList() {
        return this.spawnList;
    }

    public boolean canSpawnStructureAtCoords(int par1, int par2) {
        int var3 = par1 >> 4;
        int var4 = par2 >> 4;
        this.rand.setSeed((long) (var3 ^ var4 << 4) ^ this.worldObj.getSeed());
        this.rand.nextInt();
        return this.rand.nextInt(3) == 0 && (par1 == (var3 << 4) + 4 + this.rand.nextInt(8) && par2 == (var4 << 4) + 4 + this.rand.nextInt(8));
    }

    public StructureStart getStructureStart(int par1, int par2) {
        return new StructureNetherBridgeStart(this.worldObj, this.rand, par1, par2);
    }
}
