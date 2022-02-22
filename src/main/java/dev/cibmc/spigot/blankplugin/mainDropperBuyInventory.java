package dev.cibmc.spigot.blankplugin;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.kyori.adventure.text.Component;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.inventory.meta.ItemMeta;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import net.milkbowl.vault.economy.EconomyResponse;
import net.kyori.adventure.text.format.TextColor;

public class mainDropperBuyInventory  implements InventoryProvider {
    private ItemStack conveyor;
    private ItemStack dropper;
    private Economy econ;
    mainDropperBuyInventory(ItemStack conveyor, ItemStack dropper, Economy econ) {
    this.conveyor = conveyor;
    this.dropper = dropper;
    this.econ = econ;
    }
    public String nonItalic(String string) {
        return ChatColor.translateAlternateColorCodes('&', ChatColor.GRAY + string);
    }

    @Override
    public void init(Player player, InventoryContents contents) {

    ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
    ItemMeta meta = item.getItemMeta();
    meta.displayName(Component.text(""));
    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
    item.setItemMeta(meta);
    contents.fill(ClickableItem.empty(item));

/*    ItemStack itemMain = conveyor;
    ItemMeta metaMain = itemMain.getItemMeta();
    metaMain.displayName(Component.text(nonItalic("Basic Conveyor")));
    itemMain.setItemMeta(metaMain);

    ItemStack itemMain2 = dropper;
    ItemMeta metaMain2 = itemMain2.getItemMeta();
    metaMain2.displayName(Component.text(nonItalic("Basic Dropper")));
    itemMain2.setItemMeta(metaMain2);
*/
    contents.set(1, 2, ClickableItem.of(conveyor,
            (e) -> {
            if (econ.has(player, 50)) {
            EconomyResponse r = econ.withdrawPlayer(player, 50);
            player.sendMessage(Component.text("Brought Conveyor For ").color(TextColor.fromHexString("#2fad37")).append(Component.text(econ.format(r.amount)).color(TextColor.fromHexString("#FF5555"))));
            player.getInventory().addItem(conveyor);
            }
            else {
                player.sendMessage(Component.text(ChatColor.RED + "You Don't Have Enough Money To Buy This Conveyor!"));
            }
        }
            ));
            contents.set(1, 6, ClickableItem.of(dropper,
            (e) -> {
            if (econ.has(player, 100)) {
            EconomyResponse r = econ.withdrawPlayer(player, 100);
            player.sendMessage(Component.text("Brought Conveyor For ").color(TextColor.fromHexString("#2fad37")).append(Component.text(econ.format(r.amount)).color(TextColor.fromHexString("#FF5555"))));
            player.getInventory().addItem(dropper);
            }
            else {
                player.sendMessage(Component.text(ChatColor.RED + "You Don't Have Enough Money To Buy This Conveyor!"));
            }
        }
            ));

//    contents.set(1, 7, ClickableItem.of(new ItemStack(Material.BARRIER),
//            e -> player.closeInventory()));
    }

    @Override
    public void update(Player player, InventoryContents contents) {

}
}