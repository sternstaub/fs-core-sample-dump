package de.fallenstar.plot.storage.command;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.provider.PlotProvider;
import de.fallenstar.core.exception.ProviderFunctionalityNotFoundException;
import de.fallenstar.plot.storage.model.PlotStorage;
import de.fallenstar.plot.storage.provider.PlotStorageProvider;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;

/**
 * Command zum Anzeigen aller Materialien auf einem Plot.
 *
 * Syntax: /storage list
 *
 * Zeigt alle gespeicherten Materialien mit Mengen für das Plot
 * auf dem der Spieler steht.
 *
 * @author FallenStar
 * @version 1.0
 */
public class StorageListCommand implements CommandExecutor {

    private final PlotProvider plotProvider;
    private final PlotStorageProvider storageProvider;

    /**
     * Erstellt ein neues StorageListCommand.
     *
     * @param plotProvider Der PlotProvider
     * @param storageProvider Der PlotStorageProvider
     */
    public StorageListCommand(PlotProvider plotProvider, PlotStorageProvider storageProvider) {
        this.plotProvider = plotProvider;
        this.storageProvider = storageProvider;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Dieser Command kann nur von Spielern verwendet werden.");
            return true;
        }

        Player player = (Player) sender;

        try {
            // Hole Plot an Spieler-Position
            Plot plot = plotProvider.getPlot(player.getLocation());

            if (plot == null) {
                player.sendMessage(ChatColor.RED + "Du stehst nicht auf einem Grundstück!");
                return true;
            }

            // Hole PlotStorage
            PlotStorage storage = storageProvider.getPlotStorage(plot);
            Set<Material> materials = storage.getAllMaterials();

            if (materials.isEmpty()) {
                player.sendMessage(ChatColor.YELLOW + "Keine Materialien auf diesem Grundstück gefunden.");
                player.sendMessage(ChatColor.GRAY + "Plot: " + plot.getIdentifier());
                return true;
            }

            // Zeige Header
            player.sendMessage(ChatColor.GOLD + "=== Storage: " + plot.getIdentifier() + " ===");
            player.sendMessage(ChatColor.GRAY + "Truhen: " + storage.getAllChests().size());
            player.sendMessage("");

            // Zeige alle Materialien
            int totalTypes = 0;
            for (Material material : materials) {
                int amount = storage.getTotalAmount(material);
                player.sendMessage(ChatColor.WHITE + "  " + material.name() + ": " +
                                 ChatColor.GREEN + amount);
                totalTypes++;
            }

            player.sendMessage("");
            player.sendMessage(ChatColor.GRAY + "Gesamt: " + totalTypes + " Material-Typen");

        } catch (ProviderFunctionalityNotFoundException e) {
            player.sendMessage(ChatColor.RED + "Plot-System nicht verfügbar: " + e.getMessage());
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Fehler beim Abrufen des Storage: " + e.getMessage());
            e.printStackTrace();
        }

        return true;
    }
}
