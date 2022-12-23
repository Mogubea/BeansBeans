package me.playground.skills;

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

public class SkillMining extends Skill {
	
	protected SkillMining() {
		super("Mining", 0x77cacf, BarColor.BLUE, '3', "\u26cf", Material.DIAMOND_PICKAXE, Material.CYAN_DYE,
				"Earn Mining XP by excavating worlds of their many valuable ores and minerals!");
	}
	
	@Override
	protected boolean doSkillEvent(final PlayerSkillData s, final Event e) {
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
	private boolean onBlockBreak(PlayerSkillData s, BlockBreakEvent e, int skillXP) {
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
	private boolean onBlockDrop(PlayerSkillData s, BlockDropItemEvent e, boolean isSilk) {
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
			case DEEPSLATE: case SANDSTONE: case TUFF: case DIORITE: case ANDESITE: case GRANITE: case BASALT: case SMOOTH_BASALT: case BLACKSTONE: case CALCITE: return 5;
			case MAGMA_BLOCK: case GLOWSTONE: return 10;
			case DRIPSTONE_BLOCK: case END_STONE: return 11;
			case AMETHYST_BLOCK: return 12;
			case MOSSY_COBBLESTONE: return 16;
			case AMETHYST_CLUSTER: return 22;
			case COAL_ORE: return 32;
			case OBSIDIAN: case CRYING_OBSIDIAN: return 45;
			case IRON_ORE: return 45;
			case COPPER_ORE: return 55;
			case DEEPSLATE_IRON_ORE: return 75;
			case DEEPSLATE_COPPER_ORE: return 80;
			case GOLD_ORE: return 90;
			case DEEPSLATE_GOLD_ORE: return 100;
			case BUDDING_AMETHYST: return 103;
			case NETHER_QUARTZ_ORE: return 105;
			case NETHER_GOLD_ORE: return 110;
			case DEEPSLATE_REDSTONE_ORE: return 130;
			case REDSTONE_ORE: return 140;
			case GILDED_BLACKSTONE: return 200;
			case LAPIS_ORE: return 260;
			case DEEPSLATE_LAPIS_ORE: return 260;
			case RAW_IRON_BLOCK: return 400;
			case DEEPSLATE_DIAMOND_ORE: return 650;
			case DIAMOND_ORE: return 660;
			case EMERALD_ORE: return 750;
			case DEEPSLATE_COAL_ORE: return 800;
			case DEEPSLATE_EMERALD_ORE: return 900;
			case ANCIENT_DEBRIS: return 2400;
			default: 
				if (b.getType().name().endsWith("TERRACOTTA") && !b.getType().name().endsWith("GLAZED_TERRACOTTA"))
					return 6;
				return 0;
		}
	}
}
