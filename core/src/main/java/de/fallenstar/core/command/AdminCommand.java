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
import java.util.Set;

/**
 * Handler für /fscore admin Subcommands.
 *
 * Subcommands:
 * - /fscore admin gui [ui-id] - Öffnet Test-UI
 * - /fscore admin gui list - Zeigt alle registrierten Test-UIs
 * - /fscore admin items - Item-Modul Testbefehle
 * - /fscore admin plots - Plot-Modul Testbefehle
 * - /fscore admin economy - Economy-Modul Testbefehle
 * - /fscore admin npcs - NPC-Roadmap (Sprint 13-14)
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
     * Holt die AdminCommandRegistry (lazy-loading).
     *
     * @return AdminCommandRegistry oder null
     */
    private de.fallenstar.core.registry.AdminCommandRegistry getAdminRegistry() {
        return plugin.getAdminCommandRegistry();
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
            case "economy" -> handleEconomyCommand(sender, Arrays.copyOfRange(args, 1, args.length));
            case "npc", "npcs" -> handleNpcsCommand(sender, Arrays.copyOfRange(args, 1, args.length));
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
     * Delegiert an den registrierten PlotsAdminHandler (aus Plots-Modul).
     *
     * @param sender Command-Sender
     * @param args Argumente (ohne "plots")
     */
    private void handlePlotsCommand(CommandSender sender, String[] args) {
        // Delegiere an registrierten Handler
        de.fallenstar.core.registry.AdminCommandRegistry registry = getAdminRegistry();
        if (registry == null) {
            sender.sendMessage(Component.text("✗ Admin-Command-System noch nicht bereit!", NamedTextColor.RED));
            return;
        }

        registry.getHandler("plots").ifPresentOrElse(
            handler -> handler.handle(sender, args),
            () -> {
                sender.sendMessage(Component.text("✗ FallenStar-Plots Modul nicht geladen!", NamedTextColor.RED));
                sender.sendMessage(Component.text("  Bitte stelle sicher, dass FallenStar-Plots installiert ist.", NamedTextColor.GRAY));
            }
        );
    }

    /**
     * Behandelt /fscore admin economy Subcommands.
     *
     * Delegiert an den registrierten EconomyAdminHandler (aus Economy-Modul).
     *
     * @param sender Command-Sender
     * @param args Argumente (ohne "economy")
     */
    private void handleEconomyCommand(CommandSender sender, String[] args) {
        // Delegiere an registrierten Handler
        de.fallenstar.core.registry.AdminCommandRegistry registry = getAdminRegistry();
        if (registry == null) {
            sender.sendMessage(Component.text("✗ Admin-Command-System noch nicht bereit!", NamedTextColor.RED));
            return;
        }

        registry.getHandler("economy").ifPresentOrElse(
            handler -> handler.handle(sender, args),
            () -> {
                sender.sendMessage(Component.text("✗ FallenStar-Economy Modul nicht geladen!", NamedTextColor.RED));
                sender.sendMessage(Component.text("  Bitte stelle sicher, dass FallenStar-Economy installiert ist.", NamedTextColor.GRAY));
            }
        );
    }

    /**
     * Behandelt /fscore admin npc Subcommands.
     *
     * Delegiert an den registrierten NPCAdminHandler (aus NPC-Modul).
     *
     * @param sender Command-Sender
     * @param args Argumente (ohne "npc")
     */
    private void handleNpcsCommand(CommandSender sender, String[] args) {
        // Delegiere an registrierten Handler
        de.fallenstar.core.registry.AdminCommandRegistry registry = getAdminRegistry();
        if (registry == null) {
            sender.sendMessage(Component.text("✗ Admin-Command-System noch nicht bereit!", NamedTextColor.RED));
            return;
        }

        registry.getHandler("npc").ifPresentOrElse(
            handler -> handler.handle(sender, args),
            () -> {
                sender.sendMessage(Component.text("✗ FallenStar-NPCs Modul nicht geladen!", NamedTextColor.RED));
                sender.sendMessage(Component.text("  Bitte stelle sicher, dass FallenStar-NPCs installiert ist.", NamedTextColor.GRAY));
                sender.sendMessage(Component.text("  Und dass Citizens 2.x Plugin verfügbar ist!", NamedTextColor.GRAY));
            }
        );
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
        sender.sendMessage(Component.text("  /fscore admin economy", NamedTextColor.GOLD)
                .append(Component.text(" - Economy-Testbefehle", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("  /fscore admin npcs", NamedTextColor.GOLD)
                .append(Component.text(" - NPC-Roadmap (Sprint 13-14)", NamedTextColor.GRAY)));
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
            List<String> subCommands = Arrays.asList("gui", "items", "plots", "economy", "npcs");
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
        } else if (args.length == 2 && "economy".equalsIgnoreCase(args[0])) {
            // /fscore admin economy <?>
            List<String> economySubs = Arrays.asList("getcoin", "withdraw");
            for (String sub : economySubs) {
                if (sub.startsWith(args[1].toLowerCase())) {
                    completions.add(sub);
                }
            }
        } else if (args.length == 3 && "economy".equalsIgnoreCase(args[0]) &&
                   ("getcoin".equalsIgnoreCase(args[1]) || "withdraw".equalsIgnoreCase(args[1]))) {
            // /fscore admin economy [getcoin|withdraw] <?>
            completions.add("sterne");
        } else if (args.length == 4 && "economy".equalsIgnoreCase(args[0]) &&
                   ("getcoin".equalsIgnoreCase(args[1]) || "withdraw".equalsIgnoreCase(args[1]))) {
            // /fscore admin economy [getcoin|withdraw] <währung> <?>
            List<String> tiers = Arrays.asList("bronze", "silver", "gold");
            for (String tier : tiers) {
                if (tier.startsWith(args[3].toLowerCase())) {
                    completions.add(tier);
                }
            }
        } else if (args.length == 5 && "economy".equalsIgnoreCase(args[0]) &&
                   ("getcoin".equalsIgnoreCase(args[1]) || "withdraw".equalsIgnoreCase(args[1]))) {
            // /fscore admin economy [getcoin|withdraw] <währung> <tier> <?>
            completions.add("1");
            completions.add("10");
            completions.add("64");
        }

        return completions;
    }
}
