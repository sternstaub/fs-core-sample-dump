package de.fallenstar.core.distributor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-Tests für QuestDistributor Interface.
 *
 * Testet:
 * - distribute() und undistribute() Logik
 * - Quest-Container-Verwaltung
 * - Kapazitäts-Management (hasQuestCapacity, getCapacity, getCurrentCount)
 * - getDistributedQuests() Rückgabe
 * - Automatische Verteilung an verfügbare Container
 *
 * @author FallenStar
 * @version 1.0
 */
@DisplayName("QuestDistributor Tests")
class QuestDistributorTest {

    private MockQuestDistributor distributor;
    private MockQuestContainer container1;
    private MockQuestContainer container2;
    private MockDistributableQuest quest1;
    private MockDistributableQuest quest2;

    @BeforeEach
    void setUp() {
        distributor = new MockQuestDistributor();
        container1 = new MockQuestContainer(UUID.randomUUID(), "Trader NPC", 3);
        container2 = new MockQuestContainer(UUID.randomUUID(), "Guard NPC", 2);

        distributor.addContainer(container1);
        distributor.addContainer(container2);

        quest1 = new MockDistributableQuest(UUID.randomUUID(), "Collect 10 Wood");
        quest2 = new MockDistributableQuest(UUID.randomUUID(), "Kill 5 Zombies");
    }

    @Test
    @DisplayName("distribute() sollte Quest erfolgreich zu Container hinzufügen")
    void testDistribute_Success() {
        assertTrue(distributor.distribute(quest1),
            "distribute() sollte true zurückgeben");

        assertEquals(1, distributor.getCurrentCount(),
            "getCurrentCount() sollte 1 sein");
        assertTrue(distributor.getDistributedQuests().contains(quest1),
            "Quest sollte in distribuierten Quests enthalten sein");
    }

    @Test
    @DisplayName("distribute() sollte false zurückgeben wenn keine Container vorhanden")
    void testDistribute_NoContainers() {
        MockQuestDistributor emptyDistributor = new MockQuestDistributor();

        assertFalse(emptyDistributor.distribute(quest1),
            "distribute() sollte false zurückgeben ohne Container");
    }

    @Test
    @DisplayName("distribute() sollte false zurückgeben bei voller Kapazität")
    void testDistribute_FullCapacity() {
        // Fülle alle Container
        distributor.distribute(quest1);
        distributor.distribute(quest2);
        distributor.distribute(new MockDistributableQuest(UUID.randomUUID(), "Quest 3"));
        distributor.distribute(new MockDistributableQuest(UUID.randomUUID(), "Quest 4"));
        distributor.distribute(new MockDistributableQuest(UUID.randomUUID(), "Quest 5"));

        assertFalse(distributor.hasQuestCapacity(),
            "hasQuestCapacity() sollte false sein bei voller Kapazität");

        MockDistributableQuest quest6 = new MockDistributableQuest(UUID.randomUUID(), "Quest 6");
        assertFalse(distributor.distribute(quest6),
            "distribute() sollte false zurückgeben bei voller Kapazität");
    }

    @Test
    @DisplayName("undistribute() sollte Quest erfolgreich entfernen")
    void testUndistribute_Success() {
        distributor.distribute(quest1);

        assertTrue(distributor.undistribute(quest1),
            "undistribute() sollte true zurückgeben");

        assertEquals(0, distributor.getCurrentCount(),
            "getCurrentCount() sollte 0 sein nach Entfernung");
        assertFalse(distributor.getDistributedQuests().contains(quest1),
            "Quest sollte nicht mehr in distribuierten Quests enthalten sein");
    }

    @Test
    @DisplayName("undistribute() sollte false zurückgeben wenn Quest nicht distribuiert")
    void testUndistribute_NotDistributed() {
        assertFalse(distributor.undistribute(quest1),
            "undistribute() sollte false zurückgeben für nicht-distribuierte Quest");
    }

    @Test
    @DisplayName("hasQuestCapacity() sollte korrekte Kapazität prüfen")
    void testHasQuestCapacity() {
        assertTrue(distributor.hasQuestCapacity(),
            "hasQuestCapacity() sollte true sein bei leerer Kapazität");

        // Fülle Container 1 (3 Quests)
        distributor.distribute(quest1);
        distributor.distribute(quest2);
        distributor.distribute(new MockDistributableQuest(UUID.randomUUID(), "Quest 3"));
        assertTrue(distributor.hasQuestCapacity(),
            "hasQuestCapacity() sollte true sein solange Container 2 noch Platz hat");

        // Fülle Container 2 (2 Quests)
        distributor.distribute(new MockDistributableQuest(UUID.randomUUID(), "Quest 4"));
        distributor.distribute(new MockDistributableQuest(UUID.randomUUID(), "Quest 5"));
        assertFalse(distributor.hasQuestCapacity(),
            "hasQuestCapacity() sollte false sein bei voller Kapazität");
    }

    @Test
    @DisplayName("getCapacity() sollte Summe aller Container-Kapazitäten zurückgeben")
    void testGetCapacity() {
        assertEquals(5, distributor.getCapacity(),
            "getCapacity() sollte 5 zurückgeben (3 + 2)");
    }

    @Test
    @DisplayName("getCurrentCount() sollte korrekte Anzahl distribuierter Quests zurückgeben")
    void testGetCurrentCount() {
        assertEquals(0, distributor.getCurrentCount(),
            "getCurrentCount() sollte 0 sein bei leerem Distributor");

        distributor.distribute(quest1);
        assertEquals(1, distributor.getCurrentCount(),
            "getCurrentCount() sollte 1 sein nach Hinzufügen einer Quest");

        distributor.distribute(quest2);
        assertEquals(2, distributor.getCurrentCount(),
            "getCurrentCount() sollte 2 sein nach Hinzufügen zweier Quests");

        distributor.undistribute(quest1);
        assertEquals(1, distributor.getCurrentCount(),
            "getCurrentCount() sollte 1 sein nach Entfernung einer Quest");
    }

    @Test
    @DisplayName("getDistributedQuests() sollte Liste aller distribuierten Quests zurückgeben")
    void testGetDistributedQuests() {
        assertTrue(distributor.getDistributedQuests().isEmpty(),
            "getDistributedQuests() sollte leer sein bei leerem Distributor");

        distributor.distribute(quest1);
        distributor.distribute(quest2);

        List<DistributableQuest> distributed = distributor.getDistributedQuests();
        assertEquals(2, distributed.size(),
            "getDistributedQuests() sollte 2 Quests enthalten");
        assertTrue(distributed.contains(quest1),
            "Liste sollte quest1 enthalten");
        assertTrue(distributed.contains(quest2),
            "Liste sollte quest2 enthalten");
    }

    @Test
    @DisplayName("getQuestContainers() sollte alle Container zurückgeben")
    void testGetQuestContainers() {
        List<QuestContainer> containers = distributor.getQuestContainers();

        assertEquals(2, containers.size(),
            "getQuestContainers() sollte 2 Container zurückgeben");
        assertTrue(containers.contains(container1),
            "Liste sollte container1 enthalten");
        assertTrue(containers.contains(container2),
            "Liste sollte container2 enthalten");
    }

    @Test
    @DisplayName("distributeToContainer() sollte Quest zu spezifischem Container hinzufügen")
    void testDistributeToContainer() {
        assertTrue(distributor.distributeToContainer(quest1, container1),
            "distributeToContainer() sollte true zurückgeben");

        assertTrue(container1.getQuests().contains(quest1),
            "Container 1 sollte quest1 enthalten");
        assertFalse(container2.getQuests().contains(quest1),
            "Container 2 sollte quest1 nicht enthalten");
    }

    @Test
    @DisplayName("distributeToContainer() sollte false zurückgeben bei vollem Container")
    void testDistributeToContainer_FullContainer() {
        // Fülle Container 2 (max 2 Quests)
        container2.addQuest(quest1);
        container2.addQuest(quest2);

        assertFalse(distributor.distributeToContainer(
                new MockDistributableQuest(UUID.randomUUID(), "Quest 3"),
                container2),
            "distributeToContainer() sollte false zurückgeben bei vollem Container");
    }

    @Test
    @DisplayName("Quest sollte korrekt zu Container zugewiesen werden")
    void testQuestContainerAssignment() {
        distributor.distribute(quest1);

        Optional<QuestContainer> containerOpt = quest1.getCurrentContainer();
        assertTrue(containerOpt.isPresent(),
            "Quest sollte Container haben nach Distribution");

        QuestContainer container = containerOpt.get();
        assertTrue(distributor.getQuestContainers().contains(container),
            "Container sollte in Distributor-Container-Liste sein");
    }

    // ========== Mock-Implementierung für Tests ==========

    /**
     * Mock-Implementierung von QuestDistributor für Tests.
     */
    private static class MockQuestDistributor implements QuestDistributor {
        private final List<QuestContainer> containers = new ArrayList<>();
        private final List<DistributableQuest> quests = new ArrayList<>();

        public void addContainer(QuestContainer container) {
            containers.add(container);
        }

        @Override
        public boolean distribute(DistributableQuest quest) {
            List<QuestContainer> available = containers.stream()
                .filter(QuestContainer::hasQuestCapacity)
                .toList();

            if (available.isEmpty()) {
                return false;
            }

            // Wähle ersten Container mit Kapazität
            QuestContainer container = available.get(0);

            boolean success = container.addQuest(quest);
            if (success) {
                quest.setCurrentContainer(container);
                quests.add(quest);
                quest.onDistributed(this);
            }

            return success;
        }

        @Override
        public boolean undistribute(DistributableQuest quest) {
            Optional<QuestContainer> containerOpt = quest.getCurrentContainer();
            if (containerOpt.isEmpty()) {
                return false;
            }

            QuestContainer container = containerOpt.get();
            boolean success = container.removeQuest(quest);

            if (success) {
                quest.setCurrentContainer(null);
                quests.remove(quest);
                quest.onUndistributed();
            }

            return success;
        }

        @Override
        public boolean hasQuestCapacity() {
            return containers.stream()
                .anyMatch(QuestContainer::hasQuestCapacity);
        }

        @Override
        public List<DistributableQuest> getDistributedQuests() {
            return new ArrayList<>(quests);
        }

        @Override
        public List<QuestContainer> getQuestContainers() {
            return new ArrayList<>(containers);
        }
    }

    /**
     * Mock-Implementierung von QuestContainer für Tests.
     */
    private static class MockQuestContainer implements QuestContainer {
        private final UUID id;
        private final String name;
        private final int maxQuests;
        private final List<DistributableQuest> quests = new ArrayList<>();

        public MockQuestContainer(UUID id, String name, int maxQuests) {
            this.id = id;
            this.name = name;
            this.maxQuests = maxQuests;
        }

        @Override
        public UUID getContainerId() {
            return id;
        }

        @Override
        public String getContainerName() {
            return name;
        }

        @Override
        public int getMaxQuests() {
            return maxQuests;
        }

        @Override
        public List<DistributableQuest> getQuests() {
            return new ArrayList<>(quests);
        }

        @Override
        public boolean addQuest(DistributableQuest quest) {
            if (!hasQuestCapacity()) {
                return false;
            }
            return quests.add(quest);
        }

        @Override
        public boolean removeQuest(DistributableQuest quest) {
            return quests.remove(quest);
        }
    }

    /**
     * Mock-Implementierung von DistributableQuest für Tests.
     */
    private static class MockDistributableQuest implements DistributableQuest {
        private final UUID id;
        private final String title;
        private QuestContainer currentContainer;

        public MockDistributableQuest(UUID id, String title) {
            this.id = id;
            this.title = title;
        }

        @Override
        public UUID getId() {
            return id;
        }

        @Override
        public String getType() {
            return "quest";
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public String getDescription() {
            return "Test quest description";
        }

        @Override
        public int getLevel() {
            return 1;
        }

        @Override
        public Optional<QuestContainer> getCurrentContainer() {
            return Optional.ofNullable(currentContainer);
        }

        @Override
        public void setCurrentContainer(QuestContainer container) {
            this.currentContainer = container;
        }
    }
}
