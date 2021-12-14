package me.playground.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import me.playground.playerprofile.stats.StatType;
import me.playground.utils.BeanColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

public class BeanGuiSupportUs extends BeanGui {
	
	protected static final ItemStack blank = newItem(new ItemStack(Material.BLACK_STAINED_GLASS_PANE, 1), "");
	protected static final ItemStack blankbl = newItem(new ItemStack(Material.BLUE_STAINED_GLASS_PANE, 1), "");
	
	public BeanGuiSupportUs(Player p) {
		super(p);
		
		this.name = "Support the Server!";
		this.presetSize = 54;
		this.presetInv = new ItemStack[] {
				blankbl,blankbl,blank,blank,newItem(tpp.getSkull(), tpp.getColouredName()),blank,blank,blankbl,blankbl,
				blankbl,null,null,null,null,null,null,null,blankbl,
				blank,null,null,null,null,null,null,null,blank,
				blank,null,null,null,null,null,null,null,blank,
				blank,null,null,null,null,null,null,null,blank,
				blankbl,blankbl,blankbl,blankbl,goBack,blankbl,blankbl,blankbl,blankbl
		};
	}

	@Override
	public void onInventoryClosed(InventoryCloseEvent e) {
		
	}

	@Override
	public void onInventoryClicked(InventoryClickEvent e) {
		final int slot = e.getRawSlot();
		switch(slot) {
		case 10: // Voting - Display Voting Menu
			close();
			p.openBook(getPlugin().voteManager().getVoteBook());
			
			return;
		case 13: // Playing - Nothing
			return;
		case 16: // Donating - In Future, Display Donation Links
			return;
		case 29: // Helping Users - Nothing
			return;
		case 33: // Reporting Bugs - Nothing
			return;
		case 40: // Following Rules - Show Rules later
			return;
		default:
			return;
		}
	}

	@Override
	public void onInventoryOpened() {
		// Update Contents
		final ItemStack[] contents = presetInv.clone();
		
		contents[4] = newItem(tpp.getSkull(), tpp.getColouredName(), Component.text("\u00a77You have \u00a7r" + tpp.getSapphire() + " Sapphire\u00a77!").colorIfAbsent(BeanColor.SAPPHIRE).decoration(TextDecoration.ITALIC, false));
		
		contents[10] = newItem(new ItemStack(Material.AXOLOTL_BUCKET), Component.text("Voting", BeanColor.SAPPHIRE), 
				Component.text("\u00a77Voting for our server on any of the available"),
				Component.text("\u00a77\u00a7fMinecraft Server Lists\u00a77 can help significantly!"),
				Component.text("\u00a77You also receive \u00a7rSapphire\u00a77 and \u00a76Coins\u00a77 for voting!").colorIfAbsent(BeanColor.SAPPHIRE).decoration(TextDecoration.ITALIC, false),
				Component.empty(),
				Component.text("\u00a77Daily Voting Streak: \u00a7r" + tpp.getStat(StatType.VOTING, "voteStreak") + " Days\u00a77!").colorIfAbsent(BeanColor.SAPPHIRE).decoration(TextDecoration.ITALIC, false),
				Component.text("\u00a77You've voted \u00a7r" + tpp.getStat(StatType.VOTING, "votes") + " \u00a77times!").colorIfAbsent(BeanColor.SAPPHIRE).decoration(TextDecoration.ITALIC, false),
				Component.text(tpp.getStat(StatType.VOTING, "lastVoteDay") != (System.currentTimeMillis() / 86400000L) ? "\u00a7cYou haven't voted today!" : "\u00a7bThanks for voting today!"));
		
		contents[16] = newItem(new ItemStack(Material.GOLDEN_APPLE), Component.text("Donating", BeanColor.SAPPHIRE), 
				Component.text("\u00a7cCurrently unavailable."));
		
		contents[22] = newItem(new ItemStack(Material.CRAFTING_TABLE), Component.text("Playing", BeanColor.SAPPHIRE), 
				Component.text("\u00a77Just playing on the server helps and means"),
				Component.text("\u00a77far more than you think! Also, the longer"),
				Component.text("\u00a77you play the better your playtime rank becomes!"),
				Component.empty(),
				Component.text("\u00a77Your playtime rank is: ").append(tpp.getPlaytimeRank().toComponent()));
		
		contents[29] = newItem(new ItemStack(Material.DIAMOND_HELMET), Component.text("Helping", BeanColor.SAPPHIRE), 
				Component.text("\u00a77Helping out players by answering questions"),
				Component.text("\u00a77and directing them to staff when needed is"),
				Component.text("\u00a77a fantastic help!"),
				Component.empty(),
				Component.text("\u00a77Furthermore, if you have a talent for building"),
				Component.text("\u00a77please don't hesitate to let us know! We may"),
				Component.text("\u00a77have something for you to build!"));
		
		contents[33] = newItem(new ItemStack(Material.CAKE), Component.text("Reporting Bugs", BeanColor.SAPPHIRE), 
				Component.text("\u00a77Reporting bugs, glitches and exploits help"),
				Component.text("\u00a77a lot and reward you with special \u00a7ddebug cakes\u00a77!"),
				Component.empty(),
				Component.text("\u00a77Report bugs with \u00a7b/report bug <info>\u00a77."));
		
		contents[40] = newItem(new ItemStack(Material.ANVIL), Component.text("Following the Rules", BeanColor.SAPPHIRE), 
				Component.text("\u00a77We thank you for following the rules and"),
				Component.text("\u00a77for whenever you're able to discourage"),
				Component.text("\u00a77others from breaking them!"),
				Component.empty(),
				Component.text("\u00a77Report players with \u00a7b/report player <name>\u00a77."));
		
		contents[48] = newItem(new ItemStack(Material.KNOWLEDGE_BOOK), Component.text("What is this?", BeanColor.SAPPHIRE), "", 
				"\u00a77This menu displays various methods of",
				"\u00a77how you can support the server and",
				"\u00a77help it grow.");
		
		i.setContents(contents);
	}
	
}
