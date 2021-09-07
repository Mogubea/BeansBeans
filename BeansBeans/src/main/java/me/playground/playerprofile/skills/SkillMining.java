package me.playground.playerprofile.skills;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class SkillMining extends Skill {

	public SkillMining(String name, char color, BarColor bar, Material ui, Material uiBar) {
		super(name, color, bar, ui, uiBar);
	}

	@Override
	public boolean doSkillEvent(final SkillData sd, final Event event) {
		if (event instanceof BlockBreakEvent) {
			final BlockBreakEvent e = (BlockBreakEvent) event;
			final ItemStack i = e.getPlayer().getInventory().getItemInMainHand();
			
			if (e.getBlock().isValidTool(i)) {
				int xp = getMiningValue(e.getBlock());
				sd.addXp(getSkillType(), xp);
				
				if (e.getExpToDrop() > 0)
					e.setExpToDrop((int)((float)e.getExpToDrop() * (1 + ((float)sd.getLevel(getSkillType()) / 1000F))));
				return xp > 0;
			}
		}
		
		return false;
	}
	
	private int getMiningValue(Block b) {
		switch(b.getType()) {
		case NETHERRACK: return 1;
		case STONE: return 4;
		case DEEPSLATE: return 6;
		case SANDSTONE: case TUFF: case DIORITE: case ANDESITE: case GRANITE: case BASALT: case BLACKSTONE: return 8;
		case DRIPSTONE_BLOCK: case END_STONE: return 11;
		case CALCITE: case AMETHYST_BLOCK: return 14;
		case MOSSY_COBBLESTONE: return 20;
		case AMETHYST_CLUSTER: return 25;
		case OBSIDIAN: case CRYING_OBSIDIAN: return 40;
		case COAL_ORE: return 40;
		case DEEPSLATE_COAL_ORE: return 45;
		case COPPER_ORE: return 60;
		case DEEPSLATE_COPPER_ORE: return 70;
		case IRON_ORE: return 80;
		case DEEPSLATE_IRON_ORE: return 90;
		case GOLD_ORE: return 100;
		case DEEPSLATE_GOLD_ORE: return 110;
		case BUDDING_AMETHYST: return 110;
		case NETHER_QUARTZ_ORE: return 105;
		case NETHER_GOLD_ORE: return 125;
		case REDSTONE_ORE: return 160;
		case DEEPSLATE_REDSTONE_ORE: return 180;
		case GILDED_BLACKSTONE: return 180;
		case LAPIS_ORE: return 290;
		case DEEPSLATE_LAPIS_ORE: return 350;
		case DIAMOND_ORE: return 720;
		case EMERALD_ORE: return 1020;
		case DEEPSLATE_DIAMOND_ORE: return 1200;
		case ANCIENT_DEBRIS: return 2700;
		case DEEPSLATE_EMERALD_ORE: return 5005;
		default: 
			if (b.getType().name().endsWith("TERRACOTTA") && !b.getType().name().endsWith("GLAZED_TERRACOTTA"))
				return 6;
			return 0;
		}
	}
	
	@Override
	protected SkillType getSkillType() {
		return SkillType.MINING;
	}
	
	

}
