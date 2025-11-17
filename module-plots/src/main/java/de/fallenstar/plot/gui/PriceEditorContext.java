package de.fallenstar.plot.gui;

import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;

/**
 * Kontext für Preis-Editor Sessions.
 *
 * Speichert den aktuellen Zustand einer Preis-Bearbeitungs-Session:
 * - Das Item für das der Preis gesetzt wird
 * - Der aktuelle Preiswert
 * - Der ursprüngliche Preis (für Cancel)
 *
 * @author FallenStar
 * @version 1.0
 */
public class PriceEditorContext {

    private final ItemStack item;
    private BigDecimal currentPrice;
    private final BigDecimal originalPrice;

    /**
     * Erstellt einen neuen PriceEditorContext.
     *
     * @param item Das Item
     * @param initialPrice Der Startpreis (oder aktueller Preis aus ItemBasePriceProvider)
     */
    public PriceEditorContext(ItemStack item, BigDecimal initialPrice) {
        this.item = item.clone();
        this.currentPrice = initialPrice;
        this.originalPrice = initialPrice;
    }

    /**
     * Gibt das Item zurück.
     *
     * @return ItemStack
     */
    public ItemStack getItem() {
        return item;
    }

    /**
     * Gibt den aktuellen Preis zurück.
     *
     * @return Preis
     */
    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    /**
     * Setzt den aktuellen Preis.
     *
     * @param price Neuer Preis
     */
    public void setCurrentPrice(BigDecimal price) {
        // Preis darf nicht negativ sein
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            this.currentPrice = BigDecimal.ZERO;
        } else {
            this.currentPrice = price;
        }
    }

    /**
     * Erhöht den Preis um einen Betrag.
     *
     * @param amount Betrag (in Sternen)
     */
    public void increasePrice(BigDecimal amount) {
        setCurrentPrice(currentPrice.add(amount));
    }

    /**
     * Verringert den Preis um einen Betrag.
     *
     * @param amount Betrag (in Sternen)
     */
    public void decreasePrice(BigDecimal amount) {
        setCurrentPrice(currentPrice.subtract(amount));
    }

    /**
     * Gibt den ursprünglichen Preis zurück (vor Bearbeitung).
     *
     * @return Original-Preis
     */
    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    /**
     * Prüft ob der Preis geändert wurde.
     *
     * @return true wenn geändert
     */
    public boolean isPriceChanged() {
        return !currentPrice.equals(originalPrice);
    }

    /**
     * Setzt den Preis auf den ursprünglichen Wert zurück.
     */
    public void resetToOriginal() {
        this.currentPrice = originalPrice;
    }

    /**
     * Gibt den Item-Displaynamen zurück.
     *
     * @return Display-Name
     */
    public String getItemDisplayName() {
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return item.getItemMeta().getDisplayName();
        }
        return item.getType().name();
    }
}
