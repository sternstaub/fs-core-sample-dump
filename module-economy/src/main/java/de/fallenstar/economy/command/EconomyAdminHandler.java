package de.fallenstar.economy.command;

import de.fallenstar.core.command.AdminSubcommandHandler;
import de.fallenstar.core.provider.EconomyProvider;
import de.fallenstar.core.registry.ProviderRegistry;
import de.fallenstar.economy.manager.CurrencyManager;
import de.fallenstar.economy.model.CurrencyItemSet;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Admin-Command-Handler für Economy-Modul.
 *
 * Behandelt alle /fscore admin economy Subcommands:
 * - getcoin: Gibt Münzen aus (kostenlos, Admin-Feature)
 * - withdraw: Zahlt Münzen aus und zieht von Vault-Konto ab
 *
 * Implementiert das AdminSubcommandHandler-Interface für
 * Reflection-freie Inter-Modul-Kommunikation.
 *
 * @author FallenStar
 * @version 1.0
 */
public class EconomyAdminHandler implements AdminSubcommandHandler {

    private final CurrencyManager currencyManager;
    private final ProviderRegistry providerRegistry;

    /**
     * Erstellt einen neuen EconomyAdminHandler.
     *
     * @param currencyManager Currency-Manager des Economy-Moduls
     * @param providerRegistry Provider-Registry für EconomyProvider-Zugriff
     */
    public EconomyAdminHandler(CurrencyManager currencyManager, ProviderRegistry providerRegistry) {
        this.currencyManager = currencyManager;
        this.providerRegistry = providerRegistry;
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
        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);

        switch (subCommand) {
            case "getcoin" -> handleGetCoin(player, subArgs);
            case "withdraw" -> handleWithdraw(player, subArgs);
            default -> {
                sender.sendMessage(Component.text("Unbekannter Economy-Befehl: " + subCommand, NamedTextColor.RED));
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
            completions.add("getcoin");
            completions.add("withdraw");
        } else if (args.length == 1) {
            // Second argument: currency name
            completions.addAll(currencyManager.getCurrencyIds());
        } else if (args.length == 2) {
            // Third argument: tier
            completions.add("bronze");
            completions.add("silver");
            completions.add("gold");
        } else if (args.length == 3) {
            // Fourth argument: amount
            completions.add("1");
            completions.add("5");
            completions.add("10");
            completions.add("64");
        }

        return completions;
    }

    @Override
    public void sendHelp(CommandSender sender) {
        sender.sendMessage(Component.text("╔═══════════════════════════════════════╗", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("║  Economy-Modul Testbefehle           ║", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("╚═══════════════════════════════════════╝", NamedTextColor.GOLD));
        sender.sendMessage(Component.empty());
        sender.sendMessage(Component.text("Verfügbare Befehle:", NamedTextColor.WHITE));
        sender.sendMessage(Component.text("  /fscore admin economy getcoin <währung> [tier] [anzahl]", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("    Gibt Münzen an den Spieler (kostenlos)", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("  /fscore admin economy withdraw <währung> [tier] [anzahl]", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("    Zahlt Münzen aus (zieht von Vault-Konto ab)", NamedTextColor.GRAY));
        sender.sendMessage(Component.empty());
        sender.sendMessage(Component.text("Beispiele:", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("  /fscore admin economy getcoin sterne bronze 10", NamedTextColor.GOLD)
                .append(Component.text(" (kostenlos)", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("  /fscore admin economy withdraw sterne silver 5", NamedTextColor.GOLD)
                .append(Component.text(" (zahlt 50 aus Vault)", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("  /fscore admin economy withdraw sterne gold", NamedTextColor.GOLD)
                .append(Component.text(" (zahlt 100 aus Vault)", NamedTextColor.GRAY)));
        sender.sendMessage(Component.empty());
        sender.sendMessage(Component.text("Tiers:", NamedTextColor.YELLOW)
                .append(Component.text(" bronze (1er), silver (10er), gold (100er)", NamedTextColor.GRAY)));
    }

    /**
     * Behandelt /fscore admin economy getcoin.
     *
     * @param player Spieler
     * @param args Argumente: <währungsname> [bronze/silver/gold] [anzahl]
     */
    private void handleGetCoin(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(Component.text("Verwendung: /fscore admin economy getcoin <währungsname> [bronze/silver/gold] [anzahl]", NamedTextColor.RED));
            player.sendMessage(Component.text("Beispiel: /fscore admin economy getcoin sterne gold 10", NamedTextColor.GRAY));
            return;
        }

        String currencyName = args[0].toLowerCase();
        String tierName = args.length > 1 ? args[1].toLowerCase() : "bronze";
        int amount = 1;

        if (args.length > 2) {
            try {
                amount = Integer.parseInt(args[2]);
                if (amount <= 0 || amount > 64) {
                    player.sendMessage(Component.text("Menge muss zwischen 1 und 64 liegen!", NamedTextColor.RED));
                    return;
                }
            } catch (NumberFormatException e) {
                player.sendMessage(Component.text("Ungültige Anzahl: " + args[2], NamedTextColor.RED));
                return;
            }
        }

        // Parse Tier
        CurrencyItemSet.CurrencyTier tier = CurrencyItemSet.CurrencyTier.fromString(tierName);
        if (tier == null) {
            player.sendMessage(Component.text("Ungültiger Tier: " + tierName, NamedTextColor.RED));
            player.sendMessage(Component.text("Verfügbar: bronze, silver, gold", NamedTextColor.GRAY));
            return;
        }

        // Payout coins
        boolean success = currencyManager.payoutCoins(player, currencyName, tier, amount);

        if (success) {
            player.sendMessage(Component.text("✓ ", NamedTextColor.GREEN)
                    .append(Component.text(amount + "x ", NamedTextColor.WHITE))
                    .append(Component.text(tierName.toUpperCase(), NamedTextColor.GOLD))
                    .append(Component.text(" " + currencyName.toUpperCase() + " ausgezahlt!", NamedTextColor.WHITE)));
        } else {
            player.sendMessage(Component.text("✗ Währung nicht gefunden: " + currencyName, NamedTextColor.RED));
            player.sendMessage(Component.text("Tipp: Verwende 'sterne' für die Basiswährung", NamedTextColor.GRAY));
        }
    }

    /**
     * Behandelt /fscore admin economy withdraw.
     *
     * @param player Spieler
     * @param args Argumente: <währungsname> [bronze/silver/gold] [anzahl]
     */
    private void handleWithdraw(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(Component.text("Verwendung: /fscore admin economy withdraw <währungsname> [bronze/silver/gold] [anzahl]", NamedTextColor.RED));
            player.sendMessage(Component.text("Beispiel: /fscore admin economy withdraw sterne gold 10", NamedTextColor.GRAY));
            player.sendMessage(Component.text("Hinweis: Zieht Geld von deinem Vault-Konto ab!", NamedTextColor.YELLOW));
            return;
        }

        String currencyName = args[0].toLowerCase();
        String tierName = args.length > 1 ? args[1].toLowerCase() : "bronze";
        int amount = 1;

        if (args.length > 2) {
            try {
                amount = Integer.parseInt(args[2]);
                if (amount <= 0 || amount > 64) {
                    player.sendMessage(Component.text("Menge muss zwischen 1 und 64 liegen!", NamedTextColor.RED));
                    return;
                }
            } catch (NumberFormatException e) {
                player.sendMessage(Component.text("Ungültige Anzahl: " + args[2], NamedTextColor.RED));
                return;
            }
        }

        // Parse Tier
        CurrencyItemSet.CurrencyTier tier = CurrencyItemSet.CurrencyTier.fromString(tierName);
        if (tier == null) {
            player.sendMessage(Component.text("Ungültiger Tier: " + tierName, NamedTextColor.RED));
            player.sendMessage(Component.text("Verfügbar: bronze, silver, gold", NamedTextColor.GRAY));
            return;
        }

        // Show balance before withdrawal
        try {
            EconomyProvider economyProvider = providerRegistry.getEconomyProvider();
            if (economyProvider.isAvailable()) {
                double balance = economyProvider.getBalance(player);
                player.sendMessage(Component.text("Aktuelles Guthaben: ", NamedTextColor.GRAY)
                        .append(Component.text(String.format("%.2f", balance), NamedTextColor.GOLD)));
            }
        } catch (Exception ignored) {
            // Balance display is optional
        }

        // Withdraw coins
        int actualAmount = currencyManager.withdrawCoins(player, currencyName, tier, amount);

        if (actualAmount > 0) {
            if (actualAmount < amount) {
                player.sendMessage(Component.text("⚠ ", NamedTextColor.YELLOW)
                        .append(Component.text("Nicht genug Guthaben für " + amount + " Münzen!", NamedTextColor.WHITE)));
                player.sendMessage(Component.text("✓ ", NamedTextColor.GREEN)
                        .append(Component.text(actualAmount + "x ", NamedTextColor.WHITE))
                        .append(Component.text(tierName.toUpperCase(), NamedTextColor.GOLD))
                        .append(Component.text(" " + currencyName.toUpperCase() + " ausgezahlt!", NamedTextColor.WHITE)));
            } else {
                player.sendMessage(Component.text("✓ ", NamedTextColor.GREEN)
                        .append(Component.text(actualAmount + "x ", NamedTextColor.WHITE))
                        .append(Component.text(tierName.toUpperCase(), NamedTextColor.GOLD))
                        .append(Component.text(" " + currencyName.toUpperCase() + " ausgezahlt!", NamedTextColor.WHITE)));
            }

            // Show new balance
            try {
                EconomyProvider economyProvider = providerRegistry.getEconomyProvider();
                if (economyProvider.isAvailable()) {
                    double newBalance = economyProvider.getBalance(player);
                    player.sendMessage(Component.text("Neues Guthaben: ", NamedTextColor.GRAY)
                            .append(Component.text(String.format("%.2f", newBalance), NamedTextColor.GOLD)));
                }
            } catch (Exception ignored) {
                // Balance display is optional
            }
        } else {
            player.sendMessage(Component.text("✗ Auszahlung fehlgeschlagen!", NamedTextColor.RED));
            player.sendMessage(Component.text("Mögliche Gründe:", NamedTextColor.GRAY));
            player.sendMessage(Component.text("  - Nicht genug Guthaben", NamedTextColor.GRAY));
            player.sendMessage(Component.text("  - Währung nicht gefunden: " + currencyName, NamedTextColor.GRAY));
            player.sendMessage(Component.text("  - Economy-System nicht verfügbar", NamedTextColor.GRAY));
        }
    }
}
