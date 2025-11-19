package de.fallenstar.core.currency;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Registry für alle verfügbaren Währungs-Items (CurrencyItems).
 *
 * **Registry-Pattern (Sprint 19 Phase 3):**
 *
 * **Problem:**
 * - CoinProvider hart-kodiert für Vanilla Coins
 * - Neue Währungen erfordern Code-Änderungen
 * - Verstößt gegen Open/Closed Principle
 *
 * **Lösung:**
 * - CurrencyRegistry als zentrale Verwaltung
 * - Module registrieren ihre Währungen beim Start
 * - CoinProvider nutzt Registry statt Hardcoding
 *
 * **SOLID-Prinzipien:**
 *
 * **Single Responsibility:**
 * - NUR Registry-Verwaltung (register, get, getAll)
 * - KEINE Währungs-Logik (Value-Calculation, etc.)
 *
 * **Open/Closed:**
 * - Offen für Erweiterung (neue CurrencyItems)
 * - Geschlossen für Änderung (Registry-Code bleibt gleich)
 *
 * **Liskov Substitution:**
 * - Alle CurrencyItems sind austauschbar
 * - Registry funktioniert mit JEDER Implementierung
 *
 * **Dependency Inversion:**
 * - CoinProvider hängt von CurrencyRegistry ab
 * - NICHT von konkreten Currency-Implementierungen
 *
 * **Thread-Safety:**
 * - ConcurrentHashMap für Thread-sichere Operationen
 * - Mehrere Module können parallel registrieren
 *
 * **Verwendung:**
 * <pre>
 * // ItemsModule Initialisierung:
 * CurrencyItem vanillaStar = new VanillaCoinItem("vanilla_star", Material.NETHER_STAR, "§6Stern");
 * currencyRegistry.register(vanillaStar);
 *
 * CurrencyItem vanillaGold = new VanillaCoinItem("vanilla_gold", Material.GOLD_INGOT, "§eGoldbarren");
 * currencyRegistry.register(vanillaGold);
 *
 * // CoinProvider Nutzung:
 * Optional&lt;CurrencyItem&gt; currency = currencyRegistry.get("vanilla_star");
 * if (currency.isPresent()) {
 *     ItemStack coin = currency.get().getDisplayItem(10);
 *     player.getInventory().addItem(coin);
 * }
 *
 * // Alle Währungen auflisten:
 * Collection&lt;CurrencyItem&gt; allCurrencies = currencyRegistry.getAll();
 * allCurrencies.forEach(c -> logger.info("Currency: " + c.getIdentifier()));
 * </pre>
 *
 * **Architektur:**
 * <pre>
 * Core → CurrencyRegistry (Interface + Implementierung)
 *  ↑
 *  ├── Items-Modul → VanillaCoinItem, MMOItemsCoinItem
 *  ├── Economy-Modul → Nutzt CurrencyRegistry für Pricing
 *  └── Plots-Modul → Nutzt CurrencyRegistry für Storage
 * </pre>
 *
 * @author FallenStar
 * @version 1.0
 * @see CurrencyItem
 * @see de.fallenstar.items.model.VanillaCoinItem
 */
public class CurrencyRegistry {

    /**
     * Thread-safe Map für Currency-Items.
     * Key: CurrencyItem.getIdentifier()
     * Value: CurrencyItem Implementierung
     */
    private final Map<String, CurrencyItem> currencies;

    /**
     * Logger für Registry-Operationen.
     */
    private final Logger logger;

    /**
     * Erstellt eine neue CurrencyRegistry.
     *
     * @param logger Logger für Logging
     */
    public CurrencyRegistry(Logger logger) {
        this.currencies = new ConcurrentHashMap<>();
        this.logger = Objects.requireNonNull(logger, "Logger darf nicht null sein");
    }

    /**
     * Registriert ein CurrencyItem.
     *
     * **Validierung:**
     * - currency != null
     * - identifier != null && !isEmpty()
     * - Keine Duplikate (wirft IllegalArgumentException)
     *
     * **Beispiel:**
     * <pre>
     * CurrencyItem vanillaStar = new VanillaCoinItem("vanilla_star", Material.NETHER_STAR, "§6Stern");
     * currencyRegistry.register(vanillaStar);
     * </pre>
     *
     * @param currency Das zu registrierende CurrencyItem
     * @throws NullPointerException wenn currency null ist
     * @throws IllegalArgumentException wenn identifier null/leer ist
     * @throws IllegalStateException wenn identifier bereits registriert ist
     */
    public void register(CurrencyItem currency) {
        Objects.requireNonNull(currency, "CurrencyItem darf nicht null sein");

        String identifier = currency.getIdentifier();
        if (identifier == null || identifier.isEmpty()) {
            throw new IllegalArgumentException("CurrencyItem Identifier darf nicht null/leer sein");
        }

        if (currencies.containsKey(identifier)) {
            throw new IllegalStateException(
                "CurrencyItem mit Identifier '" + identifier + "' ist bereits registriert"
            );
        }

        currencies.put(identifier, currency);
        logger.info("CurrencyItem registriert: " + identifier + " (" + currency.getDisplayName() + ")");
    }

    /**
     * Holt ein CurrencyItem via Identifier.
     *
     * **Beispiel:**
     * <pre>
     * Optional&lt;CurrencyItem&gt; currency = currencyRegistry.get("vanilla_star");
     * currency.ifPresent(c -> {
     *     ItemStack coin = c.getDisplayItem(10);
     *     player.getInventory().addItem(coin);
     * });
     * </pre>
     *
     * @param identifier Der Currency-Identifier (z.B. "vanilla_star")
     * @return Optional mit CurrencyItem, oder leer wenn nicht registriert
     */
    public Optional<CurrencyItem> get(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(currencies.get(identifier));
    }

    /**
     * Prüft ob ein CurrencyItem registriert ist.
     *
     * **Beispiel:**
     * <pre>
     * if (currencyRegistry.isRegistered("vanilla_star")) {
     *     // Currency ist verfügbar
     * }
     * </pre>
     *
     * @param identifier Der Currency-Identifier
     * @return true wenn registriert, false sonst
     */
    public boolean isRegistered(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            return false;
        }
        return currencies.containsKey(identifier);
    }

    /**
     * Liefert alle registrierten CurrencyItems.
     *
     * **Verwendung:**
     * <pre>
     * Collection&lt;CurrencyItem&gt; allCurrencies = currencyRegistry.getAll();
     * allCurrencies.forEach(c -> {
     *     logger.info("Currency: " + c.getIdentifier() + " = " + c.getDisplayName());
     * });
     * </pre>
     *
     * **Immutability:**
     * - Gibt unmodifiable Collection zurück
     * - Verhindert externe Änderungen an Registry
     *
     * @return Unmodifiable Collection aller CurrencyItems
     */
    public Collection<CurrencyItem> getAll() {
        return Collections.unmodifiableCollection(currencies.values());
    }

    /**
     * Liefert alle registrierten Identifiers.
     *
     * **Verwendung:**
     * <pre>
     * Set&lt;String&gt; identifiers = currencyRegistry.getAllIdentifiers();
     * identifiers.forEach(id -> logger.info("Currency ID: " + id));
     * </pre>
     *
     * @return Unmodifiable Set aller Identifiers
     */
    public Set<String> getAllIdentifiers() {
        return Collections.unmodifiableSet(currencies.keySet());
    }

    /**
     * Gibt die Anzahl registrierter Currencies zurück.
     *
     * **Verwendung:**
     * <pre>
     * int count = currencyRegistry.size();
     * logger.info("Registrierte Currencies: " + count);
     * </pre>
     *
     * @return Anzahl registrierter CurrencyItems
     */
    public int size() {
        return currencies.size();
    }

    /**
     * Prüft ob die Registry leer ist.
     *
     * @return true wenn keine Currencies registriert sind
     */
    public boolean isEmpty() {
        return currencies.isEmpty();
    }

    /**
     * Entfernt ein CurrencyItem aus der Registry.
     *
     * **ACHTUNG: Nur für Tests und Cleanup!**
     * Normale Module sollten NIEMALS Currencies deregistrieren!
     *
     * @param identifier Der Currency-Identifier
     * @return true wenn entfernt, false wenn nicht vorhanden
     */
    public boolean unregister(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            return false;
        }

        CurrencyItem removed = currencies.remove(identifier);
        if (removed != null) {
            logger.warning("CurrencyItem deregistriert: " + identifier);
            return true;
        }
        return false;
    }

    /**
     * Löscht ALLE registrierten Currencies.
     *
     * **ACHTUNG: Nur für Tests und Server-Shutdown!**
     * Normale Module sollten NIEMALS clear() aufrufen!
     */
    public void clear() {
        int count = currencies.size();
        currencies.clear();
        logger.warning("CurrencyRegistry geleert (" + count + " Currencies entfernt)");
    }
}
