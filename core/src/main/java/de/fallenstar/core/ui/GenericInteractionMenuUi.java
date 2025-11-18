package de.fallenstar.core.ui;

import de.fallenstar.core.interaction.UiContext;
import de.fallenstar.core.interaction.UiTarget;
import de.fallenstar.core.interaction.action.UiActionInfo;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generisches Interaktionsmenü für UiTargets.
 *
 * **Konzept:**
 * - Zeigt alle verfügbaren Aktionen eines UiTargets
 * - Self-Constructing: UI baut sich aus getAvailableActions() automatisch auf
 * - Universell einsetzbar für NPCs, Plots, Items, etc.
 *
 * **Features:**
 * - Automatische Button-Generierung aus UiActionInfo
 * - Kontextabhängig (Owner/Guest, Permissions, etc.)
 * - Click-Handler delegiert an UiTarget.executeAction()
 *
 * **Verwendung:**
 * <pre>
 * // Für NPC
 * UiTarget npc = ...;
 * GenericInteractionMenuUi menu = new GenericInteractionMenuUi(npc, player, context);
 * menu.open(player);
 *
 * // Für Plot
 * UiTarget plot = ...;
 * GenericInteractionMenuUi menu = new GenericInteractionMenuUi(plot, player, context);
 * menu.open(player);
 * </pre>
 *
 * **Button-Layout:**
 * - Slot 0-8: Verfügbare Aktionen (bis zu 9)
 * - Slot 9-17: Weitere Aktionen (falls mehr als 9)
 * - Slot 26: Schließen-Button (immer)
 *
 * @author FallenStar
 * @version 1.0
 */
public class GenericInteractionMenuUi extends SmallChestUi {

    private final UiTarget target;
    private final Player viewer;
    private final UiContext context;
    private final Map<Integer, String> slotToActionId = new HashMap<>();

    /**
     * Erstellt ein Generic Interaction Menu.
     *
     * @param target Das UiTarget (NPC, Plot, etc.)
     * @param viewer Der Spieler der das Menü öffnet
     * @param context Der Interaktionskontext
     */
    public GenericInteractionMenuUi(UiTarget target, Player viewer, UiContext context) {
        super("§6§lInteraktionsmenü");
        this.target = target;
        this.viewer = viewer;
        this.context = context;
        buildMenu();
    }

    /**
     * Baut das Menü aus den verfügbaren Aktionen.
     */
    private void buildMenu() {
        // Hole alle verfügbaren Aktionen
        List<UiActionInfo> actions = target.getAvailableActions(viewer, context);

        // Erstelle Buttons für jede Aktion
        int slot = 0;
        for (UiActionInfo action : actions) {
            if (slot >= 26) break; // Max 26 Aktionen (Slot 26 = Schließen)

            ItemStack button = createActionButton(action);
            setItem(slot, button);
            slotToActionId.put(slot, action.getActionId());

            slot++;
        }

        // Schließen-Button (immer Slot 26)
        ItemStack closeButton = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeButton.getItemMeta();
        if (closeMeta != null) {
            closeMeta.setDisplayName("§c§lSchließen");
            closeMeta.setLore(List.of("§7Klicken um zu schließen"));
            closeButton.setItemMeta(closeMeta);
        }
        setItem(26, closeButton);
    }

    /**
     * Erstellt einen Button für eine Aktion.
     *
     * @param action Die Aktion
     * @return ItemStack-Button
     */
    private ItemStack createActionButton(UiActionInfo action) {
        ItemStack button = new ItemStack(action.getIcon());
        ItemMeta meta = button.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(action.getDisplayName());

            if (action.getDescription() != null && !action.getDescription().isEmpty()) {
                meta.setLore(List.of("§7" + action.getDescription()));
            }

            button.setItemMeta(meta);
        }

        return button;
    }

    @Override
    protected void handleClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true); // Verhindere Item-Manipulation

        int slot = event.getSlot();

        // Schließen-Button
        if (slot == 26) {
            player.closeInventory();
            return;
        }

        // Aktions-Button
        String actionId = slotToActionId.get(slot);
        if (actionId != null) {
            boolean success = target.executeAction(player, actionId);

            if (success) {
                // Aktualisiere Menü (Aktionen könnten sich geändert haben)
                rebuild();
            } else {
                player.sendMessage("§cDiese Aktion konnte nicht ausgeführt werden.");
            }
        }
    }

    /**
     * Baut das Menü neu auf (z.B. nach Aktions-Ausführung).
     */
    private void rebuild() {
        // Leere alte Daten
        clearItems();
        slotToActionId.clear();

        // Baue Menü neu
        buildMenu();

        // Öffne UI erneut (triggert Rebuild in open())
        open(viewer);
    }
}
