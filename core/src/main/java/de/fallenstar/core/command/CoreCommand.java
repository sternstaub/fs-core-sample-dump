package de.fallenstar.core.command;

import de.fallenstar.core.FallenStarCore;
import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.provider.PlotProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Hauptkommando-Handler für /fscore.
 *
 * Subcommands:
 * - /fscore status - Zeigt Provider-Status und System-Info
 * - /fscore reload - Lädt Config und Provider neu
 * - /fscore debug - Toggle Debug-Modus
 * - /fscore help - Zeigt Hilfe
 *
 * @author FallenStar
 * @version 1.0
 */
public class CoreCommand implements CommandExecutor, TabCompleter {

    private final FallenStarCore plugin;
    private boolean debugMode;

    public CoreCommand(FallenStarCore plugin) {
        this.plugin = plugin;
        this.debugMode = false;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "status" -> handleStatus(sender);
            case "reload" -> handleReload(sender);
            case "debug" -> handleDebug(sender);
            case "plotstorage" -> handlePlotStorage(sender, Arrays.copyOfRange(args, 1, args.length));
            case "help" -> sendHelp(sender);
            default -> {
                sender.sendMessage(Component.text("Unbekannter Befehl: " + subCommand, NamedTextColor.RED));
                sendHelp(sender);
            }
        }

        return true;
    }

    /**
     * Zeigt Status-Informationen.
     */
    private void handleStatus(CommandSender sender) {
        sender.sendMessage(Component.text("╔═══════════════════════════════════╗", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("║  FallenStar Core - Status         ║", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("╚═══════════════════════════════════╝", NamedTextColor.GOLD));

        // Version
        sender.sendMessage(Component.text("Version: ", NamedTextColor.GRAY)
                .append(Component.text("1.0-SNAPSHOT", NamedTextColor.WHITE)));

        // DataStore
        String dbType = plugin.getConfig().getString("database.type", "sqlite");
        sender.sendMessage(Component.text("DataStore: ", NamedTextColor.GRAY)
                .append(Component.text(dbType.toUpperCase(), NamedTextColor.GREEN)));

        // Debug Mode
        sender.sendMessage(Component.text("Debug Mode: ", NamedTextColor.GRAY)
                .append(Component.text(debugMode ? "AN" : "AUS",
                        debugMode ? NamedTextColor.GREEN : NamedTextColor.RED)));

        sender.sendMessage(Component.empty());
        sender.sendMessage(Component.text("Provider-Status:", NamedTextColor.YELLOW, TextDecoration.BOLD));

        // Provider Status
        var registry = plugin.getProviderRegistry();

        sendProviderStatus(sender, "Plot", registry.getPlotProvider().isAvailable());
        sendProviderStatus(sender, "Economy", registry.getEconomyProvider().isAvailable());
        sendProviderStatus(sender, "NPC", registry.getNpcProvider().isAvailable());
        sendProviderStatus(sender, "Item", registry.getItemProvider().isAvailable());
        sendProviderStatus(sender, "Chat", registry.getChatProvider().isAvailable());
        sendProviderStatus(sender, "Network", registry.getNetworkProvider().isAvailable());
    }

    /**
     * Hilfsmethode für Provider-Status-Anzeige.
     */
    private void sendProviderStatus(CommandSender sender, String name, boolean available) {
        Component status = available
                ? Component.text("✓", NamedTextColor.GREEN)
                : Component.text("✗", NamedTextColor.RED);

        sender.sendMessage(Component.text("  ")
                .append(status)
                .append(Component.text(" " + name + " Provider", NamedTextColor.GRAY)));
    }

    /**
     * Lädt Config und Provider neu.
     */
    private void handleReload(CommandSender sender) {
        sender.sendMessage(Component.text("Lade FallenStar Core neu...", NamedTextColor.YELLOW));

        try {
            // Config neu laden
            plugin.reloadConfig();
            sender.sendMessage(Component.text("✓ Config neu geladen", NamedTextColor.GREEN));

            // Provider neu erkennen
            plugin.getProviderRegistry().detectAndRegister();
            sender.sendMessage(Component.text("✓ Provider neu erkannt", NamedTextColor.GREEN));

            sender.sendMessage(Component.text("Reload abgeschlossen!", NamedTextColor.GREEN, TextDecoration.BOLD));

        } catch (Exception e) {
            sender.sendMessage(Component.text("✗ Fehler beim Reload: " + e.getMessage(), NamedTextColor.RED));
            plugin.getLogger().severe("Reload failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Toggle Debug-Modus.
     */
    private void handleDebug(CommandSender sender) {
        debugMode = !debugMode;

        if (debugMode) {
            sender.sendMessage(Component.text("✓ Debug-Modus aktiviert", NamedTextColor.GREEN));
            plugin.getLogger().info("Debug mode enabled by " + sender.getName());
        } else {
            sender.sendMessage(Component.text("✓ Debug-Modus deaktiviert", NamedTextColor.YELLOW));
            plugin.getLogger().info("Debug mode disabled by " + sender.getName());
        }
    }

    /**
     * Handler für /fscore plotstorage Subcommands.
     */
    private void handlePlotStorage(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("Dieser Befehl kann nur von Spielern verwendet werden.", NamedTextColor.RED));
            return;
        }

        if (args.length == 0) {
            sender.sendMessage(Component.text("Verwendung: /fscore plotstorage view", NamedTextColor.RED));
            return;
        }

        Player player = (Player) sender;
        String subCommand = args[0].toLowerCase();

        if ("view".equals(subCommand)) {
            handlePlotStorageView(player);
        } else {
            sender.sendMessage(Component.text("Unbekannter Subcommand: " + subCommand, NamedTextColor.RED));
            sender.sendMessage(Component.text("Verwendung: /fscore plotstorage view", NamedTextColor.YELLOW));
        }
    }

    /**
     * Zeigt die registrierten Materialien auf dem aktuellen Grundstück.
     */
    private void handlePlotStorageView(Player player) {
        try {
            // Hole PlotProvider
            PlotProvider plotProvider = plugin.getProviderRegistry().getPlotProvider();

            if (!plotProvider.isAvailable()) {
                player.sendMessage(Component.text("PlotProvider nicht verfügbar!", NamedTextColor.RED));
                return;
            }

            // Hole Plot an Spieler-Position
            Plot plot = plotProvider.getPlot(player.getLocation());

            if (plot == null) {
                player.sendMessage(Component.text("Du stehst nicht auf einem Grundstück!", NamedTextColor.RED));
                return;
            }

            // Prüfe ob Storage-Modul geladen ist
            Plugin storagePlugin = Bukkit.getPluginManager().getPlugin("FallenStar-Storage");

            if (storagePlugin == null || !storagePlugin.isEnabled()) {
                player.sendMessage(Component.text("Storage-Modul ist nicht geladen!", NamedTextColor.RED));
                return;
            }

            // Greife auf StorageModule zu via Reflection (oder API)
            try {
                Object storageModule = storagePlugin;
                Object storageProvider = storageModule.getClass().getMethod("getStorageProvider").invoke(storageModule);
                Object plotStorage = storageProvider.getClass().getMethod("getPlotStorage", Plot.class).invoke(storageProvider, plot);

                // Hole alle Materialien
                @SuppressWarnings("unchecked")
                java.util.Set<org.bukkit.Material> materials =
                    (java.util.Set<org.bukkit.Material>) plotStorage.getClass().getMethod("getAllMaterials").invoke(plotStorage);

                // Zeige Header
                player.sendMessage(Component.text("╔═══════════════════════════════════════╗", NamedTextColor.GOLD));
                player.sendMessage(Component.text("║  Plot Storage: " + plot.getIdentifier(), NamedTextColor.GOLD));
                player.sendMessage(Component.text("╚═══════════════════════════════════════╝", NamedTextColor.GOLD));
                player.sendMessage(Component.empty());

                if (materials.isEmpty()) {
                    player.sendMessage(Component.text("Keine Materialien gefunden.", NamedTextColor.YELLOW));
                    return;
                }

                // Zeige alle Materialien
                for (org.bukkit.Material material : materials) {
                    int amount = (int) plotStorage.getClass().getMethod("getTotalAmount", org.bukkit.Material.class)
                                                  .invoke(plotStorage, material);

                    player.sendMessage(Component.text("  ", NamedTextColor.WHITE)
                        .append(Component.text(material.name(), NamedTextColor.WHITE))
                        .append(Component.text(": ", NamedTextColor.GRAY))
                        .append(Component.text(String.valueOf(amount), NamedTextColor.GREEN)));
                }

                player.sendMessage(Component.empty());
                player.sendMessage(Component.text("Gesamt: " + materials.size() + " Material-Typen", NamedTextColor.GRAY));

            } catch (Exception e) {
                player.sendMessage(Component.text("Fehler beim Zugriff auf Storage: " + e.getMessage(), NamedTextColor.RED));
                plugin.getLogger().warning("PlotStorage view failed: " + e.getMessage());
                e.printStackTrace();
            }

        } catch (Exception e) {
            player.sendMessage(Component.text("Fehler: " + e.getMessage(), NamedTextColor.RED));
            plugin.getLogger().warning("PlotStorage command failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Zeigt Hilfe-Text.
     */
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Component.text("╔═══════════════════════════════════╗", NamedTextColor.AQUA));
        sender.sendMessage(Component.text("║  FallenStar Core - Befehle        ║", NamedTextColor.AQUA));
        sender.sendMessage(Component.text("╚═══════════════════════════════════╝", NamedTextColor.AQUA));
        sender.sendMessage(Component.empty());

        sendCommandHelp(sender, "/fscore status", "Zeigt Provider-Status und System-Info");
        sendCommandHelp(sender, "/fscore reload", "Lädt Config und Provider neu");
        sendCommandHelp(sender, "/fscore debug", "Toggle Debug-Modus");
        sendCommandHelp(sender, "/fscore plotstorage view", "Zeigt Materialien auf dem aktuellen Plot");
        sendCommandHelp(sender, "/fscore help", "Zeigt diese Hilfe");
    }

    /**
     * Hilfsmethode für Command-Help-Anzeige.
     */
    private void sendCommandHelp(CommandSender sender, String command, String description) {
        sender.sendMessage(Component.text("  " + command, NamedTextColor.GOLD)
                .append(Component.text(" - " + description, NamedTextColor.GRAY)));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                  @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> subCommands = Arrays.asList("status", "reload", "debug", "plotstorage", "help");
            List<String> completions = new ArrayList<>();

            for (String sub : subCommands) {
                if (sub.startsWith(args[0].toLowerCase())) {
                    completions.add(sub);
                }
            }

            return completions;
        }

        // Tab-Completion für plotstorage subcommands
        if (args.length == 2 && "plotstorage".equalsIgnoreCase(args[0])) {
            List<String> subCommands = Arrays.asList("view");
            List<String> completions = new ArrayList<>();

            for (String sub : subCommands) {
                if (sub.startsWith(args[1].toLowerCase())) {
                    completions.add(sub);
                }
            }

            return completions;
        }

        return new ArrayList<>();
    }

    /**
     * Getter für Debug-Mode (für andere Klassen).
     */
    public boolean isDebugMode() {
        return debugMode;
    }
}
