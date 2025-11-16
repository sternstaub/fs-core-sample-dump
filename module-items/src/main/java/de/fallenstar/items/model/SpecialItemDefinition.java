package de.fallenstar.items.model;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;

import java.util.List;

/**
 * Definition für ein SpecialItem (Vanilla oder MMOItems).
 *
 * SpecialItems sind Custom-Items, die vom FallenStar-System
 * verwaltet werden. Sie können entweder Vanilla-Items mit
 * Custom Model Data + PDC sein, oder MMOItems-Custom-Items.
 *
 * Verwendung:
 * - Vanilla: Währungen (Bronzestern, Silberstern, Goldstern)
 * - Vanilla: Quest-Items, Event-Items, etc.
 * - MMOItems: Waffen, Rüstungen, Tools (wenn MMOItems geladen)
 *
 * @author FallenStar
 * @version 3.0 (Generisches SpecialItem-System)
 */
public record SpecialItemDefinition(
        String id,                      // Eindeutige ID (z.B. "bronze_stern", "quest_compass")
        ItemType type,                  // VANILLA oder MMO_ITEM
        // Vanilla-spezifisch:
        Material material,              // Material für Vanilla-Items
        int customModelData,            // Custom Model Data für Texturpacks
        Component displayName,          // Display-Name des Items
        List<Component> lore,           // Lore (Beschreibung)
        // MMOItems-spezifisch:
        String mmoType,                 // MMOItems Type (z.B. "SWORD", "ARMOR")
        String mmoId                    // MMOItems ID (z.B. "LEGENDARY_BLADE")
) {

    /**
     * Item-Typ Enum.
     */
    public enum ItemType {
        /**
         * Vanilla Minecraft Item mit Custom Model Data + PDC.
         */
        VANILLA,

        /**
         * MMOItems Custom-Item (erfordert MMOItems Plugin).
         */
        MMO_ITEM
    }

    /**
     * Erstellt eine Vanilla SpecialItem Definition.
     *
     * @param id Eindeutige ID
     * @param material Vanilla Material
     * @param customModelData Custom Model Data
     * @param displayName Display-Name
     * @param lore Lore
     * @return SpecialItemDefinition
     */
    public static SpecialItemDefinition createVanilla(String id, Material material, int customModelData,
                                                       Component displayName, List<Component> lore) {
        return new SpecialItemDefinition(
                id,
                ItemType.VANILLA,
                material,
                customModelData,
                displayName,
                lore,
                null,  // kein MMO-Type
                null   // keine MMO-ID
        );
    }

    /**
     * Erstellt eine MMOItems SpecialItem Definition.
     *
     * @param id Eindeutige ID
     * @param mmoType MMOItems Type
     * @param mmoId MMOItems ID
     * @return SpecialItemDefinition
     */
    public static SpecialItemDefinition createMMOItem(String id, String mmoType, String mmoId) {
        return new SpecialItemDefinition(
                id,
                ItemType.MMO_ITEM,
                null,  // kein Vanilla-Material
                0,     // keine CMD
                null,  // kein Display-Name (kommt von MMOItems)
                null,  // keine Lore (kommt von MMOItems)
                mmoType,
                mmoId
        );
    }

    /**
     * Prüft ob diese Definition ein Vanilla-Item ist.
     *
     * @return true wenn VANILLA
     */
    public boolean isVanilla() {
        return type == ItemType.VANILLA;
    }

    /**
     * Prüft ob diese Definition ein MMOItem ist.
     *
     * @return true wenn MMO_ITEM
     */
    public boolean isMMOItem() {
        return type == ItemType.MMO_ITEM;
    }
}
