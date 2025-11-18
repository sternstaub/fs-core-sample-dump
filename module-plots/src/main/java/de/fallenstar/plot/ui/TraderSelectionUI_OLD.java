package de.fallenstar.plot.ui;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.provider.PlotProvider;
import de.fallenstar.core.ui.LargeChestUI;
import de.fallenstar.plot.PlotModule;
import de.fallenstar.plot.registry.PlotRegistry;
import de.fallenstar.plot.slot.PlotSlot;
import de.fallenstar.plot.trader.VirtualTraderInventory;
import de.fallenstar.plot.trader.VirtualTraderInventoryManager;
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
import java.util.UUID;

/**
 * GUI zur Auswahl eines Händlers für einen Slot.
 *
 * Features:
 * - Zeigt alle verfügbaren Händler des Spielers
 * - Händler kommen aus PlotRegistry-Handelsgilden
 * - Nur eigene Händler anzeigen
 * - Kosten + Verzögerung via NPC-Reisesystem
 *
 * **Workflow:**
 * 1. Spieler wählt Slot in SlotManagementUi
 * 2. TraderSelectionUI zeigt verfügbare Händler
 * 3. Spieler wählt Händler
 * 4. Kosten berechnen (5 Sterne/Chunk)
 * 5. Händler reist zum Slot (NPC-Reisesystem)
 *
 * **Layout:**
 * - Zeile 0 (Slots 0-8): Navigation (Zurück, Info)
 * - Zeilen 1-5 (Slots 9-53): Händler-Liste
 *
 * @author FallenStar
 * @version 1.0
 */
class TraderSelectionUI extends LargeChestUI {

    private final PlotModule plotModule;
    private final PlotSlot targetSlot;
    private final PlotRegistry plotRegistry;
    private final PlotProvider plotProvider;
    private final VirtualTraderInventoryManager inventoryManager;

    /**
     * Erstellt eine neue TraderSelectionUI.
     *
     * @param plugin Plugin-Instanz
     * @param targetSlot Ziel-Slot für Händler
     * @param plotRegistry PlotRegistry
     * @param plotProvider PlotProvider
     * @param inventoryManager VirtualTraderInventoryManager
     */
    public TraderSelectionUI(
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

        // UI wird später gebaut (player-spezifisch)
    }

    /**
     * Öffnet das UI für einen Spieler.
     *
     * @param player Der Spieler
     */
    @Override
    public void open(Player player) {
        buildUI(player);
        super.open(player);
    }

    /**
     * Baut das UI auf (player-spezifisch).
     *
     * @param player Der Spieler
     */
    private void buildUI(Player player) {
        // Zeile 0: Navigation
        buildNavigationBar();

        // Zeilen 1-5: Händler-Liste
        buildTraderList(player);
    }

    /**
     * Baut die Navigation-Bar.
     */
    private void buildNavigationBar() {
        // Slot 0: Zurück
        ItemStack backButton = createNavigationItem(
                Material.ARROW,
                "§cZurück",
                List.of("§7Zurück zur Slot-Verwaltung")
        );
        setItem(0, backButton, player -> {
            // TODO: Öffne SlotManagementUi
            player.closeInventory();
        });

        // Slot 4: Info
        ItemStack infoButton = createNavigationItem(
                Material.BOOK,
                "§e§lHändler-Auswahl",
                List.of(
                        "§7Ziel-Slot: §e" + targetSlot.getSlotType().getDisplayName(),
                        "§7Position: §e" + formatLocation(targetSlot.getLocation()),
                        "§7",
                        "§7Wähle einen Händler aus",
                        "§7deinen Handelsgilden"
                )
        );
        setItem(4, infoButton);
    }

    /**
     * Baut die Händler-Liste.
     *
     * @param player Der Spieler
     */
    private void buildTraderList(Player player) {
        // Hole alle Handelsgilden des Spielers
        List<String> guildPlotIds = plotRegistry.getPlotIdsByType(PlotRegistry.PlotType.MERCHANT_GUILD);

        if (guildPlotIds.isEmpty()) {
            // Keine Handelsgilden vorhanden
            ItemStack emptyItem = createInfoItem(
                    "§7Keine Handelsgilden gefunden",
                    List.of("§7Erstelle zuerst eine Handelsgilde", "§7(COMMERCIAL-Plot in Towny)")
            );
            setItem(22, emptyItem);
            return;
        }

        int slotIndex = 9;  // Start bei Zeile 1

        for (String plotId : guildPlotIds) {
            if (slotIndex >= 54) break;  // Max. Slots erreicht

            try {
                // Hole Plot via PlotProvider (theoretisch - hier nur Mock)
                // Plot guildPlot = plotProvider.getPlot(plotId);

                // Prüfe ob Spieler Besitzer ist (Mock)
                // boolean isOwner = plotProvider.isOwner(guildPlot, player);

                // if (!isOwner) continue;  // Nur eigene Handelsgilden

                // Erstelle Händler-Item
                ItemStack traderItem = createTraderItem(plotId, player.getUniqueId());

                setItem(slotIndex, traderItem, p -> {
                    assignTraderToSlot(p, plotId);
                });

                slotIndex++;

            } catch (Exception e) {
                // Fehler beim Laden des Plots → Überspringen
                plotModule.getLogger().warning("Fehler beim Laden von Plot " + plotId + ": " + e.getMessage());
            }
        }

        if (slotIndex == 9) {
            // Keine Händler gefunden
            ItemStack emptyItem = createInfoItem(
                    "§7Keine Händler verfügbar",
                    List.of("§7Du hast noch keine Händler", "§7in deinen Handelsgilden")
            );
            setItem(22, emptyItem);
        }
    }

    /**
     * Erstellt ein Händler-Item.
     *
     * @param guildPlotId Plot-ID der Handelsgilde
     * @param playerId Player-UUID
     * @return ItemStack
     */
    private ItemStack createTraderItem(String guildPlotId, UUID playerId) {
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
     * Weist einen Händler einem Slot zu.
     *
     * @param player Der Spieler
     * @param guildPlotId Plot-ID der Handelsgilde
     */
    private void assignTraderToSlot(Player player, String guildPlotId) {
        // TODO: NPC-Reisesystem-Integration
        player.sendMessage(Component.text("✗ NPC-Reisesystem noch nicht implementiert!", NamedTextColor.RED));
        player.sendMessage(Component.text("Wird in Phase 4 verfügbar sein", NamedTextColor.YELLOW));

        player.sendMessage(Component.text("Händler würde von " + guildPlotId + " zu Slot " +
                targetSlot.getSlotId().toString().substring(0, 8) + " reisen", NamedTextColor.GRAY));

        player.closeInventory();
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
