package me.playground.npc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.network.protocol.game.*;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import me.playground.main.IPluginRef;
import me.playground.main.Main;
import me.playground.menushop.MenuShop;
import me.playground.npc.interactions.NPCInteractShop;
import me.playground.npc.interactions.NPCInteraction;
import me.playground.playerprofile.PlayerProfile;
import me.playground.utils.BeanColor;
import me.playground.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.network.protocol.Packet;

public abstract class NPC<T extends LivingEntity> implements IPluginRef {
	final private int npcId;
	private Location location;
	private Location baseLocation;
	
	protected int creatorId; // Can be set to 0.
	protected NPCInteraction interaction;
	
	protected MenuShop shop;
	
	private boolean enabled = true;
	
	final protected Main plugin;
	final protected T entity;

//	protected EntityNPCHologram hologram;

	private final List<Player> nearbyPlayers = new ArrayList<>();
	
	public NPC(Main plugin, T entity, Location location) {
		this(0, -1, plugin, entity, location, null);
	}
	
	public NPC(int creatorId, int npcId, Main plugin, T entity, Location location, JSONObject json) {
		this.creatorId = creatorId;
		this.npcId = npcId;
		this.plugin = plugin;
		this.entity = entity;
		this.location = location;
		this.baseLocation = location;
		entity.moveTo(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

		// Assign json stuff
		if (json != null) {
			interaction = NPCInteraction.getByName(json.optString("interaction", "base"));
			if (interaction instanceof NPCInteractShop)
				shop = plugin.menuShopManager().getOrMakeShop(json.optString("shop"));
		} else {
			interaction = NPCInteraction.getByName("base");
		}
		
		interaction.onInit(this);
		onTick();
	}
	
	public abstract void showTo(Player p);
	protected abstract NPCType getType();
	
	/**
	 * Obtained to save exclusive data depending on Entity, to the database. Used to load the entities.
	 * @return Exclusive Json data based on the Entity.
	 */
	public JSONObject getJsonData() {
		JSONObject obj = new JSONObject();
		if (shop != null) obj.put("shop", shop.getIdentifier());

		obj.put("interaction", interaction.getName());
		return obj;
	}
	
	protected void hideFrom(@NotNull Player p) {
		ServerGamePacketListenerImpl connection = ((CraftPlayer)p).getHandle().connection;
		connection.send(new ClientboundRemoveEntitiesPacket(getEntityId()));
	}
	
	protected void hideFromAll() {
		Bukkit.getOnlinePlayers().forEach(this::hideFrom);
//		removeHologram();
	}
	
	public void spawnParticle(Particle particle, double xOffset, double yOffset, double zOffset, int particles) {
		getLocation().getWorld().spawnParticle(particle, getLocation().getX(), getLocation().getY()+entity.getBukkitEntity().getHeight(), getLocation().getZ(), particles, xOffset, yOffset, zOffset);
	}
	
	public NPC<T> teleport(Location loc, boolean dirty) {
		location = loc;
		baseLocation = loc;
		entity.moveTo(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
//		hologram.moveTo(loc.getX(), loc.getY() + 0.5, loc.getZ());
		sendPacketToAll(makeTeleportPacket(loc));
		sendPacketToAll(new ClientboundRotateHeadPacket(entity, getFixedRot(loc.getYaw())));
		if (dirty)
			setDirty();
		return this;
	}
	
	protected ClientboundTeleportEntityPacket makeTeleportPacket(Location loc) {
		ClientboundTeleportEntityPacket packet = new ClientboundTeleportEntityPacket(entity);
		Utils.setPacketValue(packet, "a", getEntityId());
		Utils.setPacketValue(packet, "b", loc.getX());
		Utils.setPacketValue(packet, "c", loc.getY());
		Utils.setPacketValue(packet, "d", loc.getZ());
		Utils.setPacketValue(packet, "e", getFixedRot(loc.getYaw()));
		Utils.setPacketValue(packet, "f", getFixedRot(loc.getPitch()));
		return packet;
	}
	
	private boolean isLookingAtTarget;
	
	protected void lookAtTarget(Entity entity) {
		if (entity == null) {
			//location.setDirection(baseLocation.subtract(location).toVector());
			isLookingAtTarget = false;
		} else {
			location.setDirection(entity.getLocation().subtract(location).toVector());
			isLookingAtTarget = true;
		}

		sendPacketToAll(new ClientboundMoveEntityPacket.Rot(getEntityId(), getFixedRot(location.getYaw()), getFixedRot(location.getPitch()), false));
		sendPacketToAll(new ClientboundRotateHeadPacket(this.entity, getFixedRot(location.getYaw())));
	}
	
	protected void sendPacketToAll(@SuppressWarnings("rawtypes") Packet pa) {
		for (Player p : nearbyPlayers) {
			ServerGamePacketListenerImpl connection = ((CraftPlayer)p).getHandle().connection;
			connection.send(pa);
		}
	}
	
	public T getEntity() {
		return entity;
	}
	
	public int getEntityId() {
		return entity.getBukkitEntity().getEntityId();
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
	 * Marks the NPC as {@link #dirty}.
	 */
	public void setDirty() {
		dirty = true;
	}
	
	public void setClean() {
		dirty = false;
	}
	
	public int getId() {
		return npcId;
	}
	
	public boolean isOwner(Player p) {
		return creatorId == PlayerProfile.getDBID(p);
	}
	
	public NPC<T> setInteraction(@NotNull NPCInteraction interaction) {
		this.interaction = interaction;
		this.interaction.onInit(this);
		setDirty();
		return this;
	}
	
	public NPCInteraction getInteraction() {
		return interaction;
	}
	
	public MenuShop getMenuShop() {
		return shop;
	}
	
	public void setMenuShop(String shop) {
		this.shop = plugin.menuShopManager().getExistingShop(shop);
		setDirty();
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
		return location.clone();
	}
	
	protected byte getFixedRot(float rot) {
		return (byte) ((int) (rot * 256.0F / 360.0F));
	}
	
	protected void refresh() {
		this.hideFromAll();
		this.onTick();
	}

	// TODO: NPCs need a hologram system

	/*public void removeHologram() {
		if (hologram != null)
			hologram.remove(net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
	}
	
	public void setHologramText(List<TextComponent> components) {
		if (hologram == null) hologram = CustomEntityType.NPC_HOLOGRAM.spawn(entity.getBukkitEntity().getLocation().add(0, 0.5, 0));
		hologram.setOwnerId(this.getId());
		hologram.setComponents(components);
	}

	public void setHologram(EntityNPCHologram hologram) {
		this.hologram = hologram;
	}*/
	
	@Override
	public @NotNull Main getPlugin() {
		return plugin;
	}
	
	public Component getName() {
		return this.getEntity().getBukkitEntity().name();
	}

	public String getDisplayName() {
		return this.getEntity().getScoreboardName();
	}

	private int nearbyCheck = 8;

	/**
	 * Fires every 3 ticks for every {@link NPC} who's chunk is loaded.
	 */
	public void onTick() {
		if (!isEnabled() || !entity.isChunkLoaded()) return;

		// Check every 30 ticks, onTick fires every 3 ticks.
		if (--nearbyCheck == 0) {
			nearbyCheck = 10;
			// Players within 64 blocks
			List<Player> playersInRange = new ArrayList<>(getLocation().getNearbyPlayers(64));
			int currentSize = nearbyPlayers.size();

			// Hide from, now, no longer in range players
			for (int x = currentSize; --x > -1;) {
				Player p = nearbyPlayers.get(x);
				if (p != null) {
					if (playersInRange.contains(p)) {
						playersInRange.remove(p);
						continue;
					}
					this.hideFrom(p);
				}
				nearbyPlayers.remove(x);
			}

			// Show to new nearby players
			currentSize = playersInRange.size();
			for (int x = -1; ++x < currentSize;) {
				this.showTo(playersInRange.get(x));
				nearbyPlayers.add(playersInRange.get(x));
			}
		}

		Iterator<Player> ents = getLocation().getNearbyPlayers(4).iterator();
		if (!ents.hasNext()) {
			if (isLookingAtTarget)
				this.lookAtTarget(null);
			return;
		}
		this.lookAtTarget(ents.next());
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enable) {
		this.enabled = enable;
	}
	
	/**
	 * Fires when a player right-clicks this entity.
	 */
	public void onInteract(Player player) {
		if (enabled)
			interaction.onInteract(this, player);
	}
	
	public void sendMessage(Player player, Component message) {
		sendMessage(player, message, Sound.ENTITY_VILLAGER_YES);
	}
	
	public void sendMessage(Player player, Component message, Sound sound) {
		player.sendMessage(Component.text("[NPC] ").append(getName()).color(BeanColor.NPC).append(Component.text("\u00a73 »")).append(message).color(NamedTextColor.WHITE));
		if (sound != null)
			player.playSound(player.getLocation(), sound, 0.55F, 1.4F);
	}
	
}
