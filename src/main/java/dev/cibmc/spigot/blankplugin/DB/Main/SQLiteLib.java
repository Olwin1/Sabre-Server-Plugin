// 
// Decompiled by Procyon v0.5.36
// 

package dev.cibmc.spigot.blankplugin.DB.Main;

import dev.cibmc.spigot.blankplugin.DB.Database.SQLite;
import org.bukkit.plugin.Plugin;
import java.util.HashMap;
import dev.cibmc.spigot.blankplugin.DB.Database.Database;
import java.util.Map;
import org.bukkit.plugin.java.JavaPlugin;

public class SQLiteLib extends JavaPlugin {
    private static SQLiteLib INSTANCE;
    private Map<String, Database> databases;

    public SQLiteLib() {
        this.databases = new HashMap<String, Database>();
        getLogger().info("2");

    }

    public void onEnable() {
        this.getDataFolder().mkdirs();
        SQLiteLib.INSTANCE = this;
        getLogger().info("3");
    }

    public void onDisable() {
    }

    public static SQLiteLib getInstance() {
        return SQLiteLib.INSTANCE;
    }

    public static SQLiteLib hookSQLiteLib(final Plugin hostPlugin) {
        final SQLiteLib plugin = new SQLiteLib();
        return plugin;
    }

    public void initializeDatabase() {
        getLogger().info("6");
        //final Database db = new SQLite("blankplugin", "CREATE TABLE IF NOT EXISTS droppers", this.getDataFolder());
        getLogger().info("7");
        //db.load();
        //this.databases.put("blankplugin", db);
    }

    public void initializeDatabase(final Plugin plugin, final String databaseName, final String createStatement) {
        getLogger().info("8");
        final Database db = new SQLite(databaseName, createStatement, plugin.getDataFolder());
        getLogger().info("9");
        db.load();
        getLogger().info("10");
        this.databases.put(databaseName, db);
        getLogger().info("11");
    }

    public Map<String, Database> getDatabases() {
        getLogger().info("12");
        return this.databases;
    }

    public Database getDatabase(final String databaseName) {
        getLogger().info("1");
        return this.getDatabases().get(databaseName);
    }
}
