package de.fallenstar.ui.ui;

import de.fallenstar.core.ui.BaseUI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * AnvilUI - Text-Input via Anvil-Interface.
 *
 * **HINWEIS:** Vollständige Anvil-Interface-Implementierung erfordert:
 * - ProtocolLib für Packet-Manipulation ODER
 * - AnvilGUI-Bibliothek (https://github.com/WesJD/AnvilGUI) ODER
 * - NMS-Reflection für direkten Zugriff
 *
 * **Aktueller Workaround:** Chat-basierte Eingabe
 * - Spieler schließt GUI
 * - Gibt Text im Chat ein
 * - Callback wird aufgerufen
 *
 * **TODO (zukünftiger Sprint):**
 * - AnvilGUI-Bibliothek integrieren
 * - Echtes Anvil-Interface implementieren
 * - Echtzeit-Vorschau
 *
 * **Verwendung:**
 * <pre>
 * AnvilUI anvilUI = new AnvilUI(
 *     "Gib einen Namen ein:",
 *     "Standardwert",
 *     input -> {
 *         player.sendMessage("Du hast eingegeben: " + input);
 *     }
 * );
 * anvilUI.open(player);
 * </pre>
 *
 * @author FallenStar
 * @version 1.0 (Chat-basierter Workaround)
 */
public class AnvilUI extends BaseUI implements Listener {

    private static final Map<UUID, AnvilUI> activeInputs = new HashMap<>();

    private final String prompt;
    private final String defaultValue;
    private final Consumer<String> onInput;
    private final Consumer<Player> onCancel;

    /**
     * Erstellt eine AnvilUI.
     *
     * @param prompt Eingabe-Aufforderung
     * @param defaultValue Standardwert (kann leer sein)
     * @param onInput Callback bei Eingabe
     */
    public AnvilUI(String prompt, String defaultValue, Consumer<String> onInput) {
        this(prompt, defaultValue, onInput, null);
    }

    /**
     * Erstellt eine AnvilUI mit Cancel-Handler.
     *
     * @param prompt Eingabe-Aufforderung
     * @param defaultValue Standardwert (kann leer sein)
     * @param onInput Callback bei Eingabe
     * @param onCancel Callback bei Abbruch
     */
    public AnvilUI(String prompt, String defaultValue, Consumer<String> onInput, Consumer<Player> onCancel) {
        super("Text-Eingabe"); // Titel wird nicht verwendet (Chat-basiert)
        this.prompt = prompt;
        this.defaultValue = defaultValue;
        this.onInput = onInput;
        this.onCancel = onCancel;
    }

    @Override
    public void open(Player player) {
        // Event-Listener registrieren
        if (getPlugin() != null) {
            Bukkit.getPluginManager().registerEvents(this, getPlugin());
        }

        // Spieler in Map speichern
        activeInputs.put(player.getUniqueId(), this);

        // Chat-basierte Eingabe (Workaround)
        player.sendMessage("§8§m                                                    ");
        player.sendMessage("");
        player.sendMessage("§e§l" + prompt);

        if (defaultValue != null && !defaultValue.isEmpty()) {
            player.sendMessage("§7Aktuell: §f" + defaultValue);
        }

        player.sendMessage("");
        player.sendMessage("§7Gib den Text im Chat ein.");
        player.sendMessage("§7Schreibe §c'cancel'§7 zum Abbrechen.");
        player.sendMessage("");
        player.sendMessage("§8§m                                                    ");
    }

    @Override
    public void close(Player player) {
        activeInputs.remove(player.getUniqueId());
        org.bukkit.event.HandlerList.unregisterAll(this);
    }

    /**
     * Event-Handler für Chat-Eingabe.
     */
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        AnvilUI ui = activeInputs.get(player.getUniqueId());

        if (ui == null || ui != this) {
            return;
        }

        // Event abbrechen (Chat-Nachricht nicht senden)
        event.setCancelled(true);

        String input = event.getMessage().trim();

        // Sync zum Main-Thread (Chat-Event ist async!)
        Bukkit.getScheduler().runTask(getPlugin(), () -> {
            if (input.equalsIgnoreCase("cancel")) {
                // Abbruch
                player.sendMessage("§cEingabe abgebrochen.");

                if (onCancel != null) {
                    onCancel.accept(player);
                }

                close(player);
            } else {
                // Eingabe
                onInput.accept(input);
                close(player);
            }
        });
    }

    /**
     * Factory-Methode für einfache Text-Eingabe.
     *
     * @param prompt Eingabe-Aufforderung
     * @param onInput Callback bei Eingabe
     * @return AnvilUI-Instanz
     */
    public static AnvilUI createSimple(String prompt, Consumer<String> onInput) {
        return new AnvilUI(prompt, "", onInput);
    }

    /**
     * Factory-Methode mit Standardwert.
     *
     * @param prompt Eingabe-Aufforderung
     * @param defaultValue Standardwert
     * @param onInput Callback bei Eingabe
     * @return AnvilUI-Instanz
     */
    public static AnvilUI createWithDefault(String prompt, String defaultValue, Consumer<String> onInput) {
        return new AnvilUI(prompt, defaultValue, onInput);
    }

    /**
     * Gibt die aktive AnvilUI für einen Spieler zurück.
     *
     * @param player Spieler
     * @return Aktive AnvilUI oder null
     */
    public static AnvilUI getActiveInput(Player player) {
        return activeInputs.get(player.getUniqueId());
    }
}
