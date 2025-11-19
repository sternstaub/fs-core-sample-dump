package de.fallenstar.plot.action;

import de.fallenstar.core.provider.NamedPlot;
import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.registry.ProviderRegistry;
import de.fallenstar.core.ui.element.PlotAction;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Action zum Anzeigen der Grundstücks-Informationen.
 *
 * **Naming Convention:** PlotAction* Prefix für alphabetische Hierarchie-Erkennung
 *
 * **Command Pattern:**
 * - Erweitert PlotAction (Plot-Referenz + canExecute)
 * - Keine Owner-Requirement (jeder darf Infos sehen)
 * - Führt Command "/plot info" aus
 *
 * **Grundstücks-Informationen:**
 * - Besitzer
 * - Typ (Handelsgilde, Botschaft, etc.)
 * - Größe
 * - Permissions
 * - etc.
 *
 * **GuiRenderable:**
 * - Self-Rendering via getDisplayItem()
 * - Zeigt Plot-Name in Lore (wenn vorhanden)
 *
 * **Verwendung:**
 * ```java
 * new PlotActionInfo(plot, providers)
 * ```
 *
 * @author FallenStar
 * @version 3.0 (Sprint 19 - Migration zu PlotAction)
 */
public final class PlotActionInfo extends PlotAction {

    /**
     * Konstruktor für PlotActionInfo.
     *
     * @param plot Der Plot dessen Infos angezeigt werden sollen
     * @param providers ProviderRegistry für Owner-Checks
     */
    public PlotActionInfo(Plot plot, ProviderRegistry providers) {
        super(plot, providers);
    }

    @Override
    protected boolean requiresOwnership() {
        return false; // Jeder darf Infos sehen
    }

    @Override
    protected void executeAction(Player player) {
        player.closeInventory();
        player.performCommand("plot info");
    }

    // ========== GuiRenderable Implementation ==========

    @Override
    protected Material getIcon() {
        return Material.MAP;
    }

    @Override
    protected String getDisplayName() {
        return "§eGrundstücks-Info";
    }

    @Override
    protected List<String> getLore() {
        List<String> lore = new ArrayList<>();

        lore.add("§7Zeigt detaillierte Informationen");
        lore.add("§7über dieses Grundstück:");
        lore.add("");
        lore.add("§7• Besitzer");
        lore.add("§7• Typ");
        lore.add("§7• Größe");
        lore.add("§7• Permissions");

        // Wenn NamedPlot, zeige Custom-Name
        if (plot instanceof NamedPlot namedPlot) {
            namedPlot.getCustomName().ifPresent(name -> {
                lore.add("");
                lore.add("§7Plot-Name: §e" + name);
            });
        }

        lore.add("");
        lore.add("§7Klicke zum Anzeigen");

        return lore;
    }
}
