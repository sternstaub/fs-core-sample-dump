package de.fallenstar.storage.command;

import de.fallenstar.core.exception.ProviderFunctionalityNotFoundException;
import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.provider.PlotProvider;
import de.fallenstar.core.registry.ProviderRegistry;
import de.fallenstar.storage.StorageModule;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Beispiel-Command für Storage-Modul.
 * 
 * Zeigt:
 * - Provider-Nutzung in Commands
 * - Exception-Handling
 * - Graceful Degradation
 * 
 * Command: /storage register
 * 
 * @author FallenStar
 * @version 1.0
 */
public class StorageRegisterCommand implements CommandExecutor {
    
    private final StorageModule plugin;
    private final ProviderRegistry providers;
    
    public StorageRegisterCommand(StorageModule plugin, ProviderRegistry providers) {
        this.plugin = plugin;
        this.providers = providers;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }
        
        Player player = (Player) sender;
        
        // Prüfe ob Spieler auf eine Truhe schaut
        Block targetBlock = player.getTargetBlockExact(5);
        if (targetBlock == null || targetBlock.getType() != Material.CHEST) {
            player.sendMessage("§cYou must be looking at a chest!");
            return true;
        }
        
        Chest chest = (Chest) targetBlock.getState();
        
        // Verwende PlotProvider wenn verfügbar
        PlotProvider plotProvider = providers.getPlotProvider();
        
        if (plotProvider.isAvailable()) {
            // Plot-basierte Validierung
            try {
                Plot plot = plotProvider.getPlot(chest.getLocation());
                
                if (plot == null) {
                    player.sendMessage("§cThis chest is not on a plot!");
                    return true;
                }
                
                if (!plotProvider.hasAdminRights(player, plot)) {
                    player.sendMessage("§cYou don't have permission on this plot!");
                    return true;
                }
                
                // Material-Bestimmung (aus Hand des Spielers beim Sneak+Rechtsklick)
                Material material = player.getInventory().getItemInMainHand().getType();
                
                if (material == Material.AIR) {
                    player.sendMessage("§cHold an item to assign a material to this chest!");
                    return true;
                }
                
                // Registriere Truhe
                registerChest(plot, chest, material);
                player.sendMessage("§aChest registered for material: §f" + material.name());
                
            } catch (ProviderFunctionalityNotFoundException e) {
                // Fallback wenn Plot-Features nicht verfügbar
                player.sendMessage("§ePlot features unavailable, using basic registration");
                registerChestBasic(player, chest);
            }
        } else {
            // Kein Plot-Provider: Basic Registration
            player.sendMessage("§eNo plot system detected, using player-based storage");
            registerChestBasic(player, chest);
        }
        
        return true;
    }
    
    /**
     * Registriert Truhe mit Plot-Kontext.
     */
    private void registerChest(Plot plot, Chest chest, Material material) {
        // Implementierung...
        // Speichere in DataStore: plot.getUuid() -> chest location -> material
    }
    
    /**
     * Registriert Truhe ohne Plot (Fallback).
     */
    private void registerChestBasic(Player player, Chest chest) {
        // Implementierung...
        // Speichere in DataStore: player.getUuid() -> chest location
    }
}
