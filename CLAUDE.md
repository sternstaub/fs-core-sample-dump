# CLAUDE.md - AI Assistant Guide

**FallenStar Paper Core - Modular Plugin System**

This document provides comprehensive guidance for AI assistants working on this codebase.

---

## Table of Contents

1. [Project Overview](#project-overview)
2. [Codebase Structure](#codebase-structure)
3. [Architecture & Design Patterns](#architecture--design-patterns)
4. [Development Workflow](#development-workflow)
5. [Code Conventions](#code-conventions)
6. [Common Tasks](#common-tasks)
7. [Testing Guidelines](#testing-guidelines)
8. [Important Files Reference](#important-files-reference)

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
- **Phase:** Sample Development / Sprint Planning
- **Completion:** ~40% (Core samples + Storage module partial)
- **Next Sprint:** Sprint 1 - Core Implementation

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
│   │   │   │   ├── NetworkProvider.java
│   │   │   │   ├── Plot.java                  # Data model
│   │   │   │   └── impl/                      # Concrete implementations
│   │   │   │       ├── TownyPlotProvider.java
│   │   │   │       ├── NoOpPlotProvider.java
│   │   │   │       ├── NoOpEconomyProvider.java (missing)
│   │   │   │       ├── NoOpNPCProvider.java (missing)
│   │   │   │       ├── VaultEconomyProvider.java (missing)
│   │   │   │       └── CitizensNPCProvider.java (missing)
│   │   │   ├── registry/
│   │   │   │   └── ProviderRegistry.java      # Auto-detects providers
│   │   │   ├── exception/
│   │   │   │   └── ProviderFunctionalityNotFoundException.java
│   │   │   ├── event/
│   │   │   │   └── ProvidersReadyEvent.java
│   │   │   └── database/
│   │   │       ├── DataStore.java             # Interface
│   │   │       └── impl/                      # (missing implementations)
│   │   └── resources/
│   │       ├── plugin.yml
│   │       └── config.yml
│
├── module-storage/                  # Storage Module (Sprint 3)
│   ├── pom.xml
│   ├── src/main/java/de/fallenstar/storage/
│   │   ├── StorageModule.java                 # Main class
│   │   ├── command/
│   │   │   └── StorageRegisterCommand.java
│   │   ├── manager/                           # (to be implemented)
│   │   ├── model/                             # (to be implemented)
│   │   └── listener/                          # (to be implemented)
│   └── src/main/resources/
│       ├── plugin.yml
│       └── config.yml
│
├── module-merchants/                # Merchants Module (Sprint 4-5)
├── module-travel/                   # Travel Module (Sprint 8-9)
├── module-adminshops/               # AdminShops Module (Sprint 6-7)
│
└── Documentation Files (*.md)
```

### Module Dependency Graph

```
Core (Foundation - NO business logic)
 ↑
 ├── Storage          (Chest management)
 ├── Merchants        (NPC trading)
 ├── AdminShops       (Template-based shops)
 └── TravelSystem     (Traveling merchants)
```

**Important:** Modules **ONLY** depend on Core, never on each other.

---

## Architecture & Design Patterns

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

The project follows a 10-sprint roadmap:

| Sprint | Module | Duration | Status |
|--------|--------|----------|--------|
| 1-2 | Core | 2 weeks | In Planning |
| 3 | Storage | 1 week | Partial |
| 4-5 | Merchants | 2 weeks | Planned |
| 6-7 | AdminShops | 2 weeks | Planned |
| 8-9 | Travel | 2 weeks | Planned |
| 10 | Polish | 1 week | Planned |

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
- `module-storage/target/FallenStar-Storage-1.0.jar`
- etc.

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
| `StorageModule.java` | `module-storage/src/main/java/.../storage/` | Example module main class |

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
| New Module Main Class | `StorageModule.java` |
| New Command | `StorageRegisterCommand.java` |
| New Module README | `module-storage/README.md` |

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
"Implement Storage Module" →
  1. Implement StorageListCommand
  2. Implement StorageInfoCommand
  3. Implement ChestManager
  4. Implement MaterialTracker
  5. Implement ChestInteractListener
  6. Create config.yml
  7. Test functionality
```

### Code Generation Best Practices

1. **Always follow existing patterns** - Don't invent new structures
2. **Copy Javadoc style** from templates
3. **Use German or English** consistently (match existing files)
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

1. ✅ **Understand the provider pattern** - it's the foundation
2. ✅ **Follow sprint-based approach** - one module at a time
3. ✅ **Use templates** - don't reinvent patterns
4. ✅ **Document heavily** - Javadoc everything
5. ✅ **Test incrementally** - build after each class
6. ✅ **Check SETUP_COMPLETE.md** - know what's missing
7. ✅ **Reference existing code** - maintain consistency

**Key Architecture Principle:**
> "Modules depend on Core, Core provides providers, providers abstract external plugins, NoOp providers enable graceful degradation."

**Development Mantra:**
> "Sprint → Template → Implement → Document → Test → Commit"

---

**Last Updated:** 2025-11-15
**Repository:** fs-core-sample-dump
**Branch:** claude/claude-md-mi0sco9raq2ajdr6-01Tte3UhY6FdvsCXyqqVvJ8k
**Version:** 1.0-SNAPSHOT
