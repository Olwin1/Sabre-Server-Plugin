// 
// Decompiled by Procyon v0.5.36
// 

package dev.cibmc.spigot.blankplugin.DB.Database;

import dev.cibmc.spigot.blankplugin.DB.Error.Error;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import dev.cibmc.spigot.blankplugin.DB.Error.Errors;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import dev.cibmc.spigot.blankplugin.App;
import java.sql.Connection;

public abstract class Database
{
    protected Connection connection;
    
    public abstract Connection getSQLConnection();
    
    public abstract void load();
    
    public void initialize() {
        this.connection = this.getSQLConnection();
        try {
            final PreparedStatement ps = this.connection.prepareStatement("SELECT * FROM test");
            final ResultSet rs = ps.executeQuery();
            this.close(ps, rs);
        }
        catch (SQLException ex) {
            App.getInstance().getLogger().log(Level.SEVERE, "Unable to retreive connection", ex);
        }
    }
    
    public Boolean executeStatement(final String statement) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = this.getSQLConnection();
            ps = conn.prepareStatement(statement);
            return !ps.execute();
        }
        catch (SQLException ex) {
            App.getInstance().getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
            return false;
        }
        finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (conn != null) {
                    conn.close();
                }
            }
            catch (SQLException ex2) {
                App.getInstance().getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex2);
                return false;
            }
        }
    }
    
    public Object queryValue(final String statement, final String row) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = this.getSQLConnection();
            ps = conn.prepareStatement(statement);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getObject(row);
            }
        }
        catch (SQLException ex) {
            App.getInstance().getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
            try {
                if (ps != null) {
                    ps.close();
                }
                if (conn != null) {
                    conn.close();
                }
            }
            catch (SQLException ex2) {
                App.getInstance().getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex2);
            }
            return null;
        }
        finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (conn != null) {
                    conn.close();
                }
            }
            catch (SQLException ex2) {
                App.getInstance().getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex2);
            }
        }
        try {
            if (ps != null) {
                ps.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
        catch (SQLException ex2) {
            App.getInstance().getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex2);
        }
        return null;
    }
    
    public List<Object> queryRow(final String statement, final String row) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        final List<Object> objects = new ArrayList<Object>();
        try {
            conn = this.getSQLConnection();
            ps = conn.prepareStatement(statement);
            rs = ps.executeQuery();
            while (rs.next()) {
                objects.add(rs.getObject(row));
            }
            return objects;
        }
        catch (SQLException ex) {
            App.getInstance().getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
            try {
                if (ps != null) {
                    ps.close();
                }
                if (conn != null) {
                    conn.close();
                }
            }
            catch (SQLException ex2) {
                App.getInstance().getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex2);
            }
        }
        finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (conn != null) {
                    conn.close();
                }
            }
            catch (SQLException ex2) {
                App.getInstance().getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex2);
            }
        }
        return null;
    }
    
    public Map<String, List<Object>> queryMultipleRows(final String statement, final String... row) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        final List<Object> objects = new ArrayList<Object>();
        final Map<String, List<Object>> map = new HashMap<String, List<Object>>();
        try {
            conn = this.getSQLConnection();
            ps = conn.prepareStatement(statement);
            rs = ps.executeQuery();
            while (rs.next()) {
                for (final String singleRow : row) {
                    objects.add(rs.getObject(singleRow));
                }
                for (final String singleRow : row) {
                    map.put(singleRow, objects);
                }
            }
            return map;
        }
        catch (SQLException ex) {
            App.getInstance().getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
            try {
                if (ps != null) {
                    ps.close();
                }
                if (conn != null) {
                    conn.close();
                }
            }
            catch (SQLException ex2) {
                App.getInstance().getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex2);
            }
        }
        finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (conn != null) {
                    conn.close();
                }
            }
            catch (SQLException ex2) {
                App.getInstance().getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex2);
            }
        }
        return null;
    }
    
    public void close(final PreparedStatement ps, final ResultSet rs) {
        try {
            if (ps != null) {
                ps.close();
            }
            if (rs != null) {
                rs.close();
            }
        }
        catch (SQLException ex) {
            Error.close(App.getInstance(), ex);
        }
    }
    
    public void closeConnection() {
        try {
            this.connection.close();
        }
        catch (SQLException e) {
            Error.close(App.getInstance(), e);
        }
    }
}
