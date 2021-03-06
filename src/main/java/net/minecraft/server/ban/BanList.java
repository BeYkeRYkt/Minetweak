package net.minecraft.server.ban;

import net.minecraft.server.MinecraftServer;
import org.minetweak.Minetweak;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

public class BanList {
    private final HashMap<String, BanEntry> theBanList = new HashMap<String, BanEntry>();
    private final File fileName;

    /**
     * set to true if not singlePlayer
     */
    private boolean listActive = true;

    public BanList(File par1File) {
        this.fileName = par1File;
    }

    public boolean isListActive() {
        return this.listActive;
    }

    public void setListActive(boolean par1) {
        this.listActive = par1;
    }

    /**
     * removes expired Bans before returning
     */
    public HashMap<String, BanEntry> getBannedList() {
        this.removeExpiredBans();
        return this.theBanList;
    }

    public boolean isBanned(String par1Str) {
        if (!this.isListActive()) {
            return false;
        } else {
            this.removeExpiredBans();
            return this.theBanList.containsKey(par1Str);
        }
    }

    public void put(BanEntry par1BanEntry) {
        this.theBanList.put(par1BanEntry.getBannedUsername(), par1BanEntry);
        this.saveToFileWithHeader();
    }

    public void remove(String par1Str) {
        this.theBanList.remove(par1Str);
        this.saveToFileWithHeader();
    }

    public void removeExpiredBans() {
        Iterator var1 = this.theBanList.values().iterator();

        while (var1.hasNext()) {
            BanEntry var2 = (BanEntry) var1.next();

            if (var2.hasBanExpired()) {
                var1.remove();
            }
        }
    }

    /**
     * Loads the ban list from the file (adds every entry, does not clear the current list).
     */
    public void loadBanList() {
        if (this.fileName.isFile()) {
            BufferedReader var1;

            try {
                var1 = new BufferedReader(new FileReader(this.fileName));
            } catch (FileNotFoundException var4) {
                throw new Error();
            }

            String var2;

            try {
                while ((var2 = var1.readLine()) != null) {
                    if (!var2.startsWith("#")) {
                        BanEntry var3 = BanEntry.parse(var2);

                        if (var3 != null) {
                            this.theBanList.put(var3.getBannedUsername(), var3);
                        }
                    }
                }
            } catch (IOException var5) {
                MinecraftServer.getServer().getLogAgent().logSevereException("Could not load ban list", var5);
            }
        }
    }

    public void saveToFileWithHeader() {
        this.saveToFile(true);
    }

    /**
     * par1: include header
     */
    public void saveToFile(boolean par1) {
        this.removeExpiredBans();

        try {
            PrintWriter var2 = new PrintWriter(new FileWriter(this.fileName, false));

            if (par1) {
                var2.println("# Updated " + (new SimpleDateFormat()).format(new Date()) + " by Minecraft " + Minetweak.getMinecraftVersion());
                var2.println("# victim name | ban date | banned by | banned until | reason");
                var2.println();
            }

            for (Object o : this.theBanList.values()) {
                BanEntry var4 = (BanEntry) o;
                var2.println(var4.buildBanString());
            }

            var2.close();
        } catch (IOException var5) {
            MinecraftServer.getServer().getLogAgent().logSevereException("Could not save ban list", var5);
        }
    }
}
