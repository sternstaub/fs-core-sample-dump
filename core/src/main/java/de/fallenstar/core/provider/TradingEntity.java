package de.fallenstar.core.provider;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.List;
import java.util.Optional;

/**
 * Interface für handelbare Entities (NPCs, Shops, etc.).
 *
 * Dieses Interface abstrahiert alle handelnden Entities im System:
 * - Gildenhändler (nutzen Plot-Storage)
 * - Spielerhändler (nutzen virtuelles Inventar)
 * - Fahrende Händler (nutzen eigenes Inventar)
 * - Weltbankier (unbegrenztes Inventar)
 *
 * **Provider-Pattern:**
 * Dieses Interface gehört zum Core und wird von Modulen implementiert.
 * Das UI-Modul nutzt dieses Interface für TradeUI.
 *
 * Features:
 * - TradeSets abrufen (Handels-Angebote)
 * - Inventar-Zugriff (Rohstoffspeicher)
 * - Trade-Validierung
 * - Trade-Ausführung
 *
 * **Verwendung:**
 * <pre>
 * TradingEntity trader = getGuildTrader(plot);
 * TradeUI.openTradeUI(player, trader);
 * </pre>
 *
 * Implementierungen:
 * - GuildTraderNPC (Plots-Modul) - nutzt Plot-Storage
 * - PlayerTraderNPC (Plots-Modul) - nutzt virtuelles Inventar
 * - TravelingMerchantNPC (NPCs-Modul) - nutzt eigenes Inventar
 * - WorldBankerNPC (NPCs-Modul) - unbegrenztes Inventar
 *
 * @author FallenStar
 * @version 1.0
 */
public interface TradingEntity {

    /**
     * Gibt alle TradeSets dieser Entity zurück.
     *
     * TradeSets definieren die Handels-Angebote (Input → Output + Preise).
     *
     * @return Liste von TradeSets (kann leer sein)
     */
    List<?> getTradeSets();  // Object wegen TradeSet aus Economy-Modul

    /**
     * Gibt das Inventar (Rohstoffspeicher) dieser Entity zurück.
     *
     * Dieses Inventar wird für Trade-Validierung genutzt:
     * - Gildenhändler → Plot-Storage
     * - Spielerhändler → Virtuelles Inventar
     * - Fahrende Händler → Eigenes Inventar
     * - Weltbankier → Empty (unbegrenzt)
     *
     * @return Optional mit Inventory, oder empty wenn unbegrenzt
     */
    Optional<Inventory> getTradeInventory();

    /**
     * Prüft ob ein Trade ausgeführt werden kann.
     *
     * Validierung umfasst:
     * - Inventar-Prüfung (genug Output-Items vorhanden?)
     * - Spieler-Inventar-Prüfung (genug Input-Items?)
     * - Trade-Limit-Prüfung (maxUses erreicht?)
     *
     * @param trade Das TradeSet (Object wegen Economy-Modul)
     * @param player Der Spieler
     * @return true wenn Trade möglich
     */
    boolean canExecuteTrade(Object trade, Player player);

    /**
     * Führt einen Trade aus.
     *
     * Ablauf:
     * 1. Validierung via canExecuteTrade()
     * 2. Items aus Spieler-Inventar entfernen (Inputs)
     * 3. Items in Spieler-Inventar hinzufügen (Output)
     * 4. Items aus Entity-Inventar entfernen (Output)
     * 5. Items in Entity-Inventar hinzufügen (Inputs)
     *
     * @param trade Das TradeSet (Object wegen Economy-Modul)
     * @param player Der Spieler
     * @return true wenn erfolgreich
     */
    boolean executeTrade(Object trade, Player player);

    /**
     * Gibt den Namen dieser Entity zurück.
     *
     * Wird im Merchant-Interface angezeigt.
     *
     * @return Name (z.B. "Gildenhändler", "Händler von SpielerX")
     */
    String getName();

    /**
     * Gibt den Entity-Typ zurück.
     *
     * @return TradingEntityType
     */
    TradingEntityType getEntityType();

    /**
     * Entity-Typen für verschiedene Händler-Arten.
     */
    enum TradingEntityType {
        /**
         * Gildenhändler - nutzt Plot-Storage.
         */
        GUILD_TRADER,

        /**
         * Spielerhändler - nutzt virtuelles Inventar.
         */
        PLAYER_TRADER,

        /**
         * Fahrender Händler - nutzt eigenes Inventar.
         */
        TRAVELING_MERCHANT,

        /**
         * Weltbankier - unbegrenztes Inventar.
         */
        WORLD_BANKER
    }
}
