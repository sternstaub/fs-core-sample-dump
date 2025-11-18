package de.fallenstar.plot.command;

import de.fallenstar.core.provider.EconomyProvider;
import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.provider.PlotProvider;
import de.fallenstar.core.registry.ProviderRegistry;
import de.fallenstar.plot.gui.PriceEditorContext;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Command-Handler für /plot price.
 *
 * Verwaltet Handelspreise auf Handelsgilde-Grundstücken.
 *
 * Subcommands:
 * - /plot price set - Aktiviert Interaktionsmodus zum Preisfestlegen
 * - /plot price list - Zeigt alle gesetzten Preise auf diesem Plot
 *
 * @author FallenStar
 * @version 1.0
 */
public class PlotPriceCommand {

    private final ProviderRegistry providers;
    private final java.util.logging.Logger logger;

    /**
     * Spieler die aktuell im Preis-Setzungs-Modus sind.
     * Map: Player UUID → Plot Location String
     */
    private final Map<UUID, String> activePriceSetMode;

    /**
     * Aktive Preis-Editor-Sessions.
     * Map: Session UUID → PriceEditorContext
     */
    private final Map<UUID, PriceEditorContext> activeSessions;

    /**
     * Mapping: Player UUID → Session UUID
     */
    private final Map<UUID, UUID> playerSessions;

    /**
     * Erstellt einen neuen PlotPriceCommand.
     *
     * @param providers ProviderRegistry
     * @param logger Logger für Debug-Ausgaben
     */
    public PlotPriceCommand(ProviderRegistry providers, java.util.logging.Logger logger) {
        this.providers = providers;
        this.logger = logger;
        this.activePriceSetMode = new HashMap<>();
        this.activeSessions = new HashMap<>();
        this.playerSessions = new HashMap<>();
    }

    /**
     * Führt den Price-Command aus.
     *
     * @param player Der Spieler
     * @param args Command-Argumente (set/list)
     * @return true wenn erfolgreich
     */
    public boolean execute(Player player, String[] args) {
        // Prüfe ob Spieler auf Handelsgilde-Plot steht
        Plot plot = getCurrentPlot(player);
        if (plot == null) {
            player.sendMessage("§cDu stehst nicht auf einem Grundstück!");
            return true;
        }

        PlotProvider plotProvider = providers.getPlotProvider();

        // Prüfe Plot-Typ
        String plotType;
        try {
            plotType = plotProvider.getPlotType(plot);
        } catch (Exception e) {
            player.sendMessage("§cFehler beim Abrufen des Plot-Typs!");
            return true;
        }

        if (plotType == null || !plotType.equalsIgnoreCase("handelsgilde")) {
            player.sendMessage("§cDieser Befehl funktioniert nur auf Handelsgilde-Grundstücken!");
            player.sendMessage("§7Aktueller Plot-Typ: §e" + (plotType != null ? plotType : "unbekannt"));
            return true;
        }

        // Keine Argumente -> Hilfe
        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        // /plot price list ist für ALLE sichtbar
        if (subCommand.equals("list")) {
            handleListPrices(player, plot);
            return true;
        }

        // ALLE anderen Befehle erfordern Besitzer-Rechte
        if (!isPlotOwner(player, plot)) {
            player.sendMessage("§cDu musst der Besitzer dieses Grundstücks sein!");
            try {
                String owner = plotProvider.getOwnerName(plot);
                player.sendMessage("§7Besitzer: §e" + owner);
            } catch (Exception e) {
                // Ignoriere Fehler
            }
            return true;
        }

        // Owner-exklusive Befehle
        switch (subCommand) {
            case "set" -> handleSetPrice(player, plot);
            case "adjust" -> {
                if (args.length < 3) {
                    player.sendMessage("§cFehler: /plot price adjust <sessionId> <amount>");
                    return true;
                }
                return handleAdjustPrice(player, args);
            }
            case "confirm" -> {
                if (args.length < 2) {
                    player.sendMessage("§cFehler: /plot price confirm <sessionId>");
                    return true;
                }
                return handleConfirmPrice(player, args[1]);
            }
            case "cancel" -> {
                if (args.length < 2) {
                    // Ohne SessionId - Abbruch des Preis-Setzungs-Modus
                    cancelPriceSetMode(player);
                    return true;
                }
                return handleCancelPrice(player, args[1]);
            }
            default -> {
                player.sendMessage("§cUnbekannter Subcommand: " + subCommand);
                sendHelp(player);
            }
        }

        return true;
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
            // Bei Fehler: kein Zugriff
            return false;
        }
    }

    /**
     * Aktiviert den Preis-Setzungs-Modus.
     *
     * Spieler muss dann ein Item in die Hand nehmen und rechtsklicken.
     *
     * @param player Der Spieler
     * @param plot Der Plot
     */
    private void handleSetPrice(Player player, Plot plot) {
        // Aktiviere Interaktionsmodus
        Location loc = plot.getLocation();
        String plotKey = loc.getBlockX() + "," + loc.getBlockZ();
        activePriceSetMode.put(player.getUniqueId(), plotKey);

        player.sendMessage("§a§lPreis-Setzungs-Modus aktiviert!");
        player.sendMessage("§7Nimm ein Item in die Hand und §eRechtsklicke§7, um den Preis festzulegen.");
        player.sendMessage("§7Schreibe §e/plot price cancel§7 zum Abbrechen.");
    }

    /**
     * Zeigt alle gesetzten Preise auf diesem Plot an.
     *
     * @param player Der Spieler
     * @param plot Der Plot
     */
    private void handleListPrices(Player player, Plot plot) {
        PlotProvider plotProvider = providers.getPlotProvider();

        String owner = "unbekannt";
        try {
            owner = plotProvider.getOwnerName(plot);
        } catch (Exception e) {
            // Fehler beim Abrufen des Besitzers
        }

        Location loc = plot.getLocation();
        int x = loc.getBlockX();
        int z = loc.getBlockZ();

        player.sendMessage("§6§m----------§r §e§lHandelsgilde Preisliste §6§m----------");
        player.sendMessage("§7Grundstück: §e" + x + "," + z);
        player.sendMessage("§7Besitzer: §e" + owner);
        player.sendMessage("");

        // Hole alle Preise vom EconomyProvider (eliminiert Reflection!)
        try {
            EconomyProvider economyProvider = providers.getEconomyProvider();
            if (economyProvider == null || !economyProvider.isAvailable()) {
                player.sendMessage("§cEconomy-Provider nicht verfügbar!");
                return;
            }

            // Hole alle Materialien mit definierten Preisen
            Collection<Material> pricedMaterials = economyProvider.getAllPricedMaterials();

            if (pricedMaterials.isEmpty()) {
                player.sendMessage("§7Keine Preise definiert.");
                player.sendMessage("§7Nutze §e/plot price set§7, um Preise festzulegen.");
            } else {
                player.sendMessage("§7Registrierte Preise (§e" + pricedMaterials.size() + "§7):");
                player.sendMessage("");

                // Sortiere Materialien nach Name
                var sortedMaterials = new java.util.ArrayList<>(pricedMaterials);
                sortedMaterials.sort((a, b) -> a.name().compareTo(b.name()));

                // Zeige Preise an
                for (Material material : sortedMaterials) {
                    try {
                        // Hole Ankaufs- und Verkaufspreis
                        Optional<BigDecimal> buyPriceOpt = economyProvider.getBuyPrice(material);
                        Optional<BigDecimal> sellPriceOpt = economyProvider.getSellPrice(material);

                        // Nur anzeigen wenn mindestens ein Preis gesetzt ist
                        if (buyPriceOpt.isPresent() || sellPriceOpt.isPresent()) {
                            // Formatiere Material-Name schöner
                            String materialName = material.name().replace("_", " ").toLowerCase();
                            materialName = capitalizeWords(materialName);

                            player.sendMessage("§e  " + materialName);

                            // Ankaufspreis (was der Händler vom Spieler kauft)
                            if (buyPriceOpt.isPresent()) {
                                BigDecimal buyPrice = buyPriceOpt.get();
                                player.sendMessage("§7    Ankauf: §a" + buyPrice + " Sterne §7(Händler kauft)");
                            } else {
                                player.sendMessage("§7    Ankauf: §8-§7 (nicht gesetzt)");
                            }

                            // Verkaufspreis (was der Händler an den Spieler verkauft)
                            if (sellPriceOpt.isPresent()) {
                                BigDecimal sellPrice = sellPriceOpt.get();
                                player.sendMessage("§7    Verkauf: §6" + sellPrice + " Sterne §7(Spieler kauft)");
                            } else {
                                player.sendMessage("§7    Verkauf: §8-§7 (nicht gesetzt)");
                            }

                            player.sendMessage(""); // Leerzeile zwischen Items
                        }
                    } catch (Exception e) {
                        player.sendMessage("§c  Fehler beim Lesen eines Preises: " + e.getMessage());
                        logger.warning("Fehler beim Lesen eines Preises: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }

        } catch (Exception e) {
            player.sendMessage("§cFehler beim Laden der Preise!");
            e.printStackTrace();
        }

        player.sendMessage("§6§m--------------------------------------------");
    }

    /**
     * Kapitalisiert die ersten Buchstaben jedes Wortes.
     *
     * @param text Text
     * @return Kapitalisierter Text
     */
    private String capitalizeWords(String text) {
        String[] words = text.split(" ");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1))
                      .append(" ");
            }
        }

        return result.toString().trim();
    }

    /**
     * Prüft ob ein Spieler im Preis-Setzungs-Modus ist.
     *
     * @param player Der Spieler
     * @return true wenn aktiv
     */
    public boolean isInPriceSetMode(Player player) {
        return activePriceSetMode.containsKey(player.getUniqueId());
    }

    /**
     * Entfernt einen Spieler aus dem Preis-Setzungs-Modus.
     *
     * @param player Der Spieler
     */
    public void cancelPriceSetMode(Player player) {
        if (activePriceSetMode.remove(player.getUniqueId()) != null) {
            player.sendMessage("§7Preis-Setzungs-Modus §cdeaktiviert§7.");
        }
    }

    /**
     * Holt den aktuellen Plot des Spielers.
     *
     * @param player Der Spieler
     * @return Plot oder null
     */
    private Plot getCurrentPlot(Player player) {
        PlotProvider plotProvider = providers.getPlotProvider();
        if (plotProvider == null || !plotProvider.isAvailable()) {
            return null;
        }

        try {
            return plotProvider.getPlot(player.getLocation());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Handhabt Preis-Anpassungen.
     *
     * @param player Der Spieler
     * @param args Command-Argumente
     * @return true wenn erfolgreich
     */
    private boolean handleAdjustPrice(Player player, String[] args) {
        UUID sessionId;
        try {
            sessionId = UUID.fromString(args[1]);
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cUngültige Session-ID!");
            return true;
        }

        PriceEditorContext context = activeSessions.get(sessionId);
        if (context == null) {
            player.sendMessage("§cSession nicht gefunden!");
            return true;
        }

        // Prüfe ob Session dem Spieler gehört
        if (!playerSessions.getOrDefault(player.getUniqueId(), UUID.randomUUID()).equals(sessionId)) {
            player.sendMessage("§cDies ist nicht deine Session!");
            return true;
        }

        // Parse Amount
        BigDecimal amount;
        try {
            amount = new BigDecimal(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage("§cUngültiger Betrag: " + args[2]);
            return true;
        }

        // Passe Preis an
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            context.increasePrice(amount);
            player.sendMessage("§aPreis erhöht um " + amount + " Sterne");
        } else if (amount.compareTo(BigDecimal.ZERO) < 0) {
            context.decreasePrice(amount.abs());
            player.sendMessage("§aPreis verringert um " + amount.abs() + " Sterne");
        }

        player.sendMessage("§7Neuer Preis: §e" + context.getCurrentPrice() + " Sterne");

        // TODO: UI neu öffnen (refresh)
        return true;
    }

    /**
     * Bestätigt den Preis und speichert ihn.
     *
     * @param player Der Spieler
     * @param sessionIdStr Session-ID String
     * @return true wenn erfolgreich
     */
    private boolean handleConfirmPrice(Player player, String sessionIdStr) {
        UUID sessionId;
        try {
            sessionId = UUID.fromString(sessionIdStr);
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cUngültige Session-ID!");
            return true;
        }

        PriceEditorContext context = activeSessions.get(sessionId);
        if (context == null) {
            player.sendMessage("§cSession nicht gefunden!");
            return true;
        }

        // Prüfe ob Session dem Spieler gehört
        if (!playerSessions.getOrDefault(player.getUniqueId(), UUID.randomUUID()).equals(sessionId)) {
            player.sendMessage("§cDies ist nicht deine Session!");
            return true;
        }

        // Speichere Preis in ItemBasePriceProvider
        boolean saved = savePriceToProvider(context);

        if (saved) {
            player.sendMessage("§a§lPreis gespeichert!");
            player.sendMessage("§7Item: §e" + context.getItemDisplayName());
            player.sendMessage("§7Preis: §e" + context.getCurrentPrice() + " Sterne");
        } else {
            player.sendMessage("§c§lFehler beim Speichern!");
            player.sendMessage("§7Economy-Modul nicht verfügbar");
            player.sendMessage("§7Preis: §e" + context.getCurrentPrice() + " Sterne (nicht gespeichert)");
        }

        // Session beenden
        removeSession(player, sessionId);

        return true;
    }

    /**
     * Bricht die Preis-Bearbeitung ab.
     *
     * @param player Der Spieler
     * @param sessionIdStr Session-ID String
     * @return true wenn erfolgreich
     */
    private boolean handleCancelPrice(Player player, String sessionIdStr) {
        UUID sessionId;
        try {
            sessionId = UUID.fromString(sessionIdStr);
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cUngültige Session-ID!");
            return true;
        }

        PriceEditorContext context = activeSessions.get(sessionId);
        if (context == null) {
            player.sendMessage("§cSession nicht gefunden!");
            return true;
        }

        // Prüfe ob Session dem Spieler gehört
        if (!playerSessions.getOrDefault(player.getUniqueId(), UUID.randomUUID()).equals(sessionId)) {
            player.sendMessage("§cDies ist nicht deine Session!");
            return true;
        }

        player.sendMessage("§7Preis-Bearbeitung §cabgebrochen§7.");

        // Session beenden
        removeSession(player, sessionId);

        return true;
    }

    /**
     * Erstellt eine neue Preis-Editor-Session.
     *
     * @param player Der Spieler
     * @param context PriceEditorContext
     * @return Session-ID
     */
    public UUID createSession(Player player, PriceEditorContext context) {
        UUID sessionId = UUID.randomUUID();
        activeSessions.put(sessionId, context);
        playerSessions.put(player.getUniqueId(), sessionId);
        return sessionId;
    }

    /**
     * Entfernt eine Session.
     *
     * @param sessionId Session-ID
     */
    public void removeSession(UUID sessionId) {
        PriceEditorContext context = activeSessions.remove(sessionId);
        if (context != null) {
            // Entferne auch Player-Session-Mapping
            playerSessions.values().removeIf(id -> id.equals(sessionId));
        }
    }

    /**
     * Handhabt Preis-Bestätigung für eine Session.
     *
     * @param player Der Spieler
     * @param sessionId Session-ID
     */
    public void handleConfirmPrice(Player player, UUID sessionId) {
        PriceEditorContext context = activeSessions.get(sessionId);
        if (context == null) {
            player.sendMessage("§cSession nicht gefunden!");
            return;
        }

        // Speichere Preis
        boolean success = savePriceToProvider(context);

        if (success) {
            player.sendMessage("§a✓ Preis gespeichert: " + context.getCurrentPrice() + " Sterne");
            player.sendMessage("§7Item: §e" + getItemDisplayName(context.getItem()));
        } else {
            player.sendMessage("§cFehler beim Speichern des Preises!");
        }

        // Entferne Session
        removeSession(sessionId);
    }

    /**
     * Entfernt eine Session (alte Signatur für Kompatibilität).
     *
     * @param player Der Spieler
     * @param sessionId Session-ID
     * @deprecated Nutze stattdessen removeSession(UUID sessionId)
     */
    @Deprecated
    private void removeSession(Player player, UUID sessionId) {
        removeSession(sessionId);
    }

    /**
     * Holt den aktuellen Preis eines Items aus dem EconomyProvider (eliminiert Reflection!).
     *
     * @param item Das Item
     * @return Preis (oder 0 wenn nicht verfügbar)
     * @deprecated Verwende {@link #loadBuyPriceFromProvider(ItemStack)} oder {@link #loadSellPriceFromProvider(ItemStack)}
     */
    @Deprecated
    public BigDecimal loadPriceFromProvider(ItemStack item) {
        // Fallback auf Sell-Preis
        return loadSellPriceFromProvider(item);
    }

    /**
     * Holt den Ankaufspreis eines Items aus dem EconomyProvider (eliminiert Reflection!).
     *
     * @param item Das Item
     * @return Ankaufspreis (oder 0 wenn nicht verfügbar)
     */
    public BigDecimal loadBuyPriceFromProvider(ItemStack item) {
        try {
            EconomyProvider economyProvider = providers.getEconomyProvider();
            if (economyProvider == null || !economyProvider.isAvailable()) {
                return BigDecimal.ZERO;
            }

            Material material = item.getType();
            return economyProvider.getBuyPrice(material).orElse(BigDecimal.ZERO);

        } catch (Exception e) {
            // Economy-Provider nicht verfügbar oder Fehler
            logger.warning("Fehler beim Laden des Ankaufspreises: " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    /**
     * Holt den Verkaufspreis eines Items aus dem EconomyProvider (eliminiert Reflection!).
     *
     * @param item Das Item
     * @return Verkaufspreis (oder 0 wenn nicht verfügbar)
     */
    public BigDecimal loadSellPriceFromProvider(ItemStack item) {
        try {
            EconomyProvider economyProvider = providers.getEconomyProvider();
            if (economyProvider == null || !economyProvider.isAvailable()) {
                return BigDecimal.ZERO;
            }

            Material material = item.getType();
            return economyProvider.getSellPrice(material).orElse(BigDecimal.ZERO);

        } catch (Exception e) {
            // Economy-Provider nicht verfügbar oder Fehler
            logger.warning("Fehler beim Laden des Verkaufspreises: " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    /**
     * Speichert Buy/Sell-Preise im EconomyProvider (eliminiert teilweise Reflection!).
     *
     * @param context PriceEditorContext mit Buy/Sell-Preisen
     * @return true wenn erfolgreich
     */
    public boolean savePriceToProvider(PriceEditorContext context) {
        try {
            EconomyProvider economyProvider = providers.getEconomyProvider();
            if (economyProvider == null || !economyProvider.isAvailable()) {
                logger.warning("Economy-Provider nicht verfügbar");
                return false;
            }

            // Setze Preis via Provider
            Material material = context.getItem().getType();
            boolean success = economyProvider.setItemPrice(
                material,
                context.getBuyPrice(),
                context.getSellPrice()
            );

            if (!success) {
                logger.warning("Preis konnte nicht gesetzt werden");
                return false;
            }

            // Config-Speicherung erfolgt automatisch in economyProvider.setItemPrice()!
            // Kein Reflection mehr nötig - VaultEconomyProvider hat Plugin-Injection

            return true;

        } catch (Exception e) {
            // Economy-Provider nicht verfügbar oder Fehler
            logger.warning("Fehler beim Speichern der Preise: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Gibt den Anzeigenamen eines Items zurück.
     *
     * @param item ItemStack
     * @return Display-Name
     */
    private String getItemDisplayName(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return item.getItemMeta().getDisplayName();
        }
        return item.getType().name();
    }

    /**
     * Sendet Hilfe-Nachricht.
     *
     * @param player Der Spieler
     */
    private void sendHelp(Player player) {
        player.sendMessage("§6§m-----§r §e§lHandelsgilde Preisverwaltung §6§m-----");
        player.sendMessage("§e/plot price set §7- Preis für Item festlegen");
        player.sendMessage("§e/plot price list §7- Alle Preise anzeigen");
        player.sendMessage("§6§m---------------------------------");
    }
}
