package de.fallenstar.core.command;

import de.fallenstar.core.FallenStarCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
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
 * - /fscore admin - Admin-Befehle (erfordert Permission)
 * - /fscore help - Zeigt Hilfe
 *
 * @author FallenStar
 * @version 1.0
 */
public class CoreCommand implements CommandExecutor, TabCompleter {

    private final FallenStarCore plugin;
    private final AdminCommand adminCommand;
    private boolean debugMode;

    public CoreCommand(FallenStarCore plugin) {
        this.plugin = plugin;
        this.adminCommand = new AdminCommand(plugin);
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
            case "admin" -> adminCommand.handleAdminCommand(sender, Arrays.copyOfRange(args, 1, args.length));
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
        sendCommandHelp(sender, "/fscore admin", "Admin-Befehle (erfordert Permission)");
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
            List<String> subCommands = Arrays.asList("status", "reload", "debug", "admin", "help");
            List<String> completions = new ArrayList<>();

            for (String sub : subCommands) {
                if (sub.startsWith(args[0].toLowerCase())) {
                    completions.add(sub);
                }
            }

            return completions;
        }

        // Tab-Completion für admin subcommands
        if (args.length >= 2 && "admin".equalsIgnoreCase(args[0])) {
            return adminCommand.getTabCompletions(Arrays.copyOfRange(args, 1, args.length));
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
