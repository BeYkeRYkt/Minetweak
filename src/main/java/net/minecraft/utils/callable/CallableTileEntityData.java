package net.minecraft.utils.callable;

import net.minecraft.tileentity.TileEntity;

import java.util.concurrent.Callable;

import static java.lang.Integer.toBinaryString;
import static java.lang.Integer.valueOf;

public class CallableTileEntityData implements Callable<String> {
    final TileEntity theTileEntity;

    public CallableTileEntityData(TileEntity par1TileEntity) {
        this.theTileEntity = par1TileEntity;
    }

    public String callTileEntityDataInfo() {
        int var1 = this.theTileEntity.worldObj.getBlockMetadata(this.theTileEntity.xCoord, this.theTileEntity.yCoord, this.theTileEntity.zCoord);

        if (var1 < 0) {
            return "Unknown? (Got " + var1 + ")";
        } else {
            String var2 = String.format("%4s", new Object[]{toBinaryString(var1)}).replace(" ", "0");
            return String.format("%1$d / 0x%1$X / 0b%2$s", valueOf(var1), var2);
        }
    }

    @Override
    public String call() {
        return this.callTileEntityDataInfo();
    }
}
