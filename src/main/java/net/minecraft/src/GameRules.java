package net.minecraft.src;

import java.util.Collection;
import java.util.Set;
import java.util.TreeMap;

public class GameRules
{
    private TreeMap<String, GameRuleValue> theGameRules = new TreeMap<String, GameRuleValue>();

    public GameRules()
    {
        this.addGameRule("doFireTick", "true");
        this.addGameRule("mobGriefing", "true");
        this.addGameRule("keepInventory", "false");
        this.addGameRule("doMobSpawning", "true");
        this.addGameRule("doMobLoot", "true");
        this.addGameRule("doTileDrops", "true");
        this.addGameRule("commandBlockOutput", "true");
    }

    /**
     * Define a game rule and its default value.
     */
    public void addGameRule(String par1Str, String par2Str)
    {
        this.theGameRules.put(par1Str, new GameRuleValue(par2Str));
    }

    public void setOrCreateGameRule(String par1Str, String par2Str)
    {
        GameRuleValue var3 = this.theGameRules.get(par1Str);

        if (var3 != null)
        {
            var3.setValue(par2Str);
        }
        else
        {
            this.addGameRule(par1Str, par2Str);
        }
    }

    /**
     * Gets the string Game Rule value.
     */
    public String getGameRuleStringValue(String par1Str)
    {
        GameRuleValue var2 = this.theGameRules.get(par1Str);
        return var2 != null ? var2.getGameRuleStringValue() : "";
    }

    /**
     * Gets the boolean Game Rule value.
     */
    public boolean getGameRuleBooleanValue(String par1Str)
    {
        GameRuleValue var2 = this.theGameRules.get(par1Str);
        return var2 != null && var2.getGameRuleBooleanValue();
    }

    /**
     * Return the defined game rules as NBT.
     */
    public NBTTagCompound writeGameRulesToNBT()
    {
        NBTTagCompound var1 = new NBTTagCompound("GameRules");

        for (String var3 : this.theGameRules.keySet()) {
            GameRuleValue var4 = this.theGameRules.get(var3);
            var1.setString(var3, var4.getGameRuleStringValue());
        }

        return var1;
    }

    /**
     * Set defined game rules from NBT.
     */
    public void readGameRulesFromNBT(NBTTagCompound par1NBTTagCompound)
    {
        Collection var2 = par1NBTTagCompound.getTags();

        for (Object aVar2 : var2) {
            NBTBase var4 = (NBTBase) aVar2;
            String var5 = var4.getName();
            String var6 = par1NBTTagCompound.getString(var4.getName());
            this.setOrCreateGameRule(var5, var6);
        }
    }

    /**
     * Return the defined game rules.
     */
    public String[] getRules()
    {
        Set<String> rules = this.theGameRules.keySet();
        return rules.toArray(new String[rules.size()]);
    }

    /**
     * Return whether the specified game rule is defined.
     */
    public boolean hasRule(String par1Str)
    {
        return this.theGameRules.containsKey(par1Str);
    }
}
