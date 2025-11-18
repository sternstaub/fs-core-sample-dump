package de.fallenstar.npc.model;

import de.fallenstar.core.distributor.DistributableNpc;
import de.fallenstar.core.distributor.DistributableQuest;
import de.fallenstar.core.distributor.Distributor;
import de.fallenstar.core.distributor.QuestContainer;
import de.fallenstar.core.interaction.InteractionContext;
import de.fallenstar.core.interaction.InteractionType;
import de.fallenstar.core.interaction.UiContext;
import de.fallenstar.core.interaction.UiTarget;
import de.fallenstar.core.interaction.action.UiActionInfo;
import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.provider.TradingEntity;
import de.fallenstar.core.ui.BaseUi;
import de.fallenstar.npc.npctype.GuildTraderNPC;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Wrapper für einzelne GuildTrader-NPC-Instanzen mit vollständiger Trait-Composition.
 *
 * **Konzept:**
 * - GuildTraderNPC = NPC-Typ (verwaltet alle Gildenhändler)
 * - GuildTraderNpcEntity = Einzelner NPC (Interactable + UiTarget + DistributableNpc + QuestContainer)
 *
 * **Implementierte Traits:**
 * 1. **UiTarget** - Click-to-UI (Trade-UI öffnet sich beim Klick)
 * 2. **DistributableNpc** - Kann auf Plot-Slots verteilt werden
 * 3. **QuestContainer** - Kann bis zu 5 Quests halten
 *
 * **Features:**
 * - Click-to-UI: Spieler klickt auf NPC → Trade-UI öffnet sich
 * - InteractionRegistry-Integration
 * - TradingEntity-Delegation
 * - Automatische Slot-Distribution über NpcDistributor
 * - Quest-Verwaltung (max. 5 Quests pro NPC)
 *
 * **Verwendung:**
 * <pre>
 * // NPC erstellen
 * UUID id = UUID.randomUUID();
 * GuildTraderNpcEntity entity = new GuildTraderNpcEntity(id, plot, guildTraderType);
 *
 * // In InteractionRegistry registrieren
 * interactionRegistry.registerEntity(id, entity);
 *
 * // Auf Plot distribuieren (spawnt automatisch + weist Slot zu)
 * boolean success = plot.distribute(entity);
 *
 * // Quest zuweisen
 * boolean questAdded = entity.addQuest(quest);
 *
 * // Spieler klickt auf NPC → Trade-UI öffnet sich automatisch!
 * </pre>
 *
 * **Distribution-Workflow:**
 * 1. TradeguildPlot.distribute(entity) wird aufgerufen
 * 2. Plot findet freien Slot
 * 3. Plot ruft entity.spawn(location) auf
 * 4. NPC wird gespawnt und in InteractionRegistry eingetragen
 * 5. Slot wird belegt
 *
 * **Quest-Distribution:**
 * - QuestDistributor wählt zufälligen NPC mit Kapazität
 * - Quest wird via addQuest() hinzugefügt
 * - Max. 5 Quests pro NPC (konfigurierbar via maxQuests)
 *
 * @author FallenStar
 * @version 2.0
 */
public class GuildTraderNpcEntity implements UiTarget, DistributableNpc, QuestContainer {

    private final UUID id; // Distributable-ID
    private final Plot plot;
    private final GuildTraderNPC guildTraderType;

    // DistributableNpc-Daten
    private UUID entityId; // Entity-UUID (nach Spawn)

    // QuestContainer-Daten
    private final List<DistributableQuest> quests = Collections.synchronizedList(new ArrayList<>());
    private int maxQuests = 5; // Default: 5 Quests pro NPC

    /**
     * Erstellt eine GuildTraderNpcEntity.
     *
     * @param id Distributable-ID (wird zu Entity-ID nach Spawn)
     * @param plot Plot zu dem der NPC gehört
     * @param guildTraderType GuildTraderNPC-Typ-Instanz
     */
    public GuildTraderNpcEntity(UUID id, Plot plot, GuildTraderNPC guildTraderType) {
        this.id = id;
        this.plot = plot;
        this.guildTraderType = guildTraderType;
    }

    /**
     * Gibt die Distributable-ID zurück.
     *
     * @return UUID
     */
    @Override
    public UUID getId() {
        return id;
    }

    /**
     * Gibt die NPC-ID zurück (Backward-Compatibility).
     *
     * @return Entity-UUID oder Distributable-ID
     */
    public UUID getNpcId() {
        return entityId != null ? entityId : id;
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
        guildTraderType.onClick(player, getNpcId());
        return true;
    }

    @Override
    public InteractionType getInteractionType() {
        return InteractionType.ENTITY;
    }

    // ========== DistributableNpc Implementation ==========

    @Override
    public Optional<UUID> getEntityId() {
        return Optional.ofNullable(entityId);
    }

    @Override
    public UUID spawn(Location location) {
        // Spawne NPC über GuildTraderNPC
        // TODO: GuildTraderNPC.spawnNPC() muss die Entity-UUID zurückgeben
        // Temporär: Nutze die ID als Entity-ID
        this.entityId = id;
        return entityId;
    }

    @Override
    public void despawn() {
        // TODO: Despawn NPC über NPCProvider
        // Temporär: Nur entityId clearen
        this.entityId = null;
    }

    @Override
    public String getType() {
        return "guild_trader";
    }

    @Override
    public String getNpcType() {
        return "guild_trader";
    }

    @Override
    public String getDisplayName() {
        // TODO: GuildTraderNPC sollte Display-Namen bereitstellen
        return "§6Gildenhändler";
    }

    // ========== QuestContainer Implementation ==========

    @Override
    public UUID getContainerId() {
        return getNpcId();
    }

    @Override
    public String getContainerName() {
        return getDisplayName();
    }

    @Override
    public int getMaxQuests() {
        return maxQuests;
    }

    @Override
    public List<DistributableQuest> getQuests() {
        return new ArrayList<>(quests);
    }

    @Override
    public boolean addQuest(DistributableQuest quest) {
        if (!hasQuestCapacity()) {
            return false;
        }
        boolean added = quests.add(quest);
        if (added) {
            quest.setCurrentContainer(this);
        }
        return added;
    }

    @Override
    public boolean removeQuest(DistributableQuest quest) {
        boolean removed = quests.remove(quest);
        if (removed) {
            quest.setCurrentContainer(null);
        }
        return removed;
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
