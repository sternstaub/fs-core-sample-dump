package de.fallenstar.plot.ui;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.registry.ProviderRegistry;
import de.fallenstar.core.ui.container.BasicGsUi;
import de.fallenstar.plot.action.PlotActionManageNpcs;
import de.fallenstar.plot.action.ManageTraderSlotsAction;
import de.fallenstar.plot.action.ViewAvailableGoodsAction;
import de.fallenstar.plot.action.ViewMarketStatsAction;
import de.fallenstar.plot.slot.MarketPlot;
import de.fallenstar.plot.slot.PlotSlotManager;
import de.fallenstar.plot.storage.manager.StorageManager;
import de.fallenstar.plot.storage.provider.PlotStorageProvider;
import org.bukkit.Material;
import de.fallenstar.plot.PlotModule;

import java.util.List;

/**
 * Type-Safe Market-Plot Hauptmenü mit Guest/Owner Ansichten.
 *
 * **Migration:** Ersetzt alte MarketPlotUI (434 Zeilen) durch
 * type-safe BasicGsUi-basierte Implementierung (~200 Zeilen).
 *
 * **Guest-Ansicht (Besucher):**
 * - Händler finden (Placeholder)
 * - Verfügbare Waren (Placeholder)
 * - Plot-Info
 *
 * **Owner-Ansicht (Besitzer):**
 * - Händler-Slots verwalten
 * - Slot kaufen (conditional)
 * - NPCs verwalten (Placeholder)
 * - Storage verwalten
 * - Plot-Info
 * - Markt-Statistiken (Placeholder)
 *
 * **Type-Safety:**
 * - Alle Buttons haben Actions (Compiler-erzwungen)
 * - Keine Inline-Lambdas
 * - Wiederverwendbare Action-Klassen
 *
 * @author FallenStar
 * @version 2.0
 */
public class MarketPlotUi extends BasicGsUi {

    private final PlotModule plotModule;
    private final Plot plot;
    private final MarketPlot marketPlot;
    private final PlotSlotManager slotManager;
    private final PlotStorageProvider storageProvider;
    private final StorageManager storageManager;
    private final ProviderRegistry providers;
    private final boolean isOwner;

    /**
     * Erstellt eine neue MarketPlotUi.
     *
     * @param plotModule PlotModule-Instanz
     * @param plot Der Plot
     * @param marketPlot Das MarketPlot
     * @param slotManager PlotSlotManager
     * @param storageProvider PlotStorageProvider
     * @param storageManager StorageManager
     * @param providers ProviderRegistry für Owner-Checks und NPC-Verwaltung
     * @param isOwner Ob der öffnende Spieler der Besitzer ist
     */
    public MarketPlotUi(
            PlotModule plotModule,
            Plot plot,
            MarketPlot marketPlot,
            PlotSlotManager slotManager,
            PlotStorageProvider storageProvider,
            StorageManager storageManager,
            ProviderRegistry providers,
            boolean isOwner
    ) {
        super(isOwner ? "§6§lMarkt - Verwaltung" : "§e§lMarkt - Übersicht");
        this.plotModule = plotModule;
        this.plot = plot;
        this.marketPlot = marketPlot;
        this.slotManager = slotManager;
        this.storageProvider = storageProvider;
        this.storageManager = storageManager;
        this.providers = providers;
        this.isOwner = isOwner;

        buildUi();
    }

    /**
     * Baut das UI auf (Guest oder Owner Ansicht).
     */
    private void buildUi() {
        // Info-Bereich
        buildInfoSection();

        // Funktionen
        if (isOwner) {
            buildOwnerFunctions();
        } else {
            buildGuestFunctions();
        }
    }

    /**
     * Baut den Info-Bereich (statische Elemente).
     */
    private void buildInfoSection() {
        // Grundstücks-Info
        addInfoElement(
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

        // Owner: Slot-Info
        if (isOwner) {
            int currentSlots = marketPlot.getCurrentlyAvailableSlots();
            int maxSlots = marketPlot.getMaximumAvailableSlots();
            int usedSlots = (int) marketPlot.getTraderSlots().stream()
                    .filter(slot -> slot.isOccupied())
                    .count();

            addInfoElement(
                    Material.ARMOR_STAND,
                    "§6Händler-Slots",
                    List.of(
                            "§7Verfügbar: §e" + currentSlots + "§7/§e" + maxSlots,
                            "§7Belegt: §e" + usedSlots + "§7/§e" + currentSlots,
                            "§7Frei: §e" + (currentSlots - usedSlots)
                    )
            );
        }
    }

    /**
     * Baut die Owner-Funktionen.
     */
    private void buildOwnerFunctions() {
        // Händler-Slots verwalten
        int currentSlots = marketPlot.getCurrentlyAvailableSlots();
        int maxSlots = marketPlot.getMaximumAvailableSlots();
        boolean canBuyMore = marketPlot.canPurchaseMoreSlots();

        addFunctionButton(
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
                ),
                new ManageTraderSlotsAction(marketPlot)  // Type-Safe!
        );

        // Slot kaufen (conditional)
        if (canBuyMore) {
            double slotPrice = slotManager.getSlotPrice(plotModule.getConfig());
            String currency = slotManager.getSlotCurrency(plotModule.getConfig());

            addFunctionButton(
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
                    ),
                    new BuySlotAction(marketPlot)  // Type-Safe!
            );
        }

        // NPCs verwalten
        addFunctionButton(
                Material.VILLAGER_SPAWN_EGG,
                "§6§lNPCs verwalten",
                List.of(
                        "§7Verwalte Händler-NPCs",
                        "§7auf diesem Marktplatz",
                        "§7",
                        "§a§lKlicke zum Öffnen"
                ),
                new PlotActionManageNpcs(plot, providers, plotModule)  // Type-Safe Action!
        );

        // Storage verwalten
        addFunctionButton(
                Material.CHEST,
                "§6§lStorage verwalten",
                List.of(
                        "§7Verwalte das Plot-Storage",
                        "§7für Handelswaren",
                        "§7",
                        "§a§lKlicke zum Öffnen"
                ),
                new OpenStorageUiAction(plot, storageProvider, storageManager, isOwner)  // Type-Safe!
        );

        // Plot-Info
        addFunctionButton(
                Material.MAP,
                "§e§lGrundstücks-Info",
                List.of(
                        "§7Zeigt Details zu diesem Plot",
                        "§7",
                        "§a§lKlicke zum Öffnen"
                ),
                new ViewPlotInfoAction(plot)  // Type-Safe!
        );

        // Markt-Statistiken (Placeholder)
        addFunctionButton(
                Material.BOOK,
                "§e§lMarkt-Statistiken",
                List.of(
                        "§7Zeigt Umsatz und",
                        "§7Verkaufsstatistiken an",
                        "§7",
                        "§c§lNoch nicht implementiert"
                ),
                new ViewMarketStatsAction(plot)  // Type-Safe Placeholder!
        );
    }

    /**
     * Baut die Guest-Funktionen.
     */
    private void buildGuestFunctions() {
        // Händler finden (Placeholder)
        addFunctionButton(
                Material.COMPASS,
                "§e§lHändler finden",
                List.of(
                        "§7Zeigt alle aktiven Händler",
                        "§7auf diesem Marktplatz an",
                        "§7",
                        "§c§lNoch nicht implementiert"
                ),
                new FindTradersAction(plot)  // Type-Safe Placeholder!
        );

        // Verfügbare Waren (Placeholder)
        addFunctionButton(
                Material.EMERALD,
                "§a§lVerfügbare Waren",
                List.of(
                        "§7Zeigt alle verfügbaren",
                        "§7Waren und Preise an",
                        "§7",
                        "§c§lNoch nicht implementiert"
                ),
                new ViewAvailableGoodsAction(plot)  // Type-Safe Placeholder!
        );

        // Plot-Info
        addFunctionButton(
                Material.MAP,
                "§e§lGrundstücks-Info",
                List.of(
                        "§7Zeigt Details zu diesem Plot",
                        "§7",
                        "§a§lKlicke zum Öffnen"
                ),
                new ViewPlotInfoAction(plot)  // Type-Safe!
        );
    }
}
