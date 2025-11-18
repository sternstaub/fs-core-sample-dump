package de.fallenstar.core.interaction.handler;

import de.fallenstar.core.interaction.Interactable;
import de.fallenstar.core.interaction.InteractionContext;
import de.fallenstar.core.interaction.InteractionType;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Optional;
import java.util.logging.Logger;

/**
 * Event-Listener für Spieler-Interaktionen.
 *
 * Routet Klicks zu entsprechenden Interactable-Objekten.
 *
 * **Workflow:**
 * 1. Spieler klickt auf Block/Entity
 * 2. Event wird gefeuert
 * 3. InteractionHandler fängt Event ab
 * 4. Registry wird nach Interactable durchsucht
 * 5. Interactable.onInteract() wird aufgerufen
 * 6. Event wird ggf. gecancelt
 *
 * **Features:**
 * - Automatisches Click-Routing
 * - Permission-Checks
 * - Event-Cancellation bei erfolgreicher Behandlung
 * - Debug-Logging
 *
 * @author FallenStar
 * @version 1.0
 */
public class InteractionHandler implements Listener {

    private final InteractionRegistry registry;
    private final Logger logger;

    /**
     * Erstellt einen InteractionHandler.
     *
     * @param registry InteractionRegistry
     * @param logger Logger für Debug-Ausgaben
     */
    public InteractionHandler(InteractionRegistry registry, Logger logger) {
        this.registry = registry;
        this.logger = logger;
    }

    /**
     * Behandelt Block-Interaktionen.
     *
     * Wird bei PlayerInteractEvent gefeuert.
     *
     * @param event PlayerInteractEvent
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Nur Rechts/Links-Klick auf Blöcke
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_BLOCK && action != Action.LEFT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        Location location = event.getClickedBlock().getLocation();

        // Nach Interactable an dieser Location suchen
        Optional<Interactable> plotOpt = registry.getInteractableAtLocation(location);
        if (plotOpt.isEmpty()) {
            return; // Kein Interactable an dieser Location
        }

        Interactable interactable = plotOpt.get();

        // Permission-Check
        if (!interactable.canInteract(player)) {
            player.sendMessage("§cDu hast keine Berechtigung dazu!");
            event.setCancelled(true);
            return;
        }

        // InteractionContext erstellen
        InteractionContext context = InteractionContext.fromEvent(event, InteractionType.BLOCK);

        // Debug-Logging
        logger.fine(String.format(
                "Block-Interaktion: %s klickt auf %s (Type: %s, Shift: %s)",
                player.getName(),
                interactable.getClass().getSimpleName(),
                interactable.getInteractionType(),
                context.isSneaking()
        ));

        // Interactable behandeln lassen
        boolean handled = interactable.onInteract(player, context);

        // Event canceln wenn erfolgreich behandelt
        if (handled) {
            event.setCancelled(true);
            logger.fine(String.format(
                    "Block-Interaktion erfolgreich behandelt von %s",
                    interactable.getClass().getSimpleName()
            ));
        }
    }

    /**
     * Behandelt Entity-Interaktionen.
     *
     * Wird bei PlayerInteractEntityEvent gefeuert.
     *
     * @param event PlayerInteractEntityEvent
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();

        // Nach Interactable für diese Entity suchen
        Optional<Interactable> npcOpt = registry.getInteractableForEntity(entity);
        if (npcOpt.isEmpty()) {
            return; // Kein Interactable für diese Entity
        }

        Interactable interactable = npcOpt.get();

        // Permission-Check
        if (!interactable.canInteract(player)) {
            player.sendMessage("§cDu hast keine Berechtigung dazu!");
            event.setCancelled(true);
            return;
        }

        // InteractionContext erstellen
        InteractionContext context = InteractionContext.fromEntityEvent(event);

        // Debug-Logging
        logger.fine(String.format(
                "Entity-Interaktion: %s klickt auf %s (Type: %s, Shift: %s)",
                player.getName(),
                interactable.getClass().getSimpleName(),
                interactable.getInteractionType(),
                context.isSneaking()
        ));

        // Interactable behandeln lassen
        boolean handled = interactable.onInteract(player, context);

        // Event canceln wenn erfolgreich behandelt
        if (handled) {
            event.setCancelled(true);
            logger.fine(String.format(
                    "Entity-Interaktion erfolgreich behandelt von %s",
                    interactable.getClass().getSimpleName()
            ));
        }
    }

    /**
     * Gibt die Registry zurück.
     *
     * @return InteractionRegistry
     */
    public InteractionRegistry getRegistry() {
        return registry;
    }
}
