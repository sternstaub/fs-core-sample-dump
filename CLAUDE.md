# CLAUDE.md - AI Assistant Guide

**FallenStar Paper Core - Modular Plugin System**

Kompakter Guide für AI-Assistenten. Für Details siehe jeweilige README-Dateien der Module.

---

## Language Conventions / Sprachkonventionen

### 🇩🇪 **WICHTIG: Dieses Projekt verwendet Deutsch als primäre Sprache**

**Für AI-Assistenten:**
- **Kommunikation:** Immer auf Deutsch
- **Code-Kommentare & Javadoc:** Deutsch
- **Commit Messages:** Deutsch
- **Code selbst:** Englisch (Klassen-, Methoden-, Variablennamen)
- **Technische Begriffe:** Englisch (Provider, Registry, Plugin, etc.)

**Beispiel:**
```java
/**
 * Initialisiert die Provider-Registry.
 */
private void initializeProviders() {
    // Registry erstellen und Provider erkennen
    providerRegistry = new ProviderRegistry(getLogger());
}
```

### 📚 README-Aktualisierungs-Regel

**Bei JEDER Änderung:**
- Prüfe betroffene README-Dateien (`/README.md`, `core/README.md`, `module-*/README.md`)
- Aktualisiere Features, APIs, Code-Beispiele, Sprint-Status

---

## Project Overview

### Was ist das?

**Modulares Minecraft-Plugin-System** für Paper 1.21.1 mit Provider-basierter Architektur.

**Technologie:**
- Paper/Spigot API 1.21.1, Java 21, Maven (Multi-Module)
- SQLite (primary), MySQL (geplant)
- Optional: Towny, Vault, Citizens, MMOItems

**Ziele:**
1. Provider-Abstraction: Entkopplung von externen Plugins
2. Graceful Degradation: Features deaktivieren sich automatisch bei fehlenden Dependencies
3. Modulares Design: Module abhängig nur von Core
4. AI-freundlich: Sprint-basierte Entwicklung

### Status

- **Version:** 1.0-SNAPSHOT
- **Phase:** Aktive Entwicklung (~55%)
- **Abgeschlossen:** Core ✅, Plots ✅, Items ✅, UI ✅, Economy ✅
- **Aktuell:** Sprint 11-12 (Trading, NPC-GUI ✅)
- **Nächster:** Sprint 13-14 (Citizens-Integration)

---

## Codebase Structure

### Module

```
fs-core-sample-dump/
├── core/                   # Core Plugin (Provider-Interfaces, NoOp-Implementierungen)
├── module-plots/           # Plot-System + Storage (Towny-Integration)
├── module-items/           # MMOItems-Wrapper
├── module-ui/              # UI-Framework Erweiterungen
├── module-economy/         # Währungssystem, Vault-Integration
└── module-npcs/            # NPC-System (geplant)
```

### Dependency Graph

```
Core (Interfaces + NoOp + UI-Framework)
 ↑
 ├── Plots (Towny → TownyPlotProvider)
 ├── Items (MMOItems → MMOItemsItemProvider)
 ├── UI (ConfirmationUI, SimpleTradeUI, UIButtonManager)
 ├── Economy (Vault → VaultEconomyProvider)
 └── NPCs (Citizens → CitizensNPCProvider)
```

**Wichtig:** Module kommunizieren NUR über Core-Interfaces, nie direkt untereinander!

---

## Architecture Patterns

### 1. Provider Pattern

**Core enthält NUR Interfaces:**
```java
public interface PlotProvider {
    boolean isAvailable();
    Plot getPlot(Location location) throws ProviderFunctionalityNotFoundException;
}
```

**Module implementieren Provider:**
```
module-plots/provider/TownyPlotProvider.java  ← Implementiert PlotProvider
```

**Verwendung:**
```java
// ✅ RICHTIG: Via Core-Interface
PlotProvider provider = registry.getPlotProvider();
if (provider.isAvailable()) {
    Plot plot = provider.getPlot(location);
}

// ❌ FALSCH: Direkter Zugriff
import com.palmergames.bukkit.towny.*;  // NIEMALS!
```

### 2. NoOp Pattern

**Fallback wenn Plugin fehlt:**
```java
public class NoOpPlotProvider implements PlotProvider {
    @Override
    public boolean isAvailable() { return false; }

    @Override
    public Plot getPlot(Location location) {
        throw new ProviderFunctionalityNotFoundException(...);
    }
}
```

### 3. Graceful Degradation

```java
private boolean plotBasedStorageEnabled = false;

private void checkOptionalFeatures() {
    if (plotProvider.isAvailable()) {
        plotBasedStorageEnabled = true;
    }
}

public void someFeature() {
    if (plotBasedStorageEnabled) {
        // Use plot-based logic
    } else {
        // Use fallback
    }
}
```

### 4. Event-Driven Initialization

**Core feuert Event:**
```java
ProvidersReadyEvent event = new ProvidersReadyEvent(providerRegistry);
Bukkit.getPluginManager().callEvent(event);
```

**Module lauschen:**
```java
@EventHandler
public void onProvidersReady(ProvidersReadyEvent event) {
    this.providers = event.getRegistry();
    initializeModule();
}
```

### 5. Data Persistence

**Regel:** Alle Daten SOFORT nach Änderung speichern!

```java
public void setData(String key, Data data) {
    dataMap.put(key, data);
    plugin.saveConfiguration();  // Sofort speichern!
}

// In Plugin:
public void saveConfiguration() {
    dataManager.saveToConfig(getConfig());  // In-Memory → Config
    saveConfig();  // Config → Festplatte
}
```

**Empfohlen:** Plot-bezogene Daten in **Towny MetaData** statt Config:
```java
// Persistenter als Config, Plot-gebunden
townBlock.addMetaData(new StringDataField("fs_plot_name", name), true);
```

### 6. Command-Handler-Registry (Reflection eliminiert!)

**Alte Methode (❌):**
```java
// Reflection-Aufrufe → Runtime-Errors
plugin.getClass().getMethod("someMethod").invoke(plugin);
```

**Neue Methode (✅):**
```java
// Handler-Pattern → Type-Safe
public interface AdminSubcommandHandler {
    boolean handle(CommandSender sender, String[] args);
}

// Module registrieren Handler
registry.registerHandler("economy", new EconomyAdminHandler(manager));

// Core delegiert
registry.getHandler("economy").ifPresent(h -> h.handle(sender, args));
```

---

## Code Conventions

### Package Structure
```
de.fallenstar.<module>/
├── <Module>Main.java
├── command/
├── manager/
├── model/
└── listener/ oder gui/ oder task/
```

### Naming
- Klassen: `PascalCase`
- Methoden: `camelCase`
- Konstanten: `UPPER_SNAKE_CASE`
- **Akronyme:** Im Code als `camelCase` (UI → Ui, NPC → Npc, GS → Gs)

### Javadoc (Deutsch!)
```java
/**
 * Lädt Daten aus der Config.
 *
 * @param config Die Config
 * @throws IOException bei Fehlern
 */
public void loadFromConfig(FileConfiguration config) throws IOException {
    // ...
}
```

---

## Development Workflow

### Build & Testing (für AI)

**WICHTIG:** AI-Assistenten kompilieren **NICHT**!

**Workflow:**
1. Code vollständig implementieren
2. Git commit + push
3. User testet lokal

**Best Practices:**
- ✅ Code vollständig (keine Platzhalter)
- ✅ Syntax-korrekt (Imports, etc.)
- ✅ Sofort pushen
- ✅ Klare Commit-Messages

```bash
git add -A
git commit -m "Feature: XYZ implementiert"
git push -u origin <branch-name>
```

### Sprint-Übersicht

| Sprint | Modul | Status |
|--------|-------|--------|
| 1-2 | Core + UI Framework | ✅ |
| 3-4 | Plots (inkl. Storage) | ✅ |
| 5-6 | Items (MMOItems) | ✅ |
| 7-8 | UI-Modul | ✅ |
| 9-10 | Economy | ✅ |
| 11-12 | Trading + NPC-GUI | 🔨 |
| 13-14 | NPCs (Citizens) | 📋 |
| 15-20 | Chat, Auth, WebHooks | 📋 |

### Testbefehle

```bash
/fscore admin gui <ui-id>       # UI testen
/fscore admin items <cmd>       # Items testen
/fscore admin plots <cmd>       # Plots testen
/fscore admin economy <cmd>     # Economy testen
```

---

## Anti-Patterns (Vermeiden!)

### ❌ Reflection

**Problem:** Runtime-Errors, keine IDE-Unterstützung

**Vermeiden:**
```java
// ❌ FALSCH
Method m = plugin.getClass().getMethod("someMethod");
m.invoke(plugin);
```

**Stattdessen:**
1. **Provider-Pattern** (bevorzugt)
2. **Direct Dependency** (wenn akzeptabel)
3. **Service Registry**
4. **Reflection** (nur als letztes Mittel!)

### ❌ Null-Parameter

```java
// ❌ FALSCH
public ClickableUiElement(UiAction action) {
    this.action = action;  // NPE-Risiko!
}

// ✅ RICHTIG
public ClickableUiElement(UiAction action) {
    this.action = Objects.requireNonNull(action);
}
```

### ❌ Setter für finale Dependencies

```java
// ❌ FALSCH
private UiAction action;
public void setAction(UiAction action) { ... }

// ✅ RICHTIG
private final UiAction action;
public ClickableUiElement(UiAction action) { ... }
```

---

## Important Patterns

### Owner-Berechtigungen

**Regel:** Plot-Befehle erfordern Owner-Check!

```java
private boolean isPlotOwner(Player player, Plot plot) {
    try {
        return plotProvider.isOwner(plot, player);
    } catch (Exception e) {
        return false;
    }
}

// Vor Owner-exklusiven Aktionen:
if (!isPlotOwner(player, plot)) {
    player.sendMessage("§cDu musst der Besitzer sein!");
    return;
}
```

### Guest vs. Owner UI

```java
public void openPlotUI(Player player, Plot plot) {
    if (isPlotOwner(player, plot)) {
        ui.openOwnerView(player, plot);  // Verwaltung
    } else {
        ui.openGuestView(player, plot);   // Nutzung
    }
}
```

### Type-Safe UI-System

```java
// UI-Element MUSS Action haben (Compiler-Check!)
public sealed class ClickableUiElement<T extends UiAction> {
    private final T action;

    protected ClickableUiElement(T action) {
        this.action = Objects.requireNonNull(action);
    }
}

// Verwendung
var action = new TeleportToPlotAction(plot);
var button = new ClickableUiElement.CustomButton<>(item, action);
```

---

## Current Sprint (11-12)

### Implementierte Features

1. ✅ **TradeSet-System** (Economy-Modul)
2. ✅ **TradingEntity-Interface** (Core)
3. ✅ **TradeUI** (UI-Modul)
4. ✅ **PlotRegistry** (Plots-Modul)
5. ✅ **Virtuelles Händler-Inventar** (Plots-Modul)
6. ✅ **NPC-Verwaltungs-GUI** (Plots-Modul)
   - Owner-View: Alle NPCs auf Plot
   - Spieler-View: Nur eigene NPCs
   - Plot-gebundene NPCs (IMMER!)

### Geplant (Sprint 13-14)

- Citizens-Integration (NPCProvider)
- NPC-Typen: Weltbankier, Lokaler Bankier, Botschafter, Gildenhändler, Spielerhändler
- Slot-System (NPC-Platzierung)
- NPC-Reisesystem

---

## Quick Reference

### Wichtige Dateien

| Datei | Zweck |
|-------|-------|
| `README.md` | Haupt-Dokumentation |
| `core/README.md` | Core-Plugin Details |
| `module-*/README.md` | Modul-spezifische Docs |
| `CLAUDE.md` | Dieser Guide |

### Build

```bash
mvn clean package              # Alle Module
cd core/ && mvn clean package  # Einzelnes Modul
```

### Git

```bash
git status
git add -A
git commit -m "Message"
git push -u origin <branch>
```

---

## Summary

**Wichtigste Regeln:**
1. ✅ **Deutsch** für Doku, Kommentare, Kommunikation
2. ✅ **Provider-Pattern** für externe Plugins
3. ✅ **Graceful Degradation** bei fehlenden Dependencies
4. ✅ **Sofort speichern** nach Daten-Änderungen
5. ✅ **Owner-Checks** vor Plot-Verwaltung
6. ✅ **Type-Safety** (final fields, Objects.requireNonNull)
7. ✅ **Kein Reflection** (Handler-Pattern stattdessen)

**Architektur-Mantra:**
> "Core = Interfaces, Module = Implementierungen, Kommunikation = Provider"

**Development-Mantra:**
> "Code → Commit → Push → User testet"

---

**Last Updated:** 2025-11-17
**Version:** 1.0-SNAPSHOT
**Sprint:** 11-12 (Trading + NPC-GUI ✅)
**Branch:** claude/migrate-storage-price-ui-01XKnojmCKHCGNiSBZgzUsro
