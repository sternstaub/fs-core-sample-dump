package de.fallenstar.core.ui.manager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Manager für UI-Button Items.
 *
 * Verwaltet Special Items für UIs wie:
 * - Confirm (Grüne Wolle)
 * - Cancel (Rote Wolle)
 * - Close (Barriere)
 * - Info (Buch)
 * - Navigation (Pfeile)
 *
 * Diese Items werden in konkreten UIs verwendet (ConfirmationUI, etc.).
 *
 * @author FallenStar
 * @version 1.0
 */
public class UIButtonManager {

    /**
     * Button-Typ Enum.
     */
    public enum ButtonType {
        CONFIRM,    // Grüne Wolle
        CANCEL,     // Rote Wolle
        CLOSE,      // Barriere
        INFO,       // Buch
        NEXT,       // Pfeil rechts
        PREVIOUS,   // Pfeil links
        BACK        // Pfeil zurück
    }

    private final Map<ButtonType, ItemStack> buttonCache = new HashMap<>();

    /**
     * Initialisiert alle Button-Items.
     */
    public void initialize() {
        // Confirm Button (Grüne Wolle)
        ItemStack confirm = new ItemStack(Material.GREEN_WOOL);
        ItemMeta confirmMeta = confirm.getItemMeta();
        confirmMeta.displayName(Component.text("Bestätigen", NamedTextColor.GREEN, TextDecoration.BOLD));
        confirmMeta.lore(List.of(
                Component.text("Klicke um zu bestätigen", NamedTextColor.GRAY)
        ));
        confirm.setItemMeta(confirmMeta);
        buttonCache.put(ButtonType.CONFIRM, confirm);

        // Cancel Button (Rote Wolle)
        ItemStack cancel = new ItemStack(Material.RED_WOOL);
        ItemMeta cancelMeta = cancel.getItemMeta();
        cancelMeta.displayName(Component.text("Abbrechen", NamedTextColor.RED, TextDecoration.BOLD));
        cancelMeta.lore(List.of(
                Component.text("Klicke um abzubrechen", NamedTextColor.GRAY)
        ));
        cancel.setItemMeta(cancelMeta);
        buttonCache.put(ButtonType.CANCEL, cancel);

        // Close Button (Barriere)
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.displayName(Component.text("Schließen", NamedTextColor.RED, TextDecoration.BOLD));
        closeMeta.lore(List.of(
                Component.text("Klicke um zu schließen", NamedTextColor.GRAY)
        ));
        close.setItemMeta(closeMeta);
        buttonCache.put(ButtonType.CLOSE, close);

        // Info Button (Buch)
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.displayName(Component.text("Information", NamedTextColor.AQUA, TextDecoration.BOLD));
        infoMeta.lore(List.of(
                Component.text("Informationen zu diesem UI", NamedTextColor.GRAY)
        ));
        info.setItemMeta(infoMeta);
        buttonCache.put(ButtonType.INFO, info);

        // Next Button (Pfeil)
        ItemStack next = new ItemStack(Material.ARROW);
        ItemMeta nextMeta = next.getItemMeta();
        nextMeta.displayName(Component.text("Weiter »", NamedTextColor.YELLOW, TextDecoration.BOLD));
        nextMeta.lore(List.of(
                Component.text("Zur nächsten Seite", NamedTextColor.GRAY)
        ));
        next.setItemMeta(nextMeta);
        buttonCache.put(ButtonType.NEXT, next);

        // Previous Button (Pfeil)
        ItemStack previous = new ItemStack(Material.ARROW);
        ItemMeta previousMeta = previous.getItemMeta();
        previousMeta.displayName(Component.text("« Zurück", NamedTextColor.YELLOW, TextDecoration.BOLD));
        previousMeta.lore(List.of(
                Component.text("Zur vorherigen Seite", NamedTextColor.GRAY)
        ));
        previous.setItemMeta(previousMeta);
        buttonCache.put(ButtonType.PREVIOUS, previous);

        // Back Button (Pfeil)
        ItemStack back = new ItemStack(Material.SPECTRAL_ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.displayName(Component.text("↶ Zurück", NamedTextColor.GOLD, TextDecoration.BOLD));
        backMeta.lore(List.of(
                Component.text("Zum vorherigen Menü", NamedTextColor.GRAY)
        ));
        back.setItemMeta(backMeta);
        buttonCache.put(ButtonType.BACK, back);
    }

    /**
     * Erstellt ein Button-Item mit optionalem Custom-Text.
     *
     * @param type Button-Typ
     * @param customName Optionaler Custom-Name (null für Standard)
     * @param customLore Optionale Custom-Lore (null für Standard)
     * @return Button ItemStack
     */
    public Optional<ItemStack> createButton(ButtonType type, Component customName, List<Component> customLore) {
        if (!buttonCache.containsKey(type)) {
            return Optional.empty();
        }

        ItemStack button = buttonCache.get(type).clone();

        // Custom-Anpassungen
        if (customName != null || customLore != null) {
            ItemMeta meta = button.getItemMeta();
            if (customName != null) {
                meta.displayName(customName);
            }
            if (customLore != null) {
                meta.lore(customLore);
            }
            button.setItemMeta(meta);
        }

        return Optional.of(button);
    }

    /**
     * Erstellt ein Button-Item mit Standard-Text.
     *
     * @param type Button-Typ
     * @return Button ItemStack
     */
    public Optional<ItemStack> createButton(ButtonType type) {
        return createButton(type, null, null);
    }

    /**
     * Erstellt ein Confirm-Button.
     *
     * @return Confirm Button
     */
    public Optional<ItemStack> createConfirmButton() {
        return createButton(ButtonType.CONFIRM);
    }

    /**
     * Erstellt ein Cancel-Button.
     *
     * @return Cancel Button
     */
    public Optional<ItemStack> createCancelButton() {
        return createButton(ButtonType.CANCEL);
    }

    /**
     * Erstellt ein Close-Button.
     *
     * @return Close Button
     */
    public Optional<ItemStack> createCloseButton() {
        return createButton(ButtonType.CLOSE);
    }

    /**
     * Erstellt ein Info-Button.
     *
     * @param customLore Optionale Custom-Lore
     * @return Info Button
     */
    public Optional<ItemStack> createInfoButton(List<Component> customLore) {
        return createButton(ButtonType.INFO, null, customLore);
    }
}
