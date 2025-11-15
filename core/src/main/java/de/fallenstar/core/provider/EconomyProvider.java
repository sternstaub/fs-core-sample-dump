package de.fallenstar.core.provider;

import de.fallenstar.core.exception.ProviderFunctionalityNotFoundException;
import org.bukkit.entity.Player;
import java.util.UUID;

/**
 * Provider-Interface für Economy-Systeme.
 * 
 * Implementierungen:
 * - VaultEconomyProvider (Vault-API Integration)
 * - NoOpEconomyProvider (Fallback)
 * 
 * Unterstützt sowohl Player- als auch Fraktions-/Stadt-Konten.
 * 
 * @author FallenStar
 * @version 1.0
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
}
