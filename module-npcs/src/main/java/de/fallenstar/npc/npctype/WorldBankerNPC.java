package de.fallenstar.npc.npctype;

import de.fallenstar.core.provider.EconomyProvider;
import de.fallenstar.core.provider.TradingEntity;
import de.fallenstar.core.registry.ProviderRegistry;
import de.fallenstar.economy.model.TradeSet;
import de.fallenstar.ui.ui.TradeUI;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Logger;

/**
 * Weltbankier-NPC (Globale Bank).
 *
 * Features:
 * - Globale Bank ohne Limits
 * - Sterne ↔ Vault-Guthaben Umtausch
 * - Verfügbar auf Admin-Plots
 * - Keine Inventar-Beschränkungen
 *
 * **Implementiert:**
 * - {@link NPCType} für NPC-Funktionalität
 * - {@link TradingEntity} für Trading-Interface
 *
 * **Verwendung:**
 * <pre>
 * WorldBankerNPC banker = new WorldBankerNPC(providers, logger);
 * banker.initialize();
 *
 * // Bei NPC-Click:
 * banker.onClick(player, npcId);
 * </pre>
 *
 * **Wechselkurse:**
 * - Einzahlung: 1 Stern = 1 Vault-Guthaben
 * - Auszahlung: 1 Vault-Guthaben = 1 Stern
 *
 * @author FallenStar
 * @version 1.0
 */
public class WorldBankerNPC implements NPCType, TradingEntity {

    private final ProviderRegistry providers;
    private final Logger logger;
    private final Map<UUID, List<TradeSet>> npcTradeSets;

    // Config-Werte (könnten aus Config geladen werden)
    private static final String NPC_NAME = "Weltbankier";
    private static final String NPC_SKIN = "MHF_Villager";
    private static final BigDecimal EXCHANGE_RATE = BigDecimal.ONE;  // 1:1

    /**
     * Erstellt einen neuen WorldBankerNPC.
     *
     * @param providers ProviderRegistry
     * @param logger Logger
     */
    public WorldBankerNPC(ProviderRegistry providers, Logger logger) {
        this.providers = providers;
        this.logger = logger;
        this.npcTradeSets = new HashMap<>();
    }

    // ===== NPCType Implementation =====

    @Override
    public String getTypeName() {
        return "worldbanker";
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
        // Öffne Banking-UI (Trade-Interface)
        TradeUI.openTradeUI(player, this);

        logger.fine("WorldBankerNPC clicked by " + player.getName() + " (NPC: " + npcId + ")");
    }

    @Override
    public void initialize() {
        logger.info("WorldBankerNPC initialized");
    }

    @Override
    public void shutdown() {
        npcTradeSets.clear();
        logger.info("WorldBankerNPC shutdown");
    }

    @Override
    public boolean isAvailable() {
        // WorldBankier benötigt EconomyProvider (Vault)
        EconomyProvider economyProvider = providers.getEconomyProvider();
        return economyProvider != null && economyProvider.isAvailable();
    }

    // ===== TradingEntity Implementation =====

    @Override
    public String getName() {
        return NPC_NAME;
    }

    @Override
    public TradingEntityType getEntityType() {
        return TradingEntityType.WORLD_BANKER;
    }

    @Override
    public List<TradeSet> getTradeSets() {
        // Weltbankier hat keine festen TradeSets
        // Stattdessen: Dynamische Einzahlung/Auszahlung via Custom-UI
        // Für Vanilla Merchant: Leere Liste (Custom-Banking-UI nötig)
        return Collections.emptyList();
    }

    @Override
    public Optional<Inventory> getTradeInventory() {
        // Weltbankier hat kein physisches Inventar (unbegrenzt)
        return Optional.empty();
    }

    @Override
    public boolean canExecuteTrade(Object trade, Player player) {
        // Cast zu TradeSet (safe in diesem Kontext)
        if (!(trade instanceof TradeSet)) {
            return false;
        }

        TradeSet tradeSet = (TradeSet) trade;

        // Weltbankier hat unbegrenzte Resourcen
        // Prüfe nur ob Spieler genug Vault-Guthaben hat (für Auszahlungen)
        EconomyProvider economyProvider = providers.getEconomyProvider();

        if (!economyProvider.isAvailable()) {
            return false;
        }

        // TODO: Prüfe Spieler-Guthaben vs. Trade-Kosten
        // Für jetzt: Immer true
        return true;
    }

    @Override
    public boolean executeTrade(Object trade, Player player) {
        // Cast zu TradeSet (safe in diesem Kontext)
        if (!(trade instanceof TradeSet)) {
            return false;
        }

        TradeSet tradeSet = (TradeSet) trade;

        // Weltbankier-Trades werden custom gehandhabt
        // Vanilla Merchant Interface nicht ideal für Banking
        // TODO: Implementiere Custom-Banking-UI statt Vanilla Merchant

        logger.warning("WorldBankerNPC.executeTrade() nicht implementiert - nutze Custom-Banking-UI!");
        player.sendMessage("§cBanking-Funktion noch nicht verfügbar.");
        return false;
    }

    // ===== Banking-spezifische Methoden =====

    /**
     * Zahlt Sterne vom Spieler-Inventar in Vault-Guthaben ein.
     *
     * @param player Der Spieler
     * @param amount Anzahl Sterne
     * @return true wenn erfolgreich
     */
    public boolean depositStars(Player player, BigDecimal amount) {
        EconomyProvider economyProvider = providers.getEconomyProvider();

        if (!economyProvider.isAvailable()) {
            player.sendMessage("§cEconomy-System nicht verfügbar!");
            return false;
        }

        // TODO: Prüfe ob Spieler genug Sterne im Inventar hat
        // TODO: Entferne Sterne aus Inventar
        // TODO: Erhöhe Vault-Guthaben

        try {
            // Berechne Vault-Betrag (1:1 Wechselkurs)
            BigDecimal vaultAmount = amount.multiply(EXCHANGE_RATE);

            // Zahle in Vault ein
            economyProvider.deposit(player, vaultAmount.doubleValue());

            player.sendMessage("§a✓ " + amount + " Sterne eingezahlt → " + vaultAmount + " Guthaben");
            logger.info(player.getName() + " deposited " + amount + " stars");

            return true;

        } catch (Exception e) {
            logger.severe("Fehler bei depositStars: " + e.getMessage());
            player.sendMessage("§cFehler beim Einzahlen!");
            return false;
        }
    }

    /**
     * Zahlt Vault-Guthaben als Sterne aus.
     *
     * @param player Der Spieler
     * @param amount Anzahl Vault-Guthaben
     * @return true wenn erfolgreich
     */
    public boolean withdrawStars(Player player, BigDecimal amount) {
        EconomyProvider economyProvider = providers.getEconomyProvider();

        if (!economyProvider.isAvailable()) {
            player.sendMessage("§cEconomy-System nicht verfügbar!");
            return false;
        }

        try {
            // Prüfe Vault-Guthaben
            double balance = economyProvider.getBalance(player);

            if (balance < amount.doubleValue()) {
                player.sendMessage("§cNicht genug Guthaben! (Verfügbar: " + balance + ")");
                return false;
            }

            // Berechne Sterne-Anzahl (1:1 Wechselkurs)
            BigDecimal starAmount = amount.multiply(EXCHANGE_RATE);

            // Ziehe von Vault ab
            economyProvider.withdraw(player, amount.doubleValue());

            // TODO: Füge Sterne zum Inventar hinzu (via CurrencyManager)

            player.sendMessage("§a✓ " + amount + " Guthaben → " + starAmount + " Sterne ausgezahlt");
            logger.info(player.getName() + " withdrew " + amount + " vault balance");

            return true;

        } catch (Exception e) {
            logger.severe("Fehler bei withdrawStars: " + e.getMessage());
            player.sendMessage("§cFehler beim Auszahlen!");
            return false;
        }
    }

    /**
     * Zeigt dem Spieler sein Vault-Guthaben an.
     *
     * @param player Der Spieler
     */
    public void showBalance(Player player) {
        EconomyProvider economyProvider = providers.getEconomyProvider();

        if (!economyProvider.isAvailable()) {
            player.sendMessage("§cEconomy-System nicht verfügbar!");
            return;
        }

        try {
            double balance = economyProvider.getBalance(player);
            String formatted = economyProvider.format(balance);

            player.sendMessage("§6Dein Guthaben: §e" + formatted);

        } catch (Exception e) {
            logger.severe("Fehler bei showBalance: " + e.getMessage());
            player.sendMessage("§cFehler beim Abrufen des Guthabens!");
        }
    }
}
