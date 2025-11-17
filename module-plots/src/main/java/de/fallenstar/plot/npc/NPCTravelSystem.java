package de.fallenstar.plot.npc;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.plot.slot.PlotSlot;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Verwaltet NPC-Reisen zwischen Grundstücken.
 *
 * Features:
 * - Verzögerung: 10 Sekunden pro Chunk-Entfernung
 * - Kosten: 5 Sterne pro Chunk-Entfernung
 * - Routen-Unterstützung (mehrere Waypoints)
 * - Restart-Handling: Bei Server-Neustart → NPC direkt ans Ziel
 *
 * **Verwendung:**
 * <pre>
 * TravelTicket ticket = npcTravelSystem.startTravel(npcId, fromPlot, toSlot);
 * // Warte auf Fertigstellung
 * if (ticket.isComplete()) {
 *     // NPC ans Ziel teleportieren
 * }
 * </pre>
 *
 * **Restart-Handling:**
 * - Aktive Reisen werden in Config gespeichert
 * - Bei onEnable() werden Reisen geladen
 * - Abgeschlossene Reisen → NPC direkt ans Ziel
 * - Laufende Reisen → Fortsetzen
 *
 * @author FallenStar
 * @version 1.0
 */
public class NPCTravelSystem {

    private static final int SECONDS_PER_CHUNK = 10;      // 10 Sekunden pro Chunk
    private static final int COST_PER_CHUNK = 5;          // 5 Sterne pro Chunk

    private final Plugin plugin;
    private final Logger logger;
    private final Map<UUID, TravelTicket> activeTravel;   // NPC-UUID → TravelTicket

    /**
     * Konstruktor für NPCTravelSystem.
     *
     * @param plugin Plugin-Instanz
     * @param logger Logger
     */
    public NPCTravelSystem(Plugin plugin, Logger logger) {
        this.plugin = plugin;
        this.logger = logger;
        this.activeTravel = new ConcurrentHashMap<>();

        logger.info("NPCTravelSystem initialisiert");
    }

    /**
     * Startet eine NPC-Reise.
     *
     * @param npcId NPC-UUID
     * @param from Start-Grundstück
     * @param toSlot Ziel-Slot
     * @return TravelTicket mit Reise-Details
     */
    public TravelTicket startTravel(UUID npcId, Plot from, PlotSlot toSlot) {
        // Hole Locations
        Location fromLocation = from.getSpawnLocation();
        Location toLocation = toSlot.getLocation();

        // Berechne Kosten und Dauer
        BigDecimal cost = calculateTravelCost(fromLocation, toLocation);
        int duration = calculateTravelTime(fromLocation, toLocation);

        // Erstelle Ticket
        TravelTicket ticket = new TravelTicket(npcId, fromLocation, toLocation, cost, duration);

        // Registriere Reise
        activeTravel.put(npcId, ticket);

        logger.info("NPC-Reise gestartet: " + ticket);

        // Schedule Ankunft
        scheduleArrival(ticket);

        return ticket;
    }

    /**
     * Berechnet Reisekosten.
     *
     * @param from Start-Location
     * @param to Ziel-Location
     * @return Kosten in Basiswährung (5 Sterne/Chunk)
     */
    public BigDecimal calculateTravelCost(Location from, Location to) {
        int chunkDistance = calculateChunkDistance(from, to);
        return BigDecimal.valueOf(chunkDistance * COST_PER_CHUNK);
    }

    /**
     * Berechnet Reisedauer.
     *
     * @param from Start-Location
     * @param to Ziel-Location
     * @return Dauer in Sekunden (10s/Chunk)
     */
    public int calculateTravelTime(Location from, Location to) {
        int chunkDistance = calculateChunkDistance(from, to);
        return chunkDistance * SECONDS_PER_CHUNK;
    }

    /**
     * Berechnet Chunk-Entfernung zwischen zwei Locations.
     *
     * @param from Start-Location
     * @param to Ziel-Location
     * @return Chunk-Entfernung
     */
    private int calculateChunkDistance(Location from, Location to) {
        // Prüfe ob in derselben Welt
        if (from.getWorld() == null || to.getWorld() == null) {
            return 0;
        }

        if (!from.getWorld().equals(to.getWorld())) {
            // Verschiedene Welten → Hohe Kosten
            return 100;  // Beispiel-Wert
        }

        // Berechne Chunk-Koordinaten
        int fromChunkX = from.getBlockX() >> 4;
        int fromChunkZ = from.getBlockZ() >> 4;
        int toChunkX = to.getBlockX() >> 4;
        int toChunkZ = to.getBlockZ() >> 4;

        // Manhattan-Distanz (Chunk-Ebene)
        int deltaX = Math.abs(toChunkX - fromChunkX);
        int deltaZ = Math.abs(toChunkZ - fromChunkZ);

        return deltaX + deltaZ;
    }

    /**
     * Schedulet Ankunft eines NPCs.
     *
     * @param ticket Das TravelTicket
     */
    private void scheduleArrival(TravelTicket ticket) {
        long delayTicks = ticket.getDurationSeconds() * 20L;  // Sekunden → Ticks

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            handleArrival(ticket);
        }, delayTicks);
    }

    /**
     * Behandelt Ankunft eines NPCs.
     *
     * @param ticket Das TravelTicket
     */
    private void handleArrival(TravelTicket ticket) {
        UUID npcId = ticket.getNpcId();

        // Entferne aus aktiven Reisen
        activeTravel.remove(npcId);

        // TODO: NPC ans Ziel teleportieren (via NPCProvider)
        logger.info("NPC angekommen: " + npcId + " an " + ticket.getTo());

        // Optional: Event feuern
        // Bukkit.getPluginManager().callEvent(new NPCArrivalEvent(npcId, ticket));
    }

    /**
     * Gibt ein aktives Travel-Ticket zurück.
     *
     * @param npcId NPC-UUID
     * @return Optional mit TravelTicket
     */
    public Optional<TravelTicket> getActiveTravelTicket(UUID npcId) {
        return Optional.ofNullable(activeTravel.get(npcId));
    }

    /**
     * Prüft ob ein NPC aktuell reist.
     *
     * @param npcId NPC-UUID
     * @return true wenn Reise aktiv
     */
    public boolean isTraveling(UUID npcId) {
        return activeTravel.containsKey(npcId);
    }

    /**
     * Bricht eine Reise ab.
     *
     * @param npcId NPC-UUID
     * @return true wenn erfolgreich abgebrochen
     */
    public boolean cancelTravel(UUID npcId) {
        TravelTicket removed = activeTravel.remove(npcId);

        if (removed != null) {
            logger.info("Reise abgebrochen: " + npcId);
            return true;
        }

        return false;
    }

    /**
     * Lädt aktive Reisen aus Config (Restart-Handling).
     *
     * @param config FileConfiguration
     */
    public void loadActiveTravel(FileConfiguration config) {
        ConfigurationSection section = config.getConfigurationSection("active-travels");
        if (section == null) {
            logger.info("Keine aktiven Reisen gefunden");
            return;
        }

        int loadedCount = 0;
        int completedCount = 0;

        for (String npcIdString : section.getKeys(false)) {
            try {
                UUID npcId = UUID.fromString(npcIdString);

                // Lade Reise-Daten
                ConfigurationSection travelSection = section.getConfigurationSection(npcIdString);
                if (travelSection == null) continue;

                Location from = deserializeLocation(travelSection.getConfigurationSection("from"));
                Location to = deserializeLocation(travelSection.getConfigurationSection("to"));
                long startTime = travelSection.getLong("start-time");
                int duration = travelSection.getInt("duration");
                BigDecimal cost = BigDecimal.valueOf(travelSection.getDouble("cost"));

                // Erstelle Ticket
                TravelTicket ticket = new TravelTicket(npcId, from, to, startTime, duration, cost);

                if (ticket.isComplete()) {
                    // Reise abgeschlossen → NPC direkt ans Ziel
                    handleArrival(ticket);
                    completedCount++;
                } else {
                    // Reise läuft noch → Fortsetzen
                    activeTravel.put(npcId, ticket);
                    scheduleArrival(ticket);
                    loadedCount++;
                }

            } catch (Exception e) {
                logger.warning("Fehler beim Laden einer Reise: " + e.getMessage());
            }
        }

        logger.info("Aktive Reisen geladen: " + loadedCount + " fortgesetzt, " + completedCount + " abgeschlossen");
    }

    /**
     * Speichert aktive Reisen in Config.
     *
     * @param config FileConfiguration
     */
    public void saveActiveTravel(FileConfiguration config) {
        // Lösche alte Daten
        config.set("active-travels", null);

        // Speichere alle aktiven Reisen
        for (Map.Entry<UUID, TravelTicket> entry : activeTravel.entrySet()) {
            UUID npcId = entry.getKey();
            TravelTicket ticket = entry.getValue();

            String path = "active-travels." + npcId.toString();

            serializeLocation(config, path + ".from", ticket.getFrom());
            serializeLocation(config, path + ".to", ticket.getTo());
            config.set(path + ".start-time", ticket.getStartTime());
            config.set(path + ".duration", ticket.getDurationSeconds());
            config.set(path + ".cost", ticket.getCost().doubleValue());
        }

        logger.info("Aktive Reisen gespeichert: " + activeTravel.size());
    }

    /**
     * Serialisiert eine Location in Config.
     *
     * @param config Config
     * @param path Config-Pfad
     * @param location Location
     */
    private void serializeLocation(FileConfiguration config, String path, Location location) {
        config.set(path + ".world", location.getWorld() != null ? location.getWorld().getName() : "world");
        config.set(path + ".x", location.getX());
        config.set(path + ".y", location.getY());
        config.set(path + ".z", location.getZ());
        config.set(path + ".yaw", location.getYaw());
        config.set(path + ".pitch", location.getPitch());
    }

    /**
     * Deserialisiert eine Location aus Config.
     *
     * @param section ConfigurationSection
     * @return Location
     */
    private Location deserializeLocation(ConfigurationSection section) {
        if (section == null) {
            return new Location(Bukkit.getWorlds().get(0), 0, 0, 0);
        }

        String worldName = section.getString("world", "world");
        double x = section.getDouble("x");
        double y = section.getDouble("y");
        double z = section.getDouble("z");
        float yaw = (float) section.getDouble("yaw");
        float pitch = (float) section.getDouble("pitch");

        return new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
    }

    /**
     * Gibt die Anzahl aktiver Reisen zurück.
     *
     * @return Anzahl
     */
    public int getActiveTravelCount() {
        return activeTravel.size();
    }

    /**
     * Gibt Debug-Informationen zurück.
     *
     * @return Debug-String
     */
    public String getDebugInfo() {
        return "NPCTravelSystem{" +
                "activeTravel=" + activeTravel.size() +
                ", secondsPerChunk=" + SECONDS_PER_CHUNK +
                ", costPerChunk=" + COST_PER_CHUNK +
                '}';
    }
}
