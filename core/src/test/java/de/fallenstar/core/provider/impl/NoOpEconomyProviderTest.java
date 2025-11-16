package de.fallenstar.core.provider.impl;

import de.fallenstar.core.exception.ProviderFunctionalityNotFoundException;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Unit-Tests für NoOpEconomyProvider.
 *
 * Testet dass:
 * - isAvailable() false zurückgibt
 * - Alle Methoden ProviderFunctionalityNotFoundException werfen
 * - Exception-Messages korrekte Informationen enthalten
 *
 * @author FallenStar
 * @version 1.0
 */
@DisplayName("NoOpEconomyProvider Tests")
class NoOpEconomyProviderTest {

    private NoOpEconomyProvider provider;
    private Player mockPlayer;
    private UUID mockAccountId;

    @BeforeEach
    void setUp() {
        provider = new NoOpEconomyProvider();
        mockPlayer = mock(Player.class);
        mockAccountId = UUID.randomUUID();
    }

    @Test
    @DisplayName("isAvailable() sollte false zurückgeben")
    void testIsAvailable_ReturnsFalse() {
        assertFalse(provider.isAvailable(),
            "NoOpEconomyProvider sollte nicht verfügbar sein");
    }

    @Test
    @DisplayName("getBalance() sollte ProviderFunctionalityNotFoundException werfen")
    void testGetBalance_ThrowsException() {
        ProviderFunctionalityNotFoundException exception =
            assertThrows(ProviderFunctionalityNotFoundException.class,
                () -> provider.getBalance(mockPlayer),
                "getBalance() sollte Exception werfen");

        assertExceptionContent(exception, "getBalance");
    }

    @Test
    @DisplayName("withdraw() sollte ProviderFunctionalityNotFoundException werfen")
    void testWithdraw_ThrowsException() {
        ProviderFunctionalityNotFoundException exception =
            assertThrows(ProviderFunctionalityNotFoundException.class,
                () -> provider.withdraw(mockPlayer, 100.0),
                "withdraw() sollte Exception werfen");

        assertExceptionContent(exception, "withdraw");
    }

    @Test
    @DisplayName("deposit() sollte ProviderFunctionalityNotFoundException werfen")
    void testDeposit_ThrowsException() {
        ProviderFunctionalityNotFoundException exception =
            assertThrows(ProviderFunctionalityNotFoundException.class,
                () -> provider.deposit(mockPlayer, 100.0),
                "deposit() sollte Exception werfen");

        assertExceptionContent(exception, "deposit");
    }

    @Test
    @DisplayName("getFactionBalance() sollte ProviderFunctionalityNotFoundException werfen")
    void testGetFactionBalance_ThrowsException() {
        ProviderFunctionalityNotFoundException exception =
            assertThrows(ProviderFunctionalityNotFoundException.class,
                () -> provider.getFactionBalance(mockAccountId),
                "getFactionBalance() sollte Exception werfen");

        assertExceptionContent(exception, "getFactionBalance");
    }

    @Test
    @DisplayName("withdrawFaction() sollte ProviderFunctionalityNotFoundException werfen")
    void testWithdrawFaction_ThrowsException() {
        ProviderFunctionalityNotFoundException exception =
            assertThrows(ProviderFunctionalityNotFoundException.class,
                () -> provider.withdrawFaction(mockAccountId, 100.0),
                "withdrawFaction() sollte Exception werfen");

        assertExceptionContent(exception, "withdrawFaction");
    }

    @Test
    @DisplayName("depositFaction() sollte ProviderFunctionalityNotFoundException werfen")
    void testDepositFaction_ThrowsException() {
        ProviderFunctionalityNotFoundException exception =
            assertThrows(ProviderFunctionalityNotFoundException.class,
                () -> provider.depositFaction(mockAccountId, 100.0),
                "depositFaction() sollte Exception werfen");

        assertExceptionContent(exception, "depositFaction");
    }

    /**
     * Hilfsmethode: Prüft ob Exception korrekte Informationen enthält.
     */
    private void assertExceptionContent(
            ProviderFunctionalityNotFoundException exception,
            String expectedMethodName) {

        String message = exception.getMessage();

        assertNotNull(message, "Exception message sollte nicht null sein");
        assertTrue(message.contains("EconomyProvider"),
            "Exception sollte Provider-Namen enthalten");
        assertTrue(message.contains(expectedMethodName),
            "Exception sollte Methoden-Namen enthalten: " + expectedMethodName);
        assertTrue(message.contains("economy") || message.contains("Vault"),
            "Exception sollte Grund enthalten");
    }
}
