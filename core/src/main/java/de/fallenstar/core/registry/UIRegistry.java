package de.fallenstar.core.registry;

import de.fallenstar.core.ui.*;

import java.util.*;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * Registry für Test-UIs.
 *
 * Module können ihre UI-Implementierungen hier registrieren
 * um sie über /fscore admin gui testbar zu machen.
 *
 * Jedes registrierte UI muss einen parameterfreien Konstruktor
 * (oder Supplier) bereitstellen für Test-Instanzen.
 *
 * @author FallenStar
 * @version 1.0
 */
public class UIRegistry {

    private final Logger logger;
    private final Map<String, UIRegistration> registeredUIs;

    /**
     * Konstruktor für UIRegistry.
     *
     * @param logger Logger-Instanz
     */
    public UIRegistry(Logger logger) {
        this.logger = logger;
        this.registeredUIs = new LinkedHashMap<>();
    }

    /**
     * Registriert ein Test-UI.
     *
     * @param id Eindeutige ID des UI (z.B. "ambassador", "market_trader")
     * @param displayName Anzeigename für /fscore admin gui Liste
     * @param description Beschreibung des UI
     * @param supplier Supplier für Test-Instanzen (parameterfreier Konstruktor)
     */
    public void registerUI(String id, String displayName, String description, Supplier<? extends BaseUi> supplier) {
        if (registeredUIs.containsKey(id)) {
            logger.warning("UI with ID '" + id + "' already registered, overwriting...");
        }

        registeredUIs.put(id, new UIRegistration(id, displayName, description, supplier));
        logger.info("✓ Registered Test-UI: " + id + " (" + displayName + ")");
    }

    /**
     * Entfernt ein registriertes UI.
     *
     * @param id ID des UI
     */
    public void unregisterUI(String id) {
        if (registeredUIs.remove(id) != null) {
            logger.info("Unregistered Test-UI: " + id);
        }
    }

    /**
     * Erstellt eine Test-Instanz eines registrierten UI.
     *
     * @param id ID des UI
     * @return Test-UI-Instanz oder null wenn nicht gefunden
     */
    public BaseUi createTestUI(String id) {
        UIRegistration registration = registeredUIs.get(id);
        if (registration == null) {
            return null;
        }

        try {
            return registration.supplier().get();
        } catch (Exception e) {
            logger.severe("Failed to create test UI '" + id + "': " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gibt alle registrierten UI-IDs zurück.
     *
     * @return Set von UI-IDs
     */
    public Set<String> getRegisteredUIIds() {
        return new LinkedHashSet<>(registeredUIs.keySet());
    }

    /**
     * Gibt alle UI-Registrierungen zurück.
     *
     * @return Liste von UI-Registrierungen
     */
    public List<UIRegistration> getAllRegistrations() {
        return new ArrayList<>(registeredUIs.values());
    }

    /**
     * Prüft ob ein UI registriert ist.
     *
     * @param id ID des UI
     * @return true wenn registriert, false sonst
     */
    public boolean isRegistered(String id) {
        return registeredUIs.containsKey(id);
    }

    /**
     * Gibt die Anzahl registrierter UIs zurück.
     *
     * @return Anzahl UIs
     */
    public int getRegisteredCount() {
        return registeredUIs.size();
    }

    /**
     * UI-Registrierungs-Daten.
     */
    public record UIRegistration(
            String id,
            String displayName,
            String description,
            Supplier<? extends BaseUi> supplier
    ) {
    }
}
