package me.playground.gui;

import java.util.LinkedHashMap;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import me.playground.loot.LootTable;
import me.playground.playerprofile.stats.StatType;
import me.playground.utils.BeanColor;
import me.playground.utils.Utils;
import net.kyori.adventure.text.Component;

public class BeanGuiBestiaryEntity extends BeanGuiBestiary {
	
	// TODO: move all skulls to a class that holds them in non-static references maybe?
	private final static LinkedHashMap<EntityType, ItemStack> creatureHeads = new LinkedHashMap<EntityType, ItemStack>();
	private final static EntityType[] creatures;
	private final ItemStack missingKill = newItem(notUnlocked, "\u00a7c???", "\u00a78Find and kill this mob to", "\u00a78add it to your Bestiary!");
	protected final ItemStack whatIsThis = newItem(new ItemStack(Material.KNOWLEDGE_BOOK), Component.text("What is the Bestiary?", BeanColor.BESTIARY), "", 
			"\u00a77The \u00a72Bestiary\u00a77 is an interface where",
			"\u00a77you can view information about various",
			"\u00a77mobs you've encountered on your adventure!",
			"",
			"\u00a77When viewing loot information, your \u00a7aLuck Level",
			"\u00a77and \u00a7bLooting Enchantments\u00a77 will affect what's shown!",
			"\u00a77 * \u00a7aLuck Level\u00a77 affects the \u00a7fChances\u00a77.",
			"\u00a77 * \u00a7bLooting\u00a77 affects the \u00a7eMaximum Quantity\u00a77.");
	
	static {
		creatureHeads.put(EntityType.BLAZE, Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjc4ZWYyZTRjZjJjNDFhMmQxNGJmZGU5Y2FmZjEwMjE5ZjViMWJmNWIzNWE0OWViNTFjNjQ2Nzg4MmNiNWYwIn19fQ=="));
		creatureHeads.put(EntityType.CHICKEN, Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTYzODQ2OWE1OTljZWVmNzIwNzUzNzYwMzI0OGE5YWIxMWZmNTkxZmQzNzhiZWE0NzM1YjM0NmE3ZmFlODkzIn19fQ=="));
		creatureHeads.put(EntityType.COW, Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2RmYTBhYzM3YmFiYTJhYTI5MGU0ZmFlZTQxOWE2MTNjZDYxMTdmYTU2OGU3MDlkOTAzNzQ3NTNjMDMyZGNiMCJ9fX0="));
		creatureHeads.put(EntityType.CREEPER, new ItemStack(Material.CREEPER_HEAD));
		creatureHeads.put(EntityType.DROWNED, Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzg0ZGY3OWM0OTEwNGIxOThjZGFkNmQ5OWZkMGQwYmNmMTUzMWM5MmQ0YWI2MjY5ZTQwYjdkM2NiYmI4ZTk4YyJ9fX0="));
		creatureHeads.put(EntityType.ENDERMAN, Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTZjMGIzNmQ1M2ZmZjY5YTQ5YzdkNmYzOTMyZjJiMGZlOTQ4ZTAzMjIyNmQ1ZTgwNDVlYzU4NDA4YTM2ZTk1MSJ9fX0="));
		creatureHeads.put(EntityType.HUSK, Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDY3NGM2M2M4ZGI1ZjRjYTYyOGQ2OWEzYjFmOGEzNmUyOWQ4ZmQ3NzVlMWE2YmRiNmNhYmI0YmU0ZGIxMjEifX19"));
		creatureHeads.put(EntityType.PHANTOM, Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzQ2ODMwZGE1ZjgzYTNhYWVkODM4YTk5MTU2YWQ3ODFhNzg5Y2ZjZjEzZTI1YmVlZjdmNTRhODZlNGZhNCJ9fX0="));
		creatureHeads.put(EntityType.PIG, Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjIxNjY4ZWY3Y2I3OWRkOWMyMmNlM2QxZjNmNGNiNmUyNTU5ODkzYjZkZjRhNDY5NTE0ZTY2N2MxNmFhNCJ9fX0="));
		creatureHeads.put(EntityType.PIGLIN, Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTBiYzlkYmI0NDA0YjgwMGY4Y2YwMjU2MjIwZmY3NGIwYjcxZGJhOGI2NjYwMGI2NzM0ZjRkNjMzNjE2MThmNSJ9fX0="));
		creatureHeads.put(EntityType.PIGLIN_BRUTE, Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2UzMDBlOTAyNzM0OWM0OTA3NDk3NDM4YmFjMjllM2E0Yzg3YTg0OGM1MGIzNGMyMTI0MjcyN2I1N2Y0ZTFjZiJ9fX0="));
		creatureHeads.put(EntityType.ZOMBIFIED_PIGLIN, Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTkzNTg0MmFmNzY5MzgwZjc4ZThiOGE4OGQxZWE2Y2EyODA3YzFlNTY5M2MyY2Y3OTc0NTY2MjA4MzNlOTM2ZiJ9fX0="));
		creatureHeads.put(EntityType.SHEEP, Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjMxZjljY2M2YjNlMzJlY2YxM2I4YTExYWMyOWNkMzNkMThjOTVmYzczZGI4YTY2YzVkNjU3Y2NiOGJlNzAifX19"));
		creatureHeads.put(EntityType.SKELETON, new ItemStack(Material.SKELETON_SKULL));
		creatureHeads.put(EntityType.SPIDER, Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzg3YTk2YThjMjNiODNiMzJhNzNkZjA1MWY2Yjg0YzJlZjI0ZDI1YmE0MTkwZGJlNzRmMTExMzg2MjliNWFlZiJ9fX0="));
		creatureHeads.put(EntityType.STRAY, Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmM1MDk3OTE2YmMwNTY1ZDMwNjAxYzBlZWJmZWIyODcyNzdhMzRlODY3YjRlYTQzYzYzODE5ZDUzZTg5ZWRlNyJ9fX0="));
		creatureHeads.put(EntityType.WITHER, Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2RmNzRlMzIzZWQ0MTQzNjk2NWY1YzU3ZGRmMjgxNWQ1MzMyZmU5OTllNjhmYmI5ZDZjZjVjOGJkNDEzOWYifX19"));
		creatureHeads.put(EntityType.WITHER_SKELETON, new ItemStack(Material.WITHER_SKELETON_SKULL));
		creatureHeads.put(EntityType.ZOMBIE, new ItemStack(Material.ZOMBIE_HEAD));
		
		creatures = creatureHeads.keySet().toArray(new EntityType[0]);
	}
	
	private EntityType entity;
	//private boolean passive;
	
	public BeanGuiBestiaryEntity(Player p) {
		super(p);
		
		setName("Bestiary");
		this.presetSize = 54;
		this.presetInv = new ItemStack[] {
				blank,blank,blank,blank,null,blank,blank,blank,blank,
				blank2,blank2,blank2,blank,blank,blank,blank2,blank2,blank2,
				blank2,null,null,null,null,null,null,null,blank2,
				blank2,null,null,null,null,null,null,null,blank2,
				blank2,blank2,blank2,blank2,blank2,blank2,blank2,blank2,blank2,
				blank,blank,blank,whatIsThis,goBack,blank,blank,blank,blank
		};
	}

	@Override
	public void onInventoryClosed(InventoryCloseEvent e) {}

	@Override
	public void onInventoryClicked(InventoryClickEvent e) {
		if (getEntityType() != null) {
			super.onInventoryClicked(e);
		} else {
			if (e.getRawSlot() >= creatures.length) return;
			setEntityType(creatures[e.getRawSlot()]);
		}
	}
	
	@Override
	public void onInventoryOpened() {
		final ItemStack[] contents = presetInv.clone();
		
		// XXX: Statistics and information about the entity in question.
		if (getEntityType() != null) {
			showLoot(contents, getPlugin().lootManager().getLootTable(getEntityType()), false, 2, 7);
			contents[4] = newItem(creatureHeads.get(getEntityType()), Component.translatable(getEntityType().translationKey()).color(BeanColor.BESTIARY));
		// XXX: A list of the entities available to examine in the Beastiary.
		} else {
			int size = creatures.length;
			for (int x = -1; ++x < size;) {
				int kills = getStats().getStat(StatType.KILLS, creatures[x].name());
				if (kills > 0) { // Unlocked
					int obtained = 0, loots = 0;
					LootTable table = getPlugin().lootManager().getLootTable(creatures[x]);
					if (table != null) {
						loots = table.getEntries().size();
						for (int e = -1; ++e < loots;)
							if (getStats().getStat(StatType.LOOT_EARNED, table.getEntries().get(e).getId()+"") > 0)
								obtained++;
					}
					
					ItemStack displayItem = newItem(creatureHeads.getOrDefault(creatures[x], notUnlocked), Component.translatable(creatures[x].translationKey(), BeanColor.BESTIARY), "",
							"\u00a77Kills: \u00a7a" + df.format(getStats().getStat(StatType.KILLS, creatures[x].name())),
							"\u00a77Loot Found: " + "\u00a7a" + obtained + "\u00a77/\u00a72" + loots,
							Utils.getProgressBar('-', 16, obtained, loots, 0x444444, 0x55ffff) +  (obtained>=loots ? "\u00a76 " : "\u00a7a ") + dec.format((((float)obtained/(float)loots) * 100F)) + "%");
					contents[x] = displayItem;
				} else { // Not
					ItemStack displayItem = missingKill;
					contents[x] = displayItem;
				}
			}
		}
		
		i.setContents(contents);
	}
	
	private EntityType getEntityType() {
		return entity;
	}
	
	private void setEntityType(EntityType type) {
		this.entity = type;
		onInventoryOpened();
	}
	
	@Override
	public boolean preInventoryClick(InventoryClickEvent e) {
		if (e.getRawSlot() == 49 && !pp.onCdElseAdd("guiClick", 300, true)) {
			e.setCancelled(true);
			if (getEntityType() != null)
				setEntityType(null);
			else
				new BeanGuiBestiary(p).openInventory();
			return true;
		}
		
		return super.preInventoryClick(e);
	}
	
}
