package de.fallenstar.economy.manager;

import de.fallenstar.economy.model.CurrencyItemSet;
import de.fallenstar.items.manager.SpecialItemManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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
 * - Integration mit SpecialItemManager (Items-Modul)
 *
 * @author FallenStar
 * @version 1.0
 */
public class CurrencyManager {

    private final Logger logger;
    private final SpecialItemManager itemManager;
    private final Map<String, CurrencyItemSet> currencies;
    private CurrencyItemSet baseCurrency;

    /**
     * Konstruktor für CurrencyManager.
     *
     * @param logger Logger
     * @param itemManager SpecialItemManager (aus Items-Modul)
     */
    public CurrencyManager(Logger logger, SpecialItemManager itemManager) {
        this.logger = logger;
        this.itemManager = itemManager;
        this.currencies = new HashMap<>();

        logger.info("CurrencyManager initialisiert");
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
     * Zahlt Münzen an einen Spieler aus.
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

        // Erstelle Münzen via SpecialItemManager
        Optional<ItemStack> coins = itemManager.createItem(itemId, amount);

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
}
