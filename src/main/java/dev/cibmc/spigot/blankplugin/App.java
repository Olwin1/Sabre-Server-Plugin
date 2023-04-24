package dev.cibmc.spigot.blankplugin;

import org.bukkit.plugin.java.JavaPlugin;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import java.io.IOException;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.util.Random;
import java.util.Set;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;

import java.util.Collection;

import org.bukkit.block.data.type.Bell;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import java.util.List;
import org.bukkit.util.Vector;

import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.Arrays;

import net.kyori.adventure.text.format.TextColor;

import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;

import org.bukkit.entity.Item;
import dev.cibmc.spigot.blankplugin.DB.Database.SQLite;
import it.unimi.dsi.fastutil.Pair;

import java.util.HashMap;
import dev.cibmc.spigot.blankplugin.DB.Database.Database;
import java.util.Map;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.inventory.PlayerInventory;
import java.util.Date;
import java.util.Calendar;

public class App extends JavaPlugin implements Listener {

    // Create a new file object representing the "blockSpeedConfig.yml" configuration file.
    File file = new File("./config/", "blockSpeedConfig.yml");
    // Load the configuration file into a new YamlConfiguration object.
    YamlConfiguration blockSpeedConfig = YamlConfiguration.loadConfiguration(file);

    // Create a new file object representing the "spawn.yml" configuration file.
    File f = new File(getDataFolder(), "spawn.yml");
    // Load the configuration file into a new YamlConfiguration object.
    YamlConfiguration spawnLocations = YamlConfiguration.loadConfiguration(f);

    // Create a new file object representing the "teleport.yml" configuration file.
    File f2 = new File(getDataFolder(), "teleport.yml");
    // Load the configuration file into a new YamlConfiguration object.
    YamlConfiguration teleportLocations = YamlConfiguration.loadConfiguration(f2);

    // Create a new hashmap to store database connections.
    private Map<String, Database> databases = new HashMap<String, Database>();

    // Create a static instance of the App class.
    public static App INSTANCE;

    // Create a static arraylist to store player names.
    static ArrayList<String> playerName = new ArrayList<String>();
    // Create a static arraylist to store bell locations.
    static ArrayList<ArrayList<Integer[]>> bellLocations = new ArrayList<ArrayList<Integer[]>>();
    // Create a static database object.
    static Database database;
    // Create a static instance of the Economy class.
    private static Economy econ = null;
    // Create a static arraylist to store current spawn locations.
    static ArrayList<Integer> currentSpawn = new ArrayList<Integer>();
    // Create a static arraylist to store teleport locations.
    static ArrayList<Integer[]> teleportLocationsList = new ArrayList<Integer[]>();


    @Override
    public void onEnable() {
        for (int i = 0; i < teleportLocations.getKeys(true).size(); i++) {
            String str = (String) teleportLocations.get(String.valueOf(i));
            String[] origin = str.split(",");
            Integer[] tmp = { Integer.parseInt(origin[0]), Integer.parseInt(origin[1]), Integer.parseInt(origin[2]),
                    Integer.parseInt(origin[3]), Integer.parseInt(origin[4]), Integer.parseInt(origin[5]) };
            teleportLocationsList.add(tmp);
        }
        String day = (String) spawnLocations.get("day");
        getLogger().info("DAY IS: " + day);
        Date d = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        Integer date = cal.get(Calendar.DAY_OF_WEEK);
        if (Integer.parseInt(day) != date) {
            getLogger().info("NEW ONE:" + day + "also " + String.valueOf(date));
            int maxIndex = spawnLocations.getKeys(true).size() - 2;
            Random rand = new Random();
            int n = rand.nextInt(maxIndex);
            spawnLocations.set("day", String.valueOf(date));
            try {
                spawnLocations.save(f);
            } catch (IOException e) {
                getLogger().warning("Unable to save Spawn Configuration."); // shouldn't really happen, but save
                                                                            // throws the exception
            }
            String currSpawnStr = (String) spawnLocations.get(String.valueOf(n));
            String[] currSpawn = currSpawnStr.split(",");
            currentSpawn.add(Integer.parseInt(currSpawn[0]));
            currentSpawn.add(Integer.parseInt(currSpawn[1]));
            currentSpawn.add(Integer.parseInt(currSpawn[2]));
            currentSpawn.add(Integer.parseInt(currSpawn[3]));

        } else {
            Integer i = (Integer) spawnLocations.get("current");
            String currSpawnStr = (String) spawnLocations.get(String.valueOf(i));
            String[] currSpawn = currSpawnStr.split(",");
            currentSpawn.add(Integer.parseInt(currSpawn[0]));
            currentSpawn.add(Integer.parseInt(currSpawn[1]));
            currentSpawn.add(Integer.parseInt(currSpawn[2]));
            currentSpawn.add(Integer.parseInt(currSpawn[3]));
        }

        if (!setupEconomy()) {
            getLogger().severe(
                    String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        INSTANCE = this;
        this.getDataFolder().mkdirs();
        initializeDatabase("blankplugin",
                "CREATE TABLE IF NOT EXISTS gens (uuid VARCHAR(36), gen_x INTEGER[], gen_y INTEGER[], gen_z INTEGER[]);");

        Collection<? extends Player> players = this.getServer().getOnlinePlayers();
        database = getDatabase("blankplugin");
        for (Player player : players) {
            getLogger().info(player.getUniqueId().toString());
            List<Object> results = getDatabase("blankplugin").queryRowThree(
                    "SELECT * FROM gens WHERE uuid=\"" + player.getUniqueId().toString() + "\"", "gen_x", "gen_y",
                    "gen_z");
            playerName.add(player.getName());
            if (results != null) {
                ArrayList<Integer> x = new ArrayList<Integer>();
                ArrayList<Integer> y = new ArrayList<Integer>();
                ArrayList<Integer> z = new ArrayList<Integer>();

                Integer iter = 0;
                for (Object obj : results) {
                    ArrayList<Integer> arr = new ArrayList<Integer>();
                    String str = (String) obj;
                    str = StringUtils.chop(str).substring(1);
                    String[] f = str.split(",");
                    for (String i : f) {
                        if (i != "") {
                            arr.add(Integer.parseInt(i));
                        }
                    }
                    getLogger().info("ID: " + arr.toString() + "iter:" + iter);
                    if (iter == 0) {
                        x = arr;
                    }
                    if (iter == 1) {
                        y = arr;
                    }
                    if (iter == 2) {
                        z = arr;
                    }
                    iter++;
                }
                ArrayList<Integer[]> coords = new ArrayList<Integer[]>();

                for (int i = 0; i <= x.size() - 1; i++) {
                    Integer[] temp = { x.get(i), y.get(i), z.get(i) };
                    coords.add(temp);
                }
                bellLocations.add(coords);
                {
                    // get element number 0 and 1 and put it in a variable,
                    // and the next time get element 1 and 2 and put this in another variable.
                }
            } else {
                ArrayList<Integer[]> inner = new ArrayList<Integer[]>();

                bellLocations.add(inner);
            }
        }

        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        getCommand("blank").setExecutor(this);

        getServer().getScheduler().scheduleSyncRepeatingTask(this, new EntitySearch(), 1L, 1L);
        getLogger().info("Hello, SpigotMC!");
        File file = new File("./config/", "mainGeneratorConfiguration.yml");
        YamlConfiguration configStuff = YamlConfiguration.loadConfiguration(file);

        for (String key : configStuff.getKeys(false)) {
            String data = configStuff.getString(key);
            String[] arr = data.replace("(", "").replace(")", "").split(",");

            new BukkitRunnable() {
                public void run() {
                    World world = Bukkit.getWorld(arr[3]);

                    Location coords = new Location(world, Double.parseDouble(arr[4]), Double.parseDouble(arr[5]),
                            Double.parseDouble(arr[6]));
                    Material t = Material.matchMaterial(arr[0]);

                    world.dropItem(coords, new ItemStack(t, Integer.parseInt(arr[2])));

                }
            }.runTaskTimer(this, 10, Integer.parseInt(arr[1]) * 20);

        }
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this,
                () -> {
                    if (playerName.size() != 0) {

                        for (int i = 0; i <= playerName.size() - 1; i++) {
                            for (Integer[] location : bellLocations.get(i)) {

                                World world = Bukkit.getWorld("plots");
                                if (world.isChunkLoaded(location[0] >> 4, location[2] >> 4)) {

                                    Location coords = new Location(world, location[0], location[1],
                                            location[2]);
                                    Block bell = coords.getBlock();
                                    BlockData bellData = bell.getBlockData();
                                    Material t = bell.getRelative(((Directional) bellData).getFacing()).getType();
                                    Material drop;
                                    String name;
                                    if (t == Material.OAK_LOG) {
                                        name = "Wood";
                                        drop = Material.OAK_PLANKS;
                                    } else if (t == Material.LAPIS_BLOCK) {
                                        name = "Lapis";
                                        drop = Material.LAPIS_LAZULI;
                                    } else if (t == Material.IRON_BLOCK) {
                                        name = "Iron";
                                        drop = Material.IRON_INGOT;
                                    } else if (t == Material.REDSTONE_BLOCK) {
                                        name = "Redstone";
                                        drop = Material.REDSTONE;
                                    } else if (t == Material.GOLD_BLOCK) {
                                        name = "Gold";
                                        drop = Material.GOLD_INGOT;
                                    } else if (t == Material.DIAMOND_BLOCK) {
                                        name = "Diamond";
                                        drop = Material.DIAMOND;
                                    } else if (t == Material.EMERALD_BLOCK) {
                                        name = "Emerald";
                                        drop = Material.EMERALD;
                                    } else if (t == Material.WAXED_COPPER_BLOCK) {
                                        name = "Copper";
                                        drop = Material.COPPER_INGOT;
                                    } else if (t == Material.OXIDIZED_COPPER) {
                                        name = "Aged Copper";
                                        drop = Material.RAW_COPPER;
                                    } else if (t == Material.PRISMARINE) {
                                        name = "Prismarine";
                                        drop = Material.PRISMARINE_SHARD;
                                    } else if (t == Material.PRISMARINE_BRICKS) {
                                        name = "Advanced Prismarine";
                                        drop = Material.PRISMARINE_SHARD;
                                    } else if (t == Material.DARK_PRISMARINE) {
                                        name = "Heavy Prismarine";
                                        drop = Material.PRISMARINE_SHARD;
                                    } else if (t == Material.SEA_LANTERN) {
                                        name = "Crystalized Prismarine";
                                        drop = Material.PRISMARINE_CRYSTALS;
                                    } else if (t == Material.GLOWSTONE) {
                                        name = "Glowstone";
                                        drop = Material.GLOWSTONE_DUST;
                                    } else if (t == Material.COBBLED_DEEPSLATE) {
                                        name = "Deepslate";
                                        drop = Material.COBBLED_DEEPSLATE;
                                    } else if (t == Material.AMETHYST_BLOCK) {
                                        name = "Amethyst";
                                        drop = Material.AMETHYST_SHARD;
                                    } else if (t == Material.NETHERITE_BLOCK) {
                                        name = "Netherite";
                                        drop = Material.NETHERITE_INGOT;
                                    } else if (t == Material.NETHER_BRICKS) {
                                        name = "Bricks";
                                        drop = Material.NETHER_BRICK;
                                    } else if (t == Material.RED_NETHER_BRICKS) {
                                        name = "Red Bricks";
                                        drop = Material.NETHER_BRICK;
                                    } else if (t == Material.CRIMSON_STEM) {
                                        name = "Crimson";
                                        drop = Material.CRIMSON_FUNGUS;
                                    } else if (t == Material.NETHER_WART_BLOCK) {
                                        name = "Advanced Crimson";
                                        drop = Material.CRIMSON_FUNGUS;
                                    } else if (t == Material.WARPED_STEM) {
                                        name = "Warped";
                                        drop = Material.WARPED_FUNGUS;
                                    } else if (t == Material.WARPED_WART_BLOCK) {
                                        name = "Advanced Warped";
                                        drop = Material.WARPED_FUNGUS;
                                    } else if (t == Material.MAGMA_BLOCK) {
                                        name = "Magma";
                                        drop = Material.MAGMA_CREAM;
                                    } else if (t == Material.SHROOMLIGHT) {
                                        name = "Mushroom";
                                        drop = Material.RED_MUSHROOM;
                                    } else if (t == Material.PURPUR_BLOCK) {
                                        name = "Purpur";
                                        drop = Material.PURPLE_DYE;
                                    } else if (t == Material.PURPUR_PILLAR) {
                                        name = "Advanced Purpur";
                                        drop = Material.PURPLE_DYE;
                                    } else if (t == Material.CALCITE) {
                                        name = "Calcite";
                                        drop = Material.CALCITE;
                                    } else if (t == Material.TUFF) {
                                        name = "Tuff";
                                        drop = Material.TUFF;
                                    } else if (t == Material.RAW_IRON_BLOCK) {
                                        name = "Raw Iron";
                                        drop = Material.RAW_IRON;
                                    } else if (t == Material.RAW_COPPER_BLOCK) {
                                        name = "Raw Copper";
                                        drop = Material.RAW_COPPER;
                                    } else if (t == Material.RAW_GOLD_BLOCK) {
                                        name = "Raw Gold";
                                        drop = Material.RAW_GOLD;
                                    } else if (t == Material.DRIPSTONE_BLOCK) {
                                        name = "Ancient Ore";
                                        drop = Material.DRIPSTONE_BLOCK;
                                    } else {
                                        name = "null";
                                        drop = Material.LIGHT_BLUE_WOOL;
                                    }
                                    ItemStack preItem = new ItemStack(drop, 1);
                                    ItemMeta meta = preItem.getItemMeta();

                                    meta.displayName(
                                            Component
                                                    .text(nonItalic("Mined "
                                                            + name))
                                                    .color(TextColor.fromHexString("#8e8e8e")));
                                    preItem.setItemMeta(meta);
                                    Item item = world.dropItem(coords.add(coords, 0.5, -0.5, 0.5), preItem);
                                    item.setVelocity(new Vector());

                                }
                            }
                        }
                    }
                }, 10, 10 * 20);
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new Placeholders(this).register();
        }

    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = this.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public String nonItalic(String string) {
        return ChatColor.translateAlternateColorCodes('&', ChatColor.GRAY + string);
    }

    public void initializeDatabase(final String databaseName, final String createStatement) {
        final Database db = new SQLite(databaseName, createStatement, this.getDataFolder());
        db.load();
        this.databases.put(databaseName, db);
    }

    public Map<String, Database> getDatabases() {
        return this.databases;
    }

    public Database getDatabase(final String databaseName) {
        return this.getDatabases().get(databaseName);
    }

    public static App getInstance() {
        return INSTANCE;
    }

    public static String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public String getPlayerGens(String name) {
        int index = playerName.indexOf(name);
        return String.valueOf(bellLocations.get(index).size());
    }

    public static String capitalizeWord(String str) {
        String words[] = str.split("\\s");
        String capitalizeWord = "";
        for (String w : words) {
            String first = w.substring(0, 1);
            String afterfirst = w.substring(1);
            capitalizeWord += first.toUpperCase() + afterfirst + " ";
        }
        return capitalizeWord.trim();
    }

    static ItemStack createItem(Material material, Integer amount, String title, String titleColour,
            List<Component> lore) {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta reMeta = item.getItemMeta();
        reMeta.displayName(Component.text(title).color(TextColor.fromHexString(titleColour)));
        reMeta.lore(lore);
        item.setItemMeta(reMeta);
        return item;
    }

    static ItemStack createConveyorItem(String name, String nameColour, String speed) {
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text(""));
        lore.add(Component.text("Conveyor Speed:").color(TextColor.fromHexString("#a8a8a8")));
        Component x = Component.text("• ").color(TextColor.fromHexString("#a8a8a8"));
        x = x.append(Component.text(speed + " m/s").color(TextColor.fromHexString("#87a1ff")));
        lore.add(x);
        return createItem(Material.PAPER, 1, name, nameColour, lore);
    }

    static ItemStack createDropperItem(String name, String nameColour, String value) {
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text(""));
        lore.add(Component.text("Drop Value:").color(TextColor.fromHexString("#a8a8a8")));
        Component x = Component.text("• ").color(TextColor.fromHexString("#a8a8a8"));
        x = x.append(Component.text("$" + value).color(TextColor.fromHexString("#87a1ff")));
        lore.add(x);
        return createItem(Material.PAPER, 1, name, nameColour, lore);
    }

    void placeDropper(Player player, Block b, Material mat) {
        Block b2 = b.getRelative(BlockFace.UP);
        BlockFace dir = player.getFacing();
        Block b3 = b2.getRelative(dir);
        if (b3.getType() != Material.AIR) {
            return;
        }
        if (b2.getType() != Material.AIR) {
            return;
        }
        if (b.getType() != Material.AIR) {
            return;
        }
        b.setType(mat);
        b2.setType(mat);
        b3.setType(Material.BELL);
        Bell bloc = (Bell) b3.getBlockData();
        ((Directional) bloc).setFacing(dir.getOppositeFace());
        bloc.setAttachment(Bell.Attachment.SINGLE_WALL);

        b3.setBlockData(bloc);
        ItemStack paper = player.getInventory().getItemInMainHand();
        paper.setAmount(paper.getAmount() - 1);
        player.getInventory().setItemInMainHand(paper);
        Integer index = playerName.indexOf(player.getName());
        ArrayList<Integer[]> list = bellLocations.get(index);
        Location bellLocation = b3.getLocation();
        Integer[] coordinates = { bellLocation.getBlockX(), bellLocation.getBlockY(), bellLocation.getBlockZ() };
        list.add(coordinates);
        bellLocations.set(index, list);

    }

    @Override
    public void onDisable() {
        for (Player player : this.getServer().getOnlinePlayers()) {
            List<Object> results = database
                    .queryRow("SELECT * FROM gens WHERE uuid=\"" + player.getUniqueId().toString() + "\"", "gen_x");
            getLogger().info("Is");
            getLogger().info(String.valueOf(results.size()));
            Integer index = playerName.indexOf(player.getName());
            getLogger().info(String.valueOf(index));
            ArrayList<Integer[]> locations = bellLocations.get(index);
            getLogger().info(String.valueOf(locations));

            String x = "";
            String y = "";
            String z = "";
            for (Integer[] coords : locations) {
                x = x + String.valueOf(coords[0]) + ",";
                y = y + String.valueOf(coords[1]) + ",";
                z = z + String.valueOf(coords[2]) + ",";
            }
            if (results.size() == 0) {
                if (database.executeStatement("INSERT INTO gens (uuid, gen_x, gen_y, gen_z) VALUES(\""
                        + player.getUniqueId().toString() + "\", '{" + x + "}', '{" + y + "}', '{" + z + "}');")) {
                    // Executed statement successfully
                } else {
                    getLogger().info("errores");
                    // Execution failure.
                }
            } else {
                if (database.executeStatement("UPDATE gens SET gen_x = '{" + x + "}',gen_y = '{" + y + "}', gen_z = '{"
                        + z + "}' WHERE uuid=\"" + player.getUniqueId().toString() + "\";")) {
                    // Execution Successfull
                } else {
                    getLogger().info("errores");
                }
            }
            for (Object obj : results) {
                String ID = (String) obj;
                getLogger().info("ID: " + ID);
            }
            getLogger().info("See you again, SpigotMC!");
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        getLogger().info(player.getUniqueId().toString());
        List<Object> results = getDatabase("blankplugin").queryRowThree(
                "SELECT * FROM gens WHERE uuid=\"" + player.getUniqueId().toString() + "\"", "gen_x", "gen_y", "gen_z");
        playerName.add(player.getName());
        if (results != null) {
            ArrayList<Integer> x = new ArrayList<Integer>();
            ArrayList<Integer> y = new ArrayList<Integer>();
            ArrayList<Integer> z = new ArrayList<Integer>();

            Integer iter = 0;
            for (Object obj : results) {
                ArrayList<Integer> arr = new ArrayList<Integer>();
                String str = (String) obj;
                str = StringUtils.chop(str).substring(1);
                String[] f = str.split(",");
                for (String i : f) {
                    if (i != "") {
                        arr.add(Integer.parseInt(i));
                    }
                }
                getLogger().info("ID: " + arr.toString() + "iter:" + iter);
                if (iter == 0) {
                    x = arr;
                }
                if (iter == 1) {
                    y = arr;
                }
                if (iter == 2) {
                    z = arr;
                }
                iter++;
            }
            ArrayList<Integer[]> coords = new ArrayList<Integer[]>();

            for (int i = 0; i <= x.size() - 1; i++) {
                Integer[] temp = { x.get(i), y.get(i), z.get(i) };
                coords.add(temp);
            }
            bellLocations.add(coords);
            {
                // get element number 0 and 1 and put it in a variable,
                // and the next time get element 1 and 2 and put this in another variable.
            }
        } else {
            ArrayList<Integer[]> inner = new ArrayList<Integer[]>();

            bellLocations.add(inner);
        }

    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        List<Object> results = database
                .queryRow("SELECT * FROM gens WHERE uuid=\"" + player.getUniqueId().toString() + "\"", "gen_x");
        getLogger().info("Is");
        getLogger().info(String.valueOf(results.size()));
        int index = playerName.indexOf(player.getName());
        ArrayList<Integer[]> locations = bellLocations.get(index);
        playerName.remove(index);
        bellLocations.remove(index);
        String x = "";
        String y = "";
        String z = "";
        for (Integer[] coords : locations) {
            x = x + String.valueOf(coords[0]) + ",";
            y = y + String.valueOf(coords[1]) + ",";
            z = z + String.valueOf(coords[2]) + ",";
        }
        if (results.size() == 0) {
            if (database.executeStatement("INSERT INTO gens (uuid, gen_x, gen_y, gen_z) VALUES(\""
                    + player.getUniqueId().toString() + "\", '{" + x + "}', '{" + y + "}', '{" + z + "}');")) {
                // Executed statement successfully
            } else {
                getLogger().info("errores");
                // Execution failure.
            }
        } else {
            if (database.executeStatement("UPDATE gens SET gen_x = '{" + x + "}',gen_y = '{" + y + "}', gen_z = '{" + z
                    + "}' WHERE uuid=\"" + player.getUniqueId().toString() + "\";")) {
                // Execution Successfull
            } else {
                getLogger().info("errores");
            }
        }
        for (Object obj : results) {
            String ID = (String) obj;
            getLogger().info("ID: " + ID);
        }
        getLogger().info("See you again, SpigotMC!");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMoveEvent(PlayerMoveEvent event) {
        // will check if the player is in the portal or not.
        Player player = event.getPlayer();
        Location loc = player.getLocation();
        for (Integer[] coord : teleportLocationsList) {
            if (loc.getBlockX() == coord[0]) {
                if (loc.getBlockY() == coord[1]) {
                    if (loc.getBlockZ() == coord[2]) {
                        if (player.getWorld().equals(Bukkit.getWorld("testingWorld"))) {
                            int decimalX = (int) loc.getBlockX();
                            int decimalY = (int) loc.getBlockY();
                            int decimalZ = (int) loc.getBlockZ();
                            double fractionalX = loc.getX() - decimalX;
                            double fractionalY = loc.getY() - decimalY;
                            double fractionalZ = loc.getZ() - decimalZ;
                            Location dest = new Location(player.getWorld(), coord[3] + fractionalX,
                                    coord[4] + fractionalY, coord[5] + fractionalZ);
                            dest.setPitch(loc.getPitch());
                            dest.setYaw(loc.getYaw());
                            Vector velo = player.getVelocity();
                            player.teleport(dest);
                            velo.setY(-1);
                            player.setVelocity(velo);

                        }
                    }
                }
            }
        }
        /*
         * if (-34 < loc.getX() && loc.getX() < -29) {
         * double y = loc.getY();
         * if (-317 < loc.getZ() && loc.getZ() < -316) {
         * 
         * if (-4 <= y && 0 > y) {
         * 
         * double end = y - Math.floor(y);
         * loc.setY(-22 + end);
         * Vector velo = player.getVelocity();
         * player.teleport(loc);
         * player.setVelocity(velo);
         * }
         * 
         * } else if (-314 < loc.getZ() && loc.getZ() < -312) {
         * 
         * if (-22 <= y && -19 > y) {
         * 
         * double end = y - Math.floor(y);
         * loc.setY(-4 + end);
         * Vector velo = player.getVelocity();
         * player.teleport(loc);
         * player.setVelocity(velo);
         * }
         * 
         * }
         * }
         */

    }
    private Location getLocationIfValid(Block block, Material material, BlockFace blockFace) {
        Block tempBlock = block.getRelative(blockFace);
        if (tempBlock.getType() == material) {
            Directional direction = (Directional) tempBlock.getBlockData();
            if (direction.getFacing() == blockFace.getOppositeFace()) {
                return tempBlock.getLocation();
            }
        }
        return null;
    }
    private Location getLocationIfFacingBell(Block block, BlockFace facingDirection) {
        Block tempBlock = block.getRelative(facingDirection);
        if (tempBlock.getType() == Material.BELL) {
            Directional direction = (Directional) tempBlock.getBlockData();
            if (direction.getFacing() == facingDirection.getOppositeFace()) {
                return tempBlock.getLocation();
            }
        }
        return null;
    }
    
    

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        int i = 0;
        for (ArrayList<Integer[]> item : bellLocations) {
            Location location = event.getBlock().getLocation();
            Block tempBlockW = event.getBlock().getRelative(BlockFace.EAST);
            Block tempBlockE = event.getBlock().getRelative(BlockFace.WEST);
            Block tempBlockN = event.getBlock().getRelative(BlockFace.SOUTH);
            Block tempBlockS = event.getBlock().getRelative(BlockFace.NORTH);

            if (tempBlockW.getType() == Material.BELL) {
                Directional direction = (Directional) tempBlockW.getBlockData();
                if (direction.getFacing() == BlockFace.WEST) {
                    location = tempBlockW.getLocation();
                }
            }

            if (tempBlockE.getType() == Material.BELL) {
                Directional direction = (Directional) tempBlockE.getBlockData();
                if (direction.getFacing() == BlockFace.EAST) {
                    location = tempBlockE.getLocation();
                }
            }

            if (tempBlockN.getType() == Material.BELL) {
                Directional direction = (Directional) tempBlockN.getBlockData();
                if (direction.getFacing() == BlockFace.NORTH) {
                    location = tempBlockN.getLocation();
                }
            }

            if (tempBlockS.getType() == Material.BELL) {
                Directional direction = (Directional) tempBlockS.getBlockData();
                if (direction.getFacing() == BlockFace.SOUTH) {
                    location = tempBlockS.getLocation();
                }
            }

            Block facing = event.getBlock().getRelative(BlockFace.UP);
            tempBlockW = facing.getRelative(BlockFace.EAST);
            tempBlockE = facing.getRelative(BlockFace.WEST);
            tempBlockN = facing.getRelative(BlockFace.SOUTH);
            tempBlockS = facing.getRelative(BlockFace.NORTH);

            if (tempBlockW.getType() == Material.BELL) {
                Directional direction = (Directional) tempBlockW.getBlockData();
                if (direction.getFacing() == BlockFace.WEST) {
                    location = tempBlockW.getLocation();
                }
            }

            if (tempBlockE.getType() == Material.BELL) {
                Directional direction = (Directional) tempBlockE.getBlockData();
                if (direction.getFacing() == BlockFace.EAST) {
                    location = tempBlockE.getLocation();
                }
            }

            if (tempBlockN.getType() == Material.BELL) {
                Directional direction = (Directional) tempBlockN.getBlockData();
                if (direction.getFacing() == BlockFace.NORTH) {
                    location = tempBlockN.getLocation();
                }
            }

            if (tempBlockS.getType() == Material.BELL) {
                Directional direction = (Directional) tempBlockS.getBlockData();
                if (direction.getFacing() == BlockFace.SOUTH) {
                    location = tempBlockS.getLocation();
                }
            }

            Integer[] x = { location.getBlockX(), location.getBlockY(), location.getBlockZ() };
            int i2 = 0;
            for (Integer[] coords : item) {
                if (Arrays.equals(coords, x)) {

                    event.setCancelled(true);
                    if (playerName.get(i) == event.getPlayer().getName()) {
                        Block temp = location.getBlock();
                        BlockData bd = temp.getBlockData();
                        Directional direction = (Directional) bd;
                        Block temp2 = temp.getRelative(direction.getFacing());
                        Block temp3 = temp2.getRelative(BlockFace.DOWN);
                        Material mat = temp2.getType();
                        if (mat == Material.OAK_LOG) {
                            event.getPlayer().getInventory()
                                    .addItem(createDropperItem("Basic Dropper", "#a8a8a8", "5"));
                        } else if (mat == Material.LAPIS_BLOCK) {
                            event.getPlayer().getInventory()
                                    .addItem(createDropperItem("Lapis Dropper", "#a8a8a8", "10"));
                        } else if (mat == Material.IRON_BLOCK) {
                            event.getPlayer().getInventory()
                                    .addItem(createDropperItem("Iron Dropper", "#a8a8a8", "15"));
                        } else if (mat == Material.REDSTONE_BLOCK) {
                            event.getPlayer().getInventory()
                                    .addItem(createDropperItem("Redstone Dropper", "#a8a8a8", "20"));
                        } else if (mat == Material.GOLD_BLOCK) {
                            event.getPlayer().getInventory()
                                    .addItem(createDropperItem("Gold Dropper", "#a8a8a8", "25"));
                        } else if (mat == Material.DIAMOND_BLOCK) {
                            event.getPlayer().getInventory()
                                    .addItem(createDropperItem("Diamond Dropper", "#a8a8a8", "30"));
                        } else if (mat == Material.EMERALD_BLOCK) {
                            event.getPlayer().getInventory()
                                    .addItem(createDropperItem("Emerald Dropper", "#a8a8a8", "35"));
                        } else if (mat == Material.WAXED_COPPER_BLOCK) {
                            event.getPlayer().getInventory()
                                    .addItem(createDropperItem("Copper Dropper", "#a8a8a8", "40"));
                        } else if (mat == Material.OXIDIZED_COPPER) {
                            event.getPlayer().getInventory()
                                    .addItem(createDropperItem("Aged Copper Dropper", "#a8a8a8", "45"));
                        } else if (mat == Material.PRISMARINE) {
                            event.getPlayer().getInventory()
                                    .addItem(createDropperItem("Prismarine Dropper", "#a8a8a8", "60"));
                        } else if (mat == Material.PRISMARINE_BRICKS) {
                            event.getPlayer().getInventory()
                                    .addItem(createDropperItem("Advanced Prismarine Dropper", "#a8a8a8", "70"));
                        } else if (mat == Material.DARK_PRISMARINE) {
                            event.getPlayer().getInventory()
                                    .addItem(createDropperItem("Heavy Prismarine Dropper", "#a8a8a8", "80"));
                        } else if (mat == Material.SEA_LANTERN) {
                            event.getPlayer().getInventory()
                                    .addItem(createDropperItem("Crystalised Prismarine Dropper", "#a8a8a8", "90"));
                        } else if (mat == Material.GLOWSTONE) {
                            event.getPlayer().getInventory()
                                    .addItem(createDropperItem("Glowing Dropper", "#a8a8a8", "110"));
                        } else if (mat == Material.COBBLED_DEEPSLATE) {
                            event.getPlayer().getInventory()
                                    .addItem(createDropperItem("Cobbled Dropper", "#a8a8a8", "130"));
                        } else if (mat == Material.AMETHYST_BLOCK) {
                            event.getPlayer().getInventory()
                                    .addItem(createDropperItem("Amethyst Dropper", "#a8a8a8", "150"));
                        } else if (mat == Material.NETHERITE_BLOCK) {
                            event.getPlayer().getInventory()
                                    .addItem(createDropperItem("Netherite Dropper", "#a8a8a8", "180"));
                        } else if (mat == Material.NETHER_BRICKS) {
                            event.getPlayer().getInventory()
                                    .addItem(createDropperItem("Nether Brick Dropper", "#a8a8a8", "210"));
                        } else if (mat == Material.RED_NETHER_BRICKS) {
                            event.getPlayer().getInventory()
                                    .addItem(createDropperItem("Red Nether Brick Dropper", "#a8a8a8", "240"));
                        } else if (mat == Material.CRIMSON_STEM) {
                            event.getPlayer().getInventory()
                                    .addItem(createDropperItem("Crimson Dropper", "#a8a8a8", "270"));
                        } else if (mat == Material.NETHER_WART_BLOCK) {
                            event.getPlayer().getInventory()
                                    .addItem(createDropperItem("Advanced Crimson Dropper", "#a8a8a8", "300"));
                        } else if (mat == Material.WARPED_STEM) {
                            event.getPlayer().getInventory()
                                    .addItem(createDropperItem("Warped Dropper", "#a8a8a8", "330"));
                        } else if (mat == Material.WARPED_WART_BLOCK) {
                            event.getPlayer().getInventory()
                                    .addItem(createDropperItem("Advanced Warped Dropper", "#a8a8a8", "360"));
                        } else if (mat == Material.MAGMA_BLOCK) {
                            event.getPlayer().getInventory()
                                    .addItem(createDropperItem("Molten Dropper", "#a8a8a8", "390"));
                        } else if (mat == Material.SHROOMLIGHT) {
                            event.getPlayer().getInventory()
                                    .addItem(createDropperItem("Shroom Dropper", "#a8a8a8", "420"));
                        } else if (mat == Material.PURPUR_BLOCK) {
                            event.getPlayer().getInventory()
                                    .addItem(createDropperItem("Purpur Dropper", "#a8a8a8", "460"));
                        } else if (mat == Material.PURPUR_PILLAR) {
                            event.getPlayer().getInventory()
                                    .addItem(createDropperItem("Heavy Purpur Dropper", "#a8a8a8", "500"));
                        } else if (mat == Material.CALCITE) {
                            event.getPlayer().getInventory()
                                    .addItem(createDropperItem("Calcite Dropper", "#a8a8a8", "540"));
                        } else if (mat == Material.TUFF) {
                            event.getPlayer().getInventory()
                                    .addItem(createDropperItem("Tuff Dropper", "#a8a8a8", "580"));
                        } else if (mat == Material.RAW_IRON_BLOCK) {
                            event.getPlayer().getInventory()
                                    .addItem(createDropperItem("Raw Iron Dropper", "#a8a8a8", "620"));
                        } else if (mat == Material.RAW_COPPER_BLOCK) {
                            event.getPlayer().getInventory()
                                    .addItem(createDropperItem("Raw Copper Dropper", "#a8a8a8", "660"));
                        } else if (mat == Material.RAW_GOLD_BLOCK) {
                            event.getPlayer().getInventory()
                                    .addItem(createDropperItem("Raw Gold Dropper", "#a8a8a8", "700"));
                        } else if (mat == Material.DRIPSTONE_BLOCK) {
                            event.getPlayer().getInventory()
                                    .addItem(createDropperItem("Ancient Dropper", "#a8a8a8", "900"));
                        } else {
                            return;
                        }
                        temp.setType(Material.AIR);
                        temp2.setType(Material.AIR);
                        temp3.setType(Material.AIR);
                        ArrayList<Integer[]> f = bellLocations.get(i);
                        f.remove(i2);
                        bellLocations.set(i, f);

                    }

                    return;
                }
                i2++;
            }
            i++;
        }
    }

    // Get text from component:
    // PlainTextComponentSerializer.plainText().serialize(component)
    private ArrayList<String> cooldown = new ArrayList<String>();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getHand() == EquipmentSlot.HAND) {
                if (cooldown.contains(player.getName())) {
                    return;
                }
                cooldown.add(player.getName());
                this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {

                    public void run() {
                        cooldown.remove(player.getName());
                    }
                }, 4L);
            } else {
                return;
            }

            if (player.getInventory().getItemInMainHand().hasItemMeta()) {
                if (player.getInventory().getItemInMainHand().getItemMeta().hasLore()) {
                    Block b = event.getClickedBlock().getRelative(event.getBlockFace());
                    if (createConveyorItem("Basic Conveyor", "#a8a8a8", "1").getItemMeta().equals(player
                            .getInventory()
                            .getItemInMainHand().getItemMeta())) {
                        b.setType(Material.BLACK_GLAZED_TERRACOTTA);
                        BlockData bloc = b.getBlockData();
                        ((Directional) bloc).setFacing(player.getFacing().getOppositeFace());
                        b.setBlockData(bloc);
                        ItemStack paper = player.getInventory().getItemInMainHand();
                        paper.setAmount(paper.getAmount() - 1);
                        player.getInventory().setItemInMainHand(paper);
                    } else if (createConveyorItem("Simple Conveyor", "#a8a8a8", "1.6").getItemMeta().equals(player
                            .getInventory().getItemInMainHand().getItemMeta())) {
                        b.setType(Material.RED_GLAZED_TERRACOTTA);
                        BlockData bloc = b.getBlockData();
                        ((Directional) bloc).setFacing(player.getFacing().getOppositeFace());
                        b.setBlockData(bloc);
                        ItemStack paper = player.getInventory().getItemInMainHand();
                        paper.setAmount(paper.getAmount() - 1);
                        player.getInventory().setItemInMainHand(paper);
                    } else if (createConveyorItem("Very Slow Conveyor", "#a8a8a8", "2").getItemMeta().equals(player
                            .getInventory().getItemInMainHand().getItemMeta())) {
                        b.setType(Material.GREEN_GLAZED_TERRACOTTA);
                        BlockData bloc = b.getBlockData();
                        ((Directional) bloc).setFacing(player.getFacing().getOppositeFace());
                        b.setBlockData(bloc);
                        ItemStack paper = player.getInventory().getItemInMainHand();
                        paper.setAmount(paper.getAmount() - 1);
                        player.getInventory().setItemInMainHand(paper);
                    } else if (createConveyorItem("Slow Conveyor", "#a8a8a8", "3").getItemMeta().equals(player
                            .getInventory().getItemInMainHand().getItemMeta())) {
                        b.setType(Material.BROWN_GLAZED_TERRACOTTA);
                        BlockData bloc = b.getBlockData();
                        ((Directional) bloc).setFacing(player.getFacing().getOppositeFace());
                        b.setBlockData(bloc);
                        ItemStack paper = player.getInventory().getItemInMainHand();
                        paper.setAmount(paper.getAmount() - 1);
                        player.getInventory().setItemInMainHand(paper);
                    } else if (createConveyorItem("Ancient Conveyor", "#a8a8a8", "3.6").getItemMeta().equals(player
                            .getInventory().getItemInMainHand().getItemMeta())) {
                        b.setType(Material.BLUE_GLAZED_TERRACOTTA);
                        BlockData bloc = b.getBlockData();
                        ((Directional) bloc).setFacing(player.getFacing().getOppositeFace());
                        b.setBlockData(bloc);
                        ItemStack paper = player.getInventory().getItemInMainHand();
                        paper.setAmount(paper.getAmount() - 1);
                        player.getInventory().setItemInMainHand(paper);
                    } else if (createConveyorItem("Solar Conveyor", "#a8a8a8", "4.4").getItemMeta().equals(player
                            .getInventory().getItemInMainHand().getItemMeta())) {
                        b.setType(Material.PURPLE_GLAZED_TERRACOTTA);
                        BlockData bloc = b.getBlockData();
                        ((Directional) bloc).setFacing(player.getFacing().getOppositeFace());
                        b.setBlockData(bloc);
                        ItemStack paper = player.getInventory().getItemInMainHand();
                        paper.setAmount(paper.getAmount() - 1);
                        player.getInventory().setItemInMainHand(paper);
                    } else if (createConveyorItem("Industrial Conveyor", "#a8a8a8", "5.2").getItemMeta()
                            .equals(player
                                    .getInventory().getItemInMainHand().getItemMeta())) {
                        b.setType(Material.CYAN_GLAZED_TERRACOTTA);
                        BlockData bloc = b.getBlockData();
                        ((Directional) bloc).setFacing(player.getFacing().getOppositeFace());
                        b.setBlockData(bloc);
                        ItemStack paper = player.getInventory().getItemInMainHand();
                        paper.setAmount(paper.getAmount() - 1);
                        player.getInventory().setItemInMainHand(paper);
                    } else if (createConveyorItem("Millitary-Grade Conveyor", "#a8a8a8", "6").getItemMeta()
                            .equals(player
                                    .getInventory().getItemInMainHand().getItemMeta())) {
                        b.setType(Material.LIGHT_GRAY_GLAZED_TERRACOTTA);
                        BlockData bloc = b.getBlockData();
                        ((Directional) bloc).setFacing(player.getFacing().getOppositeFace());
                        b.setBlockData(bloc);
                        ItemStack paper = player.getInventory().getItemInMainHand();
                        paper.setAmount(paper.getAmount() - 1);
                        player.getInventory().setItemInMainHand(paper);
                    } else if (createConveyorItem("Hydraulic Conveyor", "#a8a8a8", "6.8").getItemMeta()
                            .equals(player
                                    .getInventory().getItemInMainHand().getItemMeta())) {
                        b.setType(Material.GRAY_GLAZED_TERRACOTTA);
                        BlockData bloc = b.getBlockData();
                        ((Directional) bloc).setFacing(player.getFacing().getOppositeFace());
                        b.setBlockData(bloc);
                        ItemStack paper = player.getInventory().getItemInMainHand();
                        paper.setAmount(paper.getAmount() - 1);
                        player.getInventory().setItemInMainHand(paper);
                    } else if (createConveyorItem("Nuclear Conveyor", "#a8a8a8", "7.6").getItemMeta().equals(player
                            .getInventory().getItemInMainHand().getItemMeta())) {
                        b.setType(Material.PINK_GLAZED_TERRACOTTA);
                        BlockData bloc = b.getBlockData();
                        ((Directional) bloc).setFacing(player.getFacing().getOppositeFace());
                        b.setBlockData(bloc);
                        ItemStack paper = player.getInventory().getItemInMainHand();
                        paper.setAmount(paper.getAmount() - 1);
                        player.getInventory().setItemInMainHand(paper);
                    } else if (createConveyorItem("Molten Conveyor", "#a8a8a8", "8.4").getItemMeta().equals(player
                            .getInventory().getItemInMainHand().getItemMeta())) {
                        b.setType(Material.ORANGE_GLAZED_TERRACOTTA);
                        BlockData bloc = b.getBlockData();
                        ((Directional) bloc).setFacing(player.getFacing().getOppositeFace());
                        b.setBlockData(bloc);
                        ItemStack paper = player.getInventory().getItemInMainHand();
                        paper.setAmount(paper.getAmount() - 1);
                        player.getInventory().setItemInMainHand(paper);
                    } else if (createConveyorItem("Nuclear Fusion Conveyor", "#a8a8a8", "9.2").getItemMeta()
                            .equals(player
                                    .getInventory().getItemInMainHand().getItemMeta())) {
                        b.setType(Material.MAGENTA_GLAZED_TERRACOTTA);
                        BlockData bloc = b.getBlockData();
                        ((Directional) bloc).setFacing(player.getFacing().getOppositeFace());
                        b.setBlockData(bloc);
                        ItemStack paper = player.getInventory().getItemInMainHand();
                        paper.setAmount(paper.getAmount() - 1);
                        player.getInventory().setItemInMainHand(paper);
                    } else if (createConveyorItem("Plasma Conveyor", "#a8a8a8", "10").getItemMeta().equals(player
                            .getInventory().getItemInMainHand().getItemMeta())) {
                        b.setType(Material.WHITE_GLAZED_TERRACOTTA);
                        BlockData bloc = b.getBlockData();
                        ((Directional) bloc).setFacing(player.getFacing().getOppositeFace());
                        b.setBlockData(bloc);
                        ItemStack paper = player.getInventory().getItemInMainHand();
                        paper.setAmount(paper.getAmount() - 1);
                        player.getInventory().setItemInMainHand(paper);
                    } else if (createConveyorItem("Communist Conveyor", "#a8a8a8", "10.4").getItemMeta()
                            .equals(player
                                    .getInventory().getItemInMainHand().getItemMeta())) {
                        b.setType(Material.LIME_GLAZED_TERRACOTTA);
                        BlockData bloc = b.getBlockData();
                        ((Directional) bloc).setFacing(player.getFacing().getOppositeFace());
                        b.setBlockData(bloc);
                        ItemStack paper = player.getInventory().getItemInMainHand();
                        paper.setAmount(paper.getAmount() - 1);
                        player.getInventory().setItemInMainHand(paper);
                    } else if (createConveyorItem("Superior Communist Conveyor", "#a8a8a8", "11.6")
                            .getItemMeta() == player.getInventory().getItemInMainHand().getItemMeta().lore()) {
                        b.setType(Material.YELLOW_GLAZED_TERRACOTTA);
                        BlockData bloc = b.getBlockData();
                        ((Directional) bloc).setFacing(player.getFacing().getOppositeFace());
                        b.setBlockData(bloc);
                        ItemStack paper = player.getInventory().getItemInMainHand();
                        paper.setAmount(paper.getAmount() - 1);
                        player.getInventory().setItemInMainHand(paper);
                    } else if (createConveyorItem("Godly Communist Conveyor", "#a8a8a8", "13.6").getItemMeta()
                            .equals(player
                                    .getInventory().getItemInMainHand().getItemMeta())) {
                        b.setType(Material.LIGHT_BLUE_GLAZED_TERRACOTTA);
                        BlockData bloc = b.getBlockData();
                        ((Directional) bloc).setFacing(player.getFacing().getOppositeFace());
                        b.setBlockData(bloc);
                        ItemStack paper = player.getInventory().getItemInMainHand();
                        paper.setAmount(paper.getAmount() - 1);
                        player.getInventory().setItemInMainHand(paper);
                    }

                    // Droppers Are Below Here Now

                    else if (createDropperItem("Basic Dropper", "#a8a8a8", "5").getItemMeta()
                            .equals(player
                                    .getInventory().getItemInMainHand().getItemMeta())) {
                        placeDropper(player, b, Material.OAK_LOG);

                    } else if (createDropperItem("Lapis Dropper", "#a8a8a8", "10").getItemMeta()
                            .equals(player
                                    .getInventory().getItemInMainHand().getItemMeta())) {
                        placeDropper(player, b, Material.LAPIS_BLOCK);

                    } else if (createDropperItem("Iron Dropper", "#a8a8a8", "15").getItemMeta()
                            .equals(player
                                    .getInventory().getItemInMainHand().getItemMeta())) {
                        placeDropper(player, b, Material.IRON_BLOCK);

                    } else if (createDropperItem("Redstone Dropper", "#a8a8a8", "20").getItemMeta()
                            .equals(player
                                    .getInventory().getItemInMainHand().getItemMeta())) {
                        placeDropper(player, b, Material.REDSTONE_BLOCK);

                    } else if (createDropperItem("Gold Dropper", "#a8a8a8", "25").getItemMeta()
                            .equals(player
                                    .getInventory().getItemInMainHand().getItemMeta())) {
                        placeDropper(player, b, Material.GOLD_BLOCK);

                    } else if (createDropperItem("Diamond Dropper", "#a8a8a8", "30").getItemMeta()
                            .equals(player
                                    .getInventory().getItemInMainHand().getItemMeta())) {
                        placeDropper(player, b, Material.DIAMOND_BLOCK);

                    } else if (createDropperItem("Emerald Dropper", "#a8a8a8", "35").getItemMeta()
                            .equals(player
                                    .getInventory().getItemInMainHand().getItemMeta())) {
                        placeDropper(player, b, Material.EMERALD_BLOCK);

                    } else if (createDropperItem("Copper Dropper", "#a8a8a8", "40").getItemMeta()
                            .equals(player
                                    .getInventory().getItemInMainHand().getItemMeta())) {
                        placeDropper(player, b, Material.WAXED_COPPER_BLOCK);

                    } else if (createDropperItem("Aged Copper Dropper", "#a8a8a8", "45").getItemMeta()
                            .equals(player
                                    .getInventory().getItemInMainHand().getItemMeta())) {
                        placeDropper(player, b, Material.OXIDIZED_COPPER);

                    } else if (createDropperItem("Prismarine Dropper", "#a8a8a8", "60").getItemMeta()
                            .equals(player
                                    .getInventory().getItemInMainHand().getItemMeta())) {
                        placeDropper(player, b, Material.PRISMARINE);

                    } else if (createDropperItem("Advanced Prismarine Dropper", "#a8a8a8", "70").getItemMeta()
                            .equals(player
                                    .getInventory().getItemInMainHand().getItemMeta())) {
                        placeDropper(player, b, Material.PRISMARINE_BRICKS);

                    } else if (createDropperItem("Heavy Prismarine Dropper", "#a8a8a8", "80").getItemMeta()
                            .equals(player
                                    .getInventory().getItemInMainHand().getItemMeta())) {
                        placeDropper(player, b, Material.DARK_PRISMARINE);

                    } else if (createDropperItem("Crystalised Prismarine Dropper", "#a8a8a8", "90").getItemMeta()
                            .equals(player
                                    .getInventory().getItemInMainHand().getItemMeta())) {
                        placeDropper(player, b, Material.SEA_LANTERN);

                    } else if (createDropperItem("Glowing Dropper", "#a8a8a8", "110").getItemMeta()
                            .equals(player
                                    .getInventory().getItemInMainHand().getItemMeta())) {
                        placeDropper(player, b, Material.GLOWSTONE);

                    } else if (createDropperItem("Cobbled Dropper", "#a8a8a8", "130").getItemMeta()
                            .equals(player
                                    .getInventory().getItemInMainHand().getItemMeta())) {
                        placeDropper(player, b, Material.COBBLED_DEEPSLATE);

                    } else if (createDropperItem("Amethyst Dropper", "#a8a8a8", "150").getItemMeta()
                            .equals(player
                                    .getInventory().getItemInMainHand().getItemMeta())) {
                        placeDropper(player, b, Material.AMETHYST_BLOCK);

                    } else if (createDropperItem("Netherite Dropper", "#a8a8a8", "180").getItemMeta()
                            .equals(player
                                    .getInventory().getItemInMainHand().getItemMeta())) {
                        placeDropper(player, b, Material.NETHERITE_BLOCK);

                    } else if (createDropperItem("Nether Brick Dropper", "#a8a8a8", "210").getItemMeta()
                            .equals(player
                                    .getInventory().getItemInMainHand().getItemMeta())) {
                        placeDropper(player, b, Material.NETHER_BRICKS);

                    } else if (createDropperItem("Red Nether Brick Dropper", "#a8a8a8", "240").getItemMeta()
                            .equals(player
                                    .getInventory().getItemInMainHand().getItemMeta())) {
                        placeDropper(player, b, Material.RED_NETHER_BRICKS);

                    } else if (createDropperItem("Crimson Dropper", "#a8a8a8", "270").getItemMeta()
                            .equals(player
                                    .getInventory().getItemInMainHand().getItemMeta())) {
                        placeDropper(player, b, Material.CRIMSON_STEM);

                    } else if (createDropperItem("Advanced Crimson Dropper", "#a8a8a8", "300").getItemMeta()
                            .equals(player
                                    .getInventory().getItemInMainHand().getItemMeta())) {
                        placeDropper(player, b, Material.NETHER_WART_BLOCK);

                    } else if (createDropperItem("Warped Dropper", "#a8a8a8", "330").getItemMeta()
                            .equals(player
                                    .getInventory().getItemInMainHand().getItemMeta())) {
                        placeDropper(player, b, Material.WARPED_STEM);

                    } else if (createDropperItem("Advanced Warped Dropper", "#a8a8a8", "360").getItemMeta()
                            .equals(player
                                    .getInventory().getItemInMainHand().getItemMeta())) {
                        placeDropper(player, b, Material.WARPED_WART_BLOCK);

                    } else if (createDropperItem("Molten Dropper", "#a8a8a8", "390").getItemMeta()
                            .equals(player
                                    .getInventory().getItemInMainHand().getItemMeta())) {
                        placeDropper(player, b, Material.MAGMA_BLOCK);

                    } else if (createDropperItem("Shroom Dropper", "#a8a8a8", "420").getItemMeta()
                            .equals(player
                                    .getInventory().getItemInMainHand().getItemMeta())) {
                        placeDropper(player, b, Material.SHROOMLIGHT);

                    } else if (createDropperItem("Purpur Dropper", "#a8a8a8", "460").getItemMeta()
                            .equals(player
                                    .getInventory().getItemInMainHand().getItemMeta())) {
                        placeDropper(player, b, Material.PURPUR_BLOCK);

                    } else if (createDropperItem("Heavy Purpur Dropper", "#a8a8a8", "500").getItemMeta()
                            .equals(player
                                    .getInventory().getItemInMainHand().getItemMeta())) {
                        placeDropper(player, b, Material.PURPUR_PILLAR);

                    } else if (createDropperItem("Calcite Dropper", "#a8a8a8", "540").getItemMeta()
                            .equals(player
                                    .getInventory().getItemInMainHand().getItemMeta())) {
                        placeDropper(player, b, Material.CALCITE);

                    } else if (createDropperItem("Tuff Dropper", "#a8a8a8", "580").getItemMeta()
                            .equals(player
                                    .getInventory().getItemInMainHand().getItemMeta())) {
                        placeDropper(player, b, Material.TUFF);

                    } else if (createDropperItem("Raw Iron Dropper", "#a8a8a8", "620").getItemMeta()
                            .equals(player
                                    .getInventory().getItemInMainHand().getItemMeta())) {
                        placeDropper(player, b, Material.RAW_IRON_BLOCK);

                    } else if (createDropperItem("Raw Copper Dropper", "#a8a8a8", "660").getItemMeta()
                            .equals(player
                                    .getInventory().getItemInMainHand().getItemMeta())) {
                        placeDropper(player, b, Material.RAW_COPPER_BLOCK);

                    } else if (createDropperItem("Raw Gold Dropper", "#a8a8a8", "700").getItemMeta()
                            .equals(player
                                    .getInventory().getItemInMainHand().getItemMeta())) {
                        placeDropper(player, b, Material.RAW_GOLD_BLOCK);

                    } else if (createDropperItem("Ancient Dropper", "#a8a8a8", "900").getItemMeta()
                            .equals(player
                                    .getInventory().getItemInMainHand().getItemMeta())) {
                        placeDropper(player, b, Material.DRIPSTONE_BLOCK);

                    }

                }
            }
            // Detect Right Click Of Dropper

            int i = 0;
            
            for (ArrayList<Integer[]> item : bellLocations) {
                Location location = event.getClickedBlock().getLocation();

                location = getLocationIfValid(event.getClickedBlock(), Material.BELL, BlockFace.EAST);
                if (location == null) {
                    location = getLocationIfValid(event.getClickedBlock(), Material.BELL, BlockFace.WEST);
                }
                if (location == null) {
                    location = getLocationIfValid(event.getClickedBlock(), Material.BELL, BlockFace.SOUTH);
                }
                if (location == null) {
                    location = getLocationIfValid(event.getClickedBlock(), Material.BELL, BlockFace.NORTH);
                }
                
                Block facing = event.getClickedBlock().getRelative(BlockFace.UP);
                for (BlockFace facingDirection : new BlockFace[]{BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH, BlockFace.NORTH}) {
                    Location tempLocation = getLocationIfFacingBell(facing, facingDirection);
                    if (tempLocation != null) {
                        location = tempLocation;
                        break;
                    }
                }
                

                Integer[] x = { location.getBlockX(), location.getBlockY(), location.getBlockZ() };
                for (Integer[] coords : item) {
                    if (Arrays.equals(coords, x)) {
                        // EXECUTE GUI OPEN
                        if (playerName.get(i) == event.getPlayer().getName()) {
                            Block temp = location.getBlock();
                            BlockData bd = temp.getBlockData();
                            Directional direction = (Directional) bd;
                            Block temp2 = temp.getRelative(direction.getFacing());
                            Material mat = temp2.getType();
    String name = null;
    int price = 0;
    switch (mat) {
        case OAK_LOG:
            name = "Lapis";
            price = 200;
            break;
        case LAPIS_BLOCK:
            name = "Iron";
            price = 300;
            break;
        case IRON_BLOCK:
            name = "Redstone";
            price = 600;
            break;
        case REDSTONE_BLOCK:
            name = "Gold";
            price = 1000;
            break;
        case GOLD_BLOCK:
            name = "Diamond";
            price = 1500;
            break;
        case DIAMOND_BLOCK:
            name = "Emerald";
            price = 3000;
            break;
        case WAXED_COPPER_BLOCK:
            name = "Copper";
            price = 5000;
            break;
        case OXIDIZED_COPPER:
            name = "Ancient Copper";
            price = 7500;
            break;
        case PRISMARINE:
            name = "Prismarine";
            price = 8300;
            break;
        case PRISMARINE_BRICKS:
            name = "Advanced Prismarine";
            price = 10000;
            break;
        case DARK_PRISMARINE:
            name = "Heavy Prismarine";
            price = 13000;
            break;
        case SEA_LANTERN:
            name = "Crystalised Prismarine";
            price = 16000;
            break;
        case GLOWSTONE:
            name = "Glowing";
            price = 20000;
            break;
        case COBBLED_DEEPSLATE:
            name = "Cobbled";
            price = 24000;
            break;
        case AMETHYST_BLOCK:
            name = "Amethyst";
            price = 29000;
            break;
        case NETHERITE_BLOCK:
            name = "Netherite";
            price = 34000;
            break;
        case NETHER_BRICKS:
            name = "Nether Brick";
            price = 38000;
            break;
        case RED_NETHER_BRICKS:
            name = "Red Nether Brick";
            price = 42000;
            break;
            case CRIMSON_STEM:
            name = "Advanced Crimson";
            price = 50000;
            break;
            case NETHER_WART_BLOCK:
            name = "Warped";
            price = 54000;
            break;
            case WARPED_STEM:
                                name = "Advanced Warped";price = 5900; break;
            case WARPED_WART_BLOCK:
                                name = "Molten";price = 64000; break;
            case MAGMA_BLOCK:
                                name = "Shroom";price = 70000; break;
            case SHROOMLIGHT:
                                name = "Purpur";price = 90000; break;
            case PURPUR_BLOCK:
                                name = "Advanced Purpur";price = 130000; break;
            case PURPUR_PILLAR:
            name = "Calcite";price = 240000; break;
            case CALCITE:
            name = "Tuff";price = 320000; break;
            case TUFF:
            name = "Raw Iron";price = 400000; break;
            case RAW_IRON_BLOCK:
            name = "Raw Copper";price = 500000; break;
            case RAW_COPPER_BLOCK:
            name = "Raw Gold";price = 1000000; break;
            case RAW_GOLD_BLOCK:
            name = "Ancient";price = 10000000; break;
        default:
            break;
    }
    if(name != null) {
    MyGUIs.rankedInventory(Material.DRIPSTONE_BLOCK, name, econ, price, temp2)
                                        .open(player);
    }

                        }

                        return;
                    }
                }
                i++;
            }

            // End Of Detector
        }
        return;
    }

    public Pair<Integer, ArrayList<Integer>> createSellItem(String name, Material mat, PlayerInventory inv) {
        ItemStack preItem = new ItemStack(mat, 1);
        ItemMeta meta = preItem.getItemMeta();
        meta.displayName(
                Component
                        .text(nonItalic("Mined "
                                + name))
                        .color(TextColor.fromHexString("#8e8e8e")));
        preItem.setItemMeta(meta);
        ArrayList<Integer> keys = new ArrayList<Integer>();
        ArrayList<Integer> sellValue = new ArrayList<Integer>();
        inv.all(mat).forEach((key, value) -> {
            Integer tempSellValue = 0;
            getLogger().info("I am the iteration! \"" + key + "\"");
            getLogger().info("I am the ItemStack! \"" + value + "\"");
            if (preItem.getItemMeta().equals(value.getItemMeta())) {
                Map<Material, Integer> valueMap = new HashMap<>();
                valueMap.put(Material.OAK_PLANKS, 5);
                valueMap.put(Material.LAPIS_LAZULI, 10);
                valueMap.put(Material.IRON_INGOT, 15);
                valueMap.put(Material.REDSTONE, 20);
                valueMap.put(Material.GOLD_INGOT, 25);
                valueMap.put(Material.DIAMOND, 30);
                valueMap.put(Material.EMERALD, 35);
                valueMap.put(Material.COPPER_INGOT, 40);
                valueMap.put(Material.RAW_COPPER, 45);
                valueMap.put(Material.PRISMARINE_SHARD, 60);
                valueMap.put(Material.PRISMARINE_CRYSTALS, 90);
                valueMap.put(Material.GLOWSTONE_DUST, 110);
                valueMap.put(Material.COBBLED_DEEPSLATE, 130);
                valueMap.put(Material.AMETHYST_SHARD, 150);
                valueMap.put(Material.NETHERITE_INGOT, 180);
                valueMap.put(Material.NETHER_BRICK, 210);
                valueMap.put(Material.CRIMSON_FUNGUS, 270);
                valueMap.put(Material.WARPED_FUNGUS, 330);
                valueMap.put(Material.MAGMA_CREAM, 390);
                valueMap.put(Material.RED_MUSHROOM, 420);
                valueMap.put(Material.PURPLE_DYE, 460);
                valueMap.put(Material.CALCITE, 540);
                valueMap.put(Material.TUFF, 580);
                valueMap.put(Material.RAW_IRON, 620);
                valueMap.put(Material.RAW_GOLD, 700);
                valueMap.put(Material.DRIPSTONE_BLOCK, 900);
                int itemValue = valueMap.getOrDefault(value.getType(), 0) * value.getAmount();
                tempSellValue += itemValue;
                keys.add(key);
            }
            sellValue.add(tempSellValue);

        });
        Integer total = 0;
        for (Integer val : sellValue) {
            total += val;
        }
        Pair<Integer, ArrayList<Integer>> pair = Pair.of(total, keys);
        return pair;
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
            World world = Bukkit.getWorld("world");
            Collection<Item> items = world.getEntitiesByClass(Item.class);
            /* Bukkit.getScheduler().runTaskTimer(this, () -> { */while (items.iterator().hasNext()) {
                items.iterator().next().setVelocity(items.iterator().next().getVelocity().setZ(-5));
            } /* },100 , 200); */
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
        if (cmd.getName().equalsIgnoreCase("sell") && sender instanceof Player) {
            Player player = (Player) sender;
            PlayerInventory inv = player.getInventory();
            inv.all(Material.PURPLE_WOOL).forEach((key, value) -> {
                getLogger().info("I am the key! \"" + key + "\"");
                getLogger().info("I am the value! \"" + value + "\"");
            });
            Integer total = 0;
            ArrayList<Pair<Integer, ArrayList<Integer>>> items = new ArrayList<Pair<Integer, ArrayList<Integer>>>();
            items.add(createSellItem("Wood", Material.OAK_PLANKS, inv));
            items.add(createSellItem("Lapis", Material.LAPIS_LAZULI, inv));
            items.add(createSellItem("Iron", Material.IRON_INGOT, inv));
            items.add(createSellItem("Redstone", Material.REDSTONE, inv));
            items.add(createSellItem("Gold", Material.GOLD_INGOT, inv));
            items.add(createSellItem("Diamond", Material.DIAMOND, inv));
            items.add(createSellItem("Emerald", Material.EMERALD, inv));
            items.add(createSellItem("Copper", Material.COPPER_INGOT, inv));
            items.add(createSellItem("Aged Copper", Material.RAW_COPPER, inv));
            items.add(createSellItem("Prismarine", Material.PRISMARINE_SHARD, inv));
            items.add(createSellItem("Advanced Prismarine", Material.PRISMARINE_SHARD, inv));
            items.add(createSellItem("Heavy Prismarine", Material.PRISMARINE_SHARD, inv));
            items.add(createSellItem("Crystalized Prismarine", Material.PRISMARINE_CRYSTALS, inv));
            items.add(createSellItem("Glowstone", Material.GLOWSTONE_DUST, inv));
            items.add(createSellItem("Deepslate", Material.COBBLED_DEEPSLATE, inv));
            items.add(createSellItem("Amethyst", Material.AMETHYST_SHARD, inv));
            items.add(createSellItem("Netherite", Material.NETHERITE_INGOT, inv));
            items.add(createSellItem("Bricks", Material.NETHER_BRICK, inv));
            items.add(createSellItem("Red Bricks", Material.NETHER_BRICK, inv));
            items.add(createSellItem("Crimson", Material.CRIMSON_FUNGUS, inv));
            items.add(createSellItem("Advanced Crimson", Material.CRIMSON_FUNGUS, inv));
            items.add(createSellItem("Warped", Material.WARPED_FUNGUS, inv));
            items.add(createSellItem("Advanced Warped", Material.WARPED_FUNGUS, inv));
            items.add(createSellItem("Magma", Material.MAGMA_CREAM, inv));
            items.add(createSellItem("Mushroom", Material.RED_MUSHROOM, inv));
            items.add(createSellItem("Purpur", Material.PURPLE_DYE, inv));
            items.add(createSellItem("Advanced Purpur", Material.PURPLE_DYE, inv));
            items.add(createSellItem("Calcite", Material.CALCITE, inv));
            items.add(createSellItem("Tuff", Material.TUFF, inv));
            items.add(createSellItem("Raw Iron", Material.RAW_IRON, inv));
            items.add(createSellItem("Raw Copper", Material.RAW_COPPER, inv));
            items.add(createSellItem("Raw Gold", Material.RAW_GOLD, inv));
            items.add(createSellItem("Ancient Ore", Material.DRIPSTONE_BLOCK, inv));
            items.add(createSellItem("null", Material.LIGHT_BLUE_WOOL, inv));
            for (Pair<Integer, ArrayList<Integer>> item : items) {
                for (Integer key : item.second()) {
                    inv.setItem(key, new ItemStack(Material.AIR));
                }
                total += item.first();
            }
            EconomyResponse r = econ.depositPlayer(player, total);
            if (r.transactionSuccess()) {
                sender.sendMessage(Component.text("Sold Mined Items For ").color(TextColor.fromHexString("#2fad37"))
                        .append(Component.text(econ.format(r.amount)).color(TextColor.fromHexString("#FF5555"))));
                // Below Here Is Debug
                // End Of Debug

            } else {
                sender.sendMessage(String.format("An error occured: %s", r.errorMessage));
            }
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("blank") && sender instanceof Player) {
            Player player = (Player) sender;
            reloadConfig();
            blockSpeedConfig = YamlConfiguration.loadConfiguration(file);

            player.sendMessage("Configuration Has Been Reloaded.");
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("buydropper") && sender instanceof Player) {
            Player player = (Player) sender;
            MyGUIs.mainDropperBuyInventory(createConveyorItem("Basic Conveyor", "#a8a8a8", "1"),
                    createDropperItem("Basic Dropper", "#a8a8a8", "5"), econ).open(player);
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("giveitems") && sender instanceof Player) {
            Player player = (Player) sender;
            if (player.hasPermission("blankplugin.debug")) {
                player.getInventory().addItem(createConveyorItem("Basic Conveyor", "#a8a8a8", "1"));
                player.getInventory().addItem(createConveyorItem("Simple Conveyor", "#a8a8a8", "1.6"));
                player.getInventory().addItem(createConveyorItem("Very Slow Conveyor", "#a8a8a8", "2"));
                player.getInventory().addItem(createConveyorItem("Slow Conveyor", "#a8a8a8", "3"));
                player.getInventory().addItem(createConveyorItem("Ancient Conveyor", "#a8a8a8", "3.6"));
                player.getInventory().addItem(createConveyorItem("Solar Conveyor", "#a8a8a8", "4.4"));
                player.getInventory().addItem(createConveyorItem("Industrial Conveyor", "#a8a8a8", "5.2"));
                player.getInventory().addItem(createConveyorItem("Millitary-Grade Conveyor", "#a8a8a8", "6"));
                player.getInventory().addItem(createConveyorItem("Hydraulic Conveyor", "#a8a8a8", "6.8"));
                player.getInventory().addItem(createConveyorItem("Nuclear Conveyor", "#a8a8a8", "7.6"));
                player.getInventory().addItem(createConveyorItem("Molten Conveyor", "#a8a8a8", "8.4"));
                player.getInventory().addItem(createConveyorItem("Nuclear Fusion Conveyor", "#a8a8a8", "9.2"));
                player.getInventory().addItem(createConveyorItem("Plasma Conveyor", "#a8a8a8", "10"));
                player.getInventory().addItem(createConveyorItem("Communist Conveyor", "#a8a8a8", "10.4"));
                player.getInventory().addItem(createConveyorItem("Superior Communist Conveyor", "#a8a8a8", "11.6"));
                player.getInventory().addItem(createConveyorItem("Godly Communist Conveyor", "#a8a8a8", "13.6"));

                player.sendMessage("Given Player Item");
            } else {
                player.sendMessage(ChatColor.RED + "Insufficient Permissions");
            }
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("givedroppers") && sender instanceof Player) {
            Player player = (Player) sender;
            if (player.hasPermission("blankplugin.debug")) {
                player.getInventory().addItem(createDropperItem("Basic Dropper", "#a8a8a8", "5"));
                player.getInventory().addItem(createDropperItem("Lapis Dropper", "#a8a8a8", "10"));
                player.getInventory().addItem(createDropperItem("Iron Dropper", "#a8a8a8", "15"));
                player.getInventory().addItem(createDropperItem("Redstone Dropper", "#a8a8a8", "20"));
                player.getInventory().addItem(createDropperItem("Gold Dropper", "#a8a8a8", "25"));
                player.getInventory().addItem(createDropperItem("Diamond Dropper", "#a8a8a8", "30"));
                player.getInventory().addItem(createDropperItem("Emerald Dropper", "#a8a8a8", "35"));
                player.getInventory().addItem(createDropperItem("Copper Dropper", "#a8a8a8", "40"));
                player.getInventory().addItem(createDropperItem("Aged Copper Dropper", "#a8a8a8", "45"));
                player.getInventory().addItem(createDropperItem("Prismarine Dropper", "#a8a8a8", "60"));
                player.getInventory().addItem(createDropperItem("Advanced Prismarine Dropper", "#a8a8a8", "70"));
                player.getInventory().addItem(createDropperItem("Heavy Prismarine Dropper", "#a8a8a8", "80"));
                player.getInventory().addItem(createDropperItem("Crystalised Prismarine Dropper", "#a8a8a8", "90"));
                player.getInventory().addItem(createDropperItem("Glowing Dropper", "#a8a8a8", "110"));
                player.getInventory().addItem(createDropperItem("Cobbled Dropper", "#a8a8a8", "130"));
                player.getInventory().addItem(createDropperItem("Amethyst Dropper", "#a8a8a8", "150"));
                player.getInventory().addItem(createDropperItem("Netherite Dropper", "#a8a8a8", "180"));
                player.getInventory().addItem(createDropperItem("Nether Brick Dropper", "#a8a8a8", "210"));
                player.getInventory().addItem(createDropperItem("Red Nether Brick Dropper", "#a8a8a8", "240"));
                player.getInventory().addItem(createDropperItem("Crimson Dropper", "#a8a8a8", "270"));
                player.getInventory().addItem(createDropperItem("Advanced Crimson Dropper", "#a8a8a8", "300"));
                player.getInventory().addItem(createDropperItem("Warped Dropper", "#a8a8a8", "330"));
                player.getInventory().addItem(createDropperItem("Advanced Warped Dropper", "#a8a8a8", "360"));
                player.getInventory().addItem(createDropperItem("Molten Dropper", "#a8a8a8", "390"));
                player.getInventory().addItem(createDropperItem("Shroom Dropper", "#a8a8a8", "420"));
                player.getInventory().addItem(createDropperItem("Purpur Dropper", "#a8a8a8", "460"));
                player.getInventory().addItem(createDropperItem("Heavy Purpur Dropper", "#a8a8a8", "500"));
                player.getInventory().addItem(createDropperItem("Calcite Dropper", "#a8a8a8", "540"));
                player.getInventory().addItem(createDropperItem("Tuff Dropper", "#a8a8a8", "580"));
                player.getInventory().addItem(createDropperItem("Raw Iron Dropper", "#a8a8a8", "620"));
                player.getInventory().addItem(createDropperItem("Raw Copper Dropper", "#a8a8a8", "660"));
                player.getInventory().addItem(createDropperItem("Raw Gold Dropper", "#a8a8a8", "700"));
                player.getInventory().addItem(createDropperItem("Ancient Dropper", "#a8a8a8", "900"));

                player.sendMessage("Given Player Item");
            } else {
                player.sendMessage(ChatColor.RED + "Insufficient Permissions");
            }
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("spawn") && sender instanceof Player) {
            Player player = (Player) sender;
            String day = (String) spawnLocations.get("day");
            Date d = new Date();
            Calendar cal = Calendar.getInstance();
            cal.setTime(d);
            Integer date = cal.get(Calendar.DAY_OF_WEEK);
            if (Integer.parseInt(day) != date) {
                int maxIndex = spawnLocations.getKeys(true).size() - 2;
                Random rand = new Random();
                int n = rand.nextInt(maxIndex);
                spawnLocations.set("day", String.valueOf(date));
                try {
                    spawnLocations.save(f);
                } catch (IOException e) {
                    getLogger().warning("Unable to save Spawn Configuration."); // shouldn't really happen, but save
                                                                                // throws the exception
                }
                String currSpawnStr = (String) spawnLocations.get(String.valueOf(n));
                String[] currSpawn = currSpawnStr.split(",");
                currentSpawn.set(0, Integer.parseInt(currSpawn[0]));
                currentSpawn.set(1, Integer.parseInt(currSpawn[1]));
                currentSpawn.set(2, Integer.parseInt(currSpawn[2]));
                currentSpawn.set(3, Integer.parseInt(currSpawn[3]));

            }
            World world = Bukkit.getWorld("testingWorld");
            Location loc = new Location(world, currentSpawn.get(0), currentSpawn.get(1), currentSpawn.get(2));
            loc.setPitch(currentSpawn.get(3));
            player.teleport(loc);
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("setspawn") && sender instanceof Player) {
            Player player = (Player) sender;
            if (player.hasPermission("blankplugin.setspawn")) {
                Set<String> keys = spawnLocations.getKeys(true);
                keys.size();
                // keys.forEach((String key) -> {
                // System.out.println(name);
                // });
                Location loc = player.getLocation();
                double facing = loc.getPitch();
                int face = 0;
                if (facing > 135 && facing > -134.9) {
                    face = 180;// NORTH
                }
                if (facing > -135 && facing > -44.9) {
                    face = -90;// EAST
                }
                if (facing > -45 && facing < 44.9) {
                    face = 0;// SOUTH
                } else {
                    face = 90;// WEST
                }
                spawnLocations.set(String.valueOf(keys.size() - 2),
                        String.valueOf(loc.getBlockX()) + "," + String.valueOf(loc.getBlockY()) + ","
                                + String.valueOf(loc.getBlockZ()) + "," + String.valueOf(face));
                try {
                    spawnLocations.save(f);
                } catch (IOException e) {
                    getLogger().warning("Unable to save Spawn Configuration."); // shouldn't really happen, but save
                                                                                // throws the exception
                }

            } else {
                player.sendMessage(ChatColor.RED + "Insufficient Permissions");
            }
        }

        return false;
    }

    class EntitySearch implements Runnable {
        @Override
        public void run() {
            for (World world : getServer().getWorlds()) {
                List<Player> players = world.getPlayers();
                // No players in this world
                if (players.isEmpty()) {
                    continue;
                }

                for (Entity entity : world.getEntitiesByClass(Entity.class)) {
                    Location entityLocation = entity.getLocation();

                    if (entity.isDead()) {
                        continue;
                    }

                    Block blockUnder = entityLocation.getBlock().getRelative(BlockFace.DOWN);

                    Material mat = blockUnder.getType();
                    if (mat != Material.BLACK_GLAZED_TERRACOTTA &&
                            mat != Material.BLUE_GLAZED_TERRACOTTA &&
                            mat != Material.BROWN_GLAZED_TERRACOTTA &&
                            mat != Material.CYAN_GLAZED_TERRACOTTA &&
                            mat != Material.GRAY_GLAZED_TERRACOTTA &&
                            mat != Material.GREEN_GLAZED_TERRACOTTA &&
                            mat != Material.LIGHT_BLUE_GLAZED_TERRACOTTA &&
                            mat != Material.LIGHT_GRAY_GLAZED_TERRACOTTA &&
                            mat != Material.LIME_GLAZED_TERRACOTTA &&
                            mat != Material.MAGENTA_GLAZED_TERRACOTTA &&
                            mat != Material.ORANGE_GLAZED_TERRACOTTA &&
                            mat != Material.PINK_GLAZED_TERRACOTTA &&
                            mat != Material.PURPLE_GLAZED_TERRACOTTA &&
                            mat != Material.RED_GLAZED_TERRACOTTA &&
                            mat != Material.WHITE_GLAZED_TERRACOTTA &&
                            mat != Material.YELLOW_GLAZED_TERRACOTTA) {
                        continue;
                    }

                    if (entity instanceof Player) {
                        Player player = (Player) entity;
                        // Pressing shift stops the movement
                        if (player.isSneaking()) {
                            continue;
                        }
                    }

                    BlockData blockData = blockUnder.getBlockData();
                    Directional direction = (Directional) blockData;
                    Vector entityDirection = entity.getVelocity();
                    double VELOCITY = blockSpeedConfig.getDouble(mat.name());
                    switch (direction.getFacing()) {
                        case NORTH:
                            entityDirection.setZ(VELOCITY);
                            break;
                        case SOUTH:
                            entityDirection.setZ(-VELOCITY);
                            break;
                        case WEST:
                            entityDirection.setX(VELOCITY);
                            break;
                        case EAST:
                            entityDirection.setX(-VELOCITY);
                            break;
                        default:
                            //
                            break;
                    }

                    entity.setVelocity(entityDirection);
                }
            }
        }
    }
}