package de.fallenstar.items.provider;

import de.fallenstar.core.exception.ProviderFunctionalityNotFoundException;
import de.fallenstar.core.provider.ItemProvider;
import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * MMOItems-Implementation des ItemProviders.
 *
 * Wrapper für MMOItems-API - bietet einheitliche Abstraktion
 * für Custom-Items im FallenStar-System.
 *
 * @author FallenStar
 * @version 1.0
 */
public class MMOItemsItemProvider implements ItemProvider {

    private final Logger logger;
    private final MMOItems mmoItemsPlugin;

    public MMOItemsItemProvider(Logger logger) {
        this.logger = logger;
        this.mmoItemsPlugin = (MMOItems) Bukkit.getPluginManager().getPlugin("MMOItems");
    }

    @Override
    public boolean isAvailable() {
        return mmoItemsPlugin != null && mmoItemsPlugin.isEnabled();
    }

    @Override
    public Optional<ItemStack> createItem(String itemId) throws ProviderFunctionalityNotFoundException {
        return createItem(itemId, 1);
    }

    @Override
    public Optional<ItemStack> createItem(String itemId, int amount) throws ProviderFunctionalityNotFoundException {
        // Versuche alle Types durchzugehen
        for (Type type : MMOItems.plugin.getTypes().getAll()) {
            Optional<ItemStack> result = createItem(type.getId(), itemId, amount);
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<ItemStack> createItem(String type, String itemId, int amount)
            throws ProviderFunctionalityNotFoundException {
        try {
            Type mmoType = MMOItems.plugin.getTypes().get(type);
            if (mmoType == null) {
                logger.warning("Unknown MMOItems type: " + type);
                return Optional.empty();
            }

            MMOItemTemplate template = MMOItems.plugin.getTemplates().getTemplate(mmoType, itemId);
            if (template == null) {
                return Optional.empty();
            }

            ItemStack item = template.newBuilder().build().newBuilder().build();
            item.setAmount(amount);
            return Optional.of(item);

        } catch (Exception e) {
            logger.severe("Failed to create MMOItem " + type + ":" + itemId + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> getItemId(ItemStack itemStack) throws ProviderFunctionalityNotFoundException {
        try {
            NBTItem nbtItem = NBTItem.get(itemStack);
            return Optional.ofNullable(nbtItem.getString("MMOITEMS_ITEM_ID"));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean isCustomItem(ItemStack itemStack) throws ProviderFunctionalityNotFoundException {
        return NBTItem.get(itemStack).hasTag("MMOITEMS_ITEM_ID");
    }

    @Override
    public List<String> getItemsByCategory(String category) throws ProviderFunctionalityNotFoundException {
        // TODO: MMOItems hat keine direkte Kategorie-API - verwende Tags
        return new ArrayList<>();
    }

    @Override
    public List<String> getCategories() throws ProviderFunctionalityNotFoundException {
        // TODO: Implementierung basierend auf MMOItems-Tags
        return new ArrayList<>();
    }

    @Override
    public Optional<String> getItemCategory(String itemId) throws ProviderFunctionalityNotFoundException {
        // TODO: Implementierung
        return Optional.empty();
    }

    @Override
    public Optional<Double> getSuggestedPrice(String itemId) throws ProviderFunctionalityNotFoundException {
        // TODO: Basierend auf Item-Stats berechnen
        return Optional.empty();
    }

    @Override
    public Optional<String> getItemType(String itemId) throws ProviderFunctionalityNotFoundException {
        for (Type type : MMOItems.plugin.getTypes().getAll()) {
            if (MMOItems.plugin.getTemplates().getTemplate(type, itemId) != null) {
                return Optional.of(type.getId());
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> getItemType(ItemStack itemStack) throws ProviderFunctionalityNotFoundException {
        try {
            NBTItem nbtItem = NBTItem.get(itemStack);
            return Optional.ofNullable(nbtItem.getString("MMOITEMS_ITEM_TYPE"));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<String> getAllTypes() throws ProviderFunctionalityNotFoundException {
        return MMOItems.plugin.getTypes().getAll().stream()
            .map(Type::getId)
            .collect(Collectors.toList());
    }

    @Override
    public List<String> getItemsByType(String type) throws ProviderFunctionalityNotFoundException {
        Type mmoType = MMOItems.plugin.getTypes().get(type);
        if (mmoType == null) {
            return Collections.emptyList();
        }

        return MMOItems.plugin.getTemplates().getTemplates(mmoType).stream()
            .map(MMOItemTemplate::getId)
            .collect(Collectors.toList());
    }

    @Override
    public List<String> getAllItemIds() throws ProviderFunctionalityNotFoundException {
        List<String> allIds = new ArrayList<>();
        for (Type type : MMOItems.plugin.getTypes().getAll()) {
            allIds.addAll(getItemsByType(type.getId()));
        }
        return allIds;
    }

    @Override
    public boolean itemExists(String itemId) throws ProviderFunctionalityNotFoundException {
        return createItem(itemId, 1).isPresent();
    }

    @Override
    public boolean itemExists(String type, String itemId) throws ProviderFunctionalityNotFoundException {
        return createItem(type, itemId, 1).isPresent();
    }
}
