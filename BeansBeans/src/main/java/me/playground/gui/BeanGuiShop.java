package me.playground.gui;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import me.playground.items.BeanItem;
import me.playground.shop.Shop;
import me.playground.utils.Utils;
import net.kyori.adventure.text.Component;

public abstract class BeanGuiShop extends BeanGui {
	
	protected static final ItemStack shop_itemStorage = newItem(Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTA5MDVhYmE3MjNlOGY5N2JlZGNkMDYyM2Y0YzU1NzRlN2EyYzViZTNmMTFiYWViYTM4MzNjN2FkOTRlOTkzIn19fQ=="), "");
	protected static final ItemStack blank = newItem(new ItemStack(Material.YELLOW_STAINED_GLASS_PANE, 1), Component.empty());
	
	protected final Shop shop;
	
	public BeanGuiShop(Player p, Shop s) {
		super(p);
		
		this.shop = s;
		this.name = s.getOwnerId() > 0 ? "Player Shop" : "Server Shop";
		this.presetSize = 45;
		this.presetInv = null;
	}
	
	protected int[] quantities = { 1, 4, 8, 16, 64 };

	@Override
	public void onInventoryClosed(InventoryCloseEvent e) {
		
	}

	@Override
	public void onInventoryClicked(InventoryClickEvent e) {
		
	}

	@Override
	public void onInventoryOpened() {
		
	}
	
	protected boolean isSimilar(ItemStack item1, ItemStack item2) {
		int prioramt = item1.getAmount();
		BeanItem.resetItemFormatting(item1);
		item1.setAmount(1);
		item2.setAmount(1);
		boolean yes = item1.isSimilar(item2);
		item1.setAmount(prioramt);
		return yes;
	}
	
	protected int addToInvReturnLost(Player p, ItemStack item) {
        if (p.getInventory().firstEmpty() >= 0 && item.getAmount() <= item.getMaxStackSize()) {
        	p.getInventory().addItem(item);
            return 0;
        }
        Map<Integer, ? extends ItemStack> items = p.getInventory().all(item.getType());
        int amount = item.getAmount();
        for (ItemStack i : items.values()) {
            amount -= i.getMaxStackSize() - i.getAmount();
        }
        return amount;
    }
	
	protected void refreshShopViewers() {
		getAllViewers(BeanGuiShop.class).forEach((gui) -> {
			if (gui.getViewer() != p && gui.shop == shop) {
				gui.refresh(); 
			}
		});
	}
	
	protected void kickShopViewers() {
		getAllViewers(BeanGuiShop.class).forEach((gui) -> {
			if (gui.getViewer() != p && gui.shop == shop) {
				gui.close();
			}
		});
	}
	
}
