package de.fallenstar.core.distributor;

import java.util.UUID;

/**
 * Interface für Objekte die distribuiert werden können.
 *
 * **Konzept:**
 * - Distributable = Objekt das verteilt werden kann
 * - Distributor = Objekt das Distributables verteilt
 *
 * **Verwendung:**
 * - NPCs auf Slots verteilen (DistributableNpc)
 * - Quests an NPCs verteilen (DistributableQuest)
 * - Items in Lager verteilen (DistributableItem)
 *
 * **Beispiel:**
 * <pre>
 * class MyNpc implements DistributableNpc {
 *     {@literal @}Override
 *     public void onDistributed(Distributor<?> distributor) {
 *         logger.info("NPC wurde auf " + distributor + " verteilt");
 *     }
 * }
 * </pre>
 *
 * @author FallenStar
 * @version 1.0
 */
public interface Distributable {

    /**
     * Gibt die ID des Distributable zurück.
     *
     * @return UUID
     */
    UUID getId();

    /**
     * Gibt den Typ zurück.
     *
     * @return Typ-String (z.B. "guild_trader", "quest", "item")
     */
    String getType();

    /**
     * Callback wenn Objekt distribuiert wurde.
     *
     * Optional: Override für custom Logic
     * (z.B. Logging, Event-Firing, State-Updates)
     *
     * @param distributor Der Distributor der das Objekt aufgenommen hat
     */
    default void onDistributed(Distributor<?> distributor) {
        // Default: Keine Aktion
    }

    /**
     * Callback wenn Objekt de-distribuiert wurde.
     *
     * Optional: Override für custom Logic
     * (z.B. Cleanup, Event-Firing, State-Reset)
     */
    default void onUndistributed() {
        // Default: Keine Aktion
    }
}
