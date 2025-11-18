package de.fallenstar.plot.action;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.ui.element.UiAction;
import org.bukkit.entity.Player;

import java.util.Objects;

/**
 * Action zum Scannen des Plot-Storage.
 *
 * Scannt alle Truhen auf dem Grundstück neu und
 * aktualisiert die Material-Liste.
 *
 * **Nur für Owner verfügbar!**
 *
 * Führt den Befehl "/plot storage scan" aus.
 *
 * **Verwendung:**
 * ```java
 * if (isOwner) {
 *     addFunctionButton(
 *         Material.COMPASS,
 *         "§a§lStorage scannen",
 *         List.of("§7Scannt alle Truhen"),
 *         new ScanStorageAction(plot)  // Type-Safe!
 *     );
 * }
 * ```
 *
 * @author FallenStar
 * @version 2.0
 */
public final class ScanStorageAction implements UiAction {

    private final Plot plot;

    /**
     * Konstruktor für ScanStorageAction.
     *
     * @param plot Der Plot dessen Storage gescannt werden soll
     */
    public ScanStorageAction(Plot plot) {
        this.plot = Objects.requireNonNull(plot, "Plot darf nicht null sein");
    }

    @Override
    public void execute(Player player) {
        player.closeInventory();
        player.sendMessage("§e§lStorage wird gescannt...");
        player.performCommand("plot storage scan");
    }

    @Override
    public String getActionName() {
        return "ScanStorage[" + plot.getIdentifier() + "]";
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
