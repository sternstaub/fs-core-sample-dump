package de.fallenstar.plot.ui;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.ui.container.GenericUiLargeChest;
import de.fallenstar.core.ui.element.ClickableUiElement;
import de.fallenstar.core.ui.element.StaticUiElement;
import de.fallenstar.core.ui.element.UiAction;
import de.fallenstar.core.ui.element.navigation.CloseButton;
import de.fallenstar.core.ui.element.navigation.NavigateLeftButton;
import de.fallenstar.core.ui.element.navigation.NavigateRightButton;
import de.fallenstar.core.ui.element.navigation.PageNavigationAction;
import de.fallenstar.core.ui.row.BasicUiRowForContent;
import de.fallenstar.plot.action.ScanStorageAction;
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
 * Type-Safe Plot-Storage Verwaltungs-UI.
 *
 * **Migration:** Ersetzt alte PlotStorageUI (396 Zeilen) durch
 * type-safe GenericUiLargeChest-basierte Implementierung.
 *
 * **Features:**
 * - Material-Liste mit Mengen (sortiert)
 * - Receiver-Kiste Status
 * - Storage-Scan Funktion (Owner)
 * - Pagination für viele Items
 *
 * **Layout:**
 * - Row 0 (Slots 0-8): Navigation (Close, Receiver-Info, Storage-Info, Scan)
 * - Row 1-4 (Slots 9-44): Material-Liste (bis zu 36 Items pro Seite)
 * - Row 5 (Slots 45-53): Pagination (Previous, Page-Info, Next)
 *
 * **Type-Safety:**
 * - Scan-Button nutzt ScanStorageAction (wiederverwendbar)
 * - Navigation nutzt PageNavigationAction
 * - Alle Buttons compiler-enforced
 *
 * @author FallenStar
 * @version 2.0
 */
public class PlotStorageUi extends GenericUiLargeChest implements PageNavigationAction.PageNavigable {

    private final Plot plot;
    private final PlotStorage plotStorage;
    private final PlotStorageProvider storageProvider;
    private final StorageManager storageManager;
    private final boolean isOwner;

    private int currentPage = 0;
    private static final int ITEMS_PER_PAGE = 36; // 4 Rows × 9 Slots

    /**
     * Konstruktor für PlotStorageUi.
     *
     * @param plot Der Plot
     * @param plotStorage Das PlotStorage-Objekt
     * @param storageProvider PlotStorageProvider
     * @param storageManager StorageManager
     * @param isOwner Ob Spieler Owner ist
     */
    public PlotStorageUi(
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
        buildMaterialList();
        buildPaginationBar();
    }

    /**
     * Baut die Navigation-Bar (Row 0).
     */
    private void buildNavigationBar() {
        var row = getRow(0);

        // Slot 0: Close Button
        row.setElement(0, CloseButton.create(this));

        // Slot 2: Receiver-Kiste Status
        row.setElement(2, createReceiverInfo());

        // Slot 4: Storage-Info
        row.setElement(4, createStorageInfo());

        // Slot 8: Scan Button (nur Owner)
        if (isOwner) {
            var scanButton = new ClickableUiElement.CustomButton<>(
                    createButtonItem(
                            Material.COMPASS,
                            "§a§lStorage scannen",
                            List.of(
                                    "§7Scannt alle Truhen auf",
                                    "§7dem Grundstück neu",
                                    "§7",
                                    "§e§lKlicke zum Scannen"
                            )
                    ),
                    new ScanStorageAction(plot)
            );
            row.setElement(8, scanButton);
        } else {
            row.setElement(8, new StaticUiElement(
                    createButtonItem(
                            Material.BOOK,
                            "§7Info",
                            List.of(
                                    "§7Nur der Besitzer kann",
                                    "§7den Storage scannen"
                            )
                    )
            ));
        }
    }

    /**
     * Erstellt das Receiver-Info Element.
     */
    private StaticUiElement createReceiverInfo() {
        ChestData receiverChest = plotStorage.getReceiverChest();

        if (receiverChest != null) {
            return new StaticUiElement(
                    createButtonItem(
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
                    )
            );
        } else {
            return new StaticUiElement(
                    createButtonItem(
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
                    )
            );
        }
    }

    /**
     * Erstellt das Storage-Info Element.
     */
    private StaticUiElement createStorageInfo() {
        Set<Material> materials = plotStorage.getAllMaterials();
        int totalChests = plotStorage.getAllChests().size();
        long lastUpdate = plotStorage.getLastUpdate();
        long secondsAgo = (System.currentTimeMillis() - lastUpdate) / 1000;

        return new StaticUiElement(
                createButtonItem(
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
                )
        );
    }

    /**
     * Baut die Material-Liste (Rows 1-4).
     */
    private void buildMaterialList() {
        Set<Material> materials = plotStorage.getAllMaterials();

        // Sortiere Materialien nach Menge (absteigend)
        List<Material> sortedMaterials = new ArrayList<>(materials);
        sortedMaterials.sort(Comparator.comparingInt(
                m -> -plotStorage.getTotalAmount(m)
        ));

        // Pagination
        int startIndex = currentPage * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, sortedMaterials.size());

        if (sortedMaterials.isEmpty()) {
            // Kein Storage vorhanden - Zeige Info in Row 2, Slot 4 (Mitte)
            var emptyInfo = new StaticUiElement(
                    createButtonItem(
                            Material.PAPER,
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
                    )
            );
            getRow(2).setElement(4, emptyInfo); // Row 2, Mitte
            return;
        }

        // Fülle Material-Liste (Rows 1-4, Slots 9-44)
        int materialIndex = 0;
        for (int i = startIndex; i < endIndex; i++) {
            Material material = sortedMaterials.get(i);
            int amount = plotStorage.getTotalAmount(material);

            // Berechne Row und Position
            int absoluteSlot = 9 + materialIndex; // Start bei Slot 9 (Row 1, Pos 0)
            int rowIndex = absoluteSlot / 9;
            int position = absoluteSlot % 9;

            // Erstelle Material-Item mit Click-Action (zeigt Info)
            var materialButton = new ClickableUiElement.CustomButton<>(
                    createMaterialItem(material, amount),
                    new MaterialInfoAction(material, amount)
            );

            getRow(rowIndex).setElement(position, materialButton);
            materialIndex++;
        }
    }

    /**
     * Baut die Pagination-Bar (Row 5).
     */
    private void buildPaginationBar() {
        var row = getRow(5);

        Set<Material> materials = plotStorage.getAllMaterials();
        int totalPages = (int) Math.ceil((double) materials.size() / ITEMS_PER_PAGE);

        // Slot 45 (Row 5, Pos 0): Previous Page
        if (currentPage > 0) {
            row.setElement(0, NavigateLeftButton.previous(this));
        }

        // Slot 49 (Row 5, Pos 4): Page-Info
        row.setElement(4, new StaticUiElement(
                createButtonItem(
                        Material.PAPER,
                        "§7Seite " + (currentPage + 1) + " / " + Math.max(1, totalPages),
                        List.of(
                                "§7Materialien: §e" + materials.size(),
                                "§7Pro Seite: §e" + ITEMS_PER_PAGE
                        )
                )
        ));

        // Slot 53 (Row 5, Pos 8): Next Page
        if ((currentPage + 1) * ITEMS_PER_PAGE < materials.size()) {
            row.setElement(8, NavigateRightButton.next(this));
        }
    }

    // ========================================
    // PageNavigable Implementation
    // ========================================

    @Override
    public void nextPage(Player player) {
        Set<Material> materials = plotStorage.getAllMaterials();
        int totalPages = (int) Math.ceil((double) materials.size() / ITEMS_PER_PAGE);

        if (currentPage < totalPages - 1) {
            currentPage++;
            rebuild(player);
        }
    }

    @Override
    public void previousPage(Player player) {
        if (currentPage > 0) {
            currentPage--;
            rebuild(player);
        }
    }

    @Override
    public void firstPage(Player player) {
        currentPage = 0;
        rebuild(player);
    }

    @Override
    public void lastPage(Player player) {
        Set<Material> materials = plotStorage.getAllMaterials();
        int totalPages = (int) Math.ceil((double) materials.size() / ITEMS_PER_PAGE);

        currentPage = Math.max(0, totalPages - 1);
        rebuild(player);
    }

    @Override
    public void goToPage(Player player, int page) {
        Set<Material> materials = plotStorage.getAllMaterials();
        int totalPages = (int) Math.ceil((double) materials.size() / ITEMS_PER_PAGE);

        if (page >= 0 && page < totalPages) {
            currentPage = page;
            rebuild(player);
        }
    }

    /**
     * Baut das UI neu auf und aktualisiert die Anzeige.
     */
    private void rebuild(Player player) {
        // Lösche alle Rows
        for (int i = 0; i < ROW_COUNT; i++) {
            getRow(i).clear();
        }

        // Baue UI neu auf
        buildUi();

        // Öffne neu
        open(player);
    }

    // ========================================
    // Helper Methods
    // ========================================

    /**
     * Erstellt ein Button-Item.
     */
    private ItemStack createButtonItem(Material material, String name, List<String> lore) {
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

    /**
     * Action zum Anzeigen von Material-Info.
     */
    private static final class MaterialInfoAction implements UiAction {
        private final Material material;
        private final int amount;

        public MaterialInfoAction(Material material, int amount) {
            this.material = material;
            this.amount = amount;
        }

        @Override
        public void execute(Player player) {
            player.sendMessage("§e§lMaterial-Info:");
            player.sendMessage("§7Material: §e" + material.name());
            player.sendMessage("§7Menge: §e" + amount);
        }

        @Override
        public String getActionName() {
            return "MaterialInfo[" + material.name() + ", " + amount + "]";
        }
    }
}
