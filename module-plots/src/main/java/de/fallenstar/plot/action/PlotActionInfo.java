package de.fallenstar.plot.action;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.registry.ProviderRegistry;
import de.fallenstar.core.ui.element.PlotAction;
import de.fallenstar.plot.ui.PlotInfoUi;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Action zum Anzeigen der Plot-Informationen.
 *
 * **Berechtigungen:**
 * - KEINE Owner-Requirement - Gäste dürfen Plot-Infos sehen!
 * - Optional: Permission "fallenstar.plot.info"
 *
 * **Command Pattern:**
 * - Erweitert PlotAction (Plot-Referenz + canExecute)
 * - Zeigt Plot-Details für alle Spieler
 * - Öffnet PlotInfoUi
 *
 * **GuiRenderable (Sprint 18):**
 * - Icon: Material.BOOK
 * - DisplayName: "§bPlot-Informationen"
 * - Lore: Owner, Typ, Identifier
 *
 * **Funktionalität:**
 * - Zeigt Owner des Plots
 * - Zeigt Plot-Typ (Handelsgilde, Markt, etc.)
 * - Zeigt Plot-Größe
 * - Zeigt Identifier + UUID
 * - Für Owner + Gäste verfügbar
 *
 * **Verwendung:**
 * ```java
 * // In TradeguildPlot.getAvailablePlotActions():
 * actions.add(new PlotActionInfo(this, providers));
 * ```
 *
 * @author FallenStar
 * @version 1.0
 * @see PlotInfoUi
 */
public final class PlotActionInfo extends PlotAction {

    /**
     * Erstellt eine neue PlotActionInfo.
     *
     * @param plot Der Plot dessen Infos angezeigt werden sollen
     * @param providers ProviderRegistry für Owner-Ermittlung
     */
    public PlotActionInfo(Plot plot, ProviderRegistry providers) {
        super(plot, providers);
    }

    @Override
    protected boolean requiresOwnership() {
        return false; // Gäste dürfen Plot-Infos sehen!
    }

    @Override
    protected String requiredPermission() {
        return "fallenstar.plot.info";
    }

    @Override
    protected void executeAction(Player player) {
        player.closeInventory();

        // Öffne PlotInfoUi
        PlotInfoUi infoUi = new PlotInfoUi(plot, providers);
        infoUi.open(player);
    }

    // ========== GuiRenderable Implementation ==========

    @Override
    protected Material getIcon() {
        return Material.BOOK;
    }

    @Override
    protected String getDisplayName() {
        return "§bPlot-Informationen";
    }

    @Override
    protected List<String> getLore() {
        List<String> lore = new ArrayList<>();

        lore.add("§7Zeige Details zu diesem Plot:");
        lore.add("");
        lore.add("§7• §eIdentifier: §f" + plot.getIdentifier());

        // Owner-Info (wenn verfügbar)
        try {
            var plotProvider = providers.getPlotProvider();
            if (plotProvider != null && plotProvider.isAvailable()) {
                var ownerOpt = plotProvider.getOwner(plot);
                if (ownerOpt.isPresent()) {
                    lore.add("§7• §eOwner: §f" + ownerOpt.get().getName());
                }
            }
        } catch (Exception e) {
            // Owner nicht ermittelbar - überspringen
        }

        lore.add("");
        lore.add("§7Weitere Details im Info-UI");
        lore.add("");
        lore.add("§b§l➤ Klicke zum Öffnen");

        return lore;
    }

    @Override
    public boolean isVisible(Player viewer) {
        // Immer sichtbar
        return true;
    }
}
