package de.fallenstar.coin;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Coin-Modul - Münzen-Verwaltung (Dummy/Platzhalter).
 *
 * TODO:
 * - Geldmünzen als Items erstellen
 * - Auszahlung über Banker-NPC
 * - Integration mit ItemProvider
 * - Coin-Inventar-Verwaltung
 *
 * @author FallenStar
 * @version 1.0
 */
public class CoinModule extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("=== Coin Module Starting (PLACEHOLDER) ===");
        getLogger().warning("This module is a placeholder for future coin functionality.");
        getLogger().warning("Banker NPC functionality will be added later.");
    }

    @Override
    public void onDisable() {
        getLogger().info("Coin Module disabled");
    }
}
