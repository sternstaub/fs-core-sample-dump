package de.fallenstar.plot.listener;

import de.fallenstar.core.provider.UIProvider;
import de.fallenstar.core.registry.ProviderRegistry;
import de.fallenstar.plot.command.PlotPriceCommand;
import de.fallenstar.plot.gui.PriceEditorContext;
import de.fallenstar.plot.gui.PriceEditorUIBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Listener für Preis-Setzungs-Interaktionen.
 *
 * Hört auf Rechtsklicks wenn der Spieler im Preis-Setzungs-Modus ist
 * und öffnet die PriceEditorUI.
 *
 * @author FallenStar
 * @version 1.0
 */
public class PriceSetListener implements Listener {

    private final Logger logger;
    private final ProviderRegistry providers;
    private final PlotPriceCommand priceCommand;

    /**
     * Erstellt einen neuen PriceSetListener.
     *
     * @param logger Logger
     * @param providers ProviderRegistry
     * @param priceCommand PlotPriceCommand (für Modus-Check)
     */
    public PriceSetListener(Logger logger, ProviderRegistry providers, PlotPriceCommand priceCommand) {
        this.logger = logger;
        this.providers = providers;
        this.priceCommand = priceCommand;
    }

    /**
     * Handhabt Rechtsklick-Interaktionen im Preis-Setzungs-Modus.
     *
     * @param event PlayerInteractEvent
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // Prüfe ob Spieler im Preis-Setzungs-Modus ist
        if (!priceCommand.isInPriceSetMode(player)) {
            return;
        }

        // Nur Rechtsklick
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        // Prüfe ob Item in Hand
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand == null || itemInHand.getType() == Material.AIR) {
            player.sendMessage("§cKein Item in der Hand!");
            return;
        }

        // Event canceln (verhindert normale Interaktion)
        event.setCancelled(true);

        // Deaktiviere Modus
        priceCommand.cancelPriceSetMode(player);

        // Öffne PriceEditorUI
        openPriceEditorUI(player, itemInHand);
    }

    /**
     * Öffnet die PriceEditorUI für ein Item.
     *
     * @param player Der Spieler
     * @param item Das Item für das der Preis gesetzt werden soll
     */
    private void openPriceEditorUI(Player player, ItemStack item) {
        UIProvider uiProvider = providers.getUIProvider();
        if (uiProvider == null || !uiProvider.isAvailable()) {
            player.sendMessage("§cUI-System nicht verfügbar!");
            return;
        }

        // Lade aktuellen Preis aus ItemBasePriceProvider
        BigDecimal initialPrice = priceCommand.loadPriceFromProvider(item);

        // Erstelle Kontext und Session
        PriceEditorContext context = new PriceEditorContext(item, initialPrice);
        UUID sessionId = priceCommand.createSession(player, context);

        // Baue UI
        var menu = PriceEditorUIBuilder.buildPriceEditorUI(context, sessionId);

        player.sendMessage("§a§lPreis-Editor geöffnet!");
        player.sendMessage("§7Item: §e" + getItemDisplayName(item));
        player.sendMessage("§7Aktueller Preis: §e" + initialPrice + " Sterne");

        uiProvider.showMenu(player, menu);
    }

    /**
     * Gibt den Anzeigenamen eines Items zurück.
     *
     * @param item ItemStack
     * @return Display-Name
     */
    private String getItemDisplayName(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return item.getItemMeta().getDisplayName();
        }
        return item.getType().name();
    }
}
