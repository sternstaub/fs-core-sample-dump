package de.fallenstar.plot.ui;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.ui.container.GenericUiLargeChest;
import de.fallenstar.core.ui.element.ClickableUiElement;
import de.fallenstar.core.ui.element.StaticUiElement;
import de.fallenstar.core.ui.element.navigation.CloseButton;
import de.fallenstar.core.ui.element.navigation.NavigateLeftButton;
import de.fallenstar.core.ui.element.navigation.NavigateRightButton;
import de.fallenstar.core.ui.element.navigation.PageNavigationAction;
import de.fallenstar.core.ui.row.BasicUiRow;
import de.fallenstar.core.ui.row.BasicUiRowForContent;
import de.fallenstar.core.ui.row.BasicUiRowForControl;
import de.fallenstar.plot.PlotModule;
import de.fallenstar.plot.action.npc.ConfigureNpcAction;
import de.fallenstar.plot.action.npc.SpawnNpcAction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * NPC-Verwaltungs-UI für Plot-Besitzer.
 *
 * **Owner-Ansicht:**
 * - Zeigt alle NPCs auf dem Plot (unabhängig vom Besitzer)
 * - Erlaubt Spawnen neuer NPCs (nach Typ)
 * - NPC-Konfiguration und Verwaltung
 * - Slot-System Integration
 *
 * **Features:**
 * - NPC-Liste mit Typen und Status
 * - Spawn-Menü für verschiedene NPC-Typen
 * - NPC-Aktionen (Inventar, Position, Entfernen)
 * - Pagination für viele NPCs
 *
 * **Layout:**
 * - Row 0 (Slots 0-8): Control-Row (Close, Info, Spawn-Menü)
 * - Row 1-4 (Slots 9-44): NPC-Liste (bis zu 36 NPCs pro Seite)
 * - Row 5 (Slots 45-53): Pagination (Previous, Page-Info, Next)
 *
 * **Type-Safety:**
 * - Spawn-Buttons nutzen SpawnNpcAction
 * - NPC-Buttons nutzen ConfigureNpcAction
 * - Navigation nutzt PageNavigationAction
 *
 * **Verwendung:**
 * ```java
 * NpcManagementUi ui = new NpcManagementUi(plot, providers);
 * ui.open(player);
 * ```
 *
 * @author FallenStar
 * @version 1.0
 */
public class NpcManagementUi extends GenericUiLargeChest implements PageNavigationAction.PageNavigable {

    private final Plot plot;
    private final PlotModule plotModule;
    private final List<NpcInfo> npcs;  // Alle NPCs auf dem Plot
    private int currentPage;

    private static final int NPCS_PER_PAGE = 36;  // 4 Rows à 9 Slots

    /**
     * Konstruktor für NpcManagementUi.
     *
     * @param plot Der Plot dessen NPCs verwaltet werden
     * @param plotModule Das PlotModule für NPC-Manager-Zugriff
     */
    public NpcManagementUi(Plot plot, PlotModule plotModule) {
        super("§6§lNPC-Verwaltung: " + getPlotName(plot));
        this.plot = Objects.requireNonNull(plot, "Plot darf nicht null sein");
        this.plotModule = Objects.requireNonNull(plotModule, "PlotModule darf nicht null sein");
        this.npcs = new ArrayList<>();
        this.currentPage = 0;

        // Lade NPCs aus Registry
        loadNPCsFromRegistry();

        initializeRows();
    }

    /**
     * Lädt NPCs aus der PlotBoundNPCRegistry.
     */
    private void loadNPCsFromRegistry() {
        var npcRegistry = plotModule.getNPCRegistry();
        if (npcRegistry == null) {
            return;
        }

        // Hole alle NPCs für diesen Plot
        var registryNpcs = npcRegistry.getNPCsForPlot(plot);

        // Konvertiere Registry-NPCInfo zu UI-NpcInfo
        for (var registryNpc : registryNpcs) {
            // Erstelle UI-NpcInfo aus Registry-Daten
            UUID npcId = registryNpc.npcId();
            String npcName = generateNpcName(registryNpc.npcType());
            String npcType = registryNpc.npcType();
            NpcType uiType = mapNpcType(registryNpc.npcType());
            String ownerName = "System";  // TODO: Owner-Tracking
            boolean active = true;  // TODO: Citizens-Check

            npcs.add(new NpcInfo(npcId, npcName, npcType, uiType, ownerName, active));
        }
    }

    /**
     * Generiert NPC-Namen aus Typ.
     */
    private String generateNpcName(String npcType) {
        return switch (npcType.toLowerCase()) {
            case "guildtrader" -> "Gildenhändler";
            case "playertrader" -> "Spielerhändler";
            case "worldbanker" -> "Weltbankier";
            case "ambassador" -> "Botschafter";
            default -> "NPC";
        };
    }

    /**
     * Mappt Registry-NPC-Typ zu UI-Enum.
     */
    private NpcType mapNpcType(String npcType) {
        return switch (npcType.toLowerCase()) {
            case "guildtrader", "playertrader" -> NpcType.TRADER;
            case "worldbanker" -> NpcType.WORLD_BANKER;
            case "ambassador" -> NpcType.AMBASSADOR;
            default -> NpcType.TRADER;
        };
    }

    /**
     * Initialisiert die UI-Rows.
     */
    private void initializeRows() {
        // Row 0: Control-Row (Close, Info, Spawn-Menü)
        BasicUiRowForControl controlRow = new BasicUiRowForControl();
        setRow(0, controlRow);

        // Row 1-4: Content-Rows für NPCs
        for (int i = 1; i <= 4; i++) {
            setRow(i, new BasicUiRowForContent());
        }

        // Row 5: Pagination-Row
        BasicUiRowForContent paginationRow = new BasicUiRowForContent();
        setRow(5, paginationRow);

        // Befülle Rows
        populateControlRow();
        populateNpcList();
        populatePaginationRow();
    }

    /**
     * Befüllt die Control-Row.
     */
    private void populateControlRow() {
        BasicUiRow controlRow = getRow(0);

        // Slot 0: Info-Element
        ItemStack infoItem = createInfoItem();
        controlRow.setElement(0, new StaticUiElement(infoItem));

        // Slot 4: Spawn-Menü Button
        ItemStack spawnMenuItem = createSpawnMenuButton();
        SpawnNpcAction spawnAction = new SpawnNpcAction(plot, plotModule);  // Öffnet Spawn-Menü
        controlRow.setElement(4, new ClickableUiElement.CustomButton<>(spawnMenuItem, spawnAction));

        // Slot 8: Close-Button
        controlRow.setElement(8, CloseButton.create(this));
    }

    /**
     * Befüllt die NPC-Liste.
     */
    private void populateNpcList() {
        // Berechne Start- und End-Index für aktuelle Seite
        int startIndex = currentPage * NPCS_PER_PAGE;
        int endIndex = Math.min(startIndex + NPCS_PER_PAGE, npcs.size());

        // Lösche alte NPC-Items
        for (int row = 1; row <= 4; row++) {
            BasicUiRowForContent contentRow = (BasicUiRowForContent) getRow(row);
            contentRow.clear();
        }

        // Füge NPCs für aktuelle Seite hinzu
        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            NpcInfo npc = npcs.get(i);
            ItemStack npcItem = createNpcItem(npc);

            int rowIndex = 1 + (slot / 9);  // Row 1-4
            int position = slot % 9;

            BasicUiRowForContent contentRow = (BasicUiRowForContent) getRow(rowIndex);

            // Erstelle ConfigureNpcAction für NPC-Konfiguration
            ConfigureNpcAction configAction = new ConfigureNpcAction(
                    plot,
                    npc.npcId,
                    npc.name,
                    npc.npcType,
                    plotModule
            );
            contentRow.setElement(position, new ClickableUiElement.CustomButton<>(npcItem, configAction));

            slot++;
        }

        // Falls keine NPCs vorhanden: Zeige Placeholder
        if (npcs.isEmpty()) {
            BasicUiRowForContent firstContentRow = (BasicUiRowForContent) getRow(2);  // Mittlere Row
            ItemStack placeholder = createNoNpcsPlaceholder();
            firstContentRow.setElement(4, new StaticUiElement(placeholder));  // Mittlerer Slot
        }
    }

    /**
     * Befüllt die Pagination-Row.
     */
    private void populatePaginationRow() {
        BasicUiRow paginationRow = getRow(5);

        // Previous-Button (Slot 3)
        if (hasPreviousPage()) {
            paginationRow.setElement(3, NavigateLeftButton.previous(this));
        }

        // Page-Info (Slot 4)
        ItemStack pageInfo = createPageInfoItem();
        paginationRow.setElement(4, new StaticUiElement(pageInfo));

        // Next-Button (Slot 5)
        if (hasNextPage()) {
            paginationRow.setElement(5, NavigateRightButton.next(this));
        }
    }

    /**
     * Erstellt das Info-Item.
     */
    private ItemStack createInfoItem() {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("§6§lNPC-Verwaltung")
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("§7Plot: §e" + getPlotName(plot))
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§7NPCs gesamt: §e" + npcs.size())
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text(""));
        lore.add(Component.text("§7Verwalte alle NPCs auf diesem Plot")
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§7oder spawne neue NPCs.")
                .decoration(TextDecoration.ITALIC, false));

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Erstellt den Spawn-Menü Button.
     */
    private ItemStack createSpawnMenuButton() {
        ItemStack item = new ItemStack(Material.VILLAGER_SPAWN_EGG);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("§a§lNPC spawnen")
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("§7Öffnet das Spawn-Menü")
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text(""));
        lore.add(Component.text("§7Verfügbare NPC-Typen:")
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§7  • §eGildenhändler")
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§7  • §eSpielhändler")
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§7  • §eBankier")
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§7  • §eBotschafter")
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text(""));
        lore.add(Component.text("§a§lKlicke zum Öffnen")
                .decoration(TextDecoration.ITALIC, false));

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Erstellt ein NPC-Item.
     */
    private ItemStack createNpcItem(NpcInfo npc) {
        ItemStack item = new ItemStack(getNpcMaterial(npc.uiType));
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("§e§l" + npc.name)
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("§7Typ: §e" + getNpcTypeName(npc.uiType))
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§7Besitzer: §e" + npc.ownerName)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§7Status: " + (npc.active ? "§a§lAktiv" : "§c§lInaktiv"))
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text(""));
        lore.add(Component.text("§a§lKlicke zum Konfigurieren")
                .decoration(TextDecoration.ITALIC, false));

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Erstellt Placeholder wenn keine NPCs vorhanden.
     */
    private ItemStack createNoNpcsPlaceholder() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("§c§lKeine NPCs vorhanden")
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("§7Auf diesem Plot sind noch")
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§7keine NPCs gespawnt.")
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text(""));
        lore.add(Component.text("§7Nutze den §eSpawn-Button§7 oben,")
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§7um NPCs zu spawnen.")
                .decoration(TextDecoration.ITALIC, false));

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Erstellt Page-Info Item.
     */
    private ItemStack createPageInfoItem() {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();

        int totalPages = getTotalPages();
        meta.displayName(Component.text("§7Seite §e" + (currentPage + 1) + " §7von §e" + totalPages)
                .decoration(TextDecoration.ITALIC, false));

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Gibt Material für NPC-Typ zurück.
     */
    private Material getNpcMaterial(NpcType type) {
        return switch (type) {
            case TRADER -> Material.EMERALD;
            case BANKER -> Material.GOLD_INGOT;
            case AMBASSADOR -> Material.ENDER_PEARL;
            case CRAFTSMAN -> Material.ANVIL;
            default -> Material.VILLAGER_SPAWN_EGG;
        };
    }

    /**
     * Gibt Namen für NPC-Typ zurück.
     */
    private String getNpcTypeName(NpcType type) {
        return switch (type) {
            case TRADER -> "Händler";
            case BANKER -> "Bankier";
            case AMBASSADOR -> "Botschafter";
            case CRAFTSMAN -> "Handwerker";
            default -> "Unbekannt";
        };
    }

    /**
     * Gibt Plot-Namen zurück.
     */
    private static String getPlotName(Plot plot) {
        // TODO: Integration mit PlotNameManager
        return "Plot #" + plot.getIdentifier();
    }

    // PageNavigable Implementation

    @Override
    public void nextPage(Player player) {
        currentPage++;
        populateNpcList();
        populatePaginationRow();
        build();
        open(player);
    }

    @Override
    public void previousPage(Player player) {
        currentPage--;
        populateNpcList();
        populatePaginationRow();
        build();
        open(player);
    }

    @Override
    public void firstPage(Player player) {
        currentPage = 0;
        populateNpcList();
        populatePaginationRow();
        build();
        open(player);
    }

    @Override
    public void lastPage(Player player) {
        currentPage = getTotalPages() - 1;
        populateNpcList();
        populatePaginationRow();
        build();
        open(player);
    }

    @Override
    public void goToPage(Player player, int page) {
        int totalPages = getTotalPages();
        if (page >= 0 && page < totalPages) {
            currentPage = page;
            populateNpcList();
            populatePaginationRow();
            build();
            open(player);
        }
    }

    /**
     * Hilfsmethoden für Page-Checks.
     */
    private int getTotalPages() {
        if (npcs.isEmpty()) {
            return 1;
        }
        return (int) Math.ceil((double) npcs.size() / NPCS_PER_PAGE);
    }

    private boolean hasNextPage() {
        return currentPage < getTotalPages() - 1;
    }

    private boolean hasPreviousPage() {
        return currentPage > 0;
    }

    /**
     * NPC-Info Record für UI-Darstellung.
     */
    private record NpcInfo(
            UUID npcId,
            String name,
            String npcType,
            NpcType uiType,
            String ownerName,
            boolean active
    ) {}

    /**
     * NPC-Typen Enum.
     *
     * TODO: In Core-Modul verschieben (NPCProvider)
     */
    public enum NpcType {
        TRADER,        // Gildenhändler / Spielerhändler
        BANKER,        // Lokaler Bankier
        AMBASSADOR,    // Botschafter
        CRAFTSMAN,     // Handwerker
        WORLD_BANKER   // Weltbankier (Admin)
    }
}
