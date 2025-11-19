package de.fallenstar.core.ui.element;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.provider.PlotProvider;
import de.fallenstar.core.registry.ProviderRegistry;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Abstrakte Basis-Klasse für Plot-gebundene Actions.
 *
 * **Command Pattern mit Objekt-Referenz:**
 * - Actions haben Plot-Referenz im Konstruktor
 * - canExecute() prüft Berechtigungen (z.B. isOwner)
 * - execute() führt Logik aus ODER öffnet Untermenü
 * - Wiederverwendbar und testbar
 *
 * **GuiRenderable-Integration (Sprint 18):**
 * Actions können sich selbst als ItemStack rendern:
 * <pre>
 * public class PlotActionSetName extends PlotAction {
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
 *     // getDisplayItem() automatisch generiert!
 * }
 * </pre>
 *
 * **MenuAction-Integration:**
 * Actions können hierarchische Untermenüs haben:
 * <pre>
 * public class ManageNpcsAction extends PlotAction {
 *     {@literal @}Override
 *     public List&lt;PlotAction&gt; getSubActions(Player player) {
 *         // Dynamisches Untermenü
 *         return plot.getNpcIds().stream()
 *             .map(npcId -> new ConfigureNpcAction(plot, npcId))
 *             .toList();
 *     }
 * }
 * </pre>
 *
 * **Beispiel (Simple Action):**
 * <pre>
 * public class SetNameAction extends PlotAction {
 *     public SetNameAction(Plot plot, ProviderRegistry providers) {
 *         super(plot, providers);
 *     }
 *
 *     {@literal @}Override
 *     protected boolean requiresOwnership() {
 *         return true;  // Nur Owner dürfen Namen setzen
 *     }
 *
 *     {@literal @}Override
 *     protected void executeAction(Player player) {
 *         // Öffne Name-Input-UI
 *         openNameInputUI(player);
 *     }
 * }
 * </pre>
 *
 * **Trait-Integration:**
 * <pre>
 * interface NamedPlot extends Plot {
 *     default List&lt;PlotAction&gt; getNameActions(ProviderRegistry providers) {
 *         return List.of(new SetNameAction(this, providers));
 *     }
 * }
 * </pre>
 *
 * @author FallenStar
 * @version 3.0
 */
public abstract class PlotAction implements UiAction, MenuAction, GuiRenderable {

    protected final Plot plot;
    protected final ProviderRegistry providers;

    /**
     * Erstellt eine neue Plot-Action.
     *
     * @param plot Der Plot auf dem die Action ausgeführt wird
     * @param providers ProviderRegistry für Owner-Checks und andere Provider
     */
    protected PlotAction(Plot plot, ProviderRegistry providers) {
        this.plot = Objects.requireNonNull(plot, "Plot darf nicht null sein");
        this.providers = Objects.requireNonNull(providers, "ProviderRegistry darf nicht null sein");
    }

    /**
     * Prüft ob diese Action Plot-Besitz erfordert.
     *
     * Überschreibe diese Methode um Owner-Requirement zu definieren.
     *
     * **Default:** false (keine Owner-Requirement)
     *
     * @return true wenn nur Owner diese Action ausführen dürfen
     */
    protected boolean requiresOwnership() {
        return false;
    }

    /**
     * Prüft ob ein Spieler zusätzliche Permissions benötigt.
     *
     * Überschreibe diese Methode um Permission-Requirement zu definieren.
     *
     * **Default:** null (keine Permission erforderlich)
     *
     * @return Permission-String oder null
     */
    protected String requiredPermission() {
        return null;
    }

    /**
     * Prüft ob ein Spieler diese Action ausführen darf.
     *
     * Diese Methode kombiniert:
     * 1. Owner-Check (wenn requiresOwnership() == true)
     * 2. Permission-Check (wenn requiredPermission() != null)
     * 3. Custom-Check (canExecuteCustom())
     *
     * @param player Der Spieler
     * @return true wenn berechtigt
     */
    @Override
    public final boolean canExecute(Player player) {
        // 1. Owner-Check
        if (requiresOwnership() && !isOwner(player)) {
            return false;
        }

        // 2. Permission-Check
        String permission = requiredPermission();
        if (permission != null && !player.hasPermission(permission)) {
            return false;
        }

        // 3. Custom-Check
        return canExecuteCustom(player);
    }

    /**
     * Custom Berechtigungsprüfung.
     *
     * Überschreibe diese Methode für spezielle Berechtigungslogik
     * (z.B. Level-Requirements, cooldowns, etc.).
     *
     * **Default:** true (immer erlaubt)
     *
     * @param player Der Spieler
     * @return true wenn erlaubt
     */
    protected boolean canExecuteCustom(Player player) {
        return true;
    }

    /**
     * Prüft ob ein Spieler der Besitzer des Plots ist.
     *
     * @param player Der Spieler
     * @return true wenn Owner
     */
    protected boolean isOwner(Player player) {
        PlotProvider plotProvider = providers.getPlotProvider();
        try {
            return plotProvider.isOwner(plot, player);
        } catch (Exception e) {
            // Bei Fehler: kein Owner
            return false;
        }
    }

    /**
     * Gibt den Plot zurück.
     *
     * @return Der Plot
     */
    public Plot getPlot() {
        return plot;
    }

    /**
     * Gibt die ProviderRegistry zurück.
     *
     * @return Die ProviderRegistry
     */
    public ProviderRegistry getProviders() {
        return providers;
    }

    // ========== UiAction Implementation ==========

    /**
     * Führt die Action aus.
     *
     * Diese Methode ist final und entscheidet automatisch:
     * - Wenn hasSubMenu() → Öffne Untermenü via openSubMenu()
     * - Sonst → Führe executeAction() aus
     *
     * Subklassen sollten executeAction() überschreiben, nicht execute()!
     *
     * @param player Der Spieler
     */
    @Override
    public final void execute(Player player) {
        if (hasSubMenu(player)) {
            // Öffne Untermenü
            openSubMenu(player);
        } else {
            // Führe normale Action aus
            executeAction(player);
        }
    }

    /**
     * Öffnet das Untermenü für diese Action.
     *
     * Wird automatisch von execute() aufgerufen wenn hasSubMenu() == true.
     *
     * TODO: Implementierung mit GuiBuilder (Sprint 18)
     * Aktuell: Placeholder mit Nachricht
     *
     * @param player Der Spieler
     */
    private void openSubMenu(Player player) {
        // TODO: Ersetzen durch GuiBuilder sobald implementiert
        // List<? extends GuiRenderable> subActions = getSubActions(player);
        // PageableGui gui = GuiBuilder.buildFrom(player, getSubMenuTitle(), subActions);
        // gui.open(player);

        // Placeholder:
        player.sendMessage("§e[Untermenü] §f" + getSubMenuTitle());
        player.sendMessage("§7TODO: GuiBuilder implementieren (Sprint 18)");
    }

    /**
     * Führt die Action-Logik aus (für normale Actions ohne Untermenü).
     *
     * Überschreibe diese Methode in Subklassen für normale Actions.
     * Wenn die Action ein Untermenü hat (getSubActions() nicht leer),
     * wird diese Methode NICHT aufgerufen!
     *
     * **Default:** Nichts tun
     *
     * @param player Der Spieler
     */
    protected void executeAction(Player player) {
        // Default: Nichts tun
        // Subklassen überschreiben diese Methode
    }

    @Override
    public String getActionName() {
        return this.getClass().getSimpleName() + "[" + plot.getIdentifier() + "]";
    }

    // ========== GuiRenderable Implementation ==========

    /**
     * Gibt das Icon-Material für diese Action zurück.
     *
     * Subklassen MÜSSEN diese Methode überschreiben um ihr Icon zu definieren.
     *
     * **Beispiele:**
     * - SetNameAction: Material.NAME_TAG
     * - ManageNpcsAction: Material.VILLAGER_SPAWN_EGG
     * - ManageStorageAction: Material.CHEST
     *
     * @return Icon-Material
     */
    protected abstract Material getIcon();

    /**
     * Gibt den Anzeige-Namen für diese Action zurück.
     *
     * Subklassen MÜSSEN diese Methode überschreiben.
     *
     * **Format:** Color-Code + Titel (z.B. "§dPlot-Name setzen")
     *
     * @return Anzeige-Name
     */
    protected abstract String getDisplayName();

    /**
     * Gibt die Lore-Zeilen für diese Action zurück.
     *
     * Subklassen MÜSSEN diese Methode überschreiben.
     *
     * **WICHTIG:** Permission-Lore wird automatisch hinzugefügt!
     * - Wenn canExecute() → keine Extra-Lore
     * - Wenn !canExecute() → "§c§l✗ Keine Berechtigung" wird angehängt
     *
     * **Format:**
     * - Beschreibung in §7 (grau)
     * - Werte in §e (gelb) oder §a (grün)
     * - Aktionen in §6 (gold)
     *
     * @return Lore-Zeilen (ohne Permission-Lore!)
     */
    protected abstract List<String> getLore();

    /**
     * Erstellt das Display-Item für diese Action.
     *
     * Diese Methode kombiniert:
     * 1. Icon (getIcon())
     * 2. DisplayName (getDisplayName())
     * 3. Lore (getLore())
     * 4. Permission-Lore (automatisch wenn !canExecute())
     *
     * Subklassen sollten getIcon/DisplayName/Lore überschreiben,
     * NICHT diese Methode!
     *
     * @param viewer Der betrachtende Spieler
     * @return ItemStack für GUI-Darstellung
     */
    @Override
    public ItemStack getDisplayItem(Player viewer) {
        ItemStack item = new ItemStack(getIcon());
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            // Fallback wenn ItemMeta fehlt (sollte nie passieren)
            return item;
        }

        // DisplayName setzen
        meta.setDisplayName(getDisplayName());

        // Lore zusammenbauen
        List<String> lore = new ArrayList<>(getLore());

        // Permission-Lore automatisch hinzufügen
        if (!canExecute(viewer)) {
            lore.add(""); // Leerzeile
            lore.add("§c§l✗ Keine Berechtigung");

            // Owner-Requirement anzeigen wenn zutreffend
            if (requiresOwnership() && !isOwner(viewer)) {
                lore.add("§7Nur für Plot-Owner");
            }

            // Permission-Requirement anzeigen wenn zutreffend
            String permission = requiredPermission();
            if (permission != null && !viewer.hasPermission(permission)) {
                lore.add("§7Benötigt: §e" + permission);
            }
        }

        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    /**
     * Prüft ob diese Action für einen Spieler sichtbar sein soll.
     *
     * **Default:** Immer sichtbar
     *
     * Subklassen können überschreiben für:
     * - Context-basierte Sichtbarkeit (z.B. "NPC-Actions nur wenn NPCs vorhanden")
     * - Permission-basierte Filterung (Admin-Actions komplett ausblenden)
     * - Status-basierte Filterung (Upgrade nur wenn Bedingungen erfüllt)
     *
     * **WICHTIG:**
     * - isVisible() filtert Elemente aus dem GUI
     * - canExecute() zeigt Elemente grau/mit Fehler-Lore
     *
     * @param viewer Der betrachtende Spieler
     * @return true wenn sichtbar
     */
    @Override
    public boolean isVisible(Player viewer) {
        return true; // Default: Immer sichtbar
    }

    /**
     * Gibt einen Debug-Namen für diese Action zurück.
     *
     * @return Debug-Name
     */
    @Override
    public String getRenderableName() {
        return getActionName();
    }
}
