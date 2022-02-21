package dev.cibmc.spigot.blankplugin;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.ChatColor;
import org.bukkit.Material;


public class RankedInventory  implements InventoryProvider {
    @Override
    public void init(Player player, InventoryContents contents) {
    System.out.println("dss");
    contents.fillBorders(ClickableItem.empty(new ItemStack(Material.GRANITE)));

    contents.set(1, 1, ClickableItem.of(new ItemStack(Material.CARROT),
            e -> player.sendMessage(ChatColor.GOLD + "You clicked on a potato.")));

    contents.set(1, 7, ClickableItem.of(new ItemStack(Material.BARRIER),
            e -> player.closeInventory()));
    }

    @Override
    public void update(Player player, InventoryContents contents) {

}
}
