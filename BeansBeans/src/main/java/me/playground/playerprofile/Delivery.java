package me.playground.playerprofile;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import me.playground.data.Dirty;
import me.playground.utils.Utils;

public class Delivery implements Dirty {
	
	private final int id;
	private final int recipientId; // The recipient of this delivery, the player that can open and claim anything from it
	private final int senderId; // The sender of this delivery, will likely be the server a lot of the time
	private final long dateReceived; // Date this instance was initially created
	private long openDate; // Date this instance was opened by the recipient
	private long expiryDate; // Date this instance expires and is no longer accessible by the recipient
	
	private final String msg; // Display message
	private final List<DeliveryContent> content = new ArrayList<DeliveryContent>(); // Content of Delivery
	
	private boolean dirty; // Dirty flag - When dirty, update the database entry on profile save.
	private boolean toRemove; // toRemove flag - Remove from player's delivery list on profile save.
	private int contentClaimed; // Simple counter, incremented by DeliveryContent classes, to save scanning through the list every time.
	
	public Delivery(int id, int recipientId, int senderId, long dateReceived, long openDate, long expiryDate, String msg, JSONObject content) {
		this.id = id;
		this.recipientId = recipientId;
		this.senderId = senderId;
		this.dateReceived = dateReceived;
		this.openDate = openDate;
		this.expiryDate = expiryDate;
		this.msg = msg;
		
		if (content == null || content.isEmpty()) return;
		JSONArray contentArray = content.optJSONArray("content");
		
		if (contentArray == null || contentArray.isEmpty()) return;
		int size = contentArray.length();
		
		for (int x = -1; ++x < size;) {
			DeliveryContent cont = DeliveryContent.fromJSON(this, contentArray.optJSONObject(x));
			if (cont != null) this.content.add(cont);
			if (cont.isClaimed()) this.contentClaimed++;
		}
	}
	
	public int getId() {
		return id;
	}
	
	public PlayerProfile getRecipientProfile() {
		return PlayerProfile.fromIfExists(recipientId);
	}
	
	public int getSenderId() {
		return senderId;
	}
	
	public long getOpenDate() {
		return openDate;
	}
	
	public long getDateReceived() {
		return dateReceived;
	}
	
	public String getReceiveString() {
		return Utils.timeStringFromNow(dateReceived);
	}
	
	public boolean canExpire() {
		return expiryDate > 0;
	}
	
	public long getExpiryDate() {
		return expiryDate;
	}
	
	public String getExpiryString() {
		return Utils.timeStringFromNow(expiryDate);
	}
	
	public boolean hasExpired() {
		boolean has = canExpire() && System.currentTimeMillis() >= expiryDate;
		if (has) flagRemoval(); // To let the Datasource know to remove this delivery from the live server.
		return has;
	}
	
	public boolean isOpened() {
		return openDate > 0L;
	}
	
	public void setOpened() {
		this.openDate = System.currentTimeMillis();
		setDirty(true);
	}
	
	public String getMessage() {
		return msg;
	}
	
	public List<DeliveryContent> getContent() {
		return content;
	}
	
	/**
	 * @return the size of the content list.
	 */
	public int getContentSize() {
		return content.size();
	}
	
	/**
	 * @return the amount of content claimed.
	 */
	public int getContentClaimed() {
		return contentClaimed;
	}
	
	public boolean isContentClaimed() {
		boolean has = contentClaimed >= content.size();
		if (has) flagRemoval(); // To let the Datasource know to remove this delivery from the live server.
		return has;
	}
	
	private void flagRemoval() {
		this.dirty = true;
		this.toRemove = true;
	}
	
	@Override
	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}
	
	public boolean toBeRemoved() {
		return toRemove;
	}
	
	protected void performClaim() {
		setDirty(true); // To save the time when package contents were claimed
		this.contentClaimed++;
	}
	
	public JSONObject getJson() {
		JSONObject obj = new JSONObject();
		JSONArray array = new JSONArray();
		int size = content.size();
		
		for (int x = -1; ++x < size;) {
			DeliveryContent cont = content.get(x);
			if (cont != null) array.put(cont.getJson());
		}
		
		obj.put("content", array);
		return obj;
	}
	
}
