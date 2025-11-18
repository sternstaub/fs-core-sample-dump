package de.fallenstar.plot.storage.manager;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.provider.PlotProvider;
import de.fallenstar.core.exception.ProviderFunctionalityNotFoundException;
import de.fallenstar.plot.storage.model.ChestData;
import de.fallenstar.plot.storage.model.ChestType;
import de.fallenstar.plot.storage.model.PlotStorage;
import de.fallenstar.plot.storage.model.StoredMaterial;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.logging.Logger;

/**
 * Service zum Scannen von Truhen auf Grundstücken.
 *
 * Durchsucht alle Chunks in einem Plot-Bereich und registriert
 * alle gefundenen Truhen mit ihren Inventaren im PlotStorage.
 *
 * @author FallenStar
 * @version 1.0
 */
public class ChestScanService {

    private final Logger logger;
    private final PlotProvider plotProvider;

    /**
     * Erstellt einen neuen ChestScanService.
     *
     * @param logger Der Logger für Ausgaben
     * @param plotProvider Der PlotProvider für Plot-Informationen
     */
    public ChestScanService(Logger logger, PlotProvider plotProvider) {
        this.logger = logger;
        this.plotProvider = plotProvider;
    }

    /**
     * Scannt alle Truhen auf einem Plot und registriert sie im PlotStorage.
     *
     * @param plot Das zu scannende Plot
     * @param plotStorage Das PlotStorage-Objekt zum Speichern
     * @return Anzahl der gefundenen Truhen
     */
    public int scanPlot(Plot plot, PlotStorage plotStorage) {
        int chestCount = 0;

        try {
            // Hole Plot-Grenzen (vereinfachte Annahme: nutze Location als Zentrum)
            Location centerLocation = plot.getLocation();
            if (centerLocation == null || centerLocation.getWorld() == null) {
                logger.warning("Plot " + plot.getIdentifier() + " hat keine gültige Location");
                return 0;
            }

            World world = centerLocation.getWorld();

            // Scanne Bereich um Plot-Location (z.B. 50x50 Blocks)
            // TODO: Später durch echte Plot-Grenzen ersetzen (via PlotProvider)
            int radius = 25; // 50x50 Bereich
            int centerX = centerLocation.getBlockX();
            int centerZ = centerLocation.getBlockZ();

            // Scanne alle Blöcke im Bereich
            for (int x = centerX - radius; x <= centerX + radius; x++) {
                for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                    for (int y = world.getMinHeight(); y < world.getMaxHeight(); y++) {
                        Block block = world.getBlockAt(x, y, z);

                        if (block.getType() == Material.CHEST ||
                            block.getType() == Material.TRAPPED_CHEST ||
                            block.getType() == Material.BARREL) {

                            Location blockLoc = block.getLocation();

                            // Prüfe ob Block wirklich auf diesem Plot ist
                            Plot blockPlot = plotProvider.getPlot(blockLoc);
                            if (blockPlot != null && blockPlot.getUuid().equals(plot.getUuid())) {
                                chestCount += scanChest(block, plot, plotStorage);
                            }
                        }
                    }
                }
            }

        } catch (ProviderFunctionalityNotFoundException e) {
            logger.warning("PlotProvider nicht verfügbar: " + e.getMessage());
        } catch (Exception e) {
            logger.severe("Fehler beim Scannen von Plot " + plot.getIdentifier() + ": " + e.getMessage());
            e.printStackTrace();
        }

        logger.info("Plot " + plot.getIdentifier() + ": " + chestCount + " Truhen gefunden");
        return chestCount;
    }

    /**
     * Scannt eine einzelne Truhe und registriert deren Inhalt.
     *
     * @param block Der Truhen-Block
     * @param plot Das zugehörige Plot
     * @param plotStorage Das PlotStorage-Objekt
     * @return 1 wenn erfolgreich, 0 bei Fehler
     */
    private int scanChest(Block block, Plot plot, PlotStorage plotStorage) {
        try {
            BlockState state = block.getState();
            if (!(state instanceof Chest)) {
                return 0;
            }

            Chest chest = (Chest) state;
            Location chestLocation = chest.getLocation();
            UUID chestId = generateChestId(chestLocation);

            // Registriere Truhe
            ChestData chestData = new ChestData(chestId, plot.getUuid(), chestLocation);
            plotStorage.registerChest(chestData);

            // Scanne Inventar
            Inventory inventory = chest.getInventory();
            Map<Material, Integer> materialCounts = new HashMap<>();

            for (ItemStack item : inventory.getContents()) {
                if (item != null && item.getType() != Material.AIR) {
                    Material material = item.getType();
                    int amount = item.getAmount();
                    materialCounts.merge(material, amount, Integer::sum);
                }
            }

            // Registriere Materialien
            for (Map.Entry<Material, Integer> entry : materialCounts.entrySet()) {
                StoredMaterial storedMaterial = new StoredMaterial(
                    chestId,
                    chestLocation,
                    entry.getKey(),
                    entry.getValue()
                );
                plotStorage.addMaterial(storedMaterial);
            }

            return 1;

        } catch (Exception e) {
            logger.warning("Fehler beim Scannen einer Truhe: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Generiert eine eindeutige ID für eine Truhe basierend auf ihrer Location.
     *
     * @param location Die Location der Truhe
     * @return Eindeutige UUID
     */
    private UUID generateChestId(Location location) {
        String idString = location.getWorld().getName() + "_" +
                         location.getBlockX() + "_" +
                         location.getBlockY() + "_" +
                         location.getBlockZ();
        return UUID.nameUUIDFromBytes(idString.getBytes());
    }

    /**
     * Scannt alle Plots und registriert deren Truhen.
     * Sollte beim Serverstart ausgeführt werden.
     *
     * @param plots Liste aller zu scannenden Plots
     * @param plotStorages Map von Plot-UUID zu PlotStorage
     * @return Gesamtanzahl gefundener Truhen
     */
    public int scanAllPlots(Collection<Plot> plots, Map<UUID, PlotStorage> plotStorages) {
        int totalChests = 0;

        logger.info("Starte Scan von " + plots.size() + " Plots...");

        for (Plot plot : plots) {
            PlotStorage storage = plotStorages.computeIfAbsent(
                plot.getUuid(),
                k -> new PlotStorage(plot)
            );

            storage.clear(); // Leere vorherige Daten
            totalChests += scanPlot(plot, storage);
        }

        logger.info("Scan abgeschlossen: " + totalChests + " Truhen auf " + plots.size() + " Plots");
        return totalChests;
    }

    /**
     * Scannt ein einzelnes Plot und gibt detaillierte Scan-Ergebnisse zurück.
     *
     * Diese Methode wird von `/plot storage scan` verwendet.
     *
     * @param plot Das zu scannende Plot
     * @return ScanResult mit Kisten-Statistiken
     */
    public ScanResult scanPlotChests(Plot plot, PlotStorage plotStorage) {
        // Leere vorherige Daten
        plotStorage.clear();

        // Scan durchführen
        scanPlot(plot, plotStorage);

        // Zähle Kisten nach Typ
        int inputChests = 0;
        int outputChests = 0;
        int receiverChests = 0;

        for (ChestData chest : plotStorage.getAllChests()) {
            ChestType type = chest.getChestType();
            if (type == ChestType.INPUT) {
                inputChests++;
            } else if (type == ChestType.OUTPUT) {
                outputChests++;
            } else if (chest.isReceiverChest()) {
                receiverChests++;
            }
        }

        return new ScanResult(inputChests, outputChests, receiverChests);
    }

    /**
     * Ergebnis eines Plot-Scans mit Kisten-Statistiken.
     *
     * @param inputChests Anzahl Input-Kisten
     * @param outputChests Anzahl Output-Kisten
     * @param receiverChests Anzahl Receiver-Kisten
     */
    public record ScanResult(int inputChests, int outputChests, int receiverChests) {}
}
