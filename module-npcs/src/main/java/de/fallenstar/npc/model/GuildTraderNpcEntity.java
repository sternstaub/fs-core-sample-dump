package de.fallenstar.npc.model;

import de.fallenstar.core.interaction.InteractionContext;
import de.fallenstar.core.interaction.InteractionType;
import de.fallenstar.core.interaction.UiContext;
import de.fallenstar.core.interaction.UiTarget;
import de.fallenstar.core.interaction.action.UiActionInfo;
import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.provider.TradingEntity;
import de.fallenstar.core.ui.BaseUi;
import de.fallenstar.npc.npctype.GuildTraderNPC;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Wrapper für einzelne GuildTrader-NPC-Instanzen mit UiTarget-Support.
 *
 * **Konzept:**
 * - GuildTraderNPC = NPC-Typ (verwaltet alle Gildenhändler)
 * - GuildTraderNpcEntity = Einzelner NPC (Interactable + UiTarget)
 *
 * **Features:**
 * - Click-to-UI: Spieler klickt auf NPC → Trade-UI öffnet sich
 * - InteractionRegistry-Integration
 * - TradingEntity-Delegation
 *
 * **Verwendung:**
 * <pre>
 * // NPC spawnen
 * UUID npcId = npcManager.spawnNPC(...);
 * GuildTraderNpcEntity entity = new GuildTraderNpcEntity(npcId, plot, guildTraderType);
 *
 * // In InteractionRegistry registrieren
 * interactionRegistry.registerEntity(npcId, entity);
 *
 * // Spieler klickt auf NPC → Trade-UI öffnet sich automatisch!
 * </pre>
 *
 * @author FallenStar
 * @version 1.0
 */
public class GuildTraderNpcEntity implements UiTarget {

    private final UUID npcId;
    private final Plot plot;
    private final GuildTraderNPC guildTraderType;

    /**
     * Erstellt eine GuildTraderNpcEntity.
     *
     * @param npcId NPC-UUID
     * @param plot Plot zu dem der NPC gehört
     * @param guildTraderType GuildTraderNPC-Typ-Instanz
     */
    public GuildTraderNpcEntity(UUID npcId, Plot plot, GuildTraderNPC guildTraderType) {
        this.npcId = npcId;
        this.plot = plot;
        this.guildTraderType = guildTraderType;
    }

    /**
     * Gibt die NPC-ID zurück.
     *
     * @return NPC-UUID
     */
    public UUID getNpcId() {
        return npcId;
    }

    /**
     * Gibt den Plot zurück.
     *
     * @return Plot
     */
    public Plot getPlot() {
        return plot;
    }

    // ========== Interactable Implementation ==========

    @Override
    public boolean onInteract(Player player, InteractionContext context) {
        // Nutze die onClick-Logik von GuildTraderNPC
        guildTraderType.onClick(player, npcId);
        return true;
    }

    @Override
    public InteractionType getInteractionType() {
        return InteractionType.ENTITY;
    }

    // ========== UiActionTarget Implementation ==========

    @Override
    public List<UiActionInfo> getAvailableActions(Player player, UiContext context) {
        // GuildTrader hat keine konfigurierbaren Aktionen
        // Das Trade-UI wird direkt beim Klick geöffnet
        return List.of();
    }

    @Override
    public boolean executeAction(Player player, String actionId) {
        // Keine Aktionen verfügbar
        return false;
    }

    // ========== UiTarget Implementation ==========

    @Override
    public Optional<BaseUi> createUi(Player player, InteractionContext context) {
        // Trade-UI wird von GuildTraderNPC.onClick() geöffnet
        // Daher geben wir empty zurück (UI-Opening erfolgt in onInteract)
        return Optional.empty();
    }

    // ========== TradingEntity Delegation ==========

    /**
     * Gibt die TradingEntity-Instanz für diesen NPC zurück.
     *
     * @return TradingEntity
     */
    public TradingEntity getTradingEntity() {
        return guildTraderType.new GuildTraderInstance(npcId);
    }
}
