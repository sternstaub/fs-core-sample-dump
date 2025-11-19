package de.fallenstar.core.distributor;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Distributor für Quests an NPCs.
 *
 * **HINWEIS:** Erweitert NICHT Distributor<T> um Kompatibilität mit NpcDistributor zu ermöglichen.
 * Java erlaubt nicht, dass eine Klasse Distributor<DistributableNpc> UND Distributor<DistributableQuest>
 * gleichzeitig implementiert (Generics Erasure Problem).
 *
 * **Features:**
 * - Automatische Zuweisung an NPCs
 * - Zufällige Verteilung
 * - Nullable Container-Liste (falls keine NPCs vorhanden)
 *
 * **Verwendung:**
 * <pre>
 * class TradeguildPlot implements QuestDistributor {
 *     {@literal @}Override
 *     public boolean distribute(DistributableQuest quest) {
 *         List&lt;QuestContainer&gt; containers = getQuestContainers();
 *
 *         if (containers.isEmpty()) {
 *             return false; // Keine NPCs vorhanden
 *         }
 *
 *         // Filtere Container mit Kapazität
 *         List&lt;QuestContainer&gt; available = containers.stream()
 *             .filter(QuestContainer::hasQuestCapacity)
 *             .toList();
 *
 *         if (available.isEmpty()) {
 *             return false; // Alle NPCs voll
 *         }
 *
 *         // Wähle zufälligen Container
 *         QuestContainer container = available.get(
 *             ThreadLocalRandom.current().nextInt(available.size())
 *         );
 *
 *         // Weise Quest zu
 *         boolean success = container.addQuest(quest);
 *         if (success) {
 *             quest.setCurrentContainer(container);
 *             quest.onDistributed(this);
 *         }
 *
 *         return success;
 *     }
 * }
 * </pre>
 *
 * @author FallenStar
 * @version 2.0 - Refactored: Entfernt Distributor<T> Vererbung
 */
public interface QuestDistributor {

    /**
     * Distribuiert eine Quest an einen zufälligen NPC.
     *
     * Algorithmus:
     * 1. Hole alle NPCs (QuestContainer)
     * 2. Filtere NPCs mit Kapazität
     * 3. Wähle zufälligen NPC
     * 4. Weise Quest zu
     * 5. Rufe quest.onDistributed() auf
     *
     * @param quest Die Quest
     * @return true wenn erfolgreich
     */
    boolean distribute(DistributableQuest quest);

    /**
     * Entfernt eine distribuierte Quest.
     *
     * @param quest Die Quest
     * @return true wenn erfolgreich entfernt
     */
    boolean undistribute(DistributableQuest quest);

    /**
     * Gibt die maximale Quest-Kapazität zurück.
     *
     * @return Maximale Anzahl an Quests über alle Container
     */
    default int getCapacity() {
        return getQuestContainers().stream()
            .mapToInt(QuestContainer::getMaxQuestCapacity)
            .sum();
    }

    /**
     * Gibt die aktuelle Anzahl distribuierter Quests zurück.
     *
     * @return Anzahl Quests
     */
    default int getCurrentCount() {
        return getDistributedQuests().size();
    }

    /**
     * Prüft ob noch Quest-Kapazität verfügbar ist.
     *
     * @return true wenn Platz frei
     */
    default boolean hasCapacity() {
        return canDistributeQuests();
    }

    /**
     * Gibt alle distribuierten Quests zurück.
     *
     * HINWEIS: Umbenennung von getDistributed() um Konflikt mit NpcDistributor zu vermeiden.
     * NpcDistributor.getDistributedNpcs() gibt List<DistributableNpc> zurück.
     *
     * @return Liste von DistributableQuests
     */
    List<DistributableQuest> getDistributedQuests();

    /**
     * Gibt alle Quest-Container zurück.
     *
     * NULLABLE: Kann empty list zurückgeben wenn keine NPCs vorhanden!
     *
     * @return Liste von QuestContainern (kann leer sein!)
     */
    List<QuestContainer> getQuestContainers();

    /**
     * Distribuiert eine Quest an einen spezifischen Container.
     *
     * Alternative zu distribute() wenn man den Container auswählen will.
     *
     * @param quest Die Quest
     * @param container Der Ziel-Container
     * @return true wenn erfolgreich
     */
    default boolean distributeToContainer(DistributableQuest quest, QuestContainer container) {
        if (!container.hasQuestCapacity()) {
            return false;
        }

        boolean success = container.addQuest(quest);
        if (success) {
            quest.setCurrentContainer(container);
            quest.onDistributed(this);
        }

        return success;
    }

    /**
     * Gibt die Anzahl verfügbarer Quest-Slots zurück.
     *
     * @return Anzahl freier Slots über alle Container
     */
    default int getTotalAvailableQuestSlots() {
        return getQuestContainers().stream()
            .mapToInt(QuestContainer::getFreeQuestCapacity)
            .sum();
    }

    /**
     * Prüft ob mindestens ein Container Platz hat.
     *
     * @return true wenn Quests distribuiert werden können
     */
    default boolean canDistributeQuests() {
        return getQuestContainers().stream()
            .anyMatch(QuestContainer::hasQuestCapacity);
    }
}
