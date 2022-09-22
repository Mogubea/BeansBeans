package me.playground.skills;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public class SkillMining extends Skill {
	
	protected SkillMining() {
		super("Mining", 0x77cacf, BarColor.BLUE, '3', "\u26cf", Material.DIAMOND_PICKAXE, Material.CYAN_DYE,
				"Earn Mining XP by excavating worlds of their many valuable ores and minerals!");
		addPerk(0, 2, SkillPerk.MINING_TEST13); // S Tier
		addPerk(1, 1, SkillPerk.MINING_TEST10).addPerk(1, 2, SkillPerk.MINING_TEST11).addPerk(1, 3, SkillPerk.MINING_TEST12); // A Tier
		addPerk(2, 2, SkillPerk.MINING_TEST13); // B Tier
		addPerk(3, 2, SkillPerk.MINING_TEST13); // C Tier
		addPerk(4, 2, SkillPerk.MINING_TEST13); // D Tier
		addPerk(5, 2, SkillPerk.MINING_TEST13); // E Tier
		addPerk(6, 2, SkillPerk.MINING_PROFICIENCY); // F Tier
	}
	
	@Override
	protected boolean doSkillEvent(final Skills s, final Event e) {
		boolean blockBreak;
		if (!((blockBreak = e instanceof BlockBreakEvent) || e instanceof BlockDropItemEvent)) return false;
		Player p = blockBreak ? ((BlockBreakEvent)e).getPlayer() : ((BlockDropItemEvent)e).getPlayer();
		ItemStack i = p.getInventory().getItemInMainHand();
		Block b = ((BlockEvent)e).getBlock();
		
		if (!b.isValidTool(i)) return false;
		boolean isSilk = i.containsEnchantment(Enchantment.SILK_TOUCH);

		int skillXP = getExperienceValue(b);
		if (skillXP < 1) return false;
		
		return blockBreak ? onBlockBreak(s, (BlockBreakEvent) e, skillXP) : onBlockDrop(s, (BlockDropItemEvent) e, isSilk);
	}
	
	/**
	 * Handles the skill EXP and skills that occur on breaking the block
	 */
	private boolean onBlockBreak(Skills s, BlockBreakEvent e, int skillXP) {
		s.addExperience(this, skillXP);
		
		// Ore Experience Multiplier
		/*int expDropped = event.getExpToDrop();
		if (expDropped > 0) {
			double expMultiplier = 1;
			event.setExpToDrop((int) ((double)expDropped * expMultiplier));
		}*/
		return true;
	}
	
	/**
	 * Handles any drop changes
	 */
	private boolean onBlockDrop(Skills s, BlockDropItemEvent e, boolean isSilk) {
		// Extra Item Drop
		/*double chance = -1;
		int bonusDrops = 0;
		if (bonusDrops > 0 && rand.nextDouble() <= chance) {
			ItemStack i = e.getItems().get(0).getItemStack();
			i.setAmount(i.getAmount() + bonusDrops);
		}*/
		
		return true;
	}
	
	
	private int getExperienceValue(Block b) {
		final Material material = b.getType();
		
		switch(material) {
		case NETHERRACK: return 1;
		case STONE: return 3;
		case DEEPSLATE: return 6;
		case SANDSTONE: case TUFF: case DIORITE: case ANDESITE: case GRANITE: case BASALT: case SMOOTH_BASALT: case BLACKSTONE: case CALCITE: return 6;
		case MAGMA_BLOCK: case GLOWSTONE: return 10;
		case DRIPSTONE_BLOCK: case END_STONE: return 11;
		case AMETHYST_BLOCK: return 14;
		case MOSSY_COBBLESTONE: return 20;
		case AMETHYST_CLUSTER: return 24;
		case COAL_ORE: return 35;
		case OBSIDIAN: case CRYING_OBSIDIAN: return 45;
		case IRON_ORE: return 50;
		case COPPER_ORE: return 55;
		case DEEPSLATE_IRON_ORE: return 75;
		case DEEPSLATE_COPPER_ORE: return 80;
		case GOLD_ORE: return 95;
		case DEEPSLATE_GOLD_ORE: return 105;
		case BUDDING_AMETHYST: return 110;
		case NETHER_QUARTZ_ORE: return 105;
		case NETHER_GOLD_ORE: return 125;
		case DEEPSLATE_REDSTONE_ORE: return 150;
		case REDSTONE_ORE: return 160;
		case GILDED_BLACKSTONE: return 180;
		case LAPIS_ORE: return 300;
		case DEEPSLATE_LAPIS_ORE: return 300;
		case DEEPSLATE_DIAMOND_ORE: return 720;
		case DIAMOND_ORE: return 770;
		case EMERALD_ORE: return 850;
		case DEEPSLATE_EMERALD_ORE: return 1100;
		case DEEPSLATE_COAL_ORE: return 1300;
		case ANCIENT_DEBRIS: return 2600; // Annoying bastard.
		default: 
			if (b.getType().name().endsWith("TERRACOTTA") && !b.getType().name().endsWith("GLAZED_TERRACOTTA"))
				return 6;
			return 0;
		}
	}

	@Override
	public List<Component> getGUIDescription(Skills s) {
		List<Component> desc = new ArrayList<>();
		
		
		int proficiency = s.getPerkLevel(SkillPerk.MINING_PROFICIENCY);
		desc.add(Component.text("• ").append(Component.text(proficiency + "%", proficiency < 100 ? NamedTextColor.RED : NamedTextColor.GREEN)).append(Component.text(" Pickaxe Proficiency \u26a1", TextColor.color(0xaaaa11))));
		desc.add(Component.text("Pickaxe Proficiency affects the amount of", NamedTextColor.DARK_GRAY));
		desc.add(Component.text("\u00a77\u00a7oDurability \u00a78\u00a7oand \u00a76\u00a7oHunger \u00a78\u00a7oconsumed when using"));
		desc.add(Component.text("Pickaxes. Your current Pickaxe proficiency equates to;", NamedTextColor.DARK_GRAY));
		desc.add(Component.text("• ").append(Component.text(proficiency + "%", proficiency < 100 ? NamedTextColor.RED : NamedTextColor.GREEN)).append(Component.text(" Durability Usage")));
		desc.add(Component.text("• ").append(Component.text(proficiency + "%", proficiency < 100 ? NamedTextColor.RED : NamedTextColor.GREEN)).append(Component.text(" Hunger Drain")));
		
		return desc;
	}

}
