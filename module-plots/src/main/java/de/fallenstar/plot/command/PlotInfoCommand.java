package de.fallenstar.plot.command;

import de.fallenstar.core.exception.ProviderFunctionalityNotFoundException;
import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.provider.PlotProvider;
import de.fallenstar.core.registry.PlotTypeRegistry;
import de.fallenstar.core.registry.ProviderRegistry;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

/**
 * Command: /plot info
 *
 * Zeigt Informationen über das Plot an dem der Spieler steht.
 * - Plot-Typ
 * - Besitzer
 * - Registrierte NPCs
 * - Admin-Rechte
 *
 * @author FallenStar
 * @version 1.0
 */
public class PlotInfoCommand {

    private final PlotProvider plotProvider;
    private final PlotTypeRegistry plotTypeRegistry;

    /**
     * Erstellt einen neuen PlotInfoCommand.
     *
     * @param providers ProviderRegistry
     * @param plotTypeRegistry PlotTypeRegistry
     */
    public PlotInfoCommand(ProviderRegistry providers, PlotTypeRegistry plotTypeRegistry) {
        this.plotProvider = providers.getPlotProvider();
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
        try {
            // Hole Plot an Spieler-Location
            Plot plot = plotProvider.getPlot(player.getLocation());

            if (plot == null) {
                player.sendMessage("§cDu stehst auf keinem Plot!");
                return true;
            }

            // Plot-Informationen sammeln
            String plotType = plotProvider.getPlotType(plot);
            String owner = plotProvider.getOwnerName(plot);
            boolean hasAdmin = plotProvider.hasAdminRights(player, plot);

            // NPCs auf Plot
            List<UUID> npcs = plotTypeRegistry.getNPCsForPlot(plot.getUuid());

            // Informationen anzeigen
            player.sendMessage("§8§m---------§r §6Plot Info §8§m---------");
            player.sendMessage("§7Typ: §e" + plotType);
            player.sendMessage("§7Besitzer: §e" + owner);
            player.sendMessage("§7Admin-Rechte: " + (hasAdmin ? "§a✓" : "§c✗"));

            if (!npcs.isEmpty()) {
                player.sendMessage("§7NPCs: §e" + npcs.size());
                player.sendMessage("  §8(nutze §7/plot npc list§8 für Details)");
            } else {
                player.sendMessage("§7NPCs: §8keine");
            }

            player.sendMessage("§8§m----------------------------");

            return true;

        } catch (ProviderFunctionalityNotFoundException e) {
            player.sendMessage("§cFehler: " + e.getMessage());
            return true;
        } catch (Exception e) {
            player.sendMessage("§cEin Fehler ist aufgetreten: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
