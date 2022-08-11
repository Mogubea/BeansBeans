package me.playground.npc;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;

public class EntityNPC extends EntityPlayer {
	private final int npcId;
	private int creatorId;
	
	public EntityNPC(int id, MinecraftServer minecraftserver, WorldServer worldserver, GameProfile gameprofile) {
		super(minecraftserver, worldserver, gameprofile);
		this.npcId = id;
	}
	
	public int getNPCId() {
		return npcId;
	}
	
	public int getCreatorId() {
		return creatorId;
	}
	
	public EntityNPC setCreator(int playerId) {
		this.creatorId = playerId;
		return this;
	}
	
}
