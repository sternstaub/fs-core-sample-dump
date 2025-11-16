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
- **Completion:** ~15% (Core ✅ + Plots ✅)
- **Nächster Sprint:** Sprint 1-2 Erweiterung - UI Provider Interface in Core
- **Dann:** Sprint 5-6 - UI-Modul (natives Rendering)
- **Wichtige Architektur:** Provider-Implementierungen in Modulen, Core nur Interfaces!
- **Neue Planung:** 20 Sprints (40 Wochen) mit UI-System, Chat, Auth, WebHooks
- **Storage-Modul:** ✅ Entfernt (redundant, in Plots integriert)
- **Denizen-Ersatz:** 📋 Geplant (natives NPC-System mit UI)

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
├── module-items/                    # FallenStar Items (Sprint 5-6)
│   ├── pom.xml                      # Custom Items, MMOItems-Integration
│   ├── src/main/java/de/fallenstar/items/
│   │   ├── ItemsModule.java                   # Main class
│   │   ├── provider/                          # Provider-Implementierungen
│   │   │   └── MMOItemsItemProvider.java      # MMOItems-Integration
│   │   ├── command/                           # Item-Befehle
│   │   ├── manager/                           # Item-Manager
│   │   ├── model/                             # Item-Modelle
│   │   └── factory/                           # Item-Factory
│   └── src/main/resources/
│       ├── plugin.yml
│       └── config.yml
│
├── module-economy/                  # FallenStar Economy (Sprint 7-8)
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
├── module-worldanchors/             # FallenStar WorldAnchors (Sprint 9-10)
│   ├── pom.xml                      # Schnellreisen, POIs, Wegpunkte
│   ├── src/main/java/de/fallenstar/worldanchors/
│   │   ├── WorldAnchorsModule.java            # Main class
│   │   ├── command/                           # Reise-Befehle
│   │   ├── manager/                           # Reise-Manager
│   │   ├── model/                             # POI-Modelle
│   │   └── task/                              # Reise-Tasks
│   └── src/main/resources/
│       ├── plugin.yml
│       └── config.yml
│
├── module-npcs/                     # FallenStar NPCs (Sprint 11-12)
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
Core (UI Provider Interface + Native Fallback + alle Interfaces)
 ↑
 ├── UI               (Natives UI-Rendering, registriert NativeUIProvider)
 ├── Plots            (Plot-System + Storage ✅, Towny → TownyPlotProvider)
 ├── Items            (Custom Items, MMOItems, nutzt UIProvider)
 ├── Economy          (Weltwirtschaft, Vault, nutzt UIProvider)
 ├── WorldAnchors     (Schnellreisen, POIs, Wegpunkte)
 ├── NPCs             (NPC-System, Denizen-Ersatz, nutzt UIProvider + PlotProvider)
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

---

## Development Workflow

### Sprint-Based Development

Das Projekt folgt einem 20-Sprint-Fahrplan (40 Wochen):

| Sprint | Module | Duration | Status | Beschreibung |
|--------|--------|----------|--------|--------------|
| **1-2** | **Core + UI Provider Interface** | 2 Wochen | ✅ / 📋 | Core abgeschlossen, UI Provider Interface hinzufügen |
| **3-4** | **Plots (inkl. Storage)** | 2 Wochen | ✅ | Plot-System + Storage-Integration (fertig) |
| **5-6** | **UI-Modul** | 2 Wochen | 📋 | Natives UI-Rendering (Text, Chat, Inventory, Books) |
| **7-8** | **Items** | 2 Wochen | 📋 | Custom Items mit UI-Integration |
| **9-10** | **Economy** | 2 Wochen | 📋 | Weltwirtschaft mit UI-Integration |
| **11-12** | **WorldAnchors** | 2 Wochen | 📋 | Schnellreisen, POIs, Wegpunkte |
| **13-14** | **NPCs** | 2 Wochen | 📋 | NPC-System mit UI, Denizen-Ersatz |
| **15-16** | **Chat** | 2 Wochen | 📋 | Matrix-Bridge, globaler Chat |
| **17-18** | **Auth** | 2 Wochen | 📋 | Keycloak-Integration, SSO |
| **19-20** | **WebHooks** | 2 Wochen | 📋 | Wiki/Forum-Integration |

**Legende:**
- ✅ Abgeschlossen
- 🔨 In Arbeit
- 📋 Geplant

**Wichtige Architektur-Änderungen:**
- **Core** enthält nur Interfaces + NoOp-Implementierungen + natives UI-Fallback
- **Provider-Implementierungen** liegen in den jeweiligen Modulen
- **Module** kommunizieren NUR über Core-Interfaces
- **Storage-Modul** ❌ entfernt (redundant, in Plots integriert)
- **UI-Provider-System** ✅ neu (Interface + NativeTextUIProvider in Core)
- **Denizen-Ersatz** 📋 natives NPC-Dialog-System im NPCs-Modul
- **Neue Module:** UI (Sprint 5-6), Chat (15-16), Auth (17-18), WebHooks (19-20)
- **Sprint-Umplanung:** Items verschoben von 5-6 → 7-8, Economy 7-8 → 9-10, etc.

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
- `module-economy/target/FallenStar-Economy-1.0.jar`
- `module-worldanchors/target/FallenStar-WorldAnchors-1.0.jar`
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

**Last Updated:** 2025-11-16
**Repository:** fs-core-sample-dump
**Branch:** claude/restructure-project-modules-018sEM2NT9pJcUDj7CmmeWTC
**Version:** 1.0-SNAPSHOT
**Architektur:** Provider-Implementierungen in Modulen, Core nur Interfaces + NoOp
**Modulstruktur:** Items-Modul vor Economy eingefügt (Sprint 5-6)
