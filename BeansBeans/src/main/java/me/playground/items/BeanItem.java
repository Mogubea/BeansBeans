package me.playground.items;

import java.io.Serial;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import me.playground.gui.stations.BeanGuiEnchantingTable;
import me.playground.items.tracking.DemanifestationReason;
import me.playground.items.tracking.ItemTrackingManager;
import me.playground.items.tracking.ManifestationReason;
import me.playground.playerprofile.PlayerProfile;
import me.playground.playerprofile.stats.DirtyDouble;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.ShulkerBox;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.BlockDataMeta;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.TropicalFishBucketMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.checkerframework.checker.nullness.qual.NonNull;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import me.playground.enchants.BEnchantment;
import me.playground.main.Main;
import me.playground.utils.BeanColor;
import me.playground.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;

public class BeanItem {
	private final static DecimalFormat df = new DecimalFormat("0.###");
	protected static final Map<Attribute, UUID> refinementAttributeUUIDs = new HashMap<>();
	private final static HashMap<Material, ItemRarity> vanillaRarities = new HashMap<>() {
		@Serial
		private static final long serialVersionUID = 1018055300807913156L;

		{
			for (Material m : Material.values()) {
				if (m.name().endsWith("_SPAWN_EGG"))
					put(m, ItemRarity.UNCOMMON);
				else if (m.name().endsWith("SHULKER_BOX"))
					put(m, ItemRarity.RARE);
				else if (m.name().startsWith("NETHERITE_"))
					put(m, ItemRarity.UNCOMMON);
				else if (m.name().startsWith("DIAMOND_") && m != Material.DIAMOND_ORE)
					put(m, ItemRarity.UNCOMMON);
				else if (m.name().startsWith("CHAINMAIL_"))
					put(m, ItemRarity.UNCOMMON);
				else if (m.name().contains("COMMAND_BLOCK"))
					put(m, ItemRarity.UNTOUCHABLE);
			}

			put(Material.SKELETON_SKULL, ItemRarity.UNCOMMON);
			put(Material.CREEPER_HEAD, ItemRarity.UNCOMMON);
			put(Material.PLAYER_HEAD, ItemRarity.UNCOMMON);
			put(Material.ZOMBIE_HEAD, ItemRarity.UNCOMMON);
			put(Material.WITHER_SKELETON_SKULL, ItemRarity.UNCOMMON);

			put(Material.END_CRYSTAL, ItemRarity.UNCOMMON);
			put(Material.HEART_OF_THE_SEA, ItemRarity.UNCOMMON);

			put(Material.ANCIENT_DEBRIS, ItemRarity.UNCOMMON);

			put(Material.SCULK_SHRIEKER, ItemRarity.UNCOMMON);
			put(Material.SCULK_CATALYST, ItemRarity.UNCOMMON);

			put(Material.ECHO_SHARD, ItemRarity.RARE);
			put(Material.NETHER_STAR, ItemRarity.RARE);
			put(Material.TRIDENT, ItemRarity.RARE);
			put(Material.ELYTRA, ItemRarity.RARE);
			put(Material.DRAGON_HEAD, ItemRarity.RARE);
			put(Material.BEACON, ItemRarity.RARE);
			put(Material.CONDUIT, ItemRarity.RARE);
			put(Material.DEEPSLATE_COAL_ORE, ItemRarity.RARE);
			put(Material.DEEPSLATE_EMERALD_ORE, ItemRarity.RARE);
			put(Material.DRAGON_EGG, ItemRarity.RARE);
			put(Material.SPAWNER, ItemRarity.RARE);
			put(Material.ENCHANTED_GOLDEN_APPLE, ItemRarity.RARE);

			put(Material.ELDER_GUARDIAN_SPAWN_EGG, ItemRarity.RARE);
			put(Material.EVOKER_SPAWN_EGG, ItemRarity.RARE);

			put(Material.TOTEM_OF_UNDYING, ItemRarity.EPIC);

			put(Material.LIGHT, ItemRarity.SPECIAL);

			put(Material.END_PORTAL_FRAME, ItemRarity.UNTOUCHABLE);
			put(Material.BEDROCK, ItemRarity.UNTOUCHABLE);
			put(Material.DEBUG_STICK, ItemRarity.UNTOUCHABLE);
			put(Material.BUNDLE, ItemRarity.UNTOUCHABLE);
			put(Material.BARRIER, ItemRarity.UNTOUCHABLE);
			put(Material.STRUCTURE_BLOCK, ItemRarity.UNTOUCHABLE);
			put(Material.STRUCTURE_VOID, ItemRarity.UNTOUCHABLE);
			put(Material.JIGSAW, ItemRarity.UNTOUCHABLE);
			put(Material.KNOWLEDGE_BOOK, ItemRarity.UNTOUCHABLE);
		}
	};
	
	public final static NamespacedKey KEY_ID = key("ID"); // String
	public final static NamespacedKey KEY_DURABILITY = key("DURABILITY"); // Integer
	public final static NamespacedKey KEY_MAX_DURABILITY = key("MAX_DURABILITY"); // Integer
	public final static NamespacedKey KEY_COUNTER = key("COUNTER"); // Integer

	public final static NamespacedKey KEY_CREATION_TIME = key("ITEM_CREATION_TIME"); // Long
	public final static NamespacedKey KEY_CREATION_IDX = key("ITEM_CREATION_IDX"); // Long
	public final static NamespacedKey KEY_CREATION_PLAYER = key("ITEM_CREATION_PLAYER"); // Int
	
	private final static HashMap<String, BeanItem> itemsByName = new HashMap<>();
	private final static ArrayList<Integer> usedNumerics = new ArrayList<>();
	private static final BeanItem[] items;
	
	public final static BeanItem FORMATTING_WAND = new BItemDurable(2000000, "FORMATTING_WAND", "Formatting Wand", Material.GOLDEN_AXE, ItemRarity.UNTOUCHABLE, 1, 500) {
		public List<TextComponent> getCustomLore(ItemStack item) {
			item.getItemMeta().setUnbreakable(true);

			return new ArrayList<>(Arrays.asList(
					Component.text("\u00a77Right clicking a container will update"),
					Component.text("\u00a77all items inside to the newest format!")));
		}
		
		public void onBlockMined(BlockBreakEvent e) {
			e.setCancelled(true);
		}
		
		public boolean onInteract(PlayerInteractEvent e) {
			e.setCancelled(true);
			Block b = e.getClickedBlock();
			if (e.getHand() == EquipmentSlot.HAND && e.getAction() == Action.RIGHT_CLICK_BLOCK && b != null && b.getState() instanceof Container c) {
				ItemStack[] newInv = c.getInventory().getContents();
				for (int x = 0; x < newInv.length; x++) {
					ItemStack i = newInv[x];
					if (i == null) continue;
					newInv[x] = formatItem(i);
				}
				c.getInventory().setContents(newInv);
			}
			return true;
		}
	};
	
	public final static BeanItem DEFORMATTING_WAND = new BItemDurable(2000001, "DEFORMATTING_WAND", "Deformatting Wand", Material.NETHERITE_AXE, ItemRarity.UNTOUCHABLE, 2, 500) {
		public List<TextComponent> getCustomLore(ItemStack item) {
			item.getItemMeta().setUnbreakable(true);

			return new ArrayList<>(Arrays.asList(
					Component.text("\u00a77Right clicking a container will clear"),
					Component.text("\u00a77all materialistic items inside of formatting!")));
		}
		
		public void onBlockMined(BlockBreakEvent e) {
			e.setCancelled(true);
		}
		
		public boolean onInteract(PlayerInteractEvent e) {
			e.setCancelled(true);
			Block b = e.getClickedBlock();
			if (b != null && b.getState() instanceof Container c) {
				ItemStack[] newInv = c.getInventory().getContents();
				for (int x = 0; x < newInv.length; x++) {
					ItemStack i = newInv[x];
					if (i == null) continue;
					newInv[x] = resetItemFormatting(i);
				}
				c.getInventory().setContents(newInv);
			}
			return true;
		}
	};
	
	public final static BeanItem DEBUG_CAKE = new BeanItem(2000002, "DEBUG_CAKE", "Bug Finder's Cake", Material.CAKE, ItemRarity.IRIDESCENT, 1) {
		public boolean onInteract(PlayerInteractEvent e) {
			e.setCancelled(true);
			return true;
		}
	}.setDefaultLore(Component.text("\u00a77A token of appreciation for finding"), Component.text("\u00a77and reporting serious bugs! Thanks!"));
	
	public final static BeanItem SHOP_STAND = new BItemShopStand(0, "SHOP_STAND", "Shopping Stand", ItemRarity.RARE);
	public final static BeanItem PLAYER_MENU = new BeanItem(1, "PLAYER_MENU", "Player Menu", Material.NETHER_STAR, ItemRarity.UNCOMMON, 1)
			.setDefaultLore(Component.text("Click to open the ", NamedTextColor.GRAY).append(Component.text("/menu", BeanColor.COMMAND)));
	
	public final static BeanItem TALARIANS = new BItemDurable(3, "TALARIANS", "Lesser Talarians", Utils.getDyedLeather(Material.LEATHER_BOOTS, 0xFF8833), ItemRarity.RARE, 1, 176)
			.setDefaultLore(
					Component.text("While sprinting, slowly gather momentum", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
					Component.text("up to a maximum bonus of ", NamedTextColor.GRAY).append(Component.text("+25% Movement", NamedTextColor.WHITE)).decoration(TextDecoration.ITALIC, false),
					Component.text("Speed", NamedTextColor.WHITE).append(Component.text(", at the cost of some durability!", NamedTextColor.GRAY)).decoration(TextDecoration.ITALIC, false))
			.addAttribute(Attribute.GENERIC_ARMOR, 1, EquipmentSlot.FEET)
			.addAttribute(Attribute.GENERIC_MOVEMENT_SPEED, 0.004, EquipmentSlot.FEET);
	
	public final static BeanItem ROSE_GOLD_HELMET = new BItemHelmetRoseGold(4, 1);
	public final static BeanItem ROSE_GOLD_CHESTPLATE = new BItemChestplateRoseGold(5, 1);
	public final static BeanItem ROSE_GOLD_LEGGINGS = new BItemLeggingsRoseGold(6, 1);
	public final static BeanItem ROSE_GOLD_BOOTS = new BItemBootsRoseGold(7, 2);

	public final static BeanBlock BASIC_REGION_CAPSULE = new BItemRegionCapsule(8, "BASIC_REGION_CAPSULE", "Basic Region Capsule", ItemRarity.UNCOMMON, 21);
	public final static BeanBlock REGION_CRYSTAL = new BItemRegionCrystal(9, "REGION_CRYSTAL", "Region Crystal", ItemRarity.COMMON);

	public final static BeanItem POOR_QUALITY_WHEAT = new BeanItem(500, "POOR_QUALITY_WHEAT", "Poor Quality Wheat", Material.WHEAT, ItemRarity.TRASH, 1);
	public final static BeanItem POOR_QUALITY_POTATO = new BeanItem(501, "POOR_QUALITY_POTATO", "Poor Quality Potato", Material.POTATO, ItemRarity.TRASH, 1);
	public final static BeanItem POOR_QUALITY_CARROT = new BeanItem(502, "POOR_QUALITY_CARROT", "Poor Quality Carrot", Material.CARROT, ItemRarity.TRASH, 1);
	public final static BeanItem POOR_QUALITY_BEETROOT = new BeanItem(503, "POOR_QUALITY_BEETROOT", "Poor Quality Beetroot", Material.BEETROOT, ItemRarity.TRASH, 1);
	public final static BeanItem QUALITY_WHEAT = new BeanItem(504, "QUALITY_WHEAT", "Quality Wheat", Material.WHEAT, ItemRarity.UNCOMMON, 1);
	public final static BeanItem QUALITY_POTATO = new BeanItem(505, "QUALITY_POTATO", "Quality Potato", Material.POTATO, ItemRarity.UNCOMMON, 1);
	public final static BeanItem QUALITY_CARROT = new BeanItem(506, "QUALITY_CARROT", "Quality Carrot", Material.CARROT, ItemRarity.UNCOMMON, 1);
	public final static BeanItem QUALITY_BEETROOT = new BeanItem(507, "QUALITY_BEETROOT", "Quality Beetroot", Material.BEETROOT, ItemRarity.UNCOMMON, 1);

	public final static BeanItem IRON_CHEST = new BItemBigChest(1001, "IRON_CHEST", "Iron Reinforced Chest", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGZmN2UwMGQ1OGQ4OTUzZTZlNmZkYzY1ZGJkYTRmYmQ3NGE4MGRiYzJjMWNkOGZhZjliMmZhYmE4MTNmY2NlNiJ9fX0=", ItemRarity.UNCOMMON, 1, 45, Material.IRON_BLOCK);
	public final static BeanItem GOLDEN_CHEST = new BItemBigChest(1002, "GOLDEN_CHEST", "Gold Reinforced Chest", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWI0ZDJkOWMxN2Y0YzgxOTAyMmI5MDViNTlmYTBlNzUyOWY2ZTNmYzcxZGJmNTQ3MGIzNzM4ZmMzODhiMTQwYSJ9fX0=", ItemRarity.RARE, 1, 45 * 2, Material.GOLD_BLOCK);
	public final static BeanItem DIAMOND_CHEST = new BItemBigChest(1003, "DIAMOND_CHEST", "Diamond Reinforced Chest", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2M0ZTIwZWYzMzJkYzczNDY2NmQzMWUwZjI5MDRmMGM5M2VkMjkzNDBhMzU5MzJmOTA5YWZiM2Q2OTliMGU1ZSJ9fX0=", ItemRarity.EPIC, 1, 45 * 3, Material.DIAMOND_BLOCK);
	public final static BeanItem CRYSTAL_CHEST = new BItemBigChest(1004, "CRYSTAL_CHEST", "Crystal Reinforced Chest", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTJiMmQ2NGQ2YWU1MzE1N2I2ZGFhZDZjMjAxNmE3M2U0NjU2MDU0MDlhZjg0NTQxOTU5ZjFhMGRjNTM3YjE5ZiJ9fX0=", ItemRarity.SPECIAL, 1, 45 * 6, Material.AMETHYST_BLOCK);
	public final static BeanItem CATS_PAW = new BItemFishingRodCatsPaw(1005, "CATS_PAW", "Cat's Paw", ItemRarity.UNCOMMON, 1, 90);
	public final static BeanItem LIVING_METAL_CHUNK = new BeanItem(1006, "LIVING_METAL_CHUNK", "Living Metal Chunk", Material.IRON_NUGGET, ItemRarity.COMMON, 1).addGlow();
	public final static BeanItem LIVING_METAL_INGOT = new BeanItem(1007, "LIVING_METAL_INGOT", "Living Metal Ingot", Material.IRON_INGOT, ItemRarity.UNCOMMON, 1).addGlow();
	public final static BeanItem LIVING_METAL_BLOCK = new BeanItem(1008, "LIVING_METAL_BLOCK", "Living Metal Block", Material.IRON_BLOCK, ItemRarity.RARE, 1).addGlow();
	public final static BeanItem LIVING_HOPPER = new BItemLivingHopper(1009, "LIVING_HOPPER", "Living Hopper", ItemRarity.UNCOMMON, 1).addGlow();
	public final static BeanItem CAPTURE_EGG = new BItemCaptureEgg(1010, "CAPTURE_EGG", "Capture Egg", Material.GHAST_SPAWN_EGG, ItemRarity.UNCOMMON, 1);
	public final static BeanItem PICKY_AXE = new BItemPickyAxe(1011, "PICKY_AXE", "Picky Axe", Material.STONE_AXE, ItemRarity.UNCOMMON, 1, 222);
	public final static BeanItem PICKIER_AXE = new BItemPickierAxe(1012, "PICKIER_AXE", "Pickier Axe", Material.STONE_AXE, ItemRarity.RARE, 2, 444);

	public final static BeanItem HUNGRY_TREECAPITATOR = new BItemTreecapitator(); // 1000

	public final static BeanItemHeirloom HL_MOCHI 			= new BItemHeirloomMochi(10000, "HL_MOCHI", "Mochi", ItemRarity.RARE);
	public final static BeanItemHeirloom HL_ANCIENT_SKULL 	= new BItemHeirloomAncientSkull(10001, "HL_ANCIENT_SKULL", "Ancient Skull", ItemRarity.RARE);
	public final static BeanItemHeirloom TALISMAN_OF_CONFIDENCE = new BeanItemHeirloomTalismanOfConfidence(10002, "TALISMAN_OF_CONFIDENCE", "\"Talisman\" of Confidence", ItemRarity.RARE);

	//public final static BeanItem REGION_CAPSULE_STARTER = new BItemRegionCapsuleStarter(15000, "RC_STARTER", "Starting Region Capsule", ItemRarity.EPIC);
	
	public final static BeanItem WOODEN_CRATE = new BItemPackage(19500, "WOODEN_CRATE", "Wooden Crate", ItemRarity.UNCOMMON, "crate_oak", 3, Material.OAK_PLANKS, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjYyMDUxOWI3NDUzNmMxZjg1YjdjN2U1ZTExY2U1YzA1OWMyZmY3NTljYjhkZjI1NGZjN2Y5Y2U3ODFkMjkifX19");
	public final static BeanItem DEEP_OCEAN_CRATE = new BItemPackage(19501, "DEEP_OCEAN_CRATE", "Deep Ocean Crate", ItemRarity.RARE, "crate_deep", 4, Material.DARK_OAK_PLANKS, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTcxMTQwNjVhM2M5NWM1ZDIyNTE4OGFkN2JmZGFhOWI4YjA4NDVkZjRlMzZjMjRiNDUzNDdmZDc0NzBhNyJ9fX0=");
	public final static BeanItem IRON_CRATE = new BItemPackage(19502, "METAL_CRATE", "Metal Crate", ItemRarity.RARE, "crate_iron", 3, Material.IRON_BLOCK, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzFhODE0NWFhY2Y1YzRjNjNmMmQwMjM4OGJmNDcxNGJiMDk2MWRjYjVhZjdlMTU4MTk5YjI1MTgzZWQ4NDFmZCJ9fX0=");
	
	//public final static BeanItem BIRCH_CRATE = new BItemPackage(19502, "BIRCH_CRATE", "Birch Crate", ItemRarity.UNCOMMON, "crate_birch", 3, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2I0ZTQ3OGMxMWNjZGZhNDUzMjk4NzQ3MjQ1ZTA0MjVlYzQyNTFlMTdmYjNlNjdkYmVjMTQxNzZjNjQ3MTcifX19");
	//public final static BeanItem JUNGLE_CRATE = new BItemPackage(19503, "JUNGLE_CRATE", "Jungle Crate", ItemRarity.UNCOMMON, "crate_jungle", 3, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmNmNWIxY2ZlZDFjMjdkZDRjM2JlZjZiOTg0NDk5NDczOTg1MWU0NmIzZmM3ZmRhMWNiYzI1YjgwYWIzYiJ9fX0=");
	//public final static BeanItem DARK_OAK_CRATE = new BItemPackage(19504, "DARK_OAK_CRATE", "Dark Oak Crate", ItemRarity.UNCOMMON, "crate_dark_oak", 3, "");
	
	public final static BeanBlock SPIDER_HEAD = new BeanBlock(20000, "SPIDER_HEAD", "Spider Head", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzg3YTk2YThjMjNiODNiMzJhNzNkZjA1MWY2Yjg0YzJlZjI0ZDI1YmE0MTkwZGJlNzRmMTExMzg2MjliNWFlZiJ9fX0=", ItemRarity.UNCOMMON).setWearable();
	public final static BeanBlock BLAZE_HEAD = new BeanBlock(20001, "BLAZE_HEAD", "Blaze Head", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjc4ZWYyZTRjZjJjNDFhMmQxNGJmZGU5Y2FmZjEwMjE5ZjViMWJmNWIzNWE0OWViNTFjNjQ2Nzg4MmNiNWYwIn19fQ==", ItemRarity.RARE).setWearable();
	public final static BeanBlock PIGLIN_HEAD = new BeanBlock(20002, "PIGLIN_HEAD", "Piglin Head", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTBiYzlkYmI0NDA0YjgwMGY4Y2YwMjU2MjIwZmY3NGIwYjcxZGJhOGI2NjYwMGI2NzM0ZjRkNjMzNjE2MThmNSJ9fX0=", ItemRarity.RARE).setWearable();
	public final static BeanBlock PHANTOM_HEAD = new BeanBlock(20003, "PHANTOM_HEAD", "Phantom Head", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzQ2ODMwZGE1ZjgzYTNhYWVkODM4YTk5MTU2YWQ3ODFhNzg5Y2ZjZjEzZTI1YmVlZjdmNTRhODZlNGZhNCJ9fX0=", ItemRarity.RARE).setWearable();
	public final static BeanBlock COW_HEAD = new BeanBlock(20004, "COW_HEAD", "Cow Head", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2RmYTBhYzM3YmFiYTJhYTI5MGU0ZmFlZTQxOWE2MTNjZDYxMTdmYTU2OGU3MDlkOTAzNzQ3NTNjMDMyZGNiMCJ9fX0=", ItemRarity.UNCOMMON).setWearable();
	public final static BeanBlock PIG_HEAD = new BeanBlock(20005, "PIG_HEAD", "Pig Head", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjIxNjY4ZWY3Y2I3OWRkOWMyMmNlM2QxZjNmNGNiNmUyNTU5ODkzYjZkZjRhNDY5NTE0ZTY2N2MxNmFhNCJ9fX0=", ItemRarity.UNCOMMON).setWearable();
	public final static BeanBlock CHICKEN_HEAD = new BeanBlock(20006, "CHICKEN_HEAD", "Chicken Head", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTYzODQ2OWE1OTljZWVmNzIwNzUzNzYwMzI0OGE5YWIxMWZmNTkxZmQzNzhiZWE0NzM1YjM0NmE3ZmFlODkzIn19fQ==", ItemRarity.UNCOMMON).setWearable();
	public final static BeanBlock SHEEP_HEAD = new BeanBlock(20007, "SHEEP_HEAD", "Sheep Head", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjMxZjljY2M2YjNlMzJlY2YxM2I4YTExYWMyOWNkMzNkMThjOTVmYzczZGI4YTY2YzVkNjU3Y2NiOGJlNzAifX19", ItemRarity.UNCOMMON).setWearable();
	public final static BeanBlock DROWNED_HEAD = new BeanBlock(20008, "DROWNED_HEAD", "Drowned Head", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzg0ZGY3OWM0OTEwNGIxOThjZGFkNmQ5OWZkMGQwYmNmMTUzMWM5MmQ0YWI2MjY5ZTQwYjdkM2NiYmI4ZTk4YyJ9fX0=", ItemRarity.UNCOMMON).setWearable();
	public final static BeanBlock ENDERMAN_HEAD = new BeanBlock(20009, "ENDERMAN_HEAD", "Enderman Head", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTZjMGIzNmQ1M2ZmZjY5YTQ5YzdkNmYzOTMyZjJiMGZlOTQ4ZTAzMjIyNmQ1ZTgwNDVlYzU4NDA4YTM2ZTk1MSJ9fX0=", ItemRarity.UNCOMMON).setWearable();
	
	static {
		items = itemsByName.values().toArray(new BeanItem[0]);
		for (BeanItem item : items)
			formatItem(item.originalStack);

		for (Attribute attribute : Attribute.values())
			refinementAttributeUUIDs.put(attribute, UUID.fromString("fa1cb695-62e7-4f36-82cd-9ed930e2" + String.format("%04d", getAttributeId(attribute))));
	}
	
	private final byte[]		    bytes;
	private final TextComponent	displayName;
	private final ItemStack 		originalStack;
	private final String	 		identifier;
	private final int				customModelData;
	protected final Material  		material;
	protected final ItemRarity 		defaultRarity;
	protected final boolean 		enchantable;
	protected final boolean 		repairable;
	protected final boolean 		reforgable;
	protected final boolean 		canRarityChange;
	protected final int				maxDurability;
	protected final Multimap<Attribute, AttributeModifier> baseAttributes = HashMultimap.create();
	protected final List<TextComponent> baseLore = new ArrayList<>();
	protected final Random		    random = Main.getInstance().getRandom();
	
	protected boolean 				enabled = true;
	private boolean 				trackCreation = false; // Tracking an item's creation will cause it to not be stackable due to data.
	
	protected BeanItem(final int numeric, @NonNull final String identifier, String name, Material material, ItemRarity rarity, int modelDataInt) {
		this(numeric, identifier, name, new ItemStack(material), rarity, modelDataInt, 0);
	}
	
	protected BeanItem(final int numeric, @NonNull final String identifier, String name, ItemStack item, ItemRarity rarity, int modelDataInt) {
		this(numeric, identifier, name, item, rarity, modelDataInt, 0);
	}
	
	protected BeanItem(final int numeric, @NonNull final String identifier, String name, Material material, ItemRarity rarity, int modelDataInt, int maxDurability) {
		this(numeric, identifier, name, new ItemStack(material), rarity, modelDataInt, maxDurability);
	}
	
	/**
	 * Used for creating an instance of {@link BeanItem} using a skull with a custom skin.
	 */
	protected BeanItem(final int numeric, @NonNull final String identifier, String name, String skullBase64, ItemRarity rarity) {
		this(numeric, identifier, name, Utils.getSkullWithCustomSkin(UUID.nameUUIDFromBytes(ByteBuffer.allocate(16).putInt(numeric).array()), skullBase64), rarity, 0, 0);
	}
	
	/**
	 * When the server has been booted on live at least once, do not change {@link #identifier} ever as it is the sole identifier for these items.
	 * Additionally, do not change the numeric declaration number, as it is used to generate the {@link UUID} for {@link AttributeModifier}s.
	 */
	protected BeanItem(final int numeric, @NonNull final String identifier, String name, ItemStack item, ItemRarity rarity, int modelDataInt, int maxDurability) {
		if (usedNumerics.contains(numeric))
			throw new IllegalArgumentException("The numeric value '"+numeric+"' was already used to declare a different item.");
		
		usedNumerics.add(numeric);
		itemsByName.put(identifier, this);
		
		this.identifier = identifier; // don't change.
		this.bytes = ByteBuffer.allocate(16).putInt(numeric).array();
		this.displayName = Component.text(name);
		this.material = item.getType();
		this.defaultRarity = rarity;
		this.maxDurability = maxDurability == 0 ? material.getMaxDurability() : maxDurability;
		
		ItemMeta meta = item.getItemMeta();
		meta.setCustomModelData(modelDataInt);
		PersistentDataContainer container = meta.getPersistentDataContainer();
		container.set(KEY_ID, PersistentDataType.STRING, identifier);
		
		if (maxDurability > 0) {
			container.set(KEY_DURABILITY, PersistentDataType.INTEGER, maxDurability);
			container.set(KEY_MAX_DURABILITY, PersistentDataType.INTEGER, maxDurability);
		}
		
		item.setItemMeta(meta);
		
		this.originalStack = item;
		this.enchantable = true;
		this.repairable = true;
		this.reforgable = true;
		this.canRarityChange = true;
		this.customModelData = modelDataInt;
	}

	/**
	 * Handles the given {@link BlockBreakEvent} once initial Region checks have been performed.
	 * Called from the BlockListener class.
	 */
	public void onBlockMined(BlockBreakEvent e) {
	}
	
	public void onEntityAttack(EntityDamageByEntityEvent e) {
	}

	/**
	 * Handles the given {@link PlayerInteractEvent} after {@link BeanBlock#onBlockInteract(PlayerInteractEvent)}
	 * is checked for in the PlayerListener class.
	 * @return True if the item handles the event.
	 */
	public boolean onInteract(PlayerInteractEvent e) {
		return false;
	}
	
	/**
	 * Handles the given {@link PlayerInteractEntityEvent}.
	 * Called from the PlayerListener class.
	 * @return True if the item handles the event.
	 */
	public boolean onEntityInteract(PlayerInteractEntityEvent e) {
		return e.isCancelled();
	}
	
	protected List<TextComponent> getCustomLore(ItemStack item) {
		return getDefaultLore();
	}
	
	protected byte[] getUniqueBytes() {
		return bytes;
	}
	
	protected UUID getUniqueId(Attribute attribute) {
		byte[] newBytes = getUniqueBytes();
		newBytes[15] = (byte) getAttributeId(attribute);
		return UUID.nameUUIDFromBytes(newBytes);
	}

	// Do not change these, only add on with future attributes, safer than ordinal.
	protected static int getAttributeId(Attribute attribute) {
		return switch(attribute) {
			case GENERIC_MAX_HEALTH -> 0;
			case GENERIC_FOLLOW_RANGE -> 1;
			case GENERIC_KNOCKBACK_RESISTANCE -> 2;
			case GENERIC_MOVEMENT_SPEED -> 3;
			case GENERIC_FLYING_SPEED -> 4;
			case GENERIC_ATTACK_DAMAGE -> 5;
			case GENERIC_ATTACK_KNOCKBACK -> 6;
			case GENERIC_ATTACK_SPEED -> 7;
			case GENERIC_ARMOR -> 8;
			case GENERIC_ARMOR_TOUGHNESS -> 9;
			case GENERIC_LUCK -> 10;
			case HORSE_JUMP_STRENGTH -> 11;
			case ZOMBIE_SPAWN_REINFORCEMENTS -> 12;
			case GENERIC_MAX_ABSORPTION -> 13;
		};
	}

	public static String getAttributeString(Attribute attribute) {
		return switch(attribute) {
			case GENERIC_MAX_HEALTH -> "Health";
			case GENERIC_KNOCKBACK_RESISTANCE -> "KB. Resistance";
			case GENERIC_MOVEMENT_SPEED -> "Speed";
			case GENERIC_FLYING_SPEED -> "Flight Speed";
			case GENERIC_ATTACK_DAMAGE -> "Damage";
			case GENERIC_ATTACK_KNOCKBACK -> "Knockback";
			case GENERIC_ATTACK_SPEED -> "Attack Speed";
			case GENERIC_ARMOR -> "Defense";
			case GENERIC_ARMOR_TOUGHNESS -> "Toughness";
			case GENERIC_LUCK -> "Luck";
			case HORSE_JUMP_STRENGTH -> "Horse Jump";
			default -> "";
		};
	}

	public static double getAttributeValue(@NotNull ItemStack item, @NotNull Attribute attribute) {
		DirtyDouble dirtyDouble = new DirtyDouble(0);
		ItemMeta meta = item.getItemMeta();

		ItemAttributes attributes = ItemAttributes.fromItem(item);
		dirtyDouble.setValue(attributes.getAttribute(attribute));

		if (!meta.hasAttributeModifiers()) return dirtyDouble.getValue();

		Collection<AttributeModifier> modifiers = meta.getAttributeModifiers(attribute);
		if (modifiers == null || modifiers.isEmpty()) return attributes.getAttribute(attribute);

		modifiers.forEach(attributeModifier -> {
			switch (attributeModifier.getOperation()) {
				case ADD_NUMBER -> dirtyDouble.addToValue(attributeModifier.getAmount());
				case ADD_SCALAR -> dirtyDouble.addToValue(dirtyDouble.getValue() * attributeModifier.getAmount());
				case MULTIPLY_SCALAR_1 -> dirtyDouble.addToValue(dirtyDouble.getValue() * (1 + attributeModifier.getAmount()));
			}
		});

		return dirtyDouble.getValue() - attributes.getAttribute(attribute); // Negate the base attribute value from modified versions
	}

	/**
	 * Add the {@link BEnchantment#FAKE_GLOW} enchantment to the original {@link ItemStack}.
	 * @return this
	 */
	protected BeanItem addGlow() {
		originalStack.addUnsafeEnchantment(BEnchantment.FAKE_GLOW, 1);
		return this;
	}

	/**
	 * Grabs the list of default enchantments placed on the original {@link ItemStack}.
	 * @return An unmodifiable copy of the default enchantments.
	 */
	public Map<Enchantment, Integer> getDefaultEnchantments() {
		return Map.copyOf(originalStack.getEnchantments());
	}

	/**
	 * @return Whether
	 */
	public boolean hasDefaultEnchantments() {
		return !originalStack.getEnchantments().isEmpty();
	}

	/**
	 * Toggles whether this item will be tracked when crafted by a player.
	 * However, this will also force the maximum stack size to become 1.<br><br>
	 * The item will not be given tracking if spawned in by an Administrator.
	 * @return this
	 */
	protected BeanItem setTrackCreation(boolean trackCreation) {
		this.trackCreation = trackCreation;
		return this;
	}

	protected BeanItem addAttribute(Attribute attribute, double value, EquipmentSlot slot) {
		this.baseAttributes.put(attribute, new AttributeModifier(getUniqueId(attribute), attribute.translationKey(), value, Operation.ADD_NUMBER, slot));
		return this;
	}
	
	protected BeanItem setDefaultLore(TextComponent...lore) {
		final int size = lore.length;
		for (int x = -1; ++x < size;)
			this.baseLore.add(lore[x]);
		return this;
	}

	protected BeanItem setDefaultLore(List<TextComponent> components) {
		this.baseLore.addAll(components);
		return this;
	}
	
	protected List<TextComponent> getDefaultLore() {
		return baseLore;
	}
	
	public Multimap<Attribute, AttributeModifier> getAttributes() {
		return baseAttributes;
	}

	public static boolean isFullyRepaired(ItemStack item) {
		return getDurability(item) >= getMaxDurability(item);
	}

	public static ItemStack reduceItemDurabilityBy(ItemStack item, int amt) {
		int dura = getDurability(item);
		return setDurability(item, dura - amt);
	}

	public static ItemStack addDurability(ItemStack item, int amt) {
		int dura = getDurability(item);
		return setDurability(item, Math.min(getMaxDurability(item), dura + amt));
	}
	
	public static ItemStack setDurability(ItemStack item, int newDura) {
		return setDurability(item, newDura, -1);
	}
	
	public static int getDurability(ItemStack item) {
		final ItemMeta meta = item.getItemMeta();
		final PersistentDataContainer container = meta.getPersistentDataContainer();
		return container.getOrDefault(KEY_DURABILITY, PersistentDataType.INTEGER, item.getItemMeta() instanceof Damageable ? (item.getType().getMaxDurability() - ((Damageable)item.getItemMeta()).getDamage()) : 0);
	}

	/**
	 * Updates the durability of the item as well as the lore.
	 */
	public static ItemStack setDurability(ItemStack item, int newDura, int newMaxDura) {
		if (item.getType().getMaxDurability() < 1) return item;
		
		if (newDura < 0) {
			item.setAmount(item.getAmount() - 1);
			Main.getInstance().getItemTrackingManager().incrementDemanifestationCount(item, DemanifestationReason.TOOL_BREAK, 1);
			return item;
		}

		List<Component> lore = item.lore();

		// Every single durable item in the game WILL have lore, so if it doesn't, it just hasn't been formatted yet.
		if (lore == null)
			return BeanItem.formatItem(item);
		
		ItemMeta meta = item.getItemMeta();
		float base_maxDura = item.getType().getMaxDurability();
		
		final boolean updateMax = newMaxDura > 0;
		final int maxDura = updateMax ? newMaxDura : getMaxDurability(item);
		if (updateMax)
			meta.getPersistentDataContainer().set(KEY_MAX_DURABILITY, PersistentDataType.INTEGER, maxDura);

		if (newDura > maxDura)
			newDura = maxDura;

		meta.getPersistentDataContainer().set(KEY_DURABILITY, PersistentDataType.INTEGER, newDura);

		// Create a transition of durability colour from White -> Yellow -> Red
		int colourInt;
		float percent = (float)newDura / (float)maxDura; // 1 - 0

		// White to Yellow
		if (percent >= 0.5)
			colourInt = ~(0xFF) | (int) ((percent * 2) * 122d);
		else
			colourInt = 0xffffff55 & ~(0xFF << 8) | (int)((percent * 2) * 200d) << 8;

		TextColor durabilityColour = TextColor.color(colourInt);
		lore.set(lore.size() - 1,
				Component.text("Durability: ", NamedTextColor.GRAY).append(Component.text(newDura, durabilityColour).append(Component.text("/", NamedTextColor.GRAY)
				.append(Component.text(maxDura, NamedTextColor.WHITE)))).decoration(TextDecoration.ITALIC, false));
		meta.lore(lore);
		Damageable metad = (Damageable) meta;
		int newDmg = (int) (newDura == 0 ? base_maxDura : (base_maxDura - (((float)newDura/(float)maxDura) * base_maxDura)));

		// If the item is damaged AT ALL, make sure there is at least a damage bar rendered on the item, even if it's such a small amount to where the above math ignores it.
		if (newDmg == 0 && percent < 1)
			newDmg = 1;

		metad.setDamage(newDmg);
		item.setItemMeta(meta);
		
		return item;
	}
	
	public static int getMaxDurability(ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		PersistentDataContainer container = meta.getPersistentDataContainer();
		float base_maxDura = item.getType().getMaxDurability();
		if (!container.has(KEY_DURABILITY, PersistentDataType.INTEGER))
			container.set(KEY_DURABILITY, PersistentDataType.INTEGER, ((Damageable)meta).getDamage());
		if (!container.has(KEY_MAX_DURABILITY, PersistentDataType.INTEGER)) {
			final BeanItem custom = from(item);
			final int maxDura = custom == null ? (int)base_maxDura : custom.getMaxDurability();
			container.set(KEY_MAX_DURABILITY, PersistentDataType.INTEGER, maxDura);
		}
		return container.get(KEY_MAX_DURABILITY, PersistentDataType.INTEGER);
	}
	
	private static ArrayList<Component> doVanillaLore(ItemStack item) {
		final ArrayList<Component> lore = new ArrayList<>();
		final Material material = item.getType();

		// Enchanted Books
		if (material == Material.ENCHANTED_BOOK) {
			EnchantmentStorageMeta bMeta = (EnchantmentStorageMeta) item.getItemMeta();
			Map<Enchantment, Integer> enchants = bMeta.getStoredEnchants();
			int size = enchants.size();
			int totalRunicUsage = 0;
			int totalExperienceCost = 0;
			boolean hideInfo = size > 5;

			for (Entry<Enchantment, Integer> enchant : enchants.entrySet()) {
				BEnchantment bEnchantment = BEnchantment.from(enchant.getKey());
				if (bEnchantment.isHiddenFromLore()) continue;
				lore.add(Component.text(" • ", NamedTextColor.GRAY).append(bEnchantment.displayName(enchant.getValue())).decoration(TextDecoration.ITALIC, false));
				totalExperienceCost += bEnchantment.getExperienceCost(enchant.getValue());
				totalRunicUsage += bEnchantment.getRunicValue(enchant.getValue());
				if (!hideInfo)
					lore.addAll(bEnchantment.getLore(enchant.getValue()));
			}

			lore.add(Component.empty());
			lore.add(Component.text(" • Base Cost: ", NamedTextColor.GRAY).append(Component.text(totalExperienceCost + " \u25CE Experience Levels", BeanColor.EXPERIENCE)).decoration(TextDecoration.ITALIC, false));
			lore.add(Component.text(" • Requires: ", NamedTextColor.GRAY).append(Component.text(totalRunicUsage + " \u269D Runic Capacity", BeanColor.ENCHANT)).decoration(TextDecoration.ITALIC, false));

		}
		// Shulker Boxes
		else if (material.name().endsWith("SHULKER_BOX")) {
			BlockStateMeta bsm = (BlockStateMeta) item.getItemMeta();
			ShulkerBox box = (ShulkerBox) bsm.getBlockState();
			int x = 0;
			for (ItemStack i : box.getInventory()) {
				if (i == null) continue;
				if (x++ > 2) continue;
				lore.add(Component.text(" • " + i.getAmount() + "x ", NamedTextColor.GRAY).append(i.getItemMeta().hasDisplayName() ? i.getItemMeta().displayName() : Component.translatable(i, NamedTextColor.WHITE)).decoration(TextDecoration.ITALIC, false));
			}
			
			if (x > 3)
				lore.add(Component.text("And " + (x-3) + " more...", NamedTextColor.GRAY));
		}
		else if (material == Material.ENCHANTING_TABLE && item.hasItemMeta()) {
			PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
			byte level = pdc.getOrDefault(BeanGuiEnchantingTable.KEY_LAPIS_LEVEL, PersistentDataType.BYTE, (byte)0);
			if (level > 0)
				lore.add(Component.text(" • Compartment Size: ", NamedTextColor.GRAY).append(Component.text(BeanGuiEnchantingTable.getLapisStorageTitle(level), NamedTextColor.YELLOW)).decoration(TextDecoration.ITALIC, false));
		}

		return lore;
	}
	
	public static ItemStack formatItem(ItemStack item) {
		if (item == null || item.getType() == Material.AIR) return new ItemStack(Material.AIR);
		// TODO: TEMPORARY
		Main.getInstance().getEnchantmentManager().replaceEnchantments(item, false);

		ItemMeta meta = item.getItemMeta();
		
		ArrayList<Component> lore = doVanillaLore(item);
		final BeanItem custom = from(item);

		if (custom != null)
			meta.setCustomModelData(custom.getCustomModelData());

		if (!lore.isEmpty())
			meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
		
		ItemRarity rarity = getItemRarity(item);
		Map<Enchantment, Integer> enchants = item.getEnchantments();
		
		final Component rarityString = getRarityString(rarity, item);
		final boolean shouldFormatNameRarity = rarityString != null;
		
		if (custom != null)
			meta.setAttributeModifiers(custom.getAttributes());

		final int refinementLevel = BItemDurable.getRefinementTier(item);
		final ItemAttributes ia = ItemAttributes.fromItem(item);

		/*if (refinementLevel > 0) {
			// % damage increase per level for any item
			Attribute attribute = Attribute.GENERIC_ATTACK_DAMAGE;
			double value = getAttributeValue(item, attribute);
			if (value > 0) {
				AttributeModifier modifier = new AttributeModifier(refinementAttributeUUIDs.get(attribute), attribute.translationKey(), value + ((value / 10) * refinementLevel), Operation.ADD_NUMBER);
//				meta.removeAttributeModifier(attribute, modifier);
				meta.addAttributeModifier(attribute, modifier);
			}
		}*/

		final boolean att1 = ia != ItemAttributes.NIL && ia != ItemAttributes.FISHING_ROD;
		final boolean att2 = meta.hasAttributeModifiers();
		if (att1 || att2) {
			final LinkedHashMap<String, Double> modifiers = new LinkedHashMap<>();
			final LinkedHashMap<String, Component> modsuffix = new LinkedHashMap<>();
			meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

			for (Attribute attribute : Attribute.values()) {
				double val = getAttributeValue(item, attribute);
				if (val == 0) continue;

				switch (attribute) {
					case GENERIC_MOVEMENT_SPEED -> val *= 1000; // 0.001 = 1
					case GENERIC_LUCK -> val *= 20;
					default -> {}
				}

				modifiers.put(getAttributeString(attribute), val);
			}
			/*
			if (att1) {
				if (ia.isTool()) {
					modifiers.put(getAttributeString(Attribute.GENERIC_ATTACK_DAMAGE), ia.getAttackDamage());
					modifiers.put(getAttributeString(Attribute.GENERIC_ATTACK_SPEED), ia.getAttackSpeed());
				} else {
					modifiers.put(getAttributeString(Attribute.GENERIC_ARMOR), ia.getDefensePoints());
					modifiers.put(getAttributeString(Attribute.GENERIC_ARMOR_TOUGHNESS), ia.getArmourToughness());
					modifiers.put(getAttributeString(Attribute.GENERIC_KNOCKBACK_RESISTANCE), ia.getKnockbackResistance());
				}
			}
			
			if (att2) {
				List<Attribute> overridesBase = new ArrayList<>();

				for (Entry<Attribute, AttributeModifier> entry : meta.getAttributeModifiers().entries()) {
					AttributeModifier am = entry.getValue();
					//Operation o = am.getOperation();
					Double d = am.getAmount();
					String attString = getAttributeString(entry.getKey());

					// The first modifier of each type overrides the base item values.
					if (!overridesBase.contains(entry.getKey())) {
						modifiers.remove(attString);
						overridesBase.add(entry.getKey());
					}

					switch (entry.getKey()) {
						case GENERIC_ARMOR, GENERIC_MAX_HEALTH -> modifiers.put(attString, modifiers.getOrDefault(attString, 0.0) + d);
						case GENERIC_ATTACK_DAMAGE -> modifiers.put(attString, modifiers.getOrDefault(attString, 1.0) + d); // default 1
						case GENERIC_MOVEMENT_SPEED -> modifiers.put(attString, modifiers.getOrDefault(attString, 0.0) + d * 1000); // 0.001 is 1 move speed
						case GENERIC_LUCK -> modifiers.put(attString, modifiers.getOrDefault(attString, 0.0) + d * 20);
						case GENERIC_ATTACK_SPEED -> modifiers.put(attString, modifiers.getOrDefault(attString, 4.0) + d); // default 4
						default -> {
						}
					}
				}
			}
			*/

			if (item.getItemMeta().hasEnchant(Enchantment.DAMAGE_ALL)) {
				double x = ((item.getItemMeta().getEnchantLevel(Enchantment.DAMAGE_ALL)+1) * 0.5D);
				modifiers.put("Damage", modifiers.getOrDefault("Damage", 0.0) + x);
//				modsuffix.put("Damage", Component.text(" ("+(x>0?"+"+x:x)+")").color(BeanColor.ENCHANT));
			}
			
			if (item.getItemMeta().hasEnchant(Enchantment.DAMAGE_UNDEAD)) {
				double x = (item.getItemMeta().getEnchantLevel(Enchantment.DAMAGE_UNDEAD) * 2.5D);
				modifiers.put("Undead Damage", modifiers.getOrDefault("Damage", 0.0) + x);
//				modsuffix.put("Undead Damage", Component.text(" ("+(x>0?"+"+x:x)+")").color(BeanColor.ENCHANT));
			}
			
			if (item.getItemMeta().hasEnchant(Enchantment.DAMAGE_ARTHROPODS)) {
				double x = (item.getItemMeta().getEnchantLevel(Enchantment.DAMAGE_ARTHROPODS) * 2.5D);
				modifiers.put("Arthro Damage", modifiers.getOrDefault("Damage", 0.0) + x);
//				modsuffix.put("Arthro Damage", Component.text(" ("+(x>0?"+"+x:x)+")").color(BeanColor.ENCHANT));
			}
			
			// Write it
			for (Entry<String, Double> modifier : modifiers.entrySet()) {
				final double val = modifier.getValue();
				
				if (modifier.getValue() != 0)
					lore.add(Component.text("\u25C8 "+modifier.getKey()+": ").color(rarity.getAttributeColour())
							.append(Component.text(df.format(val)).color(NamedTextColor.WHITE))
							.append(modsuffix.getOrDefault(modifier.getKey(), Component.text("")))
							.decoration(TextDecoration.ITALIC, false));
			}
			lore.add(Component.empty());
		}
		
		// Hide stuff
		if (meta instanceof LeatherArmorMeta) {
			if (custom != null && custom.getOriginalStack() != null)
				((LeatherArmorMeta)meta).setColor(((LeatherArmorMeta)custom.getOriginalStack().getItemMeta()).getColor());
			meta.addItemFlags(ItemFlag.HIDE_DYE);
		}
		
		if (!enchants.isEmpty() || shouldFormatNameRarity)
			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ITEM_SPECIFICS, ItemFlag.HIDE_UNBREAKABLE);
		
		// Enchantment Lore
		if (!enchants.isEmpty()) {
			Map<BEnchantment, Integer> burdens = new LinkedHashMap<>();
			for (Entry<Enchantment, Integer> enchant : enchants.entrySet()) {
				BEnchantment bEnchantment = BEnchantment.from(enchant.getKey());
				if (bEnchantment.isHiddenFromLore()) continue;

				if (bEnchantment.isCursed())
					burdens.put(bEnchantment, enchant.getValue());
				else
					lore.add(Component.text("\u269D ").color(TextColor.color(0x99CCCC)).append(bEnchantment.displayName(enchant.getValue()).color(enchant.getKey().getMaxLevel() < enchant.getValue() ? BeanColor.ENCHANT_OP : BeanColor.ENCHANT)).decoration(TextDecoration.ITALIC, false));
			}
			
			// Do Burdens after
			for (Entry<BEnchantment, Integer> burden : burdens.entrySet())
				lore.add(Component.text("\u2623 ").color(TextColor.color(0xff7799)).append(burden.getKey().displayName(burden.getValue()).color(BeanColor.ENCHANT_BURDEN)).decoration(TextDecoration.ITALIC, false));
		}
		
		// Custom Lore
		if (custom != null) {
			final List<TextComponent> customLore = custom.getCustomLore(item);
			
			if (customLore != null && !customLore.isEmpty()) {
				if (!enchants.isEmpty())
					lore.add(Component.empty());
				lore.addAll(customLore);
			}
		}

		// Rarity String at the Bottom
		if (shouldFormatNameRarity) {
			if (lore.isEmpty() || !(lore.get(lore.size()-1).equals(Component.empty())))
				lore.add(Component.empty());

			if (refinementLevel > 0)
				lore.add(Component.text("\u2692 Tier " + Utils.toRoman(refinementLevel) + " \u2692", TextColor.color((rarity.getColour().value() & 0xfefefe) >> 1)).decoration(TextDecoration.ITALIC, false));
			// Perhaps Astral Refinement or something can use this format
			//lore.add(Component.text("•.·*\u2727° • Tier " + Utils.toRoman(refineTier) + " • °\u2727*·.•", BeanColor.REFINEMENT).decoration(TextDecoration.ITALIC, false));
			lore.add(getRarityString(rarity, item));
		}
		
		// Display Name
		if (meta.hasDisplayName()) {
			meta.displayName(Objects.requireNonNull(meta.displayName()).color(rarity.getColour()).decoration(TextDecoration.ITALIC, false));
			
			if (hasBeenRenamed(item))
				lore.add(0, custom != null ? custom.getDisplayName().color(NamedTextColor.DARK_GRAY) : Component.translatable(item, NamedTextColor.DARK_GRAY));
		} else if (custom != null) {
			meta.displayName(custom.getDisplayName().color(rarity.getColour()).decoration(TextDecoration.ITALIC, false));
		} else if (rarity != ItemRarity.COMMON || item.getI18NDisplayName().contains("§")) { // Not Common or it's a vanilla item with custom colour (e.g. enchanted book)
			meta.displayName(Component.translatable(item, rarity.getColour()).decoration(TextDecoration.ITALIC, false));
		}

		if (custom instanceof BItemDurable || item.getType().getMaxDurability() > 0) { // lore will never be empty
			lore.add(Component.empty());
			meta.lore(lore);
			item.setItemMeta(meta);
			recalculateMaxDurability(item);
			return reduceItemDurabilityBy(item, 0);
		}

		meta.lore(lore.isEmpty() ? null : lore);
		item.setItemMeta(meta);
		
		return item;
	}

	/**
	 * Checks the {@link ItemStack}'s display name to determine if it has been renamed.
	 * This also takes into account {@link BeanItem}s and their display names.
	 * @param item The ItemStack to check.
	 * @return If this ItemStack has been renamed.
	 */
	public static boolean hasBeenRenamed(ItemStack item) {
		if (item == null) return false;

		ItemMeta meta = item.getItemMeta();
		Component name = meta.displayName();

		if (name == null) return false; // Check if the item even has a display name.
		if (meta.displayName() instanceof TranslatableComponent) return false; // Renamed items are always TextComponents.

		BeanItem custom = BeanItem.from(item); // If it's a custom item and has the same name
		if (custom != null && ((TextComponent)name).content().equals(custom.getDisplayName().content())) return false;

		if (((TextComponent)name).content().equals(item.getI18NDisplayName())) { // If the item's display name is the same as its English name, fix.
			meta.displayName(null);
			item.setItemMeta(meta);
			return false;
		}
		return true;
	}

	/**
	 * Returns the total amount of Runic Energy used on the {@link ItemStack}.
	 * @return Cumulative value of Enchantment runic values on the item. Or 0 if null.
	 */
	public static int getRunicExpenses(ItemStack item) {
		if (item == null) return 0;

		int runicUsed = 0;
		for (Map.Entry<Enchantment, Integer> entry : item.getEnchantments().entrySet()) {
			BEnchantment enchant = BEnchantment.from(entry.getKey());
			runicUsed += enchant.getRunicValue(entry.getValue());
		}

		return runicUsed;
	}

	/**
	 * Returns the Base Runic Capacity of the {@link ItemStack}.
	 * @return runic capacity. Or 0 if null.
	 */
	public static int getBaseRunicCapacity(ItemStack item) {
		if (item == null) return 0;
		if (item.getType() == Material.ENCHANTED_BOOK) return 99999;
		int runicCapacity = 15;

		final BeanItem custom = from(item);

		// Grab base Runic Capacity
		if (custom != null) {
			runicCapacity = custom.getBaseRunicCapacity();
		} else {
			ItemAttributes attributes = ItemAttributes.fromItem(item);
			if (attributes != null)
				runicCapacity = attributes.getRunicCapacity();
		}

		// Add Refinement Bonuses
		int tier = BItemDurable.getRefinementTier(item);
		if (tier > 0)
			runicCapacity += Math.floorDiv((tier + 1), 2);

		return runicCapacity;
	}

	/**
	 * Returns the current Runic Capacity of the {@link ItemStack}, calculating existing enchantment runic costs etc.
	 * @return runic capacity. Or 0 if null.
	 */
	public static int getRunicCapacity(ItemStack item) {
		return getBaseRunicCapacity(item) - getRunicExpenses(item);
	}

	@Nullable
	protected String getRarityString() {
		return null;
	}

	protected static Component getRarityString(ItemRarity rarity, ItemStack item) {
		final Material m = item.getType();
		final BeanItem bi = BeanItem.from(item);
		
		if (bi != null && bi.getRarityString() != null)
			return rarity.toComponent().append(Component.text(" " + bi.getRarityString()));
			
		if (m.toString().endsWith("PICKAXE"))
			return rarity.toComponent().append(Component.text(" Pickaxe"));
		if (m.toString().endsWith("SWORD"))
			return rarity.toComponent().append(Component.text(" Sword"));
		if (m.toString().endsWith("AXE"))
			return rarity.toComponent().append(Component.text(" Axe"));
		if (m.toString().endsWith("SHOVEL"))
			return rarity.toComponent().append(Component.text(" Shovel"));
		if (m.toString().endsWith("HOE"))
			return rarity.toComponent().append(Component.text(" Hoe"));
		if (m.toString().endsWith("HELMET"))
			return rarity.toComponent().append(Component.text(" Helmet"));
		if (m.toString().endsWith("CHESTPLATE"))
			return rarity.toComponent().append(Component.text(" Chestplate"));
		if (m.toString().endsWith("LEGGINGS"))
			return rarity.toComponent().append(Component.text(" Leggings"));
		if (m.toString().endsWith("BOOTS"))
			return rarity.toComponent().append(Component.text(" Boots"));
		if (m == Material.BOW)
			return rarity.toComponent().append(Component.text(" Bow"));
		if (m == Material.CROSSBOW)
			return rarity.toComponent().append(Component.text(" Crossbow"));
		if (m == Material.TRIDENT)
			return rarity.toComponent().append(Component.text(" Trident"));
		if (m == Material.ENCHANTED_BOOK)
			return rarity.toComponent().append(Component.text(" Book"));
		if (m == Material.FISHING_ROD)
			return rarity.toComponent().append(Component.text(" Rod"));
		if (m == Material.SHIELD)
			return rarity.toComponent().append(Component.text(" Shield"));
		if (m == Material.ELYTRA)
			return rarity.toComponent().append(Component.text(" Elytra"));
		return null;
	}
	
	public static ItemRarity getItemRarity(ItemStack item) {
		if (item.getType() == Material.ENCHANTED_BOOK) {
			EnchantmentStorageMeta bmeta = (EnchantmentStorageMeta) item.getItemMeta();
			ItemRarity current = ItemRarity.COMMON;
			for (Entry<Enchantment, Integer> enchant : bmeta.getStoredEnchants().entrySet()) {
				ItemRarity rarity = BEnchantment.from(enchant.getKey()).getItemRarity(enchant.getValue());
				if (rarity.ordinal() > current.ordinal())
					current = rarity;
			}
			return current;
		}

		final BeanItem custom = from(item);
		ItemRarity rarity = (custom != null ? custom.getDefaultRarity() : vanillaRarities.getOrDefault(item.getType(), ItemRarity.COMMON));
		if (BItemDurable.getRefinementTier(item) >= 15)
			rarity = rarity.upOne();
		return rarity;
	}

	/**
	 * Calls a {@link Function} from an {@link ItemStack} if it's a type of {@link BeanItem}.
	 * @param item The ItemStack to check
	 * @param function The function to execute
	 * @return If the item is not a BeanItem, false. Otherwise, the result of the function.
	 */
	public static boolean func(ItemStack item, Function<BeanItem, Boolean> function) {
		BeanItem custom = from(item);
		return custom != null && function.apply(custom);
	}

	/**
	 * Calls a {@link Function} from an {@link ItemStack} if it's a type of {@link BeanItem}.
	 * @param item The ItemStack to check
	 * @param consumer The consumer to execute
	 * @return The BeanItem or null.
	 */
	@Nullable
	public static BeanItem from(ItemStack item, Consumer<BeanItem> consumer) {
		BeanItem custom = from(item);
		if (custom != null)
			consumer.accept(custom);
		return custom;
	}

	/**
	 * Calls a {@link Function} from an {@link ItemStack} if it's a type of {@link BeanItem}.
	 * @param item The ItemStack to check
	 * @param <T> The type of {@link BeanItem} class to become.
	 * @param consumer The consumer to execute
	 * @return The BeanItem class if it's an instanceof, or null.
	 */
	@Nullable
	public static <T extends BeanItem> T from(ItemStack item, Class<T> clazz, Consumer<T> consumer) {
		BeanItem custom = from(item);
		if (clazz.isInstance(custom)) {
			T tCustom = clazz.cast(custom);
			if (consumer != null)
				consumer.accept(tCustom);
			return tCustom;
		}
		return null;
	}

	/**
	 * Grabs the respective type of {@link BeanItem} from the {@link ItemStack} if it's a type of {@link BeanItem}.
	 * @param item The ItemStack to check
	 * @param <T> The type of {@link BeanItem} class to become.
	 * @return The BeanItem class if it's an instanceof, or null.
	 */
	@Nullable
	public static <T extends BeanItem> T from(ItemStack item, Class<T> clazz) {
		return from(item, clazz, null);
	}

	@Nullable
	public static BeanItem from(ItemStack item) {
		if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) return null;

		ItemMeta meta = item.getItemMeta();
		PersistentDataContainer container = meta.getPersistentDataContainer();
		String string = container.get(KEY_ID, PersistentDataType.STRING);
		return string == null ? null : itemsByName.getOrDefault(string, null);
	}
	
	public static BeanItem from(String string) {
		return string == null ? null : itemsByName.getOrDefault(string.toUpperCase(), null);
	}
	
	public static ItemStack convert(ItemStack item, BeanItem bitem) {
		ItemMeta meta = item.getItemMeta();
		PersistentDataContainer container = meta.getPersistentDataContainer();
		container.set(KEY_ID, PersistentDataType.STRING, bitem.getIdentifier());
		item.setItemMeta(meta);
		return formatItem(item);
	}
	
	public static boolean is(ItemStack item, BeanItem bitem) {
		final BeanItem bi = from(item);
		return (bi == null && bitem == null) || (bi != null && bi.is(bitem));
	}

	/**
	 * Recalculate and set the maximum durability for this {@link ItemStack}.
	 */
	public static void recalculateMaxDurability(@NotNull ItemStack item) {
		BeanItem custom = from(item);
		int baseDurability = item.getType().getMaxDurability();
		if (custom instanceof BItemDurable durable)
			baseDurability = durable.getMaxDurability();
		else if (baseDurability <= 0)
			return;

		int refinementDurability = (int) ((double)baseDurability * (0.1 * (BItemDurable.getRefinementTier(item) * 2)));
		final int newDurability = baseDurability + refinementDurability;

		item.editMeta(meta -> {
			PersistentDataContainer pdc = meta.getPersistentDataContainer();
			// TODO: Add a better way of checking for this without having to hard code it here.
			if (!meta.hasEnchant(BEnchantment.REJUVENATING))
				pdc.remove(BEnchantment.KEY_REJUVENATION);

			int rejuvenationDurability = pdc.getOrDefault(BEnchantment.KEY_REJUVENATION, PersistentDataType.SHORT, (short)0);
			pdc.set(KEY_MAX_DURABILITY, PersistentDataType.INTEGER, newDurability + rejuvenationDurability);
		});
	}

	public boolean is(BeanItem item) {
		if (item == null) return false;
		return getIdentifier().equals(item.getIdentifier());
	}
	
	public ItemRarity getDefaultRarity() {
		return defaultRarity;
	}

	public int getMaxDurability() {
		return maxDurability;
	}
	
	public boolean canRarityChange() {
		return canRarityChange;
	}

	public boolean isReforgable() {
		return reforgable;
	}

	public boolean isRepairable() {
		return repairable;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public Material getMaterial() {
		return material;
	}

	public TextComponent getDisplayName() {
		return displayName;
	}

	public boolean isEnchantable() {
		return enchantable;
	}

	public ItemStack getOriginalStack() {
		return originalStack;
	}
	
	public String getIdentifier() {
		return identifier;
	}

	public int getCustomModelData() { return customModelData; }

	public int getBaseRunicCapacity() {
		return 15;
	}

	/**
	 * Use {@link #getTrackedStack(Player, ManifestationReason, int)} if giving an item directly to a player. Otherwise, just make sure to call
	 * {@link ItemTrackingManager#incrementManifestationCount(BeanItem, ManifestationReason, long)}
	 * @return A cloned version of the original item stack.
	 */
	@Nonnull
	public ItemStack getItemStack() {
		return originalStack.clone();
	}

	/**
	 * Gets a cloned version of the original item stack but with tracking variables if {@link BeanItem#trackCreation} is set to true. This
	 * method also calls {@link ItemTrackingManager#incrementManifestationCount(BeanItem, ManifestationReason, long)}.<br><br>
	 * <b>It is imperative that this is only ever called when an item is 100% manifested and is in the world of Bean's Beans for players to use.</b>
	 * @param stackSize Forced between 1 and max for the item type (Always 1 if {@link BeanItem#trackCreation}).
	 * @param creator The creator of this ItemStack.
	 * @return The manifested item.
	 */
	@NotNull
	public ItemStack getTrackedStack(@Nullable Player creator, @NotNull ManifestationReason reason, int stackSize) {
		ItemStack i = getItemStack();
		int maxStackSize = trackCreation ? 1 : i.getMaxStackSize();
		if (stackSize > maxStackSize)
			stackSize = maxStackSize;
		else if (stackSize < 1)
			stackSize = 1;

		ItemTrackingManager manager = Main.getInstance().getItemTrackingManager();
		long newValue = manager.incrementManifestationCount(this, reason, stackSize);

		if (trackCreation) {
			i.editMeta(meta -> {
				PersistentDataContainer pdc = meta.getPersistentDataContainer();
				pdc.set(KEY_CREATION_TIME, PersistentDataType.LONG, System.currentTimeMillis() / 1000L);
				if (creator != null)
					pdc.set(KEY_CREATION_PLAYER, PersistentDataType.INTEGER, PlayerProfile.from(creator).getId());
				pdc.set(KEY_CREATION_IDX, PersistentDataType.LONG, newValue);
			});
		}
		return i;
	}

	public boolean isBlock() {
		return this instanceof BeanBlock;
	}
	
	public Random getRandom() {
		return this.random;
	}
	
	protected static NamespacedKey key(String key) {
        return Main.getInstance().getKey(key);
    }

	/**
	 * @return {@link Material#name()} for normal items, {@link BeanItem#getIdentifier()} for custom items.
	 */
	@NotNull
	public static String getIdentifier(@NotNull ItemStack item) {
		BeanItem custom = from(item);
		return custom != null ? custom.getIdentifier() : item.getType().name();
	}

	public static ItemStack resetItemFormatting(ItemStack item) {
		ItemMeta basicMeta = item.getItemMeta();
		
		if (basicMeta instanceof PotionMeta meta) {
			item = new ItemStack(item.getType(), item.getAmount());
			PotionMeta neww = (PotionMeta) item.getItemMeta();
			neww.setBasePotionData(meta.getBasePotionData());
			item.setItemMeta(neww);
			return item;
		} else if (basicMeta instanceof MapMeta meta) {
			item = new ItemStack(item.getType(), item.getAmount());
			MapMeta neww = (MapMeta) item.getItemMeta();
			if (meta.hasMapView())
				neww.setMapView(meta.getMapView());
			item.setItemMeta(neww);
			return item;
		} else if (basicMeta instanceof CompassMeta meta) {
			item = new ItemStack(item.getType(), item.getAmount());
			CompassMeta neww = (CompassMeta) item.getItemMeta();
			if (meta.hasLodestone())
				neww.setLodestone(meta.getLodestone());
			neww.setLodestoneTracked(meta.isLodestoneTracked());
			item.setItemMeta(neww);
			return item;
		} else if (basicMeta instanceof BannerMeta meta) {
			item = new ItemStack(item.getType(), item.getAmount());
			BannerMeta neww = (BannerMeta) item.getItemMeta();
			neww.setPatterns(meta.getPatterns());
			item.setItemMeta(neww);
			return item;
		} else if (basicMeta instanceof TropicalFishBucketMeta meta) {
			item = new ItemStack(item.getType(), item.getAmount());
			TropicalFishBucketMeta neww = (TropicalFishBucketMeta) item.getItemMeta();
			neww.setBodyColor(meta.getBodyColor());
			neww.setPatternColor(meta.getPatternColor());
			item.setItemMeta(neww);
			return item;
		} else if (item.getType() == Material.BEE_NEST) {
			BlockDataMeta meta = (BlockDataMeta) basicMeta;
			item = new ItemStack(item.getType(), item.getAmount());
			BlockDataMeta neww = (BlockDataMeta) item.getItemMeta();
			if (meta.hasBlockData())
				neww.setBlockData(meta.getBlockData(item.getType()));
			item.setItemMeta(neww);
			return item;
		} else if (basicMeta instanceof BookMeta meta) {
			item = new ItemStack(item.getType(), item.getAmount());
			BookMeta neww = (BookMeta) item.getItemMeta();
			if (meta.hasAuthor())
				neww.setAuthor(meta.getAuthor());
			if (meta.hasGeneration())
				neww.setGeneration(meta.getGeneration());
			if (meta.hasTitle())
				neww.setTitle(meta.getTitle());
			if (meta.hasPages())
				neww.pages(meta.pages());
			item.setItemMeta(neww);
			return item;
		} else {
			return BeanItem.formatItem(item);
		}
	}
	
	public static BeanItem[] values() {
		return items;
	}

	public static int size() {
		return items.length;
	}
	
}
