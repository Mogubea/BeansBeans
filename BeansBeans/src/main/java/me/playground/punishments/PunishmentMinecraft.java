package me.playground.punishments;

import me.playground.ranks.Rank;
import me.playground.utils.BeanColor;
import me.playground.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerKickEvent;

import java.time.Instant;
import java.util.UUID;

public class PunishmentMinecraft extends Punishment<UUID> {

    protected PunishmentMinecraft(PunishmentManager manager, int id, int punisherId, Type type, int punished, UUID punishedIdentifier, Instant punishmentStart, Instant punishmentEnd, String reason, boolean enabled, boolean appealable) {
        super(manager, id, punisherId, type, punished, punishedIdentifier, punishmentStart, punishmentEnd, reason, enabled, appealable);
    }

    @Override
    public void enact() {
        Player p = Bukkit.getPlayer(getPunishedIdentifier());
        if (p == null) return;

        switch(this.getType()) {
            case MINECRAFT_BAN, MINECRAFT_IP_BAN -> p.kick(manager.getBanMessage(this), PlayerKickEvent.Cause.BANNED);
            case MINECRAFT_GLOBAL_MUTE, MINECRAFT_FULL_MUTE -> p.sendMessage(manager.getMuteMessage(this));
            case MINECRAFT_KICK -> {
                Component message = Component.text("\u00a74\u26a0 \u00a7rYou were kicked from Bean's Beans! \u00a74\u26a0", BeanColor.BAN);

                if (getReason() != null)
                    message = message.append(Component.text("\n\nReason: ", BeanColor.BAN)).append(Component.text("\"" + getReason() + "\"", BeanColor.BAN_REASON).append(Component.text(".", BeanColor.BAN)));

                p.kick(message, PlayerKickEvent.Cause.KICK_COMMAND);
            }
        }
    }

    @Override
    public void pardon() {
        setEnabled(false);

        Player p = Bukkit.getPlayer(getPunishedIdentifier());
        if (p == null) return;

        switch(this.getType()) {
            case MINECRAFT_GLOBAL_MUTE, MINECRAFT_FULL_MUTE -> p.sendMessage(Component.text("\u26a0", BeanColor.BAN).append(Component.text(" You are no longer muted.", BeanColor.BAN_REASON)));
        }

        if (getPunishedProfile() == null) return; // Unlikely
        Utils.sendMessage(Rank.MODERATOR, Component.text("\u26a0", BeanColor.BAN).append(getPunishedProfile().getComponentName().append(Component.text("'s " + getType().getPresentTense().toLowerCase() + " has just expired.", BeanColor.BAN_REASON)))
                .hoverEvent(HoverEvent.showText(toComponent())), true);
    }
}
