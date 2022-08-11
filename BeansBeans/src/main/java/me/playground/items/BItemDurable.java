package me.playground.items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class BItemDurable extends BeanItem {
	
	private List<Enchantment> incompatibleEnchants = new ArrayList<Enchantment>();
	private List<Enchantment> validEnchants = new ArrayList<Enchantment>(ItemAttributes.fromMaterial(material).getValidEnchantments());
	private final Map<String, Float> repairItems = new LinkedHashMap<String, Float>();
	private boolean cleansable = true;
	
	protected BItemDurable(int numeric, String identifier, String name, Material material, ItemRarity rarity, int modelDataInt, int durability) {
		super(numeric, identifier, name, material, rarity, modelDataInt, durability);
	}
	
	protected BItemDurable(int numeric, String identifier, String name, ItemStack material, ItemRarity rarity, int modelDataInt, int durability) {
		super(numeric, identifier, name, material, rarity, modelDataInt, durability);
	}
	
	public boolean isEnchantAllowed(Enchantment enchant) {
		return !incompatibleEnchants.contains(enchant);
	}
	
	public boolean hasForbiddenEnchantments() {
		return !incompatibleEnchants.isEmpty();
	}
	
	/**
	 * @return An unmodifiable list of forbidden enchantments for this {@link BItemDurable}.
	 */
	public List<Enchantment> getForbiddenEnchantments() {
		return incompatibleEnchants;
	}
	
	/**
	 * @return An unmodifiable list of valid enchantments for this {@link BItemDurable}.
	 */
	public List<Enchantment> getValidEnchantments() {
		return validEnchants;
	}
	
	@Override
	public boolean isEnchantable() {
		return enchantable && !validEnchants.isEmpty();
	}
	
	/**
	 * Set the forbidden enchantments of this tool.
	 * <p>Can only do this once.
	 */
	protected void setForbiddenEnchantments(Enchantment...enchantments) {
		final int size = enchantments.length;
		for (int x = -1; ++x < size;)
			incompatibleEnchants.add(enchantments[x]);
		validEnchants.removeAll(incompatibleEnchants);
		
		validEnchants = Collections.unmodifiableList(validEnchants);
		incompatibleEnchants = Collections.unmodifiableList(incompatibleEnchants);
	}
	
	protected void addRepairMaterial(BeanItem item, float durabilityPercentage) {
		this.addRepairMaterial(item.getIdentifier(), durabilityPercentage);
	}
	
	protected void addRepairMaterial(Material material, float durabilityPercentage) {
		this.addRepairMaterial(material.name(), durabilityPercentage);
	}
	
	/**
	 * @param identifier The material that can be combined with the tool.
	 * @param durabilityPercentage The percentage of durability that it restores.
	 */
	private void addRepairMaterial(String identifier, float durabilityPercentage) {
		repairItems.put(identifier, durabilityPercentage);
	}
	
	/**
	 * Set whether this item can have its enchantments removed via match-crafting or grindstone.
	 */
	protected void setCleansable(boolean can) {
		this.cleansable = can;
	}
	
	public boolean isCleansable() {
		return this.cleansable;
	}
	
	public float getRepairPercentage(ItemStack item) {
		if (item == null || item.getType() == Material.AIR) return 0f;
		final BeanItem bi = from(item);
		
		if (bi != null && bi.is(this)) return 6.75f + ((float)getDurability(item) / (float)getMaxDurability()) * 100f;
		String identifier = bi != null ? bi.getIdentifier() : item.getType().name();
		return repairItems.getOrDefault(identifier, 0f);
	}
	
}
