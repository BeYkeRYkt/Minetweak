package net.minecraft.utils.callable;

import java.util.concurrent.Callable;

public final class CallableBlockDataValue implements Callable<String> {
    final int field_85063_a;

    public CallableBlockDataValue(int par1) {
        this.field_85063_a = par1;
    }

    public String callBlockDataValue() {
        if (this.field_85063_a < 0) {
            return "Unknown? (Got " + this.field_85063_a + ")";
        } else {
            String var1 = String.format("%4s", new Object[]{Integer.toBinaryString(this.field_85063_a)}).replace(" ", "0");
            return String.format("%1$d / 0x%1$X / 0b%2$s", this.field_85063_a, var1);
        }
    }

    @Override
    public String call() {
        return this.callBlockDataValue();
    }
}
