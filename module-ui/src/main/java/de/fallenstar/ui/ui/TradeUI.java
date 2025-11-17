package de.fallenstar.ui.ui;

import de.fallenstar.core.provider.TradingEntity;
import de.fallenstar.core.ui.BaseUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Universelles Trading-UI für alle TradingEntities.
 *
 * Features:
 * - Nutzt Vanilla Merchant Interface
 * - Dynamische TradeSets von TradingEntity
 * - Automatische Preis-Konvertierung (Münzen)
 * - Inventar-Validierung gegen TradingEntity.getTradeInventory()
 *
 * **Verwendung:**
 * <pre>
 * TradingEntity guildTrader = getGuildTrader(plot);
 * TradeUI.openTradeUI(player, guildTrader);
 * </pre>
 *
 * **Architektur:**
 * - Core-Interface: TradingEntity (abstrakt)
 * - Economy-Modul: TradeSet (konkrete Implementierung)
 * - UI-Modul: TradeUI (verwendet Reflection für TradeSet)
 *
 * @author FallenStar
 * @version 1.0
 */
public class TradeUI extends BaseUI {

    /**
     * Konstruktor für TradeUI.
     *
     * @param entityName Name der TradingEntity
     */
    private TradeUI(String entityName) {
        super("§6§l" + entityName);
    }

    /**
     * Öffnet das Trade-UI für einen Spieler.
     *
     * Diese Methode:
     * 1. Erstellt Merchant mit TradeSets
     * 2. Konvertiert TradeSets zu MerchantRecipes
     * 3. Öffnet Merchant-Interface
     *
     * @param player Der Spieler
     * @param trader Die TradingEntity (Händler)
     */
    public static void openTradeUI(Player player, TradingEntity trader) {
        // Erstelle Merchant mit Entity-Namen
        Merchant merchant = Bukkit.createMerchant(Component.text(trader.getName()));

        // Hole TradeSets von Entity
        List<?> tradeSets = trader.getTradeSets();

        if (tradeSets.isEmpty()) {
            player.sendMessage(Component.text("✗ Dieser Händler hat keine Angebote!", NamedTextColor.RED));
            return;
        }

        // Konvertiere TradeSets zu MerchantRecipes
        List<MerchantRecipe> recipes = new ArrayList<>();

        for (Object tradeSetObj : tradeSets) {
            try {
                // Reflection-Zugriff auf TradeSet.createRecipe()
                Method createRecipeMethod = tradeSetObj.getClass().getMethod("createRecipe");
                MerchantRecipe recipe = (MerchantRecipe) createRecipeMethod.invoke(tradeSetObj);
                recipes.add(recipe);
            } catch (Exception e) {
                // Fehler beim Konvertieren → Überspringen
                Bukkit.getLogger().warning("Fehler beim Laden eines TradeSets: " + e.getMessage());
            }
        }

        if (recipes.isEmpty()) {
            player.sendMessage(Component.text("✗ Fehler beim Laden der Handels-Angebote!", NamedTextColor.RED));
            return;
        }

        // Füge Rezepte zum Merchant hinzu
        merchant.setRecipes(recipes);

        // Öffne Merchant-Interface
        player.openMerchant(merchant, true);

        // Erfolgs-Nachricht
        player.sendMessage(Component.text(
                "✓ Händler geöffnet - " + recipes.size() + " Angebote verfügbar",
                NamedTextColor.GREEN
        ));
    }

    /**
     * Schließt das Merchant-Interface.
     *
     * @param player Der Spieler
     */
    @Override
    public void close(Player player) {
        player.closeInventory();
    }

    /**
     * Öffnet das UI (BaseUI-Kompatibilität).
     *
     * Diese Methode sollte nicht direkt aufgerufen werden.
     * Nutze stattdessen {@link #openTradeUI(Player, TradingEntity)}.
     *
     * @param player Der Spieler
     */
    @Override
    public void open(Player player) {
        // Leere Implementierung - nutze openTradeUI() stattdessen
        player.sendMessage(Component.text(
                "✗ TradeUI.open() sollte nicht direkt aufgerufen werden!",
                NamedTextColor.RED
        ));
        player.sendMessage(Component.text(
                "Nutze stattdessen: TradeUI.openTradeUI(player, trader)",
                NamedTextColor.YELLOW
        ));
    }
}
