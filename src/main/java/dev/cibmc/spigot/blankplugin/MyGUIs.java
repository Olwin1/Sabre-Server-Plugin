package dev.cibmc.spigot.blankplugin;
import fr.minuskube.inv.SmartInventory;

public class MyGUIs {
    public static final SmartInventory rankedInventory = SmartInventory.builder()
                .provider(new RankedInventory())
                .id("rankedInventory")
                .size(3,9)
                .title("PracticeConfig.RANKED_TITLE")
                .build();

}
