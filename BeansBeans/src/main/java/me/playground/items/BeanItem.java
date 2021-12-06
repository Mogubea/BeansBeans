package me.playground.items;

import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Nonnull;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.ShulkerBox;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
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

import me.playground.enchants.EnchantmentInfo;
import me.playground.main.Main;
import me.playground.utils.BeanColor;
import me.playground.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class BeanItem {
	private final static DecimalFormat df = new DecimalFormat("0.###");
	private final static HashMap<Material, ItemRarity> baseItemRarities = new HashMap<Material, ItemRarity>() {
		private static final long serialVersionUID = 1018055300807913156L;
	{
		put(Material.SKELETON_SKULL, ItemRarity.UNCOMMON);
		put(Material.CREEPER_HEAD, ItemRarity.UNCOMMON);
		put(Material.PLAYER_HEAD, ItemRarity.UNCOMMON);
		put(Material.ZOMBIE_HEAD, ItemRarity.UNCOMMON);
		put(Material.WITHER_SKELETON_SKULL, ItemRarity.UNCOMMON);
		
		put(Material.ENDER_CHEST, ItemRarity.UNCOMMON);
		put(Material.END_CRYSTAL, ItemRarity.UNCOMMON);
		
		put(Material.DIAMOND_SWORD, ItemRarity.UNCOMMON);
		put(Material.DIAMOND_AXE, ItemRarity.UNCOMMON);
		put(Material.DIAMOND_PICKAXE, ItemRarity.UNCOMMON);
		put(Material.DIAMOND_SHOVEL, ItemRarity.UNCOMMON);
		put(Material.DIAMOND_HOE, ItemRarity.UNCOMMON);
		put(Material.DIAMOND_HELMET, ItemRarity.UNCOMMON);
		put(Material.DIAMOND_CHESTPLATE, ItemRarity.UNCOMMON);
		put(Material.DIAMOND_LEGGINGS, ItemRarity.UNCOMMON);
		put(Material.DIAMOND_BOOTS, ItemRarity.UNCOMMON);
		
		put(Material.CHAINMAIL_HELMET, ItemRarity.UNCOMMON);
		put(Material.CHAINMAIL_CHESTPLATE, ItemRarity.UNCOMMON);
		put(Material.CHAINMAIL_LEGGINGS, ItemRarity.UNCOMMON);
		put(Material.CHAINMAIL_BOOTS, ItemRarity.UNCOMMON);
		
		put(Material.SHULKER_BOX, ItemRarity.RARE);
		put(Material.BLACK_SHULKER_BOX, ItemRarity.RARE);
		put(Material.BLUE_SHULKER_BOX, ItemRarity.RARE);
		put(Material.GREEN_SHULKER_BOX, ItemRarity.RARE);
		put(Material.CYAN_SHULKER_BOX, ItemRarity.RARE);
		put(Material.RED_SHULKER_BOX, ItemRarity.RARE);
		put(Material.PURPLE_SHULKER_BOX, ItemRarity.RARE);
		put(Material.ORANGE_SHULKER_BOX, ItemRarity.RARE);
		put(Material.GRAY_SHULKER_BOX, ItemRarity.RARE);
		put(Material.LIGHT_GRAY_SHULKER_BOX, ItemRarity.RARE);
		put(Material.LIGHT_BLUE_SHULKER_BOX, ItemRarity.RARE);
		put(Material.LIME_SHULKER_BOX, ItemRarity.RARE);
		put(Material.BROWN_SHULKER_BOX, ItemRarity.RARE);
		put(Material.WHITE_SHULKER_BOX, ItemRarity.RARE);
		put(Material.PINK_SHULKER_BOX, ItemRarity.RARE);
		put(Material.YELLOW_SHULKER_BOX, ItemRarity.RARE);
		put(Material.MAGENTA_SHULKER_BOX, ItemRarity.RARE);
		
		put(Material.NETHERITE_AXE, ItemRarity.RARE);
		put(Material.NETHERITE_PICKAXE, ItemRarity.RARE);
		put(Material.NETHERITE_SWORD, ItemRarity.RARE);
		put(Material.NETHERITE_HOE, ItemRarity.RARE);
		put(Material.NETHERITE_SHOVEL, ItemRarity.RARE);
		put(Material.NETHERITE_HELMET, ItemRarity.RARE);
		put(Material.NETHERITE_CHESTPLATE, ItemRarity.RARE);
		put(Material.NETHERITE_LEGGINGS, ItemRarity.RARE);
		put(Material.NETHERITE_BOOTS, ItemRarity.RARE);
		
		put(Material.TRIDENT, ItemRarity.RARE);
		put(Material.ELYTRA, ItemRarity.RARE);
		put(Material.DRAGON_HEAD, ItemRarity.RARE);
		put(Material.BEACON, ItemRarity.RARE);
		put(Material.DEEPSLATE_COAL_ORE, ItemRarity.RARE);
		put(Material.DEEPSLATE_EMERALD_ORE, ItemRarity.RARE);
		
		put(Material.DRAGON_EGG, ItemRarity.EPIC);
		put(Material.TOTEM_OF_UNDYING, ItemRarity.EPIC);
		put(Material.ENCHANTED_GOLDEN_APPLE, ItemRarity.EPIC);
		
		put(Material.END_PORTAL_FRAME, ItemRarity.UNTOUCHABLE);
		put(Material.COMMAND_BLOCK, ItemRarity.UNTOUCHABLE);
		put(Material.COMMAND_BLOCK_MINECART, ItemRarity.UNTOUCHABLE);
	}};
	
	public final static NamespacedKey KEY_ID = key("ID"); // String
	public final static NamespacedKey KEY_DURABILITY = key("DURABILITY"); // Integer
	public final static NamespacedKey KEY_MAX_DURABILITY = key("MAX_DURABILITY"); // Integer
	public final static NamespacedKey KEY_COUNTER = key("COUNTER"); // Integer
	
	private final static HashMap<String, BeanItem> itemsByName = new HashMap<String, BeanItem>();
	private final static ArrayList<Integer> usedNumerics = new ArrayList<Integer>();
	private static BeanItem[] items;
	
	public final static BeanItem FORMATTING_WAND = new BItemDurable(2000000, "FORMATTING_WAND", "Formatting Wand", Material.GOLDEN_AXE, ItemRarity.UNTOUCHABLE, 1, 500) {
		public ArrayList<Component> getCustomLore(ItemStack item) {
			item.getItemMeta().setUnbreakable(true);
			
			final ArrayList<Component> lore = new ArrayList<Component>();
			lore.addAll(Arrays.asList(
					Component.text("\u00a77Right clicking a container will update"),
					Component.text("\u00a77all items inside to the newest format!")));
			return lore;
		}
		
		public void onBlockMined(BlockBreakEvent e) {
			e.setCancelled(true);
		}
		
		public void onInteract(PlayerInteractEvent e) {
			e.setCancelled(true);
			Block b = e.getClickedBlock();
			if (e.getHand() == EquipmentSlot.HAND && e.getAction() == Action.RIGHT_CLICK_BLOCK && b != null && b.getState() instanceof Container) {
				Container c = (Container) b.getState();
				ItemStack[] newInv = c.getInventory().getContents();
				for (int x = 0; x < newInv.length; x++) {
					ItemStack i = newInv[x];
					if (i == null) continue;
					newInv[x] = formatItem(i);
				}
				c.getInventory().setContents(newInv);
			}
		}
	};
	
	public final static BeanItem DEFORMATTING_WAND = new BItemDurable(2000001, "DEFORMATTING_WAND", "Deformatting Wand", Material.NETHERITE_AXE, ItemRarity.UNTOUCHABLE, 1, 500) {
		public ArrayList<Component> getCustomLore(ItemStack item) {
			item.getItemMeta().setUnbreakable(true);
			
			final ArrayList<Component> lore = new ArrayList<Component>();
			lore.addAll(Arrays.asList(
					Component.text("\u00a77Right clicking a container will clear"),
					Component.text("\u00a77all materialistic items inside of formatting!")));
			return lore;
		}
		
		public void onBlockMined(BlockBreakEvent e) {
			e.setCancelled(true);
		}
		
		public void onInteract(PlayerInteractEvent e) {
			e.setCancelled(true);
			Block b = e.getClickedBlock();
			if (e.getHand() == EquipmentSlot.HAND && e.getAction() == Action.RIGHT_CLICK_BLOCK && b != null && b.getState() instanceof Container) {
				Container c = (Container) b.getState();
				ItemStack[] newInv = c.getInventory().getContents();
				for (int x = 0; x < newInv.length; x++) {
					ItemStack i = newInv[x];
					if (i == null) continue;
					newInv[x] = resetItemFormatting(i);
				}
				c.getInventory().setContents(newInv);
			}
		}
	};
	
	public final static BeanItem DEBUG_CAKE = new BeanItem(2000002, "DEBUG_CAKE", "Bug Finder's Cake", Material.CAKE, ItemRarity.IRIDESCENT, 1) {
		public void onInteract(PlayerInteractEvent e) {
			if (e.getHand() == EquipmentSlot.HAND && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
				e.setCancelled(true);
			}
		}
		
		public ArrayList<Component> getCustomLore(ItemStack item) {
			final ArrayList<Component> lore = new ArrayList<Component>();
			lore.addAll(Arrays.asList(
					Component.text("\u00a77A token of appreciation for finding"),
					Component.text("\u00a77and reporting serious bugs! Thanks!")));
			return lore;
		}
	};
	
	public final static BeanItem SHOP_STAND = new BItemShopStand(0, "SHOP_STAND", "Shopping Stand", ItemRarity.RARE);
	
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
	
	public final static BeanItem CATS_PAW = new BItemFishingRodCatsPaw(500, "CATS_PAW", "Cat's Paw", ItemRarity.UNCOMMON, 1, 90);
	
	
	public final static BeanItemHeirloom HL_MOCHI 			= new BItemHeirloomMochi(10000, "HL_MOCHI", "Mochi", ItemRarity.RARE);
	public final static BeanItemHeirloom HL_ANCIENT_SKULL 	= new BItemHeirloomAncientSkull(10001, "HL_ANCIENT_SKULL", "Ancient Skull", ItemRarity.RARE);
	public final static BeanItemHeirloom HL_SHUNGITE 		= new BItemHeirloomShungite(10002, "HL_SHUNGITE", "Lucky Shungite", ItemRarity.UNCOMMON);
	
	public final static BeanItem WOODEN_CRATE = new BItemPackage(19500, "WOODEN_CRATE", "Wooden Crate", ItemRarity.UNCOMMON, "crate_oak", 3, Material.OAK_PLANKS, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjYyMDUxOWI3NDUzNmMxZjg1YjdjN2U1ZTExY2U1YzA1OWMyZmY3NTljYjhkZjI1NGZjN2Y5Y2U3ODFkMjkifX19");
	public final static BeanItem DEEP_OCEAN_CRATE = new BItemPackage(19501, "DEEP_OCEAN_CRATE", "Deep Ocean Crate", ItemRarity.RARE, "crate_deep", 4, Material.DARK_OAK_PLANKS, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTcxMTQwNjVhM2M5NWM1ZDIyNTE4OGFkN2JmZGFhOWI4YjA4NDVkZjRlMzZjMjRiNDUzNDdmZDc0NzBhNyJ9fX0=");
	public final static BeanItem IRON_CRATE = new BItemPackage(19502, "METAL_CRATE", "Metal Crate", ItemRarity.RARE, "crate_iron", 3, Material.IRON_BLOCK, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzFhODE0NWFhY2Y1YzRjNjNmMmQwMjM4OGJmNDcxNGJiMDk2MWRjYjVhZjdlMTU4MTk5YjI1MTgzZWQ4NDFmZCJ9fX0=");
	
	//public final static BeanItem BIRCH_CRATE = new BItemPackage(19502, "BIRCH_CRATE", "Birch Crate", ItemRarity.UNCOMMON, "crate_birch", 3, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2I0ZTQ3OGMxMWNjZGZhNDUzMjk4NzQ3MjQ1ZTA0MjVlYzQyNTFlMTdmYjNlNjdkYmVjMTQxNzZjNjQ3MTcifX19");
	//public final static BeanItem JUNGLE_CRATE = new BItemPackage(19503, "JUNGLE_CRATE", "Jungle Crate", ItemRarity.UNCOMMON, "crate_jungle", 3, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmNmNWIxY2ZlZDFjMjdkZDRjM2JlZjZiOTg0NDk5NDczOTg1MWU0NmIzZmM3ZmRhMWNiYzI1YjgwYWIzYiJ9fX0=");
	//public final static BeanItem DARK_OAK_CRATE = new BItemPackage(19504, "DARK_OAK_CRATE", "Dark Oak Crate", ItemRarity.UNCOMMON, "crate_dark_oak", 3, "");
	
	public final static BeanItem SPIDER_HEAD = new BeanItem(20000, "SPIDER_HEAD", "Spider Head", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzg3YTk2YThjMjNiODNiMzJhNzNkZjA1MWY2Yjg0YzJlZjI0ZDI1YmE0MTkwZGJlNzRmMTExMzg2MjliNWFlZiJ9fX0=", ItemRarity.UNCOMMON);
	public final static BeanItem BLAZE_HEAD = new BeanItem(20001, "BLAZE_HEAD", "Blaze Head", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjc4ZWYyZTRjZjJjNDFhMmQxNGJmZGU5Y2FmZjEwMjE5ZjViMWJmNWIzNWE0OWViNTFjNjQ2Nzg4MmNiNWYwIn19fQ==", ItemRarity.RARE);
	public final static BeanItem PIGLIN_HEAD = new BeanItem(20002, "PIGLIN_HEAD", "Piglin Head", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTBiYzlkYmI0NDA0YjgwMGY4Y2YwMjU2MjIwZmY3NGIwYjcxZGJhOGI2NjYwMGI2NzM0ZjRkNjMzNjE2MThmNSJ9fX0=", ItemRarity.RARE);
	public final static BeanItem PHANTOM_HEAD = new BeanItem(20003, "PHANTOM_HEAD", "Phantom Head", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzQ2ODMwZGE1ZjgzYTNhYWVkODM4YTk5MTU2YWQ3ODFhNzg5Y2ZjZjEzZTI1YmVlZjdmNTRhODZlNGZhNCJ9fX0=", ItemRarity.RARE);
	public final static BeanItem COW_HEAD = new BeanItem(20004, "COW_HEAD", "Cow Head", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2RmYTBhYzM3YmFiYTJhYTI5MGU0ZmFlZTQxOWE2MTNjZDYxMTdmYTU2OGU3MDlkOTAzNzQ3NTNjMDMyZGNiMCJ9fX0=", ItemRarity.UNCOMMON);
	public final static BeanItem PIG_HEAD = new BeanItem(20005, "PIG_HEAD", "Pig Head", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjIxNjY4ZWY3Y2I3OWRkOWMyMmNlM2QxZjNmNGNiNmUyNTU5ODkzYjZkZjRhNDY5NTE0ZTY2N2MxNmFhNCJ9fX0=", ItemRarity.UNCOMMON);
	public final static BeanItem CHICKEN_HEAD = new BeanItem(20006, "CHICKEN_HEAD", "Chicken Head", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTYzODQ2OWE1OTljZWVmNzIwNzUzNzYwMzI0OGE5YWIxMWZmNTkxZmQzNzhiZWE0NzM1YjM0NmE3ZmFlODkzIn19fQ==", ItemRarity.UNCOMMON);
	public final static BeanItem SHEEP_HEAD = new BeanItem(20007, "SHEEP_HEAD", "Sheep Head", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjMxZjljY2M2YjNlMzJlY2YxM2I4YTExYWMyOWNkMzNkMThjOTVmYzczZGI4YTY2YzVkNjU3Y2NiOGJlNzAifX19", ItemRarity.UNCOMMON);
	public final static BeanItem DROWNED_HEAD = new BeanItem(20008, "DROWNED_HEAD", "Drowned Head", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzg0ZGY3OWM0OTEwNGIxOThjZGFkNmQ5OWZkMGQwYmNmMTUzMWM5MmQ0YWI2MjY5ZTQwYjdkM2NiYmI4ZTk4YyJ9fX0=", ItemRarity.UNCOMMON);
	public final static BeanItem ENDERMAN_HEAD = new BeanItem(20009, "ENDERMAN_HEAD", "Enderman Head", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTZjMGIzNmQ1M2ZmZjY5YTQ5YzdkNmYzOTMyZjJiMGZlOTQ4ZTAzMjIyNmQ1ZTgwNDVlYzU4NDA4YTM2ZTk1MSJ9fX0=", ItemRarity.UNCOMMON);
	
	static {
		items = itemsByName.values().toArray(new BeanItem[0]);
	}
	
	protected final byte[]		    bytes;
	protected final TextComponent	displayName;
	protected final String	 		identifier;
	protected final Material  		material;
	protected final ItemRarity 		defaultRarity;
	protected final ItemStack 		originalStack;
	protected final boolean 		enchantable;
	protected final boolean 		repairable;
	protected final boolean 		reforgable;
	protected final boolean 		canRarityChange;
	protected final int				maxDurability;
	protected final Multimap<Attribute, AttributeModifier> baseAttributes = HashMultimap.create();
	protected final ArrayList<Component> baseLore = new ArrayList<Component>();
	protected final Random		    random = Main.getInstance().getRandom();
	
	protected boolean 				enabled = true;
	
	/**
	 * Used for creating an instance of {@link BeanItem} using a skull with a custom skin.
	 */
	protected BeanItem(@NonNull final int numeric, @NonNull final String identifier, String name, String skullBase64, ItemRarity rarity) {
		this(numeric, identifier, name, Utils.getSkullWithCustomSkin(UUID.nameUUIDFromBytes(ByteBuffer.allocate(16).putInt(numeric).array()), skullBase64), rarity, 0, 0);
	}
	
	protected BeanItem(@NonNull final int numeric, @NonNull final String identifier, String name, Material material, ItemRarity rarity, int modelDataInt) {
		this(numeric, identifier, name, new ItemStack(material), rarity, modelDataInt, 0);
	}
	
	protected BeanItem(@NonNull final int numeric, @NonNull final String identifier, String name, ItemStack item, ItemRarity rarity, int modelDataInt) {
		this(numeric, identifier, name, item, rarity, modelDataInt, 0);
	}
	
	protected BeanItem(@NonNull final int numeric, @NonNull final String identifier, String name, Material material, ItemRarity rarity, int modelDataInt, int maxDurability) {
		this(numeric, identifier, name, new ItemStack(material), rarity, modelDataInt, maxDurability);
	}
	
	/**
	 * When the server has been booted on live at least once, do not change {@link #identifier} ever as it is the sole identifier for these items.
	 * Additionally, do not change the numeric declaration number, as it is used to generate the {@link uuid} for {@link AttributeModifier}s.
	 */
	protected BeanItem(@NonNull final int numeric, @NonNull final String identifier, String name, ItemStack item, ItemRarity rarity, int modelDataInt, int maxDurability) {
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
		
		item = initializeItem(item);
		
		ItemMeta meta = item.getItemMeta();
		meta.setCustomModelData(modelDataInt);
		PersistentDataContainer container = meta.getPersistentDataContainer();
		container.set(KEY_ID, PersistentDataType.STRING, identifier);
		
		if (maxDurability > 0) {
			container.set(KEY_DURABILITY, PersistentDataType.INTEGER, maxDurability);
			container.set(KEY_MAX_DURABILITY, PersistentDataType.INTEGER, maxDurability);
		}
		
		item.setItemMeta(meta);
		
		this.originalStack = formatItem(item);
		
		this.enchantable = true;
		this.repairable = true;
		this.reforgable = true;
		this.canRarityChange = true;
	}
	
	public void onBlockMined(BlockBreakEvent e) {
		if (e.isCancelled()) return;
	}
	
	public void onEntityAttack(EntityDamageByEntityEvent e) {
		if (e.isCancelled()) return;
	}
	
	public void onInteract(PlayerInteractEvent e) {
		if (e.useItemInHand() == Result.DENY) return;
	}
	
	protected ArrayList<Component> getCustomLore(ItemStack item) {
		return getDefaultLore();
	}
	
	protected ItemStack initializeItem(ItemStack item) {
		return item;
	}
	
	protected byte[] getUniqueBytes() {
		return bytes;
	}
	
	protected UUID getUniqueId(Attribute attribute) {
		byte[] newBytes = getUniqueBytes();
		newBytes[15] = (byte) attribute.ordinal();
		return UUID.nameUUIDFromBytes(newBytes);
	}
	
	protected BeanItem addAttribute(Attribute attribute, double value, EquipmentSlot slot) {
		this.baseAttributes.put(attribute, new AttributeModifier(getUniqueId(attribute), attribute.translationKey(), value, Operation.ADD_NUMBER, slot));
		formatItem(this.originalStack);
		return this;
	}
	
	protected BeanItem setDefaultLore(Component...lore) {
		final int size = lore.length;
		for (int x = -1; ++x < size;)
			this.baseLore.add(lore[x]);
		formatItem(this.originalStack);
		return this;
	}
	
	protected ArrayList<Component> getDefaultLore() {
		return baseLore;
	}
	
	public Multimap<Attribute, AttributeModifier> getAttributes() {
		return baseAttributes;
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
	
	public static ItemStack setDurability(ItemStack item, int newDura, int newMaxDura) {
		if (item.getType().getMaxDurability() < 1)
			return item;
		
		if (newDura < 0)
			return null;
		
		if (item.lore() == null)
			return BeanItem.formatItem(item);
		
		ItemMeta meta = item.getItemMeta();
		float base_maxDura = item.getType().getMaxDurability();
		ArrayList<Component> lore = item.lore() == null ? new ArrayList<Component>() : (ArrayList<Component>) item.lore();
		
		final boolean updateMax = newMaxDura > 0;
		final int maxDura = updateMax ? newMaxDura : getMaxDurability(item);
		if (updateMax)
			meta.getPersistentDataContainer().set(KEY_MAX_DURABILITY, PersistentDataType.INTEGER, maxDura);
		
		meta.getPersistentDataContainer().set(KEY_DURABILITY, PersistentDataType.INTEGER, newDura);
		
		lore.remove(lore.size() - 1);
		lore.add(Component.text("Durability: ", NamedTextColor.GRAY).append(Component.text(""+newDura, NamedTextColor.WHITE).append(Component.text("/", NamedTextColor.GRAY)
				.append(Component.text(""+maxDura, NamedTextColor.WHITE)))).decoration(TextDecoration.ITALIC, false));
		meta.lore(lore);
		Damageable metad = (Damageable) meta;
		int newDmg = (int) (newDura == 0 ? base_maxDura : (base_maxDura - (((float)newDura/(float)maxDura) * base_maxDura)));
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
		final ArrayList<Component> lore = new ArrayList<Component>();
		final Material material = item.getType();
		if (material.name().endsWith("SHULKER_BOX")) {
			BlockStateMeta bsm = (BlockStateMeta) item.getItemMeta();
			ShulkerBox box = (ShulkerBox) bsm.getBlockState();
			int x = 0;
			for (ItemStack i : box.getInventory()) {
				if (i == null) continue;
				if (x++ > 2) continue;
				lore.add(Component.text("\u00a77" + i.getAmount() + "x ").append(i.displayName()));
			}
			
			if (x > 2)
				lore.add(Component.text("\u00a77\u00a7oAnd " + (x-3) + " more..."));
		}
		return lore;
	}
	
	public static ItemStack formatItem(ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		
		ArrayList<Component> lore = doVanillaLore(item);
		final BeanItem custom = from(item);
		
		ItemRarity rarity = getItemRarity(item);
		Map<Enchantment, Integer> enchants = null;
		
		if (item.getType() == Material.ENCHANTED_BOOK) {
			EnchantmentStorageMeta bmeta = (EnchantmentStorageMeta) meta;
			enchants = bmeta.getStoredEnchants();
			rarity = EnchantmentInfo.rarityOf(enchants);
		} else {
			enchants = item.getEnchantments();
		}
		
		final Component rarityString = getRarityString(rarity, item);
		final boolean shouldFormatNameRarity = !rarity.equals(ItemRarity.COMMON) || !rarityString.equals(rarity.toComponent());
		
		// Display Name
		if (true) {
			if (meta.hasDisplayName()) {
				meta.displayName(meta.displayName().color(rarity.getColour()).decoration(TextDecoration.ITALIC, false));
				if (custom != null) {
					if (!((TextComponent)meta.displayName()).content().equals(custom.getDisplayName().content())) {
						lore.add(custom.getDisplayName().color(NamedTextColor.DARK_GRAY));
					}
				} else if (meta.displayName() instanceof TextComponent) {
					if (!((TextComponent)meta.displayName()).content().equals(item.getI18NDisplayName())) {
						lore.add(Component.text(item.getI18NDisplayName()).color(NamedTextColor.DARK_GRAY));
					}
				}
			} else if (custom != null) {
				meta.displayName(custom.getDisplayName().color(rarity.getColour()).decoration(TextDecoration.ITALIC, false));
			} else {
				meta.displayName(Component.text(item.getI18NDisplayName()).color(rarity.getColour()).decoration(TextDecoration.ITALIC, false));
			}
		}
		
		if (custom != null)
			meta.setAttributeModifiers(custom.getAttributes());
		
		ItemAttributes ia = ItemAttributes.fromItem(item);
		final boolean att1 = ia != ItemAttributes.NIL && ia != ItemAttributes.FISHING_ROD;
		final boolean att2 = meta.hasAttributeModifiers();
		if (att1 || att2) {
			final LinkedHashMap<String, Double> modifiers = new LinkedHashMap<String, Double>();
			final LinkedHashMap<String, Component> modsuffix = new LinkedHashMap<String, Component>();
			meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			
			if (att1) {
				if (ia.isTool()) {
					modifiers.put("Damage", ia.getAttackDamage());
					modifiers.put("Attack Speed", ia.getAttackSpeed());
				} else {
					modifiers.put("Defense", ia.getDefensePoints());
					modifiers.put("Toughness", ia.getArmourToughness());
					modifiers.put("KB. Resistance", ia.getKnockbackResistance());
				}
			}
			
			if (att2) {
				for (Entry<Attribute, AttributeModifier> entry : meta.getAttributeModifiers().entries()) {
					AttributeModifier am = entry.getValue();
					//Operation o = am.getOperation();
					Double d = am.getAmount();
					if (entry.getKey().equals(Attribute.GENERIC_ARMOR))
						modifiers.put("Defense", modifiers.getOrDefault("Defense", 0.0) + d);
					if (entry.getKey().equals(Attribute.GENERIC_MAX_HEALTH))
						modifiers.put("Health", modifiers.getOrDefault("Health", 0.0) + d);
					if (entry.getKey().equals(Attribute.GENERIC_ATTACK_DAMAGE))
						modifiers.put("Damage", modifiers.getOrDefault("Damage", 0.0) + d);
					if (entry.getKey().equals(Attribute.GENERIC_ATTACK_SPEED))
						modifiers.put("Attack Speed", modifiers.getOrDefault("Attack Speed", 0.0) + d);
					if (entry.getKey().equals(Attribute.GENERIC_MOVEMENT_SPEED))
						modifiers.put("Movement Speed", modifiers.getOrDefault("Movement Speed", 0.0) + d * 1000);
					if (entry.getKey().equals(Attribute.GENERIC_LUCK))
						modifiers.put("Luck", modifiers.getOrDefault("Luck", 0.0) + d * 20);
				}
			}
			
			if (item.containsEnchantment(Enchantment.DAMAGE_ALL)) {
				double x = ((item.getEnchantmentLevel(Enchantment.DAMAGE_ALL)+1) * 0.5D);
				modifiers.put("Damage", modifiers.getOrDefault("Damage", 0.0) + x);
				modsuffix.put("Damage", Component.text(" ("+(x>0?"+"+x:x)+")").color(BeanColor.ENCHANT));
			}
			
			if (item.containsEnchantment(Enchantment.DAMAGE_UNDEAD)) {
				double x = (item.getEnchantmentLevel(Enchantment.DAMAGE_UNDEAD) * 2.5D);
				modifiers.put("Undead Damage", modifiers.getOrDefault("Damage", 0.0) + x);
				modsuffix.put("Undead Damage", Component.text(" ("+(x>0?"+"+x:x)+")").color(BeanColor.ENCHANT));
			}
			
			if (item.containsEnchantment(Enchantment.DAMAGE_ARTHROPODS)) {
				double x = (item.getEnchantmentLevel(Enchantment.DAMAGE_ARTHROPODS) * 2.5D);
				modifiers.put("Arthro Damage", modifiers.getOrDefault("Damage", 0.0) + x);
				modsuffix.put("Arthro Damage", Component.text(" ("+(x>0?"+"+x:x)+")").color(BeanColor.ENCHANT));
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
		if (meta instanceof LeatherArmorMeta)
			meta.addItemFlags(ItemFlag.HIDE_DYE);
		
		if (enchants.size() > 0 || shouldFormatNameRarity)
			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_UNBREAKABLE);
		
		// Enchantment Lore
		if (enchants.size() > 0) {
			boolean skipLore = item.getType() != Material.ENCHANTED_BOOK;
			for (Entry<Enchantment, Integer> enchant : enchants.entrySet()) {
				if (enchant.getKey() == Enchantment.BINDING_CURSE || enchant.getKey() == Enchantment.VANISHING_CURSE)
					lore.add(Component.text("\u269D ").color(TextColor.color(0xDF4444)).append(enchant.getKey().displayName(enchant.getValue()).color(TextColor.color(0xff5555))));
				else
					lore.add(Component.text("\u269D ").color(TextColor.color(0x99CCCC)).append(enchant.getKey().displayName(enchant.getValue()).color(enchant.getKey().getMaxLevel() < enchant.getValue() ? BeanColor.ENCHANT_OP : BeanColor.ENCHANT)).decoration(TextDecoration.ITALIC, false));
				if (!skipLore)
					lore.addAll(EnchantmentInfo.loreOf(enchant.getKey(), enchant.getValue()));
			}
		}
		
		// Custom Lore
		if (custom != null) {
			final ArrayList<Component> customLore = custom.getCustomLore(item);
			
			if (customLore != null && customLore.size() > 0) {
				if (enchants.size() > 0)
					lore.add(Component.empty());
				lore.addAll(customLore);
			}
		}
		
		// Rarity String at the Bottom
		if (shouldFormatNameRarity) {
			if (lore.size() == 0 || !(lore.get(lore.size()-1).equals(Component.empty())))
				lore.add(Component.empty());
			lore.add(getRarityString(rarity, item));
		}
		
		
		if (item.getType().getMaxDurability() > 0) { // lore will never be empty
			lore.add(Component.empty());
			meta.lore(lore);
			item.setItemMeta(meta);
			reduceItemDurabilityBy(item, 0);
		} else if (lore.size() > 0) {
			meta.lore(lore);
			item.setItemMeta(meta);
		}
		
		return item;
	}
	
	protected static Component getRarityString(ItemRarity rarity, ItemStack item) {
		final Material m = item.getType();
		final BeanItem bi = BeanItem.from(item);
		
		if (bi != null) {
			if (bi instanceof BeanItemHeirloom)
				return rarity.toComponent().append(Component.text(" Heirloom"));
			if (bi instanceof BItemPackage)
				return rarity.toComponent().append(Component.text(" Crate"));
		}
			
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
		if (m.toString().endsWith("BOW"))
			return rarity.toComponent().append(Component.text(" Bow"));
		if (m == Material.TRIDENT)
			return rarity.toComponent().append(Component.text(" Trident"));
		if (m == Material.ENCHANTED_BOOK)
			return rarity.toComponent().append(Component.text(" Book"));
		if (m == Material.FISHING_ROD)
			return rarity.toComponent().append(Component.text(" Rod"));
		if (m == Material.SHIELD)
			return rarity.toComponent().append(Component.text(" Shield"));
		return rarity.toComponent();
	}
	
	public static ItemRarity getItemRarity(ItemStack item) {
		final BeanItem custom = from(item);
		return (custom != null ? custom.getDefaultRarity() : baseItemRarities.getOrDefault(item.getType(), ItemRarity.COMMON));
	}
	
	public static BeanItem from(ItemStack item) {
		if (item == null || item.getType() == Material.AIR) 
			return null;
		ItemMeta meta = item.getItemMeta();
		PersistentDataContainer container = meta.getPersistentDataContainer();
		String string = container.getOrDefault(KEY_ID, PersistentDataType.STRING, null);
		return string == null ? null : itemsByName.getOrDefault(string, null);
	}
	
	public static BeanItem from(String string) {
		return string == null ? null : itemsByName.getOrDefault(string.toUpperCase(), null);
	}
	
	public static boolean is(ItemStack item, BeanItem bitem) {
		final BeanItem bi = from(item);
		return bi != null && bi.is(bitem);
	}
	
	public boolean is(BeanItem item) {
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
	
	/**
	 * @return A cloned version of the original item stack.
	 */
	@Nonnull
	public ItemStack getItemStack() {
		return originalStack.clone();
	}
	
	public Random getRandom() {
		return this.random;
	}
	
	private static NamespacedKey key(String key) {
        return new NamespacedKey(Main.getInstance(), key);
    }
	
	public static ItemStack resetItemFormatting(ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		ItemRarity rarity = getItemRarity(item);
		
		final Component rarityString = getRarityString(rarity, item);
		final boolean shouldFormatNameRarity = !rarity.equals(ItemRarity.COMMON) || !rarityString.equals(rarity.toComponent());
		
		if (meta instanceof PotionMeta) {
			PotionMeta pm = (PotionMeta) meta;
			item = new ItemStack(item.getType(), item.getAmount());
			PotionMeta neww = (PotionMeta) item.getItemMeta();
			neww.setBasePotionData(pm.getBasePotionData());
			item.setItemMeta(neww);
			return item;
		} else if (meta instanceof MapMeta) {
			MapMeta pm = (MapMeta) meta;
			item = new ItemStack(item.getType(), item.getAmount());
			MapMeta neww = (MapMeta) item.getItemMeta();
			if (pm.hasMapView())
				neww.setMapView(pm.getMapView());
			item.setItemMeta(neww);
			return item;
		} else if (meta instanceof CompassMeta) {
			CompassMeta pm = (CompassMeta) meta;
			item = new ItemStack(item.getType(), item.getAmount());
			CompassMeta neww = (CompassMeta) item.getItemMeta();
			if (pm.hasLodestone())
				neww.setLodestone(pm.getLodestone());
			neww.setLodestoneTracked(pm.isLodestoneTracked());
			item.setItemMeta(neww);
			return item;
		} else if (meta instanceof BannerMeta) {
			BannerMeta pm = (BannerMeta) meta;
			item = new ItemStack(item.getType(), item.getAmount());
			BannerMeta neww = (BannerMeta) item.getItemMeta();
			neww.setPatterns(pm.getPatterns());
			item.setItemMeta(neww);
			return item;
		} else if (meta instanceof TropicalFishBucketMeta) {
			TropicalFishBucketMeta pm = (TropicalFishBucketMeta) meta;
			item = new ItemStack(item.getType(), item.getAmount());
			TropicalFishBucketMeta neww = (TropicalFishBucketMeta) item.getItemMeta();
			neww.setBodyColor(pm.getBodyColor());
			neww.setPatternColor(pm.getPatternColor());
			item.setItemMeta(neww);
			return item;
		} else if (item.getType() == Material.BEE_NEST) {
			BlockDataMeta pm = (BlockDataMeta) meta;
			item = new ItemStack(item.getType(), item.getAmount());
			BlockDataMeta neww = (BlockDataMeta) item.getItemMeta();
			if (pm.hasBlockData())
				neww.setBlockData(pm.getBlockData(item.getType()));
			item.setItemMeta(neww);
			return item;
		} else if (meta instanceof BookMeta) {
			BookMeta pm = (BookMeta) meta;
			item = new ItemStack(item.getType(), item.getAmount());
			BookMeta neww = (BookMeta) item.getItemMeta();
			if (pm.hasAuthor())
				neww.setAuthor(pm.getAuthor());
			if (pm.hasGeneration())
				neww.setGeneration(pm.getGeneration());
			if (pm.hasTitle())
				neww.setTitle(pm.getTitle());
			if (pm.hasPages())
				neww.pages(pm.pages());
			item.setItemMeta(neww);
			return item;
		} else if (!shouldFormatNameRarity) {
			return new ItemStack(item.getType(), item.getAmount());
		} else {
			return BeanItem.formatItem(item);
		}
	}
	
	public static BeanItem[] values() {
		return items;
	}
	
}
