package de.fallenstar.ui.ui;

import de.fallenstar.core.ui.LargeChestUI;
import de.fallenstar.ui.manager.UIButtonManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Einfaches Trade-UI mit Vanilla-Items (Demo).
 *
 * Layout (54 Slots, 6 Reihen):
 * - Reihe 1-2: Header + Info
 * - Reihe 3-5: Trade-Angebote (3x3 Grid pro Trade)
 * - Reihe 6: Navigation (Close)
 *
 * Features:
 * - Zeigt Test-Trades mit Vanilla-Items
 * - Input 1 + Input 2 → Output Layout
 * - Demo-Trades (Gold → Diamant, Eisen → Gold, etc.)
 *
 * @author FallenStar
 * @version 1.0
 */
public class SimpleTradeUI extends LargeChestUI {

    private final UIButtonManager buttonManager;

    /**
     * Konstruktor für SimpleTradeUI.
     *
     * @param buttonManager UIButtonManager-Instanz
     */
    public SimpleTradeUI(UIButtonManager buttonManager) {
        super("§6Test-Händler - Demo");
        this.buttonManager = buttonManager;
    }

    @Override
    public void open(Player player) {
        clearItems();

        // Header (Reihe 1)
        for (int i = 0; i < 9; i++) {
            ItemStack glass = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
            ItemMeta glassMeta = glass.getItemMeta();
            glassMeta.displayName(Component.text(" "));
            glass.setItemMeta(glassMeta);
            setItem(i, glass, null);
        }

        // Info-Item (Slot 4)
        buttonManager.createInfoButton(List.of(
                Component.text("Dies ist ein Test-Händler", NamedTextColor.GRAY),
                Component.empty(),
                Component.text("Trade-Format:", NamedTextColor.YELLOW),
                Component.text("Input 1 + Input 2 → Output", NamedTextColor.WHITE),
                Component.empty(),
                Component.text("Klicke auf ein Angebot", NamedTextColor.GRAY),
                Component.text("um zu handeln (Demo)", NamedTextColor.GRAY)
        )).ifPresent(info -> setItem(4, info, null));

        // Trade 1: 10 Gold-Barren + 5 Redstone → Diamant (Reihe 2, Slots 10-12)
        addTrade(10, Material.GOLD_INGOT, 10, Material.REDSTONE, 5, Material.DIAMOND, 1,
                "10 Gold + 5 Redstone", "1 Diamant");

        // Trade 2: 20 Eisen-Barren → 5 Gold-Barren (Reihe 2, Slots 14-16)
        addTrade(14, Material.IRON_INGOT, 20, null, 0, Material.GOLD_INGOT, 5,
                "20 Eisen", "5 Gold");

        // Trade 3: 32 Kohle + 16 Holz → 8 Fackeln (Reihe 3, Slots 19-21)
        addTrade(19, Material.COAL, 32, Material.OAK_LOG, 16, Material.TORCH, 8,
                "32 Kohle + 16 Holz", "8 Fackeln");

        // Trade 4: 5 Diamanten → Verzauberter Bogen (Reihe 3, Slots 23-25)
        addTrade(23, Material.DIAMOND, 5, null, 0, Material.BOW, 1,
                "5 Diamanten", "Verzauberter Bogen");

        // Trade 5: 64 Weizen + 32 Karotten → Golden Carrot (Reihe 4, Slots 28-30)
        addTrade(28, Material.WHEAT, 64, Material.CARROT, 32, Material.GOLDEN_CARROT, 16,
                "64 Weizen + 32 Karotten", "16 Goldene Karotten");

        // Trade 6: 16 Smaragde → Elytra (Reihe 4, Slots 32-34)
        addTrade(32, Material.EMERALD, 16, null, 0, Material.ELYTRA, 1,
                "16 Smaragde", "Elytra");

        // Footer (Reihe 6)
        for (int i = 45; i < 54; i++) {
            ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            ItemMeta glassMeta = glass.getItemMeta();
            glassMeta.displayName(Component.text(" "));
            glass.setItemMeta(glassMeta);
            setItem(i, glass, null);
        }

        // Close-Button (Slot 49 - Mitte unten)
        buttonManager.createCloseButton().ifPresent(close -> setItem(49, close, this::close));

        super.open(player);
    }

    /**
     * Fügt einen Trade hinzu.
     *
     * @param baseSlot Start-Slot für Trade (3 aufeinanderfolgende Slots)
     * @param input1Material Input 1 Material
     * @param input1Amount Input 1 Anzahl
     * @param input2Material Input 2 Material (optional)
     * @param input2Amount Input 2 Anzahl
     * @param outputMaterial Output Material
     * @param outputAmount Output Anzahl
     * @param tradeName Trade-Name
     * @param outputName Output-Name
     */
    private void addTrade(int baseSlot, Material input1Material, int input1Amount,
                          Material input2Material, int input2Amount,
                          Material outputMaterial, int outputAmount,
                          String tradeName, String outputName) {
        // Input 1
        ItemStack input1 = new ItemStack(input1Material, Math.min(input1Amount, 64));
        ItemMeta input1Meta = input1.getItemMeta();
        input1Meta.displayName(Component.text(input1Material.name(), NamedTextColor.WHITE));
        List<Component> input1Lore = new ArrayList<>();
        input1Lore.add(Component.text("Input 1", NamedTextColor.YELLOW, TextDecoration.BOLD));
        input1Lore.add(Component.text("Benötigt: " + input1Amount + "x", NamedTextColor.GRAY));
        input1Meta.lore(input1Lore);
        input1.setItemMeta(input1Meta);
        setItem(baseSlot, input1, null);

        // Input 2 (oder Placeholder)
        if (input2Material != null) {
            ItemStack input2 = new ItemStack(input2Material, Math.min(input2Amount, 64));
            ItemMeta input2Meta = input2.getItemMeta();
            input2Meta.displayName(Component.text(input2Material.name(), NamedTextColor.WHITE));
            List<Component> input2Lore = new ArrayList<>();
            input2Lore.add(Component.text("Input 2", NamedTextColor.YELLOW, TextDecoration.BOLD));
            input2Lore.add(Component.text("Benötigt: " + input2Amount + "x", NamedTextColor.GRAY));
            input2Meta.lore(input2Lore);
            input2.setItemMeta(input2Meta);
            setItem(baseSlot + 1, input2, null);
        } else {
            // Leerer Slot
            ItemStack empty = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            ItemMeta emptyMeta = empty.getItemMeta();
            emptyMeta.displayName(Component.text("Kein zweiter Input", NamedTextColor.DARK_GRAY));
            empty.setItemMeta(emptyMeta);
            setItem(baseSlot + 1, empty, null);
        }

        // Output
        ItemStack output = new ItemStack(outputMaterial, Math.min(outputAmount, 64));
        ItemMeta outputMeta = output.getItemMeta();
        outputMeta.displayName(Component.text(outputName, NamedTextColor.GREEN, TextDecoration.BOLD));
        List<Component> outputLore = new ArrayList<>();
        outputLore.add(Component.text("Output", NamedTextColor.GREEN, TextDecoration.BOLD));
        outputLore.add(Component.text("Erhältst: " + outputAmount + "x", NamedTextColor.GRAY));
        outputLore.add(Component.empty());
        outputLore.add(Component.text("» Klicke zum Handeln (Demo)", NamedTextColor.AQUA));
        outputMeta.lore(outputLore);
        output.setItemMeta(outputMeta);
        setItem(baseSlot + 2, output, player -> {
            attemptTrade(player, tradeName, outputName, outputMaterial, outputAmount);
        });
    }

    /**
     * Versucht einen Trade durchzuführen (Demo).
     *
     * @param player Spieler
     * @param tradeName Trade-Name
     * @param outputName Output-Name
     * @param outputMaterial Output Material
     * @param outputAmount Output Anzahl
     */
    private void attemptTrade(Player player, String tradeName, String outputName,
                              Material outputMaterial, int outputAmount) {
        // Demo-Implementierung: Einfach Items geben
        // TODO: Echte Inventar-Prüfung + Item-Entfernung

        player.sendMessage(Component.text("═══════════════════════════════", NamedTextColor.GOLD));
        player.sendMessage(Component.text("  Trade: " + tradeName, NamedTextColor.WHITE));
        player.sendMessage(Component.text("═══════════════════════════════", NamedTextColor.GOLD));
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("✓ Handel erfolgreich! (Demo)", NamedTextColor.GREEN));
        player.sendMessage(Component.text("  → Erhalten: " + outputAmount + "x " + outputName, NamedTextColor.GRAY));
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("Hinweis:", NamedTextColor.YELLOW)
                .append(Component.text(" Dies ist eine Demo-Implementierung.", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("Echte Inventar-Prüfung noch nicht implementiert.", NamedTextColor.GRAY));

        // Gebe Items (Demo)
        ItemStack result = new ItemStack(outputMaterial, outputAmount);
        player.getInventory().addItem(result);

        // Schließe UI
        close(player);
    }
}
