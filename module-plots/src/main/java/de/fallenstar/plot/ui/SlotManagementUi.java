package de.fallenstar.plot.ui;

import de.fallenstar.core.ui.container.GenericUiLargeChest;
import de.fallenstar.core.ui.element.ClickableUiElement;
import de.fallenstar.core.ui.element.StaticUiElement;
import de.fallenstar.core.ui.element.navigation.CloseButton;
import de.fallenstar.core.ui.row.BasicUiRowForContent;
import de.fallenstar.plot.action.BuySlotAction;
import de.fallenstar.plot.action.OpenTraderSelectionAction;
import de.fallenstar.plot.action.RemoveTraderFromSlotAction;
import de.fallenstar.plot.registry.PlotRegistry;
import de.fallenstar.plot.slot.PlotSlot;
import de.fallenstar.plot.slot.SlottedPlot;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Type-Safe GUI zur Verwaltung von Händler-Slots.
 *
 * **Migration:** Ersetzt alte SlotManagementUI (264 Zeilen) durch
 * type-safe GenericUiLargeChest-basierte Implementierung.
 *
 * **Features:**
 * - Zeigt alle verfügbaren Slots auf dem Grundstück
 * - Händler auf Slots platzieren (aus PlotRegistry-Handelsgilden)
 * - Händler von Slots entfernen
 * - Neue Slots kaufen (Kosten konfigurierbar)
 *
 * **Workflow:**
 * 1. Spieler öffnet /plot gui auf Grundstück mit Trader-Slots
 * 2. Klickt auf "Händler-Slots verwalten"
 * 3. Sieht Liste freier Slots
 * 4. Klickt auf Slot → Händler-Auswahl-UI
 * 5. Wählt Händler aus PlotRegistry-Handelsgilden
 * 6. Händler reist zum Slot (NPC-Reisesystem)
 *
 * **Layout:**
 * - Row 0 (Slots 0-8): Navigation (Close, Info, Buy Slot)
 * - Row 1-5 (Slots 9-53): Slot-Buttons
 *
 * **Type-Safety:**
 * - OpenTraderSelectionAction für freie Slots
 * - RemoveTraderFromSlotAction für belegte Slots
 * - BuySlotAction für Slot-Kauf
 *
 * @author FallenStar
 * @version 2.0
 */
public class SlotManagementUi extends GenericUiLargeChest {

    private final Plugin plugin;
    private final SlottedPlot plot;
    private final PlotRegistry plotRegistry;

    /**
     * Erstellt eine neue SlotManagementUi.
     *
     * @param plugin Plugin-Instanz
     * @param plot Der Plot (muss SlottedPlot sein)
     * @param plotRegistry PlotRegistry (für Händler-Suche)
     */
    public SlotManagementUi(Plugin plugin, SlottedPlot plot, PlotRegistry plotRegistry) {
        super("§6§lHändler-Slots verwalten");
        this.plugin = plugin;
        this.plot = plot;
        this.plotRegistry = plotRegistry;

        // Initialisiere Rows
        for (int i = 0; i < ROW_COUNT; i++) {
            setRow(i, new BasicUiRowForContent());
        }

        buildUi();
    }

    /**
     * Baut das UI auf.
     */
    private void buildUi() {
        buildNavigationBar();
        buildSlotList();
    }

    /**
     * Baut die Navigation-Bar (Row 0).
     */
    private void buildNavigationBar() {
        var row = getRow(0);

        // Slot 0: Close
        row.setElement(0, CloseButton.create(this));

        // Slot 4: Info
        int usedSlots = plot.getUsedSlots();
        int maxSlots = plot.getMaxSlots();

        row.setElement(4, new StaticUiElement(
                createInfoItem(
                        Material.BOOK,
                        "§e§lSlot-Übersicht",
                        List.of(
                                "§7Belegt: §e" + usedSlots + "§7/§e" + maxSlots,
                                "§7Frei: §a" + plot.getFreeSlots(),
                                "§7",
                                "§7Klicke auf einen Slot um",
                                "§7einen Händler zu platzieren"
                        )
                )
        ));

        // Slot 8: Neuen Slot kaufen (falls verfügbar)
        if (plot.hasFreeSlots()) {
            row.setElement(8, new ClickableUiElement.CustomButton<>(
                    createInfoItem(
                            Material.EMERALD,
                            "§a§lNeuen Slot kaufen",
                            List.of(
                                    "§7Kosten: §e100 Sterne",
                                    "§7",
                                    "§a§lKlicke zum Kaufen"
                            )
                    ),
                    new BuySlotAction(plot)  // Type-Safe!
            ));
        }
    }

    /**
     * Baut die Slot-Liste (Rows 1-5).
     */
    private void buildSlotList() {
        List<PlotSlot> slots = plot.getAllSlots();

        int slotIndex = 9;  // Start bei Row 1, Slot 0

        for (PlotSlot plotSlot : slots) {
            if (slotIndex >= 54) break;  // Max. Slots erreicht

            ItemStack slotItem = createSlotItem(plotSlot);

            if (plotSlot.isOccupied()) {
                // Slot belegt → Händler-Info + Entfernen-Option
                var button = new ClickableUiElement.CustomButton<>(
                        slotItem,
                        new RemoveTraderFromSlotAction(plotSlot)  // Type-Safe!
                );
                setElement(slotIndex, button);
            } else {
                // Slot frei → Händler-Auswahl
                var button = new ClickableUiElement.CustomButton<>(
                        slotItem,
                        new OpenTraderSelectionAction(plotSlot)  // Type-Safe!
                );
                setElement(slotIndex, button);
            }

            slotIndex++;
        }
    }

    /**
     * Erstellt ein Slot-Item.
     *
     * @param slot Der PlotSlot
     * @return ItemStack
     */
    private ItemStack createSlotItem(PlotSlot slot) {
        Material material = slot.isOccupied() ? Material.PLAYER_HEAD : Material.ARMOR_STAND;
        String name = slot.isOccupied() ?
                "§a§lSlot #" + slot.getSlotId().toString().substring(0, 8) + " §7(Belegt)" :
                "§7§lSlot #" + slot.getSlotId().toString().substring(0, 8) + " §7(Frei)";

        List<String> lore = new ArrayList<>();
        lore.add("§7Typ: §e" + slot.getSlotType().getDisplayName());
        lore.add("§7Position: §e" + formatLocation(slot.getLocation()));
        lore.add("");

        if (slot.isOccupied()) {
            lore.add("§7Händler: §e" + slot.getAssignedNPC().map(uuid -> uuid.toString().substring(0, 8)).orElse("Unbekannt"));
            lore.add("");
            lore.add("§c§lKlicke um zu entfernen");
        } else {
            lore.add("§a§lKlicke um Händler zu platzieren");
        }

        return createInfoItem(material, name, lore);
    }

    /**
     * Erstellt ein Info-Item.
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
