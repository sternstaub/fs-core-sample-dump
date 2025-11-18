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
import de.fallenstar.plot.action.npc.BuyNpcSlotAction;
import net.kyori.adventure.text.Component;
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
 * NPC-Verwaltungs-UI für Spieler (eigene NPCs).
 *
 * **Spieler-Ansicht:**
 * - Zeigt nur die eigenen NPCs des Spielers
 * - Erlaubt Kauf neuer NPC-Slots
 * - Verwaltung eigener Händler-Inventare
 * - Preisverwaltung für eigene Händler
 *
 * **Features:**
 * - Liste eigener NPCs mit Status
 * - NPC-Slot kaufen Button
 * - NPC-Konfiguration (Inventar, Preise)
 * - Pagination für viele NPCs
 *
 * **Layout:**
 * - Row 0 (Slots 0-8): Control-Row (Close, Info, Slot kaufen)
 * - Row 1-4 (Slots 9-44): Eigene NPC-Liste (bis zu 36 NPCs pro Seite)
 * - Row 5 (Slots 45-53): Pagination (Previous, Page-Info, Next)
 *
 * **Type-Safety:**
 * - Slot-Kauf nutzt BuyNpcSlotAction
 * - NPC-Buttons nutzen ConfigurePlayerNpcAction
 * - Navigation nutzt PageNavigationAction
 *
 * **Verwendung:**
 * ```java
 * PlayerNpcManagementUi ui = new PlayerNpcManagementUi(plot, player);
 * ui.open(player);
 * ```
 *
 * @author FallenStar
 * @version 1.0
 */
public class PlayerNpcManagementUi extends GenericUiLargeChest implements PageNavigationAction.PageNavigable {

    private final Plot plot;
    private final UUID playerId;
    private final String playerName;
    private final List<PlayerNpcInfo> playerNpcs;  // Nur NPCs des Spielers
    private int currentPage;

    private static final int NPCS_PER_PAGE = 36;  // 4 Rows à 9 Slots

    /**
     * Konstruktor für PlayerNpcManagementUi.
     *
     * @param plot Der Plot
     * @param player Der Spieler dessen NPCs verwaltet werden
     */
    public PlayerNpcManagementUi(Plot plot, Player player) {
        super("§6§lMeine NPCs: " + getPlotName(plot));
        this.plot = Objects.requireNonNull(plot, "Plot darf nicht null sein");
        this.playerId = player.getUniqueId();
        this.playerName = player.getName();
        this.playerNpcs = new ArrayList<>();  // TODO: Load from NPC registry (filtered by owner)
        this.currentPage = 0;

        initializeRows();
    }

    /**
     * Initialisiert die UI-Rows.
     */
    private void initializeRows() {
        // Row 0: Control-Row (Close, Info, Slot kaufen)
        BasicUiRowForControl controlRow = new BasicUiRowForControl(this);
        setRow(0, controlRow);

        // Row 1-4: Content-Rows für eigene NPCs
        for (int i = 1; i <= 4; i++) {
            setRow(i, new BasicUiRowForContent());
        }

        // Row 5: Pagination-Row
        BasicUiRow paginationRow = new BasicUiRow();
        setRow(5, paginationRow);

        // Befülle Rows
        populateControlRow();
        populatePlayerNpcList();
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

        // Slot 4: NPC-Slot kaufen Button
        ItemStack buySlotItem = createBuySlotButton();
        BuyNpcSlotAction buyAction = new BuyNpcSlotAction(plot, playerId);
        controlRow.setElement(4, new ClickableUiElement.CustomButton<>(buySlotItem, buyAction));

        // Slot 8: Close-Button
        controlRow.setElement(8, CloseButton.create(this));
    }

    /**
     * Befüllt die Spieler-NPC-Liste.
     */
    private void populatePlayerNpcList() {
        // Berechne Start- und End-Index für aktuelle Seite
        int startIndex = currentPage * NPCS_PER_PAGE;
        int endIndex = Math.min(startIndex + NPCS_PER_PAGE, playerNpcs.size());

        // Lösche alte NPC-Items
        for (int row = 1; row <= 4; row++) {
            BasicUiRowForContent contentRow = (BasicUiRowForContent) getRow(row);
            contentRow.clear();
        }

        // Füge NPCs für aktuelle Seite hinzu
        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            PlayerNpcInfo npc = playerNpcs.get(i);
            ItemStack npcItem = createPlayerNpcItem(npc);

            int rowIndex = 1 + (slot / 9);  // Row 1-4
            int position = slot % 9;

            BasicUiRowForContent contentRow = (BasicUiRowForContent) getRow(rowIndex);

            // TODO: Erstelle ConfigurePlayerNpcAction statt Placeholder
            contentRow.setElement(position, new StaticUiElement(npcItem));

            slot++;
        }

        // Falls keine eigenen NPCs vorhanden: Zeige Placeholder
        if (playerNpcs.isEmpty()) {
            BasicUiRowForContent firstContentRow = (BasicUiRowForContent) getRow(2);  // Mittlere Row
            ItemStack placeholder = createNoPlayerNpcsPlaceholder();
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

        meta.displayName(Component.text("§6§lMeine NPCs")
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("§7Plot: §e" + getPlotName(plot))
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§7Meine NPCs: §e" + playerNpcs.size())
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text(""));
        lore.add(Component.text("§7Verwalte deine NPCs auf diesem Plot")
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§7oder kaufe neue NPC-Slots.")
                .decoration(TextDecoration.ITALIC, false));

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Erstellt den NPC-Slot kaufen Button.
     */
    private ItemStack createBuySlotButton() {
        ItemStack item = new ItemStack(Material.EMERALD);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("§a§lNPC-Slot kaufen")
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("§7Kaufe einen NPC-Slot, um einen")
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§7eigenen Händler zu spawnen.")
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text(""));
        lore.add(Component.text("§7Kosten: §e500 Sterne")
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§7NPC-Typ: §eSpielhändler")
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text(""));
        lore.add(Component.text("§7Dein Händler kann:")
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§7  • §eEigenes Inventar verwalten")
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§7  • §ePreise festlegen")
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§7  • §eItems kaufen/verkaufen")
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text(""));
        lore.add(Component.text("§a§lKlicke zum Kaufen")
                .decoration(TextDecoration.ITALIC, false));

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Erstellt ein Spieler-NPC-Item.
     */
    private ItemStack createPlayerNpcItem(PlayerNpcInfo npc) {
        ItemStack item = new ItemStack(Material.EMERALD);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("§e§l" + npc.name)
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("§7Typ: §eSpielhändler")
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§7Status: " + (npc.active ? "§a§lAktiv" : "§c§lInaktiv"))
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text(""));
        lore.add(Component.text("§7Inventar: §e" + npc.inventoryItems + " Items")
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§7Angebote: §e" + npc.tradeOffers + " Trades")
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text(""));
        lore.add(Component.text("§a§lKlicke zum Konfigurieren")
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§7  • Inventar verwalten")
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§7  • Preise festlegen")
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§7  • Position ändern")
                .decoration(TextDecoration.ITALIC, false));

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Erstellt Placeholder wenn keine eigenen NPCs vorhanden.
     */
    private ItemStack createNoPlayerNpcsPlaceholder() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("§c§lKeine eigenen NPCs")
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("§7Du hast noch keine NPCs")
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§7auf diesem Plot.")
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text(""));
        lore.add(Component.text("§7Kaufe einen §eNPC-Slot§7 oben,")
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§7um einen eigenen Händler zu spawnen.")
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
     * Gibt Plot-Namen zurück.
     */
    private static String getPlotName(Plot plot) {
        // TODO: Integration mit PlotNameManager
        return "Plot #" + plot.getIdentifier();
    }

    // PageNavigable Implementation

    @Override
    public void nextPage() {
        if (hasNextPage()) {
            currentPage++;
            populatePlayerNpcList();
            populatePaginationRow();
            build();
        }
    }

    @Override
    public void previousPage() {
        if (hasPreviousPage()) {
            currentPage--;
            populatePlayerNpcList();
            populatePaginationRow();
            build();
        }
    }

    @Override
    public void gotoPage(int page) {
        int totalPages = getTotalPages();
        if (page >= 0 && page < totalPages) {
            currentPage = page;
            populatePlayerNpcList();
            populatePaginationRow();
            build();
        }
    }

    @Override
    public int getCurrentPage() {
        return currentPage;
    }

    @Override
    public int getTotalPages() {
        if (playerNpcs.isEmpty()) {
            return 1;
        }
        return (int) Math.ceil((double) playerNpcs.size() / NPCS_PER_PAGE);
    }

    @Override
    public boolean hasNextPage() {
        return currentPage < getTotalPages() - 1;
    }

    @Override
    public boolean hasPreviousPage() {
        return currentPage > 0;
    }

    /**
     * Spieler-NPC-Info Record für UI-Darstellung.
     *
     * TODO: Ersetzen durch echte NPC-Entity-Referenzen
     */
    private record PlayerNpcInfo(
            String name,
            boolean active,
            int inventoryItems,
            int tradeOffers
    ) {}
}
