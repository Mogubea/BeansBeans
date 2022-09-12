package me.playground.loot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Steerable;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.playground.playerprofile.PlayerProfile;
import me.playground.playerprofile.stats.StatType;
import net.kyori.adventure.text.Component;

/**
 * Chainable
 */
public class LootRetriever {
	
	private final LootTable table;
	private final RetrieveMethod method;
	private final Player player;
	
	// CUMULATIVE
	private byte loops = 1;
	
	// GENERAL LOOT FLAGS
	private boolean creeper;
	private boolean skeleton;
	private boolean burn;
	private Biome biome;
	private float luckInfluence;
	private int lootingInfluence;
	private LivingEntity entity;
	
	private LootRetriever(LootTable table, RetrieveMethod method, Player p) {
		this.table = table;
		this.method = method;
		this.player = p;
	}
	
	/**
	 * Creates a new {@link LootRetriever} instance.
	 */
	public static LootRetriever from(@Nonnull LootTable table, @Nonnull RetrieveMethod method) {
		return from(table, method, null);
	}
	
	/**
	 * Creates a new {@link LootRetriever} instance.
	 * @throws NullPointerException If the method or table are null.
	 */
	
	public static LootRetriever from(@Nonnull LootTable table, @Nonnull RetrieveMethod method, Player p) {
		return new LootRetriever(table, method, p);
	}
	
	@Nonnull
	public List<ItemStack> getLoot() {
		final List<LootEntry> entries = table.getEntries();
		final List<ItemStack> stacks = new ArrayList<>();
		final int size = entries.size();
		
		switch(method) {
		case CUMULATIVE_CHANCE:
			final float totalChance = table.getTotalChance(luckInfluence);
			final List<Float> chanceCache = new ArrayList<>(); // micro-optimization for multiple loops.
			for (int loop = -1; ++loop < loops;) {
				float random = nextFloat(totalChance);
				for (int x = -1; ++x < size;) {
					LootEntry entry = entries.get(x);
					if (chanceCache.size() <= x) chanceCache.add(entry.getChance());
					float chance = chanceCache.get(x);
					
					if ((random-=chance) < 0) {
						ItemStack item = entry.generateReward(lootingInfluence, luckInfluence, burn);
						if (item.getAmount() < 1) continue;
						stacks.add(item);
						if (player != null)
							doStatStuff(player, entry, item);
						break;
					}
				}
			}
			break;
		case INDIVIDUAL_CHANCE:
			for (int x = -1; ++x < size;) {
				LootEntry entry = entries.get(x);
				if (entry.requiresPlayer() && player == null) continue;
				if (entry.requiresChargedCreeper() && !creeper) continue;
				if (entry.requiresSkeletonShot() && !skeleton) continue;
				if (entry.isBiomeExclusive() && biome != null && !entry.getRequiredBiomes().contains(biome)) continue;
				
				float chance = entry.getChance(luckInfluence);
				if (chance > nextFloat(100)) {
					ItemStack item = entry.generateReward(lootingInfluence, luckInfluence, burn);
					if (item.getAmount() < 1) continue;
					stacks.add(item);
					if (player != null)
						doStatStuff(player, entry, item);
				}
			}
			break;
		default:
			break;
		}
		
		if (entity != null)
			stacks.addAll(getStupidEntityLoot());
		
		return stacks;
	}
	
	/**
	 * Get unique loot that's exclusive to an entity's data and isn't really plausible to be overwritten in database form...
	 */
	@Nonnull
	private List<ItemStack> getStupidEntityLoot() {
		List<ItemStack> items = new ArrayList<>();
		
		// Steerable entities like Strider and Pig...
		if (entity instanceof Steerable) {
			if (((Steerable)entity).hasSaddle())
				items.add(new ItemStack(Material.SADDLE));
		// Entities that can pick up items...
		} else if (entity.getCanPickupItems() && entity.getEquipment() != null) {
			for (EquipmentSlot slot : EquipmentSlot.values())
				if (entity.getEquipment().getDropChance(slot) > table.getManager().getRandom().nextFloat())
					items.add(entity.getEquipment().getItem(slot));
		// Sheep and their wool...
		} else if (entity instanceof Sheep) {
			if (!((Sheep)entity).isSheared())
				items.add(new ItemStack(Material.valueOf(((Sheep)entity).getColor().name() + "_WOOL")));
		}
		
		return items;
	}
	
	/**
	 * The amount of times this {@link LootRetriever} will attempt to gather loot.
	 * Do note that the amount of loops is limited to {@link Byte#MAX_VALUE} with the minimum 
	 * amount of loops being 1.
	 * 
	 * <p>Only applicable when the RetrieveMethod is <b>not</b> {@link RetrieveMethod#INDIVIDUAL_CHANCE}.
	 */
	public LootRetriever loops(int loops) {
		if (loops < 1) loops = 1;
		else if (loops > Byte.MAX_VALUE) loops = Byte.MAX_VALUE;
		
		this.loops = (byte) loops;
		
		return this;
	}
	
	/**
	 * Allows the possibility of generating loot from entries with the {@link LootEntry#requiresChargedCreeper()} requirement.
	 * <p>Only applicable when the RetrieveMethod is {@link RetrieveMethod#INDIVIDUAL_CHANCE}.
	 */
	public LootRetriever creeper(boolean creeper) {
		this.creeper = creeper;
		return this;
	}
	
	/**
	 * Allows the possibility of generating loot from entries with the {@link LootEntry#requiresSkeletonShot()} requirement.
	 * <p>Only applicable when the RetrieveMethod is {@link RetrieveMethod#INDIVIDUAL_CHANCE}.
	 */
	public LootRetriever skeleton(boolean skeleton) {
		this.skeleton = skeleton;
		return this;
	}
	
	/**
	 * Allows the possibility of generating loot from entries with the {@link LootEntry#isBiomeExclusive()} requirement,
	 * assuming that {@link LootEntry#getRequiredBiomes()} contains the specified biome.
	 */
	public LootRetriever biome(Biome biome) {
		this.biome = biome;
		return this;
	}
	
	public LootRetriever luck(float luck) {
		this.luckInfluence = luck;
		return this;
	}
	
	public LootRetriever looting(int looting) {
		this.lootingInfluence = looting;
		return this;
	}
	
	/**
	 * If the loot generated can be burnt or cooked, do so.
	 */
	public LootRetriever burn(boolean burn) {
		this.burn = burn;
		return this;
	}
	
	/**
	 * Allows for hardcoded loot to be given based on the entity's data.
	 */
	public LootRetriever entity(LivingEntity entity) {
		this.entity = entity;
		return this;
	}
	
	private void doStatStuff(@Nonnull Player p, LootEntry entry, ItemStack item) {
		final PlayerProfile pp = PlayerProfile.from(p);
		if (entry.shouldAnnounce())
			p.sendMessage(Component.text("\u00a77You found ").append(toHover(item)).append(Component.text(" \u00a77(\u00a7f"+entry.getChance()+"% Chance\u00a77)!")));
		pp.getStats().addToStat(StatType.LOOT_EARNED, entry.getId()+"", item.getAmount());
	}
	
	private Component toHover(ItemStack item) {
		if (item == null || item.getType() == Material.AIR)
			return Component.empty();
		
		final ItemMeta meta = item.getItemMeta();
		final Component displayName = meta.hasDisplayName() ? meta.displayName() : Component.translatable(item.translationKey());

		return displayName.hoverEvent(item.asHoverEvent());
	}
	
	/**
	 * For slightly better accuracy than doing a nextInt((int) float).
	 */
	private float nextFloat(float bound) {
		return (float)table.getManager().getRandom().nextInt((int)(bound * 100F)) / 100F;
	}
	
}
