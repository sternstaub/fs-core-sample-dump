package de.fallenstar.items.model;

import de.fallenstar.core.currency.CurrencyItem;
import de.fallenstar.items.manager.SpecialItemManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;
import java.util.Optional;

/**
 * Vanilla Coin Implementierung für CurrencyItem (Sprint 19 Phase 3).
 *
 * **Design:**
 * - Wrapper um SpecialItemDefinition
 * - Nutzt SpecialItemManager für ItemStack-Erstellung
 * - Kompatibel mit bestehendem SpecialItem-System
 *
 * **SOLID-Prinzipien:**
 *
 * **Single Responsibility:**
 * - Nur Currency-Darstellung als ItemStack
 * - KEINE Value-Berechnung, KEIN Storage
 *
 * **Liskov Substitution:**
 * - Kann gegen CurrencyItem Interface ausgetauscht werden
 * - CoinProvider funktioniert mit VanillaCoinItem + MMOItemsCoinItem
 *
 * **Dependency Inversion:**
 * - Hängt von CurrencyItem-Interface ab
 * - SpecialItemManager injiziert (nicht hart-kodiert)
 *
 * **Integration mit SpecialItem-System:**
 * - VanillaCoinItem VERWENDET SpecialItemDefinition
 * - SpecialItemManager erstellt ItemStacks (PDC-basiert)
 * - Keine Duplikation von Item-Erstellung
 *
 * **Verwendung:**
 * <pre>
 * // ItemsModule Initialisierung:
 * SpecialItemManager itemManager = new SpecialItemManager(...);
 *
 * // Registriere SpecialItem (für SpecialItemManager):
 * itemManager.registerVanillaItem(
 *     "vanilla_star",
 *     Material.NETHER_STAR,
 *     1,
 *     Component.text("§6Stern"),
 *     List.of(Component.text("§7Währung"))
 * );
 *
 * // Erstelle VanillaCoinItem (für CurrencyRegistry):
 * SpecialItemDefinition definition = itemManager.getDefinition("vanilla_star").get();
 * VanillaCoinItem coinItem = new VanillaCoinItem(definition, itemManager);
 *
 * // Registriere in CurrencyRegistry:
 * currencyRegistry.register(coinItem);
 *
 * // Verwendung:
 * ItemStack coin = coinItem.getDisplayItem(10);
 * player.getInventory().addItem(coin);
 * </pre>
 *
 * **Architektur:**
 * <pre>
 * CurrencyRegistry → VanillaCoinItem → SpecialItemDefinition
 *                                    ↓
 *                         SpecialItemManager (erstellt ItemStacks)
 * </pre>
 *
 * @author FallenStar
 * @version 1.0
 * @see CurrencyItem
 * @see SpecialItemDefinition
 * @see SpecialItemManager
 */
public class VanillaCoinItem implements CurrencyItem {

    /**
     * Die SpecialItem-Definition für diesen Coin.
     */
    private final SpecialItemDefinition definition;

    /**
     * Der SpecialItemManager für ItemStack-Erstellung.
     */
    private final SpecialItemManager itemManager;

    /**
     * Cached Display-Name (Legacy Format für CurrencyItem Interface).
     */
    private final String displayName;

    /**
     * Erstellt ein VanillaCoinItem.
     *
     * **Validierung:**
     * - definition MUSS VANILLA sein (keine MMOItems!)
     * - itemManager != null
     *
     * @param definition SpecialItemDefinition (MUSS VANILLA sein!)
     * @param itemManager SpecialItemManager für ItemStack-Erstellung
     * @throws NullPointerException wenn definition oder itemManager null ist
     * @throws IllegalArgumentException wenn definition kein VANILLA Item ist
     */
    public VanillaCoinItem(SpecialItemDefinition definition, SpecialItemManager itemManager) {
        this.definition = Objects.requireNonNull(definition, "SpecialItemDefinition darf nicht null sein");
        this.itemManager = Objects.requireNonNull(itemManager, "SpecialItemManager darf nicht null sein");

        if (!definition.isVanilla()) {
            throw new IllegalArgumentException(
                "VanillaCoinItem erfordert VANILLA SpecialItemDefinition! " +
                "Für MMOItems nutze MMOItemsCoinItem."
            );
        }

        // Cache Display-Name in Legacy-Format
        this.displayName = definition.displayName() != null
            ? LegacyComponentSerializer.legacySection().serialize(definition.displayName())
            : definition.id();
    }

    @Override
    public String getIdentifier() {
        return definition.id();
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public Material getMaterial() {
        return definition.material();
    }

    @Override
    public ItemStack getDisplayItem() {
        // Nutze SpecialItemManager für konsistente ItemStack-Erstellung
        Optional<ItemStack> itemOpt = itemManager.createItem(definition.id(), 1);

        if (itemOpt.isEmpty()) {
            // Fallback: Manuell erstellen (sollte nicht passieren)
            ItemStack fallback = new ItemStack(definition.material(), 1);
            // Minimal-Item ohne Metadata
            return fallback;
        }

        return itemOpt.get();
    }

    @Override
    public int getCustomModelData() {
        return definition.customModelData();
    }

    @Override
    public boolean isCurrency(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }

        // Prüfe via SpecialItemManager (PDC-basiert)
        Optional<String> itemId = itemManager.getSpecialItemId(item);

        // Vergleiche mit Definition-ID
        return itemId.isPresent() && itemId.get().equals(definition.id());
    }

    /**
     * Gibt die interne SpecialItemDefinition zurück.
     *
     * **Hinweis:** Nur für interne Verwendung!
     * Normale Module sollten CurrencyItem-Interface nutzen.
     *
     * @return Die SpecialItemDefinition
     */
    public SpecialItemDefinition getDefinition() {
        return definition;
    }

    @Override
    public String toString() {
        return "VanillaCoinItem{" +
               "id='" + getIdentifier() + '\'' +
               ", displayName='" + getDisplayName() + '\'' +
               ", material=" + getMaterial() +
               ", customModelData=" + getCustomModelData() +
               '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof VanillaCoinItem other)) return false;
        return Objects.equals(definition.id(), other.definition.id());
    }

    @Override
    public int hashCode() {
        return Objects.hash(definition.id());
    }
}
