package de.fallenstar.plot.listener;

import de.fallenstar.core.provider.UIProvider;
import de.fallenstar.core.registry.ProviderRegistry;
import de.fallenstar.plot.command.PlotPriceCommand;
import de.fallenstar.plot.gui.PriceEditorContext;
import de.fallenstar.ui.ui.PriceEditorUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.math.BigDecimal;
import java.util.List;
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
        // Lade aktuellen Preis aus ItemBasePriceProvider
        BigDecimal initialPrice = priceCommand.loadPriceFromProvider(item);

        // Erstelle Kontext
        PriceEditorContext context = new PriceEditorContext(item, initialPrice);
        UUID sessionId = priceCommand.createSession(player, context);

        // Hole Coin-Items vom SpecialItemManager (via Reflection)
        ItemStack coinBronze = getCoinItem("bronze_stern");
        ItemStack coinSilver = getCoinItem("silver_stern");
        ItemStack coinGold = getCoinItem("gold_stern");

        if (coinBronze == null || coinSilver == null || coinGold == null) {
            player.sendMessage("§cFehler: Münz-Items nicht verfügbar!");
            logger.warning("Coin-Items nicht geladen - SpecialItemManager nicht verfügbar?");
            return;
        }

        // Erstelle Callback-Implementierung
        PriceEditorUI.PriceCallback callback = new PriceEditorUI.PriceCallback() {
            @Override
            public void onIncrease(int amount) {
                context.increasePrice(BigDecimal.valueOf(amount));
            }

            @Override
            public void onDecrease(int amount) {
                context.decreasePrice(BigDecimal.valueOf(amount));
            }

            @Override
            public void onConfirm() {
                // Speichere Preis und schließe Session
                priceCommand.handleConfirmPrice(player, sessionId);
                player.sendMessage("§a✓ Preis gespeichert: " + context.getCurrentPrice() + " Sterne");
            }

            @Override
            public void onCancel() {
                // Schließe Session ohne zu speichern
                priceCommand.removeSession(sessionId);
                player.sendMessage("§7Preis-Editor abgebrochen");
            }

            @Override
            public ItemStack getCurrentDisplayItem() {
                // Item mit Preis-Lore erstellen
                ItemStack displayItem = item.clone();
                ItemMeta meta = displayItem.getItemMeta();

                List<Component> lore = meta.hasLore() ? meta.lore() : new java.util.ArrayList<>();
                lore.add(Component.text("", NamedTextColor.GRAY));
                lore.add(Component.text("Aktueller Preis:", NamedTextColor.GOLD));
                lore.add(Component.text(context.getCurrentPrice() + " Sterne", NamedTextColor.YELLOW));

                meta.lore(lore);
                displayItem.setItemMeta(meta);

                return displayItem;
            }
        };

        // Erstelle und öffne UI
        PriceEditorUI ui = new PriceEditorUI(
                "Preis festlegen - " + getItemDisplayName(item),
                callback,
                coinBronze,
                coinSilver,
                coinGold
        );

        // Registriere Listener
        Bukkit.getPluginManager().registerEvents(ui, Bukkit.getPluginManager().getPlugin("FallenStar-Plots"));

        // Öffne UI
        ui.open(player);

        player.sendMessage("§a§lPreis-Editor geöffnet!");
        player.sendMessage("§7Item: §e" + getItemDisplayName(item));
        player.sendMessage("§7Ursprünglicher Preis: §e" + initialPrice + " Sterne");
    }

    /**
     * Holt ein Coin-Item vom SpecialItemManager via Reflection.
     *
     * @param itemId Item-ID (z.B. "bronze_stern")
     * @return ItemStack oder null bei Fehler
     */
    private ItemStack getCoinItem(String itemId) {
        try {
            var itemsPlugin = Bukkit.getPluginManager().getPlugin("FallenStar-Items");
            if (itemsPlugin == null) {
                return null;
            }

            var getSpecialItemManager = itemsPlugin.getClass().getMethod("getSpecialItemManager");
            var specialItemManager = getSpecialItemManager.invoke(itemsPlugin);

            var getItemMethod = specialItemManager.getClass().getMethod("getItem", String.class, int.class);
            var optionalItem = getItemMethod.invoke(specialItemManager, itemId, 1);

            // Optional.isPresent() und Optional.get()
            var isPresentMethod = optionalItem.getClass().getMethod("isPresent");
            boolean isPresent = (boolean) isPresentMethod.invoke(optionalItem);

            if (isPresent) {
                var getMethod = optionalItem.getClass().getMethod("get");
                return (ItemStack) getMethod.invoke(optionalItem);
            }

            return null;
        } catch (Exception e) {
            logger.warning("Fehler beim Laden von Coin-Item '" + itemId + "': " + e.getMessage());
            return null;
        }
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
