package me.playground.gui;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.playground.highscores.Highscore;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.playerprofile.skills.SkillInfo;
import me.playground.playerprofile.skills.SkillType;
import me.playground.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

public class BeanGuiSkills extends BeanGui {
	
	final private static DecimalFormat df = new DecimalFormat("#.##");
	
	public BeanGuiSkills(Player p) {
		super(p);
		
		this.name = "Skills";
		this.presetSize = 54;
		this.presetInv = new ItemStack[] {
				null,blank,null,null,null,null,null,null,null,
				blank,blank,null,null,null,null,null,null,null,
				prevPage,blank,null,null,null,null,null,null,null,
				nextPage,blank,null,null,null,null,null,null,null,
				blank,blank,blank,blank,blank,blank,blank,blank,blank,
				null,null,null,null,null,null,null,null,null
		};
	}

	@Override
	public void onInventoryClosed(InventoryCloseEvent e) {
	}

	@Override
	public void onInventoryOpened() {
		ItemStack[] contents = i.getContents();
		final SkillType[] skills = SkillType.values();
		
		for (int x = 0; x < 9; x++) {
			final SkillType skill = skills[x];
			final SkillInfo info = pp.getSkills().getSkillInfo(skill);
			final int level = info.getLevel();
			final long xp = info.getXp();
			final int xpCur = (int) info.getCurrentLevelXp();
			final int xpNeed = (int) info.xpRequiredToLevelUp(level);
			final float percent = (float) (info.getPercentageDone() * 100F);
				
			final Highscore highscore = Main.getInstance().highscores.highscores[x];
				
			ItemStack skillStack = new ItemStack(skill.getIconStack().getType());
			ItemMeta skillMeta = skillStack.getItemMeta();
			skillMeta.displayName(Component.text(skill.getDisplayName()));
			
			final ArrayList<Component> loreComp = new ArrayList<Component>();
			loreComp.add(Component.text("\u00a77Level: \u00a7b" + level));
			loreComp.add(Component.text("\u00a77Experience: \u00a72" + xpCur + "\u00a77/\u00a7a" + xpNeed));
			loreComp.add(Component.text(Utils.getProgressBar('-', 20, xpCur, xpNeed, ChatColor.DARK_GRAY, skill.getColour()) + skill.getColour() + " " + df.format(percent) + "%"));
			loreComp.add(Component.empty());
			loreComp.add(Component.text("\u00a77Total XP: \u00a72" + xp + " XP"));
			loreComp.add(Component.text("\u00a78Leaderboard Position: \u00a7e#" + highscore.getPositionOf(pp.getUniqueId()) + "\u00a78 of \u00a76" + highscore.getSize() + " \u00a78Players!"));
			
			skillMeta.lore(loreComp);
			
			if (page == x) {
				skillMeta.addEnchant(Enchantment.ARROW_DAMAGE, 1, true);
				skillMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			}
			skillMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			
			skillStack.setItemMeta(skillMeta);
			skillStack.setAmount(Math.max(1, level/10));
			
			contents[45 + x] = skillStack;
				
		}
		
		contents[18] = icon_dataU;
		contents[27] = icon_dataD;
			
		i.setContents(contents);
		updateSkillTree();
	}
	
	@Override
	public void onInventoryClicked(InventoryClickEvent e) {
		final int slot = e.getRawSlot();
		e.setCancelled(true);
		
		if (slot >= 45 && slot <= 53) {
			setPage(slot-45);
		} else if (slot == 18 && getData()<40) {
			upData();
			updateSkillTree();
		} else if (slot == 27 && getData()>0) {
			downData();
			updateSkillTree();
		}
	}
	
//  1    2     3     4     5     6     7
		// [02],  03,  [04], [05], [06],  07,  [08]
		// [11],  12,  [13],  14,  [15],  16,  [17]
		// [20],  21,  [22],  23,  [24],  25,  [26]
		// [29], [30], [31],  32,  [33], [34], [35]
		private void updateSkillTree() {
			ItemStack[] contents = i.getContents();
			final SkillType[] skills = SkillType.values();
			final SkillType skill = skills[page];
			final SkillInfo skillInfo = tpp.getSkills().getSkillInfo(skill);
			final int skillLevel = skillInfo.getLevel();
			final int pat = data % 4; // 0, 1, 2, 3
			int skillLevelReq = 10 * (data+1 + (((data+1)/2) * 3));
			
			// Highscore Head
			ItemStack head = pp.getSkull();
			ItemMeta headMeta = head.getItemMeta();
			headMeta.displayName(Component.text(skill.getDisplayName() + " Leaderboard:"));
			
			List<Component> scoreLore = new ArrayList<Component>();
			final Highscore highscore = Main.getInstance().highscores.highscores[page];
			scoreLore.add(Component.text(""));
			for (Integer peep : highscore.getOrder())
				scoreLore.add(Component.text("\u00a78" + highscore.getPositionOf(peep) + ". ").append(PlayerProfile.getDisplayName(peep).decoration(TextDecoration.ITALIC, false)).append(Component.text("\u00a78 with \u00a77" + highscore.getScoreOf(peep) + "\u00a77 xp")));
			
			headMeta.lore(scoreLore);
			head.setItemMeta(headMeta);
			contents[0] = head;
			
			
			// Highscore Head End
			
			// Unlocked Skill Information
			for (int x = 0; x < 5; x++) {
				ItemStack skillItem = new ItemStack(skill.getDye());
				ItemMeta skillMeta = skillItem.getItemMeta();
				skillMeta.displayName(Component.text(skill.getColour()+"..."));
				
				skillItem.setItemMeta(skillMeta);
				
				contents[39 + x] = skillItem;
			}
			// Unlocked Skill Information End
			
			// Skill Branch
			ItemStack goop = new ItemStack(skill.getGlass());
			ItemMeta goopMeta = goop.getItemMeta();
			
			for (int x = 0; x < 7; x++) {
				
				for (int y = 0; y < 4; y++) // Refresh slots
					contents[2 + x + (y*9)] = null;
				
				int pattern = (x+pat) % 4;
				
				switch(pattern) {
				case 0: // 4 down
					for (int y = 0; y < 4; y++) {
						goop = new ItemStack(skillLevel>=skillLevelReq ? skill.getConcrete() : skill.getGlass());
						goopMeta.displayName(Component.text(skill.getDisplayName() + " Level " + skillLevelReq));
						goop.setItemMeta(goopMeta);
						
						contents[2 + x + (y*9)] = goop;
						skillLevelReq += 10;
					}
					break;
				case 1: // 1 bottom along
					goop = new ItemStack(skillLevel>=skillLevelReq ? skill.getConcrete() : skill.getGlass());
					goopMeta.displayName(Component.text(skill.getDisplayName() + " Level " + skillLevelReq));
					goop.setItemMeta(goopMeta);
					
					contents[29 + x] = goop;
					skillLevelReq += 10;
					break;
				case 2: // 4 up
					for (int y = 3; y > -1; y--) {
						goop = new ItemStack(skillLevel>=skillLevelReq ? skill.getConcrete() : skill.getGlass());
						goopMeta.displayName(Component.text(skill.getDisplayName() + " Level " + skillLevelReq));
						goop.setItemMeta(goopMeta);
						
						contents[2 + x + (y*9)] = goop;
						skillLevelReq += 10;
					}
					break;
				case 3: // 1 bottom along
					goop = new ItemStack(skillLevel>=skillLevelReq ? skill.getConcrete() : skill.getGlass());
					goopMeta.displayName(Component.text(skill.getDisplayName() + " Level " + skillLevelReq));
					goop.setItemMeta(goopMeta);
					
					contents[2 + x] = goop;
					skillLevelReq += 10;
					break;
				}
			}
			i.setContents(contents);
		}

}
