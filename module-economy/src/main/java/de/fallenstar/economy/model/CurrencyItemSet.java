package de.fallenstar.economy.model;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Repräsentiert eine Währung mit 3 Tiers (Bronze/Silber/Gold) und Wechselkurs.
 *
 * Ein CurrencyItemSet definiert eine vollständige Währung:
 * - Bronze-Tier (1er Münze, Wert: 1)
 * - Silber-Tier (10er Münze, Wert: 10)
 * - Gold-Tier (100er Münze, Wert: 100)
 * - Wechselkurs zur Basiswährung (Sterne)
 *
 * Beispiele:
 * - Sterne: Wechselkurs 1.0 (Basiswährung), Singular="Stern", Plural="Sterne"
 * - Dukaten: Wechselkurs 1.2 (1 Dukat = 1.2 Sterne), Singular="Dukat", Plural="Dukaten"
 * - Kronen: Wechselkurs 0.8 (1 Krone = 0.8 Sterne), Singular="Krone", Plural="Kronen"
 *
 * @author FallenStar
 * @version 2.0
 */
public record CurrencyItemSet(
        String currencyId,              // Eindeutige ID (z.B. "sterne", "dukaten")
        String displayName,             // Anzeigename (z.B. "Sterne", "Dukaten") - deprecated, use namePlural
        String nameSingular,            // Währungsname Singular (z.B. "Stern", "Dukat")
        String namePlural,              // Währungsname Plural (z.B. "Sterne", "Dukaten")
        String bronzeItemId,            // SpecialItem-ID für Bronze-Tier (1er)
        String silverItemId,            // SpecialItem-ID für Silber-Tier (10er)
        String goldItemId,              // SpecialItem-ID für Gold-Tier (100er)
        BigDecimal exchangeRate         // Wechselkurs zur Basiswährung
) {

    /**
     * Erstellt ein CurrencyItemSet.
     *
     * @param currencyId Eindeutige ID
     * @param displayName Anzeigename (deprecated, use namePlural)
     * @param nameSingular Währungsname Singular
     * @param namePlural Währungsname Plural
     * @param bronzeItemId SpecialItem-ID für Bronze-Tier
     * @param silverItemId SpecialItem-ID für Silber-Tier
     * @param goldItemId SpecialItem-ID für Gold-Tier
     * @param exchangeRate Wechselkurs zur Basiswährung
     * @throws IllegalArgumentException wenn exchangeRate <= 0
     */
    public CurrencyItemSet {
        if (exchangeRate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Wechselkurs muss größer als 0 sein!");
        }
    }

    /**
     * Erstellt die Basiswährung "Sterne" (Wechselkurs 1:1).
     *
     * @return CurrencyItemSet für Sterne
     */
    public static CurrencyItemSet createBaseCurrency() {
        return new CurrencyItemSet(
                "sterne",
                "Sterne",          // displayName (deprecated)
                "Stern",           // nameSingular
                "Sterne",          // namePlural
                "bronze_stern",    // Bronzestern (aus Items-Modul)
                "silver_stern",    // Silberstern (aus Items-Modul)
                "gold_stern",      // Goldstern (aus Items-Modul)
                BigDecimal.ONE     // Wechselkurs 1:1
        );
    }

    /**
     * Prüft ob diese Währung die Basiswährung ist.
     *
     * @return true wenn Wechselkurs = 1.0
     */
    public boolean isBaseCurrency() {
        return exchangeRate.compareTo(BigDecimal.ONE) == 0;
    }

    /**
     * Konvertiert einen Betrag in dieser Währung zur Basiswährung.
     *
     * Beispiel: 100 Dukaten (Wechselkurs 1.2) = 120 Sterne
     *
     * @param amount Betrag in dieser Währung
     * @return Äquivalenter Betrag in Basiswährung
     */
    public BigDecimal toBaseCurrency(BigDecimal amount) {
        return amount.multiply(exchangeRate);
    }

    /**
     * Konvertiert einen Betrag von der Basiswährung zu dieser Währung.
     *
     * Beispiel: 120 Sterne → 100 Dukaten (Wechselkurs 1.2)
     *
     * @param baseAmount Betrag in Basiswährung
     * @return Äquivalenter Betrag in dieser Währung
     */
    public BigDecimal fromBaseCurrency(BigDecimal baseAmount) {
        return baseAmount.divide(exchangeRate, 2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Gibt den Wert eines Tiers in Basiseinheiten zurück.
     *
     * @param tier Tier (BRONZE, SILVER, GOLD)
     * @return Wert in Basiseinheiten (1, 10, 100)
     */
    public int getTierValue(CurrencyTier tier) {
        return switch (tier) {
            case BRONZE -> 1;
            case SILVER -> 10;
            case GOLD -> 100;
        };
    }

    /**
     * Gibt die SpecialItem-ID für einen Tier zurück.
     *
     * @param tier Tier (BRONZE, SILVER, GOLD)
     * @return SpecialItem-ID
     */
    public String getItemId(CurrencyTier tier) {
        return switch (tier) {
            case BRONZE -> bronzeItemId;
            case SILVER -> silverItemId;
            case GOLD -> goldItemId;
        };
    }

    /**
     * Gibt den Display-Namen für einen Tier mit korrekter Singular/Plural-Form zurück.
     *
     * Beispiele:
     * - Bronze, 1 → "Bronze-Stern"
     * - Bronze, 10 → "Bronze-Sterne"
     * - Silber, 1 → "Silber-Stern"
     *
     * @param tier Tier (BRONZE, SILVER, GOLD)
     * @param count Anzahl (für Singular/Plural-Unterscheidung)
     * @return Display-Name (z.B. "Bronze-Sterne", "Silber-Stern")
     */
    public String getTierDisplayName(CurrencyTier tier, int count) {
        String tierPrefix = switch (tier) {
            case BRONZE -> "Bronze";
            case SILVER -> "Silber";
            case GOLD -> "Gold";
        };

        String currencyName = (count == 1) ? nameSingular : namePlural;
        return tierPrefix + "-" + currencyName.toLowerCase();
    }

    /**
     * Sucht Münzen eines bestimmten Tiers in einem Inventar.
     *
     * Diese Methode findet alle ItemStacks im Inventar, die Münzen des angegebenen Tiers
     * repräsentieren, und gibt sie zusammen mit der Gesamtmenge zurück.
     *
     * @param inventory Inventar zum Durchsuchen
     * @param tier Münz-Tier (BRONZE, SILVER, GOLD)
     * @param itemChecker Funktion zum Prüfen ob ItemStack eine Münze ist (SpecialItem-Check)
     * @return InventoryCoinResult mit gefundenen Münzen
     */
    public InventoryCoinResult findCoinsInInventory(Inventory inventory, CurrencyTier tier,
                                                      CoinItemChecker itemChecker) {
        String targetItemId = getItemId(tier);
        List<InventorySlotStack> foundStacks = new ArrayList<>();
        int totalAmount = 0;

        ItemStack[] contents = inventory.getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack stack = contents[i];
            if (stack == null || stack.getType().isAir()) {
                continue;
            }

            // Prüfe ob dieser Stack eine Münze des gesuchten Tiers ist
            if (itemChecker.isCoin(stack, targetItemId)) {
                int amount = stack.getAmount();
                foundStacks.add(new InventorySlotStack(i, stack, amount));
                totalAmount += amount;
            }
        }

        return new InventoryCoinResult(foundStacks, totalAmount);
    }

    /**
     * Berechnet den Wert einer Anzahl von Münzen in Basiseinheiten.
     *
     * @param tier Münz-Tier
     * @param amount Anzahl der Münzen
     * @return Wert in Basiseinheiten
     */
    public BigDecimal calculateValue(CurrencyTier tier, int amount) {
        int tierValue = getTierValue(tier);
        return BigDecimal.valueOf(tierValue).multiply(BigDecimal.valueOf(amount));
    }

    /**
     * Functional Interface zum Prüfen ob ein ItemStack eine Münze ist.
     *
     * Wird von SpecialItemManager bereitgestellt.
     */
    @FunctionalInterface
    public interface CoinItemChecker {
        /**
         * Prüft ob ein ItemStack eine bestimmte Münze ist.
         *
         * @param stack ItemStack zum Prüfen
         * @param itemId SpecialItem-ID der gesuchten Münze
         * @return true wenn der Stack die gesuchte Münze ist
         */
        boolean isCoin(ItemStack stack, String itemId);
    }

    /**
     * Ergebnis einer Inventar-Münzsuche.
     *
     * @param stacks Gefundene ItemStacks mit Slot-Informationen
     * @param totalAmount Gesamtanzahl der gefundenen Münzen
     */
    public record InventoryCoinResult(
            List<InventorySlotStack> stacks,
            int totalAmount
    ) {
        /**
         * Prüft ob Münzen gefunden wurden.
         *
         * @return true wenn totalAmount > 0
         */
        public boolean hasCoins() {
            return totalAmount > 0;
        }

        /**
         * Prüft ob mindestens die gewünschte Menge gefunden wurde.
         *
         * @param requiredAmount Gewünschte Menge
         * @return true wenn genug gefunden wurde
         */
        public boolean hasEnough(int requiredAmount) {
            return totalAmount >= requiredAmount;
        }
    }

    /**
     * Repräsentiert einen ItemStack in einem bestimmten Inventar-Slot.
     *
     * @param slot Slot-Index (0-based)
     * @param stack ItemStack
     * @param amount Anzahl (für Convenience)
     */
    public record InventorySlotStack(
            int slot,
            ItemStack stack,
            int amount
    ) {
    }

    /**
     * Currency Tier Enum.
     */
    public enum CurrencyTier {
        BRONZE,  // 1er Münze
        SILVER,  // 10er Münze
        GOLD;    // 100er Münze

        /**
         * Parst einen Tier-Namen.
         *
         * @param name Tier-Name (case-insensitive)
         * @return CurrencyTier oder null
         */
        public static CurrencyTier fromString(String name) {
            try {
                return valueOf(name.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }
}
