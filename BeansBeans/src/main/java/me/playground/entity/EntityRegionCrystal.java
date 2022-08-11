package me.playground.entity;

import me.playground.data.CustomPersistentDataType;
import me.playground.gui.BeanGuiRegionMain;
import me.playground.main.Main;
import me.playground.regions.Region;
import me.playground.utils.Utils;
import net.minecraft.core.Rotations;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

public class EntityRegionCrystal extends ArmorStand implements IBeanEntity {

	private static final ItemStack crystalIcon = Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTUwNjQ5NjI2YzQxMDEzNTJjNTk5NWM1M2I0OGJmZjYwYTkzODIxMmI3Y2U5MDI0MTVmZWI3NmVhMjczYjM1ZiJ9fX0=");
	private static final NamespacedKey KEY_REAL_LOCATION = new NamespacedKey(Main.getInstance(), "KEY_REAL_LOCATION");
	private static final NamespacedKey KEY_REGION_ID = new NamespacedKey(Main.getInstance(), "KEY_REGION_ID");

	private int regionId;
	private Region region;
	private Vector realPos;

	private int ascTicks = 0;
	private boolean asc = true;

	protected EntityRegionCrystal(Location location) {
		super(EntityType.ARMOR_STAND, ((CraftWorld)location.getWorld()).getHandle());
		setPos(location.getBlockX() + 0.5, location.getBlockY() + 0.2, location.getBlockZ() + 0.5);
		this.realPos = new Vector(location.getBlockX() + 0.5, location.getBlockY() + 0.2, location.getBlockZ() + 0.5);

		setInvulnerable(true);
	}

	@Override
	public void postCreation() {
		org.bukkit.entity.ArmorStand stand = (org.bukkit.entity.ArmorStand) this.getBukkitEntity();
		PersistentDataContainer container = stand.getPersistentDataContainer();
		stand.setHeadPose(new EulerAngle(0, 0, 0));
		stand.setItem(EquipmentSlot.HEAD, crystalIcon);
		stand.setInvisible(true);
		stand.setBasePlate(false);
		stand.setGravity(false);
		stand.setSmall(true);

		if (getRegion() != null) {
			stand.customName(getRegion().getColouredName());
			stand.setCustomNameVisible(true);
			getRegion().addCrystal(this);
		}

		moveTo(realPos.getX(), realPos.getY(), realPos.getZ());
		container.set(KEY_REGION_ID, PersistentDataType.INTEGER, regionId);
		container.set(KEY_REAL_LOCATION, CustomPersistentDataType.VECTOR, realPos);
	}

	@Override
	public void transferData(Entity oldEntity) {
		PersistentDataContainer oldContainer = oldEntity.getPersistentDataContainer();

		regionId = oldContainer.getOrDefault(KEY_REGION_ID, PersistentDataType.INTEGER, 0);
		realPos = oldContainer.getOrDefault(KEY_REAL_LOCATION, CustomPersistentDataType.VECTOR, new Vector(realPos.getX(), realPos.getY(), realPos.getZ()));
	}

	@Override
	public void tick() {
		double yChange = Math.sin((ascTicks / 40F) * 3.14);
		yChange *= 0.033;

		moveTo(getX(), getY() + yChange, getZ());
		setHeadPose(new Rotations(0, getHeadPose().getY() + 7F, 0));

		if (ascTicks % 5 == 0) {
			org.bukkit.entity.ArmorStand stand = (org.bukkit.entity.ArmorStand) this.getBukkitEntity();
			stand.getLocation().getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, stand.getEyeLocation(), 2, 1, 1, 1);
			stand.getLocation().getWorld().spawnParticle(Particle.FIREWORKS_SPARK, stand.getEyeLocation(), 1, 0.15, 0.1, 0.15, 0.02);
			stand.getLocation().getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, stand.getEyeLocation().add(0, 0.3, 0), 2, 0.2, 0.2, 0.2, -0.2, new Particle.DustTransition(Color.BLUE, Color.AQUA, 0.8F));
			float f = this.random.nextFloat() * 0.4F + this.random.nextFloat() > 0.9F ? 0.6F : 0.0F;
			this.playSound(SoundEvents.SOUL_ESCAPE, f, 0.6F + this.random.nextFloat() * 0.4F);
		}

		ascTicks++;
		if (ascTicks >= 80) {
			ascTicks = 0;
			asc = !asc;
		}
	}

	@Override
	public InteractionResult interactAt(Player entityhuman, Vec3 vec3d, InteractionHand enumhand) {
		org.bukkit.entity.Player player = (org.bukkit.entity.Player) entityhuman.getBukkitEntity();
		new BeanGuiRegionMain(player).openInventory();

		return InteractionResult.SUCCESS;
	}

	@Nullable
	public Region getRegion() {
		if (region == null)
			region = Main.getRegionManager().getRegion(regionId);
		return region;
	}

	public void setRegion(Region region) {
		if (getRegion() != null)
			getRegion().removeCrystal(this);

		this.regionId = region == null ? 0 : region.getRegionId();

		if (getRegion() != null) {
			getRegion().addCrystal(this);

			org.bukkit.entity.ArmorStand stand = (org.bukkit.entity.ArmorStand) this.getBukkitEntity();
			stand.customName(getRegion().getColouredName());
			stand.setCustomNameVisible(true);

			stand.getPersistentDataContainer().set(KEY_REGION_ID, PersistentDataType.INTEGER, regionId);
		}
	}

	@Override
	public void remove(RemovalReason entity_removalreason) {
		super.remove(entity_removalreason);
		if (getRegion() != null)
			getRegion().removeCrystal(this);
	}

}
