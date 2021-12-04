package me.playground.gui.staff;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import me.playground.gui.BeanGuiConfirm;
import me.playground.playerprofile.ProfileModifyRequest;
import me.playground.playerprofile.ProfileStore;
import net.kyori.adventure.text.Component;

public class BeanGuiStaffConfirmModify extends BeanGuiConfirm {
	
	protected static final ItemStack confirm = newItem(new ItemStack(Material.LIME_STAINED_GLASS_PANE, 1), "\u00a7aApprove Nickname Change");
	protected static final ItemStack cancel = newItem(new ItemStack(Material.RED_STAINED_GLASS_PANE, 1), "\u00a7cDeny Nickname Change");
	
	private final ProfileModifyRequest request;
	
	public BeanGuiStaffConfirmModify(Player p, ProfileModifyRequest request) {
		super(p, Arrays.asList(
				ProfileStore.from(request.getPlayerId()).getColouredName().append(Component.text("\u00a77 would like the nickname:")),
				Component.text("\u00a7f" + request.getData()),
				Component.empty(),
				Component.text("\u00a77If you believe this new nickname is appropriate"),
				Component.text("\u00a77then \u00a7aapprove it by clicking on the left\u00a77. Otherwise"),
				Component.text("\u00a7cdecline it by clicking on the right\u00a77.")));
		
		this.request = request;
	}

	@Override
	public void onInventoryClosed(InventoryCloseEvent e) {}
	
	public void onAccept() {
		request.approve(pp.getId(), System.currentTimeMillis());
		new BeanGuiStaffModifyRequests(p).openInventory();
	}
	
	public void onDecline() {
		request.deny(pp.getId(), System.currentTimeMillis());
		new BeanGuiStaffModifyRequests(p).openInventory();
	}
	
}
