package de.fallenstar.storage.model;

import org.bukkit.Location;

import java.util.Objects;
import java.util.UUID;

/**
 * Metadaten zu einer registrierten Truhe.
 *
 * Speichert Position, Typ (normal/Empfangskiste) und Plot-Zugehörigkeit.
 *
 * @author FallenStar
 * @version 1.0
 */
public class ChestData {

    private final UUID chestId;
    private final UUID plotId;
    private final Location location;
    private boolean receiverChest;
    private long lastAccessed;

    /**
     * Erstellt ein neues ChestData-Objekt.
     *
     * @param chestId Eindeutige ID der Truhe
     * @param plotId ID des zugehörigen Plots
     * @param location Location der Truhe
     */
    public ChestData(UUID chestId, UUID plotId, Location location) {
        this.chestId = chestId;
        this.plotId = plotId;
        this.location = location;
        this.receiverChest = false;
        this.lastAccessed = System.currentTimeMillis();
    }

    /**
     * @return Eindeutige ID der Truhe
     */
    public UUID getChestId() {
        return chestId;
    }

    /**
     * @return ID des zugehörigen Plots
     */
    public UUID getPlotId() {
        return plotId;
    }

    /**
     * @return Location der Truhe
     */
    public Location getLocation() {
        return location;
    }

    /**
     * @return true wenn diese Truhe die Empfangskiste ist
     */
    public boolean isReceiverChest() {
        return receiverChest;
    }

    /**
     * Setzt den Empfangskisten-Status.
     *
     * @param receiverChest true um als Empfangskiste zu markieren
     */
    public void setReceiverChest(boolean receiverChest) {
        this.receiverChest = receiverChest;
    }

    /**
     * @return Zeitstempel des letzten Zugriffs
     */
    public long getLastAccessed() {
        return lastAccessed;
    }

    /**
     * Aktualisiert den Zugriffs-Zeitstempel.
     */
    public void updateAccessTime() {
        this.lastAccessed = System.currentTimeMillis();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChestData chestData = (ChestData) o;
        return chestId.equals(chestData.chestId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chestId);
    }

    @Override
    public String toString() {
        return "ChestData{" +
                "chestId=" + chestId +
                ", plotId=" + plotId +
                ", receiverChest=" + receiverChest +
                ", location=" + location +
                '}';
    }
}
