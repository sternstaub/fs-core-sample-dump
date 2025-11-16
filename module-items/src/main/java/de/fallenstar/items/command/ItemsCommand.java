package de.fallenstar.items.command;

import de.fallenstar.items.ItemsModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Command-Handler für /fsitems.
 *
 * @author FallenStar
 * @version 1.0
 */
public class ItemsCommand implements CommandExecutor, TabCompleter {

    private final ItemsModule plugin;

    public ItemsCommand(ItemsModule plugin) {
        this.plugin = plugin;
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
            case "currency" -> handleCurrency(sender, Arrays.copyOfRange(args, 1, args.length));
            case "info" -> handleInfo(sender);
            case "reload" -> handleReload(sender);
            case "help" -> sendHelp(sender);
            default -> {
                sender.sendMessage(Component.text("Unbekannter Befehl: " + subCommand, NamedTextColor.RED));
                sendHelp(sender);
            }
        }

        return true;
    }

    private void handleCurrency(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(Component.text("Usage: /fsitems currency <player> <type> <amount>", NamedTextColor.RED));
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(Component.text("Spieler nicht gefunden!", NamedTextColor.RED));
            return;
        }

        String currencyType = args[1];
        int amount = Integer.parseInt(args[2]);

        Optional<ItemStack> currency = plugin.getSpecialItemManager().createCurrency(currencyType, amount);

        if (currency.isPresent()) {
            target.getInventory().addItem(currency.get());
            sender.sendMessage(Component.text("✓ " + amount + "x " + currencyType + " Münzen an " + target.getName() + " gegeben", NamedTextColor.GREEN));
        } else {
            sender.sendMessage(Component.text("Unbekannter Währungstyp: " + currencyType, NamedTextColor.RED));
        }
    }

    private void handleInfo(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Nur Spieler können diesen Befehl verwenden!", NamedTextColor.RED));
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType().isAir()) {
            player.sendMessage(Component.text("Kein Item in der Hand!", NamedTextColor.RED));
            return;
        }

        if (!plugin.getItemProvider().isCustomItem(item)) {
            player.sendMessage(Component.text("Dies ist kein Custom-Item!", NamedTextColor.YELLOW));
            return;
        }

        Optional<String> itemId = plugin.getItemProvider().getItemId(item);
        Optional<String> itemType = plugin.getItemProvider().getItemType(item);

        player.sendMessage(Component.text("═══ Custom Item Info ═══", NamedTextColor.GOLD));
        player.sendMessage(Component.text("ID: ", NamedTextColor.GRAY)
                .append(Component.text(itemId.orElse("Unknown"), NamedTextColor.WHITE)));
        player.sendMessage(Component.text("Type: ", NamedTextColor.GRAY)
                .append(Component.text(itemType.orElse("Unknown"), NamedTextColor.WHITE)));
    }

    private void handleReload(CommandSender sender) {
        plugin.reloadConfig();

        // Invalidiere Provider-Cache
        plugin.getItemProvider().invalidateCache();

        sender.sendMessage(Component.text("✓ Items Module reloaded!", NamedTextColor.GREEN));
        sender.sendMessage(Component.text("  → Category cache invalidated", NamedTextColor.GRAY));
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Component.text("╔══════════════════════════════╗", NamedTextColor.AQUA));
        sender.sendMessage(Component.text("║  FallenStar Items Commands  ║", NamedTextColor.AQUA));
        sender.sendMessage(Component.text("╚══════════════════════════════╝", NamedTextColor.AQUA));
        sender.sendMessage(Component.empty());
        sender.sendMessage(Component.text("  /fsitems currency <player> <type> <amount>", NamedTextColor.GOLD)
                .append(Component.text(" - Gebe Münzen", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("  /fsitems info", NamedTextColor.GOLD)
                .append(Component.text(" - Item-Info", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("  /fsitems reload", NamedTextColor.GOLD)
                .append(Component.text(" - Reload", NamedTextColor.GRAY)));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                  @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return Arrays.asList("currency", "info", "reload", "help");
        }

        if (args.length == 2 && "currency".equalsIgnoreCase(args[0])) {
            return null; // Player names
        }

        if (args.length == 3 && "currency".equalsIgnoreCase(args[0])) {
            return new ArrayList<>(plugin.getSpecialItemManager().getCurrencyTypes());
        }

        return Collections.emptyList();
    }
}
