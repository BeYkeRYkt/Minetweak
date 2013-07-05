package net.minecraft.src;

import java.util.concurrent.Callable;

class CallableLevelGenerator implements Callable<String> {
    final WorldInfo worldInfoInstance;

    CallableLevelGenerator(WorldInfo par1WorldInfo) {
        this.worldInfoInstance = par1WorldInfo;
    }

    public String callLevelGeneratorInfo() {
        return String.format("ID %02d - %s, ver %d. Features enabled: %b", WorldInfo.getTerrainTypeOfWorld(this.worldInfoInstance).getWorldTypeID(), WorldInfo.getTerrainTypeOfWorld(this.worldInfoInstance).getWorldTypeName(), WorldInfo.getTerrainTypeOfWorld(this.worldInfoInstance).getGeneratorVersion(), WorldInfo.getMapFeaturesEnabled(this.worldInfoInstance));
    }

    @Override
    public String call() {
        return this.callLevelGeneratorInfo();
    }
}
