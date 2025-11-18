package de.fallenstar.npc.npctype;

import de.fallenstar.core.exception.ProviderFunctionalityNotFoundException;
import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.provider.PlotProvider;
import de.fallenstar.core.provider.TradingEntity;
import de.fallenstar.npc.manager.NPCManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Logger;

/**
 * Gildenhändler-NPC.
 *
 * Funktionalität:
 * - Automatischer Händler auf Handelsgilde-Grundstücken
 * - Nutzt Plot-Storage als Inventar
 * - Preise aus ItemBasePriceProvider (Economy-Modul)
 * - Verkauft/Kauft Items basierend auf Plot-Storage
 *
 * Features:
 * - Dynamische TradeSets aus Plot-Storage-Inventar
 * - Automatische Preis-Berechnung via ItemBasePriceProvider
 * - Output-Chests → verkaufbare Items
 * - Input-Chest → Einnahmen
 *
 * Implementiert:
 * - NPCType (für NPC-Management)
 * - TradingEntity (für Trading-System)
 *
 * Voraussetzungen:
 * - PlotProvider (required)
 * - PlotStorageProvider via Reflection (Plots-Modul)
 * - ItemBasePriceProvider via Reflection (Economy-Modul)
 *
 * @author FallenStar
 * @version 1.0
 */
public class GuildTraderNPC implements NPCType, TradingEntity {

    private final NPCManager npcManager;
    private final PlotProvider plotProvider;
    private final FileConfiguration config;
    private final Logger logger;
    private final de.fallenstar.core.registry.ProviderRegistry providers;

    /**
     * Zuordnung: NPC-UUID → Plot
     * Speichert welcher NPC zu welchem Grundstück gehört.
     */
    private final Map<UUID, Plot> npcPlotMap;

    /**
     * Cache für generierte TradeSets.
     * Map: NPC-UUID → Liste von TradeSets
     */
    private final Map<UUID, List<Object>> tradeSetCache;

    /**
     * Erstellt einen neuen Gildenhändler-NPC-Typ.
     *
     * @param npcManager NPCManager
     * @param plotProvider PlotProvider
     * @param providers ProviderRegistry
     * @param config Plugin-Config
     */
    public GuildTraderNPC(
            NPCManager npcManager,
            PlotProvider plotProvider,
            de.fallenstar.core.registry.ProviderRegistry providers,
            FileConfiguration config
    ) {
        this.npcManager = npcManager;
        this.plotProvider = plotProvider;
        this.providers = providers;
        this.config = config;
        this.logger = npcManager.getLogger();

        this.npcPlotMap = new HashMap<>();
        this.tradeSetCache = new HashMap<>();
    }

    // ==================== NPCType Interface ====================

    @Override
    public String getTypeName() {
        return "guildtrader";
    }

    @Override
    public String getDisplayName() {
        return config.getString("npc.guildtrader.name", "§6Gildenhändler");
    }

    @Override
    public String getSkin() {
        return config.getString("npc.guildtrader.skin", "MHF_Villager");
    }

    @Override
    public void onClick(Player player, UUID npcId) {
        try {
            // Hole Plot des NPCs
            Plot plot = npcPlotMap.get(npcId);
            if (plot == null) {
                player.sendMessage("§cDieser Händler ist nicht korrekt konfiguriert!");
                logger.warning("GuildTrader " + npcId + " has no plot assigned!");
                return;
            }

            // Erstelle NPC-spezifische TradingEntity-Instanz
            GuildTraderInstance traderInstance = new GuildTraderInstance(npcId);

            // Öffne Trading-UI mit NPC-spezifischer Instanz
            openTradeUIForInstance(player, traderInstance);

        } catch (Exception e) {
            player.sendMessage("§cEin Fehler ist beim Öffnen des Händlers aufgetreten!");
            logger.severe("Error opening GuildTrader UI: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void initialize() {
        logger.info("GuildTrader NPC initialized");
    }

    @Override
    public void shutdown() {
        // Cleanup
        npcPlotMap.clear();
        tradeSetCache.clear();
    }

    @Override
    public boolean isAvailable() {
        return plotProvider != null && plotProvider.isAvailable();
    }

    // ==================== TradingEntity Interface ====================

    @Override
    public List<?> getTradeSets() {
        // Diese Methode wird für einen SPEZIFISCHEN NPC aufgerufen
        // Wir müssen die NPC-UUID kennen - wird vom Caller gesetzt
        logger.warning("getTradeSets() called without NPC context - returning empty list");
        return Collections.emptyList();
    }

    /**
     * Gibt TradeSets für einen spezifischen NPC zurück.
     *
     * @param npcId UUID des NPCs
     * @return Liste von TradeSets
     */
    public List<Object> getTradeSets(UUID npcId) {
        logger.info("=== getTradeSets called for NPC " + npcId + " ===");

        // Cache prüfen
        if (tradeSetCache.containsKey(npcId)) {
            List<Object> cached = tradeSetCache.get(npcId);
            logger.info("Returning " + cached.size() + " cached TradeSets");
            return cached;
        }

        // Hole Plot
        Plot plot = npcPlotMap.get(npcId);
        if (plot == null) {
            logger.warning("GuildTrader " + npcId + " has no plot - cannot generate TradeSets");
            logger.warning("npcPlotMap contents: " + npcPlotMap.keySet());
            return Collections.emptyList();
        }

        logger.info("Found plot for NPC: " + plot.getUuid());

        // Generiere TradeSets aus Plot-Storage
        List<Object> tradeSets = generateTradeSetsFromPlotStorage(plot, npcId);

        logger.info("Generated " + tradeSets.size() + " TradeSets");

        // Cache TradeSets
        tradeSetCache.put(npcId, tradeSets);

        return tradeSets;
    }

    @Override
    public Optional<Inventory> getTradeInventory() {
        // Gildenhändler nutzen Plot-Storage (kein echtes Inventory)
        return Optional.empty();
    }

    /**
     * Gibt das Plot-Storage-Inventar für einen NPC zurück.
     *
     * @param npcId UUID des NPCs
     * @return Optional mit Plot-Storage-Items
     */
    public Optional<List<ItemStack>> getPlotStorageInventory(UUID npcId) {
        Plot plot = npcPlotMap.get(npcId);
        if (plot == null) {
            logger.warning("getPlotStorageInventory: No plot found for NPC " + npcId);
            return Optional.empty();
        }

        logger.info("getPlotStorageInventory: Found plot " + plot.getUuid());

        // Hole Items aus Plot-Storage via Reflection
        try {
            var plotsPlugin = Bukkit.getPluginManager().getPlugin("FallenStar-Plots");
            if (plotsPlugin == null) {
                logger.warning("getPlotStorageInventory: Plots plugin not found!");
                return Optional.empty();
            }

            // Reflection: hole PlotStorageProvider (KORRIGIERT: getStorageProvider statt getPlotStorageProvider)
            var getStorageProviderMethod = plotsPlugin.getClass().getMethod("getStorageProvider");
            var storageProvider = getStorageProviderMethod.invoke(plotsPlugin);

            if (storageProvider == null) {
                logger.warning("getPlotStorageInventory: StorageProvider is null!");
                return Optional.empty();
            }

            logger.info("getPlotStorageInventory: Got StorageProvider");

            // Hole Output-Chest-Contents
            var getOutputContentsMethod = storageProvider.getClass().getMethod("getOutputChestContents", Plot.class);
            @SuppressWarnings("unchecked")
            List<ItemStack> items = (List<ItemStack>) getOutputContentsMethod.invoke(storageProvider, plot);

            logger.info("getPlotStorageInventory: Got " + (items != null ? items.size() : "null") + " items");

            return Optional.ofNullable(items);

        } catch (Exception e) {
            logger.warning("Failed to get plot storage inventory: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public boolean canExecuteTrade(Object trade, Player player) {
        try {
            // Hole TradeSet-Daten via Reflection
            var tradeClass = trade.getClass();
            var getInput1 = tradeClass.getMethod("getInput1");
            var getOutput = tradeClass.getMethod("getOutput");

            ItemStack input = (ItemStack) getInput1.invoke(trade);
            ItemStack output = (ItemStack) getOutput.invoke(trade);

            // Prüfe ob Spieler genug Input-Items hat
            if (!player.getInventory().containsAtLeast(input, input.getAmount())) {
                return false;
            }

            // Prüfe ob genug Platz im Spieler-Inventar für Output
            if (player.getInventory().firstEmpty() == -1) {
                // Inventar voll - prüfe ob Output-Item stackbar ist
                int freeSpace = 0;
                for (ItemStack item : player.getInventory().getContents()) {
                    if (item != null && item.isSimilar(output)) {
                        freeSpace += (output.getMaxStackSize() - item.getAmount());
                    }
                }
                if (freeSpace < output.getAmount()) {
                    return false;
                }
            }

            // Trade ist möglich
            return true;

        } catch (Exception e) {
            logger.warning("Failed to validate trade: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean executeTrade(Object trade, Player player) {
        try {
            // Validiere Trade
            if (!canExecuteTrade(trade, player)) {
                player.sendMessage("§cDieser Handel kann nicht ausgeführt werden!");
                return false;
            }

            // Hole TradeSet-Daten
            var tradeClass = trade.getClass();
            var getInput1 = tradeClass.getMethod("getInput1");
            var getOutput = tradeClass.getMethod("getOutput");

            ItemStack input = (ItemStack) getInput1.invoke(trade);
            ItemStack output = (ItemStack) getOutput.invoke(trade);

            // Hole NPC-ID aus Kontext - WORKAROUND für Single-NPC
            UUID npcId = npcPlotMap.keySet().stream().findFirst().orElse(null);
            if (npcId == null) {
                logger.warning("No NPC found for trade execution");
                player.sendMessage("§cFehler: NPC nicht gefunden!");
                return false;
            }

            // Hole Plot
            Plot plot = npcPlotMap.get(npcId);
            if (plot == null) {
                logger.warning("NPC " + npcId + " has no plot assigned");
                player.sendMessage("§cFehler: Grundstück nicht gefunden!");
                return false;
            }

            // 1. Entferne Input aus Spieler-Inventar (Münzen)
            player.getInventory().removeItem(input);

            // 2. Füge Output zu Spieler-Inventar hinzu (gekauftes Item)
            player.getInventory().addItem(output);

            // 3. Füge Input zu Plot-Storage hinzu (Münzen ins Input-Chest)
            addItemToPlotInputChest(plot, input);

            // 4. Entferne Output aus Plot-Storage (Item aus Output-Chest)
            removeItemFromPlotOutputChest(plot, output);

            // Cache invalidieren (TradeSets haben sich geändert)
            invalidateCache(npcId);

            // Erfolgs-Nachricht
            player.sendMessage("§a✓ Handel erfolgreich! Du hast " + output.getAmount() + "x " +
                             output.getType() + " für " + input.getAmount() + " Münzen gekauft.");

            logger.info("Player " + player.getName() + " traded " + input.getAmount() +
                       " coins for " + output.getAmount() + "x " + output.getType() +
                       " at plot " + plot.getIdentifier());

            return true;

        } catch (Exception e) {
            logger.severe("Failed to execute trade: " + e.getMessage());
            e.printStackTrace();
            player.sendMessage("§cFehler beim Ausführen des Handels!");
            return false;
        }
    }

    @Override
    public String getName() {
        return getDisplayName();
    }

    @Override
    public TradingEntityType getEntityType() {
        return TradingEntityType.GUILD_TRADER;
    }

    // ==================== Gildenhändler-spezifische Methoden ====================

    /**
     * Registriert einen NPC für ein Grundstück.
     *
     * @param npcId UUID des NPCs
     * @param plot Das Grundstück
     */
    public void registerNPCForPlot(UUID npcId, Plot plot) {
        npcPlotMap.put(npcId, plot);

        // Invalidiere Cache
        tradeSetCache.remove(npcId);

        logger.info("Registered GuildTrader " + npcId + " for plot " + plot.getUuid());
    }

    /**
     * Entfernt einen NPC.
     *
     * @param npcId UUID des NPCs
     */
    public void unregisterNPC(UUID npcId) {
        npcPlotMap.remove(npcId);
        tradeSetCache.remove(npcId);

        logger.info("Unregistered GuildTrader " + npcId);
    }

    /**
     * Öffnet das Trading-UI für einen Spieler mit einer NPC-spezifischen Instanz.
     *
     * Nutzt das TradeUI aus dem Core-Modul (Reflection-basiert).
     * Falls Core nicht verfügbar → Fallback zu einfacher Chat-Liste.
     *
     * @param player Der Spieler
     * @param traderInstance Die NPC-spezifische TradingEntity-Instanz
     */
    private void openTradeUIForInstance(Player player, GuildTraderInstance traderInstance) {
        try {
            // Prüfe ob Core-Plugin geladen ist
            var corePlugin = Bukkit.getPluginManager().getPlugin("FallenStar-Core");
            if (corePlugin == null) {
                // Fallback: Zeige einfache Nachricht
                player.sendMessage("§eCore-Plugin nicht geladen - Trading UI nicht verfügbar");
                showSimpleTradeList(player, traderInstance.getNpcId());
                return;
            }

            // Reflection: Hole TradeUI-Klasse
            Class<?> tradeUIClass = Class.forName("de.fallenstar.core.ui.TradeUI");

            // Reflection: Hole openTradeUI(Player, TradingEntity) Methode
            var openMethod = tradeUIClass.getMethod("openTradeUI", Player.class,
                Class.forName("de.fallenstar.core.provider.TradingEntity"));

            // Aufruf: TradeUI.openTradeUI(player, traderInstance)
            openMethod.invoke(null, player, traderInstance);

            logger.fine("Opened TradeUI for player " + player.getName() +
                       " with GuildTrader " + traderInstance.getNpcId());

        } catch (ClassNotFoundException e) {
            // TradeUI-Klasse nicht gefunden → Fallback
            logger.warning("TradeUI class not found - Core module may not be loaded correctly");
            player.sendMessage("§eTrading-UI nicht verfügbar");
            showSimpleTradeList(player, traderInstance.getNpcId());

        } catch (NoSuchMethodException e) {
            // openTradeUI Methode nicht gefunden → API-Änderung?
            logger.warning("TradeUI.openTradeUI() method not found - Core API may have changed");
            player.sendMessage("§cFehler: Trading-UI API inkompatibel!");
            showSimpleTradeList(player, traderInstance.getNpcId());

        } catch (Exception e) {
            // Unerwarteter Fehler
            logger.warning("Failed to open TradeUI: " + e.getMessage());
            e.printStackTrace();
            player.sendMessage("§cFehler beim Öffnen des Handels!");
            showSimpleTradeList(player, traderInstance.getNpcId());
        }
    }

    /**
     * Zeigt eine einfache Trade-Liste im Chat.
     *
     * @param player Der Spieler
     * @param npcId UUID des NPCs
     */
    private void showSimpleTradeList(Player player, UUID npcId) {
        List<Object> tradeSets = getTradeSets(npcId);

        player.sendMessage("§8§m---------§r §6" + getDisplayName() + " §8§m---------");

        if (tradeSets.isEmpty()) {
            player.sendMessage("§cKeine Waren verfügbar!");
            player.sendMessage("§7Der Händler hat aktuell nichts zu verkaufen.");
        } else {
            player.sendMessage("§7Verfügbare Waren: §e" + tradeSets.size());
            player.sendMessage("§7(Trading-UI noch nicht implementiert)");
        }

        player.sendMessage("§8§m--------------------------------------");
    }

    /**
     * Generiert TradeSets aus Plot-Storage.
     *
     * Ablauf:
     * 1. Hole alle Items aus Output-Chests
     * 2. Für jedes Item: Preis aus PlotPriceManager laden (plot-basiert!)
     * 3. Erstelle TradeSet (Item + Münzen)
     *
     * @param plot Das Grundstück
     * @param npcId Die NPC-ID
     * @return Liste von TradeSets
     */
    private List<Object> generateTradeSetsFromPlotStorage(Plot plot, UUID npcId) {
        List<Object> tradeSets = new ArrayList<>();

        try {
            logger.info("=== Generating TradeSets for plot " + plot.getUuid() + " ===");

            // Hole Items aus Plot-Storage
            Optional<List<ItemStack>> storageItems = getPlotStorageInventory(npcId);

            if (storageItems.isEmpty()) {
                logger.warning("getPlotStorageInventory returned empty Optional");
                return tradeSets;
            }

            List<ItemStack> items = storageItems.get();
            if (items.isEmpty()) {
                logger.info("No items in plot storage for plot " + plot.getUuid());
                return tradeSets;
            }

            logger.info("Found " + items.size() + " items in plot storage");

            // Hole EconomyProvider (Core-Provider statt Reflection!)
            de.fallenstar.core.provider.EconomyProvider economyProvider = providers.getEconomyProvider();
            if (economyProvider == null || !economyProvider.isAvailable()) {
                logger.warning("Economy-Provider not available - cannot generate prices");
                return tradeSets;
            }

            logger.info("Using EconomyProvider for price lookup");

            // Hole Economy-Plugin für Münz-Erstellung
            var economyPlugin = Bukkit.getPluginManager().getPlugin("FallenStar-Economy");
            if (economyPlugin == null) {
                logger.warning("Economy module not loaded - cannot create coins");
                return tradeSets;
            }

            // Für jedes Item: Erstelle TradeSet
            for (ItemStack item : items) {
                if (item == null || item.getType() == Material.AIR) {
                    continue;
                }

                logger.info("Processing item: " + item.getType() + " x" + item.getAmount());

                // Hole Verkaufspreis für Item (aus EconomyProvider!)
                BigDecimal price = getItemSellPrice(economyProvider, item);

                logger.info("  Price for " + item.getType() + ": " + price);

                if (price.compareTo(BigDecimal.ZERO) <= 0) {
                    logger.info("  No price set - skipping");
                    continue;
                }

                // Erstelle Münzen für den Preis
                ItemStack coinStack = createCoinsForPrice(economyPlugin, price);
                if (coinStack == null) {
                    logger.warning("Failed to create coins for price " + price);
                    continue;
                }

                // Erstelle TradeSet via Reflection (Economy-Modul)
                try {
                    Class<?> tradeSetClass = Class.forName("de.fallenstar.economy.model.TradeSet");

                    // Konstruktor: (ItemStack input1, ItemStack input2, ItemStack output,
                    //                BigDecimal buyPrice, BigDecimal sellPrice, int maxUses)
                    var constructor = tradeSetClass.getConstructor(
                        ItemStack.class, ItemStack.class, ItemStack.class,
                        BigDecimal.class, BigDecimal.class, int.class
                    );

                    // Spieler kauft Item vom NPC: Münzen → Item
                    // Input: Münzen, Output: Item
                    Object tradeSet = constructor.newInstance(
                        coinStack,              // Input1: Münzen (Spieler zahlt)
                        null,                   // Input2: Keiner
                        item.clone(),           // Output: Item (Spieler erhält)
                        price,                  // Buy Price (nicht verwendet für Verkauf)
                        price,                  // Sell Price (Spieler zahlt)
                        -1                      // Unbegrenzte Trades
                    );

                    tradeSets.add(tradeSet);
                    logger.fine("Generated TradeSet for " + item.getType() + " @ " + price + " Sterne");

                } catch (Exception e) {
                    logger.warning("Failed to create TradeSet for " + item.getType() + ": " + e.getMessage());
                }
            }

        } catch (Exception e) {
            logger.warning("Failed to generate TradeSets: " + e.getMessage());
            e.printStackTrace();
        }

        return tradeSets;
    }

    /**
     * Holt den Verkaufspreis für ein Item aus dem EconomyProvider.
     *
     * Der Verkaufspreis ist der Preis, den der Spieler zahlt um das Item vom NPC zu kaufen.
     *
     * @param economyProvider EconomyProvider-Instanz
     * @param item Das Item
     * @return Verkaufspreis in Basiswährung
     */
    private BigDecimal getItemSellPrice(de.fallenstar.core.provider.EconomyProvider economyProvider, ItemStack item) {
        try {
            Material material = item.getType();

            logger.info("    getItemSellPrice: material=" + material);

            // Hole Verkaufspreis vom EconomyProvider (Type-Safe!)
            Optional<BigDecimal> priceOpt = economyProvider.getSellPrice(material);

            logger.info("    getSellPrice returned: " + priceOpt);

            return priceOpt.orElse(BigDecimal.ZERO);

        } catch (Exception e) {
            logger.warning("Failed to get sell price for " + item.getType() + ": " + e.getMessage());
            e.printStackTrace();
            return BigDecimal.ZERO;
        }
    }

    /**
     * Erstellt Münz-ItemStack für einen Preis.
     *
     * Nutzt CurrencyManager aus Economy-Modul via Reflection.
     *
     * @param economyPlugin Economy-Plugin-Instanz
     * @param price Preis in Basiswährung
     * @return ItemStack mit Münzen oder null bei Fehler
     */
    private ItemStack createCoinsForPrice(Object economyPlugin, BigDecimal price) {
        try {
            // Hole CurrencyManager
            var getCurrencyManager = economyPlugin.getClass().getMethod("getCurrencyManager");
            var currencyManager = getCurrencyManager.invoke(economyPlugin);

            // Konvertiere Preis zu int (Sterne sind ganzzahlig)
            int amount = price.intValue();

            // Erstelle Münzen via createCoin(String currencyId, CurrencyTier tier, int amount)
            // Tier bestimmen: Bronze (1-99), Silber (100-9999), Gold (10000+)
            String tier = "BRONZE";
            if (amount >= 10000) {
                tier = "GOLD";
                amount = amount / 100; // Gold = 100 Sterne
            } else if (amount >= 100) {
                tier = "SILVER";
                amount = amount / 10; // Silber = 10 Sterne
            }

            // Reflection: createCoin(String, CurrencyTier, int)
            // CurrencyTier ist eine innere Klasse von CurrencyItemSet
            Class<?> currencyTierClass = Class.forName("de.fallenstar.economy.model.CurrencyItemSet$CurrencyTier");
            var tierEnum = Enum.valueOf((Class<Enum>) currencyTierClass, tier);

            var createCoinMethod = currencyManager.getClass().getMethod(
                "createCoin", String.class, currencyTierClass, int.class
            );

            ItemStack coinStack = (ItemStack) createCoinMethod.invoke(
                currencyManager, "sterne", tierEnum, amount
            );

            return coinStack;

        } catch (Exception e) {
            logger.warning("Failed to create coins: " + e.getMessage());
            return null;
        }
    }

    /**
     * Fügt ein Item zur Input-Chest eines Plots hinzu (Münzen vom Kauf).
     *
     * @param plot Der Plot
     * @param item Das Item
     */
    private void addItemToPlotInputChest(Plot plot, ItemStack item) {
        try {
            var plotsPlugin = Bukkit.getPluginManager().getPlugin("FallenStar-Plots");
            if (plotsPlugin == null) {
                logger.warning("Plots module not loaded - cannot add item to input chest");
                return;
            }

            // Reflection: hole PlotStorageProvider
            var getStorageProvider = plotsPlugin.getClass().getMethod("getPlotStorageProvider");
            var storageProvider = getStorageProvider.invoke(plotsPlugin);

            // Reflection: addToInputChests(Plot, ItemStack)
            var addMethod = storageProvider.getClass().getMethod("addToInputChests", Plot.class, ItemStack.class);
            boolean success = (boolean) addMethod.invoke(storageProvider, plot, item);

            if (success) {
                logger.fine("Added " + item.getAmount() + "x " + item.getType() + " to input chest");
            } else {
                logger.warning("Failed to add item to input chest - no chest configured?");
            }

        } catch (Exception e) {
            logger.warning("Failed to add item to input chest: " + e.getMessage());
        }
    }

    /**
     * Entfernt ein Item aus den Output-Chests eines Plots (verkauftes Item).
     *
     * @param plot Der Plot
     * @param item Das Item (type + amount werden geprüft)
     */
    private void removeItemFromPlotOutputChest(Plot plot, ItemStack item) {
        try {
            var plotsPlugin = Bukkit.getPluginManager().getPlugin("FallenStar-Plots");
            if (plotsPlugin == null) {
                logger.warning("Plots module not loaded - cannot remove item from output chest");
                return;
            }

            // Reflection: hole PlotStorageProvider
            var getStorageProvider = plotsPlugin.getClass().getMethod("getPlotStorageProvider");
            var storageProvider = getStorageProvider.invoke(plotsPlugin);

            // Reflection: hole PlotStorage
            var getStorage = storageProvider.getClass().getMethod("getPlotStorage", Plot.class);
            var plotStorage = getStorage.invoke(storageProvider, plot);

            if (plotStorage == null) {
                logger.warning("No storage found for plot " + plot.getIdentifier());
                return;
            }

            // Reflection: getOutputChests()
            var getOutputChests = plotStorage.getClass().getMethod("getOutputChests");
            @SuppressWarnings("unchecked")
            var outputChests = (java.util.List<?>) getOutputChests.invoke(plotStorage);

            // Durchsuche Output-Chests und entferne Item
            int remainingAmount = item.getAmount();

            for (Object chestDataObj : outputChests) {
                if (remainingAmount <= 0) break;

                // Hole Location der Chest
                var getLocation = chestDataObj.getClass().getMethod("getLocation");
                org.bukkit.Location chestLocation = (org.bukkit.Location) getLocation.invoke(chestDataObj);

                // Prüfe ob Truhe vorhanden
                if (chestLocation.getBlock().getState() instanceof org.bukkit.block.Chest) {
                    org.bukkit.block.Chest chest = (org.bukkit.block.Chest) chestLocation.getBlock().getState();
                    org.bukkit.inventory.Inventory inv = chest.getInventory();

                    // Entferne Item aus dieser Chest
                    ItemStack toRemove = item.clone();
                    toRemove.setAmount(remainingAmount);

                    var removed = inv.removeItem(toRemove);

                    // Berechne wie viel entfernt wurde
                    int removedAmount = remainingAmount - (removed.isEmpty() ? 0 : removed.values().iterator().next().getAmount());
                    remainingAmount -= removedAmount;

                    logger.fine("Removed " + removedAmount + "x " + item.getType() + " from output chest");
                }
            }

            if (remainingAmount > 0) {
                logger.warning("Could only remove " + (item.getAmount() - remainingAmount) + "/" + item.getAmount() + " items from output chests");
            }

        } catch (Exception e) {
            logger.warning("Failed to remove item from output chest: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Invalidiert den TradeSet-Cache für einen NPC.
     *
     * Sollte aufgerufen werden wenn sich das Plot-Storage-Inventar ändert.
     *
     * @param npcId UUID des NPCs
     */
    public void invalidateCache(UUID npcId) {
        tradeSetCache.remove(npcId);
        logger.fine("Invalidated TradeSet cache for NPC " + npcId);
    }

    /**
     * Invalidiert den Cache für alle NPCs auf einem Grundstück.
     *
     * @param plot Das Grundstück
     */
    public void invalidateCacheForPlot(Plot plot) {
        npcPlotMap.entrySet().stream()
            .filter(e -> e.getValue().equals(plot))
            .map(Map.Entry::getKey)
            .forEach(this::invalidateCache);
    }

    // ==================== Inner Class: GuildTraderInstance ====================

    /**
     * NPC-spezifische TradingEntity-Instanz für einen einzelnen Gildenhändler.
     *
     * Diese Klasse wraps einen spezifischen NPC und delegiert an GuildTraderNPC.
     * Sie ermöglicht es dem TradeUI, TradeSets für einen konkreten NPC abzurufen.
     *
     * **Architektur:**
     * - GuildTraderNPC = NPC-Typ (verwaltet ALLE Gildenhändler)
     * - GuildTraderInstance = Einzelner NPC (TradingEntity für TradeUI)
     *
     * **Verwendung:**
     * <pre>
     * GuildTraderInstance instance = new GuildTraderInstance(npcId);
     * TradeUI.openTradeUI(player, instance);
     * </pre>
     */
    public class GuildTraderInstance implements TradingEntity {

        private final UUID npcId;

        /**
         * Erstellt eine neue GuildTraderInstance.
         *
         * @param npcId UUID des NPCs
         */
        public GuildTraderInstance(UUID npcId) {
            this.npcId = npcId;
        }

        /**
         * Gibt die NPC-ID zurück.
         *
         * @return NPC-UUID
         */
        public UUID getNpcId() {
            return npcId;
        }

        @Override
        public List<?> getTradeSets() {
            // Delegiere an GuildTraderNPC mit NPC-ID
            return GuildTraderNPC.this.getTradeSets(npcId);
        }

        @Override
        public Optional<Inventory> getTradeInventory() {
            // Gildenhändler nutzen Plot-Storage (List<ItemStack>), nicht Bukkit Inventory
            // Daher geben wir Empty zurück (wie im TradingEntity-Interface dokumentiert)
            return Optional.empty();
        }

        @Override
        public boolean canExecuteTrade(Object trade, Player player) {
            // Delegiere an GuildTraderNPC
            return GuildTraderNPC.this.canExecuteTrade(trade, player);
        }

        @Override
        public boolean executeTrade(Object trade, Player player) {
            // Delegiere an GuildTraderNPC
            return GuildTraderNPC.this.executeTrade(trade, player);
        }

        @Override
        public String getName() {
            // Delegiere an GuildTraderNPC
            return GuildTraderNPC.this.getName();
        }

        @Override
        public TradingEntityType getEntityType() {
            // Delegiere an GuildTraderNPC
            return GuildTraderNPC.this.getEntityType();
        }
    }
}
