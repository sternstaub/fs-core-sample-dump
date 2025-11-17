package de.fallenstar.plot.npc;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

/**
 * Verwaltet Skin-Pools für verschiedene NPC-Typen.
 *
 * Features:
 * - Admin setzt Skin-Pool pro NPC-Typ
 * - Zufällige Skin-Auswahl bei NPC-Erstellung
 * - Skin-Rotation (optional)
 * - Persistent in Config
 *
 * **Verwendung:**
 * <pre>
 * skinPool.addSkin(NPCType.TRADER, "Notch");
 * String randomSkin = skinPool.getRandomSkin(NPCType.TRADER);
 * </pre>
 *
 * **Integration bei NPC-Erstellung:**
 * <pre>
 * String skin = skinPool.getRandomSkin(NPCType.TRADER);
 * NPC npc = npcRegistry.createNPC(EntityType.PLAYER, "Händler");
 * npc.data().set(NPC.PLAYER_SKIN_UUID_METADATA, skin);
 * </pre>
 *
 * @author FallenStar
 * @version 1.0
 */
public class NPCSkinPool {

    private final Logger logger;
    private final Map<NPCType, List<String>> skinPools;

    /**
     * NPC-Typen für verschiedene Verwendungszwecke.
     */
    public enum NPCType {
        /**
         * Händler - Gildenhändler, Spielerhändler.
         */
        TRADER("Händler"),

        /**
         * Bankier - Lokale Bankiers, Weltbankier.
         */
        BANKER("Bankier"),

        /**
         * Botschafter - Schnellreise-NPCs.
         */
        AMBASSADOR("Botschafter"),

        /**
         * Handwerker - Schmiede, Schneider, etc.
         */
        CRAFTSMAN("Handwerker"),

        /**
         * Fahrende Händler - Zufällige Händler.
         */
        TRAVELING("Fahrender Händler");

        private final String displayName;

        NPCType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Konstruktor für NPCSkinPool.
     *
     * @param logger Logger
     */
    public NPCSkinPool(Logger logger) {
        this.logger = logger;
        this.skinPools = new ConcurrentHashMap<>();

        // Initialisiere Maps für alle Typen
        for (NPCType type : NPCType.values()) {
            skinPools.put(type, new ArrayList<>());
        }

        logger.info("NPCSkinPool initialisiert");
    }

    /**
     * Fügt Skin zu Pool hinzu.
     *
     * @param type NPC-Typ
     * @param playerName Spielername oder Skin-UUID
     * @return true wenn erfolgreich hinzugefügt
     */
    public boolean addSkin(NPCType type, String playerName) {
        if (type == null || playerName == null || playerName.isEmpty()) {
            logger.warning("Ungültiger Skin: " + playerName + " für " + type);
            return false;
        }

        List<String> skins = skinPools.get(type);

        if (skins.contains(playerName)) {
            logger.fine("Skin bereits vorhanden: " + playerName + " für " + type.getDisplayName());
            return false;
        }

        skins.add(playerName);
        logger.info("Skin hinzugefügt: " + playerName + " für " + type.getDisplayName());
        return true;
    }

    /**
     * Entfernt Skin aus Pool.
     *
     * @param type NPC-Typ
     * @param playerName Spielername oder Skin-UUID
     * @return true wenn erfolgreich entfernt
     */
    public boolean removeSkin(NPCType type, String playerName) {
        if (type == null || playerName == null) {
            return false;
        }

        List<String> skins = skinPools.get(type);
        boolean removed = skins.remove(playerName);

        if (removed) {
            logger.info("Skin entfernt: " + playerName + " von " + type.getDisplayName());
        }

        return removed;
    }

    /**
     * Gibt zufälligen Skin zurück.
     *
     * @param type NPC-Typ
     * @return Spielername oder Skin-UUID (oder Fallback)
     */
    public String getRandomSkin(NPCType type) {
        if (type == null) {
            return getFallbackSkin();
        }

        List<String> skins = skinPools.get(type);

        if (skins.isEmpty()) {
            logger.warning("Kein Skin für " + type.getDisplayName() + " vorhanden - nutze Fallback");
            return getFallbackSkin();
        }

        // Wähle zufälligen Skin
        int randomIndex = ThreadLocalRandom.current().nextInt(skins.size());
        return skins.get(randomIndex);
    }

    /**
     * Gibt alle Skins für einen Typ zurück.
     *
     * @param type NPC-Typ
     * @return Liste von Skins
     */
    public List<String> getSkins(NPCType type) {
        if (type == null) {
            return Collections.emptyList();
        }

        return new ArrayList<>(skinPools.getOrDefault(type, Collections.emptyList()));
    }

    /**
     * Gibt die Anzahl Skins für einen Typ zurück.
     *
     * @param type NPC-Typ
     * @return Anzahl Skins
     */
    public int getSkinCount(NPCType type) {
        if (type == null) {
            return 0;
        }

        return skinPools.getOrDefault(type, Collections.emptyList()).size();
    }

    /**
     * Gibt den Fallback-Skin zurück.
     *
     * @return Default-Skin (MHF_Villager)
     */
    private String getFallbackSkin() {
        return "MHF_Villager";  // Minecraft Heads Format
    }

    /**
     * Lädt Skins aus Config.
     *
     * @param config FileConfiguration
     */
    public void loadFromConfig(FileConfiguration config) {
        ConfigurationSection section = config.getConfigurationSection("skin-pools");
        if (section == null) {
            logger.warning("Keine Skin-Pools in Config gefunden - nutze Defaults");
            initializeDefaults();
            return;
        }

        int loadedCount = 0;

        for (NPCType type : NPCType.values()) {
            List<String> skins = section.getStringList(type.name());
            List<String> pool = skinPools.get(type);

            pool.clear();
            pool.addAll(skins);

            loadedCount += skins.size();
        }

        logger.info("Skin-Pools geladen: " + loadedCount + " Skins");
    }

    /**
     * Speichert Skins in Config.
     *
     * @param config FileConfiguration
     */
    public void saveToConfig(FileConfiguration config) {
        // Lösche alte Daten
        config.set("skin-pools", null);

        // Speichere alle Skin-Pools
        for (Map.Entry<NPCType, List<String>> entry : skinPools.entrySet()) {
            NPCType type = entry.getKey();
            List<String> skins = entry.getValue();

            config.set("skin-pools." + type.name(), skins);
        }

        logger.info("Skin-Pools gespeichert: " + getTotalSkinCount() + " Skins");
    }

    /**
     * Initialisiert Default-Skins.
     */
    private void initializeDefaults() {
        // Händler-Skins
        addSkin(NPCType.TRADER, "MHF_Villager");
        addSkin(NPCType.TRADER, "MHF_Alex");
        addSkin(NPCType.TRADER, "MHF_Steve");

        // Bankier-Skins
        addSkin(NPCType.BANKER, "MHF_Villager");
        addSkin(NPCType.BANKER, "Notch");

        // Botschafter-Skins
        addSkin(NPCType.AMBASSADOR, "jeb_");
        addSkin(NPCType.AMBASSADOR, "Dinnerbone");

        // Handwerker-Skins
        addSkin(NPCType.CRAFTSMAN, "MHF_ArrowUp");
        addSkin(NPCType.CRAFTSMAN, "MHF_ArrowDown");

        // Fahrende Händler
        addSkin(NPCType.TRAVELING, "MHF_Villager");

        logger.info("Default-Skins initialisiert");
    }

    /**
     * Gibt die Gesamtanzahl Skins zurück.
     *
     * @return Anzahl Skins (über alle Typen)
     */
    public int getTotalSkinCount() {
        return skinPools.values().stream()
                .mapToInt(List::size)
                .sum();
    }

    /**
     * Gibt Debug-Informationen zurück.
     *
     * @return Liste von Debug-Strings
     */
    public List<String> getDebugInfo() {
        List<String> info = new ArrayList<>();
        info.add("§e§lNPCSkinPool Debug-Info:");
        info.add("");

        for (NPCType type : NPCType.values()) {
            int count = getSkinCount(type);
            info.add("§6" + type.getDisplayName() + ": §7" + count + " Skins");

            if (count > 0) {
                List<String> skins = getSkins(type);
                for (String skin : skins) {
                    info.add("  §8- " + skin);
                }
            }
        }

        info.add("");
        info.add("§eGesamt: §7" + getTotalSkinCount() + " Skins");

        return info;
    }
}
