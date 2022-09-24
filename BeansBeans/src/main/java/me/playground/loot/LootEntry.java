package me.playground.loot;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Biome;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.json.JSONArray;
import org.json.JSONObject;

import me.playground.items.BeanItem;

/**
 * A LootEntry represents an instance of loot found within {@link LootTable}s.
 * LootEntry's have flags that can further determine the greatness of the ItemStack generated. For example;
 * <p>This entire class, and {@link LootEnchantEntry} utilise <b>Micro-Optimized</b> loops to ensure minimal delay.
 */
public class LootEntry {
	/**
	 * Adds 1 to the maximum possible stack-size when generating the reward.
	 */
	private static final byte FLAG_LOOTING = 0; // 1
	/**
	 * Allows the Luck Potion Effect and various other things (eg. Luck of the Sea enchantments) affect the
	 * odds of various additional variables when generating the reward. These variables include;
	 * <li>Drop Chance
	 * <li>Enchantment Chance
	 * <li>Higher Enchantment Level Chance
	 * <li>Higher Durability Chance
	 */
	private static final byte FLAG_LUCK = 1; // 2
	/**
	 * When saving and loading, a compressed ItemStack will be used.
	 */
	private static final byte FLAG_COMPRESS_STACK = 2; // 4
	/**
	 * Announces the reward to the player, alongside the % chance, in chat.
	 */
	private static final byte FLAG_ANNOUNCE = 3; // 8
	
	// System 2
	/**
	 * Requires the player to have caused a Skeleton to influence the drops.
	 * <p>Only used in System2 checks.
	 */
	private static final byte FLAG_SKELETON_KILL = 4; // 16
	/**
	 * Requires the player to have caused a Charged Creeper to influence the drops.
	 * <p>Only used in System2 checks.
	 */
	private static final byte FLAG_CHARGED_CREEPER_KILL = 5; // 32
	
	private final int id;
	private final LootTable table;
	private final ItemStack baseStack;
	
	private ArrayList<LootEnchantEntry> possibleEnchants = new ArrayList<>();
	private String descriptionOverride;
	private LootTable tableRedirect;
	private Material cookedMaterial;
	
	private short minDurability;
	private short maxDurability;
	
	private byte flags = FLAG_LUCK;
	
	private byte minStack = 1;
	private byte maxStack = 1;
	private float chance;
	private float luckEffectiveness;
	private boolean requiresPlayer = false;
	private boolean mustBeNatural = false;
	private boolean dirty;
	
	private ArrayList<Biome> requiredBiome = new ArrayList<>();
	
	public LootEntry(int id, LootTable tb, ItemStack itemStack, float chance) {
		this(id, tb, itemStack, null, 1, 1, chance, 0.2F, null);
	}
	
	public LootEntry(int id, LootTable tb, BeanItem beanItem, int min, int max, float chance, float luckEffectiveness) {
		this(id, tb, beanItem.getItemStack(), null, min, max, chance, luckEffectiveness, null);
	}
	
	public LootEntry(int id, LootTable tb, ItemStack itemStack, int min, int max, float chance, float luckEffectiveness) {
		this(id, tb, itemStack, null, min, max, chance, luckEffectiveness, null);
	}
	
	public LootEntry(int id, LootTable tb, ItemStack itemStack, Material cookedMaterial, int min, int max, float chance, float luckEffectiveness, JSONObject data) {
		this.id = id;
		this.table = tb;
		this.baseStack = BeanItem.formatItem(itemStack);
		setMaxStackSize(max);
		setMinStackSize(min);
		this.chance = chance;
		this.luckEffectiveness = luckEffectiveness;
		this.dirty = false;
		this.cookedMaterial = cookedMaterial;
		if (data != null)
			this.applyJSON(data);
	}
	
	public LootEntry(int id, LootTable tb, Material material, Material cookedMaterial, int min, int max, int chance, float luckEffectiveness) {
		this(id, tb, new ItemStack(material, 1), cookedMaterial, min, max, chance, luckEffectiveness, null);
	}
	
	public LootEntry setFlags(byte val) {
		this.flags = val;
		return this;
	}
	
	public byte getFlags() {
		return this.flags;
	}
	
	public LootEntry setMinStackSize(int min) {
		if (min == minStack) return this;
		this.minStack = validateSize(min);
		if (min > maxStack) maxStack = minStack;
		this.dirty = true;
		return this;
	}
	
	public int getMinStackSize() {
		return this.minStack;
	}
	
	public LootEntry setMaxStackSize(int max) {
		if (max == maxStack) return this;
		this.maxStack = validateSize(max);
		if (max < minStack) maxStack = minStack;
		this.dirty = true;
		return this;
	}
	
	public int getMaxStackSize() {
		return this.maxStack;
	}
	
	public LootEntry setStackSize(int min, int max) {
		if (min > max) max = min;
		this.maxStack = validateSize(max);
		this.minStack = validateSize(min);
		this.dirty = true;
		return this;
	}

	public LootEntry setRequiresPlayer(boolean player, boolean dirty) {
		this.requiresPlayer = player;
		if (dirty)
			this.dirty = true;
		return this;
	}

	public LootEntry setRequiresNaturalKill(boolean natural, boolean dirty) {
		this.mustBeNatural = natural;
		if (dirty)
			this.dirty = true;
		return this;
	}
	
	/**
	 * Generates the ItemStack reward with factors such as looting, luck, min and max stacks, random enchantments etc. etc.
	 * <li>{@link #FLAG_LOOTING} adds 1 to the maximum stack size per Looting Level.
	 * <li>{@link #FLAG_LUCK} can affect the odds for getting better enchants, durability etc..
	 * @return The cloned and modified reward ItemStack
	 */
	public ItemStack generateReward(int looting, float luck, boolean burn) {
		if (tableRedirect != null) {
			if (tableRedirect.getEntries().size() > 1)
				return LootRetriever.from(tableRedirect, RetrieveMethod.CUMULATIVE_CHANCE).getLoot().get(0);
			return baseStack.clone();
		}
		
		ItemStack clone = baseStack.clone();
		if (burn && hasCookedVariant())
			clone.setType(cookedMaterial);

		if (allowsLooting()) {
			int rand = maxStack + looting - minStack + 1;
			int amt = rand < 1 ? minStack : getRandom().nextInt(rand);
			clone.setAmount(minStack + amt);
		} else if (minStack >= maxStack) {
			clone.setAmount(minStack);
		} else {
			clone.setAmount(minStack + getRandom().nextInt(maxStack - minStack + 1));
		}

		if (!this.possibleEnchants.isEmpty()) {
			int size = possibleEnchants.size();
			EnchantmentStorageMeta meta = (clone.getType() == Material.ENCHANTED_BOOK) ? (EnchantmentStorageMeta) clone.getItemMeta() : null;
			
			for (int x = -1; ++x < size;) {
				LootEnchantEntry ench = possibleEnchants.get(x);
				int levelGot = ench.getEnchantmentLevel(luck);
				if (levelGot < 0) continue;
				if (meta != null)
					meta.addStoredEnchant(ench.getEnchantment(), levelGot+1, true);
				else
					clone.addUnsafeEnchantment(ench.getEnchantment(), levelGot+1);
			}
			if (meta != null)
				clone.setItemMeta(meta);
			
			clone = BeanItem.formatItem(clone);
		}
		
		if (clone.getType().getMaxDurability() > 0 && minDurability > 0) {
			int dura = !hasDurabilityRange() ? minDurability : minDurability + getRandom().nextInt(maxDurability - minDurability);
			clone = BeanItem.setDurability(clone, dura);
		}
		
		return clone;
	}
	
	public ItemStack getDisplayStack() {
		return baseStack;
	}
	
	/*
	 * Get the effectiveness of Luck and Luck enchantments on the base chance.
	 */
	public float getLuckEffectiveness() {
		return luckEffectiveness;
	}
	
	public float getChance() {
		return chance;
	}
	
	public float getChance(float luck) {
		if (!this.allowsLuck()) return chance;
		float chanc = (chance + (luck * luckEffectiveness));
		if (chanc < 0) chanc = 0F;
		return chanc;
	}
	
	public boolean allowsLooting() {
		return (flags & 1<<FLAG_LOOTING) != 0;
	}
	
	public boolean allowsLuck() {
		return (flags & 1<<FLAG_LUCK) != 0;
	}
	
	public boolean shouldCompress() {
		return (flags & 1<<FLAG_COMPRESS_STACK) != 0;
	}
	
	/**
	 * @return true if this loot should be announced to the player when dropped.
	 */
	public boolean shouldAnnounce() {
		return (flags & 1<<FLAG_ANNOUNCE) != 0;
	}
	
	/**
	 * Only used in System2.
	 * @return true if a skeleton shot is required to get this loot.
	 */
	public boolean requiresSkeletonShot() {
		return (flags & 1<<FLAG_SKELETON_KILL) != 0;
	}
	
	/**
	 * Only used in System2.
	 * @return true if a charged creeper is required to get this loot.
	 */
	public boolean requiresChargedCreeper() {
		return (flags & 1<<FLAG_CHARGED_CREEPER_KILL) != 0;
	}

	/**
	 * @return If th entity killed needs to be killed directly by the player and not by a pet.
	 */
	public boolean requiresPlayer() {
		return requiresPlayer;
	}

	/**
	 * @return If the entity killed needs to be detected as a naturally spawning mob.
	 */
	public boolean requiresNaturalKill() {
		return mustBeNatural;
	}
	
	public ArrayList<Biome> getRequiredBiomes() {
		return requiredBiome;
	}
	
	public boolean isBiomeExclusive() {
		return requiredBiome != null && !requiredBiome.isEmpty();
	}
	
	public boolean hasPossibleEnchants() {
		return !this.possibleEnchants.isEmpty();
	}
	
	public ArrayList<LootEnchantEntry> getPossibleEnchants() {
		return this.possibleEnchants;
	}
	
	/**
	 * @return if {@link #getMaxDurability()} differs from {@link #getMinDurability()}.
	 */
	public boolean hasDurabilityRange() {
		return this.maxDurability > this.minDurability;
	}
	
	public short getMaxDurability() {
		return this.maxDurability;
	}
	
	public boolean hasTableRedirect() {
		return this.tableRedirect != null;
	}
	
	public LootTable getTableRedirect() {
		return this.tableRedirect;
	}
	
	public boolean hasCookedVariant() {
		return this.cookedMaterial != null;
	}
	
	public Material getCookedMaterial() {
		return this.cookedMaterial;
	}
	
	/**
	 * Can also function as a "getDurability()" of sorts if {@link #getMaxDurability()} is less than this.
	 */
	public short getMinDurability() {
		return this.minDurability;
	}
	
	public LootTable getTable() {
		return this.table;
	}
	
	public boolean isDirty() {
		return dirty;
	}
	
	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}
	
	public int getId() {
		return id;
	}
	
	public boolean hasDescription() {
		return this.descriptionOverride != null && !this.descriptionOverride.isEmpty();
	}
	
	public String getDescription() {
		return this.descriptionOverride;
	}
	
	protected Random getRandom() {
		return table.getManager().getRandom();
	}
	
	/**
	 * Simply validates the size of the ItemStack.
	 * @return valid byte version of int
	 */
	protected byte validateSize(int size) {
		if (size > baseStack.getMaxStackSize())
			size = baseStack.getMaxStackSize();
		return (byte)size;
	}

	public JSONObject getJsonData() {
		JSONObject object = new JSONObject();
		
		if (!this.possibleEnchants.isEmpty()) {
			JSONArray arrays = new JSONArray();
			
			final int size = this.possibleEnchants.size();
			for (int x = -1; ++x<size;) {
				final LootEnchantEntry ent = possibleEnchants.get(x);
				JSONObject enchObj = new JSONObject();
				enchObj.put("enchantKey", ent.getEnchantment().getKey().getKey());
				enchObj.put("enchantNamespace", ent.getEnchantment().getKey().getNamespace());
				enchObj.put("chance", ent.getFlatChance());
				JSONArray lvls = new JSONArray();
				JSONArray lucks = new JSONArray();
				
				final int ysize = ent.getFlatLvlChances().length; // Used for both loops
				float[] luckarr = ent.getFlatLvlLuckEffects(); // Done this way to ensure the same length for both
				for (int y = -1; ++y<ysize;) {
					lvls.put(ent.getFlatLvlChances()[y]);
					lucks.put((y < luckarr.length) ? luckarr[y] : 0.05F);
				}
					
				enchObj.put("levels", lvls);
				enchObj.put("luckEffects", lucks);
				
				arrays.put(enchObj);
			}
			
			object.append("enchants", arrays);
		}
		
		if (this.maxDurability != 0)
			object.put("maxDurability", maxDurability);
		if (this.minDurability != 0)
			object.put("minDurability", minDurability);
		
		return object.isEmpty() ? null : object;
	}
	
	private LootEntry applyJSON(JSONObject data) {
		if (data == null || data.isEmpty()) return this;
		
		this.descriptionOverride = data.optString("description").replace("\u00e2", ""); // stupid
		
		JSONArray biomes = data.optJSONArray("biomes");
		if (biomes != null && !biomes.isEmpty()) {
			int size = biomes.length();
			for (int x = -1; ++x < size;) {
				final String biomeStr = biomes.optString(x);
				if (biomeStr == null || biomeStr.isEmpty()) continue;
				
				try {
					Biome biome = Biome.valueOf(biomeStr.toUpperCase());
					this.requiredBiome.add(biome);
				} catch(Exception e) {
					getTable().getManager().getPlugin().getLogger().warning("Invalid biome type Biome.\""+biomeStr+"\" for LootEntry: " + this.getId() +
							", skipping this Biome entriely. If this LootEntry gets flagged as dirty, previous database JSON will be completely overwritten.");
				}
			}
		}
		
		String tbl = data.optString("table");
		if (tbl != null && !tbl.isEmpty()) {
			this.tableRedirect = getTable().getManager().getOrCreateTable(tbl);
			return this; // Skip everything else if we're just grabbing a reward from another table.
		}
		
		JSONArray enchants = data.optJSONArray("enchants");
		if (enchants != null && !enchants.isEmpty()) {
			int size = enchants.length();
			for (int x = -1; ++x < size;) {
				final JSONObject enchObj = enchants.optJSONObject(x);
				if (enchObj == null) continue;
				
				float chance = enchObj.optNumber("chance").floatValue();
				if (chance > 100F) chance = 100F;
				
				String sKey = enchObj.optString("enchantKey");
				String sNamespace = enchObj.optString("enchantNamespace", "minecraft");
				if (sKey == null) continue;
				NamespacedKey key = NamespacedKey.fromString(sKey, sNamespace.equals("minecraft") ? null : getTable().getManager().getPlugin());
				Enchantment enchant = Enchantment.getByKey(key);
				if (enchant == null) {
					getTable().getManager().getPlugin().getLogger().warning("Almost registered null Enchantment ("+key.asString()+") for LootEntry: " + this.getId() + 
							", skipping this Enchantment entirely. If this LootEntry gets flagged as dirty, previous database JSON will be completely overwritten.");
					continue;
				}
				
				final JSONArray enchLevels = enchObj.optJSONArray("levels");
				final JSONArray enchLuckEffects = enchObj.optJSONArray("luckEffects");
				float[] levels = {100};
				float[] luckEffects = {0.05F};
				int lvlSize = 0;
				
				if (!(enchLevels == null || enchLuckEffects == null || enchLevels.isEmpty() || enchLuckEffects.isEmpty())) {
					lvlSize = enchLevels.length();
					levels = new float[lvlSize];
					luckEffects = new float[lvlSize];
					for (int y = -1; ++y < lvlSize;) {
						levels[y] = enchLevels.getNumber(y).floatValue();
						luckEffects[y] = enchLuckEffects.optNumber(y, 0.05F).floatValue();
					}
				} else {
					getTable().getManager().getPlugin().getLogger().warning("There was an issue loading Level and Luck Effect information for LootEntry: " + this.getId() +
							", setting it to default values. If this LootEntry gets flagged as dirty, previous database JSON will be completely overwritten.");
				}
				
				this.possibleEnchants.add(new LootEnchantEntry(this, enchant, chance, levels, luckEffects));
			}
		}
		
		// Only assign durability to items with durability
		if (getDisplayStack().getType().getMaxDurability() > 0) {
			this.minDurability = data.optNumber("minDurability", 0).shortValue();
			this.maxDurability = data.optNumber("maxDurability", 0).shortValue();
			int max = BeanItem.getMaxDurability(getDisplayStack());
			if (maxDurability > max)
				maxDurability = (short) max;
		}
		
		return this;
	}
}
