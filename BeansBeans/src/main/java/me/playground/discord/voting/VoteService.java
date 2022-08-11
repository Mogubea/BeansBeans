package me.playground.discord.voting;

import me.playground.utils.BeanColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

public class VoteService {
	
	private final String serviceName;
	private final String serviceDisplayName;
	private final int crystalReward;
	private final int coinReward;
	private final Component component;
	
	public VoteService(String service, String displayName, int sapphire, int coins) {
		this.serviceDisplayName = displayName;
		this.crystalReward = sapphire;
		this.coinReward = coins;
		this.serviceName = service;
		
		this.component = Component.text(serviceDisplayName, BeanColor.CRYSTALS).hoverEvent(HoverEvent.showText(
				Component.text(
						serviceDisplayName + "\n" +
						"\u00a77" + serviceName
						).colorIfAbsent(BeanColor.CRYSTALS))).clickEvent(ClickEvent.openUrl(serviceName));
	}
	
	/**
	 * @return the amount of Crystal rewarded for voting with this service.
	 */
	public int getCrystalReward() {
		return crystalReward;
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
