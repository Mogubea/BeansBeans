package me.playground.npc;

import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.ArmorStand.LockType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.json.JSONObject;

import me.playground.main.IPluginRef;
import me.playground.main.Main;
import me.playground.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutEntity;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.network.protocol.game.PacketPlayOutEntityHeadRotation;
import net.minecraft.network.protocol.game.PacketPlayOutEntityTeleport;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.entity.EntityLiving;

public abstract class NPC<T extends EntityLiving> implements IPluginRef {
	final private int npcId;
	private Location location;
	protected int creatorId;
	final protected Main plugin;
	final protected T entity;
	
	protected ArmorStand titleEntity;
	
	public NPC(Main plugin, T entity, Location location) {
		this(0, -1, plugin, entity, location, null);
	}
	
	public NPC(int creatorId, int npcId, Main plugin, T entity, Location location, JSONObject json) {
		this.creatorId = creatorId;
		this.npcId = npcId;
		this.plugin = plugin;
		this.entity = entity;
		this.location = location;
		entity.setPositionRotation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
		if (json != null) {
			String title = json.optString("title");
			if (title != null && !title.isEmpty())
				this.setTitle(Component.text(title), false);
		}
	}
	
	protected abstract void showTo(Player p);
	protected abstract NPCType getType();
	/**
	 * Obtained to save exclusive data depending on Entity, to the database. Used to load the entities.
	 * @return Exclusive Json data based on the Entity.
	 */
	public JSONObject getJsonData() {
		JSONObject obj = new JSONObject();
		if (titleEntity != null) obj.put("title", ((TextComponent)titleEntity.customName()).content());
		return obj;
	}
	
	protected void showToAll() {
		Bukkit.getOnlinePlayers().forEach((p) -> { showTo(p); });
	}
	
	protected void hideFrom(Player p) {
		PlayerConnection connection = ((CraftPlayer)p).getHandle().b;
		connection.sendPacket(new PacketPlayOutEntityDestroy(getEntityId()));
	}
	
	protected void hideFromAll() {
		Bukkit.getOnlinePlayers().forEach((p) -> { hideFrom(p); });
		removeTitle(false);
	}
	
	public NPC<T> teleport(Location loc, boolean dirty) {
		location = loc;
		entity.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
		sendPacketToAll(makeTeleportPacket(loc));
		sendPacketToAll(new PacketPlayOutEntityHeadRotation(entity, getFixedRot(loc.getYaw())));
		if (dirty)
			setDirty();
		return this;
	}
	
	protected PacketPlayOutEntityTeleport makeTeleportPacket(Location loc) {
		PacketPlayOutEntityTeleport packet = new PacketPlayOutEntityTeleport(entity);
		Utils.setPacketValue(packet, "a", getEntityId());
		Utils.setPacketValue(packet, "b", loc.getX());
		Utils.setPacketValue(packet, "c", loc.getY());
		Utils.setPacketValue(packet, "d", loc.getZ());
		Utils.setPacketValue(packet, "e", getFixedRot(loc.getYaw()));
		Utils.setPacketValue(packet, "f", getFixedRot(loc.getPitch()));
		return packet;
	}
	
	protected void lookAtTarget(Entity entity) {
		location.setDirection(entity.getLocation().subtract(location).toVector());
		sendPacketToAll(new PacketPlayOutEntity.PacketPlayOutEntityLook(getEntityId(), getFixedRot(location.getYaw()), getFixedRot(location.getPitch()), false));
		sendPacketToAll(new PacketPlayOutEntityHeadRotation(this.entity, getFixedRot(location.getYaw())));
	}
	
	protected void sendPacketToAll(@SuppressWarnings("rawtypes") Packet pa) {
		for (Player p : Bukkit.getOnlinePlayers()) {
			PlayerConnection connection = ((CraftPlayer)p).getHandle().b;
			connection.sendPacket(pa);
		}
	}
	
	public T getEntity() {
		return entity;
	}
	
	public int getEntityId() {
		return entity.getId();
	}
	
	public boolean isHuman() {
		return entity instanceof Player;
	}
	
	protected NPCManager getManager() {
		return getPlugin().npcManager();
	}
	
	/**
	 * If true, this NPC's database entry will be updated on the next save instance.
	 */
	private boolean dirty = false;
	
	/**
	 * @return {@link #dirty}
	 */
	public boolean isDirty() {
		return dirty;
	}
	
	/**
	 * Marks the NPC as {@link #dirty}, assuming they have an entry in the database. 
	 */
	protected void setDirty() {
		dirty = isDatabaseNPC();
	}
	
	public void setClean() {
		dirty = false;
	}
	
	public boolean isDatabaseNPC() {
		return npcId > 0;
	}
	
	public int getDatabaseId() {
		return npcId;
	}
	
	public int getCreatorId() {
		return creatorId;
	}
	
	public NPC<T> setCreator(int playerId) {
		this.creatorId = playerId;
		setDirty();
		return this;
	}
	
	public Location getLocation() {
		return location;
	}
	
	protected byte getFixedRot(float rot) {
		return (byte) ((int) (rot * 256.0F / 360.0F));
	}
	
	protected void refresh() {
		this.hideFromAll();
		this.showToAll();
	}
	
	public void removeTitle(boolean dirty) {
		if (titleEntity != null) 
			titleEntity.remove();
		if (dirty) setDirty();
	}
	
	public void setTitle(Component title, boolean dirty) {
		if (title == null) { removeTitle(dirty); return; }
		if (getTitleStand() != null) { titleEntity.remove(); }
		titleEntity = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
		titleEntity.setInvisible(true);
		titleEntity.setInvulnerable(true);
		titleEntity.setGravity(false);
		titleEntity.setBasePlate(false);
		titleEntity.addEquipmentLock(EquipmentSlot.HEAD, LockType.REMOVING_OR_CHANGING);
		titleEntity.addEquipmentLock(EquipmentSlot.CHEST, LockType.REMOVING_OR_CHANGING);
		titleEntity.addEquipmentLock(EquipmentSlot.LEGS, LockType.REMOVING_OR_CHANGING);
		titleEntity.addEquipmentLock(EquipmentSlot.FEET, LockType.REMOVING_OR_CHANGING);
		titleEntity.customName(title);
		titleEntity.setCustomNameVisible(true);
		if (dirty) setDirty();
	}
	
	protected ArmorStand getTitleStand() {
		return this.titleEntity;
	}
	
	@Override
	public Main getPlugin() {
		return plugin;
	}
	
	public Component getName() {
		return this.getEntity().getBukkitEntity().name();
	}
	
	public void onTick() {
		// XXX: Testing
		Iterator<Player> ents = entity.getBukkitEntity().getWorld().getNearbyPlayers(location, 4).iterator();
		if (!ents.hasNext()) return;
		this.lookAtTarget(ents.next());
	}
	
	/**
	 * Fires when a player right clicks this entity.
	 */
	public abstract void onInteract(Player player);
	
	public void sendMessage(Player player, Component message) {
		player.sendMessage(Component.text("\u00a7b[NPC] ").append(getName()).append(Component.text(" » \u00a7f")).append(message));
		player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 0.6F, 1.4F);
	}
	
}
