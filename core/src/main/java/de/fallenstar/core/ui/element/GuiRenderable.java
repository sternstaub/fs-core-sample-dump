package de.fallenstar.core.ui.element;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Interface für selbst-rendernde GUI-Elemente.
 *
 * **Konzept:**
 * Objekte die GuiRenderable implementieren können sich selbst als ItemStack
 * für GUIs darstellen. Dies ermöglicht universelle GUI-Builder die aus
 * Action-Listen automatisch PageableGuis generieren.
 *
 * **Design-Evolution:**
 * <pre>
 * Phase 1 (Sprint 15): UiActionInfo (Metadaten)
 *   └─> Action-ID + Icon + Lore → switch(actionId) in executeAction()
 *
 * Phase 2 (Sprint 17): PlotAction (Command Pattern)
 *   └─> Action kennt Logik + Permissions → canExecute() + execute()
 *
 * Phase 3 (Sprint 18): GuiRenderable (Self-Rendering Actions)
 *   └─> Action kennt Logik + Permissions + Display → getDisplayItem()
 * </pre>
 *
 * **Vorteile:**
 * - Actions kapseln Logik, Berechtigungen UND Display-Darstellung
 * - Universal GuiBuilder für ALLE Plot-Typen möglich
 * - DRY: Keine Duplikation zwischen UiActionInfo und Action
 * - Automatische Permission-Lore (grau/rot wenn keine Berechtigung)
 * - Dynamisches Rendering basierend auf Viewer
 *
 * **Verwendung:**
 * <pre>
 * public class PlotActionSetName extends PlotAction implements GuiRenderable {
 *     {@literal @}Override
 *     protected Material getIcon() {
 *         return Material.NAME_TAG;
 *     }
 *
 *     {@literal @}Override
 *     protected String getDisplayName() {
 *         return "§dPlot-Name setzen";
 *     }
 *
 *     {@literal @}Override
 *     protected List&lt;String&gt; getLore() {
 *         return List.of(
 *             "§7Aktueller Name: §e" + plot.getDisplayName(),
 *             "§7Klicke um den Namen zu ändern"
 *         );
 *     }
 *
 *     // getDisplayItem() automatisch generiert durch PlotAction!
 * }
 *
 * // Universal GuiBuilder:
 * List&lt;PlotAction&gt; actions = plot.getAvailablePlotActions(player);
 * PageableGui gui = GuiBuilder.buildFrom(player, "§6Plot-Verwaltung", actions);
 * gui.open(player);
 * </pre>
 *
 * **Integration mit UiAction:**
 * <pre>
 * public abstract class PlotAction implements UiAction, MenuAction, GuiRenderable {
 *     // Display-Logik
 *     {@literal @}Override
 *     public ItemStack getDisplayItem(Player viewer) {
 *         ItemStack item = new ItemStack(getIcon());
 *         ItemMeta meta = item.getItemMeta();
 *         meta.setDisplayName(getDisplayName());
 *
 *         List&lt;String&gt; lore = new ArrayList&lt;&gt;(getLore());
 *         if (!canExecute(viewer)) {
 *             lore.add("§c§l✗ Keine Berechtigung");
 *         }
 *         meta.setLore(lore);
 *
 *         item.setItemMeta(meta);
 *         return item;
 *     }
 *
 *     // Ausführungs-Logik
 *     {@literal @}Override
 *     public void execute(Player player) { ... }
 *
 *     // Berechtigungs-Logik
 *     {@literal @}Override
 *     public boolean canExecute(Player player) { ... }
 * }
 * </pre>
 *
 * @author FallenStar
 * @version 1.0
 * @see PlotAction
 * @see UiAction
 * @see MenuAction
 */
public interface GuiRenderable {

    /**
     * Erstellt das Display-Item für dieses GUI-Element.
     *
     * Das Item wird im GUI angezeigt und repräsentiert diese Action/Element.
     * Die Implementierung sollte:
     * - Icon (Material) basierend auf Context setzen
     * - DisplayName mit Color-Coding
     * - Lore mit Beschreibung + Status (Berechtigungen, etc.)
     * - Optional: Enchantments/Glow für visuelle Hervorhebung
     *
     * **Viewer-basiertes Rendering:**
     * Das Item kann basierend auf dem Viewer unterschiedlich dargestellt werden:
     * - Owner vs. Guest (unterschiedliche Lore)
     * - Permissions (rot markiert wenn keine Berechtigung)
     * - Cooldowns (grau wenn Cooldown aktiv)
     * - Status (Quest abgeschlossen → grün, offen → gelb)
     *
     * **Beispiel-Implementierung:**
     * <pre>
     * {@literal @}Override
     * public ItemStack getDisplayItem(Player viewer) {
     *     ItemStack item = new ItemStack(Material.NAME_TAG);
     *     ItemMeta meta = item.getItemMeta();
     *     meta.setDisplayName("§dPlot-Name setzen");
     *
     *     List&lt;String&gt; lore = new ArrayList&lt;&gt;();
     *     lore.add("§7Aktueller Name: §e" + plot.getDisplayName());
     *
     *     if (canExecute(viewer)) {
     *         lore.add("§a§l✓ Klicke zum Umbenennen");
     *     } else {
     *         lore.add("§c§l✗ Nur für Plot-Owner");
     *     }
     *
     *     meta.setLore(lore);
     *     item.setItemMeta(meta);
     *     return item;
     * }
     * </pre>
     *
     * @param viewer Der betrachtende Spieler (für Permission-Checks und Context)
     * @return ItemStack für GUI-Darstellung
     */
    ItemStack getDisplayItem(Player viewer);

    /**
     * Prüft ob dieses Element für einen Spieler sichtbar sein soll.
     *
     * Ermöglicht dynamisches Filtern von GUI-Elementen basierend auf:
     * - Permissions (bestimmte Actions nur für Admins)
     * - Context (Quest-Actions nur wenn Quest verfügbar)
     * - Status (Upgrade-Button nur wenn genug Geld)
     * - Owner-Status (Owner-Actions nur für Owner)
     *
     * **Default:** Immer sichtbar (true)
     *
     * **Verwendung in GuiBuilder:**
     * <pre>
     * List&lt;GuiRenderable&gt; visibleActions = allActions.stream()
     *     .filter(action -> action.isVisible(player))
     *     .toList();
     * </pre>
     *
     * **Beispiel-Implementierungen:**
     * <pre>
     * // Immer sichtbar (Default)
     * {@literal @}Override
     * public boolean isVisible(Player viewer) {
     *     return true;
     * }
     *
     * // Nur für Admins
     * {@literal @}Override
     * public boolean isVisible(Player viewer) {
     *     return viewer.hasPermission("fallenstar.admin");
     * }
     *
     * // Nur wenn Quest verfügbar
     * {@literal @}Override
     * public boolean isVisible(Player viewer) {
     *     return questManager.hasAvailableQuests(viewer);
     * }
     *
     * // Nur für Plot-Owner
     * {@literal @}Override
     * public boolean isVisible(Player viewer) {
     *     return plotProvider.isOwner(plot, viewer);
     * }
     * </pre>
     *
     * **WICHTIG:**
     * - Sichtbarkeit (isVisible) ≠ Ausführbarkeit (canExecute)
     * - isVisible() filtert Elemente aus dem GUI
     * - canExecute() zeigt Elemente grau/mit Fehler-Lore
     *
     * @param viewer Der betrachtende Spieler
     * @return true wenn sichtbar, false wenn ausgeblendet
     */
    default boolean isVisible(Player viewer) {
        return true; // Default: Immer sichtbar
    }

    /**
     * Gibt einen Debug-Namen für dieses GUI-Element zurück.
     *
     * Nützlich für Logging und Debugging bei GuiBuilder.
     *
     * **Default:** Klassenname
     *
     * @return Debug-Name
     */
    default String getRenderableName() {
        return this.getClass().getSimpleName();
    }
}
