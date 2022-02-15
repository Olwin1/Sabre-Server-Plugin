// 
// Decompiled by Procyon v0.5.36
// 

package dev.cibmc.spigot.blankplugin.DB.Database;

import java.sql.Statement;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.io.IOException;
import java.util.logging.Level;
import dev.cibmc.spigot.blankplugin.App;
import java.sql.Connection;
import java.io.File;

public class SQLite extends Database
{
    private String dbname;
    private String createTestTable;
    private String customCreateString;
    private File dataFolder;
    
    public SQLite(final String databaseName, final String createStatement, final File folder) {
        this.createTestTable = "CREATE TABLE IF NOT EXISTS test (`test` varchar(32) NOT NULL,PRIMARY KEY (`test`));";
        this.dbname = databaseName;
        this.customCreateString = createStatement;
        this.dataFolder = folder;
    }
    
    @Override
    public Connection getSQLConnection() {
        final File folder = new File(this.dataFolder, String.valueOf(this.dbname) + ".db");
        if (!folder.exists()) {
            try {
                folder.createNewFile();
            }
            catch (IOException e) {
                App.getInstance().getLogger().log(Level.SEVERE, "File write error: " + this.dbname + ".db");
            }
        }
        try {
            if (this.connection != null && !this.connection.isClosed()) {
                return this.connection;
            }
            Class.forName("org.sqlite.JDBC");
            return this.connection = DriverManager.getConnection("jdbc:sqlite:" + folder);
        }
        catch (SQLException ex) {
            App.getInstance().getLogger().log(Level.SEVERE, "SQLite exception on initialize", ex);
        }
        catch (ClassNotFoundException ex2) {
            App.getInstance().getLogger().log(Level.SEVERE, "You need the SQLite JBDC library. Google it. Put it in /lib folder.");
        }
        return null;
    }
    
    @Override
    public void load() {
        App.getInstance().getLogger().info("23");
        this.connection = this.getSQLConnection();
        App.getInstance().getLogger().info("24");
        try {
        App.getInstance().getLogger().info("25");
        final Statement s = this.connection.createStatement();
        App.getInstance().getLogger().info("26");
        s.executeUpdate(this.createTestTable);
        App.getInstance().getLogger().info("27");
        s.executeUpdate(this.customCreateString);
        App.getInstance().getLogger().info("28");
        s.close();
        }
        catch (SQLException e) {
        App.getInstance().getLogger().info("29");
        e.printStackTrace();
        }
        App.getInstance().getLogger().info("237");
        this.initialize();
    }
    
    public File getDataFolder() {
        return this.dataFolder;
    }
}
