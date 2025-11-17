package de.fallenstar.items;

import de.fallenstar.core.event.ProvidersReadyEvent;
import de.fallenstar.core.registry.ProviderRegistry;
import de.fallenstar.core.registry.UIRegistry;
import de.fallenstar.items.command.ItemsCommand;
import de.fallenstar.items.manager.SpecialItemManager;
import de.fallenstar.items.provider.MMOItemsItemProvider;
import de.fallenstar.items.ui.ItemBrowserUI;
import de.fallenstar.items.ui.TestTradeUI;
import org.bukkit.Material;
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

        // Erstelle SpecialItemManager v3.0 (generisches SpecialItem-System)
        // Unterstützt Vanilla SpecialItems + MMOItems (optional)
        specialItemManager = new SpecialItemManager(this, getLogger(), itemProvider);
        getLogger().info("✓ SpecialItemManager v3.0 initialized (Generisches SpecialItem-System)");

        // Registriere Basiswährung "Sterne"
        registerBaseCurrency();

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
            TestTradeUI tradeListener = new TestTradeUI(this, itemProvider, specialItemManager);

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
                    () -> new TestTradeUI(this, itemProvider, specialItemManager)
            );

            getLogger().info("✓ Test-UIs registered (items-browser, items-trade-test)");
        } else {
            getLogger().info("✗ Test-UIs not registered (MMOItems not available)");
            getLogger().info("  Vanilla currency items still work in other UIs!");
        }
    }

    /**
     * Registriert die Basiswährung "Sterne" (Bronze/Silber/Gold).
     *
     * Diese Methode registriert die drei Tier-Items der Basis-Währung:
     * - Bronzestern (1er Münze, Wert: 1) - COPPER_NUGGET (Kupferklumpen)
     * - Silberstern (10er Münze, Wert: 10) - IRON_NUGGET (Eisenklumpen)
     * - Goldstern (100er Münze, Wert: 100) - GOLD_NUGGET (Goldklumpen)
     *
     * Diese werden später vom Economy-Modul als CurrencyItemSet verwendet.
     */
    private void registerBaseCurrency() {
        getLogger().info("Registriere Basiswährung 'Sterne'...");

        // Lade Materialien aus Config (mit Defaults)
        String bronzeMaterialName = getConfig().getString("currency.bronze-tier-material", "COPPER_INGOT");
        String silverMaterialName = getConfig().getString("currency.silver-tier-material", "IRON_INGOT");
        String goldMaterialName = getConfig().getString("currency.gold-tier-material", "GOLD_INGOT");

        Material bronzeMaterial = parseMaterial(bronzeMaterialName, Material.COPPER_INGOT);
        Material silverMaterial = parseMaterial(silverMaterialName, Material.IRON_INGOT);
        Material goldMaterial = parseMaterial(goldMaterialName, Material.GOLD_INGOT);

        // Bronzestern (1er Münze) - Kupferbarren (default)
        specialItemManager.registerVanillaItem(
                "bronze_stern",
                bronzeMaterial,
                1,  // Custom Model Data
                net.kyori.adventure.text.Component.text("Bronzestern")
                        .color(net.kyori.adventure.text.format.NamedTextColor.GOLD)
                        .decoration(net.kyori.adventure.text.format.TextDecoration.BOLD, true),
                java.util.List.of(
                        net.kyori.adventure.text.Component.text("Wert: 1", net.kyori.adventure.text.format.NamedTextColor.GRAY),
                        net.kyori.adventure.text.Component.empty(),
                        net.kyori.adventure.text.Component.text("Basiswährung des Reiches", net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY)
                )
        );

        // Silberstern (10er Münze) - Eisenbarren (default)
        specialItemManager.registerVanillaItem(
                "silver_stern",
                silverMaterial,
                2,  // Custom Model Data
                net.kyori.adventure.text.Component.text("Silberstern")
                        .color(net.kyori.adventure.text.format.NamedTextColor.AQUA)
                        .decoration(net.kyori.adventure.text.format.TextDecoration.BOLD, true),
                java.util.List.of(
                        net.kyori.adventure.text.Component.text("Wert: 10", net.kyori.adventure.text.format.NamedTextColor.GRAY),
                        net.kyori.adventure.text.Component.empty(),
                        net.kyori.adventure.text.Component.text("Handelswährung des Reiches", net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY)
                )
        );

        // Goldstern (100er Münze) - Goldbarren (default)
        specialItemManager.registerVanillaItem(
                "gold_stern",
                goldMaterial,
                3,  // Custom Model Data
                net.kyori.adventure.text.Component.text("Goldstern")
                        .color(net.kyori.adventure.text.format.NamedTextColor.YELLOW)
                        .decoration(net.kyori.adventure.text.format.TextDecoration.BOLD, true),
                java.util.List.of(
                        net.kyori.adventure.text.Component.text("Wert: 100", net.kyori.adventure.text.format.NamedTextColor.GRAY),
                        net.kyori.adventure.text.Component.empty(),
                        net.kyori.adventure.text.Component.text("Edelwährung des Reiches", net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY)
                )
        );

        getLogger().info("✓ Basiswährung 'Sterne' registriert (bronze_stern, silver_stern, gold_stern)");
        getLogger().info("  - Bronze: " + bronzeMaterial.name());
        getLogger().info("  - Silber: " + silverMaterial.name());
        getLogger().info("  - Gold: " + goldMaterial.name());
    }

    /**
     * Parst ein Material aus einem String.
     *
     * @param materialName Material-Name
     * @param fallback Fallback-Material
     * @return Material
     */
    private Material parseMaterial(String materialName, Material fallback) {
        try {
            return Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            getLogger().warning("Ungültiges Material in config.yml: " + materialName + " - verwende " + fallback.name());
            return fallback;
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
