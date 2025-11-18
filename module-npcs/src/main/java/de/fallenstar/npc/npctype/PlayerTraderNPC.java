package de.fallenstar.npc.npctype;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.provider.TradingEntity;
import de.fallenstar.core.registry.ProviderRegistry;
import de.fallenstar.economy.model.TradeSet;
import de.fallenstar.ui.ui.TradeUI;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.*;
import java.util.logging.Logger;

/**
 * Spielerhändler-NPC (Spieler-gesteuerter Händler).
 *
 * Features:
 * - Spieler kauft Händler-Slot auf Grundstück
 * - Spieler konfiguriert eigenen Shop
 * - Nutzt eigenes virtuelles Inventar (nicht Plot-Storage)
 * - Owner kann Preise festlegen
 * - Owner verwaltet Inventar
 *
 * **Implementiert:**
 * - {@link NPCType} für NPC-Funktionalität
 * - {@link TradingEntity} für Trading-Interface
 *
 * **Verwendung:**
 * <pre>
 * PlayerTraderNPC trader = new PlayerTraderNPC(providers, logger);
 * trader.initialize();
 *
 * // Registriere Händler für Spieler + Plot
 * trader.registerNPCForPlayer(npcId, player, plot);
 *
 * // Bei NPC-Click:
 * trader.onClick(player, npcId);
 * </pre>
 *
 * **Persistierung:**
 * - NPC-Zuordnungen in Config
 * - Virtuelles Inventar via VirtualTraderInventoryManager
 * - TradeSets via PlotStorageData (Plot-basiert)
 *
 * @author FallenStar
 * @version 1.0
 */
public class PlayerTraderNPC implements NPCType, TradingEntity {

    private final ProviderRegistry providers;
    private final Logger logger;

    /**
     * Zuordnung: NPC-UUID → Owner-UUID
     */
    private final Map<UUID, UUID> npcOwners;

    /**
     * Zuordnung: NPC-UUID → Source-Plot
     */
    private final Map<UUID, Plot> npcPlots;

    /**
     * Zuordnung: NPC-UUID → TradeSets (Cache)
     */
    private final Map<UUID, List<TradeSet>> npcTradeSets;

    // Config-Werte
    private static final String NPC_NAME = "Spielerhändler";
    private static final String NPC_SKIN = "MHF_Steve";

    /**
     * Erstellt einen neuen PlayerTraderNPC.
     *
     * @param providers ProviderRegistry
     * @param logger Logger
     */
    public PlayerTraderNPC(ProviderRegistry providers, Logger logger) {
        this.providers = providers;
        this.logger = logger;
        this.npcOwners = new HashMap<>();
        this.npcPlots = new HashMap<>();
        this.npcTradeSets = new HashMap<>();
    }

    // ===== NPCType Implementation =====

    @Override
    public String getTypeName() {
        return "playertrader";
    }

    @Override
    public String getDisplayName() {
        return NPC_NAME;
    }

    @Override
    public String getSkin() {
        return NPC_SKIN;
    }

    @Override
    public void onClick(Player player, UUID npcId) {
        // Prüfe ob Spieler Owner ist
        UUID owner = npcOwners.get(npcId);

        if (owner != null && owner.equals(player.getUniqueId())) {
            // Owner-Click: Öffne Verwaltungs-UI
            openManagementUI(player, npcId);
        } else {
            // Kunden-Click: Öffne Shop-UI
            openShopUI(player, npcId);
        }

        logger.fine("PlayerTraderNPC clicked by " + player.getName() +
                   " (NPC: " + npcId + ", Owner: " + (owner != null && owner.equals(player.getUniqueId())) + ")");
    }

    @Override
    public void initialize() {
        logger.info("PlayerTraderNPC initialized");
    }

    @Override
    public void shutdown() {
        npcOwners.clear();
        npcPlots.clear();
        npcTradeSets.clear();
        logger.info("PlayerTraderNPC shutdown");
    }

    @Override
    public boolean isAvailable() {
        // PlayerTraderNPC benötigt PlotProvider + ItemProvider
        return providers.getPlotProvider().isAvailable() &&
               providers.getItemProvider().isAvailable();
    }

    // ===== TradingEntity Implementation =====

    @Override
    public String getName() {
        return NPC_NAME;
    }

    @Override
    public TradingEntityType getEntityType() {
        return TradingEntityType.PLAYER_TRADER;
    }

    @Override
    public List<TradeSet> getTradeSets() {
        // TODO: Lade TradeSets für diesen NPC
        // Für jetzt: Leere Liste
        return Collections.emptyList();
    }

    @Override
    public Optional<Inventory> getTradeInventory() {
        // TODO: Hole VirtualTraderInventory für diesen NPC
        // Für jetzt: Leer
        return Optional.empty();
    }

    @Override
    public boolean canExecuteTrade(TradeSet trade, Player player) {
        // TODO: Prüfe ob genug Items im virtuellen Inventar
        // Für jetzt: false
        return false;
    }

    @Override
    public boolean executeTrade(TradeSet trade, Player player) {
        // TODO: Führe Trade aus (Inventar-Update)
        // Für jetzt: false
        logger.warning("PlayerTraderNPC.executeTrade() noch nicht vollständig implementiert!");
        player.sendMessage("§cHändler-Funktion noch in Entwicklung.");
        return false;
    }

    // ===== PlayerTrader-spezifische Methoden =====

    /**
     * Registriert einen NPC für einen Spieler auf einem Plot.
     *
     * @param npcId NPC-UUID
     * @param owner Owner-UUID
     * @param plot Source-Plot (Inventar-Quelle)
     */
    public void registerNPCForPlayer(UUID npcId, UUID owner, Plot plot) {
        npcOwners.put(npcId, owner);
        npcPlots.put(npcId, plot);

        logger.info("Registered PlayerTraderNPC " + npcId +
                   " for owner " + owner + " on plot " + plot.getIdentifier());
    }

    /**
     * Entfernt die Registrierung eines NPCs.
     *
     * @param npcId NPC-UUID
     */
    public void unregisterNPC(UUID npcId) {
        npcOwners.remove(npcId);
        npcPlots.remove(npcId);
        npcTradeSets.remove(npcId);

        logger.info("Unregistered PlayerTraderNPC " + npcId);
    }

    /**
     * Gibt den Owner eines NPCs zurück.
     *
     * @param npcId NPC-UUID
     * @return Optional mit Owner-UUID
     */
    public Optional<UUID> getOwner(UUID npcId) {
        return Optional.ofNullable(npcOwners.get(npcId));
    }

    /**
     * Gibt den Source-Plot eines NPCs zurück.
     *
     * @param npcId NPC-UUID
     * @return Optional mit Plot
     */
    public Optional<Plot> getSourcePlot(UUID npcId) {
        return Optional.ofNullable(npcPlots.get(npcId));
    }

    /**
     * Prüft ob ein Spieler Owner eines NPCs ist.
     *
     * @param player Der Spieler
     * @param npcId NPC-UUID
     * @return true wenn Owner
     */
    public boolean isOwner(Player player, UUID npcId) {
        UUID owner = npcOwners.get(npcId);
        return owner != null && owner.equals(player.getUniqueId());
    }

    /**
     * Öffnet die Verwaltungs-UI für den Owner.
     *
     * @param player Der Owner
     * @param npcId NPC-UUID
     */
    private void openManagementUI(Player player, UUID npcId) {
        // TODO: Implementiere Verwaltungs-UI
        // Features:
        // - Inventar verwalten
        // - Preise festlegen
        // - Händler umbenennen
        // - Statistiken anzeigen

        player.sendMessage("§6Händler-Verwaltung:");
        player.sendMessage("§7- Inventar: §e/plot gui → Händler-Inventar");
        player.sendMessage("§7- Preise: §e/plot price set");
        player.sendMessage("§c[Verwaltungs-UI noch in Entwicklung]");
    }

    /**
     * Öffnet die Shop-UI für Kunden.
     *
     * @param player Der Kunde
     * @param npcId NPC-UUID
     */
    private void openShopUI(Player player, UUID npcId) {
        // Öffne TradeUI mit diesem NPC als TradingEntity
        // TODO: Setze korrekte TradeSets

        player.sendMessage("§6Willkommen beim Spielerhändler!");
        player.sendMessage("§c[Shop-UI noch in Entwicklung]");

        // Placeholder: Öffne leeres Trade-UI
        // TradeUI.openTradeUI(player, this);
    }

    /**
     * Gibt Debug-Informationen zurück.
     *
     * @return Debug-String
     */
    public String getDebugInfo() {
        return "PlayerTraderNPC{" +
                "npcOwners=" + npcOwners.size() +
                ", npcPlots=" + npcPlots.size() +
                ", npcTradeSets=" + npcTradeSets.size() +
                '}';
    }
}
