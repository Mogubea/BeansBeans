package me.playground.items.values;

import me.playground.enchants.BEnchantment;
import me.playground.playerprofile.stats.DirtyFloat;
import org.bukkit.block.ShulkerBox;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

/**
 * Temporary class until dedicated values are set for enchantments and potion effects.
 */
@Deprecated(forRemoval = true)
public class ItemValueTemp {

    private final ItemValueManager manager;

    protected ItemValueTemp(ItemValueManager manager) {
        this.manager = manager;
    }

    private float getValueOfEnchantments(ItemStack itemStack) {
        DirtyFloat value = new DirtyFloat(0F);

        itemStack.getEnchantments().forEach(((enchantment, level) -> {
            BEnchantment enchant = BEnchantment.from(enchantment);
            value.addToValue(enchant.getExperienceCost(level) * 25 + enchant.getRunicValue(level) * 10);
        }));

        return value.getValue();
    }

    private float getValueOfEffects(ItemStack itemStack) {
        DirtyFloat value = new DirtyFloat(0F);

        if (itemStack.getItemMeta() instanceof PotionMeta meta) {
            PotionType type = meta.getBasePotionData().getType();
            switch(type) {
                case WATER, UNCRAFTABLE, MUNDANE, AWKWARD, THICK -> value.addToValue(0.2F);
                default -> value.addToValue(5);
            }
            value.addToValue(meta.getCustomEffects().size() * 5);
        }

        return value.getValue();
    }

    private float getInventoryValue(ItemStack itemStack) {
        float value = 0F;

        if (!itemStack.getType().name().endsWith("SHULKER_BOX")) return value;
        if (!(itemStack.getItemMeta() instanceof BlockStateMeta meta)) return value;
        if (!(meta.getBlockState() instanceof ShulkerBox shulker)) return value;

        for (ItemStack item : shulker.getInventory().getContents()) {
            if (item == null) continue;
            value += getExtraValue(item);
        }

        return value;
    }

    protected float getExtraValue(ItemStack itemStack) {
        float value = 0;
        if (itemStack.hasItemMeta()) {
            value += getValueOfEffects(itemStack);
            value += getValueOfEnchantments(itemStack);
            value += getInventoryValue(itemStack);
        }

        return value;
    }

}
