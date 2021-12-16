package me.playground.gui;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import me.playground.data.Datasource;
import me.playground.playerprofile.PlayerProfile;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.ChatColor;

public class UpdateEntry {
	
	private final static ArrayList<UpdateEntry> books = Datasource.loadNews();
	private final SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yy");
	
	private final ItemStack book;
	private final ItemStack displayItem;
	
	public UpdateEntry(int writtenBy, String title, String description, Timestamp stamp, Material cover, ArrayList<Component> pages) {
		ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
		BookMeta bmeta = (BookMeta) book.getItemMeta();
		bmeta.author(PlayerProfile.getDisplayName(writtenBy));
		bmeta.title(Component.text(ChatColor.translateAlternateColorCodes('&', title)));
		bmeta.pages(pages);
		book.setItemMeta(bmeta);
		this.book = book;
		
		ItemStack displayItem = new ItemStack(cover);
		ItemMeta meta = displayItem.getItemMeta();
		ArrayList<Component> lore = new ArrayList<Component>();
		meta.displayName(Component.text(ChatColor.translateAlternateColorCodes('&', title)));
		
		lore.add(Component.text("\u00a78\u00a7o" + format.format(stamp)));
		for (String s : description.split("`"))
			lore.add(Component.text("\u00a77"+s));
		
		lore.add(Component.empty());
		lore.add(Component.text("\u00a76» \u00a7eClick to read!"));
		//lore.add(Component.text("\u00a77\u00a7iWritten by ").append(NameCache.getDisplayName(writtenBy)));
		meta.lore(lore);
		
		displayItem.setItemMeta(meta);
		this.displayItem = displayItem;
	}
	
	public ItemStack getBook() {
		return book;
	}
	
	public ItemStack getCover() {
		return displayItem;
	}
	
	public static ArrayList<UpdateEntry> getUpdateEntries() {
		return books;
	}
	
	public static void reload() {
		books.clear();
		books.addAll(Datasource.loadNews());
	}
	
	
}
