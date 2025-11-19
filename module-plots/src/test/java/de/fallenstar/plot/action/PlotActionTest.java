package de.fallenstar.plot.action;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.provider.StorageContainerPlot;
import de.fallenstar.core.provider.PlotProvider;
import de.fallenstar.core.registry.ProviderRegistry;
import de.fallenstar.plot.PlotModule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit-Tests für neue PlotActions (Sprint 19, Phase 1).
 *
 * Testet:
 * - PlotActionManageStorage
 * - PlotActionManagePrices
 * - PlotActionViewPrices
 * - PlotActionTeleport
 * - PlotActionInfo
 *
 * Fokus:
 * - Owner vs. Guest Berechtigungen
 * - GuiRenderable DisplayItem Generierung
 * - Permission-Lore bei !canExecute()
 * - isVisible() Filterung
 *
 * @author FallenStar
 * @version 1.0
 */
@DisplayName("PlotAction Tests (Sprint 19 Phase 1)")
class PlotActionTest {

    private ProviderRegistry mockProviders;
    private PlotProvider mockPlotProvider;
    private PlotModule mockPlotModule;
    private Player mockOwner;
    private Player mockGuest;
    private Plot mockPlot;
    private StorageContainerPlot mockStoragePlot;
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

        // Mock Plot
        mockPlot = mock(Plot.class);
        when(mockPlot.getIdentifier()).thenReturn("TestPlot_001");
        when(mockPlot.getUuid()).thenReturn(UUID.randomUUID());

        // Mock StorageContainerPlot
        mockStoragePlot = mock(StorageContainerPlot.class);
        when(mockStoragePlot.getIdentifier()).thenReturn("TestStoragePlot_001");
        when(mockStoragePlot.getUuid()).thenReturn(UUID.randomUUID());

        // Mock Location
        mockWorld = mock(World.class);
        when(mockWorld.getName()).thenReturn("world");
        mockLocation = mock(Location.class);
        when(mockLocation.getWorld()).thenReturn(mockWorld);
        when(mockLocation.getBlockX()).thenReturn(100);
        when(mockLocation.getBlockY()).thenReturn(64);
        when(mockLocation.getBlockZ()).thenReturn(200);

        when(mockPlot.getLocation()).thenReturn(mockLocation);
        when(mockStoragePlot.getLocation()).thenReturn(mockLocation);

        // Mock Owner-Checks
        try {
            when(mockPlotProvider.isOwner(mockPlot, mockOwner)).thenReturn(true);
            when(mockPlotProvider.isOwner(mockPlot, mockGuest)).thenReturn(false);
            when(mockPlotProvider.isOwner(mockStoragePlot, mockOwner)).thenReturn(true);
            when(mockPlotProvider.isOwner(mockStoragePlot, mockGuest)).thenReturn(false);

            when(mockPlotProvider.getOwner(eq(mockPlot))).thenReturn(Optional.of(mockOwner));
            when(mockPlotProvider.getOwner(eq(mockStoragePlot))).thenReturn(Optional.of(mockOwner));
        } catch (Exception e) {
            fail("Setup failed: " + e.getMessage());
        }
    }

    // ========== PlotActionManageStorage Tests ==========

    @Test
    @DisplayName("ManageStorage: Owner kann Action ausführen")
    void testManageStorageAction_ownerCanExecute() {
        PlotActionManageStorage action = new PlotActionManageStorage(mockStoragePlot, mockProviders, mockPlotModule);

        boolean canExecute = action.canExecute(mockOwner);

        assertTrue(canExecute, "Owner sollte ManageStorage ausführen können");
    }

    @Test
    @DisplayName("ManageStorage: Guest kann Action NICHT ausführen")
    void testManageStorageAction_guestCannotExecute() {
        PlotActionManageStorage action = new PlotActionManageStorage(mockStoragePlot, mockProviders, mockPlotModule);

        boolean canExecute = action.canExecute(mockGuest);

        assertFalse(canExecute, "Guest sollte ManageStorage NICHT ausführen können");
    }

    @Test
    @DisplayName("ManageStorage: Nur sichtbar für StorageContainerPlot")
    void testManageStorageAction_onlyVisibleForStoragePlot() {
        PlotActionManageStorage actionStorage = new PlotActionManageStorage(mockStoragePlot, mockProviders, mockPlotModule);
        PlotActionManageStorage actionRegular = new PlotActionManageStorage(mockPlot, mockProviders, mockPlotModule);

        assertTrue(actionStorage.isVisible(mockOwner), "Should be visible for StorageContainerPlot");
        assertFalse(actionRegular.isVisible(mockOwner), "Should NOT be visible for regular Plot");
    }

    // ========== PlotActionManagePrices Tests ==========

    @Test
    @DisplayName("ManagePrices: Owner kann Action ausführen")
    void testManagePricesAction_ownerCanExecute() {
        PlotActionManagePrices action = new PlotActionManagePrices(mockStoragePlot, mockProviders, mockPlotModule);

        boolean canExecute = action.canExecute(mockOwner);

        assertTrue(canExecute, "Owner sollte ManagePrices ausführen können");
    }

    @Test
    @DisplayName("ManagePrices: Guest kann Action NICHT ausführen")
    void testManagePricesAction_guestCannotExecute() {
        PlotActionManagePrices action = new PlotActionManagePrices(mockStoragePlot, mockProviders, mockPlotModule);

        boolean canExecute = action.canExecute(mockGuest);

        assertFalse(canExecute, "Guest sollte ManagePrices NICHT ausführen können");
    }

    // ========== PlotActionViewPrices Tests ==========

    @Test
    @DisplayName("ViewPrices: Guest kann Action ausführen (READ-ONLY!)")
    void testViewPricesAction_guestCanExecute() {
        PlotActionViewPrices action = new PlotActionViewPrices(mockStoragePlot, mockProviders, mockPlotModule);

        boolean canExecute = action.canExecute(mockGuest);

        assertTrue(canExecute, "Guest sollte ViewPrices ausführen können (read-only!)");
    }

    @Test
    @DisplayName("ViewPrices: Owner kann Action auch ausführen")
    void testViewPricesAction_ownerCanExecute() {
        PlotActionViewPrices action = new PlotActionViewPrices(mockStoragePlot, mockProviders, mockPlotModule);

        boolean canExecute = action.canExecute(mockOwner);

        assertTrue(canExecute, "Owner sollte ViewPrices auch ausführen können");
    }

    // ========== PlotActionTeleport Tests ==========

    @Test
    @DisplayName("Teleport: Guest kann Action ausführen")
    void testTeleportAction_guestCanExecute() {
        PlotActionTeleport action = new PlotActionTeleport(mockPlot, mockProviders);

        boolean canExecute = action.canExecute(mockGuest);

        assertTrue(canExecute, "Guest sollte Teleport ausführen können");
    }

    @Test
    @DisplayName("Teleport: Nur sichtbar wenn Location valid")
    void testTeleportAction_onlyVisibleWithValidLocation() {
        PlotActionTeleport actionValid = new PlotActionTeleport(mockPlot, mockProviders);

        Plot plotInvalidLocation = mock(Plot.class);
        when(plotInvalidLocation.getLocation()).thenReturn(null);
        PlotActionTeleport actionInvalid = new PlotActionTeleport(plotInvalidLocation, mockProviders);

        assertTrue(actionValid.isVisible(mockOwner), "Should be visible with valid location");
        assertFalse(actionInvalid.isVisible(mockOwner), "Should NOT be visible with null location");
    }

    // ========== PlotActionInfo Tests ==========

    @Test
    @DisplayName("Info: Guest kann Action ausführen")
    void testInfoAction_guestCanExecute() {
        PlotActionInfo action = new PlotActionInfo(mockPlot, mockProviders);

        boolean canExecute = action.canExecute(mockGuest);

        assertTrue(canExecute, "Guest sollte Info ausführen können");
    }

    @Test
    @DisplayName("Info: Immer sichtbar")
    void testInfoAction_alwaysVisible() {
        PlotActionInfo action = new PlotActionInfo(mockPlot, mockProviders);

        assertTrue(action.isVisible(mockOwner), "Info sollte immer sichtbar sein");
        assertTrue(action.isVisible(mockGuest), "Info sollte auch für Gäste sichtbar sein");
    }

    // ========== GuiRenderable Tests ==========

    @Test
    @DisplayName("Alle Actions haben valide DisplayItems")
    void testAllActions_haveValidDisplayItem() {
        PlotActionManageStorage storage = new PlotActionManageStorage(mockStoragePlot, mockProviders, mockPlotModule);
        PlotActionManagePrices prices = new PlotActionManagePrices(mockStoragePlot, mockProviders, mockPlotModule);
        PlotActionViewPrices viewPrices = new PlotActionViewPrices(mockStoragePlot, mockProviders, mockPlotModule);
        PlotActionTeleport teleport = new PlotActionTeleport(mockPlot, mockProviders);
        PlotActionInfo info = new PlotActionInfo(mockPlot, mockProviders);

        assertValidDisplayItem(storage, mockOwner, "ManageStorage");
        assertValidDisplayItem(prices, mockOwner, "ManagePrices");
        assertValidDisplayItem(viewPrices, mockOwner, "ViewPrices");
        assertValidDisplayItem(teleport, mockOwner, "Teleport");
        assertValidDisplayItem(info, mockOwner, "Info");
    }

    @Test
    @DisplayName("Permission-Lore wird bei !canExecute() hinzugefügt")
    void testAllActions_permissionLoreAdded() {
        PlotActionManageStorage storage = new PlotActionManageStorage(mockStoragePlot, mockProviders, mockPlotModule);

        // Guest kann ManageStorage NICHT ausführen
        ItemStack displayItem = storage.getDisplayItem(mockGuest);

        assertNotNull(displayItem, "DisplayItem darf nicht null sein");
        assertNotNull(displayItem.getItemMeta(), "ItemMeta darf nicht null sein");

        var lore = displayItem.getItemMeta().getLore();
        assertNotNull(lore, "Lore darf nicht null sein");

        boolean hasPermissionLore = lore.stream()
            .anyMatch(line -> line.contains("Keine Berechtigung"));

        assertTrue(hasPermissionLore, "Lore sollte Permission-Hinweis enthalten bei !canExecute()");
    }

    // ========== Helper Methods ==========

    private void assertValidDisplayItem(PlotAction action, Player player, String actionName) {
        ItemStack item = action.getDisplayItem(player);

        assertNotNull(item, actionName + ": DisplayItem darf nicht null sein");
        assertNotEquals(Material.AIR, item.getType(), actionName + ": DisplayItem darf nicht AIR sein");

        assertNotNull(item.getItemMeta(), actionName + ": ItemMeta darf nicht null sein");
        assertNotNull(item.getItemMeta().getDisplayName(), actionName + ": DisplayName darf nicht null sein");
        assertFalse(item.getItemMeta().getDisplayName().isEmpty(), actionName + ": DisplayName darf nicht leer sein");
    }
}
