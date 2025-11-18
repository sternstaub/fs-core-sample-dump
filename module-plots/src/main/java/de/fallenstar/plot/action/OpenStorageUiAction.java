package de.fallenstar.plot.action;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.ui.element.UiAction;
import de.fallenstar.plot.storage.manager.StorageManager;
import de.fallenstar.plot.storage.model.PlotStorage;
import de.fallenstar.plot.storage.provider.PlotStorageProvider;
import de.fallenstar.plot.ui.PlotStorageUi;
import org.bukkit.entity.Player;

import java.util.Objects;

/**
 * Action zum Öffnen des Plot-Storage-UI.
 *
 * Öffnet die PlotStorageUi, die alle gespeicherten
 * Materialien auf dem Grundstück anzeigt.
 *
 * **Features:**
 * - Material-Liste mit Mengen
 * - Receiver-Kiste Status
 * - Storage-Scan Funktion (Owner)
 * - Pagination
 *
 * **Verwendung:**
 * ```java
 * addFunctionButton(
 *     Material.CHEST,
 *     "§6§lStorage anzeigen",
 *     List.of("§7Zeigt alle Materialien"),
 *     new OpenStorageUiAction(plot, storageProvider, storageManager, isOwner)
 * );
 * ```
 *
 * @author FallenStar
 * @version 2.0
 */
public final class OpenStorageUiAction implements UiAction {

    private final Plot plot;
    private final PlotStorageProvider storageProvider;
    private final StorageManager storageManager;
    private final boolean isOwner;

    /**
     * Konstruktor für OpenStorageUiAction.
     *
     * @param plot Der Plot
     * @param storageProvider PlotStorageProvider
     * @param storageManager StorageManager
     * @param isOwner Ob Spieler Owner ist
     */
    public OpenStorageUiAction(
            Plot plot,
            PlotStorageProvider storageProvider,
            StorageManager storageManager,
            boolean isOwner
    ) {
        this.plot = Objects.requireNonNull(plot, "Plot darf nicht null sein");
        this.storageProvider = Objects.requireNonNull(storageProvider, "StorageProvider darf nicht null sein");
        this.storageManager = Objects.requireNonNull(storageManager, "StorageManager darf nicht null sein");
        this.isOwner = isOwner;
    }

    @Override
    public void execute(Player player) {
        // Hole PlotStorage für aktuellen Plot
        PlotStorage plotStorage = storageProvider.getPlotStorage(plot);

        if (plotStorage == null) {
            player.sendMessage("§cStorage-System nicht verfügbar für dieses Grundstück!");
            return;
        }

        // Öffne PlotStorageUi
        PlotStorageUi storageUI = new PlotStorageUi(
                plot,
                plotStorage,
                storageProvider,
                storageManager,
                isOwner
        );
        storageUI.open(player);
    }

    @Override
    public String getActionName() {
        return "OpenStorageUI[" + plot.getIdentifier() + "]";
    }

    /**
     * Gibt den Plot zurück.
     *
     * @return Der Plot
     */
    public Plot getPlot() {
        return plot;
    }
}
