package de.fallenstar.plot.ui;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.ui.TextInputUi;
import de.fallenstar.plot.manager.PlotNameManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

/**
 * UI zur Eingabe eines Plot-Namens via TextInputUI (Anvil-basiert).
 *
 * Features:
 * - Namen-Eingabe via Anvil-Rename
 * - Validierung (max. 32 Zeichen, Sonderzeichen)
 * - Callback bei Erfolg/Abbruch
 *
 * **Verwendung:**
 * <pre>
 * PlotNameInputUi.openNameInput(player, plot, plotNameManager, name -> {
 *     player.sendMessage("Name gesetzt: " + name);
 * });
 * </pre>
 *
 * @author FallenStar
 * @version 2.0
 */
public class PlotNameInputUi {

    /**
     * Öffnet die Namen-Eingabe für einen Spieler.
     *
     * @param player Der Spieler
     * @param plot Der Plot
     * @param plotNameManager PlotNameManager
     * @param onSuccess Callback bei Erfolg
     */
    public static void openNameInput(
            Player player,
            Plot plot,
            PlotNameManager plotNameManager,
            Consumer<String> onSuccess
    ) {
        // Aktueller Name als Placeholder
        String currentName = plotNameManager.getPlotName(plot);
        if (currentName == null || currentName.trim().isEmpty()) {
            currentName = plot.getIdentifier();
        }

        // Erstelle TextInputUI
        TextInputUi ui = new TextInputUi(
                "Plot-Namen setzen",
                currentName,
                // Input-Handler (Bestätigung)
                name -> {
                    // Validierung
                    String error = getValidationError(name);
                    if (error != null) {
                        player.sendMessage(Component.text("§c✗ " + error));
                        player.sendMessage(Component.text("§7Versuche es erneut: /plot setname <name>", NamedTextColor.GRAY));
                        return;
                    }

                    // Namen setzen
                    plotNameManager.setPlotName(plot, name);
                    player.sendMessage(Component.text("§a✓ Plot-Name gesetzt: §e" + name));

                    // Callback
                    if (onSuccess != null) {
                        onSuccess.accept(name);
                    }
                },
                // Cancel-Handler
                p -> p.sendMessage(Component.text("§7Namen-Eingabe abgebrochen.", NamedTextColor.GRAY))
        );

        // Öffne UI
        ui.open(player);
    }

    /**
     * Validiert einen Plot-Namen.
     *
     * @param name Der zu validierende Name
     * @return true wenn gültig
     */
    public static boolean isValidName(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }

        // Max. 32 Zeichen
        if (name.length() > 32) {
            return false;
        }

        // Keine Sonderzeichen außer Leerzeichen, -, _
        return name.matches("[a-zA-Z0-9äöüÄÖÜß \\-_]+");
    }

    /**
     * Gibt eine Validierungs-Fehlermeldung zurück.
     *
     * @param name Der validierte Name
     * @return Fehlermeldung oder null wenn gültig
     */
    public static String getValidationError(String name) {
        if (name == null || name.isEmpty()) {
            return "Name darf nicht leer sein!";
        }

        if (name.length() > 32) {
            return "Name zu lang! Max. 32 Zeichen erlaubt.";
        }

        if (!name.matches("[a-zA-Z0-9äöüÄÖÜß \\-_]+")) {
            return "Name enthält ungültige Zeichen! Nur Buchstaben, Zahlen, Leerzeichen, -, _ erlaubt.";
        }

        return null;
    }
}
