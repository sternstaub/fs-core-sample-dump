package de.fallenstar.core.distributor;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Distributor für Quests an NPCs.
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
 * @version 1.0
 */
public interface QuestDistributor extends Distributor<DistributableQuest> {

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
    @Override
    boolean distribute(DistributableQuest quest);

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
