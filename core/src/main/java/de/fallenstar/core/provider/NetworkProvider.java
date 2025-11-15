package de.fallenstar.core.provider;

import de.fallenstar.core.exception.ProviderFunctionalityNotFoundException;
import org.bukkit.entity.Player;
import java.util.List;
import java.util.UUID;

/**
 * Provider-Interface für Netzwerk-/Proxy-Systeme.
 * 
 * Ermöglicht Cross-Server-Kommunikation in einem Velocity/BungeeCord-Netzwerk.
 * 
 * Implementierungen:
 * - VelocityNetworkProvider (Velocity Proxy)
 * - BungeeNetworkProvider (BungeeCord Proxy)
 * - NoOpNetworkProvider (Standalone Server)
 * 
 * Use-Cases:
 * - Händler können zwischen Servern reisen
 * - Globale Admin-Shops auf allen Servern
 * - Cross-Server-Economy
 * - Spieler-Teleportation zwischen Servern
 * 
 * @author FallenStar
 * @version 1.0
 */
public interface NetworkProvider {
    
    /**
     * Prüft ob dieser Provider verfügbar ist.
     * 
     * @return true wenn in einem Netzwerk (Velocity/Bungee)
     */
    boolean isAvailable();
    
    /**
     * Gibt alle Server im Netzwerk zurück.
     * 
     * @return Liste von Server-Namen
     * @throws ProviderFunctionalityNotFoundException wenn nicht verfügbar
     */
    List<String> getServers() throws ProviderFunctionalityNotFoundException;
    
    /**
     * Teleportiert einen Spieler zu einem anderen Server.
     * 
     * @param player Der Spieler
     * @param serverName Ziel-Server
     * @throws ProviderFunctionalityNotFoundException wenn nicht verfügbar
     */
    void transferPlayer(Player player, String serverName) 
            throws ProviderFunctionalityNotFoundException;
    
    /**
     * Sendet eine Plugin-Message an einen anderen Server.
     * 
     * Für custom data exchange zwischen Servern.
     * 
     * @param targetServer Ziel-Server
     * @param channel Message-Channel
     * @param data Payload
     * @throws ProviderFunctionalityNotFoundException wenn nicht verfügbar
     */
    void sendPluginMessage(String targetServer, String channel, byte[] data) 
            throws ProviderFunctionalityNotFoundException;
    
    /**
     * Registriert einen Listener für Plugin-Messages von anderen Servern.
     * 
     * @param channel Message-Channel
     * @param handler Handler für eingehende Messages
     * @throws ProviderFunctionalityNotFoundException wenn nicht verfügbar
     */
    void registerMessageListener(String channel, MessageHandler handler) 
            throws ProviderFunctionalityNotFoundException;
    
    /**
     * Gibt den aktuellen Server-Namen zurück.
     * 
     * @return Server-Name im Netzwerk
     * @throws ProviderFunctionalityNotFoundException wenn nicht verfügbar
     */
    String getCurrentServerName() throws ProviderFunctionalityNotFoundException;
    
    /**
     * Gibt die Anzahl Spieler auf einem Server zurück.
     * 
     * @param serverName Server-Name
     * @return Spieler-Anzahl
     * @throws ProviderFunctionalityNotFoundException wenn nicht verfügbar
     */
    int getPlayerCount(String serverName) 
            throws ProviderFunctionalityNotFoundException;
    
    /**
     * Prüft ob ein Server online ist.
     * 
     * @param serverName Server-Name
     * @return true wenn online
     * @throws ProviderFunctionalityNotFoundException wenn nicht verfügbar
     */
    boolean isServerOnline(String serverName) 
            throws ProviderFunctionalityNotFoundException;
    
    /**
     * Findet einen Spieler im gesamten Netzwerk.
     * 
     * @param playerUuid Spieler-UUID
     * @return Server-Name wo der Spieler ist, oder null
     * @throws ProviderFunctionalityNotFoundException wenn nicht verfügbar
     */
    String findPlayer(UUID playerUuid) 
            throws ProviderFunctionalityNotFoundException;
    
    /**
     * Handler-Interface für Plugin-Messages.
     */
    interface MessageHandler {
        void handle(String sourceServer, byte[] data);
    }
}
