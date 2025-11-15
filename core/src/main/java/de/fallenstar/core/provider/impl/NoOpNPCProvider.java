package de.fallenstar.core.provider.impl;

import de.fallenstar.core.exception.ProviderFunctionalityNotFoundException;
import de.fallenstar.core.provider.NPCProvider;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * NoOp (No Operation) Implementation des NPCProviders.
 *
 * Wird verwendet wenn kein NPC-Plugin (Citizens, ZNPC) verfügbar ist.
 * Alle Methoden werfen ProviderFunctionalityNotFoundException.
 *
 * Pattern: Null Object Pattern kombiniert mit Exception-Handling
 * Vorteil: Kein Null-Checking nötig, explizites Fehlerhandling
 *
 * @author FallenStar
 * @version 1.0
 */
public class NoOpNPCProvider implements NPCProvider {

    private static final String PROVIDER_NAME = "NPCProvider";
    private static final String REASON = "No NPC plugin (Citizens, ZNPC) available";

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public UUID createNPC(Location location, String name, String skin)
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            PROVIDER_NAME,
            "createNPC",
            REASON
        );
    }

    @Override
    public boolean removeNPC(UUID npcId) throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            PROVIDER_NAME,
            "removeNPC",
            REASON
        );
    }

    @Override
    public boolean teleportNPC(UUID npcId, Location location)
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            PROVIDER_NAME,
            "teleportNPC",
            REASON
        );
    }

    @Override
    public void setClickHandler(UUID npcId, Consumer<Player> handler)
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            PROVIDER_NAME,
            "setClickHandler",
            REASON
        );
    }

    @Override
    public boolean setSkin(UUID npcId, String skin)
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            PROVIDER_NAME,
            "setSkin",
            REASON
        );
    }

    @Override
    public boolean npcExists(UUID npcId) throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            PROVIDER_NAME,
            "npcExists",
            REASON
        );
    }
}
