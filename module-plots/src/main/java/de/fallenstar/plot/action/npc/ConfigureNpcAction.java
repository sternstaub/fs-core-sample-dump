package de.fallenstar.plot.action.npc;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.ui.element.UiAction;
import de.fallenstar.plot.PlotModule;
import de.fallenstar.plot.ui.NpcConfigUi;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;

/**
 * Action zum Öffnen der NPC-Konfiguration.
 *
 * **Funktionalität:**
 * - Öffnet NPC-Konfigurations-UI
 * - Zeigt NPC-Details
 * - Bietet Verwaltungsoptionen (Despawn, etc.)
 *
 * **Verwendung:**
 * Wird von NpcManagementUi verwendet wenn Spieler auf NPC-Item klickt.
 *
 * @author FallenStar
 * @version 1.0
 */
public final class ConfigureNpcAction implements UiAction {

    private final Plot plot;
    private final UUID npcId;
    private final String npcName;
    private final String npcType;
    private final PlotModule plotModule;

    /**
     * Konstruktor für ConfigureNpcAction.
     *
     * @param plot Der Plot auf dem der NPC ist
     * @param npcId Die UUID des NPCs
     * @param npcName Name des NPCs
     * @param npcType Typ des NPCs
     * @param plotModule Das PlotModule
     */
    public ConfigureNpcAction(Plot plot, UUID npcId, String npcName, String npcType, PlotModule plotModule) {
        this.plot = Objects.requireNonNull(plot, "Plot darf nicht null sein");
        this.npcId = Objects.requireNonNull(npcId, "NpcId darf nicht null sein");
        this.npcName = Objects.requireNonNull(npcName, "NpcName darf nicht null sein");
        this.npcType = Objects.requireNonNull(npcType, "NpcType darf nicht null sein");
        this.plotModule = Objects.requireNonNull(plotModule, "PlotModule darf nicht null sein");
    }

    @Override
    public void execute(Player player) {
        player.closeInventory();

        // Öffne NPC-Konfigurations-UI
        NpcConfigUi configUi = new NpcConfigUi(plot, npcId, npcName, npcType, plotModule);
        configUi.open(player);
    }

    @Override
    public String getActionName() {
        return "ConfigureNpc[" + npcName + ", " + npcId + "]";
    }
}
