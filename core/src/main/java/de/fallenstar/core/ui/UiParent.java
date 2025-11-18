package de.fallenstar.core.ui;

import org.bukkit.entity.Player;

/**
 * Parent-Interface für alle UI-Typen im System.
 *
 * Definiert die grundlegendsten Operationen, die jedes UI bieten muss:
 * - Öffnen und Schließen
 * - Titel-Verwaltung
 *
 * Implementierungen:
 * - BaseUI (bestehendes System)
 * - GenericUiSmallChest (neues type-safe System)
 * - PageableBasicUi (pageable UIs)
 *
 * @author FallenStar
 * @version 2.0
 */
public interface UiParent {

    /**
     * Öffnet das UI für einen Spieler.
     *
     * @param player Spieler für den das UI geöffnet wird
     */
    void open(Player player);

    /**
     * Schließt das UI für einen Spieler.
     *
     * @param player Spieler für den das UI geschlossen wird
     */
    void close(Player player);

    /**
     * Gibt den Titel des UI zurück.
     *
     * @return UI-Titel
     */
    String getTitle();
}
