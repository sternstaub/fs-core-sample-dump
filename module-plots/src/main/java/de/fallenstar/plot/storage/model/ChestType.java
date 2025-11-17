package de.fallenstar.plot.storage.model;

/**
 * Typ einer Truhe im Plot-Storage-System.
 *
 * Truhen können verschiedene Rollen haben:
 * - INPUT: Empfangskiste für gekaufte Items
 * - OUTPUT: Verkaufskiste für zum Verkauf angebotene Items
 * - STORAGE: Normale Storage-Truhe
 *
 * @author FallenStar
 * @version 1.0
 */
public enum ChestType {

    /**
     * Input-Chest (Empfangskiste).
     *
     * Verwendung:
     * - Gekaufte Items werden hier hinzugefügt
     * - Automatisches Handling bei Kauftransaktionen
     * - Nur eine Input-Chest pro Plot empfohlen
     */
    INPUT("Input", "Empfangskiste"),

    /**
     * Output-Chest (Verkaufskiste).
     *
     * Verwendung:
     * - Items werden von hier gescannt für Verkauf
     * - Händler lesen aus Output-Chests
     * - Mehrere Output-Chests pro Plot möglich
     */
    OUTPUT("Output", "Verkaufskiste"),

    /**
     * Storage-Chest (Normale Kiste).
     *
     * Verwendung:
     * - Allgemeiner Storage
     * - Nicht automatisch für Handel verwendet
     * - Standard-Typ für neue Truhen
     */
    STORAGE("Storage", "Normale Kiste");

    private final String name;
    private final String displayName;

    ChestType(String name, String displayName) {
        this.name = name;
        this.displayName = displayName;
    }

    /**
     * @return Technischer Name
     */
    public String getName() {
        return name;
    }

    /**
     * @return Anzeigename (Deutsch)
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Konvertiert von String zu ChestType.
     *
     * @param name Name des Typs
     * @return ChestType oder STORAGE als Default
     */
    public static ChestType fromString(String name) {
        if (name == null) {
            return STORAGE;
        }

        for (ChestType type : values()) {
            if (type.name.equalsIgnoreCase(name) || type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }

        return STORAGE; // Default
    }
}
