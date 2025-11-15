package de.fallenstar.core.provider.impl;

import de.fallenstar.core.exception.ProviderFunctionalityNotFoundException;
import de.fallenstar.core.provider.ChatProvider;
import java.util.UUID;

/**
 * NoOp (No Operation) Implementation des ChatProviders.
 *
 * Wird verwendet wenn kein externes Chat-System (Matrix, Discord) verfügbar ist.
 * Alle Methoden werfen ProviderFunctionalityNotFoundException.
 *
 * Pattern: Null Object Pattern kombiniert mit Exception-Handling
 * Vorteil: Kein Null-Checking nötig, explizites Fehlerhandling
 *
 * @author FallenStar
 * @version 1.0
 */
public class NoOpChatProvider implements ChatProvider {

    private static final String PROVIDER_NAME = "ChatProvider";
    private static final String REASON = "No external chat system (Matrix, Discord) available";

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public void sendMessage(String roomId, String message)
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            PROVIDER_NAME,
            "sendMessage",
            REASON
        );
    }

    @Override
    public void sendDirectMessage(String userId, String message)
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            PROVIDER_NAME,
            "sendDirectMessage",
            REASON
        );
    }

    @Override
    public void linkUser(UUID minecraftUuid, String externalUserId)
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            PROVIDER_NAME,
            "linkUser",
            REASON
        );
    }

    @Override
    public String getLinkedUser(UUID minecraftUuid)
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            PROVIDER_NAME,
            "getLinkedUser",
            REASON
        );
    }

    @Override
    public void sendEmbedMessage(String roomId, String title, String description, String color)
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            PROVIDER_NAME,
            "sendEmbedMessage",
            REASON
        );
    }

    @Override
    public void registerExternalCommand(String command, ExternalCommandHandler handler)
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            PROVIDER_NAME,
            "registerExternalCommand",
            REASON
        );
    }
}
