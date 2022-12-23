package me.playground.gui;

import me.playground.enchants.BEnchantment;
import me.playground.items.lore.Lore;
import me.playground.skills.*;
import me.playground.utils.BeanColor;
import me.playground.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BeanGuiSkillMilestones extends BeanGuiSkill {

	private final int filterSlot = 51;
	private Filter filter = Filter.NONE;
	private final List<Milestone> visibleMilestones;
	private final Map<Integer, Milestone> mappings = new HashMap<>();

	public BeanGuiSkillMilestones(@NotNull Player p, @NotNull Skill skill) {
		super(p, skill);

		visibleMilestones = tpp.getSkills().getMilestoneManager().getMilestones(skill);
		preparePresetInventory(0);
	}

	@Override
	protected void loadPresetInventory() {
		PlayerSkillData data = tpp.getSkills();
		int col = skill.getColour().value();

		// Milestone Diamond Item
		ItemStack milestoneItem = newItem(skill.getDisplayStack(), Component.text(skill.getName() + " Milestones", skill.getColour()));
		milestoneItem.editMeta(meta -> {
			List<Component> lore = new ArrayList<>(Lore.fastBuild(true, 29, "Complete various tasks to unlock and upgrade your " + skill.getName() + " &9\u2B50 Milestones&r.\n"));

			int msP = data.getMilestoneScore(skill);
			int msM = data.getMilestoneManager().getMaxMilestoneScore(skill);
			double percent = ((double)msP/(double)msM) * 100;

			int spaces = (int) (20 - ((msP + "/" + msM + " ("+dec.format(percent)+"%)").length() * 0.75F));
			StringBuilder spaceb = new StringBuilder();
			for (int a = -1; ++a < spaces;)
				spaceb.append(" ");
			final String space = spaceb.toString();

			lore.add(Component.text(" \u2B50 ", NamedTextColor.RED).append(Utils.getProgressBar('-', 20, msP, msM, 0x454565, col).append(Component.text(" \u2B50 ", NamedTextColor.GREEN))).decoration(TextDecoration.ITALIC, false));
			lore.add(Component.text(space + "\u00a7" + skill.getColourCode() + df.format(msP))
					.append(Component.text("/", NamedTextColor.DARK_GRAY)
							.append(Component.text("\u00a7" + skill.getColourCode() + df.format(msM))
									.append(Component.text(" (", NamedTextColor.DARK_GRAY)
											.append(Component.text(dec.format(percent) + "%", NamedTextColor.GRAY)
													.append(Component.text(")", NamedTextColor.DARK_GRAY)))))).decoration(TextDecoration.ITALIC, false));

			meta.lore(lore);
			meta.addEnchant(BEnchantment.FAKE_GLOW, 1, true);
		});

		presetInv[4] = milestoneItem;
		presetInv[filterSlot] = getFilterItem();
	}

	@Override
	public void onInventoryClicked(InventoryClickEvent e) {
		int rawSlot = e.getRawSlot();

		switch(rawSlot) {
			case filterSlot -> {
				Filter[] values = Filter.values();
				if (e.isLeftClick()) {
					if (e.isShiftClick())
						filter = values[values.length - 1];
					else
						filter = filter.ordinal() >= (values.length - 1) ? values[0] : values[filter.ordinal() + 1];
				} else if (e.isRightClick()) {
					if (e.isShiftClick())
						filter = values[0];
					else
						filter = filter.ordinal() == 0 ? values[values.length - 1] : values[filter.ordinal() - 1];
				}

				i.setItem(filterSlot, getFilterItem());
			}
			default -> {
				Milestone milestone = mappings.get(rawSlot);
				pp.setWatchingMilestone(milestone, !pp.isWatching(milestone));
				if (milestone == null) return;

				i.setItem(e.getRawSlot(), getMilestoneItem(milestone));
				p.playSound(p.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 0.25F, 1.0F);
			}
		}
	}

	@Override
	public void onBackPress() {
		new BeanGuiSkill(p, skill).openInventory();
	}

	@Override
	public void pageUp() {
		preparePresetInventory(++this.page);
		openInventory();
	}

	@Override
	public void pageDown() {
		preparePresetInventory(--this.page);
		openInventory();
	}

	/**
	 * Set the new Inventory for when the inventory gets opened again.
	 */
	private void preparePresetInventory(int page) {
		ItemStack[] newInv = basePreset;
		List<Milestone> milestones = new ArrayList<>();

		if (filter != Filter.NONE) {
			int size = visibleMilestones.size();
			if (filter == Filter.NOT_OBTAINED) {
				for (int x = -1; ++x < size;) {
					Milestone milestone = visibleMilestones.get(x);
					if (!tpp.getSkills().getMilestoneTier(milestone).lowerThan(milestone.getTier())) continue;
					milestones.add(milestone);
				}
			} else {
				for (int x = -1; ++x < size;) {
					Milestone milestone = visibleMilestones.get(x);
					if (tpp.getSkills().getMilestoneTier(milestone) != filter.tier) continue;
					milestones.add(milestone);
				}
			}
		} else {
			milestones.addAll(visibleMilestones);
		}

		int maxPerPage = 28;
		int size = milestones.size();
		int maxThisPage = Math.min(size - (maxPerPage * page), maxPerPage);
		this.page = Math.min(page, size/maxPerPage);
		int maxPages = size/maxPerPage + 1;

		newInv[48] = page > 0 ? prevPage : basePreset[48];
		newInv[50] = size > (maxPerPage * (page+1)) ? nextPage : basePreset[50];

		// Loop through all visible commands while respecting the current page and max page limits.
		for (int rx = maxThisPage, idx = (maxPerPage * page); rx > 0; ++idx) {
			int slot = (maxThisPage-rx + 10 + (((maxThisPage-rx) / 7) * 2)); // Calculate the slot position
			if (idx >= size) break; // If exceeds the array size, break

			mappings.put(slot, milestones.get(idx));
			newInv[slot] = getMilestoneItem(milestones.get(idx));
			rx--;
		}

		setName(skill.getNameWithIcon() + " Milestones" + (maxPages > 1 ? " ("+(page+1)+"/"+maxPages+")" : ""));

		newInv[filterSlot] = getFilterItem();
		this.presetInv = newInv;
	}

	private ItemStack getMilestoneItem(Milestone milestone) {
		MilestoneTier tier = tpp.getSkills().getMilestoneTier(milestone);

		ItemStack item = newItem(new ItemStack(tier.getMaterial()), Component.text(milestone.getName(), tier.getColour()).append(Component.text(" \u00a7f[\u00a79" + milestone.getValueOf(tier) + " \u2B50\u00a7f]")));
		item.editMeta(meta -> {
			boolean maxed = !(tier.lowerThan(milestone.getMaxTier()));

			List<Component> lore = new ArrayList<>();
			lore.add(Component.text(tier.lowerThan(milestone.getTier()) ? "Unobtained Milestone" : tier.getName() + " Tier Milestone", NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false));

			Instant timeOfObtaining = tpp.getSkills().getMilestoneTime(milestone, tier);
			if (timeOfObtaining != null)
				lore.add(Component.text(DateTimeFormatter.RFC_1123_DATE_TIME.format(timeOfObtaining), BeanColor.MILESTONE_TIME));

			lore.add(Component.empty());
			lore.addAll(milestone.getLore().getLore());
			lore.add(Component.empty());

			int msP = tpp.getSkills().getMilestoneProgress(milestone);
			int msM = milestone.getRequirementFor(tier.tierUp());
			double percent = maxed ? 100 : ((double)msP/(double)msM) * 100;

			int spaces = (int) (20 - ((msP + "/" + msM + " ("+dec.format(percent)+"%)").length() * 0.75F));
			StringBuilder spaceb = new StringBuilder();
			for (int a = -1; ++a < spaces;)
				spaceb.append(" ");
			final String space = spaceb.toString();

			MilestoneTier nextTier = tier.tierUp().lowerThan(milestone.getTier()) ? milestone.getTier() : tier.tierUp();
			int valueIncrease = (milestone.getValueOf(nextTier)-milestone.getValueOf(tier));

			lore.add(Component.text(" \u2B50 ", tier.getColour()).append(Utils.getProgressBar('-', 20, msP, msM, 0x454565, skill.getColour().value())).append(Component.text(" \u2B50 ", nextTier.getColour())).decoration(TextDecoration.ITALIC, false));
			lore.add(Component.text(space + "\u00a7" + skill.getColourCode() + df.format(msP))
					.append(Component.text("/", NamedTextColor.DARK_GRAY)
							.append(Component.text("\u00a7" + skill.getColourCode() + df.format(msM))
									.append(Component.text(" (", NamedTextColor.DARK_GRAY)
											.append(Component.text(dec.format(percent) + "%", NamedTextColor.GRAY)
													.append(Component.text(")", NamedTextColor.DARK_GRAY)))))).decoration(TextDecoration.ITALIC, false));

			lore.add(Component.empty());

			if (maxed) {
				lore.add(Component.text(" \u2B50 Milestone Completed! \u2B50 ", skill.getColour()).decoration(TextDecoration.ITALIC, false));
				lore.add(Component.empty());
			} else {
				lore.addAll(Lore.getBuilder("&" + nextTier.getColour().asHexString() + nextTier.getName() + " Tier &rRewards:",
						"&8 • &6" + df.format(valueIncrease * 250L) + " Coins",
						"&8 • &" + skill.getColour().asHexString() + df.format(valueIncrease * 1500L) + " " + skill.getNameWithIcon() + " XP",
						"&8 • &" + lightCol.asHexString() + df.format(valueIncrease * 2L) + " \ud83d\udd25 " + skill.getName() + " Essence",
						"&8 • &9" + valueIncrease + " \u2B50 Milestone Score",
						"\n&" + lightCol.asHexString() + "» &" + lighterCol.asHexString() + "Click to " + (pp.isWatching(milestone) ? "un-watch" : "watch") + "!").setLineLimit(38).build().getLore());
			}

			if (pp.isWatching(milestone))
				meta.addEnchant(BEnchantment.FAKE_GLOW, 1, true);

			meta.lore(lore);
		});

		return item;
	}

	private ItemStack getFilterItem() {
		ItemStack filterItem = newItem(new ItemStack(Material.HOPPER), Component.text("Milestone Filter", skill.getColour()));
		filterItem.editMeta(meta -> {
			if (filter != Filter.NONE) meta.addEnchant(BEnchantment.FAKE_GLOW, 1, true);
			List<Component> lore = new ArrayList<>(Lore.fastBuild(true, 30, "Filter through the list of &9Milestones&r.\n"));
			for (Filter filter : Filter.values()) {
				boolean isFilter = this.filter == filter;
				lore.add(Component.text(isFilter ? "  \u00a7f\u25b6 " : " \u00a78\u25b6 ").append(Component.text(filter.name, isFilter ? filter.tier.getColour() : NamedTextColor.DARK_GRAY)).decoration(TextDecoration.ITALIC, false));
			}

			lore.addAll(Lore.fastBuild(true, 34, "&" + lightCol.asHexString() + "» &" + lighterCol.asHexString() + "Click to swap!"));
			meta.lore(lore);
		});

		return filterItem;
	}

	@Override
	protected void loadBasePresetInventory() {
		presetInv = new ItemStack[presetSize];
		ItemStack frame = newItem(new ItemStack(skill.getGlassPane()), Component.text(skill.getNameWithIcon() + " Menu " + skill.getIcon(), skill.getColour()));
		this.presetInv = new ItemStack[] {
				frame, frame,bBlank,bBlank,skill.getDisplayStack(),bBlank,bBlank, frame, frame,
				frame,gBlank,gBlank,gBlank,gBlank,gBlank,gBlank,gBlank, frame,
				bBlank,gBlank,gBlank,gBlank,gBlank,gBlank,gBlank,gBlank,bBlank,
				bBlank,gBlank,gBlank,gBlank,gBlank,gBlank,gBlank,gBlank,bBlank,
				bBlank,gBlank,gBlank,gBlank,gBlank,gBlank,gBlank,gBlank,bBlank,
				frame, frame, frame, frame,goBack, frame, frame, frame, frame
		};
	}

	private enum Filter {
		NONE("No Filter", MilestoneTier.STONE),
		NOT_OBTAINED("Unobtained", MilestoneTier.NONE),
		DIRT("Dirt Tier", MilestoneTier.DIRT),
		WOOD("Wood Tier", MilestoneTier.WOOD),
		STONE("Stone Tier", MilestoneTier.STONE),
		COPPER("Copper Tier", MilestoneTier.COPPER),
		IRON("Iron Tier", MilestoneTier.IRON),
		GOLD("Gold Tier", MilestoneTier.GOLD),
		DIAMOND("Diamond Tier", MilestoneTier.DIAMOND),
		EMERALD("Emerald Tier", MilestoneTier.EMERALD);

		private final String name;
		private final MilestoneTier tier;

		Filter(String displayName, MilestoneTier tier) {
			this.name = displayName;
			this.tier = tier;
		}
	}

}
