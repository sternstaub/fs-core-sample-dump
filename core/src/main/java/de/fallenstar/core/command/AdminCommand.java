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
            ui.open(player);
            player.sendMessage(Component.text("✓ Test-UI geöffnet: " + uiId, NamedTextColor.GREEN));
        } catch (Exception e) {
            player.sendMessage(Component.text("Fehler beim Öffnen des UI: " + e.getMessage(), NamedTextColor.RED));
            plugin.getLogger().severe("Failed to open test UI '" + uiId + "': " + e.getMessage());
            e.printStackTrace();
        }
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

        sender.sendMessage(Component.text("  /fscore admin gui list", NamedTextColor.GOLD)
                .append(Component.text(" - Zeigt alle Test-UIs", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("  /fscore admin gui <ui-id>", NamedTextColor.GOLD)
                .append(Component.text(" - Öffnet ein Test-UI", NamedTextColor.GRAY)));
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
            List<String> subCommands = Arrays.asList("gui");
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
        }

        return completions;
    }
}
