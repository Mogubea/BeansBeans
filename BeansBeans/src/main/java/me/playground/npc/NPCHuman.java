package me.playground.npc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.datafixers.util.Pair;

import me.playground.main.Main;
import net.minecraft.network.protocol.game.PacketPlayOutEntityEquipment;
import net.minecraft.network.protocol.game.PacketPlayOutEntityHeadRotation;
import net.minecraft.network.protocol.game.PacketPlayOutNamedEntitySpawn;
import net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo;
import net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.entity.EnumItemSlot;

public class NPCHuman extends NPC<EntityPlayer> {
	
	private final int invSize = 6;
	private ItemStack[] inventory = new ItemStack[invSize];
	private List<Pair<EnumItemSlot, net.minecraft.world.item.ItemStack>> nmsInventory = new ArrayList<Pair<EnumItemSlot, net.minecraft.world.item.ItemStack>>();
	
	public NPCHuman(int creatorId, int dbid, Main plugin, EntityPlayer entity, Location location, JSONObject json) {
		super(creatorId, dbid, plugin, entity, location, json);
		
		if (json == null) return;
		if (json.has("texValue") && json.has("texSig"))
			getGameProfile().getProperties().put("textures", new Property("textures", json.getString("texValue"), json.getString("texSig")));
		
		JSONArray invArray = json.optJSONArray("inventory");
		if (invArray == null) return;
		for (int x = -1; ++x < invSize;) {
			JSONObject itemObj = invArray.optJSONObject(x);
			if (itemObj == null) return;
			Map<String, Object> itemMap = itemObj.toMap();
			try {
				inventory[x] = ItemStack.deserialize(itemMap);
			} catch (Exception e) {}
		}
		for (int x = -1; ++x < invSize;)
			nmsInventory.add(new Pair<EnumItemSlot,net.minecraft.world.item.ItemStack>(EnumItemSlot.values()[x], CraftItemStack.asNMSCopy(inventory[x])));
	}
	
	public ItemStack[] getEquipment() {
		return inventory;
	}
	
	public void setEquipment(ItemStack... equipment) {
		nmsInventory.clear();
		this.inventory = equipment;
		for (int x = -1; ++x < invSize;)
			nmsInventory.add(new Pair<EnumItemSlot,net.minecraft.world.item.ItemStack>(EnumItemSlot.values()[x], CraftItemStack.asNMSCopy(equipment[x])));
		
		for (Player p : Bukkit.getOnlinePlayers())
			showEquipment(p);
		
		setDirty();
	}
	
	protected void showEquipment(Player p) {
		PlayerConnection connection = ((CraftPlayer)p).getHandle().b;
		connection.sendPacket(new PacketPlayOutEntityEquipment(getEntityId(), nmsInventory));
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
	protected void showToAll() {
		Bukkit.getOnlinePlayers().forEach((p) -> { showTo(p); });
	}
	
	@Override
	protected void showTo(Player p) {
		PlayerConnection connection = ((CraftPlayer)p).getHandle().b;
		connection.sendPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.a, entity)); // add npc to existence
		connection.sendPacket(new PacketPlayOutNamedEntitySpawn(entity)); // spawn entity
		connection.sendPacket(new PacketPlayOutEntityHeadRotation(entity, getFixedRot(getLocation().getYaw())));
		connection.sendPacket(new PacketPlayOutEntityEquipment(getEntityId(), nmsInventory));
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
		JSONObject obj = super.getJsonData();
		
		JSONArray invArray = new JSONArray();
		for (int x = -1; ++x < invSize;) {
			JSONObject itemObj = new JSONObject();
			if (inventory[x] != null) {
				Map<String, Object> itemMap = inventory[x].serialize();
				itemMap.forEach((string, object) -> {
					itemObj.put(string, object);
				});
			}
			invArray.put(x, itemObj);
		}
		obj.put("inventory", invArray);
		
		Object[] textures = getGameProfile().getProperties().get("textures").toArray();
		if (textures == null || textures.length < 1)
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
	}
	
}
