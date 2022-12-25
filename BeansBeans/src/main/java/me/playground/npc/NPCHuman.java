package me.playground.npc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.EquipmentSlot;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.json.JSONObject;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.datafixers.util.Pair;

import me.playground.main.Main;
import me.playground.utils.Utils;

public class NPCHuman extends NPC<ServerPlayer> {
	
	private final int invSize = 6;
	private ItemStack[] inventory = new ItemStack[invSize];
	private final List<Pair<EquipmentSlot, net.minecraft.world.item.ItemStack>> nmsInventory = new ArrayList<>();
	
	public NPCHuman(int creatorId, int dbid, Main plugin, ServerPlayer entity, Location location, JSONObject json) {
		super(creatorId, dbid, plugin, entity, location, json);

		if (json != null) {
			if (json.has("texValue") && json.has("texSig"))
				getGameProfile().getProperties().put("textures", new Property("textures", json.getString("texValue"), json.getString("texSig")));

			String invArray = json.optString("inventory");
			if (!(invArray == null || invArray.isEmpty()))
				inventory = Utils.itemStackArrayFromBase64(invArray);
		}

		for (int x = -1; ++x < invSize;)
			nmsInventory.add(new Pair<>(EquipmentSlot.values()[x], CraftItemStack.asNMSCopy(inventory[x])));
	}
	
	public ItemStack[] getEquipment() {
		return inventory;
	}
	
	public void setEquipment(ItemStack... equipment) {
		nmsInventory.clear();
		this.inventory = equipment;
		for (int x = -1; ++x < invSize;)
			nmsInventory.add(new Pair<>(EquipmentSlot.values()[x], CraftItemStack.asNMSCopy(equipment[x])));
		
		for (Player p : Bukkit.getOnlinePlayers())
			showEquipment(p);
		
		setDirty();
	}
	
	protected void showEquipment(Player p) {
		ServerGamePacketListenerImpl connection = ((CraftPlayer)p).getHandle().connection;
		connection.send(new ClientboundSetEquipmentPacket(getEntityId(), nmsInventory));
	}
	
	public GameProfile getGameProfile() {
		return getEntity().getGameProfile();
	}
	
	public UUID getUniqueId() {
		return getEntity().getUUID();
	}
	
	public NPCHuman setSkin(String value, String signature) {
		getGameProfile().getProperties().put("textures", new Property("textures", value, signature));
		refresh();
		setDirty();
		return this;
	}
	
	@Override
	public void showTo(Player p) {
		ServerGamePacketListenerImpl connection = ((CraftPlayer)p).getHandle().connection;
		//connection.send(new ClientboundAddPlayerPacket(getEntity())); // add npc to existence
		connection.send(ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(List.of(getEntity())));
		getPlugin().getServer().getScheduler().runTask(getPlugin(), () -> {
			connection.send(new ClientboundAddPlayerPacket(getEntity())); // Spawns the Entity for the player
			connection.send(new ClientboundRotateHeadPacket(entity, getFixedRot(getLocation().getYaw()))); // Rotates the head for the player
			});

		// Equipment and nullification of profile has to be sent slightly later
		getPlugin().getServer().getScheduler().runTaskLater(getPlugin(), () -> {
			connection.send(new ClientboundPlayerInfoRemovePacket(List.of(getEntity().getUUID()))); // Removes from tab by nullifying the profile
			showEquipment(p); // Sends equipment
		}, 5L);
	}

	@Override
	protected NPCType getType() {
		return NPCType.HUMAN;
	}

	@Override
	public JSONObject getJsonData() {
		JSONObject obj = super.getJsonData();
		
		obj.put("inventory", Utils.itemStackArrayToBase64(inventory));
		
		Object[] textures = getGameProfile().getProperties().get("textures").toArray();
		if (textures.length < 1)
			return obj;
		Property texture = (Property) textures[0];
		
		obj.put("uuid", getUniqueId().toString())
		.put("texValue", texture.getValue())
		.put("texSig", texture.getSignature());
		return obj;
	}

	@Override
	public void onInteract(Player player) {
		this.lookAtTarget(player);
		super.onInteract(player);
	}
	
}
