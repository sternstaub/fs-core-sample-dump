package de.fallenstar.plot.model;

import org.bukkit.Material;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

/**
 * Serialisierbares Daten-Objekt für TradeguildPlot-Persistierung.
 *
 * **Zweck:**
 * - Speichert alle TradeguildPlot-Daten in serialisierbarer Form
 * - Wird von TradeguildPlotFactory für save/load verwendet
 * - Keine Business-Logik, nur Daten
 *
 * **Enthaltene Daten:**
 * - Plot-Identifier (UUID → Plot-Identifier mapping)
 * - Custom-Name
 * - Storage (Lager-Inventar)
 * - Preise (Buy/Sell)
 * - NPC-Daten (IDs, Typen, Slots)
 * - Quest-IDs (Quests werden separat gespeichert)
 *
 * **Nicht enthalten:**
 * - npcCache (wird zur Laufzeit rekonstruiert)
 * - Quest-Objekte (nur IDs)
 * - Location (wird über PlotProvider geholt)
 *
 * @author FallenStar
 * @version 1.0
 */
public class TradeguildPlotData implements Serializable {

    private static final long serialVersionUID = 1L;

    // Plot-Identifier
    private String plotIdentifier;

    // NamedPlot-Daten
    private String customName;

    // StorageContainerPlot-Daten
    private Map<String, Integer> storage;  // Material.name() → Menge
    private Map<String, String> buyPrices;  // Material.name() → BigDecimal.toString()
    private Map<String, String> sellPrices;  // Material.name() → BigDecimal.toString()

    // NpcContainerPlot-Daten
    private List<String> npcIds;  // UUID.toString()
    private Map<String, String> npcTypes;  // UUID.toString() → Type

    // SlottablePlot-Daten
    private Map<Integer, String> slots;  // Slot → UUID.toString()
    private int maxSlots;

    // QuestDistributor-Daten
    private List<String> questIds;  // Quest-UUIDs

    /**
     * Leerer Konstruktor für Deserialisierung.
     */
    public TradeguildPlotData() {
        this.storage = new HashMap<>();
        this.buyPrices = new HashMap<>();
        this.sellPrices = new HashMap<>();
        this.npcIds = new ArrayList<>();
        this.npcTypes = new HashMap<>();
        this.slots = new HashMap<>();
        this.questIds = new ArrayList<>();
        this.maxSlots = 5;
    }

    /**
     * Konstruktor mit allen Daten.
     */
    public TradeguildPlotData(String plotIdentifier) {
        this();
        this.plotIdentifier = plotIdentifier;
    }

    // ========== Getter/Setter ==========

    public String getPlotIdentifier() {
        return plotIdentifier;
    }

    public void setPlotIdentifier(String plotIdentifier) {
        this.plotIdentifier = plotIdentifier;
    }

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    public Map<String, Integer> getStorage() {
        return storage;
    }

    public void setStorage(Map<String, Integer> storage) {
        this.storage = storage;
    }

    public Map<String, String> getBuyPrices() {
        return buyPrices;
    }

    public void setBuyPrices(Map<String, String> buyPrices) {
        this.buyPrices = buyPrices;
    }

    public Map<String, String> getSellPrices() {
        return sellPrices;
    }

    public void setSellPrices(Map<String, String> sellPrices) {
        this.sellPrices = sellPrices;
    }

    public List<String> getNpcIds() {
        return npcIds;
    }

    public void setNpcIds(List<String> npcIds) {
        this.npcIds = npcIds;
    }

    public Map<String, String> getNpcTypes() {
        return npcTypes;
    }

    public void setNpcTypes(Map<String, String> npcTypes) {
        this.npcTypes = npcTypes;
    }

    public Map<Integer, String> getSlots() {
        return slots;
    }

    public void setSlots(Map<Integer, String> slots) {
        this.slots = slots;
    }

    public int getMaxSlots() {
        return maxSlots;
    }

    public void setMaxSlots(int maxSlots) {
        this.maxSlots = maxSlots;
    }

    public List<String> getQuestIds() {
        return questIds;
    }

    public void setQuestIds(List<String> questIds) {
        this.questIds = questIds;
    }

    // ========== Utility Methods ==========

    /**
     * Konvertiert Material-Map zu String-Map.
     */
    public static Map<String, Integer> convertStorageToStrings(Map<Material, Integer> storage) {
        Map<String, Integer> result = new HashMap<>();
        storage.forEach((material, amount) -> result.put(material.name(), amount));
        return result;
    }

    /**
     * Konvertiert String-Map zu Material-Map.
     */
    public static Map<Material, Integer> convertStorageFromStrings(Map<String, Integer> storage) {
        Map<Material, Integer> result = new HashMap<>();
        storage.forEach((materialName, amount) -> {
            try {
                Material material = Material.valueOf(materialName);
                result.put(material, amount);
            } catch (IllegalArgumentException e) {
                // Material existiert nicht mehr → überspringen
            }
        });
        return result;
    }

    /**
     * Konvertiert BigDecimal-Map zu String-Map.
     */
    public static Map<String, String> convertPricesToStrings(Map<Material, BigDecimal> prices) {
        Map<String, String> result = new HashMap<>();
        prices.forEach((material, price) -> result.put(material.name(), price.toString()));
        return result;
    }

    /**
     * Konvertiert String-Map zu BigDecimal-Map.
     */
    public static Map<Material, BigDecimal> convertPricesFromStrings(Map<String, String> prices) {
        Map<Material, BigDecimal> result = new HashMap<>();
        prices.forEach((materialName, priceStr) -> {
            try {
                Material material = Material.valueOf(materialName);
                BigDecimal price = new BigDecimal(priceStr);
                result.put(material, price);
            } catch (IllegalArgumentException e) {
                // Material existiert nicht mehr oder ungültige BigDecimal → überspringen
            }
        });
        return result;
    }

    /**
     * Konvertiert UUID-Liste zu String-Liste.
     */
    public static List<String> convertUuidsToStrings(List<UUID> uuids) {
        List<String> result = new ArrayList<>();
        uuids.forEach(uuid -> result.add(uuid.toString()));
        return result;
    }

    /**
     * Konvertiert String-Liste zu UUID-Liste.
     */
    public static List<UUID> convertUuidsFromStrings(List<String> strings) {
        List<UUID> result = new ArrayList<>();
        strings.forEach(str -> {
            try {
                result.add(UUID.fromString(str));
            } catch (IllegalArgumentException e) {
                // Ungültige UUID → überspringen
            }
        });
        return result;
    }

    /**
     * Konvertiert UUID-Map zu String-Map.
     */
    public static Map<String, String> convertUuidMapToStrings(Map<UUID, String> map) {
        Map<String, String> result = new HashMap<>();
        map.forEach((uuid, value) -> result.put(uuid.toString(), value));
        return result;
    }

    /**
     * Konvertiert String-Map zu UUID-Map.
     */
    public static Map<UUID, String> convertUuidMapFromStrings(Map<String, String> map) {
        Map<UUID, String> result = new HashMap<>();
        map.forEach((uuidStr, value) -> {
            try {
                result.put(UUID.fromString(uuidStr), value);
            } catch (IllegalArgumentException e) {
                // Ungültige UUID → überspringen
            }
        });
        return result;
    }

    /**
     * Konvertiert Slot-Map (Integer → UUID) zu String-Map.
     */
    public static Map<Integer, String> convertSlotMapToStrings(Map<Integer, UUID> slots) {
        Map<Integer, String> result = new HashMap<>();
        slots.forEach((slot, uuid) -> result.put(slot, uuid.toString()));
        return result;
    }

    /**
     * Konvertiert String-Map zu Slot-Map (Integer → UUID).
     */
    public static Map<Integer, UUID> convertSlotMapFromStrings(Map<Integer, String> slots) {
        Map<Integer, UUID> result = new HashMap<>();
        slots.forEach((slot, uuidStr) -> {
            try {
                result.put(slot, UUID.fromString(uuidStr));
            } catch (IllegalArgumentException e) {
                // Ungültige UUID → überspringen
            }
        });
        return result;
    }
}
