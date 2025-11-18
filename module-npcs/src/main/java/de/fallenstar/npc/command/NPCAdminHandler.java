package de.fallenstar.npc.command;

import de.fallenstar.core.command.AdminSubcommandHandler;
import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.provider.PlotProvider;
import de.fallenstar.core.registry.ProviderRegistry;
import de.fallenstar.npc.manager.GuildTraderManager;
import de.fallenstar.npc.manager.NPCManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Admin-Command-Handler für /fscore npc.
 *
 * Subcommands:
 * - /fscore npc spawn guildtrader - Spawnt Gildenhändler am Spieler-Standort
 * - /fscore npc remove <uuid> - Entfernt NPC
 * - /fscore npc list - Zeigt alle NPCs
 * - /fscore npc info <uuid> - Zeigt NPC-Details
 *
 * @author FallenStar
 * @version 1.0
 */
public class NPCAdminHandler implements AdminSubcommandHandler {

    private final NPCManager npcManager;
    private final GuildTraderManager guildTraderManager;
    private final ProviderRegistry providers;

    /**
     * Erstellt einen neuen NPCAdminHandler.
     *
     * @param npcManager NPCManager
     * @param guildTraderManager GuildTraderManager (kann null sein)
     * @param providers ProviderRegistry
     */
    public NPCAdminHandler(
            NPCManager npcManager,
            GuildTraderManager guildTraderManager,
            ProviderRegistry providers
    ) {
        this.npcManager = npcManager;
        this.guildTraderManager = guildTraderManager;
        this.providers = providers;
    }

    @Override
    public boolean handle(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "spawn" -> {
                return handleSpawn(sender, args);
            }
            case "remove" -> {
                return handleRemove(sender, args);
            }
            case "list" -> {
                return handleList(sender);
            }
            case "info" -> {
                return handleInfo(sender, args);
            }
            default -> {
                sender.sendMessage("§cUnbekannter Subcommand: " + subCommand);
                sendHelp(sender);
                return true;
            }
        }
    }

    /**
     * Handhabt /fscore npc spawn <typ>.
     */
    private boolean handleSpawn(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cDieser Befehl kann nur von Spielern ausgeführt werden!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cFehler: /fscore npc spawn <typ>");
            sender.sendMessage("§7Verfügbare Typen: guildtrader");
            return true;
        }

        String npcType = args[1].toLowerCase();

        switch (npcType) {
            case "guildtrader" -> {
                return handleSpawnGuildTrader(player);
            }
            default -> {
                sender.sendMessage("§cUnbekannter NPC-Typ: " + npcType);
                sender.sendMessage("§7Verfügbare Typen: guildtrader");
                return true;
            }
        }
    }

    /**
     * Spawnt einen Gildenhändler.
     */
    private boolean handleSpawnGuildTrader(Player player) {
        if (guildTraderManager == null) {
            player.sendMessage("§cGildenhändler-System nicht verfügbar!");
            player.sendMessage("§7PlotProvider wird benötigt.");
            return true;
        }

        // Hole Plot am Spieler-Standort
        PlotProvider plotProvider = providers.getPlotProvider();
        try {
            Plot plot = plotProvider.getPlot(player.getLocation());
            if (plot == null) {
                player.sendMessage("§cDu stehst nicht auf einem Grundstück!");
                return true;
            }

            // Prüfe Plot-Typ
            String plotType = plotProvider.getPlotType(plot);
            if (plotType == null || !plotType.equalsIgnoreCase("handelsgilde")) {
                player.sendMessage("§cDieser Befehl funktioniert nur auf Handelsgilde-Grundstücken!");
                player.sendMessage("§7Aktueller Plot-Typ: §e" + (plotType != null ? plotType : "unbekannt"));
                return true;
            }

            // Prüfe Owner
            if (!plotProvider.isOwner(plot, player)) {
                player.sendMessage("§cDu musst der Besitzer dieses Grundstücks sein!");
                try {
                    String owner = plotProvider.getOwnerName(plot);
                    player.sendMessage("§7Besitzer: §e" + owner);
                } catch (Exception e) {
                    // Ignoriere
                }
                return true;
            }

            // Spawne Gildenhändler
            UUID npcId = guildTraderManager.spawnGuildTrader(plot, player.getLocation());

            if (npcId != null) {
                player.sendMessage("§a§lGildenhändler gespawnt!");
                player.sendMessage("§7NPC-ID: §e" + npcId);
                player.sendMessage("§7");
                player.sendMessage("§7§oDer NPC erscheint sobald Citizens verfügbar ist.");
            } else {
                player.sendMessage("§cFehler beim Spawnen des Gildenhändlers!");
            }

        } catch (Exception e) {
            player.sendMessage("§cFehler: " + e.getMessage());
            e.printStackTrace();
        }

        return true;
    }

    /**
     * Handhabt /fscore npc remove <uuid>.
     */
    private boolean handleRemove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cFehler: /fscore npc remove <uuid>");
            return true;
        }

        try {
            UUID npcId = UUID.fromString(args[1]);

            // Prüfe ob Gildenhändler
            if (guildTraderManager != null) {
                Plot plot = guildTraderManager.getPlotForTrader(npcId);
                if (plot != null) {
                    boolean removed = guildTraderManager.removeGuildTrader(npcId);
                    if (removed) {
                        sender.sendMessage("§aGildenhändler §e" + npcId + " §aentfernt!");
                        return true;
                    } else {
                        sender.sendMessage("§cFehler beim Entfernen des Gildenhändlers!");
                        return true;
                    }
                }
            }

            // Allgemeiner NPC-Remove
            npcManager.unregisterNPC(npcId);
            sender.sendMessage("§aNPC §e" + npcId + " §aentfernt!");

        } catch (IllegalArgumentException e) {
            sender.sendMessage("§cUngültige UUID: " + args[1]);
        } catch (Exception e) {
            sender.sendMessage("§cFehler: " + e.getMessage());
            e.printStackTrace();
        }

        return true;
    }

    /**
     * Handhabt /fscore npc list.
     */
    private boolean handleList(CommandSender sender) {
        sender.sendMessage("§8§m---------§r §6NPC-Liste §8§m---------");

        // Gildenhändler
        if (guildTraderManager != null) {
            int count = guildTraderManager.getGuildTraderCount();
            sender.sendMessage("§e§lGildenhändler: §7" + count);
        }

        // Weitere NPC-Typen
        var npcTypes = npcManager.getNPCTypes();
        sender.sendMessage("§7");
        sender.sendMessage("§7Registrierte NPC-Typen:");
        for (String typeName : npcTypes.keySet()) {
            sender.sendMessage("  §e- §7" + typeName);
        }

        sender.sendMessage("§8§m--------------------------------------");

        return true;
    }

    /**
     * Handhabt /fscore npc info <uuid>.
     */
    private boolean handleInfo(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cFehler: /fscore npc info <uuid>");
            return true;
        }

        try {
            UUID npcId = UUID.fromString(args[1]);

            sender.sendMessage("§8§m---------§r §6NPC-Info §8§m---------");
            sender.sendMessage("§7UUID: §e" + npcId);

            // Prüfe ob Gildenhändler
            if (guildTraderManager != null) {
                Plot plot = guildTraderManager.getPlotForTrader(npcId);
                if (plot != null) {
                    sender.sendMessage("§7Typ: §eGildenhändler");
                    sender.sendMessage("§7Plot: §e" + plot.getUuid());
                    sender.sendMessage("§8§m--------------------------------------");
                    return true;
                }
            }

            sender.sendMessage("§7Typ: §eUnbekannt");
            sender.sendMessage("§8§m--------------------------------------");

        } catch (IllegalArgumentException e) {
            sender.sendMessage("§cUngültige UUID: " + args[1]);
        } catch (Exception e) {
            sender.sendMessage("§cFehler: " + e.getMessage());
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        if (args.length == 1) {
            // Erste Ebene: spawn, remove, list, info
            return filterCompletions(args[0], Arrays.asList("spawn", "remove", "list", "info"));
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("spawn")) {
                // Zweite Ebene: NPC-Typen
                return filterCompletions(args[1], Arrays.asList("guildtrader"));
            }
            if (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("info")) {
                // UUID-Eingabe - keine Completions
                return List.of();
            }
        }

        return List.of();
    }

    /**
     * Filtert Completions basierend auf Input.
     */
    private List<String> filterCompletions(String input, List<String> options) {
        String lowerInput = input.toLowerCase();
        return options.stream()
                .filter(option -> option.toLowerCase().startsWith(lowerInput))
                .toList();
    }

    @Override
    public void sendHelp(CommandSender sender) {
        sender.sendMessage("§8§m---------§r §6/fscore npc §8§m---------");
        sender.sendMessage("§e/fscore npc spawn <typ> §7- Spawnt NPC");
        sender.sendMessage("§7  Typen: §eguildtrader");
        sender.sendMessage("§e/fscore npc remove <uuid> §7- Entfernt NPC");
        sender.sendMessage("§e/fscore npc list §7- Zeigt alle NPCs");
        sender.sendMessage("§e/fscore npc info <uuid> §7- Zeigt NPC-Details");
        sender.sendMessage("§8§m--------------------------------------");
    }
}
