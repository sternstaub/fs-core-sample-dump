package de.fallenstar.plot.action;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.provider.PlotProvider;
import de.fallenstar.core.registry.ProviderRegistry;
import de.fallenstar.core.ui.element.UiAction;
import de.fallenstar.plot.PlotModule;
import de.fallenstar.plot.ui.NpcManagementUi;
import de.fallenstar.plot.ui.PlayerNpcManagementUi;
import org.bukkit.entity.Player;

import java.util.Objects;

/**
 * Action zum Verwalten von NPCs auf einem Grundstück.
 *
 * **Command Pattern:**
 * - Erweitert PlotAction (Plot-Referenz + canExecute)
 * - Berechtigungen: Keine Owner-Requirement (auch Gäste sehen UI)
 * - Owner-View vs Spieler-View wird in execute() unterschieden
 *
 * **Status:** Basis-Implementierung (Sprint 11-12)
 *
 * **Aktuelle Funktionalität:**
 * - Öffnet Owner-View (NpcManagementUi) für Plot-Besitzer
 * - Öffnet Spieler-View (PlayerNpcManagementUi) für andere Spieler
 * - Automatische Ansichts-Auswahl basierend auf Owner-Status
 *
 * **Zwei Ansichten:**
 * 1. **Owner-View (NpcManagementUi):**
 *    - Zeigt alle NPCs auf dem Plot
 *    - Erlaubt Spawnen neuer NPCs
 *    - Vollständige Plot-NPC-Verwaltung
 *
 * 2. **Spieler-View (PlayerNpcManagementUi):**
 *    - Zeigt nur eigene NPCs
 *    - NPC-Slot kaufen
 *    - Eigene Händler-Inventare verwalten
 *
 * **Geplante Features (Sprint 13-14):**
 * - Citizens-Integration (echte NPC-Entities)
 * - NPC-Konfiguration (Inventar, Preise, Skins)
 * - Slot-System-Integration
 * - NPC-Reisesystem
 *
 * **Type-Safety:**
 * - Compiler erzwingt Plot-Referenz
 * - Provider-basierte Owner-Checks via PlotAction.isOwner()
 *
 * **Verwendung:**
 * ```java
 * addFunctionButton(
 *     Material.VILLAGER_SPAWN_EGG,
 *     "§6§lNPCs verwalten",
 *     List.of("§7Verwalte NPCs auf diesem Plot"),
 *     new ManageNpcsAction(plot, providers, plotModule)  // Type-Safe!
 * );
 * ```
 *
 * @author FallenStar
 * @version 4.0
 */
public final class ManageNpcsAction extends de.fallenstar.core.ui.element.PlotAction {

    private final PlotModule plotModule;

    /**
     * Konstruktor für ManageNpcsAction.
     *
     * @param plot Der Plot dessen NPCs verwaltet werden sollen
     * @param providers Die ProviderRegistry für Owner-Checks
     * @param plotModule Das PlotModule für NPC-Manager-Zugriff
     */
    public ManageNpcsAction(Plot plot, ProviderRegistry providers, PlotModule plotModule) {
        super(plot, providers); // PlotAction-Konstruktor
        this.plotModule = Objects.requireNonNull(plotModule, "PlotModule darf nicht null sein");
    }

    @Override
    protected boolean requiresOwnership() {
        return false; // Auch Gäste dürfen NPCs sehen (Spieler-View)
    }

    @Override
    protected void executeAction(Player player) {
        player.closeInventory();

        // Prüfe ob Spieler Owner ist (via PlotAction.isOwner()!)
        if (isOwner(player)) {
            // Owner-Ansicht: Alle NPCs auf dem Plot
            NpcManagementUi ownerUi = new NpcManagementUi(plot, plotModule);
            ownerUi.open(player);
        } else {
            // Spieler-Ansicht: Nur eigene NPCs
            PlayerNpcManagementUi playerUi = new PlayerNpcManagementUi(plot, player);
            playerUi.open(player);
        }
    }

    /**
     * Gibt das PlotModule zurück.
     *
     * @return Das PlotModule
     */
    public PlotModule getPlotModule() {
        return plotModule;
    }
}
