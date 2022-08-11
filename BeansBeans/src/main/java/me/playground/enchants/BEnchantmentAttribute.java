package me.playground.enchants;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class BEnchantmentAttribute {

    private final UUID uuid;
    private final Attribute attribute;
    private final AttributeModifier.Operation operation;
    private final double baseValue;
    private final double perLevel;

    private final Map<Integer, AttributeModifier> cachedModifiers = new LinkedHashMap<>();

    protected BEnchantmentAttribute(BEnchantment enchant, Attribute attribute, AttributeModifier.Operation operation, double baseValue, double perLevel) {
        this.uuid = UUID.nameUUIDFromBytes((enchant.key().asString() + "_" + attribute.translationKey()).getBytes());
        this.attribute = attribute;
        this.operation = operation;
        this.baseValue = baseValue;
        this.perLevel = perLevel;
    }

    protected double getValue(int level) {
        return baseValue + (perLevel * level);
    }

    /**
     * Get the {@link AttributeModifier} for the enchantment level provided.
     * @param level The level of the enchantment
     * @return AttributeModifier.
     */
    public AttributeModifier getModifier(int level) {
        if (cachedModifiers.containsKey(level)) return cachedModifiers.get(level);
        AttributeModifier modifier = new AttributeModifier(uuid, attribute.translationKey(), getValue(level), operation);
        cachedModifiers.put(level, modifier);

        return modifier;
    }

}
