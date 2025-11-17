package de.fallenstar.plot.npc;

import org.bukkit.Location;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Reise-Ticket mit Reise-Details für NPC-Reisen.
 *
 * Features:
 * - Start- und Ziel-Location
 * - Reisekosten und -dauer
 * - Status-Prüfung (abgeschlossen?)
 * - Verbleibende Zeit berechnen
 *
 * **Verwendung:**
 * <pre>
 * TravelTicket ticket = new TravelTicket(npcId, from, to, cost, durationSeconds);
 * if (ticket.isComplete()) {
 *     // NPC ans Ziel teleportieren
 * }
 * </pre>
 *
 * @author FallenStar
 * @version 1.0
 */
public class TravelTicket {

    private final UUID npcId;
    private final Location from;
    private final Location to;
    private final long startTime;           // Timestamp in Millisekunden
    private final int durationSeconds;      // Dauer in Sekunden
    private final BigDecimal cost;          // Kosten in Basiswährung

    /**
     * Konstruktor für TravelTicket.
     *
     * @param npcId NPC-UUID
     * @param from Start-Location
     * @param to Ziel-Location
     * @param cost Kosten in Basiswährung
     * @param durationSeconds Dauer in Sekunden
     */
    public TravelTicket(
            UUID npcId,
            Location from,
            Location to,
            BigDecimal cost,
            int durationSeconds
    ) {
        this.npcId = npcId;
        this.from = from.clone();
        this.to = to.clone();
        this.startTime = System.currentTimeMillis();
        this.durationSeconds = durationSeconds;
        this.cost = cost;
    }

    /**
     * Konstruktor für Deserialisierung aus Config.
     *
     * @param npcId NPC-UUID
     * @param from Start-Location
     * @param to Ziel-Location
     * @param startTime Start-Timestamp
     * @param durationSeconds Dauer in Sekunden
     * @param cost Kosten
     */
    public TravelTicket(
            UUID npcId,
            Location from,
            Location to,
            long startTime,
            int durationSeconds,
            BigDecimal cost
    ) {
        this.npcId = npcId;
        this.from = from.clone();
        this.to = to.clone();
        this.startTime = startTime;
        this.durationSeconds = durationSeconds;
        this.cost = cost;
    }

    /**
     * Prüft ob die Reise abgeschlossen ist.
     *
     * @return true wenn die Reisezeit abgelaufen ist
     */
    public boolean isComplete() {
        long currentTime = System.currentTimeMillis();
        long elapsedMillis = currentTime - startTime;
        long durationMillis = durationSeconds * 1000L;

        return elapsedMillis >= durationMillis;
    }

    /**
     * Gibt die verbleibende Reisezeit in Sekunden zurück.
     *
     * @return Verbleibende Sekunden (0 wenn abgeschlossen)
     */
    public int getRemainingSeconds() {
        if (isComplete()) {
            return 0;
        }

        long currentTime = System.currentTimeMillis();
        long elapsedMillis = currentTime - startTime;
        long durationMillis = durationSeconds * 1000L;
        long remainingMillis = durationMillis - elapsedMillis;

        return (int) (remainingMillis / 1000);
    }

    /**
     * Gibt den Fortschritt in Prozent zurück.
     *
     * @return Fortschritt (0-100%)
     */
    public int getProgressPercent() {
        if (isComplete()) {
            return 100;
        }

        long currentTime = System.currentTimeMillis();
        long elapsedMillis = currentTime - startTime;
        long durationMillis = durationSeconds * 1000L;

        return (int) ((elapsedMillis * 100) / durationMillis);
    }

    /**
     * Gibt die NPC-UUID zurück.
     *
     * @return NPC-UUID
     */
    public UUID getNpcId() {
        return npcId;
    }

    /**
     * Gibt die Start-Location zurück.
     *
     * @return Geklonte Location
     */
    public Location getFrom() {
        return from.clone();
    }

    /**
     * Gibt die Ziel-Location zurück.
     *
     * @return Geklonte Location
     */
    public Location getTo() {
        return to.clone();
    }

    /**
     * Gibt den Start-Timestamp zurück.
     *
     * @return Timestamp in Millisekunden
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * Gibt die Reisedauer in Sekunden zurück.
     *
     * @return Dauer
     */
    public int getDurationSeconds() {
        return durationSeconds;
    }

    /**
     * Gibt die Reisekosten zurück.
     *
     * @return Kosten in Basiswährung
     */
    public BigDecimal getCost() {
        return cost;
    }

    @Override
    public String toString() {
        return "TravelTicket{" +
                "npc=" + npcId +
                ", from=" + formatLocation(from) +
                ", to=" + formatLocation(to) +
                ", duration=" + durationSeconds + "s" +
                ", remaining=" + getRemainingSeconds() + "s" +
                ", progress=" + getProgressPercent() + "%" +
                ", cost=" + cost +
                '}';
    }

    /**
     * Formatiert eine Location.
     *
     * @param location Die Location
     * @return Formatierter String
     */
    private String formatLocation(Location location) {
        return String.format("%s(%d,%d,%d)",
                location.getWorld() != null ? location.getWorld().getName() : "?",
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ()
        );
    }
}
