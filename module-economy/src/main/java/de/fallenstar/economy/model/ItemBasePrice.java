package de.fallenstar.economy.model;

import org.bukkit.Material;

import java.math.BigDecimal;

/**
 * Repräsentiert einen Basispreis für ein Item (Vanilla oder Custom).
 *
 * Basispreise dienen als Berechnungsgrundlage für Handel:
 * - Handelsgilden-Plots nutzen diese Preise
 * - Händler leiten ihre Handelspreise davon ab
 * - Preise sind in Basiswährung (Sterne) angegeben
 *
 * Arten von Items:
 * - VANILLA: Minecraft Material (z.B. DIAMOND, IRON_INGOT)
 * - CUSTOM: MMOItems oder andere Custom-Items (Type + ID)
 *
 * @author FallenStar
 * @version 1.0
 */
public sealed interface ItemBasePrice {

    /**
     * Gibt den Basispreis zurück (Legacy - nutze buyPrice/sellPrice stattdessen).
     *
     * @return Preis in Basiswährung (Sterne)
     * @deprecated Verwende buyPrice() oder sellPrice()
     */
    @Deprecated
    BigDecimal getPrice();

    /**
     * Gibt den Ankaufspreis zurück (Spieler verkauft → NPC zahlt).
     *
     * @return Ankaufspreis in Sternen
     */
    BigDecimal getBuyPrice();

    /**
     * Gibt den Verkaufspreis zurück (Spieler kauft → Spieler zahlt).
     *
     * @return Verkaufspreis in Sternen
     */
    BigDecimal getSellPrice();

    /**
     * Gibt eine String-Repräsentation des Items zurück.
     *
     * @return Item-Identifikator (z.B. "DIAMOND", "SWORD:EXCALIBUR")
     */
    String getItemIdentifier();

    /**
     * Basispreis für ein Vanilla-Item.
     *
     * @param material Minecraft Material
     * @param buyPrice Ankaufspreis in Sternen (Spieler verkauft → NPC zahlt)
     * @param sellPrice Verkaufspreis in Sternen (Spieler kauft → Spieler zahlt)
     */
    record VanillaItemPrice(
            Material material,
            BigDecimal buyPrice,
            BigDecimal sellPrice
    ) implements ItemBasePrice {

        /**
         * Erstellt einen VanillaItemPrice.
         *
         * @param material Material
         * @param buyPrice Ankaufspreis
         * @param sellPrice Verkaufspreis
         * @throws IllegalArgumentException wenn Preise negativ
         */
        public VanillaItemPrice {
            if (buyPrice.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Ankaufspreis darf nicht negativ sein!");
            }
            if (sellPrice.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Verkaufspreis darf nicht negativ sein!");
            }
        }

        /**
         * Legacy-Konstruktor für Kompatibilität.
         *
         * @param material Material
         * @param price Einheitspreis (wird für Buy und Sell verwendet)
         * @deprecated Verwende VanillaItemPrice(Material, BigDecimal, BigDecimal)
         */
        @Deprecated
        public VanillaItemPrice(Material material, BigDecimal price) {
            this(material, price, price);
        }

        @Override
        @Deprecated
        public BigDecimal getPrice() {
            // Fallback auf Verkaufspreis
            return sellPrice;
        }

        @Override
        public BigDecimal getBuyPrice() {
            return buyPrice;
        }

        @Override
        public BigDecimal getSellPrice() {
            return sellPrice;
        }

        @Override
        public String getItemIdentifier() {
            return material.name();
        }
    }

    /**
     * Basispreis für ein Custom-Item (MMOItems, etc.).
     *
     * @param itemType Item-Type (z.B. "SWORD", "TOOL")
     * @param itemId Item-ID (z.B. "EXCALIBUR", "LEGENDARY_PICKAXE")
     * @param buyPrice Ankaufspreis in Sternen
     * @param sellPrice Verkaufspreis in Sternen
     */
    record CustomItemPrice(
            String itemType,
            String itemId,
            BigDecimal buyPrice,
            BigDecimal sellPrice
    ) implements ItemBasePrice {

        /**
         * Erstellt einen CustomItemPrice.
         *
         * @param itemType Type
         * @param itemId ID
         * @param buyPrice Ankaufspreis
         * @param sellPrice Verkaufspreis
         * @throws IllegalArgumentException wenn Preise negativ
         */
        public CustomItemPrice {
            if (buyPrice.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Ankaufspreis darf nicht negativ sein!");
            }
            if (sellPrice.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Verkaufspreis darf nicht negativ sein!");
            }
        }

        /**
         * Legacy-Konstruktor für Kompatibilität.
         *
         * @param itemType Type
         * @param itemId ID
         * @param price Einheitspreis
         * @deprecated Verwende CustomItemPrice(String, String, BigDecimal, BigDecimal)
         */
        @Deprecated
        public CustomItemPrice(String itemType, String itemId, BigDecimal price) {
            this(itemType, itemId, price, price);
        }

        @Override
        @Deprecated
        public BigDecimal getPrice() {
            return sellPrice;
        }

        @Override
        public BigDecimal getBuyPrice() {
            return buyPrice;
        }

        @Override
        public BigDecimal getSellPrice() {
            return sellPrice;
        }

        @Override
        public String getItemIdentifier() {
            return itemType + ":" + itemId;
        }
    }
}
