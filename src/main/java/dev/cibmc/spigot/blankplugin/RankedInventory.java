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
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import java.util.ArrayList;

public class RankedInventory  implements InventoryProvider {
    private Material mat;
    private String name;
    private Economy econ;
    private Integer price;
    private Block block;
    RankedInventory(Material mat, String name, Economy econ, Integer price, Block block) {
    this.mat = mat;
    this.name = name;
    this.econ = econ;
    this.price = price;
    this.block = block;
    }
    public String nonItalic(String string) {
        return ChatColor.translateAlternateColorCodes('&', ChatColor.GRAY + string);
    }

    @Override
    public void init(Player player, InventoryContents contents) {
    System.out.println("dss");

    ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
    ItemMeta meta = item.getItemMeta();
    meta.displayName(Component.text(""));
    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
    item.setItemMeta(meta);
    contents.fill(ClickableItem.empty(item));

    ItemStack itemMain = new ItemStack(this.mat);
    ItemMeta metaMain = itemMain.getItemMeta();
    metaMain.displayName(Component.text(nonItalic(name + " Upgrade")));
    ArrayList<Component> lore = new ArrayList<Component>();
    lore.add(Component.text(nonItalic(ChatColor.GREEN + "Buy: " + ChatColor.RED + "$" + String.valueOf(price))));
    metaMain.lore(lore);
    itemMain.setItemMeta(metaMain);

    contents.set(1, 4, ClickableItem.of(itemMain,
            (e) -> {
            if (econ.has(player, price)) {
            EconomyResponse r = econ.withdrawPlayer(player, price);
            player.sendMessage(Component.text("Upgraded Dropper For ").color(TextColor.fromHexString("#2fad37")).append(Component.text(econ.format(r.amount)).color(TextColor.fromHexString("#FF5555"))));
            block.setType(mat);
            block.getRelative(BlockFace.DOWN).setType(mat);
            player.closeInventory();
            }
            else {
                player.sendMessage(Component.text(ChatColor.RED + "You Don't Have Enough Money To Upgrade This Dropper!"));
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
