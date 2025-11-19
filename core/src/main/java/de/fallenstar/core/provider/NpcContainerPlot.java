package de.fallenstar.core.provider;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Trait-Interface für Plots mit NPC-Verwaltung.
 *
 * **Features:**
 * - Plot-gebundene NPCs
 * - NPC-Registrierung und Verwaltung
 * - Persistente Speicherung
 *
 * **Konzept:**
 * - NPCs gehören zum Plot (nicht zum Spieler!)
 * - Nur Plot-Owner kann NPCs verwalten
 * - NPCs können auf andere Plots geschickt werden (Trader Slots)
 *
 * **Verwendung:**
 * <pre>
 * class TradeguildPlot extends BasePlot implements NpcContainerPlot {
 *     private List&lt;UUID&gt; npcIds = new ArrayList&lt;&gt;();
 *
 *     {@literal @}Override
 *     public void registerNpc(UUID npcId) {
 *         if (!npcIds.contains(npcId)) {
 *             npcIds.add(npcId);
 *             // Speichern...
 *         }
 *     }
 *
 *     {@literal @}Override
 *     public List&lt;UUID&gt; getNpcIds() {
 *         return new ArrayList&lt;&gt;(npcIds);
 *     }
 * }
 * </pre>
 *
 * **Integration:**
 * - NpcManagementUi: NPCs spawnen/entfernen
 * - PlotBoundNPCRegistry: Zuordnung Plot → NPCs
 * - GuildTraderNPC: Als NPC-Typ registriert
 *
 * @author FallenStar
 * @version 1.0
 */
public interface NpcContainerPlot extends Plot {

    /**
     * Registriert einen NPC für diesen Plot.
     *
     * @param npcId NPC UUID
     */
    void registerNpc(UUID npcId);

    /**
     * Entfernt einen NPC von diesem Plot.
     *
     * @param npcId NPC UUID
     * @return true wenn NPC entfernt wurde
     */
    boolean unregisterNpc(UUID npcId);

    /**
     * Gibt alle NPC-IDs für diesen Plot zurück.
     *
     * @return Liste von NPC UUIDs
     */
    List<UUID> getNpcIds();

    /**
     * Prüft ob ein NPC zu diesem Plot gehört.
     *
     * @param npcId NPC UUID
     * @return true wenn NPC registriert ist
     */
    default boolean hasNpc(UUID npcId) {
        return getNpcIds().contains(npcId);
    }

    /**
     * Gibt die Anzahl registrierter NPCs zurück.
     *
     * @return Anzahl NPCs
     */
    default int getNpcCount() {
        return getNpcIds().size();
    }

    /**
     * Entfernt alle NPCs von diesem Plot.
     */
    void clearNpcs();

    /**
     * Gibt den NPC-Typ für einen NPC zurück.
     *
     * Optional: Plots können verschiedene NPC-Typen verwalten.
     *
     * @param npcId NPC UUID
     * @return Optional mit NPC-Typ String, oder empty
     */
    default Optional<String> getNpcType(UUID npcId) {
        return Optional.empty();
    }

    /**
     * Setzt den NPC-Typ für einen NPC.
     *
     * Optional: Zur Kategorisierung von NPCs.
     *
     * @param npcId NPC UUID
     * @param type NPC-Typ (z.B. "guild_trader", "guard", etc.)
     */
    default void setNpcType(UUID npcId, String type) {
        // Default: Keine Implementierung
    }
}
