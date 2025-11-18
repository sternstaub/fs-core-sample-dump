package de.fallenstar.plot.ui;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.ui.container.BasicGsUi;
import de.fallenstar.plot.PlotModule;
import de.fallenstar.plot.action.npc.ConfirmSpawnNpcAction;
import de.fallenstar.plot.npc.model.PlotNPCType;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;

/**
 * Spawn-Menü für Grundstücks-NPCs.
 *
 * **Funktionalität:**
 * - Zeigt verfügbare NPC-Typen (Gildenhändler, Bankier, etc.)
 * - Klick auf Typ → spawnt NPC am Spieler-Standort
 * - Nur für Plot-Besitzer
 *
 * **NPC-Typen (Grundstück):**
 * - GUILD_TRADER: Gildenhändler (Handelsgilde-Plot)
 * - PLAYER_TRADER: Spielerhändler (Market-Plot, später)
 * - LOCAL_BANKER: Lokaler Bankier (später)
 * - WORLD_BANKER: Weltbankier (Admin-only, später)
 *
 * **Layout:**
 * - Info-Bereich (Zeile 0)
 * - NPC-Typen (Zeilen 1-2)
 *
 * @author FallenStar
 * @version 1.0
 */
public class NpcSpawnMenuUi extends BasicGsUi {

    private final Plot plot;
    private final PlotModule plotModule;
    private final Player spawner;

    /**
     * Erstellt ein neues NPC-Spawn-Menü.
     *
     * @param plot Der Plot auf dem NPCs gespawnt werden sollen
     * @param plotModule Das PlotModule für NPC-Manager-Zugriff
     * @param spawner Der Spieler der das Menü öffnet (muss Owner sein)
     */
    public NpcSpawnMenuUi(Plot plot, PlotModule plotModule, Player spawner) {
        super("§6§lNPC Spawnen");
        this.plot = Objects.requireNonNull(plot, "Plot darf nicht null sein");
        this.plotModule = Objects.requireNonNull(plotModule, "PlotModule darf nicht null sein");
        this.spawner = Objects.requireNonNull(spawner, "Spawner darf nicht null sein");

        buildUi();
    }

    /**
     * Baut das UI auf.
     */
    private void buildUi() {
        // Info-Bereich
        buildInfoSection();

        // NPC-Typen
        buildNpcTypes();
    }

    /**
     * Baut den Info-Bereich.
     */
    private void buildInfoSection() {
        addInfoElement(
                Material.VILLAGER_SPAWN_EGG,
                "§e§lNPC spawnen",
                List.of(
                        "§7Wähle einen NPC-Typ aus,",
                        "§7um ihn an deiner Position zu spawnen",
                        "§7",
                        "§7Plot: §e" + plot.getIdentifier()
                )
        );
    }

    /**
     * Baut die NPC-Typ-Auswahl.
     */
    private void buildNpcTypes() {
        // Gildenhändler
        addFunctionButton(
                Material.EMERALD,
                "§a§lGildenhändler",
                List.of(
                        "§7Handelt mit Items aus dem",
                        "§7Grundstücks-Storage",
                        "§7",
                        "§7Typ: §eGUILD_TRADER",
                        "§7Nur für: §eHandelsgilde-Plots",
                        "§7",
                        "§a§lKlicke zum Spawnen"
                ),
                new ConfirmSpawnNpcAction(plot, PlotNPCType.GUILD_TRADER, plotModule, spawner)
        );

        // Spielerhändler (Placeholder)
        addFunctionButton(
                Material.GOLD_INGOT,
                "§6§lSpielerhändler",
                List.of(
                        "§7Handelt mit Items aus dem",
                        "§7virtuellen Spieler-Inventar",
                        "§7",
                        "§7Typ: §ePLAYER_TRADER",
                        "§7Nur für: §eMarket-Plots",
                        "§7",
                        "§c§lNoch nicht verfügbar"
                ),
                player -> {
                    player.closeInventory();
                    player.sendMessage("§cSpielerhändler werden in einem späteren Sprint verfügbar sein!");
                }
        );

        // Lokaler Bankier (Placeholder)
        addFunctionButton(
                Material.GOLD_BLOCK,
                "§e§lLokaler Bankier",
                List.of(
                        "§7Verwaltet Plot-gebundene",
                        "§7Geldeinlagen und Auszahlungen",
                        "§7",
                        "§7Typ: §eLOCAL_BANKER",
                        "§7",
                        "§c§lNoch nicht verfügbar"
                ),
                player -> {
                    player.closeInventory();
                    player.sendMessage("§cLokale Bankiers werden in einem späteren Sprint verfügbar sein!");
                }
        );

        // Botschafter (Placeholder)
        addFunctionButton(
                Material.ENDER_PEARL,
                "§d§lBotschafter",
                List.of(
                        "§7Ermöglicht Reisen zu anderen",
                        "§7Towns gegen Geld",
                        "§7",
                        "§7Typ: §eAMBASSADOR",
                        "§7Nur für: §eBotschaft-Plots",
                        "§7",
                        "§c§lNoch nicht verfügbar"
                ),
                player -> {
                    player.closeInventory();
                    player.sendMessage("§cBotschafter werden in einem späteren Sprint verfügbar sein!");
                }
        );
    }
}
