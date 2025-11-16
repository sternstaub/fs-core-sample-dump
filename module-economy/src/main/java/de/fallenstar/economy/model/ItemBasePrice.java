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
     * Gibt den Basispreis zurück.
     *
     * @return Preis in Basiswährung (Sterne)
     */
    BigDecimal getPrice();

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
     * @param price Basispreis in Sternen
     */
    record VanillaItemPrice(
            Material material,
            BigDecimal price
    ) implements ItemBasePrice {

        /**
         * Erstellt einen VanillaItemPrice.
         *
         * @param material Material
         * @param price Preis
         * @throws IllegalArgumentException wenn price negativ
         */
        public VanillaItemPrice {
            if (price.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Preis darf nicht negativ sein!");
            }
        }

        @Override
        public BigDecimal getPrice() {
            return price;
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
     * @param price Basispreis in Sternen
     */
    record CustomItemPrice(
            String itemType,
            String itemId,
            BigDecimal price
    ) implements ItemBasePrice {

        /**
         * Erstellt einen CustomItemPrice.
         *
         * @param itemType Type
         * @param itemId ID
         * @param price Preis
         * @throws IllegalArgumentException wenn price negativ
         */
        public CustomItemPrice {
            if (price.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Preis darf nicht negativ sein!");
            }
        }

        @Override
        public BigDecimal getPrice() {
            return price;
        }

        @Override
        public String getItemIdentifier() {
            return itemType + ":" + itemId;
        }
    }
}
