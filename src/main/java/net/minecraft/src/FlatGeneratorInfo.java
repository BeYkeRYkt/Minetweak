package net.minecraft.src;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@SuppressWarnings("UnusedAssignment")
public class FlatGeneratorInfo
{
    /** List of layers on this preset. */
    private final List<FlatLayerInfo> flatLayers = new ArrayList<FlatLayerInfo>();

    /** List of world features enabled on this preset. */
    private final Map<String, Object> worldFeatures = new HashMap<String, Object>();
    private int biomeToUse = 0;

    /**
     * Return the biome used on this preset.
     */
    public int getBiome()
    {
        return this.biomeToUse;
    }

    /**
     * Set the biome used on this preset.
     */
    public void setBiome(int par1)
    {
        this.biomeToUse = par1;
    }

    /**
     * Return the list of world features enabled on this preset.
     */
    public Map<String, Object> getWorldFeatures()
    {
        return this.worldFeatures;
    }

    /**
     * Return the list of layers on this preset.
     */
    public List<FlatLayerInfo> getFlatLayers()
    {
        return this.flatLayers;
    }

    public void func_82645_d()
    {
        int var1 = 0;
        FlatLayerInfo var3;

        for (Iterator<FlatLayerInfo> var2 = this.flatLayers.iterator(); var2.hasNext(); var1 += var3.getLayerCount())
        {
            var3 = var2.next();
            var3.setMinY(var1);
        }
    }

    public String toString()
    {
        StringBuilder var1 = new StringBuilder();
        var1.append(2);
        var1.append(";");
        int var2;

        for (var2 = 0; var2 < this.flatLayers.size(); ++var2)
        {
            if (var2 > 0)
            {
                var1.append(",");
            }

            var1.append(this.flatLayers.get(var2).toString());
        }

        var1.append(";");
        var1.append(this.biomeToUse);

        if (!this.worldFeatures.isEmpty())
        {
            var1.append(";");
            var2 = 0;

            for (Entry<String, Object> var4 : this.worldFeatures.entrySet()) {
                if (var2++ > 0) {
                    var1.append(",");
                }

                var1.append((var4.getKey()).toLowerCase());
                Map<String, String> var5 = (Map<String, String>) var4.getValue();

                if (!var5.isEmpty()) {
                    var1.append("(");
                    int var6 = 0;

                    for (Entry<String, String> o : var5.entrySet()) {

                        if (var6++ > 0) {
                            var1.append(" ");
                        }

                        var1.append(o.getKey());
                        var1.append("=");
                        var1.append(o.getValue());
                    }

                    var1.append(")");
                }
            }
        }
        else
        {
            var1.append(";");
        }

        return var1.toString();
    }

    private static FlatLayerInfo func_82646_a(String par0Str, int par1)
    {
        String[] var2 = par0Str.split("x", 2);
        int var3 = 1;
        int var5 = 0;

        if (var2.length == 2)
        {
            try
            {
                var3 = Integer.parseInt(var2[0]);

                if (par1 + var3 >= 256)
                {
                    var3 = 256 - par1;
                }

                if (var3 < 0)
                {
                    var3 = 0;
                }
            }
            catch (Throwable var7)
            {
                return null;
            }
        }

        int var4;

        try
        {
            String var6 = var2[var2.length - 1];
            var2 = var6.split(":", 2);
            var4 = Integer.parseInt(var2[0]);

            if (var2.length > 1)
            {
                var5 = Integer.parseInt(var2[1]);
            }

            if (Block.blocksList[var4] == null)
            {
                var4 = 0;
                var5 = 0;
            }

            if (var5 < 0 || var5 > 15)
            {
                var5 = 0;
            }
        }
        catch (Throwable var8)
        {
            return null;
        }

        FlatLayerInfo var9 = new FlatLayerInfo(var3, var4, var5);
        var9.setMinY(par1);
        return var9;
    }

    private static List<FlatLayerInfo> func_82652_b(String par0Str)
    {
        if (par0Str != null && par0Str.length() >= 1)
        {
            ArrayList<FlatLayerInfo> var1 = new ArrayList<FlatLayerInfo>();
            String[] var2 = par0Str.split(",");
            int var3 = 0;

            for (String var7 : var2) {
                FlatLayerInfo var8 = func_82646_a(var7, var3);

                if (var8 == null) {
                    return null;
                }

                var1.add(var8);
                var3 += var8.getLayerCount();
            }

            return var1;
        }
        else
        {
            return null;
        }
    }

    public static FlatGeneratorInfo createFlatGeneratorFromString(String par0Str)
    {
        if (par0Str == null)
        {
            return getDefaultFlatGenerator();
        }
        else
        {
            String[] var1 = par0Str.split(";", -1);
            int var2 = var1.length == 1 ? 0 : MathHelper.parseIntWithDefault(var1[0], 0);

            if (var2 >= 0 && var2 <= 2)
            {
                FlatGeneratorInfo var3 = new FlatGeneratorInfo();
                int var4 = var1.length == 1 ? 0 : 1;
                List<FlatLayerInfo> var5 = func_82652_b(var1[var4++]);

                if (var5 != null && !var5.isEmpty())
                {
                    var3.getFlatLayers().addAll(var5);
                    var3.func_82645_d();
                    int var6 = BiomeGenBase.plains.biomeID;

                    if (var2 > 0 && var1.length > var4)
                    {
                        var6 = MathHelper.parseIntWithDefault(var1[var4++], var6);
                    }

                    var3.setBiome(var6);

                    if (var2 > 0 && var1.length > var4)
                    {
                        String[] var7 = var1[var4++].toLowerCase().split(",");

                        for (String var11 : var7) {
                            String[] var12 = var11.split("\\(", 2);
                            HashMap<String, String> var13 = new HashMap<String, String>();

                            if (var12[0].length() > 0) {
                                var3.getWorldFeatures().put(var12[0], var13);

                                if (var12.length > 1 && var12[1].endsWith(")") && var12[1].length() > 1) {
                                    String[] var14 = var12[1].substring(0, var12[1].length() - 1).split(" ");

                                    for (String aVar14 : var14) {
                                        String[] var16 = aVar14.split("=", 2);

                                        if (var16.length == 2) {
                                            var13.put(var16[0], var16[1]);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    else
                    {
                        var3.getWorldFeatures().put("village", new HashMap<String, String>());
                    }

                    return var3;
                }
                else
                {
                    return getDefaultFlatGenerator();
                }
            }
            else
            {
                return getDefaultFlatGenerator();
            }
        }
    }

    public static FlatGeneratorInfo getDefaultFlatGenerator()
    {
        FlatGeneratorInfo var0 = new FlatGeneratorInfo();
        var0.setBiome(BiomeGenBase.plains.biomeID);
        var0.getFlatLayers().add(new FlatLayerInfo(1, Block.bedrock.blockID));
        var0.getFlatLayers().add(new FlatLayerInfo(2, Block.dirt.blockID));
        var0.getFlatLayers().add(new FlatLayerInfo(1, Block.grass.blockID));
        var0.func_82645_d();
        var0.getWorldFeatures().put("village", new HashMap<String, String>());
        return var0;
    }
}
