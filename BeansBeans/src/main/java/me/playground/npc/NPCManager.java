package me.playground.npc;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.json.JSONObject;

import com.mojang.authlib.GameProfile;

import me.playground.data.Datasource;
import me.playground.main.IPluginRef;
import me.playground.main.Main;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;

public class NPCManager implements IPluginRef {
	
	private final Main plugin;
	private boolean enabled = true;
	
	private final HashMap<Integer, NPC<?>> npcsByEntityId; // contains all NPC's
	private final HashMap<Integer, NPC<?>> npcsByDBID; // Only contains NPC's that have a Database Entry.
	
	public NPCManager(Main plugin) {
		this.plugin = plugin;
		npcsByEntityId = new HashMap<Integer, NPC<?>>();
		npcsByDBID = new HashMap<Integer, NPC<?>>();
	}
	
	public void showAllNPCs(Player p) {
		npcsByEntityId.values().forEach((npc) -> {
			npc.showTo(p);
		});
	}
	
	public void showAllNPCsToAll() {
		npcsByEntityId.values().forEach((npc) -> {
			npc.showToAll();
		});
	}
	
	public void hideAllNPCs(Player p) {
		npcsByEntityId.values().forEach((npc) -> {
			npc.hideFrom(p);
		});
	}
	
	public void hideAllNPCsFromAll() {
		npcsByEntityId.values().forEach((npc) -> {
			npc.hideFromAll();
		});
	}
	
	public NPC<?> getDatabaseNPC(int id) {
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
	
	public void setEnabled(boolean enable) {
		this.enabled = enable;
		if (enable)
			showAllNPCsToAll();
		else 
			hideAllNPCsFromAll();
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	@Override
	public Main getPlugin() {
		return plugin;
	}
	
	public void reload() {
		this.hideAllNPCsFromAll();
		Datasource.saveDirtyNPCs();
		this.npcsByEntityId.clear();
		this.npcsByDBID.clear();
		Datasource.loadAllNPCs();
		//this.showAllNPCsToAll();
	}
	
	/**
	 * Create a TEMPORARY NPC that removes itself upon restart.
	 */
	public NPC<?> createTempNPC(int creatorId, Location location, NPCType type, String name) {
		return createNPC(creatorId, location, type, name, -1, null);
	}
	
	/**
	 * Create and generate a permanent NPC with a database entry.
	 */
	public NPC<?> createNPC(int creatorId, Location location, NPCType type, String name) {
		int dbid = Datasource.saveNewNPC(creatorId, name, location);
		return createNPC(creatorId, location, type, name, dbid, null);
	}
	
	/**
	 * Load an NPC
	 */
	public NPC<?> loadNPC(int creatorId, Location location, NPCType type, String name, int id, JSONObject json) {
		return createNPC(creatorId, location, type, name, id, json);
	}
	
	private NPC<?> createNPC(int creatorId, Location location, NPCType type, String name, int id, JSONObject json) {
		NPC<?> ack = null;
		final DedicatedServer server = ((CraftServer)Bukkit.getServer()).getServer();
		final WorldServer world = ((CraftWorld)location.getWorld()).getHandle();
		
		switch(type) {
		case HUMAN:
			UUID uuid = UUID.randomUUID();
			if (json != null && json.has("uuid"))
				uuid = UUID.fromString(json.getString("uuid"));
			
			GameProfile profile = new GameProfile(uuid, name);
			EntityPlayer humanNpc = new EntityPlayer(server, world, profile);
			humanNpc.spawnIn(world);
			ack = new NPCHuman(creatorId, id, getPlugin(), humanNpc, location, json);
			break;
		default:
			throw new RuntimeException("Invalid NPCType provided, cannot create NPC.");
		}
		ack.showToAll();
		return registerNPC(ack);
	}
	
	private NPC<?> registerNPC(NPC<?> npc) {
		if (npc.isDatabaseNPC())
			npcsByDBID.put(npc.getDatabaseId(), npc);
		npcsByEntityId.put(npc.getEntityId(), npc);
		return npc;
	}
	
}
