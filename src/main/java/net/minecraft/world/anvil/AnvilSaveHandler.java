package net.minecraft.world.anvil;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.src.ThreadedFileIOBase;
import net.minecraft.world.WorldInfo;
import net.minecraft.world.chunk.IChunkLoader;
import net.minecraft.world.provider.WorldProvider;
import net.minecraft.world.provider.WorldProviderEnd;
import net.minecraft.world.provider.WorldProviderHell;
import net.minecraft.world.region.RegionFileCache;
import net.minecraft.world.storage.SaveHandler;

import java.io.File;

public class AnvilSaveHandler extends SaveHandler {
    public AnvilSaveHandler(File par1File, String par2Str, boolean par3) {
        super(par1File, par2Str, par3);
    }

    /**
     * initializes and returns the chunk loader for the specified world provider
     */
    @Override
    public IChunkLoader getChunkLoader(WorldProvider par1WorldProvider) {
        File var2 = this.getWorldDirectory();
        File var3;

        if (par1WorldProvider instanceof WorldProviderHell) {
            var3 = new File(var2, "DIM-1");
            var3.mkdirs();
            return new AnvilChunkLoader(var3);
        } else if (par1WorldProvider instanceof WorldProviderEnd) {
            var3 = new File(var2, "DIM1");
            var3.mkdirs();
            return new AnvilChunkLoader(var3);
        } else {
            return new AnvilChunkLoader(var2);
        }
    }

    /**
     * Saves the given World Info with the given NBTTagCompound as the Player.
     */
    @Override
    public void saveWorldInfoWithPlayer(WorldInfo par1WorldInfo, NBTTagCompound par2NBTTagCompound) {
        par1WorldInfo.setSaveVersion(19133);
        super.saveWorldInfoWithPlayer(par1WorldInfo, par2NBTTagCompound);
    }

    /**
     * Called to flush all changes to disk, waiting for them to complete.
     */
    @Override
    public void flush() {
        try {
            ThreadedFileIOBase.threadedIOInstance.waitForFinish();
        } catch (InterruptedException var2) {
            var2.printStackTrace();
        }

        RegionFileCache.clearRegionFileReferences();
    }
}
