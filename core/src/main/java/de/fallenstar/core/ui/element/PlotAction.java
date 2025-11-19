package de.fallenstar.core.ui.element;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.provider.PlotProvider;
import de.fallenstar.core.registry.ProviderRegistry;
import org.bukkit.entity.Player;

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
 * @version 2.0
 */
public abstract class PlotAction implements UiAction, MenuAction {

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
}
