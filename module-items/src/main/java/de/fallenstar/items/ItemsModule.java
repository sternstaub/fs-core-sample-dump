package de.fallenstar.items;

import de.fallenstar.core.event.ProvidersReadyEvent;
import de.fallenstar.core.registry.ProviderRegistry;
import de.fallenstar.core.registry.UIRegistry;
import de.fallenstar.items.command.ItemsCommand;
import de.fallenstar.items.manager.SpecialItemManager;
import de.fallenstar.items.provider.MMOItemsItemProvider;
import de.fallenstar.items.ui.ItemBrowserUI;
import de.fallenstar.items.ui.TestTradeUI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * FallenStar Items Module - Vanilla Currency Items + Optional MMOItems Integration.
 *
 * Verantwortlichkeiten:
 * - Vanilla Währungs-Items (Bronze/Silver/Gold Coins) - IMMER verfügbar
 * - OPTIONAL: MMOItemsItemProvider (nur wenn MMOItems installiert)
 * - Item-Browser und Admin-Commands
 *
 * Graceful Degradation:
 * - Modul läuft OHNE MMOItems (nur Vanilla Coins)
 * - Mit MMOItems: Volle Custom-Item-Unterstützung
 *
 * @author FallenStar
 * @version 2.0
 */
public class ItemsModule extends JavaPlugin implements Listener {

    private ProviderRegistry providers;
    private MMOItemsItemProvider itemProvider; // OPTIONAL!
    private SpecialItemManager specialItemManager; // IMMER verfügbar
    private boolean mmoItemsAvailable = false;

    @Override
    public void onEnable() {
        getLogger().info("╔════════════════════════════════════╗");
        getLogger().info("║ FallenStar Items Module v1.0      ║");
        getLogger().info("║ MMOItems Integration               ║");
        getLogger().info("╚════════════════════════════════════╝");

        // Config laden/erstellen
        saveDefaultConfig();

        // Event-Listener registrieren
        getServer().getPluginManager().registerEvents(this, this);
    }

    /**
     * Wird gefeuert wenn Core-Provider bereit sind.
     */
    @EventHandler
    public void onProvidersReady(ProvidersReadyEvent event) {
        this.providers = event.getRegistry();

        // Prüfe ob MMOItems verfügbar ist (OPTIONAL!)
        mmoItemsAvailable = checkMMOItemsAvailable();

        if (mmoItemsAvailable) {
            try {
                // Erstelle MMOItems-Provider
                itemProvider = new MMOItemsItemProvider(getLogger());
                getLogger().info("✓ MMOItemsItemProvider initialized");

                // Registriere Provider bei der ProviderRegistry
                providers.setItemProvider(itemProvider);
                getLogger().info("✓ MMOItemsItemProvider registered with ProviderRegistry");
            } catch (Exception e) {
                getLogger().warning("✗ Failed to initialize MMOItems Provider: " + e.getMessage());
                getLogger().warning("  Falling back to Vanilla-only mode");
                mmoItemsAvailable = false;
            }
        } else {
            getLogger().warning("✗ MMOItems not found - running in Vanilla-only mode");
            getLogger().info("  Currency items (coins) will still work!");
        }

        // Erstelle SpecialItemManager (Vanilla Items, KEINE MMOItems-Dependency!)
        // Läuft IMMER, auch ohne MMOItems
        specialItemManager = new SpecialItemManager(this, getLogger());
        getLogger().info("✓ SpecialItemManager initialized (Vanilla Currency Items)");

        // Registriere Commands
        registerCommands();

        // Registriere Test-UIs in UIRegistry
        registerTestUIs();

        getLogger().info("═══════════════════════════════════════");
        if (mmoItemsAvailable) {
            getLogger().info("✓ Items Module enabled (Full Mode - MMOItems + Vanilla)");
        } else {
            getLogger().info("✓ Items Module enabled (Vanilla Mode - Coins only)");
        }
        getLogger().info("═══════════════════════════════════════");
    }

    /**
     * Prüft ob MMOItems verfügbar ist.
     */
    private boolean checkMMOItemsAvailable() {
        Plugin mmoItems = getServer().getPluginManager().getPlugin("MMOItems");
        return mmoItems != null && mmoItems.isEnabled();
    }

    /**
     * Registriert Commands.
     */
    private void registerCommands() {
        ItemsCommand cmd = new ItemsCommand(this);
        getCommand("fsitems").setExecutor(cmd);
        getCommand("fsitems").setTabCompleter(cmd);
        getLogger().info("✓ Commands registered");
    }

    /**
     * Registriert Test-UIs in der UIRegistry.
     */
    private void registerTestUIs() {
        UIRegistry uiRegistry = providers.getUIRegistry();

        // Nur MMOItems-basierte UIs registrieren wenn MMOItems verfügbar
        if (mmoItemsAvailable && itemProvider != null) {
            // Erstelle Singleton-Listener-Instanzen für Event-Handling
            ItemBrowserUI browserListener = new ItemBrowserUI(itemProvider);
            TestTradeUI tradeListener = new TestTradeUI(itemProvider, specialItemManager);

            // Registriere als Event-Listener
            getServer().getPluginManager().registerEvents(browserListener, this);
            getServer().getPluginManager().registerEvents(tradeListener, this);

            // ItemBrowserUI registrieren
            uiRegistry.registerUI(
                    "items-browser",
                    "Item Browser",
                    "Durchstöbere alle Custom-Items nach Kategorien",
                    () -> new ItemBrowserUI(itemProvider)
            );

            // TestTradeUI registrieren (mit MMOItems)
            uiRegistry.registerUI(
                    "items-trade-test",
                    "Trade Test UI (MMOItems)",
                    "Demo-Händler mit Custom-Items und Münz-System",
                    () -> new TestTradeUI(itemProvider, specialItemManager)
            );

            getLogger().info("✓ Test-UIs registered (items-browser, items-trade-test)");
        } else {
            getLogger().info("✗ Test-UIs not registered (MMOItems not available)");
            getLogger().info("  Vanilla currency items still work in other UIs!");
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("Items Module shutting down...");
    }

    /**
     * API-Methode: Gibt den ItemProvider zurück.
     */
    public MMOItemsItemProvider getItemProvider() {
        return itemProvider;
    }

    /**
     * API-Methode: Gibt den SpecialItemManager zurück.
     */
    public SpecialItemManager getSpecialItemManager() {
        return specialItemManager;
    }
}
