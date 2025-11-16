package de.fallenstar.economy.model;

import java.math.BigDecimal;

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
 * - Sterne: Wechselkurs 1.0 (Basiswährung)
 * - Dukaten: Wechselkurs 1.2 (1 Dukat = 1.2 Sterne)
 * - Kronen: Wechselkurs 0.8 (1 Krone = 0.8 Sterne)
 *
 * @author FallenStar
 * @version 1.0
 */
public record CurrencyItemSet(
        String currencyId,              // Eindeutige ID (z.B. "sterne", "dukaten")
        String displayName,             // Anzeigename (z.B. "Sterne", "Dukaten")
        String bronzeItemId,            // SpecialItem-ID für Bronze-Tier (1er)
        String silverItemId,            // SpecialItem-ID für Silber-Tier (10er)
        String goldItemId,              // SpecialItem-ID für Gold-Tier (100er)
        BigDecimal exchangeRate         // Wechselkurs zur Basiswährung
) {

    /**
     * Erstellt ein CurrencyItemSet.
     *
     * @param currencyId Eindeutige ID
     * @param displayName Anzeigename
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
                "Sterne",
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
