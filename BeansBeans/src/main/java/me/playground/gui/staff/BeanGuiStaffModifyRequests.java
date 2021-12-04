package me.playground.gui.staff;

import java.util.ArrayList;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import me.playground.playerprofile.ProfileModifyRequest;
import me.playground.playerprofile.ProfileStore;
import me.playground.utils.Utils;
import net.kyori.adventure.text.Component;

public class BeanGuiStaffModifyRequests extends BeanGuiStaff {
	
	private final int AMT = 21;
	
	public BeanGuiStaffModifyRequests(Player p) {
		super(p);
		
		this.name = "Staff Menu";
		this.presetSize = 54;
		this.presetInv = new ItemStack[] {
				bBlank,bBlank,bBlank,bBlank,null,bBlank,bBlank,bBlank,bBlank,
				bBlank,null,null,null,null,null,null,null,bBlank,
				bBlank,null,null,null,null,null,null,null,bBlank,
				bBlank,null,null,null,null,null,null,null,bBlank,
				bBlank,bBlank,bBlank,bBlank,null,bBlank,bBlank,bBlank,bBlank,
				blank,blank,blank,blank,goBack,blank,blank,blank,blank
		};
	}

	@Override
	public void onInventoryClosed(InventoryCloseEvent e) {}
	
	@Override
	public void onInventoryClicked(InventoryClickEvent e) {
		final int slot = e.getRawSlot();
		if (!(slot > 9 && slot < 35)) return;
		int idx = slot - 10 - (((slot-10) % 9) * 2) + (AMT * page);
		ProfileModifyRequest req = ProfileModifyRequest.getPendingRequests().get(idx);
		if (req == null) {
			p.sendMessage(Component.text("\u00a7cError: request idx " + idx + " does not exist in slot " + slot));
		} else {
			new BeanGuiStaffConfirmModify(p, req).openInventory();
		}
	}
	
	@Override
	public void onInventoryOpened() {
		final ItemStack[] contents = presetInv.clone();
		contents[4] = newItem(pp.getSkull(), pp.getHighestRank().toComponent().append(Component.text("\u00a7b Menu")), Component.text("\u00a77Nickname Requests"));
		ArrayList<ProfileModifyRequest> reqs = ProfileModifyRequest.getPendingRequests();
		int len = Math.min(reqs.size() - (page * AMT), AMT); 
		
		for (int x = -1; ++x < len;) {
			final int slot = 10 + ((x / 7) * 9) + (x % 7);
			ProfileModifyRequest req = reqs.get(x + (AMT * page));
			ProfileStore ps = ProfileStore.from(req.getPlayerId());
			contents[slot] = newItem(Utils.getSkullFromPlayer(ps.getUniqueId()), ps.getColouredName(), 
					Component.text("\u00a77Would like to change their"), 
					Component.text("\u00a77nickname to: \u00a7f" + req.getData() + "\u00a77."),
					Component.empty(),
					Component.text("\u00a77Requested " + Utils.timeStringFromNow(req.getRequestTime()) + " ago."),
					Component.text("\u00a78Click to review this request."));
		}
		
		i.setContents(contents);
	}
	
	@Override
	public boolean preInventoryClick(InventoryClickEvent e) {
		e.setCancelled(true);
		final ItemStack i = e.getCurrentItem();
		if (i == null) return true;
		
		if (pp.onCdElseAdd("guiClick", 300))
			return true;
			
		if (i.isSimilar(goBack)) {
			new BeanGuiStaff(p).openInventory();
			return true;
		} else if (i.isSimilar(nextPage)) {
			pageUp();
			return true;
		} else if (i.isSimilar(prevPage)) {
			pageDown();
			return true;
		}
		return false;
	}
	
}
