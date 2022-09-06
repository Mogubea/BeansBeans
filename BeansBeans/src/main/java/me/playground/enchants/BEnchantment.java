package me.playground.enchants;

import java.util.*;

import me.playground.items.ItemRarity;
import me.playground.items.lore.Lore;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.EntityCategory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import io.papermc.paper.enchantments.EnchantmentRarity;
import me.playground.main.Main;
import me.playground.utils.BeanColor;
import me.playground.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.Nullable;

/**
 * Either a custom Enchantment or a vanilla Enchantment with additional variables attached.
 */
public class BEnchantment extends Enchantment {
	
	private static final List<BEnchantment> customEnchants = new ArrayList<>();
	private static final Map<String, BEnchantment> byKeyString = new TreeMap<>();
	private static final Map<String, BEnchantment> byNameString = new TreeMap<>();

	// Replaced Vanilla Enchantments
	public static final BEnchantment LEGACY_DURABILITY = new BEnchantment(Enchantment.DURABILITY, "Unbreaking", 4, 2, 5, 5)
			.setItemRarity(ItemRarity.UNTOUCHABLE)
			.setLegacy();

	// Custom Enchantments
	public static final BEnchantment FAKE_GLOW = new BEnchantment("fake_glow", "Glow", BEnchantmentTarget.VANISHABLE, 1, 1, false, false, false, 0, 0, 0, 0)
			.setLore(Lore.getBuilder("Adds a glow.").dontFormatColours().build())
			.setItemRarity(ItemRarity.UNTOUCHABLE)
			.setHiddenFromLore()
			.setUncleansable();
	
	public static final BEnchantment TELEKINESIS = new BEnchantment("telekinesis", "Telekinesis", BEnchantmentTarget.TOOL, 1, 1, false, false, false, 3, 0, 12, 0)
			.setLore(Lore.getBuilder("Drops from mobs and blocks are automatically added to your Inventory.").dontFormatColours().build())
			.setItemRarity(ItemRarity.UNCOMMON)
			.setBookPowerRequirement(80);

	// replacement enchantment
	public static final BEnchantment UNBREAKING = new BEnchantment("unbreaking", "Unbreaking", BEnchantmentTarget.BREAKABLE, 1, 3, false, false, false, 4, 2, 5, 5) {
		public List<TextComponent> getLore(int level) {
			return getLoreInstance().getLore((int)(12.5 + 12.5 * level) + "");
		}
	}.setLore(Lore.getBuilder("Grants the item a &a{0}%&r chance to avoid losing durability.").build())
			.setBookPowerRequirement(30, 45, 60);

	// tools
	public static final BEnchantment EXPERIENCED = new BEnchantment("experienced", "Experienced", BEnchantmentTarget.TOOL, 1, 3, false, false, false, 4, 2, 6, 4) {
		public List<TextComponent> getLore(int level) {
			return getLoreInstance().getLore("" + 7.5 * level, "" + 0.01 * level);
		}
	}.setLore(Lore.getBuilder("Blocks have a &a{0}%&r chance to drop some experience orbs. There is also a &a{1}%&r chance to drop a significantly larger amount.").build())
			.setItemRarity(ItemRarity.UNCOMMON, 3)
			.setBookPowerRequirement(50, 80, 110);
	
	// pickaxe, axe, shovel
	public static final BEnchantment SMELTING_EDGE = new BEnchantment("smelting_edge", "Smelting Edge", BEnchantmentTarget.TOOL_NO_HOE, 1, 1, false, false, false, 5, 0, 20, 0)
			.setLore(Lore.getBuilder("Drops from appropriate blocks are &#ab7732automatically smelted&r.").build())
			.setItemRarity(ItemRarity.UNCOMMON)
			.setBookPowerRequirement(80);
	
	// fishing rod 
	public static final BEnchantment SCORCHING = new BEnchantment("scorching", "Scorching", BEnchantmentTarget.FISHING_ROD, 1, 1, false, false, false, 2, 0, 10, 0)
			.setLore(Lore.getBuilder("Cooks caught fish and sets reeled mobs on &#bb8855fire&r.").build())
			.setBookPowerRequirement(20);
	
	// hoes
	public static final BEnchantment REAPING = new BEnchantment("reaping", "Reaping", BEnchantmentTarget.HOE, 1, 1, false, false, true, 0, 0, 100, 0) {
		public List<TextComponent> getLore(int level) {
			return getLoreInstance().getLore(2 * level + "");
		}
	}.setLore(Lore.getBuilder("Causes hoes to also reap &a{0} &radjacent crops to the &#666666left &rand &#666666right&r.").build());

	public static final BEnchantment REPLENISH = new BEnchantment("replenish", "Replenish", BEnchantmentTarget.HOE, 1, 1, false, false, false, 4, 0, 15, 0)
			.setLore(Lore.getBuilder("Upon harvesting a &#77cc77replant-able crop&r, a seed from the drops will be used to automatically replant the &#77cc77crop&d.").build())
			.setBookPowerRequirement(50);
	public static final BEnchantment PRESERVATION = new BEnchantment("preservation", "Preservation", BEnchantmentTarget.HOE, 1, 1, false, false, false, 4, 0, 15, 0)
			.setLore(Lore.getBuilder("Prevents hoes from destroying &#aaaa33melon stems&r, &#ddaa33pumpkin stems &rand &#77cc77premature crops&r.").build())
			.setBookPowerRequirement(30);

	// sword
	/**
	 * Attacking an Entity will take some of your coins in exchange for additional damage.
	 */
	public static final BEnchantment PAY_TO_WIN = new BEnchantment("pay_to_win", "Pay to Win", BEnchantmentTarget.WEAPON, 1, 2, false, false, false, 2, 2, 5, 5) {
		public List<TextComponent> getLore(int level) {
			return getLoreInstance().getLore((2^level) + (level-1F), ((2^level) + (level-1))/4F, ((2^level) + (level-1)) * 10);
		}
	}.setLore(Lore.getBuilder("Attacks deal an additional &c{0} \ud83d\udde1 Damage&r and &c{1} \ud83c\udf0a Sweeping Damage &rat the cost of &6{2} Coins&r.").build())
			.setBookPowerRequirement(50, 100);

	// armour
	/**
	 * Additional maximum health, higher levels only obtainable by combining
	 */
	public static final BEnchantment THICKEN = new BEnchantment("thicken", "Thicken", BEnchantmentTarget.ARMOR, 1, 8, false, false, false, 1, 1, 3, 3) {
		public List<TextComponent> getLore(int level) {
			return getLoreInstance().getLore(level);
		}
	}.setLore(Lore.getBuilder("Grants the wearer &#ff5075+{0} \u2764 Max Health&r.").build())
			.addAttribute(Attribute.GENERIC_MAX_HEALTH, AttributeModifier.Operation.ADD_NUMBER, 1D, 1D)
			.setItemRarity(ItemRarity.UNCOMMON, 4)
			.setItemRarity(ItemRarity.RARE, 6)
			.setItemRarity(ItemRarity.EPIC, 8)
			.setItemRarity(ItemRarity.SPECIAL, 9);

	/**
	 * Items take additional durability damage, cannot be removed until fully repaired.
	 */
	public static final BEnchantment BURDEN_FRAGILE = new BEnchantment("fragile", "Fragile", BEnchantmentTarget.BREAKABLE, 1, 3, true, false, false, -2, -2, 0, 0, BEnchantment.UNBREAKING) {
		public List<TextComponent> getLore(int level) {
			return getLoreInstance().getLore(20 * level, level + 1);
		}
	}.setLore(Lore.getBuilder("When taking damage, the item has a &c{0}%&r chance to lose an additional &c1 - {1}&r durability.").build())
			.setItemRarity(ItemRarity.UNCOMMON, 3)
			.setBookPowerRequirement(60, 80, 120);
	/**
	 * Items slow the player and can fail at their main task
	 */
	public static final BEnchantment BURDEN_PONDEROUS = new BEnchantment("ponderous", "Ponderous", BEnchantmentTarget.TOOL, 1, 4, true, false, false, -1, -1, 0, 0)
			.setItemRarity(ItemRarity.UNCOMMON, 3)
			.setBookPowerRequirement(60, 80, 100, 120);
	/**
	 * Items take additional XP and Items to repair, cannot be removed until fully repaired.
	 */
	public static final BEnchantment BURDEN_IRREPARABLE = new BEnchantment("irreparable", "Irreparable", BEnchantmentTarget.BREAKABLE, 1, 3, true, false, false, -1, -1, 0, 0) {
		public List<TextComponent> getLore(int level) {
			return getLoreInstance().getLore(25 * level + "");
		}
	}.setLore(Lore.getBuilder("Increases the material, coin and experience cost of repairing this item by &c{0}%&r.").build())
			.setBookPowerRequirement(50, 60, 70);
	/**
	 * Items steal common drops, items, xp and coins at random.
	 */
	public static final BEnchantment BURDEN_RAPACIOUS = new BEnchantment("rapacious", "Rapacious", BEnchantmentTarget.HANDHELD, 1, 5, true, false, false, -1,- 1, 0, 0)
			.setItemRarity(ItemRarity.UNCOMMON)
			.setBookPowerRequirement(80, 100, 120, 140, 160);
	/**
	 * Items actively refuse to touch spiders.
	 */
	public static final BEnchantment BURDEN_ARACHNOPHOBIC = new BEnchantment("arachnophobic", "Arachnophobic", BEnchantmentTarget.WEAPON, 1, 1, true, false, false, -3, 0, 0, 0)
			.setLore(Lore.getBuilder("The sword is rendered &#774444completely ineffective &ragainst spiders.").build())
			.setBookPowerRequirement(80);
	/**
	 * Items actively refuse to touch the undead.
	 */
	public static final BEnchantment BURDEN_NECROPHOBIC = new BEnchantment("necrophobic", "Necrophobic", BEnchantmentTarget.WEAPON, 1, 1, true, false, false, -4, 0, 0, 0)
			.setLore(Lore.getBuilder("The sword is rendered &#774444completely ineffective &ragainst the undead.").build())
			.setItemRarity(ItemRarity.UNCOMMON)
			.setBookPowerRequirement(100);
	/**
	 * Items actively refuse to touch animals.
	 */
	public static final BEnchantment BURDEN_ZOOPHOBIC = new BEnchantment("zoophobic", "Zoophobic", BEnchantmentTarget.WEAPON, 1, 1, true, false, false, -3, 0, 0, 0)
			.setLore(Lore.getBuilder("The sword is rendered &#774444completely ineffective &ragainst animals.").build())
			.setBookPowerRequirement(80);

	// Vanilla Enchantments
	public static final BEnchantment PROTECTION_ENVIRONMENTAL = new BEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, "Protection", 2, 2, 2, 4)
			.setLore(Lore.getBuilder("The wearer takes less damage from all damage sources.").build())
			.setItemRarity(ItemRarity.UNCOMMON, 4)
			.setBookPowerRequirement(10, 20, 40, 80);

	public static final BEnchantment PROTECTION_FIRE = new BEnchantment(Enchantment.PROTECTION_FIRE, "Fire Protection", 2, 2, 2, 4, Enchantment.PROTECTION_ENVIRONMENTAL)
			.setLore(Lore.getBuilder("The wearer takes less damage from fire.").build())
			.setItemRarity(ItemRarity.UNCOMMON, 4)
			.setBookPowerRequirement(10, 20, 40, 80);

    public static final BEnchantment PROTECTION_FALL = new BEnchantment(Enchantment.PROTECTION_FALL, "Feather Falling", 2, 2, 2, 4) {
		public List<TextComponent> getLore(int level) {
			return getLoreInstance().getLore(Math.min(80, 12*level));
		}
	}.setLore(Lore.getBuilder("The wearer takes &a{0}% &rless fall damage.").build())
			.setItemRarity(ItemRarity.UNCOMMON, 4)
			.setBookPowerRequirement(0, 16, 32, 64);

    public static final BEnchantment PROTECTION_EXPLOSIONS = new BEnchantment(Enchantment.PROTECTION_EXPLOSIONS, "Blast Protection", 2, 2, 2, 4, Enchantment.PROTECTION_FIRE, Enchantment.PROTECTION_ENVIRONMENTAL)
			.setLore(Lore.getBuilder("The wearer takes less damage from explosions.").build())
			.setItemRarity(ItemRarity.UNCOMMON, 4)
			.setBookPowerRequirement(10, 20, 40, 80);

    public static final BEnchantment PROTECTION_PROJECTILE = new BEnchantment(Enchantment.PROTECTION_PROJECTILE, "Projectile Protection", 2, 2, 2, 4, Enchantment.PROTECTION_FIRE, Enchantment.PROTECTION_ENVIRONMENTAL, Enchantment.PROTECTION_EXPLOSIONS)
			.setLore(Lore.getBuilder("The wearer takes less damage from projectiles.").build())
			.setItemRarity(ItemRarity.UNCOMMON, 4)
			.setBookPowerRequirement(10, 20, 40, 80);

    public static final BEnchantment OXYGEN = new BEnchantment(Enchantment.OXYGEN, "Respiration", 2, 2, 5, 3) {
		public List<TextComponent> getLore(int level) {
			return getLoreInstance().getLore(15 * level);
		}
	}.setLore(Lore.getBuilder("The wearer can breathe &#667799underwater &rfor an additional &#667799{0}&r seconds.").build())
    		.setBookPowerRequirement(10, 15, 30);

    public static final BEnchantment WATER_WORKER = new BEnchantment(Enchantment.WATER_WORKER, "Aqua Affinity", 4, 0, 10, 0)
			.setLore(Lore.getBuilder("The wearer ignores all &#667799underwater &#dddd55\u26cf Mining Speed&r penalties.").build())
    		.setBookPowerRequirement(20);

    public static final BEnchantment THORNS = new BEnchantment(Enchantment.THORNS, "Thorns", 1, 2, 5, 3) {
		public List<TextComponent> getLore(int level) {
			return getLoreInstance().getLore(Math.min(100, 15*level));
		}
	}.setLore(Lore.getBuilder("The wearer has a &a{0}%&r chance to reflect &c1 - 4&r damage back to the attacker.").build())
    		.setBookPowerRequirement(5, 10, 20);

    public static final BEnchantment DEPTH_STRIDER = new BEnchantment(Enchantment.DEPTH_STRIDER, "Depth Strider", 3, 1, 5, 3) {
		public List<TextComponent> getLore(int level) {
			return getLoreInstance().getLore((level>=3 ? 100 : 33*level));
		}
	}.setLore(Lore.getBuilder("The wearer ignores &#667799{0}% &rof the &#667799underwater &#a8baef\ud83c\udf0a Speed &rpenalty.").build())
    		.setBookPowerRequirement(5, 10, 20);

    public static final BEnchantment FROST_WALKER = new BEnchantment(Enchantment.FROST_WALKER, "Frost Walker", 4, 0, 10, 0)
			.setLore(Lore.getBuilder("Allows the wearer to walk across &#667799water&r by forming &#6687ccice&r.").build())
    		.setBookPowerRequirement(20);

    public static final BEnchantment BINDING_CURSE = new BEnchantment(Enchantment.BINDING_CURSE, "Curse of Binding", 0, 0, 0, 0)
			.setLore(Lore.getBuilder("The item can not be removed once worn.").build());

    public static final BEnchantment DAMAGE_ALL = new BEnchantment(Enchantment.DAMAGE_ALL, "Sharpness", 1, 2, 3, 3) {
		public List<TextComponent> getLore(int level) {
			return getLoreInstance().getLore(0.5F + (0.5F*level));
		}
	}.setLore(Lore.getBuilder("Increases the weapons &c\ud83d\udde1 Damage&r by &c{0}&r.").build())
			.setItemRarity(ItemRarity.UNCOMMON, 5)
			.setItemRarity(ItemRarity.RARE, 6)
    		.setBookPowerRequirement(0, 5, 15, 30, 70);

    public static final BEnchantment DAMAGE_UNDEAD = new BEnchantment(Enchantment.DAMAGE_UNDEAD, "Smite", 1, 2, 3, 3, Enchantment.DAMAGE_ALL, BEnchantment.BURDEN_NECROPHOBIC) {
		public List<TextComponent> getLore(int level) {
			return getLoreInstance().getLore(2.5F*level);
		}
	}.setLore(Lore.getBuilder("Increases the weapons &c\ud83d\udde1 Undead Damage&r by &c{0}&r.").build())
			.setItemRarity(ItemRarity.UNCOMMON, 5)
			.setItemRarity(ItemRarity.RARE, 6)
    		.setBookPowerRequirement(0, 5, 15, 30, 70);

    public static final BEnchantment DAMAGE_ARTHROPODS = new BEnchantment(Enchantment.DAMAGE_ARTHROPODS, "Bane of Arthropods", 1, 2, 3, 3, Enchantment.DAMAGE_ALL, Enchantment.DAMAGE_UNDEAD, BEnchantment.BURDEN_ARACHNOPHOBIC) {
		public List<TextComponent> getLore(int level) {
			return getLoreInstance().getLore(2.5F*level);
		}
	}.setLore(Lore.getBuilder("Increases the weapons &c\ud83d\udde1 Arthropod Damage&r by &c{0}&r.").build())
			.setItemRarity(ItemRarity.UNCOMMON, 5)
			.setItemRarity(ItemRarity.RARE, 6)
    		.setBookPowerRequirement(0, 5, 15, 30, 70);

    public static final BEnchantment KNOCKBACK = new BEnchantment(Enchantment.KNOCKBACK, "Knockback", 2, 1, 5, 3) {
		public List<TextComponent> getLore(int level) {
			return getLoreInstance().getLore(3 * level);
		}
	}.setLore(Lore.getBuilder("Attacks knock back the target up to an additional &a{0}&r blocks.").build())
    		.setBookPowerRequirement(10, 30);

    public static final BEnchantment FIRE_ASPECT = new BEnchantment(Enchantment.FIRE_ASPECT, "Fire Aspect", 3, 2, 5, 2) {
		public List<TextComponent> getLore(int level) {
			return getLoreInstance().getLore(4*level);
		}
	}.setLore(Lore.getBuilder("Attacks set the target on &#bb8855fire&r for &#bb8855{0}&r seconds.").build())
    		.setBookPowerRequirement(30, 40);

    public static final BEnchantment LOOT_BONUS_MOBS = new BEnchantment(Enchantment.LOOT_BONUS_MOBS, "Looting", 2, 2, 4, 4) {
		public List<TextComponent> getLore(int level) {
			return getLoreInstance().getLore(level <= 1 ? "item" : "&a" + level + " &ritems");
		}
	}.setLore(Lore.getBuilder("Mobs potentially drop an extra {0} where applicable.").build())
			.setItemRarity(ItemRarity.UNCOMMON, 3)
			.setItemRarity(ItemRarity.RARE, 4)
    		.setBookPowerRequirement(30, 60, 90);

    public static final BEnchantment SWEEPING_EDGE = new BEnchantment(Enchantment.SWEEPING_EDGE, "Sweeping Edge", 2, 1, 5, 3) {
		public List<TextComponent> getLore(int level) {
			return getLoreInstance().getLore(37.5F+(12.5F*level));
		}
	}.setLore(Lore.getBuilder("Increases the swords &c\ud83c\udf0a Sweeping Damage&r by &c{0}%&r.").build())
    		.setBookPowerRequirement(10, 15, 25);

    public static final BEnchantment DIG_SPEED = new BEnchantment(Enchantment.DIG_SPEED, "Efficiency", 2, 1, 2, 4) {
		public List<TextComponent> getLore(int level) {
			return getLoreInstance().getLore((level * level) + 1);
		}
	}.setLore(Lore.getBuilder("Increases the tools base &#dddd55\u26cf Mining Speed&r multiplier by &#dddd55{0}&r.").build())
			.setItemRarity(ItemRarity.UNCOMMON, 5)
			.setItemRarity(ItemRarity.RARE, 6)
    		.setBookPowerRequirement(0, 5, 15, 35, 80);

    public static final BEnchantment SILK_TOUCH = new BEnchantment(Enchantment.SILK_TOUCH, "Silk Touch", 5, 0, 15, 0, BEnchantment.SMELTING_EDGE)
			.setLore(Lore.getBuilder("Mining a block will cause it to drop itself, rather than its typical drops.").build())
			.setItemRarity(ItemRarity.UNCOMMON)
    		.setBookPowerRequirement(60);

    public static final BEnchantment LOOT_BONUS_BLOCKS = new BEnchantment(Enchantment.LOOT_BONUS_BLOCKS, "Fortune", 2, 2, 4, 4) {
		public List<TextComponent> getLore(int level) {
			return getLoreInstance().getLore(level + 1);
		}
	}.setLore(Lore.getBuilder("Mining blocks has a chance to multiply drops by up to &a{0} &rtimes.").build())
			.setItemRarity(ItemRarity.UNCOMMON, 3)
			.setItemRarity(ItemRarity.RARE, 4)
    		.setBookPowerRequirement(30, 60, 90);

    public static final BEnchantment ARROW_DAMAGE = new BEnchantment(Enchantment.ARROW_DAMAGE, "Power", 1, 2, 3, 3) {
		public List<TextComponent> getLore(int level) {
			return getLoreInstance().getLore(25 + (25 * level));
		}
	}.setLore(Lore.getBuilder("Increases the &c\ud83d\udde1 Damage&r of arrows fired by &c{0}%&r.").build())
			.setItemRarity(ItemRarity.UNCOMMON, 5)
			.setItemRarity(ItemRarity.RARE, 6)
    		.setBookPowerRequirement(0, 5, 15, 30, 70);

    public static final BEnchantment ARROW_KNOCKBACK = new BEnchantment(Enchantment.ARROW_KNOCKBACK, "Punch", 2, 1, 5, 3) {
		public List<TextComponent> getLore(int level) {
			return getLoreInstance().getLore(3 * level);
		}
	}.setLore(Lore.getBuilder("Arrows fired will knock back the target up to an additional &a{0}&r blocks.").build())
    		.setBookPowerRequirement(5, 15);

    public static final BEnchantment ARROW_FIRE = new BEnchantment(Enchantment.ARROW_FIRE, "Flame", 3, 0, 8, 0) {
		public List<TextComponent> getLore(int level) {
			return getLoreInstance().getLore(level + 1);
		}
	}.setLore(Lore.getBuilder("Arrows fired will set the target on &#bb8855fire&r for &#bb8855{0} &rseconds.").build())
    		.setBookPowerRequirement(5, 15);

    public static final BEnchantment ARROW_INFINITE = new BEnchantment(Enchantment.ARROW_INFINITE, "Infinity", 5, 0, 15, 0)
			.setItemRarity(ItemRarity.UNCOMMON)
			.setLore(Lore.getBuilder("The bow will no longer consume regular arrows when firing.").build());

    public static final BEnchantment LUCK = new BEnchantment(Enchantment.LUCK, "Luck of the Sea", 2, 2, 4, 4) {
		public List<TextComponent> getLore(int level) {
			return getLoreInstance().getLore(Utils.toRoman(level));
		}
	}.setLore(Lore.getBuilder("Affects fishing loot similarly to having the &#55cf55Luck {0}&r status effect.").build())
    		.setBookPowerRequirement(0, 10, 20);

    public static final BEnchantment LURE = new BEnchantment(Enchantment.LURE, "Lure", 2, 2, 4, 4) {
		public List<TextComponent> getLore(int level) {
			return getLoreInstance().getLore(level * 5);
		}
	}.setLore(Lore.getBuilder("Decreases the maximum wait time for catching something by &a{0}&r seconds.").build())
    		.setBookPowerRequirement(0, 10, 20);

    public static final BEnchantment LOYALTY = new BEnchantment(Enchantment.LOYALTY, "Loyalty", 5, 2, 10, 3) {
		public List<TextComponent> getLore(int level) {
			return getLoreInstance().getLore(level == 0 ? "return slowly" : level == 1 ? "return quickly" : "return very quickly");
		}
	}.setLore(Lore.getBuilder("When the trident is thrown, it will", "{0} after landing.").build())
    		.setBookPowerRequirement(10, 20, 30);

    public static final BEnchantment IMPALING = new BEnchantment(Enchantment.IMPALING, "Impaling", 1, 2, 3, 3) {
		public List<TextComponent> getLore(int level) {
			return getLoreInstance().getLore(2.5F * level);
		}
	}.setLore(Lore.getBuilder("Deals an additional &c+{0} \ud83d\udde1 Damage&r against &#667799aquatic&r targets.").build())
			.setItemRarity(ItemRarity.UNCOMMON, 5)
    		.setBookPowerRequirement(0, 5, 15, 30, 70);

    public static final BEnchantment RIPTIDE = new BEnchantment(Enchantment.RIPTIDE, "Riptide", 3, 3, 5, 5, Enchantment.CHANNELING, Enchantment.LOYALTY) {
		public List<TextComponent> getLore(int level) {
			return getLoreInstance().getLore(level == 0 ? "short" : level == 1 ? "large" : "very large");
		}
	}.setLore(Lore.getBuilder("When the trident is thrown when &#667799wet&r, it will instead launch the player a {0} distance at the cost of durability.").build())
			.setItemRarity(ItemRarity.RARE)
    		.setBookPowerRequirement(150, 180, 210);

    public static final BEnchantment CHANNELING = new BEnchantment(Enchantment.CHANNELING, "Channeling", 4, 0, 10, 0, Enchantment.RIPTIDE)
			.setItemRarity(ItemRarity.UNCOMMON)
			.setLore(Lore.getBuilder("During storms, targets truck by the trident will also be struck by lightning.").build());

    public static final BEnchantment MULTISHOT = new BEnchantment(Enchantment.MULTISHOT, "Multishot", 5, 0, 10, 0) {
		public List<TextComponent> getLore(int level) {
			return getLoreInstance().getLore(2.5F * level);
		}
	}.setLore(Lore.getBuilder("Crossbows fire an additional &a2&r projectiles when firing.").build())
    		.setBookPowerRequirement(45);

    public static final BEnchantment QUICK_CHARGE = new BEnchantment(Enchantment.QUICK_CHARGE, "Quick Charge", 2, 2, 4, 4) {
		public List<TextComponent> getLore(int level) {
			return getLoreInstance().getLore(0.25F*level);
		}
	}.setLore(Lore.getBuilder("Crossbows charge &a{0} &rseconds faster.").build())
    		.setBookPowerRequirement(15, 30, 55);

    public static final BEnchantment PIERCING = new BEnchantment(Enchantment.PIERCING, "Piercing", 1, 1, 3, 3, Enchantment.MULTISHOT) {
		public List<TextComponent> getLore(int level) {
			return getLoreInstance().getLore(level);
		}
	}.setLore(Lore.getBuilder("Arrows fired can pierce up to &a{0}&r targets.").build())
    		.setBookPowerRequirement(5, 15, 25);

    public static final BEnchantment MENDING = new BEnchantment(Enchantment.MENDING, "Mending", 0, 0, 250, 0, true, Enchantment.ARROW_INFINITE, BEnchantment.BURDEN_IRREPARABLE)
			.setLore(Lore.getBuilder("Collecting experience while the item is worn or held will consume the experience and repair this item slightly.").build());

    public static final BEnchantment VANISHING_CURSE = new BEnchantment(Enchantment.VANISHING_CURSE, "Curse of Vanishing", 0, 0, 0, 0)
			.setLore(Lore.getBuilder("Dropping the item on death will remove the item from existence.").build());

    public static final BEnchantment SOUL_SPEED = new BEnchantment(Enchantment.SOUL_SPEED, "Soul Speed", 2, 1, 4, 2) {
		public List<TextComponent> getLore(int level) {
			return getLoreInstance().getLore(30 + (level * 10.5F), 4);
		}
	}.setLore(Lore.getBuilder("The wearer's &#a8baef\ud83c\udf0a Speed&r is increased by &#a8baef{0}%&r when walking on &#773545Soul Blocks&r. Each block traversed has a &c{1}% &rchance to consume durability.").build())
			.setItemRarity(ItemRarity.RARE, 5)
			.setBookPowerRequirement(5, 15, 30, 45);

	private final boolean isCustom;
	private final String englishString;
	private final String translationKey;
	private Component componentName;

	private Lore lore = Lore.getBuilder("The description for this Enchant hasn't been defined yet.").dontFormatColours().build();

	// Both of these values are overridden by isAstral
	private ItemRarity baseRarity = ItemRarity.COMMON;
	private final Map<Integer, ItemRarity> rarityRequirements = new TreeMap<>();

	private final EnchantmentRarity vanillaRarity;
	private final BEnchantmentTarget target;
	private final List<Enchantment> conflicts;
	
	private final int baseRunicValue;
	private final int runicValuePerLevel;
	private final int baseXpCost;
	private final int xpCostPerLevel;
	
	private boolean inEnchantTable = false;
	private int[] bookRequirement;

	private final Map<Attribute, BEnchantmentAttribute> itemModifiers = new HashMap<>();

	private final int minLevel;
	private final int maxLevel;
	private final boolean isCurse;
	private final boolean isTreasure;
	private final boolean isTradeable;
	private final boolean isDiscoverable;
	private final boolean isAstral;

	private boolean isHiddenFromLore = false;
	private boolean isCleansable = true;
	private boolean isLegacy = false;
	
	//private final int skillRequirement = 0;
	
	private boolean enabled = true;
	
	protected BEnchantment(String keyName, String engName, BEnchantmentTarget target, int minLevel, int maxLevel, boolean isCurse, boolean isTreasure, boolean isAstral, int runicCost, int runicIncrease, int xpCost, int xpIncrease, Enchantment... conflicts) {
		super(Main.getInstance().getKey(keyName));

		this.vanillaRarity = EnchantmentRarity.VERY_RARE;
		this.minLevel = minLevel;
		this.maxLevel = maxLevel;
		this.isCurse = isCurse;
		this.isTreasure = isTreasure;
		this.isTradeable = false;
		this.isDiscoverable = false;
		this.isAstral = isAstral;
		
		this.isCustom = true;
		this.englishString = engName;
		this.translationKey = engName;
		this.componentName = Component.text(engName, getColour()).decoration(TextDecoration.ITALIC, false);
		this.target = target;

		// Reverse set conflict
		for (Enchantment conflict : conflicts) {
			BEnchantment bEnchantment = BEnchantment.from(conflict);
			if (bEnchantment == null) continue;
			bEnchantment.conflicts.add(this);
		}

		this.conflicts = new ArrayList<>(Arrays.asList(conflicts));
		this.baseRunicValue = runicCost;
		this.runicValuePerLevel = runicIncrease;
		this.baseXpCost = xpCost;
		this.xpCostPerLevel = xpIncrease;
		
		customEnchants.add(this);
		byKeyString.put(key().asString(), this);
		byNameString.put(key().value(), this);
	}
	
	protected BEnchantment(Enchantment enchant, String engName, int runicValue, int runicPerLevel, int xpCost, int xpCostPerLevel, boolean isAstral, Enchantment...conflicts) {
		super(enchant.getKey());
		
		this.vanillaRarity = enchant.getRarity();
		this.minLevel = enchant.getStartLevel();
		this.maxLevel = enchant.getMaxLevel();
		this.isCurse = enchant.isCursed();
		this.isTreasure = enchant.isTreasure();
		this.isTradeable = enchant.isTradeable();
		this.isDiscoverable = enchant.isDiscoverable();
		this.isAstral = isAstral;

		this.isCustom = false;
		this.englishString = engName;
		this.translationKey = enchant.translationKey();
		this.componentName = Component.translatable(translationKey, getColour()).decoration(TextDecoration.ITALIC, false);
		this.target = BEnchantmentTarget.valueOf(enchant.getItemTarget().name());
		this.conflicts = Arrays.asList(conflicts);
		this.baseRunicValue = runicValue;
		this.runicValuePerLevel = runicPerLevel;
		this.baseXpCost = xpCost;
		this.xpCostPerLevel = xpCostPerLevel;

		byKeyString.put(key().asString(), this);
		byNameString.put(key().value(), this);
	}
	
	protected BEnchantment(Enchantment enchant, String engName, int runicValue, int runicPerLevel, int xpCost, int xpCostPerLevel, Enchantment...conflicts) {
		this(enchant, engName, runicValue, runicPerLevel, xpCost, xpCostPerLevel, false, conflicts);
	}

	/**
	 * Grab the {@link BEnchantment} version of an {@link Enchantment}.
	 */
	public static BEnchantment from(Enchantment enchant) {
		if (enchant instanceof BEnchantment bEnchantment)
			return bEnchantment;
		return byKeyString.get(enchant.key().asString());
	}

	@NotNull
	public static List<BEnchantment> getCustomEnchants() {
		return customEnchants;
	}

	@Nullable
	public static BEnchantment getByName(String name) {
		return byNameString.get(name);
	}

	/**
	 * Sets the {@link ItemRarity} of this Enchantment by default.
	 */
	protected BEnchantment setItemRarity(ItemRarity rarity) {
		this.baseRarity = rarity;
		return this;
	}

	/**
	 * Set the Enchantment level requirement for this {@link ItemRarity}.
	 */
	protected BEnchantment setItemRarity(ItemRarity rarity, int level) {
		this.rarityRequirements.put(level, rarity);
		return this;
	}

	@NotNull
	public ItemRarity getItemRarity() {
		return baseRarity;
	}

	@NotNull
	public ItemRarity getItemRarity(int enchantmentLevel) {
		if (isAstral) return ItemRarity.ASTRAL;
		if (enchantmentLevel <= 1) return baseRarity;

		for (Map.Entry<Integer, ItemRarity> entry : rarityRequirements.entrySet())
			if (enchantmentLevel >= entry.getKey()) return entry.getValue();

		return baseRarity;
	}

	/**
	 * Sets the Enchantment Power requirement to apply this Enchantment onto an item
	 */
	protected BEnchantment setBookPowerRequirement(int...bookRequirements) {
		this.bookRequirement = bookRequirements;
		this.inEnchantTable = true;
		
		return this;
	}

	/**
	 * Removes it from the named map but keeps it in the key map
	 */
	protected BEnchantment setLegacy() {
		this.componentName = this.componentName.color(NamedTextColor.DARK_GRAY).decoration(TextDecoration.STRIKETHROUGH, true);
		this.isLegacy = true;

		byNameString.remove(key().value());
		return this;
	}

	/**
	 * Add a basic Attribute Modifier to the items that have this Enchantment applied.
	 */
	protected BEnchantment addAttribute(@NotNull Attribute attribute, AttributeModifier.Operation operation, double defaultValue, double perLevel) {
		itemModifiers.put(attribute, new BEnchantmentAttribute(this, attribute, operation, defaultValue, perLevel));
		return this;
	}

	@NotNull
	private Map<Attribute, AttributeModifier> getAttributes(int level) {
		Map<Attribute, AttributeModifier> clone = new HashMap<>();
		itemModifiers.forEach((attribute, attributeModifier) -> clone.put(attribute, attributeModifier.getModifier(level)));
		return clone;
	}

	public void applyAttributes(@NotNull ItemStack itemStack, int level) {
		if (itemModifiers.isEmpty()) return;
		revokeAttributes(itemStack);

		Map<Attribute, AttributeModifier> map = getAttributes(level);
		itemStack.editMeta(meta -> map.forEach(meta::addAttributeModifier));
	}

	public void revokeAttributes(@NotNull ItemStack itemStack) {
		itemStack.editMeta(meta -> itemModifiers.forEach(((attribute, attributeModifier) -> meta.removeAttributeModifier(attribute, attributeModifier.getModifier(1)))));
	}

	protected BEnchantment setLore(Lore lore) {
		this.lore = lore;
		return this;
	}

	protected Lore getLoreInstance() {
		return lore;
	}

	public List<TextComponent> getLore() {
		return lore.getLore();
	}

	public List<TextComponent> getLore(int level) {
		return lore.getLore();
	}

	public int getBookRequirement(int level) {
		if (level < 1) level = 1;
		if (level > bookRequirement.length) level = bookRequirement.length;
		
		return bookRequirement[level - 1];
	}

	/**
	 * Can this Enchantment be removed from the {@link ItemStack}?
	 * @return true or false.
	 */
	public boolean isCleansable() { return isCleansable; }

	/**
	 * Does this Enchantment show up in the lore of an {@link ItemStack}?
	 * @return true or false.
	 */
	public boolean isHiddenFromLore() { return isHiddenFromLore; }

	/**
	 * Is this Enchantment exclusively a {@link BEnchantment}?
	 * @return true or false.
	 */
	public boolean isCustom() {
		return isCustom;
	}

	/**
	 * Is this Enchantment an Atral Enchantment?
	 * @return true or false.
	 */
	public boolean isAstral() {
		return isAstral;
	}

	/**
	 * Is this Enchantment enabled?
	 * @return true or false.
	 */
	public boolean isEnabled() {
		return enabled;
	}
	
	public boolean inEnchantingTable() {
		return inEnchantTable;
	}

	@NotNull
	public String getName() {
		return englishString;
	}

	@NotNull
	public Component displayName() {
		return componentName;
	}

	@NotNull
	public BEnchantmentTarget getEnchantmentTarget() {
		return target;
	}
	
	public int getExperienceCost(int level) {
		return baseXpCost + (xpCostPerLevel * (level-1));
	}
	
	public int getRunicValue(int level) {
		return baseRunicValue + (runicValuePerLevel * (level-1));
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public BEnchantment setHiddenFromLore() {
		this.isHiddenFromLore = true;
		return this;
	}

	public BEnchantment setUncleansable() {
		this.isCleansable = false;
		return this;
	}
	
	// default methods from enchantment wrapper
	
	public boolean canEnchantItem(@NotNull ItemStack item) {
	    return target.includes(item);
	}
	
    public int getMaxLevel() {
        return maxLevel;
    }

    public int getStartLevel() {
        return minLevel;
    }

    public boolean isTreasure() {
        return isTreasure;
    }

    public boolean isCursed() {
        return isCurse;
    }

    public boolean conflictsWith(@NotNull Enchantment other) {
        return conflicts.contains(other);
    }
    
    public boolean conflictsWith(@NotNull ItemStack item) {
    	for (Enchantment enchantment : conflicts)
    		if (item.containsEnchantment(enchantment))
    			return true;
    	return false;
    }
    
    public List<Enchantment> getConflicts() {
    	return this.conflicts;
    }
    
    public List<Enchantment> getActiveConflicts(ItemStack item) {
    	List<Enchantment> konflicts = new ArrayList<>();
    	for (Enchantment ench : conflicts)
    		if (item.containsEnchantment(ench))
    			konflicts.add(ench);
    	return konflicts;
    }
    
    @NotNull
    public net.kyori.adventure.text.Component displayName(int level) {
    	Component heck = isCustom ? Component.text(englishString) : Component.translatable(translationKey);
    	if (maxLevel != 1)
    		heck = heck.append(Component.text(" " + Utils.toRoman(level)));
        return heck.color(getColour()).decoration(TextDecoration.ITALIC, false);
    }

    public @NotNull String translationKey() {
        return translationKey;
    }

    public boolean isTradeable() {
        return isTradeable;
    }

    public boolean isDiscoverable() {
        return isDiscoverable;
    }

    @NotNull
    public EnchantmentRarity getRarity() {
        return vanillaRarity;
    }
    
    /**
     * Pointless for custom enchantments
     */
    @Deprecated
    public float getDamageIncrease(int level, @NotNull EntityCategory entityCategory) {
        return 0;
    }
    
    /**
     * Pointless for custom enchantments
     */
    @Deprecated
    public java.util.Set<org.bukkit.inventory.EquipmentSlot> getActiveSlots() {
        return null;
    }

    /**
     * Overriden by BEnchantmentTarget
     */
	@Deprecated
	public @NotNull EnchantmentTarget getItemTarget() {
		return EnchantmentTarget.TRIDENT; // not used anyway
	}
	
	private BeanColor getColour() {
		if (isAstral) return BeanColor.ENCHANT_ASTRAL;
		if (isCurse) return BeanColor.ENCHANT_BURDEN;
		return BeanColor.ENCHANT;
	}
	
	@NotNull
    public static BEnchantment[] values() {
        return byKeyString.values().toArray(new BEnchantment[0]);
    }
	
	public static int size(boolean includeLegacy) {
		return includeLegacy ? byKeyString.size() : byNameString.size();
	}

	public boolean isLegacy() {
		return isLegacy;
	}
}
