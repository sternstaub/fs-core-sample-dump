package de.fallenstar.ui;

import de.fallenstar.core.event.ProvidersReadyEvent;
import de.fallenstar.core.registry.ProviderRegistry;
import de.fallenstar.core.registry.UIRegistry;
import de.fallenstar.ui.manager.UIButtonManager;
import de.fallenstar.ui.ui.ConfirmationUI;
import de.fallenstar.ui.ui.SimpleTradeUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * FallenStar UI-Modul (Sprint 7-8).
 *
 * Dieses Modul implementiert konkrete UIs:
 * - ConfirmationUI (Ja/Nein Dialog)
 * - SimpleTradeUI (Trading Demo mit Vanilla-Items)
 * - Weitere UIs folgen...
 *
 * Features:
 * - UIButtonManager für Special UI Items (Confirm, Cancel, Close, etc.)
 * - Registrierung aller UIs in der Core UIRegistry
 * - Test-UIs via /fscore admin gui <ui-id>
 *
 * @author FallenStar
 * @version 1.0
 */
public class UIModule extends JavaPlugin implements Listener {

    private ProviderRegistry providers;
    private UIButtonManager buttonManager;
    private UIRegistry uiRegistry;

    @Override
    public void onEnable() {
        getLogger().info("═══════════════════════════════════════");
        getLogger().info("  FallenStar UI-Modul");
        getLogger().info("  Version: " + getDescription().getVersion());
        getLogger().info("═══════════════════════════════════════");

        // Event-Listener registrieren
        getServer().getPluginManager().registerEvents(this, this);

        getLogger().info("UI-Modul gestartet - warte auf ProvidersReadyEvent...");
    }

    @Override
    public void onDisable() {
        getLogger().info("UI-Modul gestoppt.");
    }

    /**
     * Behandelt ProvidersReadyEvent vom Core-Plugin.
     *
     * @param event ProvidersReadyEvent
     */
    @EventHandler
    public void onProvidersReady(ProvidersReadyEvent event) {
        getLogger().info("ProvidersReadyEvent erhalten - initialisiere UI-Modul...");

        this.providers = event.getRegistry();

        // Hole UIRegistry vom Core
        try {
            JavaPlugin corePlugin = (JavaPlugin) getServer().getPluginManager().getPlugin("FallenStar-Core");
            if (corePlugin == null) {
                getLogger().severe("FallenStar-Core nicht gefunden!");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }

            // Reflection-Zugriff auf UIRegistry
            this.uiRegistry = (UIRegistry) corePlugin.getClass()
                    .getMethod("getUIRegistry")
                    .invoke(corePlugin);

            if (this.uiRegistry == null) {
                getLogger().severe("UIRegistry nicht verfügbar!");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }

        } catch (Exception e) {
            getLogger().severe("Fehler beim Zugriff auf UIRegistry: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Initialisiere Module
        initializeManagers();
        registerUIs();

        getLogger().info("═══════════════════════════════════════");
        getLogger().info("  UI-Modul erfolgreich initialisiert!");
        getLogger().info("═══════════════════════════════════════");
    }

    /**
     * Initialisiert alle Manager.
     */
    private void initializeManagers() {
        getLogger().info("Initialisiere Manager...");

        // UIButtonManager
        this.buttonManager = new UIButtonManager();
        this.buttonManager.initialize();
        getLogger().info("✓ UIButtonManager initialisiert");
    }

    /**
     * Registriert alle UIs in der UIRegistry.
     */
    private void registerUIs() {
        getLogger().info("Registriere UIs in UIRegistry...");

        // ConfirmationUI registrieren
        uiRegistry.registerUI(
                "confirm",
                "Bestätigungs-Dialog (Ja/Nein)",
                "Generisches Ja/Nein Confirmation UI mit UI-Buttons",
                () -> ConfirmationUI.createSimple(
                        buttonManager,
                        "Möchtest du diese Aktion ausführen?",
                        player -> player.sendMessage(Component.text("✓ Bestätigt!", NamedTextColor.GREEN))
                )
        );
        getLogger().info("✓ ConfirmationUI registriert (ID: confirm)");

        // SimpleTradeUI registrieren
        uiRegistry.registerUI(
                "trade",
                "Händler-Demo (Vanilla Items)",
                "Einfaches Trading-UI mit Vanilla-Items",
                () -> new SimpleTradeUI(buttonManager)
        );
        getLogger().info("✓ SimpleTradeUI registriert (ID: trade)");

        getLogger().info("Alle UIs registriert!");
        getLogger().info("Verwende: /fscore admin gui list");
    }

    /**
     * Getter für UIButtonManager.
     *
     * @return UIButtonManager
     */
    public UIButtonManager getButtonManager() {
        return buttonManager;
    }

    /**
     * Getter für ProviderRegistry.
     *
     * @return ProviderRegistry
     */
    public ProviderRegistry getProviders() {
        return providers;
    }
}
