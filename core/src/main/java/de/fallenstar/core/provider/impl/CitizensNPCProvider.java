package de.fallenstar.core.provider.impl;

import de.fallenstar.core.exception.ProviderFunctionalityNotFoundException;
import de.fallenstar.core.provider.NPCProvider;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Citizens NPC Provider Implementation.
 *
 * Integriert mit dem Citizens-Plugin für NPC-Management.
 * Verwaltet NPCs für Händler, Reisende, etc.
 *
 * @author FallenStar
 * @version 1.0
 */
public class CitizensNPCProvider implements NPCProvider, Listener {

    private final Logger logger;
    private final Plugin plugin;
    private final NPCRegistry registry;
    private final Map<UUID, Consumer<Player>> clickHandlers;

    /**
     * Erstellt einen neuen CitizensNPCProvider.
     *
     * @param plugin Plugin-Instanz für Event-Registrierung
     * @param logger Logger für Ausgaben
     */
    public CitizensNPCProvider(Plugin plugin, Logger logger) {
        this.logger = logger;
        this.plugin = plugin;
        this.clickHandlers = new ConcurrentHashMap<>();

        if (isAvailable()) {
            this.registry = CitizensAPI.getNPCRegistry();
            Bukkit.getPluginManager().registerEvents(this, plugin);
            logger.info("✓ CitizensNPCProvider initialized");
        } else {
            this.registry = null;
            logger.warning("✗ CitizensNPCProvider: Citizens not found");
        }
    }

    @Override
    public boolean isAvailable() {
        return Bukkit.getPluginManager().getPlugin("Citizens") != null;
    }

    @Override
    public UUID createNPC(Location location, String name, String skin)
            throws ProviderFunctionalityNotFoundException {
        if (!isAvailable()) {
            throw new ProviderFunctionalityNotFoundException(
                "NPCProvider",
                "createNPC",
                "Citizens plugin not available"
            );
        }

        NPC npc = registry.createNPC(EntityType.PLAYER, name);

        // Skin setzen
        if (skin != null && !skin.isEmpty()) {
            SkinTrait skinTrait = npc.getOrAddTrait(SkinTrait.class);
            skinTrait.setSkinName(skin);
        }

        // Spawnen
        npc.spawn(location);

        logger.info("Created NPC: " + name + " (" + npc.getUniqueId() + ")");
        return npc.getUniqueId();
    }

    @Override
    public boolean removeNPC(UUID npcId) throws ProviderFunctionalityNotFoundException {
        if (!isAvailable()) {
            throw new ProviderFunctionalityNotFoundException(
                "NPCProvider",
                "removeNPC",
                "Citizens plugin not available"
            );
        }

        NPC npc = registry.getByUniqueId(npcId);
        if (npc == null) {
            return false;
        }

        npc.destroy();
        clickHandlers.remove(npcId);
        logger.info("Removed NPC: " + npc.getName() + " (" + npcId + ")");
        return true;
    }

    @Override
    public boolean teleportNPC(UUID npcId, Location location)
            throws ProviderFunctionalityNotFoundException {
        if (!isAvailable()) {
            throw new ProviderFunctionalityNotFoundException(
                "NPCProvider",
                "teleportNPC",
                "Citizens plugin not available"
            );
        }

        NPC npc = registry.getByUniqueId(npcId);
        if (npc == null || !npc.isSpawned()) {
            return false;
        }

        return npc.getEntity().teleport(location);
    }

    @Override
    public void setClickHandler(UUID npcId, Consumer<Player> handler)
            throws ProviderFunctionalityNotFoundException {
        if (!isAvailable()) {
            throw new ProviderFunctionalityNotFoundException(
                "NPCProvider",
                "setClickHandler",
                "Citizens plugin not available"
            );
        }

        clickHandlers.put(npcId, handler);
    }

    @Override
    public boolean setSkin(UUID npcId, String skin)
            throws ProviderFunctionalityNotFoundException {
        if (!isAvailable()) {
            throw new ProviderFunctionalityNotFoundException(
                "NPCProvider",
                "setSkin",
                "Citizens plugin not available"
            );
        }

        NPC npc = registry.getByUniqueId(npcId);
        if (npc == null) {
            return false;
        }

        SkinTrait skinTrait = npc.getOrAddTrait(SkinTrait.class);
        skinTrait.setSkinName(skin);
        return true;
    }

    @Override
    public boolean npcExists(UUID npcId) throws ProviderFunctionalityNotFoundException {
        if (!isAvailable()) {
            throw new ProviderFunctionalityNotFoundException(
                "NPCProvider",
                "npcExists",
                "Citizens plugin not available"
            );
        }

        return registry.getByUniqueId(npcId) != null;
    }

    /**
     * Event-Handler für NPC-Clicks.
     * Wird von Citizens gefeuert wenn ein Spieler einen NPC anklickt.
     *
     * @param event Click-Event
     */
    @EventHandler
    public void onNPCClick(net.citizensnpcs.api.event.NPCRightClickEvent event) {
        UUID npcId = event.getNPC().getUniqueId();
        Consumer<Player> handler = clickHandlers.get(npcId);

        if (handler != null) {
            handler.accept(event.getClicker());
        }
    }
}
