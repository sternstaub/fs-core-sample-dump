package de.fallenstar.plot.npc.model;

import org.bukkit.Location;

import java.util.Objects;
import java.util.UUID;

/**
 * Datenmodell für einen Plot-NPC.
 *
 * NPCs werden auf Grundstücken gespawnt und haben folgende Eigenschaften:
 * - **HostPlot**: Das Grundstück auf dem der NPC steht
 * - **SourcePlot**: Das Grundstück von dem Resourcen gelesen werden
 * - **Owner**: Optional, für Spieler-gekaufte NPCs
 *
 * **SourcePlot-Konzept:**
 * - Gildenhändler: SourcePlot = HostPlot (liest vom eigenen Plot)
 * - Spielerhändler: SourcePlot = Wählbar (eigenes Grundstück des Spielers)
 * - Lokaler Bankier: SourcePlot = Bank-Plot (Münzreserven)
 *
 * @author FallenStar
 * @version 1.0
 */
public class PlotNPC {

    private final UUID npcId;
    private final NPCType type;
    private final UUID hostPlotId;
    private final UUID sourcePlotId;
    private final Location location;
    private UUID ownerUUID;
    private String customName;

    /**
     * Erstellt einen neuen PlotNPC.
     *
     * @param npcId Eindeutige NPC-ID
     * @param type NPC-Typ
     * @param hostPlotId Host-Plot ID (wo der NPC steht)
     * @param sourcePlotId Source-Plot ID (wo Resourcen herkommen)
     * @param location Spawn-Location
     * @param ownerUUID Owner-UUID (null für System-NPCs)
     * @param customName Custom-Name (null für Default-Namen)
     */
    public PlotNPC(
            UUID npcId,
            NPCType type,
            UUID hostPlotId,
            UUID sourcePlotId,
            Location location,
            UUID ownerUUID,
            String customName
    ) {
        this.npcId = npcId;
        this.type = type;
        this.hostPlotId = hostPlotId;
        this.sourcePlotId = sourcePlotId;
        this.location = location;
        this.ownerUUID = ownerUUID;
        this.customName = customName;
    }

    /**
     * @return Eindeutige NPC-ID
     */
    public UUID getNpcId() {
        return npcId;
    }

    /**
     * @return NPC-Typ
     */
    public NPCType getType() {
        return type;
    }

    /**
     * @return Host-Plot ID (wo der NPC steht)
     */
    public UUID getHostPlotId() {
        return hostPlotId;
    }

    /**
     * @return Source-Plot ID (wo Resourcen herkommen)
     */
    public UUID getSourcePlotId() {
        return sourcePlotId;
    }

    /**
     * @return Spawn-Location
     */
    public Location getLocation() {
        return location;
    }

    /**
     * @return Owner-UUID oder null wenn System-NPC
     */
    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    /**
     * Setzt den Owner.
     *
     * @param ownerUUID Neue Owner-UUID
     */
    public void setOwnerUUID(UUID ownerUUID) {
        this.ownerUUID = ownerUUID;
    }

    /**
     * @return true wenn dieser NPC einen Owner hat
     */
    public boolean hasOwner() {
        return ownerUUID != null;
    }

    /**
     * @return Custom-Name oder null
     */
    public String getCustomName() {
        return customName;
    }

    /**
     * Setzt den Custom-Namen.
     *
     * @param customName Neuer Name
     */
    public void setCustomName(String customName) {
        this.customName = customName;
    }

    /**
     * @return Anzeigename (Custom-Name oder Default)
     */
    public String getDisplayName() {
        if (customName != null && !customName.isEmpty()) {
            return customName;
        }
        return type.getDisplayName();
    }

    /**
     * @return true wenn dieser NPC von einem Spieler gekauft wurde
     */
    public boolean isPurchasable() {
        return type.isPurchasable();
    }

    /**
     * @return true wenn dieser NPC einen SourcePlot benötigt
     */
    public boolean requiresSourcePlot() {
        return type.requiresSourcePlot();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlotNPC plotNPC = (PlotNPC) o;
        return npcId.equals(plotNPC.npcId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(npcId);
    }

    @Override
    public String toString() {
        return "PlotNPC{" +
                "npcId=" + npcId +
                ", type=" + type +
                ", displayName='" + getDisplayName() + '\'' +
                ", hostPlotId=" + hostPlotId +
                ", sourcePlotId=" + sourcePlotId +
                ", hasOwner=" + hasOwner() +
                '}';
    }
}
