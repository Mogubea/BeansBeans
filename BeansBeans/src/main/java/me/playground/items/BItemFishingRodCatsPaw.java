package me.playground.items;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;

import me.playground.utils.BeanColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * This rod finds a fish within 3-4 seconds, but has a high chance to fail.
 * @author Beandon
 */
public class BItemFishingRodCatsPaw extends BItemFishingRod {
	
	protected BItemFishingRodCatsPaw(int numeric, String identifier, String name, ItemRarity rarity, int modelDataInt, int durability) {
		super(numeric, identifier, name, rarity, modelDataInt, durability);
		setForbiddenEnchantments(Enchantment.LURE);
		addRepairMaterial(Material.COD, 4f);
		addRepairMaterial(Material.SALMON, 8f);
		addRepairMaterial(Material.TROPICAL_FISH, 20f);
		setDefaultLore(
				Component.text("A strange fishing rod that subtly mimicks", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
				Component.text("the experience of fishing like a feline.", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
				Component.empty(),
				Component.text("Bites occur within ", NamedTextColor.GRAY).append(Component.text("3 - 5 seconds ", TextColor.color(0xdddddd)).append(Component.text("but have a", NamedTextColor.GRAY))).decoration(TextDecoration.ITALIC, false),
				Component.text("70% chance to ", NamedTextColor.GRAY).append(Component.text("escape", TextColor.color(0xaa4444)).append(Component.text(". Incompatible with ", NamedTextColor.GRAY).append(Component.text("Lure", BeanColor.ENCHANT).append(Component.text(".", NamedTextColor.GRAY))))).decoration(TextDecoration.ITALIC, false));
	}
	
	@Override
	public void onFish(PlayerFishEvent e) {
		if (e.getState() == State.FISHING) {
			e.getHook().setApplyLure(false);
			e.getHook().setMinWaitTime(3);
			e.getHook().setMaxWaitTime(5);
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
