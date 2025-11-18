package de.fallenstar.plot.action;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.provider.PlotProvider;
import de.fallenstar.core.registry.ProviderRegistry;
import de.fallenstar.core.ui.element.UiAction;
import de.fallenstar.plot.ui.NpcManagementUi;
import de.fallenstar.plot.ui.PlayerNpcManagementUi;
import org.bukkit.entity.Player;

import java.util.Objects;

/**
 * Action zum Verwalten von NPCs auf einem Grundstück.
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
 * - Provider-basierte Owner-Checks
 *
 * **Verwendung:**
 * ```java
 * addFunctionButton(
 *     Material.VILLAGER_SPAWN_EGG,
 *     "§6§lNPCs verwalten",
 *     List.of("§7Verwalte NPCs auf diesem Plot"),
 *     new ManageNpcsAction(plot, providers)  // Type-Safe!
 * );
 * ```
 *
 * @author FallenStar
 * @version 3.0
 */
public final class ManageNpcsAction implements UiAction {

    private final Plot plot;
    private final ProviderRegistry providers;

    /**
     * Konstruktor für ManageNpcsAction.
     *
     * @param plot Der Plot dessen NPCs verwaltet werden sollen
     * @param providers Die ProviderRegistry für Owner-Checks
     */
    public ManageNpcsAction(Plot plot, ProviderRegistry providers) {
        this.plot = Objects.requireNonNull(plot, "Plot darf nicht null sein");
        this.providers = Objects.requireNonNull(providers, "ProviderRegistry darf nicht null sein");
    }

    @Override
    public void execute(Player player) {
        player.closeInventory();

        // Prüfe ob Spieler Owner ist
        boolean isOwner = isPlotOwner(player);

        if (isOwner) {
            // Owner-Ansicht: Alle NPCs auf dem Plot
            NpcManagementUi ownerUi = new NpcManagementUi(plot);
            ownerUi.open(player);
        } else {
            // Spieler-Ansicht: Nur eigene NPCs
            PlayerNpcManagementUi playerUi = new PlayerNpcManagementUi(plot, player);
            playerUi.open(player);
        }
    }

    @Override
    public String getActionName() {
        return "ManageNpcs[" + plot.getIdentifier() + "]";
    }

    /**
     * Prüft ob ein Spieler der Besitzer des Plots ist.
     *
     * @param player Der Spieler
     * @return true wenn Besitzer, sonst false
     */
    private boolean isPlotOwner(Player player) {
        PlotProvider plotProvider = providers.getPlotProvider();
        try {
            return plotProvider.isOwner(plot, player);
        } catch (Exception e) {
            // Bei Fehler: kein Owner
            return false;
        }
    }

    /**
     * Gibt den Plot zurück.
     *
     * @return Der Plot
     */
    public Plot getPlot() {
        return plot;
    }

    /**
     * Gibt die ProviderRegistry zurück.
     *
     * @return Die ProviderRegistry
     */
    public ProviderRegistry getProviders() {
        return providers;
    }
}
