package de.fallenstar.plot.action;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.provider.StorageContainerPlot;
import de.fallenstar.core.registry.ProviderRegistry;
import de.fallenstar.core.ui.element.PlotAction;
import de.fallenstar.plot.PlotModule;
import de.fallenstar.plot.ui.PlotStorageUi;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Action zum Verwalten des Plot-Lagers.
 *
 * **Berechtigungen:**
 * - Nur Plot-Owner dürfen Lager verwalten
 * - Optional: Permission "fallenstar.plot.storage.manage"
 *
 * **Command Pattern:**
 * - Erweitert PlotAction (Plot-Referenz + canExecute)
 * - Hat PlotModule-Referenz für Storage-Zugriff
 * - Öffnet PlotStorageUi für Owner
 *
 * **GuiRenderable (Sprint 18):**
 * - Icon: Material.CHEST
 * - DisplayName: "§6Lager verwalten"
 * - Lore: Anzahl Items + Kapazität + Beschreibung
 *
 * **Funktionalität:**
 * - Zeigt alle Items im Lager mit Mengen
 * - Receiver-Kiste Status
 * - Storage-Scan Funktion (rescan)
 *
 * **Verwendung:**
 * ```java
 * // In TradeguildPlot.getAvailablePlotActions():
 * actions.add(new PlotActionManageStorage(this, providers, plotModule));
 * ```
 *
 * @author FallenStar
 * @version 1.0
 * @see PlotStorageUi
 * @see StorageContainerPlot
 */
public final class PlotActionManageStorage extends PlotAction {

    private final PlotModule plotModule;

    /**
     * Erstellt eine neue PlotActionManageStorage.
     *
     * @param plot Der Plot dessen Lager verwaltet werden soll
     * @param providers ProviderRegistry für Owner-Checks
     * @param plotModule PlotModule für Storage-Zugriff
     */
    public PlotActionManageStorage(Plot plot, ProviderRegistry providers, PlotModule plotModule) {
        super(plot, providers);
        this.plotModule = Objects.requireNonNull(plotModule, "PlotModule darf nicht null sein");
    }

    @Override
    protected boolean requiresOwnership() {
        return true; // Nur Owner dürfen Lager verwalten
    }

    @Override
    protected String requiredPermission() {
        return "fallenstar.plot.storage.manage";
    }

    @Override
    protected void executeAction(Player player) {
        player.closeInventory();

        // Prüfe ob Plot StorageContainerPlot ist
        if (!(plot instanceof StorageContainerPlot storagePlot)) {
            player.sendMessage("§cDieser Plot hat kein Lager-System.");
            return;
        }

        // Öffne PlotStorageUi
        PlotStorageUi storageUi = new PlotStorageUi(
            storagePlot,
            plotModule.getProviderRegistry(),
            plotModule
        );
        storageUi.open(player);
    }

    // ========== GuiRenderable Implementation ==========

    @Override
    protected Material getIcon() {
        return Material.CHEST;
    }

    @Override
    protected String getDisplayName() {
        return "§6Lager verwalten";
    }

    @Override
    protected List<String> getLore() {
        List<String> lore = new ArrayList<>();

        // Storage-Info anzeigen wenn StorageContainerPlot
        if (plot instanceof StorageContainerPlot storagePlot) {
            try {
                var storage = storagePlot.getStorage();
                int itemCount = storage.getAllItems().size();

                lore.add("§7Aktuelle Items: §e" + itemCount);
                lore.add("§7Receiver: " + (storagePlot.hasReceiver() ? "§a✓ Aktiv" : "§c✗ Nicht gesetzt"));
                lore.add("");
            } catch (Exception e) {
                // Fehler beim Laden - zeige Standard-Lore
            }
        }

        lore.add("§7Verwalte das Lager dieses Plots:");
        lore.add("§7• Items anzeigen mit Mengen");
        lore.add("§7• Receiver-Kiste Status");
        lore.add("§7• Storage neu scannen");
        lore.add("");
        lore.add("§6§l➤ Klicke zum Öffnen");

        return lore;
    }

    @Override
    public boolean isVisible(Player viewer) {
        // Nur anzeigen wenn Plot ein StorageContainerPlot ist
        return plot instanceof StorageContainerPlot;
    }
}
