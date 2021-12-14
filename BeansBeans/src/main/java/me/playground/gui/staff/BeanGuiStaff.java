package me.playground.gui.staff;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import me.playground.gui.BeanGui;
import me.playground.playerprofile.ProfileModifyRequest;
import me.playground.ranks.Rank;
import me.playground.utils.BeanColor;
import net.kyori.adventure.text.Component;

public class BeanGuiStaff extends BeanGui {
	
	protected static final ItemStack blank = newItem(new ItemStack(Material.BLUE_STAINED_GLASS_PANE, 1), "");
	
	public BeanGuiStaff(Player p) {
		super(p);
		
		this.name = "Staff Menu";
		this.presetSize = 54;
		this.presetInv = new ItemStack[] {
				blank,blank,bBlank,bBlank,null,bBlank,bBlank,blank,blank,
				blank,null,null,null,null,null,null,null,blank,
				bBlank,null,null,null,null,null,null,null,bBlank,
				bBlank,null,null,null,null,null,null,null,bBlank,
				bBlank,bBlank,bBlank,bBlank,null,bBlank,bBlank,bBlank,bBlank,
				blank,blank,blank,blank,goBack,blank,blank,blank,blank
		};
	}

	@Override
	public void onInventoryClosed(InventoryCloseEvent e) {
	}
	
	@Override
	public void onInventoryClicked(InventoryClickEvent e) {
		final int slot = e.getRawSlot();
		
		switch(slot) {
		case 19: // TEST LIST FOR MODIFY REQUSTS
			new BeanGuiStaffModifyRequests(p).openInventory();
			return;
		}
	}
	
	@Override
	public void onInventoryOpened() {
		final ItemStack[] contents = presetInv.clone();
		contents[4] = newItem(new ItemStack(Material.ANVIL), Component.text("Staff Menu", BeanColor.STAFF));
		
		contents[19] = newItem(new ItemStack(Material.NAME_TAG), Component.text("Nickname Requests"),
				Component.text("\u00a77There are currently \u00a7b" + ProfileModifyRequest.getPendingRequests().size() + "\u00a77"),
				Component.text("\u00a77pending nickname requests."));
		
		i.setContents(contents);
	}
	
	@Override
	public void openInventory() {
		if (pp.isRank(Rank.MODERATOR))
			super.openInventory();
		else
			p.sendActionBar(Component.text("\u00a7cYou don't have permission to view that!"));
	}
	
	protected void refreshStaffViewers() {
		getAllViewers(BeanGuiStaff.class).forEach((gui) -> {
			if (gui.getViewer() != p) {
				gui.refresh(); 
			}
		});
	}
	
}
