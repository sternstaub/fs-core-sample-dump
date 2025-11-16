package de.fallenstar.items.ui;

import de.fallenstar.core.ui.SmallChestUI;
import de.fallenstar.items.manager.SpecialItemManager;
import de.fallenstar.items.provider.MMOItemsItemProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * Test-UI für Trading-System mit MMOItems.
 *
 * Demonstriert:
 * - Handel mit Custom-Items
 * - Münz-basierte Transaktionen
 * - Trade-Entry-Struktur (Input1 + Input2 → Output)
 *
 * @author FallenStar
 * @version 1.0
 */
public class TestTradeUI extends SmallChestUI {

    private final MMOItemsItemProvider itemProvider;
    private final SpecialItemManager specialItemManager;

    public TestTradeUI(MMOItemsItemProvider itemProvider, SpecialItemManager specialItemManager) {
        super("§6Test Händler - Prototype");
        this.itemProvider = itemProvider;
        this.specialItemManager = specialItemManager;
    }

    @Override
    public void open(Player player) {
        // Beim Öffnen lade Trades
        loadTrades(player);
    }

    /**
     * Lädt die Test-Trades.
     */
    public void loadTrades(Player player) {
        clearItems();

        // Trade 1: 10 Gold-Münzen → Eisenschwert
        addTradeDisplay(0, "gold", 10, null, 0, "SWORD", "IRON_BLADE");

        // Trade 2: 5 Silber-Münzen + 1 Diamant → Stahl-Schwert
        addTradeDisplay(1, "silver", 5, Material.DIAMOND, 1, "SWORD", "STEEL_BLADE");

        // Trade 3: 50 Bronze-Münzen → Leder-Rüstung
        addTradeDisplay(2, "bronze", 50, null, 0, "ARMOR", "LEATHER_ARMOR_SET");

        // Info-Slot
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.displayName(Component.text("Händler-Info", NamedTextColor.GOLD));
        infoMeta.lore(List.of(
                Component.text("Dies ist ein Test-Händler", NamedTextColor.GRAY),
                Component.empty(),
                Component.text("Trade-Format:", NamedTextColor.YELLOW),
                Component.text("Input 1 + Input 2 → Output", NamedTextColor.WHITE),
                Component.empty(),
                Component.text("Klicke auf ein Angebot", NamedTextColor.GRAY),
                Component.text("um zu handeln (Demo)", NamedTextColor.GRAY)
        ));
        info.setItemMeta(infoMeta);
        setItem(13, info, null);

        // Schließen-Button
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.displayName(Component.text("Schließen", NamedTextColor.RED));
        close.setItemMeta(closeMeta);
        setItem(26, close, this::close);

        super.open(player);
    }

    /**
     * Erstellt eine Trade-Anzeige.
     *
     * @param tradeIndex Index des Trades (0-2)
     * @param currencyType Währungstyp (bronze, silver, gold)
     * @param currencyAmount Anzahl Münzen
     * @param extraInputMaterial Extra Material (optional)
     * @param extraInputAmount Anzahl Extra Material
     * @param outputType MMOItems Type
     * @param outputId MMOItems ID
     */
    private void addTradeDisplay(int tradeIndex, String currencyType, int currencyAmount,
                                  Material extraInputMaterial, int extraInputAmount,
                                  String outputType, String outputId) {
        // Layout: Jeder Trade bekommt 3 Slots (Input1, Input2, Output)
        // Trade 0: Slots 9, 10, 11
        // Trade 1: Slots 12, 13, 14
        // Trade 2: Slots 15, 16, 17

        int baseSlot = 18 + (tradeIndex * 3);

        // Input 1: Münzen
        specialItemManager.createCurrency(currencyType, currencyAmount).ifPresent(coins -> {
            ItemStack display = coins.clone();
            ItemMeta meta = display.getItemMeta();
            List<Component> lore = meta.hasLore() ? List.copyOf(meta.lore()) : List.of();
            meta.lore(List.of(
                    Component.text("Input 1", NamedTextColor.YELLOW),
                    Component.empty()
            ));
            meta.lore().addAll(lore);
            display.setItemMeta(meta);
            setItem(baseSlot, display, null);
        });

        // Input 2: Extra Material (optional)
        if (extraInputMaterial != null) {
            ItemStack extraInput = new ItemStack(extraInputMaterial, extraInputAmount);
            ItemMeta meta = extraInput.getItemMeta();
            meta.displayName(Component.text(extraInputMaterial.name(), NamedTextColor.WHITE));
            meta.lore(List.of(
                    Component.text("Input 2", NamedTextColor.YELLOW)
            ));
            extraInput.setItemMeta(meta);
            setItem(baseSlot + 1, extraInput, null);
        } else {
            // Leerer Slot
            ItemStack empty = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            ItemMeta meta = empty.getItemMeta();
            meta.displayName(Component.text("Kein zweiter Input", NamedTextColor.DARK_GRAY));
            empty.setItemMeta(meta);
            setItem(baseSlot + 1, empty, null);
        }

        // Output: Custom Item
        itemProvider.createItem(outputType, outputId, 1).ifPresentOrElse(
                output -> {
                    ItemStack display = output.clone();
                    ItemMeta meta = display.getItemMeta();
                    List<Component> lore = meta.hasLore() ? new java.util.ArrayList<>(meta.lore()) : new java.util.ArrayList<>();
                    lore.add(Component.empty());
                    lore.add(Component.text("Output", NamedTextColor.GREEN));
                    lore.add(Component.empty());
                    lore.add(Component.text("» Klicke zum Handeln", NamedTextColor.AQUA));
                    meta.lore(lore);
                    display.setItemMeta(meta);
                    setItem(baseSlot + 2, display, player -> {
                        attemptTrade(player, currencyType, currencyAmount,
                                extraInputMaterial, extraInputAmount,
                                outputType, outputId);
                    });
                },
                () -> {
                    // Item nicht gefunden
                    ItemStack error = new ItemStack(Material.BARRIER);
                    ItemMeta meta = error.getItemMeta();
                    meta.displayName(Component.text("Item nicht gefunden!", NamedTextColor.RED));
                    meta.lore(List.of(
                            Component.text("Type: " + outputType, NamedTextColor.GRAY),
                            Component.text("ID: " + outputId, NamedTextColor.GRAY)
                    ));
                    error.setItemMeta(meta);
                    setItem(baseSlot + 2, error, null);
                }
        );
    }

    /**
     * Versucht einen Trade durchzuführen (Demo-Implementierung).
     */
    private void attemptTrade(Player player, String currencyType, int currencyAmount,
                              Material extraInputMaterial, int extraInputAmount,
                              String outputType, String outputId) {
        // TODO: Echte Inventar-Prüfung + Item-Entfernung
        // Für Test-Zwecke: Einfach Item geben

        boolean hasCurrency = hasItemsInInventory(player, currencyType, currencyAmount);
        boolean hasExtra = (extraInputMaterial == null) ||
                hasItemsInInventory(player, extraInputMaterial, extraInputAmount);

        if (!hasCurrency || !hasExtra) {
            player.sendMessage(Component.text("✗ Du hast nicht genug Items für diesen Handel!", NamedTextColor.RED));
            return;
        }

        // Entferne Inputs (vereinfacht)
        // TODO: Echte Item-Entfernung implementieren

        // Gebe Output
        itemProvider.createItem(outputType, outputId, 1).ifPresent(output -> {
            player.getInventory().addItem(output);
            player.sendMessage(Component.text("✓ Handel erfolgreich!", NamedTextColor.GREEN));
            player.sendMessage(Component.text("  → Erhalten: " + outputId, NamedTextColor.GRAY));
        });

        close(player);
    }

    /**
     * Prüft ob Spieler bestimmte Items hat (vereinfacht).
     */
    private boolean hasItemsInInventory(Player player, String currencyType, int amount) {
        // Vereinfachte Demo-Implementierung
        // TODO: Echte Currency-Prüfung via SpecialItemManager
        return true; // Für Test immer true
    }

    /**
     * Prüft ob Spieler Material hat (vereinfacht).
     */
    private boolean hasItemsInInventory(Player player, Material material, int amount) {
        return player.getInventory().contains(material, amount);
    }
}
