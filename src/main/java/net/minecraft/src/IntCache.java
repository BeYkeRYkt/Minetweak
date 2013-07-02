package net.minecraft.src;

import java.util.ArrayList;
import java.util.List;

public class IntCache
{
    private static int intCacheSize = 256;

    /**
     * A list of pre-allocated int[256] arrays that are currently unused and can be returned by getIntCache()
     */
    private static List<int[]> freeSmallArrays = new ArrayList<int[]>();

    /**
     * A list of pre-allocated int[256] arrays that were previously returned by getIntCache() and which will not be re-
     * used again until resetIntCache() is called.
     */
    private static List<int[]> inUseSmallArrays = new ArrayList<int[]>();

    /**
     * A list of pre-allocated int[cacheSize] arrays that are currently unused and can be returned by getIntCache()
     */
    private static List<int[]> freeLargeArrays = new ArrayList<int[]>();

    /**
     * A list of pre-allocated int[cacheSize] arrays that were previously returned by getIntCache() and which will not
     * be re-used again until resetIntCache() is called.
     */
    private static List<int[]> inUseLargeArrays = new ArrayList<int[]>();

    public static synchronized int[] getIntCache(int par0)
    {
        int[] var1;

        if (par0 <= 256)
        {
            if (freeSmallArrays.isEmpty())
            {
                var1 = new int[256];
                inUseSmallArrays.add(var1);
                return var1;
            }
            else
            {
                var1 = freeSmallArrays.remove(freeSmallArrays.size() - 1);
                inUseSmallArrays.add(var1);
                return var1;
            }
        }
        else if (par0 > intCacheSize)
        {
            intCacheSize = par0;
            freeLargeArrays.clear();
            inUseLargeArrays.clear();
            var1 = new int[intCacheSize];
            inUseLargeArrays.add(var1);
            return var1;
        }
        else if (freeLargeArrays.isEmpty())
        {
            var1 = new int[intCacheSize];
            inUseLargeArrays.add(var1);
            return var1;
        }
        else
        {
            var1 = freeLargeArrays.remove(freeLargeArrays.size() - 1);
            inUseLargeArrays.add(var1);
            return var1;
        }
    }

    /**
     * Mark all pre-allocated arrays as available for re-use by moving them to the appropriate free lists.
     */
    public static synchronized void resetIntCache()
    {
        if (!freeLargeArrays.isEmpty())
        {
            freeLargeArrays.remove(freeLargeArrays.size() - 1);
        }

        if (!freeSmallArrays.isEmpty())
        {
            freeSmallArrays.remove(freeSmallArrays.size() - 1);
        }

        freeLargeArrays.addAll(inUseLargeArrays);
        freeSmallArrays.addAll(inUseSmallArrays);
        inUseLargeArrays.clear();
        inUseSmallArrays.clear();
    }

    public static synchronized String func_85144_b()
    {
        return "cache: " + freeLargeArrays.size() + ", tcache: " + freeSmallArrays.size() + ", allocated: " + inUseLargeArrays.size() + ", tallocated: " + inUseSmallArrays.size();
    }
}
