package de.fallenstar.core.event;

import de.fallenstar.core.registry.ProviderRegistry;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event das gefeuert wird wenn alle Provider geladen und registriert sind.
 * 
 * Module sollten auf dieses Event warten bevor sie Provider nutzen.
 * 
 * Beispiel-Nutzung in einem Modul:
 * <pre>
 * @EventHandler
 * public void onProvidersReady(ProvidersReadyEvent event) {
 *     ProviderRegistry registry = event.getRegistry();
 *     
 *     // Check welche Provider verfügbar sind
 *     if (registry.getPlotProvider().isAvailable()) {
 *         enablePlotFeatures();
 *     }
 *     
 *     if (!registry.getEconomyProvider().isAvailable()) {
 *         getLogger().severe("Economy provider required!");
 *         Bukkit.getPluginManager().disablePlugin(this);
 *     }
 * }
 * </pre>
 * 
 * @author FallenStar
 * @version 1.0
 */
public class ProvidersReadyEvent extends Event {
    
    private static final HandlerList HANDLERS = new HandlerList();
    private final ProviderRegistry registry;
    
    /**
     * Erstellt ein neues ProvidersReadyEvent.
     * 
     * @param registry Die ProviderRegistry mit allen registrierten Providern
     */
    public ProvidersReadyEvent(ProviderRegistry registry) {
        this.registry = registry;
    }
    
    /**
     * @return Die ProviderRegistry
     */
    public ProviderRegistry getRegistry() {
        return registry;
    }
    
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
    
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
