package net.minecraft.world.storage;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.src.IProgressUpdate;
import net.minecraft.world.WorldInfo;

import java.io.File;
import java.io.FileInputStream;

public class SaveFormatOld implements ISaveFormat {
    /**
     * Reference to the File object representing the directory for the world saves
     */
    protected final File savesDirectory;

    public SaveFormatOld(File par1File) {
        if (!par1File.exists()) {
            par1File.mkdirs();
        }

        this.savesDirectory = par1File;
    }

    public void flushCache() {
    }

    /**
     * gets the world info
     */
    public WorldInfo getWorldInfo(String par1Str) {
        File var2 = new File(this.savesDirectory, par1Str);

        if (!var2.exists()) {
            return null;
        } else {
            File var3 = new File(var2, "level.dat");
            NBTTagCompound var4;
            NBTTagCompound var5;

            if (var3.exists()) {
                try {
                    var4 = CompressedStreamTools.readCompressed(new FileInputStream(var3));
                    var5 = var4.getCompoundTag("Data");
                    return new WorldInfo(var5);
                } catch (Exception var7) {
                    var7.printStackTrace();
                }
            }

            var3 = new File(var2, "level.dat_old");

            if (var3.exists()) {
                try {
                    var4 = CompressedStreamTools.readCompressed(new FileInputStream(var3));
                    var5 = var4.getCompoundTag("Data");
                    return new WorldInfo(var5);
                } catch (Exception var6) {
                    var6.printStackTrace();
                }
            }

            return null;
        }
    }

    /**
     * Deletes World Directory
     *
     * @param par1Str the name of the directory of the world to delete.
     */
    public boolean deleteWorldDirectory(String par1Str) {
        File var2 = new File(this.savesDirectory, par1Str);

        if (!var2.exists()) {
            return true;
        } else {
            System.out.println("Deleting level " + par1Str);

            for (int var3 = 1; var3 <= 5; ++var3) {
                System.out.println("Attempt " + var3 + "...");

                if (deleteFiles(var2.listFiles())) {
                    break;
                }

                System.out.println("Unsuccessful in deleting contents.");

                if (var3 < 5) {
                    try {
                        Thread.sleep(500L);
                    } catch (InterruptedException ignored) {

                    }
                }
            }

            return var2.delete();
        }
    }

    /**
     * Deletes the files
     *
     * @param par0ArrayOfFile the list of files and directories to delete.
     */
    protected static boolean deleteFiles(File[] par0ArrayOfFile) {
        for (File var2 : par0ArrayOfFile) {
            System.out.println("Deleting " + var2);

            if (var2.isDirectory() && !deleteFiles(var2.listFiles())) {
                System.out.println("Couldn\'t delete directory " + var2);
                return false;
            }

            if (!var2.delete()) {
                System.out.println("Couldn\'t delete file " + var2);
                return false;
            }
        }

        return true;
    }

    /**
     * Returns back a loader for the specified save directory
     */
    public ISaveHandler getSaveLoader(String par1Str, boolean par2) {
        return new SaveHandler(this.savesDirectory, par1Str, par2);
    }

    /**
     * gets if the map is old chunk saving (true) or McRegion (false)
     */
    public boolean isOldMapFormat(String par1Str) {
        return false;
    }

    /**
     * converts the map to mcRegion
     */
    public boolean convertMapFormat(String par1Str, IProgressUpdate par2IProgressUpdate) {
        return false;
    }
}