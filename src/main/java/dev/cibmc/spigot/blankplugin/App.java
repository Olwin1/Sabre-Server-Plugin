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
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.util.Random;
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
import java.util.HashMap;
import dev.cibmc.spigot.blankplugin.DB.Database.Database;
import java.util.Map;
import org.bukkit.event.block.BlockBreakEvent;

public class App extends JavaPlugin implements Listener {
    File file = new File("./config/", "blockSpeedConfig.yml");
    YamlConfiguration blockSpeedConfig = YamlConfiguration.loadConfiguration(file);
    private Map<String, Database> databases = new HashMap<String, Database>();
    public static App INSTANCE;


    static ArrayList<String> playerName = new ArrayList<String>();
    static ArrayList<ArrayList<Integer[]>> bellLocations = new ArrayList<ArrayList<Integer[]>>();

    @Override
    public void onEnable() {
        INSTANCE = this;
        this.getDataFolder().mkdirs();
        initializeDatabase("blankplugin",
                "CREATE TABLE IF NOT EXISTS gens (uuid VARCHAR(36), gen_x INTEGER[], gen_y INTEGER[], gen_z INTEGER[]);");

        Collection<? extends Player> players = this.getServer().getOnlinePlayers();
        for (Player player : players) {
            playerName.add(player.getName());
            ArrayList<Integer[]> inner = new ArrayList<Integer[]>();
            bellLocations.add(inner);
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

    static void placeDropper(Player player, Block b, Material mat) {
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
        getLogger().info("See you again, SpigotMC!");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMoveEvent(PlayerMoveEvent event) {
        // will check if the player is in the portal or not.
        Player player = event.getPlayer();
        Location loc = player.getLocation();
        if (-34 < loc.getX() && loc.getX() < -29) {
            double y = loc.getY();
            if (-317 < loc.getZ() && loc.getZ() < -316) {

                if (-4 <= y && 0 > y) {

                    double end = y - Math.floor(y);
                    loc.setY(-22 + end);
                    Vector velo = player.getVelocity();
                    player.teleport(loc);
                    player.setVelocity(velo);
                }

            } else if (-314 < loc.getZ() && loc.getZ() < -312) {

                if (-22 <= y && -19 > y) {

                    double end = y - Math.floor(y);
                    loc.setY(-4 + end);
                    Vector velo = player.getVelocity();
                    player.teleport(loc);
                    player.setVelocity(velo);
                }

            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
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

            else if (tempBlockE.getType() == Material.BELL) {
                Directional direction = (Directional) tempBlockE.getBlockData();
                if (direction.getFacing() == BlockFace.EAST) {
                    location = tempBlockE.getLocation();
                }
            }

            else if (tempBlockN.getType() == Material.BELL) {
                Directional direction = (Directional) tempBlockN.getBlockData();
                if (direction.getFacing() == BlockFace.NORTH) {
                    location = tempBlockN.getLocation();
                }
            }

            else if (tempBlockS.getType() == Material.BELL) {
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

            else if (tempBlockE.getType() == Material.BELL) {
                Directional direction = (Directional) tempBlockE.getBlockData();
                if (direction.getFacing() == BlockFace.EAST) {
                    location = tempBlockE.getLocation();
                }
            }

            else if (tempBlockN.getType() == Material.BELL) {
                Directional direction = (Directional) tempBlockN.getBlockData();
                if (direction.getFacing() == BlockFace.NORTH) {
                    location = tempBlockN.getLocation();
                }
            }

            else if (tempBlockS.getType() == Material.BELL) {
                Directional direction = (Directional) tempBlockS.getBlockData();
                if (direction.getFacing() == BlockFace.SOUTH) {
                    location = tempBlockS.getLocation();
                }
            }

            Integer[] x = { location.getBlockX(), location.getBlockY(), location.getBlockZ() };
            for (Integer[] coords : item) {
                if (Arrays.equals(coords, x)) {

                    event.setCancelled(true);
                    return;
                }
            }

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

                    else if (createDropperItem("Basic Dropper", "#a8a8a8", "$13.6").getItemMeta()
                            .equals(player
                                    .getInventory().getItemInMainHand().getItemMeta())) {
                        placeDropper(player, b, Material.LIGHT_BLUE_WOOL);

                    }

                }
            }
        }
        return;
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
        if (cmd.getName().equalsIgnoreCase("blank") && sender instanceof Player) {
            Player player = (Player) sender;
            reloadConfig();
            blockSpeedConfig = YamlConfiguration.loadConfiguration(file);

            player.sendMessage("Configuration Has Been Reloaded.");
        }
        if (cmd.getName().equalsIgnoreCase("giveitems") && sender instanceof Player) {
            Player player = (Player) sender;
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
            player.getInventory().addItem(createDropperItem("Basic Dropper", "#a8a8a8", "$13.6"));

            player.sendMessage("Given Player Item");
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