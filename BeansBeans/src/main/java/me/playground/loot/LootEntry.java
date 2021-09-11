package me.playground.loot;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.json.JSONObject;

import me.playground.items.BeanItem;

/**
 * A basic loot entry, cannot handle random enchantment or durability generation, simply holds a basic ItemStack with a min-max stack.
 * <p>LootEntry's have flags that can further determine the greatness of the ItemStack generated. For example; 
 * <li>{@link #FLAG_GRINDABLE} being off will completely deny the item from even dropping if there was no player input.
 * <li>{@link #FLAG_LOOTING} adds 1 to the maximum stack size per Looting Level.
 * <li>{@link #FLAG_LUCK} can affect the odds for getting better enchants, durability etc..
 */
public class LootEntry {
	private static final byte FLAG_GRINDABLE = 1; // 1
	private static final byte FLAG_LOOTING = 2; // 2
	private static final byte FLAG_LUCK = 3; // 4
	private static final byte FLAG_COMPRESS_STACK = 4; // 8
	private static final byte FLAG_ANNOUNCE = 5; // 16
	
	private final int id;
	private final LootTable table;
	private final ItemStack baseStack;
	
	private byte flags = FLAG_LUCK + FLAG_LOOTING;
	
	private byte minStack = 1;
	private byte maxStack = 1;
	private int chance;
	private boolean dirty;
	
	public LootEntry(int id, LootTable tb, ItemStack itemStack, int chance) {
		this(id, tb, itemStack, 1, 1, chance);
	}
	
	public LootEntry(int id, LootTable tb, BeanItem beanItem, int min, int max, int chance) {
		this(id, tb, beanItem.getItemStack(), min, max, chance);
	}
	
	public LootEntry(int id, LootTable tb, ItemStack itemStack, int min, int max, int chance) {
		this.id = id;
		this.table = tb;
		this.baseStack = itemStack;
		setMaxStackSize(max);
		setMinStackSize(min);
		this.chance = chance;
		this.dirty = false;
	}
	
	public LootEntry(int id, LootTable tb, Material material, int min, int max, int chance) {
		this(id, tb, new ItemStack(material, 1), min, max, chance);
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
		ItemStack clone = baseStack.clone();
		if ((flags & 1<<FLAG_LOOTING) != 0)
			clone.setAmount(minStack + getRandom().nextInt(maxStack + looting - minStack));
		else if (minStack >= maxStack)
			clone.setAmount(minStack);
		else
			clone.setAmount(minStack + getRandom().nextInt(maxStack - minStack));
		return clone;
	}
	
	public final ItemStack getDisplayStack() {
		return baseStack;
	}
	
	public int getChance() {
		return chance;
	}
	
	public boolean isGrindable() {
		return (flags & 1<<FLAG_GRINDABLE) != 0;
	}
	
	public boolean allowsLuck() {
		return (flags & 1<<FLAG_LUCK) != 0;
	}
	
	public boolean shouldCompress() {
		return (flags & 1<<FLAG_COMPRESS_STACK) != 0;
	}
	
	public boolean shouldAnnounce() {
		return (flags & 1<<FLAG_ANNOUNCE) != 0;
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
		if (size < 1/* || size > baseStack.getMaxStackSize()*/) { // Handled elsewhere by dividing the item stacks
			size = 1;
			throw new IllegalArgumentException("Invalid stacksize, forcefully set to 1.");
		}
		return (byte)size;
	}
	
	/**
	 * 
	 * @return
	 */
	public JSONObject getBonusData() {
		return null;
	}
}
