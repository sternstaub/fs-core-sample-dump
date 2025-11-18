package de.fallenstar.economy.manager;

import de.fallenstar.core.provider.EconomyProvider;
import de.fallenstar.core.provider.ItemProvider;
import de.fallenstar.economy.model.CurrencyItemSet;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Verwaltet Währungen und Münz-Transaktionen im Economy-System.
 *
 * Funktionen:
 * - Registrierung von Währungen (CurrencyItemSet)
 * - Auszahlung von Münzen an Spieler
 * - Wechselkurs-Berechnungen
 * - Integration mit ItemProvider (Core-Interface)
 *
 * @author FallenStar
 * @version 2.0 - Refactored: SpecialItemManager → ItemProvider (eliminiert Module-Dependency)
 */
public class CurrencyManager {

    private final Logger logger;
    private final ItemProvider itemProvider;
    private final Map<String, CurrencyItemSet> currencies;
    private CurrencyItemSet baseCurrency;
    private EconomyProvider economyProvider;

    /**
     * Konstruktor für CurrencyManager.
     *
     * @param logger Logger
     * @param itemProvider ItemProvider (Core-Interface, eliminiert Items-Modul Dependency)
     */
    public CurrencyManager(Logger logger, ItemProvider itemProvider) {
        this.logger = logger;
        this.itemProvider = itemProvider;
        this.currencies = new HashMap<>();

        logger.info("CurrencyManager initialisiert");
    }

    /**
     * Setzt den EconomyProvider (für Vault-Integration).
     *
     * @param economyProvider EconomyProvider
     */
    public void setEconomyProvider(EconomyProvider economyProvider) {
        this.economyProvider = economyProvider;
        logger.info("EconomyProvider für CurrencyManager gesetzt: " +
                (economyProvider.isAvailable() ? "verfügbar" : "nicht verfügbar"));
    }

    /**
     * Registriert eine Währung.
     *
     * @param currency CurrencyItemSet
     */
    public void registerCurrency(CurrencyItemSet currency) {
        currencies.put(currency.currencyId(), currency);

        // Setze Basiswährung (Wechselkurs = 1.0)
        if (currency.isBaseCurrency()) {
            baseCurrency = currency;
            logger.info("Basiswährung registriert: " + currency.displayName());
        } else {
            logger.info("Währung registriert: " + currency.displayName() +
                    " (Wechselkurs: " + currency.exchangeRate() + ")");
        }
    }

    /**
     * Zahlt Münzen an einen Spieler aus (ohne Vault-Abbuchung).
     *
     * @param player Spieler
     * @param currencyId Währungs-ID
     * @param tier Münz-Tier (BRONZE, SILVER, GOLD)
     * @param amount Anzahl
     * @return true wenn erfolgreich
     */
    public boolean payoutCoins(Player player, String currencyId,
                                CurrencyItemSet.CurrencyTier tier, int amount) {
        Optional<CurrencyItemSet> currencyOpt = getCurrency(currencyId);

        if (currencyOpt.isEmpty()) {
            logger.warning("Währung nicht gefunden: " + currencyId);
            return false;
        }

        CurrencyItemSet currency = currencyOpt.get();
        String itemId = currency.getItemId(tier);

        // Erstelle Münzen via ItemProvider (eliminiert Items-Modul Dependency!)
        Optional<ItemStack> coins;
        try {
            coins = itemProvider.getSpecialItem(itemId, amount);
        } catch (Exception e) {
            logger.warning("Fehler beim Erstellen von Münzen: " + e.getMessage());
            return false;
        }

        if (coins.isEmpty()) {
            logger.warning("Konnte Münzen nicht erstellen: " + itemId);
            return false;
        }

        // Gebe Items an Spieler
        player.getInventory().addItem(coins.get());
        logger.fine("Ausgezahlt: " + amount + "x " + tier + " " + currency.displayName() +
                " an " + player.getName());

        return true;
    }

    /**
     * Zieht Münzen vom Vault-Konto ab und zahlt sie als Items aus.
     *
     * Falls das Konto nicht genug enthält, wird der höchstmögliche Betrag ausgezahlt.
     *
     * @param player Spieler
     * @param currencyId Währungs-ID
     * @param tier Münz-Tier (BRONZE, SILVER, GOLD)
     * @param requestedAmount Gewünschte Anzahl
     * @return Ausgezahlte Anzahl (0 wenn fehlgeschlagen)
     */
    public int withdrawCoins(Player player, String currencyId,
                              CurrencyItemSet.CurrencyTier tier, int requestedAmount) {
        // Prüfe ob EconomyProvider verfügbar
        if (economyProvider == null || !economyProvider.isAvailable()) {
            logger.warning("EconomyProvider nicht verfügbar - Auszahlung nicht möglich!");
            return 0;
        }

        // Prüfe ob Währung existiert
        Optional<CurrencyItemSet> currencyOpt = getCurrency(currencyId);
        if (currencyOpt.isEmpty()) {
            logger.warning("Währung nicht gefunden: " + currencyId);
            return 0;
        }

        CurrencyItemSet currency = currencyOpt.get();

        // Berechne Kosten in Basiswährung
        BigDecimal costPerCoin = calculateCoinCost(currency, tier);
        BigDecimal totalCost = costPerCoin.multiply(BigDecimal.valueOf(requestedAmount));

        // Hole Vault-Balance
        double balance;
        try {
            balance = economyProvider.getBalance(player);
        } catch (Exception e) {
            logger.warning("Fehler beim Abrufen der Balance: " + e.getMessage());
            return 0;
        }

        // Prüfe ob genug Guthaben vorhanden
        int actualAmount = requestedAmount;
        BigDecimal actualCost = totalCost;

        if (balance < totalCost.doubleValue()) {
            // Berechne höchstmögliche Menge
            actualAmount = (int) (balance / costPerCoin.doubleValue());
            actualCost = costPerCoin.multiply(BigDecimal.valueOf(actualAmount));

            if (actualAmount <= 0) {
                logger.fine("Nicht genug Guthaben für " + player.getName() +
                        " (benötigt: " + totalCost + ", hat: " + balance + ")");
                return 0;
            }

            logger.fine("Guthaben reicht nur für " + actualAmount + " statt " +
                    requestedAmount + " Münzen");
        }

        // Ziehe von Vault ab
        try {
            boolean success = economyProvider.withdraw(player, actualCost.doubleValue());
            if (!success) {
                logger.warning("Vault-Abbuchung fehlgeschlagen für " + player.getName());
                return 0;
            }
        } catch (Exception e) {
            logger.warning("Fehler bei Vault-Abbuchung: " + e.getMessage());
            return 0;
        }

        // Zahle Münzen aus
        boolean payoutSuccess = payoutCoins(player, currencyId, tier, actualAmount);

        if (!payoutSuccess) {
            // Rollback: Geld zurückerstatten
            try {
                economyProvider.deposit(player, actualCost.doubleValue());
                logger.warning("Münz-Auszahlung fehlgeschlagen - Geld zurückerstattet");
            } catch (Exception e) {
                logger.severe("KRITISCH: Rollback fehlgeschlagen! Spieler " + player.getName() +
                        " hat " + actualCost + " verloren!");
            }
            return 0;
        }

        logger.info("Ausgezahlt an " + player.getName() + ": " + actualAmount + "x " +
                tier + " " + currency.displayName() + " (Kosten: " + actualCost + ")");

        return actualAmount;
    }

    /**
     * Berechnet die Kosten einer einzelnen Münze in Basiswährung.
     *
     * @param currency Währung
     * @param tier Münz-Tier
     * @return Kosten in Basiswährung
     */
    private BigDecimal calculateCoinCost(CurrencyItemSet currency, CurrencyItemSet.CurrencyTier tier) {
        // Tier-Wert (1, 10, 100)
        BigDecimal tierValue = BigDecimal.valueOf(currency.getTierValue(tier));

        // Wechselkurs zur Basiswährung
        BigDecimal exchangeRate = currency.exchangeRate();

        // Kosten = Tier-Wert * Wechselkurs
        return tierValue.multiply(exchangeRate);
    }

    /**
     * Gibt eine Währung zurück.
     *
     * @param currencyId Währungs-ID
     * @return Optional mit CurrencyItemSet
     */
    public Optional<CurrencyItemSet> getCurrency(String currencyId) {
        return Optional.ofNullable(currencies.get(currencyId));
    }

    /**
     * Gibt die Basiswährung zurück.
     *
     * @return Basiswährung (Sterne)
     */
    public CurrencyItemSet getBaseCurrency() {
        return baseCurrency;
    }

    /**
     * Gibt alle registrierten Währungs-IDs zurück.
     *
     * @return Set von IDs
     */
    public Set<String> getCurrencyIds() {
        return currencies.keySet();
    }

    /**
     * Gibt die Anzahl registrierter Währungen zurück.
     *
     * @return Anzahl
     */
    public int getCurrencyCount() {
        return currencies.size();
    }

    /**
     * Erstellt Münzen als ItemStack (ohne Auszahlung an Spieler).
     *
     * Diese Methode wird von TradeSet-Generatoren verwendet, um Münzen
     * für Handelstransaktionen zu erstellen.
     *
     * @param currencyId Währungs-ID (z.B. "sterne")
     * @param tier Münz-Tier (BRONZE, SILVER, GOLD)
     * @param amount Anzahl
     * @return ItemStack mit Münzen, oder null wenn fehlgeschlagen
     */
    public ItemStack createCoin(String currencyId, CurrencyItemSet.CurrencyTier tier, int amount) {
        Optional<CurrencyItemSet> currencyOpt = getCurrency(currencyId);

        if (currencyOpt.isEmpty()) {
            logger.warning("Währung nicht gefunden: " + currencyId);
            return null;
        }

        CurrencyItemSet currency = currencyOpt.get();
        String itemId = currency.getItemId(tier);

        // Erstelle Münzen via ItemProvider
        Optional<ItemStack> coins;
        try {
            coins = itemProvider.getSpecialItem(itemId, amount);
        } catch (Exception e) {
            logger.warning("Fehler beim Erstellen von Münzen: " + e.getMessage());
            return null;
        }

        if (coins.isEmpty()) {
            logger.warning("Konnte Münzen nicht erstellen: " + itemId);
            return null;
        }

        return coins.get();
    }

    /**
     * Nimmt Münzen vom Spieler-Inventar und zahlt den Gegenwert auf Vault ein.
     *
     * Diese Methode:
     * 1. Sucht Münzen im Inventar
     * 2. Entfernt sie aus dem Inventar
     * 3. Zahlt den Wert auf Vault-Konto ein
     *
     * Falls nicht genug Münzen im Inventar sind, wird der maximal mögliche Betrag eingezahlt.
     *
     * @param player Spieler
     * @param currencyId Währungs-ID
     * @param tier Münz-Tier (BRONZE, SILVER, GOLD)
     * @param requestedAmount Gewünschte Anzahl
     * @return Eingezahlte Anzahl (0 wenn fehlgeschlagen)
     */
    public int depositCoins(Player player, String currencyId,
                             CurrencyItemSet.CurrencyTier tier, int requestedAmount) {
        // Prüfe ob EconomyProvider verfügbar
        if (economyProvider == null || !economyProvider.isAvailable()) {
            logger.warning("EconomyProvider nicht verfügbar - Einzahlung nicht möglich!");
            return 0;
        }

        // Prüfe ob Währung existiert
        Optional<CurrencyItemSet> currencyOpt = getCurrency(currencyId);
        if (currencyOpt.isEmpty()) {
            logger.warning("Währung nicht gefunden: " + currencyId);
            return 0;
        }

        CurrencyItemSet currency = currencyOpt.get();

        // Suche Münzen im Inventar
        CurrencyItemSet.InventoryCoinResult coinResult = currency.findCoinsInInventory(
                player.getInventory(),
                tier,
                (stack, itemId) -> {
                    try {
                        return itemProvider.isCustomItem(stack);
                    } catch (Exception e) {
                        logger.fine("Fehler beim Prüfen von Custom-Item: " + e.getMessage());
                        return false;
                    }
                }
        );

        if (!coinResult.hasCoins()) {
            logger.fine("Keine " + tier + " " + currency.namePlural() + " im Inventar von " + player.getName());
            return 0;
        }

        // Berechne tatsächlichen Betrag (kann weniger sein als gewünscht)
        int actualAmount = Math.min(requestedAmount, coinResult.totalAmount());

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

        // Berechne Wert in Basiswährung
        BigDecimal depositValue = calculateDepositValue(currency, tier, actualAmount);

        // Zahle auf Vault ein
        try {
            boolean success = economyProvider.deposit(player, depositValue.doubleValue());
            if (!success) {
                logger.warning("Vault-Einzahlung fehlgeschlagen für " + player.getName());
                // Rollback: Münzen zurückgeben
                payoutCoins(player, currencyId, tier, actualAmount);
                return 0;
            }
        } catch (Exception e) {
            logger.warning("Fehler bei Vault-Einzahlung: " + e.getMessage());
            // Rollback: Münzen zurückgeben
            payoutCoins(player, currencyId, tier, actualAmount);
            return 0;
        }

        logger.info("Eingezahlt von " + player.getName() + ": " + actualAmount + "x " +
                tier + " " + currency.namePlural() + " (Wert: " + depositValue + ")");

        return actualAmount;
    }

    /**
     * Berechnet den Wert einer Anzahl von Münzen für Vault-Deposit.
     *
     * @param currency Währung
     * @param tier Münz-Tier
     * @param amount Anzahl
     * @return Wert in Basiswährung
     */
    private BigDecimal calculateDepositValue(CurrencyItemSet currency, CurrencyItemSet.CurrencyTier tier, int amount) {
        // Tier-Wert (1, 10, 100)
        BigDecimal tierValue = BigDecimal.valueOf(currency.getTierValue(tier));

        // Anzahl
        BigDecimal amountDecimal = BigDecimal.valueOf(amount);

        // Wechselkurs zur Basiswährung
        BigDecimal exchangeRate = currency.exchangeRate();

        // Wert = Tier-Wert * Anzahl * Wechselkurs
        return tierValue.multiply(amountDecimal).multiply(exchangeRate);
    }
}
