package io.github.levtey.Compressors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import redempt.redlib.commandmanager.ArgType;
import redempt.redlib.commandmanager.CommandHook;
import redempt.redlib.commandmanager.CommandParser;

public class CompressorsCommand {
	
	private final Compressors plugin;
	
	public CompressorsCommand(Compressors plugin) {
		this.plugin = plugin;
		ArgType<Boolean> fromToType = new ArgType<>("fromto", str -> str.equalsIgnoreCase("from"))
				.setTab(sender -> Arrays.asList("from", "to"));
		new CommandParser(plugin.getResource("command.rdcml")).setArgTypes(fromToType).parse().register("", this);
	}
	
	@CommandHook("give")
	public void give(CommandSender sender, Player player) {
		player.getInventory().addItem(plugin.compressor());
		sendMessage(sender, "&aGave &e%player% &aa compressor.",
				"%player%", player.getName());
	}
	
	@SuppressWarnings("unchecked")
	@CommandHook("set")
	public void set(Player player, String recipe, Boolean from) {
		ItemStack item = player.getInventory().getItemInMainHand();
		if (item == null) {
			sendMessage(player, "&cYou must hold something to do this!");
			return;
		}
		if (from) {
			List<ItemStack> requiredItems = (List<ItemStack>) plugin.config.getConfig().getList("recipes." + recipe + ".from", new ArrayList<>());
			requiredItems.add(item);
			plugin.config.getConfig().set("recipes." + recipe + ".from", requiredItems);
		} else {
			plugin.config.getConfig().set("recipes." + recipe + ".to", item);
		}
		plugin.config.save();
		plugin.reloadConfigs();
		sendMessage(player, "&aSet &e%recipe%&a's %fromto% item to &3%amount%x %item%&a.",
				"%recipe%", recipe,
				"%fromto%", from ? "from" : "to",
				"%amount%", item.getAmount(),
				"%item%", item.getType().toString().toLowerCase().replace('_',  ' '));
	}
	
	@CommandHook("reload")
	public void reload(CommandSender sender) {
		plugin.reloadConfigs();
		sendMessage(sender, "&aConfigs reloaded!");
	}
	
	private void sendMessage(CommandSender sender, String message, Object...objects) {
		String toReplace = null;
		for (int i = 0; i < objects.length; i++) {
			if (i % 2 == 0) toReplace = objects[i].toString();
			else message = message.replace(toReplace, objects[i].toString());
		}
		sender.sendMessage(Compressors.makeReadable(message));
	}

}
