package de.fallenstar.items.provider;

import de.fallenstar.core.exception.ProviderFunctionalityNotFoundException;
import de.fallenstar.core.provider.ItemProvider;
import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.stat.data.DoubleData;
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
 * Features:
 * - Tag-basierte Kategorisierung
 * - Stats-basierte Preisberechnung
 * - Performance-optimiertes Caching
 *
 * @author FallenStar
 * @version 1.0
 */
public class MMOItemsItemProvider implements ItemProvider {

    private final Logger logger;
    private final MMOItems mmoItemsPlugin;

    // Cache für Kategorien (wird bei Bedarf aktualisiert)
    private Set<String> cachedCategories;
    private Map<String, Set<String>> categoryItemsCache;
    private long lastCacheUpdate;
    private static final long CACHE_TTL = 300000; // 5 Minuten

    public MMOItemsItemProvider(Logger logger) {
        this.logger = logger;
        this.mmoItemsPlugin = (MMOItems) Bukkit.getPluginManager().getPlugin("MMOItems");
        this.cachedCategories = new HashSet<>();
        this.categoryItemsCache = new HashMap<>();
        this.lastCacheUpdate = 0;
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
        // Aktualisiere Cache falls nötig
        updateCategoryCache();

        Set<String> items = categoryItemsCache.get(category.toUpperCase());
        return items != null ? new ArrayList<>(items) : new ArrayList<>();
    }

    @Override
    public List<String> getCategories() throws ProviderFunctionalityNotFoundException {
        // Aktualisiere Cache falls nötig
        updateCategoryCache();

        return new ArrayList<>(cachedCategories);
    }

    @Override
    public Optional<String> getItemCategory(String itemId) throws ProviderFunctionalityNotFoundException {
        // Suche Item und hole erste Tag als Kategorie
        for (Type type : MMOItems.plugin.getTypes().getAll()) {
            MMOItemTemplate template = MMOItems.plugin.getTemplates().getTemplate(type, itemId);
            if (template != null) {
                try {
                    // Hole Tags vom Template
                    Set<String> tags = template.getTags();
                    if (tags != null && !tags.isEmpty()) {
                        // Priorisiere spezifische Kategorien
                        for (String tag : tags) {
                            if (tag.startsWith("CURRENCY_") || tag.startsWith("UI_") ||
                                tag.equals("WEAPON") || tag.equals("ARMOR") || tag.equals("CONSUMABLE")) {
                                return Optional.of(tag);
                            }
                        }
                        // Fallback: Erste Tag zurückgeben
                        return Optional.of(tags.iterator().next());
                    }
                } catch (Exception e) {
                    logger.warning("Failed to get tags for item " + itemId + ": " + e.getMessage());
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Double> getSuggestedPrice(String itemId) throws ProviderFunctionalityNotFoundException {
        // Suche Item und berechne Preis basierend auf Stats
        for (Type type : MMOItems.plugin.getTypes().getAll()) {
            MMOItemTemplate template = MMOItems.plugin.getTemplates().getTemplate(type, itemId);
            if (template != null) {
                return Optional.of(calculateSuggestedPrice(template, type));
            }
        }
        return Optional.empty();
    }

    /**
     * Aktualisiert den Kategorie-Cache.
     */
    private void updateCategoryCache() {
        long now = System.currentTimeMillis();
        if (now - lastCacheUpdate < CACHE_TTL) {
            return; // Cache noch gültig
        }

        logger.info("Updating category cache...");
        cachedCategories.clear();
        categoryItemsCache.clear();

        // Durchlaufe alle Items und sammle Kategorien (Tags)
        for (Type type : MMOItems.plugin.getTypes().getAll()) {
            for (MMOItemTemplate template : MMOItems.plugin.getTemplates().getTemplates(type)) {
                try {
                    Set<String> tags = template.getTags();
                    if (tags != null) {
                        for (String tag : tags) {
                            String category = tag.toUpperCase();
                            cachedCategories.add(category);

                            categoryItemsCache
                                .computeIfAbsent(category, k -> new HashSet<>())
                                .add(template.getId());
                        }
                    }
                } catch (Exception e) {
                    logger.warning("Failed to process tags for " + template.getId());
                }
            }
        }

        lastCacheUpdate = now;
        logger.info("Category cache updated: " + cachedCategories.size() + " categories, " +
                   categoryItemsCache.values().stream().mapToInt(Set::size).sum() + " items");
    }

    /**
     * Berechnet einen Preisvorschlag basierend auf Item-Stats.
     *
     * Berücksichtigt:
     * - Item-Tier (Seltenheit)
     * - Attribute (Attack Damage, Defense, etc.)
     * - Type-spezifische Multiplikatoren
     */
    private double calculateSuggestedPrice(MMOItemTemplate template, Type type) {
        double basePrice = 10.0; // Basis-Preis

        try {
            // Erstelle temporäres MMOItem für Stats-Zugriff
            MMOItem mmoItem = template.newBuilder().build();

            // Tier-Multiplikator (falls vorhanden)
            String tier = template.getConfigFile().getConfig().getString("tier", "COMMON");
            double tierMultiplier = getTierMultiplier(tier);
            basePrice *= tierMultiplier;

            // Type-Multiplikator
            double typeMultiplier = getTypeMultiplier(type.getId());
            basePrice *= typeMultiplier;

            // Stats-basierte Berechnung
            double statsValue = 0.0;

            // Attack Damage
            if (mmoItem.hasData(net.Indyuce.mmoitems.api.item.build.ItemStackBuilder.ItemStat.ATTACK_DAMAGE)) {
                try {
                    DoubleData attackData = (DoubleData) mmoItem.getData(net.Indyuce.mmoitems.api.item.build.ItemStackBuilder.ItemStat.ATTACK_DAMAGE);
                    statsValue += attackData.getValue() * 5.0;
                } catch (Exception ignored) {}
            }

            // Defense
            if (mmoItem.hasData(net.Indyuce.mmoitems.api.item.build.ItemStackBuilder.ItemStat.DEFENSE)) {
                try {
                    DoubleData defenseData = (DoubleData) mmoItem.getData(net.Indyuce.mmoitems.api.item.build.ItemStackBuilder.ItemStat.DEFENSE);
                    statsValue += defenseData.getValue() * 3.0;
                } catch (Exception ignored) {}
            }

            basePrice += statsValue;

            // Spezial-Items (Münzen, UI-Buttons) haben feste Preise
            Set<String> tags = template.getTags();
            if (tags != null) {
                if (tags.contains("CURRENCY_COIN")) {
                    // Münzen sollten nicht handelbar sein
                    return 0.0;
                }
                if (tags.contains("UI_BUTTON") || tags.contains("SYSTEM_ITEM")) {
                    // System-Items sollten nicht handelbar sein
                    return 0.0;
                }
            }

        } catch (Exception e) {
            logger.warning("Failed to calculate price for " + template.getId() + ": " + e.getMessage());
        }

        // Runde auf volle Einheiten
        return Math.max(1.0, Math.round(basePrice));
    }

    /**
     * Gibt Multiplikator für Item-Tier zurück.
     */
    private double getTierMultiplier(String tier) {
        return switch (tier.toUpperCase()) {
            case "COMMON" -> 1.0;
            case "UNCOMMON" -> 2.0;
            case "RARE" -> 4.0;
            case "EPIC" -> 8.0;
            case "LEGENDARY" -> 16.0;
            case "MYTHIC" -> 32.0;
            default -> 1.0;
        };
    }

    /**
     * Gibt Multiplikator für Item-Type zurück.
     */
    private double getTypeMultiplier(String typeId) {
        return switch (typeId.toUpperCase()) {
            case "SWORD", "BOW", "STAFF" -> 2.0;
            case "ARMOR", "HELMET", "CHESTPLATE", "LEGGINGS", "BOOTS" -> 1.5;
            case "ACCESSORY", "TRINKET" -> 3.0;
            case "CONSUMABLE", "MATERIAL" -> 0.5;
            case "MISC" -> 0.25;
            default -> 1.0;
        };
    }

    /**
     * Invalidiert den Kategorie-Cache (für Reload).
     */
    public void invalidateCache() {
        lastCacheUpdate = 0;
        logger.info("Category cache invalidated");
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
