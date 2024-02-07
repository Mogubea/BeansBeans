package me.playground.enchants;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class CustomEnchantTest extends Enchantment {

    protected CustomEnchantTest() {
        super(Rarity.VERY_RARE, EnchantmentCategory.BREAKABLE, EquipmentSlot.values());
    }

    @Override
    public boolean isTradeable() {
        return false;
    }

    @Override
    public boolean isDiscoverable() {
        return false;
    }

    @Override
    public boolean isCurse() {
        return false;
    }

}
