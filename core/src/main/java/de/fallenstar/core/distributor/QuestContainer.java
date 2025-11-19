package de.fallenstar.core.distributor;

import java.util.List;
import java.util.UUID;

/**
 * Container der Quests halten kann (z.B. NPC).
 *
 * **Features:**
 * - Quest-Verwaltung
 * - Kapazitäts-Limits
 * - Quest-Zugriff per ID
 *
 * **Verwendung:**
 * <pre>
 * class GuildTraderNpcEntity implements QuestContainer {
 *     private List&lt;DistributableQuest&gt; quests = new ArrayList&lt;&gt;();
 *     private int maxQuests = 5;
 *
 *     {@literal @}Override
 *     public boolean addQuest(DistributableQuest quest) {
 *         if (!hasQuestCapacity()) return false;
 *         quests.add(quest);
 *         quest.setCurrentContainer(this);
 *         return true;
 *     }
 * }
 * </pre>
 *
 * @author FallenStar
 * @version 1.0
 */
public interface QuestContainer {

    /**
     * Gibt die Container-ID zurück.
     *
     * @return UUID (z.B. NPC Entity ID)
     */
    UUID getContainerId();

    /**
     * Gibt den Container-Namen zurück.
     *
     * @return Name (z.B. NPC-Name)
     */
    String getContainerName();

    /**
     * Gibt die maximale Anzahl Quests zurück.
     *
     * @return Max Quests
     */
    int getMaxQuests();

    /**
     * Gibt aktuelle Quests zurück.
     *
     * @return Liste von Quests (kann leer sein!)
     */
    List<DistributableQuest> getQuests();

    /**
     * Fügt eine Quest hinzu.
     *
     * @param quest Die Quest
     * @return true wenn erfolgreich, false wenn voll
     */
    boolean addQuest(DistributableQuest quest);

    /**
     * Entfernt eine Quest.
     *
     * @param quest Die Quest
     * @return true wenn erfolgreich
     */
    boolean removeQuest(DistributableQuest quest);

    /**
     * Prüft ob eine Quest vorhanden ist.
     *
     * @param questId Quest-ID
     * @return true wenn vorhanden
     */
    default boolean hasQuest(UUID questId) {
        return getQuests().stream()
            .anyMatch(q -> q.getId().equals(questId));
    }

    /**
     * Prüft ob noch Kapazität für Quests vorhanden ist.
     *
     * @return true wenn Platz frei
     */
    default boolean hasQuestCapacity() {
        return getQuests().size() < getMaxQuests();
    }

    /**
     * Gibt die Anzahl aktueller Quests zurück.
     *
     * @return Anzahl
     */
    default int getQuestCount() {
        return getQuests().size();
    }

    /**
     * Gibt die freie Kapazität zurück.
     *
     * @return Anzahl freier Slots
     */
    default int getFreeQuestCapacity() {
        return getMaxQuests() - getQuestCount();
    }
}
