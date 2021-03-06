package net.minecraft.utils.callable;

import net.minecraft.entity.EntityTracker;

import java.util.concurrent.Callable;

public class CallableEntityTracker implements Callable<String> {
    final int field_96570_a;

    final EntityTracker theEntityTracker;

    public CallableEntityTracker(EntityTracker par1EntityTracker, int par2) {
        this.theEntityTracker = par1EntityTracker;
        this.field_96570_a = par2;
    }

    public String func_96568_a() {
        String var1 = "Once per " + this.field_96570_a + " ticks";

        if (this.field_96570_a == Integer.MAX_VALUE) {
            var1 = "Maximum (" + var1 + ")";
        }

        return var1;
    }

    @Override
    public String call() {
        return this.func_96568_a();
    }
}
