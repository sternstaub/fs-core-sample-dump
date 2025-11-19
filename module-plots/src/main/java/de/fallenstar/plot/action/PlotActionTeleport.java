package de.fallenstar.plot.action;

import de.fallenstar.core.provider.NamedPlot;
import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.registry.ProviderRegistry;
import de.fallenstar.core.ui.element.PlotAction;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Action zum Teleportieren zum Grundstück.
 *
 * **Naming Convention:** PlotAction* Prefix für alphabetische Hierarchie-Erkennung
 *
 * **Command Pattern:**
 * - Erweitert PlotAction (Plot-Referenz + canExecute)
 * - Owner-Requirement (nur Owner dürfen teleportieren)
 * - Optional: Permission "fallenstar.plot.teleport"
 *
 * **Teleport-Logik:**
 * - Teleportiert zum Plot-Center
 * - Sucht sicheren Spawn-Punkt (Y+1)
 * - Feedback-Message an Spieler
 *
 * **GuiRenderable:**
 * - Self-Rendering via getDisplayItem()
 * - Zeigt Koordinaten in Lore
 *
 * **Verwendung:**
 * ```java
 * new PlotActionTeleport(plot, providers)
 * ```
 *
 * @author FallenStar
 * @version 1.0 (Sprint 19 - Neue Action)
 */
public final class PlotActionTeleport extends PlotAction {

    /**
     * Konstruktor für PlotActionTeleport.
     *
     * @param plot Der Plot zu dem teleportiert werden soll
     * @param providers ProviderRegistry für Owner-Checks
     */
    public PlotActionTeleport(Plot plot, ProviderRegistry providers) {
        super(plot, providers);
    }

    @Override
    protected boolean requiresOwnership() {
        return true; // Nur Owner dürfen teleportieren (oder mit Permission)
    }

    @Override
    protected String requiredPermission() {
        return "fallenstar.plot.teleport";
    }

    @Override
    protected void executeAction(Player player) {
        player.closeInventory();

        // Hole Plot-Location
        Location plotLocation = plot.getLocation();

        if (plotLocation == null || plotLocation.getWorld() == null) {
            player.sendMessage("§cTeleport fehlgeschlagen: Ungültige Plot-Location!");
            return;
        }

        // Teleport zum Plot-Center (Y+1 für sicheren Spawn)
        Location teleportLoc = plotLocation.clone();
        teleportLoc.setY(teleportLoc.getY() + 1);

        // Teleport
        player.teleport(teleportLoc);

        // Feedback
        String plotName = plot instanceof de.fallenstar.core.provider.NamedPlot namedPlot
                ? namedPlot.getDisplayName()
                : plot.getIdentifier();
        player.sendMessage("§a✓ Teleportiert zu: §e" + plotName);
    }

    // ========== GuiRenderable Implementation ==========

    @Override
    protected Material getIcon() {
        return Material.ENDER_PEARL;
    }

    @Override
    protected String getDisplayName() {
        return "§bZum Plot teleportieren";
    }

    @Override
    protected List<String> getLore() {
        List<String> lore = new ArrayList<>();

        lore.add("§7Teleportiert dich zum");
        lore.add("§7Zentrum dieses Grundstücks");

        // Zeige Plot-Name wenn NamedPlot
        if (plot instanceof NamedPlot namedPlot) {
            namedPlot.getCustomName().ifPresent(name -> {
                lore.add("");
                lore.add("§7Ziel: §e" + name);
            });
        }

        // Zeige Koordinaten
        Location loc = plot.getLocation();
        if (loc != null) {
            lore.add("");
            lore.add("§7Position:");
            lore.add("§7X: §e" + loc.getBlockX());
            lore.add("§7Y: §e" + loc.getBlockY());
            lore.add("§7Z: §e" + loc.getBlockZ());
        }

        lore.add("");
        lore.add("§7Klicke zum Teleportieren");

        return lore;
    }
}
