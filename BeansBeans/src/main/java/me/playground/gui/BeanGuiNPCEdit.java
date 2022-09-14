package me.playground.gui;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import me.playground.items.BeanItem;
import me.playground.npc.NPCHuman;
import me.playground.npc.interactions.NPCInteractShop;
import me.playground.npc.interactions.NPCInteraction;
import me.playground.utils.BeanColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class BeanGuiNPCEdit extends BeanGui {
	
	protected static final ItemStack icon_name = newItem(new ItemStack(Material.WARPED_SIGN), Component.text("NPC Name", BeanColor.NPC));
	protected static final ItemStack icon_title = newItem(new ItemStack(Material.WARPED_SIGN), Component.text("NPC Title", BeanColor.NPC));
	protected static final ItemStack icon_titleCol = newItem(new ItemStack(Material.WARPED_SIGN), Component.text("NPC Title Colour", BeanColor.NPC));
	protected static final ItemStack icon_interaction = newItem(new ItemStack(Material.MOJANG_BANNER_PATTERN), Component.text("NPC Interaction Script", BeanColor.NPC));
	protected static final ItemStack icon_refresh = newItem(new ItemStack(Material.BELL), Component.text("Refresh NPC", BeanColor.NPC));
	
	private final NPCHuman npc;
	
	public BeanGuiNPCEdit(Player p, NPCHuman npc) {
		super(p);
		setName("Edit NPC \""+npc.getEntity().displayName+"\"");
		this.presetSize = 54;
		this.presetInv = new ItemStack[] {
				bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,blank,null,
				bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,blank,null,
				bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,blank,null,
				bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,blank,null,
				bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,blank,null,
				bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,blank,null,
		};
		this.npc = npc;
	}

	@Override
	public void onInventoryClosed(InventoryCloseEvent e) { // setTitle(Component.text("\u00a7e" + job.getNiceName() + " Employer"), false);
		ItemStack[] blah = new ItemStack[6];
		for (int x = -1; ++x < 6;)
			blah[x] = e.getInventory().getItem(54 - (x * 9) - 1);
		npc.setEquipment(blah);
	}
	
	private final String[] slotChecks = { "_HELMET", "_CHESTPLATE", "_LEGGINGS", "_BOOTS" };
	
	@Override
	public void onInventoryClicked(InventoryClickEvent e) {
		final int slot = e.getRawSlot();
		
		// Player Inventory Slots
		if (slot > i.getSize()) {
			if (e.isShiftClick()) {
				ItemStack item = e.getCurrentItem();
				String mat = item.getType().name();
				for (int x = -1; ++x < 6;) {
					int aSlot = 8 + (x * 9);
					if ((x >= 4 || mat.endsWith(slotChecks[x])) && i.getItem(aSlot) == null) {
						i.setItem(aSlot, item); // Duplicate the item by shift clicking, only admins can get into this gui anyway
						p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 0.6F, 1);
						return;
					}
				}
			} else {
				if (e.getCurrentItem() != null)
					p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 0.6F, 1);
				e.setCancelled(false);
			}
		// Equipment Slots
		} else if (slot % 9 == 8) {
			e.setCancelled(false);
		// TODO: Name Change
		} else if (slot == 10) {

		} else if (slot == 12) {
			close();
			p.chat("/op npc settitle");
			// Interaction Change
		} else if (slot == 14) {
			getPlugin().getSignMenuFactory().requestSignResponse(p, Material.BIRCH_WALL_SIGN, (strings) -> {
				NPCInteraction interaction = NPCInteraction.getByName(strings[0]);
				npc.setInteraction(interaction);
			}, true, "Interaction ID", "for this NPC");
		// Set Shop
		} else if (slot == 41 && npc.getInteraction() instanceof NPCInteractShop) {
			getPlugin().getSignMenuFactory().requestSignResponse(p, Material.BIRCH_WALL_SIGN, (strings) -> npc.setMenuShop(strings[0] + strings[1]), true, "NPC MenuShop ID");
		} else if (slot == 45) {
			
		}
	}
	
	@Override
	public void onInventoryOpened() {
		final ItemStack[] contents = presetInv.clone();
		ItemStack[] items = npc.getEquipment();
		for (int x = 6; --x > -1;)
			contents[x * 9 + 8] = items[5 - x];
		
		contents[10] = newItem(icon_name, Component.text("NPC Name", BeanColor.NPC), npc.getName());
		
		if (npc.getInteraction() instanceof NPCInteractShop)
			contents[41] = newItem(BeanItem.GOLDEN_CHEST.getItemStack(), Component.text("\u00a7eNPC Shop"), Component.text("\u00a77The MenuShop that will open when"), Component.text("\u00a77a player clicks this NPC.."), Component.empty(), npc.getMenuShop() == null ? Component.text("\u00a7cNo MenuShop set.") : Component.text("\u00a77MenuShop: \u00a76" + npc.getMenuShop().getIdentifier()));
		contents[14] = newItem(icon_interaction, Component.text("NPC Interaction", BeanColor.NPC), npc.getInteraction().getName());

		i.setContents(contents);
	}
	
	@Override
	public boolean preInventoryClick(InventoryClickEvent e) {
		if (super.preInventoryClick(e)) {
			final ItemStack i = e.getCurrentItem();
			if (i == null) return e.getCursor() == null; // Only cancel if both cursor and slot are null.
		}
		return false;
	}
}
