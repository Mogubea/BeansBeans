package me.playground.gui;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.playground.main.Main;
import me.playground.ranks.Permission;
import me.playground.utils.BeanColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

public class BeanGuiCommands extends BeanGui {
	
	protected static final ItemStack blank = newItem(new ItemStack(Material.LIME_STAINED_GLASS_PANE, 1), "\u00a7aCommands");
	
	final List<Command> cmds = Main.getCommandManager().getMyCommands();
	
	public BeanGuiCommands(Player p) {
		super(p);
		
		this.name = "Commands";
		this.presetSize = 54;
		this.presetInv = new ItemStack[] {
				null,null,null,null,null,null,null,null,null,
				null,null,null,null,null,null,null,null,null,
				null,null,null,null,null,null,null,null,null,
				null,null,null,null,null,null,null,null,null,
				null,null,null,null,null,null,null,null,null,
				blank,blank,blank,blank,goBack,blank,blank,blank,blank
		};
	}

	@Override
	public void onInventoryClosed(InventoryCloseEvent e) {
	}

	@Override
	public void onInventoryClicked(InventoryClickEvent e) {
	}

	@Override
	public void onInventoryOpened() {
		final ItemStack[] contents = presetInv.clone();
		contents[46] = page > 0 ? prevPage : blank;
		
		int x = 0 + (page * 45);
		for (Command c : cmds) {
			if (x > (44 + (page * 45))) {
				contents[52] = nextPage;
				break;
			}
			
			if (!c.getPermission().isEmpty() && !pp.hasPermission(c.getPermission()))
				continue;
			ItemStack test = new ItemStack(Material.FILLED_MAP);
			ItemMeta testm = test.getItemMeta();
			testm.displayName(Component.text("/"+c.getName()).color(BeanColor.COMMAND).decoration(TextDecoration.ITALIC, false));
			
			ArrayList<Component> ack = new ArrayList<Component>();
			ack.add(Component.text("\u00a78\u00a7i\"" + c.getDescription() + "\""));
			if (pp.hasPermission(Permission.MODIFY_PERMISSIONS))
				ack.add(Component.text("\u00a77Permission: \u00a7f" + c.getPermission()));
			testm.lore(ack);
			test.setItemMeta(testm);
			
			contents[x - (page * 45)] = test;
			x++;
		}
		i.setContents(contents);
	}
	
}
