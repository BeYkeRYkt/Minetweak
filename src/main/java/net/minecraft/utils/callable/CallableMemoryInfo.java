package net.minecraft.utils.callable;

import net.minecraft.crash.CrashReport;

import java.util.concurrent.Callable;

public class CallableMemoryInfo implements Callable<String> {
    /**
     * Reference to the CrashReport object.
     */
    final CrashReport theCrashReport;

    public CallableMemoryInfo(CrashReport par1CrashReport) {
        this.theCrashReport = par1CrashReport;
    }

    /**
     * Returns the memory information as a String.  Includes the Free Memory in bytes and MB, Total Memory in bytes and
     * MB, and Max Memory in Bytes and MB.
     */
    public String getMemoryInfoAsString() {
        Runtime var1 = Runtime.getRuntime();
        long var2 = var1.maxMemory();
        long var4 = var1.totalMemory();
        long var6 = var1.freeMemory();
        long var8 = var2 / 1024L / 1024L;
        long var10 = var4 / 1024L / 1024L;
        long var12 = var6 / 1024L / 1024L;
        return var6 + " bytes (" + var12 + " MB) / " + var4 + " bytes (" + var10 + " MB) up to " + var2 + " bytes (" + var8 + " MB)";
    }

    @Override
    public String call() {
        return this.getMemoryInfoAsString();
    }
}
