package de.fallenstar.plot.command;

import de.fallenstar.core.exception.ProviderFunctionalityNotFoundException;
import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.provider.PlotProvider;
import de.fallenstar.core.registry.ProviderRegistry;
import de.fallenstar.plot.PlotModule;
import de.fallenstar.plot.slot.MarketPlot;
import de.fallenstar.plot.slot.PlotSlot;
import de.fallenstar.plot.slot.PlotSlotManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.text.MessageFormat;
import java.util.*;

/**
 * Command für Market-Plot Händler-Slot Verwaltung: /plot slots
 *
 * Subcommands:
 * - /plot slots list - Zeigt alle Slots auf dem aktuellen Plot
 * - /plot slots buy - Kauft einen neuen Händler-Slot
 * - /plot slots set <slot-id> - Setzt die Position eines Slots
 * - /plot slots remove <slot-id> - Entfernt einen leeren Slot
 *
 * **Owner-Only:**
 * Alle Slot-Commands erfordern Owner-Rechte auf dem Plot!
 *
 * @author FallenStar
 * @version 1.0
 */
public class PlotSlotCommand {

    private final PlotModule plugin;
    private final ProviderRegistry providers;
    private final PlotSlotManager slotManager;

    // Aktive Slot-Positionierungs-Sessions (Player → Slot-ID)
    private final Map<UUID, UUID> activeSlotSessions = new HashMap<>();

    /**
     * Konstruktor.
     *
     * @param plugin PlotModule-Instanz
     * @param providers ProviderRegistry
     * @param slotManager PlotSlotManager
     */
    public PlotSlotCommand(PlotModule plugin, ProviderRegistry providers, PlotSlotManager slotManager) {
        this.plugin = plugin;
        this.providers = providers;
        this.slotManager = slotManager;
    }

    /**
     * Führt den Command aus.
     *
     * @param player Spieler
     * @param args Command-Argumente
     * @return true wenn erfolgreich
     */
    public boolean execute(Player player, String[] args) {
        // Prüfe ob Spieler auf einem Plot steht
        PlotProvider plotProvider = providers.getPlotProvider();
        Plot plot;

        try {
            plot = plotProvider.getPlot(player.getLocation());
            if (plot == null) {
                player.sendMessage(plugin.getConfig().getString("messages.no-plot",
                        "§cDu stehst auf keinem Plot!"));
                return true;
            }
        } catch (ProviderFunctionalityNotFoundException e) {
            player.sendMessage("§cPlot-System nicht verfügbar!");
            return true;
        }

        // Prüfe Owner-Rechte
        if (!isPlotOwner(player, plot)) {
            player.sendMessage(plugin.getConfig().getString("messages.no-permissions",
                    "§cDu hast keine Berechtigung für diese Aktion!"));
            try {
                String owner = plotProvider.getOwnerName(plot);
                player.sendMessage("§7Besitzer: §e" + owner);
            } catch (Exception e) {
                // Ignoriere Fehler
            }
            return true;
        }

        // Prüfe ob Plot ein Market-Plot ist (hat Market-Type in Towny)
        // Für jetzt: Erstelle MarketPlot on-demand
        MarketPlot marketPlot = slotManager.getOrCreateMarketPlot(plot);

        // Verarbeite Subcommand
        if (args.length == 0 || args[0].equalsIgnoreCase("list")) {
            return handleList(player, marketPlot);
        } else if (args[0].equalsIgnoreCase("buy")) {
            return handleBuy(player, marketPlot);
        } else if (args[0].equalsIgnoreCase("set")) {
            if (args.length < 2) {
                player.sendMessage("§cVerwendung: /plot slots set <slot-nummer>");
                return true;
            }
            return handleSet(player, marketPlot, args[1]);
        } else if (args[0].equalsIgnoreCase("remove")) {
            if (args.length < 2) {
                player.sendMessage("§cVerwendung: /plot slots remove <slot-nummer>");
                return true;
            }
            return handleRemove(player, marketPlot, args[1]);
        } else {
            player.sendMessage("§cUnbekannter Subcommand!");
            player.sendMessage("§7Verfügbar: list, buy, set, remove");
            return true;
        }
    }

    /**
     * /plot slots list - Zeigt alle Slots.
     */
    private boolean handleList(Player player, MarketPlot marketPlot) {
        List<PlotSlot> slots = marketPlot.getTraderSlots();

        player.sendMessage("§6=== Händler-Slots ===");
        player.sendMessage("§7Slots: §e" + marketPlot.getCurrentlyAvailableSlots() + "§7/§e" +
                marketPlot.getMaximumAvailableSlots());
        player.sendMessage("§7Belegt: §e" + slots.stream().filter(PlotSlot::isOccupied).count());
        player.sendMessage("");

        if (slots.isEmpty()) {
            player.sendMessage("§7Keine Slots vorhanden.");
            player.sendMessage("§7Verwende §e/plot slots buy§7 um einen Slot zu kaufen.");
            return true;
        }

        int index = 1;
        for (PlotSlot slot : slots) {
            String status = slot.isOccupied() ? "§c✗ Belegt" : "§a✓ Frei";
            Location loc = slot.getLocation();
            String locStr = String.format("§7[§e%.0f§7, §e%.0f§7, §e%.0f§7]",
                    loc.getX(), loc.getY(), loc.getZ());

            player.sendMessage("§6#" + index + " " + status + " §7- " + locStr);

            if (slot.getAssignedNPC().isPresent()) {
                player.sendMessage("  §7NPC: §e" + slot.getAssignedNPC().get());
            }

            index++;
        }

        player.sendMessage("");
        player.sendMessage("§7Slot kaufen: §e/plot slots buy");
        player.sendMessage("§7Position setzen: §e/plot slots set <nummer>");

        return true;
    }

    /**
     * /plot slots buy - Kauft einen neuen Slot.
     */
    private boolean handleBuy(Player player, MarketPlot marketPlot) {
        // Prüfe ob weitere Slots kaufbar
        if (!marketPlot.canPurchaseMoreSlots()) {
            player.sendMessage(plugin.getConfig().getString("messages.market.slot-limit-reached",
                    "§cDu hast bereits die maximale Anzahl an Slots!"));
            return true;
        }

        // Hole Preis aus Config
        double price = slotManager.getSlotPrice(plugin.getConfig());
        String currency = slotManager.getSlotCurrency(plugin.getConfig());
        String tier = slotManager.getSlotCurrencyTier(plugin.getConfig());

        // TODO: Prüfe Guthaben und ziehe Kosten ab (Economy-Integration)
        // Für jetzt: Einfach Slot erstellen

        // Erstelle Slot an aktueller Position
        Location location = player.getLocation();
        boolean success = slotManager.purchaseSlot(marketPlot, location);

        if (success) {
            // Speichere Config
            plugin.saveConfiguration();

            String message = MessageFormat.format(
                    plugin.getConfig().getString("messages.market.slot-purchased",
                            "§aHändler-Slot erfolgreich gekauft! ({0}/{1})"),
                    marketPlot.getCurrentlyAvailableSlots(),
                    marketPlot.getMaximumAvailableSlots()
            );
            player.sendMessage(message);
            player.sendMessage("§7Preis: §e" + price + " " + currency + " (" + tier + ")");
            player.sendMessage("§7Verwende §e/plot slots list§7 um alle Slots zu sehen.");
            return true;
        } else {
            player.sendMessage("§cFehler beim Erstellen des Slots!");
            return true;
        }
    }

    /**
     * /plot slots set <slot-id> - Startet Slot-Positionierungs-Session.
     */
    private boolean handleSet(Player player, MarketPlot marketPlot, String slotIndexStr) {
        // Parse Slot-Index
        int slotIndex;
        try {
            slotIndex = Integer.parseInt(slotIndexStr);
        } catch (NumberFormatException e) {
            player.sendMessage("§cUngültige Slot-Nummer!");
            return true;
        }

        // Hole Slot
        List<PlotSlot> slots = marketPlot.getTraderSlots();
        if (slotIndex < 1 || slotIndex > slots.size()) {
            player.sendMessage("§cSlot #" + slotIndex + " existiert nicht!");
            player.sendMessage("§7Verfügbare Slots: 1-" + slots.size());
            return true;
        }

        PlotSlot slot = slots.get(slotIndex - 1);

        // Setze Position auf aktuelle Position
        Location newLocation = player.getLocation();
        boolean success = slotManager.setSlotPosition(marketPlot, slot.getSlotId(), newLocation);

        if (success) {
            // Speichere Config
            plugin.saveConfiguration();

            player.sendMessage(plugin.getConfig().getString("messages.market.slot-position-set",
                    "§aSlot-Position wurde festgelegt!"));
            player.sendMessage("§7Slot: §e#" + slotIndex);
            player.sendMessage(String.format("§7Position: §e%.0f§7, §e%.0f§7, §e%.0f",
                    newLocation.getX(), newLocation.getY(), newLocation.getZ()));
            return true;
        } else {
            player.sendMessage("§cFehler beim Setzen der Slot-Position!");
            return true;
        }
    }

    /**
     * /plot slots remove <slot-id> - Entfernt einen Slot.
     */
    private boolean handleRemove(Player player, MarketPlot marketPlot, String slotIndexStr) {
        // Parse Slot-Index
        int slotIndex;
        try {
            slotIndex = Integer.parseInt(slotIndexStr);
        } catch (NumberFormatException e) {
            player.sendMessage("§cUngültige Slot-Nummer!");
            return true;
        }

        // Hole Slot
        List<PlotSlot> slots = marketPlot.getTraderSlots();
        if (slotIndex < 1 || slotIndex > slots.size()) {
            player.sendMessage("§cSlot #" + slotIndex + " existiert nicht!");
            return true;
        }

        PlotSlot slot = slots.get(slotIndex - 1);

        // Prüfe ob Slot belegt
        if (slot.isOccupied()) {
            player.sendMessage(plugin.getConfig().getString("messages.market.slot-occupied",
                    "§cDieser Slot ist belegt und kann nicht entfernt werden!"));
            return true;
        }

        // Entferne Slot
        boolean success = slotManager.removeSlot(marketPlot, slot.getSlotId());

        if (success) {
            // Speichere Config
            plugin.saveConfiguration();

            player.sendMessage(plugin.getConfig().getString("messages.market.slot-removed",
                    "§aHändler-Slot entfernt!"));
            return true;
        } else {
            player.sendMessage("§cFehler beim Entfernen des Slots!");
            return true;
        }
    }

    /**
     * Prüft ob ein Spieler der Besitzer eines Plots ist.
     *
     * @param player Der Spieler
     * @param plot Der Plot
     * @return true wenn Besitzer
     */
    private boolean isPlotOwner(Player player, Plot plot) {
        PlotProvider plotProvider = providers.getPlotProvider();
        try {
            return plotProvider.isOwner(plot, player);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Tab-Completion für /plot slots.
     *
     * @param args Command-Argumente
     * @return Liste möglicher Completions
     */
    public List<String> getTabCompletions(String[] args) {
        if (args.length == 1) {
            return Arrays.asList("list", "buy", "set", "remove");
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("remove"))) {
            return Arrays.asList("1", "2", "3", "4", "5");
        }

        return new ArrayList<>();
    }
}
