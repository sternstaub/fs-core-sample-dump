package de.fallenstar.plot.command;

import de.fallenstar.core.command.AdminSubcommandHandler;
import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.provider.PlotProvider;
import de.fallenstar.core.registry.ProviderRegistry;
import de.fallenstar.plot.storage.manager.ChestScanService;
import de.fallenstar.plot.storage.model.PlotStorage;
import de.fallenstar.plot.storage.provider.PlotStorageProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Admin-Command-Handler für Plots-Modul.
 *
 * Behandelt alle /fscore admin plots Subcommands:
 * - info: Zeigt Plot-Info am aktuellen Standort
 * - storage view: Zeigt Storage-Materialien
 * - storage scan: Scannt Storage neu
 *
 * Implementiert das AdminSubcommandHandler-Interface für
 * Reflection-freie Inter-Modul-Kommunikation.
 *
 * @author FallenStar
 * @version 1.0
 */
public class PlotsAdminHandler implements AdminSubcommandHandler {

    private final ProviderRegistry providerRegistry;
    private final PlotStorageProvider storageProvider;
    private final ChestScanService scanService;

    /**
     * Erstellt einen neuen PlotsAdminHandler.
     *
     * @param providerRegistry Provider-Registry für PlotProvider-Zugriff
     * @param storageProvider PlotStorageProvider des Plots-Moduls
     * @param scanService ChestScanService des Plots-Moduls
     */
    public PlotsAdminHandler(ProviderRegistry providerRegistry,
                             PlotStorageProvider storageProvider,
                             ChestScanService scanService) {
        this.providerRegistry = providerRegistry;
        this.storageProvider = storageProvider;
        this.scanService = scanService;
    }

    @Override
    public boolean handle(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Dieser Befehl kann nur von Spielern verwendet werden.", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "info" -> handlePlotInfo(player);
            case "storage" -> handlePlotStorage(player, Arrays.copyOfRange(args, 1, args.length));
            default -> {
                sender.sendMessage(Component.text("Unbekannter Plot-Befehl: " + subCommand, NamedTextColor.RED));
                sendHelp(sender);
            }
        }

        return true;
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 0) {
            // First argument: subcommand
            completions.add("info");
            completions.add("storage");
        } else if (args.length == 1 && args[0].equalsIgnoreCase("storage")) {
            // Second argument for storage subcommand
            completions.add("view");
            completions.add("scan");
        }

        return completions;
    }

    @Override
    public void sendHelp(CommandSender sender) {
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
     * Zeigt Plot-Info am aktuellen Standort.
     *
     * @param player Spieler
     */
    private void handlePlotInfo(Player player) {
        try {
            PlotProvider plotProvider = providerRegistry.getPlotProvider();

            if (!plotProvider.isAvailable()) {
                player.sendMessage(Component.text("✗ Plot-System nicht verfügbar!", NamedTextColor.RED));
                return;
            }

            Plot plot = plotProvider.getPlot(player.getLocation());

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
            e.printStackTrace();
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
        try {
            // Hole Plot an Spieler-Position
            PlotProvider plotProvider = providerRegistry.getPlotProvider();

            if (!plotProvider.isAvailable()) {
                player.sendMessage(Component.text("✗ Plot-System nicht verfügbar!", NamedTextColor.RED));
                return;
            }

            Plot plot = plotProvider.getPlot(player.getLocation());

            if (plot == null) {
                player.sendMessage(Component.text("✗ Du stehst nicht auf einem Grundstück!", NamedTextColor.RED));
                return;
            }

            // Hole PlotStorage - KEINE REFLECTION MEHR!
            PlotStorage plotStorage = storageProvider.getPlotStorage(plot);
            Set<Material> materials = plotStorage.getAllMaterials();
            Set<?> chests = plotStorage.getAllChests();

            if (materials.isEmpty()) {
                player.sendMessage(Component.text("✗ Keine Materialien auf diesem Grundstück gefunden!", NamedTextColor.YELLOW));
                player.sendMessage(Component.text("  Plot: ", NamedTextColor.GRAY)
                        .append(Component.text(plot.getIdentifier(), NamedTextColor.WHITE)));
                player.sendMessage(Component.text("  Tipp: Verwende ", NamedTextColor.GRAY)
                        .append(Component.text("/fscore admin plots storage scan", NamedTextColor.GOLD))
                        .append(Component.text(" zum Scannen", NamedTextColor.GRAY)));
                return;
            }

            // Zeige Header
            player.sendMessage(Component.text("╔═══════════════════════════════════════╗", NamedTextColor.GOLD));
            player.sendMessage(Component.text("║  Plot-Storage Materialien            ║", NamedTextColor.GOLD));
            player.sendMessage(Component.text("╚═══════════════════════════════════════╝", NamedTextColor.GOLD));
            player.sendMessage(Component.empty());
            player.sendMessage(Component.text("Plot: ", NamedTextColor.GRAY)
                    .append(Component.text(plot.getIdentifier(), NamedTextColor.WHITE)));
            player.sendMessage(Component.text("Truhen: ", NamedTextColor.GRAY)
                    .append(Component.text(String.valueOf(chests.size()), NamedTextColor.WHITE)));
            player.sendMessage(Component.empty());

            // Zeige alle Materialien - KEINE REFLECTION MEHR!
            int totalTypes = 0;
            for (Material material : materials) {
                int amount = plotStorage.getTotalAmount(material);

                player.sendMessage(Component.text("  " + material.name() + ": ", NamedTextColor.WHITE)
                        .append(Component.text(String.valueOf(amount), NamedTextColor.GREEN)));
                totalTypes++;
            }

            player.sendMessage(Component.empty());
            player.sendMessage(Component.text("Gesamt: ", NamedTextColor.GRAY)
                    .append(Component.text(totalTypes + " Material-Typen", NamedTextColor.WHITE)));

        } catch (Exception e) {
            player.sendMessage(Component.text("✗ Fehler beim Abrufen des Storage: " + e.getMessage(), NamedTextColor.RED));
            e.printStackTrace();
        }
    }

    /**
     * Scannt Storage am aktuellen Plot neu.
     *
     * @param player Spieler
     */
    private void handleStorageScan(Player player) {
        try {
            // Hole Plot an Spieler-Position
            PlotProvider plotProvider = providerRegistry.getPlotProvider();

            if (!plotProvider.isAvailable()) {
                player.sendMessage(Component.text("✗ Plot-System nicht verfügbar!", NamedTextColor.RED));
                return;
            }

            Plot plot = plotProvider.getPlot(player.getLocation());

            if (plot == null) {
                player.sendMessage(Component.text("✗ Du stehst nicht auf einem Grundstück!", NamedTextColor.RED));
                return;
            }

            player.sendMessage(Component.text("╔═══════════════════════════════════════╗", NamedTextColor.GOLD));
            player.sendMessage(Component.text("║  Plot-Storage Scan                    ║", NamedTextColor.GOLD));
            player.sendMessage(Component.text("╚═══════════════════════════════════════╝", NamedTextColor.GOLD));
            player.sendMessage(Component.empty());
            player.sendMessage(Component.text("Scanne Plot: ", NamedTextColor.GRAY)
                    .append(Component.text(plot.getIdentifier(), NamedTextColor.WHITE)));
            player.sendMessage(Component.empty());

            // Hole PlotStorage - KEINE REFLECTION MEHR!
            PlotStorage plotStorage = storageProvider.getPlotStorage(plot);

            player.sendMessage(Component.text("Scanne Truhen...", NamedTextColor.YELLOW));

            // Scan durchführen - KEINE REFLECTION MEHR!
            int chestCount = scanService.scanPlot(plot, plotStorage);

            player.sendMessage(Component.text("✓ Scan abgeschlossen!", NamedTextColor.GREEN));
            player.sendMessage(Component.text("  Gefundene Truhen: ", NamedTextColor.GRAY)
                    .append(Component.text(String.valueOf(chestCount), NamedTextColor.WHITE)));
            player.sendMessage(Component.empty());
            player.sendMessage(Component.text("Verwende ", NamedTextColor.GRAY)
                    .append(Component.text("/fscore admin plots storage view", NamedTextColor.GOLD))
                    .append(Component.text(" zum Anzeigen", NamedTextColor.GRAY)));

        } catch (Exception e) {
            player.sendMessage(Component.text("✗ Fehler beim Scannen: " + e.getMessage(), NamedTextColor.RED));
            e.printStackTrace();
        }
    }
}
