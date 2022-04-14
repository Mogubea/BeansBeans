package me.playground.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import me.playground.utils.BeanColor;
import net.kyori.adventure.text.Component;

public class BeanGuiBestiaryFishing extends BeanGuiBestiary {
	
	protected final ItemStack missingLoot = newItem(notUnlocked, "\u00a7c???", "\u00a78Unlock information about this unknown", "\u00a78piece of loot by fishing it up first!");
	protected final ItemStack whatIsThis = newItem(new ItemStack(Material.KNOWLEDGE_BOOK), Component.text("What is the Bestiary?", BeanColor.BESTIARY), "", 
			"\u00a77The \u00a72Bestiary\u00a77 is an interface where",
			"\u00a77you can view information about various",
			"\u00a77things you've encountered in your adventure!",
			"",
			"\u00a77When viewing fishing information, your \u00a7aLuck Level",
			"\u00a77and \u00a7aLuck Enchants\u00a77 will affect the \u00a7fChances \u00a77of",
			"\u00a77all of the items shown!");
	
	public BeanGuiBestiaryFishing(Player p) {
		super(p);
		
		this.name = "Bestiary";
		this.presetSize = 54;
		this.presetInv = new ItemStack[] {
				blank,blank,blank,blank,null,blank,blank,blank,blank,
				blank2,blank2,blank2,blank,blank,blank,blank2,blank2,blank2,
				null,null,null,null,null,null,null,null,null,
				null,null,null,null,null,null,null,null,null,
				null,null,null,null,null,null,null,null,null,
				blank,blank,blank,whatIsThis,goBack,blank,blank,blank,blank
		};
	}
	
	@Override
	public void onInventoryOpened() {
		final ItemStack[] contents = presetInv.clone();
		showLoot(contents, getPlugin().lootManager().getLootTable("fishing"), true, 3, 9);
		contents[4] = newItem(new ItemStack(Material.FISHING_ROD), Component.text("Fishing Rewards").color(BeanColor.BESTIARY));	
		i.setContents(contents);
	}
	
	@Override
	public boolean preInventoryClick(InventoryClickEvent e) {
		if (e.getRawSlot() == 49 && !pp.onCdElseAdd("guiClick", 300, true)) {
			new BeanGuiBestiary(p).openInventory();
			return true;
		}
		
		return super.preInventoryClick(e);
	}
	
}
