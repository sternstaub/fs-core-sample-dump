package de.fallenstar.plot.ui;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.ui.container.BasicGsUi;
import de.fallenstar.plot.action.*;
import de.fallenstar.plot.storage.manager.StorageManager;
import de.fallenstar.plot.storage.provider.PlotStorageProvider;
import org.bukkit.Material;

import java.util.List;

/**
 * Type-Safe Handelsgilde-UI mit Guest/Owner Ansichten.
 *
 * **Migration:** Ersetzt alte HandelsgildeUI (639 Zeilen) durch
 * type-safe BasicGsUi-basierte Implementierung (~150 Zeilen).
 *
 * **Guest-Ansicht (Besucher):**
 * - Preisliste anzeigen
 * - Shop öffnen (Placeholder)
 * - Grundstücks-Info
 *
 * **Owner-Ansicht (Besitzer):**
 * - Preise anzeigen/setzen
 * - Storage verwalten
 * - NPCs verwalten (Placeholder)
 * - Händler-Slots (Placeholder)
 * - Grundstücks-Info
 *
 * **Type-Safety:**
 * - Alle Buttons haben Actions (Compiler-erzwungen)
 * - Keine Inline-Lambdas
 * - Wiederverwendbare Action-Klassen
 *
 * @author FallenStar
 * @version 2.0
 */
public class HandelsgildeUi extends BasicGsUi {

    private final Plot plot;
    private final PlotStorageProvider storageProvider;
    private final StorageManager storageManager;
    private final boolean isOwner;

    /**
     * Erstellt eine neue HandelsgildeUi.
     *
     * @param plot Der Plot
     * @param storageProvider PlotStorageProvider für Storage-Zugriff
     * @param storageManager StorageManager für Storage-Verwaltung
     * @param isOwner Ob der öffnende Spieler der Besitzer ist
     */
    public HandelsgildeUi(
            Plot plot,
            PlotStorageProvider storageProvider,
            StorageManager storageManager,
            boolean isOwner
    ) {
        super(isOwner ? "§6§lHandelsgilde - Verwaltung" : "§e§lHandelsgilde - Shop");
        this.plot = plot;
        this.storageProvider = storageProvider;
        this.storageManager = storageManager;
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

        // NPCs verwalten (Placeholder)
        addFunctionButton(
                Material.VILLAGER_SPAWN_EGG,
                "§6§lNPCs verwalten",
                List.of(
                        "§7Verwalte Gildenhändler und",
                        "§7Spielerhändler auf diesem Plot",
                        "§7",
                        "§c§lNoch nicht implementiert"
                ),
                new ManageNpcsAction(plot)  // Type-Safe Placeholder!
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
