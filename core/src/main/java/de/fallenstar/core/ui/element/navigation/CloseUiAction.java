package de.fallenstar.core.ui.element.navigation;

import de.fallenstar.core.ui.UiParent;
import de.fallenstar.core.ui.element.UiAction;
import org.bukkit.entity.Player;

import java.util.Objects;

/**
 * Action zum Schließen eines UI.
 *
 * Verwendung:
 * ```java
 * var closeAction = new CloseUiAction(myUi);
 * var closeButton = new CloseButton(closeAction);
 * ```
 *
 * @author FallenStar
 * @version 2.0
 */
public final class CloseUiAction implements UiAction {

    private final UiParent uiToClose;

    /**
     * Konstruktor für Close Action.
     *
     * @param uiToClose Das UI das geschlossen werden soll
     */
    public CloseUiAction(UiParent uiToClose) {
        this.uiToClose = Objects.requireNonNull(uiToClose, "UI darf nicht null sein");
    }

    @Override
    public void execute(Player player) {
        uiToClose.close(player);
    }

    @Override
    public String getActionName() {
        return "CloseUi[" + uiToClose.getClass().getSimpleName() + "]";
    }
}
