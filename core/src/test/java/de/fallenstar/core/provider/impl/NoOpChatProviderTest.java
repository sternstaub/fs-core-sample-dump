package de.fallenstar.core.provider.impl;

import de.fallenstar.core.exception.ProviderFunctionalityNotFoundException;
import de.fallenstar.core.provider.ChatProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-Tests für NoOpChatProvider.
 *
 * Testet dass:
 * - isAvailable() false zurückgibt
 * - Alle Methoden ProviderFunctionalityNotFoundException werfen
 * - Exception-Messages korrekte Informationen enthalten
 *
 * @author FallenStar
 * @version 1.0
 */
@DisplayName("NoOpChatProvider Tests")
class NoOpChatProviderTest {

    private NoOpChatProvider provider;
    private UUID mockUuid;
    private ChatProvider.ExternalCommandHandler mockHandler;

    @BeforeEach
    void setUp() {
        provider = new NoOpChatProvider();
        mockUuid = UUID.randomUUID();
        mockHandler = args -> {}; // Einfacher Lambda-Handler
    }

    @Test
    @DisplayName("isAvailable() sollte false zurückgeben")
    void testIsAvailable_ReturnsFalse() {
        assertFalse(provider.isAvailable(),
            "NoOpChatProvider sollte nicht verfügbar sein");
    }

    @Test
    @DisplayName("sendMessage() sollte ProviderFunctionalityNotFoundException werfen")
    void testSendMessage_ThrowsException() {
        ProviderFunctionalityNotFoundException exception =
            assertThrows(ProviderFunctionalityNotFoundException.class,
                () -> provider.sendMessage("room123", "Hello"),
                "sendMessage() sollte Exception werfen");

        assertExceptionContent(exception, "sendMessage");
    }

    @Test
    @DisplayName("sendDirectMessage() sollte ProviderFunctionalityNotFoundException werfen")
    void testSendDirectMessage_ThrowsException() {
        ProviderFunctionalityNotFoundException exception =
            assertThrows(ProviderFunctionalityNotFoundException.class,
                () -> provider.sendDirectMessage("user123", "Hello"),
                "sendDirectMessage() sollte Exception werfen");

        assertExceptionContent(exception, "sendDirectMessage");
    }

    @Test
    @DisplayName("linkUser() sollte ProviderFunctionalityNotFoundException werfen")
    void testLinkUser_ThrowsException() {
        ProviderFunctionalityNotFoundException exception =
            assertThrows(ProviderFunctionalityNotFoundException.class,
                () -> provider.linkUser(mockUuid, "externalUser"),
                "linkUser() sollte Exception werfen");

        assertExceptionContent(exception, "linkUser");
    }

    @Test
    @DisplayName("getLinkedUser() sollte ProviderFunctionalityNotFoundException werfen")
    void testGetLinkedUser_ThrowsException() {
        ProviderFunctionalityNotFoundException exception =
            assertThrows(ProviderFunctionalityNotFoundException.class,
                () -> provider.getLinkedUser(mockUuid),
                "getLinkedUser() sollte Exception werfen");

        assertExceptionContent(exception, "getLinkedUser");
    }

    @Test
    @DisplayName("sendEmbedMessage() sollte ProviderFunctionalityNotFoundException werfen")
    void testSendEmbedMessage_ThrowsException() {
        ProviderFunctionalityNotFoundException exception =
            assertThrows(ProviderFunctionalityNotFoundException.class,
                () -> provider.sendEmbedMessage("room123", "Title", "Description", "#FF0000"),
                "sendEmbedMessage() sollte Exception werfen");

        assertExceptionContent(exception, "sendEmbedMessage");
    }

    @Test
    @DisplayName("registerExternalCommand() sollte ProviderFunctionalityNotFoundException werfen")
    void testRegisterExternalCommand_ThrowsException() {
        ProviderFunctionalityNotFoundException exception =
            assertThrows(ProviderFunctionalityNotFoundException.class,
                () -> provider.registerExternalCommand("test", mockHandler),
                "registerExternalCommand() sollte Exception werfen");

        assertExceptionContent(exception, "registerExternalCommand");
    }

    /**
     * Hilfsmethode: Prüft ob Exception korrekte Informationen enthält.
     */
    private void assertExceptionContent(
            ProviderFunctionalityNotFoundException exception,
            String expectedMethodName) {

        String message = exception.getMessage();

        assertNotNull(message, "Exception message sollte nicht null sein");
        assertTrue(message.contains("ChatProvider"),
            "Exception sollte Provider-Namen enthalten");
        assertTrue(message.contains(expectedMethodName),
            "Exception sollte Methoden-Namen enthalten: " + expectedMethodName);
        assertTrue(message.contains("chat") || message.contains("Matrix") || message.contains("Discord"),
            "Exception sollte Grund enthalten");
    }
}
