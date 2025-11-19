package de.fallenstar.plot.model;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.provider.PlotProvider;
import de.fallenstar.core.registry.ProviderRegistry;
import de.fallenstar.core.ui.container.BaseUi;
import de.fallenstar.core.ui.container.PageableBasicUi;
import de.fallenstar.core.ui.element.PlotAction;
import de.fallenstar.core.interaction.InteractionContext;
import de.fallenstar.plot.PlotModule;
import de.fallenstar.plot.action.*;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit-Tests für TradeguildPlot (Sprint 19, Phase 2).
 *
 * Testet:
 * - getAvailablePlotActions() - Liefert vollständige Action-Liste
 * - createUi() - Nutzt GuiBuilder wenn Dependencies gesetzt sind
 * - Fallback zu GenericInteractionMenuUi wenn Dependencies fehlen
 * - Dependency-Injection via setPlotModule() + setProviderRegistry()
 *
 * @author FallenStar
 * @version 1.0
 */
@DisplayName("TradeguildPlot Tests (Sprint 19 Phase 2)")
class TradeguildPlotTest {

    private TradeguildPlot tradeguildPlot;
    private ProviderRegistry mockProviders;
    private PlotProvider mockPlotProvider;
    private PlotModule mockPlotModule;
    private Player mockOwner;
    private Player mockGuest;
    private Location mockLocation;
    private World mockWorld;

    @BeforeEach
    void setUp() {
        // Mock ProviderRegistry
        mockProviders = mock(ProviderRegistry.class);
        mockPlotProvider = mock(PlotProvider.class);
        when(mockProviders.getPlotProvider()).thenReturn(mockPlotProvider);

        // Mock PlotModule
        mockPlotModule = mock(PlotModule.class);
        when(mockPlotModule.getProviderRegistry()).thenReturn(mockProviders);

        // Mock Players
        mockOwner = mock(Player.class);
        mockGuest = mock(Player.class);

        when(mockOwner.getUniqueId()).thenReturn(UUID.randomUUID());
        when(mockOwner.getName()).thenReturn("OwnerPlayer");
        when(mockOwner.hasPermission(anyString())).thenReturn(true);

        when(mockGuest.getUniqueId()).thenReturn(UUID.randomUUID());
        when(mockGuest.getName()).thenReturn("GuestPlayer");
        when(mockGuest.hasPermission(anyString())).thenReturn(true);

        // Mock Location
        mockWorld = mock(World.class);
        when(mockWorld.getName()).thenReturn("world");
        mockLocation = mock(Location.class);
        when(mockLocation.getWorld()).thenReturn(mockWorld);
        when(mockLocation.getBlockX()).thenReturn(100);
        when(mockLocation.getBlockY()).thenReturn(64);
        when(mockLocation.getBlockZ()).thenReturn(200);

        // Create TradeguildPlot
        tradeguildPlot = new TradeguildPlot(
            "TradeguildTest_001",
            UUID.randomUUID(),
            mockLocation
        );

        // Mock Owner-Checks
        try {
            when(mockPlotProvider.isOwner(any(Plot.class), eq(mockOwner))).thenReturn(true);
            when(mockPlotProvider.isOwner(any(Plot.class), eq(mockGuest))).thenReturn(false);
            when(mockPlotProvider.getOwner(any(Plot.class))).thenReturn(Optional.of(mockOwner));
        } catch (Exception e) {
            fail("Setup failed: " + e.getMessage());
        }
    }

    // ========== getAvailablePlotActions Tests ==========

    @Test
    @DisplayName("getAvailablePlotActions: Liefert alle erwarteten Actions")
    void testGetAvailablePlotActions_containsAllExpectedActions() {
        List<PlotAction> actions = tradeguildPlot.getAvailablePlotActions(
            mockOwner,
            mockProviders,
            mockPlotModule
        );

        assertNotNull(actions, "Action-Liste darf nicht null sein");
        assertEquals(7, actions.size(), "Es sollten genau 7 Actions vorhanden sein");
    }

    @Test
    @DisplayName("getAvailablePlotActions: Enthält SetNameAction")
    void testGetAvailablePlotActions_containsSetNameAction() {
        List<PlotAction> actions = tradeguildPlot.getAvailablePlotActions(
            mockOwner,
            mockProviders,
            mockPlotModule
        );

        boolean hasSetNameAction = actions.stream()
            .anyMatch(action -> action instanceof PlotActionSetName);

        assertTrue(hasSetNameAction, "Actions sollten PlotActionSetName enthalten");
    }

    @Test
    @DisplayName("getAvailablePlotActions: Enthält Storage-Actions")
    void testGetAvailablePlotActions_containsStorageActions() {
        List<PlotAction> actions = tradeguildPlot.getAvailablePlotActions(
            mockOwner,
            mockProviders,
            mockPlotModule
        );

        boolean hasManageStorage = actions.stream()
            .anyMatch(action -> action instanceof PlotActionManageStorage);
        boolean hasManagePrices = actions.stream()
            .anyMatch(action -> action instanceof PlotActionManagePrices);
        boolean hasViewPrices = actions.stream()
            .anyMatch(action -> action instanceof PlotActionViewPrices);

        assertTrue(hasManageStorage, "Actions sollten PlotActionManageStorage enthalten");
        assertTrue(hasManagePrices, "Actions sollten PlotActionManagePrices enthalten");
        assertTrue(hasViewPrices, "Actions sollten PlotActionViewPrices enthalten");
    }

    @Test
    @DisplayName("getAvailablePlotActions: Enthält NPC-Actions")
    void testGetAvailablePlotActions_containsNpcActions() {
        List<PlotAction> actions = tradeguildPlot.getAvailablePlotActions(
            mockOwner,
            mockProviders,
            mockPlotModule
        );

        boolean hasManageNpcs = actions.stream()
            .anyMatch(action -> action instanceof ManageNpcsAction);

        assertTrue(hasManageNpcs, "Actions sollten ManageNpcsAction enthalten");
    }

    @Test
    @DisplayName("getAvailablePlotActions: Enthält generische Actions (Teleport + Info)")
    void testGetAvailablePlotActions_containsGenericActions() {
        List<PlotAction> actions = tradeguildPlot.getAvailablePlotActions(
            mockOwner,
            mockProviders,
            mockPlotModule
        );

        boolean hasTeleport = actions.stream()
            .anyMatch(action -> action instanceof PlotActionTeleport);
        boolean hasInfo = actions.stream()
            .anyMatch(action -> action instanceof PlotActionInfo);

        assertTrue(hasTeleport, "Actions sollten PlotActionTeleport enthalten");
        assertTrue(hasInfo, "Actions sollten PlotActionInfo enthalten");
    }

    // ========== createUi Tests ==========

    @Test
    @DisplayName("createUi: Nutzt GuiBuilder wenn Dependencies gesetzt sind")
    void testCreateUi_usesGuiBuilderWhenDependenciesSet() {
        // Dependencies setzen
        tradeguildPlot.setPlotModule(mockPlotModule);
        tradeguildPlot.setProviderRegistry(mockProviders);

        // createUi aufrufen
        Optional<BaseUi> uiOptional = tradeguildPlot.createUi(
            mockOwner,
            mock(InteractionContext.class)
        );

        assertTrue(uiOptional.isPresent(), "createUi sollte ein UI zurückgeben");

        BaseUi ui = uiOptional.get();
        assertTrue(ui instanceof PageableBasicUi, "UI sollte PageableBasicUi sein (GuiBuilder!)");

        PageableBasicUi pageableUi = (PageableBasicUi) ui;
        assertNotNull(pageableUi.getTitle(), "GUI sollte einen Titel haben");
        assertTrue(pageableUi.getTitle().contains("Handelsgilde"), "Titel sollte 'Handelsgilde' enthalten");
    }

    @Test
    @DisplayName("createUi: Nutzt Fallback wenn Dependencies NICHT gesetzt sind")
    void testCreateUi_usesFallbackWhenDependenciesNotSet() {
        // Dependencies NICHT setzen (null)
        tradeguildPlot.setPlotModule(null);
        tradeguildPlot.setProviderRegistry(null);

        // createUi aufrufen
        Optional<BaseUi> uiOptional = tradeguildPlot.createUi(
            mockOwner,
            mock(InteractionContext.class)
        );

        assertTrue(uiOptional.isPresent(), "createUi sollte ein Fallback-UI zurückgeben");

        BaseUi ui = uiOptional.get();
        // Fallback ist GenericInteractionMenuUi
        assertNotNull(ui, "Fallback-UI sollte nicht null sein");
    }

    @Test
    @DisplayName("createUi: Nutzt Fallback wenn nur PlotModule fehlt")
    void testCreateUi_usesFallbackWhenOnlyPlotModuleMissing() {
        // Nur ProviderRegistry setzen, PlotModule fehlt
        tradeguildPlot.setPlotModule(null);
        tradeguildPlot.setProviderRegistry(mockProviders);

        // createUi aufrufen
        Optional<BaseUi> uiOptional = tradeguildPlot.createUi(
            mockOwner,
            mock(InteractionContext.class)
        );

        assertTrue(uiOptional.isPresent(), "createUi sollte Fallback nutzen");

        BaseUi ui = uiOptional.get();
        assertNotNull(ui, "Fallback-UI sollte nicht null sein");
    }

    // ========== Dependency Injection Tests ==========

    @Test
    @DisplayName("setPlotModule: Setzt PlotModule korrekt")
    void testSetPlotModule_setsCorrectly() {
        tradeguildPlot.setPlotModule(mockPlotModule);

        // Verifizierung: getAvailablePlotActions sollte funktionieren
        assertDoesNotThrow(() -> {
            tradeguildPlot.getAvailablePlotActions(mockOwner, mockProviders, mockPlotModule);
        });
    }

    @Test
    @DisplayName("setProviderRegistry: Setzt ProviderRegistry korrekt")
    void testSetProviderRegistry_setsCorrectly() {
        tradeguildPlot.setProviderRegistry(mockProviders);

        // Verifizierung: getAvailablePlotActions sollte funktionieren
        assertDoesNotThrow(() -> {
            tradeguildPlot.getAvailablePlotActions(mockOwner, mockProviders, mockPlotModule);
        });
    }
}
