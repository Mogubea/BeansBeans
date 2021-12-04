package me.playground.gui;

import java.util.Arrays;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;

import me.playground.celestia.logging.CelestiaAction;
import me.playground.data.Datasource;
import me.playground.discord.DiscordBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.kyori.adventure.text.Component;

public class BeanGuiConfirmDiscord extends BeanGuiConfirm {
	
	final private DiscordBot manager;
	final private TextChannel channel;
	final private Member member;
	
	public BeanGuiConfirmDiscord(DiscordBot manager, Player p, TextChannel channel, Member member) {
		super(p, Arrays.asList(
				Component.text("\u00a77This action will link your \u00a7aMinecraft Account"),
				Component.text("\u00a77with the following \u00a79Discord Account\u00a77 on the"),
				Component.text("\u00a77Bean's Beans Server Discord:"),
				Component.empty(),
				Component.text("\u00a79" + member.getUser().getAsTag()),
				Component.empty(),
				Component.text("\u00a7cThis action can not currently be reversed."),
				Component.text("\u00a7cSo please make sure that is YOUR ACCOUNT.")));
		this.manager = manager;
		this.channel = channel;
		this.member = member;
	}

	@Override
	public void onInventoryClosed(InventoryCloseEvent e) {}
	
	public void onAccept() {
		Datasource.setDiscordLink(pp.getId(), member.getIdLong());
		manager.linkedAccounts.put(pp.getId(), member.getIdLong());
		final EmbedBuilder eb = new EmbedBuilder();
		eb.setFooter("Responding to " + member.getUser().getAsTag() + "'s /link Command", member.getUser().getEffectiveAvatarUrl());
		eb.setColor(0x44ddff);
		eb.appendDescription("Your Discord Account has been successfully linked to **"+p.getName()+"**.");
		p.sendMessage(Component.text("\u00a7aYou are now successfully linked to \u00a79" + member.getUser().getAsTag()));
		
		if (!member.hasPermission(Permission.ADMINISTRATOR)) {
			manager.updateNickname(pp);
			manager.updateRoles(pp);
		} else {
			p.sendMessage(Component.text("\u00a77However, due to having Administrator perms, your nickname and roles cannot be updated by the bot."));
		}
		
		channel.sendMessageEmbeds(eb.build()).queue();
		Datasource.logCelestia(CelestiaAction.LINK_DISCORD, pp.getId(), p.getLocation(), "Linked to Discord: " + member.getUser().getAsTag() + " (ID: "+member.getIdLong()+")");
		pp.grantAdvancement("beansbeans:advancements/discord");
		p.closeInventory();
	}
	
	public void onDecline() {
		final EmbedBuilder eb = new EmbedBuilder();
		eb.setColor(0xff4455);
		eb.setFooter("Responding to " + member.getUser().getAsTag() + "'s /link Command", member.getUser().getEffectiveAvatarUrl());
		eb.appendDescription("**"+p.getName()+"** has denied the Discord Link request. Please make sure that this is YOUR ACCOUNT, misuse of this command can lead to penalties.");
		channel.sendMessageEmbeds(eb.build()).queue();
		p.closeInventory();
	}
	
}
