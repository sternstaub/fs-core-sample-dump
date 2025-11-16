package de.fallenstar.storage.model;

import org.bukkit.Material;
import org.bukkit.Location;

import java.util.Objects;
import java.util.UUID;

/**
 * Repräsentiert ein gelagertes Material in einer Truhe.
 *
 * Speichert Material-Typ, Menge und Position der Truhe.
 *
 * @author FallenStar
 * @version 1.0
 */
public class StoredMaterial {

    private final UUID chestId;
    private final Location chestLocation;
    private final Material material;
    private int amount;

    /**
     * Erstellt ein neues StoredMaterial-Objekt.
     *
     * @param chestId Eindeutige ID der Truhe
     * @param chestLocation Location der Truhe
     * @param material Der Material-Typ
     * @param amount Die Menge
     */
    public StoredMaterial(UUID chestId, Location chestLocation, Material material, int amount) {
        this.chestId = chestId;
        this.chestLocation = chestLocation;
        this.material = material;
        this.amount = amount;
    }

    /**
     * @return Eindeutige ID der Truhe
     */
    public UUID getChestId() {
        return chestId;
    }

    /**
     * @return Location der Truhe
     */
    public Location getChestLocation() {
        return chestLocation;
    }

    /**
     * @return Der Material-Typ
     */
    public Material getMaterial() {
        return material;
    }

    /**
     * @return Die aktuelle Menge
     */
    public int getAmount() {
        return amount;
    }

    /**
     * Setzt die Menge des Materials.
     *
     * @param amount Die neue Menge
     */
    public void setAmount(int amount) {
        this.amount = amount;
    }

    /**
     * Fügt eine Menge hinzu.
     *
     * @param amount Die hinzuzufügende Menge
     */
    public void addAmount(int amount) {
        this.amount += amount;
    }

    /**
     * Entfernt eine Menge.
     *
     * @param amount Die zu entfernende Menge
     * @return true wenn erfolgreich, false wenn nicht genug vorhanden
     */
    public boolean removeAmount(int amount) {
        if (this.amount >= amount) {
            this.amount -= amount;
            return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StoredMaterial that = (StoredMaterial) o;
        return chestId.equals(that.chestId) && material == that.material;
    }

    @Override
    public int hashCode() {
        return Objects.hash(chestId, material);
    }

    @Override
    public String toString() {
        return "StoredMaterial{" +
                "material=" + material +
                ", amount=" + amount +
                ", chestId=" + chestId +
                '}';
    }
}
