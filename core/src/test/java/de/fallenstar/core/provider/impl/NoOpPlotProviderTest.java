package de.fallenstar.core.provider.impl;

import de.fallenstar.core.exception.ProviderFunctionalityNotFoundException;
import de.fallenstar.core.provider.Plot;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Unit-Tests für NoOpPlotProvider.
 *
 * Testet dass:
 * - isAvailable() false zurückgibt
 * - Alle Methoden ProviderFunctionalityNotFoundException werfen
 * - Exception-Messages korrekte Informationen enthalten
 *
 * @author FallenStar
 * @version 1.0
 */
@DisplayName("NoOpPlotProvider Tests")
class NoOpPlotProviderTest {

    private NoOpPlotProvider provider;
    private Location mockLocation;
    private Player mockPlayer;
    private Plot mockPlot;

    @BeforeEach
    void setUp() {
        provider = new NoOpPlotProvider();
        mockLocation = mock(Location.class);
        mockPlayer = mock(Player.class);
        mockPlot = mock(Plot.class);
    }

    @Test
    @DisplayName("isAvailable() sollte false zurückgeben")
    void testIsAvailable_ReturnsFalse() {
        assertFalse(provider.isAvailable(),
            "NoOpPlotProvider sollte nicht verfügbar sein");
    }

    @Test
    @DisplayName("getPlot() sollte ProviderFunctionalityNotFoundException werfen")
    void testGetPlot_ThrowsException() {
        ProviderFunctionalityNotFoundException exception =
            assertThrows(ProviderFunctionalityNotFoundException.class,
                () -> provider.getPlot(mockLocation),
                "getPlot() sollte Exception werfen");

        assertExceptionContent(exception, "getPlot");
    }

    @Test
    @DisplayName("canBuild() sollte ProviderFunctionalityNotFoundException werfen")
    void testCanBuild_ThrowsException() {
        ProviderFunctionalityNotFoundException exception =
            assertThrows(ProviderFunctionalityNotFoundException.class,
                () -> provider.canBuild(mockPlayer, mockLocation),
                "canBuild() sollte Exception werfen");

        assertExceptionContent(exception, "canBuild");
    }

    @Test
    @DisplayName("getOwnerName() sollte ProviderFunctionalityNotFoundException werfen")
    void testGetOwnerName_ThrowsException() {
        ProviderFunctionalityNotFoundException exception =
            assertThrows(ProviderFunctionalityNotFoundException.class,
                () -> provider.getOwnerName(mockPlot),
                "getOwnerName() sollte Exception werfen");

        assertExceptionContent(exception, "getOwnerName");
    }

    @Test
    @DisplayName("getPlotType() sollte ProviderFunctionalityNotFoundException werfen")
    void testGetPlotType_ThrowsException() {
        ProviderFunctionalityNotFoundException exception =
            assertThrows(ProviderFunctionalityNotFoundException.class,
                () -> provider.getPlotType(mockPlot),
                "getPlotType() sollte Exception werfen");

        assertExceptionContent(exception, "getPlotType");
    }

    @Test
    @DisplayName("hasAdminRights() sollte ProviderFunctionalityNotFoundException werfen")
    void testHasAdminRights_ThrowsException() {
        ProviderFunctionalityNotFoundException exception =
            assertThrows(ProviderFunctionalityNotFoundException.class,
                () -> provider.hasAdminRights(mockPlayer, mockPlot),
                "hasAdminRights() sollte Exception werfen");

        assertExceptionContent(exception, "hasAdminRights");
    }

    /**
     * Hilfsmethode: Prüft ob Exception korrekte Informationen enthält.
     */
    private void assertExceptionContent(
            ProviderFunctionalityNotFoundException exception,
            String expectedMethodName) {

        String message = exception.getMessage();

        assertNotNull(message, "Exception message sollte nicht null sein");
        assertTrue(message.contains("PlotProvider"),
            "Exception sollte Provider-Namen enthalten");
        assertTrue(message.contains(expectedMethodName),
            "Exception sollte Methoden-Namen enthalten: " + expectedMethodName);
        assertTrue(message.contains("plot plugin") || message.contains("Towny") || message.contains("Factions"),
            "Exception sollte Grund enthalten");
    }
}
