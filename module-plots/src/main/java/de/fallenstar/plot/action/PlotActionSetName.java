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
 * Action zum Setzen des Plot-Namens.
 *
 * **Naming Convention:** PlotAction* Prefix für alphabetische Hierarchie-Erkennung
 *
 * **Berechtigungen:**
 * - Nur Plot-Owner dürfen Namen setzen
 * - Optional: Permission "fallenstar.plot.name.set"
 *
 * **Command Pattern:**
 * - Hat Plot-Referenz (this.plot)
 * - Prüft canExecute() → isOwner()
 * - Führt execute() aus → Öffnet Name-Input-UI
 *
 * **GuiRenderable:**
 * - Self-Rendering via getDisplayItem()
 * - Automatische Permission-Lore bei !canExecute()
 *
 * **Verwendung:**
 * <pre>
 * // In NamedPlot-Trait:
 * default List&lt;PlotAction&gt; getNameActions(ProviderRegistry providers) {
 *     return List.of(new PlotActionSetName(this, providers));
 * }
 * </pre>
 *
 * @author FallenStar
 * @version 2.0 (Sprint 19 - Naming Convention)
 */
public class PlotActionSetName extends PlotAction {

    /**
     * Erstellt eine neue PlotActionSetName.
     *
     * @param plot Der Plot dessen Name gesetzt werden soll
     * @param providers ProviderRegistry für Owner-Checks
     */
    public PlotActionSetName(Plot plot, ProviderRegistry providers) {
        super(plot, providers);
    }

    @Override
    protected boolean requiresOwnership() {
        return true; // Nur Owner dürfen Namen setzen
    }

    @Override
    protected String requiredPermission() {
        return "fallenstar.plot.name.set";
    }

    @Override
    protected void executeAction(Player player) {
        player.closeInventory();

        // Prüfe ob Plot NamedPlot ist
        if (!(plot instanceof NamedPlot namedPlot)) {
            player.sendMessage("§cDieser Plot unterstützt keine benutzerdefinierten Namen.");
            return;
        }

        // Öffne Name-Input via Command
        // TODO: Später durch TextInputUI ersetzen
        player.sendMessage("§aGib den neuen Plot-Namen im Chat ein:");
        player.sendMessage("§7(Verwende /plot name <name>)");

        // Alternativ: Direkter Command
        // player.performCommand("plot name");
    }

    // ========== GuiRenderable Implementation ==========

    @Override
    protected Material getIcon() {
        return Material.NAME_TAG;
    }

    @Override
    protected String getDisplayName() {
        return "§dPlot-Name setzen";
    }

    @Override
    protected List<String> getLore() {
        List<String> lore = new ArrayList<>();

        // Aktueller Name anzeigen wenn NamedPlot
        if (plot instanceof NamedPlot namedPlot) {
            lore.add("§7Aktueller Name: §e" + namedPlot.getDisplayName());
            lore.add("");
        }

        lore.add("§7Klicke um den Plot-Namen zu ändern");
        lore.add("§7Du wirst aufgefordert einen neuen");
        lore.add("§7Namen im Chat einzugeben");

        return lore;
    }
}
