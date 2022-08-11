package me.playground.gui.stations;

import java.util.ArrayList;
import java.util.List;

import net.kyori.adventure.text.TextComponent;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.EnchantingTable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import me.playground.gui.BeanGui;
import me.playground.gui.BeanGuiConfirmPurchase;
import me.playground.main.Main;
import me.playground.menushop.PurchaseOption;
import net.kyori.adventure.text.Component;

public class BeanGuiEnchantingTableLapis extends BeanGui {

	protected static final ItemStack goBack = newItem(new ItemStack(Material.ENCHANTING_TABLE), Component.text("\u00a7cGo Back"), Component.text("\u00a77Return to Enchanting Table"));
	
	private static final List<PurchaseOption> lapisUpgrades = Main.getInstance().menuShopManager().getOrMakeShop("lazuliStorageCompartment").getPurchaseOptions();
	
	private final EnchantingTable table;
	private byte lapisLevel = 0;
	
	public BeanGuiEnchantingTableLapis(Player p, EnchantingTable table) {
		super(p);
		setName("Lazuli Storage Compartment™");
		this.presetSize = 36;
		
		this.presetInv = new ItemStack[] {
				bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,
				bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,
				bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,
				bBlank,bBlank,bBlank,bBlank,goBack,bBlank,bBlank,bBlank,bBlank
		};
		
		this.table = table;
		
		// Book Power
		if (table != null)
			lapisLevel = table.getPersistentDataContainer().getOrDefault(BeanGuiEnchantingTable.KEY_LAPIS_LEVEL, PersistentDataType.BYTE, (byte)0);
	}

	@Override
	public void onInventoryClosed(InventoryCloseEvent e) {
		
	}
	
	@Override
	public void onInventoryClicked(InventoryClickEvent e) {
		final int slot = e.getRawSlot();
		
		if (slot >= 10 && slot < 18) {
			PurchaseOption ppp = lapisUpgrades.get(slot - 10);
			if (lapisLevel + 1 == (slot - 10) && ppp.canPurchase(p)) {
				new BeanGuiConfirmPurchase(p, ppp) {

					@Override
					public void onAccept() {
						PersistentDataContainer pdc = table.getPersistentDataContainer();
						pdc.set(BeanGuiEnchantingTable.KEY_LAPIS_LEVEL, PersistentDataType.BYTE, (byte)(lapisLevel + 1));
						BeanGuiEnchantingTable.forceUpdateLapis(table);
						new BeanGuiEnchantingTableLapis(p, table).openInventory();
						p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.4F, 1.2F);
					}

					@Override
					public void onDecline() {
						new BeanGuiEnchantingTableLapis(p, table).openInventory();
					}
				}.openInventory();
			} else {
				p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.4F, 0.8F);
			}
		} else if (slot == 31) {
			new BeanGuiEnchantingTable(p, table).openInventory();
		}
	}
	
	@Override
	public void onInventoryOpened() {
		for (int x = -1; ++x < 7;) {
			List<TextComponent> lore = new ArrayList<>();
			if (lapisLevel + 1 < x)
				lore.add(Component.text("\u00a7c\u26a0 Requires the ").append(lapisUpgrades.get(x - 1).getDisplayName()).append(Component.text("\u00a7c.")));
			else if (lapisLevel + 1 > x)
				lore.add(Component.text("\u00a7c\u26a0 Already upgraded."));
			
			ItemStack displayItem = lapisUpgrades.get(x).getDisplayItem(p, lore, lore.isEmpty(), true);
			
			if (lapisLevel >= x) {
				displayItem.editMeta(meta -> {
					meta.addEnchant(Enchantment.ARROW_INFINITE, 1, false);
					meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
				});
			}
			
			i.setItem(10 + x, displayItem);
		}
	}
	
	@Override
	protected void playOpenSound() {
		p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 0.4F, 1.0F);
	}
	
}
