package de.fallenstar.plot.storage.model;

import de.fallenstar.core.provider.Plot;
import org.bukkit.Material;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Aggregation aller gelagerter Materialien auf einem Grundstück.
 *
 * Verwaltet alle Truhen und deren Inhalte auf einem Plot.
 * Thread-safe für asynchrone Updates.
 *
 * @author FallenStar
 * @version 1.0
 */
public class PlotStorage {

    private final Plot plot;
    private final Map<Material, List<StoredMaterial>> materialMap;
    private final Map<UUID, ChestData> chestDataMap;
    private long lastUpdate;

    /**
     * Erstellt ein neues PlotStorage-Objekt.
     *
     * @param plot Das zugehörige Plot-Objekt
     */
    public PlotStorage(Plot plot) {
        this.plot = plot;
        this.materialMap = new ConcurrentHashMap<>();
        this.chestDataMap = new ConcurrentHashMap<>();
        this.lastUpdate = System.currentTimeMillis();
    }

    /**
     * @return Das zugehörige Plot-Objekt
     */
    public Plot getPlot() {
        return plot;
    }

    /**
     * Fügt gespeichertes Material hinzu.
     *
     * @param storedMaterial Das zu speichernde Material
     */
    public void addMaterial(StoredMaterial storedMaterial) {
        materialMap.computeIfAbsent(storedMaterial.getMaterial(), k -> new ArrayList<>())
                   .add(storedMaterial);
        updateTimestamp();
    }

    /**
     * Entfernt gespeichertes Material.
     *
     * @param storedMaterial Das zu entfernende Material
     */
    public void removeMaterial(StoredMaterial storedMaterial) {
        List<StoredMaterial> materials = materialMap.get(storedMaterial.getMaterial());
        if (materials != null) {
            materials.remove(storedMaterial);
            if (materials.isEmpty()) {
                materialMap.remove(storedMaterial.getMaterial());
            }
        }
        updateTimestamp();
    }

    /**
     * Gibt die Gesamtmenge eines Materials auf dem Plot zurück.
     *
     * @param material Der Material-Typ
     * @return Die Gesamtmenge (0 wenn nicht vorhanden)
     */
    public int getTotalAmount(Material material) {
        List<StoredMaterial> materials = materialMap.get(material);
        if (materials == null) {
            return 0;
        }
        return materials.stream()
                       .mapToInt(StoredMaterial::getAmount)
                       .sum();
    }

    /**
     * Gibt alle StoredMaterial-Objekte für ein Material zurück.
     *
     * @param material Der Material-Typ
     * @return Liste von StoredMaterial (leer wenn nicht vorhanden)
     */
    public List<StoredMaterial> getMaterialLocations(Material material) {
        return new ArrayList<>(materialMap.getOrDefault(material, Collections.emptyList()));
    }

    /**
     * Gibt alle gespeicherten Material-Typen zurück.
     *
     * @return Set aller Material-Typen
     */
    public Set<Material> getAllMaterials() {
        return new HashSet<>(materialMap.keySet());
    }

    /**
     * Registriert Truhen-Metadaten.
     *
     * @param chestData Die Truhen-Metadaten
     */
    public void registerChest(ChestData chestData) {
        chestDataMap.put(chestData.getChestId(), chestData);
    }

    /**
     * Gibt Truhen-Metadaten zurück.
     *
     * @param chestId Die Truhen-ID
     * @return ChestData oder null wenn nicht registriert
     */
    public ChestData getChestData(UUID chestId) {
        return chestDataMap.get(chestId);
    }

    /**
     * Gibt die Empfangskiste zurück (falls gesetzt).
     *
     * @return ChestData der Empfangskiste oder null
     */
    public ChestData getReceiverChest() {
        return chestDataMap.values().stream()
                          .filter(ChestData::isReceiverChest)
                          .findFirst()
                          .orElse(null);
    }

    /**
     * Gibt alle registrierten Truhen zurück.
     *
     * @return Collection aller ChestData-Objekte
     */
    public Collection<ChestData> getAllChests() {
        return new ArrayList<>(chestDataMap.values());
    }

    /**
     * Leert den gesamten Storage (z.B. bei Rescan).
     */
    public void clear() {
        materialMap.clear();
        chestDataMap.clear();
        updateTimestamp();
    }

    /**
     * @return Zeitstempel des letzten Updates
     */
    public long getLastUpdate() {
        return lastUpdate;
    }

    /**
     * Aktualisiert den Update-Zeitstempel.
     */
    private void updateTimestamp() {
        this.lastUpdate = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return "PlotStorage{" +
                "plot=" + plot.getIdentifier() +
                ", materials=" + materialMap.size() +
                ", chests=" + chestDataMap.size() +
                ", lastUpdate=" + lastUpdate +
                '}';
    }
}
