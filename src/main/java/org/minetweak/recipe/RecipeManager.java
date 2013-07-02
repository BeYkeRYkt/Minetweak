package org.minetweak.recipe;

import com.google.common.eventbus.Subscribe;
import net.minecraft.src.CraftingManager;
import net.minecraft.src.ItemStack;
import org.minetweak.event.server.CraftingReadyEvent;

public class RecipeManager {
    private static final RecipeManager instance = new RecipeManager();
    private CraftingManager craftingManager;

    public static RecipeManager getInstance() {
        return instance;
    }

    public void addRecipe(ItemStack output, Object... data) {
        craftingManager.addRecipe(output, data);
    }

    public void addShapelessRecipe(ItemStack output, Object... inputs) {
        craftingManager.addShapelessRecipe(output, inputs);
    }

    @Subscribe
    public void craftingReadyCallback(CraftingReadyEvent event) {
        this.craftingManager = event.getCraftingManager();
    }
}
