package de.fallenstar.core.interaction;

/**
 * Typen von Interaktionen im System.
 *
 * Ermöglicht differenzierte Behandlung verschiedener Klick-Quellen.
 *
 * @author FallenStar
 * @version 1.0
 */
public enum InteractionType {

    /**
     * Block-Interaktion (rechts/links-klick auf Block).
     */
    BLOCK,

    /**
     * Entity-Interaktion (rechts/links-klick auf Entity).
     */
    ENTITY,

    /**
     * Item-Interaktion (rechts/links-klick mit Item).
     */
    ITEM,

    /**
     * Plot-Interaktion (abgeleitete Interaktion über Location).
     */
    PLOT,

    /**
     * World-Interaktion (generische Welt-Interaktion).
     */
    WORLD
}
