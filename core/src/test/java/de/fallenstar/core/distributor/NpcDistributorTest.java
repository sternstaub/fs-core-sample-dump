package de.fallenstar.core.distributor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-Tests für NpcDistributor Interface.
 *
 * Testet:
 * - distribute() und undistribute() Logik
 * - Kapazitäts-Management (hasNpcCapacity, getCapacity, getCurrentCount)
 * - Slot-Management (getSlotForNpc, getDistributableNpcInSlot, undistributeNpcFromSlot)
 * - getDistributedNpcs() Rückgabe
 *
 * @author FallenStar
 * @version 1.0
 */
@DisplayName("NpcDistributor Tests")
class NpcDistributorTest {

    private MockNpcDistributor distributor;
    private MockDistributableNpc npc1;
    private MockDistributableNpc npc2;

    @BeforeEach
    void setUp() {
        distributor = new MockNpcDistributor(3); // Kapazität: 3 NPCs
        npc1 = new MockDistributableNpc(UUID.randomUUID(), "trader");
        npc2 = new MockDistributableNpc(UUID.randomUUID(), "guard");
    }

    @Test
    @DisplayName("distribute() sollte NPC erfolgreich hinzufügen")
    void testDistribute_Success() {
        assertTrue(distributor.distribute(npc1),
            "distribute() sollte true zurückgeben");

        assertEquals(1, distributor.getCurrentCount(),
            "getCurrentCount() sollte 1 sein");
        assertTrue(distributor.getDistributedNpcs().contains(npc1),
            "NPC sollte in distribuierten NPCs enthalten sein");
    }

    @Test
    @DisplayName("distribute() sollte bei voller Kapazität false zurückgeben")
    void testDistribute_FullCapacity() {
        // Fülle Distributor bis Kapazität
        distributor.distribute(npc1);
        distributor.distribute(npc2);
        distributor.distribute(new MockDistributableNpc(UUID.randomUUID(), "crafter"));

        assertFalse(distributor.hasNpcCapacity(),
            "hasNpcCapacity() sollte false sein bei voller Kapazität");

        MockDistributableNpc npc4 = new MockDistributableNpc(UUID.randomUUID(), "merchant");
        assertFalse(distributor.distribute(npc4),
            "distribute() sollte false zurückgeben bei voller Kapazität");
    }

    @Test
    @DisplayName("undistribute() sollte NPC erfolgreich entfernen")
    void testUndistribute_Success() {
        distributor.distribute(npc1);

        assertTrue(distributor.undistribute(npc1),
            "undistribute() sollte true zurückgeben");

        assertEquals(0, distributor.getCurrentCount(),
            "getCurrentCount() sollte 0 sein nach Entfernung");
        assertFalse(distributor.getDistributedNpcs().contains(npc1),
            "NPC sollte nicht mehr in distribuierten NPCs enthalten sein");
    }

    @Test
    @DisplayName("undistribute() sollte false zurückgeben wenn NPC nicht distribuiert")
    void testUndistribute_NotDistributed() {
        assertFalse(distributor.undistribute(npc1),
            "undistribute() sollte false zurückgeben für nicht-distribuierten NPC");
    }

    @Test
    @DisplayName("hasNpcCapacity() sollte korrekte Kapazität prüfen")
    void testHasNpcCapacity() {
        assertTrue(distributor.hasNpcCapacity(),
            "hasNpcCapacity() sollte true sein bei leerer Kapazität");

        distributor.distribute(npc1);
        assertTrue(distributor.hasNpcCapacity(),
            "hasNpcCapacity() sollte true sein bei 1/3 Kapazität");

        distributor.distribute(npc2);
        distributor.distribute(new MockDistributableNpc(UUID.randomUUID(), "crafter"));
        assertFalse(distributor.hasNpcCapacity(),
            "hasNpcCapacity() sollte false sein bei voller Kapazität");
    }

    @Test
    @DisplayName("getCapacity() sollte korrekte maximale Kapazität zurückgeben")
    void testGetCapacity() {
        assertEquals(3, distributor.getCapacity(),
            "getCapacity() sollte 3 zurückgeben");
    }

    @Test
    @DisplayName("getCurrentCount() sollte korrekte Anzahl distribuierter NPCs zurückgeben")
    void testGetCurrentCount() {
        assertEquals(0, distributor.getCurrentCount(),
            "getCurrentCount() sollte 0 sein bei leerem Distributor");

        distributor.distribute(npc1);
        assertEquals(1, distributor.getCurrentCount(),
            "getCurrentCount() sollte 1 sein nach Hinzufügen eines NPCs");

        distributor.distribute(npc2);
        assertEquals(2, distributor.getCurrentCount(),
            "getCurrentCount() sollte 2 sein nach Hinzufügen zweier NPCs");

        distributor.undistribute(npc1);
        assertEquals(1, distributor.getCurrentCount(),
            "getCurrentCount() sollte 1 sein nach Entfernung eines NPCs");
    }

    @Test
    @DisplayName("getDistributedNpcs() sollte Liste aller distribuierten NPCs zurückgeben")
    void testGetDistributedNpcs() {
        assertTrue(distributor.getDistributedNpcs().isEmpty(),
            "getDistributedNpcs() sollte leer sein bei leerem Distributor");

        distributor.distribute(npc1);
        distributor.distribute(npc2);

        List<DistributableNpc> distributed = distributor.getDistributedNpcs();
        assertEquals(2, distributed.size(),
            "getDistributedNpcs() sollte 2 NPCs enthalten");
        assertTrue(distributed.contains(npc1),
            "Liste sollte npc1 enthalten");
        assertTrue(distributed.contains(npc2),
            "Liste sollte npc2 enthalten");
    }

    @Test
    @DisplayName("getSlotForNpc() sollte korrekten Slot zurückgeben")
    void testGetSlotForNpc() {
        distributor.distribute(npc1);

        Optional<Integer> slot = distributor.getSlotForNpc(npc1.getEntityId().get());
        assertTrue(slot.isPresent(),
            "getSlotForNpc() sollte Slot zurückgeben für distribuierten NPC");
        assertEquals(0, slot.get(),
            "Erster NPC sollte Slot 0 haben");
    }

    @Test
    @DisplayName("getSlotForNpc() sollte empty zurückgeben für nicht-distribuierten NPC")
    void testGetSlotForNpc_NotFound() {
        Optional<Integer> slot = distributor.getSlotForNpc(UUID.randomUUID());
        assertFalse(slot.isPresent(),
            "getSlotForNpc() sollte empty zurückgeben für nicht-distribuierten NPC");
    }

    @Test
    @DisplayName("getDistributableNpcInSlot() sollte NPC für gültigen Slot zurückgeben")
    void testGetDistributableNpcInSlot() {
        distributor.distribute(npc1);

        Optional<DistributableNpc> npcInSlot = distributor.getDistributableNpcInSlot(0);
        assertTrue(npcInSlot.isPresent(),
            "getDistributableNpcInSlot() sollte NPC zurückgeben");
        assertEquals(npc1, npcInSlot.get(),
            "NPC sollte npc1 sein");
    }

    @Test
    @DisplayName("getDistributableNpcInSlot() sollte empty zurückgeben für leeren Slot")
    void testGetDistributableNpcInSlot_Empty() {
        Optional<DistributableNpc> npcInSlot = distributor.getDistributableNpcInSlot(0);
        assertFalse(npcInSlot.isPresent(),
            "getDistributableNpcInSlot() sollte empty zurückgeben für leeren Slot");
    }

    @Test
    @DisplayName("undistributeNpcFromSlot() sollte NPC aus Slot entfernen")
    void testUndistributeNpcFromSlot() {
        distributor.distribute(npc1);

        assertTrue(distributor.undistributeNpcFromSlot(0),
            "undistributeNpcFromSlot() sollte true zurückgeben");
        assertEquals(0, distributor.getCurrentCount(),
            "getCurrentCount() sollte 0 sein nach Entfernung");
    }

    @Test
    @DisplayName("undistributeNpcFromSlot() sollte false zurückgeben für leeren Slot")
    void testUndistributeNpcFromSlot_EmptySlot() {
        assertFalse(distributor.undistributeNpcFromSlot(0),
            "undistributeNpcFromSlot() sollte false zurückgeben für leeren Slot");
    }

    // ========== Mock-Implementierung für Tests ==========

    /**
     * Mock-Implementierung von NpcDistributor für Tests.
     */
    private static class MockNpcDistributor implements NpcDistributor {
        private final int capacity;
        private final Map<Integer, DistributableNpc> slots = new HashMap<>();
        private final Map<UUID, Integer> npcSlots = new HashMap<>();

        public MockNpcDistributor(int capacity) {
            this.capacity = capacity;
        }

        @Override
        public boolean distribute(DistributableNpc npc) {
            if (!hasNpcCapacity()) {
                return false;
            }

            // Finde freien Slot
            int freeSlot = -1;
            for (int i = 0; i < capacity; i++) {
                if (!slots.containsKey(i)) {
                    freeSlot = i;
                    break;
                }
            }

            if (freeSlot == -1) {
                return false;
            }

            // Setze Entity-ID für Mock
            UUID entityId = UUID.randomUUID();
            ((MockDistributableNpc) npc).setEntityId(entityId);

            slots.put(freeSlot, npc);
            npcSlots.put(entityId, freeSlot);
            npc.onDistributed(this);

            return true;
        }

        @Override
        public boolean undistribute(DistributableNpc npc) {
            Optional<UUID> entityIdOpt = npc.getEntityId();
            if (entityIdOpt.isEmpty()) {
                return false;
            }

            UUID entityId = entityIdOpt.get();
            Integer slot = npcSlots.remove(entityId);
            if (slot == null) {
                return false;
            }

            slots.remove(slot);
            npc.onUndistributed();

            return true;
        }

        @Override
        public int getCapacity() {
            return capacity;
        }

        @Override
        public int getCurrentCount() {
            return slots.size();
        }

        @Override
        public boolean hasNpcCapacity() {
            return getCurrentCount() < getCapacity();
        }

        @Override
        public List<DistributableNpc> getDistributedNpcs() {
            return new ArrayList<>(slots.values());
        }

        @Override
        public Optional<Integer> getSlotForNpc(UUID npcId) {
            return Optional.ofNullable(npcSlots.get(npcId));
        }

        @Override
        public Optional<DistributableNpc> getDistributableNpcInSlot(int slot) {
            return Optional.ofNullable(slots.get(slot));
        }

        @Override
        public boolean undistributeNpcFromSlot(int slot) {
            DistributableNpc npc = slots.get(slot);
            if (npc == null) {
                return false;
            }

            return undistribute(npc);
        }
    }

    /**
     * Mock-Implementierung von DistributableNpc für Tests.
     */
    private static class MockDistributableNpc implements DistributableNpc {
        private final UUID id;
        private final String npcType;
        private UUID entityId;

        public MockDistributableNpc(UUID id, String npcType) {
            this.id = id;
            this.npcType = npcType;
        }

        public void setEntityId(UUID entityId) {
            this.entityId = entityId;
        }

        @Override
        public UUID getId() {
            return id;
        }

        @Override
        public String getType() {
            return "npc";
        }

        @Override
        public String getNpcType() {
            return npcType;
        }

        @Override
        public String getDisplayName() {
            return "§e" + npcType + " NPC";
        }

        @Override
        public Optional<UUID> getEntityId() {
            return Optional.ofNullable(entityId);
        }

        @Override
        public UUID spawn(org.bukkit.Location location) {
            this.entityId = UUID.randomUUID();
            return entityId;
        }

        @Override
        public void despawn() {
            this.entityId = null;
        }

        @Override
        public boolean isSpawned() {
            return entityId != null;
        }
    }
}
