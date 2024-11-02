package dev.dubhe.anvilcraft.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.Unbreakable;

public class EmberAnvilHammerItem extends AnvilHammerItem {
    /**
     * 初始化铁砧锤
     *
     * @param properties 物品属性
     */
    public EmberAnvilHammerItem(Properties properties) {
        super(
            properties.durability(0)
                .component(DataComponents.UNBREAKABLE, new Unbreakable(true))
        );
    }

    @Override
    protected float getAttackDamageModifierAmount() {
        return 9;
    }

    @Override
    protected float calculateFallDamageBonus(float fallDistance) {
        return Math.min(120, fallDistance * 2);
    }
}