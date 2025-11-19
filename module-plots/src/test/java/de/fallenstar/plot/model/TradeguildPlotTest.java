package de.fallenstar.plot.model;

import de.fallenstar.core.registry.ProviderRegistry;
import de.fallenstar.core.ui.element.PlotAction;
import de.fallenstar.plot.PlotModule;
import de.fallenstar.plot.action.*;
import de.fallenstar.plot.storage.manager.StorageManager;
import de.fallenstar.plot.storage.provider.PlotStorageProvider;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mockito;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit Tests für TradeguildPlot.getAvailablePlotActions() (Sprint 19 - Phase 2).
 *
 * **Testet:**
 * - getAvailablePlotActions() liefert korrekte Actions
 * - Dependency Injection funktioniert
 * - Null-Safety bei fehlenden Dependencies
 * - Action-Typen sind korrekt
 *
 * @author FallenStar
 * @version 1.0
 */
class TradeguildPlotTest {

    private TradeguildPlot plot;
    private Player mockPlayer;
    private ProviderRegistry mockProviderRegistry;
    private PlotModule mockPlotModule;
    private StorageManager mockStorageManager;
    private PlotStorageProvider mockStorageProvider;

    @BeforeEach
    void setUp() {
        // Mock Player
        mockPlayer = Mockito.mock(Player.class);

        // Mock Dependencies
        mockProviderRegistry = Mockito.mock(ProviderRegistry.class);
        mockPlotModule = Mockito.mock(PlotModule.class);
        mockStorageManager = Mockito.mock(StorageManager.class);
        mockStorageProvider = Mockito.mock(PlotStorageProvider.class);

        // Erstelle TradeguildPlot
        Location mockLocation = Mockito.mock(Location.class);
        plot = new TradeguildPlot(
                UUID.randomUUID(),
                "test-plot",
                mockLocation,
                null // nativePlot
        );
    }

    // ========== Dependency Injection Tests ==========

    @Test
    @DisplayName("getAvailablePlotActions: Ohne Dependencies gibt leere Liste zurück")
    void testGetAvailablePlotActions_NoDependencies() {
        // KEINE Dependencies injiziert

        List<PlotAction> actions = plot.getAvailablePlotActions(mockPlayer);

        assertNotNull(actions);
        assertTrue(actions.isEmpty(), "Sollte leere Liste zurückgeben wenn providerRegistry null ist");
    }

    @Test
    @DisplayName("getAvailablePlotActions: Mit ProviderRegistry gibt Actions zurück")
    void testGetAvailablePlotActions_WithProviderRegistry() {
        // Injiziere nur ProviderRegistry
        plot.setProviderRegistry(mockProviderRegistry);

        List<PlotAction> actions = plot.getAvailablePlotActions(mockPlayer);

        assertNotNull(actions);
        assertFalse(actions.isEmpty(), "Sollte mindestens einige Actions haben");

        // Sollte mindestens diese Actions haben:
        // - PlotActionSetName
        // - PlotActionViewPrices
        // - PlotActionManagePrices
        // - PlotActionInfo
        // - PlotActionTeleport
        assertTrue(actions.size() >= 5, "Sollte mindestens 5 Actions haben ohne optionale Dependencies");
    }

    @Test
    @DisplayName("getAvailablePlotActions: Mit allen Dependencies gibt alle Actions zurück")
    void testGetAvailablePlotActions_AllDependencies() {
        // Injiziere ALLE Dependencies
        plot.setProviderRegistry(mockProviderRegistry);
        plot.setPlotModule(mockPlotModule);
        plot.setStorageManager(mockStorageManager);
        plot.setPlotStorageProvider(mockStorageProvider);

        List<PlotAction> actions = plot.getAvailablePlotActions(mockPlayer);

        assertNotNull(actions);

        // Sollte ALLE Actions haben:
        // 1. PlotActionSetName
        // 2. PlotActionManageStorage (nur wenn StorageProvider + Manager injiziert)
        // 3. PlotActionViewPrices
        // 4. PlotActionManagePrices
        // 5. PlotActionManageNpcs (nur wenn PlotModule injiziert)
        // 6. PlotActionInfo
        // 7. PlotActionTeleport
        assertEquals(7, actions.size(), "Sollte genau 7 Actions haben mit allen Dependencies");
    }

    // ========== Action-Type Tests ==========

    @Test
    @DisplayName("getAvailablePlotActions: Enthält PlotActionSetName")
    void testGetAvailablePlotActions_ContainsSetName() {
        plot.setProviderRegistry(mockProviderRegistry);

        List<PlotAction> actions = plot.getAvailablePlotActions(mockPlayer);

        boolean hasSetNameAction = actions.stream()
                .anyMatch(a -> a instanceof PlotActionSetName);

        assertTrue(hasSetNameAction, "Sollte PlotActionSetName enthalten");
    }

    @Test
    @DisplayName("getAvailablePlotActions: Enthält PlotActionViewPrices")
    void testGetAvailablePlotActions_ContainsViewPrices() {
        plot.setProviderRegistry(mockProviderRegistry);

        List<PlotAction> actions = plot.getAvailablePlotActions(mockPlayer);

        boolean hasViewPricesAction = actions.stream()
                .anyMatch(a -> a instanceof PlotActionViewPrices);

        assertTrue(hasViewPricesAction, "Sollte PlotActionViewPrices enthalten");
    }

    @Test
    @DisplayName("getAvailablePlotActions: Enthält PlotActionManagePrices")
    void testGetAvailablePlotActions_ContainsManagePrices() {
        plot.setProviderRegistry(mockProviderRegistry);

        List<PlotAction> actions = plot.getAvailablePlotActions(mockPlayer);

        boolean hasManagePricesAction = actions.stream()
                .anyMatch(a -> a instanceof PlotActionManagePrices);

        assertTrue(hasManagePricesAction, "Sollte PlotActionManagePrices enthalten");
    }

    @Test
    @DisplayName("getAvailablePlotActions: Enthält PlotActionInfo")
    void testGetAvailablePlotActions_ContainsInfo() {
        plot.setProviderRegistry(mockProviderRegistry);

        List<PlotAction> actions = plot.getAvailablePlotActions(mockPlayer);

        boolean hasInfoAction = actions.stream()
                .anyMatch(a -> a instanceof PlotActionInfo);

        assertTrue(hasInfoAction, "Sollte PlotActionInfo enthalten");
    }

    @Test
    @DisplayName("getAvailablePlotActions: Enthält PlotActionTeleport")
    void testGetAvailablePlotActions_ContainsTeleport() {
        plot.setProviderRegistry(mockProviderRegistry);

        List<PlotAction> actions = plot.getAvailablePlotActions(mockPlayer);

        boolean hasTeleportAction = actions.stream()
                .anyMatch(a -> a instanceof PlotActionTeleport);

        assertTrue(hasTeleportAction, "Sollte PlotActionTeleport enthalten");
    }

    @Test
    @DisplayName("getAvailablePlotActions: Enthält PlotActionManageStorage nur mit Dependencies")
    void testGetAvailablePlotActions_ManageStorage_RequiresDependencies() {
        // OHNE StorageProvider und StorageManager
        plot.setProviderRegistry(mockProviderRegistry);

        List<PlotAction> actions = plot.getAvailablePlotActions(mockPlayer);

        boolean hasManageStorageAction = actions.stream()
                .anyMatch(a -> a instanceof PlotActionManageStorage);

        assertFalse(hasManageStorageAction, "Sollte PlotActionManageStorage NICHT enthalten ohne Dependencies");

        // MIT StorageProvider und StorageManager
        plot.setStorageManager(mockStorageManager);
        plot.setPlotStorageProvider(mockStorageProvider);

        actions = plot.getAvailablePlotActions(mockPlayer);

        hasManageStorageAction = actions.stream()
                .anyMatch(a -> a instanceof PlotActionManageStorage);

        assertTrue(hasManageStorageAction, "Sollte PlotActionManageStorage enthalten mit Dependencies");
    }

    @Test
    @DisplayName("getAvailablePlotActions: Enthält PlotActionManageNpcs nur mit PlotModule")
    void testGetAvailablePlotActions_ManageNpcs_RequiresPlotModule() {
        // OHNE PlotModule
        plot.setProviderRegistry(mockProviderRegistry);

        List<PlotAction> actions = plot.getAvailablePlotActions(mockPlayer);

        boolean hasManageNpcsAction = actions.stream()
                .anyMatch(a -> a instanceof PlotActionManageNpcs);

        assertFalse(hasManageNpcsAction, "Sollte PlotActionManageNpcs NICHT enthalten ohne PlotModule");

        // MIT PlotModule
        plot.setPlotModule(mockPlotModule);

        actions = plot.getAvailablePlotActions(mockPlayer);

        hasManageNpcsAction = actions.stream()
                .anyMatch(a -> a instanceof PlotActionManageNpcs);

        assertTrue(hasManageNpcsAction, "Sollte PlotActionManageNpcs enthalten mit PlotModule");
    }

    // ========== Null-Safety Tests ==========

    @Test
    @DisplayName("setProviderRegistry: Akzeptiert nicht-null Wert")
    void testSetProviderRegistry_NonNull() {
        assertDoesNotThrow(() -> plot.setProviderRegistry(mockProviderRegistry));
    }

    @Test
    @DisplayName("setPlotModule: Akzeptiert nicht-null Wert")
    void testSetPlotModule_NonNull() {
        assertDoesNotThrow(() -> plot.setPlotModule(mockPlotModule));
    }

    @Test
    @DisplayName("setStorageManager: Akzeptiert nicht-null Wert")
    void testSetStorageManager_NonNull() {
        assertDoesNotThrow(() -> plot.setStorageManager(mockStorageManager));
    }

    @Test
    @DisplayName("setPlotStorageProvider: Akzeptiert nicht-null Wert")
    void testSetPlotStorageProvider_NonNull() {
        assertDoesNotThrow(() -> plot.setPlotStorageProvider(mockStorageProvider));
    }

    // ========== Integration Test ==========

    @Test
    @DisplayName("Integration: Alle Actions sind PlotAction-Instanzen")
    void testGetAvailablePlotActions_AllActionsArePlotActions() {
        plot.setProviderRegistry(mockProviderRegistry);
        plot.setPlotModule(mockPlotModule);
        plot.setStorageManager(mockStorageManager);
        plot.setPlotStorageProvider(mockStorageProvider);

        List<PlotAction> actions = plot.getAvailablePlotActions(mockPlayer);

        for (PlotAction action : actions) {
            assertNotNull(action, "Action sollte nicht null sein");
            assertInstanceOf(PlotAction.class, action, "Action sollte PlotAction-Instanz sein");
        }
    }

    @Test
    @DisplayName("Integration: Alle Actions haben Plot-Referenz")
    void testGetAvailablePlotActions_AllActionsHavePlotReference() {
        plot.setProviderRegistry(mockProviderRegistry);
        plot.setPlotModule(mockPlotModule);
        plot.setStorageManager(mockStorageManager);
        plot.setPlotStorageProvider(mockStorageProvider);

        List<PlotAction> actions = plot.getAvailablePlotActions(mockPlayer);

        for (PlotAction action : actions) {
            assertNotNull(action.getPlot(), "Action sollte Plot-Referenz haben");
            assertEquals(plot, action.getPlot(), "Action sollte auf den gleichen Plot referenzieren");
        }
    }
}
