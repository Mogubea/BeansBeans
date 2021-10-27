package me.playground.npc;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.json.JSONObject;

import me.playground.civilizations.jobs.Job;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import net.kyori.adventure.text.Component;
import net.minecraft.server.level.EntityPlayer;

public class NPCHumanEmployer extends NPCHuman {
	
	private Job job = Job.GATHERER;
	
	public NPCHumanEmployer(int creatorId, int dbid, Main plugin, EntityPlayer entity, Location location, JSONObject json) {
		super(creatorId, dbid, plugin, entity, location, json);
		if (json != null) {
			String s = json.optString("job", "GATHERER").toUpperCase();
			job = Job.getByName(s);
		}
		
		setTitle(Component.text("\u00a7e" + job.getName() + " Employer"), false);
	}
	
	public Job getJob() {
		return job;
	}
	
	public void setJob(Job job) {
		this.job = job;
		setDirty();
	}
	
	@Override
	public void onInteract(Player player) {
		super.onInteract(player);
		PlayerProfile pp = PlayerProfile.from(player);
		if (!pp.isInCivilization()) {
			this.sendMessage(player, Component.text("I can't hire those who aren't part of a Civilization!"));
			return;
		}
		
		if (pp.onCooldown("changeJob")) {
			this.sendMessage(player, Component.text("Hey there! It looks like you've changed your job fairly recently, please wait another " + (pp.getCooldown("changeJob")/1000) + "s!"));
			return;
		}
	}
	
	@Override
	public JSONObject getJsonData() {
		JSONObject obj = super.getJsonData();
		obj.put("job", getJob().getName().toUpperCase());
		return obj;
	}
	
	@Override
	protected NPCType getType() {
		return NPCType.HUMAN_EMPLOYER;
	}
	
}
