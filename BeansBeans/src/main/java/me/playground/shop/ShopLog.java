package me.playground.shop;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import me.playground.data.Datasource;

public class ShopLog {
	
	public static LoadingCache<Integer, ArrayList<ShopLog>> cache = CacheBuilder.from("maximumSize=500,expireAfterAccess=5m")
			.build(
					new CacheLoader<Integer, ArrayList<ShopLog>>() {
						public ArrayList<ShopLog> load(Integer shopId) throws Exception { // if the key doesn't exist, request it via this method
							ArrayList<ShopLog> prof = Datasource.loadShopLogs(shopId);
							return prof;
						}
					});
	
	public static ArrayList<ShopLog> from(Shop s) {
		if (s == null) return null;
		try {
			return cache.get(s.getShopId());
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public Timestamp getTime() {
		return time;
	}

	public int getPlayerId() {
		return playerId;
	}

	public String getComment() {
		return comment;
	}

	public String getData() {
		return data;
	}

	private final Timestamp time;
	private final int playerId;
	private final String comment;
	private final String data;
	
	public ShopLog(Timestamp time, int playerId, String comment, String data) {
		this.time = time;
		this.playerId = playerId;
		this.comment = comment;
		this.data = data;
	}
	
	
}
