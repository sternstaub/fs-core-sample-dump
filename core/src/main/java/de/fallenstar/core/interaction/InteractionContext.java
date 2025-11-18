package de.fallenstar.core.interaction;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

/**
 * Kontext einer Spieler-Interaktion.
 *
 * Erfasst alle relevanten Details eines Klicks:
 * - Spieler
 * - Klick-Typ (Links/Rechts)
 * - Shift-Status
 * - Geklicktes Objekt (Block/Entity)
 * - Item in Hand
 *
 * @param player Spieler der interagiert
 * @param isSneaking Ob Spieler shiftet
 * @param isLeftClick Ob Links-Klick (false = Rechts-Klick)
 * @param clickedBlock Optional: Geklickter Block
 * @param clickedEntity Optional: Geklickte Entity
 * @param itemInHand Optional: Item in Hand
 * @param location Location der Interaktion
 *
 * @author FallenStar
 * @version 1.0
 */
public record InteractionContext(
        Player player,
        boolean isSneaking,
        boolean isLeftClick,
        Optional<Block> clickedBlock,
        Optional<Entity> clickedEntity,
        Optional<ItemStack> itemInHand,
        Location location
) {

    /**
     * Prüft ob Spieler Admin-Rechte hat und shiftet.
     *
     * @return true wenn Shift + Admin-Permission
     */
    public boolean isAdminInteraction() {
        return isSneaking && player.hasPermission("fallenstar.admin");
    }

    /**
     * Erstellt InteractionContext aus PlayerInteractEvent.
     *
     * @param event PlayerInteractEvent
     * @param type InteractionType
     * @return InteractionContext
     */
    public static InteractionContext fromEvent(PlayerInteractEvent event, InteractionType type) {
        Player player = event.getPlayer();
        boolean isSneaking = player.isSneaking();
        boolean isLeftClick = event.getAction() == Action.LEFT_CLICK_BLOCK
                || event.getAction() == Action.LEFT_CLICK_AIR;

        Optional<Block> clickedBlock = Optional.ofNullable(event.getClickedBlock());
        Optional<ItemStack> itemInHand = Optional.ofNullable(event.getItem());

        Location location = clickedBlock
                .map(Block::getLocation)
                .orElse(player.getLocation());

        return new InteractionContext(
                player,
                isSneaking,
                isLeftClick,
                clickedBlock,
                Optional.empty(),
                itemInHand,
                location
        );
    }

    /**
     * Erstellt InteractionContext aus PlayerInteractEntityEvent.
     *
     * @param event PlayerInteractEntityEvent
     * @return InteractionContext
     */
    public static InteractionContext fromEntityEvent(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        boolean isSneaking = player.isSneaking();
        Entity entity = event.getRightClicked();

        Optional<ItemStack> itemInHand = Optional.ofNullable(player.getInventory().getItemInMainHand())
                .filter(item -> !item.getType().isAir());

        return new InteractionContext(
                player,
                isSneaking,
                false, // Entity-Interaktionen sind immer Rechts-Klick
                Optional.empty(),
                Optional.of(entity),
                itemInHand,
                entity.getLocation()
        );
    }
}
