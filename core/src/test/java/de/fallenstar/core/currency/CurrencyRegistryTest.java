package de.fallenstar.core.currency;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit-Tests für CurrencyRegistry (Sprint 19 Phase 3).
 *
 * Testet:
 * - Registry-Pattern funktioniert
 * - Validierung von Registrierungen
 * - Duplikate werden rejected
 * - get() + getAll() funktionieren
 *
 * @author FallenStar
 * @version 1.0
 */
@DisplayName("CurrencyRegistry Tests (Sprint 19 Phase 3)")
class CurrencyRegistryTest {

    private CurrencyRegistry registry;
    private Logger mockLogger;

    @BeforeEach
    void setUp() {
        mockLogger = mock(Logger.class);
        registry = new CurrencyRegistry(mockLogger);
    }

    // ========== register() Tests ==========

    @Test
    @DisplayName("register: Registriert CurrencyItem erfolgreich")
    void testRegister_success() {
        CurrencyItem testCurrency = createTestCurrency("test_coin", "Test Coin", Material.GOLD_INGOT);

        assertDoesNotThrow(() -> registry.register(testCurrency));

        assertTrue(registry.isRegistered("test_coin"), "Currency sollte registriert sein");
        assertEquals(1, registry.size(), "Registry sollte 1 Currency enthalten");
    }

    @Test
    @DisplayName("register: Wirft NullPointerException bei null")
    void testRegister_throwsOnNull() {
        assertThrows(NullPointerException.class, () -> {
            registry.register(null);
        }, "Sollte NPE werfen bei null Currency");
    }

    @Test
    @DisplayName("register: Wirft IllegalStateException bei Duplikaten")
    void testRegister_throwsOnDuplicate() {
        CurrencyItem currency1 = createTestCurrency("duplicate_coin", "Coin 1", Material.GOLD_INGOT);
        CurrencyItem currency2 = createTestCurrency("duplicate_coin", "Coin 2", Material.DIAMOND);

        registry.register(currency1);

        assertThrows(IllegalStateException.class, () -> {
            registry.register(currency2);
        }, "Sollte IllegalStateException werfen bei Duplikat-ID");
    }

    @Test
    @DisplayName("register: Wirft IllegalArgumentException bei null/leerem Identifier")
    void testRegister_throwsOnNullIdentifier() {
        CurrencyItem currencyNullId = createTestCurrency(null, "Test", Material.GOLD_INGOT);

        assertThrows(IllegalArgumentException.class, () -> {
            registry.register(currencyNullId);
        }, "Sollte IAE werfen bei null Identifier");
    }

    // ========== get() Tests ==========

    @Test
    @DisplayName("get: Holt registriertes CurrencyItem")
    void testGet_success() {
        CurrencyItem testCurrency = createTestCurrency("get_test", "Get Test", Material.EMERALD);
        registry.register(testCurrency);

        Optional<CurrencyItem> result = registry.get("get_test");

        assertTrue(result.isPresent(), "get() sollte Currency finden");
        assertEquals("get_test", result.get().getIdentifier(), "Identifier sollte übereinstimmen");
    }

    @Test
    @DisplayName("get: Gibt empty Optional bei nicht-registrierter ID")
    void testGet_notFound() {
        Optional<CurrencyItem> result = registry.get("not_exists");

        assertFalse(result.isPresent(), "get() sollte empty Optional zurückgeben");
    }

    @Test
    @DisplayName("get: Gibt empty Optional bei null Identifier")
    void testGet_nullIdentifier() {
        Optional<CurrencyItem> result = registry.get(null);

        assertFalse(result.isPresent(), "get(null) sollte empty Optional zurückgeben");
    }

    // ========== getAll() Tests ==========

    @Test
    @DisplayName("getAll: Gibt alle registrierten Currencies zurück")
    void testGetAll_success() {
        CurrencyItem currency1 = createTestCurrency("coin1", "Coin 1", Material.GOLD_INGOT);
        CurrencyItem currency2 = createTestCurrency("coin2", "Coin 2", Material.DIAMOND);

        registry.register(currency1);
        registry.register(currency2);

        Collection<CurrencyItem> all = registry.getAll();

        assertEquals(2, all.size(), "getAll() sollte 2 Currencies zurückgeben");
        assertTrue(all.contains(currency1), "Sollte currency1 enthalten");
        assertTrue(all.contains(currency2), "Sollte currency2 enthalten");
    }

    @Test
    @DisplayName("getAll: Gibt leere Collection bei leerer Registry")
    void testGetAll_empty() {
        Collection<CurrencyItem> all = registry.getAll();

        assertTrue(all.isEmpty(), "getAll() sollte leer sein");
    }

    // ========== getAllIdentifiers() Tests ==========

    @Test
    @DisplayName("getAllIdentifiers: Gibt alle IDs zurück")
    void testGetAllIdentifiers_success() {
        CurrencyItem currency1 = createTestCurrency("id1", "Currency 1", Material.GOLD_INGOT);
        CurrencyItem currency2 = createTestCurrency("id2", "Currency 2", Material.DIAMOND);

        registry.register(currency1);
        registry.register(currency2);

        Set<String> identifiers = registry.getAllIdentifiers();

        assertEquals(2, identifiers.size(), "Sollte 2 Identifiers zurückgeben");
        assertTrue(identifiers.contains("id1"), "Sollte id1 enthalten");
        assertTrue(identifiers.contains("id2"), "Sollte id2 enthalten");
    }

    // ========== unregister() Tests ==========

    @Test
    @DisplayName("unregister: Entfernt registriertes CurrencyItem")
    void testUnregister_success() {
        CurrencyItem currency = createTestCurrency("to_remove", "Remove Me", Material.IRON_INGOT);
        registry.register(currency);

        boolean removed = registry.unregister("to_remove");

        assertTrue(removed, "unregister() sollte true zurückgeben");
        assertFalse(registry.isRegistered("to_remove"), "Currency sollte nicht mehr registriert sein");
    }

    @Test
    @DisplayName("unregister: Gibt false bei nicht-existierender ID")
    void testUnregister_notFound() {
        boolean removed = registry.unregister("not_exists");

        assertFalse(removed, "unregister() sollte false zurückgeben");
    }

    // ========== clear() Tests ==========

    @Test
    @DisplayName("clear: Löscht alle Currencies")
    void testClear_success() {
        CurrencyItem currency1 = createTestCurrency("clear1", "Clear 1", Material.GOLD_INGOT);
        CurrencyItem currency2 = createTestCurrency("clear2", "Clear 2", Material.DIAMOND);

        registry.register(currency1);
        registry.register(currency2);

        registry.clear();

        assertTrue(registry.isEmpty(), "Registry sollte leer sein");
        assertEquals(0, registry.size(), "Size sollte 0 sein");
    }

    // ========== Helper Methods ==========

    /**
     * Erstellt ein Test-CurrencyItem mit Mock-Implementierung.
     */
    private CurrencyItem createTestCurrency(String identifier, String displayName, Material material) {
        return new CurrencyItem() {
            @Override
            public String getIdentifier() {
                return identifier;
            }

            @Override
            public String getDisplayName() {
                return displayName;
            }

            @Override
            public Material getMaterial() {
                return material;
            }

            @Override
            public ItemStack getDisplayItem() {
                return new ItemStack(material, 1);
            }

            @Override
            public boolean isCurrency(ItemStack item) {
                return item != null && item.getType() == material;
            }

            @Override
            public boolean equals(Object obj) {
                if (this == obj) return true;
                if (!(obj instanceof CurrencyItem other)) return false;
                return getIdentifier() != null && getIdentifier().equals(other.getIdentifier());
            }

            @Override
            public int hashCode() {
                return getIdentifier() != null ? getIdentifier().hashCode() : 0;
            }
        };
    }
}
