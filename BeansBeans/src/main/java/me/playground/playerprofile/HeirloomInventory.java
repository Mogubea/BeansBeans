package me.playground.playerprofile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.json.JSONArray;
import org.json.JSONObject;

import me.playground.items.BeanItem;
import me.playground.items.BeanItemHeirloom;

public class HeirloomInventory {
	
//	private static boolean enabled = true;
	private final static int DEFAULT_SIZE = 5;
	
	/**
	 * @return The default size of a new {@link HeirloomInventory}.
	 */
	public static int getDefaultSize() {
		return DEFAULT_SIZE;
	}
	
	private int maxSize;
	final private PlayerProfile profile;
	final private LinkedHashMap<String, ItemStack> heirlooms = new LinkedHashMap<String, ItemStack>();
	final private HashMap<Attribute, Double> visualModifiers = new HashMap<Attribute, Double>(4); // Purely for show
	
	public HeirloomInventory(PlayerProfile owner, @Nullable JSONObject data) {
		putDefaultValues();
		this.profile = owner;
		this.maxSize = DEFAULT_SIZE;
		
		if (data == null) return;
		
		this.maxSize = data.optInt("maxSize", 5);
		JSONArray array = data.optJSONArray("heirlooms");
		
		if (array == null) return;
		
		final int size = array.length();
		for (int x = 0; x < size; x++) {
			JSONObject heirloomo = (JSONObject) array.optJSONObject(x);
			if (heirloomo == null || !heirloomo.has("identifier")) continue;
			
			final BeanItem bi = BeanItem.from(heirloomo.getString("identifier"));
			if (bi == null) continue;
			
			ItemStack heirloom = bi.getItemStack();
			heirloom.setAmount(2); // mark dirty
			
			if (heirloomo.has("counter")) {
				ItemMeta meta = heirloom.getItemMeta();
				meta.getPersistentDataContainer().set(BeanItem.KEY_COUNTER, PersistentDataType.INTEGER, heirloomo.getInt("counter"));
				heirloom.setItemMeta(meta);
			}
				
			heirlooms.put(heirloomo.getString("identifier"), heirloom);
		}
		
		recalculateStats();
	}
	
	public ArrayList<ItemStack> getContents() {
		return new ArrayList<ItemStack>(heirlooms.values());
	}
	
	public int size() {
		return heirlooms.size();
	}
	
	/**
	 * @return The maximum amount of Heirlooms this {@link HeirloomInventory} can hold.
	 * This value isn't forced if a player already has more than the limit in this inventory.
	 */
	public int getMaxHeirlooms() {
		return maxSize;
	}
	
	public @Nonnull HeirloomInventory setMaxHeirlooms(int amount) {
		this.maxSize = amount;
		return this;
	}
	
	public @Nonnull PlayerProfile getProfile() {
		return profile;
	}
	
	/**
	 * @param item - The item to be added to the {@link HeirloomInventory}
	 * @return Whether this was successful.
	 */
	public boolean addHeirloom(@Nonnull ItemStack item) {
		BeanItemHeirloom heirloom = BeanItemHeirloom.from(item);
		if (heirloom != null && !this.heirlooms.containsKey(heirloom.getIdentifier())) {
			ItemStack clone = item.clone();
			clone.setAmount(1);
			this.heirlooms.put(heirloom.getIdentifier(), clone);
			addStats(item, true);
			return true;
		}
		return false;
	}
	
	/**
	 * @param item - The item to be removed from the {@link HeirloomInventory}
	 * @return Whether this was successful.
	 */
	public boolean removeHeirloom(@Nonnull ItemStack item) {
		BeanItemHeirloom heirloom = BeanItemHeirloom.from(item);
		if (heirloom != null) {
			removeStats(this.heirlooms.remove(heirloom.getIdentifier()), true);
			return true;
		}
		return false;
	}
	
	public boolean hasHeirloom(@Nonnull String identifier) {
		return this.heirlooms.containsKey(identifier);
	}
	
	public boolean hasHeirloom(@Nonnull BeanItemHeirloom heirloom) {
		return this.hasHeirloom(heirloom.getIdentifier());
	}
	
	public int getCounterFor(@Nonnull BeanItemHeirloom heirloom) {
		if (!hasHeirloom(heirloom)) throw new RuntimeException("Can't get counter for " + profile.getDisplayName() + "'s " + heirloom.getIdentifier() + " as they don't have one!");
		return this.heirlooms.get(heirloom.getIdentifier()).getItemMeta().getPersistentDataContainer().getOrDefault(BeanItem.KEY_COUNTER, PersistentDataType.INTEGER, 0);
	}
	
	/**
	 * Updates the counter value for this {@link BeanItemHeirloom}. After updating the PersistentDataContainer, 
	 * it will run the usual {@link BeanItem#formatItem(ItemStack)} method to ensure things are updated.
	 * <p>Use {@link #getCounterFor(BeanItemHeirloom)} to retrieve the existing value.</p>
	 * @param heirloom - {@link BeanItemHeirloom}
	 * @param amt - The new counter value.
	 */
	public void setCounterFor(@Nonnull BeanItemHeirloom heirloom, int amt) {
		if (!hasHeirloom(heirloom)) throw new RuntimeException("Can't set counter for " + profile.getDisplayName() + "'s " + heirloom.getIdentifier() + " as they don't have one!");
		
		ItemStack item = this.heirlooms.get(heirloom.getIdentifier());
		ItemMeta meta = item.getItemMeta();
		meta.getPersistentDataContainer().set(BeanItem.KEY_COUNTER, PersistentDataType.INTEGER, amt);
		item.setAmount(2); // hacky dirty flag
		item.setItemMeta(meta);
		//BeanItem.formatItem(item); No point in formatting the item unless it's going to be viewed.
	}
	
	private void addStats(@Nonnull ItemStack i, boolean update) {
		if (!i.getItemMeta().hasAttributeModifiers()) return;
		i.getItemMeta().getAttributeModifiers().forEach((attribute, modifier) -> { // ItemMeta is already confirmed from BeanItemHeirloom check.
			if (modifier.getOperation() != AttributeModifier.Operation.ADD_NUMBER) return;
			if (!visualModifiers.containsKey(attribute)) return; // Valid Attribute
			double newAmt = visualModifiers.get(attribute) + modifier.getAmount();
			visualModifiers.put(attribute, newAmt);
			if (profile.isOnline() && !profile.getPlayer().getAttribute(attribute).getModifiers().contains(modifier)) // Bukkit throws an error if the modifier already exists..
				profile.getPlayer().getAttribute(attribute).addModifier(modifier);
		});
	}
	
	private void removeStats(@Nonnull ItemStack i, boolean update) {
		if (!i.getItemMeta().hasAttributeModifiers()) return;
		i.getItemMeta().getAttributeModifiers().forEach((attribute, modifier) -> { // ItemMeta is already confirmed from BeanItemHeirloom check.
			if (modifier.getOperation() != AttributeModifier.Operation.ADD_NUMBER) return;
			if (!visualModifiers.containsKey(attribute)) return; // Valid Attribute
			double newAmt = visualModifiers.get(attribute) - modifier.getAmount();
			visualModifiers.put(attribute, newAmt);
			if (profile.isOnline())
				profile.getPlayer().getAttribute(attribute).removeModifier(modifier);
		});
	}
	
	private void recalculateStats() {
		putDefaultValues();
		heirlooms.forEach((identifier, item) -> {
			addStats(item, false);
		});
	}
	
	public double getLuckBonus() {
		return visualModifiers.get(Attribute.GENERIC_LUCK) * 20;
	}
	
	public double getDamageBonus() {
		return visualModifiers.get(Attribute.GENERIC_ATTACK_DAMAGE);
	}
	
	public double getMovementBonus() {
		return visualModifiers.get(Attribute.GENERIC_MOVEMENT_SPEED);
	}
	
	public double getHealthBonus() {
		return visualModifiers.get(Attribute.GENERIC_MAX_HEALTH);
	}
	
	public HashMap<Attribute, Double> getModifiers() {
		return visualModifiers;
	}
	
	/**
	 * @return The {@link HeirloomInventory} turned into a JSONObject.
	 */
	public JSONObject getJsonData() {
		JSONObject obj = new JSONObject();
		JSONArray array = new JSONArray();
		
		heirlooms.forEach((identifier, item) -> {
			// Should always be true but, just in case;
			if (item.hasItemMeta()) {
				JSONObject idd = new JSONObject();
				PersistentDataContainer ack = item.getItemMeta().getPersistentDataContainer();
				idd.put("identifier", identifier);
				if (ack.has(BeanItem.KEY_COUNTER, PersistentDataType.INTEGER))
					idd.put("counter", ack.get(BeanItem.KEY_COUNTER, PersistentDataType.INTEGER));
				array.put(idd);
			}
		});
		
		obj.put("maxSize", maxSize);
		obj.put("heirlooms", array);
		return obj;
	}
	
	private void putDefaultValues() {
		final Attribute[] atts = {Attribute.GENERIC_LUCK, Attribute.GENERIC_ATTACK_DAMAGE, Attribute.GENERIC_MAX_HEALTH, Attribute.GENERIC_MOVEMENT_SPEED};
		int size = atts.length;
		for (int x = -1; ++x < size;)
			visualModifiers.put(atts[x], 0d);
	}
	
	/**
	 * Fires the {@link BeanItemHeirloom#onConsumeItem(PlayerItemConsumeEvent, HeirloomInventory)} method for each {@link BeanItemHeirloom} within the {@link HeirloomInventory}.
	 *<p>By default, the method does nothing, and has to be overriden when an item is declared.</p>
	 * @param e
	 */
	public void doPlayerItemConsumeEvent(PlayerItemConsumeEvent e) {
		this.heirlooms.keySet().forEach((identifier) -> {
			BeanItemHeirloom heirloom = (BeanItemHeirloom) BeanItemHeirloom.from(identifier);
			if (heirloom == null) return; // Should never happen, but just in-case.
			heirloom.onConsumeItem(e, this);
		});
	}
	
	/**
	 * Fires the {@link BeanItemHeirloom#onMeleeDamageToEntity(EntityDamageByEntityEvent, HeirloomInventory)} method for each {@link BeanItemHeirloom} within the {@link HeirloomInventory}.
	 *<p>By default, the method does nothing, and has to be overriden when an item is declared.</p>
	 * @param e
	 */
	public void doMeleeDamageEvent(EntityDamageByEntityEvent e) {
		this.heirlooms.keySet().forEach((identifier) -> {
			BeanItemHeirloom heirloom = (BeanItemHeirloom) BeanItemHeirloom.from(identifier);
			if (heirloom == null) return; // Should never happen, but just in-case.
			heirloom.onMeleeDamageToEntity(e, this);
		});
	}
	
	/**
	 * Fires the {@link BeanItemHeirloom#onDamageFromEntity(EntityDamageByEntityEvent, HeirloomInventory)} method for each {@link BeanItemHeirloom} within the {@link HeirloomInventory}.
	 *<p>By default, the method does nothing, and has to be overriden when an item is declared.</p>
	 * @param e
	 */
	public void doDamageTakenByEntityEvent(EntityDamageByEntityEvent e) {
		this.heirlooms.keySet().forEach((identifier) -> {
			BeanItemHeirloom heirloom = (BeanItemHeirloom) BeanItemHeirloom.from(identifier);
			if (heirloom == null) return; // Should never happen, but just in-case.
			heirloom.onDamageFromEntity(e, this);
		});
	}
	
}
