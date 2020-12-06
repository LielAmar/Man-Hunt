package com.lielamar.manhunt;

import com.packetmanager.lielamar.PacketManager;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class ManHunt extends JavaPlugin implements Listener, CommandExecutor {

	private Player target;
	private Location lastWorldLocation, lastNetherLocation;

	@Override
	public void onEnable() {
		target = null;

		Bukkit.getPluginManager().registerEvents(this, this);
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		if(target != null && target == event.getPlayer())
			target = null;
	}

	@EventHandler
	public void onUpdate(PlayerInteractEvent event) {
		if(target == null) return;
		if(event.getPlayer() == target) return;

		if(event.getPlayer().getWorld() != target.getWorld()) {
			if(lastWorldLocation != null && event.getPlayer().getWorld().getEnvironment().equals(World.Environment.NORMAL))
				event.getPlayer().setCompassTarget(lastWorldLocation);
			else if(lastNetherLocation != null && event.getPlayer().getWorld().getEnvironment().equals(World.Environment.NETHER))
				event.getPlayer().setCompassTarget(lastNetherLocation);
			PacketManager.sendActionbar(event.getPlayer(), ChatColor.RED + "Could not track " + target.getName() + "!");
			return;
		}

		PacketManager.sendActionbar(event.getPlayer(), ChatColor.YELLOW + "Tracking " + target.getName() + "!");
		event.getPlayer().setCompassTarget(target.getLocation());
	}

	@EventHandler
	public void onWorldChange(PlayerChangedWorldEvent event) {
		if(event.getFrom().getEnvironment().equals(World.Environment.NORMAL)) {
			lastWorldLocation = event.getPlayer().getLocation();
		} else if(event.getFrom().getEnvironment().equals(World.Environment.NETHER)) {
			lastNetherLocation = event.getPlayer().getLocation();
		}
	}

	@EventHandler
	public void onDeath(PlayerDeathEvent event) {
		if(target != null)
			event.getDrops().removeIf(item -> item.getType() == Material.COMPASS);
	}
	
	@EventHandler
	public void onRespawn(PlayerRespawnEvent event) {
		if(target != null)
			event.getPlayer().getInventory().addItem(new ItemStack(Material.COMPASS));
	}

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String cmdLabel, String[] args) {
		if(!(cs instanceof Player)) {
			cs.sendMessage(ChatColor.RED + "You must be a player to do that!");
			return false;
		}

		Player player = (Player) cs;
		if(!player.hasPermission("manhunt.command")) {
			player.sendMessage(ChatColor.RED + "You don't have enough permissions to do that!");
			return false;
		}

		if(cmd.getName().equalsIgnoreCase("manhunt")) {
			if(args.length == 0 && target == null) {
				player.sendMessage(ChatColor.RED + "Usage: /ManHunt <Player>");
				return false;
			} else if(args.length == 0) {
				player = null;
				return true;
			}

			Player tmpTarget = Bukkit.getPlayer(args[0]);
			if(tmpTarget == null) {
				player.sendMessage(ChatColor.RED + args[0] + " could not be found!");
				return false;
			}

			if(target == null) {
				target = tmpTarget;
				player.sendMessage(ChatColor.GREEN + "Started ManHunt!");

				for(Player pl : Bukkit.getOnlinePlayers()) {
					if(pl != target)
						pl.getInventory().addItem(new ItemStack(Material.COMPASS));
				}
			} else {
				target = null;
				player.sendMessage(ChatColor.RED + "Stopped ManHunt!");
			}
		}
		return false;
	}
}
