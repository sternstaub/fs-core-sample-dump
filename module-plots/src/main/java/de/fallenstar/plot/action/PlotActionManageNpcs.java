package de.fallenstar.plot.action;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.registry.ProviderRegistry;
import de.fallenstar.plot.PlotModule;
import de.fallenstar.plot.ui.NpcManagementUi;
import de.fallenstar.plot.ui.PlayerNpcManagementUi;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Action zum Verwalten von NPCs auf einem Grundstück.
 *
 * **Naming Convention:** PlotAction* Prefix für alphabetische Hierarchie-Erkennung
 *
 * **Command Pattern:**
 * - Erweitert PlotAction (Plot-Referenz + canExecute)
 * - Berechtigungen: Keine Owner-Requirement (auch Gäste sehen UI)
 * - Owner-View vs Spieler-View wird in execute() unterschieden
 *
 * **GuiRenderable:**
 * - Self-Rendering via getDisplayItem()
 * - Automatische Permission-Lore bei !canExecute()
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
 * **Type-Safety:**
 * - Compiler erzwingt Plot-Referenz
 * - Provider-basierte Owner-Checks via PlotAction.isOwner()
 *
 * **Verwendung:**
 * ```java
 * new PlotActionManageNpcs(plot, providers, plotModule)
 * ```
 *
 * @author FallenStar
 * @version 5.0 (Sprint 19 - Naming Convention)
 */
public final class PlotActionManageNpcs extends de.fallenstar.core.ui.element.PlotAction {

    private final PlotModule plotModule;

    /**
     * Konstruktor für PlotActionManageNpcs.
     *
     * @param plot Der Plot dessen NPCs verwaltet werden sollen
     * @param providers Die ProviderRegistry für Owner-Checks
     * @param plotModule Das PlotModule für NPC-Manager-Zugriff
     */
    public PlotActionManageNpcs(Plot plot, ProviderRegistry providers, PlotModule plotModule) {
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

    // ========== GuiRenderable Implementation ==========

    @Override
    protected Material getIcon() {
        return Material.VILLAGER_SPAWN_EGG;
    }

    @Override
    protected String getDisplayName() {
        return "§bNPCs verwalten";
    }

    @Override
    protected List<String> getLore() {
        List<String> lore = new ArrayList<>();

        lore.add("§7Verwalte Gildenhändler und");
        lore.add("§7Spielerhändler auf diesem Plot");
        lore.add("");
        lore.add("§7Klicke zum Öffnen");

        return lore;
    }
}
