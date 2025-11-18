package de.fallenstar.core.provider;

import de.fallenstar.core.exception.ProviderFunctionalityNotFoundException;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * Provider-Interface für Economy-Systeme.
 *
 * Implementierungen:
 * - VaultEconomyProvider (Vault-API Integration)
 * - NoOpEconomyProvider (Fallback)
 *
 * Unterstützt sowohl Player- als auch Fraktions-/Stadt-Konten.
 * Mapped Vault-Funktionen für andere Module, damit diese Vault nicht explizit laden müssen.
 *
 * @author FallenStar
 * @version 2.0
 */
public interface EconomyProvider {

    /**
     * Prüft ob dieser Provider verfügbar ist.
     *
     * @return true wenn Economy-System verfügbar
     */
    boolean isAvailable();
    
    /**
     * Gibt das Guthaben eines Spielers zurück.
     * 
     * @param player Der Spieler
     * @return Guthaben
     * @throws ProviderFunctionalityNotFoundException wenn nicht verfügbar
     */
    double getBalance(Player player) throws ProviderFunctionalityNotFoundException;
    
    /**
     * Zieht Geld vom Konto eines Spielers ab.
     * 
     * @param player Der Spieler
     * @param amount Betrag
     * @return true wenn erfolgreich
     * @throws ProviderFunctionalityNotFoundException wenn nicht verfügbar
     */
    boolean withdraw(Player player, double amount) 
            throws ProviderFunctionalityNotFoundException;
    
    /**
     * Fügt Geld zum Konto eines Spielers hinzu.
     * 
     * @param player Der Spieler
     * @param amount Betrag
     * @return true wenn erfolgreich
     * @throws ProviderFunctionalityNotFoundException wenn nicht verfügbar
     */
    boolean deposit(Player player, double amount) 
            throws ProviderFunctionalityNotFoundException;
    
    /**
     * Gibt das Guthaben eines Fraktions-/Stadt-Kontos zurück.
     * 
     * @param accountId Konto-ID (z.B. Stadt-UUID)
     * @return Guthaben
     * @throws ProviderFunctionalityNotFoundException wenn nicht verfügbar
     */
    double getFactionBalance(UUID accountId) 
            throws ProviderFunctionalityNotFoundException;
    
    /**
     * Zieht Geld von einem Fraktions-/Stadt-Konto ab.
     * 
     * @param accountId Konto-ID
     * @param amount Betrag
     * @return true wenn erfolgreich
     * @throws ProviderFunctionalityNotFoundException wenn nicht verfügbar
     */
    boolean withdrawFaction(UUID accountId, double amount) 
            throws ProviderFunctionalityNotFoundException;
    
    /**
     * Fügt Geld zu einem Fraktions-/Stadt-Konto hinzu.
     *
     * @param accountId Konto-ID
     * @param amount Betrag
     * @return true wenn erfolgreich
     * @throws ProviderFunctionalityNotFoundException wenn nicht verfügbar
     */
    boolean depositFaction(UUID accountId, double amount)
            throws ProviderFunctionalityNotFoundException;

    /**
     * Setzt das Guthaben eines Spielers auf einen bestimmten Betrag.
     *
     * @param player Der Spieler
     * @param amount Neuer Kontostand
     * @return true wenn erfolgreich
     * @throws ProviderFunctionalityNotFoundException wenn nicht verfügbar
     */
    boolean setBalance(Player player, double amount)
            throws ProviderFunctionalityNotFoundException;

    /**
     * Prüft ob ein Spieler ein Economy-Konto hat.
     *
     * @param player Der Spieler
     * @return true wenn Konto existiert
     * @throws ProviderFunctionalityNotFoundException wenn nicht verfügbar
     */
    boolean hasAccount(OfflinePlayer player)
            throws ProviderFunctionalityNotFoundException;

    /**
     * Erstellt ein Economy-Konto für einen Spieler.
     *
     * @param player Der Spieler
     * @return true wenn erfolgreich
     * @throws ProviderFunctionalityNotFoundException wenn nicht verfügbar
     */
    boolean createAccount(OfflinePlayer player)
            throws ProviderFunctionalityNotFoundException;

    /**
     * Formatiert einen Betrag als Währungs-String.
     *
     * Beispiele: "$100.00", "100 Gold", "100€"
     *
     * @param amount Betrag
     * @return Formatierter String
     * @throws ProviderFunctionalityNotFoundException wenn nicht verfügbar
     */
    String format(double amount)
            throws ProviderFunctionalityNotFoundException;

    /**
     * Gibt den Namen der Economy-Implementation zurück.
     *
     * Beispiele: "Essentials Economy", "CMI Economy", "Vault"
     *
     * @return Economy-Name
     * @throws ProviderFunctionalityNotFoundException wenn nicht verfügbar
     */
    String getEconomyName()
            throws ProviderFunctionalityNotFoundException;

    /**
     * Gibt den Namen der Währung zurück.
     *
     * Beispiele: "Dollar", "Gold", "Euro"
     *
     * @return Währungsname (Singular)
     * @throws ProviderFunctionalityNotFoundException wenn nicht verfügbar
     */
    String getCurrencyNameSingular()
            throws ProviderFunctionalityNotFoundException;

    /**
     * Gibt den Plural-Namen der Währung zurück.
     *
     * Beispiele: "Dollars", "Gold", "Euro"
     *
     * @return Währungsname (Plural)
     * @throws ProviderFunctionalityNotFoundException wenn nicht verfügbar
     */
    String getCurrencyNamePlural()
            throws ProviderFunctionalityNotFoundException;

    // ================== Item-Preis-Verwaltung ==================

    /**
     * Gibt den Ankaufspreis eines Materials zurück (Spieler verkauft an NPC/System).
     *
     * Eliminiert Reflection-Zugriff auf ItemBasePriceProvider.
     *
     * @param material Das Material
     * @return Optional mit Ankaufspreis, leer wenn kein Preis definiert
     * @throws ProviderFunctionalityNotFoundException wenn nicht verfügbar
     */
    Optional<BigDecimal> getBuyPrice(Material material)
            throws ProviderFunctionalityNotFoundException;

    /**
     * Gibt den Verkaufspreis eines Materials zurück (Spieler kauft von NPC/System).
     *
     * Eliminiert Reflection-Zugriff auf ItemBasePriceProvider.
     *
     * @param material Das Material
     * @return Optional mit Verkaufspreis, leer wenn kein Preis definiert
     * @throws ProviderFunctionalityNotFoundException wenn nicht verfügbar
     */
    Optional<BigDecimal> getSellPrice(Material material)
            throws ProviderFunctionalityNotFoundException;

    /**
     * Setzt Ankaufs- und Verkaufspreis für ein Material.
     *
     * Eliminiert Reflection-Zugriff auf ItemBasePriceProvider.
     *
     * @param material Das Material
     * @param buyPrice Ankaufspreis (Spieler verkauft an NPC)
     * @param sellPrice Verkaufspreis (Spieler kauft von NPC)
     * @return true wenn erfolgreich
     * @throws ProviderFunctionalityNotFoundException wenn nicht verfügbar
     */
    boolean setItemPrice(Material material, BigDecimal buyPrice, BigDecimal sellPrice)
            throws ProviderFunctionalityNotFoundException;

    /**
     * Gibt alle Materialien zurück, für die Preise definiert sind.
     *
     * Eliminiert Reflection-Zugriff auf ItemBasePriceProvider.
     *
     * @return Collection von Materialien mit definierten Preisen
     * @throws ProviderFunctionalityNotFoundException wenn nicht verfügbar
     */
    Collection<Material> getAllPricedMaterials()
            throws ProviderFunctionalityNotFoundException;
}
