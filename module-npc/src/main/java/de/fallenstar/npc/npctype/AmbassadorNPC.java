package de.fallenstar.npc.npctype;

import de.fallenstar.core.exception.ProviderFunctionalityNotFoundException;
import de.fallenstar.core.provider.EconomyProvider;
import de.fallenstar.core.provider.TownProvider;
import de.fallenstar.npc.manager.NPCManager;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

/**
 * Botschafter-NPC.
 *
 * Funktion:
 * - Teleportiert Spieler zu anderen Towns
 * - Optionale Kosten für Teleport
 * - Zeigt Liste aller verfügbaren Towns
 *
 * Voraussetzungen:
 * - TownProvider (required)
 * - EconomyProvider (optional für Kosten)
 *
 * @author FallenStar
 * @version 1.0
 */
public class AmbassadorNPC implements NPCType {

    private final NPCManager npcManager;
    private final TownProvider townProvider;
    private final EconomyProvider economyProvider;
    private final boolean economyEnabled;
    private final FileConfiguration config;

    private double teleportCost;
    private boolean requiresPayment;

    /**
     * Erstellt einen neuen Botschafter-NPC.
     *
     * @param npcManager NPCManager
     * @param townProvider TownProvider
     * @param economyProvider EconomyProvider
     * @param economyEnabled Ob Economy aktiviert ist
     * @param config Plugin-Config
     */
    public AmbassadorNPC(
            NPCManager npcManager,
            TownProvider townProvider,
            EconomyProvider economyProvider,
            boolean economyEnabled,
            FileConfiguration config
    ) {
        this.npcManager = npcManager;
        this.townProvider = townProvider;
        this.economyProvider = economyProvider;
        this.economyEnabled = economyEnabled;
        this.config = config;
    }

    @Override
    public String getTypeName() {
        return "ambassador";
    }

    @Override
    public String getDisplayName() {
        return config.getString("npc.ambassador.name", "§6Botschafter");
    }

    @Override
    public String getSkin() {
        return config.getString("npc.ambassador.skin", "Steve");
    }

    @Override
    public void onClick(Player player, UUID npcId) {
        try {
            // Hole Liste aller Towns
            List<String> towns = townProvider.getAllTowns();

            if (towns.isEmpty()) {
                player.sendMessage("§cKeine Towns verfügbar!");
                return;
            }

            // Zeige Town-Liste
            showTownList(player, towns);

        } catch (ProviderFunctionalityNotFoundException e) {
            player.sendMessage("§cFehler: " + e.getMessage());
        } catch (Exception e) {
            player.sendMessage("§cEin Fehler ist aufgetreten!");
            e.printStackTrace();
        }
    }

    /**
     * Zeigt die Liste aller Towns.
     *
     * @param player Der Spieler
     * @param towns Liste der Towns
     */
    private void showTownList(Player player, List<String> towns) {
        player.sendMessage("§8§m---------§r §6Verfügbare Towns §8§m---------");
        player.sendMessage("§7Klicke im Chat auf einen Town-Namen zum Teleportieren:");

        for (String townName : towns) {
            try {
                // Prüfe ob Town existiert und Spawn hat
                if (townProvider.townExists(townName)) {
                    int residents = townProvider.getResidentCount(townName);

                    // Erstelle klickbare Nachricht
                    String message = String.format(
                        "§e➤ §6%s §7(%d Einwohner)",
                        townName,
                        residents
                    );

                    // TODO: Hier könnte ein klickbares Text-Component mit /plot teleport <town> eingebaut werden
                    player.sendMessage(message);
                    player.sendMessage("  §8/town spawn " + townName);
                }
            } catch (Exception e) {
                // Town-Info fehlgeschlagen, trotzdem anzeigen
                player.sendMessage("§e➤ §6" + townName);
            }
        }

        if (requiresPayment && economyEnabled) {
            player.sendMessage("");
            player.sendMessage("§7Kosten: §e" + teleportCost + " Coins");
        }

        player.sendMessage("§8§m--------------------------------------");
    }

    /**
     * Teleportiert einen Spieler zu einer Town.
     *
     * @param player Der Spieler
     * @param townName Name der Town
     */
    public void teleportToTown(Player player, String townName) {
        try {
            // Prüfe ob Town existiert
            if (!townProvider.townExists(townName)) {
                player.sendMessage("§cTown '§e" + townName + "§c' existiert nicht!");
                return;
            }

            // Prüfe Kosten
            if (requiresPayment && economyEnabled) {
                try {
                    double balance = economyProvider.getBalance(player);
                    if (balance < teleportCost) {
                        player.sendMessage("§cDu hast nicht genug Geld! (Benötigt: §e" + teleportCost + "§c)");
                        return;
                    }

                    // Ziehe Kosten ab
                    economyProvider.withdraw(player, teleportCost);
                    player.sendMessage("§7" + teleportCost + " Coins wurden abgebucht.");

                } catch (ProviderFunctionalityNotFoundException e) {
                    // Economy nicht verfügbar - trotzdem teleportieren
                    player.sendMessage("§eEconomy-System nicht verfügbar - kostenloser Teleport.");
                }
            }

            // Hole Town-Spawn
            Location spawn = townProvider.getTownSpawn(townName);

            if (spawn == null) {
                player.sendMessage("§cTown '§e" + townName + "§c' hat keinen Spawn-Punkt!");
                return;
            }

            // Teleportiere Spieler
            player.teleport(spawn);
            player.sendMessage("§aDu wurdest nach §e" + townName + " §ateleportiert!");

        } catch (ProviderFunctionalityNotFoundException e) {
            player.sendMessage("§cFehler beim Teleportieren: " + e.getMessage());
        } catch (Exception e) {
            player.sendMessage("§cEin Fehler ist aufgetreten!");
            e.printStackTrace();
        }
    }

    @Override
    public void initialize() {
        // Lade Config
        teleportCost = config.getDouble("npc.ambassador.teleport-cost", 100.0);
        requiresPayment = config.getBoolean("npc.ambassador.requires-payment", true);

        npcManager.getLogger().info("Ambassador NPC initialized (Cost: " + teleportCost + ")");
    }

    @Override
    public void shutdown() {
        // Cleanup wenn nötig
    }

    @Override
    public boolean isAvailable() {
        return townProvider != null && townProvider.isAvailable();
    }
}
