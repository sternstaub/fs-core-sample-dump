package de.fallenstar.core.provider.impl;

import de.fallenstar.core.exception.ProviderFunctionalityNotFoundException;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Unit-Tests für NoOpNPCProvider.
 *
 * Testet dass:
 * - isAvailable() false zurückgibt
 * - Alle Methoden ProviderFunctionalityNotFoundException werfen
 * - Exception-Messages korrekte Informationen enthalten
 *
 * @author FallenStar
 * @version 1.0
 */
@DisplayName("NoOpNPCProvider Tests")
class NoOpNPCProviderTest {

    private NoOpNPCProvider provider;
    private Location mockLocation;
    private UUID mockNpcId;
    private Consumer<Player> mockHandler;

    @BeforeEach
    void setUp() {
        provider = new NoOpNPCProvider();
        mockLocation = mock(Location.class);
        mockNpcId = UUID.randomUUID();
        mockHandler = player -> {}; // Einfacher Lambda-Handler
    }

    @Test
    @DisplayName("isAvailable() sollte false zurückgeben")
    void testIsAvailable_ReturnsFalse() {
        assertFalse(provider.isAvailable(),
            "NoOpNPCProvider sollte nicht verfügbar sein");
    }

    @Test
    @DisplayName("createNPC() sollte ProviderFunctionalityNotFoundException werfen")
    void testCreateNPC_ThrowsException() {
        ProviderFunctionalityNotFoundException exception =
            assertThrows(ProviderFunctionalityNotFoundException.class,
                () -> provider.createNPC(mockLocation, "TestNPC", "TestSkin"),
                "createNPC() sollte Exception werfen");

        assertExceptionContent(exception, "createNPC");
    }

    @Test
    @DisplayName("removeNPC() sollte ProviderFunctionalityNotFoundException werfen")
    void testRemoveNPC_ThrowsException() {
        ProviderFunctionalityNotFoundException exception =
            assertThrows(ProviderFunctionalityNotFoundException.class,
                () -> provider.removeNPC(mockNpcId),
                "removeNPC() sollte Exception werfen");

        assertExceptionContent(exception, "removeNPC");
    }

    @Test
    @DisplayName("teleportNPC() sollte ProviderFunctionalityNotFoundException werfen")
    void testTeleportNPC_ThrowsException() {
        ProviderFunctionalityNotFoundException exception =
            assertThrows(ProviderFunctionalityNotFoundException.class,
                () -> provider.teleportNPC(mockNpcId, mockLocation),
                "teleportNPC() sollte Exception werfen");

        assertExceptionContent(exception, "teleportNPC");
    }

    @Test
    @DisplayName("setClickHandler() sollte ProviderFunctionalityNotFoundException werfen")
    void testSetClickHandler_ThrowsException() {
        ProviderFunctionalityNotFoundException exception =
            assertThrows(ProviderFunctionalityNotFoundException.class,
                () -> provider.setClickHandler(mockNpcId, mockHandler),
                "setClickHandler() sollte Exception werfen");

        assertExceptionContent(exception, "setClickHandler");
    }

    @Test
    @DisplayName("setSkin() sollte ProviderFunctionalityNotFoundException werfen")
    void testSetSkin_ThrowsException() {
        ProviderFunctionalityNotFoundException exception =
            assertThrows(ProviderFunctionalityNotFoundException.class,
                () -> provider.setSkin(mockNpcId, "NewSkin"),
                "setSkin() sollte Exception werfen");

        assertExceptionContent(exception, "setSkin");
    }

    @Test
    @DisplayName("npcExists() sollte ProviderFunctionalityNotFoundException werfen")
    void testNpcExists_ThrowsException() {
        ProviderFunctionalityNotFoundException exception =
            assertThrows(ProviderFunctionalityNotFoundException.class,
                () -> provider.npcExists(mockNpcId),
                "npcExists() sollte Exception werfen");

        assertExceptionContent(exception, "npcExists");
    }

    /**
     * Hilfsmethode: Prüft ob Exception korrekte Informationen enthält.
     */
    private void assertExceptionContent(
            ProviderFunctionalityNotFoundException exception,
            String expectedMethodName) {

        String message = exception.getMessage();

        assertNotNull(message, "Exception message sollte nicht null sein");
        assertTrue(message.contains("NPCProvider"),
            "Exception sollte Provider-Namen enthalten");
        assertTrue(message.contains(expectedMethodName),
            "Exception sollte Methoden-Namen enthalten: " + expectedMethodName);
        assertTrue(message.contains("NPC") || message.contains("Citizens") || message.contains("ZNPC"),
            "Exception sollte Grund enthalten");
    }
}
