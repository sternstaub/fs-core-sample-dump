package de.fallenstar.plot.ui;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.registry.ProviderRegistry;
import de.fallenstar.core.ui.SmallChestUI;
import de.fallenstar.plot.PlotModule;
import de.fallenstar.plot.slot.MarketPlot;
import de.fallenstar.plot.slot.PlotSlotManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Market-Plot Hauptmenü mit Guest/Owner Ansichten.
 *
 * **Guest-Ansicht (Besucher):**
 * - Zeigt Markt-Optionen
 * - Händler finden
 * - Items kaufen/verkaufen (über NPCs)
 * - Plot-Info
 *
 * **Owner-Ansicht (Besitzer):**
 * - Zeigt Verwaltungs-Optionen
 * - Händler-Slots verwalten ★
 * - NPCs verwalten
 * - Storage verwalten
 * - Plot-Info
 *
 * Layout:
 * - Zeile 0 (Slots 0-8): Navigation (Zurück, Info)
 * - Zeilen 1-2 (Slots 9-26): Optionen-Buttons
 *
 * @author FallenStar
 * @version 1.0
 */
class MarketPlotUI extends SmallChestUI {

    private final Plugin plugin;
    private final ProviderRegistry providers;
    private final PlotSlotManager slotManager;
    private final Plot plot;
    private final MarketPlot marketPlot;
    private final boolean isOwner;

    /**
     * Erstellt eine neue MarketPlotUI.
     *
     * @param plugin Plugin-Instanz
     * @param providers ProviderRegistry
     * @param slotManager PlotSlotManager
     * @param plot Der Plot
     * @param marketPlot Das MarketPlot
     * @param isOwner Ob der öffnende Spieler der Besitzer ist
     */
    public MarketPlotUI(
            Plugin plugin,
            ProviderRegistry providers,
            PlotSlotManager slotManager,
            Plot plot,
            MarketPlot marketPlot,
            boolean isOwner
    ) {
        super(isOwner ? "§6§lMarkt - Verwaltung" : "§e§lMarkt - Übersicht");
        this.plugin = plugin;
        this.providers = providers;
        this.slotManager = slotManager;
        this.plot = plot;
        this.marketPlot = marketPlot;
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

        // Slot 4: Info/Hilfe
        ItemStack infoButton = createNavigationItem(
                Material.BOOK,
                isOwner ? "§e§lVerwaltungsmodus" : "§e§lMarkt-Übersicht",
                isOwner ?
                        List.of(
                                "§7Du bist der Besitzer dieses Plots",
                                "§7",
                                "§eVerwalte Händler-Slots und NPCs"
                        ) :
                        List.of(
                                "§7Willkommen auf dem Marktplatz!",
                                "§7",
                                "§7Finde Händler und handle mit ihnen"
                        )
        );
        setItem(4, infoButton);

        // Owner-exklusive Optionen
        if (isOwner) {
            // Slot 8: Slot-Info
            int currentSlots = marketPlot.getCurrentlyAvailableSlots();
            int maxSlots = marketPlot.getMaximumAvailableSlots();
            int usedSlots = (int) marketPlot.getTraderSlots().stream()
                    .filter(slot -> slot.isOccupied())
                    .count();

            ItemStack slotInfoButton = createNavigationItem(
                    Material.ARMOR_STAND,
                    "§6Händler-Slots",
                    List.of(
                            "§7Verfügbar: §e" + currentSlots + "§7/§e" + maxSlots,
                            "§7Belegt: §e" + usedSlots + "§7/§e" + currentSlots,
                            "§7Frei: §e" + (currentSlots - usedSlots)
                    )
            );
            setItem(8, slotInfoButton);
        }
    }

    /**
     * Baut die Optionen für Owner (Verwaltung).
     */
    private void buildOwnerOptions() {
        // Zeile 1: Verwaltungs-Optionen

        // Slot 10: Händler-Slots verwalten ★
        int currentSlots = marketPlot.getCurrentlyAvailableSlots();
        int maxSlots = marketPlot.getMaximumAvailableSlots();
        boolean canBuyMore = marketPlot.canPurchaseMoreSlots();

        ItemStack slotsButton = createOptionButton(
                Material.ARMOR_STAND,
                "§6§lHändler-Slots verwalten",
                List.of(
                        "§7Verwalte Händler-Slot-Positionen",
                        "§7auf diesem Marktplatz",
                        "§7",
                        "§7Verfügbar: §e" + currentSlots + "§7/§e" + maxSlots,
                        canBuyMore ?
                                "§7Weitere Slots: §a✓ Kaufbar" :
                                "§7Weitere Slots: §c✗ Limit erreicht",
                        "§7",
                        "§a§lKlicke für Slot-Verwaltung"
                )
        );
        setItem(10, slotsButton, player -> {
            player.closeInventory();
            // Öffne TraderSlotsUI (wird noch implementiert)
            player.performCommand("plot slots list");
        });

        // Slot 12: Slot kaufen
        if (canBuyMore) {
            double slotPrice = slotManager.getSlotPrice(plugin.getConfig());
            String currency = slotManager.getSlotCurrency(plugin.getConfig());

            ItemStack buySlotButton = createOptionButton(
                    Material.GOLD_INGOT,
                    "§a§lNeuen Slot kaufen",
                    List.of(
                            "§7Kaufe einen zusätzlichen",
                            "§7Händler-Slot für diesen Markt",
                            "§7",
                            "§7Preis: §e" + slotPrice + " " + currency,
                            "§7Verfügbar: §e" + marketPlot.getRemainingPurchasableSlots(),
                            "§7",
                            "§a§lKlicke zum Kaufen"
                    )
            );
            setItem(12, buySlotButton, player -> {
                player.closeInventory();
                player.performCommand("plot slots buy");
            });
        } else {
            ItemStack maxSlotsButton = createOptionButton(
                    Material.BARRIER,
                    "§c§lMaximale Slots erreicht",
                    List.of(
                            "§7Du hast bereits alle",
                            "§7verfügbaren Slots gekauft",
                            "§7",
                            "§7Slots: §e" + currentSlots + "§7/§e" + maxSlots
                    )
            );
            setItem(12, maxSlotsButton);
        }

        // Slot 14: NPCs verwalten
        ItemStack npcButton = createOptionButton(
                Material.VILLAGER_SPAWN_EGG,
                "§6§lNPCs verwalten",
                List.of(
                        "§7Verwalte Händler-NPCs",
                        "§7auf diesem Marktplatz",
                        "§7",
                        "§7[ROADMAP] Feature noch nicht verfügbar"
                )
        );
        setItem(14, npcButton, player -> {
            player.sendMessage("§c[Roadmap] NPC-Verwaltung noch nicht implementiert!");
            player.sendMessage("§7Geplant für zukünftige Sprints");
            player.sendMessage("§7Nutze §e/plot npc list§7 für NPC-Liste");
        });

        // Slot 16: Storage verwalten
        ItemStack storageButton = createOptionButton(
                Material.CHEST,
                "§6§lStorage verwalten",
                List.of(
                        "§7Verwalte das Plot-Storage",
                        "§7für Handelswaren",
                        "§7",
                        "§a§lKlicke zum Öffnen"
                )
        );
        setItem(16, storageButton, player -> {
            openPlotStorageUI(player);
        });

        // Zeile 2: Weitere Optionen

        // Slot 20: Plot-Info
        ItemStack infoButton = createOptionButton(
                Material.MAP,
                "§e§lGrundstücks-Info",
                List.of(
                        "§7Zeigt Details zu diesem Plot",
                        "§7",
                        "§a§lKlicke zum Öffnen"
                )
        );
        setItem(20, infoButton, player -> {
            player.closeInventory();
            player.performCommand("plot info");
        });

        // Slot 22: Markt-Statistiken (Platzhalter)
        ItemStack statsButton = createOptionButton(
                Material.BOOK,
                "§e§lMarkt-Statistiken",
                List.of(
                        "§7Zeigt Umsatz und",
                        "§7Verkaufsstatistiken an",
                        "§7",
                        "§c§lNoch nicht implementiert"
                )
        );
        setItem(22, statsButton, player -> {
            player.sendMessage("§c§lMarkt-Statistiken noch nicht implementiert!");
        });
    }

    /**
     * Baut die Optionen für Guest (Besucher).
     */
    private void buildGuestOptions() {
        // Zeile 1: Markt-Optionen

        // Slot 11: Händler finden
        ItemStack findTradersButton = createOptionButton(
                Material.COMPASS,
                "§e§lHändler finden",
                List.of(
                        "§7Zeigt alle aktiven Händler",
                        "§7auf diesem Marktplatz an",
                        "§7",
                        "§c§lNoch nicht implementiert"
                )
        );
        setItem(11, findTradersButton, player -> {
            player.sendMessage("§c§lHändler-Suche noch nicht implementiert!");
            player.sendMessage("§7Suche manuell nach Händler-NPCs auf dem Plot");
        });

        // Slot 13: Shop-Liste (Platzhalter)
        ItemStack shopListButton = createOptionButton(
                Material.EMERALD,
                "§a§lVerfügbare Waren",
                List.of(
                        "§7Zeigt alle verfügbaren",
                        "§7Waren und Preise an",
                        "§7",
                        "§c§lNoch nicht implementiert"
                )
        );
        setItem(13, shopListButton, player -> {
            player.sendMessage("§c§lWaren-Übersicht noch nicht implementiert!");
            player.sendMessage("§7Sprich direkt mit den Händler-NPCs");
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
     * Erstellt ein Navigation-Item.
     */
    private ItemStack createNavigationItem(Material material, String name, List<String> lore) {
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
     * Erstellt ein Optionen-Button Item.
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
     * Öffnet die PlotStorageUi für den Spieler.
     *
     * @param player Der Spieler
     */
    private void openPlotStorageUI(Player player) {
        // Cast Plugin zu PlotModule
        PlotModule plotModule = (PlotModule) plugin;

        // Hole Storage-Provider und Manager
        de.fallenstar.plot.storage.provider.PlotStorageProvider storageProvider = plotModule.getStorageProvider();
        de.fallenstar.plot.storage.manager.StorageManager storageManager = plotModule.getStorageManager();

        if (storageProvider == null || storageManager == null) {
            player.sendMessage("§cStorage-System nicht verfügbar!");
            return;
        }

        // Hole PlotStorage für aktuellen Plot
        de.fallenstar.plot.storage.model.PlotStorage plotStorage = storageProvider.getPlotStorage(plot);

        // Öffne PlotStorageUi
        PlotStorageUi storageUI = new PlotStorageUi(
                plot,
                plotStorage,
                storageProvider,
                storageManager,
                isOwner
        );
        storageUI.open(player);
    }

    /**
     * Öffnet die SlotManagementUI für den Spieler.
     *
     * @param player Der Spieler
     */
    private void openSlotManagementUI(Player player) {
        // Cast Plugin zu PlotModule
        PlotModule plotModule = (PlotModule) plugin;

        // Hole PlotRegistry
        de.fallenstar.plot.registry.PlotRegistry plotRegistry = plotModule.getPlotRegistry();

        if (plotRegistry == null) {
            player.sendMessage("§cPlotRegistry nicht verfügbar!");
            return;
        }

        // MarketPlot ist SlottedPlot, daher direkt nutzen
        de.fallenstar.plot.slot.SlottedPlot slottedPlot = marketPlot;

        // Öffne SlotManagementUI
        SlotManagementUI slotUI = new SlotManagementUI(
                plugin,
                slottedPlot,
                plotRegistry
        );
        slotUI.open(player);
    }
}
