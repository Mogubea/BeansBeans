package me.playground.gui;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import me.playground.civilizations.jobs.Job;
import me.playground.npc.NPCHuman;
import me.playground.npc.interactions.NPCInteractEmployer;
import me.playground.npc.interactions.NPCInteraction;
import me.playground.utils.BeanColor;
import me.playground.utils.SignMenuFactory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class BeanGuiNPCEdit extends BeanGui {
	
	protected static final ItemStack icon_name = newItem(new ItemStack(Material.WARPED_SIGN), Component.text("NPC Name", BeanColor.NPC));
	protected static final ItemStack icon_title = newItem(new ItemStack(Material.WARPED_SIGN), Component.text("NPC Title", BeanColor.NPC));
	protected static final ItemStack icon_titleCol = newItem(new ItemStack(Material.WARPED_SIGN), Component.text("NPC Title Colour", BeanColor.NPC));
	protected static final ItemStack icon_employment = newItem(new ItemStack(Material.DIAMOND_AXE), Component.text("NPC Occupation", BeanColor.NPC));
	protected static final ItemStack icon_civilization = newItem(new ItemStack(Material.BIRCH_SIGN), Component.text("NPC Civilization", BeanColor.NPC));
	protected static final ItemStack icon_interaction = newItem(new ItemStack(Material.MOJANG_BANNER_PATTERN), Component.text("NPC Interaction Script", BeanColor.NPC));
	protected static final ItemStack icon_refresh = newItem(new ItemStack(Material.BELL), Component.text("Refresh NPC", BeanColor.NPC));
	
	private final NPCHuman npc;
	
	public BeanGuiNPCEdit(Player p, NPCHuman npc) {
		super(p);
		this.name = "Edit NPC \""+npc.getEntity().getName()+"\"";
		this.presetSize = 54;
		this.presetInv = new ItemStack[] {
				bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,blank,null,
				bBlank,icon_name,null,icon_title,null,icon_interaction,bBlank,blank,null,
				bBlank,null,null,icon_titleCol,null,null,bBlank,blank,null,
				bBlank,null,null,null,null,null,bBlank,blank,null,
				bBlank,null,icon_civilization,null,icon_employment,null,bBlank,blank,null,
				icon_refresh,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,blank,null
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
					if ((x < 4 ? mat.endsWith(slotChecks[x]) : true) && i.getItem(aSlot) == null) {
						i.setItem(aSlot, item); // Duplicate the item by shift clicking, only admins can get into this gui anyway
						return;
					}
				}
			} else {
				e.setCancelled(false);
			}
		// Equipment Slots
		} else if (slot % 9 == 8) {
			e.setCancelled(false);
		// TODO: Name Change
		} else if (slot == 10) {
			
		// Title Change
		} else if (slot == 12) {
			if (npc.getInteraction().hasLockedTitle()) return;
			if (e.isRightClick()) {
				npc.removeTitle(true);
				p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5F, 0.8F);
				onInventoryOpened();
				return;
			}
			
			p.closeInventory();
			
			SignMenuFactory.Menu menu = plugin.getSignMenuFactory().newMenu(Arrays.asList("","", "^^^^^^^^^^", "\u00a7bTitle for NPC"), Material.WARPED_WALL_SIGN)
            .reopenIfFail(true)
            .response((player, strings) -> {
                try {
                	if (strings[0] == null && strings[1] == null)
                		Bukkit.getScheduler().runTaskLater(plugin, () -> npc.removeTitle(true), 1L);
                	Bukkit.getScheduler().runTaskLater(plugin, () -> npc.setTitle(Component.text(strings[0] + strings[1]), true), 1L);
                	p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5F, 0.8F);
                } catch (RuntimeException ex) {
                	p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5F, 0.8F);
                }
            	
                Bukkit.getScheduler().runTaskLater(plugin, () -> new BeanGuiNPCEdit(p, npc).openInventory(), 1L);
                return true;
            });
			menu.open(p);
			return;
		// Interaction Change
		} else if (slot == 14) {
			p.closeInventory();
			
			SignMenuFactory.Menu menu = plugin.getSignMenuFactory().newMenu(Arrays.asList("", "^^^^^^^^^^", "\u00a7bSpecify an interaction", "\u00a7bscript for the NPC"), Material.BIRCH_WALL_SIGN)
            .reopenIfFail(true)
            .response((player, strings) -> {
                try {
                	NPCInteraction interaction = NPCInteraction.getByName(strings[0]);
                	if (interaction == null) {
                		p.sendActionBar(Component.text("\u00a7cYou provided an invalid interaction script!"));
                		throw new RuntimeException("Invalid interaction script.");
                	}
                	Bukkit.getScheduler().runTaskLater(plugin, () -> npc.setInteraction(interaction), 1L);
                	p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5F, 0.8F);
                } catch (RuntimeException ex) {
                	p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5F, 0.8F);
                }
            	
                Bukkit.getScheduler().runTaskLater(plugin, () -> new BeanGuiNPCEdit(p, npc).openInventory(), 1L);
                return true;
            });
			menu.open(p);
			return;
		// Title Colour Change
		} else if (slot == 21) {
			if (npc.getInteraction().hasLockedTitle()) return;
			if (e.isRightClick()) {
				npc.setTitleColour(BeanColor.NPC.value());
				p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5F, 0.8F);
				onInventoryOpened();
				return;
			}
			
			p.closeInventory();
			
			SignMenuFactory.Menu menu = plugin.getSignMenuFactory().newMenu(Arrays.asList("","^^^^^^^^^^", "\u00a7bTitle Colour", "\u00a7bfor NPC"), Material.WARPED_WALL_SIGN)
            .reopenIfFail(true)
            .response((player, strings) -> {
                try {
                	Bukkit.getScheduler().runTaskLater(plugin, () -> npc.setTitleColour(Integer.parseInt(strings[0])), 1L);
                	p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5F, 0.8F);
                } catch (RuntimeException ex) {
                	p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5F, 0.8F);
                }
            	
                Bukkit.getScheduler().runTaskLater(plugin, () -> new BeanGuiNPCEdit(p, npc).openInventory(), 1L);
                return true;
            });
			menu.open(p);
			return;
		// Employment Job Change
		} else if (slot == 40) {
			p.closeInventory();
			
			SignMenuFactory.Menu menu = plugin.getSignMenuFactory().newMenu(Arrays.asList("", "^^^^^^^^^^", "\u00a7bSpecify a job", "\u00a7bfor the NPC"), Material.BIRCH_WALL_SIGN)
            .reopenIfFail(true)
            .response((player, strings) -> {
                try {
                	Job job = Job.getByName(strings[0]);
                	if (job == null) {
                		p.sendActionBar(Component.text("\u00a7cYou provided an invalid job!"));
                		throw new RuntimeException("Invalid job.");
                	}
                	npc.setJob(job);
                	Bukkit.getScheduler().runTaskLater(plugin, () -> npc.getInteraction().onInit(npc), 1L);
                	p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5F, 0.8F);
                } catch (RuntimeException ex) {
                	p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5F, 0.8F);
                }
            	
                Bukkit.getScheduler().runTaskLater(plugin, () -> new BeanGuiNPCEdit(p, npc).openInventory(), 1L);
                return true;
            });
			menu.open(p);
			return;
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
		boolean isJobNPC = npc.getInteraction() instanceof NPCInteractEmployer;
		contents[12] = newItem(new ItemStack(isJobNPC ? Material.CRIMSON_SIGN : Material.WARPED_SIGN),
				Component.text("NPC Title", isJobNPC ? NamedTextColor.RED : BeanColor.NPC), npc.getTitle().decoration(TextDecoration.ITALIC, false));
		contents[21] = newItem(new ItemStack(isJobNPC ? Material.CRIMSON_SIGN : Material.WARPED_SIGN),
				Component.text("NPC Title Colour", isJobNPC ? NamedTextColor.RED : BeanColor.NPC), Component.text(npc.getTitleColour()).color(TextColor.color(npc.getTitleColour())).decoration(TextDecoration.ITALIC, false));
		contents[14] = newItem(icon_interaction, Component.text("NPC Interaction Script", BeanColor.NPC), "\u00a7f" + npc.getInteraction().getName());
		contents[40] = newItem(icon_employment, Component.text("NPC Occupation", BeanColor.NPC), npc.getJob().toComponent());
		
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
