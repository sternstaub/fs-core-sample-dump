package de.fallenstar.core.ui.container;

import de.fallenstar.core.ui.element.ClickableUiElement;
import de.fallenstar.core.ui.element.StaticUiElement;
import de.fallenstar.core.ui.element.UiAction;
import de.fallenstar.core.ui.element.navigation.CloseButton;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * Basis-UI für Grundstücke (GS = Grundstück).
 *
 * **Verwendung:**
 * Jedes Grundstück hat ein eigenes UI das dieses Template nutzt.
 * Subklassen implementieren spezifische Funktionen (Handelsgilde, Botschaft, etc.).
 *
 * **Type-Safety Beispiel:**
 * - Jede Funktion MUSS eine Action haben
 * - Actions sind type-safe (TeleportAction, SetPriceAction, etc.)
 * - UI-Elemente können nicht ohne Action erstellt werden
 *
 * **Layout:**
 * - Row 0: Grundstücks-Info (Name, Besitzer, Typ)
 * - Row 1: Funktionen (abhängig von GS-Typ)
 * - Row 2: Navigation (Schließen)
 *
 * **Migration (Sprint 19):**
 * Manuelle BasicGsUi-Subklassen wurden durch GuiBuilder + PlotAction-System ersetzt.
 * Verwende stattdessen: `GuiBuilder.buildFrom(player, title, plotActions)`
 *
 * @author FallenStar
 * @version 2.0
 */
public abstract class BasicGsUi extends BasicUi {

    /**
     * Konstruktor für Grundstücks-UI.
     *
     * @param title Titel des UI (z.B. "Handelsgilde: Marktplatz")
     */
    protected BasicGsUi(String title) {
        super(title);

        // Setup Close Button
        getControlRow().setCloseButton(CloseButton.create(this));
    }

    /**
     * Fügt ein Info-Element zur Info-Row hinzu.
     *
     * Info-Elemente sind statisch (nicht klickbar).
     *
     * @param material Material für das Icon
     * @param displayName Anzeigename
     * @param lore Beschreibung
     */
    protected void addInfoElement(Material material, String displayName, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        getContentRow1().append(new StaticUiElement(item));
    }

    /**
     * Fügt eine klickbare Funktion zur Funktions-Row hinzu.
     *
     * **Type-Safety:**
     * - action MUSS vom Typ T extends UiAction sein
     * - Compiler erzwingt korrekten Action-Typ
     *
     * @param material Material für den Button
     * @param displayName Anzeigename
     * @param lore Beschreibung
     * @param action Action die beim Click ausgeführt wird
     * @param <T> Action-Typ (extends UiAction)
     */
    protected <T extends UiAction> void addFunctionButton(
            Material material,
            String displayName,
            List<String> lore,
            T action) {

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        // Type-Safe: CustomButton erzwingt Action-Parameter!
        var button = new ClickableUiElement.CustomButton<>(item, action);
        getContentRow2().append(button);
    }

    /**
     * Beispiel für eine Grundstücks-Action.
     *
     * Diese Action könnte in einem Modul (z.B. module-plots) definiert werden.
     */
    public static class ExamplePlotAction implements UiAction {
        private final String plotName;

        public ExamplePlotAction(String plotName) {
            this.plotName = plotName;
        }

        @Override
        public void execute(Player player) {
            player.sendMessage("§aGrundstücks-Funktion ausgeführt für: " + plotName);
        }

        @Override
        public String getActionName() {
            return "ExamplePlotAction[" + plotName + "]";
        }
    }

    /**
     * Beispiel-Implementierung: Handelsgilde-UI.
     *
     * Zeigt wie man BasicGsUi erweitert.
     */
    public static class HandelsgildeExampleUi extends BasicGsUi {
        public HandelsgildeExampleUi(String plotName) {
            super("Handelsgilde: " + plotName);

            // Info-Elemente
            addInfoElement(
                    Material.GOLD_INGOT,
                    "§6§lHandelsgilde",
                    List.of(
                            "§7Name: §e" + plotName,
                            "§7Typ: §eHandelsgilde"
                    )
            );

            // Funktionen (Type-Safe!)
            addFunctionButton(
                    Material.EMERALD,
                    "§a§lPreise festlegen",
                    List.of("§7Klicke um Preise zu setzen"),
                    new ExamplePlotAction(plotName) // Action MUSS übergeben werden!
            );

            addFunctionButton(
                    Material.CHEST,
                    "§6§lLager anzeigen",
                    List.of("§7Klicke um das Lager zu öffnen"),
                    new ExamplePlotAction(plotName)
            );
        }
    }
}
