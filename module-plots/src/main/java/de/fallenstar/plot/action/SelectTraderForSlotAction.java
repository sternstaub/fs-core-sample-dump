package de.fallenstar.plot.action;

import de.fallenstar.core.ui.element.UiAction;
import de.fallenstar.plot.slot.PlotSlot;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.Objects;

/**
 * Action zum Auswählen eines Händlers für einen Slot.
 *
 * Startet den NPC-Reiseprozess:
 * 1. Berechnet Reisekosten (5 Sterne/Chunk)
 * 2. Berechnet Reisedauer (10s/Chunk)
 * 3. Startet NPC-Reise (NPCTravelSystem)
 *
 * **Status:** Placeholder (NPC-Reisesystem noch nicht implementiert)
 *
 * **Verwendung:**
 * ```java
 * if (!plotSlot.isOccupied()) {
 *     var button = new ClickableUiElement.CustomButton<>(
 *         traderItem,
 *         new SelectTraderForSlotAction(plotSlot, guildPlotId)
 *     );
 * }
 * ```
 *
 * @author FallenStar
 * @version 2.0
 */
public final class SelectTraderForSlotAction implements UiAction {

    private final PlotSlot targetSlot;
    private final String sourceGuildPlotId;

    /**
     * Konstruktor für SelectTraderForSlotAction.
     *
     * @param targetSlot Ziel-Slot für Händler
     * @param sourceGuildPlotId Plot-ID der Quellle-Handelsgilde
     */
    public SelectTraderForSlotAction(PlotSlot targetSlot, String sourceGuildPlotId) {
        this.targetSlot = Objects.requireNonNull(targetSlot, "PlotSlot darf nicht null sein");
        this.sourceGuildPlotId = Objects.requireNonNull(sourceGuildPlotId, "Guildengilde-Plot-ID darf nicht null sein");
    }

    @Override
    public void execute(Player player) {
        // TODO: NPC-Reisesystem-Integration
        player.closeInventory();

        player.sendMessage(Component.text("✗ NPC-Reisesystem noch nicht implementiert!", NamedTextColor.RED));
        player.sendMessage(Component.text("Wird in Sprint 13-14 verfügbar sein", NamedTextColor.YELLOW));

        player.sendMessage(Component.text(""));
        player.sendMessage(Component.text("Geplanter Ablauf:", NamedTextColor.GRAY));
        player.sendMessage(Component.text("1. Händler von " + sourceGuildPlotId.substring(0, 8) + " auswählen", NamedTextColor.GRAY));
        player.sendMessage(Component.text("2. Reisekosten: ~5 Sterne/Chunk berechnen", NamedTextColor.GRAY));
        player.sendMessage(Component.text("3. Reisedauer: ~10s/Chunk berechnen", NamedTextColor.GRAY));
        player.sendMessage(Component.text("4. NPC reist zu Slot " + targetSlot.getSlotId().toString().substring(0, 8), NamedTextColor.GRAY));
    }

    @Override
    public String getActionName() {
        return "SelectTraderForSlot[" +
                "target=" + targetSlot.getSlotId().toString().substring(0, 8) +
                ", source=" + sourceGuildPlotId.substring(0, 8) +
                "]";
    }

    /**
     * Gibt den Ziel-Slot zurück.
     *
     * @return Der PlotSlot
     */
    public PlotSlot getTargetSlot() {
        return targetSlot;
    }

    /**
     * Gibt die Quell-Handelsgilden-Plot-ID zurück.
     *
     * @return Plot-ID
     */
    public String getSourceGuildPlotId() {
        return sourceGuildPlotId;
    }
}
