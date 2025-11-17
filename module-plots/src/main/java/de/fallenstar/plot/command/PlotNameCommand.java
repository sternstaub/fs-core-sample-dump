package de.fallenstar.plot.command;

import de.fallenstar.core.exception.ProviderFunctionalityNotFoundException;
import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.provider.PlotProvider;
import de.fallenstar.core.registry.ProviderRegistry;
import de.fallenstar.plot.PlotModule;
import de.fallenstar.plot.manager.PlotNameManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Command für Plot-Namen-Verwaltung.
 *
 * Verwendung:
 * - /plot name <name> - Setzt Plot-Namen
 * - /plot name clear - Löscht Plot-Namen
 * - /plot name - Zeigt aktuellen Plot-Namen
 *
 * @author FallenStar
 * @version 1.0
 */
public class PlotNameCommand {

    private final Logger logger;
    private final PlotModule plugin;
    private final ProviderRegistry providers;
    private final PlotNameManager plotNameManager;

    /**
     * Erstellt einen neuen PlotNameCommand.
     *
     * @param logger Logger für Debug-Ausgaben
     * @param plugin PlotModule-Instanz
     * @param providers ProviderRegistry
     * @param plotNameManager PlotNameManager
     */
    public PlotNameCommand(Logger logger, PlotModule plugin,
                           ProviderRegistry providers,
                           PlotNameManager plotNameManager) {
        this.logger = logger;
        this.plugin = plugin;
        this.providers = providers;
        this.plotNameManager = plotNameManager;
    }

    /**
     * Führt den Command aus.
     *
     * @param sender Der Sender
     * @param args Command-Argumente
     * @return true wenn erfolgreich
     */
    public boolean execute(CommandSender sender, String[] args) {
        // Nur Spieler können Commands ausführen
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cDieser Command kann nur von Spielern ausgeführt werden!");
            return true;
        }

        Player player = (Player) sender;
        PlotProvider plotProvider = providers.getPlotProvider();

        try {
            // Hole aktuelles Plot
            Plot plot = plotProvider.getPlot(player.getLocation());

            if (plot == null) {
                player.sendMessage("§cDu stehst auf keinem Grundstück!");
                return true;
            }

            // Prüfe Owner-Rechte
            if (!plotProvider.isOwner(plot, player)) {
                player.sendMessage("§cDu musst der Besitzer dieses Grundstücks sein!");
                try {
                    String owner = plotProvider.getOwnerName(plot);
                    player.sendMessage("§7Besitzer: §e" + owner);
                } catch (Exception e) {
                    // Ignoriere Fehler
                }
                return true;
            }

            // Handle Subcommands
            if (args.length == 0) {
                // Zeige aktuellen Namen
                return handleShowName(player, plot);
            }

            String subCommand = args[0].toLowerCase();

            if (subCommand.equals("clear")) {
                // Lösche Namen
                return handleClearName(player, plot);
            } else {
                // Setze Namen (alle args zusammen)
                String name = String.join(" ", args);
                return handleSetName(player, plot, name);
            }

        } catch (ProviderFunctionalityNotFoundException e) {
            player.sendMessage("§cPlot-System nicht verfügbar: " + e.getMessage());
            logger.warning("Plot-System nicht verfügbar: " + e.getMessage());
            return true;
        } catch (Exception e) {
            player.sendMessage("§cFehler beim Verarbeiten des Commands: " + e.getMessage());
            logger.warning("Fehler in PlotNameCommand: " + e.getMessage());
            e.printStackTrace();
            return true;
        }
    }

    /**
     * Zeigt den aktuellen Plot-Namen.
     *
     * @param player Der Spieler
     * @param plot Der Plot
     * @return true
     */
    private boolean handleShowName(Player player, Plot plot) {
        String customName = plotNameManager.getPlotName(plot);

        player.sendMessage("§e§l=== Plot-Name ===");
        player.sendMessage("§7Grundstück: §e" + plot.getIdentifier());

        if (customName != null && !customName.isEmpty()) {
            player.sendMessage("§7Benutzerdefinierter Name: §a" + customName);
            player.sendMessage("");
            player.sendMessage("§7Zum Ändern: §e/plot name <neuer name>");
            player.sendMessage("§7Zum Löschen: §e/plot name clear");
        } else {
            player.sendMessage("§7Benutzerdefinierter Name: §cnicht gesetzt");
            player.sendMessage("");
            player.sendMessage("§7Zum Setzen: §e/plot name <name>");
        }

        return true;
    }

    /**
     * Setzt den Plot-Namen.
     *
     * @param player Der Spieler
     * @param plot Der Plot
     * @param name Der neue Name
     * @return true
     */
    private boolean handleSetName(Player player, Plot plot, String name) {
        // Validierung via NamedPlot
        if (!de.fallenstar.plot.model.NamedPlot.isValidName(name)) {
            player.sendMessage("§cUngültiger Plot-Name!");
            player.sendMessage("§7Namen müssen 1-32 Zeichen lang sein.");
            player.sendMessage("§7Erlaubt: Buchstaben, Zahlen, Leerzeichen, -, _");
            return true;
        }

        // Setze Namen
        boolean success = plotNameManager.setPlotName(plot, name);

        if (success) {
            player.sendMessage("§aPlot-Name gesetzt: §e" + name);
            player.sendMessage("§7Grundstück: §7" + plot.getIdentifier());
            player.sendMessage("§7§oGespeichert in Towny MetaData");

            // Speichere Config (Fallback)
            plugin.saveConfiguration();
        } else {
            player.sendMessage("§cFehler beim Setzen des Plot-Namens!");
        }

        return true;
    }

    /**
     * Löscht den Plot-Namen.
     *
     * @param player Der Spieler
     * @param plot Der Plot
     * @return true
     */
    private boolean handleClearName(Player player, Plot plot) {
        String oldName = plotNameManager.getPlotName(plot);

        if (oldName == null || oldName.isEmpty()) {
            player.sendMessage("§cDieses Grundstück hat keinen benutzerdefinierten Namen!");
            return true;
        }

        // Lösche Namen
        boolean success = plotNameManager.setPlotName(plot, null);

        if (success) {
            player.sendMessage("§aPlot-Name gelöscht: §7" + oldName);
            player.sendMessage("§7Grundstück: §e" + plot.getIdentifier());

            // Speichere Config
            plugin.saveConfiguration();
        } else {
            player.sendMessage("§cFehler beim Löschen des Plot-Namens!");
        }

        return true;
    }

    /**
     * Gibt Tab-Completion-Vorschläge zurück.
     *
     * @param sender Der Sender
     * @param args Bisherige Argumente
     * @return Liste von Vorschlägen
     */
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Erste Ebene: Subcommands
            completions.add("clear");
        }

        return completions;
    }

    /**
     * Sendet Hilfe-Nachricht an Sender.
     *
     * @param sender Der Sender
     */
    public void sendHelp(CommandSender sender) {
        sender.sendMessage("§e§l=== Plot Name ===");
        sender.sendMessage("§7/plot name §f- Zeigt aktuellen Plot-Namen");
        sender.sendMessage("§7/plot name <name> §f- Setzt Plot-Namen");
        sender.sendMessage("§7/plot name clear §f- Löscht Plot-Namen");
    }
}
