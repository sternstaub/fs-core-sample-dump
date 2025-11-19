package de.fallenstar.core.provider;

import org.bukkit.Location;
import java.util.UUID;

/**
 * Interface für alle Plot-Typen.
 *
 * Ermöglicht Trait-Composition anstelle von Klassen-Vererbung.
 *
 * **Konzept:**
 * - Plot ist Interface (nicht Klasse!)
 * - Konkrete Implementierungen kombinieren Traits
 * - Type-Safe durch Interface-Hierarchie
 *
 * **Basis-Implementierung:**
 * - {@link BasePlot} - Standard-Implementierung für einfache Plots
 *
 * **Trait-Interfaces:**
 * - {@link NamedPlot} - Plot mit Custom-Namen
 * - {@link StorageContainerPlot} - Plot mit Lager-Funktionalität
 * - {@link NpcContainerPlot} - Plot mit NPC-Verwaltung
 * - {@link SlottablePlot} - Plot mit Trader-Slots
 *
 * **Beispiel:**
 * <pre>
 * // Einfacher Plot
 * Plot plot = new BasePlot(uuid, identifier, location, nativePlot);
 *
 * // Handelsgilde (mehrere Traits)
 * class TradeguildPlot extends BasePlot implements NamedPlot,
 *                                                   StorageContainerPlot,
 *                                                   NpcContainerPlot,
 *                                                   SlottablePlot {
 *     // Implementierungen...
 * }
 * </pre>
 *
 * @author FallenStar
 * @version 1.0
 */
public interface Plot {

    /**
     * Gibt die eindeutige UUID des Plots zurück.
     *
     * @return UUID
     */
    UUID getUuid();

    /**
     * Gibt den lesbaren Identifier zurück.
     *
     * Format: z.B. "TownName_PlotID"
     *
     * @return Identifier
     */
    String getIdentifier();

    /**
     * Gibt eine Location innerhalb des Plots zurück.
     *
     * @return Location
     */
    Location getLocation();

    /**
     * Gibt das originale Plot-Objekt zurück.
     *
     * Sollte nur von Provider-Implementierungen verwendet werden.
     *
     * @param <T> Typ des nativen Plot-Objekts
     * @return Das native Plot-Objekt (TownBlock, Claim, etc.)
     */
    <T> T getNativePlot();
}
