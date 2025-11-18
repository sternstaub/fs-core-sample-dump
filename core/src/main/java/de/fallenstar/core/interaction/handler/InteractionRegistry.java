package de.fallenstar.core.interaction.handler;

import de.fallenstar.core.interaction.Interactable;
import de.fallenstar.core.interaction.InteractionType;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry für alle Interactable-Objekte im System.
 *
 * Verwaltet Mappings:
 * - Location → Plot
 * - Entity UUID → NPC
 * - Item ID → Custom Item
 *
 * **Features:**
 * - Thread-Safe (ConcurrentHashMap)
 * - Schnelle Lookups
 * - Type-basierte Filterung
 *
 * **Verwendung:**
 * <pre>
 * // Plot registrieren
 * registry.registerPlot(location, tradeguildPlot);
 *
 * // NPC registrieren
 * registry.registerEntity(entityUuid, guildTraderNpc);
 *
 * // Beim Klick finden
 * Optional&lt;Interactable&gt; target = registry.getInteractableAtLocation(location);
 * </pre>
 *
 * @author FallenStar
 * @version 1.0
 */
public class InteractionRegistry {

    /**
     * Location → Interactable (für Plots).
     * Key: "world,x,y,z" Format für schnelle Lookups.
     */
    private final Map<String, Interactable> locationMap = new ConcurrentHashMap<>();

    /**
     * Entity UUID → Interactable (für NPCs).
     */
    private final Map<UUID, Interactable> entityMap = new ConcurrentHashMap<>();

    /**
     * Custom Item ID → Interactable (für Items).
     */
    private final Map<String, Interactable> itemMap = new ConcurrentHashMap<>();

    /**
     * Registriert ein Plot an einer Location.
     *
     * @param location Location
     * @param interactable Interactable (Plot)
     */
    public void registerPlot(Location location, Interactable interactable) {
        if (interactable.getInteractionType() != InteractionType.PLOT) {
            throw new IllegalArgumentException("Nur PLOT-Typ erlaubt für registerPlot()");
        }
        String key = locationKey(location);
        locationMap.put(key, interactable);
    }

    /**
     * Registriert eine Entity (NPC).
     *
     * @param entityUuid Entity UUID
     * @param interactable Interactable (NPC)
     */
    public void registerEntity(UUID entityUuid, Interactable interactable) {
        if (interactable.getInteractionType() != InteractionType.ENTITY) {
            throw new IllegalArgumentException("Nur ENTITY-Typ erlaubt für registerEntity()");
        }
        entityMap.put(entityUuid, interactable);
    }

    /**
     * Registriert ein Custom Item.
     *
     * @param itemId Item-ID
     * @param interactable Interactable (Item)
     */
    public void registerItem(String itemId, Interactable interactable) {
        if (interactable.getInteractionType() != InteractionType.ITEM) {
            throw new IllegalArgumentException("Nur ITEM-Typ erlaubt für registerItem()");
        }
        itemMap.put(itemId, interactable);
    }

    /**
     * Entfernt ein Plot von einer Location.
     *
     * @param location Location
     */
    public void unregisterPlot(Location location) {
        String key = locationKey(location);
        locationMap.remove(key);
    }

    /**
     * Entfernt eine Entity.
     *
     * @param entityUuid Entity UUID
     */
    public void unregisterEntity(UUID entityUuid) {
        entityMap.remove(entityUuid);
    }

    /**
     * Entfernt ein Custom Item.
     *
     * @param itemId Item-ID
     */
    public void unregisterItem(String itemId) {
        itemMap.remove(itemId);
    }

    /**
     * Findet Interactable an einer Location.
     *
     * @param location Location
     * @return Optional mit Interactable (Plot)
     */
    public Optional<Interactable> getInteractableAtLocation(Location location) {
        String key = locationKey(location);
        return Optional.ofNullable(locationMap.get(key));
    }

    /**
     * Findet Interactable für eine Entity.
     *
     * @param entityUuid Entity UUID
     * @return Optional mit Interactable (NPC)
     */
    public Optional<Interactable> getInteractableForEntity(UUID entityUuid) {
        return Optional.ofNullable(entityMap.get(entityUuid));
    }

    /**
     * Findet Interactable für ein Item.
     *
     * @param itemId Item-ID
     * @return Optional mit Interactable (Item)
     */
    public Optional<Interactable> getInteractableForItem(String itemId) {
        return Optional.ofNullable(itemMap.get(itemId));
    }

    /**
     * Findet Interactable für eine Entity-Instanz.
     *
     * @param entity Entity
     * @return Optional mit Interactable
     */
    public Optional<Interactable> getInteractableForEntity(Entity entity) {
        return getInteractableForEntity(entity.getUniqueId());
    }

    /**
     * Gibt alle registrierten Plots zurück.
     *
     * @return Collection von Interactables
     */
    public Collection<Interactable> getAllPlots() {
        return new ArrayList<>(locationMap.values());
    }

    /**
     * Gibt alle registrierten Entities zurück.
     *
     * @return Collection von Interactables
     */
    public Collection<Interactable> getAllEntities() {
        return new ArrayList<>(entityMap.values());
    }

    /**
     * Gibt alle registrierten Items zurück.
     *
     * @return Collection von Interactables
     */
    public Collection<Interactable> getAllItems() {
        return new ArrayList<>(itemMap.values());
    }

    /**
     * Löscht alle Registrierungen.
     */
    public void clear() {
        locationMap.clear();
        entityMap.clear();
        itemMap.clear();
    }

    /**
     * Erstellt Location-Key für Map.
     *
     * Format: "worldName,x,y,z"
     *
     * @param location Location
     * @return Location-Key
     */
    private String locationKey(Location location) {
        return String.format("%s,%d,%d,%d",
                location.getWorld().getName(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ()
        );
    }

    /**
     * Gibt Anzahl registrierter Plots zurück.
     *
     * @return Anzahl
     */
    public int getPlotCount() {
        return locationMap.size();
    }

    /**
     * Gibt Anzahl registrierter Entities zurück.
     *
     * @return Anzahl
     */
    public int getEntityCount() {
        return entityMap.size();
    }

    /**
     * Gibt Anzahl registrierter Items zurück.
     *
     * @return Anzahl
     */
    public int getItemCount() {
        return itemMap.size();
    }
}
