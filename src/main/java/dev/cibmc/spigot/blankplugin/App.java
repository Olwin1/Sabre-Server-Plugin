package dev.cibmc.spigot.blankplugin;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import java.io.IOException;
import org.bukkit.World;
//import org.bukkit.plugin.Plugin;
import org.bukkit.configuration.file.YamlConfiguration;
//import org.bukkit.event.EventHandler;
//import org.bukkit.entity.Item;
//import org.bukkit.event.player.PlayerDropItemEvent;
import java.io.File;
//import org.bukkit.persistence.PersistentDataContainer;
import java.util.Random;
//import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.inventory.ItemStack;
import dev.cibmc.spigot.blankplugin.Conveyors.Conveyors;
import java.util.Collection;

import org.bukkit.entity.Item;

public class App extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("Hello, SpigotMC!");
        File file = new File("./config/", "mainGeneratorConfiguration.yml");
        YamlConfiguration configStuff = YamlConfiguration.loadConfiguration(file);

        for (String key : configStuff.getKeys(false)) {
            String data = configStuff.getString(key);
            getLogger().info(data);
            String[] arr = data.replace("(", "").replace(")", "").split(",");
            getLogger().info(arr.toString() + " fffd");





            new BukkitRunnable() {
                public void run() {
                    World world = Bukkit.getWorld(arr[3]);
                    
                    Location coords = new Location(world, Double.parseDouble(arr[4]),Double.parseDouble(arr[5]),Double.parseDouble(arr[6]));
                    Material t = Material.matchMaterial(arr[0]);
                    
                    world.dropItem(coords, new ItemStack(t, Integer.parseInt(arr[2])));
                   
                }
            }.runTaskTimer(this, 10, Integer.parseInt(arr[1])*20);
                    
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("See you again, SpigotMC!");
    }

    public boolean onCommand(CommandSender sender, Command cmd, String abel, String[] args) {

        if (cmd.getName().equalsIgnoreCase("wild") && sender instanceof Player) {
            Player player = (Player) sender;

            Location originalLocation = player.getLocation();

            Random random = new Random();

            int x = random.nextInt(1000) + 100;
            int y = 150;
            int z = random.nextInt(1000) + 100;

            Location teleportLocation = new Location(player.getWorld(), x, y, z);

            player.teleport(teleportLocation);

            player.sendMessage("");
            player.sendMessage(ChatColor.GRAY + "[" + ChatColor.DARK_RED + "RandomTeleport" + ChatColor.GRAY + "]:"
                    + ChatColor.GOLD + "You have been teleported at " + ChatColor.GREEN
                    + (int) teleportLocation.distance(originalLocation) + ChatColor.GOLD + " blocks away.");
            player.sendMessage("");
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("randomteleport") && sender instanceof Player) {
            Player player = (Player) sender;
            player.sendMessage("");
            player.sendMessage(ChatColor.GRAY + "[" + ChatColor.DARK_RED + "RandomTeleport" + ChatColor.GRAY + "]:"
                    + ChatColor.GOLD + "Plugin is developed by: " + ChatColor.GREEN + "Novial" + ChatColor.GOLD + "!");
            player.sendMessage("");
            getLogger().info("loaded conveyor");
            World world = Bukkit.getWorld("world");
            Collection<Item> items = world.getEntitiesByClass(Item.class);
            /*Bukkit.getScheduler().runTaskTimer(this, () -> {*/while (items.iterator().hasNext()) {
                items.iterator().next().setVelocity(items.iterator().next().getVelocity().setZ(-5));
            }/*},100 , 200);*/
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("diamond") && sender instanceof Player) {
            Player player = (Player) sender;
            Location originalLocation = player.getLocation();
            player.sendBlockChange(originalLocation, Material.DIAMOND_BLOCK.createBlockData());
            player.sendMessage(ChatColor.GRAY + "[" + ChatColor.DARK_RED + "RandomTeleport" + ChatColor.GRAY + "]:"
                    + ChatColor.GOLD + "A Block Of" + ChatColor.GREEN + "Diamond" + ChatColor.GOLD
                    + "has Been Created!");
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("setgenerator") && sender instanceof Player) {
            Player player = (Player) sender;
            // Location originalLocation = player.getLocation();
            if (Material.matchMaterial(args[1]) != null) {
                String item = Material.matchMaterial(args[1]).name();
                String name = args[0];
                String delay = args[2];
                String count = args[3];
                Location location = player.getLocation();
                String locationString = "(%s,%s,%s,%s)".formatted(location.getWorld().getName(), location.getX(),
                        location.getY(), location.getZ());
                // String time = plugin.getConfig().getString("player.time.join");
                File file = new File("./config/", "mainGeneratorConfiguration.yml");
                YamlConfiguration configStuff = YamlConfiguration.loadConfiguration(file);
                configStuff.set(name, item + "," + delay + "," + count + "," + locationString);
                try {
                    configStuff.save(file);
                } catch (IOException e) {
                    getLogger().warning("Unable to save Generator Configuration."); // shouldn't really happen, but save
                                                                                    // throws the exception
                }

                player.sendMessage("Generator Has Been Set. (" + configStuff.get(name) + ")");
            } else {
                player.sendMessage("Please Enter A Valid Material.");
            }
            // player.sendBlockChange(originalLocation,
            // Material.DIAMOND_BLOCK.createBlockData());
            // player.sendMessage(ChatColor.GRAY + "[" + ChatColor.DARK_RED +
            // "RandomTeleport" + ChatColor.GRAY + "]:" + ChatColor.GOLD + "A Block Of" +
            // ChatColor.GREEN + "Diamond" + ChatColor.GOLD + "has Been Created!");
            return true;
        }

        return false;
    }

    public Conveyors getTpsCounter() {
        return new Conveyors();
    }
}