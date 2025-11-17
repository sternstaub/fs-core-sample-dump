package de.fallenstar.plot.trader;

import de.fallenstar.core.provider.Plot;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Virtuelles Inventar für Spielerhändler auf Handelsgilden.
 *
 * Features:
 * - Plot-gebunden (nicht weltbasiert)
 * - Persistent in Config gespeichert
 * - 54 Slots (LargeChest-Größe)
 * - Verwaltung via /plot gui → "Händler-Inventar"
 *
 * **Speicherung:**
 * - Plots-Modul Config (trader-inventories.yml)
 * - Serialisierung: ItemStack → Base64 → Config
 *
 * **Verwendung:**
 * <pre>
 * VirtualTraderInventory inv = new VirtualTraderInventory(player.getUniqueId(), plot, logger);
 * inv.loadFromConfig(config);
 * inv.open(player);
 * inv.saveToConfig(config);
 * </pre>
 *
 * @author FallenStar
 * @version 1.0
 */
public class VirtualTraderInventory {

    private static final int INVENTORY_SIZE = 54;  // LargeChest

    private final UUID playerId;
    private final Plot plot;
    private final Logger logger;
    private ItemStack[] contents;

    /**
     * Konstruktor für VirtualTraderInventory.
     *
     * @param playerId UUID des Spielers (Händler-Besitzer)
     * @param plot Der Plot (Handelsgilde)
     * @param logger Logger
     */
    public VirtualTraderInventory(UUID playerId, Plot plot, Logger logger) {
        this.playerId = playerId;
        this.plot = plot;
        this.logger = logger;
        this.contents = new ItemStack[INVENTORY_SIZE];
    }

    /**
     * Lädt Inventar aus Config.
     *
     * @param config FileConfiguration
     */
    public void loadFromConfig(FileConfiguration config) {
        String key = getConfigKey();
        String base64 = config.getString(key);

        if (base64 == null || base64.isEmpty()) {
            logger.fine("Kein Händler-Inventar gefunden für " + playerId + " auf " + plot.getPlotId());
            return;
        }

        try {
            // Decode Base64
            byte[] data = Base64Coder.decodeLines(base64);

            // Deserialisiere ItemStacks
            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
                 BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {

                ItemStack[] loaded = new ItemStack[INVENTORY_SIZE];

                for (int i = 0; i < INVENTORY_SIZE; i++) {
                    loaded[i] = (ItemStack) dataInput.readObject();
                }

                this.contents = loaded;
                logger.fine("Händler-Inventar geladen für " + playerId);
            }

        } catch (Exception e) {
            logger.warning("Fehler beim Laden des Händler-Inventars für " + playerId + ": " + e.getMessage());
        }
    }

    /**
     * Speichert Inventar in Config.
     *
     * @param config FileConfiguration
     */
    public void saveToConfig(FileConfiguration config) {
        String key = getConfigKey();

        try {
            // Serialisiere ItemStacks
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            for (int i = 0; i < INVENTORY_SIZE; i++) {
                dataOutput.writeObject(contents[i]);
            }

            dataOutput.close();

            // Encode Base64
            String base64 = Base64Coder.encodeLines(outputStream.toByteArray());

            // Speichere in Config
            config.set(key, base64);

            logger.fine("Händler-Inventar gespeichert für " + playerId);

        } catch (Exception e) {
            logger.warning("Fehler beim Speichern des Händler-Inventars für " + playerId + ": " + e.getMessage());
        }
    }

    /**
     * Öffnet Inventar für Spieler (Bearbeitung).
     *
     * @param player Der Spieler
     */
    public void open(Player player) {
        if (!player.getUniqueId().equals(playerId)) {
            player.sendMessage("§cDu darfst dieses Inventar nicht bearbeiten!");
            return;
        }

        // Erstelle Bukkit-Inventar
        Inventory inventory = Bukkit.createInventory(null, INVENTORY_SIZE, "§6§lHändler-Inventar");

        // Fülle Inventar mit gespeicherten Items
        inventory.setContents(contents);

        // Öffne Inventar
        player.openInventory(inventory);

        player.sendMessage("§a✓ Händler-Inventar geöffnet");
        player.sendMessage("§7Schließe das Inventar um zu speichern");
    }

    /**
     * Aktualisiert Inventar-Inhalte (z.B. nach Schließen).
     *
     * @param inventory Das Bukkit-Inventar
     */
    public void updateContents(Inventory inventory) {
        this.contents = inventory.getContents();
        logger.fine("Händler-Inventar aktualisiert für " + playerId);
    }

    /**
     * Gibt Items zurück.
     *
     * @return Geklontes ItemStack-Array
     */
    public ItemStack[] getContents() {
        ItemStack[] cloned = new ItemStack[INVENTORY_SIZE];
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            cloned[i] = contents[i] != null ? contents[i].clone() : null;
        }
        return cloned;
    }

    /**
     * Setzt Items.
     *
     * @param contents Neues ItemStack-Array (wird geklont)
     */
    public void setContents(ItemStack[] contents) {
        if (contents.length != INVENTORY_SIZE) {
            throw new IllegalArgumentException("Inventar muss " + INVENTORY_SIZE + " Slots haben!");
        }

        for (int i = 0; i < INVENTORY_SIZE; i++) {
            this.contents[i] = contents[i] != null ? contents[i].clone() : null;
        }
    }

    /**
     * Gibt die Player-ID zurück.
     *
     * @return UUID des Besitzers
     */
    public UUID getPlayerId() {
        return playerId;
    }

    /**
     * Gibt den Plot zurück.
     *
     * @return Der Plot
     */
    public Plot getPlot() {
        return plot;
    }

    /**
     * Gibt den Config-Key zurück.
     *
     * Format: "trader-inventories.<plot-id>.<player-id>"
     *
     * @return Config-Key
     */
    private String getConfigKey() {
        return "trader-inventories." + plot.getPlotId() + "." + playerId.toString();
    }

    @Override
    public String toString() {
        return "VirtualTraderInventory{" +
                "player=" + playerId +
                ", plot=" + plot.getPlotId() +
                ", slots=" + INVENTORY_SIZE +
                '}';
    }
}
