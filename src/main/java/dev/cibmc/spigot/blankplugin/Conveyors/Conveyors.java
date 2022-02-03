package dev.cibmc.spigot.blankplugin.Conveyors;

import org.bukkit.World;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Item;

public class Conveyors extends JavaPlugin {
        public void run() {
            getLogger().info("loaded conveyor");
            World world = Bukkit.getWorld("world");
            Bukkit.getScheduler().runTaskLater(this, () -> world.getEntitiesByClass(Item.class).forEach((value)-> getLogger().info(value.getName())), 120);
            //Collection<Item> entities = world.getEntitiesByClass(Item.class);
           
        }

}
