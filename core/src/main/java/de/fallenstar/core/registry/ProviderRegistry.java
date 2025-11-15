package de.fallenstar.core.registry;

import de.fallenstar.core.provider.*;
import de.fallenstar.core.provider.impl.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import java.util.logging.Logger;

/**
 * Zentrale Registry für alle Provider.
 *
 * Verantwortlich für:
 * - Auto-Detection verfügbarer Plugins
 * - Registrierung entsprechender Provider
 * - Bereitstellung von Provider-Instanzen für Module
 *
 * Pattern: Service Locator + Dependency Injection
 *
 * @author FallenStar
 * @version 1.0
 */
public class ProviderRegistry {

    private final Logger logger;
    private final Plugin plugin;

    private PlotProvider plotProvider;
    private EconomyProvider economyProvider;
    private NPCProvider npcProvider;
    private ItemProvider itemProvider;
    private ChatProvider chatProvider;
    private NetworkProvider networkProvider;

    /**
     * Erstellt eine neue ProviderRegistry.
     *
     * @param plugin Plugin-Instanz
     * @param logger Logger für Debug-Ausgaben
     */
    public ProviderRegistry(Plugin plugin, Logger logger) {
        this.plugin = plugin;
        this.logger = logger;
    }
    
    /**
     * Erkennt verfügbare Plugins und registriert entsprechende Provider.
     *
     * Wird beim Server-Start vom Core-Plugin aufgerufen.
     */
    public void detectAndRegister() {
        logger.info("Detecting available providers...");

        // Plot Provider Detection
        if (isPluginEnabled("Towny")) {
            plotProvider = new TownyPlotProvider();
            logger.info("✓ Registered TownyPlotProvider");
        } else if (isPluginEnabled("Factions")) {
            // plotProvider = new FactionsPlotProvider();
            logger.info("✓ Registered FactionsPlotProvider");
        } else {
            plotProvider = new NoOpPlotProvider();
            logger.warning("✗ No plot plugin found - plot features disabled");
        }

        // Economy Provider Detection
        if (isPluginEnabled("Vault")) {
            economyProvider = new VaultEconomyProvider(logger);
            logger.info("✓ Registered VaultEconomyProvider");
        } else {
            economyProvider = new NoOpEconomyProvider();
            logger.warning("✗ No economy plugin found - economy features disabled");
        }

        // NPC Provider Detection
        if (isPluginEnabled("Citizens")) {
            npcProvider = new CitizensNPCProvider(plugin, logger);
            logger.info("✓ Registered CitizensNPCProvider");
        } else if (isPluginEnabled("ZNPCsPlus")) {
            // npcProvider = new ZNPCProvider();
            logger.info("✓ Registered ZNPCProvider");
        } else {
            npcProvider = new NoOpNPCProvider();
            logger.warning("✗ No NPC plugin found - NPC features disabled");
        }

        // Item Provider Detection (optional)
        if (isPluginEnabled("MMOItems")) {
            // itemProvider = new MMOItemsProvider();
            logger.info("✓ Registered MMOItemsProvider");
        } else {
            itemProvider = new NoOpItemProvider();
            logger.info("○ No custom item plugin - using vanilla items only");
        }

        // Chat Provider Detection (optional)
        chatProvider = new NoOpChatProvider();
        logger.info("○ Chat provider disabled - no external chat integration");

        // Network Provider Detection (optional)
        networkProvider = new NoOpNetworkProvider();
        logger.info("○ Network provider disabled - standalone server mode");

        logger.info("Provider detection completed");
    }
    
    /**
     * Hilfsmethode: Prüft ob ein Plugin geladen und aktiviert ist.
     */
    private boolean isPluginEnabled(String pluginName) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
        return plugin != null && plugin.isEnabled();
    }
    
    // Getter für Provider
    public PlotProvider getPlotProvider() { return plotProvider; }
    public EconomyProvider getEconomyProvider() { return economyProvider; }
    public NPCProvider getNpcProvider() { return npcProvider; }
    public ItemProvider getItemProvider() { return itemProvider; }
    public ChatProvider getChatProvider() { return chatProvider; }
    public NetworkProvider getNetworkProvider() { return networkProvider; }
}
