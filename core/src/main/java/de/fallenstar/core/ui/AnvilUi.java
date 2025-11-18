package de.fallenstar.core.ui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

/**
 * Anvil UI - Text-Input über Anvil-Rename.
 *
 * Verwendet für:
 * - Single-Line Text-Eingaben
 * - Suchfelder
 * - Benennungen
 *
 * Der Spieler öffnet ein virtuelles Anvil-Fenster und kann
 * den Namen eines Items bearbeiten. Der eingegebene Text wird
 * an den Handler übergeben.
 *
 * Hinweis: Die tatsächliche Implementierung erfolgt im UI-Modul,
 * da hier plattform-spezifische Code benötigt wird.
 *
 * @author FallenStar
 * @version 1.0
 */
public abstract class AnvilUi extends BaseUi {

    protected ItemStack inputItem;
    protected Consumer<String> inputHandler;

    /**
     * Konstruktor für AnvilUi.
     *
     * @param title Titel des Anvil-UI
     */
    public AnvilUi(String title) {
        super(title);
    }

    /**
     * Setzt das Item für den ersten Slot des Anvil.
     *
     * @param item Item das umbenannt werden soll
     */
    public void setInputItem(ItemStack item) {
        this.inputItem = item;
    }

    /**
     * Setzt den Handler für die Text-Eingabe.
     *
     * @param handler Handler-Funktion die mit dem eingegebenen Text aufgerufen wird
     */
    public void setInputHandler(Consumer<String> handler) {
        this.inputHandler = handler;
    }

    /**
     * Öffnet das Anvil-UI für einen Spieler.
     *
     * @param player Spieler für den das UI geöffnet wird
     */
    @Override
    public abstract void open(Player player);

    /**
     * Schließt das Anvil-UI für einen Spieler.
     *
     * @param player Spieler für den das UI geschlossen wird
     */
    @Override
    public void close(Player player) {
        player.closeInventory();
    }

    /**
     * Behandelt die Eingabe des Spielers.
     *
     * @param player Spieler der die Eingabe gemacht hat
     * @param text Eingegebener Text
     */
    protected void handleInput(Player player, String text) {
        if (inputHandler != null) {
            inputHandler.accept(text);
        }
    }
}
