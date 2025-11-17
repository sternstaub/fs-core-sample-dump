package de.fallenstar.plot.storage.model;

import org.bukkit.Location;

import java.util.Objects;
import java.util.UUID;

/**
 * Metadaten zu einer registrierten Truhe.
 *
 * Speichert Position, Typ (INPUT/OUTPUT/STORAGE) und Plot-Zugehörigkeit.
 *
 * **ChestType:**
 * - INPUT: Empfangskiste für gekaufte Items
 * - OUTPUT: Verkaufskiste für zum Verkauf angebotene Items
 * - STORAGE: Normale Storage-Truhe
 *
 * @author FallenStar
 * @version 2.0
 */
public class ChestData {

    private final UUID chestId;
    private final UUID plotId;
    private final Location location;
    private ChestType chestType;
    private long lastAccessed;

    /**
     * Erstellt ein neues ChestData-Objekt.
     *
     * @param chestId Eindeutige ID der Truhe
     * @param plotId ID des zugehörigen Plots
     * @param location Location der Truhe
     */
    public ChestData(UUID chestId, UUID plotId, Location location) {
        this(chestId, plotId, location, ChestType.STORAGE);
    }

    /**
     * Erstellt ein neues ChestData-Objekt mit Typ.
     *
     * @param chestId Eindeutige ID der Truhe
     * @param plotId ID des zugehörigen Plots
     * @param location Location der Truhe
     * @param chestType Typ der Truhe
     */
    public ChestData(UUID chestId, UUID plotId, Location location, ChestType chestType) {
        this.chestId = chestId;
        this.plotId = plotId;
        this.location = location;
        this.chestType = chestType;
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
     * @return Typ der Truhe (INPUT/OUTPUT/STORAGE)
     */
    public ChestType getChestType() {
        return chestType;
    }

    /**
     * Setzt den Truhen-Typ.
     *
     * @param chestType Neuer Typ
     */
    public void setChestType(ChestType chestType) {
        this.chestType = chestType;
    }

    /**
     * @return true wenn diese Truhe eine Input-Chest ist
     */
    public boolean isInputChest() {
        return chestType == ChestType.INPUT;
    }

    /**
     * @return true wenn diese Truhe eine Output-Chest ist
     */
    public boolean isOutputChest() {
        return chestType == ChestType.OUTPUT;
    }

    /**
     * @return true wenn diese Truhe eine Storage-Chest ist
     */
    public boolean isStorageChest() {
        return chestType == ChestType.STORAGE;
    }

    /**
     * Legacy-Kompatibilität: Receiver-Chest = Input-Chest.
     *
     * @return true wenn diese Truhe die Empfangskiste ist
     * @deprecated Verwende {@link #isInputChest()} stattdessen
     */
    @Deprecated
    public boolean isReceiverChest() {
        return isInputChest();
    }

    /**
     * Legacy-Kompatibilität: Receiver-Chest = Input-Chest.
     *
     * @param receiverChest true um als Empfangskiste zu markieren
     * @deprecated Verwende {@link #setChestType(ChestType)} mit ChestType.INPUT stattdessen
     */
    @Deprecated
    public void setReceiverChest(boolean receiverChest) {
        if (receiverChest) {
            this.chestType = ChestType.INPUT;
        } else if (this.chestType == ChestType.INPUT) {
            this.chestType = ChestType.STORAGE;
        }
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
                ", chestType=" + chestType +
                ", location=" + location +
                '}';
    }
}
