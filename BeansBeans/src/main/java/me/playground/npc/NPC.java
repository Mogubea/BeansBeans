package me.playground.npc;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.json.JSONObject;

import me.playground.main.IPluginRef;
import me.playground.main.Main;
import me.playground.utils.Utils;
import net.minecraft.network.protocol.Packet;
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
	
	public NPC(Main plugin, T entity, Location location) {
		this(-1, plugin, entity, location);
	}
	
	public NPC(int npcId, Main plugin, T entity, Location location) {
		this.npcId = npcId;
		this.plugin = plugin;
		this.entity = entity;
		this.location = location;
		entity.setPositionRotation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
	}
	
	protected abstract void showTo(Player p);
	protected abstract NPCType getType();
	/**
	 * Obtained to save exclusive data depending on Entity, to the database. Used to load the entities.
	 * @return Exclusive Json data based on the Entity.
	 */
	public abstract JSONObject getJsonData();
	
	protected void showToAll() {
		Bukkit.getOnlinePlayers().forEach((p) -> { showTo(p); });
	}
	
	protected void hideFrom(Player p) {
		PlayerConnection connection = ((CraftPlayer)p).getHandle().b;
		connection.sendPacket(new PacketPlayOutEntityDestroy(getEntityId()));
	}
	
	protected void hideFromAll() {
		Bukkit.getOnlinePlayers().forEach((p) -> { hideFrom(p); });
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
	
	@Override
	public Main getPlugin() {
		return plugin;
	}
	
}
