package me.playground.items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.annotation.Nonnull;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.block.ShulkerBox;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
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
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.TropicalFishBucketMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import me.playground.items.enchants.BeanEnchantmentListener;
import me.playground.items.heirlooms.BItemHeirloomAncientSkull;
import me.playground.items.heirlooms.BItemHeirloomMochi;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.utils.BeanColor;
import me.playground.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public abstract class BeanItem {
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
		
		put(Material.SHULKER_BOX, ItemRarity.UNCOMMON);
		put(Material.BLACK_SHULKER_BOX, ItemRarity.UNCOMMON);
		put(Material.BLUE_SHULKER_BOX, ItemRarity.UNCOMMON);
		put(Material.GREEN_SHULKER_BOX, ItemRarity.UNCOMMON);
		put(Material.CYAN_SHULKER_BOX, ItemRarity.UNCOMMON);
		put(Material.RED_SHULKER_BOX, ItemRarity.UNCOMMON);
		put(Material.PURPLE_SHULKER_BOX, ItemRarity.UNCOMMON);
		put(Material.ORANGE_SHULKER_BOX, ItemRarity.UNCOMMON);
		put(Material.GRAY_SHULKER_BOX, ItemRarity.UNCOMMON);
		put(Material.LIGHT_GRAY_SHULKER_BOX, ItemRarity.UNCOMMON);
		put(Material.LIGHT_BLUE_SHULKER_BOX, ItemRarity.UNCOMMON);
		put(Material.LIME_SHULKER_BOX, ItemRarity.UNCOMMON);
		put(Material.BROWN_SHULKER_BOX, ItemRarity.UNCOMMON);
		put(Material.WHITE_SHULKER_BOX, ItemRarity.UNCOMMON);
		put(Material.PINK_SHULKER_BOX, ItemRarity.UNCOMMON);
		put(Material.YELLOW_SHULKER_BOX, ItemRarity.UNCOMMON);
		put(Material.MAGENTA_SHULKER_BOX, ItemRarity.UNCOMMON);
		
		put(Material.EMERALD_ORE, ItemRarity.UNCOMMON);
		put(Material.DEEPSLATE_DIAMOND_ORE, ItemRarity.UNCOMMON);
		
		put(Material.NETHERITE_AXE, ItemRarity.RARE);
		put(Material.NETHERITE_PICKAXE, ItemRarity.RARE);
		put(Material.NETHERITE_SWORD, ItemRarity.RARE);
		put(Material.NETHERITE_HOE, ItemRarity.RARE);
		put(Material.NETHERITE_SHOVEL, ItemRarity.RARE);
		put(Material.NETHERITE_HELMET, ItemRarity.RARE);
		put(Material.NETHERITE_CHESTPLATE, ItemRarity.RARE);
		put(Material.NETHERITE_LEGGINGS, ItemRarity.RARE);
		put(Material.NETHERITE_BOOTS, ItemRarity.RARE);
		
		put(Material.ENCHANTED_GOLDEN_APPLE, ItemRarity.RARE);
		put(Material.TRIDENT, ItemRarity.RARE);
		put(Material.ELYTRA, ItemRarity.RARE);
		put(Material.DRAGON_HEAD, ItemRarity.RARE);
		put(Material.DRAGON_EGG, ItemRarity.RARE);
		put(Material.BEACON, ItemRarity.RARE);
		
		put(Material.DEEPSLATE_EMERALD_ORE, ItemRarity.EPIC);
		put(Material.TOTEM_OF_UNDYING, ItemRarity.EPIC);
		
		put(Material.END_PORTAL_FRAME, ItemRarity.UNTOUCHABLE);
		put(Material.COMMAND_BLOCK, ItemRarity.UNTOUCHABLE);
		put(Material.COMMAND_BLOCK_MINECART, ItemRarity.UNTOUCHABLE);
	}};
	
	private final static NamespacedKey KEY_ID = key("ID"); // String
	public final static NamespacedKey KEY_DURABILITY = key("DURABILITY"); // Integer
	public final static NamespacedKey KEY_MAX_DURABILITY = key("MAX_DURABILITY"); // Integer
	public final static NamespacedKey KEY_COUNTER = key("COUNTER"); // Integer
	
	public final static HashMap<String, BeanItem> itemsByName = new HashMap<String, BeanItem>();
	private static BeanItem[] items;
	
	public final static BeanItem FORMATTING_WAND = new BeanItem("FORMATTING_WAND", "Formatting Wand", Material.GOLDEN_AXE, ItemRarity.UNTOUCHABLE, 1, 500) {
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
	
	public final static BeanItem DEFORMATTING_WAND = new BeanItem("DEFORMATTING_WAND", "Deformatting Wand", Material.NETHERITE_AXE, ItemRarity.UNTOUCHABLE, 1, 500) {
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
	
	public final static BeanItem SHOP_STAND = new BeanItem("SHOP_STAND", "Shopping Stand", Utils.getSkullWithCustomSkin(UUID.fromString("6145234d-06a3-402d-a4a0-68787735fbfa"), "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2UzZGViNTdlYWEyZjRkNDAzYWQ1NzI4M2NlOGI0MTgwNWVlNWI2ZGU5MTJlZTJiNGVhNzM2YTlkMWY0NjVhNyJ9fX0="), ItemRarity.RARE, 1) {
		public void onInteract(PlayerInteractEvent e) {
			if (e.getHand() == EquipmentSlot.HAND && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
				e.setCancelled(true);
				final Location l = e.getClickedBlock().getLocation().add(e.getBlockFace().getDirection().toLocation(e.getClickedBlock().getWorld()));
				final boolean upwards = e.getBlockFace() == BlockFace.UP;
				final boolean carpet = l.subtract(0,upwards ? 1 : 0,0).getBlock().getType().toString().endsWith("CARPET");
				
				if (upwards && !carpet)
					l.add(0,1,0);
				
				final Block b = l.getBlock();
				final Block below = l.subtract(0,1,0).getBlock();
				
				
				if ((!carpet && b.getType() != Material.AIR) || !below.isSolid() || below.isPassable() || below.getType() == Material.HOPPER) {
					e.getPlayer().sendActionBar(Component.text("\u00a7cThat is an invalid shop location!"));
				} else if (b.getLocation().add(0.5, 0.5, 0.5).getNearbyEntitiesByType(ArmorStand.class, 2.49).size() > 0) {
					e.getPlayer().sendActionBar(Component.text("\u00a7cThere are too many stands here!"));
				} else {
					try {
						final PlayerProfile pp = PlayerProfile.from(e.getPlayer());
						Main.getShopManager().createNewShop(pp.getId(), l);
						if (e.getPlayer().getGameMode() != GameMode.CREATIVE)
							e.getItem().setAmount(e.getItem().getAmount() - 1);
					} catch (Throwable ee) {
						e.getPlayer().sendActionBar(Component.text("\u00a7cReport this, there was a problem creating this shop!"));
						ee.printStackTrace();
					}
				}
			}
		}
		
		public ArrayList<Component> getCustomLore(ItemStack item) {
			final ArrayList<Component> lore = new ArrayList<Component>();
			lore.addAll(Arrays.asList(
					Component.text("\u00a77Place this down to create your"),
					Component.text("\u00a77very own \u00a7eShop\u00a77 and start selling"),
					Component.text("\u00a77via an interactive shopping interface!"),
					Component.text("\u00a78\u00a7o(Maximum Storage: 1728 Items)")));
			return lore;
		}
	};
	
	public final static BeanItem DEBUG_CAKE = new BeanItem("DEBUG_CAKE", "Bug Finder's Cake", Material.CAKE, ItemRarity.IRIDESCENT, 1) {
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
	
	public final static BeanItem TALARIANS = new BeanItem("TALARIANS", "Lesser Talarians", Utils.getDyedLeather(Material.LEATHER_BOOTS, 0xFF8833), ItemRarity.RARE, 1, 176) {
		@Override
		public Multimap<Attribute, AttributeModifier> getAttributes() {
			final Multimap<Attribute, AttributeModifier> attributes = HashMultimap.create();
			attributes.put(Attribute.GENERIC_ARMOR, new AttributeModifier(UUID.randomUUID(), "generic.armor", 1, Operation.ADD_NUMBER, EquipmentSlot.FEET));
			attributes.put(Attribute.GENERIC_MOVEMENT_SPEED, new AttributeModifier(UUID.randomUUID(), "generic.movement_speed", 0.004, Operation.ADD_NUMBER, EquipmentSlot.FEET));
			return attributes;
		}
		
		public ArrayList<Component> getCustomLore(ItemStack item) {
			final ArrayList<Component> lore = new ArrayList<Component>();
			lore.addAll(Arrays.asList(
					Component.text("\u00a77While sprinting, slowly gather momentum"),
					Component.text("\u00a77up to a maximum bonus of \u00a7f+25% Movement"),
					Component.text("\u00a7fSpeed\u00a77, at the cost of some durability!")));
			return lore;
		}
	};
	
	public final static BeanItemHeirloom HL_MOCHI 			= new BItemHeirloomMochi("HL_MOCHI", "Mochi", Utils.getSkullWithCustomSkin(UUID.fromString("6145234d-06a3-402d-a4a0-68787735fbfd"), "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjkzNTUzYmU2ODMwMjJmODU5NTljNmMyMTdiNGE1NmJmMzNkZWZiZjUyYTZhODRjNjlkMmNiZGI1MTY0M2IifX19"), ItemRarity.RARE);
	public final static BeanItemHeirloom HL_ANCIENT_SKULL 	= new BItemHeirloomAncientSkull("HL_ANCIENT_SKULL", "Ancient Skull", Utils.getSkullWithCustomSkin(UUID.fromString("6145234d-06a3-402d-a4a0-68787735fbfc"), "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzk3MjNlNTIwYjMwNzAwZTNiYzUzZTg1MjYyMDdlMjJhZjdmZjhmOTY2ODk3OTVmYzk0OWZhYmQ4ZDk4NDcxNSJ9fX0="), ItemRarity.RARE);
	
	static {
		items = itemsByName.values().toArray(new BeanItem[7]);
	}
	
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
	
	protected boolean 				enabled = true;
	
	protected BeanItem(String identifier, String name, Material material, ItemRarity rarity, int modelDataInt) {
		this(identifier, name, new ItemStack(material), rarity, modelDataInt, 0);
	}
	
	protected BeanItem(String identifier, String name, ItemStack item, ItemRarity rarity, int modelDataInt) {
		this(identifier, name, item, rarity, modelDataInt, 0);
	}
	
	protected BeanItem(String identifier, String name, Material material, ItemRarity rarity, int modelDataInt, int maxDurability) {
		this(identifier, name, new ItemStack(material), rarity, modelDataInt, maxDurability);
	}
	
	protected BeanItem(String identifier, String name, ItemStack item, ItemRarity rarity, int modelDataInt, int maxDurability) {
		itemsByName.put(identifier, this);
		
		this.identifier = identifier;
		this.displayName = Component.text(name);
		this.material = item.getType();
		this.defaultRarity = rarity;
		this.maxDurability = maxDurability == 0 ? material.getMaxDurability() : maxDurability;
		
		item = initializeItem(item);
		
		ItemMeta meta = item.getItemMeta();
		meta.setCustomModelData(modelDataInt);
		PersistentDataContainer container = meta.getPersistentDataContainer();
		container.set(KEY_ID, PersistentDataType.STRING, identifier);
		
		if (maxDurability > 8) {
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
	
	public ArrayList<Component> getCustomLore(ItemStack item) {
		return null;
	}
	
	protected ItemStack initializeItem(ItemStack item) {
		return item;
	}
	
	public Multimap<Attribute, AttributeModifier> getAttributes() {
		final Multimap<Attribute, AttributeModifier> attributes = HashMultimap.create();
		return attributes;
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
		lore.add(Component.text("\u00a77Durability: \u00a7f" + (newDura-1) + "\u00a77/\u00a7f" + (maxDura-1)));
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
			int score = 0;
			enchants = bmeta.getStoredEnchants();
			for (int s : enchants.values()) // TODO: Make a better score system for Enchantments..
				score += s;
			if (score > 3)
				rarity = rarity.upOne();
			if (score > 9)
				rarity = rarity.upOne();
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
		final boolean att1 = ia != ItemAttributes.NIL;
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
					if (entry.getKey().equals(Attribute.GENERIC_ATTACK_DAMAGE))
						modifiers.put("Damage", modifiers.getOrDefault("Damage", 0.0) + d);
					if (entry.getKey().equals(Attribute.GENERIC_ATTACK_SPEED))
						modifiers.put("Attack Speed", modifiers.getOrDefault("Attack Speed", 0.0) + d);
					if (entry.getKey().equals(Attribute.GENERIC_MOVEMENT_SPEED))
						modifiers.put("Movement Speed", modifiers.getOrDefault("Movement Speed", 0.0) + d * 1000);
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
				final boolean isInt = (int)val == val;
				
				if (modifier.getValue() != 0)
					lore.add(Component.text("\u25C8 "+modifier.getKey()+": ").color(rarity.getAttributeColour())
							.append(Component.text(isInt ? (int)val : val).color(NamedTextColor.WHITE))
							.append(modsuffix.getOrDefault(modifier.getKey(), Component.text("")))
							.decoration(TextDecoration.ITALIC, false));
			}
			lore.add(Component.empty());
		}
		
		// Hide stuff
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
					lore.addAll(BeanEnchantmentListener.getEnchantLore(enchant.getKey(), enchant.getValue()));
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
	
	private static Component getRarityString(ItemRarity rarity, ItemStack item) {
		final Material m = item.getType();
		
		if (BeanItemHeirloom.from(item) != null)
			return rarity.toComponent().append(Component.text(" Heirloom"));
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
		if (m.toString().endsWith("HELMET") || m.toString().endsWith("SKULL"))
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
