package me.playground.items;

import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.BlockState;
import org.bukkit.block.Hopper;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.util.EulerAngle;

import me.playground.data.CustomPersistentDataType;
import me.playground.gui.BeanGuiLivingHopper;

public class BItemLivingHopper extends BeanBlock {
	
	public static final NamespacedKey KEY_ITEMS = key("ITEMS");
	protected final ItemStack indicator;
	
	protected BItemLivingHopper(int numeric, String identifier, String name, ItemRarity rarity, int modelDataInt) {
		super(numeric, identifier, name, Material.HOPPER, rarity, 0);
		this.indicator = new ItemStack(Material.WHITE_STAINED_GLASS);
		this.indicator.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
	}
	
	@Override
	public void onBlockInteract(PlayerInteractEvent e) {
		if (!(e.getPlayer().isSneaking() && e.getItem() != null) && !e.getPlayer().isBlocking()) {
			e.setCancelled(true);
			new BeanGuiLivingHopper(e.getPlayer(), (Hopper) e.getClickedBlock().getState()).openInventory();
		}
	}
	
	@Override
	public void onBlockBreak(BlockBreakEvent e) {
		Collection<ArmorStand> stands = e.getBlock().getLocation().add(0.5, -0.2, 0.5).getNearbyEntitiesByType(ArmorStand.class, 0.1D);
		for (ArmorStand s : stands)
			if (s.isMarker())
				s.remove();
		
		// Additional block effect when breaking
		e.getBlock().getWorld().spawnParticle(Particle.BLOCK_DUST, e.getBlock().getLocation().add(0.5, 0.5, 0.5), 8, 0.25, 0.25, 0.25, Material.AMETHYST_CLUSTER.createBlockData());
	}
	
	@Override
	public void onBlockPlace(BlockPlaceEvent e) {
		BlockState bs = e.getBlock().getState();
		PersistentDataHolder holder = (PersistentDataHolder) bs;
		PersistentDataContainer pdc = holder.getPersistentDataContainer();
		pdc.set(KEY_ITEMS, CustomPersistentDataType.ITEMSTACK_ARRAYLIST, new ItemStack[7]);
		bs.update();
		
		// Additional sound effect when placing
		Location l = e.getBlock().getLocation().add(0.5, 0.5, 0.5);
		e.getPlayer().getWorld().playSound(l, Sound.BLOCK_AZALEA_LEAVES_PLACE, 0.08F, 1.3F);
		
		// Chest type indicator as a stand
		e.getBlock().getWorld().spawnEntity(l.subtract(0, 0.7, 0), EntityType.ARMOR_STAND, SpawnReason.CUSTOM, armorStand -> {
			ArmorStand stand = (ArmorStand) armorStand;
			stand.setSmall(true);
			stand.setVisible(false);
			stand.setInvulnerable(true);
			stand.setGravity(false);
			stand.setBasePlate(false);
			stand.setCanTick(false);
			stand.setCollidable(false);
			stand.setCanMove(false);
			stand.setMarker(true);
			stand.setHeadPose(new EulerAngle(0, 3.14 / 2, 0));
			stand.addDisabledSlots(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);
			stand.getEquipment().setHelmet(indicator);
		}); 
	}
	
	/**
	 * Get the raw array of {@link ItemStack}s from this chest which are currently contained in the {@link PersistentDataContainer}.
	 */
	public ItemStack[] getFilterContents(Hopper hopper) {
		PersistentDataContainer pdc = hopper.getPersistentDataContainer();
		ItemStack[] items = pdc.getOrDefault(KEY_ITEMS, CustomPersistentDataType.ITEMSTACK_ARRAYLIST, null);
		if (items == null) {
			pdc.set(KEY_ITEMS, CustomPersistentDataType.ITEMSTACK_ARRAYLIST, new ItemStack[7]);
			hopper.update();
		}
		
		return items;
	}
	
	/**
	 * Save this custom container's inventory to the {@link #KEY_ITEMS} key in the Chest's {@link PersistentDataContainer}.
	 */
	public void saveFilterInventory(Hopper hopper, ItemStack[] items) {
		hopper.getPersistentDataContainer().set(KEY_ITEMS, CustomPersistentDataType.ITEMSTACK_ARRAYLIST, items);
		hopper.update();
		BeanGuiLivingHopper.updateViewerInventory(hopper, items);
	}
	
}
