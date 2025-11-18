package de.fallenstar.core.ui;

import de.fallenstar.core.ui.manager.UIButtonManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.function.Consumer;

/**
 * Generisches Ja/Nein Bestätigungs-UI.
 *
 * Layout (9 Slots):
 * [ ] [ ] [ ] [Ja] [ ] [Nein] [ ] [ ] [X]
 *  0   1   2    3   4    5    6   7   8
 *
 * Features:
 * - Grüne Wolle (Ja) - Slot 3
 * - Rote Wolle (Nein) - Slot 5
 * - Barriere (Schließen) - Slot 8
 *
 * @author FallenStar
 * @version 1.0
 */
public class ConfirmationUI extends SmallChestUI {

    private final UIButtonManager buttonManager;
    private final String message;
    private final Consumer<Player> onConfirm;
    private final Consumer<Player> onCancel;

    /**
     * Konstruktor für ConfirmationUI.
     *
     * @param plugin Plugin-Instanz
     * @param buttonManager UIButtonManager-Instanz
     * @param title UI-Titel
     * @param message Bestätigungs-Message
     * @param onConfirm Callback bei Bestätigung
     * @param onCancel Callback bei Abbruch
     */
    public ConfirmationUI(Plugin plugin, UIButtonManager buttonManager, String title, String message,
                          Consumer<Player> onConfirm, Consumer<Player> onCancel) {
        super(title);
        this.buttonManager = buttonManager;
        this.message = message;
        this.onConfirm = onConfirm;
        this.onCancel = onCancel;
    }

    @Override
    public void open(Player player) {
        clearItems();

        // Message-Item (Slot 1)
        ItemStack messageItem = new ItemStack(Material.PAPER);
        ItemMeta messageMeta = messageItem.getItemMeta();
        messageMeta.displayName(Component.text("Bestätigung erforderlich", NamedTextColor.GOLD));
        messageMeta.lore(List.of(
                Component.empty(),
                Component.text(message, NamedTextColor.WHITE),
                Component.empty(),
                Component.text("Wähle eine Option:", NamedTextColor.GRAY)
        ));
        messageItem.setItemMeta(messageMeta);
        setItem(1, messageItem, null);

        // Ja-Button (Grüne Wolle, Slot 3)
        buttonManager.createConfirmButton().ifPresent(confirmButton -> {
            setItem(3, confirmButton, player1 -> {
                if (onConfirm != null) {
                    onConfirm.accept(player1);
                }
                close(player1);
            });
        });

        // Nein-Button (Rote Wolle, Slot 5)
        buttonManager.createCancelButton().ifPresent(cancelButton -> {
            setItem(5, cancelButton, player1 -> {
                if (onCancel != null) {
                    onCancel.accept(player1);
                }
                close(player1);
            });
        });

        // Schließen-Button (Barriere, Slot 8 - oben rechts)
        buttonManager.createCloseButton().ifPresent(closeButton -> {
            setItem(8, closeButton, this::close);
        });

        super.open(player);
    }

    /**
     * Factory-Methode für einfache Ja/Nein-Bestätigungen.
     *
     * @param plugin Plugin-Instanz
     * @param buttonManager UIButtonManager
     * @param message Bestätigungs-Message
     * @param onConfirm Callback bei Ja
     * @return ConfirmationUI
     */
    public static ConfirmationUI createSimple(Plugin plugin, UIButtonManager buttonManager, String message,
                                               Consumer<Player> onConfirm) {
        return new ConfirmationUI(
                plugin,
                buttonManager,
                "§eBestätigung",
                message,
                onConfirm,
                player -> player.sendMessage(Component.text("Abgebrochen.", NamedTextColor.YELLOW))
        );
    }

    /**
     * Factory-Methode für Ja/Nein-Bestätigungen mit Cancel-Callback.
     *
     * @param plugin Plugin-Instanz
     * @param buttonManager UIButtonManager
     * @param message Bestätigungs-Message
     * @param onConfirm Callback bei Ja
     * @param onCancel Callback bei Nein
     * @return ConfirmationUI
     */
    public static ConfirmationUI createWithCallbacks(Plugin plugin, UIButtonManager buttonManager, String message,
                                                      Consumer<Player> onConfirm, Consumer<Player> onCancel) {
        return new ConfirmationUI(
                plugin,
                buttonManager,
                "§eBestätigung",
                message,
                onConfirm,
                onCancel
        );
    }
}
