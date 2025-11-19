package de.fallenstar.plot.action;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.provider.StorageContainerPlot;
import de.fallenstar.core.registry.ProviderRegistry;
import de.fallenstar.core.ui.element.PlotAction;
import de.fallenstar.plot.storage.manager.StorageManager;
import de.fallenstar.plot.storage.model.PlotStorage;
import de.fallenstar.plot.storage.provider.PlotStorageProvider;
import de.fallenstar.plot.ui.PlotStorageUi;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Action zum Verwalten des Plot-Storage.
 *
 * **Naming Convention:** PlotAction* Prefix für alphabetische Hierarchie-Erkennung
 *
 * **Command Pattern:**
 * - Erweitert PlotAction (Plot-Referenz + canExecute)
 * - Keine Owner-Requirement (jeder darf Storage sehen)
 * - Owner-spezifische Features (Scan) via PlotStorageUi
 *
 * **Features:**
 * - Material-Liste mit Mengen
 * - Receiver-Kiste Status
 * - Storage-Scan Funktion (Owner only)
 * - Pagination
 *
 * **GuiRenderable:**
 * - Self-Rendering via getDisplayItem()
 * - Zeigt Anzahl gespeicherter Items in Lore
 *
 * **Verwendung:**
 * ```java
 * new PlotActionManageStorage(plot, providers, storageProvider, storageManager)
 * ```
 *
 * @author FallenStar
 * @version 3.0 (Sprint 19 - Migration zu PlotAction)
 */
public final class PlotActionManageStorage extends PlotAction {

    private final PlotStorageProvider storageProvider;
    private final StorageManager storageManager;

    /**
     * Konstruktor für PlotActionManageStorage.
     *
     * @param plot Der Plot
     * @param providers ProviderRegistry für Owner-Checks
     * @param storageProvider PlotStorageProvider
     * @param storageManager StorageManager
     */
    public PlotActionManageStorage(
            Plot plot,
            ProviderRegistry providers,
            PlotStorageProvider storageProvider,
            StorageManager storageManager
    ) {
        super(plot, providers);
        this.storageProvider = Objects.requireNonNull(storageProvider, "StorageProvider darf nicht null sein");
        this.storageManager = Objects.requireNonNull(storageManager, "StorageManager darf nicht null sein");
    }

    @Override
    protected boolean requiresOwnership() {
        return false; // Jeder darf Storage sehen (Owner hat zusätzliche Features in UI)
    }

    @Override
    protected void executeAction(Player player) {
        // Hole PlotStorage für aktuellen Plot
        PlotStorage plotStorage = storageProvider.getPlotStorage(plot);

        if (plotStorage == null) {
            player.sendMessage("§cStorage-System nicht verfügbar für dieses Grundstück!");
            return;
        }

        // Öffne PlotStorageUi (Owner-Check automatisch via PlotAction.isOwner()!)
        PlotStorageUi storageUI = new PlotStorageUi(
                plot,
                plotStorage,
                storageProvider,
                storageManager,
                isOwner(player) // PlotAction-Methode statt isOwner-Flag!
        );
        storageUI.open(player);
    }

    // ========== GuiRenderable Implementation ==========

    @Override
    protected Material getIcon() {
        return Material.CHEST;
    }

    @Override
    protected String getDisplayName() {
        return "§6Storage verwalten";
    }

    @Override
    protected List<String> getLore() {
        List<String> lore = new ArrayList<>();

        lore.add("§7Zeigt alle gespeicherten");
        lore.add("§7Materialien auf diesem Plot");

        // Wenn StorageContainerPlot, zeige Anzahl Items
        if (plot instanceof StorageContainerPlot storagePlot) {
            Map<Material, Integer> storage = storagePlot.getStorageContents();

            if (!storage.isEmpty()) {
                int totalItems = storage.values().stream().mapToInt(Integer::intValue).sum();
                int uniqueMaterials = storage.size();

                lore.add("");
                lore.add("§7Gespeichert: §a" + totalItems + " Items");
                lore.add("§7Materialien: §e" + uniqueMaterials);
            }
        }

        lore.add("");
        lore.add("§7Klicke zum Öffnen");

        return lore;
    }
}
