package de.fallenstar.plot.action;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.provider.StorageContainerPlot;
import de.fallenstar.core.registry.ProviderRegistry;
import de.fallenstar.core.ui.element.PlotAction;
import de.fallenstar.plot.PlotModule;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Action zum Anzeigen der Preis liste (Read-Only für Gäste).
 *
 * **Berechtigungen:**
 * - KEINE Owner-Requirement - Gäste dürfen Preise sehen!
 * - Optional: Permission "fallenstar.plot.prices.view"
 *
 * **Command Pattern:**
 * - Erweitert PlotAction (Plot-Referenz + canExecute)
 * - Hat PlotModule-Referenz für Preis-Zugriff
 * - Zeigt Preisliste für alle Spieler (Read-Only)
 *
 * **GuiRenderable (Sprint 18):**
 * - Icon: Material.EMERALD
 * - DisplayName: "§aPreisliste"
 * - Lore: Anzahl konfigurierte Preise + Info
 *
 * **Funktionalität:**
 * - Zeigt Ankauf-Preise (Spieler verkauft an NPC)
 * - Zeigt Verkauf-Preise (Spieler kauft vom NPC)
 * - Read-Only Ansicht (keine Bearbeitung möglich)
 * - Für Owner + Gäste verfügbar
 *
 * **TODO (Sprint 19+):**
 * - Dedizierte Read-Only UI implementieren
 * - Aktuell: Nachricht mit Preisliste im Chat
 *
 * **Verwendung:**
 * ```java
 * // In TradeguildPlot.getAvailablePlotActions():
 * actions.add(new PlotActionViewPrices(this, providers, plotModule));
 * ```
 *
 * @author FallenStar
 * @version 1.0
 * @see StorageContainerPlot
 */
public final class PlotActionViewPrices extends PlotAction {

    private final PlotModule plotModule;

    /**
     * Erstellt eine neue PlotActionViewPrices.
     *
     * @param plot Der Plot dessen Preise angezeigt werden sollen
     * @param providers ProviderRegistry für Zugriff
     * @param plotModule PlotModule für Preis-Zugriff
     */
    public PlotActionViewPrices(Plot plot, ProviderRegistry providers, PlotModule plotModule) {
        super(plot, providers);
        this.plotModule = Objects.requireNonNull(plotModule, "PlotModule darf nicht null sein");
    }

    @Override
    protected boolean requiresOwnership() {
        return false; // Gäste dürfen Preise SEHEN!
    }

    @Override
    protected String requiredPermission() {
        return "fallenstar.plot.prices.view";
    }

    @Override
    protected void executeAction(Player player) {
        player.closeInventory();

        // Prüfe ob Plot StorageContainerPlot ist
        if (!(plot instanceof StorageContainerPlot storagePlot)) {
            player.sendMessage("§cDieser Plot hat keine Preisliste.");
            return;
        }

        // TODO: Dedizierte Read-Only Preis-UI (Sprint 19+)
        // Aktuell: Chat-Nachricht
        player.sendMessage("§7§m                                    ");
        player.sendMessage("§6§lPreisliste: §e" + storagePlot.getDisplayName());
        player.sendMessage("");
        player.sendMessage("§7Hier siehst du alle Ankauf- und");
        player.sendMessage("§7Verkaufs-Preise dieses Händlers.");
        player.sendMessage("");
        player.sendMessage("§7Verwende §e/plot prices §7für Details");
        player.sendMessage("§7oder kaufe vom NPC-Händler.");
        player.sendMessage("§7§m                                    ");

        // TODO: Später ersetzen durch:
        // PriceListUi priceUi = new PriceListUi(storagePlot, plotModule, true); // read-only
        // priceUi.open(player);
    }

    // ========== GuiRenderable Implementation ==========

    @Override
    protected Material getIcon() {
        return Material.EMERALD;
    }

    @Override
    protected String getDisplayName() {
        return "§aPreisliste";
    }

    @Override
    protected List<String> getLore() {
        List<String> lore = new ArrayList<>();

        lore.add("§7Zeige aktuelle Ankauf- und");
        lore.add("§7Verkaufs-Preise dieses Händlers");
        lore.add("");
        lore.add("§7• §aAnkauf: §7Du verkaufst an NPC");
        lore.add("§7• §eVerkauf: §7Du kaufst vom NPC");
        lore.add("");
        lore.add("§7Diese Ansicht ist nur zur Info,");
        lore.add("§7Preise können nicht geändert werden");
        lore.add("");
        lore.add("§a§l➤ Klicke zum Anzeigen");

        return lore;
    }

    @Override
    public boolean isVisible(Player viewer) {
        // Nur anzeigen wenn Plot ein StorageContainerPlot ist
        return plot instanceof StorageContainerPlot;
    }
}
