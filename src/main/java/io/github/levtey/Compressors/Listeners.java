package io.github.levtey.Compressors;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Dispenser;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import redempt.redlib.itemutils.ItemUtils;
import redempt.redlib.misc.Task;

public class Listeners implements Listener {
	
	private Compressors plugin;
	
	public Listeners(Compressors plugin) {
		this.plugin = plugin;
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void onPlace(BlockPlaceEvent evt) {
		if (evt.getItemInHand().getType() == Material.AIR) return;
		if (!evt.getItemInHand().getItemMeta().getPersistentDataContainer().has(plugin.compressorKey, PersistentDataType.BYTE)) return;
		Block block = evt.getBlock();
		if (block.getType() != Material.DISPENSER) return;
		if (!evt.getPlayer().hasPermission("compressors.place")) {
			evt.setCancelled(true);
			return;
		}
		Dispenser dispenser = (Dispenser) block.getState();
		dispenser.getPersistentDataContainer().set(plugin.compressorKey, PersistentDataType.BYTE, (byte) 1);
		dispenser.update(true);
	}
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onBreak(BlockBreakEvent evt) {
		Block block = evt.getBlock();
		if (block.getType() != Material.DISPENSER) return;
		Dispenser dispenser = (Dispenser) block.getState();
		if (!dispenser.getPersistentDataContainer().has(plugin.compressorKey, PersistentDataType.BYTE)) return;
		evt.setDropItems(false);
		Location location = block.getLocation();
		location.getWorld().dropItemNaturally(location, plugin.compressor());
		for (ItemStack item : dispenser.getInventory()) {
			if (item == null) continue;
			location.getWorld().dropItemNaturally(location, item);
		}
	}
	
	private Set<Block> ignoreExtra = new HashSet<>();
	
	@SuppressWarnings("unchecked")
	@EventHandler
	public void onDispense(BlockDispenseEvent evt) {
		Block block = evt.getBlock();
		if (ignoreExtra.contains(block)) return;
		Dispenser dispenser = (Dispenser) block.getState();
		if (!dispenser.getPersistentDataContainer().has(plugin.compressorKey, PersistentDataType.BYTE)) return;
		String selectedRecipe = null;
		ConfigurationSection recipeSection = plugin.config.getConfig().getConfigurationSection("recipes");
		if (recipeSection == null) return;
		ItemStack dispensedItem = evt.getItem();
		for (String key : recipeSection.getKeys(false)) {
			List<ItemStack> requiredItems = (List<ItemStack>) recipeSection.getList(key + ".from");
			boolean valid = true;
			for (ItemStack requiredItem : requiredItems) {
				if (ItemUtils.count(dispenser.getInventory(), requiredItem) < requiredItem.getAmount() + (requiredItem.isSimilar(dispensedItem) ? -1 : 0)) {
					valid = false;
					break;
				}
			}
			if (!valid) continue;
			selectedRecipe = key;
			break;
		}
		if (selectedRecipe == null) {
			evt.setCancelled(true);
			return;
		}
		String selectedCopy = selectedRecipe;
		ignoreExtra.add(block);
		Task.syncDelayed(() -> {
			ignoreExtra.remove(block);
			for (ItemStack item : (List<ItemStack>) recipeSection.getList(selectedCopy + ".from")) {
				ItemUtils.remove(dispenser.getInventory(), item, item.getAmount());
			}
		});
		evt.setItem(recipeSection.getItemStack(selectedRecipe + ".to"));
	}

}
