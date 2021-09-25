package me.playground.enchants;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.enchantments.Enchantment;

import me.playground.items.ItemRarity;
import net.kyori.adventure.text.Component;

public enum EnchantmentInfo {
	
	ARROW_DAMAGE(Enchantment.ARROW_DAMAGE, 1F, 0.5F),
	ARROW_FIRE(Enchantment.ARROW_FIRE, 2F),
	ARROW_INFINITE(Enchantment.ARROW_INFINITE, 5F),
	ARROW_KNOCKBACK(Enchantment.ARROW_KNOCKBACK, 1F, 1F),
	BINDING_CURSE(Enchantment.BINDING_CURSE, 0F),
	CHANNELING(Enchantment.CHANNELING, 3F),
	DEPTH_STRIDER(Enchantment.DEPTH_STRIDER, 1F, 1F),
	DIG_SPEED(Enchantment.DIG_SPEED, 1F, 0.7F),
	DAMAGE_ALL(Enchantment.DAMAGE_ALL, 1F, 0.7F),
	DAMAGE_ARTHROPODS(Enchantment.DAMAGE_ARTHROPODS, 0.4F, 0.5F),
	DAMAGE_UNDEAD(Enchantment.DAMAGE_UNDEAD, 1F, 0.7F),
	DURABILITY(Enchantment.DURABILITY, 1F, 1F),
	FIRE_ASPECT(Enchantment.FIRE_ASPECT, 1F, 0.5F),
	FROST_WALKER(Enchantment.FROST_WALKER, 2F),
	IMPALING(Enchantment.IMPALING, 1F, 1F),
	KNOCKBACK(Enchantment.KNOCKBACK, 0.25F, 0.5F),
	LOOT_BONUS_MOBS(Enchantment.LOOT_BONUS_MOBS, 1F, 1F),
	LOOT_BONUS_BLOCKS(Enchantment.LOOT_BONUS_BLOCKS, 1F, 1.25F),
	LOYALTY(Enchantment.LOYALTY, 3F, 0.5F),
	LUCK(Enchantment.LUCK, 1F, 1F),
	LURE(Enchantment.LURE, 1F, 1F),
	MENDING(Enchantment.MENDING, 10F),
	MULTISHOT(Enchantment.MULTISHOT, 4F),
	OXYGEN(Enchantment.OXYGEN, 1F, 0.5F),
	PROTECTION_ENVIRONMENTAL(Enchantment.PROTECTION_ENVIRONMENTAL, 0.6F, 0.8F),
	PROTECTION_EXPLOSIONS(Enchantment.PROTECTION_EXPLOSIONS, 0.3F, 0.3F),
	PROTECTION_FIRE(Enchantment.PROTECTION_FIRE, 0.3F, 0.3F),
	PROTECTION_PROJECTILE(Enchantment.PROTECTION_PROJECTILE, 0.4F, 0.4F),
	PROTECTION_FALL(Enchantment.PROTECTION_FALL, 0.4F, 0.4F),
	PIERCING(Enchantment.PIERCING, 0.4F, 0.4F),
	QUICK_CHARGE(Enchantment.QUICK_CHARGE, 1F, 0.5F),
	RIPTIDE(Enchantment.RIPTIDE, 3F, 1F),
	SILK_TOUCH(Enchantment.SILK_TOUCH, 4F),
	SOUL_SPEED(Enchantment.SOUL_SPEED, 1F, 7F),
	SWEEPING_EDGE(Enchantment.SWEEPING_EDGE, 0.5F, 0.7F),
	THORNS(Enchantment.THORNS, 0.3F, 0.4F),
	WATER_WORKER(Enchantment.WATER_WORKER, 3.5F),
	
	MOLTEN_TOUCH(BeanEnchantment.MOLTEN_TOUCH, 10F);
	
	private final Enchantment enchant;
	private final float value; // Base value for rarity calculation
	private final float levelIncr; // Value increase per level for rarity calculation
	
	EnchantmentInfo(Enchantment enchant, float value) {
		this(enchant, value, 0F);
	}
	
	EnchantmentInfo(Enchantment enchant, float value, float levelIncr) {
		this.enchant = enchant;
		this.value = value;
		this.levelIncr = levelIncr;
	}
	
	public static ItemRarity rarityOf(Map<Enchantment, Integer> enchants) {
		float totalValue = 0F;
		
		for (Entry<Enchantment, Integer> ench : enchants.entrySet()) {
			EnchantmentInfo ei = valueOf(ench.getKey());
			totalValue += ei.getBaseValue() + (ei.getPerLevelValue() * (ench.getValue()-1));
		}
		
		if (totalValue >= 30)
			return ItemRarity.LEGENDARY;
		if (totalValue >= 20)
			return ItemRarity.EPIC;
		if (totalValue >= 10)
			return ItemRarity.RARE;
		if (totalValue >= 3)
			return ItemRarity.UNCOMMON;
		return ItemRarity.COMMON;
		
	}
	
	public float getBaseValue() {
		return this.value;
	}
	
	public float getPerLevelValue() {
		return this.levelIncr;
	}
	
	public static EnchantmentInfo valueOf(Enchantment ench) {
		final int size = values().length;
		for (int x = -1; ++x < size;)
			if (values()[x].enchant.equals(ench)) return values()[x];
		return ARROW_DAMAGE; // default generic values
	}
	
	public static List<Component> loreOf(Enchantment ench, int level) {
		if (ench.equals(BeanEnchantment.MOLTEN_TOUCH))
			return Arrays.asList(Component.text("\u00a77 Automatically \u00a76smelt\u00a77 blocks at"), Component.text("\u00a77 the cost of \u00a7c2\u00a77 durability"));
		if (ench.equals(Enchantment.ARROW_DAMAGE))
			return Arrays.asList(Component.text("\u00a77 Increases arrow damage by \u00a7c"+(25+(25*level))+"%"));
		if (ench.equals(Enchantment.ARROW_FIRE))
			return Arrays.asList(Component.text("\u00a77 Arrows set enemies on fire"), Component.text("\u00a77 for \u00a7c5 \u00a77seconds"));
		if (ench.equals(Enchantment.ARROW_INFINITE))
			return Arrays.asList(Component.text("\u00a77 Regular arrows are no longer consumed"));
		if (ench.equals(Enchantment.ARROW_KNOCKBACK))
			return Arrays.asList(Component.text("\u00a77 Arrows knock enemies back an extra"), Component.text("\u00a7c " + (3*level) + "\u00a77 blocks"));
		if (ench.equals(Enchantment.BINDING_CURSE))
			return Arrays.asList(Component.text("\u00a77 Can't be removed once worn"));
		if (ench.equals(Enchantment.CHANNELING))
			return Arrays.asList(Component.text("\u00a77 During storms, enemies hit will"), Component.text("\u00a77 struck by lightning"));
		if (ench.equals(Enchantment.DEPTH_STRIDER))
			return Arrays.asList(Component.text("\u00a77 Reduces the amount water slows"), Component.text("\u00a77 movement speed by \u00a7a" + (level>=3 ? 100 : 33*level) + "%"));
		if (ench.equals(Enchantment.DIG_SPEED))
			return Arrays.asList(Component.text("\u00a77 Increases mining speed"));
		if (ench.equals(Enchantment.DAMAGE_ALL))
			return Arrays.asList(Component.text("\u00a77 Increases attack damage by \u00a7c"+(0.5+(0.5*level))));
		if (ench.equals(Enchantment.DAMAGE_ARTHROPODS))
			return Arrays.asList(Component.text("\u00a77 Increases attack damage against"), Component.text("\u00a74 arthropods\u00a77 by \u00a7c"+(2.5*level)));
		if (ench.equals(Enchantment.DAMAGE_UNDEAD))
			return Arrays.asList(Component.text("\u00a77 Increases attack damage against"), Component.text("\u00a77 the \u00a74undead\u00a77 by \u00a7c"+(2.5*level)));
		if (ench.equals(Enchantment.DURABILITY))
			return Arrays.asList(Component.text("\u00a77 Has a chance to not lose durability"));
		if (ench.equals(Enchantment.FIRE_ASPECT))
			return Arrays.asList(Component.text("\u00a77 Attacks set enemies on fire for"), Component.text("\u00a7c " + (4*level) + "\u00a77 seconds"));
		if (ench.equals(Enchantment.FROST_WALKER))
			return Arrays.asList(Component.text("\u00a77 Walking over water creates an icy"), Component.text("\u00a77 path to walk on"));
		if (ench.equals(Enchantment.IMPALING))
			return Arrays.asList(Component.text("\u00a77 Increases attack damage against"), Component.text("\u00a74 aquatic mobs\u00a77 by \u00a7c" + (2.5*level)));
		if (ench.equals(Enchantment.KNOCKBACK))
			return Arrays.asList(Component.text("\u00a77 Attacks knock enemies back an"), Component.text("\u00a77 extra \u00a7c" + (3*level) + "\u00a77 blocks"));
		if (ench.equals(Enchantment.LOOT_BONUS_MOBS))
			return Arrays.asList(Component.text("\u00a77 All mobs potentially drop an extra"), Component.text("\u00a7a " + (1*level) + "\u00a77 " + (level>1?"items":"item") + " where applicable."));
		if (ench.equals(Enchantment.LOOT_BONUS_BLOCKS))
			return Arrays.asList(Component.text("\u00a77 Mining ores potentially multiplies"), Component.text("\u00a77 drops by up to \u00a7a" + (1+(1*level)) + "x"));
		if (ench.equals(Enchantment.LOYALTY))
			return Arrays.asList(Component.text("\u00a77 Will return after being thrown once"), Component.text("\u00a77 a short amount of time has passed"));
		if (ench.equals(Enchantment.LUCK))
			return Arrays.asList(Component.text("\u00a77 Increases the chance of finding"), Component.text("\u00a77 treasure by \u00a7a" + (2*level) + "%"));
		if (ench.equals(Enchantment.LURE))
			return Arrays.asList(Component.text("\u00a77 Decreases the wait time for catching"), Component.text("\u00a77 something by \u00a7a" + (5*level) + "\u00a77 seconds"));
		if (ench.equals(Enchantment.MENDING))
			return Arrays.asList(Component.text("\u00a77 While in use, collecting experience"), Component.text("\u00a77 will slowly repair this item"));
		if (ench.equals(Enchantment.MULTISHOT))
			return Arrays.asList(Component.text("\u00a77 Fires an additional \u00a7a2 \u00a77projectiles"), Component.text("\u00a77 while only consuming one"));
		if (ench.equals(Enchantment.OXYGEN))
			return Arrays.asList(Component.text("\u00a77 Increases underwater breathing time"), Component.text("\u00a77 by \u00a7a" + (15*level) + "\u00a77 seconds"));
		if (ench.equals(Enchantment.PROTECTION_ENVIRONMENTAL))
			return Arrays.asList(Component.text("\u00a77 Reduces all damage taken"));
		if (ench.equals(Enchantment.PROTECTION_EXPLOSIONS))
			return Arrays.asList(Component.text("\u00a77 Reduces damage taken by explosions"));
		if (ench.equals(Enchantment.PROTECTION_FIRE))
			return Arrays.asList(Component.text("\u00a77 Reduces damage taken by fire"));
		if (ench.equals(Enchantment.PROTECTION_PROJECTILE))
			return Arrays.asList(Component.text("\u00a77 Reduces damage taken by projectiles"));
		if (ench.equals(Enchantment.PROTECTION_FALL))
			return Arrays.asList(Component.text("\u00a77 Reduces fall damage taken by \u00a7a"+Math.min(80, (12*level))+"%"));
		if (ench.equals(Enchantment.PIERCING))
			return Arrays.asList(Component.text("\u00a77 Arrows can pierce up to \u00a7c"+level+"\u00a77 mobs"));
		if (ench.equals(Enchantment.QUICK_CHARGE))
			return Arrays.asList(Component.text("\u00a77 Decreases loading time by \u00a7a" + (0.25*level) + "\u00a77 seconds"));
		if (ench.equals(Enchantment.RIPTIDE))
			return Arrays.asList(Component.text("\u00a77 Launches the player when thrown, but"), Component.text("\u00a77 can only be thrown during rainy"), Component.text("\u00a77 weather and underwater"));
		if (ench.equals(Enchantment.SILK_TOUCH))
			return Arrays.asList(Component.text("\u00a77 Mining a block will typically drop itself"));
		if (ench.equals(Enchantment.SOUL_SPEED))
			return Arrays.asList(Component.text("\u00a77 At the cost of durability, increases"), Component.text("\u00a77 movespeed when on soulsand by \u00a7a" + (30+(10.5*level) + "%")));
		if (ench.equals(Enchantment.SWEEPING_EDGE))
			return Arrays.asList(Component.text("\u00a77 Increases sweeping damage by \u00a7c" + (37.5+(12.5*level))+"%"), Component.text("\u00a77 of this swords attack damage"));
		if (ench.equals(Enchantment.THORNS))
			return Arrays.asList(Component.text("\u00a77 Taking damage has a \u00a7c" + (15*level) + "%\u00a77 chance to"), Component.text("\u00a77 reflect \u00a7c1 - 4\u00a77 damage back to the attacker"));
		if (ench.equals(Enchantment.WATER_WORKER))
			return Arrays.asList(Component.text("\u00a77 Ignore underwater mining penalties"));
		
		return Arrays.asList(Component.text("\u00a77 No info yet."));
	}
	
}
