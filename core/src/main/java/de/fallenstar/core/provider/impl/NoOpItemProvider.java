package de.fallenstar.core.provider.impl;

import de.fallenstar.core.exception.ProviderFunctionalityNotFoundException;
import de.fallenstar.core.provider.ItemProvider;
import org.bukkit.inventory.ItemStack;
import java.util.List;
import java.util.Optional;

/**
 * NoOp (No Operation) Implementation des ItemProviders.
 *
 * Wird verwendet wenn kein Custom-Item-Plugin (MMOItems, ItemsAdder) verfügbar ist.
 * Alle Methoden werfen ProviderFunctionalityNotFoundException.
 *
 * Pattern: Null Object Pattern kombiniert mit Exception-Handling
 * Vorteil: Kein Null-Checking nötig, explizites Fehlerhandling
 *
 * @author FallenStar
 * @version 1.0
 */
public class NoOpItemProvider implements ItemProvider {

    private static final String PROVIDER_NAME = "ItemProvider";
    private static final String REASON = "No custom item plugin (MMOItems, ItemsAdder) available";

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public Optional<ItemStack> createItem(String itemId)
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            PROVIDER_NAME,
            "createItem",
            REASON
        );
    }

    @Override
    public Optional<String> getItemId(ItemStack itemStack)
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            PROVIDER_NAME,
            "getItemId",
            REASON
        );
    }

    @Override
    public boolean isCustomItem(ItemStack itemStack)
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            PROVIDER_NAME,
            "isCustomItem",
            REASON
        );
    }

    @Override
    public List<String> getItemsByCategory(String category)
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            PROVIDER_NAME,
            "getItemsByCategory",
            REASON
        );
    }

    @Override
    public List<String> getCategories()
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            PROVIDER_NAME,
            "getCategories",
            REASON
        );
    }

    @Override
    public Optional<String> getItemCategory(String itemId)
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            PROVIDER_NAME,
            "getItemCategory",
            REASON
        );
    }

    @Override
    public Optional<Double> getSuggestedPrice(String itemId)
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            PROVIDER_NAME,
            "getSuggestedPrice",
            REASON
        );
    }
}
