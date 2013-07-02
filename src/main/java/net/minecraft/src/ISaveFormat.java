package net.minecraft.src;

public interface ISaveFormat
{
    /**
     * Returns back a loader for the specified save directory
     */
    ISaveHandler getSaveLoader(String var1, boolean var2);

    void flushCache();

    /*
     * delete the associated directory recursively.
     * @param var1 name of directory to delete
     */
    boolean deleteWorldDirectory(String var1);

    /**
     * gets if the map is old chunk saving (true) or McRegion (false)
     */
    boolean isOldMapFormat(String var1);

    /**
     * converts the map to mcRegion
     */
    boolean convertMapFormat(String var1, IProgressUpdate var2);
}
