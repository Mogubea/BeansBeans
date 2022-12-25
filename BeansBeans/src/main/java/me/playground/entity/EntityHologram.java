package me.playground.entity;

import me.playground.data.CustomPersistentDataType;
import me.playground.gui.BeanGuiHologram;
import me.playground.items.lore.Lore;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.ranks.Rank;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_19_R2.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EntityHologram extends ArmorStand implements IBeanEntity {

	private static final NamespacedKey KEY_REAL_LOCATION = new NamespacedKey(Main.getInstance(), "KEY_REAL_LOCATION"); // Vector
	private static final NamespacedKey KEY_COMPONENTS = new NamespacedKey(Main.getInstance(), "KEY_COMPONENTS"); // String
	private static final NamespacedKey KEY_CREATOR_ID = new NamespacedKey(Main.getInstance(), "KEY_CREATOR_ID"); // Integer
	private static final NamespacedKey KEY_COMPONENT_SPACE = new NamespacedKey(Main.getInstance(), "KEY_COMPONENT_SPACE"); // Float
	private static final NamespacedKey KEY_OVERRIDE_POWER = new NamespacedKey(Main.getInstance(), "KEY_OVERRIDE_POWER"); // Integer

	private final List<EntityHologramLine> lines = new ArrayList<>(); // Hologram lines.

	private int creatorId; // Creator of this hologram.
	private Vector realPos; // The real position of this hologram.
	private List<TextComponent> components = new ArrayList<>();
	private float spaceBetweenComponents; // Space between each line of text.
	private short overridePower;

	protected EntityHologram(Location location) {
		super(EntityType.ARMOR_STAND, ((CraftWorld)location.getWorld()).getHandle());

		// Round the Location to 1 decimal space, down Y by 1
		Location newLoc = new Location(location.getWorld(), Math.round(location.getX() * 10D) / 10D, (Math.round(location.getY() * 10D) / 10D), Math.round(location.getZ() * 10D) / 10D);
		this.realPos = new Vector(newLoc.getX(), newLoc.getY(), newLoc.getZ());

		setInvisible(true);
		setInvulnerable(true);
		setNoBasePlate(true);
		setNoGravity(true);
	}

	protected List<EntityHologramLine> getHologramLines() {
		return lines;
	}

	public void setComponents(@Nullable List<TextComponent> components) {
		List<EntityHologramLine> concurrentSafe = new ArrayList<>(lines);
		concurrentSafe.forEach(entity -> entity.remove(RemovalReason.DISCARDED));

		if (components == null || components.isEmpty())
			components = new ArrayList<>(Lore.getBuilder("I am a hologram!").colorIfAbsent(NamedTextColor.WHITE).build().getLore());

		components.forEach(component -> {
			EntityHologramLine hologram = new EntityHologramLine(this, component);
			lines.add(hologram);
		});

		org.bukkit.entity.ArmorStand stand = (org.bukkit.entity.ArmorStand) this.getBukkitEntity();
		PersistentDataContainer container = stand.getPersistentDataContainer();

		container.set(KEY_COMPONENTS, CustomPersistentDataType.TEXT_COMPONENT_ARRAY, components.toArray(new TextComponent[0]));
		setSpaceBetweenComponents(spaceBetweenComponents);
		this.components = components;
	}

	public void setSpaceBetweenComponents(float distance) {
		spaceBetweenComponents = distance;
		org.bukkit.entity.ArmorStand stand = (org.bukkit.entity.ArmorStand) this.getBukkitEntity();
		PersistentDataContainer container = stand.getPersistentDataContainer();
		container.set(KEY_COMPONENT_SPACE, PersistentDataType.FLOAT, distance);

		int total = lines.size();
		for (int x = -1; ++x < total;) {
			EntityHologramLine line = lines.get(x);
			line.setPos(realPos.getX(), realPos.getY() + ((total-1) * (distance + 0.3)) - ((distance + 0.3) * x) + 1, realPos.getZ());
		}
	}

	public List<TextComponent> getComponents() {
		return List.copyOf(components);
	}

	public void addComponent(TextComponent component) {
		components.add(component);
		setComponents(components);
	}

	public void removeComponent(int line) {
		components.remove(line);
		setComponents(components);
	}

	public void setComponent(int line, TextComponent component) {
		components.set(line, component);
		setComponents(components);
	}

	public int getSize() {
		return lines.size();
	}

	public int getCreatorId() {
		return creatorId;
	}

	public void setOwnerId(int id) {
		this.creatorId = id;
	}

	/**
	 * Get the group level required in order to modify this hologram regardless of ownership.
	 */
	public int getOverridePower() {
		return overridePower;
	}

	public void setOverridePower(int power) {
		this.overridePower = (short) power;
	}

	@Override
	public void postCreation() {
		org.bukkit.entity.ArmorStand stand = (org.bukkit.entity.ArmorStand) this.getBukkitEntity();
		PersistentDataContainer container = stand.getPersistentDataContainer();
		stand.setPersistent(true);
		stand.setCollidable(false);
		stand.setMarker(true);
		stand.setCanTick(false); // TODO: add additional features that require ticking

		moveTo(realPos.getX(), realPos.getY(), realPos.getZ());
		container.set(KEY_CREATOR_ID, PersistentDataType.INTEGER, creatorId);
		container.set(KEY_REAL_LOCATION, CustomPersistentDataType.VECTOR, realPos);

		if (overridePower != Rank.MODERATOR.power())
			container.set(KEY_OVERRIDE_POWER, PersistentDataType.SHORT, overridePower);

		setComponents(components);
	}

	@Override
	public void transferData(Entity oldEntity) {
		PersistentDataContainer oldContainer = oldEntity.getPersistentDataContainer();

		creatorId = oldContainer.getOrDefault(KEY_CREATOR_ID, PersistentDataType.INTEGER, 0);
		realPos = oldContainer.getOrDefault(KEY_REAL_LOCATION, CustomPersistentDataType.VECTOR, new Vector(realPos.getX(), realPos.getY(), realPos.getZ()));
		components = new ArrayList<>(Arrays.asList(oldContainer.getOrDefault(KEY_COMPONENTS, CustomPersistentDataType.TEXT_COMPONENT_ARRAY, new TextComponent[] {Component.text("I am a hologram!")})));
		spaceBetweenComponents = oldContainer.getOrDefault(KEY_COMPONENT_SPACE, PersistentDataType.FLOAT, 0f);
		overridePower = oldContainer.getOrDefault(KEY_OVERRIDE_POWER, PersistentDataType.SHORT, (short) Rank.MODERATOR.power());
	}

	@Override
	public void tick() {

	}

	@Override
	public InteractionResult interactAt(Player entityhuman, Vec3 vec3d, InteractionHand enumhand) {
		org.bukkit.entity.Player player = (org.bukkit.entity.Player) entityhuman.getBukkitEntity();
		PlayerProfile pp = PlayerProfile.from(player);

		if (pp.getId() != getCreatorId() && pp.getHighestRank().power() < getOverridePower())
			return InteractionResult.FAIL;

		new BeanGuiHologram(player, this).openInventory();
		return InteractionResult.SUCCESS;
	}

	@Override
	public PushReaction getPistonPushReaction() {
		return PushReaction.IGNORE;
	}

	@Override
	public void moveTo(double d0, double d1, double d2, float yRot, float xRot) {
		super.moveTo(d0, d1, d2, this.getYRot(), this.getXRot());

		realPos = new Vector(d0, d1, d2);

		org.bukkit.entity.ArmorStand stand = (org.bukkit.entity.ArmorStand) this.getBukkitEntity();
		PersistentDataContainer container = stand.getPersistentDataContainer();
		container.set(KEY_REAL_LOCATION, CustomPersistentDataType.VECTOR, realPos);
		setComponents(components);
	}

	@Override
	public void remove(RemovalReason entity_removalreason) {
		int size = lines.size();
		for (int x = size; --x > -1;)
			lines.remove(x);
		super.remove(entity_removalreason);
	}

}
