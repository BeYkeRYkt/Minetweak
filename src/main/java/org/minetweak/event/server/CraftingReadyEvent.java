package org.minetweak.event.server;

import net.minecraft.src.CraftingManager;

public class CraftingReadyEvent {
    private CraftingManager craftingManager;
    public CraftingReadyEvent(CraftingManager craftingManager) {
        this.craftingManager = craftingManager;
    }

    public CraftingManager getCraftingManager() {
        return craftingManager;
    }
}
