package de.fallenstar.core.provider;

import de.fallenstar.core.exception.ProviderFunctionalityNotFoundException;
import de.fallenstar.core.ui.UIMenu;
import org.bukkit.entity.Player;

/**
 * Provider-Interface für UI-Systeme.
 *
 * Implementierungen:
 * - InventoryUIProvider (UI-Modul) - Inventory-basierte GUIs
 * - NativeTextUIProvider (Core) - Chat-basierte Fallback-UI
 * - NoOpUIProvider (Fallback)
 *
 * Das UIProvider-Pattern erlaubt verschiedene UI-Rendering-Strategien:
 * - **Inventory-UI:** Grafische Menüs mit Items (benötigt UI-Modul)
 * - **Text-UI:** Chat-basiert mit anklickbaren Links (Core-Fallback)
 *
 * @author FallenStar
 * @version 1.0
 */
public interface UIProvider {

    /**
     * Prüft ob dieser Provider verfügbar ist.
     *
     * @return true wenn verfügbar
     */
    boolean isAvailable();

    /**
     * Zeigt ein UI-Menü für einen Spieler an.
     *
     * Je nach Implementierung:
     * - Inventory-basiert (UI-Modul)
     * - Chat-basiert (Native Fallback)
     *
     * @param player Spieler
     * @param menu UIMenu mit Buttons
     * @throws ProviderFunctionalityNotFoundException wenn nicht verfügbar
     */
    void showMenu(Player player, UIMenu menu)
            throws ProviderFunctionalityNotFoundException;

    /**
     * Gibt den Typ des UI-Providers zurück.
     *
     * Beispiele: "inventory", "text", "noop"
     *
     * @return Provider-Typ
     * @throws ProviderFunctionalityNotFoundException wenn nicht verfügbar
     */
    String getProviderType()
            throws ProviderFunctionalityNotFoundException;

    /**
     * Schließt alle offenen UIs für einen Spieler.
     *
     * @param player Spieler
     * @throws ProviderFunctionalityNotFoundException wenn nicht verfügbar
     */
    void closeAll(Player player)
            throws ProviderFunctionalityNotFoundException;
}
