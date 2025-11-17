package de.fallenstar.economy.manager;

import de.fallenstar.economy.model.BankAccount;
import de.fallenstar.economy.model.CurrencyItemSet;
import de.fallenstar.items.manager.SpecialItemManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Logger;

/**
 * Verwaltet Bank-Konten für eine bestimmte Währung.
 *
 * Ein BankAccountHandler repräsentiert eine Ingame-Bank (z.B. Bankgebäude)
 * und verwaltet Spielerkonten für eine spezifische Währung (CurrencyItemSet).
 *
 * Features:
 * - Konten registrieren/verwalten
 * - Deposit: Münzen vom Inventar → Konto
 * - Withdraw: Konto → Münzen ins Inventar
 * - totalBankBalance: Bank hat eigenen Münz-Vorrat
 * - Auszahlung nur wenn Bank genug Liquidität hat
 *
 * Use Cases:
 * - Bankgebäude in Städten
 * - Spezial-Währungen (Dukaten, Kronen)
 * - Event-Währungen
 *
 * @author FallenStar
 * @version 1.0
 */
public class BankAccountHandler {

    private final Logger logger;
    private final SpecialItemManager itemManager;
    private final CurrencyItemSet currency;
    private final Map<UUID, BankAccount> accounts;
    private BigDecimal totalBankBalance;

    /**
     * Konstruktor für BankAccountHandler.
     *
     * @param logger Logger
     * @param itemManager SpecialItemManager (für Münz-Erstellung)
     * @param currency Währung die diese Bank verwaltet
     * @param initialBankBalance Initialer Münz-Vorrat der Bank
     */
    public BankAccountHandler(Logger logger, SpecialItemManager itemManager,
                               CurrencyItemSet currency, BigDecimal initialBankBalance) {
        this.logger = logger;
        this.itemManager = itemManager;
        this.currency = currency;
        this.accounts = new HashMap<>();
        this.totalBankBalance = initialBankBalance;

        logger.info("BankAccountHandler für " + currency.namePlural() +
                " initialisiert (Bank-Balance: " + totalBankBalance + ")");
    }

    /**
     * Registriert ein neues Konto für einen Spieler.
     *
     * @param player Spieler
     * @return true wenn erfolgreich, false wenn bereits existiert
     */
    public boolean registerAccount(Player player) {
        UUID playerId = player.getUniqueId();

        if (accounts.containsKey(playerId)) {
            logger.fine("Konto für " + player.getName() + " existiert bereits");
            return false;
        }

        BankAccount account = BankAccount.createEmpty(playerId, currency.currencyId());
        accounts.put(playerId, account);

        logger.info("Neues Konto erstellt für " + player.getName() +
                " (" + currency.namePlural() + ")");

        return true;
    }

    /**
     * Prüft ob ein Spieler ein Konto hat.
     *
     * @param playerId Spieler-UUID
     * @return true wenn Konto existiert
     */
    public boolean hasAccount(UUID playerId) {
        return accounts.containsKey(playerId);
    }

    /**
     * Gibt ein Konto zurück.
     *
     * @param playerId Spieler-UUID
     * @return Optional mit BankAccount
     */
    public Optional<BankAccount> getAccount(UUID playerId) {
        return Optional.ofNullable(accounts.get(playerId));
    }

    /**
     * Zahlt Münzen vom Spieler-Inventar auf das Bank-Konto ein.
     *
     * Diese Methode:
     * 1. Sucht Münzen im Spieler-Inventar
     * 2. Entfernt sie aus dem Inventar
     * 3. Erhöht den Kontostand
     * 4. Erhöht totalBankBalance (Bank erhält die Münzen)
     *
     * @param player Spieler
     * @param tier Münz-Tier (BRONZE, SILVER, GOLD)
     * @param amount Anzahl der Münzen
     * @return Tatsächlich eingezahlter Betrag (kann weniger sein wenn nicht genug im Inventar)
     */
    public int deposit(Player player, CurrencyItemSet.CurrencyTier tier, int amount) {
        UUID playerId = player.getUniqueId();

        // Prüfe ob Konto existiert
        if (!hasAccount(playerId)) {
            logger.warning("Kein Konto für " + player.getName() + " - deposit fehlgeschlagen");
            return 0;
        }

        // Suche Münzen im Inventar
        CurrencyItemSet.InventoryCoinResult coinResult = currency.findCoinsInInventory(
                player.getInventory(),
                tier,
                (stack, itemId) -> itemManager.isSpecialItem(stack)
        );

        if (!coinResult.hasCoins()) {
            logger.fine("Keine " + tier + " " + currency.namePlural() + " im Inventar von " + player.getName());
            return 0;
        }

        // Berechne tatsächlichen Betrag (kann weniger sein als gewünscht)
        int actualAmount = Math.min(amount, coinResult.totalAmount());

        // Entferne Münzen aus Inventar
        int remainingToRemove = actualAmount;
        for (CurrencyItemSet.InventorySlotStack slotStack : coinResult.stacks()) {
            if (remainingToRemove <= 0) break;

            ItemStack stack = slotStack.stack();
            int stackAmount = stack.getAmount();

            if (stackAmount <= remainingToRemove) {
                // Ganzen Stack entfernen
                player.getInventory().setItem(slotStack.slot(), null);
                remainingToRemove -= stackAmount;
            } else {
                // Teilmenge entfernen
                stack.setAmount(stackAmount - remainingToRemove);
                remainingToRemove = 0;
            }
        }

        // Berechne Wert in Basiseinheiten
        BigDecimal depositValue = currency.calculateValue(tier, actualAmount);

        // Aktualisiere Kontostand
        BankAccount account = accounts.get(playerId);
        BigDecimal newBalance = account.balance().add(depositValue);
        accounts.put(playerId, account.withBalance(newBalance));

        // Erhöhe Bank-Balance
        totalBankBalance = totalBankBalance.add(depositValue);

        logger.info("Einzahlung: " + actualAmount + "x " + tier + " " + currency.namePlural() +
                " von " + player.getName() + " (Neuer Stand: " + newBalance + ")");

        return actualAmount;
    }

    /**
     * Hebt Münzen vom Bank-Konto ab und gibt sie ins Spieler-Inventar.
     *
     * Diese Methode:
     * 1. Prüft Kontostand (genug Guthaben?)
     * 2. Prüft totalBankBalance (genug Liquidität?)
     * 3. Zahlt maximal möglichen Betrag aus
     * 4. Verringert Kontostand und totalBankBalance
     * 5. Gibt Münzen ins Inventar
     *
     * @param player Spieler
     * @param tier Münz-Tier (BRONZE, SILVER, GOLD)
     * @param requestedAmount Gewünschte Anzahl
     * @return Tatsächlich ausgezahlter Betrag (0 wenn fehlgeschlagen)
     */
    public int withdraw(Player player, CurrencyItemSet.CurrencyTier tier, int requestedAmount) {
        UUID playerId = player.getUniqueId();

        // Prüfe ob Konto existiert
        if (!hasAccount(playerId)) {
            logger.warning("Kein Konto für " + player.getName() + " - withdraw fehlgeschlagen");
            return 0;
        }

        BankAccount account = accounts.get(playerId);

        // Berechne Kosten
        BigDecimal costPerCoin = currency.calculateValue(tier, 1);
        BigDecimal totalCost = costPerCoin.multiply(BigDecimal.valueOf(requestedAmount));

        // Prüfe Kontostand
        int actualAmount = requestedAmount;
        BigDecimal actualCost = totalCost;

        if (!account.hasEnough(totalCost)) {
            // Berechne höchstmögliche Menge basierend auf Kontostand
            actualAmount = account.balance().divide(costPerCoin, 0, BigDecimal.ROUND_DOWN).intValue();
            actualCost = costPerCoin.multiply(BigDecimal.valueOf(actualAmount));

            if (actualAmount <= 0) {
                logger.fine("Nicht genug Guthaben für " + player.getName() +
                        " (benötigt: " + totalCost + ", hat: " + account.balance() + ")");
                return 0;
            }

            logger.fine("Guthaben reicht nur für " + actualAmount + " statt " + requestedAmount + " Münzen");
        }

        // Prüfe Bank-Liquidität
        if (totalBankBalance.compareTo(actualCost) < 0) {
            // Bank hat nicht genug Münzen - reduziere Auszahlung
            int maxBankAmount = totalBankBalance.divide(costPerCoin, 0, BigDecimal.ROUND_DOWN).intValue();
            if (maxBankAmount < actualAmount) {
                actualAmount = maxBankAmount;
                actualCost = costPerCoin.multiply(BigDecimal.valueOf(actualAmount));

                if (actualAmount <= 0) {
                    logger.warning("Bank hat keine Liquidität mehr!");
                    return 0;
                }

                logger.fine("Bank-Liquidität reicht nur für " + actualAmount + " Münzen");
            }
        }

        // Erstelle Münzen
        String itemId = currency.getItemId(tier);
        Optional<ItemStack> coins = itemManager.createItem(itemId, actualAmount);

        if (coins.isEmpty()) {
            logger.warning("Konnte Münzen nicht erstellen: " + itemId);
            return 0;
        }

        // Gebe Münzen an Spieler
        player.getInventory().addItem(coins.get());

        // Aktualisiere Kontostand
        BigDecimal newBalance = account.balance().subtract(actualCost);
        accounts.put(playerId, account.withBalance(newBalance));

        // Verringere Bank-Balance
        totalBankBalance = totalBankBalance.subtract(actualCost);

        logger.info("Auszahlung: " + actualAmount + "x " + tier + " " + currency.namePlural() +
                " an " + player.getName() + " (Neuer Stand: " + newBalance + ", Bank: " + totalBankBalance + ")");

        return actualAmount;
    }

    /**
     * Gibt die Gesamtliquidität der Bank zurück.
     *
     * @return Total Bank Balance
     */
    public BigDecimal getTotalBankBalance() {
        return totalBankBalance;
    }

    /**
     * Setzt die Bank-Liquidität (z.B. Admin-Befehl).
     *
     * @param newBalance Neue Bank-Balance
     */
    public void setTotalBankBalance(BigDecimal newBalance) {
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            logger.warning("Versuch, negative Bank-Balance zu setzen!");
            return;
        }

        totalBankBalance = newBalance;
        logger.info("Bank-Balance geändert: " + totalBankBalance);
    }

    /**
     * Gibt die Währung zurück die diese Bank verwaltet.
     *
     * @return CurrencyItemSet
     */
    public CurrencyItemSet getCurrency() {
        return currency;
    }

    /**
     * Gibt die Anzahl registrierter Konten zurück.
     *
     * @return Anzahl Konten
     */
    public int getAccountCount() {
        return accounts.size();
    }

    /**
     * Gibt die Summe aller Kontostände zurück.
     *
     * @return Gesamtguthaben aller Spieler
     */
    public BigDecimal getTotalPlayerBalance() {
        return accounts.values().stream()
                .map(BankAccount::balance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
