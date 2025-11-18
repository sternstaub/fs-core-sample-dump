package de.fallenstar.core.interaction.action;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;

/**
 * Informationen über eine UI-Aktion.
 *
 * Definiert wie ein Button im UI aussieht und sich verhält.
 *
 * Features:
 * - ID für Ausführung
 * - Display-Name und Lore
 * - Icon (Material oder ItemStack)
 * - Permission-Check
 * - Slot-Position (optional)
 *
 * **Verwendung:**
 * <pre>
 * var action = UiActionInfo.builder()
 *     .id("manage_storage")
 *     .displayName("§aLager verwalten")
 *     .lore(List.of("§7Klicke um das Lager zu öffnen"))
 *     .icon(Material.CHEST)
 *     .requiredPermission("fallenstar.plot.storage")
 *     .slot(10)
 *     .build();
 * </pre>
 *
 * @param id Eindeutige Aktion-ID
 * @param displayName Anzeigename
 * @param lore Beschreibung (mehrere Zeilen)
 * @param icon Material oder ItemStack
 * @param requiredPermission Optional: Benötigte Permission
 * @param slot Optional: Slot-Position im UI
 *
 * @author FallenStar
 * @version 1.0
 */
public record UiActionInfo(
        String id,
        String displayName,
        List<String> lore,
        Object icon, // Material oder ItemStack
        Optional<String> requiredPermission,
        Optional<Integer> slot
) {

    /**
     * Erstellt einen Builder für UiActionInfo.
     *
     * @return Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Prüft ob das Icon ein Material ist.
     *
     * @return true wenn Material
     */
    public boolean hasIconMaterial() {
        return icon instanceof Material;
    }

    /**
     * Prüft ob das Icon ein ItemStack ist.
     *
     * @return true wenn ItemStack
     */
    public boolean hasIconItemStack() {
        return icon instanceof ItemStack;
    }

    /**
     * Gibt das Icon als Material zurück.
     *
     * @return Material oder null
     */
    public Material getIconMaterial() {
        return hasIconMaterial() ? (Material) icon : null;
    }

    /**
     * Gibt das Icon als ItemStack zurück.
     *
     * @return ItemStack oder null
     */
    public ItemStack getIconItemStack() {
        return hasIconItemStack() ? (ItemStack) icon : null;
    }

    /**
     * Builder für UiActionInfo.
     */
    public static class Builder {
        private String id;
        private String displayName;
        private List<String> lore = List.of();
        private Object icon;
        private String requiredPermission;
        private Integer slot;

        /**
         * Setzt die Aktion-ID.
         *
         * @param id Aktion-ID
         * @return Builder
         */
        public Builder id(String id) {
            this.id = id;
            return this;
        }

        /**
         * Setzt den Anzeige-Namen.
         *
         * @param displayName Anzeige-Name
         * @return Builder
         */
        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        /**
         * Setzt die Lore.
         *
         * @param lore Lore-Zeilen
         * @return Builder
         */
        public Builder lore(List<String> lore) {
            this.lore = lore;
            return this;
        }

        /**
         * Setzt das Icon als Material.
         *
         * @param material Material
         * @return Builder
         */
        public Builder icon(Material material) {
            this.icon = material;
            return this;
        }

        /**
         * Setzt das Icon als ItemStack.
         *
         * @param itemStack ItemStack
         * @return Builder
         */
        public Builder icon(ItemStack itemStack) {
            this.icon = itemStack;
            return this;
        }

        /**
         * Setzt die benötigte Permission.
         *
         * @param permission Permission
         * @return Builder
         */
        public Builder requiredPermission(String permission) {
            this.requiredPermission = permission;
            return this;
        }

        /**
         * Setzt die Slot-Position.
         *
         * @param slot Slot
         * @return Builder
         */
        public Builder slot(int slot) {
            this.slot = slot;
            return this;
        }

        /**
         * Baut UiActionInfo.
         *
         * @return UiActionInfo
         * @throws IllegalStateException wenn Pflichtfelder fehlen
         */
        public UiActionInfo build() {
            if (id == null || displayName == null || icon == null) {
                throw new IllegalStateException("id, displayName und icon sind Pflichtfelder");
            }

            return new UiActionInfo(
                    id,
                    displayName,
                    lore,
                    icon,
                    Optional.ofNullable(requiredPermission),
                    Optional.ofNullable(slot)
            );
        }
    }
}
