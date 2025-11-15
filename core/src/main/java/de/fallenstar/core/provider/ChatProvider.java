package de.fallenstar.core.provider;

import de.fallenstar.core.exception.ProviderFunctionalityNotFoundException;
import java.util.UUID;

/**
 * Provider-Interface für externe Chat-Systeme.
 * 
 * Ermöglicht Brücken zu:
 * - Matrix (dezentrales Chat-Protokoll)
 * - Discord
 * - IRC
 * - Andere Chat-Systeme
 * 
 * Implementierungen:
 * - MatrixChatProvider (Matrix-Protocol Integration)
 * - DiscordChatProvider (Discord Bot Integration)
 * - NoOpChatProvider (Fallback)
 * 
 * Use-Case:
 * Spieler können in-game mit externen Chat-Systemen kommunizieren.
 * Handels-Benachrichtigungen können an externe Chats gesendet werden.
 * 
 * @author FallenStar
 * @version 1.0
 */
public interface ChatProvider {
    
    /**
     * Prüft ob dieser Provider verfügbar ist.
     * 
     * @return true wenn Chat-System verbunden
     */
    boolean isAvailable();
    
    /**
     * Sendet eine Nachricht an einen externen Chat-Raum.
     * 
     * @param roomId ID des Raums (z.B. Matrix Room ID, Discord Channel ID)
     * @param message Die Nachricht
     * @throws ProviderFunctionalityNotFoundException wenn nicht verfügbar
     */
    void sendMessage(String roomId, String message) 
            throws ProviderFunctionalityNotFoundException;
    
    /**
     * Sendet eine Nachricht an einen bestimmten Benutzer.
     * 
     * @param userId Externe User-ID
     * @param message Die Nachricht
     * @throws ProviderFunctionalityNotFoundException wenn nicht verfügbar
     */
    void sendDirectMessage(String userId, String message) 
            throws ProviderFunctionalityNotFoundException;
    
    /**
     * Verknüpft einen Minecraft-Spieler mit einer externen User-ID.
     * 
     * @param minecraftUuid Spieler-UUID
     * @param externalUserId Externe User-ID (Matrix ID, Discord ID, etc.)
     * @throws ProviderFunctionalityNotFoundException wenn nicht verfügbar
     */
    void linkUser(UUID minecraftUuid, String externalUserId) 
            throws ProviderFunctionalityNotFoundException;
    
    /**
     * Gibt die externe User-ID für einen Minecraft-Spieler zurück.
     * 
     * @param minecraftUuid Spieler-UUID
     * @return Externe User-ID oder null wenn nicht verknüpft
     * @throws ProviderFunctionalityNotFoundException wenn nicht verfügbar
     */
    String getLinkedUser(UUID minecraftUuid) 
            throws ProviderFunctionalityNotFoundException;
    
    /**
     * Sendet eine formatierte Embed-Message (für Discord/Matrix Rich Content).
     * 
     * @param roomId Raum-ID
     * @param title Titel
     * @param description Beschreibung
     * @param color Farbe (Hex)
     * @throws ProviderFunctionalityNotFoundException wenn nicht verfügbar
     */
    void sendEmbedMessage(String roomId, String title, String description, String color) 
            throws ProviderFunctionalityNotFoundException;
    
    /**
     * Registriert einen Command-Handler für externe Chat-Befehle.
     * 
     * Beispiel: Spieler kann von Matrix aus "/trade status" eingeben
     * 
     * @param command Der Befehl (ohne /)
     * @param handler Handler-Interface
     * @throws ProviderFunctionalityNotFoundException wenn nicht verfügbar
     */
    void registerExternalCommand(String command, ExternalCommandHandler handler) 
            throws ProviderFunctionalityNotFoundException;
    
    /**
     * Handler-Interface für externe Commands.
     */
    interface ExternalCommandHandler {
        void handle(String userId, String[] args);
    }
}
