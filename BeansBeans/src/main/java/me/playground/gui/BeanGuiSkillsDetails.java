package me.playground.gui;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import me.playground.highscores.Highscore;
import me.playground.playerprofile.PlayerProfile;
import me.playground.playerprofile.skills.SkillType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class BeanGuiSkillsDetails extends BeanGui {
	
	private static final ItemStack blank = newItem(new ItemStack(Material.ORANGE_STAINED_GLASS_PANE, 1), "\u00a76Skills");
	private final SkillType skill;
	
	public BeanGuiSkillsDetails(Player p, SkillType skill) {
		super(p);
		
		this.skill = skill;
		this.name = skill.getPlainName() + " Skill";
		this.presetSize = 54;
		this.presetInv = new ItemStack[] {
				blank,blank,bBlank,bBlank,newItem(skill.getDisplayStack(), skill.getDisplayName()),bBlank,bBlank,blank,blank,
				blank,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,blank,
				bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,
				bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,
				bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,
				blank,blank,blank,blank,goBack,blank,blank,blank,blank
		};
	}

	@Override
	public void onInventoryClosed(InventoryCloseEvent e) {
	}

	@Override
	public void onInventoryClicked(InventoryClickEvent e) {
		int slot = e.getRawSlot();
		
		switch(slot) {
		case 46: // Previous Skill
			SkillType[] types = SkillType.values();
			int a = skill.ordinal() - 1;
			if (a < 0) a = types.length - 1;
			
			new BeanGuiSkillsDetails(p, types[a]).openInventory();
			return;
		case 52: // Next Skill
			SkillType[] typez = SkillType.values();
			int b = skill.ordinal() + 1;
			if (b >= typez.length) b = 0;
			
			new BeanGuiSkillsDetails(p, typez[b]).openInventory();
			return;
		}
	}

	@Override // TODO:
	public void onInventoryOpened() {
		final ItemStack[] contents = presetInv.clone();
		SkillType[] types = SkillType.values();
		int a = skill.ordinal() - 1;
		if (a < 0) a = types.length - 1;
		contents[46] = newItem(types[a].getDisplayStack(), types[a].getDisplayName());
		a = skill.ordinal() + 1;
		if (a >= types.length) a = 0;
		contents[52] = newItem(types[a].getDisplayStack(), types[a].getDisplayName());
		
		if (page > 0)
			contents[36] = prevPage;
		if (page < 20)
			contents[44] = nextPage;
		
		for (int x = -1; ++x < 7;) {
			int level = 25 * (x + page);
			boolean isLevel = tpp.getSkillLevel(skill) >= level;
			ItemStack stack = (level % 100 == 0) ? skill.getDisplayStack() : new ItemStack(isLevel ? Material.ACACIA_BUTTON : Material.POLISHED_BLACKSTONE_BUTTON);
			
			contents[37 + x] = newItem(stack, Component.text("Level " + level, isLevel ? skill.getColour() : NamedTextColor.DARK_GRAY),
					Component.text("\u00a77Progress rewards soon."));
			if (isLevel)
				contents[37 + x].addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
		}
		
		Highscore highscore = getPlugin().highscores.getHighscore(skill.ordinal());
		ItemStack head = new ItemStack(Material.CARTOGRAPHY_TABLE);
		
		List<Component> scoreLore = new ArrayList<Component>();
		boolean hasViewer = false;
		scoreLore.add(Component.text(""));
		for (int x = -1; ++x < 8;) {
			int peep = highscore.getOrder().get(x);
			if (peep == tpp.getId()) 
				hasViewer = true;
			scoreLore.add(Component.text("\u00a78" + highscore.getPositionOf(peep) + ". ").append(PlayerProfile.getDisplayName(peep).decoration(TextDecoration.ITALIC, false)).append(Component.text("\u00a78 with \u00a77" + df.format(highscore.getScoreOf(peep)) + "\u00a77 xp")));
		}
		
		if (!hasViewer) {
			int peep = tpp.getId();
			scoreLore.add(Component.text("\u00a78..."));
			scoreLore.add(Component.text("\u00a78" + highscore.getPositionOf(peep) + ". ").append(PlayerProfile.getDisplayName(peep).decoration(TextDecoration.ITALIC, false)).append(Component.text("\u00a78 with \u00a77" + df.format(highscore.getScoreOf(peep)) + "\u00a77 xp")));
		}
		
		head.editMeta(meta -> {
			meta.displayName(Component.text(skill.getPlainName() + " Leaderboard", skill.getColour()).decoration(TextDecoration.ITALIC, false));
			meta.lore(scoreLore);
		});
		
		contents[50] = head;
		
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
			new BeanGuiSkills(p).openInventory();
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
