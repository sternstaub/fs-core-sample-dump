package de.fallenstar.plot.storage.command;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.provider.PlotProvider;
import de.fallenstar.core.exception.ProviderFunctionalityNotFoundException;
import de.fallenstar.plot.storage.model.PlotStorage;
import de.fallenstar.plot.storage.model.StoredMaterial;
import de.fallenstar.plot.storage.provider.PlotStorageProvider;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Command zum Anzeigen von Details zu einem Material auf einem Plot.
 *
 * Syntax: /storage info <material>
 *
 * Zeigt alle Truhen mit diesem Material und deren Mengen.
 *
 * @author FallenStar
 * @version 1.0
 */
public class StorageInfoCommand implements CommandExecutor {

    private final PlotProvider plotProvider;
    private final PlotStorageProvider storageProvider;

    /**
     * Erstellt ein neues StorageInfoCommand.
     *
     * @param plotProvider Der PlotProvider
     * @param storageProvider Der PlotStorageProvider
     */
    public StorageInfoCommand(PlotProvider plotProvider, PlotStorageProvider storageProvider) {
        this.plotProvider = plotProvider;
        this.storageProvider = storageProvider;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Dieser Command kann nur von Spielern verwendet werden.");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Verwendung: /storage info <material>");
            return true;
        }

        Player player = (Player) sender;
        String materialName = args[0].toUpperCase();

        // Parse Material
        Material material;
        try {
            material = Material.valueOf(materialName);
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Unbekanntes Material: " + materialName);
            return true;
        }

        try {
            // Hole Plot an Spieler-Position
            Plot plot = plotProvider.getPlot(player.getLocation());

            if (plot == null) {
                player.sendMessage(ChatColor.RED + "Du stehst nicht auf einem Grundstück!");
                return true;
            }

            // Hole PlotStorage
            PlotStorage storage = storageProvider.getPlotStorage(plot);
            List<StoredMaterial> materialLocations = storage.getMaterialLocations(material);

            if (materialLocations.isEmpty()) {
                player.sendMessage(ChatColor.YELLOW + material.name() + " nicht auf diesem Grundstück gefunden.");
                player.sendMessage(ChatColor.GRAY + "Plot: " + plot.getIdentifier());
                return true;
            }

            // Zeige Header
            player.sendMessage(ChatColor.GOLD + "=== " + material.name() + " auf " + plot.getIdentifier() + " ===");
            player.sendMessage("");

            // Zeige alle Truhen mit diesem Material
            int totalAmount = 0;
            int chestCount = 0;

            for (StoredMaterial stored : materialLocations) {
                player.sendMessage(ChatColor.WHITE + "  Truhe #" + (++chestCount) + ":");
                player.sendMessage(ChatColor.GRAY + "    Menge: " + ChatColor.GREEN + stored.getAmount());
                player.sendMessage(ChatColor.GRAY + "    Position: " +
                                 stored.getChestLocation().getBlockX() + ", " +
                                 stored.getChestLocation().getBlockY() + ", " +
                                 stored.getChestLocation().getBlockZ());
                totalAmount += stored.getAmount();
            }

            player.sendMessage("");
            player.sendMessage(ChatColor.GRAY + "Gesamt: " + ChatColor.GREEN + totalAmount +
                             ChatColor.GRAY + " in " + chestCount + " Truhe(n)");

        } catch (ProviderFunctionalityNotFoundException e) {
            player.sendMessage(ChatColor.RED + "Plot-System nicht verfügbar: " + e.getMessage());
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Fehler beim Abrufen der Informationen: " + e.getMessage());
            e.printStackTrace();
        }

        return true;
    }
}
