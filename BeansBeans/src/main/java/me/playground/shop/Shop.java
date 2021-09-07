package me.playground.shop;

import java.text.DecimalFormat;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.ArmorStand.LockType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import me.playground.data.Datasource;
import me.playground.data.Dirty;
import me.playground.items.BeanItem;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import net.kyori.adventure.text.Component;

public class Shop implements Dirty {
	
	private final Location location;
	private ArmorStand stand;
	private Item floatingItem;
	
	private final int id;
	private int ownerId;
	
	private int maxItemQuantity;
	private int itemQuantity;
	private ItemStack itemStack;
	
	private int storedMoney;
	private int totalMoneyEarned;
	private int totalMoneyTaxed;
	
	private int sellPrice;
	private int buyPrice;
	
	private boolean dirty;
	
	public Shop(int id, int ownerId, Location location, int maxQuantity) {
		this(id, ownerId, location, maxQuantity, 0, null, 0, 0, 0, 0, 0, true);
	}
	
	public Shop(int id, int ownerId, Location location, int maxQuantity, int quantity, ItemStack item, int storedMoney, int totalMoneyEarned, int totalMoneyTaxed, int sellPrice, int buyPrice) {
		this(id, ownerId, location, maxQuantity, quantity, item, storedMoney, totalMoneyEarned, totalMoneyTaxed, sellPrice, buyPrice, false);
	}
	
	public Shop(int id, int ownerId, Location location, int maxQuantity, int quantity, ItemStack item, int storedMoney, int totalMoneyEarned, int totalMoneyTaxed, int sellPrice, int buyPrice, boolean load) {
		this.id = id;
		this.ownerId = ownerId;
		this.location = location;
		
		this.maxItemQuantity = maxQuantity;
		this.itemQuantity = quantity;
		
		this.setSellPrice(sellPrice);
		this.setBuyPrice(buyPrice);
		
		if (item == null || item.getType() == Material.AIR) {
			this.itemStack = null;
			this.itemQuantity = 0;
		} else {
			this.itemStack = BeanItem.resetItemFormatting(item);
		}
		
		location.add(0, 2, 0).getBlock().setMetadata("protected", new FixedMetadataValue(Main.getInstance(), true));
		location.subtract(0, 2, 0);
		
		this.storedMoney = storedMoney;
		this.totalMoneyEarned = totalMoneyEarned;
		this.totalMoneyTaxed = totalMoneyTaxed;
		
		if (load)
			loadEntities();
		Datasource.markShop(this);
	}
	
	
	public Item getFloatingItem() {
		return floatingItem;
	}

	public int getMaxItemQuantity() {
		return maxItemQuantity;
	}

	public void setMaxItemQuantity(int maxItemQuantity) {
		setDirty(true);
		this.maxItemQuantity = maxItemQuantity;
	}

	public int getItemQuantity() {
		return itemQuantity;
	}

	public void setItemQuantity(int itemQuantity) {
		setDirty(true);
		this.itemQuantity = itemQuantity;
	}

	public ItemStack getItemStack() {
		return itemStack;
	}

	public void setItemStack(ItemStack itemStack) {
		ItemStack shopItem = (itemStack != null) ? itemStack.clone() : null;
		
		if (shopItem != null)
			shopItem.setAmount(1);
		this.itemStack = shopItem;
		setDirty(true);
		updateItemEntity();
		updateHologram();
	}
	
	private void updateHologram() {
		Component itemName = null;
		if (itemStack != null) {
			ItemMeta meta = itemStack.getItemMeta();
			if (meta.hasDisplayName())
				itemName = meta.displayName();
			else
				itemName = Component.text(itemStack.getI18NDisplayName());
		}
		
		//Component holo = itemStack != null ? itemStack.displayName() : null;
		//if (getSellPrice() > 0 && getItemQuantity() > 0)
		//	holo.append(Component.text("\n\u00a76" + df.format(getSellPrice()) + " Coins"));
		if (stand != null) {
			if (itemName != null)
				stand.customName(itemName);
			stand.setCustomNameVisible(itemName != null);
		}
			
	}

	public int getStoredMoney() {
		return storedMoney;
	}

	public void setStoredMoney(int storedMoney) {
		setDirty(true);
		this.storedMoney = storedMoney;
	}

	public int getTotalMoneyEarned() {
		return totalMoneyEarned;
	}

	public void setTotalMoneyEarned(int totalMoneyEarned) {
		setDirty(true);
		this.totalMoneyEarned = totalMoneyEarned;
	}

	public int getTotalMoneyTaxed() {
		return totalMoneyTaxed;
	}

	public void setTotalMoneyTaxed(int totalMoneyTaxed) {
		setDirty(true);
		this.totalMoneyTaxed = totalMoneyTaxed;
	}

	public ArmorStand getArmourStand() {
		return stand;
	}

	public int getShopId() {
		return id;
	}

	public int getOwnerId() {
		return ownerId;
	}
	
	public void setOwner(int dbid) {
		this.ownerId = dbid;
	}
	
	private void updateArmourStandEntity() {
		if (stand != null) {
			stand.getVehicle().remove();
			stand.remove();
		}
		
		stand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
		stand.setInvisible(true);
		stand.setInvulnerable(true);
		stand.setGravity(false);
		stand.setBasePlate(false);
		stand.addEquipmentLock(EquipmentSlot.HEAD, LockType.REMOVING_OR_CHANGING);
		stand.addEquipmentLock(EquipmentSlot.CHEST, LockType.REMOVING_OR_CHANGING);
		stand.addEquipmentLock(EquipmentSlot.LEGS, LockType.REMOVING_OR_CHANGING);
		stand.addEquipmentLock(EquipmentSlot.FEET, LockType.REMOVING_OR_CHANGING);
		stand.getPersistentDataContainer().set(new NamespacedKey(Main.getInstance(), "shopOwner"), PersistentDataType.INTEGER, ownerId);
		stand.getPersistentDataContainer().set(new NamespacedKey(Main.getInstance(), "shopId"), PersistentDataType.INTEGER, id);
		stand.getEquipment().setHelmet(new ItemStack(Material.GLASS));
		stand.setCustomNameVisible(true);
		
		ArmorStand a = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
		a.getPersistentDataContainer().set(new NamespacedKey(Main.getInstance(), "shopId"), PersistentDataType.INTEGER, id);
		a.setInvisible(true);
		a.setInvulnerable(true);
		a.setGravity(false);
		a.setBasePlate(false);
		a.setMarker(true);
		a.addPassenger(stand);
	}
	
	private void updateItemEntity() {
		if (itemStack != null && itemStack.getType() != Material.AIR) {
			if (stand == null) {
				refreshEntities();
			} else if (floatingItem != null) {
				floatingItem.setItemStack(itemStack);
			} else {
				final Location l = stand.getLocation().clone();
				floatingItem = (Item) l.getWorld().spawnEntity(l.add(0, 1.44, 0), EntityType.DROPPED_ITEM);
				floatingItem.setItemStack(itemStack);
				floatingItem.setWillAge(false);
				floatingItem.setCanMobPickup(false);
				floatingItem.setCanPlayerPickup(false);
				floatingItem.setGravity(false);
				floatingItem.setVelocity(new Vector(0,0,0));
				floatingItem.setInvulnerable(true);
				floatingItem.setCustomNameVisible(true);
				stand.addPassenger(floatingItem);
			}
		} else if (floatingItem != null) {
			floatingItem.remove();
			this.floatingItem = null;
		}
	}

	/**
	 * Unloads all relevant entities within a small radius, so this will remove corrupted entities also.
	 */
	public void unloadEntities() {
		for (Entity e : location.getNearbyEntities(0.3, 2, 0.3)) {
			if (e instanceof Item && ((Item)e).isCustomNameVisible())
				e.remove();
			else if (e instanceof ArmorStand && !(((ArmorStand)e).hasGravity()))
				e.remove();
		}
		if (stand != null)
			stand.remove();
	}
	
	public void loadEntities() {
		if (location.getWorld() == null)
			return;
		
		updateArmourStandEntity();
		updateItemEntity();
		updateHologram();
	}

	private void refreshEntities() {
		unloadEntities();
		loadEntities();
	}

	public int getBuyPrice() {
		return buyPrice;
	}


	public void setBuyPrice(int buyPrice) {
		setDirty(true);
		this.buyPrice = buyPrice;
	}


	public int getSellPrice() {
		return sellPrice;
	}


	public void setSellPrice(int sellPrice) {
		setDirty(true);
		this.sellPrice = sellPrice;
	}
	
	public void delete(int playerId) {
		if (this.getStoredMoney() > 0 && this.getOwnerId() > 0)
			PlayerProfile.fromIfExists(getOwnerId()).addToBalance(getStoredMoney(), "Removal of Shop ID: " + this.getShopId() + " by player ID: " + playerId);
		location.add(0, 2, 0).getBlock().removeMetadata("protected", Main.getInstance());
		location.subtract(0, 2, 0);
		Main.getShopManager().deleteShop(this, playerId);
	}
	
	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}
	
	public boolean isDirty() {
		return dirty;
	}
	
	public Location getLocation() {
		return this.location;
	}
	
	protected final DecimalFormat df = new DecimalFormat("#,###");
	
}
