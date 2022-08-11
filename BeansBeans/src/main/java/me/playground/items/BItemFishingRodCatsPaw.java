package me.playground.items;

import me.playground.items.lore.Lore;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;

public class BItemFishingRodCatsPaw extends BItemFishingRod {
	
	protected BItemFishingRodCatsPaw(int numeric, String identifier, String name, ItemRarity rarity, int modelDataInt, int durability) {
		super(numeric, identifier, name, rarity, modelDataInt, durability);
		setForbiddenEnchantments(Enchantment.LURE);
		addRepairMaterial(Material.COD, 6f);
		addRepairMaterial(Material.SALMON, 11f);
		addRepairMaterial(Material.TROPICAL_FISH, 25f);
		setDefaultLore(Lore.getBuilder("This rod subtly mimics the experience of fishing like a feline.", "", "Bites occur within &#dddddd5 - 7 seconds&r but have a &c70%&r chance to escape.").build().getLore());
	}
	
	@Override
	public void onFish(PlayerFishEvent e) {
		if (e.getState() == State.FISHING) {
			e.getHook().setApplyLure(false);
			e.getHook().setMinWaitTime(5);
			e.getHook().setMaxWaitTime(7);
		} else if (e.getState() == State.CAUGHT_FISH) {
			if (getRandom().nextInt(100) >= 30) {
				e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_CAT_HISS, 0.35f, 1f);
				e.getHook().getWorld().spawnParticle(Particle.CRIT, e.getHook().getLocation(), 4);
				e.setCancelled(true);
				e.getHook().remove();
				//new PlayerItemDamageEvent(e.getPlayer(), e.getPlayer()., 1).callEvent(); idk
				return;
			}
			e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_CAT_PURR, 0.5f, 1f);
		}
	}
	
}
