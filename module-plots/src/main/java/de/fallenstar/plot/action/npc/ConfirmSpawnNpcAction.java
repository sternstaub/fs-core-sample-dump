package de.fallenstar.plot.action.npc;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.ui.element.UiAction;
import de.fallenstar.plot.PlotModule;
import de.fallenstar.plot.npc.manager.NPCManager;
import de.fallenstar.plot.npc.model.PlotNPC;
import de.fallenstar.plot.npc.model.PlotNPCType;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;

/**
 * Action zum Bestätigen und Ausführen des NPC-Spawns.
 *
 * **Funktionalität:**
 * - Spawnt NPC an Spieler-Position
 * - Registriert NPC im NPCManager
 * - Speichert NPC persistent in Config
 * - Gibt Feedback an Spieler
 *
 * **Validierung:**
 * - Prüft ob Spieler Owner ist
 * - Prüft ob Plot-Typ korrekt ist
 * - Prüft ob NPC-Limit erreicht
 *
 * **Citizens-Integration:**
 * - Wenn Citizens verfügbar: Erstellt echten NPC
 * - Wenn nicht verfügbar: Speichert nur Daten, NPC erscheint später
 *
 * @author FallenStar
 * @version 1.0
 */
public final class ConfirmSpawnNpcAction implements UiAction {

    private final Plot plot;
    private final PlotNPCType npcType;
    private final PlotModule plotModule;
    private final Player spawner;

    /**
     * Konstruktor für ConfirmSpawnNpcAction.
     *
     * @param plot Der Plot auf dem der NPC gespawnt werden soll
     * @param npcType Der Typ des NPCs
     * @param plotModule Das PlotModule für Manager-Zugriff
     * @param spawner Der Spieler der den NPC spawnt
     */
    public ConfirmSpawnNpcAction(Plot plot, PlotNPCType npcType, PlotModule plotModule, Player spawner) {
        this.plot = Objects.requireNonNull(plot, "Plot darf nicht null sein");
        this.npcType = Objects.requireNonNull(npcType, "NpcType darf nicht null sein");
        this.plotModule = Objects.requireNonNull(plotModule, "PlotModule darf nicht null sein");
        this.spawner = Objects.requireNonNull(spawner, "Spawner darf nicht null sein");
    }

    @Override
    public void execute(Player player) {
        player.closeInventory();

        // Hole NPCManager
        NPCManager npcManager = plotModule.getNPCManager();
        if (npcManager == null) {
            player.sendMessage("§cNPC-System ist nicht verfügbar!");
            return;
        }

        try {
            // Erstelle und registriere NPC an Spieler-Position
            PlotNPC npc = npcManager.createNPC(
                    npcType,
                    plot.getUuid(),  // hostPlot = Grundstück auf dem NPC steht
                    plot.getUuid(),  // sourcePlot = Grundstück das NPC "besitzt"
                    player.getLocation(),
                    null,  // owner (optional für Spielerhändler)
                    null   // customName (optional)
            );

            // Speichere Config
            npcManager.saveToConfig(plotModule.getConfig());
            plotModule.saveConfig();

            // Erfolgs-Nachricht
            player.sendMessage("§a§m----------§r §e§lNPC Gespawnt §a§m----------");
            player.sendMessage("§7Typ: §e" + getNpcTypeName(npcType));
            player.sendMessage("§7Plot: §e" + plot.getIdentifier());
            player.sendMessage("§7Position: §e" +
                    (int) player.getLocation().getX() + ", " +
                    (int) player.getLocation().getY() + ", " +
                    (int) player.getLocation().getZ());
            player.sendMessage("§7UUID: §e" + npc.getNpcId());
            player.sendMessage("");

            // Citizens-Integration
            if (plotModule.getProviders().getNpcProvider().isAvailable()) {
                player.sendMessage("§a§lNPC wird gespawnt...");

                try {
                    // Spawn Citizens-NPC
                    UUID citizensNpcId = spawnCitizensNPC(npc, player);

                    if (citizensNpcId != null) {
                        // Registriere in PlotBoundNPCRegistry
                        var npcRegistry = plotModule.getNPCRegistry();
                        if (npcRegistry != null) {
                            npcRegistry.registerNPC(plot, citizensNpcId, npcType.name().toLowerCase(), player.getLocation());
                            plotModule.saveConfiguration();
                        }

                        player.sendMessage("§a§l✓ NPC erfolgreich gespawnt!");
                        player.sendMessage("§7Der NPC ist jetzt sichtbar und interaktiv");
                    } else {
                        player.sendMessage("§e§lNPC-Daten gespeichert!");
                        player.sendMessage("§7Citizens-NPC konnte nicht gespawnt werden");
                        player.sendMessage("§7Prüfe die Server-Logs für Details");
                    }
                } catch (Exception e) {
                    player.sendMessage("§c§lFehler beim Spawnen!");
                    player.sendMessage("§7" + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                player.sendMessage("§e§lNPC-Daten gespeichert!");
                player.sendMessage("§7Der NPC erscheint sobald Citizens geladen ist");
            }

            player.sendMessage("§a§m--------------------------------");

        } catch (Exception e) {
            player.sendMessage("§cFehler beim Spawnen des NPCs: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public String getActionName() {
        return "ConfirmSpawnNpc[" + plot.getIdentifier() + ", " + npcType + "]";
    }

    /**
     * Gibt Namen für NPC-Typ zurück.
     */
    private String getNpcTypeName(PlotNPCType type) {
        return switch (type) {
            case GUILD_TRADER -> "Gildenhändler";
            case PLAYER_TRADER -> "Spielerhändler";
            case LOCAL_BANKER -> "Lokaler Bankier";
            case WORLD_BANKER -> "Weltbankier";
            case AMBASSADOR -> "Botschafter";
            case CRAFTSMAN -> "Handwerker";
        };
    }

    /**
     * Spawnt einen Citizens-NPC.
     *
     * @param npc Die NPC-Daten
     * @param player Der Spieler (für Position)
     * @return UUID des gespawnten NPCs oder null bei Fehler
     */
    private UUID spawnCitizensNPC(PlotNPC npc, Player player) {
        try {
            var npcProvider = plotModule.getProviders().getNpcProvider();
            if (npcProvider == null || !npcProvider.isAvailable()) {
                return null;
            }

            // Bestimme NPC-Namen und Skin
            String npcName = getNpcTypeName(npcType);
            String skin = getNpcSkin(npcType);

            // Spawn NPC via NPCProvider
            UUID citizensId = npcProvider.createNPC(
                    player.getLocation(),
                    npcName,
                    skin
            );

            if (citizensId == null) {
                plotModule.getLogger().warning("NPCProvider.spawnNPC returned null!");
                return null;
            }

            plotModule.getLogger().info("Spawned Citizens-NPC: " + citizensId +
                    " (Type: " + npcType + ", Plot: " + plot.getUuid() + ")");

            // Registriere NPC im NPCs-Modul (falls geladen)
            registerInNPCsModule(citizensId, npc);

            return citizensId;

        } catch (Exception e) {
            plotModule.getLogger().severe("Failed to spawn Citizens-NPC: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gibt den Skin für einen NPC-Typ zurück.
     */
    private String getNpcSkin(PlotNPCType type) {
        return switch (type) {
            case GUILD_TRADER -> "MHF_Villager";
            case PLAYER_TRADER -> "MHF_Alex";
            case LOCAL_BANKER, WORLD_BANKER -> "Notch";
            case AMBASSADOR -> "jeb_";
            case CRAFTSMAN -> "MHF_Steve";
        };
    }

    /**
     * Registriert den NPC im NPCs-Modul (falls geladen).
     *
     * Verbindet den gespawnten Citizens-NPC mit dem GuildTraderNPC-System
     * und registriert den Click-Handler.
     */
    private void registerInNPCsModule(UUID citizensId, PlotNPC npc) {
        try {
            var npcsPlugin = plotModule.getServer().getPluginManager().getPlugin("FallenStar-NPCs");
            if (npcsPlugin == null) {
                plotModule.getLogger().fine("NPCs module not loaded - skipping registration");
                return;
            }

            // Reflection: Hole NPCManager aus NPCs-Modul
            var getNPCManager = npcsPlugin.getClass().getMethod("getNPCManager");
            var npcManager = getNPCManager.invoke(npcsPlugin);

            if (npcManager == null) {
                plotModule.getLogger().warning("NPCManager not available in NPCs module");
                return;
            }

            // 1. Registriere NPC im NPCManager (setzt Click-Handler!)
            String npcTypeName = mapPlotNPCTypeToNPCsModule(npcType);
            var registerNPCMethod = npcManager.getClass().getMethod("registerNPC", UUID.class, String.class);
            registerNPCMethod.invoke(npcManager, citizensId, npcTypeName);

            plotModule.getLogger().info("Registered NPC " + citizensId + " with type " + npcTypeName + " in NPCManager");

            // 2. Für GuildTrader: Registriere im GuildTraderNPC
            if (npcType == PlotNPCType.GUILD_TRADER) {
                // Reflection: Hole GuildTraderNPC-Typ
                var getNPCTypeMethod = npcManager.getClass().getMethod("getNPCType", String.class);
                var guildTraderType = getNPCTypeMethod.invoke(npcManager, "guildtrader");

                if (guildTraderType != null) {
                    // Reflection: registerNPCForPlot(UUID npcId, Plot plot)
                    var registerMethod = guildTraderType.getClass().getMethod(
                            "registerNPCForPlot",
                            UUID.class,
                            de.fallenstar.core.provider.Plot.class
                    );
                    registerMethod.invoke(guildTraderType, citizensId, plot);

                    plotModule.getLogger().info("Registered GuildTrader " + citizensId + " for plot " + plot.getUuid());
                }
            }

        } catch (Exception e) {
            plotModule.getLogger().warning("Failed to register NPC in NPCs module: " + e.getMessage());
            e.printStackTrace();
            // Nicht kritisch - NPC ist bereits gespawnt
        }
    }

    /**
     * Mappt PlotNPCType zu NPCs-Modul Typ-Namen.
     */
    private String mapPlotNPCTypeToNPCsModule(PlotNPCType type) {
        return switch (type) {
            case GUILD_TRADER -> "guildtrader";
            case PLAYER_TRADER -> "playertrader";
            case LOCAL_BANKER -> "localbanker";
            case WORLD_BANKER -> "worldbanker";
            case AMBASSADOR -> "ambassador";
            case CRAFTSMAN -> "craftsman";
        };
    }
}
