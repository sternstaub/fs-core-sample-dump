package de.fallenstar.core.interaction;

/**
 * Kontexte für UI-Aktionen.
 *
 * Bestimmt welche Aktionen ein UiActionTarget anbietet.
 *
 * @author FallenStar
 * @version 1.0
 */
public enum UiContext {

    /**
     * Hauptmenü eines Objekts.
     */
    MAIN_MENU,

    /**
     * Lager-Verwaltungs-Menü.
     */
    STORAGE_MENU,

    /**
     * NPC-Verwaltungs-Menü.
     */
    NPC_MENU,

    /**
     * Handels-Menü.
     */
    TRADE_MENU,

    /**
     * Preis-Verwaltungs-Menü.
     */
    PRICE_MENU,

    /**
     * Einstellungs-Menü.
     */
    SETTINGS_MENU,

    /**
     * Admin-Menü.
     */
    ADMIN_MENU
}
