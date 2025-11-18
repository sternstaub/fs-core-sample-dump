package de.fallenstar.core.ui.container;

import de.fallenstar.core.ui.element.UiElement;
import de.fallenstar.core.ui.element.navigation.NavigateLeftButton;
import de.fallenstar.core.ui.element.navigation.NavigateRightButton;
import de.fallenstar.core.ui.element.navigation.PageNavigationAction;
import de.fallenstar.core.ui.element.navigation.CloseButton;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Pageable Basic UI - UI mit automatischer Pagination.
 *
 * **Features:**
 * - Unendlich viele Items hinzufügbar via append()
 * - Automatische Aufteilung auf mehrere Seiten
 * - Navigation via Pfeiltasten (Links/Rechts)
 * - Type-Safe Navigation (PageNavigationAction)
 *
 * **Layout pro Seite:**
 * - Row 0 (Slots 0-8): Content (9 Items)
 * - Row 1 (Slots 9-17): Content (9 Items)
 * - Row 2 (Slots 18-26): Navigation (Links, Close, Rechts)
 *
 * **Items pro Seite:** 18 (2 Content-Rows × 9 Slots)
 *
 * **Type-Safety:**
 * - Implements PageNavigable → erzwingt Navigation-Methoden
 * - Navigation-Buttons sind type-safe (PageNavigationAction)
 * - Compiler verhindert fehlende Navigation
 *
 * **Verwendung:**
 * ```java
 * var ui = new PageableBasicUi("Shop");
 * ui.append(shopItem1);
 * ui.append(shopItem2);
 * // ...
 * ui.open(player); // Öffnet Seite 1
 * ```
 *
 * @author FallenStar
 * @version 2.0
 */
public class PageableBasicUi extends BasicUi implements PageNavigationAction.PageNavigable {

    /**
     * Items pro Seite (2 Content-Rows × 9 Slots).
     */
    public static final int ITEMS_PER_PAGE = 18;

    /**
     * Alle Items die angezeigt werden sollen.
     */
    private final List<UiElement> allItems;

    /**
     * Aktuelle Seite (0-basiert).
     */
    private int currentPage;

    /**
     * Konstruktor für PageableBasicUi.
     *
     * @param title Titel des UI
     */
    public PageableBasicUi(String title) {
        super(title);
        this.allItems = new ArrayList<>();
        this.currentPage = 0;
    }

    /**
     * Fügt ein Item hinzu (InfiniteAppend Pattern).
     *
     * Items werden automatisch auf Seiten verteilt.
     *
     * @param item UI-Element
     */
    public void append(UiElement item) {
        allItems.add(item);
    }

    /**
     * Fügt mehrere Items hinzu.
     *
     * @param items Liste von UI-Elementen
     */
    public void appendAll(List<UiElement> items) {
        allItems.addAll(items);
    }

    /**
     * Baut das UI für die aktuelle Seite auf.
     */
    @Override
    protected void build() {
        // Lösche Content-Rows
        getContentRow1().clear();
        getContentRow2().clear();

        // Lade Items für aktuelle Seite
        int startIndex = currentPage * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, allItems.size());

        List<UiElement> pageItems = allItems.subList(startIndex, endIndex);

        // Fülle Content-Rows
        int itemIndex = 0;
        for (UiElement item : pageItems) {
            if (itemIndex < 9) {
                getContentRow1().setElement(itemIndex, item);
            } else {
                getContentRow2().setElement(itemIndex - 9, item);
            }
            itemIndex++;
        }

        // Setup Navigation
        setupNavigation();

        // Baue BaseUi auf
        super.build();
    }

    /**
     * Richtet die Navigation-Buttons ein.
     */
    private void setupNavigation() {
        var controlRow = getControlRow();
        controlRow.clear();

        // Close Button (Mitte)
        controlRow.setCloseButton(CloseButton.create(this));

        // Navigation nur anzeigen wenn mehrere Seiten vorhanden
        if (getTotalPages() > 1) {
            // Previous Button (links)
            if (currentPage > 0) {
                controlRow.setNavigateLeft(NavigateLeftButton.previous(this));
            }

            // Next Button (rechts)
            if (currentPage < getTotalPages() - 1) {
                controlRow.setNavigateRight(NavigateRightButton.next(this));
            }
        }
    }

    /**
     * Gibt die Gesamtanzahl der Seiten zurück.
     *
     * @return Anzahl der Seiten (mindestens 1)
     */
    public int getTotalPages() {
        if (allItems.isEmpty()) {
            return 1;
        }
        return (int) Math.ceil((double) allItems.size() / ITEMS_PER_PAGE);
    }

    /**
     * Gibt die aktuelle Seite zurück (0-basiert).
     *
     * @return Aktuelle Seite
     */
    public int getCurrentPage() {
        return currentPage;
    }

    // ============================================================
    // PageNavigable Implementation (Type-Safe Navigation)
    // ============================================================

    @Override
    public void nextPage(Player player) {
        if (currentPage < getTotalPages() - 1) {
            currentPage++;
            refresh(player);
        }
    }

    @Override
    public void previousPage(Player player) {
        if (currentPage > 0) {
            currentPage--;
            refresh(player);
        }
    }

    @Override
    public void firstPage(Player player) {
        currentPage = 0;
        refresh(player);
    }

    @Override
    public void lastPage(Player player) {
        currentPage = getTotalPages() - 1;
        refresh(player);
    }

    @Override
    public void goToPage(Player player, int page) {
        if (page >= 0 && page < getTotalPages()) {
            currentPage = page;
            refresh(player);
        }
    }

    /**
     * Aktualisiert das UI für den Spieler.
     *
     * Baut das UI neu auf und zeigt die aktuelle Seite.
     *
     * @param player Spieler
     */
    @Override
    public void refresh(Player player) {
        // Baue UI neu auf
        build();

        // Aktualisiere das offene Inventory
        super.refresh(player);
    }
}
