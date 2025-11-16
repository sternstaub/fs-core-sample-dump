package de.fallenstar.items.manager;

import de.fallenstar.core.provider.ItemProvider;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.logging.Logger;

/**
 * Manager für Spezial-Items (Münzen, UI-Buttons, etc.).
 *
 * @author FallenStar
 * @version 1.0
 */
public class SpecialItemManager {

    private final ItemProvider itemProvider;
    private final Logger logger;
    private final Map<String, String> currencyItemIds;
    private final Map<String, String> uiButtonItemIds;

    public SpecialItemManager(ItemProvider itemProvider, Logger logger) {
        this.itemProvider = itemProvider;
        this.logger = logger;
        this.currencyItemIds = new HashMap<>();
        this.uiButtonItemIds = new HashMap<>();

        initializeCurrencyItems();
        initializeUIButtons();
    }

    private void initializeCurrencyItems() {
        currencyItemIds.put("bronze", "BRONZE_COIN");
        currencyItemIds.put("silver", "SILVER_COIN");
        currencyItemIds.put("gold", "GOLD_COIN");
        logger.info("✓ Currency items initialized: " + currencyItemIds.size());
    }

    private void initializeUIButtons() {
        uiButtonItemIds.put("next", "UI_BUTTON_NEXT");
        uiButtonItemIds.put("back", "UI_BUTTON_BACK");
        uiButtonItemIds.put("confirm", "UI_BUTTON_CONFIRM");
        uiButtonItemIds.put("cancel", "UI_BUTTON_CANCEL");
        uiButtonItemIds.put("info", "UI_BUTTON_INFO");
        logger.info("✓ UI buttons initialized: " + uiButtonItemIds.size());
    }

    public Optional<ItemStack> createCurrency(String currencyType, int amount) {
        String itemId = currencyItemIds.get(currencyType);
        if (itemId == null) {
            return Optional.empty();
        }
        try {
            return itemProvider.createItem("MATERIAL", itemId, amount);
        } catch (Exception e) {
            logger.warning("Failed to create currency item '" + currencyType + "': " + e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<ItemStack> createUIButton(String buttonType) {
        String itemId = uiButtonItemIds.get(buttonType);
        if (itemId == null) {
            return Optional.empty();
        }
        try {
            return itemProvider.createItem("MISC", itemId, 1);
        } catch (Exception e) {
            logger.warning("Failed to create UI button '" + buttonType + "': " + e.getMessage());
            return Optional.empty();
        }
    }

    public boolean isCurrencyItem(ItemStack itemStack) {
        try {
            if (!itemProvider.isCustomItem(itemStack)) {
                return false;
            }
            Optional<String> itemId = itemProvider.getItemId(itemStack);
            return itemId.isPresent() && currencyItemIds.containsValue(itemId.get());
        } catch (Exception e) {
            return false;
        }
    }

    public Set<String> getCurrencyTypes() {
        return new HashSet<>(currencyItemIds.keySet());
    }

    public Set<String> getUIButtonTypes() {
        return new HashSet<>(uiButtonItemIds.keySet());
    }
}
