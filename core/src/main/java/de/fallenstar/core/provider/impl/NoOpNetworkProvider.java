package de.fallenstar.core.provider.impl;

import de.fallenstar.core.exception.ProviderFunctionalityNotFoundException;
import de.fallenstar.core.provider.NetworkProvider;
import org.bukkit.entity.Player;
import java.util.List;
import java.util.UUID;

/**
 * NoOp (No Operation) Implementation des NetworkProviders.
 *
 * Wird verwendet wenn kein Proxy-System (Velocity, BungeeCord) verfügbar ist.
 * Alle Methoden werfen ProviderFunctionalityNotFoundException.
 *
 * Pattern: Null Object Pattern kombiniert mit Exception-Handling
 * Vorteil: Kein Null-Checking nötig, explizites Fehlerhandling
 *
 * @author FallenStar
 * @version 1.0
 */
public class NoOpNetworkProvider implements NetworkProvider {

    private static final String PROVIDER_NAME = "NetworkProvider";
    private static final String REASON = "No proxy system (Velocity, BungeeCord) available - standalone server";

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public List<String> getServers() throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            PROVIDER_NAME,
            "getServers",
            REASON
        );
    }

    @Override
    public void transferPlayer(Player player, String serverName)
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            PROVIDER_NAME,
            "transferPlayer",
            REASON
        );
    }

    @Override
    public void sendPluginMessage(String targetServer, String channel, byte[] data)
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            PROVIDER_NAME,
            "sendPluginMessage",
            REASON
        );
    }

    @Override
    public void registerMessageListener(String channel, MessageHandler handler)
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            PROVIDER_NAME,
            "registerMessageListener",
            REASON
        );
    }

    @Override
    public String getCurrentServerName() throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            PROVIDER_NAME,
            "getCurrentServerName",
            REASON
        );
    }

    @Override
    public int getPlayerCount(String serverName)
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            PROVIDER_NAME,
            "getPlayerCount",
            REASON
        );
    }

    @Override
    public boolean isServerOnline(String serverName)
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            PROVIDER_NAME,
            "isServerOnline",
            REASON
        );
    }

    @Override
    public String findPlayer(UUID playerUuid)
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            PROVIDER_NAME,
            "findPlayer",
            REASON
        );
    }
}
