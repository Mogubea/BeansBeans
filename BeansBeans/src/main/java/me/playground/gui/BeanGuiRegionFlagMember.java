package me.playground.gui;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.playground.regions.flags.FlagMember;
import me.playground.regions.flags.MemberLevel;
import me.playground.utils.BeanColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

public class BeanGuiRegionFlagMember extends BeanGuiRegion {
	
	private static final ItemStack iMaster = newItem(new ItemStack(Material.BARRIER), "\u00a74Master", "\u00a77Only Administrators");
	private static final ItemStack iOwner = newItem(new ItemStack(Material.NETHERITE_HELMET), "\u00a7cOwner", "\u00a77Only Region Owners");
	private static final ItemStack iOfficer = newItem(new ItemStack(Material.DIAMOND_HELMET), "\u00a79Officer", "\u00a77Only Region Officers");
	private static final ItemStack iTrusted = newItem(new ItemStack(Material.GOLDEN_HELMET), "\u00a79Trusted", "\u00a77Only Trusted Region Members");
	private static final ItemStack iMember = newItem(new ItemStack(Material.IRON_HELMET), "\u00a79Member", "\u00a77Only Region Members");
	private static final ItemStack iVisitor = newItem(new ItemStack(Material.LEATHER_HELMET), "\u00a79Visitor", "\u00a77Only Region Visitors");
	private static final ItemStack iNone = newItem(new ItemStack(Material.TURTLE_HELMET), "\u00a73None", "\u00a77All Players");
	
	final FlagMember flag;
	
	protected BeanGuiRegionFlagMember(Player p, int regionIdx, FlagMember flag) {
		super(p, regionIdx);
		
		setName("Flags -> " + flag.getDisplayName());
		this.presetInv = new ItemStack[] {
				rBlank,rBlank,blank,blank,null,blank,blank,rBlank,rBlank,
				rBlank,null,null,null,null,null,null,null,rBlank,
				blank,null,iOwner,iOfficer,iTrusted,iMember,iVisitor,null,blank,
				blank,null,null,null,iNone,null,null,null,blank,
				blank,null,null,null,null,null,null,null,blank,
				rBlank,rBlank,rBlank,rBlank,goBack,rBlank,rBlank,rBlank,rBlank
		};
		this.regionIdx = regionIdx;
		this.flag = flag;
	}

	@Override
	public void onInventoryClicked(InventoryClickEvent e) {
		final int slot = e.getRawSlot();
		if (slot < 0 || slot >= e.getInventory().getSize()) return;
		
		// The odds of losing permission and editing flags is very slim, but never zero.
		if (!getRegion().canModify(p)) {
			p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5F, 0.8F);
			new BeanGuiRegionFlags(p, regionIdx).openInventory();
			return;
		}
		
		switch(slot) {
			case 13 -> { if (p.hasPermission("bean.region.override")) { getRegion().setFlag(flag, MemberLevel.MASTER); }}
			case 20 -> getRegion().setFlag(flag, MemberLevel.OWNER);
			case 21 -> getRegion().setFlag(flag, MemberLevel.OFFICER);
			case 22 -> getRegion().setFlag(flag, MemberLevel.TRUSTED);
			case 23 -> getRegion().setFlag(flag, MemberLevel.MEMBER);
			case 24 -> getRegion().setFlag(flag, MemberLevel.VISITOR);
			case 31 -> getRegion().setFlag(flag, MemberLevel.NONE);
			default -> { return; }
		}
		
		p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5F, 0.8F);
		new BeanGuiRegionFlags(p, regionIdx).openInventory();
	}
	
	@Override
	public void onInventoryOpened() {
		final ItemStack[] contents = presetInv.clone();
		if (p.hasPermission("bean.region.override"))
			contents[13] = iMaster;
		contents[48] = stackForFlag(flag);
		
		i.setContents(contents);
		super.onInventoryOpened();
	}
	
	@Override
	public boolean preInventoryClick(InventoryClickEvent e) {
		if (!super.preInventoryClick(e)) {
			if (e.getRawSlot() == 50) {
				p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5F, 0.8F);
				return true;
			}
		}
		return false;
	}
	
	private ItemStack stackForFlag(FlagMember flag) {
		ItemStack item = new ItemStack(Material.CHAINMAIL_HELMET);
		ItemMeta meta = item.getItemMeta();
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
		ArrayList<Component> lore = new ArrayList<>();
		
		final boolean isInherited = getRegion().getFlag(flag, true) == null;
		meta.displayName(Component.text(flag.getDisplayName() + (isInherited ? "\u00a77 (default)" : "")).colorIfAbsent(BeanColor.REGION).decoration(TextDecoration.ITALIC, false));
		
		MemberLevel level = getRegion().getEffectiveFlag(flag);
		switch (level) {
			case MASTER -> item.setType(Material.BARRIER);
			case OWNER -> item.setType(Material.NETHERITE_HELMET);
			case OFFICER -> item.setType(Material.DIAMOND_HELMET);
			case TRUSTED -> item.setType(Material.GOLDEN_HELMET);
			case MEMBER -> item.setType(Material.IRON_HELMET);
			case VISITOR -> item.setType(Material.LEATHER_HELMET);
			case NONE -> item.setType(Material.TURTLE_HELMET);
		}
		lore.add(Component.text("Level: \u00a7f" + level).colorIfAbsent(BeanColor.REGION).decoration(TextDecoration.ITALIC, false));
		
		lore.add(Component.empty());
		lore.addAll(flag.getDescription());
		
		meta.lore(lore);
		item.setItemMeta(meta);
		if (!isInherited)
			item.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
		return item;
	}
	
}
