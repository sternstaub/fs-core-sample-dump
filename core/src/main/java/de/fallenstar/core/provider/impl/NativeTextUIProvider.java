package de.fallenstar.core.provider.impl;

import de.fallenstar.core.exception.ProviderFunctionalityNotFoundException;
import de.fallenstar.core.provider.UIProvider;
import de.fallenstar.core.ui.UIButton;
import de.fallenstar.core.ui.UIMenu;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;

/**
 * Native Text-basierter UI Provider (Fallback).
 *
 * Diese Implementierung rendert UIs als Chat-Nachrichten mit anklickbaren Links.
 * Wird verwendet wenn kein UI-Modul geladen ist.
 *
 * Features:
 * - Chat-basierte Menüs
 * - Anklickbare Buttons als Text-Links
 * - Kein Inventory nötig
 * - Native Paper/Adventure-Integration
 *
 * Beispiel-Output:
 * ```
 * ╔════ Preisverwaltung ════╗
 * [Preis setzen] [Material wählen] [Speichern]
 * ╚═══════════════════════╝
 * ```
 *
 * @author FallenStar
 * @version 1.0
 */
public class NativeTextUIProvider implements UIProvider {

    @Override
    public boolean isAvailable() {
        return true; // Immer verfügbar (Native Fallback)
    }

    @Override
    public void showMenu(Player player, UIMenu menu)
            throws ProviderFunctionalityNotFoundException {

        if (!isAvailable()) {
            throw new ProviderFunctionalityNotFoundException(
                    "UIProvider",
                    "showMenu",
                    "Native Text UI nicht verfügbar"
            );
        }

        // Header
        player.sendMessage(Component.empty());
        player.sendMessage(createHeader(menu.getTitle()));

        // Buttons als anklickbare Links
        if (menu.hasButtons()) {
            Component buttonRow = Component.empty();
            boolean first = true;

            for (UIButton button : menu.getButtons()) {
                if (!first) {
                    buttonRow = buttonRow.append(Component.text(" "));
                }
                buttonRow = buttonRow.append(createClickableButton(button));
                first = false;
            }

            player.sendMessage(buttonRow);
        } else {
            player.sendMessage(Component.text("Keine Optionen verfügbar", NamedTextColor.GRAY));
        }

        // Footer
        player.sendMessage(createFooter());
        player.sendMessage(Component.empty());
    }

    @Override
    public String getProviderType() throws ProviderFunctionalityNotFoundException {
        return "text";
    }

    @Override
    public void closeAll(Player player) throws ProviderFunctionalityNotFoundException {
        // Text-UI hat nichts zu schließen
        // Sende stattdessen eine "UI geschlossen"-Nachricht
        player.sendMessage(Component.text("UI geschlossen", NamedTextColor.GRAY));
    }

    /**
     * Erstellt einen anklickbaren Button als Text-Component.
     *
     * @param button UIButton
     * @return Component mit Click-Event
     */
    private Component createClickableButton(UIButton button) {
        return Component.text("[" + button.label() + "]", NamedTextColor.GOLD)
                .decorate(TextDecoration.BOLD)
                .clickEvent(ClickEvent.runCommand(button.action()))
                .hoverEvent(Component.text("Klick: " + button.action(), NamedTextColor.GRAY));
    }

    /**
     * Erstellt einen Header für das Menü.
     *
     * @param title Titel
     * @return Component
     */
    private Component createHeader(String title) {
        String padding = "═".repeat(Math.max(0, (30 - title.length()) / 2));
        return Component.text("╔" + padding + " ", NamedTextColor.GOLD)
                .append(Component.text(title, NamedTextColor.YELLOW, TextDecoration.BOLD))
                .append(Component.text(" " + padding + "╗", NamedTextColor.GOLD));
    }

    /**
     * Erstellt einen Footer.
     *
     * @return Component
     */
    private Component createFooter() {
        return Component.text("╚" + "═".repeat(30) + "╝", NamedTextColor.GOLD);
    }
}
