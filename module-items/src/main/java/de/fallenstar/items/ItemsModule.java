package de.fallenstar.items;

import de.fallenstar.core.event.ProvidersReadyEvent;
import de.fallenstar.core.registry.ProviderRegistry;
import de.fallenstar.items.command.ItemsCommand;
import de.fallenstar.items.manager.SpecialItemManager;
import de.fallenstar.items.provider.MMOItemsItemProvider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * FallenStar Items Module - MMOItems Integration.
 *
 * Verantwortlichkeiten:
 * - MMOItemsItemProvider bereitstellen
 * - Spezial-Items verwalten (Münzen, UI-Buttons)
 * - Item-Browser und Admin-Commands
 *
 * @author FallenStar
 * @version 1.0
 */
public class ItemsModule extends JavaPlugin implements Listener {

    private ProviderRegistry providers;
    private MMOItemsItemProvider itemProvider;
    private SpecialItemManager specialItemManager;

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

        // Prüfe ob MMOItems verfügbar ist
        if (!checkMMOItemsAvailable()) {
            getLogger().severe("MMOItems plugin not found! Disabling module...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Erstelle MMOItems-Provider
        itemProvider = new MMOItemsItemProvider(getLogger());
        getLogger().info("✓ MMOItemsItemProvider initialized");

        // Erstelle SpecialItemManager
        specialItemManager = new SpecialItemManager(itemProvider, getLogger());
        getLogger().info("✓ SpecialItemManager initialized");

        // Registriere Commands
        registerCommands();

        // TODO: Registriere Test-UIs in UIRegistry
        // registerTestUIs();

        getLogger().info("✓ Items Module enabled!");
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
