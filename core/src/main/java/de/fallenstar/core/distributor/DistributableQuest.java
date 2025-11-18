package de.fallenstar.core.distributor;

import java.util.Optional;

/**
 * Quest die an NPCs distribuiert werden kann.
 *
 * **Features:**
 * - Quest-Metadaten (Titel, Level, etc.)
 * - Container-Tracking
 * - Automatic NPC-Zuweisung
 *
 * **Verwendung:**
 * <pre>
 * class SimpleQuest implements DistributableQuest {
 *     private QuestContainer currentContainer;
 *
 *     {@literal @}Override
 *     public void setCurrentContainer(QuestContainer container) {
 *         this.currentContainer = container;
 *     }
 *
 *     {@literal @}Override
 *     public void onDistributed(Distributor<?> distributor) {
 *         logger.info("Quest '" + getTitle() + "' wurde verteilt");
 *     }
 * }
 * </pre>
 *
 * @author FallenStar
 * @version 1.0
 */
public interface DistributableQuest extends Distributable {

    /**
     * Gibt den Quest-Titel zurück.
     *
     * @return Titel (mit Farbcodes erlaubt)
     */
    String getTitle();

    /**
     * Gibt die Quest-Beschreibung zurück.
     *
     * @return Beschreibung
     */
    String getDescription();

    /**
     * Gibt die Quest-Stufe zurück.
     *
     * @return Level (1+)
     */
    int getLevel();

    /**
     * Gibt den aktuellen Quest-Container zurück.
     *
     * NULLABLE: Kann empty sein wenn Quest nicht distribuiert ist!
     *
     * @return QuestContainer oder empty
     */
    Optional<QuestContainer> getCurrentContainer();

    /**
     * Setzt den Quest-Container.
     *
     * Wird vom QuestDistributor aufgerufen.
     *
     * @param container Der Container (kann null sein)
     */
    void setCurrentContainer(QuestContainer container);

    /**
     * Prüft ob Quest distribuiert ist.
     *
     * @return true wenn Container gesetzt
     */
    default boolean isDistributed() {
        return getCurrentContainer().isPresent();
    }

    /**
     * Gibt die Quest-Kategorie zurück.
     *
     * @return Kategorie (z.B. "gathering", "combat", "delivery")
     */
    default String getCategory() {
        return "general";
    }

    /**
     * Prüft ob Quest wiederholbar ist.
     *
     * @return true wenn Quest mehrfach machbar
     */
    default boolean isRepeatable() {
        return false;
    }
}
