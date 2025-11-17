# CLAUDE.md - AI Assistant Guide

**FallenStar Paper Core - Modular Plugin System**

This document provides comprehensive guidance for AI assistants working on this codebase.

---

## Table of Contents

1. [Language Conventions / Sprachkonventionen](#language-conventions--sprachkonventionen)
2. [Project Overview](#project-overview)
3. [Codebase Structure](#codebase-structure)
4. [Architecture & Design Patterns](#architecture--design-patterns)
5. [Development Workflow](#development-workflow)
6. [Code Conventions](#code-conventions)
7. [Common Tasks](#common-tasks)
8. [Testing Guidelines](#testing-guidelines)
9. [Important Files Reference](#important-files-reference)

---

## Language Conventions / Sprachkonventionen

### 🇩🇪 **WICHTIG: Dieses Projekt verwendet Deutsch als primäre Sprache**

**Für AI-Assistenten:**
- **Kommunikation mit dem Benutzer:** Immer auf Deutsch
- **Code-Kommentare:** Deutsch (wie im bestehenden Code)
- **Javadoc-Dokumentation:** Deutsch
- **Commit Messages:** Deutsch
- **README und Dokumentation:** Deutsch (außer technische Begriffe)

**Englisch wird verwendet für:**
- Java-Code selbst (Klassen-, Methoden-, Variablennamen)
- Technische Fachbegriffe (Provider, Registry, Plugin, etc.)
- Log-Ausgaben können gemischt sein

### Beispiel:

```java
/**
 * Initialisiert die Provider-Registry.
 *
 * Auto-Detection aller verfügbaren Plugins und
 * Registrierung entsprechender Provider.
 */
private void initializeProviders() {
    // Registry erstellen und Provider erkennen
    providerRegistry = new ProviderRegistry(getLogger());
    providerRegistry.detectAndRegister();
}
```

### Wichtige Punkte:

1. ✅ **Javadoc auf Deutsch** - Beschreibungen, Parameter, Return-Werte
2. ✅ **Inline-Kommentare auf Deutsch** - Erklärungen im Code
3. ✅ **Commit Messages auf Deutsch** - Beschreibung der Änderungen
4. ✅ **User-Kommunikation auf Deutsch** - Antworten, Erklärungen, Fragen
5. ✅ **Log-Messages gemischt** - Technische Begriffe auf Englisch, Kontext auf Deutsch

---

## Project Overview

### What is This?

A **modular Minecraft plugin system** for Paper 1.21.1 with provider-based architecture that abstracts external plugin dependencies.

### Key Technologies

- **Platform:** Paper/Spigot API 1.21.1
- **Language:** Java 21
- **Build Tool:** Maven (multi-module)
- **Database:** SQLite (primary), MySQL (planned)
- **Optional Dependencies:** Towny, Vault, Citizens, MMOItems

### Project Goals

1. **Provider Abstraction:** Decouple from external plugins (Towny, Vault, etc.)
2. **Graceful Degradation:** Features auto-disable when dependencies missing
3. **Modular Design:** Independent modules that only depend on Core
4. **AI-Friendly Development:** Sprint-based planning with clear deliverables

### Current Status

- **Version:** 1.0-SNAPSHOT
- **Phase:** Aktive Entwicklung
- **Completion:** ~50% (Core ✅ + Plots ✅ + UI-Framework ✅ + Items ✅ + UI-Modul ✅ + Economy ✅)
- **Aktueller Sprint:** Sprint 11-12 🔨 IN ARBEIT (Trading-System, PlotRegistry, Händler-Inventar, NPC-Reisesystem)
- **Nächster Sprint:** Sprint 13-14 - NPCs (Citizens-Integration, NPC-Typen)
- **Wichtige Architektur:** Provider-Implementierungen in Modulen, Core nur Interfaces!
- **Planung:** 20 Sprints (40 Wochen) mit Items, UI, Economy, Chat, Auth, WebHooks
- **Storage-Modul:** ✅ Entfernt (redundant, in Plots integriert)
- **UI-Framework:** ✅ Basis-Klassen implementiert (BaseUI, SmallChestUI, etc.)
- **ItemProvider:** ✅ Interface erweitert, MMOItems 6.10+ Integration abgeschlossen
- **Items-Modul:** ✅ Vollständig implementiert mit Reflection-basiertem MMOItems-Zugriff
- **UI-Modul:** ✅ Abgeschlossen (ConfirmationUI, SimpleTradeUI, UIButtonManager)
- **Economy-Modul:** ✅ Abgeschlossen (CurrencyManager, Basiswährung "Sterne", VaultEconomyProvider, Withdraw-Funktionalität)
- **Testbefehle:** ✅ Neue Struktur unter `/fscore admin [gui/items/plots/economy]`
- **Architektur-Refactoring:** ✅ Reflection-Eliminierung (AdminCommandRegistry, Handler-Pattern)

---

## Codebase Structure

### Multi-Module Maven Layout

```
fs-core-sample-dump/
│
├── pom.xml                          # Parent POM (manages all modules)
│
├── core/                            # Core Plugin (Foundation)
│   ├── pom.xml
│   ├── src/main/
│   │   ├── java/de/fallenstar/core/
│   │   │   ├── FallenStarCore.java           # Main plugin class
│   │   │   ├── provider/                      # Provider interfaces
│   │   │   │   ├── PlotProvider.java
│   │   │   │   ├── EconomyProvider.java
│   │   │   │   ├── NPCProvider.java
│   │   │   │   ├── ItemProvider.java
│   │   │   │   ├── ChatProvider.java
│   │   │   │   ├── UIProvider.java            # 📋 NEU: UI-Provider-Interface
│   │   │   │   ├── AuthProvider.java          # 📋 NEU: Auth-Provider-Interface
│   │   │   │   ├── NetworkProvider.java
│   │   │   │   ├── Plot.java                  # Data model
│   │   │   │   └── impl/                      # NUR NoOp-Implementierungen!
│   │   │   │       ├── NoOpPlotProvider.java
│   │   │   │       ├── NoOpEconomyProvider.java
│   │   │   │       ├── NoOpNPCProvider.java
│   │   │   │       ├── NoOpItemProvider.java
│   │   │   │       ├── NoOpChatProvider.java
│   │   │   │       ├── NoOpUIProvider.java    # 📋 NEU
│   │   │   │       ├── NoOpAuthProvider.java  # 📋 NEU
│   │   │   │       └── NativeTextUIProvider.java  # 📋 NEU: Native Fallback
│   │   │   ├── registry/
│   │   │   │   └── ProviderRegistry.java      # Auto-detects providers
│   │   │   ├── exception/
│   │   │   │   └── ProviderFunctionalityNotFoundException.java
│   │   │   ├── event/
│   │   │   │   └── ProvidersReadyEvent.java
│   │   │   ├── ui/                            # 📋 NEU: UI-Kontext-Klassen
│   │   │   │   ├── context/
│   │   │   │   │   ├── TradeContext.java
│   │   │   │   │   ├── DialogContext.java
│   │   │   │   │   ├── StorageContext.java
│   │   │   │   │   └── TownContext.java
│   │   │   │   └── components/
│   │   │   │       ├── Menu.java
│   │   │   │       ├── Dialog.java
│   │   │   │       └── Form.java
│   │   │   └── database/
│   │   │       ├── DataStore.java             # Interface
│   │   │       └── impl/                      # (missing implementations)
│   │   └── resources/
│   │       ├── plugin.yml
│   │       └── config.yml
│
├── module-plots/                    # FallenStar Plots (Sprint 3-4) ✅
│   ├── pom.xml                      # Plot-System + Storage-Integration
│   ├── src/main/java/de/fallenstar/plots/
│   │   ├── PlotsModule.java                   # Main class
│   │   ├── provider/                          # Provider-Implementierungen
│   │   │   └── TownyPlotProvider.java         # Towny-Integration
│   │   ├── command/                           # Plot-Befehle
│   │   ├── manager/                           # Plot- und Storage-Manager
│   │   ├── model/                             # Plot-Datenmodelle
│   │   ├── listener/                          # Event-Handler
│   │   └── storage/                           # ✅ Storage-System (ex-module-storage)
│   │       ├── command/                       # Storage-Befehle
│   │       ├── manager/                       # Storage-Manager
│   │       ├── model/                         # Storage-Datenmodelle
│   │       ├── provider/                      # PlotStorageProvider
│   │       └── listener/                      # Storage-Events
│   └── src/main/resources/
│       ├── plugin.yml
│       └── config.yml
│
├── module-items/                    # FallenStar Items (Sprint 5-6) ✅
│   ├── pom.xml                      # MMOItems 6.10.1-SNAPSHOT + MythicLib 1.6.2-SNAPSHOT
│   ├── src/main/java/de/fallenstar/items/
│   │   ├── ItemsModule.java                   # Main class (ProvidersReadyEvent)
│   │   ├── provider/                          # Provider-Implementierungen
│   │   │   └── MMOItemsItemProvider.java      # ✅ Reflection-basiert (kein MMOPlugin!)
│   │   ├── command/                           # Item-Befehle
│   │   │   └── ItemsCommand.java              # /items browse, info, reload
│   │   ├── manager/                           # Item-Manager
│   │   │   └── SpecialItemManager.java        # ✅ Währungs-Items (Münzen)
│   │   └── ui/                                # Test-UIs (für UIRegistry)
│   │       ├── ItemBrowserUI.java             # ✅ Kategorie-basierter Browser
│   │       └── TestTradeUI.java               # ✅ MMOItems Trading Demo
│   └── src/main/resources/
│       ├── plugin.yml                         # Dependency: FallenStar-Core, MMOItems
│       └── config.yml
│
├── module-ui/                       # FallenStar UI (Sprint 7-8) 🔨
│   ├── pom.xml                      # Konkrete UIs: ConfirmationUI, SimpleTradeUI
│   ├── src/main/java/de/fallenstar/ui/
│   │   ├── UIModule.java                      # Main class (ProvidersReadyEvent)
│   │   ├── manager/                           # UI-Manager
│   │   │   └── UIButtonManager.java           # ✅ UI-Button Items (Confirm, Cancel, Close)
│   │   └── ui/                                # Konkrete UI-Implementierungen
│   │       ├── ConfirmationUI.java            # ✅ Ja/Nein Dialog
│   │       └── SimpleTradeUI.java             # ✅ Vanilla Trading Demo
│   └── src/main/resources/
│       ├── plugin.yml                         # Dependency: FallenStar-Core
│       └── config.yml
│
├── module-economy/                  # FallenStar Economy (Sprint 9-10)
│   ├── pom.xml                      # Weltwirtschaft, Münzgeld, Preise
│   ├── src/main/java/de/fallenstar/economy/
│   │   ├── EconomyModule.java                 # Main class
│   │   ├── provider/                          # Provider-Implementierungen
│   │   │   └── VaultEconomyProvider.java      # Vault-Integration
│   │   ├── command/                           # Wirtschafts-Befehle
│   │   ├── manager/                           # Wirtschafts-Manager
│   │   ├── model/                             # Wirtschafts-Modelle
│   │   └── pricing/                           # Preisberechnungen
│   └── src/main/resources/
│       ├── plugin.yml
│       └── config.yml
│
├── module-npcs/                     # FallenStar NPCs (Sprint 13-14)
│   ├── pom.xml                      # NPC-System (Citizens-Integration)
│   ├── src/main/java/de/fallenstar/npcs/
│   │   ├── NPCsModule.java                    # Main class
│   │   ├── provider/                          # Provider-Implementierungen
│   │   │   └── CitizensNPCProvider.java       # Citizens-Integration
│   │   ├── command/                           # NPC-Befehle
│   │   ├── manager/                           # NPC-Manager
│   │   ├── model/                             # NPC-Modelle
│   │   └── gui/                               # NPC-Interaktionen
│   └── src/main/resources/
│       ├── plugin.yml
│       └── config.yml
│
└── Documentation Files (*.md)
```

### Module Dependency Graph

```
Core (UI-Framework + alle Provider-Interfaces + NoOp-Implementierungen)
 ↑
 ├── Plots            (Plot-System + Storage ✅, Slot-System ✅, Towny → TownyPlotProvider)
 ├── Items            (MMOItems-Wrapper ✅, registriert MMOItemsItemProvider)
 ├── UI               (Konkrete UIs ✅: ConfirmationUI, SimpleTradeUI, UIButtonManager)
 ├── Economy          (Weltwirtschaft ✅, Vault ✅, nutzt ItemProvider + UI)
 ├── NPCs             (NPC-System, Botschafter-NPCs, Denizen-Ersatz, nutzt ItemProvider + PlotProvider + UI)
 ├── Chat             (Matrix-Bridge → MatrixChatProvider)
 ├── Auth             (Keycloak → KeycloakAuthProvider)
 └── WebHooks         (Wiki/Forum-Integration)
```

**Important:**
- Modules **ONLY** depend on Core, never on each other
- Modules communicate via Core's Provider-Interfaces
- Each module provides its own Provider-Implementation
- Core enthält NUR Interfaces + NoOp-Implementierungen + NativeTextUIProvider (Fallback)
- Konkrete Provider-Implementierungen liegen in den Modulen
- **Storage-Modul** ❌ entfernt, Funktionalität in **Plots-Modul** integriert
- **Special Items Architektur:** Module registrieren eigene Special Items Kategorien
  - Items-Modul: Währungs-Items (bronze/silver/gold coins) via SpecialItemManager
  - UI-Modul: UI-Button Items (Confirm, Cancel, Close, etc.) via UIButtonManager
  - Economy-Modul: Weitere Währungs-Items (zukünftig)

**Beispiel:** NPCs-Modul nutzt PlotProvider + UIProvider (Core-Interfaces), nicht Towny/Denizen direkt!

---

## Architecture & Design Patterns

### 0. Provider Architecture (WICHTIG!)

**Core Plugin = Abstraktionsebene**

Das Core-Plugin ist die zentrale Abstraktionsebene und enthält **NUR**:
- Provider-Interfaces (PlotProvider, NPCProvider, ItemProvider, etc.)
- NoOp-Implementierungen (NoOpPlotProvider, NoOpNPCProvider, etc.)
- ProviderRegistry zur Auto-Detection
- Basis-Events und Exceptions

**Keine konkreten Provider-Implementierungen im Core!**

**Module = Provider-Implementierungen**

Jedes Modul, das mit einer externen API arbeitet, enthält seine eigene Provider-Implementierung:

```
module-plots/
├── provider/
│   └── TownyPlotProvider.java    ← Implementiert PlotProvider (Core-Interface)
└── (verwendet Towny-API)

module-npcs/
├── provider/
│   └── CitizensNPCProvider.java  ← Implementiert NPCProvider (Core-Interface)
└── (verwendet Citizens-API)

module-items/
├── provider/
│   └── MMOItemsItemProvider.java ← Implementiert ItemProvider (Core-Interface)
└── (verwendet MMOItems-API)
```

**Inter-Modul-Kommunikation**

Module kommunizieren **ausschließlich** über Core-Interfaces:

```java
// ✅ RICHTIG: NPCs-Modul nutzt PlotProvider-Interface
public class NPCManager {
    private PlotProvider plotProvider;  // Core-Interface!

    public void spawnNPC(Location location) {
        // Spricht mit Core-Interface, nicht direkt mit Towny!
        Plot plot = plotProvider.getPlot(location);
        // ...
    }
}

// ❌ FALSCH: Direkter Zugriff auf Towny-API
import com.palmergames.bukkit.towny.*;  // NIEMALS!
```

**Registrierung der Provider**

Die ProviderRegistry erkennt automatisch, welche Module geladen sind:

```java
// In ProviderRegistry.java (Core)
public void detectAndRegister() {
    // Prüft ob Plots-Modul geladen ist
    if (isPluginEnabled("FallenStar-Plots")) {
        // Nutzt TownyPlotProvider vom Plots-Modul
        plotProvider = getForeignProvider(TownyPlotProvider.class);
    } else {
        plotProvider = new NoOpPlotProvider();
    }
}
```

### 1. Provider Pattern

**Problem:** Direct dependencies on external plugins create tight coupling.

```java
// ❌ BAD: Direct dependency
import com.palmergames.bukkit.towny.*;
TownBlock block = TownyAPI.getTownBlock(location);
```

**Solution:** Abstract behind provider interfaces.

```java
// ✅ GOOD: Provider abstraction
PlotProvider provider = registry.getPlotProvider();
if (provider.isAvailable()) {
    Plot plot = provider.getPlot(location);
    // Use plot...
}
```

**Benefits:**
- Decouple from external plugin APIs
- Easy to swap implementations
- Graceful degradation when plugins missing

### 2. NoOp (Null Object) Pattern

**Implementation:** When optional plugins unavailable, use NoOp providers that throw specific exceptions.

```java
public class NoOpPlotProvider implements PlotProvider {
    @Override
    public boolean isAvailable() {
        return false;  // Signal unavailability
    }

    @Override
    public Plot getPlot(Location location)
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            "PlotProvider", "getPlot",
            "No plot plugin (Towny, Factions, etc.) available"
        );
    }
}
```

**Benefits:**
- No null checking needed
- Explicit error handling
- Features can gracefully degrade

### 3. Graceful Degradation Pattern

**Usage in Modules:**

```java
private boolean plotBasedStorageEnabled = false;

private void checkOptionalFeatures() {
    PlotProvider plotProvider = providers.getPlotProvider();

    if (plotProvider.isAvailable()) {
        try {
            // Test the provider works
            plotProvider.getPlot(null);
            plotBasedStorageEnabled = true;
            getLogger().info("✓ Plot-based storage enabled");
        } catch (ProviderFunctionalityNotFoundException e) {
            plotBasedStorageEnabled = false;
            getLogger().warning("✗ Plot-based storage disabled");
        }
    }
}

// Later in code:
public void someFeature() {
    if (plotBasedStorageEnabled) {
        // Use plot-based logic
    } else {
        // Use fallback logic
    }
}
```

### 4. Event-Driven Initialization

**Core fires event when providers ready:**

```java
// In FallenStarCore.java
Bukkit.getScheduler().runTask(this, () -> {
    ProvidersReadyEvent event = new ProvidersReadyEvent(providerRegistry);
    Bukkit.getPluginManager().callEvent(event);
});
```

**Modules listen and initialize:**

```java
// In module main class
@EventHandler
public void onProvidersReady(ProvidersReadyEvent event) {
    this.providers = event.getRegistry();
    checkRequiredFeatures();
    checkOptionalFeatures();
    initializeModule();
}
```

### 5. Service Registry Pattern

**ProviderRegistry** acts as central service locator:

```java
public class ProviderRegistry {
    private PlotProvider plotProvider;
    private EconomyProvider economyProvider;
    // ...

    public void detectAndRegister() {
        if (isPluginEnabled("Towny")) {
            plotProvider = new TownyPlotProvider();
        } else {
            plotProvider = new NoOpPlotProvider();
        }
        // ...
    }
}
```

### 6. Data Persistence Pattern (WICHTIG!)

**Regel:** Alle Module müssen ihre Daten persistent speichern!

**Problem:** In-Memory-Datenstrukturen gehen bei Server-Neustarts verloren.

**Lösung:** Bidirektionale Config-Integration mit `loadFromConfig()` und `saveToConfig()`.

#### Standard-Pattern für Persistierung:

```java
public class DataManager {
    private final Logger logger;
    private final Map<String, SomeData> dataMap;  // In-Memory Cache

    /**
     * Lädt Daten aus der Config.
     *
     * Wird beim Modul-Start aufgerufen (onEnable oder onProvidersReady).
     */
    public void loadFromConfig(FileConfiguration config) {
        ConfigurationSection section = config.getConfigurationSection("data-section");
        if (section == null) {
            logger.warning("Keine Daten in config.yml gefunden");
            initializeDefaults();
            return;
        }

        // Parse Config und fülle dataMap
        for (String key : section.getKeys(false)) {
            SomeData data = parseData(section, key);
            dataMap.put(key, data);
        }

        logger.info("Daten geladen: " + dataMap.size() + " Einträge");
    }

    /**
     * Speichert Daten zurück in die Config.
     *
     * WICHTIG: Muss nach JEDER Daten-Änderung aufgerufen werden!
     */
    public void saveToConfig(FileConfiguration config) {
        // Lösche alte Daten
        config.set("data-section", null);

        // Schreibe alle Daten
        for (Map.Entry<String, SomeData> entry : dataMap.entrySet()) {
            String key = entry.getKey();
            SomeData data = entry.getValue();

            config.set("data-section." + key + ".field1", data.getField1());
            config.set("data-section." + key + ".field2", data.getField2());
        }

        logger.info("Daten gespeichert: " + dataMap.size() + " Einträge");
    }
}
```

#### Modul-Integration:

```java
public class MyModule extends JavaPlugin {
    private DataManager dataManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();  // Erstelle config.yml falls nicht vorhanden

        dataManager = new DataManager(getLogger());
        dataManager.loadFromConfig(getConfig());  // Lade Daten
    }

    /**
     * Speichert die Config auf Festplatte.
     *
     * MUSS nach JEDER Daten-Änderung aufgerufen werden!
     */
    public void saveConfiguration() {
        dataManager.saveToConfig(getConfig());  // In-Memory → Config
        saveConfig();  // Config → Festplatte (Bukkit API)
        getLogger().fine("Config gespeichert");
    }

    // Getter für andere Module (via Reflection)
    public DataManager getDataManager() {
        return dataManager;
    }
}
```

#### Usage in Commands/Listeners:

```java
public class SomeCommand {
    private final MyModule plugin;
    private final DataManager dataManager;

    public void execute(Player player, String[] args) {
        // Ändere Daten
        dataManager.setSomeData("key", newData);

        // WICHTIG: Sofort speichern!
        plugin.saveConfiguration();

        player.sendMessage("§aDaten gespeichert!");
    }
}
```

#### Best Practices:

1. ✅ **Immer bidirektional:** `loadFromConfig()` UND `saveToConfig()`
2. ✅ **Sofort speichern:** Nach JEDER Daten-Änderung `saveConfiguration()` aufrufen
3. ✅ **Defaults definieren:** `initializeDefaults()` wenn Config leer
4. ✅ **Fehlerbehandlung:** Try-catch bei Config-Parsing
5. ✅ **Logging:** Info bei Load/Save, Warning bei Fehlern
6. ✅ **Validierung:** Prüfe Daten-Integrität beim Laden

#### Häufige Fehler:

❌ **Nur laden, nicht speichern:**
```java
// FALSCH: Daten gehen bei Neustart verloren!
public void setData(String key, Data data) {
    dataMap.put(key, data);  // Nur In-Memory
    // Fehlt: saveConfiguration()
}
```

❌ **Speichern ohne saveConfig():**
```java
// FALSCH: Config bleibt nur im RAM!
public void saveConfiguration() {
    dataManager.saveToConfig(getConfig());
    // Fehlt: saveConfig() für Festplatte!
}
```

❌ **Speichern nur bei onDisable():**
```java
// FALSCH: Bei Server-Crash gehen Daten verloren!
@Override
public void onDisable() {
    saveConfiguration();  // Zu spät!
}
```

✅ **Richtig:**
```java
// Speichere SOFORT nach jeder Änderung
public void setData(String key, Data data) {
    dataMap.put(key, data);
    plugin.saveConfiguration();  // Sofort auf Festplatte!
}
```

#### Beispiel-Implementierungen im Projekt:

- **ItemBasePriceProvider** (Economy-Modul):
  - `loadFromConfig()` - Zeile 82-129
  - `saveToConfig()` - Zeile 347-372
  - `EconomyModule.saveConfiguration()` - Zeile 243-251

- **PlotStorageData** (Plots-Modul):
  - Speichert Plot-Storage-Materialien persistent
  - Integration mit Towny Plot-Data

#### Config-Struktur-Empfehlung:

```yaml
# config.yml - Strukturiertes Format
data-manager:
  defaults:
    some-value: 1.0

  entries:
    entry-1:
      field1: "value1"
      field2: 100
      field3: true

    entry-2:
      field1: "value2"
      field2: 200
      field3: false
```

---

## Development Workflow

### Sprint-Based Development

Das Projekt folgt einem 20-Sprint-Fahrplan (40 Wochen):

| Sprint | Module | Duration | Status | Beschreibung |
|--------|--------|----------|--------|--------------|
| **1-2** | **Core + UI Framework** | 2 Wochen | ✅ | Core + UI-Basis-Klassen + Admin-Commands |
| **3-4** | **Plots (inkl. Storage)** | 2 Wochen | ✅ | Plot-System + Storage-Integration (fertig) |
| **5-6** | **Items (MMOItems-Wrapper)** | 2 Wochen | ✅ | MMOItems 6.10+ Reflection-Integration + Test-UIs |
| **7-8** | **UI-Modul** | 2 Wochen | ✅ | ConfirmationUI ✅, SimpleTradeUI ✅, UIButtonManager ✅ |
| **9-10** | **Economy** | 2 Wochen | ✅ | CurrencyManager ✅, Basiswährung "Sterne" ✅, Vault-Integration ✅, Withdraw-Funktionalität ✅ |
| **11-12** | **Plot-Slots & Botschafter** | 2 Wochen | 📋 | NPC-Slots auf Grundstücken, Botschafter-NPCs |
| **13-14** | **NPCs** | 2 Wochen | 📋 | NPC-System mit UI, Denizen-Ersatz |
| **15-16** | **Chat** | 2 Wochen | 📋 | Matrix-Bridge, globaler Chat |
| **17-18** | **Auth** | 2 Wochen | 📋 | Keycloak-Integration, SSO |
| **19-20** | **WebHooks** | 2 Wochen | 📋 | Wiki/Forum-Integration |

**Legende:**
- ✅ Abgeschlossen
- 🔨 In Arbeit
- 📋 Geplant

**Sprint 7-8 Fortschritt:**
- ✅ UIButtonManager (UI-Button Items: Confirm, Cancel, Close, etc.)
- ✅ ConfirmationUI (Ja/Nein Dialog mit grüner/roter Wolle)
- ✅ SimpleTradeUI (Vanilla Trading Demo)
- ✅ Testbefehle: `/fscore admin gui confirm`, `/fscore admin gui trade`
- 📋 Weitere UIs (AmbassadorUI, DialogUI, etc.) folgen...

**Wichtige Architektur-Änderungen:**
- **Core** enthält nur Interfaces + NoOp-Implementierungen + UI-Framework-Basis-Klassen
- **Provider-Implementierungen** liegen in den jeweiligen Modulen
- **Module** kommunizieren NUR über Core-Interfaces
- **Storage-Modul** ❌ entfernt (redundant, in Plots integriert)
- **UI-Framework** ✅ neu (BaseUI, SmallChestUI, LargeChestUI, SignUI, AnvilUI, BookUI)
- **Admin-Command-System** ✅ neu (/fscore admin [gui/items/plots] für Modul-Tests)
- **Denizen-Ersatz** 📋 natives NPC-Dialog-System im NPCs-Modul
- **Sprint-Umplanung:** Items VOR UI-Modul (5-6), UI-Modul nach Items (7-8)
- **Begründung:** Trading-UIs benötigen Custom-Item-Support (MMOItems)

### Testbefehl-Struktur

**WICHTIG:** Alle Testbefehle sind jetzt unter `/fscore admin <kategorie>` organisiert!

#### `/fscore admin gui` - UI-Testbefehle

```
/fscore admin gui list              - Zeigt alle registrierten Test-UIs
/fscore admin gui <ui-id>           - Öffnet ein spezifisches UI
/fscore admin gui confirm           - Öffnet ConfirmationUI (Ja/Nein Dialog)
/fscore admin gui trade             - Öffnet SimpleTradeUI (Vanilla Trading Demo)
```

**UI-Registrierung:**
- Module registrieren ihre UIs in der UIRegistry (Core)
- Jedes UI bekommt eine eindeutige ID (z.B. "confirm", "trade", "itembrowser")
- UIs können via Factory-Methoden oder Konstruktoren erstellt werden
- Test-UIs sind über `/fscore admin gui <ui-id>` zugänglich

**Beispiel:**
```java
// In UIModule.java
uiRegistry.registerTestUI(
    "confirm",
    "Bestätigungs-Dialog (Ja/Nein)",
    "Generisches Ja/Nein Confirmation UI",
    () -> ConfirmationUI.createSimple(buttonManager, "Test-Nachricht", onConfirm)
);
```

#### `/fscore admin items` - Item-Testbefehle

```
/fscore admin items list [type]     - Zeigt alle MMOItems (optional nach Type gefiltert)
/fscore admin items give <type> <id> - Gibt dem Spieler ein MMOItem
/fscore admin items browse          - Öffnet ItemBrowserUI
/fscore admin items info <type> <id> - Zeigt detaillierte Item-Infos
/fscore admin items reload          - Lädt MMOItems-Cache neu
```

**Hinweis:** Diese Befehle werden vom Items-Modul bereitgestellt (noch nicht implementiert).

#### `/fscore admin plots` - Plot-Testbefehle

```
/fscore admin plots info            - Zeigt Plot-Info am aktuellen Standort
/fscore admin plots storage view    - Zeigt Plot-Storage Materialien
/fscore admin plots storage scan    - Scannt Plot-Storage neu
```

**Hinweis:** Diese Befehle werden vom Plots-Modul bereitgestellt (noch nicht implementiert).

**Migration:**
- ❌ `/fscore plotstorage view` → ✅ `/fscore admin plots storage view`
- Alte Befehle wurden entfernt, neue Struktur ist konsistent

### Working on a Sprint

1. **Read Sprint Documentation:**
   - Check `REPOSITORY_INDEX.md` for required files
   - Review module's `README.md`
   - Check `SETUP_COMPLETE.md` for what's missing

2. **Understand Context:**
   - Review relevant provider interfaces
   - Study existing implementations (templates)
   - Check similar modules for patterns

3. **Implementation Pattern:**
   ```bash
   # For each class to implement:
   1. Find template (e.g., NoOpPlotProvider for other NoOp providers)
   2. Copy structure and pattern
   3. Implement functionality
   4. Add comprehensive Javadoc
   5. Test compilation
   ```

4. **Testing:**
   ```bash
   # Build module
   cd core/  # or relevant module
   mvn clean package

   # Check for compilation errors
   # Copy JAR to test server
   # Test functionality
   ```

### Git Workflow

**Current Branch:** `claude/claude-md-mi0sco9raq2ajdr6-01Tte3UhY6FdvsCXyqqVvJ8k`

**Important Rules:**
- Always develop on the designated Claude branch
- Commit with clear, descriptive messages
- Push to origin with: `git push -u origin <branch-name>`
- Branch names must start with `claude/` and match session ID
- Use retry logic for network failures (exponential backoff: 2s, 4s, 8s, 16s)

**Commit Message Format:**
```bash
git commit -m "$(cat <<'EOF'
[Sprint X] Brief description of change

- Detail 1
- Detail 2
- Detail 3
EOF
)"
```

---

## Code Conventions

### ⚠️ Reflection vermeiden!

**WICHTIG:** Reflection sollte **nur als letztes Mittel** verwendet werden. Bevorzuge stattdessen:

#### Warum Reflection problematisch ist:
- ❌ **Keine Compile-Time-Sicherheit:** Fehler werden erst zur Laufzeit erkannt
- ❌ **Keine IDE-Unterstützung:** Kein Autocomplete, kein Refactoring
- ❌ **Performance-Overhead:** Reflection ist langsamer als direkte Aufrufe
- ❌ **Wartbarkeit:** Schwer zu verstehen und zu debuggen
- ❌ **Versionsprobleme:** API-Änderungen führen zu Runtime-Errors

#### Bessere Alternativen (in Prioritätsreihenfolge):

1. **Provider-Pattern** (bevorzugt):
   ```java
   // ✅ RICHTIG: Provider-Interface im Core
   public interface ItemProvider {
       Optional<ItemStack> getSpecialItem(String id, int amount);
   }

   // Module registrieren Provider in ProviderRegistry
   ProviderRegistry registry = core.getProviderRegistry();
   ItemProvider itemProvider = registry.getItemProvider();
   ItemStack coin = itemProvider.getSpecialItem("bronze_stern", 1);
   ```

2. **Direct Dependency** (wenn Module-Abhängigkeit akzeptabel):
   ```java
   // ✅ RICHTIG: Module als Dependency in pom.xml
   <dependency>
       <groupId>de.fallenstar</groupId>
       <artifactId>module-items</artifactId>
       <scope>provided</scope>
   </dependency>

   // Direkter Import und Nutzung
   import de.fallenstar.items.manager.SpecialItemManager;
   SpecialItemManager manager = ItemsModule.getSpecialItemManager();
   ```

3. **Service Registry Pattern**:
   ```java
   // ✅ RICHTIG: Zentrale Service-Registry
   public class ServiceRegistry {
       private static final Map<Class<?>, Object> services = new HashMap<>();

       public static <T> void register(Class<T> serviceClass, T implementation) {
           services.put(serviceClass, implementation);
       }

       public static <T> Optional<T> get(Class<T> serviceClass) {
           return Optional.ofNullable((T) services.get(serviceClass));
       }
   }
   ```

4. **Reflection** (nur wenn unvermeidbar):
   ```java
   // ❌ NUR ALS LETZTES MITTEL!
   // Beispiel: Zugriff auf Economy-Modul ohne Hard-Dependency
   try {
       var plugin = Bukkit.getPluginManager().getPlugin("FallenStar-Economy");
       var method = plugin.getClass().getMethod("getPriceProvider");
       var provider = method.invoke(plugin);
       // ...
   } catch (Exception e) {
       // Graceful Degradation
   }
   ```

#### Aktuelle Reflection-Nutzung (TODO: Refactoring):

**Plots-Modul:**
- `PriceSetListener.getCoinItem()` → Via Reflection auf Items-Modul
  - **TODO:** ItemProvider-Methode `getSpecialItem()` hinzufügen
- `PlotPriceCommand.loadPriceFromProvider()` → Via Reflection auf Economy-Modul
  - **TODO:** EconomyProvider-Methode `getItemPrice()` hinzufügen
- `PlotPriceCommand.savePriceToProvider()` → Via Reflection auf Economy-Modul
  - **TODO:** EconomyProvider-Methode `setItemPrice()` hinzufügen

**Ziel:** Alle Reflection-Calls durch Provider-Pattern ersetzen.

---

### Java Style

**Package Structure:**
```
de.fallenstar.<module>/
├── <Module>Main.java              # Main plugin class
├── command/                       # Command handlers
├── manager/                       # Business logic
├── model/                         # Data classes/POJOs
└── listener/ or gui/ or task/    # Feature-specific
```

**Naming Conventions:**
- Classes: `PascalCase`
- Methods: `camelCase`
- Constants: `UPPER_SNAKE_CASE`
- Packages: `lowercase`

**Example Class Structure:**

```java
package de.fallenstar.core.provider;

import org.bukkit.Location;
import de.fallenstar.core.exception.ProviderFunctionalityNotFoundException;

/**
 * Provider-Interface für [Feature].
 *
 * Implementierungen:
 * - [Concrete]Provider ([Plugin]-Integration)
 * - NoOp[Type]Provider (Fallback)
 *
 * @author FallenStar
 * @version 1.0
 */
public interface SomeProvider {

    /**
     * Prüft ob dieser Provider verfügbar/funktionsfähig ist.
     *
     * @return true wenn Provider funktioniert, false sonst
     */
    boolean isAvailable();

    /**
     * [Method description].
     *
     * @param param [Parameter description]
     * @return [Return value description]
     * @throws ProviderFunctionalityNotFoundException wenn Feature nicht verfügbar
     */
    SomeType someMethod(SomeParam param)
            throws ProviderFunctionalityNotFoundException;
}
```

### Documentation Requirements

**Every class needs:**
1. **Class-level Javadoc** with:
   - Purpose description (German or English)
   - List of implementations
   - Author and version

2. **Method-level Javadoc** with:
   - Brief description
   - `@param` for all parameters
   - `@return` for return values
   - `@throws` for exceptions

3. **Inline comments** for complex logic

**Example:**

```java
/**
 * Initialisiert die Provider-Registry.
 *
 * Auto-Detection aller verfügbaren Plugins und
 * Registrierung entsprechender Provider.
 */
private void initializeProviders() {
    providerRegistry = new ProviderRegistry(getLogger());
    providerRegistry.detectAndRegister();
}
```

### Error Handling

**Provider Methods:**
- Always declare `throws ProviderFunctionalityNotFoundException`
- NoOp providers throw on all operations
- Real providers only throw when feature truly unavailable

**Module Code:**
```java
try {
    // Provider operation
    Plot plot = plotProvider.getPlot(location);
    // Use plot...
} catch (ProviderFunctionalityNotFoundException e) {
    // Graceful fallback
    getLogger().warning("Plot feature unavailable: " + e.getMessage());
    // Alternative logic
}
```

### Configuration Files

**plugin.yml Structure:**

```yaml
name: FallenStar-Core
version: 1.0
main: de.fallenstar.core.FallenStarCore
api-version: 1.21
authors: [FallenStar]
description: Core plugin for modular system

# For modules:
depend: [FallenStar-Core]  # Required dependency

# Optional dependencies:
softdepend: [Towny, Vault, Citizens]
```

**config.yml Structure:**

```yaml
# Database configuration
database:
  type: sqlite  # sqlite, mysql

# Provider preferences
providers:
  plot:
    enabled: true
    preferred: towny  # towny, factions
  economy:
    enabled: true
  npc:
    enabled: true
    preferred: citizens  # citizens, znpcs
```

---

## Common Tasks

### Adding a New Provider

**Steps:**

1. **Create Interface** in `core/src/main/java/de/fallenstar/core/provider/`

```java
public interface NewProvider {
    boolean isAvailable();
    SomeType someMethod(Params...) throws ProviderFunctionalityNotFoundException;
}
```

2. **Create NoOp Implementation** in `core/src/main/java/de/fallenstar/core/provider/impl/`

```java
public class NoOpNewProvider implements NewProvider {
    @Override
    public boolean isAvailable() { return false; }

    @Override
    public SomeType someMethod(Params...)
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            "NewProvider", "someMethod", "Reason..."
        );
    }
}
```

3. **Register in ProviderRegistry** (`ProviderRegistry.java`)

```java
private NewProvider newProvider;

public void detectAndRegister() {
    // ...existing code...

    // New Provider Detection
    if (isPluginEnabled("SomePlugin")) {
        newProvider = new SomePluginNewProvider();
        logger.info("✓ Registered SomePluginNewProvider");
    } else {
        newProvider = new NoOpNewProvider();
        logger.warning("✗ New provider disabled");
    }
}

public NewProvider getNewProvider() { return newProvider; }
```

4. **(Optional) Create Concrete Implementation**

```java
public class SomePluginNewProvider implements NewProvider {
    // Real implementation using external plugin API
}
```

### Creating a New Module

**Template Structure:**

```
module-newfeature/
├── pom.xml
├── README.md
└── src/main/
    ├── java/de/fallenstar/newfeature/
    │   ├── NewFeatureModule.java      # Main class
    │   ├── command/                    # Commands
    │   ├── manager/                    # Business logic
    │   ├── model/                      # Data models
    │   └── listener/                   # Event listeners
    └── resources/
        ├── plugin.yml
        └── config.yml
```

**Main Class Template:**

```java
public class NewFeatureModule extends JavaPlugin implements Listener {
    private ProviderRegistry providers;
    private boolean someFeatureEnabled = false;

    @Override
    public void onEnable() {
        getLogger().info("NewFeature Module starting...");
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onProvidersReady(ProvidersReadyEvent event) {
        this.providers = event.getRegistry();

        if (!checkRequiredFeatures()) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        checkOptionalFeatures();
        initializeModule();
    }

    private boolean checkRequiredFeatures() {
        // Check critical providers
        return true;
    }

    private void checkOptionalFeatures() {
        // Test optional providers
    }

    private void initializeModule() {
        // Register commands, listeners, etc.
    }
}
```

**pom.xml for Module:**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>de.fallenstar</groupId>
        <artifactId>fallenstar-paper-parent</artifactId>
        <version>1.0-SNAPSHOT</version>
    </parent>

    <artifactId>module-newfeature</artifactId>
    <name>NewFeature Module</name>

    <dependencies>
        <!-- Core Dependency (REQUIRED) -->
        <dependency>
            <groupId>de.fallenstar</groupId>
            <artifactId>core</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>

        <!-- Paper API -->
        <dependency>
            <groupId>io.papermc.paper</groupId>
            <artifactId>paper-api</artifactId>
        </dependency>
    </dependencies>
</project>
```

### Building the Project

**Build All Modules:**
```bash
mvn clean package
```

**Build Single Module:**
```bash
cd core/
mvn clean package
```

**Build Outputs:**
- `core/target/FallenStar-Core-1.0.jar`
- `module-plots/target/FallenStar-Plots-1.0.jar`
- `module-items/target/FallenStar-Items-1.0.jar`
- `module-ui/target/FallenStar-UI-1.0.jar`
- `module-economy/target/FallenStar-Economy-1.0.jar`
- `module-npcs/target/FallenStar-NPCs-1.0.jar`

### Implementing Missing Classes

**Check Status:**
```bash
# See SETUP_COMPLETE.md for list of missing implementations
cat SETUP_COMPLETE.md
```

**Find Template:**
1. Locate similar existing class
2. Copy structure
3. Adapt to new purpose

**Example - Implementing NoOpEconomyProvider:**

1. **Template:** `NoOpPlotProvider.java`
2. **New File:** `core/src/main/java/de/fallenstar/core/provider/impl/NoOpEconomyProvider.java`

```java
package de.fallenstar.core.provider.impl;

import de.fallenstar.core.exception.ProviderFunctionalityNotFoundException;
import de.fallenstar.core.provider.EconomyProvider;

/**
 * NoOp Implementation des EconomyProviders.
 *
 * Wird verwendet wenn kein Economy-Plugin (Vault) verfügbar ist.
 * Alle Methoden werfen ProviderFunctionalityNotFoundException.
 *
 * @author FallenStar
 * @version 1.0
 */
public class NoOpEconomyProvider implements EconomyProvider {

    private static final String PROVIDER_NAME = "EconomyProvider";
    private static final String REASON = "No economy plugin (Vault) available";

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public double getBalance(UUID player)
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            PROVIDER_NAME, "getBalance", REASON
        );
    }

    // ... implement other methods similarly
}
```

---

## Testing Guidelines

### Unit Testing Strategy

**Test Provider Availability:**
```java
@Test
public void testProviderDetection() {
    ProviderRegistry registry = new ProviderRegistry(logger);
    registry.detectAndRegister();

    assertNotNull(registry.getPlotProvider());
    assertFalse(registry.getPlotProvider().isAvailable()); // No Towny in test
}
```

**Test Graceful Degradation:**
```java
@Test
public void testGracefulDegradation() {
    PlotProvider provider = new NoOpPlotProvider();

    assertThrows(ProviderFunctionalityNotFoundException.class, () -> {
        provider.getPlot(mockLocation);
    });
}
```

### Integration Testing

**On Test Server:**

1. **Install Core Only:**
   ```bash
   cp core/target/*.jar /server/plugins/
   # Start server, check logs for provider detection
   ```

2. **Add Optional Plugins:**
   ```bash
   # Install Towny
   # Restart server
   # Verify TownyPlotProvider registered
   ```

3. **Test Features:**
   - Commands execute without errors
   - Features gracefully disable when providers unavailable
   - No exceptions in console

### Manual Testing Checklist

**Core Plugin:**
- [ ] Plugin loads without errors
- [ ] Provider auto-detection works
- [ ] ProvidersReadyEvent fires
- [ ] Config loads correctly
- [ ] DataStore initializes

**Modules:**
- [ ] Module loads after Core
- [ ] Receives ProvidersReadyEvent
- [ ] Features enable/disable based on providers
- [ ] Commands registered
- [ ] No errors in console

---

## Important Files Reference

### Documentation Files (Read First)

| File | Purpose |
|------|---------|
| `README.md` | Main repository overview |
| `REPOSITORY_INDEX.md` | Complete file structure |
| `QUICKSTART.md` | 5-minute quick start guide |
| `SETUP_COMPLETE.md` | What's done, what's missing |
| `core/README.md` | Core plugin documentation |
| `module-*/README.md` | Module-specific docs |

### Key Source Files

| File | Location | Purpose |
|------|----------|---------|
| `FallenStarCore.java` | `core/src/main/java/.../core/` | Core plugin main class |
| `ProviderRegistry.java` | `core/src/main/java/.../registry/` | Provider auto-detection |
| `PlotProvider.java` | `core/src/main/java/.../provider/` | Example provider interface |
| `NoOpPlotProvider.java` | `core/src/main/java/.../provider/impl/` | Example NoOp implementation |
| `TownyPlotProvider.java` | `core/src/main/java/.../provider/impl/` | Example concrete implementation |
| `PlotsModule.java` | `module-plots/src/main/java/.../plots/` | Example module main class |

### Build Files

| File | Location | Purpose |
|------|----------|---------|
| `pom.xml` | Root | Parent POM, manages all modules |
| `pom.xml` | Each module | Module-specific build config |
| `plugin.yml` | `src/main/resources/` | Plugin metadata |
| `config.yml` | `src/main/resources/` | Runtime configuration |

### Templates to Copy

**When implementing:**

| Task | Template File |
|------|---------------|
| New NoOp Provider | `NoOpPlotProvider.java` |
| New Provider Interface | `PlotProvider.java` |
| New Concrete Provider | `TownyPlotProvider.java` |
| New Module Main Class | `PlotsModule.java` |
| New Command | Command-Klassen aus `module-plots/` |
| New Module README | `module-plots/README.md` |

---

## AI Assistant Tips

### Context Loading Strategy

**For Each Task:**

1. **Load Relevant Documentation:**
   - Sprint goal from `SETUP_COMPLETE.md`
   - Module README
   - Architecture overview (this file)

2. **Load Template Files:**
   - Similar existing implementation
   - Provider interfaces if working with providers
   - Module structure if creating new module

3. **Check Dependencies:**
   - What providers are needed?
   - What's already implemented?
   - What's the dependency chain?

### Task Decomposition

**Break Down Large Tasks:**

```
"Implement Items Module" →
  1. Implement ItemProvider-Interface Nutzung
  2. Implement MMOItemsItemProvider (in module-items/provider/)
  3. Implement Item-Manager
  4. Implement Item-Factory für Custom Items
  5. Implement Item-Commands
  6. Create config.yml mit Item-Definitions
  7. Test functionality
```

### Code Generation Best Practices

1. **Always follow existing patterns** - Don't invent new structures
2. **Copy Javadoc style** from templates
3. **🇩🇪 Use German** for all documentation, comments, and user communication (see [Language Conventions](#language-conventions--sprachkonventionen))
4. **Test compilation** after each class
5. **Reference line numbers** when discussing code (e.g., `FallenStarCore.java:82`)

### Common Pitfalls to Avoid

❌ **Don't:**
- Create circular dependencies between modules
- Put business logic in Core plugin
- Skip Javadoc comments
- Use direct plugin APIs (bypass providers)
- Create files without checking structure first

✅ **Do:**
- Follow provider pattern religiously
- Use graceful degradation
- Match existing code style
- Test incrementally
- Document thoroughly

---

## Quick Reference Commands

### File Navigation

```bash
# View structure
cat REPOSITORY_INDEX.md

# Find a class
find . -name "PlotProvider.java"

# Search for pattern
grep -r "ProviderRegistry" core/src/

# List Java files
find core/src -name "*.java"
```

### Build Commands

```bash
# Clean build all
mvn clean package

# Build single module
cd core && mvn clean package

# Skip tests
mvn clean package -DskipTests

# Verbose output
mvn clean package -X
```

### Git Commands

```bash
# Check status
git status

# Commit changes
git add .
git commit -m "Message"

# Push to branch
git push -u origin claude/claude-md-mi0sco9raq2ajdr6-01Tte3UhY6FdvsCXyqqVvJ8k

# View recent commits
git log --oneline -10
```

---

## Summary

**When working on this codebase:**

1. ✅ **🇩🇪 Arbeite auf Deutsch** - Dokumentation, Kommentare, User-Kommunikation
2. ✅ **Understand the provider pattern** - it's the foundation
3. ✅ **Follow sprint-based approach** - one module at a time
4. ✅ **Use templates** - don't reinvent patterns
5. ✅ **Document heavily** - Javadoc everything (auf Deutsch!)
6. ✅ **Test incrementally** - build after each class
7. ✅ **Check SETUP_COMPLETE.md** - know what's missing
8. ✅ **Reference existing code** - maintain consistency

**Key Architecture Principle:**
> "Modules depend on Core, Core provides providers, providers abstract external plugins, NoOp providers enable graceful degradation."

**Development Mantra:**
> "Sprint → Template → Implement → Document → Test → Commit"

---

## Sprint 5-6: Items-Modul - Wichtige Erkenntnisse

### Technische Herausforderungen gelöst:

1. **MMOItems API 6.10+ Kompatibilität**
   - `getTags()` entfernt → Type-basierte Kategorisierung
   - `getConfigFile()` entfernt → Vereinfachte Preisberechnung
   - `ItemStat` API komplett umgebaut → Stats-Zugriff entfernt

2. **MMOPlugin-Dependency Problem (Kritisch!)**
   - Problem: `MMOItems.plugin` benötigt `io.lumine.mythic.lib.module.MMOPlugin`
   - Lösung: **Reflection-basierter Zugriff** auf MMOItems API
   - Helper-Methoden: `getAllMMOTypes()`, `getType()`, `getTemplate()`, `getTemplates()`
   - Benefit: Kein direkter Import der MMOItems-Klasse zur Compile-Zeit nötig!

3. **Maven Dependencies**
   - `MMOItems-API 6.10.1-SNAPSHOT` (Phoenix Repository)
   - `MythicLib-dist 1.6.2-SNAPSHOT` (Required by MMOItems)
   - Core ArtifactId: `fallenstar-core` (nicht `core`)

4. **Exception Handling**
   - Alle `ItemProvider`-Methoden werfen `ProviderFunctionalityNotFoundException`
   - Try-catch in **allen** UI-Klassen, Commands und Managern erforderlich
   - Graceful Degradation mit Optional.empty() oder false-Fallbacks

### Implementierte Komponenten:

- ✅ **MMOItemsItemProvider**: Reflection-Wrapper für MMOItems 6.10+
- ✅ **ItemBrowserUI**: Kategorie-basierter Item-Browser mit Pagination
- ✅ **TestTradeUI**: Vanilla Trading Interface Demo
- ✅ **SpecialItemManager**: Währungs-Items (Münzen), UI-Buttons
- ✅ **ItemsCommand**: `/items browse`, `/items info`, `/items reload`
- ✅ **UIRegistry-Integration**: Test-UIs verfügbar über `/fscore admin gui`

### Best Practices etabliert:

1. **Reflection Pattern** für externe Plugin-APIs mit komplexen Class-Hierarchien
2. **Comprehensive Exception Handling** in allen Provider-Consumers
3. **Type-basierte Fallbacks** wenn Original-API-Features entfernt wurden
4. **Cache-Invalidierung** für Hot-Reload-Support

---

## Sprint 7-8: UI-Modul - Wichtige Erkenntnisse

### Implementierte Komponenten:

1. **UI-Modul Struktur**
   - Neues Maven-Modul `module-ui`
   - Dependency auf Core (UI-Framework)
   - ProvidersReadyEvent-basierte Initialisierung

2. **UIButtonManager** ✅
   - Verwaltet UI-Button Items (Confirm, Cancel, Close, Info, Next, Previous, Back)
   - Button-Cache für Performance
   - Factory-Methoden für einfache Erstellung
   - Customizable Namen und Lore

3. **ConfirmationUI** ✅
   - Generisches Ja/Nein Bestätigungs-Dialog
   - Layout: 9 Slots (SmallChestUI)
   - Grüne Wolle (Ja) - Slot 3
   - Rote Wolle (Nein) - Slot 5
   - Barriere (Schließen) - Slot 8 (oben rechts)
   - Factory-Methoden: `createSimple()`, `createWithCallbacks()`

4. **SimpleTradeUI** ✅
   - Vanilla Trading Demo (ohne MMOItems)
   - Layout: 54 Slots (LargeChestUI)
   - 6 Trade-Angebote mit Input1 + Input2 → Output
   - Demo-Implementierung ohne echte Inventar-Prüfung
   - Testdaten mit verschiedenen Vanilla-Items

5. **Testbefehl-Struktur** ✅
   - `/fscore admin gui confirm` - Öffnet ConfirmationUI
   - `/fscore admin gui trade` - Öffnet SimpleTradeUI
   - `/fscore admin items` - Placeholder für Item-Befehle
   - `/fscore admin plots` - Placeholder für Plot-Befehle
   - Alte `/fscore plotstorage` Befehle entfernt

### Special Items Architektur:

**Konzept:**
- Module registrieren eigene Special Items Kategorien
- Jedes Modul verwaltet seine eigenen Special Items
- Wiederverwendbare Items über Manager-Klassen

**Implementierungen:**
- **Items-Modul:** `SpecialItemManager` → Währungs-Items (bronze/silver/gold coins)
- **UI-Modul:** `UIButtonManager` → UI-Button Items (Confirm, Cancel, Close, etc.)
- **Economy-Modul:** Weitere Währungs-Items (geplant)

### Best Practices etabliert:

1. **UI-Registrierung in UIRegistry** für zentrale Verwaltung
2. **Factory-Pattern** für UI-Erstellung mit verschiedenen Konfigurationen
3. **Button-Manager Pattern** für wiederverwendbare UI-Elemente
4. **Layout-Konzepte** mit festen Slot-Positionen (Consistency)

### Nächste Schritte (Sprint 7-8 Fortsetzung):

- 📋 DialogUI (NPC-Dialoge, Quest-Texte)
- 📋 AmbassadorUI (Townverwaltung, NPC-Interaktion)
- 📋 StorageUI (Inventory-Management, Chest-Sorting)
- 📋 Weitere UI-Button Varianten (Warning, Success, etc.)

---

## Sprint 9-10: Economy-Modul - Implementierung

### Implementierte Komponenten:

1. **Economy-Modul Struktur**
   - Neues Maven-Modul `module-economy`
   - Hard Dependencies: FallenStar-Core, FallenStar-Items, Vault
   - ProvidersReadyEvent-basierte Initialisierung

2. **CurrencyItemSet (Record)** ✅
   - Immutable Währungs-Modell
   - Bronze/Silber/Gold Tiers (1er/10er/100er Münzen)
   - Exchange Rate zur Basiswährung (BigDecimal)
   - Wechselkurs-Berechnungen (toBaseCurrency, fromBaseCurrency)
   - Factory-Methode für Basiswährung

3. **CurrencyManager** ✅
   - Währungen registrieren/verwalten
   - `payoutCoins(player, currency, tier, amount)` - Münzen auszahlen
   - Integration mit SpecialItemManager (Items-Modul)
   - getCurrency, getBaseCurrency, getCurrencyIds
   - getCurrencyCount für Status-Logs

4. **Basiswährung "Sterne"** ✅
   - Bronzestern (Kupferbarren/COPPER_INGOT, Custom Model Data: 1, Wert: 1)
   - Silberstern (Eisenbarren/IRON_INGOT, Custom Model Data: 2, Wert: 10)
   - Goldstern (Goldbarren/GOLD_INGOT, Custom Model Data: 3, Wert: 100)
   - Wechselkurs: 1:1 (Referenzwährung)
   - Automatische Registrierung beim Modul-Start

5. **EconomyModule.java** ✅
   - Main Plugin Class mit ProvidersReadyEvent
   - Dependencies-Check (Items-Modul, Vault)
   - CurrencyManager-Initialisierung
   - Basiswährung automatisch registriert
   - Getter für CurrencyManager und ProviderRegistry

6. **Admin-Befehle** ✅
   - `/fscore admin economy getcoin <währung> [tier] [anzahl]` - Kostenlose Münzen
   - `/fscore admin economy withdraw <währung> [tier] [anzahl]` - Vault-basierte Auszahlung
   - Beispiele:
     - `/fscore admin economy getcoin sterne bronze 10`
     - `/fscore admin economy withdraw sterne silver 5`
   - Tab-Completion für Währung, Tier, Anzahl
   - Command-Handler-Pattern (keine Reflection mehr!)
   - Balance-Anzeige vor/nach withdraw

7. **VaultEconomyProvider** ✅
   - Implementiert EconomyProvider-Interface
   - Vault Economy API Integration (iConomy, Essentials, etc.)
   - Methoden: getBalance, withdraw, deposit, format
   - Registrierung in ProviderRegistry beim Modul-Start
   - Automatische Economy-Erkennung

8. **Withdraw-Funktionalität** ✅
   - `CurrencyManager.withdrawCoins()` mit Vault-Integration
   - Berechnet Kosten basierend auf Tier-Wert und Wechselkurs
   - Prüft Vault-Balance vor Auszahlung
   - Zahlt maximal möglichen Betrag aus bei unzureichendem Guthaben
   - Rollback-Pattern: Erstattet Geld bei Item-Auszahlungs-Fehler

### Technische Details:

1. **SpecialItem-Integration**
   - Basiswährung nutzt SpecialItemManager v3.0
   - PDC-basierte Item-Identifikation
   - Custom Model Data für Texturepack-Support
   - Vanilla-First Approach (kein MMOItems erforderlich)

2. **Extensibility**
   - Neue Währungen einfach hinzufügbar via `registerCurrency()`
   - Exchange Rates für Multi-Currency Support
   - Wechselkurs-Berechnungen automatisch
   - CurrencyTier Enum (BRONZE, SILVER, GOLD)

3. **Inter-Modul-Kommunikation** (Reflection eliminiert!)
   - **Command-Handler-Registry-Pattern** statt Reflection
   - `AdminSubcommandHandler` Interface für Module
   - `AdminCommandRegistry` für zentrale Handler-Verwaltung
   - Module registrieren eigene Handler in `onProvidersReady()`
   - Type-safe Method Calls, bessere IDE-Unterstützung

### Geplante Features (zukünftige Sprints):

- ✅ VaultEconomyProvider (Vault-Integration) - **FERTIG**
- ✅ Withdraw-System - **FERTIG**
- 📋 Deposit-Command (Münzen → Vault-Guthaben)
- 📋 Preisberechnungen (dynamisch, material-basiert, region-basiert)
- 📋 Shop-System (Admin-Shops, Player-Shops, Shop-UIs)
- 📋 Transaktions-Historie
- 📋 Währungskonvertierung-UI

### Custom Model Data Manager (Roadmap-Erweiterung):

**Hintergrund:**
- Währungs-Items nutzen aktuell Custom Model Data (CMD) für Resource Pack Support
- CMD-Werte werden manuell vergeben (Bronze: 1, Silber: 2, Gold: 3)
- Zukünftig: Zentrale Verwaltung aller Custom Model Data Werte

**Geplant für Items-Modul (zukünftiger Sprint):**
- ✅ **CustomModelDataRegistry**: Zentrale Registry für CMD-Werte
- ✅ **Kategorisierung**: Material-basierte CMD-Pools (COPPER_INGOT: 1-100, IRON_INGOT: 101-200, etc.)
- ✅ **Konflikvermeidung**: Automatische Prüfung auf CMD-Kollisionen
- ✅ **Resource Pack Integration**: Export aller CMD-Werte für Resource Pack Generator
- ✅ **Admin-UI**: Übersicht aller registrierten CMD-Werte
- ✅ **Validierung**: Warnung bei CMD-Überschneidungen zwischen Modulen

**Use Cases:**
- Währungs-Items (Economy-Modul)
- UI-Button Items (UI-Modul)
- Quest-Items (zukünftiges Quest-Modul)
- Custom Tools/Armor (zukünftige Module)

---

## Reflection-Eliminierung: Command-Handler-Registry-Pattern

### Problem

Ursprüngliche Implementierung nutzte **java.lang.reflect** für Inter-Modul-Kommunikation:
- `AdminCommand` (Core) → Reflection-Aufrufe → Module (Economy, Plots)
- Runtime-Errors bei falschen Method-Names/Signatures
- Keine IDE-Unterstützung (Autocompletion, Refactoring)
- Schwer wartbar und fehleranfällig

### Lösung: Command-Handler-Registry-Pattern

**Core-Komponenten:**
```java
// 1. Interface für Module
public interface AdminSubcommandHandler {
    boolean handle(CommandSender sender, String[] args);
    List<String> getTabCompletions(String[] args);
    void sendHelp(CommandSender sender);
}

// 2. Registry für Handler
public class AdminCommandRegistry {
    private final Map<String, AdminSubcommandHandler> handlers;

    public void registerHandler(String subcommand, AdminSubcommandHandler handler) {
        handlers.put(subcommand.toLowerCase(), handler);
    }

    public Optional<AdminSubcommandHandler> getHandler(String subcommand) {
        return Optional.ofNullable(handlers.get(subcommand.toLowerCase()));
    }
}

// 3. AdminCommand delegiert an Handler
private void handleEconomyCommand(CommandSender sender, String[] args) {
    AdminCommandRegistry registry = getAdminRegistry();
    if (registry == null) {
        sender.sendMessage("Admin-Command-System noch nicht bereit!");
        return;
    }

    registry.getHandler("economy").ifPresentOrElse(
        handler -> handler.handle(sender, args),
        () -> sender.sendMessage("Economy-Modul nicht geladen!")
    );
}
```

**Modul-Implementierung:**
```java
// Economy-Modul
public class EconomyAdminHandler implements AdminSubcommandHandler {
    private final CurrencyManager currencyManager;

    @Override
    public boolean handle(CommandSender sender, String[] args) {
        // Direkte Methoden-Aufrufe - keine Reflection!
        if (args[0].equals("getcoin")) {
            currencyManager.payoutCoins(...);
        }
    }
}

// In EconomyModule.onProvidersReady()
AdminCommandRegistry registry = core.getAdminCommandRegistry();
registry.registerHandler("economy", new EconomyAdminHandler(currencyManager, providers));
```

### Vorteile

- ✅ **Type-Safe**: Compile-time Fehlerprüfung statt Runtime-Errors
- ✅ **IDE-Support**: Autocompletion, Refactoring, "Find Usages"
- ✅ **Performance**: Direkte Method Calls statt Reflection
- ✅ **Wartbarkeit**: Klare Interfaces und Dependencies
- ✅ **Testbarkeit**: Handler isoliert testbar
- ✅ **Erweiterbar**: Neue Module registrieren einfach eigene Handler

### Implementierte Handler

- **EconomyAdminHandler** (Economy-Modul): `getcoin`, `withdraw` Commands
- **PlotsAdminHandler** (Plots-Modul): `info`, `storage view`, `storage scan` Commands

### Code-Reduktion

- ❌ Entfernt: ~550 Zeilen Reflection-Code aus `AdminCommand`
- ✅ Hinzugefügt: ~300 Zeilen sauberer Handler-Code in Modulen
- **Netto:** -250 Zeilen, bessere Wartbarkeit

---

## Plot-System: Owner-Berechtigungen

**Regel:** Grundstücks-Befehle erfordern Owner-Rechte!

### Owner-Check Pattern

Alle Plot-Verwaltungsbefehle müssen prüfen, ob der Spieler der Besitzer des Grundstücks ist:

```java
/**
 * Prüft ob ein Spieler der Besitzer eines Plots ist.
 *
 * @param player Der Spieler
 * @param plot Der Plot
 * @return true wenn Besitzer
 */
private boolean isPlotOwner(Player player, Plot plot) {
    PlotProvider plotProvider = providers.getPlotProvider();
    try {
        return plotProvider.isOwner(plot, player);
    } catch (Exception e) {
        // Bei Fehler: kein Zugriff
        return false;
    }
}
```

### Berechtigungsmatrix

#### Public Commands (ALLE Spieler):
- `/plot info` - Plot-Informationen anzeigen
- `/plot price list` - Preisliste anzeigen

#### Owner-Only Commands (NUR Besitzer):
- `/plot price set` - Preise festlegen
- `/plot storage setreceiver` - Empfangskiste setzen
- `/plot npc spawn` - NPCs spawnen
- `/plot gui` - Verwaltungs-GUI öffnen (Owner-Ansicht)

### Implementierungsbeispiel

```java
public boolean execute(Player player, String[] args) {
    Plot plot = getCurrentPlot(player);

    // Public Commands
    if (subCommand.equals("list")) {
        handleListPrices(player, plot);
        return true;
    }

    // Owner-Check für alle anderen Befehle
    if (!isPlotOwner(player, plot)) {
        player.sendMessage("§cDu musst der Besitzer dieses Grundstücks sein!");
        try {
            String owner = plotProvider.getOwnerName(plot);
            player.sendMessage("§7Besitzer: §e" + owner);
        } catch (Exception e) {
            // Ignoriere Fehler
        }
        return true;
    }

    // Owner-exklusive Befehle
    switch (subCommand) {
        case "set" -> handleSetPrice(player, plot);
        // ...
    }
}
```

---

## Sprint 11-12: Trading-System & Händler-Infrastruktur

**Ziel:** Vollständiges Trading-System mit Händler-NPCs, PlotRegistry und virtuellem Inventar.

### Implementierte Komponenten (Sprint 11-12)

#### 1. TradeSet-System (Economy-Modul)

**Handels-Modell für NPC-Händler:**

```java
package de.fallenstar.economy.model;

/**
 * Repräsentiert ein Handels-Angebot (Input → Output).
 *
 * Features:
 * - Ankauf und Verkauf-Preise
 * - Münz-basierte Preise (in Basiswährung)
 * - Mehrere Inputs (Input1 + Input2 optional)
 * - Output-Item
 */
public class TradeSet {
    private final UUID tradeId;
    private final ItemStack input1;          // Haupt-Input (erforderlich)
    private final ItemStack input2;          // Optionaler zweiter Input
    private final ItemStack output;          // Output-Item
    private final BigDecimal buyPrice;       // Ankaufpreis (Spieler verkauft an NPC)
    private final BigDecimal sellPrice;      // Verkaufspreis (Spieler kauft von NPC)
    private final int maxUses;               // Maximale Anzahl Trades (-1 = unbegrenzt)

    // Methoden: getBuyPrice(), getSellPrice(), createRecipe()
}
```

**Verwendung:**
```java
// Erstelle TradeSet: 10 Diamanten → 100 Sterne
TradeSet trade = new TradeSet(
    new ItemStack(Material.DIAMOND, 10),  // Input
    null,                                   // Kein zweiter Input
    coinManager.createCoin("sterne", BRONZE, 100),  // Output
    BigDecimal.valueOf(90),                 // Ankaufpreis (NPC zahlt 90)
    BigDecimal.valueOf(110),                // Verkaufspreis (Spieler zahlt 110)
    -1                                      // Unbegrenzte Trades
);
```

#### 2. TradingEntity-Interface (Core)

**Provider-Interface für alle handelnden Entities:**

```java
package de.fallenstar.core.provider;

/**
 * Interface für handelbare Entities (NPCs, Shops, etc.).
 *
 * Implementierungen:
 * - GuildTraderNPC (Gildenhändler - nutzt Plot-Storage)
 * - PlayerTraderNPC (Spielerhändler - nutzt virtuelles Inventar)
 * - TravelingMerchantNPC (Fahrende Händler - eigenes Inventar)
 *
 * Features:
 * - TradeSets abrufen
 * - Inventar-Zugriff (Rohstoffspeicher)
 * - Trade-Validierung
 */
public interface TradingEntity {
    /**
     * Gibt alle TradeSets dieser Entity zurück.
     */
    List<TradeSet> getTradeSets();

    /**
     * Gibt das Inventar (Rohstoffspeicher) zurück.
     */
    Optional<Inventory> getTradeInventory();

    /**
     * Prüft ob ein Trade ausgeführt werden kann.
     */
    boolean canExecuteTrade(TradeSet trade, Player player);

    /**
     * Führt einen Trade aus.
     */
    boolean executeTrade(TradeSet trade, Player player);

    /**
     * Gibt den Entity-Typ zurück.
     */
    TradingEntityType getEntityType();

    enum TradingEntityType {
        GUILD_TRADER,      // Gildenhändler (Plot-Storage)
        PLAYER_TRADER,     // Spielerhändler (virtuelles Inventar)
        TRAVELING_MERCHANT, // Fahrender Händler (eigenes Inventar)
        WORLD_BANKER       // Weltbankier (unbegrenztes Inventar)
    }
}
```

#### 3. TradeUI (UI-Modul)

**Dynamisches Trading-Interface für TradingEntities:**

```java
package de.fallenstar.ui.ui;

/**
 * Universelles Trading-UI für alle TradingEntities.
 *
 * Features:
 * - Nutzt Vanilla Merchant Interface
 * - Dynamische TradeSets von TradingEntity
 * - Automatische Preis-Konvertierung (Münzen)
 * - Inventar-Validierung gegen TradingEntity.getTradeInventory()
 *
 * Verwendung:
 * openTradeUI(player, guildTrader);
 */
public class TradeUI extends BaseUI {
    /**
     * Öffnet das Trade-UI für einen Spieler.
     *
     * @param player Der Spieler
     * @param trader Die TradingEntity (Händler)
     */
    public static void openTradeUI(Player player, TradingEntity trader) {
        // Erstelle Merchant mit TradeSets
        Merchant merchant = Bukkit.createMerchant(trader.getName());

        List<MerchantRecipe> recipes = trader.getTradeSets().stream()
            .map(TradeSet::createRecipe)
            .toList();

        merchant.setRecipes(recipes);
        player.openMerchant(merchant, true);
    }
}
```

#### 4. PlotRegistry (Plots-Modul)

**Zentrale Registry für spezielle Grundstückstypen:**

```java
package de.fallenstar.plot.registry;

/**
 * Registry für spezielle Grundstückstypen (Handelsgilden, Botschaften, etc.).
 *
 * Features:
 * - Auto-Registration via Towny-Events
 * - Auto-Deregistration bei Plot-Typ-Änderung oder Löschung
 * - Suche nach Grundstückstyp
 * - Persistent (in Config gespeichert)
 *
 * Verwendung:
 * List<Plot> guilds = plotRegistry.getPlotsByType(PlotType.MERCHANT_GUILD);
 */
public class PlotRegistry {
    private final Map<PlotType, Set<Plot>> registeredPlots;

    public enum PlotType {
        MERCHANT_GUILD,    // Handelsgilde (Händler-Slots)
        EMBASSY,           // Botschaft (Botschafter-Slots)
        BANK,              // Bank (Bankier-Slots)
        WORKSHOP           // Werkstatt (Handwerker-Slots)
    }

    /**
     * Registriert ein Grundstück.
     */
    public void registerPlot(Plot plot, PlotType type);

    /**
     * De-registriert ein Grundstück.
     */
    public void unregisterPlot(Plot plot);

    /**
     * Gibt alle Grundstücke eines Typs zurück.
     */
    public List<Plot> getPlotsByType(PlotType type);

    /**
     * Prüft ob ein Grundstück registriert ist.
     */
    public boolean isRegistered(Plot plot);

    /**
     * Gibt den Typ eines Grundstücks zurück.
     */
    public Optional<PlotType> getPlotType(Plot plot);
}
```

**Towny-Integration:**
```java
@EventHandler
public void onPlotTypeChange(TownBlockTypeRegisterEvent event) {
    // Automatische Registration bei Plot-Typ-Änderung
    TownBlock block = event.getTownBlock();

    if (block.getType() == TownBlockType.COMMERCIAL) {
        Plot plot = plotProvider.getPlot(block.getWorldCoord().getBukkitLocation());
        plotRegistry.registerPlot(plot, PlotType.MERCHANT_GUILD);
    }
}

@EventHandler
public void onPlotDelete(TownBlockRemoveEvent event) {
    // Automatische De-Registration bei Plot-Löschung
    Plot plot = plotProvider.getPlot(event.getTownBlock().getWorldCoord().getBukkitLocation());
    plotRegistry.unregisterPlot(plot);
}
```

#### 5. Virtuelles Händler-Inventar (Plots-Modul)

**Persistentes Inventar für Spielerhändler:**

```java
package de.fallenstar.plot.trader;

/**
 * Virtuelles Inventar für Spielerhändler auf Handelsgilden.
 *
 * Features:
 * - Plot-gebunden (nicht weltbasiert)
 * - Persistent in Config gespeichert
 * - 54 Slots (LargeChest-Größe)
 * - Verwaltung via /plot gui → "Händler-Inventar"
 *
 * Speicherung:
 * - Plots-Modul Config (plots.yml)
 * - Serialisierung: ItemStack → Base64 → Config
 */
public class VirtualTraderInventory {
    private final UUID playerId;           // Besitzer des Händlers
    private final Plot plot;                // Zugewiesenes Grundstück
    private final ItemStack[] contents;     // 54 Slots

    /**
     * Lädt Inventar aus Config.
     */
    public void loadFromConfig(FileConfiguration config);

    /**
     * Speichert Inventar in Config.
     */
    public void saveToConfig(FileConfiguration config);

    /**
     * Öffnet Inventar für Spieler (Bearbeitung).
     */
    public void open(Player player);

    /**
     * Gibt Items zurück.
     */
    public ItemStack[] getContents();

    /**
     * Setzt Items.
     */
    public void setContents(ItemStack[] contents);
}
```

**Zugriff via HandelsgildeUI:**
```java
// Owner-View: Button "Persönliches Handelsinventar"
ItemStack inventoryButton = createButton(
    Material.CHEST,
    "§6§lHändler-Inventar",
    "§7Verwalte das Inventar deiner Händler"
);

setItem(16, inventoryButton, player -> {
    VirtualTraderInventory inv = getPlayerTraderInventory(player, plot);
    inv.open(player);
});
```

#### 6. Slot-Verwaltungs-GUI (Plots-Modul)

**UI zum Platzieren von Händlern auf Slots:**

```java
package de.fallenstar.plot.ui;

/**
 * GUI zur Verwaltung von Händler-Slots.
 *
 * Features:
 * - Zeigt alle verfügbaren Slots auf dem Grundstück
 * - Händler auf Slots platzieren (aus PlotRegistry-Handelsgilden)
 * - Händler von Slots entfernen
 * - Neue Slots kaufen (Kosten konfigurierbar)
 *
 * Workflow:
 * 1. Spieler öffnet /plot gui auf Grundstück mit Trader-Slots
 * 2. Klickt auf "Händler-Slots verwalten"
 * 3. Sieht Liste freier Slots
 * 4. Klickt auf Slot → Händler-Auswahl-UI
 * 5. Wählt Händler aus PlotRegistry-Handelsgilden
 * 6. Händler reist zum Slot (NPC-Reisesystem)
 */
public class SlotManagementUI extends LargeChestUI {
    /**
     * Öffnet das Slot-Management-UI.
     */
    public void open(Player player, SlottedPlot plot);

    /**
     * Zeigt verfügbare Händler aus Handelsgilden.
     */
    private void showAvailableTraders(Player player, PlotSlot slot);

    /**
     * Platziert Händler auf Slot.
     */
    private void assignTraderToSlot(Player player, PlotSlot slot, TradingEntity trader);
}
```

**Integration:**
- Händler-Liste von `PlotRegistry.getPlotsByType(MERCHANT_GUILD)`
- Nur Händler des Spielers anzeigen
- Kosten + Verzögerung via NPC-Reisesystem

#### 7. NPC-Reisesystem (Plots-Modul)

**System für NPC-Bewegungen zwischen Grundstücken:**

```java
package de.fallenstar.plot.npc;

/**
 * Verwaltet NPC-Reisen zwischen Grundstücken.
 *
 * Features:
 * - Verzögerung: 10 Sekunden pro Chunk-Entfernung
 * - Kosten: 5 Sterne pro Chunk-Entfernung
 * - Routen-Unterstützung (mehrere Waypoints)
 * - Restart-Handling: Bei Server-Neustart → NPC direkt ans Ziel
 *
 * Verwendung:
 * npcTravelSystem.startTravel(npc, fromPlot, toSlot);
 */
public class NPCTravelSystem {
    /**
     * Startet eine NPC-Reise.
     *
     * @param npc Der NPC
     * @param from Start-Grundstück
     * @param toSlot Ziel-Slot
     * @return TravelTicket mit Reise-Details
     */
    public TravelTicket startTravel(UUID npc, Plot from, PlotSlot toSlot);

    /**
     * Berechnet Reisekosten.
     *
     * @param from Start-Location
     * @param to Ziel-Location
     * @return Kosten in Basiswährung (5 Sterne/Chunk)
     */
    public BigDecimal calculateTravelCost(Location from, Location to);

    /**
     * Berechnet Reisedauer.
     *
     * @param from Start-Location
     * @param to Ziel-Location
     * @return Dauer in Sekunden (10s/Chunk)
     */
    public int calculateTravelTime(Location from, Location to);

    /**
     * Lädt aktive Reisen aus Config (Restart-Handling).
     */
    public void loadActiveTravel();

    /**
     * Speichert aktive Reisen in Config.
     */
    public void saveActiveTravel();
}

/**
 * Reise-Ticket mit Reise-Details.
 */
public class TravelTicket {
    private final UUID npcId;
    private final Location from;
    private final Location to;
    private final long startTime;
    private final int durationSeconds;
    private final BigDecimal cost;

    public boolean isComplete();
    public int getRemainingSeconds();
}
```

**Restart-Handling:**
```yaml
# Config: active-travels.yml
active-travels:
  npc-uuid-123:
    from:
      world: "world"
      x: 100
      y: 64
      z: 200
    to:
      world: "world"
      x: 500
      y: 64
      z: 600
    start-time: 1234567890
    duration: 200
    cost: 50.0
```

**Bei Server-Start:**
```java
public void onEnable() {
    npcTravelSystem.loadActiveTravel();

    // Für jede aktive Reise:
    for (TravelTicket ticket : activeTravel) {
        if (ticket.isComplete()) {
            // Reise abgeschlossen → NPC direkt ans Ziel
            teleportNPC(ticket.getNpcId(), ticket.getTo());
        } else {
            // Reise läuft noch → Fortsetzen
            scheduleArrival(ticket);
        }
    }
}
```

#### 8. NPC-Skin-Pool-System (Plots-Modul)

**Zufällige Skins für NPC-Typen:**

```java
package de.fallenstar.plot.npc;

/**
 * Verwaltet Skin-Pools für verschiedene NPC-Typen.
 *
 * Features:
 * - Admin setzt Skin-Pool pro NPC-Typ
 * - Zufällige Skin-Auswahl bei NPC-Erstellung
 * - Skin-Rotation (optional)
 * - Persistent in Config
 *
 * Verwendung:
 * skinPool.addSkin(NPCType.TRADER, playerSkin);
 * String randomSkin = skinPool.getRandomSkin(NPCType.TRADER);
 */
public class NPCSkinPool {
    private final Map<NPCType, List<String>> skinPools;

    public enum NPCType {
        TRADER,        // Händler
        BANKER,        // Bankier
        AMBASSADOR,    // Botschafter
        CRAFTSMAN,     // Handwerker
        TRAVELING      // Fahrender Händler
    }

    /**
     * Fügt Skin zu Pool hinzu.
     */
    public void addSkin(NPCType type, String playerName);

    /**
     * Gibt zufälligen Skin zurück.
     */
    public String getRandomSkin(NPCType type);

    /**
     * Lädt Skins aus Config.
     */
    public void loadFromConfig(FileConfiguration config);

    /**
     * Speichert Skins in Config.
     */
    public void saveToConfig(FileConfiguration config);
}
```

**Config-Struktur:**
```yaml
# npc-skins.yml
skin-pools:
  TRADER:
    - "Notch"
    - "jeb_"
    - "Dinnerbone"
  BANKER:
    - "MHF_Villager"
    - "MHF_Alex"
  AMBASSADOR:
    - "MHF_Steve"
```

**Integration bei NPC-Erstellung:**
```java
public void spawnTrader(Player owner, PlotSlot slot) {
    // Hole zufälligen Skin
    String skin = skinPool.getRandomSkin(NPCType.TRADER);

    // Erstelle NPC mit Skin
    NPC npc = npcRegistry.createNPC(EntityType.PLAYER, "Händler");
    npc.data().set(NPC.PLAYER_SKIN_UUID_METADATA, skin);

    // Spawn an Slot-Position
    npc.spawn(slot.getLocation());
}
```

#### 9. Plot-Namen-Feature (Plots-Modul)

**Benutzerdefinierte Namen für Grundstücke:**

```java
package de.fallenstar.plot.model;

/**
 * Erweitert Plot-Interface um Namen-Feature.
 */
public interface NamedPlot extends Plot {
    /**
     * Gibt den benutzerdefinierten Namen zurück.
     */
    Optional<String> getCustomName();

    /**
     * Setzt den benutzerdefinierten Namen.
     */
    void setCustomName(String name);

    /**
     * Entfernt den benutzerdefinierten Namen.
     */
    void clearCustomName();

    /**
     * Gibt den Anzeige-Namen zurück (Custom oder Default).
     */
    default String getDisplayName() {
        return getCustomName().orElse("Plot #" + getPlotId());
    }
}
```

**Owner GUI Button:**
```java
// In HandelsgildeUI (Owner-View)
ItemStack nameButton = createButton(
    Material.NAME_TAG,
    "§e§lPlot-Namen setzen",
    "§7Aktuell: " + plot.getDisplayName(),
    "§7",
    "§a§lKlicke zum Ändern"
);

setItem(24, nameButton, player -> {
    // Öffne AnvilUI für Namen-Eingabe
    openNameInputUI(player, plot);
});
```

**Plot-Listen-Anzeige:**
```java
// In PlotListUI
private ItemStack createPlotItem(Plot plot) {
    ItemStack item = new ItemStack(Material.MAP);
    ItemMeta meta = item.getItemMeta();

    // Zeige Custom-Namen wenn vorhanden
    String displayName = plot instanceof NamedPlot namedPlot ?
        namedPlot.getDisplayName() : "Plot #" + plot.getPlotId();

    meta.displayName(Component.text(displayName).color(NamedTextColor.GOLD));
    item.setItemMeta(meta);
    return item;
}
```

**Persistierung:**
```yaml
# Towny MetaData oder eigene Config
custom-names:
  plot-uuid-123: "Meine Handelsgilde"
  plot-uuid-456: "Zentral-Markt"
```

### Zusammenfassung Sprint 11-12

**Implementierte Features:**
1. ✅ TradeSet-System (Ankauf/Verkauf-Preise)
2. ✅ TradingEntity-Interface (Provider-Pattern)
3. ✅ TradeUI (Dynamisches Trading-Interface)
4. ✅ PlotRegistry (Auto-Registration via Towny)
5. ✅ Virtuelles Händler-Inventar (Persistent)
6. ✅ Slot-Verwaltungs-GUI (Händler platzieren)
7. ✅ NPC-Reisesystem (Verzögerung, Kosten, Restart-Handling)
8. ✅ NPC-Skin-Pool (Zufällige Skins)
9. ✅ Plot-Namen-Feature (Owner GUI + Listen)

**Architektur-Highlights:**
- **Provider-Pattern**: TradingEntity als Core-Interface
- **Graceful Degradation**: System funktioniert ohne Citizens
- **Persistence**: Alle Daten in Config gespeichert
- **Inter-Modul-Kommunikation**: PlotRegistry verbindet Plots und Händler
- **Restart-Safe**: NPC-Reisen überleben Server-Neustarts

**Nächste Schritte (Sprint 13-14):**
- Citizens-Integration (NPCProvider)
- Konkrete NPC-Implementierungen (GuildTrader, PlayerTrader, etc.)
- Denizen-Ersatz (natives Dialog-System)

---

## NPC-Modul: Geplante Features (Sprint 13-14)

### NPC-Typen

#### 1. Weltbankier NPC
**Funktion:** Globale Bank ohne Limits
- Sterne einzahlen → Vault-Guthaben
- Sterne auszahlen ← Vault-Guthaben
- **Kein Limit** für Transaktionen
- Verfügbar auf speziellen Admin-Plots

**Verwendung:**
```
/npc create weltbankier
Rechtsklick auf NPC → Banking-UI öffnet sich
```

#### 2. Lokaler Bankier NPC
**Funktion:** Bank mit eigenem Münzbestand
- Gehört zu einer spezifischen Bank (Plot-gebunden)
- Kann Sterne UND eigene Währung handeln
- **Eigener Münzbestand** (kann zur Neige gehen!)
- Verwendet Plot-Storage für Münzreserven

**Features:**
- Währungsumtausch (Sterne ↔ Lokale Währung)
- Münzbestand einsehbar (Owner)
- Automatische Nachfüllung via Plot-Storage

**Verwendung:**
```
/plot npc spawn bankier
/plot npc config bankier currency <währung>
Rechtsklick → Banking-UI (zeigt Münzbestand)
```

#### 3. Botschafter NPC
**Funktion:** Schnellreise-System
- Teleportiert Spieler zu anderen Botschaftern
- **Entgelt konfigurierbar** (Default: 100 Sterne)
- Preis wird vom Plot-Besitzer festgelegt
- Falls kein Plot → Standard-Config-Wert

**Features:**
- Liste aller verbundenen Botschafter
- Teleportations-Kosten variabel
- Integration mit Plot-Slots System (AMBASSADOR-Slots)

**Verwendung:**
```
/plot npc spawn botschafter
/plot npc price botschafter <preis>  # Default: 100 Sterne
Rechtsklick → Botschafter-Liste UI
```

#### 4. Gildenhändler NPC
**Funktion:** Automatischer Handelsgilde-Händler
- Wird über Handelsgilde-Plot erstellt
- Verkauft/Kauft zu Gilden-Preisen
- **Nutzt Plot-Storage** des Handelsgilde-Grundstücks
- Items aus Storage = verkaufbar

**Features:**
- Preise via `/plot price set` definiert
- Automatisches Inventar (Plot-Storage)
- Einnahmen → Plot-Storage
- Ausgaben ← Plot-Storage

**Verwendung:**
```
/plot npc spawn gildenhändler  # Nur auf Handelsgilde-Plots
Rechtsklick → Handelsgilde-Shop UI (Preisliste)
```

#### 5. Spielerhändler NPC
**Funktion:** Persönlicher Händler für Spieler
- Spieler kauft Händler-Slot auf Grundstück
- Spieler konfiguriert eigenen Shop
- Nutzt eigenes Inventar (nicht Plot-Storage)

**Features:**
- Kauf via `/plot gui` → "Händler kaufen"
- Slotten via `/plot slots` auf Grundstück
- Eigene Preise festlegbar
- Eigenes Inventar verwalten

**Verwendung:**
```
/plot gui  # Auf Gilde-Grundstück
→ "Händler kaufen" Button (kostet Sterne)
/plot slots  # Zeigt freie Händler-Slots
→ Händler auf Slot platzieren
/npc config myhändler inventory  # Inventar verwalten
```

---

## Plot-Slots System (Sprint 11-12)

### Konzept

**Slots sind Positionen auf Grundstücken, an denen NPCs platziert werden können.**

Dies ermöglicht:
- Feste NPC-Platzierung durch Grundstücksbesitzer
- Dynamische NPC-Platzierung (fahrende Händler, Handwerker)
- Slot-Verwaltung über UI
- Verschiedene Slot-Typen für verschiedene NPC-Arten

### Architektur

#### PlotSlot-Klasse

```java
package de.fallenstar.plot.slot;

public class PlotSlot {
    private final UUID slotId;           // Eindeutige Slot-ID
    private final Location location;     // Position des Slots
    private final SlotType slotType;     // Typ des Slots
    private UUID assignedNPC;            // Zugewiesener NPC (optional)
    private boolean active;              // Aktiv-Status

    public enum SlotType {
        TRADER("Händler"),
        BANKER("Bankier"),
        AMBASSADOR("Botschafter"),
        CRAFTSMAN("Handwerker"),
        TRAVELING_MERCHANT("Fahrender Händler")
    }

    // Methoden: assignNPC(), removeNPC(), isOccupied()
}
```

#### SlottedPlot-Interface

```java
package de.fallenstar.plot.slot;

public interface SlottedPlot extends Plot {
    // Slot-Verwaltung
    List<PlotSlot> getActiveSlots();
    List<PlotSlot> getAllSlots();
    Optional<PlotSlot> getSlot(UUID slotId);
    List<PlotSlot> getSlotsByType(PlotSlot.SlotType slotType);

    // Slot-Operationen
    boolean addSlot(PlotSlot slot);
    boolean removeSlot(UUID slotId);

    // Slot-Limits
    int getMaxSlots();
    int getUsedSlots();
    int getFreeSlots();
    boolean hasFreeSlots();
}
```

#### SlottedPlotForMerchants-Interface

```java
package de.fallenstar.plot.slot;

public interface SlottedPlotForMerchants extends SlottedPlot {
    // Händler-Slots
    int getTraderSlotAmount();
    List<PlotSlot> getTraderSlots();
    int getMaxTraderSlots();  // Default: 5

    // Bankier-Slots
    int getBankerSlotAmount();
    List<PlotSlot> getBankerSlots();
    int getMaxBankerSlots();  // Default: 2

    // Handwerker-Slots
    int getCraftsmanSlotAmount();
    List<PlotSlot> getCraftsmanSlots();
    int getMaxCraftsmanSlots();  // Default: 3
}
```

### Slot-Typen

| Slot-Typ | Verwendung | Max. Anzahl (Handelsgilde) |
|----------|------------|----------------------------|
| **TRADER** | Gildenhändler, Spielerhändler | 5 |
| **BANKER** | Lokale Bankiers (eigene Münzbestände) | 2 |
| **AMBASSADOR** | Botschafter-NPCs (Schnellreisen) | - |
| **CRAFTSMAN** | Handwerks-NPCs (Rüstungsschmied, etc.) | 3 |
| **TRAVELING_MERCHANT** | Fahrende Händler (selbstplatzierend) | - |

### Commands (geplant)

```bash
# Slot erstellen (Owner)
/plot slots create <typ>          # Erstellt Slot an aktueller Position

# Slots anzeigen
/plot slots list                  # Zeigt alle Slots + Status

# NPC slotten
/plot slots assign <slot-id> <npc-id>  # Weist NPC einem Slot zu

# Slot entfernen
/plot slots remove <slot-id>      # Entfernt Slot (nur wenn leer)

# Slot aktivieren/deaktivieren
/plot slots toggle <slot-id>      # Aktiviert/Deaktiviert Slot
```

### Use Cases

#### 1. Feste NPC-Platzierung (Owner)
```java
// Besitzer erstellt Händler-Slot an Position
PlotSlot slot = new PlotSlot(location, PlotSlot.SlotType.TRADER);
merchantPlot.addSlot(slot);

// Besitzer platziert NPC auf Slot
slot.assignNPC(npcUuid);
```

#### 2. Dynamische NPC-Platzierung (Traveling Merchants)
```java
// Fahrender Händler sucht freien Slot
SlottedPlotForMerchants plot = ...;
List<PlotSlot> freeSlots = plot.getTraderSlots().stream()
    .filter(slot -> !slot.isOccupied())
    .toList();

if (!freeSlots.isEmpty()) {
    PlotSlot slot = freeSlots.get(0);
    slot.assignNPC(travelingMerchantUuid);
    // NPC teleportiert sich auf Slot-Position
}
```

#### 3. Slot-Limits prüfen
```java
// Prüfe ob noch Händler-Slots verfügbar
if (plot.getTraderSlotAmount() < plot.getMaxTraderSlots()) {
    // Neuer Slot kann erstellt werden
}
```

### Integration mit HandelsgildeUI

```java
// HandelsgildeUI zeigt Slots im Owner-View
private void buildOwnerOptions() {
    // Slot 20: Händler-Slots verwalten
    ItemStack slotsButton = createButton(
        Material.ARMOR_STAND,
        "§6Händler-Slots",
        "§7Slots: " + plot.getUsedSlots() + "/" + plot.getMaxSlots()
    );

    setItem(20, slotsButton, player -> {
        // Öffne Slot-Management-UI
        openSlotManagementUI(player, plot);
    });
}
```

### Persistierung

Slots werden in der Plot-Config persistent gespeichert:

```yaml
# Plot-Config (Towny MetaData oder eigene Config)
slots:
  slot-1:
    uuid: "abc-123-def-456"
    type: TRADER
    location:
      world: "world"
      x: 100.5
      y: 64.0
      z: 200.5
      yaw: 0.0
      pitch: 0.0
    assigned-npc: "npc-uuid-789"
    active: true

  slot-2:
    uuid: "xyz-789-uvw-012"
    type: BANKER
    location: ...
    assigned-npc: null
    active: true
```

---

## UI-System: Guest vs. Owner Pattern

**Regel:** Jedes UI hat zwei Ansichten - für Besucher und für Besitzer.

### UI-Ansichten-Pattern

```java
public interface PlotUI {
    /**
     * Öffnet die Guest-Ansicht (read-only).
     *
     * @param player Der Spieler (Besucher)
     * @param plot Der Plot
     */
    void openGuestView(Player player, Plot plot);

    /**
     * Öffnet die Owner-Ansicht (read-write).
     *
     * @param player Der Spieler (Besitzer)
     * @param plot Der Plot
     */
    void openOwnerView(Player player, Plot plot);
}
```

### Automatische Ansichtswahl

```java
public void openPlotUI(Player player, Plot plot) {
    PlotProvider plotProvider = providers.getPlotProvider();

    try {
        if (plotProvider.isOwner(plot, player)) {
            // Besitzer → Owner-Ansicht (Verwaltung)
            ui.openOwnerView(player, plot);
        } else {
            // Gast → Guest-Ansicht (Nutzung)
            ui.openGuestView(player, plot);
        }
    } catch (Exception e) {
        // Fehler → Guest-Ansicht
        ui.openGuestView(player, plot);
    }
}
```

### Beispiel: Handelsgilde-UI

#### Guest-Ansicht (Besucher)
```
┌─────────────────────────────────┐
│    Handelsgilde - Shop          │
├─────────────────────────────────┤
│ [Item 1] - 10 Sterne   [Kaufen] │
│ [Item 2] - 25 Sterne   [Kaufen] │
│ [Item 3] - 50 Sterne   [Kaufen] │
│                                  │
│ [Schließen]                      │
└─────────────────────────────────┘
```
- **Read-Only:** Nur Preise sichtbar
- **Aktion:** Kaufen (falls Guthaben vorhanden)

#### Owner-Ansicht (Besitzer)
```
┌─────────────────────────────────┐
│  Handelsgilde - Verwaltung      │
├─────────────────────────────────┤
│ [Item 1] - 10 ⭐ [Preis ändern]  │
│ [Item 2] - 25 ⭐ [Preis ändern]  │
│ [Item 3] - 50 ⭐ [Preis ändern]  │
│                                  │
│ [Händler verwalten]              │
│ [Storage anzeigen]               │
│ [Schließen]                      │
└─────────────────────────────────┘
```
- **Read-Write:** Preise änderbar
- **Extra-Features:** Händler-Verwaltung, Storage-Zugriff

### UI-Implementierung

```java
public class HandelsgildeUI extends LargeChestUI {

    public void openGuestView(Player player, Plot plot) {
        setTitle("Handelsgilde - Shop");

        // Zeige nur Verkaufs-Items
        loadShopItems(plot);

        // Kaufen-Buttons
        addBuyButtons();

        // Keine Verwaltungs-Optionen
        open(player);
    }

    public void openOwnerView(Player player, Plot plot) {
        setTitle("Handelsgilde - Verwaltung");

        // Zeige Items + Preise
        loadShopItems(plot);

        // Preis-Ändern-Buttons
        addPriceEditButtons();

        // Verwaltungs-Optionen
        addManagementButtons();

        open(player);
    }
}
```

### Best Practices

1. ✅ **Immer Owner-Check:** Vor `openOwnerView()` prüfen
2. ✅ **Fallback zu Guest:** Bei Fehler → Guest-Ansicht
3. ✅ **Unterschiedliche Items:** Owner sieht mehr Optionen
4. ✅ **UI-Titel unterscheiden:** "Shop" vs. "Verwaltung"
5. ✅ **Permissions:** Owner-Buttons nur für Besitzer anzeigen

---

**Last Updated:** 2025-11-17
**Repository:** fs-core-sample-dump
**Branch:** claude/integrate-vault-economy-01BK4oPAgZ6Eutu9QZsJTv2h
**Version:** 1.0-SNAPSHOT
**Sprint Status:** Sprint 9-10 ✅ **ABGESCHLOSSEN** (Economy: CurrencyManager ✅, VaultEconomyProvider ✅, Withdraw ✅, Reflection eliminiert ✅, Plot Storage Integration ✅, Owner-Checks ✅)
**Architektur:** Command-Handler-Registry-Pattern (kein Reflection mehr!)
**Build Status:** ✅ Alle Module kompilieren erfolgreich (Core, Plots, Items, UI, Economy)
**Testbefehle:** `/fscore admin [gui/items/plots/economy]` - Handler-basierte Struktur aktiv
**Währungssystem:** Kupferbarren/Eisenbarren/Goldbarren (COPPER_INGOT/IRON_INGOT/GOLD_INGOT, Custom Model Data 1/2/3 für Resource Pack)
