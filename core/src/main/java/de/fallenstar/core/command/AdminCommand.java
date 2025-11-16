package de.fallenstar.core.command;

import de.fallenstar.core.FallenStarCore;
import de.fallenstar.core.registry.UIRegistry;
import de.fallenstar.core.ui.BaseUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Handler für /fscore admin Subcommands.
 *
 * Subcommands:
 * - /fscore admin gui [ui-id] - Öffnet Test-UI
 * - /fscore admin gui list - Zeigt alle registrierten Test-UIs
 * - /fscore admin items - Item-Modul Testbefehle
 * - /fscore admin plots - Plot-Modul Testbefehle
 *
 * Erfordert Permission: fallenstar.core.admin
 *
 * @author FallenStar
 * @version 1.0
 */
public class AdminCommand {

    private final FallenStarCore plugin;
    private static final String PERMISSION = "fallenstar.core.admin";

    /**
     * Konstruktor für AdminCommand.
     *
     * @param plugin FallenStarCore-Instanz
     */
    public AdminCommand(FallenStarCore plugin) {
        this.plugin = plugin;
    }

    /**
     * Behandelt /fscore admin Subcommands.
     *
     * @param sender Command-Sender
     * @param args Argumente (ohne "admin")
     * @return true wenn erfolgreich verarbeitet
     */
    public boolean handleAdminCommand(CommandSender sender, String[] args) {
        // Permission prüfen
        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(Component.text("Keine Berechtigung für Admin-Befehle!", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            sendAdminHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "gui" -> handleGuiCommand(sender, Arrays.copyOfRange(args, 1, args.length));
            case "items" -> handleItemsCommand(sender, Arrays.copyOfRange(args, 1, args.length));
            case "plots" -> handlePlotsCommand(sender, Arrays.copyOfRange(args, 1, args.length));
            default -> {
                sender.sendMessage(Component.text("Unbekannter Admin-Befehl: " + subCommand, NamedTextColor.RED));
                sendAdminHelp(sender);
            }
        }

        return true;
    }

    /**
     * Behandelt /fscore admin gui Subcommands.
     *
     * @param sender Command-Sender
     * @param args Argumente (ohne "gui")
     */
    private void handleGuiCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Dieser Befehl kann nur von Spielern verwendet werden.", NamedTextColor.RED));
            return;
        }

        if (args.length == 0) {
            sender.sendMessage(Component.text("Verwendung: /fscore admin gui <ui-id|list>", NamedTextColor.RED));
            return;
        }

        String subCommand = args[0].toLowerCase();

        if ("list".equals(subCommand)) {
            listRegisteredUIs(sender);
        } else {
            openTestUI(player, subCommand);
        }
    }

    /**
     * Listet alle registrierten Test-UIs auf.
     *
     * @param sender Command-Sender
     */
    private void listRegisteredUIs(CommandSender sender) {
        UIRegistry registry = plugin.getUIRegistry();
        List<UIRegistry.UIRegistration> registrations = registry.getAllRegistrations();

        sender.sendMessage(Component.text("╔═══════════════════════════════════════╗", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("║  Registrierte Test-UIs                ║", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("╚═══════════════════════════════════════╝", NamedTextColor.GOLD));
        sender.sendMessage(Component.empty());

        if (registrations.isEmpty()) {
            sender.sendMessage(Component.text("Keine Test-UIs registriert.", NamedTextColor.YELLOW));
            sender.sendMessage(Component.text("Test-UIs werden von Modulen beim Start registriert.", NamedTextColor.GRAY));
            return;
        }

        for (UIRegistry.UIRegistration registration : registrations) {
            sender.sendMessage(Component.text("  " + registration.id(), NamedTextColor.GOLD, TextDecoration.BOLD)
                    .append(Component.text(" - " + registration.displayName(), NamedTextColor.WHITE))
            );
            sender.sendMessage(Component.text("    " + registration.description(), NamedTextColor.GRAY));
            sender.sendMessage(Component.empty());
        }

        sender.sendMessage(Component.text("Verwendung: ", NamedTextColor.GRAY)
                .append(Component.text("/fscore admin gui <ui-id>", NamedTextColor.YELLOW)));
    }

    /**
     * Öffnet ein Test-UI für einen Spieler.
     *
     * @param player Spieler
     * @param uiId UI-ID
     */
    private void openTestUI(Player player, String uiId) {
        UIRegistry registry = plugin.getUIRegistry();

        if (!registry.isRegistered(uiId)) {
            player.sendMessage(Component.text("Unbekanntes Test-UI: " + uiId, NamedTextColor.RED));
            player.sendMessage(Component.text("Verwende ", NamedTextColor.GRAY)
                    .append(Component.text("/fscore admin gui list", NamedTextColor.YELLOW))
                    .append(Component.text(" für alle verfügbaren UIs.", NamedTextColor.GRAY))
            );
            return;
        }

        BaseUI ui = registry.createTestUI(uiId);

        if (ui == null) {
            player.sendMessage(Component.text("Fehler beim Erstellen des Test-UI!", NamedTextColor.RED));
            plugin.getLogger().severe("Failed to create test UI: " + uiId);
            return;
        }

        try {
            // UI als Event-Listener registrieren (wichtig für Click-Handler!)
            if (ui instanceof org.bukkit.event.Listener) {
                plugin.getServer().getPluginManager().registerEvents((org.bukkit.event.Listener) ui, plugin);
                plugin.getLogger().fine("UI '" + uiId + "' als Event-Listener registriert");
            }

            ui.open(player);
            player.sendMessage(Component.text("✓ Test-UI geöffnet: " + uiId, NamedTextColor.GREEN));
        } catch (Exception e) {
            player.sendMessage(Component.text("Fehler beim Öffnen des UI: " + e.getMessage(), NamedTextColor.RED));
            plugin.getLogger().severe("Failed to open test UI '" + uiId + "': " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Behandelt /fscore admin items Subcommands.
     *
     * @param sender Command-Sender
     * @param args Argumente (ohne "items")
     */
    private void handleItemsCommand(CommandSender sender, String[] args) {
        sender.sendMessage(Component.text("╔═══════════════════════════════════════╗", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("║  Item-Modul Testbefehle              ║", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("╚═══════════════════════════════════════╝", NamedTextColor.GOLD));
        sender.sendMessage(Component.empty());
        sender.sendMessage(Component.text("Item-Testbefehle werden vom Items-Modul bereitgestellt.", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("Stelle sicher, dass FallenStar-Items geladen ist!", NamedTextColor.GRAY));
        sender.sendMessage(Component.empty());
        sender.sendMessage(Component.text("Verfügbare Befehle:", NamedTextColor.WHITE));
        sender.sendMessage(Component.text("  /fscore admin items list [type]", NamedTextColor.GOLD)
                .append(Component.text(" - Zeigt alle MMOItems", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("  /fscore admin items give <type> <id>", NamedTextColor.GOLD)
                .append(Component.text(" - Gibt ein MMOItem", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("  /fscore admin items browse", NamedTextColor.GOLD)
                .append(Component.text(" - Öffnet Item-Browser", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("  /fscore admin items info <type> <id>", NamedTextColor.GOLD)
                .append(Component.text(" - Zeigt Item-Infos", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("  /fscore admin items reload", NamedTextColor.GOLD)
                .append(Component.text(" - Lädt Cache neu", NamedTextColor.GRAY)));
    }

    /**
     * Behandelt /fscore admin plots Subcommands.
     *
     * @param sender Command-Sender
     * @param args Argumente (ohne "plots")
     */
    private void handlePlotsCommand(CommandSender sender, String[] args) {
        // Prüfe ob Plot-Modul geladen ist
        org.bukkit.plugin.Plugin plotModule = plugin.getServer().getPluginManager().getPlugin("FallenStar-Plots");

        if (plotModule == null || !plotModule.isEnabled()) {
            sender.sendMessage(Component.text("✗ FallenStar-Plots Modul nicht geladen!", NamedTextColor.RED));
            sender.sendMessage(Component.text("  Bitte stelle sicher, dass FallenStar-Plots installiert ist.", NamedTextColor.GRAY));
            return;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Dieser Befehl kann nur von Spielern verwendet werden.", NamedTextColor.RED));
            return;
        }

        if (args.length == 0) {
            sendPlotsHelp(sender);
            return;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "info" -> handlePlotInfo(player);
            case "storage" -> handlePlotStorage(player, Arrays.copyOfRange(args, 1, args.length));
            default -> {
                sender.sendMessage(Component.text("Unbekannter Plot-Befehl: " + subCommand, NamedTextColor.RED));
                sendPlotsHelp(sender);
            }
        }
    }

    /**
     * Zeigt Plot-Info am aktuellen Standort.
     *
     * @param player Spieler
     */
    private void handlePlotInfo(Player player) {
        try {
            de.fallenstar.core.provider.PlotProvider plotProvider = plugin.getProviderRegistry().getPlotProvider();

            if (!plotProvider.isAvailable()) {
                player.sendMessage(Component.text("✗ Plot-System nicht verfügbar!", NamedTextColor.RED));
                return;
            }

            de.fallenstar.core.provider.Plot plot = plotProvider.getPlot(player.getLocation());

            if (plot == null) {
                player.sendMessage(Component.text("✗ Kein Plot an dieser Position!", NamedTextColor.RED));
                return;
            }

            // Plot-Informationen anzeigen
            player.sendMessage(Component.text("╔═══════════════════════════════════════╗", NamedTextColor.AQUA));
            player.sendMessage(Component.text("║  Plot-Informationen                   ║", NamedTextColor.AQUA));
            player.sendMessage(Component.text("╚═══════════════════════════════════════╝", NamedTextColor.AQUA));
            player.sendMessage(Component.empty());
            player.sendMessage(Component.text("  ID: ", NamedTextColor.GRAY)
                    .append(Component.text(plot.getUuid().toString().substring(0, 8) + "...", NamedTextColor.WHITE)));
            player.sendMessage(Component.text("  Identifier: ", NamedTextColor.GRAY)
                    .append(Component.text(plot.getIdentifier(), NamedTextColor.WHITE)));
            player.sendMessage(Component.text("  Koordinaten: ", NamedTextColor.GRAY)
                    .append(Component.text(plot.getLocation().getBlockX() + ", " +
                            plot.getLocation().getBlockY() + ", " +
                            plot.getLocation().getBlockZ(), NamedTextColor.WHITE)));
            player.sendMessage(Component.empty());
            player.sendMessage(Component.text("  Hinweis:", NamedTextColor.YELLOW)
                    .append(Component.text(" Verwende ", NamedTextColor.GRAY))
                    .append(Component.text("/plot info", NamedTextColor.GOLD))
                    .append(Component.text(" für detaillierte Infos", NamedTextColor.GRAY)));

        } catch (Exception e) {
            player.sendMessage(Component.text("✗ Fehler beim Abrufen der Plot-Info: " + e.getMessage(), NamedTextColor.RED));
            plugin.getLogger().warning("Error in /fscore admin plots info: " + e.getMessage());
        }
    }

    /**
     * Behandelt /fscore admin plots storage Subcommands.
     *
     * @param player Spieler
     * @param args Argumente (ohne "storage")
     */
    private void handlePlotStorage(Player player, String[] args) {
        if (args.length == 0) {
            player.sendMessage(Component.text("Verwendung:", NamedTextColor.YELLOW));
            player.sendMessage(Component.text("  /fscore admin plots storage view", NamedTextColor.GOLD)
                    .append(Component.text(" - Zeigt Storage-Materialien", NamedTextColor.GRAY)));
            player.sendMessage(Component.text("  /fscore admin plots storage scan", NamedTextColor.GOLD)
                    .append(Component.text(" - Scannt Storage neu", NamedTextColor.GRAY)));
            return;
        }

        String storageCommand = args[0].toLowerCase();

        switch (storageCommand) {
            case "view" -> handleStorageView(player);
            case "scan" -> handleStorageScan(player);
            default -> {
                player.sendMessage(Component.text("Unbekannter Storage-Befehl: " + storageCommand, NamedTextColor.RED));
                player.sendMessage(Component.text("Verwendung: /fscore admin plots storage <view|scan>", NamedTextColor.GRAY));
            }
        }
    }

    /**
     * Zeigt Storage-Materialien am aktuellen Plot.
     *
     * @param player Spieler
     */
    private void handleStorageView(Player player) {
        player.sendMessage(Component.text("╔═══════════════════════════════════════╗", NamedTextColor.GOLD));
        player.sendMessage(Component.text("║  Plot-Storage Materialien            ║", NamedTextColor.GOLD));
        player.sendMessage(Component.text("╚═══════════════════════════════════════╝", NamedTextColor.GOLD));
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("Diese Funktion wird vom Plot-Modul bereitgestellt.", NamedTextColor.YELLOW));
        player.sendMessage(Component.text("Verwende: ", NamedTextColor.GRAY)
                .append(Component.text("/plot storage", NamedTextColor.GOLD))
                .append(Component.text(" für vollständige Storage-Funktionalität", NamedTextColor.GRAY)));
    }

    /**
     * Scannt Storage am aktuellen Plot neu.
     *
     * @param player Spieler
     */
    private void handleStorageScan(Player player) {
        player.sendMessage(Component.text("╔═══════════════════════════════════════╗", NamedTextColor.GOLD));
        player.sendMessage(Component.text("║  Plot-Storage Scan                    ║", NamedTextColor.GOLD));
        player.sendMessage(Component.text("╚═══════════════════════════════════════╝", NamedTextColor.GOLD));
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("Diese Funktion wird vom Plot-Modul bereitgestellt.", NamedTextColor.YELLOW));
        player.sendMessage(Component.text("Verwende: ", NamedTextColor.GRAY)
                .append(Component.text("/plot storage", NamedTextColor.GOLD))
                .append(Component.text(" für vollständige Storage-Funktionalität", NamedTextColor.GRAY)));
    }

    /**
     * Zeigt Plot-Hilfe.
     *
     * @param sender Command-Sender
     */
    private void sendPlotsHelp(CommandSender sender) {
        sender.sendMessage(Component.text("╔═══════════════════════════════════════╗", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("║  Plot-Modul Testbefehle              ║", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("╚═══════════════════════════════════════╝", NamedTextColor.GOLD));
        sender.sendMessage(Component.empty());
        sender.sendMessage(Component.text("Verfügbare Befehle:", NamedTextColor.WHITE));
        sender.sendMessage(Component.text("  /fscore admin plots info", NamedTextColor.GOLD)
                .append(Component.text(" - Zeigt Plot-Info", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("  /fscore admin plots storage view", NamedTextColor.GOLD)
                .append(Component.text(" - Zeigt Storage-Materialien", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("  /fscore admin plots storage scan", NamedTextColor.GOLD)
                .append(Component.text(" - Scannt Storage neu", NamedTextColor.GRAY)));
        sender.sendMessage(Component.empty());
        sender.sendMessage(Component.text("Hinweis:", NamedTextColor.YELLOW)
                .append(Component.text(" Vollständige Plot-Funktionalität via ", NamedTextColor.GRAY))
                .append(Component.text("/plot", NamedTextColor.GOLD)));
    }

    /**
     * Zeigt Admin-Hilfe.
     *
     * @param sender Command-Sender
     */
    private void sendAdminHelp(CommandSender sender) {
        sender.sendMessage(Component.text("╔═══════════════════════════════════════╗", NamedTextColor.AQUA));
        sender.sendMessage(Component.text("║  FallenStar Core - Admin-Befehle     ║", NamedTextColor.AQUA));
        sender.sendMessage(Component.text("╚═══════════════════════════════════════╝", NamedTextColor.AQUA));
        sender.sendMessage(Component.empty());

        sender.sendMessage(Component.text("  /fscore admin gui", NamedTextColor.GOLD)
                .append(Component.text(" - UI-Testbefehle", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("  /fscore admin items", NamedTextColor.GOLD)
                .append(Component.text(" - Item-Testbefehle", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("  /fscore admin plots", NamedTextColor.GOLD)
                .append(Component.text(" - Plot-Testbefehle", NamedTextColor.GRAY)));
        sender.sendMessage(Component.empty());
        sender.sendMessage(Component.text("Verwende ", NamedTextColor.GRAY)
                .append(Component.text("/fscore admin <kategorie>", NamedTextColor.YELLOW))
                .append(Component.text(" für Details.", NamedTextColor.GRAY)));
    }

    /**
     * Tab-Completion für /fscore admin.
     *
     * @param args Aktuelle Argumente
     * @return Liste von Completions
     */
    public List<String> getTabCompletions(String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // /fscore admin <?>
            List<String> subCommands = Arrays.asList("gui", "items", "plots");
            for (String sub : subCommands) {
                if (sub.startsWith(args[0].toLowerCase())) {
                    completions.add(sub);
                }
            }
        } else if (args.length == 2 && "gui".equalsIgnoreCase(args[0])) {
            // /fscore admin gui <?>
            completions.add("list");
            UIRegistry registry = plugin.getUIRegistry();
            for (String id : registry.getRegisteredUIIds()) {
                if (id.startsWith(args[1].toLowerCase())) {
                    completions.add(id);
                }
            }
        } else if (args.length == 2 && "items".equalsIgnoreCase(args[0])) {
            // /fscore admin items <?>
            List<String> itemSubs = Arrays.asList("list", "give", "browse", "info", "reload");
            for (String sub : itemSubs) {
                if (sub.startsWith(args[1].toLowerCase())) {
                    completions.add(sub);
                }
            }
        } else if (args.length == 2 && "plots".equalsIgnoreCase(args[0])) {
            // /fscore admin plots <?>
            List<String> plotSubs = Arrays.asList("info", "storage");
            for (String sub : plotSubs) {
                if (sub.startsWith(args[1].toLowerCase())) {
                    completions.add(sub);
                }
            }
        } else if (args.length == 3 && "plots".equalsIgnoreCase(args[0]) && "storage".equalsIgnoreCase(args[1])) {
            // /fscore admin plots storage <?>
            List<String> storageSubs = Arrays.asList("view", "scan");
            for (String sub : storageSubs) {
                if (sub.startsWith(args[2].toLowerCase())) {
                    completions.add(sub);
                }
            }
        }

        return completions;
    }
}
