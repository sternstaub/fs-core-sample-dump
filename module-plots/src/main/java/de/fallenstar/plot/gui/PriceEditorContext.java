package de.fallenstar.plot.gui;

import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;

/**
 * Kontext für Preis-Editor Sessions.
 *
 * Speichert den aktuellen Zustand einer Preis-Bearbeitungs-Session:
 * - Das Item für das der Preis gesetzt wird
 * - Ankaufpreis (Spieler verkauft an NPC → NPC zahlt diesem Preis)
 * - Verkaufspreis (Spieler kauft von NPC → Spieler zahlt diesen Preis)
 * - Die ursprünglichen Preise (für Cancel)
 *
 * @author FallenStar
 * @version 2.0
 */
public class PriceEditorContext {

    private final ItemStack item;
    private BigDecimal buyPrice;      // Ankaufpreis (Spieler verkauft → NPC zahlt)
    private BigDecimal sellPrice;     // Verkaufspreis (Spieler kauft → Spieler zahlt)
    private final BigDecimal originalBuyPrice;
    private final BigDecimal originalSellPrice;

    /**
     * Erstellt einen neuen PriceEditorContext.
     *
     * @param item Das Item
     * @param initialBuyPrice Ankaufpreis (oder aktueller Preis aus ItemBasePriceProvider)
     * @param initialSellPrice Verkaufspreis (oder aktueller Preis aus ItemBasePriceProvider)
     */
    public PriceEditorContext(ItemStack item, BigDecimal initialBuyPrice, BigDecimal initialSellPrice) {
        this.item = item.clone();
        this.buyPrice = initialBuyPrice;
        this.sellPrice = initialSellPrice;
        this.originalBuyPrice = initialBuyPrice;
        this.originalSellPrice = initialSellPrice;
    }

    /**
     * Erstellt einen neuen PriceEditorContext mit gleichen Preisen.
     *
     * @param item Das Item
     * @param initialPrice Einheitspreis (wird für Buy und Sell verwendet)
     * @deprecated Verwende {@link #PriceEditorContext(ItemStack, BigDecimal, BigDecimal)} stattdessen
     */
    @Deprecated
    public PriceEditorContext(ItemStack item, BigDecimal initialPrice) {
        this(item, initialPrice, initialPrice);
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
     * Gibt den Ankaufpreis zurück (Spieler verkauft → NPC zahlt).
     *
     * @return Ankaufpreis
     */
    public BigDecimal getBuyPrice() {
        return buyPrice;
    }

    /**
     * Setzt den Ankaufpreis.
     *
     * @param price Neuer Ankaufpreis
     */
    public void setBuyPrice(BigDecimal price) {
        // Preis darf nicht negativ sein
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            this.buyPrice = BigDecimal.ZERO;
        } else {
            this.buyPrice = price;
        }
    }

    /**
     * Gibt den Verkaufspreis zurück (Spieler kauft → Spieler zahlt).
     *
     * @return Verkaufspreis
     */
    public BigDecimal getSellPrice() {
        return sellPrice;
    }

    /**
     * Setzt den Verkaufspreis.
     *
     * @param price Neuer Verkaufspreis
     */
    public void setSellPrice(BigDecimal price) {
        // Preis darf nicht negativ sein
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            this.sellPrice = BigDecimal.ZERO;
        } else {
            this.sellPrice = price;
        }
    }

    /**
     * Erhöht den Ankaufpreis um einen Betrag.
     *
     * @param amount Betrag (in Sternen)
     */
    public void increaseBuyPrice(BigDecimal amount) {
        setBuyPrice(buyPrice.add(amount));
    }

    /**
     * Verringert den Ankaufpreis um einen Betrag.
     *
     * @param amount Betrag (in Sternen)
     */
    public void decreaseBuyPrice(BigDecimal amount) {
        setBuyPrice(buyPrice.subtract(amount));
    }

    /**
     * Erhöht den Verkaufspreis um einen Betrag.
     *
     * @param amount Betrag (in Sternen)
     */
    public void increaseSellPrice(BigDecimal amount) {
        setSellPrice(sellPrice.add(amount));
    }

    /**
     * Verringert den Verkaufspreis um einen Betrag.
     *
     * @param amount Betrag (in Sternen)
     */
    public void decreaseSellPrice(BigDecimal amount) {
        setSellPrice(sellPrice.subtract(amount));
    }

    /**
     * Gibt den ursprünglichen Ankaufpreis zurück (vor Bearbeitung).
     *
     * @return Original-Ankaufpreis
     */
    public BigDecimal getOriginalBuyPrice() {
        return originalBuyPrice;
    }

    /**
     * Gibt den ursprünglichen Verkaufspreis zurück (vor Bearbeitung).
     *
     * @return Original-Verkaufspreis
     */
    public BigDecimal getOriginalSellPrice() {
        return originalSellPrice;
    }

    /**
     * Prüft ob die Preise geändert wurden.
     *
     * @return true wenn geändert
     */
    public boolean isPriceChanged() {
        return !buyPrice.equals(originalBuyPrice) || !sellPrice.equals(originalSellPrice);
    }

    /**
     * Setzt die Preise auf die ursprünglichen Werte zurück.
     */
    public void resetToOriginal() {
        this.buyPrice = originalBuyPrice;
        this.sellPrice = originalSellPrice;
    }

    // ========== Legacy Compatibility (deprecated) ==========

    /**
     * Gibt den "aktuellen Preis" zurück (Verkaufspreis).
     *
     * @return Verkaufspreis
     * @deprecated Verwende {@link #getSellPrice()} oder {@link #getBuyPrice()} stattdessen
     */
    @Deprecated
    public BigDecimal getCurrentPrice() {
        return sellPrice;
    }

    /**
     * Setzt den "aktuellen Preis" (beide Preise auf denselben Wert).
     *
     * @param price Neuer Preis
     * @deprecated Verwende {@link #setBuyPrice(BigDecimal)} und {@link #setSellPrice(BigDecimal)} stattdessen
     */
    @Deprecated
    public void setCurrentPrice(BigDecimal price) {
        setBuyPrice(price);
        setSellPrice(price);
    }

    /**
     * Erhöht den Preis (beide Preise gleichzeitig).
     *
     * @param amount Betrag (in Sternen)
     * @deprecated Verwende {@link #increaseBuyPrice(BigDecimal)} und {@link #increaseSellPrice(BigDecimal)} stattdessen
     */
    @Deprecated
    public void increasePrice(BigDecimal amount) {
        increaseBuyPrice(amount);
        increaseSellPrice(amount);
    }

    /**
     * Verringert den Preis (beide Preise gleichzeitig).
     *
     * @param amount Betrag (in Sternen)
     * @deprecated Verwende {@link #decreaseBuyPrice(BigDecimal)} und {@link #decreaseSellPrice(BigDecimal)} stattdessen
     */
    @Deprecated
    public void decreasePrice(BigDecimal amount) {
        decreaseBuyPrice(amount);
        decreaseSellPrice(amount);
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
