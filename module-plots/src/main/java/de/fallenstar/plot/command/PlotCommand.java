package de.fallenstar.plot.command;

import de.fallenstar.core.registry.PlotTypeRegistry;
import de.fallenstar.core.registry.ProviderRegistry;
import de.fallenstar.plot.PlotModule;
import de.fallenstar.plot.storage.command.StorageInfoCommand;
import de.fallenstar.plot.storage.command.StorageListCommand;
import de.fallenstar.plot.storage.command.StorageSetReceiverCommand;
import de.fallenstar.plot.storage.manager.StorageManager;
import de.fallenstar.plot.storage.provider.PlotStorageProvider;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Hauptcommand für Plot-Verwaltung: /plot [subcommand]
 *
 * Subcommands:
 * - /plot info - Zeigt Plot-Informationen
 * - /plot storage [view/list/setreceiver] - Storage-Verwaltung
 * - /plot npc [spawn/remove/list] - NPC-Verwaltung
 *
 * @author FallenStar
 * @version 1.0
 */
public class PlotCommand implements CommandExecutor, TabCompleter {

    private final PlotModule plugin;
    private final ProviderRegistry providers;
    private final PlotTypeRegistry plotTypeRegistry;

    private final boolean plotSystemEnabled;
    private final boolean townSystemEnabled;
    private final boolean npcSystemEnabled;
    private final boolean storageSystemEnabled;

    // Subcommands
    private final PlotInfoCommand infoCommand;
    private final PlotNpcCommand npcCommand;
    private final PlotPriceCommand priceCommand;
    private final StorageListCommand storageListCommand;
    private final StorageInfoCommand storageInfoCommand;
    private final StorageSetReceiverCommand storageSetReceiverCommand;

    /**
     * Erstellt einen neuen PlotCommand.
     *
     * @param plugin Plugin-Instanz
     * @param providers ProviderRegistry
     * @param plotTypeRegistry PlotTypeRegistry
     * @param plotSystemEnabled Ob Plot-System aktiviert
     * @param townSystemEnabled Ob Town-System aktiviert
     * @param npcSystemEnabled Ob NPC-System aktiviert
     * @param storageSystemEnabled Ob Storage-System aktiviert
     * @param storageProvider PlotStorageProvider (kann null sein)
     * @param storageManager StorageManager (kann null sein)
     */
    public PlotCommand(
            PlotModule plugin,
            ProviderRegistry providers,
            PlotTypeRegistry plotTypeRegistry,
            boolean plotSystemEnabled,
            boolean townSystemEnabled,
            boolean npcSystemEnabled,
            boolean storageSystemEnabled,
            PlotStorageProvider storageProvider,
            StorageManager storageManager
    ) {
        this.plugin = plugin;
        this.providers = providers;
        this.plotTypeRegistry = plotTypeRegistry;
        this.plotSystemEnabled = plotSystemEnabled;
        this.townSystemEnabled = townSystemEnabled;
        this.npcSystemEnabled = npcSystemEnabled;
        this.storageSystemEnabled = storageSystemEnabled;

        // Subcommands initialisieren
        this.infoCommand = new PlotInfoCommand(providers, plotTypeRegistry);
        this.npcCommand = new PlotNpcCommand(plugin, providers, plotTypeRegistry);
        this.priceCommand = new PlotPriceCommand(providers);

        // Storage-Commands initialisieren (wenn Storage aktiviert)
        if (storageSystemEnabled && storageProvider != null && storageManager != null) {
            this.storageListCommand = new StorageListCommand(providers.getPlotProvider(), storageProvider);
            this.storageInfoCommand = new StorageInfoCommand(providers.getPlotProvider(), storageProvider);
            this.storageSetReceiverCommand = new StorageSetReceiverCommand(providers.getPlotProvider(), storageManager);
        } else {
            this.storageListCommand = null;
            this.storageInfoCommand = null;
            this.storageSetReceiverCommand = null;
        }
    }

    /**
     * Gibt den PlotPriceCommand zurück (für Listener-Registrierung).
     *
     * @return PlotPriceCommand
     */
    public PlotPriceCommand getPriceCommand() {
        return priceCommand;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        // Nur Spieler können Plot-Commands nutzen
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cDieser Befehl kann nur von Spielern ausgeführt werden!");
            return true;
        }

        // Keine Argumente -> Hilfe anzeigen
        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        // Subcommand-Routing
        String subCommand = args[0].toLowerCase();
        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);

        switch (subCommand) {
            case "info" -> {
                return infoCommand.execute(player, subArgs);
            }

            case "storage" -> {
                if (!storageSystemEnabled) {
                    player.sendMessage("§cStorage-System ist nicht verfügbar!");
                    return true;
                }
                return handleStorageCommand(player, subArgs);
            }

            case "npc" -> {
                if (!npcSystemEnabled) {
                    player.sendMessage("§cNPC-System ist nicht verfügbar (kein NPC-Plugin geladen)!");
                    return true;
                }
                return npcCommand.execute(player, subArgs);
            }

            case "gui" -> {
                return handleGuiCommand(player, subArgs);
            }

            case "price" -> {
                return priceCommand.execute(player, subArgs);
            }

            case "help" -> {
                sendHelp(player);
                return true;
            }

            default -> {
                player.sendMessage("§cUnbekannter Subcommand: " + subCommand);
                player.sendMessage("§7Nutze §e/plot help §7für eine Liste aller Commands.");
                return true;
            }
        }
    }

    /**
     * Handhabt Storage-Commands direkt.
     *
     * @param player Der Spieler
     * @param args Storage-Subcommand Args
     * @return true wenn erfolgreich
     */
    private boolean handleStorageCommand(Player player, String[] args) {
        if (args.length == 0) {
            player.sendMessage("§cBitte wähle einen Storage-Subcommand:");
            player.sendMessage("§e/plot storage list §7- Zeigt alle Materialien");
            player.sendMessage("§e/plot storage info <material> §7- Zeigt Material-Details");
            player.sendMessage("§e/plot storage setreceiver §7- Setzt Empfangskiste");
            return true;
        }

        String storageSubcmd = args[0].toLowerCase();
        String[] storageArgs = Arrays.copyOfRange(args, 1, args.length);

        // Dummy-Command für onCommand (wird nicht wirklich gebraucht)
        Command dummyCmd = new Command("plot") {
            @Override
            public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
                return false;
            }
        };

        switch (storageSubcmd) {
            case "list" -> {
                if (storageListCommand != null) {
                    return storageListCommand.onCommand(player, dummyCmd, "list", storageArgs);
                }
                player.sendMessage("§cStorage-List-Command nicht verfügbar!");
                return true;
            }

            case "info" -> {
                if (storageInfoCommand != null) {
                    return storageInfoCommand.onCommand(player, dummyCmd, "info", storageArgs);
                }
                player.sendMessage("§cStorage-Info-Command nicht verfügbar!");
                return true;
            }

            case "setreceiver" -> {
                if (storageSetReceiverCommand != null) {
                    return storageSetReceiverCommand.onCommand(player, dummyCmd, "setreceiver", storageArgs);
                }
                player.sendMessage("§cStorage-SetReceiver-Command nicht verfügbar!");
                return true;
            }

            default -> {
                player.sendMessage("§cUnbekannter Storage-Subcommand: " + storageSubcmd);
                player.sendMessage("§7Verfügbare Commands: list, info, setreceiver");
                return true;
            }
        }
    }

    /**
     * Handhabt GUI-Commands - öffnet grundstückstyp-abhängige UIs.
     *
     * @param player Der Spieler
     * @param args GUI-Subcommand Args
     * @return true wenn erfolgreich
     */
    private boolean handleGuiCommand(Player player, String[] args) {
        if (!plotSystemEnabled) {
            player.sendMessage("§cPlot-System ist nicht verfügbar!");
            return true;
        }

        // Hole UIProvider
        var uiProvider = providers.getUIProvider();
        if (uiProvider == null || !uiProvider.isAvailable()) {
            player.sendMessage("§cUI-System ist nicht verfügbar!");
            return true;
        }

        // Hole aktuellen Plot
        var plotProvider = providers.getPlotProvider();
        try {
            var plot = plotProvider.getPlot(player.getLocation());
            if (plot == null) {
                player.sendMessage("§cDu stehst nicht auf einem Grundstück!");
                return true;
            }

            // Öffne grundstückstyp-spezifische UI
            String plotType = plotProvider.getPlotType(plot);
            if (plotType == null) {
                plotType = "default";
            }

            switch (plotType.toLowerCase()) {
                case "handelsgilde" -> openHandelsgildeUI(player, uiProvider);
                case "botschaft" -> openBotschaftUI(player, uiProvider);
                default -> openDefaultPlotUI(player, uiProvider, plot, plotType);
            }

            return true;

        } catch (Exception e) {
            player.sendMessage("§cFehler beim Abrufen der Plot-Informationen: " + e.getMessage());
            return true;
        }
    }

    /**
     * Öffnet die Handelsgilde-UI (Guest/Owner Views).
     *
     * @param player Der Spieler
     * @param uiProvider UIProvider-Instanz
     */
    private void openHandelsgildeUI(Player player, de.fallenstar.core.provider.UIProvider uiProvider) {
        // Hole aktuellen Plot
        var plotProvider = providers.getPlotProvider();
        try {
            var plot = plotProvider.getPlot(player.getLocation());
            if (plot == null) {
                player.sendMessage("§cDu stehst nicht auf einem Grundstück!");
                return;
            }

            // Prüfe ob Spieler Owner ist
            boolean isOwner = plotProvider.isOwner(plot, player);

            // Öffne HandelsgildeUI (mit Guest/Owner View)
            de.fallenstar.plot.ui.HandelsgildeUI ui = new de.fallenstar.plot.ui.HandelsgildeUI(
                    providers,
                    priceCommand,
                    plot,
                    isOwner
            );

            ui.open(player);

        } catch (Exception e) {
            player.sendMessage("§cFehler beim Öffnen der Handelsgilde-UI: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Öffnet die Botschaft-UI (Placeholder).
     *
     * @param player Der Spieler
     * @param uiProvider UIProvider-Instanz
     */
    private void openBotschaftUI(Player player, de.fallenstar.core.provider.UIProvider uiProvider) {
        var menu = new de.fallenstar.core.ui.UIMenu(
            "Botschaft - Verwaltung",
            "Optionen für Botschafts-Grundstücke"
        );

        menu.addButton(de.fallenstar.core.ui.UIButton.of(
            "info",
            "Grundstücks-Info",
            "/plot info"
        ));

        player.sendMessage("§7[Roadmap] Botschaft-UI noch nicht vollständig implementiert");
        try {
            uiProvider.showMenu(player, menu);
        } catch (Exception e) {
            player.sendMessage("§cFehler beim Öffnen der UI: " + e.getMessage());
        }
    }

    /**
     * Öffnet die Default-Plot-UI.
     *
     * @param player Der Spieler
     * @param uiProvider UIProvider-Instanz
     * @param plot Der Plot
     * @param plotType Der Plot-Typ
     */
    private void openDefaultPlotUI(Player player, de.fallenstar.core.provider.UIProvider uiProvider,
                                     de.fallenstar.core.provider.Plot plot, String plotType) {
        var menu = new de.fallenstar.core.ui.UIMenu(
            "Plot-Verwaltung",
            "Grundstück: " + plotType
        );

        menu.addButton(de.fallenstar.core.ui.UIButton.of(
            "info",
            "Grundstücks-Info",
            "/plot info"
        ));

        if (storageSystemEnabled) {
            menu.addButton(de.fallenstar.core.ui.UIButton.of(
                "storage",
                "Storage-Verwaltung",
                "/plot storage list"
            ));
        }

        if (npcSystemEnabled) {
            menu.addButton(de.fallenstar.core.ui.UIButton.of(
                "npc",
                "NPC-Verwaltung",
                "/plot npc list"
            ));
        }

        try {
            uiProvider.showMenu(player, menu);
        } catch (Exception e) {
            player.sendMessage("§cFehler beim Öffnen der UI: " + e.getMessage());
        }
    }

    /**
     * Sendet Hilfe-Nachricht an Spieler.
     *
     * @param player Der Spieler
     */
    private void sendHelp(Player player) {
        player.sendMessage("§8§m---------§r §6Plot Commands §8§m---------");
        player.sendMessage("§e/plot info §7- Zeigt Plot-Informationen");

        if (storageSystemEnabled) {
            player.sendMessage("§e/plot storage list §7- Zeigt Storage-Übersicht");
            player.sendMessage("§e/plot storage info <material> §7- Material-Details");
            player.sendMessage("§e/plot storage setreceiver §7- Empfangskiste setzen");
        } else {
            player.sendMessage("§7/plot storage §8[deaktiviert - Storage-Plugin fehlt]");
        }

        if (npcSystemEnabled) {
            player.sendMessage("§e/plot npc spawn §7- Spawnt NPC auf Plot");
            player.sendMessage("§e/plot npc remove §7- Entfernt NPC auf Plot");
            player.sendMessage("§e/plot npc list §7- Zeigt alle NPCs auf Plot");
        } else {
            player.sendMessage("§7/plot npc §8[deaktiviert - kein NPC-Plugin]");
        }

        // GUI-Command
        player.sendMessage("§e/plot gui §7- Öffnet grundstückstypabhängige GUI");

        // Price-Command (nur auf Handelsgilde)
        player.sendMessage("§e/plot price set §7- Handelspreis festlegen §8[Handelsgilde]");
        player.sendMessage("§e/plot price list §7- Preisliste anzeigen §8[Handelsgilde]");

        player.sendMessage("§8§m-----------------------------");
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                 @NotNull String alias, @NotNull String[] args) {

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Hauptcommands
            completions.add("info");
            if (storageSystemEnabled) {
                completions.add("storage");
            }
            if (npcSystemEnabled) {
                completions.add("npc");
            }
            completions.add("gui");
            completions.add("price");  // Handelsgilde-Preisverwaltung
            completions.add("help");

        } else if (args.length == 2) {
            // Subcommand-spezifische Completions
            String subCommand = args[0].toLowerCase();

            switch (subCommand) {
                case "storage" -> {
                    if (storageSystemEnabled) {
                        completions.add("list");
                        completions.add("info");
                        completions.add("setreceiver");
                    }
                }
                case "npc" -> {
                    if (npcSystemEnabled) {
                        completions.add("spawn");
                        completions.add("remove");
                        completions.add("list");
                    }
                }
                case "price" -> {
                    completions.add("set");
                    completions.add("list");
                }
            }
        }

        // Filtern basierend auf aktueller Eingabe
        String currentArg = args[args.length - 1].toLowerCase();
        return completions.stream()
                .filter(c -> c.toLowerCase().startsWith(currentArg))
                .collect(Collectors.toList());
    }
}
