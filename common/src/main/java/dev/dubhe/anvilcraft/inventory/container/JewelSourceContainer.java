package dev.dubhe.anvilcraft.inventory.container;

import dev.dubhe.anvilcraft.data.recipe.jewel.JewelCraftingRecipe;
import dev.dubhe.anvilcraft.inventory.JewelCraftingMenu;
import dev.dubhe.anvilcraft.util.RecipeCaches;
import lombok.Getter;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class JewelSourceContainer extends SimpleContainer {
    @Getter
    private @Nullable JewelCraftingRecipe recipe;
    private final JewelCraftingMenu menu;

    public JewelSourceContainer(JewelCraftingMenu menu) {
        super(1);
        this.menu = menu;
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        super.setItem(index, stack);
        recipe = RecipeCaches.getJewelRecipeByResult(stack);
        this.menu.slotsChanged(this);
    }
}
