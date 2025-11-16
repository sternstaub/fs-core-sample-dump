package de.fallenstar.items.manager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.logging.Logger;

/**
 * Manager für Spezial-Items (Währungs-Münzen, etc.).
 *
 * Erstellt VANILLA ItemStacks mit Custom Model Data und PDC für Identifikation.
 * KEINE MMOItems-Dependency!
 *
 * Features:
 * - Bronze/Silver/Gold Coins (Vanilla Items mit Custom Model Data)
 * - PDC-basierte Item-Identifikation (fallenstar:item_id)
 * - Konfigurierbare Währungswerte
 *
 * @author FallenStar
 * @version 2.0
 */
public class SpecialItemManager {

    private final Plugin plugin;
    private final Logger logger;
    private final NamespacedKey itemIdKey;
    private final Map<String, CurrencyDefinition> currencyDefinitions;

    /**
     * Konstruktor für SpecialItemManager.
     *
     * @param plugin Plugin-Instanz (für NamespacedKey)
     * @param logger Logger
     */
    public SpecialItemManager(Plugin plugin, Logger logger) {
        this.plugin = plugin;
        this.logger = logger;
        this.itemIdKey = new NamespacedKey(plugin, "item_id");
        this.currencyDefinitions = new HashMap<>();

        initializeCurrencyDefinitions();
    }

    /**
     * Initialisiert Währungs-Definitionen.
     */
    private void initializeCurrencyDefinitions() {
        // Bronze Coin (1 Wert)
        currencyDefinitions.put("bronze", new CurrencyDefinition(
                "bronze_coin",
                Material.GOLD_NUGGET,
                1, // Custom Model Data
                Component.text("Bronze-Münze", NamedTextColor.GOLD, TextDecoration.BOLD),
                List.of(
                        Component.text("Wert: 1", NamedTextColor.GRAY),
                        Component.empty(),
                        Component.text("Grundwährung des Reiches", NamedTextColor.DARK_GRAY)
                ),
                1 // Währungswert
        ));

        // Silver Coin (10 Wert)
        currencyDefinitions.put("silver", new CurrencyDefinition(
                "silver_coin",
                Material.GOLD_NUGGET,
                2, // Custom Model Data
                Component.text("Silber-Münze", NamedTextColor.WHITE, TextDecoration.BOLD),
                List.of(
                        Component.text("Wert: 10", NamedTextColor.GRAY),
                        Component.empty(),
                        Component.text("Handelswährung", NamedTextColor.DARK_GRAY)
                ),
                10 // Währungswert
        ));

        // Gold Coin (100 Wert)
        currencyDefinitions.put("gold", new CurrencyDefinition(
                "gold_coin",
                Material.GOLD_INGOT,
                1, // Custom Model Data
                Component.text("Gold-Münze", NamedTextColor.YELLOW, TextDecoration.BOLD),
                List.of(
                        Component.text("Wert: 100", NamedTextColor.GRAY),
                        Component.empty(),
                        Component.text("Edelwährung für große Geschäfte", NamedTextColor.DARK_GRAY)
                ),
                100 // Währungswert
        ));

        logger.info("✓ Currency definitions initialized: " + currencyDefinitions.size());
    }

    /**
     * Erstellt eine Währungs-Münze.
     *
     * @param currencyType Währungstyp (bronze, silver, gold)
     * @param amount Anzahl
     * @return ItemStack oder Optional.empty()
     */
    public Optional<ItemStack> createCurrency(String currencyType, int amount) {
        CurrencyDefinition def = currencyDefinitions.get(currencyType.toLowerCase());
        if (def == null) {
            logger.warning("Unknown currency type: " + currencyType);
            return Optional.empty();
        }

        if (amount <= 0) {
            logger.warning("Invalid amount for currency: " + amount);
            return Optional.empty();
        }

        // Erstelle Vanilla ItemStack
        ItemStack item = new ItemStack(def.material(), Math.min(amount, 64));
        ItemMeta meta = item.getItemMeta();

        // Custom Model Data
        meta.setCustomModelData(def.customModelData());

        // Display Name
        meta.displayName(def.displayName());

        // Lore
        List<Component> lore = new ArrayList<>(def.lore());
        if (amount > 1) {
            lore.add(Component.empty());
            lore.add(Component.text("Menge: " + amount, NamedTextColor.AQUA));
        }
        meta.lore(lore);

        // PDC: Item-ID für Identifikation
        meta.getPersistentDataContainer().set(
                itemIdKey,
                PersistentDataType.STRING,
                def.itemId()
        );

        item.setItemMeta(meta);
        return Optional.of(item);
    }

    /**
     * Prüft ob ein ItemStack eine Währungs-Münze ist.
     *
     * @param itemStack ItemStack
     * @return true wenn Währung, false sonst
     */
    public boolean isCurrencyItem(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = itemStack.getItemMeta();
        if (!meta.getPersistentDataContainer().has(itemIdKey, PersistentDataType.STRING)) {
            return false;
        }

        String itemId = meta.getPersistentDataContainer().get(itemIdKey, PersistentDataType.STRING);
        return currencyDefinitions.values().stream()
                .anyMatch(def -> def.itemId().equals(itemId));
    }

    /**
     * Gibt den Währungstyp eines Items zurück.
     *
     * @param itemStack ItemStack
     * @return Währungstyp oder Optional.empty()
     */
    public Optional<String> getCurrencyType(ItemStack itemStack) {
        if (!isCurrencyItem(itemStack)) {
            return Optional.empty();
        }

        ItemMeta meta = itemStack.getItemMeta();
        String itemId = meta.getPersistentDataContainer().get(itemIdKey, PersistentDataType.STRING);

        return currencyDefinitions.entrySet().stream()
                .filter(entry -> entry.getValue().itemId().equals(itemId))
                .map(Map.Entry::getKey)
                .findFirst();
    }

    /**
     * Berechnet den Gesamtwert eines ItemStacks in Basis-Währung (Bronze).
     *
     * @param itemStack ItemStack
     * @return Wert in Bronze oder 0
     */
    public int getCurrencyValue(ItemStack itemStack) {
        return getCurrencyType(itemStack)
                .map(type -> currencyDefinitions.get(type).value() * itemStack.getAmount())
                .orElse(0);
    }

    /**
     * Gibt alle verfügbaren Währungstypen zurück.
     *
     * @return Set von Währungstypen
     */
    public Set<String> getCurrencyTypes() {
        return new HashSet<>(currencyDefinitions.keySet());
    }

    /**
     * Gibt die Definition einer Währung zurück.
     *
     * @param currencyType Währungstyp
     * @return CurrencyDefinition oder null
     */
    public CurrencyDefinition getCurrencyDefinition(String currencyType) {
        return currencyDefinitions.get(currencyType.toLowerCase());
    }

    /**
     * Währungs-Definition Record.
     *
     * @param itemId Item-ID für PDC
     * @param material Vanilla Material
     * @param customModelData Custom Model Data
     * @param displayName Display Name
     * @param lore Lore
     * @param value Währungswert in Basis-Währung (Bronze)
     */
    public record CurrencyDefinition(
            String itemId,
            Material material,
            int customModelData,
            Component displayName,
            List<Component> lore,
            int value
    ) {
    }
}
