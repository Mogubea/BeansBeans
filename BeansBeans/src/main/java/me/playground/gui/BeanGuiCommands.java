package me.playground.gui;

import java.util.*;

import me.playground.command.BeanCommand;
import me.playground.main.Main;
import me.playground.utils.Utils;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import me.playground.ranks.Permission;
import me.playground.utils.BeanColor;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class BeanGuiCommands extends BeanGui {
	
	private static final ItemStack blank = newItem(new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE, 1), Component.text("Server Commands", BeanColor.COMMAND));
	private static final ItemStack blanc = newItem(new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1), Component.empty());
	private static final List<BeanCommand> allCommands = new ArrayList<>(Main.getInstance().commandManager().getMyBeanCommands().values());

	private final List<BeanCommand> visibleCommands = new ArrayList<>();
	private final Map<Integer, BeanCommand> mappings = new HashMap<>();

	public BeanGuiCommands(Player p) {
		super(p);

		for (BeanCommand command : allCommands) {
			if (command == null || !tpp.hasPermission(command.getPermissionString())) continue;
			visibleCommands.add(command);
		}

		this.presetSize = 54;
		preparePresetInventory(0);
	}

	@Override
	public void onInventoryClicked(InventoryClickEvent e) {
		int slot = e.getRawSlot();
		BeanCommand command = mappings.getOrDefault(slot, null);
		if (command == null) return;
		if (!tpp.hasPermission(Permission.DISABLE_COMMANDS)) return;

		BeanGui thisGui = this;
		new BeanGuiConfirm(p, List.of(Component.text(command.isEnabled() ? "Disable " : "Re-enable " + "/"+command.getName() + " for all players?"), Component.text("All Staff will be Notified."))) {
			public void onAccept() {
				command.setEnabled(!command.isEnabled());
				boolean on = command.isEnabled();
				Utils.notifyAllStaff(pp.getComponentName().append(Component.text(" has " + (on ? "re-enabled" : "disabled") + " the /"+command.getName()+" command.")),
						"Command " + (on ? "Re-enabled" : "Disabled"),
						pp.getDiscordMember() == null ? pp.getDisplayName() : pp.getDiscordMember().getAsMention() +
								" has " + (on ? "re-enabled" : "disabled") + " the /"+command.getName()+" command.");
				thisGui.presetInv[slot] = getCommandItem(command); // Update the item visual in the ui
				thisGui.openInventory();
			}

			public void onDecline() {
				thisGui.openInventory();
			}
		}.openInventory();
	}

	@Override
	public void pageUp() {
		preparePresetInventory(++this.page);
		openInventory();
	}

	@Override
	public void pageDown() {
		preparePresetInventory(--this.page);
		openInventory();
	}

	/**
	 * Set the new Inventory for when the inventory gets opened again.
	 */
	private void preparePresetInventory(int page) {
		ItemStack[] newInv = new ItemStack[] {
				blank,blank,bBlank,bBlank,null,bBlank,bBlank,blank,blank,
				blank,blanc,blanc,blanc,blanc,blanc,blanc,blanc,blank,
				bBlank,blanc,blanc,blanc,blanc,blanc,blanc,blanc,bBlank,
				bBlank,blanc,blanc,blanc,blanc,blanc,blanc,blanc,bBlank,
				bBlank,blanc,blanc,blanc,blanc,blanc,blanc,blanc,bBlank,
				blank,blank,blank,blank,goBack,blank,blank,blank,blank
		};

		mappings.clear();

		int maxPerPage = 28;
		int size = visibleCommands.size();
		int maxThisPage = Math.min(size - (maxPerPage * page), maxPerPage);
		this.page = Math.min(page, size/maxPerPage);
		int maxPages = size/maxPerPage + 1;

		newInv[48] = page > 0 ? prevPage : blank;
		newInv[50] = size > (maxPerPage * (page+1)) ? nextPage : blank;

		// Loop through all visible commands while respecting the current page and max page limits.
		for (int rx = maxThisPage, idx = (maxPerPage * page); rx > 0; ++idx) {
			int slot = (maxThisPage-rx + 10 + (((maxThisPage-rx) / 7) * 2)); // Calculate the slot position
			if (idx >= size) break; // If exceeds the array size, break

			BeanCommand command = visibleCommands.get(idx);
			newInv[slot] = getCommandItem(command);
			mappings.put(slot, command);
			rx--;
		}

		// Set the title of the page, include the current page number and maximum number of pages.
		String title = "Server Commands" + (maxPages > 1 ? " ("+(page+1)+"/"+maxPages+")" : "");
		setName(title);

		this.presetInv = newInv;
	}

	@NotNull
	private ItemStack getCommandItem(@NotNull BeanCommand command) {
		ItemStack test = newItem(new ItemStack(Material.PIGLIN_BANNER_PATTERN), Component.text("/"+command.getName(), command.isEnabled() ? BeanColor.COMMAND : NamedTextColor.DARK_GRAY));
		test.editMeta(meta -> {
			List<Component> lore = new ArrayList<>();
			lore.add(Component.text("\u00a78\u00a7i\"" + command.getDescription() + "\""));
			if (tpp.hasPermission(Permission.MODIFY_PERMISSIONS))
				lore.add(Component.text("\u00a77Permission: \u00a7f" + command.getPermissionString()));

			if (!command.isEnabled()) {
				lore.add(Component.empty());
				lore.add(Component.text("\u00a7c\u26a0 Currently disabled."));
				meta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
			}

			// Can only disable commands that you have permission to use.
			if (tpp.hasPermission(Permission.DISABLE_COMMANDS) && tpp.hasPermission(command.getPermissionString())) {
				lore.add(Component.empty());
				lore.add(Component.text("\u00a76» \u00a7eClick to " + (command.isEnabled() ? "disable" : "re-enable") + "!"));
			}
			meta.lore(lore);
		});
		return test;
	}
	
}
