package me.playground.loot;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
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
 * Some of the flags listed below only apply to System2 loot calculations.
 * <li>{@link #FLAG_LOOTING} adds 1 to the maximum stack size per Looting Level.
 * <li>{@link #FLAG_LUCK} can affect the odds for getting better enchants, durability etc..
 * <li>{@link #FLAG_COMPRESS_STACK} means that the base stack information will be the same.
 * <li>{@link #FLAG_SKELETON_KILL} SYSTEM2: means that in order for this loot to drop, a skeleton has to have done the final blow.
 * <li>{@link #FLAG_CHARGED_CREEPER_KILL} SYSTEM2: means that in order for this loot to drop, a charged creeper has to have done the final blow.
 */
public class LootEntry {
	private static final byte FLAG_LOOTING = 0; // 1
	private static final byte FLAG_LUCK = 1; // 2
	private static final byte FLAG_COMPRESS_STACK = 2; // 4
	private static final byte FLAG_ANNOUNCE = 3; // 8
	
	// System 2
	private static final byte FLAG_SKELETON_KILL = 4; // 16
	private static final byte FLAG_CHARGED_CREEPER_KILL = 5; // 32
	
	private final int id;
	private final LootTable table;
	private final ItemStack baseStack;
	
	private ArrayList<LootEnchantEntry> possibleEnchants = new ArrayList<LootEnchantEntry>();
	private LootTable tableRedirect;
	private short minDurability;
	private short maxDurability;
	
	private byte flags = FLAG_LUCK + FLAG_LOOTING;
	
	private byte minStack = 1;
	private byte maxStack = 1;
	private int chance;
	private boolean dirty;
	
	public LootEntry(int id, LootTable tb, ItemStack itemStack, int chance) {
		this(id, tb, itemStack, 1, 1, chance, null);
	}
	
	public LootEntry(int id, LootTable tb, BeanItem beanItem, int min, int max, int chance) {
		this(id, tb, beanItem.getItemStack(), min, max, chance, null);
	}
	
	public LootEntry(int id, LootTable tb, ItemStack itemStack, int min, int max, int chance) {
		this(id, tb, itemStack, min, max, chance, null);
	}
	
	public LootEntry(int id, LootTable tb, ItemStack itemStack, int min, int max, int chance, JSONObject data) {
		this.id = id;
		this.table = tb;
		this.baseStack = BeanItem.formatItem(itemStack);
		setMaxStackSize(max);
		setMinStackSize(min);
		this.chance = chance;
		this.dirty = false;
		if (data != null)
			this.applyJSON(data);
	}
	
	public LootEntry(int id, LootTable tb, Material material, int min, int max, int chance) {
		this(id, tb, new ItemStack(material, 1), min, max, chance, null);
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
	
	/**
	 * Generates the ItemStack reward with factors such as looting, luck, min and max stacks, random enchantments etc. etc.
	 * <li>{@link #FLAG_LOOTING} adds 1 to the maximum stack size per Looting Level.
	 * <li>{@link #FLAG_LUCK} can affect the odds for getting better enchants, durability etc..
	 * @return The cloned and modified reward ItemStack
	 */
	public ItemStack generateReward(int looting, float luck) {
		if (tableRedirect != null) {
			if (tableRedirect.getEntries().size() > 0)
				return tableRedirect.getRewardsFromSystem1(1, looting, luck).get(0);
			return baseStack.clone();
		}
		
		ItemStack clone = baseStack.clone();
		if (allowsLooting())
			clone.setAmount(minStack + getRandom().nextInt(maxStack + looting - minStack + 1));
		else if (minStack >= maxStack)
			clone.setAmount(minStack);
		else
			clone.setAmount(minStack + getRandom().nextInt(maxStack - minStack + 1));
		
		if (!this.possibleEnchants.isEmpty()) {
			int size = possibleEnchants.size();
			for (int x = -1; ++x < size;) {
				LootEnchantEntry ench = possibleEnchants.get(x);
				int levelGot = ench.getEnchantmentLevel(luck);
				if (levelGot < 0) continue;
				if (clone.getType() == Material.ENCHANTED_BOOK) {
					EnchantmentStorageMeta meta = (EnchantmentStorageMeta) clone.getItemMeta();
					meta.addEnchant(ench.getEnchantment(), levelGot+1, true);
				} else {
					clone.addUnsafeEnchantment(ench.getEnchantment(), levelGot+1);
				}
			}
			clone = BeanItem.formatItem(clone);
		}
		
		if (clone.getType().getMaxDurability() > 0 && minDurability > 0) {
			int dura = !hasDurabilityRange() ? minDurability : minDurability + getRandom().nextInt(maxDurability - minDurability);
			clone = BeanItem.setDurability(clone, dura);
		}
		
		return clone;
	}
	
	public final ItemStack getDisplayStack() {
		return baseStack;
	}
	
	public int getChance() {
		return chance;
	}
	
	public int getChance(float luck) {
		return (int) (chance * (1F + (luck/5F))); // XXX: +20% on every single item per Luck Level.
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
	
	public boolean hasPossibleEnchants() {
		return !this.possibleEnchants.isEmpty() && this.possibleEnchants.size() > 0;
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
	
	/**
	 * @return
	 */
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
				maxDurability = (short) max; // TODO: Make getMaxDurability a short.
		}
		
		return this;
	}
}
