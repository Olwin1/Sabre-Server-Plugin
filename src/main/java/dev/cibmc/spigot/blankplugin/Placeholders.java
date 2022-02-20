package dev.cibmc.spigot.blankplugin;


import org.bukkit.OfflinePlayer;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class Placeholders extends PlaceholderExpansion {

    private App plugin = App.getInstance();
    
    public Placeholders(App plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public String getAuthor() {
        return "Olwin1";
    }
    
    @Override
    public String getIdentifier() {
        return "sabre";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }
    
    @Override
    public boolean persist() {
        return true; // This is required or else PlaceholderAPI will unregister the Expansion on reload
    }
    
    @Override
    public String onRequest(OfflinePlayer player, String params) {        
            if(params.equalsIgnoreCase("placedgens")) {
                return plugin.getPlayerGens(player.getName());
        }
        
        return null; // Placeholder is unknown by the Expansion
    }
}