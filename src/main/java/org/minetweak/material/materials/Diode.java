package org.minetweak.material.materials;

import org.minetweak.material.Material;
import org.minetweak.event.block.BlockFace;

public class Diode extends MaterialData implements Directional {
    public Diode() {
        super(Material.DIODE_BLOCK_ON);
    }

    public Diode(int type) {
        super(type);
    }

    public Diode(Material type) {
        super(type);
    }

    public Diode(int type, byte data) {
        super(type, data);
    }

    public Diode(Material type, byte data) {
        super(type, data);
    }

    /**
     * Sets the delay of the repeater
     *
     * @param delay
     *            The new delay (1-4)
     */
    public void setDelay(int delay) {
        if (delay > 4) {
            delay = 4;
        }
        if (delay < 1) {
            delay = 1;
        }
        byte newData = (byte) (getData() & 0x3);

        setData((byte) (newData | ((delay - 1) << 2)));
    }

    /**
     * Gets the delay of the repeater in ticks
     *
     * @return The delay (1-4)
     */
    public int getDelay() {
        return (getData() >> 2) + 1;
    }

    public void setFacingDirection(BlockFace face) {
        int delay = getDelay();
        byte data;

        switch (face) {
        case EAST:
            data = 0x1;
            break;

        case SOUTH:
            data = 0x2;
            break;

        case WEST:
            data = 0x3;
            break;

        case NORTH:
        default:
            data = 0x0;
        }

        setData(data);
        setDelay(delay);
    }

    public BlockFace getFacing() {
        byte data = (byte) (getData() & 0x3);

        switch (data) {
        case 0x0:
        default:
            return BlockFace.NORTH;

        case 0x1:
            return BlockFace.EAST;

        case 0x2:
            return BlockFace.SOUTH;

        case 0x3:
            return BlockFace.WEST;
        }
    }

    @Override
    public String toString() {
        return super.toString() + " facing " + getFacing() + " with " + getDelay() + " ticks delay";
    }

    @Override
    public Diode clone() {
        return (Diode) super.clone();
    }
}
