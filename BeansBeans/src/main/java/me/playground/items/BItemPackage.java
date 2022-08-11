package me.playground.items;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;

import me.playground.loot.LootRetriever;
import me.playground.loot.LootTable;
import me.playground.loot.RetrieveMethod;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.playerprofile.stats.StatType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class BItemPackage extends BeanItem {
	
	private final String table;
	private final int loops;
	private final Material visuals;
	
	public BItemPackage(int numeric, String identifier, String name, ItemRarity rarity, String lootTable, int loops, Material mat, String head) {
		super(numeric, identifier, name, head, rarity);
		this.loops = loops;
		this.table = lootTable;
		this.visuals = mat;
		setDefaultLore(Component.text("Right click to open!", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
	}
	
	@Override
	public void onInteract(PlayerInteractEvent e) {
		if (e.getHand() == EquipmentSlot.HAND && (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			e.setCancelled(true);
			
			if (getLootTable() == null) return;
			
			Player p = e.getPlayer();
			PlayerProfile pp = PlayerProfile.from(p);
			
			if (e.getAction() == Action.RIGHT_CLICK_AIR) {
				try {
					PacketContainer arm = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ANIMATION);
		            arm.getEntityModifier(p.getWorld()).write(0, p);
		            ProtocolLibrary.getProtocolManager().sendServerPacket(p, arm);
				} catch (InvocationTargetException e1) {
					e1.printStackTrace();
				}
			}
			
			p.getWorld().playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_GOLD, 0.7F, 0.9F);
			p.getWorld().spawnParticle(Particle.BLOCK_CRACK, p.getLocation().add(0, 1.5, 0), 5, visuals.createBlockData());
			
			List<ItemStack> loot = LootRetriever.from(getLootTable(), RetrieveMethod.CUMULATIVE_CHANCE, p).loops(loops).luck(pp.getLuck()).getLoot();
			for (int x = -1; ++x < loops;) {
				p.getWorld().dropItem(p.getLocation(), loot.get(x), (item) -> {
					item.setOwner(p.getUniqueId());
				});
			}
			e.getItem().subtract();
			pp.getStats().addToStat(StatType.CRATES_OPENED, identifier, 1, true);
		}
	}
	
	public LootTable getLootTable() {
		return Main.getInstance().lootManager().getLootTable(table);
	}
}
