package me.playground.entity;

import net.kyori.adventure.text.TextComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.Vec3;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.CreatureSpawnEvent;

/**
 * An entity only referenced and utilised by {@link EntityHologram}.
 * This entity isn't to be spawned by any other method and isn't included in the {@link CustomEntityType} class.
 */
public class EntityHologramLine extends ArmorStand implements IBeanEntity {

	private final EntityHologram owner;
	private final TextComponent line;

	protected EntityHologramLine(EntityHologram owner, TextComponent component) {
		super(EntityType.ARMOR_STAND, owner.level());
		this.line = component;
		this.owner = owner;
		this.getBukkitEntity().customName(component);
		this.getBukkitEntity().setCustomNameVisible(true);
		postCreation();
		level().addFreshEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM);
	}

	@Override
	public void postCreation() {
		org.bukkit.entity.ArmorStand stand = (org.bukkit.entity.ArmorStand) this.getBukkitEntity();
		stand.setInvisible(true);
		stand.setInvulnerable(true);
		stand.setBasePlate(false);
		stand.setGravity(false);
		stand.setCollidable(false);
		stand.setMarker(true);
		stand.setSmall(true);
		stand.setCanTick(false);
		stand.setPersistent(false); // No reason to save this on unload as the loading of EntityHologram creates new EntityHologramLines
	}

	public TextComponent getComponent() {
		return line;
	}

	public EntityHologram getHologramBase() {
		return owner;
	}

	@Override
	public void transferData(Entity oldEntity) {

	}

	@Override
	public void tick() {

	}

	@Override
	public InteractionResult interactAt(Player entityhuman, Vec3 vec3d, InteractionHand enumhand) {
		return owner.interactAt(entityhuman, vec3d, enumhand);
	}

	@Override
	public PushReaction getPistonPushReaction() {
		return PushReaction.IGNORE;
	}

	@Override
	public void remove(RemovalReason entity_removalreason) {
		owner.getHologramLines().remove(this);
		super.remove(entity_removalreason);
	}

}
