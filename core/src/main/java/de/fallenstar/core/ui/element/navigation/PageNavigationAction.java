package de.fallenstar.core.ui.element.navigation;

import de.fallenstar.core.ui.element.UiAction;
import org.bukkit.entity.Player;

import java.util.Objects;

/**
 * Action für Seiten-Navigation in PageableUIs.
 *
 * Ermöglicht Navigation zwischen Seiten:
 * - NEXT - Nächste Seite
 * - PREVIOUS - Vorherige Seite
 * - FIRST - Erste Seite
 * - LAST - Letzte Seite
 * - GOTO - Zu bestimmter Seite
 *
 * Verwendung:
 * ```java
 * var nextAction = new PageNavigationAction(
 *     PageNavigationAction.Direction.NEXT,
 *     pageableUi
 * );
 *
 * var nextButton = new NavigateRightButton(nextAction);
 * ```
 *
 * @author FallenStar
 * @version 2.0
 */
public final class PageNavigationAction implements UiAction {

    private final Direction direction;
    private final PageNavigable navigable;
    private final int targetPage; // Nur für GOTO verwendet

    /**
     * Navigations-Richtung.
     */
    public enum Direction {
        NEXT,       // Nächste Seite
        PREVIOUS,   // Vorherige Seite
        FIRST,      // Erste Seite
        LAST,       // Letzte Seite
        GOTO        // Zu spezifischer Seite
    }

    /**
     * Interface für navigierbare UIs.
     *
     * Muss von PageableUIs implementiert werden.
     */
    public interface PageNavigable {
        void nextPage(Player player);
        void previousPage(Player player);
        void firstPage(Player player);
        void lastPage(Player player);
        void goToPage(Player player, int page);
    }

    /**
     * Konstruktor für NEXT, PREVIOUS, FIRST, LAST.
     *
     * @param direction Navigations-Richtung
     * @param navigable Das navigierbare UI
     */
    public PageNavigationAction(Direction direction, PageNavigable navigable) {
        this(direction, navigable, 0);
        if (direction == Direction.GOTO) {
            throw new IllegalArgumentException("GOTO benötigt targetPage Parameter");
        }
    }

    /**
     * Konstruktor für GOTO (zu spezifischer Seite).
     *
     * @param navigable Das navigierbare UI
     * @param targetPage Zielseite (0-basiert)
     */
    public PageNavigationAction(PageNavigable navigable, int targetPage) {
        this(Direction.GOTO, navigable, targetPage);
    }

    /**
     * Privater Konstruktor für alle Richtungen.
     */
    private PageNavigationAction(Direction direction, PageNavigable navigable, int targetPage) {
        this.direction = Objects.requireNonNull(direction);
        this.navigable = Objects.requireNonNull(navigable);
        this.targetPage = targetPage;
    }

    @Override
    public void execute(Player player) {
        switch (direction) {
            case NEXT -> navigable.nextPage(player);
            case PREVIOUS -> navigable.previousPage(player);
            case FIRST -> navigable.firstPage(player);
            case LAST -> navigable.lastPage(player);
            case GOTO -> navigable.goToPage(player, targetPage);
        }
    }

    @Override
    public String getActionName() {
        return "PageNavigation[" + direction + "]";
    }

    public Direction getDirection() {
        return direction;
    }
}
