package de.fallenstar.plot.action;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.registry.ProviderRegistry;
import org.bukkit.Material;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mockito;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit Tests für PlotActions (Sprint 19 - Phase 1).
 *
 * **Testet:**
 * - GuiRenderable Implementation (getIcon, getDisplayName, getLore)
 * - Berechtigungen (requiresOwnership, requiredPermission)
 * - Naming Convention (PlotAction* Prefix)
 * - Self-Rendering Funktionalität
 *
 * @author FallenStar
 * @version 1.0
 */
class PlotActionTest {

    private Plot mockPlot;
    private ProviderRegistry mockProviders;

    @BeforeEach
    void setUp() {
        // Mock Plot
        mockPlot = Mockito.mock(Plot.class);
        when(mockPlot.getUuid()).thenReturn(UUID.randomUUID());
        when(mockPlot.getIdentifier()).thenReturn("test-plot");
        when(mockPlot.getDisplayName()).thenReturn("§aTest Plot");

        // Mock ProviderRegistry
        mockProviders = Mockito.mock(ProviderRegistry.class);
    }

    // ========== PlotActionSetName Tests ==========

    @Test
    @DisplayName("PlotActionSetName: Icon sollte NAME_TAG sein")
    void testPlotActionSetName_Icon() {
        PlotActionSetName action = new PlotActionSetName(mockPlot, mockProviders);
        assertEquals(Material.NAME_TAG, action.getIcon());
    }

    @Test
    @DisplayName("PlotActionSetName: DisplayName sollte gesetzt sein")
    void testPlotActionSetName_DisplayName() {
        PlotActionSetName action = new PlotActionSetName(mockPlot, mockProviders);
        String displayName = action.getDisplayName();

        assertNotNull(displayName);
        assertTrue(displayName.contains("Name"));
    }

    @Test
    @DisplayName("PlotActionSetName: Lore sollte nicht leer sein")
    void testPlotActionSetName_Lore() {
        PlotActionSetName action = new PlotActionSetName(mockPlot, mockProviders);
        List<String> lore = action.getLore();

        assertNotNull(lore);
        assertFalse(lore.isEmpty());
    }

    @Test
    @DisplayName("PlotActionSetName: Benötigt Owner-Berechtigung")
    void testPlotActionSetName_RequiresOwnership() {
        PlotActionSetName action = new PlotActionSetName(mockPlot, mockProviders);
        assertTrue(action.requiresOwnership());
    }

    @Test
    @DisplayName("PlotActionSetName: Benötigt Permission")
    void testPlotActionSetName_RequiresPermission() {
        PlotActionSetName action = new PlotActionSetName(mockPlot, mockProviders);
        assertEquals("fallenstar.plot.name.set", action.requiredPermission());
    }

    // ========== PlotActionManageNpcs Tests ==========

    @Test
    @DisplayName("PlotActionManageNpcs: Icon sollte VILLAGER_SPAWN_EGG sein")
    void testPlotActionManageNpcs_Icon() {
        de.fallenstar.plot.PlotModule mockModule = mock(de.fallenstar.plot.PlotModule.class);
        PlotActionManageNpcs action = new PlotActionManageNpcs(mockPlot, mockProviders, mockModule);
        assertEquals(Material.VILLAGER_SPAWN_EGG, action.getIcon());
    }

    @Test
    @DisplayName("PlotActionManageNpcs: Benötigt KEINE Owner-Berechtigung")
    void testPlotActionManageNpcs_NoOwnershipRequired() {
        de.fallenstar.plot.PlotModule mockModule = mock(de.fallenstar.plot.PlotModule.class);
        PlotActionManageNpcs action = new PlotActionManageNpcs(mockPlot, mockProviders, mockModule);
        assertFalse(action.requiresOwnership());
    }

    // ========== PlotActionViewPrices Tests ==========

    @Test
    @DisplayName("PlotActionViewPrices: Icon sollte BOOK sein")
    void testPlotActionViewPrices_Icon() {
        PlotActionViewPrices action = new PlotActionViewPrices(mockPlot, mockProviders);
        assertEquals(Material.BOOK, action.getIcon());
    }

    @Test
    @DisplayName("PlotActionViewPrices: Benötigt KEINE Owner-Berechtigung")
    void testPlotActionViewPrices_NoOwnershipRequired() {
        PlotActionViewPrices action = new PlotActionViewPrices(mockPlot, mockProviders);
        assertFalse(action.requiresOwnership());
    }

    // ========== PlotActionManagePrices Tests ==========

    @Test
    @DisplayName("PlotActionManagePrices: Icon sollte WRITABLE_BOOK sein")
    void testPlotActionManagePrices_Icon() {
        PlotActionManagePrices action = new PlotActionManagePrices(mockPlot, mockProviders);
        assertEquals(Material.WRITABLE_BOOK, action.getIcon());
    }

    @Test
    @DisplayName("PlotActionManagePrices: Benötigt Owner-Berechtigung")
    void testPlotActionManagePrices_RequiresOwnership() {
        PlotActionManagePrices action = new PlotActionManagePrices(mockPlot, mockProviders);
        assertTrue(action.requiresOwnership());
    }

    @Test
    @DisplayName("PlotActionManagePrices: Benötigt Permission")
    void testPlotActionManagePrices_RequiresPermission() {
        PlotActionManagePrices action = new PlotActionManagePrices(mockPlot, mockProviders);
        assertEquals("fallenstar.plot.price.set", action.requiredPermission());
    }

    // ========== PlotActionManageStorage Tests ==========

    @Test
    @DisplayName("PlotActionManageStorage: Icon sollte CHEST sein")
    void testPlotActionManageStorage_Icon() {
        de.fallenstar.plot.storage.provider.PlotStorageProvider mockStorageProvider =
                mock(de.fallenstar.plot.storage.provider.PlotStorageProvider.class);
        de.fallenstar.plot.storage.manager.StorageManager mockStorageManager =
                mock(de.fallenstar.plot.storage.manager.StorageManager.class);

        PlotActionManageStorage action = new PlotActionManageStorage(
                mockPlot, mockProviders, mockStorageProvider, mockStorageManager
        );
        assertEquals(Material.CHEST, action.getIcon());
    }

    // ========== PlotActionInfo Tests ==========

    @Test
    @DisplayName("PlotActionInfo: Icon sollte MAP sein")
    void testPlotActionInfo_Icon() {
        PlotActionInfo action = new PlotActionInfo(mockPlot, mockProviders);
        assertEquals(Material.MAP, action.getIcon());
    }

    @Test
    @DisplayName("PlotActionInfo: Benötigt KEINE Owner-Berechtigung")
    void testPlotActionInfo_NoOwnershipRequired() {
        PlotActionInfo action = new PlotActionInfo(mockPlot, mockProviders);
        assertFalse(action.requiresOwnership());
    }

    // ========== PlotActionTeleport Tests ==========

    @Test
    @DisplayName("PlotActionTeleport: Icon sollte ENDER_PEARL sein")
    void testPlotActionTeleport_Icon() {
        PlotActionTeleport action = new PlotActionTeleport(mockPlot, mockProviders);
        assertEquals(Material.ENDER_PEARL, action.getIcon());
    }

    @Test
    @DisplayName("PlotActionTeleport: Benötigt Owner-Berechtigung")
    void testPlotActionTeleport_RequiresOwnership() {
        PlotActionTeleport action = new PlotActionTeleport(mockPlot, mockProviders);
        assertTrue(action.requiresOwnership());
    }

    @Test
    @DisplayName("PlotActionTeleport: Benötigt Permission")
    void testPlotActionTeleport_RequiresPermission() {
        PlotActionTeleport action = new PlotActionTeleport(mockPlot, mockProviders);
        assertEquals("fallenstar.plot.teleport", action.requiredPermission());
    }

    // ========== Naming Convention Tests ==========

    @Test
    @DisplayName("Alle PlotActions folgen PlotAction* Naming Convention")
    void testNamingConvention() {
        // Prüfe dass alle Klassen mit PlotAction beginnen
        assertTrue(PlotActionSetName.class.getSimpleName().startsWith("PlotAction"));
        assertTrue(PlotActionManageNpcs.class.getSimpleName().startsWith("PlotAction"));
        assertTrue(PlotActionViewPrices.class.getSimpleName().startsWith("PlotAction"));
        assertTrue(PlotActionManagePrices.class.getSimpleName().startsWith("PlotAction"));
        assertTrue(PlotActionManageStorage.class.getSimpleName().startsWith("PlotAction"));
        assertTrue(PlotActionInfo.class.getSimpleName().startsWith("PlotAction"));
        assertTrue(PlotActionTeleport.class.getSimpleName().startsWith("PlotAction"));
    }

    // ========== Null-Check Tests ==========

    @Test
    @DisplayName("PlotActions werfen NullPointerException bei null Plot")
    void testNullPlot() {
        assertThrows(NullPointerException.class, () ->
                new PlotActionSetName(null, mockProviders)
        );
    }

    @Test
    @DisplayName("PlotActions werfen NullPointerException bei null ProviderRegistry")
    void testNullProviders() {
        assertThrows(NullPointerException.class, () ->
                new PlotActionSetName(mockPlot, null)
        );
    }
}
