package de.fallenstar.plot.action.npc;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.ui.element.UiAction;
import de.fallenstar.plot.PlotModule;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;

/**
 * Action zum Despawnen eines NPCs.
 *
 * **Funktionalität:**
 * - Entfernt NPC aus Citizens
 * - Entfernt NPC aus PlotBoundNPCRegistry
 * - Entfernt NPC aus NPCs-Modul (falls vorhanden)
 * - Speichert Config
 * - Gibt Feedback an Spieler
 *
 * **Validierung:**
 * - Prüft ob Spieler Owner ist
 * - Prüft ob NPC existiert
 *
 * @author FallenStar
 * @version 1.0
 */
public final class DespawnNpcAction implements UiAction {

    private final Plot plot;
    private final UUID npcId;
    private final String npcName;
    private final PlotModule plotModule;

    /**
     * Konstruktor für DespawnNpcAction.
     *
     * @param plot Der Plot auf dem der NPC ist
     * @param npcId Die UUID des NPCs
     * @param npcName Name des NPCs (für Feedback)
     * @param plotModule Das PlotModule
     */
    public DespawnNpcAction(Plot plot, UUID npcId, String npcName, PlotModule plotModule) {
        this.plot = Objects.requireNonNull(plot, "Plot darf nicht null sein");
        this.npcId = Objects.requireNonNull(npcId, "NpcId darf nicht null sein");
        this.npcName = Objects.requireNonNull(npcName, "NpcName darf nicht null sein");
        this.plotModule = Objects.requireNonNull(plotModule, "PlotModule darf nicht null sein");
    }

    @Override
    public void execute(Player player) {
        player.closeInventory();

        try {
            // Prüfe Owner-Berechtigung
            var plotProvider = plotModule.getProviders().getPlotProvider();
            if (!plotProvider.isOwner(plot, player)) {
                player.sendMessage("§cDu musst der Plot-Besitzer sein um NPCs zu entfernen!");
                return;
            }

            // Prüfe ob NPC in Registry ist
            var npcRegistry = plotModule.getNPCRegistry();
            if (npcRegistry == null) {
                player.sendMessage("§cNPC-Registry nicht verfügbar!");
                return;
            }

            if (!npcRegistry.isNPCBound(npcId)) {
                player.sendMessage("§cDieser NPC ist nicht registriert!");
                return;
            }

            player.sendMessage("§e§m----------§r §6§lNPC Entfernen §e§m----------");
            player.sendMessage("§7Entferne §e" + npcName + "§7...");
            player.sendMessage("");

            // 1. Entferne aus Citizens (falls vorhanden)
            int removed = 0;
            var npcProvider = plotModule.getProviders().getNpcProvider();
            if (npcProvider != null && npcProvider.isAvailable()) {
                try {
                    boolean citizensRemoved = npcProvider.removeNPC(npcId);
                    if (citizensRemoved) {
                        player.sendMessage("§a✓ Citizens-NPC entfernt");
                        removed++;
                    } else {
                        player.sendMessage("§e⚠ Citizens-NPC nicht gefunden (möglicherweise bereits entfernt)");
                    }
                } catch (Exception e) {
                    player.sendMessage("§e⚠ Fehler beim Entfernen aus Citizens: " + e.getMessage());
                    plotModule.getLogger().warning("Failed to remove Citizens NPC: " + e.getMessage());
                }
            } else {
                player.sendMessage("§7  Citizens nicht verfügbar - überspringe");
            }

            // 2. Entferne aus PlotBoundNPCRegistry
            boolean registryRemoved = npcRegistry.unregisterNPC(npcId);
            if (registryRemoved) {
                player.sendMessage("§a✓ NPC aus Plot-Registry entfernt");
                removed++;
            } else {
                player.sendMessage("§e⚠ NPC nicht in Registry gefunden");
            }

            // 3. Entferne aus NPCs-Modul (falls vorhanden)
            try {
                unregisterFromNPCsModule(npcId);
                player.sendMessage("§a✓ NPC aus NPCs-Modul entfernt");
                removed++;
            } catch (Exception e) {
                player.sendMessage("§7  NPCs-Modul nicht verfügbar - überspringe");
            }

            // 4. Speichere Config
            plotModule.saveConfiguration();
            player.sendMessage("§a✓ Änderungen gespeichert");

            player.sendMessage("");
            if (removed > 0) {
                player.sendMessage("§a§l✓ NPC erfolgreich entfernt!");
                player.sendMessage("§7Der NPC §e" + npcName + " §7wurde gelöscht.");
            } else {
                player.sendMessage("§e§l⚠ NPC teilweise entfernt");
                player.sendMessage("§7Einige Komponenten konnten nicht entfernt werden.");
            }
            player.sendMessage("§e§m--------------------------------");

        } catch (Exception e) {
            player.sendMessage("§cFehler beim Entfernen des NPCs: " + e.getMessage());
            plotModule.getLogger().severe("Failed to despawn NPC " + npcId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public String getActionName() {
        return "DespawnNpc[" + npcName + ", " + npcId + "]";
    }

    /**
     * Entfernt den NPC aus dem NPCs-Modul (falls geladen).
     *
     * Dies entfernt den NPC aus dem NPCManager und dem GuildTraderNPC-System.
     */
    private void unregisterFromNPCsModule(UUID npcId) throws Exception {
        var npcsPlugin = plotModule.getServer().getPluginManager().getPlugin("FallenStar-NPCs");
        if (npcsPlugin == null) {
            throw new IllegalStateException("NPCs module not loaded");
        }

        // Reflection: Hole NPCManager
        var getNPCManager = npcsPlugin.getClass().getMethod("getNPCManager");
        var npcManager = getNPCManager.invoke(npcsPlugin);

        if (npcManager == null) {
            throw new IllegalStateException("NPCManager not available");
        }

        // Reflection: unregisterNPC(UUID npcId)
        var unregisterMethod = npcManager.getClass().getMethod("unregisterNPC", UUID.class);
        unregisterMethod.invoke(npcManager, npcId);

        plotModule.getLogger().info("Unregistered NPC " + npcId + " from NPCs module");
    }
}
