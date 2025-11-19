package de.fallenstar.plot.action;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.provider.StorageContainerPlot;
import de.fallenstar.core.registry.ProviderRegistry;
import de.fallenstar.core.ui.element.PlotAction;
import de.fallenstar.plot.PlotModule;
import de.fallenstar.plot.ui.StoragePriceUi;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Action zum Verwalten der Lager-Preise (Ankauf/Verkauf).
 *
 * **Berechtigungen:**
 * - Nur Plot-Owner dürfen Preise verwalten
 * - Optional: Permission "fallenstar.plot.prices.manage"
 *
 * **Command Pattern:**
 * - Erweitert PlotAction (Plot-Referenz + canExecute)
 * - Hat PlotModule-Referenz für Preis-Zugriff
 * - Öffnet StoragePriceUi für Owner
 *
 * **GuiRenderable (Sprint 18):**
 * - Icon: Material.GOLD_INGOT
 * - DisplayName: "§ePreise verwalten"
 * - Lore: Anzahl konfigurierte Preise + Beschreibung
 *
 * **Funktionalität:**
 * - Setze Ankauf-Preise (NPC kauft von Spieler)
 * - Setze Verkauf-Preise (Spieler kauft von NPC)
 * - Preise pro Material konfigurierbar
 * - Integration mit ItemBasePriceProvider
 *
 * **Verwendung:**
 * ```java
 * // In TradeguildPlot.getAvailablePlotActions():
 * actions.add(new PlotActionManagePrices(this, providers, plotModule));
 * ```
 *
 * @author FallenStar
 * @version 1.0
 * @see StoragePriceUi
 * @see StorageContainerPlot
 */
public final class PlotActionManagePrices extends PlotAction {

    private final PlotModule plotModule;

    /**
     * Erstellt eine neue PlotActionManagePrices.
     *
     * @param plot Der Plot dessen Preise verwaltet werden sollen
     * @param providers ProviderRegistry für Owner-Checks
     * @param plotModule PlotModule für Preis-Zugriff
     */
    public PlotActionManagePrices(Plot plot, ProviderRegistry providers, PlotModule plotModule) {
        super(plot, providers);
        this.plotModule = Objects.requireNonNull(plotModule, "PlotModule darf nicht null sein");
    }

    @Override
    protected boolean requiresOwnership() {
        return true; // Nur Owner dürfen Preise setzen
    }

    @Override
    protected String requiredPermission() {
        return "fallenstar.plot.prices.manage";
    }

    @Override
    protected void executeAction(Player player) {
        player.closeInventory();

        // Prüfe ob Plot StorageContainerPlot ist
        if (!(plot instanceof StorageContainerPlot storagePlot)) {
            player.sendMessage("§cDieser Plot unterstützt kein Preis-System.");
            return;
        }

        // Öffne StoragePriceUi
        StoragePriceUi priceUi = new StoragePriceUi(
            storagePlot,
            plotModule.getProviderRegistry(),
            plotModule
        );
        priceUi.open(player);
    }

    // ========== GuiRenderable Implementation ==========

    @Override
    protected Material getIcon() {
        return Material.GOLD_INGOT;
    }

    @Override
    protected String getDisplayName() {
        return "§ePreise verwalten";
    }

    @Override
    protected List<String> getLore() {
        List<String> lore = new ArrayList<>();

        lore.add("§7Setze Ankauf- und Verkauf-Preise");
        lore.add("§7für Items in deinem Lager");
        lore.add("");
        lore.add("§7• §aAnkauf: §7NPC kauft von Spielern");
        lore.add("§7• §eVerkauf: §7Spieler kaufen vom NPC");
        lore.add("");
        lore.add("§7Preise werden pro Material");
        lore.add("§7individuell konfiguriert");
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
