package me.playground.gui;

import org.bukkit.entity.Player;

@Deprecated
public class BeanGuiSkillsDetails extends BeanGui {
	public BeanGuiSkillsDetails(Player p) {
		super(p);
	}
	
	/*private static final ItemStack blank = newItem(new ItemStack(Material.ORANGE_STAINED_GLASS_PANE, 1), "\u00a76Skills");
	
	private static final ItemStack[] grades = {
			newItem(Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWYyMmQ3Y2Q1M2Q1YmZlNjFlYWZiYzJmYjFhYzk0NDQzZWVjMjRmNDU1MjkyMTM5YWM5ZmJkYjgzZDBkMDkifX19"), Component.text("Grade S")),
			newItem(Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTdkZDM0OTI0ZDJiNmEyMTNhNWVkNDZhZTU3ODNmOTUzNzNhOWVmNWNlNWM4OGY5ZDczNjcwNTk4M2I5NyJ9fX0="), Component.text("Grade A")),
			newItem(Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWVjYTk4YmVmZDBkN2VmY2E5YjExZWJmNGIyZGE0NTljYzE5YTM3ODExNGIzY2RkZTY3ZDQwNjdhZmI4OTYifX19"), Component.text("Grade B")),
			newItem(Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTZiMTQ4NmUxZjU3NmJjOTIxYjhmOWY1OWZlNjEyMmNlNmNlOWRkNzBkNzVlMmM5MmZkYjhhYjk4OTdiNSJ9fX0="), Component.text("Grade C")),
			newItem(Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTlhYTY5MjI5ZmZkZmExODI4ODliZjMwOTdkMzIyMTVjMWIyMTU5ZDk4NzEwM2IxZDU4NDM2NDZmYWFjIn19fQ=="), Component.text("Grade D")),
			newItem(Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2VkOWY0MzFhOTk3ZmNlMGQ4YmUxODQ0ZjYyMDkwYjE3ODNhYzU2OWM5ZDI3OTc1MjgzNDlkMzdjMjE1ZmNjIn19fQ=="), Component.text("Grade E")),
			newItem(Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWQ3MTRiYWZiMGI1YWI5Y2ZhN2RiMDJlZmM4OTI3YWVkMWVmMjk3OTdhNTk1ZGEwNjZlZmM1YzNlZmRjOSJ9fX0="), Component.text("Grade F")),
			
			newItem(Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2VkOWQ5NmMzOTNlMzhhMzZiNjFhYTNjODU5ZWRlNWViNzQ0ZWYxZTg0NmQ0ZjdkMGVjYmQ2NTg4YTAyMSJ9fX0="), Component.text("Grade S")),
			newItem(Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjBhZmQ3NzdkNTU3YTIwN2JhYzdhYWQ4NDIxZmRmNzg4ZDY2ODU4NzNjNDk1MTVkNTUyOTFlOTMwNjk5ZiJ9fX0="), Component.text("Grade A")),
			newItem(Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzQ3ODk5ZDUxMWJhM2EzYmZlMTQ1NDFhNmE0Yjc3YjAzM2U0NGFjZDk1Njg5NmU3YjY1Njc0MjliZjE4ZDgzYyJ9fX0="), Component.text("Grade B")),
			newItem(Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWFjMGRhOTQ5NGI5NzRiZGJjYjc4ODYxZGY5NmFhNWFhYTVkYTM1YzY1ODcyMTcxOGFiMjZkYzJmZjY3ZDg3In19fQ=="), Component.text("Grade C")),
			newItem(Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODk0MzEyNGJmNmQ1NzkyNTExYmI4YWM1YTc5NzhlMjM2Yjc0ZmM3ZmMxZDM5YWY5NTVlY2VkZjk3YWI0In19fQ=="), Component.text("Grade D")),
			newItem(Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTE4NjQxMDE2ZDFkN2NiNDA2YTg2M2RkZTgxZjZiYmQzMDM3MWNmNDZkOGYxNDIwY2Q1ZWM2Y2RiNjRlZjcifX19"), Component.text("Grade E")),
			newItem(Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2M0YzM5OWI1ZTc1MjJhMTdkMzVlYjUzZmU1ZWI3NjdhM2IzOGVhNmU1Y2QzM2I0NDM4MDIwZmM1YTg0OGEifX19"), Component.text("Grade F"))
	};

	
	private final Skill skill;
	private final Map<Integer, SkillPerk> perks;
	private final Map<Integer, SkillPerk> mapping = new HashMap<Integer, SkillPerk>();
	
	public BeanGuiSkillsDetails(Player p, Skill skill) {
		super(p);
		
		this.skill = skill;
		this.perks = skill.getPerkTree();
		setName(skill.getName() + " Skill");
		this.presetSize = 54;
		this.presetInv = new ItemStack[] {
				bBlank,bBlank,bBlank,bBlank,newItem(skill.getDisplayStack(), skill.toComponent()),bBlank,bBlank,bBlank,bBlank,
				bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,
				bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,
				bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,
				bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,
				blank,blank,blank,blank,goBack,blank,blank,blank,blank
		};
		
		page = 2;
	}

	@Override
	public void onInventoryClosed(InventoryCloseEvent e) {
	}

	@Override
	public void onInventoryClicked(InventoryClickEvent e) {
		int slot = e.getRawSlot();
		
		switch(slot) {
		case 46: // Previous Skill
			List<Skill> types = Skill.getRegisteredSkills();
			int a = types.indexOf(skill) - 1;
			if (a < 0) a = types.size() - 1;
			
			new BeanGuiSkillsDetails(p, types.get(a)).openInventory();
			return;
		case 52: // Next Skill
			List<Skill> typez = Skill.getRegisteredSkills();
			int b = typez.indexOf(skill) + 1;
			if (b >= typez.size()) b = 0;
			
			new BeanGuiSkillsDetails(p, typez.get(b)).openInventory();
			return;
		}
	}

	@Override // TODO:
	public void onInventoryOpened() {
		final ItemStack[] contents = presetInv.clone();
		List<Skill> types = Skill.getRegisteredSkills();
		int a = types.indexOf(skill) - 1;
		if (a < 0) a = types.size() - 1;
		contents[46] = newItem(types.get(a).getDisplayStack(), types.get(a).toComponent());
		a = types.indexOf(skill) + 1;
		if (a >= types.size()) a = 0;
		contents[52] = newItem(types.get(a).getDisplayStack(), types.get(a).toComponent());
		
		if (page > 0) contents[8] = prevPage;
		if (page < 2) contents[44] = nextPage;
		
		// Upgrade Tree
		mapping.clear();
		for (int row = -1; ++row < 5;) {
			int pOffset = page * 5;
			final int reqRank = 4 - (page * 3) + 15 - (row * 3);
			boolean isGrade = tpp.getSkillLevel(skill) >= reqRank;
			
			contents[0 + row * 9] = grades[row + page + (isGrade ? 7 : 0)];
			Component grade = grades[row + page].getItemMeta().displayName();
			
			for (int column = -1; ++column < 5;) {
				SkillPerk perk = perks.getOrDefault(row * 5 + column + pOffset, null);
				if (perk == null) continue;
				
				int slot = 2 + column + (row * 9);
				int perkLevel = tpp.getSkills().getPerkLevel(perk);
				mapping.put(slot, perk);
				
				ItemStack item = new ItemStack(perkLevel < 1 ? Material.COAL : skill.getDye());
				item.editMeta(meta -> {
					meta.displayName(perk.getName().color(tpp.getSkillLevel(skill) >= reqRank ? skill.getColour() : NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false));
					
					if (tpp.getSkillLevel(skill) >= reqRank)
						if (perkLevel < 1)
							meta.displayName(meta.displayName().append(Component.text(" (Unlockable)", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)));
						else
							meta.displayName(meta.displayName().append(Component.text("\u00a77 (Level \u00a7r" + perkLevel + "\u00a77/\u00a7r" + perk.getMaxLevel() + "\u00a77)").colorIfAbsent(TextColor.color(0xbbbbbb))));
					
					if (perkLevel > 0) {
						meta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
						meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
					}
					
					List<Component> lore = new ArrayList<Component>();
					lore.addAll(perk.getInformation(perkLevel));
					
					boolean upgradePerm = perkLevel < perk.getMaxLevel() && tpp.getSkillLevel(skill) >= reqRank;
					
					if (upgradePerm) {
						lore.add(Component.empty());
						if (perkLevel > 0) {
							lore.add(Component.text("Next Level: ", skill.getColour()).decoration(TextDecoration.ITALIC, false));
							lore.addAll(perk.getInformation(perkLevel + 1));
						}
						lore.add(Component.text("----------------------", NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false));
						lore.add(Component.text("Cost", TextColor.color(0xafafaf)).decoration(TextDecoration.ITALIC, false));
						lore.add(Component.text(perk.getCost() + " " + skill.getName() + " Skill Point" + (perk.getCost() > 1 ? "s" : ""), skill.getColour()).decoration(TextDecoration.ITALIC, false));
					}
					
					lore.add(Component.empty());
					
					if (tpp.getSkillLevel(skill) < reqRank)
						lore.add(Component.text("» Requires ", NamedTextColor.RED).append(grade).decoration(TextDecoration.ITALIC, false));
					else if (perkLevel >= perk.getMaxLevel())
						lore.add(Component.text("» Maxed out!", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
					else if (tpp.getSkills().getSkillPoints(skill) < perk.getCost())
						lore.add(Component.text("» Insufficient Skill Points", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
					else if (perkLevel == 0)
						lore.add(Component.text("\u00a76» \u00a7eClick to unlock!"));
					else
						lore.add(Component.text("\u00a76» \u00a7eClick to upgrade!"));
					meta.lore(lore);
				});
				
				contents[slot] = item;
			}
		}
		
		
		
		Highscore highscore = getPlugin().highscores.getHighscore(skill.getName() + " XP");
		ItemStack head = new ItemStack(Material.CARTOGRAPHY_TABLE);
		
		List<Component> scoreLore = new ArrayList<Component>();
		boolean hasViewer = false;
		scoreLore.add(Component.text(""));
		for (int x = -1; ++x < 8;) {
			int peep = highscore.getOrder().get(x);
			if (peep == tpp.getId()) 
				hasViewer = true;
			scoreLore.add(Component.text("\u00a78" + highscore.getPositionOf(peep) + ". ").append(PlayerProfile.getDisplayName(peep).decoration(TextDecoration.ITALIC, false)).append(Component.text("\u00a78 with \u00a7b" + df.format(highscore.getScoreOf(peep)) + " XP")));
		}
		
		if (!hasViewer) {
			int peep = tpp.getId();
			scoreLore.add(Component.text("\u00a78..."));
			scoreLore.add(Component.text("\u00a78" + highscore.getPositionOf(peep) + ". ").append(PlayerProfile.getDisplayName(peep).decoration(TextDecoration.ITALIC, false)).append(Component.text("\u00a78 with \u00a7b" + df.format(highscore.getScoreOf(peep)) + " XP")));
		}
		
		head.editMeta(meta -> {
			meta.displayName(Component.text(skill.getName() + " Leaderboard", skill.getColour()).decoration(TextDecoration.ITALIC, false));
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
		
		if (pp.onCdElseAdd("guiClick", 300, true))
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
	}*/
}
