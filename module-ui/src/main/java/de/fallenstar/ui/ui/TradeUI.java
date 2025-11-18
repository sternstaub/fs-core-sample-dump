package de.fallenstar.ui.ui;

import de.fallenstar.core.provider.TradingEntity;
import de.fallenstar.economy.model.TradeSet;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Universelles Trading-UI für alle TradingEntities.
 *
 * Nutzt das **Vanilla Merchant Interface** für Handel.
 *
 * Features:
 * - Dynamische TradeSets von TradingEntity
 * - Automatische Preis-Konvertierung (Münzen)
 * - Inventar-Validierung gegen TradingEntity.getTradeInventory()
 * - Unterstützt alle TradingEntity-Typen (GuildTrader, PlayerTrader, Banker, etc.)
 *
 * **Verwendung:**
 * <pre>
 * TradeUI.openTradeUI(player, guildTrader);
 * </pre>
 *
 * **Integration:**
 * - GuildTraderNPC: onClick → TradeUI.openTradeUI()
 * - PlayerTraderNPC: onClick → TradeUI.openTradeUI()
 * - BankerNPC: onClick → TradeUI.openTradeUI()
 *
 * @author FallenStar
 * @version 1.0
 */
public class TradeUI {

    private static final Logger logger = Logger.getLogger("TradeUI");

    /**
     * Öffnet das Trade-UI für einen Spieler.
     *
     * @param player Der Spieler
     * @param trader Die TradingEntity (Händler, Bankier, etc.)
     */
    public static void openTradeUI(Player player, TradingEntity trader) {
        if (player == null || trader == null) {
            logger.warning("TradeUI.openTradeUI: player oder trader ist null!");
            return;
        }

        // Hole TradeSets von der TradingEntity
        List<TradeSet> tradeSets = trader.getTradeSets();

        if (tradeSets.isEmpty()) {
            player.sendMessage("§cDieser Händler hat derzeit keine Angebote.");
            return;
        }

        // Erstelle Vanilla Merchant
        Merchant merchant = Bukkit.createMerchant(trader.getName());

        // Konvertiere TradeSets zu MerchantRecipes
        List<MerchantRecipe> recipes = tradeSets.stream()
                .map(TradeSet::createRecipe)
                .collect(Collectors.toList());

        // Setze Rezepte
        merchant.setRecipes(recipes);

        // Öffne Merchant-UI
        player.openMerchant(merchant, true);

        logger.fine("Opened TradeUI for " + player.getName() +
                   " with " + recipes.size() + " trades from " + trader.getName());
    }

    /**
     * Öffnet das Trade-UI mit Custom-Titel.
     *
     * @param player Der Spieler
     * @param trader Die TradingEntity
     * @param customTitle Custom-Titel (wird ignoriert bei Vanilla Merchant)
     */
    public static void openTradeUI(Player player, TradingEntity trader, String customTitle) {
        // Vanilla Merchant Interface unterstützt keine Custom-Titel
        // Fallback: Nutze standard openTradeUI
        openTradeUI(player, trader);
    }

    /**
     * Öffnet das Trade-UI mit Inventar-Validierung.
     *
     * Prüft ob die TradingEntity genug Items im Inventar hat.
     *
     * @param player Der Spieler
     * @param trader Die TradingEntity
     * @param validateInventory true um Inventar zu prüfen
     */
    public static void openTradeUI(Player player, TradingEntity trader, boolean validateInventory) {
        if (!validateInventory) {
            openTradeUI(player, trader);
            return;
        }

        // Prüfe Inventar-Verfügbarkeit
        if (trader.getTradeInventory().isEmpty()) {
            player.sendMessage("§cDieser Händler hat kein Inventar.");
            logger.warning("TradingEntity " + trader.getName() + " hat kein Inventar!");
            return;
        }

        // TODO: Validierung ob genug Items im Inventar
        // Für jetzt: Öffne UI ohne Validierung
        openTradeUI(player, trader);
    }

    /**
     * Prüft ob ein Spieler ein aktives Trade-UI geöffnet hat.
     *
     * @param player Der Spieler
     * @return true wenn Merchant-UI offen
     */
    public static boolean hasTradeUIOpen(Player player) {
        return player.getOpenInventory().getTopInventory().getHolder() instanceof Merchant;
    }

    /**
     * Schließt das Trade-UI für einen Spieler.
     *
     * @param player Der Spieler
     */
    public static void closeTradeUI(Player player) {
        if (hasTradeUIOpen(player)) {
            player.closeInventory();
            logger.fine("Closed TradeUI for " + player.getName());
        }
    }
}
