package de.fallenstar.plot.ui;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.ui.container.GenericUiSmallChest;
import de.fallenstar.core.ui.element.ClickableUiElement;
import de.fallenstar.core.ui.element.StaticUiElement;
import de.fallenstar.core.ui.element.UiAction;
import de.fallenstar.core.ui.element.navigation.CloseButton;
import de.fallenstar.core.ui.row.BasicUiRowForContent;
import de.fallenstar.core.ui.row.BasicUiRowForControl;
import de.fallenstar.plot.PlotModule;
import de.fallenstar.plot.action.npc.DespawnNpcAction;
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
 * NPC-Konfigurations-UI.
 *
 * **Funktionalität:**
 * - Zeigt NPC-Details (Name, Typ, Status)
 * - Bietet Verwaltungsoptionen:
 *   - Despawn (Entfernen)
 *   - TODO: Teleport zu NPC
 *   - TODO: Inventar bearbeiten
 *
 * **Layout:**
 * - Row 0 (Slots 0-8): Control-Row (Info, Close)
 * - Row 1 (Slots 9-17): Aktionen (Despawn, etc.)
 * - Row 2 (Slots 18-26): Navigation (Back)
 *
 * **Verwendung:**
 * ```java
 * NpcConfigUi ui = new NpcConfigUi(plot, npcId, npcName, npcType, plotModule);
 * ui.open(player);
 * ```
 *
 * @author FallenStar
 * @version 1.0
 */
public class NpcConfigUi extends GenericUiSmallChest {

    private final Plot plot;
    private final UUID npcId;
    private final String npcName;
    private final String npcType;
    private final PlotModule plotModule;

    /**
     * Konstruktor für NpcConfigUi.
     *
     * @param plot Der Plot auf dem der NPC ist
     * @param npcId Die UUID des NPCs
     * @param npcName Name des NPCs
     * @param npcType Typ des NPCs
     * @param plotModule Das PlotModule
     */
    public NpcConfigUi(Plot plot, UUID npcId, String npcName, String npcType, PlotModule plotModule) {
        super("§6§lNPC-Konfiguration: " + npcName);
        this.plot = Objects.requireNonNull(plot, "Plot darf nicht null sein");
        this.npcId = Objects.requireNonNull(npcId, "NpcId darf nicht null sein");
        this.npcName = Objects.requireNonNull(npcName, "NpcName darf nicht null sein");
        this.npcType = Objects.requireNonNull(npcType, "NpcType darf nicht null sein");
        this.plotModule = Objects.requireNonNull(plotModule, "PlotModule darf nicht null sein");

        initializeRows();
    }

    /**
     * Initialisiert die UI-Rows.
     */
    private void initializeRows() {
        // Row 0: Control-Row
        BasicUiRowForControl controlRow = new BasicUiRowForControl();
        setRow(0, controlRow);

        // Row 1: Aktionen
        BasicUiRowForContent actionsRow = new BasicUiRowForContent();
        setRow(1, actionsRow);

        // Row 2: Navigation
        BasicUiRowForContent navigationRow = new BasicUiRowForContent();
        setRow(2, navigationRow);

        // Befülle Rows
        populateControlRow();
        populateActionsRow();
        populateNavigationRow();
    }

    /**
     * Befüllt die Control-Row.
     */
    private void populateControlRow() {
        var controlRow = getRow(0);

        // Slot 0: NPC-Info
        ItemStack infoItem = createNpcInfoItem();
        controlRow.setElement(0, new StaticUiElement(infoItem));

        // Slot 8: Close-Button
        controlRow.setElement(8, CloseButton.create(this));
    }

    /**
     * Befüllt die Aktionen-Row.
     */
    private void populateActionsRow() {
        var actionsRow = getRow(1);

        // Slot 4: Despawn-Button
        ItemStack despawnItem = createDespawnButton();
        DespawnNpcAction despawnAction = new DespawnNpcAction(plot, npcId, npcName, plotModule);
        actionsRow.setElement(4, new ClickableUiElement.CustomButton<>(despawnItem, despawnAction));

        // TODO: Slot 3: Teleport-Button
        // TODO: Slot 5: Inventar-Button
    }

    /**
     * Befüllt die Navigation-Row.
     */
    private void populateNavigationRow() {
        var navigationRow = getRow(2);

        // Slot 4: Back-Button (manuell erstellt)
        ItemStack backItem = createBackButton();
        BackAction backAction = new BackAction(plot, plotModule);
        navigationRow.setElement(4, new ClickableUiElement.CustomButton<>(backItem, backAction));
    }

    /**
     * Erstellt das NPC-Info Item.
     */
    private ItemStack createNpcInfoItem() {
        ItemStack item = new ItemStack(Material.NAME_TAG);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("§e§l" + npcName)
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("§7Typ: §e" + getNpcTypeName(npcType))
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§7UUID: §e" + npcId.toString().substring(0, 8) + "...")
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§7Plot: §e" + plot.getIdentifier())
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text(""));
        lore.add(Component.text("§7Konfiguriere diesen NPC")
                .decoration(TextDecoration.ITALIC, false));

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Erstellt den Despawn-Button.
     */
    private ItemStack createDespawnButton() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("§c§lNPC Entfernen")
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("§7Entfernt diesen NPC permanent.")
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text(""));
        lore.add(Component.text("§c§lWarnung:")
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§7Diese Aktion kann nicht")
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§7rückgängig gemacht werden!")
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text(""));
        lore.add(Component.text("§c§lKlicke zum Entfernen")
                .decoration(TextDecoration.ITALIC, false));

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Erstellt den Back-Button.
     */
    private ItemStack createBackButton() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("§7§lZurück")
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("§7Zurück zur NPC-Verwaltung")
                .decoration(TextDecoration.ITALIC, false));

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Gibt den Anzeigenamen für einen NPC-Typ zurück.
     */
    private String getNpcTypeName(String type) {
        return switch (type.toLowerCase()) {
            case "guildtrader" -> "Gildenhändler";
            case "playertrader" -> "Spielerhändler";
            case "localbanker" -> "Lokaler Bankier";
            case "worldbanker" -> "Weltbankier";
            case "ambassador" -> "Botschafter";
            case "craftsman" -> "Handwerker";
            default -> "Unbekannt";
        };
    }

    /**
     * Action zum Zurückkehren zur NPC-Verwaltung.
     */
    private static class BackAction implements UiAction {

        private final Plot plot;
        private final PlotModule plotModule;

        public BackAction(Plot plot, PlotModule plotModule) {
            this.plot = plot;
            this.plotModule = plotModule;
        }

        @Override
        public void execute(Player player) {
            player.closeInventory();
            NpcManagementUi managementUi = new NpcManagementUi(plot, plotModule);
            managementUi.open(player);
        }

        @Override
        public String getActionName() {
            return "BackToNpcManagement";
        }
    }
}
