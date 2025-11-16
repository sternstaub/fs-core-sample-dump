package de.fallenstar.npc.npctype;

import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Interface für NPC-Typen.
 *
 * Jeder NPC-Typ (Ambassador, Banker, etc.) implementiert dieses Interface.
 *
 * Verantwortlich für:
 * - NPC-Konfiguration (Name, Skin)
 * - Interaktions-Logik (onClick)
 * - Initialisierung und Cleanup
 *
 * @author FallenStar
 * @version 1.0
 */
public interface NPCType {

    /**
     * Gibt den Typ-Namen des NPCs zurück (z.B. "ambassador").
     *
     * @return Typ-Name
     */
    String getTypeName();

    /**
     * Gibt den Anzeigename des NPCs zurück.
     *
     * @return Display-Name
     */
    String getDisplayName();

    /**
     * Gibt den Skin des NPCs zurück (Spielername oder Textur).
     *
     * @return Skin
     */
    String getSkin();

    /**
     * Wird aufgerufen wenn ein Spieler mit dem NPC interagiert.
     *
     * @param player Der Spieler
     * @param npcId UUID des NPCs
     */
    void onClick(Player player, UUID npcId);

    /**
     * Initialisiert den NPC-Typ.
     * Wird beim Modul-Start aufgerufen.
     */
    void initialize();

    /**
     * Cleanup für den NPC-Typ.
     * Wird beim Modul-Shutdown aufgerufen.
     */
    void shutdown();

    /**
     * Prüft ob dieser NPC-Typ verfügbar ist.
     * (abhängig von Providern)
     *
     * @return true wenn verfügbar
     */
    boolean isAvailable();
}
