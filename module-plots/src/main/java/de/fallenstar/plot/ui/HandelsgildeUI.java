package de.fallenstar.plot.ui;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.provider.PlotProvider;
import de.fallenstar.core.registry.ProviderRegistry;
import de.fallenstar.core.ui.LargeChestUI;
import de.fallenstar.plot.command.PlotPriceCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Handelsgilde-Hauptmenü mit Guest/Owner Ansichten.
 *
 * **Guest-Ansicht (Besucher):**
 * - Zeigt Shop-Optionen
 * - Preisliste anzeigen
 * - Items kaufen (Roadmap)
 * - NPCs ansprechen
 *
 * **Owner-Ansicht (Besitzer):**
 * - Zeigt Verwaltungs-Optionen
 * - Preise setzen/anzeigen
 * - NPCs verwalten
 * - Storage verwalten
 * - Händler-Slots verwalten (Roadmap)
 *
 * Layout:
 * - Zeile 0 (Slots 0-8): Navigation (Zurück, Info)
 * - Zeilen 1-3 (Slots 9-35): Optionen-Buttons
 *
 * @author FallenStar
 * @version 1.0
 */
public class HandelsgildeUI extends SmallChestUI {

    private final ProviderRegistry providers;
    private final PlotPriceCommand priceCommand;
    private final Plot plot;
    private final boolean isOwner;

    /**
     * Erstellt eine neue HandelsgildeUI.
     *
     * @param providers ProviderRegistry
     * @param priceCommand PlotPriceCommand (für Preis-Zugriff)
     * @param plot Der Plot
     * @param isOwner Ob der öffnende Spieler der Besitzer ist
     */
    public HandelsgildeUI(
            ProviderRegistry providers,
            PlotPriceCommand priceCommand,
            Plot plot,
            boolean isOwner
    ) {
        super(isOwner ? "§6§lHandelsgilde - Verwaltung" : "§e§lHandelsgilde - Shop");
        this.providers = providers;
        this.priceCommand = priceCommand;
        this.plot = plot;
        this.isOwner = isOwner;

        buildUI();
    }

    /**
     * Baut das UI auf (Guest oder Owner Ansicht).
     */
    private void buildUI() {
        // Zeile 0: Navigation-Bar
        buildNavigationBar();

        // Zeilen 1-2: Optionen-Buttons
        if (isOwner) {
            buildOwnerOptions();
        } else {
            buildGuestOptions();
        }
    }

    /**
     * Baut die Navigation-Bar (Zeile 0).
     */
    private void buildNavigationBar() {
        // Slot 0: Zurück/Schließen
        ItemStack closeButton = createNavigationItem(
                Material.BARRIER,
                "§cSchließen",
                List.of("§7Klicke um das UI zu schließen")
        );
        setItem(0, closeButton, player -> player.closeInventory());

        // Slots 1-2: Blättern Links (noch nicht implementiert)
        ItemStack prevButton = createNavigationItem(
                Material.ARROW,
                "§7← Vorherige Seite",
                List.of("§cNoch nicht verfügbar")
        );
        setItem(1, prevButton);

        // Slots 6-7: Blättern Rechts (noch nicht implementiert)
        ItemStack nextButton = createNavigationItem(
                Material.ARROW,
                "§7Nächste Seite →",
                List.of("§cNoch nicht verfügbar")
        );
        setItem(7, nextButton);

        // Slot 4: Info/Hilfe
        ItemStack infoButton = createNavigationItem(
                Material.BOOK,
                isOwner ? "§e§lVerwaltungsmodus" : "§e§lShop-Modus",
                isOwner ?
                        List.of(
                                "§7Du bist der Besitzer dieses Plots",
                                "§7",
                                "§eKlicke auf Items um Preise zu ändern"
                        ) :
                        List.of(
                                "§7Willkommen im Handelsgilde-Shop!",
                                "§7",
                                "§7Kaufe Items zu den angezeigten Preisen"
                        )
        );
        setItem(4, infoButton);

        // Owner-exklusive Optionen
        if (isOwner) {
            // Slot 8: Storage-Zugriff (Platzhalter)
            ItemStack storageButton = createNavigationItem(
                    Material.CHEST,
                    "§6Storage anzeigen",
                    List.of("§cNoch nicht implementiert")
            );
            setItem(8, storageButton);
        }
    }

    /**
     * Baut die Optionen für Owner (Verwaltung).
     */
    private void buildOwnerOptions() {
        // Zeile 1: Verwaltungs-Optionen

        // Slot 10: Preise anzeigen
        ItemStack viewPricesButton = createOptionButton(
                Material.BOOK,
                "§e§lPreise anzeigen",
                List.of(
                        "§7Zeigt alle definierten Preise",
                        "§7in diesem Handelsgilde-Grundstück",
                        "§7",
                        "§a§lKlicke zum Öffnen"
                )
        );
        setItem(10, viewPricesButton, player -> {
            player.closeInventory();
            player.performCommand("plot price list");
        });

        // Slot 12: Preise setzen
        ItemStack setPriceButton = createOptionButton(
                Material.WRITABLE_BOOK,
                "§e§lPreise setzen",
                List.of(
                        "§7Aktiviert den Preis-Setzungs-Modus",
                        "§7",
                        "§71. Nimm ein Item in die Hand",
                        "§72. Rechtsklicke",
                        "§73. Setze den Preis im UI",
                        "§7",
                        "§a§lKlicke zum Aktivieren"
                )
        );
        setItem(12, setPriceButton, player -> {
            player.closeInventory();
            player.performCommand("plot price set");
        });

        // Slot 14: NPCs verwalten
        ItemStack npcButton = createOptionButton(
                Material.VILLAGER_SPAWN_EGG,
                "§6§lNPCs verwalten",
                List.of(
                        "§7Verwalte Gildenhändler und",
                        "§7Spielerhändler auf diesem Plot",
                        "§7",
                        "§c§lNoch nicht implementiert"
                )
        );
        setItem(14, npcButton, player -> {
            player.sendMessage("§c§lNPC-Verwaltung noch nicht implementiert!");
            player.sendMessage("§7Wird in Sprint 13-14 verfügbar sein");
        });

        // Slot 16: Storage verwalten
        ItemStack storageButton = createOptionButton(
                Material.CHEST,
                "§6§lStorage verwalten",
                List.of(
                        "§7Verwalte das Plot-Storage",
                        "§7für Handelswaren",
                        "§7",
                        "§c§lNoch nicht implementiert"
                )
        );
        setItem(16, storageButton, player -> {
            player.sendMessage("§c§lStorage-Verwaltung noch nicht implementiert!");
        });

        // Zeile 2: Weitere Optionen

        // Slot 20: Händler-Slots (Roadmap)
        ItemStack slotsButton = createOptionButton(
                Material.ARMOR_STAND,
                "§6§lHändler-Slots",
                List.of(
                        "§7Verwalte Händler-Slots",
                        "§7auf diesem Grundstück",
                        "§7",
                        "§c§lRoadmap: Sprint 11-12"
                )
        );
        setItem(20, slotsButton, player -> {
            player.sendMessage("§c§lHändler-Slots noch nicht implementiert!");
            player.sendMessage("§7Geplant für Sprint 11-12 (WorldAnchors)");
        });

        // Slot 22: Plot-Info
        ItemStack infoButton = createOptionButton(
                Material.MAP,
                "§e§lGrundstücks-Info",
                List.of(
                        "§7Zeigt Details zu diesem Plot",
                        "§7",
                        "§a§lKlicke zum Öffnen"
                )
        );
        setItem(22, infoButton, player -> {
            player.closeInventory();
            player.performCommand("plot info");
        });
    }

    /**
     * Baut die Optionen für Guest (Besucher).
     */
    private void buildGuestOptions() {
        // Zeile 1: Shop-Optionen

        // Slot 11: Preise anzeigen
        ItemStack viewPricesButton = createOptionButton(
                Material.BOOK,
                "§e§lPreisliste anzeigen",
                List.of(
                        "§7Zeigt alle verfügbaren Items",
                        "§7und deren Preise an",
                        "§7",
                        "§a§lKlicke zum Öffnen"
                )
        );
        setItem(11, viewPricesButton, player -> {
            player.closeInventory();
            player.performCommand("plot price list");
        });

        // Slot 13: Shop (Roadmap)
        ItemStack shopButton = createOptionButton(
                Material.EMERALD,
                "§a§lItems kaufen",
                List.of(
                        "§7Öffnet den Shop",
                        "§7um Items zu kaufen",
                        "§7",
                        "§c§lNoch nicht implementiert"
                )
        );
        setItem(13, shopButton, player -> {
            player.sendMessage("§c§lShop-System noch nicht implementiert!");
            player.sendMessage("§7Nutze vorerst die Gildenhändler-NPCs");
        });

        // Slot 15: Plot-Info
        ItemStack infoButton = createOptionButton(
                Material.MAP,
                "§e§lGrundstücks-Info",
                List.of(
                        "§7Zeigt Details zu diesem Plot",
                        "§7",
                        "§a§lKlicke zum Öffnen"
                )
        );
        setItem(15, infoButton, player -> {
            player.closeInventory();
            player.performCommand("plot info");
        });
    }

    /**
     * VERALTET: Wurde durch buildOwnerOptions() ersetzt.
     */
    @Deprecated
    private void buildOwnerItemList() {
        try {
            // Hole alle Preise vom Economy-Modul
            var economyPlugin = Bukkit.getPluginManager().getPlugin("FallenStar-Economy");
            if (economyPlugin == null) {
                // Fallback: Zeige "Economy nicht verfügbar"
                ItemStack errorItem = createErrorItem("§cEconomy-Modul nicht geladen!");
                setItem(13, errorItem);
                return;
            }

            var getPriceProvider = economyPlugin.getClass().getMethod("getPriceProvider");
            var priceProvider = getPriceProvider.invoke(economyPlugin);

            var getAllVanillaPricesMethod = priceProvider.getClass().getMethod("getAllVanillaPrices");
            var vanillaPrices = (Collection<?>) getAllVanillaPricesMethod.invoke(priceProvider);

            if (vanillaPrices.isEmpty()) {
                // Keine Preise definiert
                ItemStack emptyItem = createInfoItem(
                        "§7Keine Preise definiert",
                        List.of(
                                "§7Nutze §e/plot price set§7,",
                                "§7um Preise festzulegen"
                        )
                );
                setItem(13, emptyItem);
                return;
            }

            // Zeige Preise (max. 45 Items, Zeilen 1-5)
            int slot = 9;  // Start bei Zeile 1
            for (Object priceObj : vanillaPrices) {
                if (slot >= 54) break;  // Max. Slots erreicht

                try {
                    var getMaterial = priceObj.getClass().getMethod("getMaterial");
                    var getPrice = priceObj.getClass().getMethod("getPrice");

                    Material material = (Material) getMaterial.invoke(priceObj);
                    BigDecimal price = (BigDecimal) getPrice.invoke(priceObj);

                    // Erstelle Item mit Preis
                    ItemStack priceItem = createPriceItem(material, price, true);
                    setItem(slot, priceItem, player -> {
                        // TODO: Öffne Preis-Editor
                        player.sendMessage("§7Preis-Editor für §e" + material.name() + " §7noch nicht implementiert");
                        player.sendMessage("§7Nutze §e/plot price set§7 mit dem Item in der Hand");
                    });

                    slot++;
                } catch (Exception e) {
                    // Fehler beim Item-Parsing
                }
            }

        } catch (Exception e) {
            ItemStack errorItem = createErrorItem("§cFehler beim Laden der Preise!");
            setItem(13, errorItem);
            e.printStackTrace();
        }
    }

    /**
     * Baut die Item-Liste für Guest (nur Anzeige).
     */
    private void buildGuestItemList() {
        try {
            // Hole alle Preise vom Economy-Modul
            var economyPlugin = Bukkit.getPluginManager().getPlugin("FallenStar-Economy");
            if (economyPlugin == null) {
                ItemStack errorItem = createErrorItem("§cShop nicht verfügbar!");
                setItem(13, errorItem);
                return;
            }

            var getPriceProvider = economyPlugin.getClass().getMethod("getPriceProvider");
            var priceProvider = getPriceProvider.invoke(economyPlugin);

            var getAllVanillaPricesMethod = priceProvider.getClass().getMethod("getAllVanillaPrices");
            var vanillaPrices = (Collection<?>) getAllVanillaPricesMethod.invoke(priceProvider);

            if (vanillaPrices.isEmpty()) {
                ItemStack emptyItem = createInfoItem(
                        "§7Keine Items verfügbar",
                        List.of("§7Der Shop ist derzeit leer")
                );
                setItem(13, emptyItem);
                return;
            }

            // Zeige Preise (max. 45 Items, Zeilen 1-5)
            int slot = 9;  // Start bei Zeile 1
            for (Object priceObj : vanillaPrices) {
                if (slot >= 54) break;

                try {
                    var getMaterial = priceObj.getClass().getMethod("getMaterial");
                    var getPrice = priceObj.getClass().getMethod("getPrice");

                    Material material = (Material) getMaterial.invoke(priceObj);
                    BigDecimal price = (BigDecimal) getPrice.invoke(priceObj);

                    // Erstelle Item mit Preis (read-only)
                    ItemStack priceItem = createPriceItem(material, price, false);
                    setItem(slot, priceItem, player -> {
                        // TODO: Kaufen-Funktion
                        player.sendMessage("§cKauf-Funktion noch nicht implementiert!");
                    });

                    slot++;
                } catch (Exception e) {
                    // Fehler beim Item-Parsing
                }
            }

        } catch (Exception e) {
            ItemStack errorItem = createErrorItem("§cFehler beim Laden des Shops!");
            setItem(13, errorItem);
            e.printStackTrace();
        }
    }

    /**
     * Erstellt ein Navigation-Item.
     *
     * @param material Material
     * @param name Name
     * @param lore Lore
     * @return ItemStack
     */
    private ItemStack createNavigationItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text(name).decoration(TextDecoration.ITALIC, false));

        List<Component> loreLine = new ArrayList<>();
        for (String line : lore) {
            loreLine.add(Component.text(line).decoration(TextDecoration.ITALIC, false));
        }
        meta.lore(loreLine);

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Erstellt ein Optionen-Button Item.
     *
     * @param material Material
     * @param name Name
     * @param lore Lore
     * @return ItemStack
     */
    private ItemStack createOptionButton(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text(name).decoration(TextDecoration.ITALIC, false));

        List<Component> loreLines = new ArrayList<>();
        for (String line : lore) {
            loreLines.add(Component.text(line).decoration(TextDecoration.ITALIC, false));
        }
        meta.lore(loreLines);

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Erstellt ein Preis-Item.
     *
     * @param material Material
     * @param price Preis
     * @param ownerView Ob Owner-Ansicht
     * @return ItemStack
     */
    private ItemStack createPriceItem(Material material, BigDecimal price, boolean ownerView) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        // Name
        String materialName = material.name().replace("_", " ").toLowerCase();
        materialName = capitalizeWords(materialName);

        meta.displayName(
                Component.text(materialName)
                        .color(NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false)
        );

        // Lore
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(
                Component.text("Preis: ")
                        .color(NamedTextColor.GRAY)
                        .append(Component.text(price + " Sterne").color(NamedTextColor.GOLD))
                        .decoration(TextDecoration.ITALIC, false)
        );
        lore.add(Component.empty());

        if (ownerView) {
            lore.add(
                    Component.text("§e§lKlicke um Preis zu ändern")
                            .decoration(TextDecoration.ITALIC, false)
            );
        } else {
            lore.add(
                    Component.text("§a§lKlicke um zu kaufen")
                            .decoration(TextDecoration.ITALIC, false)
            );
        }

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Erstellt ein Fehler-Item.
     *
     * @param message Fehler-Nachricht
     * @return ItemStack
     */
    private ItemStack createErrorItem(String message) {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text(message).color(NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Erstellt ein Info-Item.
     *
     * @param title Titel
     * @param lore Lore
     * @return ItemStack
     */
    private ItemStack createInfoItem(String title, List<String> lore) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text(title).decoration(TextDecoration.ITALIC, false));

        List<Component> loreLines = new ArrayList<>();
        for (String line : lore) {
            loreLines.add(Component.text(line).decoration(TextDecoration.ITALIC, false));
        }
        meta.lore(loreLines);

        item.setItemMeta(meta);
        return item;
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
}
