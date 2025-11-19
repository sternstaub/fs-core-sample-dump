package de.fallenstar.plot.action;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.provider.StorageContainerPlot;
import de.fallenstar.core.registry.ProviderRegistry;
import de.fallenstar.core.ui.element.PlotAction;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Action zum Anzeigen der Preisliste eines Grundstücks.
 *
 * **Naming Convention:** PlotAction* Prefix für alphabetische Hierarchie-Erkennung
 *
 * **Command Pattern:**
 * - Erweitert PlotAction (Plot-Referenz + canExecute)
 * - Keine Owner-Requirement (jeder kann Preise sehen)
 * - Führt Command "/plot price list" aus
 *
 * **GuiRenderable:**
 * - Self-Rendering via getDisplayItem()
 * - Zeigt Anzahl gesetzter Preise in Lore
 *
 * **Verwendung:**
 * ```java
 * new PlotActionViewPrices(plot, providers)
 * ```
 *
 * @author FallenStar
 * @version 3.0 (Sprint 19 - Migration zu PlotAction)
 */
public final class PlotActionViewPrices extends PlotAction {

    /**
     * Konstruktor für PlotActionViewPrices.
     *
     * @param plot Der Plot dessen Preise angezeigt werden sollen
     * @param providers ProviderRegistry für Owner-Checks
     */
    public PlotActionViewPrices(Plot plot, ProviderRegistry providers) {
        super(plot, providers);
    }

    @Override
    protected boolean requiresOwnership() {
        return false; // Jeder darf Preise sehen
    }

    @Override
    protected void executeAction(Player player) {
        player.closeInventory();
        player.performCommand("plot price list");
    }

    // ========== GuiRenderable Implementation ==========

    @Override
    protected Material getIcon() {
        return Material.BOOK;
    }

    @Override
    protected String getDisplayName() {
        return "§ePreise anzeigen";
    }

    @Override
    protected List<String> getLore() {
        List<String> lore = new ArrayList<>();

        lore.add("§7Zeigt alle definierten Preise");
        lore.add("§7für dieses Grundstück an");

        // Wenn StorageContainerPlot, zeige Anzahl Preise
        if (plot instanceof StorageContainerPlot storagePlot) {
            Map<Material, BigDecimal> buyPrices = storagePlot.getAllBuyPrices();
            Map<Material, BigDecimal> sellPrices = storagePlot.getAllSellPrices();

            int totalPrices = buyPrices.size() + sellPrices.size();
            if (totalPrices > 0) {
                lore.add("");
                lore.add("§7Gesetzte Preise: §a" + totalPrices);
                lore.add("§7Ankauf: §e" + buyPrices.size());
                lore.add("§7Verkauf: §e" + sellPrices.size());
            }
        }

        lore.add("");
        lore.add("§7Klicke zum Anzeigen");

        return lore;
    }
}
