package de.fallenstar.core.database;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Abstrahierte Datenspeicher-Schnittstelle für Module.
 * 
 * Ermöglicht verschiedene Backend-Implementierungen:
 * - SQLite (default für kleine/mittlere Server)
 * - MySQL/MariaDB (für große Server/Netzwerke)
 * - Redis (für Cache und Cross-Server-Sync)
 * - YAML (für einfache Daten)
 * 
 * Module sollten NUR dieses Interface nutzen, nie direkt SQL/YAML.
 * 
 * Pattern: Repository Pattern + Data Access Object (DAO)
 * 
 * @author FallenStar
 * @version 1.0
 */
public interface DataStore {
    
    /**
     * Speichert ein Objekt unter einem Key.
     * 
     * Asynchron - blockiert nicht den Main-Thread.
     * 
     * @param namespace Namespace (z.B. "storage", "merchants")
     * @param key Eindeutiger Key
     * @param data Das zu speichernde Objekt (muss serialisierbar sein)
     * @return CompletableFuture<Boolean> - true wenn erfolgreich
     */
    CompletableFuture<Boolean> save(String namespace, String key, Object data);
    
    /**
     * Lädt ein Objekt von einem Key.
     * 
     * Asynchron - blockiert nicht den Main-Thread.
     * 
     * @param namespace Namespace
     * @param key Eindeutiger Key
     * @param type Klasse des Objekts
     * @param <T> Typ des Objekts
     * @return CompletableFuture mit Optional<T>
     */
    <T> CompletableFuture<Optional<T>> load(String namespace, String key, Class<T> type);
    
    /**
     * Löscht ein Objekt.
     * 
     * @param namespace Namespace
     * @param key Eindeutiger Key
     * @return CompletableFuture<Boolean> - true wenn gelöscht
     */
    CompletableFuture<Boolean> delete(String namespace, String key);
    
    /**
     * Prüft ob ein Key existiert.
     * 
     * @param namespace Namespace
     * @param key Eindeutiger Key
     * @return CompletableFuture<Boolean> - true wenn existiert
     */
    CompletableFuture<Boolean> exists(String namespace, String key);
    
    /**
     * Speichert synchron (blockierend).
     * 
     * Nur für Server-Shutdown oder kritische Operationen!
     * 
     * @param namespace Namespace
     * @param key Key
     * @param data Daten
     * @return true wenn erfolgreich
     */
    boolean saveSync(String namespace, String key, Object data);
    
    /**
     * Lädt synchron (blockierend).
     * 
     * Nur für Server-Startup oder kritische Operationen!
     * 
     * @param namespace Namespace
     * @param key Key
     * @param type Klasse
     * @param <T> Typ
     * @return Optional<T>
     */
    <T> Optional<T> loadSync(String namespace, String key, Class<T> type);
    
    /**
     * Schließt alle Verbindungen und speichert ausstehende Daten.
     * 
     * Wird beim Server-Shutdown aufgerufen.
     */
    void shutdown();
}
