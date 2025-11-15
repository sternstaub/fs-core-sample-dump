package de.fallenstar.core.provider;

import de.fallenstar.core.exception.ProviderFunctionalityNotFoundException;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Provider-Interface für NPC-Systeme.
 * 
 * Implementierungen:
 * - CitizensNPCProvider (Citizens-Plugin Integration)
 * - ZNPCProvider (ZNPC-Plugin Integration)
 * - NoOpNPCProvider (Fallback)
 * 
 * @author FallenStar
 * @version 1.0
 */
public interface NPCProvider {
    
    /**
     * Prüft ob dieser Provider verfügbar ist.
     * 
     * @return true wenn NPC-System verfügbar
     */
    boolean isAvailable();
    
    /**
     * Erstellt einen neuen NPC an einer Location.
     * 
     * @param location Spawn-Location
     * @param name Anzeigename des NPCs
     * @param skin Skin-Name (Spielername oder Textur)
     * @return UUID des erstellten NPCs
     * @throws ProviderFunctionalityNotFoundException wenn nicht verfügbar
     */
    UUID createNPC(Location location, String name, String skin) 
            throws ProviderFunctionalityNotFoundException;
    
    /**
     * Entfernt einen NPC.
     * 
     * @param npcId UUID des NPCs
     * @return true wenn erfolgreich entfernt
     * @throws ProviderFunctionalityNotFoundException wenn nicht verfügbar
     */
    boolean removeNPC(UUID npcId) throws ProviderFunctionalityNotFoundException;
    
    /**
     * Teleportiert einen NPC zu einer neuen Location.
     * 
     * @param npcId UUID des NPCs
     * @param location Ziel-Location
     * @return true wenn erfolgreich
     * @throws ProviderFunctionalityNotFoundException wenn nicht verfügbar
     */
    boolean teleportNPC(UUID npcId, Location location) 
            throws ProviderFunctionalityNotFoundException;
    
    /**
     * Registriert einen Click-Handler für einen NPC.
     * 
     * @param npcId UUID des NPCs
     * @param handler Consumer der beim Click ausgeführt wird
     * @throws ProviderFunctionalityNotFoundException wenn nicht verfügbar
     */
    void setClickHandler(UUID npcId, Consumer<Player> handler) 
            throws ProviderFunctionalityNotFoundException;
    
    /**
     * Ändert den Skin eines NPCs.
     * 
     * @param npcId UUID des NPCs
     * @param skin Neuer Skin
     * @return true wenn erfolgreich
     * @throws ProviderFunctionalityNotFoundException wenn nicht verfügbar
     */
    boolean setSkin(UUID npcId, String skin) 
            throws ProviderFunctionalityNotFoundException;
    
    /**
     * Prüft ob ein NPC existiert.
     * 
     * @param npcId UUID des NPCs
     * @return true wenn NPC existiert
     * @throws ProviderFunctionalityNotFoundException wenn nicht verfügbar
     */
    boolean npcExists(UUID npcId) throws ProviderFunctionalityNotFoundException;
}
