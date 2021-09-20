package me.playground.loot;

import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Nonnull;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.playground.playerprofile.PlayerProfile;
import me.playground.playerprofile.stats.StatType;
import net.kyori.adventure.text.Component;

/**
 * LootTable instances are not saved at all, but rather are created on Server Startup 
 * every time there's a new table name addressed when loading Loot Entries.
 */
public class LootTable {
	
	private final String name;
	private final LootManager manager;
	private final ArrayList<LootEntry> entries = new ArrayList<LootEntry>();
	
	private int totalChance;
	
	public LootTable(LootManager lm, String name) {
		this.manager = lm;
		this.name = name;
	}
	
	public LootManager getManager() {
		return manager;
	}
	
	public String getName() {
		return name;
	}
	
	public LootTable setEntries(ArrayList<LootEntry> entries) {
		this.entries.clear();
		setEntries(entries);
		recalcTotalChance();
		return this;
	}
	
	public LootTable addEntry(LootEntry entry) {
		if (entry.getTable() != this) throw new IllegalArgumentException("Entry's designated loot table is not equal to this loot table.");
		totalChance += entry.getChance();
		this.entries.add(entry);
		return this;
	}
	
	/**
	 * This method is to be used for systems that should guarantee an item every loop (eg. Fishing or Event Rewards).
	 * 
	 * <p>Every loop will 100% guarantee an item as it grabs the {@link #totalChance} and selects an item that way.
	 * 
	 * <p>Looting and Luck within this system do not affect the chances of getting a specific drop, but still affect the Loot Stack.
	 * @param loops - The amount of items, these items can be the same.
	 * @param player - If a player influenced the drops, used for {@link LootEntry#isGrindable()}.
	 * @return A collection of itemstacks based on the parameters given.
	 */
	public ArrayList<ItemStack> getRewardsFromSystem1(int loops, int looting, float luck) {
		ArrayList<ItemStack> stacks = new ArrayList<ItemStack>(loops);
		
		int size = entries.size();
		for (int loop = -1; ++loop < loops;) {
			int random = manager.getRandom().nextInt(totalChance);
			for (int x = -1; ++x < size;) {
				LootEntry entry = entries.get(x);
				if ((random-=entry.getChance()) < 0) {
					ItemStack item = entry.generateReward(looting, luck);
					if (item.getAmount() < 1) continue;
					stacks.add(item);
					break;
				}
			}
		}
		
		return stacks;
	}
	
	/**
	 * This method is to be used for systems that rely on pure odds to get an item, but can get several of these items in one attempt 
	 * if lucky enough, or none at all (eg. Mob Drops).
	 * 
	 * <p>Every entry is looped through, and the odds to add an entry to the final collection is based on <b>({@link LootEntry#getChance()} 
	 * * (1 + luck/5)) / nextInt(1000000)</b>. The luck calculation is only included if {@link LootEntry#allowsLuck()} is enabled.
	 * 
	 * <p>Looting and Luck within this system DO affect the chances of getting a specific drop as well as affecting the drop itself.
	 * This method does not guarantee any drops unless drops are specifically mentioned to be guaranteed.
	 * @return A collection of itemstacks based on the parameters given.
	 */
	public Collection<ItemStack> getRewardsFromSystem2(@Nonnull Player p, int looting, float luck) {
		return getRewardsFromSystem2(p, looting, luck, false, false);
	}
	
	/**
	 * This method is to be used for systems that rely on pure odds to get an item, but can get several of these items in one attempt 
	 * if lucky enough, or none at all (eg. Mob Drops).
	 * 
	 * <p>Every entry is looped through, and the odds to add an entry to the final collection is based on <b>({@link LootEntry#getChance()} 
	 * * (1 + luck/5)) / nextInt(1000000)</b>. The luck calculation is only included if {@link LootEntry#allowsLuck()} is enabled.
	 * 
	 * <p>Looting and Luck within this system DO affect the chances of getting a specific drop as well as affecting the drop itself.
	 * This method does not guarantee any drops unless drops are specifically mentioned to be guaranteed.
	 * @return A collection of itemstacks based on the parameters given.
	 */
	public Collection<ItemStack> getRewardsFromSystem2(@Nonnull Player p, int looting, float luck, boolean skeletonKill, boolean creeperKill) {
		ArrayList<ItemStack> stacks = new ArrayList<ItemStack>();
		
		final int size = entries.size();
		final PlayerProfile pp = PlayerProfile.from(p);
		
		for (int x = -1; ++x < size;) {
			LootEntry entry = entries.get(x);
			if (entry.requiresChargedCreeper() && !creeperKill) continue;
			if (entry.requiresSkeletonShot() && !skeletonKill) continue;
			
			float chance = entry.getChance(entry.allowsLuck() ? luck : 0);
			if (chance > getManager().getRandom().nextInt(1000000)) {
				ItemStack item = entry.generateReward(looting, luck);
				if (item.getAmount() < 1) continue;
				stacks.add(item);
				if (entry.shouldAnnounce())
					p.sendMessage(Component.text("\u00a77You found ").append(toHover(item)).append(Component.text(" \u00a77(\u00a7f"+(chance/10000)+"% Chance\u00a77)")));
				pp.getStats().addToStat(StatType.LOOT_EARNED, entry.getId()+"", item.getAmount());
			}
		}
		
		return stacks;
	}
	
	private Component toHover(ItemStack item) {
		if (item == null || item.getType() == Material.AIR)
			return Component.empty();
		
		final ItemMeta meta = item.getItemMeta();
		final Component displayName = meta.hasDisplayName() ? meta.displayName() : Component.text(item.getI18NDisplayName());
		
		return displayName.hoverEvent(item.asHoverEvent());
	}
	
	private void recalcTotalChance() {
		int size = entries.size();
		for (int x = -1; ++x < size;)
			totalChance += entries.get(x).getChance();
	}
	
	/**
	 * Any changes made to the entries in this table will appear in the Loot Table.
	 * @return list of entries straight from the Loot Table.
	 */
	public ArrayList<LootEntry> getEntries() {
		return this.entries;
	}
	
}
