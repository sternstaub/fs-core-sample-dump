package de.fallenstar.core.database.impl;

import de.fallenstar.core.database.DataStore;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-Tests für SQLiteDataStore.
 *
 * Testet:
 * - Asynchrone save/load Operationen
 * - Synchrone save/load Operationen
 * - Namespace-Isolation
 * - Delete und Exists
 * - Graceful Shutdown
 *
 * Verwendet JUnit @TempDir für temporäre Datenbank-Dateien.
 *
 * @author FallenStar
 * @version 1.0
 */
@DisplayName("SQLiteDataStore Tests")
class SQLiteDataStoreTest {

    @TempDir
    File tempDir;

    private DataStore dataStore;

    @BeforeEach
    void setUp() {
        dataStore = new SQLiteDataStore(tempDir);
    }

    @AfterEach
    void tearDown() {
        if (dataStore != null) {
            dataStore.shutdown();
        }
    }

    @Test
    @DisplayName("save() und load() sollten asynchron funktionieren")
    void testAsyncSaveAndLoad() throws Exception {
        // Arrange
        TestData testData = new TestData("TestName", 42);
        String namespace = "test";
        String key = "data1";

        // Act - Asynchrones Speichern
        CompletableFuture<Boolean> saveFuture = dataStore.save(namespace, key, testData);
        Boolean saveResult = saveFuture.get(5, TimeUnit.SECONDS);

        // Assert - Speichern erfolgreich
        assertTrue(saveResult, "Speichern sollte erfolgreich sein");

        // Act - Asynchrones Laden
        CompletableFuture<Optional<TestData>> loadFuture =
            dataStore.load(namespace, key, TestData.class);
        Optional<TestData> loadResult = loadFuture.get(5, TimeUnit.SECONDS);

        // Assert - Laden erfolgreich und Daten korrekt
        assertTrue(loadResult.isPresent(), "Daten sollten geladen werden");
        TestData loaded = loadResult.get();
        assertEquals("TestName", loaded.name, "Name sollte übereinstimmen");
        assertEquals(42, loaded.value, "Value sollte übereinstimmen");
    }

    @Test
    @DisplayName("saveSync() und loadSync() sollten synchron funktionieren")
    void testSyncSaveAndLoad() {
        // Arrange
        TestData testData = new TestData("SyncTest", 123);
        String namespace = "test";
        String key = "sync_data";

        // Act - Synchrones Speichern
        boolean saveResult = dataStore.saveSync(namespace, key, testData);

        // Assert - Speichern erfolgreich
        assertTrue(saveResult, "Synchrones Speichern sollte erfolgreich sein");

        // Act - Synchrones Laden
        Optional<TestData> loadResult = dataStore.loadSync(namespace, key, TestData.class);

        // Assert - Laden erfolgreich und Daten korrekt
        assertTrue(loadResult.isPresent(), "Daten sollten geladen werden");
        TestData loaded = loadResult.get();
        assertEquals("SyncTest", loaded.name);
        assertEquals(123, loaded.value);
    }

    @Test
    @DisplayName("Namespaces sollten isoliert sein")
    void testNamespaceIsolation() {
        // Arrange
        TestData data1 = new TestData("Namespace1", 1);
        TestData data2 = new TestData("Namespace2", 2);
        String key = "same_key";

        // Act - Speichern in verschiedenen Namespaces
        dataStore.saveSync("namespace1", key, data1);
        dataStore.saveSync("namespace2", key, data2);

        // Assert - Laden aus verschiedenen Namespaces
        Optional<TestData> loaded1 = dataStore.loadSync("namespace1", key, TestData.class);
        Optional<TestData> loaded2 = dataStore.loadSync("namespace2", key, TestData.class);

        assertTrue(loaded1.isPresent());
        assertTrue(loaded2.isPresent());

        assertEquals("Namespace1", loaded1.get().name, "Namespace 1 sollte isoliert sein");
        assertEquals("Namespace2", loaded2.get().name, "Namespace 2 sollte isoliert sein");
    }

    @Test
    @DisplayName("delete() sollte Daten entfernen")
    void testDelete() throws Exception {
        // Arrange
        TestData testData = new TestData("ToDelete", 999);
        String namespace = "test";
        String key = "deletable";

        // Zuerst speichern
        dataStore.saveSync(namespace, key, testData);

        // Verify existiert
        CompletableFuture<Boolean> existsFuture = dataStore.exists(namespace, key);
        assertTrue(existsFuture.get(5, TimeUnit.SECONDS), "Daten sollten existieren");

        // Act - Löschen
        CompletableFuture<Boolean> deleteFuture = dataStore.delete(namespace, key);
        Boolean deleteResult = deleteFuture.get(5, TimeUnit.SECONDS);

        // Assert
        assertTrue(deleteResult, "Löschen sollte erfolgreich sein");

        // Verify nicht mehr vorhanden
        CompletableFuture<Boolean> existsAfterFuture = dataStore.exists(namespace, key);
        assertFalse(existsAfterFuture.get(5, TimeUnit.SECONDS),
            "Daten sollten nach Löschen nicht mehr existieren");
    }

    @Test
    @DisplayName("exists() sollte korrekt prüfen")
    void testExists() throws Exception {
        // Arrange
        String namespace = "test";
        String existingKey = "existing";
        String nonExistingKey = "non_existing";

        dataStore.saveSync(namespace, existingKey, new TestData("Exists", 1));

        // Act & Assert
        CompletableFuture<Boolean> existsFuture = dataStore.exists(namespace, existingKey);
        assertTrue(existsFuture.get(5, TimeUnit.SECONDS),
            "Existierender Key sollte gefunden werden");

        CompletableFuture<Boolean> notExistsFuture = dataStore.exists(namespace, nonExistingKey);
        assertFalse(notExistsFuture.get(5, TimeUnit.SECONDS),
            "Nicht existierender Key sollte nicht gefunden werden");
    }

    @Test
    @DisplayName("load() sollte Optional.empty() bei nicht vorhandenem Key zurückgeben")
    void testLoadNonExistingKey() throws Exception {
        // Act
        CompletableFuture<Optional<TestData>> loadFuture =
            dataStore.load("test", "non_existing", TestData.class);
        Optional<TestData> result = loadFuture.get(5, TimeUnit.SECONDS);

        // Assert
        assertFalse(result.isPresent(),
            "Nicht vorhandener Key sollte Optional.empty() zurückgeben");
    }

    @Test
    @DisplayName("Überschreiben vorhandener Daten sollte funktionieren")
    void testOverwriteExistingData() {
        // Arrange
        String namespace = "test";
        String key = "overwrite";

        TestData original = new TestData("Original", 100);
        TestData updated = new TestData("Updated", 200);

        // Act - Speichern und überschreiben
        dataStore.saveSync(namespace, key, original);
        dataStore.saveSync(namespace, key, updated);

        Optional<TestData> result = dataStore.loadSync(namespace, key, TestData.class);

        // Assert - Sollte überschriebene Daten haben
        assertTrue(result.isPresent());
        assertEquals("Updated", result.get().name, "Daten sollten überschrieben sein");
        assertEquals(200, result.get().value);
    }

    @Test
    @DisplayName("shutdown() sollte ohne Fehler durchlaufen")
    void testShutdown() {
        // Act & Assert - Sollte keine Exception werfen
        assertDoesNotThrow(() -> dataStore.shutdown(),
            "Shutdown sollte ohne Fehler durchlaufen");
    }

    /**
     * Test-Datenklasse für Serialisierung.
     */
    private static class TestData {
        public String name;
        public int value;

        public TestData(String name, int value) {
            this.name = name;
            this.value = value;
        }

        // Gson benötigt einen no-arg Constructor
        @SuppressWarnings("unused")
        public TestData() {}
    }
}
