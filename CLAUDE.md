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
- **Phase:** Aktive Entwicklung (~85%)
- **Abgeschlossen:**
  - Core ✅ (Provider, UI-Framework, Interaction System, Distributor Pattern)
  - Plots ✅ (TradeguildPlot, DataStore-Persistenz, InteractionRegistry)
  - Items ✅ (Vanilla Coins, MMOItems-Support)
  - Economy ✅ (Vault-Integration, TradeSet-System)
  - Trading-System ✅ (TradeUI, TradingEntity)
  - NPCs ✅ (GuildTraderNpcEntity, DistributableNpc, QuestContainer)
- **Aktuell:** Sprint 15+ (Quest-System, Chat, Auth, WebHooks 📋)
- **Nächster:** Production-Deployment

---

## Codebase Structure

### Module

```
fs-core-sample-dump/
├── core/                   # Core Plugin (Provider-Interfaces, NoOp, UI-Framework)
├── module-plots/           # Plot-System + Storage (Towny-Integration)
├── module-items/           # Vanilla Currency Items + Optional MMOItems
├── module-economy/         # Währungssystem, Vault-Integration, TradeSet-System
└── module-npcs/            # NPC-System + Händler-NPCs (Citizens-Integration)
```

### Dependency Graph

```
Core (Interfaces + NoOp + UI-Framework inkl. TradeUI)
 ↑
 ├── Plots (Towny → TownyPlotProvider, VirtualTraderInventory, PlotRegistry)
 ├── Items (Vanilla Coins, Optional: MMOItems → MMOItemsItemProvider)
 ├── Economy (Vault → VaultEconomyProvider, TradeSet-System)
 └── NPCs (Citizens → CitizensNPCProvider, Händler-NPCs)
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

#### Klassen
- **PascalCase**
- **Akronyme:** Im Code als `camelCase` (UI → Ui, NPC → Npc, GS → Gs)
  ```java
  UiAction          // ✅ RICHTIG
  UIAction          // ❌ FALSCH
  NpcManager        // ✅ RICHTIG
  NPCManager        // ❌ FALSCH
  ```

#### Vererbungshierarchie erkennbar machen

**WICHTIG:** Die Vererbungshierarchie MUSS aus dem Klassennamen erkennbar sein,
wenn Klassen alphabetisch sortiert werden!

**Regel: Prefix-basierte Benennung**

```java
// ✅ RICHTIG: Hierarchie alphabetisch erkennbar
PlotAction.java                     // Abstract base class
PlotActionManageNpcs.java          // extends PlotAction
PlotActionSetName.java             // extends PlotAction
PlotActionSetPrice.java            // extends PlotAction

UiAction.java                       // Interface
UiElement.java                      // Abstract base
UiElementButton.java               // extends UiElement
UiElementContainer.java            // extends UiElement

// ❌ FALSCH: Hierarchie nicht erkennbar
ManageNpcsAction.java              // Erweitert PlotAction? Unklar!
PlotAction.java                    // Basis? Unklar!
SetNameAction.java                 // Erweitert PlotAction? Unklar!
```

**Begründung:**
- Bei alphabetischer Sortierung steht die Basisklasse VOR den Subklassen
- Verwandte Klassen sind gruppiert
- Vererbung ist auf einen Blick erkennbar
- Keine IDE nötig um Hierarchie zu verstehen

**Ausnahmen:**
- Interfaces: Kein "I" Prefix (Java Convention)
  ```java
  UiAction          // ✅ Interface
  IUiAction         // ❌ FALSCH (C# Convention)
  ```
- Sehr spezifische, einmalige Klassen ohne Hierarchie
- Standard Java Klassen (z.B. `ArrayList`, `HashMap`)

#### Methoden & Variablen
- Methoden: `camelCase`
- Variablen: `camelCase`
- Konstanten: `UPPER_SNAKE_CASE`

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
| 5-6 | Items (Vanilla Coins + MMOItems) | ✅ |
| 7-8 | UI-Refactoring (in Core integriert) | ✅ |
| 9-10 | Economy | ✅ |
| 11-12 | Trading + NPC-GUI | ✅ |
| 13-14 | NPCs (Citizens + Händler-NPCs) | ✅ |
| 15 | Interaction System + Distributor Pattern | ✅ |
| 16 | DataStore-Integration + Persistenz | ✅ |
| 17+ | Quest-System, Chat, Auth, WebHooks | 📋 |

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

### ❌ Vererbungshierarchie nicht erkennbar

**Problem:** Klassennamen zeigen nicht die Hierarchie, schwer wartbar

```java
// ❌ FALSCH: Alphabetisch sortiert - Hierarchie unklar
ManageNpcsAction.java          // extends PlotAction? Unklar!
PlotAction.java                // Basis-Klasse
SetNameAction.java             // extends PlotAction? Unklar!
ViewPricesAction.java          // extends UiAction? PlotAction? Unklar!

// ✅ RICHTIG: Alphabetisch sortiert - Hierarchie klar
PlotAction.java                // Basis-Klasse (zuerst!)
PlotActionManageNpcs.java      // extends PlotAction ✓
PlotActionSetName.java         // extends PlotAction ✓
UiAction.java                  // Interface
UiActionViewPrices.java        // extends UiAction ✓
```

**Begründung:**
- Code-Reviews: Schnelles Verständnis
- Refactoring: Leichter zu finden welche Klassen betroffen sind
- Onboarding: Neue Entwickler verstehen Struktur sofort

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

## Current Sprint (15-17)

### Abgeschlossen (Sprint 13-16)

**Sprint 13-14: NPCs-Modul - Citizens-Integration + Gildenhändler**
1. ✅ CitizensNPCProvider (NPCProvider-Implementierung)
2. ✅ GuildTraderNPC (Gildenhändler - PlotBoundNPC mit ItemBasePriceProvider)
3. ✅ GuildTraderNpcEntity (DistributableNpc + QuestContainer + UiTarget)
4. ✅ NPCManager + GuildTraderManager
5. ✅ Admin-Befehle (/fscore admin npc)

**Sprint 15: Interaction System + Distributor Pattern**
1. ✅ Interactable Interface (Click-Handler für Entities/Plots)
2. ✅ UiTarget Interface (Self-Constructing UIs)
3. ✅ InteractionRegistry + InteractionHandler (Event-Routing)
4. ✅ Distributor<T> + Distributable (Generisches Verteilungssystem)
5. ✅ NpcDistributor + QuestDistributor (Automatische Content-Verteilung)
6. ✅ TradeguildPlot implements NpcDistributor + QuestDistributor
7. ✅ GenericInteractionMenuUi (Self-Constructing UI aus UiActionInfo)

**Sprint 16: DataStore-Integration + Persistenz**
1. ✅ TradeguildPlotData (Serialisierbares POJO für Persistierung)
2. ✅ TradeguildPlot.exportData() / importData()
3. ✅ TradeguildPlotFactory mit DataStore-Integration
4. ✅ Lazy Loading (Auto-Load beim ersten Zugriff)
5. ✅ Auto-Save beim Server-Shutdown

### Aktuell in Arbeit (Sprint 17+)

**Quest-System:**
- 📋 Quest-UI (GenericInteractionMenuUi-basiert)
- 📋 Quest-Manager
- 📋 Quest-Persistierung

**Chat-System:**
- 📋 Chat-Provider Interface
- 📋 Channel-System

**Auth-System:**
- 📋 Authentication-Provider
- 📋 Session-Management

**WebHooks:**
- 📋 Event-Streaming zu externen Services

---

## NPC-Bindungssystem (Design-Konzept)

### Übersicht

NPCs können an **Plots** oder **Spieler** gebunden werden. Die Bindung bezieht sich auf **Verwaltung/Registrierung/Speicherung**, NICHT auf die physische Position!

### PlotBoundNPC (Plot-gebundene NPCs)

**Konzept:**
- NPCs gehören zu einem Grundstück (z.B. Gildenhändler)
- Werden über das Plot verwaltet, gespeichert, geladen
- Können auf andere Grundstücke geschickt werden (z.B. Trader Slots auf Marktplatz)
- Nur Plot-Owner kann sie verwalten

**Implementierung:**
```java
// PlotBoundNPCRegistry (Plots-Modul)
registry.registerNPC(plotId, npcId, npcType);
List<UUID> npcs = registry.getNPCsForPlot(plotId);
```

**Beispiele:**
- **GuildTraderNPC** - Händler auf Handelsgilde
- **Wächter-NPC** - Verteidigung (später)
- **Crafter-NPC** - Handwerk (später)

**Verwaltung:**
- Owner-UI: NPCs spawnen, entfernen, konfigurieren
- Guest-UI: Nur interagieren (Trading)

### PlayerBoundNPC (Spieler-gebundene NPCs)

**Konzept:**
- Spieler KAUFT NPC auf speziellem Grundstück (z.B. Handelsgilde)
- NPC wird an Spieler gebunden
- Spieler kann NPC auf eigene Grundstücke platzieren
- Wird über Spieler-UUID verwaltet/gespeichert

**Implementierung (geplant für später):**
```java
// PlayerBoundNPCRegistry (NPCs-Modul)
registry.registerNPC(playerUuid, npcId, npcType);
List<UUID> npcs = registry.getNPCsForPlayer(playerUuid);
```

**Beispiele:**
- **PlayerTraderNPC** - Privater Händler
- **Butler-NPC** - Helfer (später)

**Kauf-Mechanik:**
- Handelsgilde zeigt Guest-UI: "Händler kaufen"
- Spieler zahlt (Economy-Integration)
- NPC wird an Spieler gebunden
- Spieler platziert NPC auf eigenem Grundstück

### NPC-Positions-System (Trader Slots)

**Wichtig:** Bindung ≠ Position!

**PlotBoundNPC kann reisen:**
```
Gildenhändler (PlotBound zu Handelsgilde A)
  → Wird auf Marktplatz-Slot platziert
  → Verwaltet/gespeichert über Handelsgilde A
  → Position: Marktplatz
```

**Trader Slots:**
- Grundstücke haben NPC-Slots (bereits implementiert)
- PlotBoundNPCs können auf fremde Slots platziert werden
- Registrierung bleibt beim Ursprungs-Plot

### ItemBasePriceProvider-System

**Konzept:**
- Handelsgilde-Grundstücke SIND ItemBasePriceProvider
- Preise werden PRO GRUNDSTÜCK festgelegt
- Ankauf UND Verkauf getrennt

**Implementierung:**
```java
// PlotPriceManager (Plots-Modul)
public class PlotPriceManager implements ItemBasePriceProvider {
    // Preise pro Plot
    Map<UUID, PlotPriceData> plotPrices;

    @Override
    public Optional<BigDecimal> getBuyPrice(Plot plot, Material material) {
        // Preis für Ankauf (NPC kauft von Spieler)
    }

    @Override
    public Optional<BigDecimal> getSellPrice(Plot plot, Material material) {
        // Preis für Verkauf (Spieler kauft von NPC)
    }
}
```

**TradeSet-Generierung:**
```java
// GuildTraderNPC nutzt ItemBasePriceProvider
ItemBasePriceProvider priceProvider = getProviderForPlot(plot);
BigDecimal buyPrice = priceProvider.getBuyPrice(plot, Material.DIAMOND);
BigDecimal sellPrice = priceProvider.getSellPrice(plot, Material.DIAMOND);

// TradeSet: 1 Diamond → buyPrice Sterne (Spieler verkauft)
// TradeSet: sellPrice Sterne → 1 Diamond (Spieler kauft)
```

**Modifikatoren (später):**
- Reputation-Bonus
- Gilden-Rabatte
- Eventbedingte Preisschwankungen

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
8. ✅ **Vererbungshierarchie** im Klassennamen erkennbar (Prefix-basiert)

**Architektur-Mantra:**
> "Core = Interfaces, Module = Implementierungen, Kommunikation = Provider"

**Development-Mantra:**
> "Code → Commit → Push → User testet"

---

**Last Updated:** 2025-11-18
**Version:** 1.0-SNAPSHOT
**Sprint:** 15-17 (Interaction System + Distributor Pattern ✅, DataStore-Integration ✅)
**Branch:** claude/fix-storage-price-loop-012sXDfqzLyyPSPX8QC8egq7

**Hinweis:** module-merchants und module-adminshops wurden entfernt (obsolet - Funktionalität in NPCs-Modul)
