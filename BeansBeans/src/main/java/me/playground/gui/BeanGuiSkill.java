package me.playground.gui;

import me.playground.enchants.BEnchantment;
import me.playground.items.lore.Lore;
import me.playground.skills.PlayerSkillData;
import me.playground.skills.Skill;
import me.playground.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class BeanGuiSkill extends BeanGui {

	protected static final ItemStack gBlank = newItem(new ItemStack(Material.GRAY_STAINED_GLASS_PANE), Component.empty());
	protected final Skill skill;
	protected final TextColor lightCol, lighterCol;
	protected final ItemStack[] basePreset;
	private final int milestoneSlot = 24;
	private final int leaderboardSlot = 34;

	public BeanGuiSkill(@NotNull Player p, @NotNull Skill skill) {
		super(p);

		this.skill = skill;
		this.lightCol = TextColor.color(skill.getColour().value() | 0x3d3d3d);
		this.lighterCol = TextColor.color(skill.getColour().value() | 0x6d6d6d);
		setName(skill.getNameWithIcon() + " Menu");
		this.presetSize = 54;

		loadBasePresetInventory();
		loadPresetInventory();
		this.basePreset = presetInv;
	}

	protected void loadPresetInventory() {
		PlayerSkillData data = tpp.getSkills();
		int col = skill.getColour().value();

		// Milestone Diamond Item
		ItemStack milestoneItem = newItem(new ItemStack(Material.DIAMOND), Component.text("Milestones", skill.getColour()));
		milestoneItem.editMeta(meta -> {
			List<Component> lore = new ArrayList<>(Lore.fastBuild(true, 29, "Complete various tasks to unlock and upgrade your " + skill.getName() + " &9\u2B50 Milestones&r.\n"));

			int msP = data.getMilestoneScore(skill);
			int msM = data.getMilestoneManager().getMaxMilestoneScore(skill);
			double percent = ((double)msP/(double)msM) * 100;

			int spaces = (int) (20 - ((df.format(msP) + "/" + df.format(msM) + " ("+dec.format(percent)+"%)").length() * 0.75F));
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

			lore.addAll(Lore.getBuilder("\nUpgrading &9\u2B50 Milestones&r will reward you with &6Coins&r, &" + skill.getColour().asHexString() + skill.getName() + " XP &rand &" + lightCol.asHexString() + "\ud83d\udd25 Essence&r!", "\n&" + lightCol.asHexString() + "» &" + lighterCol.asHexString() + "Click to view!").setLineLimit(20).build().getLore());

			meta.lore(lore);
			meta.addEnchant(BEnchantment.FAKE_GLOW, 1, true);
		});

		// Highscores Item
		ItemStack leaderboardItem = newItem(Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2Y0MDk0MmYzNjRmNmNiY2VmZmNmMTE1MTc5NjQxMDI4NmE0OGIxYWViYTc3MjQzZTIxODAyNmMwOWNkMSJ9fX0="), Component.text("Leaderboard", skill.getColour()));
		leaderboardItem.editMeta(meta -> {

		});

		presetInv[milestoneSlot] = milestoneItem;
		presetInv[leaderboardSlot] = leaderboardItem;
	}

	@Override
	public void onInventoryClicked(InventoryClickEvent e) {
		switch(e.getRawSlot()) {
			case milestoneSlot -> new BeanGuiSkillMilestones(p, skill).openInventory();
//			case leaderboardSlot -> new BeanGuiSkillLeaderboard(p, skill).openInventory();
		}
	}

	@Override
	public void onBackPress() {
		new BeanGuiSkills(p).openInventory();
	}

	protected void loadBasePresetInventory() {
		presetInv = new ItemStack[presetSize];
		ItemStack frame = newItem(new ItemStack(skill.getGlassPane()), Component.text(skill.getNameWithIcon() + " Menu " + skill.getIcon(), skill.getColour()));
		this.presetInv = new ItemStack[] {
				frame, frame,bBlank,bBlank,skill.getDisplayStack(),bBlank,bBlank, frame, frame,
				frame,bBlank,gBlank,bBlank,bBlank,bBlank,gBlank,bBlank, frame,
				bBlank,gBlank,bBlank,gBlank,bBlank,gBlank,bBlank,gBlank,bBlank,
				gBlank,bBlank,bBlank,bBlank,gBlank,bBlank,bBlank,bBlank,gBlank,
				bBlank,gBlank,gBlank,gBlank,bBlank,gBlank,gBlank,gBlank,bBlank,
				frame, frame, frame, frame,goBack, frame, frame, frame, frame
		};
	}

}
