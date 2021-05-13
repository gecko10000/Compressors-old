package io.github.levtey.Compressors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;
import redempt.redlib.configmanager.ConfigManager;
import redempt.redlib.configmanager.annotations.ConfigValue;
import redempt.redlib.itemutils.ItemBuilder;

public class Compressors extends JavaPlugin {
	protected ConfigManager config;
	public NamespacedKey compressorKey = new NamespacedKey(this, "compressor");
	
	@ConfigValue("item.name")
	String itemName = "&e&lCompressor";
	
	@ConfigValue("item.lore")
	List<String> lore = Arrays.asList("&aCompresses ores and", "&avarious other blocks.");
	
	public void onEnable() {
		reloadConfigs();
		new CompressorsCommand(this);
		new Listeners(this);
	}
	
	public void reloadConfigs() {
		config = new ConfigManager(this).register(this).saveDefaults().load();
	}
	
	public ItemStack compressor() {
		return new ItemBuilder(Material.DISPENSER)
				.setName(makeReadable(itemName))
				.setLore(lore.stream().map(Compressors::makeReadable).collect(Collectors.toList()).toArray(new String[0]))
				.addPersistentTag(compressorKey, PersistentDataType.BYTE, (byte) 1);
	}
	
	public static String makeReadable(String input) {
		return ChatColor.translateAlternateColorCodes('&', input);
	}

}
