package dev.dubhe.anvilcraft.data.lang;

import dev.dubhe.anvilcraft.api.tooltip.ItemTooltipManager;

import com.tterrag.registrate.providers.RegistrateLangProvider;
import org.jetbrains.annotations.NotNull;

public class ItemTooltipLang {
    /**
     * 初始化物品 tooltip 语言
     *
     * @param provider 提供器
     */
    public static void init(@NotNull RegistrateLangProvider provider) {
        ItemTooltipManager.NEED_TOOLTIP_ITEM.forEach(
                (item, s) -> provider.add(ItemTooltipManager.getTranslationKey(item), s));
        provider.add("tooltip.anvilcraft.item.reinforced_concrete", "Creeper proof");
        provider.add("tooltip.anvilcraft.item.recipe.processing.chance", "%1$s%% Chance");

        provider.add("tooltip.anvilcraft.item.structure_tool.min_pos", "Min: x: %d, y: %d, z: %d");
        provider.add("tooltip.anvilcraft.item.structure_tool.max_pos", "Max: x: %d, y: %d, z: %d");
        provider.add("tooltip.anvilcraft.item.structure_tool.size", "Size: x: %d, y: %d, z: %d");
        provider.add("tooltip.anvilcraft.item.structure_tool.data_removed", "Structure Removed");
        provider.add("tooltip.anvilcraft.item.structure_tool.must_cube", "The selected area must be a cube");
        provider.add(
                "tooltip.anvilcraft.item.structure_tool.must_odd",
                "The side length of the selected area must be odd and cannot exceed 15");
        provider.add("tooltip.anvilcraft.item.structure_tool.click_to_copy", "Click to copy");
    }
}