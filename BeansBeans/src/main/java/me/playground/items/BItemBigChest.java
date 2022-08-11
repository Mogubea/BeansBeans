package me.playground.items;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.UUID;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.data.type.Chest.Type;
import org.bukkit.craftbukkit.v1_18_R2.inventory.util.CraftInventoryCreator;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;

import me.playground.data.CustomPersistentDataType;
import me.playground.gui.BeanGuiBigChest;
import me.playground.utils.Utils;
import net.kyori.adventure.text.Component;

public class BItemBigChest extends BeanBlock {
	
	public static final NamespacedKey KEY_ITEMS = key("ITEMS");
	public static final NamespacedKey KEY_ACCIDENT_PROTECTION = key("ACCIDENT_PROTECTION");
	protected final int storageSize;
	protected final ItemStack indicator;
//	protected final boolean hasDisplayItem;
	
	protected BItemBigChest(int numeric, String identifier, String name, String skullBase64, ItemRarity rarity, int modelDataInt, int storageSize, Material indicator) {
		super(numeric, identifier, name, Utils.getSkullWithCustomSkin(UUID.nameUUIDFromBytes(ByteBuffer.allocate(16).putInt(numeric).array()), skullBase64), rarity, modelDataInt);
		this.storageSize = (storageSize / 9) * 9; // Enforce multiples of 9
		this.indicator = new ItemStack(indicator);
//		this.hasDisplayItem = numeric == 1004;
		if (numeric == 1004)
			this.indicator.editMeta(meta -> meta.addEnchant(Enchantment.ARROW_INFINITE, 1, true));

		setDefaultLore(Component.text("Has ", NamedTextColor.GRAY).append(Component.text(storageSize, NamedTextColor.WHITE).append(Component.text(" storage slots.", NamedTextColor.GRAY))).decoration(TextDecoration.ITALIC, false));
	}
	
	@Override
	public void onBlockInteract(PlayerInteractEvent e) {
		if (!(e.getPlayer().isSneaking() && e.getItem() != null) && !e.getPlayer().isBlocking()) {
			e.setCancelled(true);
			new BeanGuiBigChest(e.getPlayer(), (Chest) e.getClickedBlock().getState()).openInventory();
		}
	}
	
	@Override
	public void onBlockBreak(BlockBreakEvent e) {
		Chest chest = (Chest) e.getBlock().getState();
		PersistentDataContainer pdc = chest.getPersistentDataContainer();
		if (pdc.has(KEY_ACCIDENT_PROTECTION)) {
			e.getPlayer().sendActionBar(Component.text("\u00a7cThis block has Break Protection enabled."));
			e.setCancelled(true);
			return;
		}
		
		Collection<ArmorStand> stands = e.getBlock().getLocation().add(0.5, -0.3, 0.5).getNearbyEntitiesByType(ArmorStand.class, 0.11D);
		for (ArmorStand s : stands)
			if (s.isMarker() && s.isSmall())
				s.remove();
		
		// Additional block effect when breaking
		e.getBlock().getWorld().spawnParticle(Particle.BLOCK_DUST, e.getBlock().getLocation().add(0.5, 0.5, 0.5), 8, 0.25, 0.25, 0.25, indicator.getType().createBlockData());
		
		// Close the inventory for all viewers.
		BeanGuiBigChest.getViewers(chest).forEach(HumanEntity::closeInventory);
	}

	@Override
	public void onBlockDropItems(BlockDropItemEvent e) {
		Chest chest = (Chest) e.getBlockState();
		PersistentDataContainer pdc = chest.getPersistentDataContainer();

		ItemStack[] items = pdc.getOrDefault(KEY_ITEMS, CustomPersistentDataType.ITEMSTACK_ARRAYLIST, new ItemStack[0]);

		for (int x = -1; ++x < items.length;) {
			ItemStack i = items[x];
			if (i == null) continue;
			e.getItems().add(e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), i));
		}
	}

	@Override
	public void onBlockPlace(BlockPlaceEvent e) {
		BlockState bs = e.getBlock().getState();
		PersistentDataHolder holder = (PersistentDataHolder) bs;
		PersistentDataContainer pdc = holder.getPersistentDataContainer();
		pdc.set(KEY_ITEMS, CustomPersistentDataType.ITEMSTACK_ARRAYLIST, new ItemStack[storageSize]);
		bs.update();
		
		// Additional sound effect when placing
		Location l = e.getBlock().getLocation().add(0.5, 0.5, 0.5);
		e.getPlayer().getWorld().playSound(l, Sound.BLOCK_ANVIL_PLACE, 0.07F, 1.3F);
		
		// Chest type indicator as a stand
		e.getBlock().getWorld().spawnEntity(l.subtract(0, 0.7, 0), EntityType.ARMOR_STAND, SpawnReason.CUSTOM, armorStand -> {
			ArmorStand stand = (ArmorStand) armorStand;
			stand.setRotation(((org.bukkit.block.data.type.Chest)bs.getBlockData()).getFacing().ordinal() * 90 + 180, 0);
			stand.setSmall(true);
			stand.setVisible(false);
			stand.setInvulnerable(true);
			stand.setGravity(false);
			stand.setBasePlate(false);
			stand.setCanTick(false);
			stand.setCollidable(false);
			stand.setCanMove(false);
			stand.setMarker(true);
			stand.addDisabledSlots(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);
			stand.getEquipment().setHelmet(indicator);
		});
		
//		createDisplayStand((Chest)bs);
	}
	
	@Override
	public String preBlockPlace(BlockPlaceEvent e) {
		e.getBlock().setType(Material.CHEST);
		
		Chest chest = (Chest) e.getBlock().getState();
		
		// Set custom name
		ItemMeta handMeta = e.getItemInHand().getItemMeta();
		chest.customName((handMeta.hasDisplayName() ? handMeta.displayName() : getDisplayName()).color(null));
		
		// Disallow connection with any other chests and set facing
		if (chest.getBlockData() instanceof org.bukkit.block.data.type.Chest chestData) {
			chestData.setFacing(BlockFace.values()[Math.round(e.getPlayer().getLocation().getYaw() / 90f) & 0x3]);
			chestData.setType(Type.SINGLE);
			chest.setBlockData(chestData);
			chest.update();
		}
		return super.preBlockPlace(e);
	}
	
	/**
	 * Get the raw array of {@link ItemStack}s from this chest which are currently contained in the {@link PersistentDataContainer}.
	 * @param chest The custom Chest
	 * @return An array of items.
	 */
	public ItemStack[] getInventoryContents(Chest chest) {
		PersistentDataContainer pdc = chest.getPersistentDataContainer();
		boolean has = pdc.has(KEY_ITEMS);
		ItemStack[] items = pdc.getOrDefault(KEY_ITEMS, CustomPersistentDataType.ITEMSTACK_ARRAYLIST, new ItemStack[storageSize]);

		if (!has) {
			pdc.set(KEY_ITEMS, CustomPersistentDataType.ITEMSTACK_ARRAYLIST, new ItemStack[storageSize]);
			chest.update();
		}
		
		return items;
	}
	
	/**
	 * Creates an {@link Inventory} Snapshot instance from the currently stored items within this chest.
	 * Updating the contents of this Inventory will not alter the true contents of this chest.
	 */
	public Inventory createInventorySnapshot(Chest chest) {
		Inventory i = CraftInventoryCreator.INSTANCE.createInventory(null, storageSize);
		i.setContents(getInventoryContents(chest));
		return i;
	}
	
	/**
	 * Save this custom container's inventory to the {@link #KEY_ITEMS} key in the Chest's {@link PersistentDataContainer}.
	 * Any viewers of the relevant {@link BeanGuiBigChest} interface will have their currently viewed items updated immediately.
	 */
	public void saveInventory(Chest chest, ItemStack[] items, boolean updateViewers) {
		chest.getPersistentDataContainer().set(KEY_ITEMS, CustomPersistentDataType.ITEMSTACK_ARRAYLIST, items);
		chest.update();
		
//		updateDisplayStand(chest, items);
		if (updateViewers)
			BeanGuiBigChest.updateViewerInventory(chest, items);
	}
	
	public int getStorageSize() {
		return storageSize;
	}
	
	/**
	 * Toggle the accidental block break protection for this Chest.
	 */
	public void toggleProtection(Chest chest) {
		if (!hasAccidentProtection(chest))
			chest.getPersistentDataContainer().set(KEY_ACCIDENT_PROTECTION, PersistentDataType.BYTE, (byte)0);
		else
			chest.getPersistentDataContainer().remove(KEY_ACCIDENT_PROTECTION);
		chest.update();
	}
	
	public boolean hasAccidentProtection(Chest chest) {
		return chest.getPersistentDataContainer().has(KEY_ACCIDENT_PROTECTION);
	}
	
	// XXX: Crystal Chest
	/*
	private void updateDisplayStand(Chest chest, ItemStack[] items) {
		if (!this.hasDisplayItem) return;
		
		ItemStack firstItem = null;
		for (int x = -1; ++x < storageSize;)
			if (items[x] != null) {
				firstItem = items[x].clone(); 
				break; 
			}
		
		Collection<ArmorStand> stands = chest.getLocation().add(0.5, -0.4, 0.5).getNearbyEntitiesByType(ArmorStand.class, 0.05D);
		for (ArmorStand s : stands)
			if (s.isMarker()) {
				if (firstItem.getType().isBlock())
					
				s.setItem(EquipmentSlot.HAND, firstItem);
				break;
			}
	}
	
	private void createDisplayStand(Chest chest) {
		if (!this.hasDisplayItem) return;
		BlockFace face = ((org.bukkit.block.data.type.Chest)chest.getBlockData()).getFacing();
		
		chest.getWorld().spawnEntity(chest.getLocation().subtract(((double)face.getModZ()*0.1D), 0.9, ((double)face.getModX()*0.1D)), EntityType.ARMOR_STAND, SpawnReason.CUSTOM, armorStand -> {
			ArmorStand stand = (ArmorStand) armorStand;
			stand.setRotation(face.ordinal() * 90 + 90, 0);
			stand.setSmall(true);
			stand.setVisible(false);
			stand.setInvulnerable(true);
			stand.setGravity(false);
			stand.setBasePlate(false);
			stand.setCanTick(false);
			stand.setCollidable(false);
			stand.setCanMove(false);
			stand.setMarker(true);
			stand.setArms(true);
			stand.setRightArmPose(new EulerAngle(4.712389, 1.570796, 0));
			stand.addDisabledSlots(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);
		});
		
	}*/
	
}
