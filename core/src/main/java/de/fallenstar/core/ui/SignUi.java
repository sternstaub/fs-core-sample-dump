package de.fallenstar.core.ui;

import org.bukkit.entity.Player;

import java.util.function.Consumer;

/**
 * Sign UI - Text-Input über Sign-Editor.
 *
 * Verwendet für:
 * - Kurze Text-Eingaben (4 Zeilen)
 * - Namen-Eingabe
 * - Einfache Formulare
 *
 * Der Spieler öffnet ein virtuelles Schild zum Bearbeiten.
 * Nach Bestätigung wird der Text an den Handler übergeben.
 *
 * Hinweis: Die tatsächliche Implementierung erfolgt im UI-Modul,
 * da hier plattform-spezifische NMS/Reflection-Code benötigt wird.
 *
 * @author FallenStar
 * @version 1.0
 */
public abstract class SignUi extends BaseUi {

    protected Consumer<String[]> inputHandler;

    /**
     * Konstruktor für SignUi.
     *
     * @param title Titel/Prompt für das Sign
     */
    public SignUi(String title) {
        super(title);
    }

    /**
     * Setzt den Handler für die Text-Eingabe.
     *
     * @param handler Handler-Funktion die mit 4 Zeilen Text aufgerufen wird
     */
    public void setInputHandler(Consumer<String[]> handler) {
        this.inputHandler = handler;
    }

    /**
     * Öffnet das Sign-UI für einen Spieler.
     *
     * @param player Spieler für den das UI geöffnet wird
     */
    @Override
    public abstract void open(Player player);

    /**
     * Schließt das Sign-UI für einen Spieler.
     *
     * @param player Spieler für den das UI geschlossen wird
     */
    @Override
    public void close(Player player) {
        // Sign-UI wird automatisch geschlossen
    }

    /**
     * Behandelt die Eingabe des Spielers.
     *
     * @param player Spieler der die Eingabe gemacht hat
     * @param lines 4 Zeilen Text vom Schild
     */
    protected void handleInput(Player player, String[] lines) {
        if (inputHandler != null) {
            inputHandler.accept(lines);
        }
    }
}
