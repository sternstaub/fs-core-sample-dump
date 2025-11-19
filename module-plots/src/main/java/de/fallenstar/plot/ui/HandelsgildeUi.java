package de.fallenstar.plot.ui;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.registry.ProviderRegistry;
import de.fallenstar.core.ui.container.BasicGsUi;
import de.fallenstar.plot.PlotModule;
import de.fallenstar.plot.action.PlotActionManageNpcs;
import de.fallenstar.plot.action.OpenStorageUiAction;
import de.fallenstar.plot.action.SetPriceAction;
import de.fallenstar.plot.action.ViewPricesAction;
import de.fallenstar.plot.storage.manager.StorageManager;
import de.fallenstar.plot.storage.provider.PlotStorageProvider;
import org.bukkit.Material;

import java.util.List;

/**
 * Type-Safe Handelsgilde-UI mit Guest/Owner Ansichten.
 *
 * **⚠️ DEPRECATED (Sprint 19 - Phase 3):**
 * Diese Klasse wurde durch das GuiBuilder-Pattern ersetzt!
 * TradeguildPlot nutzt jetzt getAvailablePlotActions() + GuiBuilder
 * für vollständig self-rendering UIs.
 *
 * **Migration (Sprint 17 → Sprint 19):**
 *
 * Sprint 17 (ALT):
 * <pre>
 * HandelsgildeUi ui = new HandelsgildeUi(plot, ...);
 * ui.open(player);
 * </pre>
 *
 * Sprint 17-19 (Übergang - UiTarget):
 * <pre>
 * if (plot instanceof UiTarget uiTarget) {
 *     uiTarget.createUi(player, context).ifPresent(ui -> ui.open(player));
 * }
 * </pre>
 *
 * Sprint 19 (NEU - GuiBuilder):
 * <pre>
 * if (plot instanceof TradeguildPlot tradeguildPlot) {
 *     List&lt;PlotAction&gt; actions = tradeguildPlot.getAvailablePlotActions(player);
 *     PageableBasicUi ui = GuiBuilder.buildFrom(player, "§6Plot-Verwaltung", actions);
 *     ui.open(player);
 * }
 * </pre>
 *
 * **Vorteile des GuiBuilder-Systems:**
 * - **Universal:** Funktioniert für ALLE Plot-Typen
 * - **Self-Rendering:** Actions kennen Display + Logik + Permissions
 * - **Automatisch:** Permission-Checks → Lore-Updates
 * - **Type-Safe:** Intersection Types (GuiRenderable & UiAction)
 * - **DRY:** Keine Duplikation zwischen Action-Display und Action-Logik
 * - **SOLID:** Single Responsibility, Open/Closed, Dependency Inversion
 *
 * **Architektur-Evolution:**
 * - Sprint 15: UiActionInfo (Metadaten) + switch(actionId)
 * - Sprint 17: PlotAction (Command Pattern) + UiTarget
 * - Sprint 18: GuiRenderable Interface + GuiBuilder
 * - Sprint 19: Vollständige Migration → HandelsgildeUi obsolet
 *
 * **Legacy-Dokumentation:**
 * - Ersetzt alte HandelsgildeUI (639 Zeilen) durch type-safe BasicGsUi (~150 Zeilen)
 * - Guest-Ansicht: Preisliste, Shop, Grundstücks-Info
 * - Owner-Ansicht: Preise, Storage, NPCs, Händler-Slots, Info
 *
 * @author FallenStar
 * @version 3.0
 * @deprecated Ersetzt durch TradeguildPlot.getAvailablePlotActions() + GuiBuilder (Sprint 19)
 * @see de.fallenstar.core.ui.builder.GuiBuilder
 * @see de.fallenstar.core.ui.element.PlotAction
 * @see de.fallenstar.plot.model.TradeguildPlot#getAvailablePlotActions
 * @see de.fallenstar.plot.model.TradeguildPlot#createUi
 */
@Deprecated(since = "Sprint 19", forRemoval = true)
public class HandelsgildeUi extends BasicGsUi {

    private final Plot plot;
    private final PlotStorageProvider storageProvider;
    private final StorageManager storageManager;
    private final ProviderRegistry providers;
    private final PlotModule plotModule;
    private final boolean isOwner;

    /**
     * Erstellt eine neue HandelsgildeUi.
     *
     * @param plot Der Plot
     * @param storageProvider PlotStorageProvider für Storage-Zugriff
     * @param storageManager StorageManager für Storage-Verwaltung
     * @param providers ProviderRegistry für Owner-Checks und NPC-Verwaltung
     * @param plotModule Das PlotModule für NPC-Manager-Zugriff
     * @param isOwner Ob der öffnende Spieler der Besitzer ist
     */
    public HandelsgildeUi(
            Plot plot,
            PlotStorageProvider storageProvider,
            StorageManager storageManager,
            ProviderRegistry providers,
            PlotModule plotModule,
            boolean isOwner
    ) {
        super(isOwner ? "§6§lHandelsgilde - Verwaltung" : "§e§lHandelsgilde - Shop");
        this.plot = plot;
        this.storageProvider = storageProvider;
        this.storageManager = storageManager;
        this.providers = providers;
        this.plotModule = plotModule;
        this.isOwner = isOwner;

        buildUi();
    }

    /**
     * Baut das UI auf (Guest oder Owner Ansicht).
     */
    private void buildUi() {
        // Info-Bereich (Zeile 0)
        buildInfoSection();

        // Funktionen (Zeilen 1-2)
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
                Material.GOLD_INGOT,
                isOwner ? "§e§lVerwaltungsmodus" : "§e§lShop-Modus",
                isOwner ?
                        List.of(
                                "§7Du bist der Besitzer dieses Plots",
                                "§7",
                                "§eKlicke auf Buttons um Funktionen zu nutzen"
                        ) :
                        List.of(
                                "§7Willkommen im Handelsgilde-Shop!",
                                "§7",
                                "§7Kaufe Items zu den angezeigten Preisen"
                        )
        );

        // Plot-Name/Identifier
        addInfoElement(
                Material.MAP,
                "§6§lGrundstück",
                List.of(
                        "§7Name: §e" + plot.getIdentifier(),
                        "§7Typ: §eHandelsgilde"
                )
        );
    }

    /**
     * Baut die Owner-Funktionen.
     */
    private void buildOwnerFunctions() {
        // Preise anzeigen
        addFunctionButton(
                Material.BOOK,
                "§e§lPreise anzeigen",
                List.of(
                        "§7Zeigt alle definierten Preise",
                        "§7in diesem Handelsgilde-Grundstück",
                        "§7",
                        "§a§lKlicke zum Öffnen"
                ),
                new ViewPricesAction(plot)  // Type-Safe Action!
        );

        // Preise setzen
        addFunctionButton(
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
                ),
                new SetPriceAction(plot)  // Type-Safe Action!
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

        // NPCs verwalten
        addFunctionButton(
                Material.VILLAGER_SPAWN_EGG,
                "§6§lNPCs verwalten",
                List.of(
                        "§7Verwalte Gildenhändler und",
                        "§7Spielerhändler auf diesem Plot",
                        "§7",
                        "§a§lKlicke zum Öffnen"
                ),
                new PlotActionManageNpcs(plot, providers, plotModule)  // Type-Safe Action!
        );

        // Händler-Slots (Placeholder)
        addFunctionButton(
                Material.ARMOR_STAND,
                "§6§lHändler-Slots",
                List.of(
                        "§7Verwalte Händler-Slots",
                        "§7auf diesem Grundstück",
                        "§7",
                        "§c§lRoadmap: Sprint 11-12"
                ),
                new ManageSlotsAction(plot)  // Type-Safe Placeholder!
        );

        // Grundstücks-Info
        addFunctionButton(
                Material.MAP,
                "§e§lGrundstücks-Info",
                List.of(
                        "§7Zeigt Details zu diesem Plot",
                        "§7",
                        "§a§lKlicke zum Öffnen"
                ),
                new ViewPlotInfoAction(plot)  // Type-Safe Action!
        );
    }

    /**
     * Baut die Guest-Funktionen.
     */
    private void buildGuestFunctions() {
        // Preisliste anzeigen
        addFunctionButton(
                Material.BOOK,
                "§e§lPreisliste anzeigen",
                List.of(
                        "§7Zeigt alle verfügbaren Items",
                        "§7und deren Preise an",
                        "§7",
                        "§a§lKlicke zum Öffnen"
                ),
                new ViewPricesAction(plot)  // Type-Safe Action!
        );

        // Shop (Placeholder)
        addFunctionButton(
                Material.EMERALD,
                "§a§lItems kaufen",
                List.of(
                        "§7Öffnet den Shop",
                        "§7um Items zu kaufen",
                        "§7",
                        "§c§lNoch nicht implementiert"
                ),
                new OpenShopAction(plot)  // Type-Safe Placeholder!
        );

        // Grundstücks-Info
        addFunctionButton(
                Material.MAP,
                "§e§lGrundstücks-Info",
                List.of(
                        "§7Zeigt Details zu diesem Plot",
                        "§7",
                        "§a§lKlicke zum Öffnen"
                ),
                new ViewPlotInfoAction(plot)  // Type-Safe Action!
        );
    }
}
