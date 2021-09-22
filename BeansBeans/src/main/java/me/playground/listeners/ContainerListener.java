package me.playground.listeners;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.SmithingInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import com.destroystokyo.paper.event.inventory.PrepareResultEvent;

import me.playground.gui.BeanGui;
import me.playground.gui.BeanGuiMainMenu;
import me.playground.items.BeanItem;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.playerprofile.skills.SkillInfo;
import me.playground.playerprofile.skills.SkillType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

public class ContainerListener extends EventListener {
	
	public ContainerListener(Main plugin) {
		super(plugin);
	}
	
	@EventHandler
	public void onCraft(PrepareResultEvent e) {
		ItemStack i = e.getResult();
		if (i != null) {
			final Inventory inv = e.getInventory();
			final Material type = i.getType();
			
			if (inv instanceof AnvilInventory || inv instanceof SmithingInventory) {
				if (type.getMaxDurability() > 1) {
					float duraPerc = ((float)type.getMaxDurability() - (float)((Damageable)i.getItemMeta()).getDamage()) / (float)type.getMaxDurability();
					ItemStack original = inv.getContents()[0];
					if (original != null && original.getType() != i.getType()) {
						// If tool's name is base item name, update it to new item name - Only smithing
						if (inv instanceof SmithingInventory) {
							if (original.hasItemMeta() && original.getItemMeta().hasDisplayName() && ((TextComponent)original.getItemMeta().displayName()).content().equals(original.getI18NDisplayName())) {
								ItemMeta meta = i.getItemMeta();
								meta.displayName(Component.text(i.getI18NDisplayName()));
								i.setItemMeta(meta);
							}
						}
						i = BeanItem.setDurability(i, (int) (duraPerc * ((float)type.getMaxDurability())), type.getMaxDurability());
					} else {
						i = BeanItem.setDurability(i, (int) (duraPerc * ((float)BeanItem.getMaxDurability(i))));
					}
				}
				
				i = BeanItem.formatItem(i);
			} else {
				i = BeanItem.resetItemFormatting(i);
			}
			
		}
		e.setResult(i);
	}
	
	@EventHandler
	public void onCraft(PrepareItemCraftEvent e) {
		if (e.getInventory() instanceof AnvilInventory || e.getInventory() instanceof SmithingInventory) return;
		if (e.getRecipe() == null) return;
		
		// All Bukkit recipe instances implement Keyed.
		NamespacedKey key = ((Keyed)e.getRecipe()).getKey();
		if (!key.getNamespace().equals(NamespacedKey.MINECRAFT)) {
			if (!e.getView().getPlayer().hasDiscoveredRecipe(key))
				e.getInventory().setResult(null);
			e.getView().getPlayer().sendActionBar(Component.text("\u00a7cYou have not unlocked this recipe."));
			return;
		}
		
		ItemStack i = e.getInventory().getResult();
		
		if (i != null)
			e.getInventory().setResult(BeanItem.formatItem(i));
	}
	
	@EventHandler
	public void onEnchant(EnchantItemEvent e) {
		Bukkit.getServer().getScheduler().runTask(getPlugin(), () -> BeanItem.formatItem(e.getItem()));
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent e) {
		List<HumanEntity> list = e.getViewers();
		if (list.isEmpty())
			return;
		
		PlayerProfile pp = PlayerProfile.from((Player) list.get(0));
		if (pp.getBeanGui() != null) {
			pp.getBeanGui().onInventoryClosed(e);
			pp.closeBeanGui();
		}
	}
	
	@EventHandler
	public void onAnvilPrepare(PrepareAnvilEvent e) {
		final SkillInfo skillInfo = PlayerProfile.from(e.getView().getPlayer()).getSkills().getSkillInfo(SkillType.REPAIR);
		
		// Min requirement
		if (skillInfo.getLevel() >= 10) {
			final AnvilInventory inv = e.getInventory();
			
			if (inv.getContents()[1] != null && inv.getContents()[1].getType() != Material.ENCHANTED_BOOK) {
				float oldCost = inv.getRepairCost();
				float newCost = Math.max(1, oldCost - Math.min(15, ((float)skillInfo.getLevel() / 60F)));
				
				Bukkit.getServer().getScheduler().runTask(getPlugin(), () -> inv.setRepairCost((int) newCost));
			}
		}
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if (e.getSlotType() == SlotType.RESULT) {
			if (e.getInventory() instanceof AnvilInventory) {
				if (e.getCurrentItem() == null) // prevents getting infinite repair xp by clicking empty slot
					return;
				AnvilInventory inv = (AnvilInventory) e.getInventory();
				if (inv.getContents()[1] != null && inv.getContents()[1].getType() != Material.ENCHANTED_BOOK) {
					int cost = inv.getRepairCost();
					
					if (cost < 1)
						return;
					int playerXp = ((Player)e.getView().getPlayer()).getLevel();
					if (playerXp >= cost)
						PlayerProfile.from(e.getView().getPlayer()).getSkills().addXp(SkillType.REPAIR, cost*200);
				}
			}
			return;
		}
		
		if (e.getSlotType() != SlotType.CONTAINER && e.getSlotType() != SlotType.QUICKBAR)
			return;
		
		final ItemStack stack = e.getClickedInventory().getItem(e.getSlot());
		final boolean menuItem = stack != null && (stack.equals(BeanGui.menuItem));
		
		if (menuItem)
			e.setCancelled(true);
		
		final Player p = (Player) e.getView().getPlayer();
		final PlayerProfile pp = PlayerProfile.from(p);
		
		BeanGui bui = pp.getBeanGui();
		
		if (menuItem) {
			if (bui == null || !(bui instanceof BeanGuiMainMenu)) {
				new BeanGuiMainMenu(p).openInventory();
				return;
			}
		}
		
		// No point in firing this if the item is null.
		if (bui != null && e.getCurrentItem() != null && !bui.preInventoryClick(e))
			bui.onInventoryClicked(e);
	}
	
	@EventHandler
	public void onLootGeneration(LootGenerateEvent e) {
		for (ItemStack i : e.getLoot())
			BeanItem.formatItem(i);
	}
	
}
