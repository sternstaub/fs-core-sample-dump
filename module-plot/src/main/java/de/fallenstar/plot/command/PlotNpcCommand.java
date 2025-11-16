package de.fallenstar.plot.command;

import de.fallenstar.core.exception.ProviderFunctionalityNotFoundException;
import de.fallenstar.core.provider.NPCProvider;
import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.provider.PlotProvider;
import de.fallenstar.core.registry.PlotTypeRegistry;
import de.fallenstar.core.registry.ProviderRegistry;
import de.fallenstar.plot.PlotModule;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

/**
 * Command: /plot npc [spawn/remove/list]
 *
 * Verwaltet NPCs auf Plots.
 * - spawn: Spawnt einen NPC basierend auf Plot-Typ
 * - remove: Entfernt einen NPC vom Plot
 * - list: Zeigt alle NPCs auf dem Plot
 *
 * Permissions: Nur für Spieler mit Admin-Rechten auf dem Plot
 *
 * @author FallenStar
 * @version 1.0
 */
public class PlotNpcCommand {

    private final PlotModule plugin;
    private final PlotProvider plotProvider;
    private final NPCProvider npcProvider;
    private final PlotTypeRegistry plotTypeRegistry;

    /**
     * Erstellt einen neuen PlotNpcCommand.
     *
     * @param plugin Plugin-Instanz
     * @param providers ProviderRegistry
     * @param plotTypeRegistry PlotTypeRegistry
     */
    public PlotNpcCommand(PlotModule plugin, ProviderRegistry providers, PlotTypeRegistry plotTypeRegistry) {
        this.plugin = plugin;
        this.plotProvider = providers.getPlotProvider();
        this.npcProvider = providers.getNpcProvider();
        this.plotTypeRegistry = plotTypeRegistry;
    }

    /**
     * Führt den Command aus.
     *
     * @param player Der Spieler
     * @param args Command-Argumente
     * @return true wenn erfolgreich
     */
    public boolean execute(Player player, String[] args) {
        // Prüfe ob Spieler auf einem Plot steht
        Plot plot;
        try {
            plot = plotProvider.getPlot(player.getLocation());
            if (plot == null) {
                player.sendMessage("§cDu stehst auf keinem Plot!");
                return true;
            }
        } catch (ProviderFunctionalityNotFoundException e) {
            player.sendMessage("§cFehler: " + e.getMessage());
            return true;
        }

        // Keine Subcommand-Args -> Hilfe
        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "spawn" -> {
                return handleSpawn(player, plot);
            }
            case "remove" -> {
                return handleRemove(player, plot);
            }
            case "list" -> {
                return handleList(player, plot);
            }
            default -> {
                player.sendMessage("§cUnbekannter Subcommand: " + subCommand);
                sendHelp(player);
                return true;
            }
        }
    }

    /**
     * Spawnt einen NPC auf dem Plot.
     *
     * @param player Der Spieler
     * @param plot Das Plot
     * @return true wenn erfolgreich
     */
    private boolean handleSpawn(Player player, Plot plot) {
        try {
            // Prüfe Admin-Rechte
            if (!plotProvider.hasAdminRights(player, plot)) {
                player.sendMessage("§cDu hast keine Admin-Rechte auf diesem Plot!");
                return true;
            }

            // Hole Plot-Typ
            String plotType = plotProvider.getPlotType(plot);

            // Hole Standard-NPC-Typ für diesen Plot-Typ
            String npcType = plotTypeRegistry.getDefaultNPCType(plotType);

            if (npcType == null) {
                player.sendMessage("§cFür Plot-Typ '§e" + plotType + "§c' ist kein Standard-NPC definiert!");
                player.sendMessage("§7Verfügbare NPC-Typen werden im NPC-Modul definiert.");
                return true;
            }

            // Erstelle NPC an Spieler-Location
            UUID npcId = npcProvider.createNPC(
                player.getLocation(),
                "NPC_" + plotType,  // Temporärer Name, wird vom NPC-Modul überschrieben
                "Steve"  // Standard-Skin
            );

            // Registriere NPC für Plot
            plotTypeRegistry.registerNPCForPlot(plot.getUuid(), npcId);

            player.sendMessage("§aNPC vom Typ '§e" + npcType + "§a' wurde gespawnt!");
            player.sendMessage("§7Das NPC-Modul wird den NPC konfigurieren...");

            // Event feuern für NPC-Modul (TODO: Custom Event)
            plugin.getLogger().info("NPC spawned on plot " + plot.getIdentifier() + " (Type: " + npcType + ")");

            return true;

        } catch (ProviderFunctionalityNotFoundException e) {
            player.sendMessage("§cFehler beim Spawnen: " + e.getMessage());
            return true;
        } catch (Exception e) {
            player.sendMessage("§cEin Fehler ist aufgetreten!");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Entfernt einen NPC vom Plot.
     *
     * @param player Der Spieler
     * @param plot Das Plot
     * @return true wenn erfolgreich
     */
    private boolean handleRemove(Player player, Plot plot) {
        try {
            // Prüfe Admin-Rechte
            if (!plotProvider.hasAdminRights(player, plot)) {
                player.sendMessage("§cDu hast keine Admin-Rechte auf diesem Plot!");
                return true;
            }

            // Hole NPCs auf Plot
            List<UUID> npcs = plotTypeRegistry.getNPCsForPlot(plot.getUuid());

            if (npcs.isEmpty()) {
                player.sendMessage("§cAuf diesem Plot sind keine NPCs!");
                return true;
            }

            // Entferne alle NPCs (später: Auswahl-System)
            int removed = 0;
            for (UUID npcId : npcs) {
                try {
                    if (npcProvider.removeNPC(npcId)) {
                        plotTypeRegistry.unregisterNPCForPlot(plot.getUuid(), npcId);
                        removed++;
                    }
                } catch (Exception e) {
                    player.sendMessage("§cFehler beim Entfernen von NPC " + npcId + ": " + e.getMessage());
                }
            }

            player.sendMessage("§a" + removed + " NPC(s) wurden entfernt!");
            return true;

        } catch (ProviderFunctionalityNotFoundException e) {
            player.sendMessage("§cFehler: " + e.getMessage());
            return true;
        } catch (Exception e) {
            player.sendMessage("§cEin Fehler ist aufgetreten!");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Zeigt alle NPCs auf dem Plot.
     *
     * @param player Der Spieler
     * @param plot Das Plot
     * @return true wenn erfolgreich
     */
    private boolean handleList(Player player, Plot plot) {
        // Hole NPCs auf Plot
        List<UUID> npcs = plotTypeRegistry.getNPCsForPlot(plot.getUuid());

        if (npcs.isEmpty()) {
            player.sendMessage("§7Auf diesem Plot sind keine NPCs.");
            return true;
        }

        player.sendMessage("§8§m---------§r §6NPCs auf Plot §8§m---------");
        for (int i = 0; i < npcs.size(); i++) {
            UUID npcId = npcs.get(i);
            player.sendMessage("§7" + (i + 1) + ". §eNPC-ID: §7" + npcId.toString().substring(0, 8) + "...");
        }
        player.sendMessage("§8§m--------------------------------");

        return true;
    }

    /**
     * Sendet Hilfe-Nachricht.
     *
     * @param player Der Spieler
     */
    private void sendHelp(Player player) {
        player.sendMessage("§8§m---------§r §6NPC Commands §8§m---------");
        player.sendMessage("§e/plot npc spawn §7- Spawnt NPC auf Plot");
        player.sendMessage("§e/plot npc remove §7- Entfernt NPCs vom Plot");
        player.sendMessage("§e/plot npc list §7- Zeigt alle NPCs auf Plot");
        player.sendMessage("§8§m-------------------------------");
    }
}
