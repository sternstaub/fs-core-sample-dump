package de.fallenstar.plot.integration;

import com.palmergames.bukkit.towny.event.TownBlockTypeRegisterEvent;
import com.palmergames.bukkit.towny.object.TownBlockType;
import com.palmergames.bukkit.towny.object.TownBlockTypeHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Towny-Integration für das Plot-Modul.
 *
 * Registriert Custom-Plot-Typen in Towny.
 *
 * @author FallenStar
 * @version 1.0
 */
public class TownyIntegration implements Listener {

    private final JavaPlugin plugin;

    public TownyIntegration(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Event-Handler für Towny TownBlockTypeRegisterEvent.
     * Registriert den Custom-Plot-Typ "botschaft".
     */
    @EventHandler
    public void onTownBlockTypeRegister(TownBlockTypeRegisterEvent event) {
        try {
            // Erstelle TownBlockType für "botschaft"
            TownBlockType botschaftType = new TownBlockType("botschaft", "Botschaft");

            // Registriere über TownBlockTypeHandler
            TownBlockTypeHandler.registerType(botschaftType);

            plugin.getLogger().info("✓ Custom-Plot-Typ 'botschaft' in Towny registriert");

        } catch (Exception e) {
            plugin.getLogger().warning("✗ Fehler beim Registrieren von 'botschaft': " + e.getMessage());
        }
    }
}
