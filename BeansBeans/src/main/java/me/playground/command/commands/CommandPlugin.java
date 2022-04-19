package me.playground.command.commands;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import me.playground.command.BeanCommand;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

public class CommandPlugin extends BeanCommand {

	public CommandPlugin(Main plugin) {
		super(plugin, true, "plugins", "pl");
		description = "View the list of enabled plugins.";
	}
	
	@Override
	public boolean runCommand(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		Plugin[] plugins = getPlugin().getServer().getPluginManager().getPlugins();
		int size = plugins.length;
		
		Component list = Component.text("\u00a77Plugins (\u00a7f"+size+"\u00a77): ");
		for (int x = -1; ++x < size;) {
			Plugin p = plugins[x];
			PluginDescriptionFile pd = p.getDescription();
			String name = (p.isEnabled() ? "\u00a7a" : "\u00a7c") + p.getName();
			
			Component hoverInfo = Component.text(name + " (v " + pd.getVersion() + ")");
			if (!pd.getAuthors().isEmpty()) { 
				String auths = pd.getAuthors().toString();
				auths = auths.substring(1, auths.length() - 1);
				
				hoverInfo = hoverInfo.append(Component.text("\n\u00a77 - Author(s): \u00a7f" + auths));
			}
			if (pd.getWebsite() != null) hoverInfo = hoverInfo.append(Component.text("\n\u00a77 - Website: \u00a7f" + pd.getWebsite()));
			if (pd.getDescription() != null) hoverInfo = hoverInfo.append(Component.text("\n\n\u00a77" + pd.getDescription()));
			
			Component pluginInfo = Component.text(name).hoverEvent(HoverEvent.showText(hoverInfo));
			if (pd.getWebsite() != null) pluginInfo = pluginInfo.clickEvent(ClickEvent.openUrl(pd.getWebsite()));
			list = list.append(pluginInfo);
			if (x+1 < size) list = list.append(Component.text("\u00a77, "));
		}	
		
		sender.sendMessage(list);
		return true;
	}

	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		return Collections.emptyList();
	}
	
	@Override
	public Component getUsage(@Nonnull CommandSender sender, String str, String[] args) {
		return null;
	}

}
