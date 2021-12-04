package me.playground.playerprofile;

import java.util.ArrayList;

import me.playground.celestia.logging.Celestia;
import me.playground.data.Datasource;
import me.playground.utils.BeanColor;
import net.kyori.adventure.text.Component;

public class ProfileModifyRequest {
	
	private static final ArrayList<ProfileModifyRequest> pendingRequests = Datasource.loadPendingModifyRequests();
	
	private final int id;
	private final int playerId;
	private final long timeInMillis;
	private final String data;
	private final ModifyType type;
	
	private RequestStatus status = RequestStatus.PENDING;
	private int reviewerId;
	private long reviewTime;
	
	public ProfileModifyRequest(int id, int playerId, long timeInMillis, String data, ModifyType type) {
		this.id = id;
		this.playerId = playerId;
		this.timeInMillis = timeInMillis;
		this.data = data;
		this.type = type;
	}
	
	public static boolean newRequest(int playerId, ModifyType type, String newData) {
		ProfileModifyRequest newReq = Datasource.createModifyRequest(playerId, type, newData);
		if (newReq != null) {
			pendingRequests.add(newReq);
		}
		return newReq != null;
	}
	
	public static ArrayList<ProfileModifyRequest> getPendingRequests() {
		return pendingRequests;
	}
	
	public void approve(int byWho, long timeInMillis) {
		this.status = RequestStatus.APPROVED;
		this.reviewerId = byWho;
		this.reviewTime = timeInMillis;
		
		PlayerProfile pp = PlayerProfile.fromIfExists(playerId);
		pp.setNickname(data);
		Celestia.logModify(reviewerId, "Changed %ID"+playerId+"'s Name to " + data);
		if (pp.isOnline())
			pp.getPlayer().sendMessage(Component.text("» ", BeanColor.STAFF).append(Component.text("\u00a7aYour nickname is now ").append(pp.getComponentName())));
		
		// Remove and save
		pendingRequests.remove(this);
		Datasource.reviewModifyRequest(this);
	}
	
	public void deny(int byWho, long timeInMillis) {
		this.status = RequestStatus.DENIED;
		this.reviewerId = byWho;
		this.reviewTime = timeInMillis;
		
		if (ProfileStore.from(playerId).isOnline()) {
			PlayerProfile.fromIfExists(playerId).getPlayer().sendMessage(Component.text("» ", BeanColor.STAFF).append(
					Component.text("\u00a7cYour request to change your nickname to \""+data+"\" was denied. Feel free to re-apply for a different nickname that's more appropriate.")));
		}
		
		// Remove and save
		pendingRequests.remove(this);
		Datasource.reviewModifyRequest(this);
	}
	
	public boolean isReviewed() {
		return status != RequestStatus.PENDING;
	}
	
	public int getId() {
		return id;
	}
	
	public RequestStatus getStatus() {
		return status;
	}

	public int getReviewerId() {
		return reviewerId;
	}
	
	public Component getReviewerName() {
		return PlayerProfile.getDisplayName(reviewerId);
	}

	public long getReviewTime() {
		return reviewTime;
	}

	public int getPlayerId() {
		return playerId;
	}

	public long getRequestTime() {
		return timeInMillis;
	}

	public String getData() {
		return data;
	}

	public ModifyType getType() {
		return type;
	}
	
	public enum ModifyType {
		NAME_CHANGE,
		LOGIN_MESSAGE,
		LOGOUT_MESSAGE;
	}
	
	public enum RequestStatus {
		PENDING,
		DENIED,
		APPROVED;
	}
	
}
