package de.fallenstar.ui.ui;

import de.fallenstar.core.ui.SmallChestUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Price Editor UI - Interaktives Preis-Anpassungs-Interface.
 *
 * Layout (27 Slots):
 * Reihe 1: [+100] [+10] [+1] ... [-1] [-10] [-100]
 * Reihe 2: ......... [ITEM] .........
 * Reihe 3: ........ [Confirm] [Cancel] ........
 *
 * Verwendet Coin-Items als +/- Buttons.
 *
 * @author FallenStar
 * @version 1.0
 */
public class PriceEditorUI extends SmallChestUI {

    /**
     * Callback-Interface für Preis-Anpassungen.
     */
    public interface PriceCallback {
        /**
         * Wird aufgerufen wenn der Preis erhöht wird.
         *
         * @param amount Erhöhungsbetrag (1, 10, 100)
         */
        void onIncrease(int amount);

        /**
         * Wird aufgerufen wenn der Preis verringert wird.
         *
         * @param amount Verringerungsbetrag (1, 10, 100)
         */
        void onDecrease(int amount);

        /**
         * Wird aufgerufen wenn der Spieler bestätigt.
         */
        void onConfirm();

        /**
         * Wird aufgerufen wenn der Spieler abbricht.
         */
        void onCancel();

        /**
         * Gibt das aktuelle Item zurück (für Anzeige in UI-Mitte).
         *
         * @return ItemStack mit aktuellem Preis in Lore
         */
        ItemStack getCurrentDisplayItem();
    }

    // Slot-Positionen
    private static final int SLOT_INCREASE_100 = 1;
    private static final int SLOT_INCREASE_10 = 2;
    private static final int SLOT_INCREASE_1 = 3;
    private static final int SLOT_DECREASE_1 = 5;
    private static final int SLOT_DECREASE_10 = 6;
    private static final int SLOT_DECREASE_100 = 7;
    private static final int SLOT_ITEM_DISPLAY = 13; // Mitte
    private static final int SLOT_CONFIRM = 21;
    private static final int SLOT_CANCEL = 23;

    private final PriceCallback callback;
    private final ItemStack coinGold;
    private final ItemStack coinSilver;
    private final ItemStack coinBronze;

    /**
     * Erstellt eine neue PriceEditorUI.
     *
     * @param title Titel des UI
     * @param callback Callback für Preis-Änderungen
     * @param coinBronze Bronze-Münze (für +1/-1)
     * @param coinSilver Silber-Münze (für +10/-10)
     * @param coinGold Gold-Münze (für +100/-100)
     */
    public PriceEditorUI(String title, PriceCallback callback,
                         ItemStack coinBronze, ItemStack coinSilver, ItemStack coinGold) {
        super(title);
        this.callback = callback;
        this.coinBronze = coinBronze;
        this.coinSilver = coinSilver;
        this.coinGold = coinGold;

        buildUI();
    }

    /**
     * Baut das UI auf.
     */
    private void buildUI() {
        // Increase-Buttons (oben links)
        setItem(SLOT_INCREASE_100, createButton(coinGold.clone(), "+100 Sterne"),
                player -> handleIncrease(player, 100));
        setItem(SLOT_INCREASE_10, createButton(coinSilver.clone(), "+10 Sterne"),
                player -> handleIncrease(player, 10));
        setItem(SLOT_INCREASE_1, createButton(coinBronze.clone(), "+1 Stern"),
                player -> handleIncrease(player, 1));

        // Decrease-Buttons (oben rechts)
        setItem(SLOT_DECREASE_1, createButton(coinBronze.clone(), "-1 Stern"),
                player -> handleDecrease(player, 1));
        setItem(SLOT_DECREASE_10, createButton(coinSilver.clone(), "-10 Sterne"),
                player -> handleDecrease(player, 10));
        setItem(SLOT_DECREASE_100, createButton(coinGold.clone(), "-100 Sterne"),
                player -> handleDecrease(player, 100));

        // Item-Display (Mitte) - wird bei jedem open() aktualisiert
        updateItemDisplay();

        // Confirm/Cancel-Buttons (unten)
        ItemStack confirmButton = new ItemStack(Material.LIME_WOOL);
        ItemMeta confirmMeta = confirmButton.getItemMeta();
        confirmMeta.displayName(Component.text("Bestätigen", NamedTextColor.GREEN));
        confirmMeta.lore(List.of(Component.text("Preis übernehmen", NamedTextColor.GRAY)));
        confirmButton.setItemMeta(confirmMeta);
        setItem(SLOT_CONFIRM, confirmButton, this::handleConfirm);

        ItemStack cancelButton = new ItemStack(Material.RED_WOOL);
        ItemMeta cancelMeta = cancelButton.getItemMeta();
        cancelMeta.displayName(Component.text("Abbrechen", NamedTextColor.RED));
        cancelMeta.lore(List.of(Component.text("Ohne Speichern schließen", NamedTextColor.GRAY)));
        cancelButton.setItemMeta(cancelMeta);
        setItem(SLOT_CANCEL, cancelButton, this::handleCancel);
    }

    /**
     * Aktualisiert das Item-Display in der Mitte.
     */
    private void updateItemDisplay() {
        ItemStack displayItem = callback.getCurrentDisplayItem();
        setItem(SLOT_ITEM_DISPLAY, displayItem);
    }

    /**
     * Erstellt einen Button mit Name und Lore.
     *
     * @param baseItem Basis-ItemStack (z.B. Münze)
     * @param displayName Anzeigename
     * @return ItemStack mit Metadaten
     */
    private ItemStack createButton(ItemStack baseItem, String displayName) {
        ItemMeta meta = baseItem.getItemMeta();
        meta.displayName(Component.text(displayName, NamedTextColor.YELLOW));
        meta.lore(List.of(Component.text("Klicken zum Anpassen", NamedTextColor.GRAY)));
        baseItem.setItemMeta(meta);
        return baseItem;
    }

    /**
     * Handler für Preis-Erhöhung.
     *
     * @param player Spieler
     * @param amount Erhöhungsbetrag
     */
    private void handleIncrease(Player player, int amount) {
        callback.onIncrease(amount);
        updateItemDisplay();
        refresh(player); // UI aktualisieren um Änderungen zu zeigen (ohne zu schließen!)
    }

    /**
     * Handler für Preis-Verringerung.
     *
     * @param player Spieler
     * @param amount Verringerungsbetrag
     */
    private void handleDecrease(Player player, int amount) {
        callback.onDecrease(amount);
        updateItemDisplay();
        refresh(player); // UI aktualisieren um Änderungen zu zeigen (ohne zu schließen!)
    }

    /**
     * Handler für Bestätigung.
     *
     * @param player Spieler
     */
    private void handleConfirm(Player player) {
        callback.onConfirm();
        close(player);
    }

    /**
     * Handler für Abbrechen.
     *
     * @param player Spieler
     */
    private void handleCancel(Player player) {
        callback.onCancel();
        close(player);
    }
}
