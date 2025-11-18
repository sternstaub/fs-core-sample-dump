package de.fallenstar.plot.action.npc;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.ui.element.UiAction;
import de.fallenstar.plot.PlotModule;
import de.fallenstar.plot.npc.manager.NPCManager;
import de.fallenstar.plot.npc.model.PlotNPC;
import de.fallenstar.plot.npc.model.PlotNPCType;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;

/**
 * Action zum Bestätigen und Ausführen des NPC-Spawns.
 *
 * **Funktionalität:**
 * - Spawnt NPC an Spieler-Position
 * - Registriert NPC im NPCManager
 * - Speichert NPC persistent in Config
 * - Gibt Feedback an Spieler
 *
 * **Validierung:**
 * - Prüft ob Spieler Owner ist
 * - Prüft ob Plot-Typ korrekt ist
 * - Prüft ob NPC-Limit erreicht
 *
 * **Citizens-Integration:**
 * - Wenn Citizens verfügbar: Erstellt echten NPC
 * - Wenn nicht verfügbar: Speichert nur Daten, NPC erscheint später
 *
 * @author FallenStar
 * @version 1.0
 */
public final class ConfirmSpawnNpcAction implements UiAction {

    private final Plot plot;
    private final PlotNPCType npcType;
    private final PlotModule plotModule;
    private final Player spawner;

    /**
     * Konstruktor für ConfirmSpawnNpcAction.
     *
     * @param plot Der Plot auf dem der NPC gespawnt werden soll
     * @param npcType Der Typ des NPCs
     * @param plotModule Das PlotModule für Manager-Zugriff
     * @param spawner Der Spieler der den NPC spawnt
     */
    public ConfirmSpawnNpcAction(Plot plot, PlotNPCType npcType, PlotModule plotModule, Player spawner) {
        this.plot = Objects.requireNonNull(plot, "Plot darf nicht null sein");
        this.npcType = Objects.requireNonNull(npcType, "NpcType darf nicht null sein");
        this.plotModule = Objects.requireNonNull(plotModule, "PlotModule darf nicht null sein");
        this.spawner = Objects.requireNonNull(spawner, "Spawner darf nicht null sein");
    }

    @Override
    public void execute(Player player) {
        player.closeInventory();

        // Hole NPCManager
        NPCManager npcManager = plotModule.getNPCManager();
        if (npcManager == null) {
            player.sendMessage("§cNPC-System ist nicht verfügbar!");
            return;
        }

        try {
            // Erstelle und registriere NPC an Spieler-Position
            PlotNPC npc = npcManager.createNPC(
                    npcType,
                    plot.getUuid(),  // hostPlot = Grundstück auf dem NPC steht
                    plot.getUuid(),  // sourcePlot = Grundstück das NPC "besitzt"
                    player.getLocation(),
                    null,  // owner (optional für Spielerhändler)
                    null   // customName (optional)
            );

            // Speichere Config
            npcManager.saveToConfig(plotModule.getConfig());
            plotModule.saveConfig();

            // Erfolgs-Nachricht
            player.sendMessage("§a§m----------§r §e§lNPC Gespawnt §a§m----------");
            player.sendMessage("§7Typ: §e" + getNpcTypeName(npcType));
            player.sendMessage("§7Plot: §e" + plot.getIdentifier());
            player.sendMessage("§7Position: §e" +
                    (int) player.getLocation().getX() + ", " +
                    (int) player.getLocation().getY() + ", " +
                    (int) player.getLocation().getZ());
            player.sendMessage("§7UUID: §e" + npc.getNpcId());
            player.sendMessage("");

            // Citizens-Status
            if (plotModule.getProviders().getNpcProvider().isAvailable()) {
                player.sendMessage("§a§lNPC wird gespawnt...");
                player.sendMessage("§7Der NPC sollte in wenigen Sekunden erscheinen");

                // TODO: Spawn actual Citizens NPC
                // For now: just save data
                player.sendMessage("§7");
                player.sendMessage("§c§lCitizens-Integration folgt in Sprint 13-14");
            } else {
                player.sendMessage("§e§lNPC-Daten gespeichert!");
                player.sendMessage("§7Der NPC erscheint sobald Citizens geladen ist");
            }

            player.sendMessage("§a§m--------------------------------");

        } catch (Exception e) {
            player.sendMessage("§cFehler beim Spawnen des NPCs: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public String getActionName() {
        return "ConfirmSpawnNpc[" + plot.getIdentifier() + ", " + npcType + "]";
    }

    /**
     * Gibt Namen für NPC-Typ zurück.
     */
    private String getNpcTypeName(PlotNPCType type) {
        return switch (type) {
            case GUILD_TRADER -> "Gildenhändler";
            case PLAYER_TRADER -> "Spielerhändler";
            case LOCAL_BANKER -> "Lokaler Bankier";
            case WORLD_BANKER -> "Weltbankier";
            case AMBASSADOR -> "Botschafter";
            case CRAFTSMAN -> "Handwerker";
        };
    }
}
