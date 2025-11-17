package de.fallenstar.economy.model;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Repräsentiert ein Handels-Angebot (Input → Output).
 *
 * Features:
 * - Ankauf und Verkauf-Preise
 * - Münz-basierte Preise (in Basiswährung)
 * - Mehrere Inputs (Input1 + Input2 optional)
 * - Output-Item
 * - MerchantRecipe-Konvertierung
 *
 * **Verwendung:**
 * <pre>
 * // Erstelle TradeSet: 10 Diamanten → 100 Bronzesterne
 * TradeSet trade = new TradeSet(
 *     new ItemStack(Material.DIAMOND, 10),  // Input
 *     null,                                   // Kein zweiter Input
 *     coinStack,                              // Output (100 Bronzesterne)
 *     BigDecimal.valueOf(90),                 // Ankaufpreis (NPC zahlt 90)
 *     BigDecimal.valueOf(110),                // Verkaufspreis (Spieler zahlt 110)
 *     -1                                      // Unbegrenzte Trades
 * );
 * </pre>
 *
 * @author FallenStar
 * @version 1.0
 */
public class TradeSet {

    private final UUID tradeId;
    private final ItemStack input1;          // Haupt-Input (erforderlich)
    private final ItemStack input2;          // Optionaler zweiter Input
    private final ItemStack output;          // Output-Item
    private final BigDecimal buyPrice;       // Ankaufpreis (Spieler verkauft an NPC)
    private final BigDecimal sellPrice;      // Verkaufspreis (Spieler kauft von NPC)
    private final int maxUses;               // Maximale Anzahl Trades (-1 = unbegrenzt)

    /**
     * Konstruktor für TradeSet.
     *
     * @param input1 Haupt-Input (erforderlich)
     * @param input2 Optionaler zweiter Input (kann null sein)
     * @param output Output-Item
     * @param buyPrice Ankaufpreis (Spieler verkauft an NPC)
     * @param sellPrice Verkaufspreis (Spieler kauft von NPC)
     * @param maxUses Maximale Anzahl Trades (-1 = unbegrenzt)
     */
    public TradeSet(
            ItemStack input1,
            ItemStack input2,
            ItemStack output,
            BigDecimal buyPrice,
            BigDecimal sellPrice,
            int maxUses
    ) {
        this.tradeId = UUID.randomUUID();
        this.input1 = input1.clone();
        this.input2 = input2 != null ? input2.clone() : null;
        this.output = output.clone();
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.maxUses = maxUses;
    }

    /**
     * Gibt die Trade-ID zurück.
     *
     * @return UUID des Trades
     */
    public UUID getTradeId() {
        return tradeId;
    }

    /**
     * Gibt den Haupt-Input zurück.
     *
     * @return Geklontes Input-Item
     */
    public ItemStack getInput1() {
        return input1.clone();
    }

    /**
     * Gibt den optionalen zweiten Input zurück.
     *
     * @return Geklontes Input-Item oder null
     */
    public ItemStack getInput2() {
        return input2 != null ? input2.clone() : null;
    }

    /**
     * Gibt den Output zurück.
     *
     * @return Geklontes Output-Item
     */
    public ItemStack getOutput() {
        return output.clone();
    }

    /**
     * Gibt den Ankaufpreis zurück (Spieler verkauft an NPC).
     *
     * @return Preis in Basiswährung
     */
    public BigDecimal getBuyPrice() {
        return buyPrice;
    }

    /**
     * Gibt den Verkaufspreis zurück (Spieler kauft von NPC).
     *
     * @return Preis in Basiswährung
     */
    public BigDecimal getSellPrice() {
        return sellPrice;
    }

    /**
     * Gibt die maximale Anzahl Trades zurück.
     *
     * @return Max Uses (-1 = unbegrenzt)
     */
    public int getMaxUses() {
        return maxUses;
    }

    /**
     * Prüft ob dieser Trade einen zweiten Input hat.
     *
     * @return true wenn Input2 vorhanden
     */
    public boolean hasTwoInputs() {
        return input2 != null;
    }

    /**
     * Erstellt ein MerchantRecipe aus diesem TradeSet.
     *
     * Verwendet für Vanilla Merchant-Interface.
     *
     * @return MerchantRecipe
     */
    public MerchantRecipe createRecipe() {
        // Berechne effektive maxUses (999 für unbegrenzt)
        int effectiveMaxUses = maxUses == -1 ? 999 : maxUses;

        // Erstelle Rezept
        MerchantRecipe recipe = new MerchantRecipe(output.clone(), effectiveMaxUses);

        // Füge Inputs hinzu
        recipe.addIngredient(input1.clone());
        if (input2 != null) {
            recipe.addIngredient(input2.clone());
        }

        // Kein XP für Trades
        recipe.setExperienceReward(false);

        return recipe;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("TradeSet{");
        sb.append("id=").append(tradeId);
        sb.append(", input1=").append(input1.getType()).append("x").append(input1.getAmount());

        if (input2 != null) {
            sb.append(", input2=").append(input2.getType()).append("x").append(input2.getAmount());
        }

        sb.append(", output=").append(output.getType()).append("x").append(output.getAmount());
        sb.append(", buyPrice=").append(buyPrice);
        sb.append(", sellPrice=").append(sellPrice);
        sb.append(", maxUses=").append(maxUses == -1 ? "∞" : maxUses);
        sb.append('}');

        return sb.toString();
    }
}
