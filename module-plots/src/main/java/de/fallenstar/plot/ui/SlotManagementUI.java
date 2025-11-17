package de.fallenstar.plot.ui;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.ui.LargeChestUI;
import de.fallenstar.plot.PlotModule;
import de.fallenstar.plot.registry.PlotRegistry;
import de.fallenstar.plot.slot.PlotSlot;
import de.fallenstar.plot.slot.SlottedPlot;
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
 * GUI zur Verwaltung von Händler-Slots.
 *
 * Features:
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
 * - Zeile 0 (Slots 0-8): Navigation (Zurück, Info)
 * - Zeilen 1-5 (Slots 9-53): Slot-Buttons
 *
 * @author FallenStar
 * @version 1.0
 */
public class SlotManagementUI extends LargeChestUI {

    private final PlotModule plotModule;
    private final SlottedPlot plot;
    private final PlotRegistry plotRegistry;

    /**
     * Erstellt eine neue SlotManagementUI.
     *
     * @param plugin Plugin-Instanz
     * @param plot Der Plot (muss SlottedPlot sein)
     * @param plotRegistry PlotRegistry (für Händler-Suche)
     */
    public SlotManagementUI(Plugin plugin, SlottedPlot plot, PlotRegistry plotRegistry) {
        super("§6§lHändler-Slots verwalten");
        this.plotModule = (PlotModule) plugin;
        this.plot = plot;
        this.plotRegistry = plotRegistry;

        buildUI();
    }

    /**
     * Baut das UI auf.
     */
    private void buildUI() {
        // Zeile 0: Navigation
        buildNavigationBar();

        // Zeilen 1-5: Slot-Liste
        buildSlotList();
    }

    /**
     * Baut die Navigation-Bar.
     */
    private void buildNavigationBar() {
        // Slot 0: Zurück
        ItemStack backButton = createNavigationItem(
                Material.ARROW,
                "§cZurück",
                List.of("§7Zurück zum Hauptmenü")
        );
        setItem(0, backButton, player -> {
            // TODO: Öffne HandelsgildeUI
            player.closeInventory();
        });

        // Slot 4: Info
        int usedSlots = plot.getUsedSlots();
        int maxSlots = plot.getMaxSlots();

        ItemStack infoButton = createNavigationItem(
                Material.BOOK,
                "§e§lSlot-Übersicht",
                List.of(
                        "§7Belegt: §e" + usedSlots + "§7/§e" + maxSlots,
                        "§7Frei: §a" + plot.getFreeSlots(),
                        "§7",
                        "§7Klicke auf einen Slot um",
                        "§7einen Händler zu platzieren"
                )
        );
        setItem(4, infoButton);

        // Slot 8: Neuen Slot kaufen (falls verfügbar)
        if (plot.hasFreeSlots()) {
            ItemStack buySlotButton = createNavigationItem(
                    Material.EMERALD,
                    "§a§lNeuen Slot kaufen",
                    List.of(
                            "§7Kosten: §e100 Sterne",
                            "§7",
                            "§a§lKlicke zum Kaufen"
                    )
            );
            setItem(8, buySlotButton, player -> {
                // TODO: Slot-Kauf-Logik
                player.sendMessage("§c§lSlot-Kauf noch nicht implementiert!");
                player.sendMessage("§7Geplant für Sprint 11-12");
            });
        }
    }

    /**
     * Baut die Slot-Liste.
     */
    private void buildSlotList() {
        List<PlotSlot> slots = plot.getAllSlots();

        int slotIndex = 9;  // Start bei Zeile 1

        for (PlotSlot plotSlot : slots) {
            if (slotIndex >= 54) break;  // Max. Slots erreicht

            ItemStack slotItem = createSlotItem(plotSlot);

            if (plotSlot.isOccupied()) {
                // Slot belegt → Händler-Info + Entfernen-Option
                setItem(slotIndex, slotItem, player -> {
                    player.sendMessage("§7Händler: §e" + plotSlot.getAssignedNPC().orElse(null));
                    player.sendMessage("§7Rechtsklick um zu entfernen (noch nicht implementiert)");
                });
            } else {
                // Slot frei → Händler-Auswahl
                setItem(slotIndex, slotItem, player -> {
                    openTraderSelection(player, plotSlot);
                });
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
            lore.add("§7Händler: §e" + slot.getAssignedNPC().orElse(null));
            lore.add("");
            lore.add("§c§lKlicke um zu entfernen");
        } else {
            lore.add("§a§lKlicke um Händler zu platzieren");
        }

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
     * Öffnet die Händler-Auswahl für einen Slot.
     *
     * @param player Der Spieler
     * @param slot Der PlotSlot
     */
    private void openTraderSelection(Player player, PlotSlot slot) {
        // Suche verfügbare Handelsgilden
        List<String> guildPlotIds = plotRegistry.getPlotIdsByType(PlotRegistry.PlotType.MERCHANT_GUILD);

        if (guildPlotIds.isEmpty()) {
            player.sendMessage(Component.text("✗ Keine Handelsgilden gefunden!", NamedTextColor.RED));
            player.sendMessage(Component.text("Erstelle zuerst eine Handelsgilde (COMMERCIAL-Plot)", NamedTextColor.YELLOW));
            return;
        }

        // TODO: Öffne TraderSelectionUI
        player.sendMessage("§a§lHändler-Auswahl:");
        player.sendMessage("§7Verfügbare Handelsgilden: " + guildPlotIds.size());

        for (String plotId : guildPlotIds) {
            player.sendMessage("§7- " + plotId);
        }

        player.sendMessage("§c§lTraderSelectionUI noch nicht implementiert!");
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
