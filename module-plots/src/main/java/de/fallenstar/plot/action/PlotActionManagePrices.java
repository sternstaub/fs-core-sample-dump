package de.fallenstar.plot.action;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.registry.ProviderRegistry;
import de.fallenstar.core.ui.element.PlotAction;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Action zum Setzen von Preisen auf einem Grundstück.
 *
 * **Naming Convention:** PlotAction* Prefix für alphabetische Hierarchie-Erkennung
 *
 * **Command Pattern:**
 * - Erweitert PlotAction (Plot-Referenz + canExecute)
 * - Owner-Requirement (nur Owner dürfen Preise setzen)
 * - Aktiviert Preis-Setzungs-Modus
 *
 * **Preis-Setzungs-Workflow:**
 * 1. Nimm ein Item in die Hand
 * 2. Rechtsklicke
 * 3. Setze den Preis im UI
 *
 * **GuiRenderable:**
 * - Self-Rendering via getDisplayItem()
 * - Automatische Permission-Lore bei !canExecute()
 *
 * **Verwendung:**
 * ```java
 * new PlotActionManagePrices(plot, providers)
 * ```
 *
 * @author FallenStar
 * @version 3.0 (Sprint 19 - Migration zu PlotAction)
 */
public final class PlotActionManagePrices extends PlotAction {

    /**
     * Konstruktor für PlotActionManagePrices.
     *
     * @param plot Der Plot auf dem Preise gesetzt werden
     * @param providers ProviderRegistry für Owner-Checks
     */
    public PlotActionManagePrices(Plot plot, ProviderRegistry providers) {
        super(plot, providers);
    }

    @Override
    protected boolean requiresOwnership() {
        return true; // Nur Owner dürfen Preise setzen
    }

    @Override
    protected String requiredPermission() {
        return "fallenstar.plot.price.set";
    }

    @Override
    protected void executeAction(Player player) {
        player.closeInventory();
        player.performCommand("plot price set");
    }

    // ========== GuiRenderable Implementation ==========

    @Override
    protected Material getIcon() {
        return Material.WRITABLE_BOOK;
    }

    @Override
    protected String getDisplayName() {
        return "§ePreise setzen";
    }

    @Override
    protected List<String> getLore() {
        List<String> lore = new ArrayList<>();

        lore.add("§7Aktiviert den Preis-Setzungs-Modus:");
        lore.add("§7");
        lore.add("§e1. §7Item in die Hand nehmen");
        lore.add("§e2. §7Rechtsklick");
        lore.add("§e3. §7Preis im UI setzen");
        lore.add("§7");
        lore.add("§7Klicke zum Aktivieren");

        return lore;
    }
}
