package de.fallenstar.plot.command;

import de.fallenstar.core.registry.PlotTypeRegistry;
import de.fallenstar.core.registry.ProviderRegistry;
import de.fallenstar.plot.PlotModule;
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

    // Subcommands
    private final PlotInfoCommand infoCommand;
    private final PlotNpcCommand npcCommand;

    /**
     * Erstellt einen neuen PlotCommand.
     *
     * @param plugin Plugin-Instanz
     * @param providers ProviderRegistry
     * @param plotTypeRegistry PlotTypeRegistry
     * @param plotSystemEnabled Ob Plot-System aktiviert
     * @param townSystemEnabled Ob Town-System aktiviert
     * @param npcSystemEnabled Ob NPC-System aktiviert
     */
    public PlotCommand(
            PlotModule plugin,
            ProviderRegistry providers,
            PlotTypeRegistry plotTypeRegistry,
            boolean plotSystemEnabled,
            boolean townSystemEnabled,
            boolean npcSystemEnabled
    ) {
        this.plugin = plugin;
        this.providers = providers;
        this.plotTypeRegistry = plotTypeRegistry;
        this.plotSystemEnabled = plotSystemEnabled;
        this.townSystemEnabled = townSystemEnabled;
        this.npcSystemEnabled = npcSystemEnabled;

        // Subcommands initialisieren
        this.infoCommand = new PlotInfoCommand(providers, plotTypeRegistry);
        this.npcCommand = new PlotNpcCommand(plugin, providers, plotTypeRegistry);
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
                // TODO: Storage-Commands aus module-storage integrieren
                player.sendMessage("§eStorage-Commands werden noch integriert...");
                return true;
            }

            case "npc" -> {
                if (!npcSystemEnabled) {
                    player.sendMessage("§cNPC-System ist nicht verfügbar (kein NPC-Plugin geladen)!");
                    return true;
                }
                return npcCommand.execute(player, subArgs);
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
     * Sendet Hilfe-Nachricht an Spieler.
     *
     * @param player Der Spieler
     */
    private void sendHelp(Player player) {
        player.sendMessage("§8§m---------§r §6Plot Commands §8§m---------");
        player.sendMessage("§e/plot info §7- Zeigt Plot-Informationen");
        player.sendMessage("§e/plot storage view §7- Zeigt Storage-Übersicht");

        if (npcSystemEnabled) {
            player.sendMessage("§e/plot npc spawn §7- Spawnt NPC auf Plot");
            player.sendMessage("§e/plot npc remove §7- Entfernt NPC auf Plot");
            player.sendMessage("§e/plot npc list §7- Zeigt alle NPCs auf Plot");
        } else {
            player.sendMessage("§7/plot npc §8[deaktiviert - kein NPC-Plugin]");
        }

        player.sendMessage("§8§m-----------------------------");
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                 @NotNull String alias, @NotNull String[] args) {

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Hauptcommands
            completions.add("info");
            completions.add("storage");
            if (npcSystemEnabled) {
                completions.add("npc");
            }
            completions.add("help");

        } else if (args.length == 2) {
            // Subcommand-spezifische Completions
            String subCommand = args[0].toLowerCase();

            switch (subCommand) {
                case "storage" -> {
                    completions.add("view");
                    completions.add("list");
                    completions.add("setreceiver");
                }
                case "npc" -> {
                    if (npcSystemEnabled) {
                        completions.add("spawn");
                        completions.add("remove");
                        completions.add("list");
                    }
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
