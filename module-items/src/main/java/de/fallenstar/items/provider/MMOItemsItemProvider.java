package de.fallenstar.items.provider;

import de.fallenstar.core.exception.ProviderFunctionalityNotFoundException;
import de.fallenstar.core.provider.ItemProvider;
import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
    private Object mmoItemsInstance; // MMOItems plugin instance (via reflection)
    private Object typeManager;      // TypeManager instance
    private Object templateManager;  // TemplateManager instance

    // Cache für Kategorien (wird bei Bedarf aktualisiert)
    private Set<String> cachedCategories;
    private Map<String, Set<String>> categoryItemsCache;
    private long lastCacheUpdate;
    private static final long CACHE_TTL = 300000; // 5 Minuten

    public MMOItemsItemProvider(Logger logger) {
        this.logger = logger;
        this.cachedCategories = new HashSet<>();
        this.categoryItemsCache = new HashMap<>();
        this.lastCacheUpdate = 0;
        initializeReflection();
    }

    /**
     * Initialisiert Reflection-Zugriff auf MMOItems API.
     * Vermeidet direkte Klassenreferenzen auf MMOItems (wegen MMOPlugin-Dependency).
     */
    private void initializeReflection() {
        try {
            Plugin plugin = Bukkit.getPluginManager().getPlugin("MMOItems");
            if (plugin == null) {
                logger.warning("MMOItems plugin not found!");
                return;
            }

            // Hole statisches 'plugin' Feld via Reflection
            Class<?> mmoItemsClass = plugin.getClass();
            Field pluginField = mmoItemsClass.getField("plugin");
            mmoItemsInstance = pluginField.get(null);

            // Hole TypeManager
            Method getTypesMethod = mmoItemsInstance.getClass().getMethod("getTypes");
            typeManager = getTypesMethod.invoke(mmoItemsInstance);

            // Hole TemplateManager
            Method getTemplatesMethod = mmoItemsInstance.getClass().getMethod("getTemplates");
            templateManager = getTemplatesMethod.invoke(mmoItemsInstance);

            logger.info("✓ MMOItems API initialized via reflection");
        } catch (Exception e) {
            logger.severe("Failed to initialize MMOItems via reflection: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Holt alle Types via Reflection.
     */
    @SuppressWarnings("unchecked")
    private Collection<Type> getAllMMOTypes() {
        try {
            Method getAllMethod = typeManager.getClass().getMethod("getAll");
            return (Collection<Type>) getAllMethod.invoke(typeManager);
        } catch (Exception e) {
            logger.warning("Failed to get all types: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Holt einen Type nach ID via Reflection.
     */
    private Type getType(String typeId) {
        try {
            Method getMethod = typeManager.getClass().getMethod("get", String.class);
            return (Type) getMethod.invoke(typeManager, typeId);
        } catch (Exception e) {
            logger.warning("Failed to get type '" + typeId + "': " + e.getMessage());
            return null;
        }
    }

    /**
     * Holt ein Template via Reflection.
     */
    private MMOItemTemplate getTemplate(Type type, String itemId) {
        try {
            Method getTemplateMethod = templateManager.getClass().getMethod("getTemplate", Type.class, String.class);
            return (MMOItemTemplate) getTemplateMethod.invoke(templateManager, type, itemId);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Holt alle Templates eines Types via Reflection.
     */
    @SuppressWarnings("unchecked")
    private Collection<MMOItemTemplate> getTemplates(Type type) {
        try {
            Method getTemplatesMethod = templateManager.getClass().getMethod("getTemplates", Type.class);
            return (Collection<MMOItemTemplate>) getTemplatesMethod.invoke(templateManager, type);
        } catch (Exception e) {
            logger.warning("Failed to get templates for type: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public boolean isAvailable() {
        // Prüfe ob MMOItems-Plugin geladen ist (vermeidet MMOPlugin-Dependency)
        return Bukkit.getPluginManager().getPlugin("MMOItems") != null;
    }

    @Override
    public Optional<ItemStack> createItem(String itemId) throws ProviderFunctionalityNotFoundException {
        return createItem(itemId, 1);
    }

    @Override
    public Optional<ItemStack> createItem(String itemId, int amount) throws ProviderFunctionalityNotFoundException {
        // Versuche alle Types durchzugehen
        for (Type type : getAllMMOTypes()) {
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
            Type mmoType = getType(type);
            if (mmoType == null) {
                logger.warning("Unknown MMOItems type: " + type);
                return Optional.empty();
            }

            MMOItemTemplate template = getTemplate(mmoType, itemId);
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
        // Suche Item und verwende Type als Kategorie (getTags() entfernt in 6.10+)
        for (Type type : getAllMMOTypes()) {
            MMOItemTemplate template = getTemplate(type, itemId);
            if (template != null) {
                // Verwende Type-Namen als Kategorie
                return Optional.of(type.getName().toUpperCase());
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Double> getSuggestedPrice(String itemId) throws ProviderFunctionalityNotFoundException {
        // Suche Item und berechne Preis basierend auf Stats
        for (Type type : getAllMMOTypes()) {
            MMOItemTemplate template = getTemplate(type, itemId);
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

        // Durchlaufe alle Items und sammle Kategorien (Type-basiert, getTags() entfernt in 6.10+)
        for (Type type : getAllMMOTypes()) {
            String category = type.getName().toUpperCase();
            cachedCategories.add(category);

            for (MMOItemTemplate template : getTemplates(type)) {
                try {
                    categoryItemsCache
                        .computeIfAbsent(category, k -> new HashSet<>())
                        .add(template.getId());
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
     * Berechnet einen Preisvorschlag basierend auf Item-Type.
     *
     * Vereinfachte Berechnung für MMOItems 6.10+ API-Kompatibilität.
     * Stats-Zugriff entfernt da ItemStat API sich geändert hat.
     */
    private double calculateSuggestedPrice(MMOItemTemplate template, Type type) {
        double basePrice = 10.0; // Basis-Preis

        try {
            // Type-Multiplikator
            double typeMultiplier = getTypeMultiplier(type.getId());
            basePrice *= typeMultiplier;

            // Spezial-Items basierend auf Item-ID
            String itemId = template.getId().toUpperCase();
            if (itemId.contains("COIN") || itemId.contains("CURRENCY")) {
                // Münzen sollten nicht handelbar sein
                return 0.0;
            }
            if (itemId.contains("UI_") || itemId.contains("BUTTON") || itemId.contains("SYSTEM")) {
                // System-Items sollten nicht handelbar sein
                return 0.0;
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
        for (Type type : getAllMMOTypes()) {
            if (getTemplate(type, itemId) != null) {
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
        return getAllMMOTypes().stream()
            .map(Type::getId)
            .collect(Collectors.toList());
    }

    @Override
    public List<String> getItemsByType(String type) throws ProviderFunctionalityNotFoundException {
        Type mmoType = getType(type);
        if (mmoType == null) {
            return Collections.emptyList();
        }

        return getTemplates(mmoType).stream()
            .map(MMOItemTemplate::getId)
            .collect(Collectors.toList());
    }

    @Override
    public List<String> getAllItemIds() throws ProviderFunctionalityNotFoundException {
        List<String> allIds = new ArrayList<>();
        for (Type type : getAllMMOTypes()) {
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
