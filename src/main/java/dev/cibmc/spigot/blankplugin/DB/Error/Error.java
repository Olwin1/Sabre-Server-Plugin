// 
// Decompiled by Procyon v0.5.36
// 

package dev.cibmc.spigot.blankplugin.DB.Error;

import java.util.logging.Level;
import dev.cibmc.spigot.blankplugin.App;

public class Error
{
    public static void execute(final App plugin, final Exception ex) {
        plugin.getLogger().log(Level.SEVERE, "Couldn't execute MySQL statement: ", ex);
    }
    
    public static void close(final App plugin, final Exception ex) {
        plugin.getLogger().log(Level.SEVERE, "Failed to close MySQL connection: ", ex);
    }
}
