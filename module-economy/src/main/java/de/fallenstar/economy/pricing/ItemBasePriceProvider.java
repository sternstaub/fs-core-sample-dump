package de.fallenstar.economy.pricing;

import de.fallenstar.economy.model.ItemBasePrice;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Logger;

/**
 * Verwaltet Basispreise für Items (Vanilla und Custom).
 *
 * Basispreise dienen als Berechnungsgrundlage für Handelspreise:
 * - Handelsgilden-Plots nutzen diese Preise
 * - Händler-NPCs leiten ihre Preise davon ab
 * - Preise sind in Basiswährung (Sterne) angegeben
 *
 * Features:
 * - Vanilla-Item-Preise (Material → Preis)
 * - Custom-Item-Preise (Type:ID → Preis)
 * - Config-basierte Preisdefinition
 * - Fallback-Preise für fehlende Einträge
 *
 * Verwendung:
 * <pre>
 * // Preis für Vanilla-Item abrufen
 * Optional<BigDecimal> diamondPrice = priceProvider.getVanillaPrice(Material.DIAMOND);
 *
 * // Preis für Custom-Item abrufen
 * Optional<BigDecimal> swordPrice = priceProvider.getCustomPrice("SWORD", "EXCALIBUR");
 * </pre>
 *
 * @author FallenStar
 * @version 1.0
 */
public class ItemBasePriceProvider {

    private final Logger logger;
    private final Map<Material, ItemBasePrice.VanillaItemPrice> vanillaPrices;
    private final Map<String, ItemBasePrice.CustomItemPrice> customPrices;
    private BigDecimal defaultVanillaPrice;
    private BigDecimal defaultCustomPrice;

    /**
     * Konstruktor für ItemBasePriceProvider.
     *
     * @param logger Logger
     */
    public ItemBasePriceProvider(Logger logger) {
        this.logger = logger;
        this.vanillaPrices = new HashMap<>();
        this.customPrices = new HashMap<>();
        this.defaultVanillaPrice = BigDecimal.ONE;
        this.defaultCustomPrice = BigDecimal.TEN;

        logger.info("ItemBasePriceProvider initialisiert");
    }

    /**
     * Lädt Preise aus einer Config.
     *
     * Config-Struktur:
     * <pre>
     * item-base-prices:
     *   defaults:
     *     vanilla: 1.0
     *     custom: 10.0
     *   vanilla:
     *     DIAMOND: 100.0
     *     IRON_INGOT: 5.0
     *     GOLD_INGOT: 10.0
     *   custom:
     *     "SWORD:EXCALIBUR": 1000.0
     *     "TOOL:LEGENDARY_PICKAXE": 500.0
     * </pre>
     *
     * @param config FileConfiguration
     */
    public void loadFromConfig(FileConfiguration config) {
        ConfigurationSection pricesSection = config.getConfigurationSection("item-base-prices");
        if (pricesSection == null) {
            logger.warning("Keine item-base-prices in config.yml gefunden - verwende Defaults");
            initializeDefaultPrices();
            return;
        }

        // Lade Default-Preise
        ConfigurationSection defaultsSection = pricesSection.getConfigurationSection("defaults");
        if (defaultsSection != null) {
            defaultVanillaPrice = BigDecimal.valueOf(defaultsSection.getDouble("vanilla", 1.0));
            defaultCustomPrice = BigDecimal.valueOf(defaultsSection.getDouble("custom", 10.0));
            logger.info("Default-Preise gesetzt: Vanilla=" + defaultVanillaPrice + ", Custom=" + defaultCustomPrice);
        }

        // Lade Vanilla-Preise
        ConfigurationSection vanillaSection = pricesSection.getConfigurationSection("vanilla");
        if (vanillaSection != null) {
            for (String materialName : vanillaSection.getKeys(false)) {
                try {
                    Material material = Material.valueOf(materialName.toUpperCase());
                    BigDecimal price = BigDecimal.valueOf(vanillaSection.getDouble(materialName));
                    registerVanillaPrice(material, price);
                } catch (IllegalArgumentException e) {
                    logger.warning("Ungültiges Material in config: " + materialName);
                }
            }
            logger.info("Vanilla-Preise geladen: " + vanillaPrices.size() + " Items");
        }

        // Lade Custom-Preise
        ConfigurationSection customSection = pricesSection.getConfigurationSection("custom");
        if (customSection != null) {
            for (String itemIdentifier : customSection.getKeys(false)) {
                String[] parts = itemIdentifier.split(":");
                if (parts.length == 2) {
                    String type = parts[0];
                    String id = parts[1];
                    BigDecimal price = BigDecimal.valueOf(customSection.getDouble(itemIdentifier));
                    registerCustomPrice(type, id, price);
                } else {
                    logger.warning("Ungültiger Custom-Item-Identifier in config: " + itemIdentifier);
                }
            }
            logger.info("Custom-Preise geladen: " + customPrices.size() + " Items");
        }
    }

    /**
     * Initialisiert Default-Preise für häufige Items.
     *
     * Diese Methode wird aufgerufen wenn keine Config existiert.
     */
    private void initializeDefaultPrices() {
        // Erze & Barren
        registerVanillaPrice(Material.DIAMOND, BigDecimal.valueOf(100));
        registerVanillaPrice(Material.EMERALD, BigDecimal.valueOf(50));
        registerVanillaPrice(Material.GOLD_INGOT, BigDecimal.valueOf(10));
        registerVanillaPrice(Material.IRON_INGOT, BigDecimal.valueOf(5));
        registerVanillaPrice(Material.COPPER_INGOT, BigDecimal.valueOf(1));

        // Rohmaterialien
        registerVanillaPrice(Material.RAW_GOLD, BigDecimal.valueOf(8));
        registerVanillaPrice(Material.RAW_IRON, BigDecimal.valueOf(4));
        registerVanillaPrice(Material.RAW_COPPER, BigDecimal.valueOf(0.8));

        // Holz
        registerVanillaPrice(Material.OAK_LOG, BigDecimal.valueOf(0.5));
        registerVanillaPrice(Material.SPRUCE_LOG, BigDecimal.valueOf(0.5));
        registerVanillaPrice(Material.BIRCH_LOG, BigDecimal.valueOf(0.5));

        // Stein
        registerVanillaPrice(Material.STONE, BigDecimal.valueOf(0.1));
        registerVanillaPrice(Material.COBBLESTONE, BigDecimal.valueOf(0.05));

        logger.info("Default-Preise für " + vanillaPrices.size() + " Vanilla-Items initialisiert");
    }

    /**
     * Registriert einen Basispreis für ein Vanilla-Item.
     *
     * @param material Material
     * @param price Preis in Sternen
     */
    public void registerVanillaPrice(Material material, BigDecimal price) {
        ItemBasePrice.VanillaItemPrice itemPrice = new ItemBasePrice.VanillaItemPrice(material, price);
        vanillaPrices.put(material, itemPrice);
        logger.fine("Vanilla-Preis registriert: " + material + " = " + price);
    }

    /**
     * Registriert einen Basispreis für ein Custom-Item.
     *
     * @param itemType Item-Type (z.B. "SWORD")
     * @param itemId Item-ID (z.B. "EXCALIBUR")
     * @param price Preis in Sternen
     */
    public void registerCustomPrice(String itemType, String itemId, BigDecimal price) {
        String key = (itemType + ":" + itemId).toUpperCase();
        ItemBasePrice.CustomItemPrice itemPrice = new ItemBasePrice.CustomItemPrice(itemType, itemId, price);
        customPrices.put(key, itemPrice);
        logger.fine("Custom-Preis registriert: " + key + " = " + price);
    }

    /**
     * Gibt den Basispreis für ein Vanilla-Item zurück.
     *
     * @param material Material
     * @return Optional mit Preis, oder empty wenn nicht definiert
     */
    public Optional<BigDecimal> getVanillaPrice(Material material) {
        ItemBasePrice.VanillaItemPrice price = vanillaPrices.get(material);
        return Optional.ofNullable(price).map(ItemBasePrice.VanillaItemPrice::getPrice);
    }

    /**
     * Gibt den Basispreis für ein Vanilla-Item zurück, oder Default-Preis.
     *
     * @param material Material
     * @return Preis (oder Default-Preis wenn nicht definiert)
     */
    public BigDecimal getVanillaPriceOrDefault(Material material) {
        return getVanillaPrice(material).orElse(defaultVanillaPrice);
    }

    /**
     * Gibt den Basispreis für ein Custom-Item zurück.
     *
     * @param itemType Item-Type
     * @param itemId Item-ID
     * @return Optional mit Preis, oder empty wenn nicht definiert
     */
    public Optional<BigDecimal> getCustomPrice(String itemType, String itemId) {
        String key = (itemType + ":" + itemId).toUpperCase();
        ItemBasePrice.CustomItemPrice price = customPrices.get(key);
        return Optional.ofNullable(price).map(ItemBasePrice.CustomItemPrice::getPrice);
    }

    /**
     * Gibt den Basispreis für ein Custom-Item zurück, oder Default-Preis.
     *
     * @param itemType Item-Type
     * @param itemId Item-ID
     * @return Preis (oder Default-Preis wenn nicht definiert)
     */
    public BigDecimal getCustomPriceOrDefault(String itemType, String itemId) {
        return getCustomPrice(itemType, itemId).orElse(defaultCustomPrice);
    }

    /**
     * Gibt den Basispreis für einen ItemStack zurück.
     *
     * Diese Methode prüft zunächst ob es ein Custom-Item ist (via PDC),
     * und falls nicht, nutzt sie das Material.
     *
     * @param stack ItemStack
     * @return Optional mit Preis
     */
    public Optional<BigDecimal> getPrice(ItemStack stack) {
        if (stack == null || stack.getType().isAir()) {
            return Optional.empty();
        }

        // TODO: Check for Custom-Item (PDC-based)
        // Für jetzt nur Vanilla-Preise

        return getVanillaPrice(stack.getType());
    }

    /**
     * Gibt den Basispreis für einen ItemStack zurück, oder Default-Preis.
     *
     * @param stack ItemStack
     * @return Preis (oder Default-Preis)
     */
    public BigDecimal getPriceOrDefault(ItemStack stack) {
        return getPrice(stack).orElse(defaultVanillaPrice);
    }

    /**
     * Setzt den Default-Preis für Vanilla-Items.
     *
     * @param price Neuer Default-Preis
     */
    public void setDefaultVanillaPrice(BigDecimal price) {
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            logger.warning("Versuch, negativen Default-Preis zu setzen!");
            return;
        }
        this.defaultVanillaPrice = price;
        logger.info("Default Vanilla-Preis geändert: " + price);
    }

    /**
     * Setzt den Default-Preis für Custom-Items.
     *
     * @param price Neuer Default-Preis
     */
    public void setDefaultCustomPrice(BigDecimal price) {
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            logger.warning("Versuch, negativen Default-Preis zu setzen!");
            return;
        }
        this.defaultCustomPrice = price;
        logger.info("Default Custom-Preis geändert: " + price);
    }

    /**
     * Gibt die Anzahl registrierter Vanilla-Preise zurück.
     *
     * @return Anzahl
     */
    public int getVanillaPriceCount() {
        return vanillaPrices.size();
    }

    /**
     * Gibt die Anzahl registrierter Custom-Preise zurück.
     *
     * @return Anzahl
     */
    public int getCustomPriceCount() {
        return customPrices.size();
    }

    /**
     * Gibt alle registrierten Vanilla-Preise zurück.
     *
     * @return Collection von VanillaItemPrice
     */
    public Collection<ItemBasePrice.VanillaItemPrice> getAllVanillaPrices() {
        return Collections.unmodifiableCollection(vanillaPrices.values());
    }

    /**
     * Gibt alle registrierten Custom-Preise zurück.
     *
     * @return Collection von CustomItemPrice
     */
    public Collection<ItemBasePrice.CustomItemPrice> getAllCustomPrices() {
        return Collections.unmodifiableCollection(customPrices.values());
    }
}
