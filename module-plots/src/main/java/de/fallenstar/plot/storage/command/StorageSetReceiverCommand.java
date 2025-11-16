package de.fallenstar.plot.storage.command;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.provider.PlotProvider;
import de.fallenstar.core.exception.ProviderFunctionalityNotFoundException;
import de.fallenstar.plot.storage.manager.StorageManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command zum Setzen einer Empfangskiste für ein Plot.
 *
 * Syntax: /storage setreceiver
 *
 * Setzt die Truhe, auf die der Spieler schaut, als Empfangskiste
 * für das aktuelle Plot. Diese wird bei Material-Transfer zuerst befüllt.
 *
 * @author FallenStar
 * @version 1.0
 */
public class StorageSetReceiverCommand implements CommandExecutor {

    private final PlotProvider plotProvider;
    private final StorageManager storageManager;

    /**
     * Erstellt ein neues StorageSetReceiverCommand.
     *
     * @param plotProvider Der PlotProvider
     * @param storageManager Der StorageManager
     */
    public StorageSetReceiverCommand(PlotProvider plotProvider, StorageManager storageManager) {
        this.plotProvider = plotProvider;
        this.storageManager = storageManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Dieser Command kann nur von Spielern verwendet werden.");
            return true;
        }

        Player player = (Player) sender;

        try {
            // Hole Block auf den Spieler schaut
            Block targetBlock = player.getTargetBlockExact(5);

            if (targetBlock == null || targetBlock.getType() == Material.AIR) {
                player.sendMessage(ChatColor.RED + "Keine Truhe gefunden! Schaue auf eine Truhe.");
                return true;
            }

            // Prüfe ob es eine Truhe ist
            if (targetBlock.getType() != Material.CHEST &&
                targetBlock.getType() != Material.TRAPPED_CHEST &&
                targetBlock.getType() != Material.BARREL) {
                player.sendMessage(ChatColor.RED + "Das ist keine Truhe!");
                return true;
            }

            Location chestLocation = targetBlock.getLocation();

            // Hole Plot an Truhen-Position
            Plot plot = plotProvider.getPlot(chestLocation);

            if (plot == null) {
                player.sendMessage(ChatColor.RED + "Diese Truhe ist nicht auf einem Grundstück!");
                return true;
            }

            // Prüfe ob Spieler auf diesem Plot steht
            Plot playerPlot = plotProvider.getPlot(player.getLocation());
            if (playerPlot == null || !playerPlot.getUuid().equals(plot.getUuid())) {
                player.sendMessage(ChatColor.RED + "Du musst auf dem gleichen Grundstück stehen!");
                return true;
            }

            // TODO: Prüfe ob Spieler Admin-Rechte hat
            // if (!plotProvider.hasAdminRights(player, plot)) {
            //     player.sendMessage(ChatColor.RED + "Du hast keine Rechte auf diesem Grundstück!");
            //     return true;
            // }

            // Setze Empfangskiste
            boolean success = storageManager.setReceiverChest(plot, chestLocation);

            if (success) {
                player.sendMessage(ChatColor.GREEN + "Empfangskiste erfolgreich gesetzt!");
                player.sendMessage(ChatColor.GRAY + "Plot: " + plot.getIdentifier());
                player.sendMessage(ChatColor.GRAY + "Position: " +
                                 chestLocation.getBlockX() + ", " +
                                 chestLocation.getBlockY() + ", " +
                                 chestLocation.getBlockZ());
            } else {
                player.sendMessage(ChatColor.RED + "Fehler beim Setzen der Empfangskiste.");
                player.sendMessage(ChatColor.GRAY + "Stelle sicher, dass diese Truhe registriert ist.");
            }

        } catch (ProviderFunctionalityNotFoundException e) {
            player.sendMessage(ChatColor.RED + "Plot-System nicht verfügbar: " + e.getMessage());
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Fehler: " + e.getMessage());
            e.printStackTrace();
        }

        return true;
    }
}
