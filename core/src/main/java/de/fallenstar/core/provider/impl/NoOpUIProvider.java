package de.fallenstar.core.provider.impl;

import de.fallenstar.core.exception.ProviderFunctionalityNotFoundException;
import de.fallenstar.core.provider.UIProvider;
import de.fallenstar.core.ui.UIMenu;
import org.bukkit.entity.Player;

/**
 * NoOp (No Operation) Implementation des UIProviders.
 *
 * Wird nur verwendet wenn UI-System komplett deaktiviert ist.
 * Normalerweise sollte NativeTextUIProvider als Fallback dienen.
 *
 * @author FallenStar
 * @version 1.0
 */
public class NoOpUIProvider implements UIProvider {

    private static final String PROVIDER_NAME = "UIProvider";
    private static final String REASON = "UI system disabled";

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public void showMenu(Player player, UIMenu menu)
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
                PROVIDER_NAME,
                "showMenu",
                REASON
        );
    }

    @Override
    public String getProviderType()
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
                PROVIDER_NAME,
                "getProviderType",
                REASON
        );
    }

    @Override
    public void closeAll(Player player)
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
                PROVIDER_NAME,
                "closeAll",
                REASON
        );
    }
}
