package de.fallenstar.core.provider.impl;

import de.fallenstar.core.exception.ProviderFunctionalityNotFoundException;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Unit-Tests für NoOpItemProvider.
 *
 * Testet dass:
 * - isAvailable() false zurückgibt
 * - Alle Methoden ProviderFunctionalityNotFoundException werfen
 * - Exception-Messages korrekte Informationen enthalten
 *
 * @author FallenStar
 * @version 1.0
 */
@DisplayName("NoOpItemProvider Tests")
class NoOpItemProviderTest {

    private NoOpItemProvider provider;
    private ItemStack mockItemStack;

    @BeforeEach
    void setUp() {
        provider = new NoOpItemProvider();
        mockItemStack = mock(ItemStack.class);
    }

    @Test
    @DisplayName("isAvailable() sollte false zurückgeben")
    void testIsAvailable_ReturnsFalse() {
        assertFalse(provider.isAvailable(),
            "NoOpItemProvider sollte nicht verfügbar sein");
    }

    @Test
    @DisplayName("createItem() sollte ProviderFunctionalityNotFoundException werfen")
    void testCreateItem_ThrowsException() {
        ProviderFunctionalityNotFoundException exception =
            assertThrows(ProviderFunctionalityNotFoundException.class,
                () -> provider.createItem("test_item"),
                "createItem() sollte Exception werfen");

        assertExceptionContent(exception, "createItem");
    }

    @Test
    @DisplayName("getItemId() sollte ProviderFunctionalityNotFoundException werfen")
    void testGetItemId_ThrowsException() {
        ProviderFunctionalityNotFoundException exception =
            assertThrows(ProviderFunctionalityNotFoundException.class,
                () -> provider.getItemId(mockItemStack),
                "getItemId() sollte Exception werfen");

        assertExceptionContent(exception, "getItemId");
    }

    @Test
    @DisplayName("isCustomItem() sollte ProviderFunctionalityNotFoundException werfen")
    void testIsCustomItem_ThrowsException() {
        ProviderFunctionalityNotFoundException exception =
            assertThrows(ProviderFunctionalityNotFoundException.class,
                () -> provider.isCustomItem(mockItemStack),
                "isCustomItem() sollte Exception werfen");

        assertExceptionContent(exception, "isCustomItem");
    }

    @Test
    @DisplayName("getItemsByCategory() sollte ProviderFunctionalityNotFoundException werfen")
    void testGetItemsByCategory_ThrowsException() {
        ProviderFunctionalityNotFoundException exception =
            assertThrows(ProviderFunctionalityNotFoundException.class,
                () -> provider.getItemsByCategory("weapons"),
                "getItemsByCategory() sollte Exception werfen");

        assertExceptionContent(exception, "getItemsByCategory");
    }

    @Test
    @DisplayName("getCategories() sollte ProviderFunctionalityNotFoundException werfen")
    void testGetCategories_ThrowsException() {
        ProviderFunctionalityNotFoundException exception =
            assertThrows(ProviderFunctionalityNotFoundException.class,
                () -> provider.getCategories(),
                "getCategories() sollte Exception werfen");

        assertExceptionContent(exception, "getCategories");
    }

    @Test
    @DisplayName("getItemCategory() sollte ProviderFunctionalityNotFoundException werfen")
    void testGetItemCategory_ThrowsException() {
        ProviderFunctionalityNotFoundException exception =
            assertThrows(ProviderFunctionalityNotFoundException.class,
                () -> provider.getItemCategory("test_item"),
                "getItemCategory() sollte Exception werfen");

        assertExceptionContent(exception, "getItemCategory");
    }

    @Test
    @DisplayName("getSuggestedPrice() sollte ProviderFunctionalityNotFoundException werfen")
    void testGetSuggestedPrice_ThrowsException() {
        ProviderFunctionalityNotFoundException exception =
            assertThrows(ProviderFunctionalityNotFoundException.class,
                () -> provider.getSuggestedPrice("test_item"),
                "getSuggestedPrice() sollte Exception werfen");

        assertExceptionContent(exception, "getSuggestedPrice");
    }

    /**
     * Hilfsmethode: Prüft ob Exception korrekte Informationen enthält.
     */
    private void assertExceptionContent(
            ProviderFunctionalityNotFoundException exception,
            String expectedMethodName) {

        String message = exception.getMessage();

        assertNotNull(message, "Exception message sollte nicht null sein");
        assertTrue(message.contains("ItemProvider"),
            "Exception sollte Provider-Namen enthalten");
        assertTrue(message.contains(expectedMethodName),
            "Exception sollte Methoden-Namen enthalten: " + expectedMethodName);
        assertTrue(message.contains("item") || message.contains("MMO") || message.contains("ItemsAdder"),
            "Exception sollte Grund enthalten");
    }
}
