package de.fallenstar.plot.model;

import de.fallenstar.core.distributor.*;
import de.fallenstar.core.interaction.Interactable;
import de.fallenstar.core.interaction.InteractionContext;
import de.fallenstar.core.interaction.InteractionType;
import de.fallenstar.core.interaction.UiContext;
import de.fallenstar.core.interaction.UiTarget;
import de.fallenstar.core.interaction.action.UiActionInfo;
import de.fallenstar.core.provider.BasePlot;
import de.fallenstar.core.provider.NamedPlot;
import de.fallenstar.core.provider.NpcContainerPlot;
import de.fallenstar.core.provider.SlottablePlot;
import de.fallenstar.core.provider.StorageContainerPlot;
import de.fallenstar.core.ui.BaseUi;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Handelsgilde-Plot mit vollständiger Trait-Implementierung.
 *
 * **Implementierte Traits:**
 * - NamedPlot - Custom-Namen
 * - StorageContainerPlot - Virtuelles Lager + Preise
 * - NpcContainerPlot - NPC-Verwaltung
 * - SlottablePlot - Trader-Slots
 * - UiTarget - Automatisches UI-Opening beim Klick
 * - NpcDistributor - Automatische NPC-Verteilung auf Slots
 * - QuestDistributor - Automatische Quest-Verteilung an NPCs
 *
 * **Features:**
 * - Click-to-UI: Spieler klickt auf Plot → UI öffnet sich
 * - Owner/Guest-Unterscheidung
 * - Admin-UI bei Shift + Admin-Permission
 * - Self-Constructing UIs aus verfügbaren Aktionen
 * - Automatische NPC-Slot-Zuweisung
 * - Automatische Quest-NPC-Zuweisung
 *
 * @author FallenStar
 * @version 1.0
 */
public class TradeguildPlot extends BasePlot implements
        NamedPlot,
        StorageContainerPlot,
        NpcContainerPlot,
        SlottablePlot,
        UiTarget,
        NpcDistributor,
        QuestDistributor {

    // NamedPlot-Daten
    private String customName;

    // StorageContainerPlot-Daten
    private final Map<Material, Integer> storage = new ConcurrentHashMap<>();
    private final Map<Material, BigDecimal> buyPrices = new ConcurrentHashMap<>();
    private final Map<Material, BigDecimal> sellPrices = new ConcurrentHashMap<>();

    // NpcContainerPlot-Daten
    private final List<UUID> npcIds = Collections.synchronizedList(new ArrayList<>());
    private final Map<UUID, String> npcTypes = new ConcurrentHashMap<>();

    // SlottablePlot-Daten
    private final Map<Integer, UUID> slots = new ConcurrentHashMap<>();
    private int maxSlots = 5; // Default: 5 Trader-Slots

    // NpcDistributor-Daten
    private final Map<UUID, DistributableNpc> npcCache = new ConcurrentHashMap<>();

    // QuestDistributor-Daten
    private final List<DistributableQuest> quests = Collections.synchronizedList(new ArrayList<>());

    // Dependencies (Injected)
    private de.fallenstar.plot.manager.PlotNameManager plotNameManager;
    private de.fallenstar.core.registry.ProviderRegistry providerRegistry;
    private de.fallenstar.plot.PlotModule plotModule;
    private de.fallenstar.plot.storage.manager.StorageManager storageManager;
    private de.fallenstar.plot.storage.provider.PlotStorageProvider storageProvider;

    /**
     * Erstellt einen TradeguildPlot.
     *
     * @param uuid Plot-UUID
     * @param identifier Plot-Identifier
     * @param location Plot-Location
     * @param nativePlot Natives Plot-Objekt (TownBlock)
     */
    public TradeguildPlot(UUID uuid, String identifier, Location location, Object nativePlot) {
        super(uuid, identifier, location, nativePlot);
    }

    /**
     * Setzt den PlotNameManager (Dependency Injection).
     *
     * @param plotNameManager PlotNameManager-Instanz
     */
    public void setPlotNameManager(de.fallenstar.plot.manager.PlotNameManager plotNameManager) {
        this.plotNameManager = plotNameManager;
    }

    /**
     * Setzt die ProviderRegistry (Dependency Injection).
     *
     * @param providerRegistry ProviderRegistry-Instanz
     */
    public void setProviderRegistry(de.fallenstar.core.registry.ProviderRegistry providerRegistry) {
        this.providerRegistry = providerRegistry;
    }

    /**
     * Setzt das PlotModule (Dependency Injection).
     *
     * @param plotModule PlotModule-Instanz
     */
    public void setPlotModule(de.fallenstar.plot.PlotModule plotModule) {
        this.plotModule = plotModule;
    }

    /**
     * Setzt den StorageManager (Dependency Injection).
     *
     * @param storageManager StorageManager-Instanz
     */
    public void setStorageManager(de.fallenstar.plot.storage.manager.StorageManager storageManager) {
        this.storageManager = storageManager;
    }

    /**
     * Setzt den PlotStorageProvider (Dependency Injection).
     *
     * @param storageProvider PlotStorageProvider-Instanz
     */
    public void setPlotStorageProvider(de.fallenstar.plot.storage.provider.PlotStorageProvider storageProvider) {
        this.storageProvider = storageProvider;
    }

    // ========== NamedPlot Implementation ==========

    @Override
    public Optional<String> getCustomName() {
        return Optional.ofNullable(customName);
    }

    @Override
    public void setCustomName(String name) {
        if (!NamedPlot.isValidName(name)) {
            throw new IllegalArgumentException("Ungültiger Plot-Name: " + name);
        }
        this.customName = name;
        // TODO: Speichern in Towny MetaData oder DataStore
    }

    @Override
    public void clearCustomName() {
        this.customName = null;
        // TODO: Aus Towny MetaData oder DataStore löschen
    }

    // ========== StorageContainerPlot Implementation ==========

    @Override
    public int getStoredAmount(Material material) {
        return storage.getOrDefault(material, 0);
    }

    @Override
    public void addToStorage(ItemStack item) {
        storage.merge(item.getType(), item.getAmount(), Integer::sum);
        // TODO: Speichern
    }

    @Override
    public int removeFromStorage(Material material, int amount) {
        int current = getStoredAmount(material);
        int toRemove = Math.min(current, amount);
        if (toRemove > 0) {
            storage.put(material, current - toRemove);
            // TODO: Speichern
        }
        return toRemove;
    }

    @Override
    public Map<Material, Integer> getStorageContents() {
        return new HashMap<>(storage);
    }

    @Override
    public void clearStorage() {
        storage.clear();
        // TODO: Speichern
    }

    @Override
    public Optional<BigDecimal> getBuyPrice(Material material) {
        return Optional.ofNullable(buyPrices.get(material));
    }

    @Override
    public Optional<BigDecimal> getSellPrice(Material material) {
        return Optional.ofNullable(sellPrices.get(material));
    }

    @Override
    public void setBuyPrice(Material material, BigDecimal price) {
        if (price == null) {
            buyPrices.remove(material);
        } else {
            buyPrices.put(material, price);
        }
        // TODO: Speichern
    }

    @Override
    public void setSellPrice(Material material, BigDecimal price) {
        if (price == null) {
            sellPrices.remove(material);
        } else {
            sellPrices.put(material, price);
        }
        // TODO: Speichern
    }

    @Override
    public Map<Material, BigDecimal> getAllBuyPrices() {
        return new HashMap<>(buyPrices);
    }

    @Override
    public Map<Material, BigDecimal> getAllSellPrices() {
        return new HashMap<>(sellPrices);
    }

    // ========== NpcContainerPlot Implementation ==========

    @Override
    public void registerNpc(UUID npcId) {
        if (!npcIds.contains(npcId)) {
            npcIds.add(npcId);
            // TODO: Speichern
        }
    }

    @Override
    public boolean unregisterNpc(UUID npcId) {
        boolean removed = npcIds.remove(npcId);
        if (removed) {
            npcTypes.remove(npcId);
            // TODO: Speichern
        }
        return removed;
    }

    @Override
    public List<UUID> getNpcIds() {
        return new ArrayList<>(npcIds);
    }

    @Override
    public void clearNpcs() {
        npcIds.clear();
        npcTypes.clear();
        // TODO: Speichern
    }

    @Override
    public Optional<String> getNpcType(UUID npcId) {
        return Optional.ofNullable(npcTypes.get(npcId));
    }

    @Override
    public void setNpcType(UUID npcId, String type) {
        if (npcIds.contains(npcId)) {
            npcTypes.put(npcId, type);
            // TODO: Speichern
        }
    }

    // ========== SlottablePlot Implementation ==========

    @Override
    public int getMaxSlots() {
        return maxSlots;
    }

    @Override
    public void setMaxSlots(int maxSlots) {
        this.maxSlots = maxSlots;
        // TODO: Speichern
    }

    @Override
    public boolean isSlotOccupied(int slot) {
        return slots.containsKey(slot);
    }

    @Override
    public Optional<UUID> getNpcInSlot(int slot) {
        return Optional.ofNullable(slots.get(slot));
    }

    @Override
    public boolean placeNpcInSlot(int slot, UUID npcId) {
        if (slot < 0 || slot >= maxSlots) {
            return false;
        }
        if (isSlotOccupied(slot)) {
            return false;
        }
        slots.put(slot, npcId);
        // TODO: Speichern
        return true;
    }

    @Override
    public Optional<UUID> removeNpcFromSlot(int slot) {
        UUID removed = slots.remove(slot);
        if (removed != null) {
            // TODO: Speichern
        }
        return Optional.ofNullable(removed);
    }

    @Override
    public List<Integer> getOccupiedSlots() {
        return new ArrayList<>(slots.keySet());
    }

    @Override
    public void clearAllSlots() {
        slots.clear();
        // TODO: Speichern
    }

    // ========== UiActionTarget Implementation ==========

    @Override
    public List<UiActionInfo> getAvailableActions(Player player, UiContext context) {
        return switch (context) {
            case MAIN_MENU -> getMainMenuActions(player);
            case STORAGE_MENU -> getStorageMenuActions(player);
            case NPC_MENU -> getNpcMenuActions(player);
            case PRICE_MENU -> getPriceMenuActions(player);
            default -> List.of();
        };
    }

    /**
     * @deprecated Ersetzt durch {@link #getAvailablePlotActions(Player)} (Sprint 19 - Phase 2).
     */
    @Deprecated(since = "Sprint 19", forRemoval = true)
    private List<UiActionInfo> getMainMenuActions(Player player) {
        List<UiActionInfo> actions = new ArrayList<>();

        boolean isOwner = isOwner(player);

        // Kombiniere Actions aus allen Traits (Trait-Komposition!)
        // 1. NamedPlot Actions
        actions.addAll(getNameActions());

        // 2. StorageContainerPlot Actions
        actions.addAll(getStorageActions());

        // 3. NpcContainerPlot Actions
        actions.addAll(getNpcActions());

        // 4. Filtere Actions basierend auf Owner-Status
        // Actions mit Permissions werden automatisch gefiltert (siehe GenericInteractionMenuUi)
        // Hier nur explizite Owner-Filterung für Actions ohne Permission-Check
        List<UiActionInfo> filteredActions = new ArrayList<>();
        for (UiActionInfo action : actions) {
            // Owner-exklusive Actions haben requiredPermission
            if (action.requiredPermission().isPresent()) {
                // Prüfe ob Owner oder Permission vorhanden
                if (isOwner || player.hasPermission(action.requiredPermission().get())) {
                    filteredActions.add(action);
                }
            } else {
                // Guest-Actions (keine Permission erforderlich)
                filteredActions.add(action);
            }
        }

        return filteredActions;
    }

    private List<UiActionInfo> getStorageMenuActions(Player player) {
        // TODO: Implementieren
        return List.of();
    }

    private List<UiActionInfo> getNpcMenuActions(Player player) {
        // TODO: Implementieren
        return List.of();
    }

    private List<UiActionInfo> getPriceMenuActions(Player player) {
        // TODO: Implementieren
        return List.of();
    }

    /**
     * Gibt alle verfügbaren PlotActions für einen Spieler zurück (Sprint 19 - Phase 2).
     *
     * **Ersetzt:** getMainMenuActions() + UiActionInfo-System
     *
     * **Trait-Komposition:**
     * Kombiniert Actions aus allen implementierten Traits:
     * - NamedPlot → PlotActionSetName
     * - StorageContainerPlot → PlotActionManageStorage, PlotActionViewPrices, PlotActionManagePrices
     * - NpcContainerPlot → PlotActionManageNpcs
     * - Zusätzlich: PlotActionInfo, PlotActionTeleport
     *
     * **Owner-Filterung:**
     * Actions prüfen via canExecute() automatisch:
     * - requiresOwnership() → isOwner()
     * - requiredPermission() → player.hasPermission()
     * - GuiBuilder filtert unsichtbare Actions via isVisible()
     *
     * **Null-Safety:**
     * Wenn Dependencies nicht injiziert sind, werden Actions übersprungen (defensive Programmierung).
     *
     * @param player Der Spieler
     * @return Liste von PlotActions (nie null, kann aber leer sein)
     */
    public List<de.fallenstar.core.ui.element.PlotAction> getAvailablePlotActions(Player player) {
        List<de.fallenstar.core.ui.element.PlotAction> actions = new ArrayList<>();

        // Null-Safety: Wenn providerRegistry nicht injiziert, returniere leere Liste
        if (providerRegistry == null) {
            return actions;
        }

        // ========== NamedPlot Actions ==========
        actions.add(new de.fallenstar.plot.action.PlotActionSetName(this, providerRegistry));

        // ========== StorageContainerPlot Actions ==========
        if (storageProvider != null && storageManager != null) {
            actions.add(new de.fallenstar.plot.action.PlotActionManageStorage(
                    this, providerRegistry, storageProvider, storageManager
            ));
        }

        actions.add(new de.fallenstar.plot.action.PlotActionViewPrices(this, providerRegistry));
        actions.add(new de.fallenstar.plot.action.PlotActionManagePrices(this, providerRegistry));

        // ========== NpcContainerPlot Actions ==========
        if (plotModule != null) {
            actions.add(new de.fallenstar.plot.action.PlotActionManageNpcs(
                    this, providerRegistry, plotModule
            ));
        }

        // ========== Allgemeine Plot-Actions ==========
        actions.add(new de.fallenstar.plot.action.PlotActionInfo(this, providerRegistry));
        actions.add(new de.fallenstar.plot.action.PlotActionTeleport(this, providerRegistry));

        // Owner-Filterung + Permission-Checks erfolgen via PlotAction.canExecute()!
        // GuiBuilder filtert via isVisible()
        return actions;
    }

    /**
     * @deprecated Ersetzt durch PlotAction-System (Sprint 19 - Phase 2).
     *             Verwende stattdessen {@link #getAvailablePlotActions(Player)}.
     */
    @Deprecated(since = "Sprint 19", forRemoval = true)
    @Override
    public boolean executeAction(Player player, String actionId) {
        return switch (actionId) {
            case "manage_storage" -> {
                // TODO: StorageUi öffnen
                player.sendMessage("§aLager-UI wird implementiert...");
                yield true;
            }
            case "manage_npcs" -> {
                // TODO: NpcManagementUi öffnen
                player.sendMessage("§bNPC-UI wird implementiert...");
                yield true;
            }
            case "manage_prices" -> {
                // TODO: PriceManagementUi öffnen
                player.sendMessage("§ePreis-UI wird implementiert...");
                yield true;
            }
            case "set_name" -> {
                // Öffne Namen-Eingabe via TextInputUI (Type-Safe via Dependency Injection!)
                if (plotNameManager == null) {
                    player.sendMessage("§cPlotNameManager nicht verfügbar!");
                    yield false;
                }

                // Öffne Namen-Eingabe
                de.fallenstar.plot.ui.PlotNameInputUi.openNameInput(
                    player,
                    this,
                    plotNameManager,
                    name -> player.sendMessage("§a✓ Plot-Name gesetzt: §e" + name)
                );
                yield true;
            }
            default -> false;
        };
    }

    // ========== UiTarget Implementation ==========

    @Override
    public Optional<BaseUi> createUi(Player player, InteractionContext context) {
        // Erstelle Generic Interaction Menu (Self-Constructing!)
        UiContext uiContext = UiContext.MAIN_MENU; // Default: Main Menu
        de.fallenstar.core.ui.GenericInteractionMenuUi menu =
                new de.fallenstar.core.ui.GenericInteractionMenuUi(this, player, uiContext);

        return Optional.of(menu);
    }

    @Override
    public InteractionType getInteractionType() {
        return InteractionType.PLOT;
    }

    // ========== NpcDistributor Implementation ==========

    @Override
    public boolean distribute(DistributableNpc npc) {
        if (!hasNpcCapacity()) {
            return false;
        }

        // Finde freien Slot
        List<Integer> freeSlots = getFreeSlots();
        if (freeSlots.isEmpty()) {
            return false;
        }

        // Wähle ersten freien Slot
        int slot = freeSlots.get(0);

        // Berechne Spawn-Location (Plot-Center + Slot-Offset)
        Location spawnLoc = calculateSlotLocation(slot);

        // Spawne NPC
        UUID entityId = npc.spawn(spawnLoc);

        // Platziere in Slot
        placeNpcInSlot(slot, entityId);

        // Registriere in NpcContainer
        registerNpc(entityId);
        setNpcType(entityId, npc.getNpcType());

        // Cache NPC
        npcCache.put(entityId, npc);

        // Callback
        npc.onDistributed(this);

        return true;
    }

    @Override
    public boolean undistribute(DistributableNpc npc) {
        Optional<UUID> entityIdOpt = npc.getEntityId();
        if (entityIdOpt.isEmpty()) {
            return false;
        }

        UUID entityId = entityIdOpt.get();

        // Finde Slot
        Optional<Integer> slotOpt = getSlotForNpc(entityId);
        if (slotOpt.isEmpty()) {
            return false;
        }

        // Entferne aus Slot
        removeNpcFromSlot(slotOpt.get());

        // Entferne aus NpcContainer
        unregisterNpc(entityId);

        // Entferne aus Cache
        npcCache.remove(entityId);

        // Despawne NPC
        npc.despawn();

        // Callback
        npc.onUndistributed();

        return true;
    }

    @Override
    public int getCapacity() {
        return getMaxSlots();
    }

    @Override
    public int getCurrentCount() {
        return getOccupiedSlotCount();
    }

    @Override
    public boolean hasNpcCapacity() {
        return getCurrentCount() < getCapacity();
    }

    @Override
    public List<DistributableNpc> getDistributedNpcs() {
        return new ArrayList<>(npcCache.values());
    }

    @Override
    public Optional<Integer> getSlotForNpc(UUID npcId) {
        return slots.entrySet().stream()
            .filter(e -> e.getValue().equals(npcId))
            .map(Map.Entry::getKey)
            .findFirst();
    }

    /**
     * NpcDistributor-Methode: Gibt den DistributableNpc in einem Slot zurück.
     * (Umbenennung um Konflikt mit SlottablePlot.getNpcInSlot() zu vermeiden)
     */
    @Override
    public Optional<DistributableNpc> getDistributableNpcInSlot(int slot) {
        return this.getNpcInSlot(slot) // SlottablePlot-Methode (gibt UUID zurück)
            .flatMap(npcId -> Optional.ofNullable(npcCache.get(npcId)));
    }

    /**
     * NpcDistributor-Methode: Entfernt einen NPC aus einem Slot (Distributor-Kontext).
     * (Umbenennung um Konflikt mit SlottablePlot.removeNpcFromSlot() zu vermeiden)
     */
    @Override
    public boolean undistributeNpcFromSlot(int slot) {
        Optional<UUID> npcIdOpt = this.getNpcInSlot(slot); // SlottablePlot-Methode
        if (npcIdOpt.isEmpty()) {
            return false;
        }

        UUID npcId = npcIdOpt.get();
        DistributableNpc npc = npcCache.get(npcId);

        if (npc != null) {
            undistribute(npc);
        } else {
            // Fallback: Entferne manuell
            this.removeNpcFromSlot(slot); // SlottablePlot-Methode (gibt Optional<UUID> zurück)
            unregisterNpc(npcId);
        }

        return true;
    }

    /**
     * Berechnet die Spawn-Location für einen Slot.
     *
     * TODO: Bessere Slot-Positionierung implementieren
     *
     * @param slot Slot-Nummer
     * @return Spawn-Location
     */
    private Location calculateSlotLocation(int slot) {
        // Placeholder: Plot-Center + Offset basierend auf Slot
        Location center = getLocation().clone();
        center.add(slot * 2, 0, 0); // 2 Blöcke Abstand pro Slot
        return center;
    }

    // ========== QuestDistributor Implementation ==========

    @Override
    public boolean distribute(DistributableQuest quest) {
        // Hole alle NPCs die Quests halten können
        List<QuestContainer> containers = getQuestContainers();

        if (containers.isEmpty()) {
            return false; // Keine NPCs vorhanden
        }

        // Filtere Container mit Kapazität
        List<QuestContainer> available = containers.stream()
            .filter(QuestContainer::hasQuestCapacity)
            .toList();

        if (available.isEmpty()) {
            return false; // Alle NPCs voll
        }

        // Wähle zufälligen Container
        QuestContainer container = available.get(
            ThreadLocalRandom.current().nextInt(available.size())
        );

        // Weise Quest zu
        boolean success = container.addQuest(quest);
        if (success) {
            quest.setCurrentContainer(container);
            quests.add(quest);
            quest.onDistributed(this);
        }

        return success;
    }

    @Override
    public boolean undistribute(DistributableQuest quest) {
        Optional<QuestContainer> containerOpt = quest.getCurrentContainer();
        if (containerOpt.isEmpty()) {
            return false;
        }

        QuestContainer container = containerOpt.get();
        boolean success = container.removeQuest(quest);

        if (success) {
            quest.setCurrentContainer(null);
            quests.remove(quest);
            quest.onUndistributed();
        }

        return success;
    }

    @Override
    public boolean hasQuestCapacity() {
        return getQuestContainers().stream()
            .anyMatch(QuestContainer::hasQuestCapacity);
    }

    @Override
    public List<QuestContainer> getQuestContainers() {
        // Hole alle NPCs die QuestContainer sind
        return npcCache.values().stream()
            .filter(npc -> npc instanceof QuestContainer)
            .map(npc -> (QuestContainer) npc)
            .toList();
    }

    @Override
    public List<DistributableQuest> getDistributedQuests() {
        return new ArrayList<>(quests);
    }

    // ========== Helper Methods ==========

    /**
     * Prüft ob Spieler Owner des Plots ist.
     *
     * TODO: Implementieren mit PlotProvider.isOwner()
     *
     * @param player Spieler
     * @return true wenn Owner
     */
    private boolean isOwner(Player player) {
        // TODO: PlotProvider.isOwner() aufrufen
        return player.hasPermission("fallenstar.admin"); // Placeholder
    }

    /**
     * Registriert einen DistributableNpc im Cache.
     *
     * Wird von externen Systemen aufgerufen wenn NPC manuell erstellt wurde.
     *
     * @param npc Der NPC
     */
    public void registerDistributableNpc(DistributableNpc npc) {
        npc.getEntityId().ifPresent(entityId -> npcCache.put(entityId, npc));
    }

    /**
     * Gibt einen DistributableNpc aus dem Cache zurück.
     *
     * @param entityId Entity-ID
     * @return Optional mit NPC
     */
    public Optional<DistributableNpc> getDistributableNpc(UUID entityId) {
        return Optional.ofNullable(npcCache.get(entityId));
    }

    // ========== Persistenz ==========

    /**
     * Exportiert alle Plot-Daten in serialisierbares Objekt.
     *
     * Wird von TradeguildPlotFactory zum Speichern verwendet.
     *
     * @return TradeguildPlotData mit allen aktuellen Daten
     */
    public TradeguildPlotData exportData() {
        TradeguildPlotData data = new TradeguildPlotData(getIdentifier());

        // NamedPlot
        data.setCustomName(customName);

        // StorageContainerPlot
        data.setStorage(TradeguildPlotData.convertStorageToStrings(storage));
        data.setBuyPrices(TradeguildPlotData.convertPricesToStrings(buyPrices));
        data.setSellPrices(TradeguildPlotData.convertPricesToStrings(sellPrices));

        // NpcContainerPlot
        data.setNpcIds(TradeguildPlotData.convertUuidsToStrings(npcIds));
        data.setNpcTypes(TradeguildPlotData.convertUuidMapToStrings(npcTypes));

        // SlottablePlot
        data.setSlots(TradeguildPlotData.convertSlotMapToStrings(slots));
        data.setMaxSlots(maxSlots);

        // QuestDistributor
        List<String> questIdStrings = quests.stream()
                .map(q -> q.getId().toString())
                .collect(Collectors.toList());
        data.setQuestIds(questIdStrings);

        return data;
    }

    /**
     * Importiert Plot-Daten aus serialisiertem Objekt.
     *
     * Wird von TradeguildPlotFactory beim Laden verwendet.
     *
     * HINWEIS: npcCache wird NICHT wiederhergestellt (nur zur Laufzeit)!
     * Quest-Objekte müssen separat geladen und via distribute() zugewiesen werden!
     *
     * @param data Die zu importierenden Daten
     */
    public void importData(TradeguildPlotData data) {
        if (data == null) {
            return;
        }

        // NamedPlot
        this.customName = data.getCustomName();

        // StorageContainerPlot
        this.storage.clear();
        this.storage.putAll(TradeguildPlotData.convertStorageFromStrings(data.getStorage()));

        this.buyPrices.clear();
        this.buyPrices.putAll(TradeguildPlotData.convertPricesFromStrings(data.getBuyPrices()));

        this.sellPrices.clear();
        this.sellPrices.putAll(TradeguildPlotData.convertPricesFromStrings(data.getSellPrices()));

        // NpcContainerPlot
        this.npcIds.clear();
        this.npcIds.addAll(TradeguildPlotData.convertUuidsFromStrings(data.getNpcIds()));

        this.npcTypes.clear();
        this.npcTypes.putAll(TradeguildPlotData.convertUuidMapFromStrings(data.getNpcTypes()));

        // SlottablePlot
        this.slots.clear();
        this.slots.putAll(TradeguildPlotData.convertSlotMapFromStrings(data.getSlots()));
        this.maxSlots = data.getMaxSlots();

        // QuestDistributor - Quest-IDs werden gespeichert, aber Objekte müssen separat geladen werden
        // quests.clear(); // NICHT clearen! Quests werden via QuestDistributor.distribute() zugewiesen
    }
}
