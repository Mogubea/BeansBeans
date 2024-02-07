package me.playground.npc;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.mysql.cj.xdevapi.Client;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R3.CraftServer;
import org.bukkit.craftbukkit.v1_20_R3.CraftWorld;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import com.mojang.authlib.GameProfile;

import me.playground.main.IPluginRef;
import me.playground.main.Main;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.world.entity.Entity.RemovalReason;

public class NPCManager implements IPluginRef {
	private final NPCDatasource datasource;
	
	private final Main plugin;
	
	private final Map<Integer, NPC<?>> npcsByEntityId = new HashMap<>(); // contains all NPC's
	private final Map<Integer, NPC<?>> npcsByDBID = new HashMap<>(); // Only contains NPC's that have a Database Entry.
	
	public NPCManager(Main plugin) {
		this.plugin = plugin;
		this.datasource = new NPCDatasource(plugin, this);
		datasource.loadAll();
		
		Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> npcsByEntityId.values().forEach(NPC::onTick), 3L, 3L);
	}

	public NPC<?> getNPC(int id) {
		return npcsByDBID.get(id);
	}
	
	public NPC<?> getEntityNPC(int id) {
		return npcsByEntityId.get(id);
	}
	
	public Collection<NPC<?>> getAllNPCs() {
		return this.npcsByEntityId.values();
	}
	
	public Collection<NPC<?>> getDatabaseNPCs() {
		return this.npcsByDBID.values();
	}

	@Override
	public @NotNull Main getPlugin() {
		return plugin;
	}
	
	public void reload() {
		datasource.saveAll();
		this.npcsByDBID.forEach((id, npc) -> { npc.hideFromAll(); npc.getEntity().remove(RemovalReason.DISCARDED); });
		this.npcsByEntityId.clear();
		this.npcsByDBID.clear();
		datasource.loadAll();
	}
	
	/**
	 * Create and generate a permanent NPC with a database entry.
	 */
	public NPC<?> createNPC(int creatorId, Location location, NPCType type, String name) {
		int dbid = datasource.createNewNPC(creatorId, name, location);
		return createNPC(creatorId, location, type, name, dbid, null);
	}
	
	/**
	 * Load an NPC
	 */
	public NPC<?> loadNPC(int creatorId, Location location, NPCType type, String name, int id, JSONObject json) {
		return createNPC(creatorId, location, type, name, id, json);
	}
	
	private NPC<?> createNPC(int creatorId, Location location, NPCType type, String name, int id, JSONObject json) {
		NPC<?> ack;
		final DedicatedServer server = ((CraftServer)Bukkit.getServer()).getServer();
		final ServerLevel world = ((CraftWorld)location.getWorld()).getHandle();
		LivingEntity entityNpc;
		
		switch(type) {
		case HUMAN:
			UUID uuid = UUID.randomUUID();
			if (json != null && json.has("uuid"))
				uuid = UUID.fromString(json.getString("uuid"));
			
			GameProfile profile = new GameProfile(uuid, name);
			entityNpc = new ServerPlayer(server, world, profile, ClientInformation.createDefault());
			((ServerPlayer) entityNpc).spawnIn(world);
			ack = new NPCHuman(creatorId, id, getPlugin(), ((ServerPlayer) entityNpc), location, json);
			break;
		default:
			throw new UnsupportedOperationException("Invalid NPCType provided, cannot create NPC.");
		}
		return registerNPC(ack);
	}
	
	private NPC<?> registerNPC(NPC<?> npc) {
		npcsByDBID.put(npc.getId(), npc);
		npcsByEntityId.put(npc.getEntityId(), npc);
		return npc;
	}
	
}
