package me.playground.npc;

import java.util.Collection;
import java.util.Iterator;

import net.minecraft.network.protocol.game.*;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import me.playground.civilizations.Civilization;
import me.playground.civilizations.jobs.Job;
import me.playground.main.IPluginRef;
import me.playground.main.Main;
import me.playground.menushop.MenuShop;
import me.playground.npc.interactions.NPCInteractShop;
import me.playground.npc.interactions.NPCInteraction;
import me.playground.playerprofile.PlayerProfile;
import me.playground.utils.BeanColor;
import me.playground.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.minecraft.network.protocol.Packet;

public abstract class NPC<T extends LivingEntity> implements IPluginRef {
	final private int npcId;
	private Location location;
	private Location baseLocation;
	
	protected int creatorId; // Can be set to 0.
	protected Civilization civilization; // If the NPC belongs to a Civilization and not neccessarily an owning player.
	protected Job job = Job.getByName("gatherer"); // If the NPC has a Job, for Interactions.
	protected NPCInteraction interaction = NPCInteraction.getByName("base");
	protected int titleCol = BeanColor.NPC.value();
	
	protected MenuShop shop;
	
	private boolean enabled = true;
	
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
		this.baseLocation = location;
		entity.moveTo(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

		// Remove previous stands if they are there.
		getLocation().getChunk().load();
		Collection<ArmorStand> stands = getLocation().add(0, entity.getBukkitEntity().getHeight() + 0.54, 0).getNearbyEntitiesByType(ArmorStand.class, 0.1D);
		for (ArmorStand s : stands)
			if (s.isMarker())
				s.remove();
		
		// Assign json stuff
		if (json != null) {
			titleCol = json.optNumber("titleCol", BeanColor.NPC.value()).intValue();
			String title = json.optString("title");
			if (!title.isEmpty())
				setTitle(Component.text(title), false);
			job = Job.getByName(json.optString("job", "gatherer"));
			interaction = NPCInteraction.getByName(json.optString("interaction", "base"));
			String strCiv = json.optString("civilization");
			if (!strCiv.isEmpty())
				civilization = Civilization.getByName(strCiv);
			if (interaction instanceof NPCInteractShop)
				shop = plugin.menuShopManager().getOrMakeShop(json.optString("shop"));
		}
		
		interaction.onInit(this);
	}
	
	public abstract void showTo(Player p);
	protected abstract NPCType getType();
	
	/**
	 * Obtained to save exclusive data depending on Entity, to the database. Used to load the entities.
	 * @return Exclusive Json data based on the Entity.
	 */
	public JSONObject getJsonData() {
		JSONObject obj = new JSONObject();
		if (titleEntity != null) obj.put("title", ((TextComponent)titleEntity.customName()).content());
		if (titleCol != BeanColor.NPC.value()) obj.put("titleCol", titleCol);
		if (civilization != null) obj.put("civilization", civilization.getName());
		if (shop != null) obj.put("shop", shop.getIdentifier());
		
		obj.put("job", job.getName());
		obj.put("interaction", interaction.getName());
		return obj;
	}
	
	protected void showToAll() {
		Bukkit.getOnlinePlayers().forEach(this::showTo);
	}
	
	protected void hideFrom(Player p) {
		ServerGamePacketListenerImpl connection = ((CraftPlayer)p).getHandle().connection;
		connection.send(new ClientboundRemoveEntitiesPacket(getEntityId()));
	}
	
	protected void hideFromAll() {
		Bukkit.getOnlinePlayers().forEach(this::hideFrom);
		removeTitle(false);
	}
	
	public void spawnParticle(Particle particle, double xOffset, double yOffset, double zOffset, int particles) {
		getLocation().getWorld().spawnParticle(particle, getLocation().getX(), getLocation().getY()+entity.getBukkitEntity().getHeight(), getLocation().getZ(), particles, xOffset, yOffset, zOffset);
	}
	
	public NPC<T> teleport(Location loc, boolean dirty) {
		location = loc;
		baseLocation = loc;
		entity.moveTo(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
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
		for (Player p : Bukkit.getOnlinePlayers()) {
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
	 * Marks the NPC as {@link #dirty}, assuming they have an entry in the database. 
	 */
	public void setDirty() {
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
	
	public boolean isOwner(Player p) {
		return creatorId == PlayerProfile.getDBID(p);
	}
	
	public boolean hasCivilization() {
		return civilization != null;
	}
	
	public Civilization getCivilization() {
		return civilization;
	}
	
	public NPC<T> setInteraction(NPCInteraction interaction) {
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
	
	public NPC<T> setJob(Job job) {
		this.job = job;
		setDirty();
		return this;
	}
	
	public Job getJob() {
		return job;
	}
	
	public Location getLocation() {
		return location.clone();
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
		Location loc = getLocation().add(0, entity.getBukkitEntity().getHeight() + 0.54, 0);
		titleEntity = (ArmorStand) location.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
		titleEntity.setInvisible(true);
		titleEntity.setInvulnerable(true);
		titleEntity.setCanTick(false);
		titleEntity.setMarker(true);
		titleEntity.setCollidable(false);
		titleEntity.customName(title.color(TextColor.color(titleCol)));
		titleEntity.setCustomNameVisible(true);
		if (dirty) setDirty();
	}
	
	public void setTitleColour(int col) {
		titleCol = col;
		setDirty();
		if (getTitle() != Component.empty())
			setTitle(getTitle(), true);
	}
	
	public int getTitleColour() {
		return titleCol;
	}
	
	public Component getTitle() {
		if (titleEntity == null) return Component.empty();
		return titleEntity.customName();
	}
	
	protected ArmorStand getTitleStand() {
		return this.titleEntity;
	}
	
	@Override
	public @NotNull Main getPlugin() {
		return plugin;
	}
	
	public Component getName() {
		return this.getEntity().getBukkitEntity().name();
	}
	
	public void onTick() {
		// XXX: Testing
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
