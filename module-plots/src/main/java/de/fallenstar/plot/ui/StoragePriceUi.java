package de.fallenstar.plot.ui;

import de.fallenstar.core.ui.container.GenericUiLargeChest;
import de.fallenstar.core.ui.element.ClickableUiElement;
import de.fallenstar.core.ui.element.StaticUiElement;
import de.fallenstar.core.ui.element.UiAction;
import de.fallenstar.core.ui.element.navigation.CloseButton;
import de.fallenstar.core.ui.row.BasicUiRowForContent;
import de.fallenstar.plot.gui.PriceEditorContext;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Type-Safe Storage-Preis-Editor UI.
 *
 * **Migration:** Ersetzt PriceEditorUIBuilder (text-basiert) durch
 * type-safe GenericUiLargeChest-basierte Implementierung.
 *
 * **Features:**
 * - Ankaufspreis festlegen (Spieler verkauft → NPC zahlt)
 * - Verkaufspreis festlegen (Spieler kauft → Spieler zahlt)
 * - +/- Buttons für beide Preise (100/10/1 Sterne)
 * - Bestätigen/Abbrechen mit Type-Safe Actions
 *
 * **Layout:**
 * - Row 0 (Slots 0-8): Navigation (Close, Item-Info)
 * - Row 1 (Slots 9-17): Leer
 * - Row 2 (Slots 18-26): Ankaufspreis-Editor
 * - Row 3 (Slots 27-35): Verkaufspreis-Editor
 * - Row 4 (Slots 36-44): Leer
 * - Row 5 (Slots 45-53): Bestätigen/Abbrechen
 *
 * **Type-Safety:**
 * - AdjustPriceAction (wiederverwendbar für Buy/Sell)
 * - ConfirmPriceAction, CancelPriceAction
 * - Alle Actions compiler-enforced
 *
 * @author FallenStar
 * @version 2.0
 */
public class StoragePriceUi extends GenericUiLargeChest {

    private final PriceEditorContext context;
    private final Consumer<PriceEditorContext> onConfirm;
    private final Runnable onCancel;

    /**
     * Konstruktor für StoragePriceUi.
     *
     * @param context PriceEditorContext mit Buy/Sell-Preisen
     * @param onConfirm Callback beim Bestätigen
     * @param onCancel Callback beim Abbrechen
     */
    public StoragePriceUi(
            PriceEditorContext context,
            Consumer<PriceEditorContext> onConfirm,
            Runnable onCancel
    ) {
        super("§6§lPreise festlegen");
        this.context = context;
        this.onConfirm = onConfirm;
        this.onCancel = onCancel;

        // Initialisiere Rows
        for (int i = 0; i < ROW_COUNT; i++) {
            setRow(i, new BasicUiRowForContent());
        }

        buildUi();
    }

    /**
     * Baut das UI auf.
     */
    private void buildUi() {
        buildNavigationBar();
        buildBuyPriceEditor();
        buildSellPriceEditor();
        buildConfirmationBar();
    }

    /**
     * Baut die Navigation-Bar (Row 0).
     */
    private void buildNavigationBar() {
        var row = getRow(0);

        // Slot 0: Close Button
        row.setElement(0, CloseButton.create(this));

        // Slot 4: Item-Info
        row.setElement(4, new StaticUiElement(
                createItemInfoDisplay()
        ));
    }

    /**
     * Baut den Ankaufspreis-Editor (Row 2).
     */
    private void buildBuyPriceEditor() {
        var row = getRow(2);

        // Slot 18 (Row 2, Pos 0): Info-Label
        row.setElement(0, new StaticUiElement(
                createButtonItem(
                        Material.EMERALD,
                        "§a§lAnkaufspreis",
                        List.of(
                                "§7Preis den der NPC",
                                "§7dem Spieler zahlt",
                                "§7",
                                "§7Aktuell: §e" + formatPrice(context.getBuyPrice()) + " Sterne"
                        )
                )
        ));

        // Slot 19-21: Erhöhen (+100, +10, +1)
        row.setElement(1, createAdjustButton(
                Material.GOLD_INGOT, "+100 ⭐", BigDecimal.valueOf(100), PriceType.BUY, true
        ));
        row.setElement(2, createAdjustButton(
                Material.IRON_INGOT, "+10 ⭐", BigDecimal.TEN, PriceType.BUY, true
        ));
        row.setElement(3, createAdjustButton(
                Material.COPPER_INGOT, "+1 ⭐", BigDecimal.ONE, PriceType.BUY, true
        ));

        // Slot 22-24: Leer

        // Slot 25-27: Verringern (-1, -10, -100)
        row.setElement(5, createAdjustButton(
                Material.COPPER_INGOT, "-1 ⭐", BigDecimal.ONE, PriceType.BUY, false
        ));
        row.setElement(6, createAdjustButton(
                Material.IRON_INGOT, "-10 ⭐", BigDecimal.TEN, PriceType.BUY, false
        ));
        row.setElement(7, createAdjustButton(
                Material.GOLD_INGOT, "-100 ⭐", BigDecimal.valueOf(100), PriceType.BUY, false
        ));
    }

    /**
     * Baut den Verkaufspreis-Editor (Row 3).
     */
    private void buildSellPriceEditor() {
        var row = getRow(3);

        // Slot 27 (Row 3, Pos 0): Info-Label
        row.setElement(0, new StaticUiElement(
                createButtonItem(
                        Material.DIAMOND,
                        "§b§lVerkaufspreis",
                        List.of(
                                "§7Preis den der Spieler",
                                "§7dem NPC zahlen muss",
                                "§7",
                                "§7Aktuell: §e" + formatPrice(context.getSellPrice()) + " Sterne"
                        )
                )
        ));

        // Slot 28-30: Erhöhen (+100, +10, +1)
        row.setElement(1, createAdjustButton(
                Material.GOLD_INGOT, "+100 ⭐", BigDecimal.valueOf(100), PriceType.SELL, true
        ));
        row.setElement(2, createAdjustButton(
                Material.IRON_INGOT, "+10 ⭐", BigDecimal.TEN, PriceType.SELL, true
        ));
        row.setElement(3, createAdjustButton(
                Material.COPPER_INGOT, "+1 ⭐", BigDecimal.ONE, PriceType.SELL, true
        ));

        // Slot 31-33: Leer

        // Slot 34-36: Verringern (-1, -10, -100)
        row.setElement(5, createAdjustButton(
                Material.COPPER_INGOT, "-1 ⭐", BigDecimal.ONE, PriceType.SELL, false
        ));
        row.setElement(6, createAdjustButton(
                Material.IRON_INGOT, "-10 ⭐", BigDecimal.TEN, PriceType.SELL, false
        ));
        row.setElement(7, createAdjustButton(
                Material.GOLD_INGOT, "-100 ⭐", BigDecimal.valueOf(100), PriceType.SELL, false
        ));
    }

    /**
     * Baut die Bestätigungs-Bar (Row 5).
     */
    private void buildConfirmationBar() {
        var row = getRow(5);

        // Slot 47 (Row 5, Pos 2): Abbrechen
        var cancelButton = new ClickableUiElement.CustomButton<>(
                createButtonItem(
                        Material.RED_WOOL,
                        "§c§lAbbrechen",
                        List.of(
                                "§7Verwirft alle Änderungen",
                                "§7und schließt das Menü"
                        )
                ),
                new CancelPriceAction(this, onCancel)
        );
        row.setElement(2, cancelButton);

        // Slot 49 (Row 5, Pos 4): Info
        row.setElement(4, new StaticUiElement(
                createButtonItem(
                        Material.PAPER,
                        "§7Preise ändern",
                        context.isPriceChanged() ?
                                List.of(
                                        "§7Änderungen vorhanden:",
                                        "§7",
                                        "§7Ankauf: §e" + formatPrice(context.getOriginalBuyPrice()) +
                                                " §7→ §a" + formatPrice(context.getBuyPrice()),
                                        "§7Verkauf: §e" + formatPrice(context.getOriginalSellPrice()) +
                                                " §7→ §a" + formatPrice(context.getSellPrice())
                                ) :
                                List.of(
                                        "§7Keine Änderungen",
                                        "§7",
                                        "§7Nutze die +/- Buttons",
                                        "§7um Preise anzupassen"
                                )
                )
        ));

        // Slot 51 (Row 5, Pos 6): Bestätigen
        var confirmButton = new ClickableUiElement.CustomButton<>(
                createButtonItem(
                        Material.LIME_WOOL,
                        "§a§lBestätigen",
                        List.of(
                                "§7Speichert die neuen Preise",
                                "§7und schließt das Menü",
                                "§7",
                                context.isPriceChanged() ?
                                        "§a§lKlicke zum Speichern" :
                                        "§7Keine Änderungen"
                        )
                ),
                new ConfirmPriceAction(this, context, onConfirm)
        );
        row.setElement(6, confirmButton);
    }

    /**
     * Erstellt einen Preis-Anpassungs-Button.
     *
     * @param material Material des Buttons
     * @param label Button-Label
     * @param amount Betrag (absolut, Vorzeichen wird ignoriert)
     * @param priceType Preis-Typ (BUY/SELL)
     * @param increase true = erhöhen, false = verringern
     * @return ClickableUiElement mit AdjustPriceAction
     */
    private ClickableUiElement<AdjustPriceAction> createAdjustButton(
            Material material,
            String label,
            BigDecimal amount,
            PriceType priceType,
            boolean increase
    ) {
        String actionDescription = (increase ? "Erhöhe" : "Verringere") +
                " " + (priceType == PriceType.BUY ? "Ankaufspreis" : "Verkaufspreis") +
                " um " + formatPrice(amount) + " Sterne";

        return new ClickableUiElement.CustomButton<>(
                createButtonItem(
                        material,
                        (increase ? "§a" : "§c") + label,
                        List.of(
                                "§7" + actionDescription
                        )
                ),
                new AdjustPriceAction(this, context, amount, priceType, increase)
        );
    }

    /**
     * Erstellt das Item-Info-Display.
     */
    private ItemStack createItemInfoDisplay() {
        ItemStack displayItem = context.getItem().clone();
        ItemMeta meta = displayItem.getItemMeta();

        // Titel
        meta.displayName(
                Component.text(context.getItemDisplayName())
                        .color(NamedTextColor.GOLD)
                        .decoration(TextDecoration.ITALIC, false)
        );

        // Lore
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("Lege Preise für dieses Item fest").color(NamedTextColor.GRAY));
        lore.add(Component.empty());
        lore.add(Component.text("Ankauf: " + formatPrice(context.getBuyPrice()) + " ⭐")
                .color(NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Verkauf: " + formatPrice(context.getSellPrice()) + " ⭐")
                .color(NamedTextColor.AQUA)
                .decoration(TextDecoration.ITALIC, false));

        meta.lore(lore);
        displayItem.setItemMeta(meta);
        return displayItem;
    }

    /**
     * Baut das UI neu auf und aktualisiert die Anzeige.
     */
    public void rebuild(Player player) {
        // Lösche alle Rows
        for (int i = 0; i < ROW_COUNT; i++) {
            getRow(i).clear();
        }

        // Baue UI neu auf
        buildUi();

        // Öffne neu
        open(player);
    }

    // ========================================
    // Helper Methods
    // ========================================

    /**
     * Erstellt ein Button-Item.
     */
    private ItemStack createButtonItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text(name).decoration(TextDecoration.ITALIC, false));

        List<Component> loreLines = new ArrayList<>();
        for (String line : lore) {
            loreLines.add(Component.text(line).decoration(TextDecoration.ITALIC, false));
        }
        meta.lore(loreLines);

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Formatiert einen Preis für die Anzeige.
     *
     * @param price Preis
     * @return Formatierter String
     */
    private String formatPrice(BigDecimal price) {
        return price.stripTrailingZeros().toPlainString();
    }

    // ========================================
    // Type-Safe Actions
    // ========================================

    /**
     * Preis-Typ Enum (BUY oder SELL).
     */
    private enum PriceType {
        BUY,   // Ankaufspreis
        SELL   // Verkaufspreis
    }

    /**
     * Action zum Anpassen eines Preises (Type-Safe).
     */
    private static final class AdjustPriceAction implements UiAction {
        private final StoragePriceUi ui;
        private final PriceEditorContext context;
        private final BigDecimal amount;
        private final PriceType priceType;
        private final boolean increase;

        public AdjustPriceAction(
                StoragePriceUi ui,
                PriceEditorContext context,
                BigDecimal amount,
                PriceType priceType,
                boolean increase
        ) {
            this.ui = ui;
            this.context = context;
            this.amount = amount;
            this.priceType = priceType;
            this.increase = increase;
        }

        @Override
        public void execute(Player player) {
            if (priceType == PriceType.BUY) {
                if (increase) {
                    context.increaseBuyPrice(amount);
                } else {
                    context.decreaseBuyPrice(amount);
                }
            } else {
                if (increase) {
                    context.increaseSellPrice(amount);
                } else {
                    context.decreaseSellPrice(amount);
                }
            }

            // UI neu aufbauen
            ui.rebuild(player);
        }

        @Override
        public String getActionName() {
            return "AdjustPrice[" + priceType + ", " + (increase ? "+" : "-") + amount + "]";
        }
    }

    /**
     * Action zum Bestätigen der Preise (Type-Safe).
     */
    private static final class ConfirmPriceAction implements UiAction {
        private final StoragePriceUi ui;
        private final PriceEditorContext context;
        private final Consumer<PriceEditorContext> onConfirm;

        public ConfirmPriceAction(
                StoragePriceUi ui,
                PriceEditorContext context,
                Consumer<PriceEditorContext> onConfirm
        ) {
            this.ui = ui;
            this.context = context;
            this.onConfirm = onConfirm;
        }

        @Override
        public void execute(Player player) {
            ui.close(player);
            onConfirm.accept(context);
        }

        @Override
        public String getActionName() {
            return "ConfirmPrice";
        }
    }

    /**
     * Action zum Abbrechen (Type-Safe).
     */
    private static final class CancelPriceAction implements UiAction {
        private final StoragePriceUi ui;
        private final Runnable onCancel;

        public CancelPriceAction(StoragePriceUi ui, Runnable onCancel) {
            this.ui = ui;
            this.onCancel = onCancel;
        }

        @Override
        public void execute(Player player) {
            ui.close(player);
            onCancel.run();
        }

        @Override
        public String getActionName() {
            return "CancelPrice";
        }
    }
}
