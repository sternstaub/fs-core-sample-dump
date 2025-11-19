package de.fallenstar.core.ui.builder;

import de.fallenstar.core.ui.container.PageableBasicUi;
import de.fallenstar.core.ui.element.ClickableUiElement;
import de.fallenstar.core.ui.element.GuiRenderable;
import de.fallenstar.core.ui.element.UiAction;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Builder für universelle GUIs aus GuiRenderable-Listen.
 *
 * **Konzept:**
 * Actions die sowohl GuiRenderable als auch UiAction implementieren
 * können sich selbst rendern UND ausführen. Der GuiBuilder erstellt
 * automatisch ein PageableBasicUi aus solchen Action-Listen.
 *
 * **Design-Evolution (Sprint 18):**
 * <pre>
 * Phase 1: UiActionInfo (Metadaten) + switch(actionId)
 *   → Duplikation zwischen Action-Display und Action-Logik
 *
 * Phase 2: PlotAction (Command Pattern) + GuiRenderable
 *   → Actions kennen Display + Logik + Permissions
 *
 * Phase 3: GuiBuilder (Universal GUI Construction)
 *   → EINE Methode für ALLE Plot-Typen
 * </pre>
 *
 * **Vorteile:**
 * - **Universal:** Ein Builder für ALLE Plot-Typen
 * - **DRY:** Keine Duplikation mehr zwischen UI-Code
 * - **Type-Safe:** Intersection Types garantieren GuiRenderable + UiAction
 * - **Automatisch:** Permission-Checks → Lore-Updates
 * - **Erweiterbar:** Neue PlotAction → automatisch im GUI
 * - **Filterbar:** isVisible() filtert unsichtbare Actions
 *
 * **Verwendung:**
 * <pre>
 * // Plot-Verwaltungs-UI
 * List&lt;PlotAction&gt; actions = plot.getAvailablePlotActions(player);
 * PageableBasicUi gui = GuiBuilder.buildFrom(player, "§6Plot-Verwaltung", actions);
 * gui.open(player);
 *
 * // NPC-Verwaltungs-UI (Untermenü)
 * List&lt;PlotAction&gt; npcActions = plot.getNpcIds().stream()
 *     .map(id -> new PlotActionConfigureNpc(plot, id, providers))
 *     .toList();
 * PageableBasicUi npcGui = GuiBuilder.buildFrom(player, "§bNPC-Verwaltung", npcActions);
 * npcGui.open(player);
 * </pre>
 *
 * **Integration mit PlotAction:**
 * <pre>
 * public class PlotActionSetName extends PlotAction {
 *     // GuiRenderable:
 *     {@literal @}Override protected Material getIcon() { return Material.NAME_TAG; }
 *     {@literal @}Override protected String getDisplayName() { return "§dPlot-Name setzen"; }
 *     {@literal @}Override protected List&lt;String&gt; getLore() { return List.of(...); }
 *
 *     // UiAction:
 *     {@literal @}Override public void execute(Player player) { ... }
 *     {@literal @}Override public boolean canExecute(Player player) { return isOwner(player); }
 *
 *     // GuiBuilder erstellt automatisch:
 *     // - ItemStack via getDisplayItem(player)
 *     // - ClickableUiElement mit Action
 *     // - PageableBasicUi mit Pagination
 * }
 * </pre>
 *
 * **Ersetzte Klassen:**
 * - HandelsgildeUi (deprecated) → GuiBuilder + TradeguildPlot.getAvailablePlotActions()
 * - Manuelle UI-Konstruktion → Automatisch via GuiBuilder
 * - UiActionInfo-Switch-Statements → Self-Executing Actions
 *
 * @author FallenStar
 * @version 1.0
 * @see GuiRenderable
 * @see PlotAction
 * @see PageableBasicUi
 */
public final class GuiBuilder {

    /**
     * Private Konstruktor - Utility-Klasse.
     */
    private GuiBuilder() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Erstellt ein PageableBasicUi aus einer Liste von GuiRenderable-Actions.
     *
     * Diese Methode:
     * 1. Filtert Actions via isVisible(viewer)
     * 2. Erstellt ItemStacks via getDisplayItem(viewer)
     * 3. Erstellt ClickableUiElements mit Actions
     * 4. Packt alles in PageableBasicUi mit automatischer Pagination
     *
     * **Intersection Type:**
     * `T extends GuiRenderable & UiAction` garantiert, dass T:
     * - GuiRenderable ist (hat getDisplayItem + isVisible)
     * - UiAction ist (hat execute + canExecute)
     *
     * **PlotAction erfüllt beide Interfaces!**
     *
     * **Beispiel:**
     * <pre>
     * // TradeguildPlot liefert PlotActions:
     * List&lt;PlotAction&gt; actions = plot.getAvailablePlotActions(player);
     *
     * // GuiBuilder erstellt PageableBasicUi:
     * PageableBasicUi gui = GuiBuilder.buildFrom(player, "§6Plot-Verwaltung", actions);
     * gui.open(player);
     * </pre>
     *
     * **Automatische Features:**
     * - Permission-Lore: Actions mit !canExecute() zeigen "§c§l✗ Keine Berechtigung"
     * - Sichtbarkeits-Filter: Actions mit !isVisible() werden ausgeblendet
     * - Pagination: Automatisch wenn > 18 Actions
     * - Navigation: Vor/Zurück-Buttons wenn mehrere Seiten
     *
     * **Leere Liste:**
     * Wenn keine Actions sichtbar sind, wird trotzdem ein leeres GUI erstellt.
     *
     * @param viewer Der Spieler der das GUI sieht (für isVisible/getDisplayItem)
     * @param title Titel des GUIs
     * @param actions Liste von Actions (GuiRenderable + UiAction)
     * @param <T> Action-Typ (muss GuiRenderable UND UiAction sein)
     * @return PageableBasicUi mit allen sichtbaren Actions
     */
    public static <T extends GuiRenderable & UiAction> PageableBasicUi buildFrom(
            Player viewer,
            String title,
            List<T> actions
    ) {
        PageableBasicUi ui = new PageableBasicUi(title);

        // Filtere und konvertiere Actions zu UiElements
        for (T action : actions) {
            // Prüfe Sichtbarkeit
            if (!action.isVisible(viewer)) {
                continue; // Action ausblenden
            }

            // Erstelle Display-Item (mit automatischer Permission-Lore!)
            ItemStack displayItem = action.getDisplayItem(viewer);

            // Erstelle klickbares UI-Element
            var button = new ClickableUiElement.CustomButton<>(displayItem, action);

            // Füge zum UI hinzu
            ui.append(button);
        }

        return ui;
    }

    /**
     * Convenience-Methode für PlotAction-Listen.
     *
     * Diese Methode ist identisch zu buildFrom(), aber verwendet
     * Raw-Type Wildcards für Flexibilität.
     *
     * **Verwendung:**
     * <pre>
     * List&lt;PlotAction&gt; actions = plot.getAvailablePlotActions(player);
     * PageableBasicUi gui = GuiBuilder.buildFromPlotActions(player, "§6Plot-Verwaltung", actions);
     * gui.open(player);
     * </pre>
     *
     * **Hinweis:** Nutze buildFrom() für volle Type-Safety mit Intersection Types.
     *
     * @param viewer Der Spieler der das GUI sieht
     * @param title Titel des GUIs
     * @param actions Liste von Actions (sollten GuiRenderable + UiAction sein)
     * @return PageableBasicUi mit allen sichtbaren Actions
     */
    public static PageableBasicUi buildFromPlotActions(
            Player viewer,
            String title,
            List<? extends GuiRenderable> actions
    ) {
        PageableBasicUi ui = new PageableBasicUi(title);

        for (var renderable : actions) {
            if (!renderable.isVisible(viewer)) {
                continue;
            }

            // Runtime-Check: GuiRenderable sollte auch UiAction sein
            if (!(renderable instanceof UiAction action)) {
                continue; // Überspringe Non-UiActions
            }

            ItemStack displayItem = renderable.getDisplayItem(viewer);
            var button = new ClickableUiElement.CustomButton<>(displayItem, action);
            ui.append(button);
        }

        return ui;
    }
}
