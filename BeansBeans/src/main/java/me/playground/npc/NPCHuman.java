package me.playground.npc;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.JSONObject;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import me.playground.main.Main;
import net.minecraft.network.protocol.game.PacketPlayOutEntityHeadRotation;
import net.minecraft.network.protocol.game.PacketPlayOutNamedEntitySpawn;
import net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo;
import net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.network.PlayerConnection;

public class NPCHuman extends NPC<EntityPlayer> {
	
	public NPCHuman(int creatorId, int dbid, Main plugin, EntityPlayer entity, Location location, JSONObject json) {
		super(dbid, plugin, entity, location);
		this.creatorId = creatorId;
		
		if (json != null && json.has("texValue") && json.has("texSig"))
			getGameProfile().getProperties().put("textures", new Property("textures", json.getString("texValue"), json.getString("texSig")));
	}
	
	public GameProfile getGameProfile() {
		return entity.getProfile();
	}
	
	public UUID getUniqueId() {
		return entity.getUniqueID();
	}
	
	public NPCHuman setSkin(String value, String signature) {
		getGameProfile().getProperties().put("textures", new Property("textures", value, signature));
		refresh();
		setDirty();
		return this;
	}

	@Override
	protected void showTo(Player p) {
		PlayerConnection connection = ((CraftPlayer)p).getHandle().b;
		connection.sendPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.a, entity)); // add npc to existence
		connection.sendPacket(new PacketPlayOutNamedEntitySpawn(entity)); // spawn entity
		connection.sendPacket(new PacketPlayOutEntityHeadRotation(entity, getFixedRot(getLocation().getYaw())));
		new BukkitRunnable() {
			public void run() {
				connection.sendPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.e, getEntity())); // remove from tab
			}
		}.runTaskAsynchronously(getPlugin());
	}

	@Override
	protected NPCType getType() {
		return NPCType.HUMAN;
	}

	@Override
	public JSONObject getJsonData() {
		Object[] textures = getGameProfile().getProperties().get("textures").toArray();
		if (textures == null || textures.length < 1)
			return null;
		Property texture = (Property) textures[0];
		
		JSONObject obj = new JSONObject()
				.put("uuid", getUniqueId().toString())
				.put("texValue", texture.getValue())
				.put("texSig", texture.getSignature());
		return obj;
	}
	
}
