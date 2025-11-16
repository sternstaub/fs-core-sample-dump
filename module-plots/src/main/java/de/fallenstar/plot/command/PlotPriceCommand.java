package de.fallenstar.plot.command;

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
import java.util.HashMap;
import java.util.Map;
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
     */
    public PlotPriceCommand(ProviderRegistry providers) {
        this.providers = providers;
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
        switch (subCommand) {
            case "set" -> handleSetPrice(player, plot);
            case "list" -> handleListPrices(player, plot);
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

        // Hole alle Preise vom ItemBasePriceProvider
        try {
            var plugin = Bukkit.getPluginManager().getPlugin("FallenStar-Economy");
            if (plugin == null) {
                player.sendMessage("§cEconomy-Modul nicht geladen!");
                return;
            }

            var getPriceProvider = plugin.getClass().getMethod("getPriceProvider");
            var priceProvider = getPriceProvider.invoke(plugin);

            // Hole alle Vanilla-Preise
            var getAllVanillaPricesMethod = priceProvider.getClass().getMethod("getAllVanillaPrices");
            var vanillaPrices = (java.util.Collection<?>) getAllVanillaPricesMethod.invoke(priceProvider);

            if (vanillaPrices.isEmpty()) {
                player.sendMessage("§7Keine Preise definiert.");
                player.sendMessage("§7Nutze §e/plot price set§7, um Preise festzulegen.");
            } else {
                player.sendMessage("§7Registrierte Preise (§e" + vanillaPrices.size() + "§7):");
                player.sendMessage("");

                // Sortiere Preise nach Material-Name
                var sortedPrices = new java.util.ArrayList<>(vanillaPrices);
                sortedPrices.sort((a, b) -> {
                    try {
                        var getMaterial = a.getClass().getMethod("getMaterial");
                        Material matA = (Material) getMaterial.invoke(a);
                        Material matB = (Material) getMaterial.invoke(b);
                        return matA.name().compareTo(matB.name());
                    } catch (Exception e) {
                        return 0;
                    }
                });

                // Zeige Preise an
                for (Object priceObj : sortedPrices) {
                    try {
                        var getMaterial = priceObj.getClass().getMethod("getMaterial");
                        var getPrice = priceObj.getClass().getMethod("getPrice");

                        Material material = (Material) getMaterial.invoke(priceObj);
                        BigDecimal price = (BigDecimal) getPrice.invoke(priceObj);

                        // Formatiere Material-Name schöner
                        String materialName = material.name().replace("_", " ").toLowerCase();
                        materialName = capitalizeWords(materialName);

                        player.sendMessage("§e  " + materialName + " §7- §6" + price + " Sterne");
                    } catch (Exception e) {
                        player.sendMessage("§c  Fehler beim Lesen eines Preises");
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
     * Holt den aktuellen Preis eines Items aus dem ItemBasePriceProvider.
     *
     * @param item Das Item
     * @return Preis (oder 0 wenn nicht verfügbar)
     */
    public BigDecimal loadPriceFromProvider(ItemStack item) {
        try {
            // Hole Economy-Modul
            var plugin = Bukkit.getPluginManager().getPlugin("FallenStar-Economy");
            if (plugin == null) {
                return BigDecimal.ZERO;
            }

            // Reflection: hole ItemBasePriceProvider
            var getPriceProvider = plugin.getClass().getMethod("getPriceProvider");
            var priceProvider = getPriceProvider.invoke(plugin);

            // Prüfe ob Vanilla-Item
            Material material = item.getType();
            var getPriceMethod = priceProvider.getClass().getMethod("getVanillaPriceOrDefault", Material.class);
            BigDecimal price = (BigDecimal) getPriceMethod.invoke(priceProvider, material);

            return price;

        } catch (Exception e) {
            // Economy-Modul nicht verfügbar oder Fehler
            return BigDecimal.ZERO;
        }
    }

    /**
     * Speichert einen Preis im ItemBasePriceProvider.
     *
     * @param context PriceEditorContext
     * @return true wenn erfolgreich
     */
    private boolean savePriceToProvider(PriceEditorContext context) {
        try {
            // Hole Economy-Modul
            var plugin = Bukkit.getPluginManager().getPlugin("FallenStar-Economy");
            if (plugin == null) {
                return false;
            }

            // Reflection: hole ItemBasePriceProvider
            var getPriceProvider = plugin.getClass().getMethod("getPriceProvider");
            var priceProvider = getPriceProvider.invoke(plugin);

            // Registriere Vanilla-Preis
            Material material = context.getItem().getType();
            var registerMethod = priceProvider.getClass().getMethod(
                "registerVanillaPrice",
                Material.class,
                BigDecimal.class
            );
            registerMethod.invoke(priceProvider, material, context.getCurrentPrice());

            // WICHTIG: Speichere Config auf Festplatte!
            var saveConfigMethod = plugin.getClass().getMethod("saveConfiguration");
            saveConfigMethod.invoke(plugin);

            return true;

        } catch (Exception e) {
            // Economy-Modul nicht verfügbar oder Fehler
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
