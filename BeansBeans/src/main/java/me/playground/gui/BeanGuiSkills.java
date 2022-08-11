package me.playground.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import me.playground.highscores.Highscore;
import me.playground.playerprofile.PlayerProfile;
import me.playground.skills.Skill;
import me.playground.skills.SkillInfo;
import me.playground.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class BeanGuiSkills extends BeanGui {
	
	private static final ItemStack blank = newItem(new ItemStack(Material.ORANGE_STAINED_GLASS_PANE, 1), "\u00a76Skills");
	private final HashMap<Integer, Skill> mappings = new HashMap<Integer, Skill>();
	private final int[] skillSlots = {20,21,22,23,24,29,30,31,32,33};
	
	public BeanGuiSkills(Player p) {
		super(p);
		
		setName("Skills");
		this.presetSize = 54;
		this.presetInv = new ItemStack[] {
				blank,blank,bBlank,bBlank,null,bBlank,bBlank,blank,blank,
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
		Skill skill = mappings.get(e.getRawSlot());
		if (skill == null) return;
		
		new BeanGuiSkillsDetails(p, skill).openInventory();
	}

	@Override // TODO:
	public void onInventoryOpened() {
		final ItemStack[] contents = presetInv.clone();
		contents[4] = newItem(new ItemStack(Material.GOLDEN_PICKAXE), "\u00a76Skills");
		List<Skill> skills = Skill.getRegisteredSkills();
		int size = skillSlots.length;
		
		for (int x = -1; ++x < size;) {
			Skill skill = skills.get(x);
			SkillInfo skillInfo = tpp.getSkills().getSkillInfo(skill);
			Highscore highscore = getPlugin().highscores.getHighscore(skill.getName() + " XP");
			int col = skill.getColour().value();
			TextColor lighter = TextColor.color(col | 0x3d3d3d);
			
			// copium
			int spaces = 18 - (skillInfo.getLevelXP() + "/" + skillInfo.getXPRequirement()).length();
			String space = "";
			for (int a = -1; ++a < spaces;)
				space += " ";
			
			contents[skillSlots[x]] = newItem(skill.getDisplayStack(), skill.toComponent(), 
					Component.text("Grade: ", lighter).append(Component.text("\u00a7f\u00a7l" + skillInfo.getGrade())),
					Component.empty(),
					Component.text("\u00a7l" + skillInfo.getGrade() + " \u00a7r", lighter)
					.append(Utils.getProgressBar('-', 24, skillInfo.getLevelXP(), skillInfo.getXPRequirement(), 0x454545, col))
					.append(Component.text(" \u00a7l"+skillInfo.getNextGrade(), skill.getColour())).decoration(TextDecoration.ITALIC, false),
					Component.text(space + df.format(skillInfo.getLevelXP()), TextColor.color(0xffffff))
					.append(Component.text("/", lighter))
					.append(Component.text(df.format(skillInfo.getXPRequirement()), lighter)).decoration(TextDecoration.ITALIC, false),
					Component.empty(),
					Component.text("Total Exp: ", lighter)
					.append(Component.text(df.format(skillInfo.getTotalXP()), TextColor.color(0xffffff))).decoration(TextDecoration.ITALIC, false),
					Component.text("Server Position: ", lighter)
					.append(Component.text("#" + highscore.getPositionOf(tpp.getId()), TextColor.color(0xffffff)))
					.append(Component.text(" of " + highscore.getSize(), lighter)).decoration(TextDecoration.ITALIC, false),
					Component.empty(),
					Component.text("\u00a76» \u00a7eClick for details!")
			);
			
			mappings.put(skillSlots[x], skill);
		}
		
		Highscore highscore = getPlugin().highscores.getHighscore("Total Skill XP");
		ItemStack head = new ItemStack(Material.CARTOGRAPHY_TABLE);
		
		List<Component> scoreLore = new ArrayList<Component>();
		boolean hasViewer = false;
		scoreLore.add(Component.text(""));
		for (int x = -1; ++x < 8;) {
			int peep = highscore.getOrder().get(x);
			if (peep == tpp.getId()) 
				hasViewer = true;
			scoreLore.add(Component.text("\u00a78" + highscore.getPositionOf(peep) + ". ").append(PlayerProfile.getDisplayName(peep).decoration(TextDecoration.ITALIC, false)).append(Component.text("\u00a78 with \u00a77" + highscore.getScoreOf(peep) + "\u00a77 xp")));
		}
		
		if (!hasViewer) {
			int peep = tpp.getId();
			scoreLore.add(Component.text("\u00a78..."));
			scoreLore.add(Component.text("\u00a78" + highscore.getPositionOf(peep) + ". ").append(PlayerProfile.getDisplayName(peep).decoration(TextDecoration.ITALIC, false)).append(Component.text("\u00a78 with \u00a77" + highscore.getScoreOf(peep) + "\u00a77 xp")));
		}
		
		head.editMeta(meta -> {
			meta.displayName(Component.text("\u00a76Total XP Leaderboard"));
			meta.lore(scoreLore);
		});
		
		contents[50] = head;
		
		i.setContents(contents);
	}
}
