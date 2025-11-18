package de.fallenstar.plot.ui;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.ui.LargeChestUI;
import de.fallenstar.plot.storage.manager.StorageManager;
import de.fallenstar.plot.storage.model.ChestData;
import de.fallenstar.plot.storage.model.PlotStorage;
import de.fallenstar.plot.storage.provider.PlotStorageProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Plot-Storage Verwaltungs-UI.
 *
 * Zeigt alle gespeicherten Materialien auf einem Grundstück an.
 *
 * **Features:**
 * - Material-Liste mit Mengen (sortiert)
 * - Receiver-Kiste Status
 * - Storage-Scan Funktion (Owner)
 * - Pagination für viele Items
 *
 * **Layout:**
 * - Zeile 0 (Slots 0-8): Navigation (Schließen, Scan, Receiver-Info)
 * - Zeilen 1-4 (Slots 9-44): Material-Liste (bis zu 36 Items)
 * - Zeile 5 (Slots 45-53): Pagination + Info
 *
 * @author FallenStar
 * @version 1.0
 */
class PlotStorageUI extends LargeChestUI {

    private final Plot plot;
    private final PlotStorage plotStorage;
    private final PlotStorageProvider storageProvider;
    private final StorageManager storageManager;
    private final boolean isOwner;

    private int currentPage = 0;
    private final int itemsPerPage = 36;

    /**
     * Erstellt eine neue PlotStorageUI.
     *
     * @param plot Der Plot
     * @param plotStorage Das PlotStorage-Objekt
     * @param storageProvider PlotStorageProvider
     * @param storageManager StorageManager
     * @param isOwner Ob Spieler Owner ist
     */
    public PlotStorageUI(
            Plot plot,
            PlotStorage plotStorage,
            PlotStorageProvider storageProvider,
            StorageManager storageManager,
            boolean isOwner
    ) {
        super("§6§lPlot-Storage");
        this.plot = plot;
        this.plotStorage = plotStorage;
        this.storageProvider = storageProvider;
        this.storageManager = storageManager;
        this.isOwner = isOwner;

        buildUI();
    }

    /**
     * Baut das UI auf.
     */
    private void buildUI() {
        // Zeile 0: Navigation
        buildNavigationBar();

        // Zeilen 1-4: Material-Liste
        buildMaterialList();

        // Zeile 5: Pagination + Info
        buildPaginationBar();
    }

    /**
     * Baut die Navigation-Bar (Zeile 0).
     */
    private void buildNavigationBar() {
        // Slot 0: Schließen
        ItemStack closeButton = createNavigationItem(
                Material.BARRIER,
                "§cSchließen",
                List.of("§7Klicke um das UI zu schließen")
        );
        setItem(0, closeButton, player -> player.closeInventory());

        // Slot 4: Storage-Info
        Set<Material> materials = plotStorage.getAllMaterials();
        int totalChests = plotStorage.getAllChests().size();
        long lastUpdate = plotStorage.getLastUpdate();
        long secondsAgo = (System.currentTimeMillis() - lastUpdate) / 1000;

        ItemStack infoButton = createNavigationItem(
                Material.CHEST,
                "§e§lStorage-Übersicht",
                List.of(
                        "§7Materialien: §e" + materials.size(),
                        "§7Truhen: §e" + totalChests,
                        "§7Letzter Scan: §e" + secondsAgo + "s her",
                        "§7",
                        "§7Dieser Storage zeigt alle",
                        "§7Materialien auf dem Grundstück"
                )
        );
        setItem(4, infoButton);

        // Slot 2: Receiver-Kiste Status
        ChestData receiverChest = plotStorage.getReceiverChest();
        if (receiverChest != null) {
            ItemStack receiverButton = createNavigationItem(
                    Material.ENDER_CHEST,
                    "§a§lEmpfangskiste",
                    List.of(
                            "§7Status: §aGesetzt",
                            "§7",
                            "§7Position:",
                            String.format("§e  X: %.0f", receiverChest.getLocation().getX()),
                            String.format("§e  Y: %.0f", receiverChest.getLocation().getY()),
                            String.format("§e  Z: %.0f", receiverChest.getLocation().getZ())
                    )
            );
            setItem(2, receiverButton);
        } else {
            ItemStack noReceiverButton = createNavigationItem(
                    Material.CHEST,
                    "§c§lKeine Empfangskiste",
                    isOwner ?
                            List.of(
                                    "§7Status: §cNicht gesetzt",
                                    "§7",
                                    "§7Setze eine Empfangskiste mit:",
                                    "§e/plot storage setreceiver",
                                    "§7",
                                    "§7Klicke eine Truhe an um sie",
                                    "§7als Empfangskiste zu markieren"
                            ) :
                            List.of(
                                    "§7Status: §cNicht gesetzt",
                                    "§7",
                                    "§7Der Besitzer muss eine",
                                    "§7Empfangskiste setzen"
                            )
            );
            setItem(2, noReceiverButton);
        }

        // Slot 8: Scan-Button (nur Owner)
        if (isOwner) {
            ItemStack scanButton = createNavigationItem(
                    Material.COMPASS,
                    "§a§lStorage scannen",
                    List.of(
                            "§7Scannt alle Truhen auf",
                            "§7dem Grundstück neu",
                            "§7",
                            "§e§lKlicke zum Scannen"
                    )
            );
            setItem(8, scanButton, player -> {
                player.closeInventory();
                player.sendMessage("§e§lStorage wird gescannt...");
                player.performCommand("plot storage scan");
            });
        } else {
            ItemStack infoIcon = createNavigationItem(
                    Material.BOOK,
                    "§7Info",
                    List.of(
                            "§7Nur der Besitzer kann",
                            "§7den Storage scannen"
                    )
            );
            setItem(8, infoIcon);
        }
    }

    /**
     * Baut die Material-Liste (Zeilen 1-4).
     */
    private void buildMaterialList() {
        Set<Material> materials = plotStorage.getAllMaterials();

        // Sortiere Materialien nach Menge (absteigend)
        List<Material> sortedMaterials = new ArrayList<>(materials);
        sortedMaterials.sort(Comparator.comparingInt(
                m -> -plotStorage.getTotalAmount(m)
        ));

        // Pagination
        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, sortedMaterials.size());

        if (sortedMaterials.isEmpty()) {
            // Kein Storage vorhanden
            ItemStack emptyItem = createInfoItem(
                    "§7Kein Storage gefunden",
                    isOwner ?
                            List.of(
                                    "§7Dieser Plot hat noch keine",
                                    "§7Truhen mit Materialien",
                                    "§7",
                                    "§7Nutze §e/plot storage scan§7 um",
                                    "§7Truhen zu scannen"
                            ) :
                            List.of(
                                    "§7Dieser Plot hat noch keine",
                                    "§7Truhen mit Materialien"
                            )
            );
            setItem(22, emptyItem);
            return;
        }

        // Fülle Material-Liste (Slots 9-44)
        int slot = 9;
        for (int i = startIndex; i < endIndex && slot < 45; i++) {
            Material material = sortedMaterials.get(i);
            int amount = plotStorage.getTotalAmount(material);

            ItemStack materialItem = createMaterialItem(material, amount);
            setItem(slot, materialItem, player -> {
                // Info anzeigen
                player.sendMessage("§e§lMaterial-Info:");
                player.sendMessage("§7Material: §e" + material.name());
                player.sendMessage("§7Menge: §e" + amount);
            });

            slot++;
        }
    }

    /**
     * Baut die Pagination-Bar (Zeile 5).
     */
    private void buildPaginationBar() {
        Set<Material> materials = plotStorage.getAllMaterials();
        int totalPages = (int) Math.ceil((double) materials.size() / itemsPerPage);

        // Slot 45: Vorherige Seite
        if (currentPage > 0) {
            ItemStack prevButton = createNavigationItem(
                    Material.ARROW,
                    "§e← Vorherige Seite",
                    List.of("§7Seite " + currentPage + " von " + totalPages)
            );
            setItem(45, prevButton, player -> {
                currentPage--;
                rebuild();
                open(player);
            });
        }

        // Slot 49: Seiten-Info
        ItemStack pageInfo = createNavigationItem(
                Material.PAPER,
                "§7Seite " + (currentPage + 1) + " / " + Math.max(1, totalPages),
                List.of(
                        "§7Materialien: §e" + materials.size(),
                        "§7Pro Seite: §e" + itemsPerPage
                )
        );
        setItem(49, pageInfo);

        // Slot 53: Nächste Seite
        if ((currentPage + 1) * itemsPerPage < materials.size()) {
            ItemStack nextButton = createNavigationItem(
                    Material.ARROW,
                    "§eNächste Seite →",
                    List.of("§7Seite " + (currentPage + 2) + " von " + totalPages)
            );
            setItem(53, nextButton, player -> {
                currentPage++;
                rebuild();
                open(player);
            });
        }
    }

    /**
     * Baut das UI neu auf (für Pagination).
     */
    private void rebuild() {
        clearItems();
        buildUI();
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
     * Erstellt ein Material-Item mit Menge.
     */
    private ItemStack createMaterialItem(Material material, int amount) {
        ItemStack item = new ItemStack(material, Math.min(amount, 64));
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
                Component.text("Menge: ")
                        .color(NamedTextColor.GRAY)
                        .append(Component.text(amount + "x").color(NamedTextColor.GOLD))
                        .decoration(TextDecoration.ITALIC, false)
        );
        lore.add(Component.empty());
        lore.add(
                Component.text("§7Auf diesem Grundstück gelagertes Material")
                        .decoration(TextDecoration.ITALIC, false)
        );

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Erstellt ein Info-Item.
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
