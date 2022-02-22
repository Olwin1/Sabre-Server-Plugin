package dev.cibmc.spigot.blankplugin;
import fr.minuskube.inv.SmartInventory;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class MyGUIs {
    public static final SmartInventory rankedInventory(Material mat, String name, Economy econ, Integer price, Block block) { return SmartInventory.builder()
                .provider(new RankedInventory(mat, name, econ, price, block))
                .id("rankedInventory")
                .size(3,9)
                .title("PracticeConfig.RANKED_TITLE")
                .build();
                
    }
    public static final SmartInventory mainDropperBuyInventory(ItemStack conveyor, ItemStack dropper, Economy econ) { return SmartInventory.builder()
        .provider(new mainDropperBuyInventory(conveyor, dropper, econ))
        .id("mainDropperBuyInventory")
        .size(3,9)
        .title("Buy A Dropper Or Conveyor")
        .build();
        
}

}
