package de.fallenstar.plot.action;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.registry.ProviderRegistry;
import de.fallenstar.core.ui.element.PlotAction;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Action zum Teleportieren zum Plot.
 *
 * **Berechtigungen:**
 * - KEINE Owner-Requirement - Gäste dürfen sich zum Plot teleportieren!
 * - Optional: Permission "fallenstar.plot.teleport"
 *
 * **Command Pattern:**
 * - Erweitert PlotAction (Plot-Referenz + canExecute)
 * - Teleportiert Spieler zu plot.getLocation()
 * - Für Owner + Gäste verfügbar
 *
 * **GuiRenderable (Sprint 18):**
 * - Icon: Material.ENDER_PEARL
 * - DisplayName: "§dTeleportieren"
 * - Lore: Info zum Teleport-Ziel
 *
 * **Funktionalität:**
 * - Teleportiert zu Plot-Location
 * - Bestätigungs-Nachricht
 * - Fehlerbehandlung bei ungültiger Location
 * - Funktioniert für alle Spieler
 *
 * **Sicherheit:**
 * - Location-Validation (kein Teleport ins Void)
 * - Cooldown könnte später hinzugefügt werden
 *
 * **Verwendung:**
 * ```java
 * // In TradeguildPlot.getAvailablePlotActions():
 * actions.add(new PlotActionTeleport(this, providers));
 * ```
 *
 * @author FallenStar
 * @version 1.0
 */
public final class PlotActionTeleport extends PlotAction {

    /**
     * Erstellt eine neue PlotActionTeleport.
     *
     * @param plot Der Plot zu dem teleportiert werden soll
     * @param providers ProviderRegistry für Checks
     */
    public PlotActionTeleport(Plot plot, ProviderRegistry providers) {
        super(plot, providers);
    }

    @Override
    protected boolean requiresOwnership() {
        return false; // Gäste dürfen sich zum Plot teleportieren!
    }

    @Override
    protected String requiredPermission() {
        return "fallenstar.plot.teleport";
    }

    @Override
    protected void executeAction(Player player) {
        player.closeInventory();

        // Hole Plot-Location
        Location location = plot.getLocation();

        if (location == null || location.getWorld() == null) {
            player.sendMessage("§cTeleport fehlgeschlagen: Ungültige Location.");
            return;
        }

        // Teleportiere Spieler
        boolean success = player.teleport(location);

        if (success) {
            player.sendMessage("§aDu wurdest zum Plot §e" + plot.getIdentifier() + " §ateleportiert.");
        } else {
            player.sendMessage("§cTeleport fehlgeschlagen. Versuche es erneut.");
        }
    }

    // ========== GuiRenderable Implementation ==========

    @Override
    protected Material getIcon() {
        return Material.ENDER_PEARL;
    }

    @Override
    protected String getDisplayName() {
        return "§dTeleportieren";
    }

    @Override
    protected List<String> getLore() {
        List<String> lore = new ArrayList<>();

        lore.add("§7Teleportiere dich zu diesem Plot");
        lore.add("");

        // Location-Info anzeigen
        Location loc = plot.getLocation();
        if (loc != null && loc.getWorld() != null) {
            lore.add("§7Ziel: §e" + plot.getIdentifier());
            lore.add("§7Welt: §e" + loc.getWorld().getName());
            lore.add("§7Position: §e" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
        } else {
            lore.add("§cLocation nicht verfügbar");
        }

        lore.add("");
        lore.add("§d§l➤ Klicke zum Teleportieren");

        return lore;
    }

    @Override
    public boolean isVisible(Player viewer) {
        // Nur anzeigen wenn Location valide ist
        Location loc = plot.getLocation();
        return loc != null && loc.getWorld() != null;
    }
}
