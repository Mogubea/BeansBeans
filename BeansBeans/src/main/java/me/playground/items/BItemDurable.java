package me.playground.items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import me.playground.main.Main;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class BItemDurable extends BeanItem {

	public static final NamespacedKey KEY_REFINEMENT = Main.getInstance().getKey("REFINEMENT_TIER");

	private List<Enchantment> incompatibleEnchants = new ArrayList<>();
	private List<Enchantment> validEnchants = new ArrayList<>(ItemAttributes.fromMaterial(material).getValidEnchantments());
	private final Map<String, Float> repairItems = new LinkedHashMap<>();
	private boolean isRefineable = true;

	protected BItemDurable(int numeric, String identifier, String name, Material material, ItemRarity rarity, int modelDataInt, int durability) {
		super(numeric, identifier, name, material, rarity, modelDataInt, durability);
		setTrackCreation(true);
	}
	
	protected BItemDurable(int numeric, String identifier, String name, ItemStack material, ItemRarity rarity, int modelDataInt, int durability) {
		super(numeric, identifier, name, material, rarity, modelDataInt, durability);
		setTrackCreation(true);
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

	public void onItemDamage(PlayerItemDamageEvent e) {

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
	
	public float getRepairPercentage(ItemStack item) {
		if (item == null || item.getType() == Material.AIR) return 0f;
		final BeanItem bi = from(item);
		
		if (bi != null && bi.is(this)) return 6.75f + ((float)getDurability(item) / (float)getMaxDurability(item)) * 100f;
		String identifier = bi != null ? bi.getIdentifier() : item.getType().name();
		return repairItems.getOrDefault(identifier, 0f);
	}

	/**
	 * Check whether this {@link BItemDurable} can be refined. Most vanilla tools and
	 * armours can be refined by default.
	 * @return true or false.
	 */
	public boolean canBeRefined() {
		return isRefineable;
	}

	public static boolean canBeRefined(ItemStack item) {
		BItemDurable custom = BeanItem.from(item, BItemDurable.class);
		if (custom != null) return custom.canBeRefined();
		if (item.getType().getMaxDurability() < 1) return false;

		return switch (item.getType()) {
			case SHEARS, FISHING_ROD, ELYTRA, FLINT_AND_STEEL, CARROT_ON_A_STICK, SHIELD, WARPED_FUNGUS_ON_A_STICK -> false;
			default -> true;
		};
	}

	public static int getRefinementTier(ItemStack item) {
		if (!item.hasItemMeta()) return 0;
		PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
		return pdc.getOrDefault(KEY_REFINEMENT, PersistentDataType.BYTE, (byte)0);
	}

	public static void setRefinementTier(ItemStack item, int level, boolean adjustDurability) {
		if (level < 0) level = 0;
		else if (level > Byte.MAX_VALUE) level = Byte.MAX_VALUE;
		final int newLevel = level;
		float duraPerc = 0;
		if (adjustDurability)
			duraPerc = ((float)getDurability(item) / (float)getMaxDurability(item)) * 100f;

		item.editMeta(meta -> {
			PersistentDataContainer pdc = meta.getPersistentDataContainer();
			pdc.set(KEY_REFINEMENT, PersistentDataType.BYTE, (byte)newLevel);
		});

		BeanItem.formatItem(item);

		if (adjustDurability) {
			if (duraPerc > 100f)
				duraPerc = 100f; // Precaution
			setDurability(item, (int) ((float)getMaxDurability(item) * (duraPerc/100f)));
		}

	}

}
