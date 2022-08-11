package me.playground.voting;

import me.playground.utils.BeanColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

public class VoteService {
	
	private final String serviceName;
	private final String serviceDisplayName;
	private final int sapphireReward;
	private final int coinReward;
	private final Component component;
	
	public VoteService(String service, String displayName, int sapphire, int coins) {
		this.serviceDisplayName = displayName;
		this.sapphireReward = sapphire;
		this.coinReward = coins;
		this.serviceName = service;
		
		this.component = Component.text(serviceDisplayName, BeanColor.SAPPHIRE).hoverEvent(HoverEvent.showText(
				Component.text(
						serviceDisplayName + "\n" +
						"\u00a77" + serviceName
						).colorIfAbsent(BeanColor.SAPPHIRE))).clickEvent(ClickEvent.openUrl(serviceName));
	}
	
	/**
	 * @return the amount of Sapphire rewarded for voting with this service.
	 */
	public int getSapphireReward() {
		return sapphireReward;
	}
	
	/**
	 * @return the amount of Coins rewarded for voting with this service.
	 */
	public int getCoinReward() {
		return coinReward;
	}
	
	/**
	 * @return Friendly Service Name
	 */
	public String getDisplayName() {
		return serviceDisplayName;
	}

	public String getServiceName() {
		return serviceName;
	}
	
	public Component toComponent() {
		return component;
	}
	
}
