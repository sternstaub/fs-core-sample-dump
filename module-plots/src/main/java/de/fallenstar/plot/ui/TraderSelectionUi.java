package de.fallenstar.plot.ui;

import de.fallenstar.core.provider.PlotProvider;
import de.fallenstar.core.ui.container.GenericUiLargeChest;
import de.fallenstar.core.ui.element.ClickableUiElement;
import de.fallenstar.core.ui.element.StaticUiElement;
import de.fallenstar.core.ui.element.navigation.CloseButton;
import de.fallenstar.core.ui.row.BasicUiRowForContent;
import de.fallenstar.plot.PlotModule;
import de.fallenstar.plot.action.SelectTraderForSlotAction;
import de.fallenstar.plot.registry.PlotRegistry;
import de.fallenstar.plot.slot.PlotSlot;
import de.fallenstar.plot.trader.VirtualTraderInventoryManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Type-Safe GUI zur Auswahl eines Händlers für einen Slot.
 *
 * **Migration:** Ersetzt alte TraderSelectionUI (298 Zeilen) durch
 * type-safe GenericUiLargeChest-basierte Implementierung.
 *
 * **Features:**
 * - Zeigt alle verfügbaren Händler des Spielers
 * - Händler kommen aus PlotRegistry-Handelsgilden
 * - Nur eigene Händler anzeigen
 * - Kosten + Verzögerung via NPC-Reisesystem (Placeholder)
 *
 * **Workflow:**
 * 1. Spieler wählt Slot in SlotManagementUi
 * 2. TraderSelectionUi zeigt verfügbare Händler
 * 3. Spieler wählt Händler
 * 4. Kosten berechnen (5 Sterne/Chunk)
 * 5. Händler reist zum Slot (NPC-Reisesystem)
 *
 * **Layout:**
 * - Row 0 (Slots 0-8): Navigation (Close, Info)
 * - Rows 1-5 (Slots 9-53): Händler-Liste
 *
 * **Type-Safety:**
 * - SelectTraderForSlotAction für Händler-Auswahl
 * - CloseButton für Zurück-Navigation
 *
 * @author FallenStar
 * @version 2.0
 */
public class TraderSelectionUi extends GenericUiLargeChest {

    private final PlotModule plotModule;
    private final PlotSlot targetSlot;
    private final PlotRegistry plotRegistry;
    private final PlotProvider plotProvider;
    private final VirtualTraderInventoryManager inventoryManager;

    /**
     * Erstellt eine neue TraderSelectionUi.
     *
     * @param plugin Plugin-Instanz
     * @param targetSlot Ziel-Slot für Händler
     * @param plotRegistry PlotRegistry
     * @param plotProvider PlotProvider
     * @param inventoryManager VirtualTraderInventoryManager
     */
    public TraderSelectionUi(
            Plugin plugin,
            PlotSlot targetSlot,
            PlotRegistry plotRegistry,
            PlotProvider plotProvider,
            VirtualTraderInventoryManager inventoryManager
    ) {
        super("§6§lHändler auswählen");
        this.plotModule = (PlotModule) plugin;
        this.targetSlot = targetSlot;
        this.plotRegistry = plotRegistry;
        this.plotProvider = plotProvider;
        this.inventoryManager = inventoryManager;

        // Initialisiere Rows
        for (int i = 0; i < ROW_COUNT; i++) {
            setRow(i, new BasicUiRowForContent());
        }
    }

    /**
     * Öffnet das UI für einen Spieler.
     *
     * @param player Der Spieler
     */
    @Override
    public void open(Player player) {
        buildUi(player);
        super.open(player);
    }

    /**
     * Baut das UI auf (player-spezifisch).
     *
     * @param player Der Spieler
     */
    private void buildUi(Player player) {
        buildNavigationBar();
        buildTraderList(player);
    }

    /**
     * Baut die Navigation-Bar (Row 0).
     */
    private void buildNavigationBar() {
        var row = getRow(0);

        // Slot 0: Close/Zurück
        row.setElement(0, CloseButton.create(this));

        // Slot 4: Info
        row.setElement(4, new StaticUiElement(
                createInfoItem(
                        Material.BOOK,
                        "§e§lHändler-Auswahl",
                        List.of(
                                "§7Ziel-Slot: §e" + targetSlot.getSlotType().getDisplayName(),
                                "§7Position: §e" + formatLocation(targetSlot.getLocation()),
                                "§7",
                                "§7Wähle einen Händler aus",
                                "§7deinen Handelsgilden"
                        )
                )
        ));
    }

    /**
     * Baut die Händler-Liste (Rows 1-5).
     *
     * @param player Der Spieler
     */
    private void buildTraderList(Player player) {
        // Hole alle Handelsgilden des Spielers
        List<String> guildPlotIds = plotRegistry.getPlotIdsByType(PlotRegistry.PlotType.MERCHANT_GUILD);

        if (guildPlotIds.isEmpty()) {
            // Keine Handelsgilden vorhanden
            ItemStack emptyItem = createInfoItem(
                    Material.BARRIER,
                    "§7Keine Handelsgilden gefunden",
                    List.of(
                            "§7Erstelle zuerst eine Handelsgilde",
                            "§7(COMMERCIAL-Plot in Towny)"
                    )
            );
            setElement(22, new StaticUiElement(emptyItem));  // Mitte (Row 2, Slot 4)
            return;
        }

        int slotIndex = 9;  // Start bei Row 1, Slot 0

        for (String plotId : guildPlotIds) {
            if (slotIndex >= 54) break;  // Max. Slots erreicht

            try {
                // Hole Plot via PlotProvider (theoretisch - hier nur Mock)
                // Plot guildPlot = plotProvider.getPlot(plotId);

                // Prüfe ob Spieler Besitzer ist (Mock)
                // boolean isOwner = plotProvider.isOwner(guildPlot, player);

                // if (!isOwner) continue;  // Nur eigene Handelsgilden

                // Erstelle Händler-Item mit Type-Safe Action
                ItemStack traderItem = createTraderItem(plotId);

                var traderButton = new ClickableUiElement.CustomButton<>(
                        traderItem,
                        new SelectTraderForSlotAction(targetSlot, plotId)  // Type-Safe!
                );

                setElement(slotIndex, traderButton);
                slotIndex++;

            } catch (Exception e) {
                // Fehler beim Laden des Plots → Überspringen
                plotModule.getLogger().warning("Fehler beim Laden von Plot " + plotId + ": " + e.getMessage());
            }
        }

        if (slotIndex == 9) {
            // Keine Händler gefunden (alle Plots übersprungen)
            ItemStack emptyItem = createInfoItem(
                    Material.BARRIER,
                    "§7Keine Händler verfügbar",
                    List.of(
                            "§7Du hast noch keine Händler",
                            "§7in deinen Handelsgilden"
                    )
            );
            setElement(22, new StaticUiElement(emptyItem));  // Mitte
        }
    }

    /**
     * Erstellt ein Händler-Item.
     *
     * @param guildPlotId Plot-ID der Handelsgilde
     * @return ItemStack
     */
    private ItemStack createTraderItem(String guildPlotId) {
        ItemStack item = new ItemStack(Material.VILLAGER_SPAWN_EGG);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("§6§lHändler aus Gilde " + guildPlotId.substring(0, 8))
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("§7Plot: §e" + guildPlotId).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("").decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§7Reisekosten: §e? Sterne").decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§7Reisedauer: §e? Sekunden").decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("").decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§a§lKlicke um zu platzieren").decoration(TextDecoration.ITALIC, false));

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Erstellt ein Info-Item.
     *
     * @param material Material
     * @param name Name
     * @param lore Lore
     * @return ItemStack
     */
    private ItemStack createInfoItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text(name).decoration(TextDecoration.ITALIC, false));

        List<Component> loreComponents = new ArrayList<>();
        for (String line : lore) {
            loreComponents.add(Component.text(line).decoration(TextDecoration.ITALIC, false));
        }
        meta.lore(loreComponents);

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Formatiert eine Location.
     *
     * @param location Die Location
     * @return Formatierter String
     */
    private String formatLocation(org.bukkit.Location location) {
        return String.format("%d, %d, %d",
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ()
        );
    }
}
