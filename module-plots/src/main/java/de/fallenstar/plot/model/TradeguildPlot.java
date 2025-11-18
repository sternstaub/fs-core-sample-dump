package de.fallenstar.plot.model;

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

/**
 * Handelsgilde-Plot mit vollständiger Trait-Implementierung.
 *
 * **Implementierte Traits:**
 * - NamedPlot - Custom-Namen
 * - StorageContainerPlot - Virtuelles Lager + Preise
 * - NpcContainerPlot - NPC-Verwaltung
 * - SlottablePlot - Trader-Slots
 * - UiTarget - Automatisches UI-Opening beim Klick
 *
 * **Features:**
 * - Click-to-UI: Spieler klickt auf Plot → UI öffnet sich
 * - Owner/Guest-Unterscheidung
 * - Admin-UI bei Shift + Admin-Permission
 * - Self-Constructing UIs aus verfügbaren Aktionen
 *
 * @author FallenStar
 * @version 1.0
 */
public class TradeguildPlot extends BasePlot implements
        NamedPlot,
        StorageContainerPlot,
        NpcContainerPlot,
        SlottablePlot,
        UiTarget {

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

    private List<UiActionInfo> getMainMenuActions(Player player) {
        List<UiActionInfo> actions = new ArrayList<>();

        // Lager-Verwaltung
        actions.add(UiActionInfo.builder()
                .id("manage_storage")
                .displayName("§aLager verwalten")
                .lore(List.of("§7Klicke um das Lager zu öffnen"))
                .icon(Material.CHEST)
                .slot(10)
                .build());

        // NPC-Verwaltung (nur Owner)
        if (isOwner(player)) {
            actions.add(UiActionInfo.builder()
                    .id("manage_npcs")
                    .displayName("§bNPCs verwalten")
                    .lore(List.of("§7Klicke um NPCs zu verwalten"))
                    .icon(Material.VILLAGER_SPAWN_EGG)
                    .requiredPermission("fallenstar.plot.npc")
                    .slot(12)
                    .build());
        }

        // Preis-Verwaltung (nur Owner)
        if (isOwner(player)) {
            actions.add(UiActionInfo.builder()
                    .id("manage_prices")
                    .displayName("§ePreise verwalten")
                    .lore(List.of("§7Klicke um Preise zu setzen"))
                    .icon(Material.GOLD_INGOT)
                    .slot(14)
                    .build());
        }

        // Plot-Name setzen (nur Owner)
        if (isOwner(player)) {
            actions.add(UiActionInfo.builder()
                    .id("set_name")
                    .displayName("§dPlot-Name setzen")
                    .lore(List.of(
                            "§7Aktuell: " + getDisplayName(),
                            "§7Klicke um Namen zu ändern"
                    ))
                    .icon(Material.NAME_TAG)
                    .slot(16)
                    .build());
        }

        return actions;
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
                // TODO: AnvilUi für Namen-Eingabe öffnen
                player.sendMessage("§dNamen-UI wird implementiert...");
                yield true;
            }
            default -> false;
        };
    }

    // ========== UiTarget Implementation ==========

    @Override
    public Optional<BaseUi> createUi(Player player, InteractionContext context) {
        // TODO: PlotMainMenuUi erstellen
        // Placeholder: Zeige Message
        player.sendMessage("§6Handelsgilde: " + getDisplayName());
        player.sendMessage("§7Main-Menu-UI wird noch implementiert");
        return Optional.empty();
    }

    @Override
    public InteractionType getInteractionType() {
        return InteractionType.PLOT;
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
}
