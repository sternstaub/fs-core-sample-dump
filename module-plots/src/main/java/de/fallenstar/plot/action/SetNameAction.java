package de.fallenstar.plot.action;

import de.fallenstar.core.provider.NamedPlot;
import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.registry.ProviderRegistry;
import de.fallenstar.core.ui.element.PlotAction;
import org.bukkit.entity.Player;

/**
 * Action zum Setzen des Plot-Namens.
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
 * **Verwendung:**
 * <pre>
 * // In NamedPlot-Trait:
 * default List&lt;PlotAction&gt; getNameActions(ProviderRegistry providers) {
 *     return List.of(new SetNameAction(this, providers));
 * }
 * </pre>
 *
 * @author FallenStar
 * @version 1.0
 */
public class SetNameAction extends PlotAction {

    /**
     * Erstellt eine neue SetNameAction.
     *
     * @param plot Der Plot dessen Name gesetzt werden soll
     * @param providers ProviderRegistry für Owner-Checks
     */
    public SetNameAction(Plot plot, ProviderRegistry providers) {
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
}
