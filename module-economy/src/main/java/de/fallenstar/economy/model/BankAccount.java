package de.fallenstar.economy.model;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Repräsentiert ein Bankkonto für einen Spieler in einem BankAccountHandler.
 *
 * Ein BankAccount ist gebunden an:
 * - Einen Spieler (UUID)
 * - Eine Währung (CurrencyItemSet)
 * - Einen Kontostand (BigDecimal)
 *
 * Bank-Konten werden von BankAccountHandler verwaltet und sind unabhängig
 * vom Vault-Economy-System. Sie ermöglichen Ingame-Banken mit eigener
 * Währung und Liquidität.
 *
 * @param playerId UUID des Spielers
 * @param currencyId ID der Währung (z.B. "sterne", "dukaten")
 * @param balance Aktueller Kontostand
 *
 * @author FallenStar
 * @version 1.0
 */
public record BankAccount(
        UUID playerId,
        String currencyId,
        BigDecimal balance
) {

    /**
     * Erstellt ein neues BankAccount.
     *
     * @param playerId Spieler-UUID
     * @param currencyId Währungs-ID
     * @param balance Kontostand
     * @throws IllegalArgumentException wenn balance negativ
     */
    public BankAccount {
        if (balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Kontostand darf nicht negativ sein!");
        }
    }

    /**
     * Erstellt ein neues Konto mit Balance 0.
     *
     * @param playerId Spieler-UUID
     * @param currencyId Währungs-ID
     * @return Neues BankAccount mit Balance 0
     */
    public static BankAccount createEmpty(UUID playerId, String currencyId) {
        return new BankAccount(playerId, currencyId, BigDecimal.ZERO);
    }

    /**
     * Erstellt eine Kopie dieses Kontos mit neuer Balance.
     *
     * @param newBalance Neue Balance
     * @return Neues BankAccount mit aktualisierter Balance
     */
    public BankAccount withBalance(BigDecimal newBalance) {
        return new BankAccount(playerId, currencyId, newBalance);
    }

    /**
     * Prüft ob das Konto genug Guthaben für einen Betrag hat.
     *
     * @param amount Gewünschter Betrag
     * @return true wenn balance >= amount
     */
    public boolean hasEnough(BigDecimal amount) {
        return balance.compareTo(amount) >= 0;
    }

    /**
     * Prüft ob das Konto leer ist (Balance = 0).
     *
     * @return true wenn Balance 0
     */
    public boolean isEmpty() {
        return balance.compareTo(BigDecimal.ZERO) == 0;
    }
}
