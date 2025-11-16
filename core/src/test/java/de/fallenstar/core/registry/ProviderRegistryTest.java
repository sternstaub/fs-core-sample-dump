package de.fallenstar.core.registry;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.plugin.PluginManagerMock;
import de.fallenstar.core.provider.*;
import de.fallenstar.core.provider.impl.*;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit-Tests für ProviderRegistry.
 *
 * Testet:
 * - Auto-Detection von Plugins
 * - Korrekte Provider-Registrierung
 * - NoOp-Fallbacks wenn Plugins fehlen
 * - Getter-Methoden
 *
 * Verwendet MockBukkit für Bukkit API Mocking.
 *
 * @author FallenStar
 * @version 1.0
 */
@DisplayName("ProviderRegistry Tests")
class ProviderRegistryTest {

    private ServerMock server;
    private Plugin mockPlugin;
    private Logger mockLogger;
    private ProviderRegistry registry;

    @BeforeEach
    void setUp() {
        // MockBukkit Server initialisieren
        server = MockBukkit.mock();
        mockPlugin = mock(Plugin.class);
        mockLogger = Logger.getLogger("TestLogger");

        registry = new ProviderRegistry(mockPlugin, mockLogger);
    }

    @AfterEach
    void tearDown() {
        // MockBukkit aufräumen
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("detectAndRegister() ohne Plugins sollte NoOp-Provider registrieren")
    void testDetectAndRegister_WithoutPlugins_RegistersNoOpProviders() {
        // Arrange - Keine Plugins geladen

        // Act
        registry.detectAndRegister();

        // Assert - Alle Provider sollten NoOp sein
        assertNotNull(registry.getPlotProvider(), "PlotProvider sollte nicht null sein");
        assertNotNull(registry.getTownProvider(), "TownProvider sollte nicht null sein");
        assertNotNull(registry.getEconomyProvider(), "EconomyProvider sollte nicht null sein");
        assertNotNull(registry.getNpcProvider(), "NPCProvider sollte nicht null sein");
        assertNotNull(registry.getItemProvider(), "ItemProvider sollte nicht null sein");
        assertNotNull(registry.getChatProvider(), "ChatProvider sollte nicht null sein");
        assertNotNull(registry.getNetworkProvider(), "NetworkProvider sollte nicht null sein");

        // Prüfe dass Provider NoOp sind (nicht verfügbar)
        assertFalse(registry.getPlotProvider().isAvailable(),
            "PlotProvider sollte NoOp sein");
        assertFalse(registry.getTownProvider().isAvailable(),
            "TownProvider sollte NoOp sein");
        assertFalse(registry.getEconomyProvider().isAvailable(),
            "EconomyProvider sollte NoOp sein");
        assertFalse(registry.getNpcProvider().isAvailable(),
            "NPCProvider sollte NoOp sein");
        assertFalse(registry.getItemProvider().isAvailable(),
            "ItemProvider sollte NoOp sein");
        assertFalse(registry.getChatProvider().isAvailable(),
            "ChatProvider sollte NoOp sein");
        assertFalse(registry.getNetworkProvider().isAvailable(),
            "NetworkProvider sollte NoOp sein");
    }

    @Test
    @DisplayName("detectAndRegister() mit Towny sollte TownyProvider registrieren")
    void testDetectAndRegister_WithTowny_RegistersTownyProviders() {
        // Arrange - Towny Plugin mocken
        Plugin townyPlugin = MockBukkit.createMockPlugin("Towny");
        server.getPluginManager().enablePlugin(townyPlugin);

        // Act
        registry.detectAndRegister();

        // Assert - PlotProvider und TownProvider sollten Towny sein
        PlotProvider plotProvider = registry.getPlotProvider();
        TownProvider townProvider = registry.getTownProvider();

        assertNotNull(plotProvider, "PlotProvider sollte nicht null sein");
        assertNotNull(townProvider, "TownProvider sollte nicht null sein");

        // Diese sollten Towny-Provider sein (isAvailable() gibt true zurück)
        assertTrue(plotProvider.isAvailable(),
            "PlotProvider sollte Towny sein und verfügbar");
        assertTrue(townProvider.isAvailable(),
            "TownProvider sollte Towny sein und verfügbar");

        // Typ-Prüfung
        assertTrue(plotProvider instanceof TownyPlotProvider,
            "PlotProvider sollte TownyPlotProvider sein");
        assertTrue(townProvider instanceof TownyTownProvider,
            "TownProvider sollte TownyTownProvider sein");
    }

    @Test
    @DisplayName("detectAndRegister() mit Citizens sollte CitizensNPCProvider registrieren")
    void testDetectAndRegister_WithCitizens_RegistersCitizensProvider() {
        // Arrange - Citizens Plugin mocken
        Plugin citizensPlugin = MockBukkit.createMockPlugin("Citizens");
        server.getPluginManager().enablePlugin(citizensPlugin);

        // Act
        registry.detectAndRegister();

        // Assert
        NPCProvider npcProvider = registry.getNpcProvider();

        assertNotNull(npcProvider, "NPCProvider sollte nicht null sein");
        assertTrue(npcProvider.isAvailable(),
            "NPCProvider sollte Citizens sein und verfügbar");
        assertTrue(npcProvider instanceof CitizensNPCProvider,
            "NPCProvider sollte CitizensNPCProvider sein");
    }

    @Test
    @DisplayName("detectAndRegister() sollte NoOp für fehlende Plugins verwenden")
    void testDetectAndRegister_MixedPlugins_UsesNoOpForMissing() {
        // Arrange - Nur Towny geladen
        Plugin townyPlugin = MockBukkit.createMockPlugin("Towny");
        server.getPluginManager().enablePlugin(townyPlugin);

        // Act
        registry.detectAndRegister();

        // Assert
        // Towny-Provider verfügbar
        assertTrue(registry.getPlotProvider().isAvailable(),
            "PlotProvider sollte verfügbar sein (Towny)");

        // Andere NoOp
        assertFalse(registry.getEconomyProvider().isAvailable(),
            "EconomyProvider sollte NoOp sein (kein Vault)");
        assertFalse(registry.getNpcProvider().isAvailable(),
            "NPCProvider sollte NoOp sein (kein Citizens)");
        assertFalse(registry.getItemProvider().isAvailable(),
            "ItemProvider sollte NoOp sein (kein MMOItems)");
    }

    @Test
    @DisplayName("Alle Getter sollten nicht-null Provider zurückgeben")
    void testGetters_ReturnNonNullProviders() {
        // Arrange
        registry.detectAndRegister();

        // Assert - Kein Getter sollte null zurückgeben
        assertNotNull(registry.getPlotProvider());
        assertNotNull(registry.getTownProvider());
        assertNotNull(registry.getEconomyProvider());
        assertNotNull(registry.getNpcProvider());
        assertNotNull(registry.getItemProvider());
        assertNotNull(registry.getChatProvider());
        assertNotNull(registry.getNetworkProvider());
    }

    @Test
    @DisplayName("detectAndRegister() sollte mehrfach aufrufbar sein")
    void testDetectAndRegister_CanBeCalledMultipleTimes() {
        // Act - Mehrfach aufrufen
        registry.detectAndRegister();
        PlotProvider firstProvider = registry.getPlotProvider();

        registry.detectAndRegister();
        PlotProvider secondProvider = registry.getPlotProvider();

        // Assert - Sollte ohne Fehler durchlaufen
        assertNotNull(firstProvider);
        assertNotNull(secondProvider);
    }

    @Test
    @DisplayName("Registry sollte mit verschiedenen Logger-Instanzen funktionieren")
    void testRegistry_WorksWithDifferentLoggers() {
        // Arrange
        Logger customLogger = Logger.getLogger("CustomTestLogger");
        ProviderRegistry customRegistry = new ProviderRegistry(mockPlugin, customLogger);

        // Act
        customRegistry.detectAndRegister();

        // Assert
        assertNotNull(customRegistry.getPlotProvider());
        assertNotNull(customRegistry.getEconomyProvider());
    }
}
