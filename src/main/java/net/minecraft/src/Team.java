package net.minecraft.src;

public abstract class Team {
    public boolean func_142054_a(Team par1Team) {
        return par1Team == null ? false : this == par1Team;
    }

    public abstract String func_96661_b();

    public abstract String func_142053_d(String var1);

    public abstract boolean func_96665_g();
}
