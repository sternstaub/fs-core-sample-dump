package de.fallenstar.items.manager;

import de.fallenstar.items.model.SpecialItemDefinition;
import de.fallenstar.items.provider.MMOItemsItemProvider;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.logging.Logger;

/**
 * Manager für SpecialItems (Vanilla + MMOItems).
 *
 * Verwaltet alle Custom-Items im FallenStar-System:
 * - Vanilla SpecialItems (PDC-basiert mit Custom Model Data)
 * - MMOItems Custom-Items (optional, wenn MMOItems geladen)
 *
 * Verwendung:
 * - Economy-Modul: Währungen (Bronzestern, Silberstern, Goldstern)
 * - Quest-System: Quest-Items
 * - Event-System: Event-Items
 * - Integration mit MMOItems für Waffen/Rüstungen
 *
 * @author FallenStar
 * @version 3.0 (Generisches SpecialItem-System)
 */
public class SpecialItemManager {

    private final Plugin plugin;
    private final Logger logger;
    private final NamespacedKey itemIdKey;
    private final Map<String, SpecialItemDefinition> registeredItems;
    private final MMOItemsItemProvider mmoItemsProvider; // Optional

    /**
     * Konstruktor für SpecialItemManager.
     *
     * @param plugin Plugin-Instanz
     * @param logger Logger
     * @param mmoItemsProvider MMOItemsItemProvider (optional, kann null sein)
     */
    public SpecialItemManager(Plugin plugin, Logger logger, MMOItemsItemProvider mmoItemsProvider) {
        this.plugin = plugin;
        this.logger = logger;
        this.mmoItemsProvider = mmoItemsProvider;
        this.itemIdKey = new NamespacedKey(plugin, "special_item_id");
        this.registeredItems = new HashMap<>();

        logger.info("SpecialItemManager v3.0 initialisiert (Generisches SpecialItem-System)");
    }

    /**
     * Registriert ein Vanilla SpecialItem.
     *
     * @param id Eindeutige ID (z.B. "bronze_stern")
     * @param material Vanilla Material
     * @param customModelData Custom Model Data
     * @param displayName Display-Name
     * @param lore Lore
     */
    public void registerVanillaItem(String id, Material material, int customModelData,
                                     Component displayName, List<Component> lore) {
        SpecialItemDefinition definition = SpecialItemDefinition.createVanilla(
                id, material, customModelData, displayName, lore
        );

        registeredItems.put(id, definition);
        logger.fine("Vanilla SpecialItem registriert: " + id);
    }

    /**
     * Registriert ein MMOItem als SpecialItem.
     *
     * Erfordert MMOItems Plugin. Wenn MMOItems nicht verfügbar,
     * wird die Registrierung ignoriert.
     *
     * @param id Eindeutige ID (z.B. "legendary_sword")
     * @param mmoType MMOItems Type (z.B. "SWORD")
     * @param mmoId MMOItems ID (z.B. "LEGENDARY_BLADE")
     */
    public void registerMMOItem(String id, String mmoType, String mmoId) {
        if (mmoItemsProvider == null) {
            logger.warning("Versuch MMOItem '" + id + "' zu registrieren, aber MMOItems nicht verfügbar!");
            return;
        }

        SpecialItemDefinition definition = SpecialItemDefinition.createMMOItem(id, mmoType, mmoId);
        registeredItems.put(id, definition);
        logger.fine("MMOItem SpecialItem registriert: " + id + " (" + mmoType + ":" + mmoId + ")");
    }

    /**
     * Erstellt ein SpecialItem.
     *
     * @param id SpecialItem-ID
     * @param amount Anzahl
     * @return Optional mit ItemStack, leer wenn nicht gefunden
     */
    public Optional<ItemStack> createItem(String id, int amount) {
        SpecialItemDefinition definition = registeredItems.get(id);

        if (definition == null) {
            logger.warning("SpecialItem nicht gefunden: " + id);
            return Optional.empty();
        }

        return switch (definition.type()) {
            case VANILLA -> createVanillaItem(definition, amount);
            case MMO_ITEM -> createMMOItem(definition, amount);
        };
    }

    /**
     * Erstellt ein Vanilla SpecialItem.
     *
     * @param definition Item-Definition
     * @param amount Anzahl
     * @return Optional mit ItemStack
     */
    private Optional<ItemStack> createVanillaItem(SpecialItemDefinition definition, int amount) {
        ItemStack item = new ItemStack(definition.material(), Math.min(amount, 64));
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return Optional.empty();
        }

        // Custom Model Data
        if (definition.customModelData() > 0) {
            meta.setCustomModelData(definition.customModelData());
        }

        // Display Name + Lore
        if (definition.displayName() != null) {
            meta.displayName(definition.displayName());
        }
        if (definition.lore() != null && !definition.lore().isEmpty()) {
            meta.lore(definition.lore());
        }

        // PDC-basierte Identifikation
        meta.getPersistentDataContainer().set(
                itemIdKey,
                PersistentDataType.STRING,
                definition.id()
        );

        item.setItemMeta(meta);
        return Optional.of(item);
    }

    /**
     * Erstellt ein MMOItems SpecialItem.
     *
     * @param definition Item-Definition
     * @param amount Anzahl
     * @return Optional mit ItemStack
     */
    private Optional<ItemStack> createMMOItem(SpecialItemDefinition definition, int amount) {
        if (mmoItemsProvider == null) {
            logger.warning("Kann MMOItem nicht erstellen - MMOItems nicht verfügbar!");
            return Optional.empty();
        }

        try {
            return mmoItemsProvider.createItem(
                    definition.mmoType(),
                    definition.mmoId(),
                    amount
            );
        } catch (Exception e) {
            logger.warning("Fehler beim Erstellen von MMOItem '" + definition.id() + "': " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Prüft ob ein ItemStack ein SpecialItem ist.
     *
     * @param item ItemStack
     * @return true wenn SpecialItem
     */
    public boolean isSpecialItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }

        // Prüfe Vanilla SpecialItem (PDC)
        ItemMeta meta = item.getItemMeta();
        if (meta.getPersistentDataContainer().has(itemIdKey, PersistentDataType.STRING)) {
            return true;
        }

        // Prüfe MMOItem
        if (mmoItemsProvider != null) {
            return mmoItemsProvider.isCustomItem(item);
        }

        return false;
    }

    /**
     * Ermittelt die SpecialItem-ID eines ItemStacks.
     *
     * @param item ItemStack
     * @return Optional mit ID, leer wenn kein SpecialItem
     */
    public Optional<String> getSpecialItemId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return Optional.empty();
        }

        // Prüfe Vanilla SpecialItem (PDC)
        ItemMeta meta = item.getItemMeta();
        if (meta.getPersistentDataContainer().has(itemIdKey, PersistentDataType.STRING)) {
            String id = meta.getPersistentDataContainer().get(itemIdKey, PersistentDataType.STRING);
            return Optional.ofNullable(id);
        }

        // Prüfe MMOItem
        if (mmoItemsProvider != null && mmoItemsProvider.isCustomItem(item)) {
            return mmoItemsProvider.getItemId(item).map(mmoId -> {
                // Finde SpecialItem-Definition mit dieser MMO-ID
                for (SpecialItemDefinition def : registeredItems.values()) {
                    if (def.isMMOItem() && mmoId.equals(def.mmoId())) {
                        return def.id();
                    }
                }
                return "mmoitems:" + mmoId; // Fallback: MMOItems-ID direkt
            });
        }

        return Optional.empty();
    }

    /**
     * Gibt alle registrierten SpecialItem-IDs zurück.
     *
     * @return Set von IDs
     */
    public Set<String> getRegisteredItemIds() {
        return new HashSet<>(registeredItems.keySet());
    }

    /**
     * Gibt eine SpecialItem-Definition zurück.
     *
     * @param id SpecialItem-ID
     * @return Optional mit Definition
     */
    public Optional<SpecialItemDefinition> getDefinition(String id) {
        return Optional.ofNullable(registeredItems.get(id));
    }

    /**
     * Gibt alle registrierten Vanilla-SpecialItems zurück.
     *
     * @return Liste von Definitionen
     */
    public List<SpecialItemDefinition> getVanillaItems() {
        return registeredItems.values().stream()
                .filter(SpecialItemDefinition::isVanilla)
                .toList();
    }

    /**
     * Gibt alle registrierten MMOItems zurück.
     *
     * @return Liste von Definitionen
     */
    public List<SpecialItemDefinition> getMMOItems() {
        return registeredItems.values().stream()
                .filter(SpecialItemDefinition::isMMOItem)
                .toList();
    }

    /**
     * Gibt die Anzahl registrierter SpecialItems zurück.
     *
     * @return Anzahl
     */
    public int getRegisteredCount() {
        return registeredItems.size();
    }
}
