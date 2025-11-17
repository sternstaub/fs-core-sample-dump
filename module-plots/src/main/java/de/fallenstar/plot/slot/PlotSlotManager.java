package de.fallenstar.plot.slot;

import de.fallenstar.core.provider.Plot;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.logging.Logger;

/**
 * Manager für Market-Plot Händler-Slots.
 *
 * Verwaltet alle Market-Grundstücke und deren Händler-Slots.
 *
 * **Features:**
 * - Market-Plots erstellen und verwalten
 * - Slots persistent speichern
 * - Slot-Käufe verarbeiten
 * - Slot-Positionen setzen
 *
 * **Persistierung:**
 * Slots werden in der config.yml gespeichert unter:
 * <pre>
 * market-plots:
 *   plot-uuid:
 *     max-slots: 5
 *     slots:
 *       slot-uuid:
 *         type: TRADER
 *         location: ...
 *         assigned-npc: ...
 *         active: true
 * </pre>
 *
 * @author FallenStar
 * @version 1.0
 */
public class PlotSlotManager {

    private final Logger logger;
    private final Map<UUID, MarketPlot> marketPlots;

    // Config-Werte
    private int initialSlots = 1;
    private int maxSlots = 5;

    /**
     * Konstruktor.
     *
     * @param logger Logger-Instanz
     */
    public PlotSlotManager(Logger logger) {
        this.logger = logger;
        this.marketPlots = new HashMap<>();
    }

    /**
     * Lädt Market-Plot Daten aus der Config.
     *
     * @param config FileConfiguration
     */
    public void loadFromConfig(FileConfiguration config) {
        // Lade Config-Werte
        initialSlots = config.getInt("market.trader-slots.initial-slots", 1);
        maxSlots = config.getInt("market.trader-slots.max-slots", 5);

        ConfigurationSection plotsSection = config.getConfigurationSection("market-plots");
        if (plotsSection == null) {
            logger.info("Keine Market-Plots in Config gefunden");
            return;
        }

        int loadedCount = 0;
        for (String plotUuidStr : plotsSection.getKeys(false)) {
            try {
                UUID plotUuid = UUID.fromString(plotUuidStr);
                ConfigurationSection plotSection = plotsSection.getConfigurationSection(plotUuidStr);

                if (plotSection == null) continue;

                // Erstelle MarketPlot (ohne Basis-Plot - wird später gesetzt)
                int plotMaxSlots = plotSection.getInt("max-slots", maxSlots);
                MarketPlot marketPlot = new MarketPlot(null, plotMaxSlots); // Basis-Plot wird später gesetzt

                // Lade Slots
                ConfigurationSection slotsSection = plotSection.getConfigurationSection("slots");
                if (slotsSection != null) {
                    for (String slotUuidStr : slotsSection.getKeys(false)) {
                        try {
                            UUID slotUuid = UUID.fromString(slotUuidStr);
                            ConfigurationSection slotSection = slotsSection.getConfigurationSection(slotUuidStr);

                            if (slotSection == null) continue;

                            // Parse Slot-Daten
                            PlotSlot.SlotType slotType = PlotSlot.SlotType.valueOf(
                                    slotSection.getString("type", "TRADER")
                            );

                            // Location parsen
                            String worldName = slotSection.getString("location.world");
                            double x = slotSection.getDouble("location.x");
                            double y = slotSection.getDouble("location.y");
                            double z = slotSection.getDouble("location.z");
                            float yaw = (float) slotSection.getDouble("location.yaw");
                            float pitch = (float) slotSection.getDouble("location.pitch");

                            Location location = new Location(
                                    org.bukkit.Bukkit.getWorld(worldName),
                                    x, y, z, yaw, pitch
                            );

                            // Optional: Assigned NPC
                            UUID assignedNPC = null;
                            String npcStr = slotSection.getString("assigned-npc");
                            if (npcStr != null && !npcStr.isEmpty()) {
                                assignedNPC = UUID.fromString(npcStr);
                            }

                            boolean active = slotSection.getBoolean("active", true);

                            // Erstelle Slot
                            PlotSlot slot = new PlotSlot(slotUuid, location, slotType, assignedNPC, active);
                            marketPlot.addSlot(slot);

                        } catch (Exception e) {
                            logger.warning("Fehler beim Laden von Slot " + slotUuidStr + ": " + e.getMessage());
                        }
                    }
                }

                marketPlots.put(plotUuid, marketPlot);
                loadedCount++;

            } catch (Exception e) {
                logger.warning("Fehler beim Laden von Plot " + plotUuidStr + ": " + e.getMessage());
            }
        }

        logger.info("Market-Plots geladen: " + loadedCount + " Einträge");
    }

    /**
     * Speichert Market-Plot Daten in die Config.
     *
     * @param config FileConfiguration
     */
    public void saveToConfig(FileConfiguration config) {
        // Lösche alte Daten
        config.set("market-plots", null);

        // Speichere alle Market-Plots
        for (Map.Entry<UUID, MarketPlot> entry : marketPlots.entrySet()) {
            UUID plotUuid = entry.getKey();
            MarketPlot marketPlot = entry.getValue();

            String basePath = "market-plots." + plotUuid;

            config.set(basePath + ".max-slots", marketPlot.getMaximumAvailableSlots());

            // Speichere Slots
            for (PlotSlot slot : marketPlot.getAllSlots()) {
                String slotPath = basePath + ".slots." + slot.getSlotId();

                config.set(slotPath + ".type", slot.getSlotType().name());

                Location loc = slot.getLocation();
                config.set(slotPath + ".location.world", loc.getWorld().getName());
                config.set(slotPath + ".location.x", loc.getX());
                config.set(slotPath + ".location.y", loc.getY());
                config.set(slotPath + ".location.z", loc.getZ());
                config.set(slotPath + ".location.yaw", loc.getYaw());
                config.set(slotPath + ".location.pitch", loc.getPitch());

                config.set(slotPath + ".assigned-npc",
                        slot.getAssignedNPC().map(UUID::toString).orElse(null));
                config.set(slotPath + ".active", slot.isActive());
            }
        }

        logger.info("Market-Plots gespeichert: " + marketPlots.size() + " Einträge");
    }

    /**
     * Erstellt oder gibt ein existierendes MarketPlot zurück.
     *
     * @param plot Basis-Plot
     * @return MarketPlot
     */
    public MarketPlot getOrCreateMarketPlot(Plot plot) {
        return marketPlots.computeIfAbsent(plot.getUuid(), uuid -> {
            MarketPlot marketPlot = new MarketPlot(plot, maxSlots);

            // Erstelle initialen Slot (ohne Position)
            for (int i = 0; i < initialSlots; i++) {
                PlotSlot initialSlot = new PlotSlot(
                        plot.getLocation(), // Temporäre Position
                        PlotSlot.SlotType.TRADER
                );
                marketPlot.addSlot(initialSlot);
            }

            logger.info("Market-Plot erstellt für: " + plot.getIdentifier());
            return marketPlot;
        });
    }

    /**
     * Gibt ein MarketPlot zurück.
     *
     * @param plotUuid UUID des Plots
     * @return Optional mit MarketPlot
     */
    public Optional<MarketPlot> getMarketPlot(UUID plotUuid) {
        return Optional.ofNullable(marketPlots.get(plotUuid));
    }

    /**
     * Gibt ein MarketPlot zurück.
     *
     * @param plot Basis-Plot
     * @return Optional mit MarketPlot
     */
    public Optional<MarketPlot> getMarketPlot(Plot plot) {
        return getMarketPlot(plot.getUuid());
    }

    /**
     * Kauft einen zusätzlichen Händler-Slot.
     *
     * Diese Methode erstellt einen neuen Slot an der angegebenen Position.
     * Die Bezahlung muss extern geprüft werden!
     *
     * @param marketPlot Das Market-Grundstück
     * @param location Position des neuen Slots
     * @return true wenn erfolgreich, false wenn Limit erreicht
     */
    public boolean purchaseSlot(MarketPlot marketPlot, Location location) {
        if (!marketPlot.canPurchaseMoreSlots()) {
            return false;
        }

        return marketPlot.createTraderSlot(location);
    }

    /**
     * Setzt die Position eines existierenden Slots.
     *
     * @param marketPlot Das Market-Grundstück
     * @param slotId UUID des Slots
     * @param newLocation Neue Position
     * @return true wenn erfolgreich
     */
    public boolean setSlotPosition(MarketPlot marketPlot, UUID slotId, Location newLocation) {
        return marketPlot.updateSlotLocation(slotId, newLocation);
    }

    /**
     * Entfernt einen Slot.
     *
     * Nur leere Slots können entfernt werden!
     *
     * @param marketPlot Das Market-Grundstück
     * @param slotId UUID des Slots
     * @return true wenn erfolgreich
     */
    public boolean removeSlot(MarketPlot marketPlot, UUID slotId) {
        return marketPlot.removeSlot(slotId);
    }

    /**
     * Gibt die Anzahl aller Market-Plots zurück.
     *
     * @return Anzahl Market-Plots
     */
    public int getMarketPlotCount() {
        return marketPlots.size();
    }

    /**
     * Gibt alle Market-Plots zurück.
     *
     * @return Map mit Plot-UUID → MarketPlot
     */
    public Map<UUID, MarketPlot> getAllMarketPlots() {
        return new HashMap<>(marketPlots);
    }

    /**
     * Gibt den Slot-Preis aus der Config zurück.
     *
     * @param config FileConfiguration
     * @return Slot-Preis
     */
    public double getSlotPrice(FileConfiguration config) {
        return config.getDouble("market.trader-slots.slot-price", 100.0);
    }

    /**
     * Gibt die Währung für Slot-Käufe zurück.
     *
     * @param config FileConfiguration
     * @return Währungs-ID
     */
    public String getSlotCurrency(FileConfiguration config) {
        return config.getString("market.trader-slots.currency", "sterne");
    }

    /**
     * Gibt das Währungs-Tier für Slot-Käufe zurück.
     *
     * @param config FileConfiguration
     * @return Tier (bronze, silver, gold)
     */
    public String getSlotCurrencyTier(FileConfiguration config) {
        return config.getString("market.trader-slots.currency-tier", "gold");
    }

    /**
     * Gibt die initial verfügbaren Slots zurück.
     *
     * @return Anzahl initialer Slots
     */
    public int getInitialSlots() {
        return initialSlots;
    }

    /**
     * Gibt die maximale Anzahl Slots zurück.
     *
     * @return Max Slots
     */
    public int getMaxSlots() {
        return maxSlots;
    }
}
