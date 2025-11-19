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
- **Phase:** Aktive Entwicklung (~88%)
- **Abgeschlossen:**
  - Core ✅ (Provider, UI-Framework, Interaction System, Distributor Pattern, GuiRenderable, GuiBuilder)
  - Plots ✅ (TradeguildPlot, DataStore-Persistenz, InteractionRegistry)
  - Items ✅ (Vanilla Coins, MMOItems-Support)
  - Economy ✅ (Vault-Integration, TradeSet-System)
  - Trading-System ✅ (TradeUI, TradingEntity)
  - NPCs ✅ (GuildTraderNpcEntity, DistributableNpc, QuestContainer)
  - Universal GUI-System ✅ (GuiRenderable + GuiBuilder - Sprint 18)
- **Aktuell:** Sprint 19 (Vollständige UI-Migration, SOLID-Refactoring) 📋
- **Nächster:** Sprint 20+ (Quest-System, Chat, Auth, WebHooks)

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

| Sprint | Modul/Feature | Status |
|--------|---------------|--------|
| 1-2 | Core + UI Framework | ✅ |
| 3-4 | Plots (inkl. Storage) | ✅ |
| 5-6 | Items (Vanilla Coins + MMOItems) | ✅ |
| 7-8 | UI-Refactoring (in Core integriert) | ✅ |
| 9-10 | Economy | ✅ |
| 11-12 | Trading + NPC-GUI | ✅ |
| 13-14 | NPCs (Citizens + Händler-NPCs) | ✅ |
| 15 | Interaction System + Distributor Pattern | ✅ |
| 16 | DataStore-Integration + Persistenz | ✅ |
| 17 | Trait-Actions + Command Pattern + Naming Convention | ✅ |
| 18 | GuiRenderable + Universal GuiBuilder | ✅ |
| 19 | Vollständige UI-Migration + SOLID-Refactoring | 📋 Geplant |
| 20+ | Quest-System, Chat, Auth, WebHooks | 📋 Geplant |

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

## Design Evolution (UI-System)

Das UI-System hat eine bedeutende Architektur-Evolution durchlaufen. Dieser Abschnitt dokumentiert die Design-Änderungen für AI-Verständlichkeit.

### Phase 1: UiActionInfo (Sprint 15)

**Konzept:** Actions als Metadaten

```java
UiActionInfo action = UiActionInfo.builder()
    .id("manage_npcs")
    .displayName("§bNPCs verwalten")
    .icon(Material.VILLAGER_SPAWN_EGG)
    .requiredPermission("fallenstar.plot.npc.manage")
    .build();

// Ausführung:
@Override
public boolean executeAction(Player player, String actionId) {
    return switch (actionId) {
        case "manage_npcs" -> { /* Logik hier */ yield true; }
        default -> false;
    };
}
```

**Probleme:**
- ❌ Action kennt nur Display, nicht Logik
- ❌ Switch-Statement wird riesig
- ❌ Logik verstreut in executeAction()
- ❌ Schwer testbar

### Phase 2: PlotAction mit Command Pattern (Sprint 17)

**Konzept:** Actions kapseln Logik + Berechtigungen

```java
public abstract class PlotAction implements UiAction {
    protected final Plot plot;

    // Berechtigungsprüfung IN der Action!
    @Override
    public boolean canExecute(Player player) {
        if (requiresOwnership() && !isOwner(player)) return false;
        if (requiredPermission() != null && !hasPermission(...)) return false;
        return true;
    }

    // Logik IN der Action!
    @Override
    public abstract void execute(Player player);
}

// Verwendung:
PlotAction action = new PlotActionManageNpcs(plot, providers, plotModule);
if (action.canExecute(player)) {
    action.execute(player); // Action führt sich selbst aus!
}
```

**Verbesserungen:**
- ✅ Action kennt Logik + Berechtigungen
- ✅ Kein Switch-Statement mehr
- ✅ Wiederverwendbar und testbar
- ✅ Type-Safe durch Compiler

**Offen:**
- ⚠️ Display-Logik noch in UiActionInfo
- ⚠️ Duplikation: Icon/DisplayName in UiActionInfo UND PlotAction

### Phase 3: GuiRenderable (Sprint 18) ✅ Abgeschlossen

**Konzept:** Actions rendern sich selbst

```java
public abstract class PlotAction implements UiAction, GuiRenderable {

    // Action kennt ihr Display!
    @Override
    public ItemStack getDisplayItem(Player viewer) {
        ItemStack item = new ItemStack(getIcon());
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(getDisplayName());

        List<String> lore = new ArrayList<>(getLore());
        if (!canExecute(viewer)) {
            lore.add("§c§l✗ Keine Berechtigung");
        }
        meta.setLore(lore);

        item.setItemMeta(meta);
        return item;
    }

    protected abstract Material getIcon();
    protected abstract String getDisplayName();
    protected abstract List<String> getLore();
}

// Universal GuiBuilder:
PageableGui gui = GuiBuilder.buildFrom(
    player,
    "§6Plot-Verwaltung",
    plot.getAvailablePlotActions(player) // List<PlotAction>
);
gui.open(player);
```

**Vorteile:**
- ✅ **Vollständige Kapselung:** Display + Logik + Berechtigungen
- ✅ **Universal:** Ein GuiBuilder für ALLE Plots
- ✅ **DRY:** Keine Duplikation mehr
- ✅ **Automatisch:** Permission-Checks → Lore-Updates
- ✅ **Erweiterbar:** Neue Action → automatisch im GUI

**Architektur-Vergleich:**

| Aspekt | UiActionInfo | PlotAction | GuiRenderable |
|--------|--------------|------------|---------------|
| Display-Logik | ✅ | ❌ | ✅ |
| Ausführungs-Logik | ❌ | ✅ | ✅ |
| Berechtigungen | Partial | ✅ | ✅ |
| Wiederverwendbar | ❌ | ✅ | ✅ |
| Type-Safe | ❌ | ✅ | ✅ |
| Self-Rendering | ❌ | ❌ | ✅ |

**Migration-Pfad:**

1. **Aktuell:** `UiActionInfo` + `switch(actionId)` in executeAction()
2. **Sprint 17:** `PlotAction` mit canExecute() + execute()
3. **Sprint 18:** `PlotAction implements GuiRenderable` + GuiBuilder
4. **Zukunft:** `HandelsgildeUi` entfernen → GuiBuilder universal

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

## Sprint-Details

### Abgeschlossen (Sprint 13-18)

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

**Sprint 17: Trait-basierte UI-Actions + Command Pattern** ✅
1. ✅ NamedPlot.getNameActions() - Actions in Traits definiert
2. ✅ StorageContainerPlot.getStorageActions() - Trait-Komposition
3. ✅ NpcContainerPlot.getNpcActions() - DRY für Actions
4. ✅ TradeguildPlot refactored - Kombiniert Trait-Actions
5. ✅ UiAction.canExecute() - Berechtigungsprüfung in Actions
6. ✅ PlotAction abstrakte Basisklasse - Command Pattern mit Objekt-Referenz
7. ✅ PlotActionSetName - Konkrete Implementierung
8. ✅ PlotActionManageNpcs refactored - Nutzt PlotAction-Basis
9. ✅ HandelsgildeUi deprecated - Ersetzt durch GenericInteractionMenuUi
10. ✅ Naming Convention - Vererbungshierarchie erkennbar (Prefix-basiert)

**Sprint 18: Universal GUI-System (GuiRenderable + GuiBuilder)** ✅
1. ✅ GuiRenderable Interface (Core)
   - `ItemStack getDisplayItem(Player viewer)` - Self-Rendering
   - `boolean isVisible(Player viewer)` - Sichtbarkeits-Filter
   - Vollständige Javadoc mit Architektur-Evolution

2. ✅ PlotAction implements GuiRenderable
   - Abstrakte Methoden: `getIcon()`, `getDisplayName()`, `getLore()`
   - Automatisches Display-Item mit Permission-Lore
   - Owner-Requirements → "Nur für Plot-Owner" Anzeige

3. ✅ SetNameAction - Display-Logik implementiert
   - Material.NAME_TAG Icon
   - Zeigt aktuellen Plot-Namen in Lore
   - Vollständig GuiRenderable

4. ✅ ManageNpcsAction - Display-Logik implementiert
   - Material.VILLAGER_SPAWN_EGG Icon
   - Beschreibung der NPC-Verwaltung
   - Vollständig GuiRenderable

5. ✅ GuiBuilder (Core)
   - `buildFrom()` mit Intersection Type `T extends GuiRenderable & UiAction`
   - Automatische Konvertierung zu PageableBasicUi
   - Universal für ALLE Plot-Typen

6. ✅ PlotAction.openSubMenu() mit GuiBuilder
   - Ersetzt Placeholder durch echtes GUI
   - Automatische Untermenü-Erstellung
   - Runtime-Checks für UiAction-Kompatibilität

7. ✅ MenuAction-Interface dokumentiert
   - **WICHTIG:** MenuAction ist INTERFACE, KEINE Klasse!
   - PlotAction implements MenuAction (KANN Untermenü haben, MUSS NICHT)
   - Hierarchische Submenus via getSubActions()

**Architektur-Erkenntnisse:**
- ✅ **Universal:** Ein GuiBuilder für ALLE Plot-Typen
- ✅ **DRY:** Action kennt Display + Logik + Permissions
- ✅ **Type-Safe:** Intersection Types garantieren Kompatibilität
- ✅ **SOLID:** Single Responsibility, Open/Closed, Dependency Inversion
- ✅ **Erweiterbar:** Neue PlotAction → automatisch im GUI

### Sprint 19: Vollständige UI-Migration + SOLID-Refactoring (🔄 IN ARBEIT)

**Hauptziel:** Übertrage das Universal GUI-Pattern auf ALLE Bereiche des Plugins.

**Kritische Prinzipien (aus Sprint 18 gelernt):**
1. **SOLID über alles:** Jede Klasse eine Verantwortung
2. **Universalität:** Code soll für ALLE Typen funktionieren (nicht nur einen)
3. **Self-Rendering:** Objekte kennen ihre Darstellung
4. **Keine manuellen UI-Konstruktionen:** GuiBuilder für alles
5. **Vererbungshierarchie erkennbar:** Prefix-basierte Namen (PlotAction*)

---

#### **IST-Zustand (Sprint 19 Start):**

**✅ Vollständig migriert (PlotAction + GuiRenderable):**
- `SetNameAction` (Naming: ❌ muss → PlotActionSetName)
- `ManageNpcsAction` (Naming: ❌ muss → PlotActionManageNpcs)

**❌ Noch alte Struktur (implements UiAction, kein GuiRenderable):**
- `ViewPricesAction`, `SetPriceAction`, `OpenStorageUiAction`
- `ScanStorageAction`, `ViewPlotInfoAction`, `ViewMarketStatsAction`
- `ManageSlotsAction`, `ManageTraderSlotsAction`, `FindTradersAction`
- Weitere 15+ Actions in `/action/` und `/action/npc/`

**🔄 TradeguildPlot:**
- Nutzt noch UiActionInfo (altes System)
- `getMainMenuActions()` statt `getAvailablePlotActions()`
- `executeAction()` mit Switch-Statement (obsolet)

---

#### **Phase 1: Core PlotActions Migration** ✅ ABGESCHLOSSEN

**Ziel:** Wichtigste Plot-Actions nach PlotAction-Pattern migrieren

**1A. Naming Convention (Umbenennen):**
- ✅ `SetNameAction` → `PlotActionSetName`
- ✅ `ManageNpcsAction` → `PlotActionManageNpcs`
- ✅ Referenzen in `HandelsgildeUi.java` und `MarketPlotUi.java` aktualisiert

**1B. Migration zu PlotAction (extends + GuiRenderable):**
- ✅ `ViewPricesAction` → `PlotActionViewPrices` (zeigt Anzahl Preise in Lore)
- ✅ `SetPriceAction` → `PlotActionManagePrices` (Owner-only, Permission-Check)
- ✅ `OpenStorageUiAction` → `PlotActionManageStorage` (isOwner via PlotAction)
- ✅ `ViewPlotInfoAction` → `PlotActionInfo` (zeigt Custom-Name in Lore)

**1C. Neue Actions erstellen:**
- ✅ `PlotActionTeleport` (Teleport zum Plot, Owner-only, zeigt Koordinaten)

**Ergebnis:** 7 vollständige PlotActions mit Self-Rendering
- Alle implementieren GuiRenderable vollständig
- Automatische Permission-Lore bei !canExecute()
- Context-aware Lore (Preise, Storage, Koordinaten)
- Naming Convention eingehalten (PlotAction* Prefix)
- **Unit Tests:** PlotActionTest.java (19 Tests)
  - Icon-Validierung für alle Actions
  - Berechtigungs-Tests (requiresOwnership, requiredPermission)
  - Naming Convention Tests
  - Null-Safety Tests

---

#### **Phase 2: TradeguildPlot Refactoring** 📋 Geplant

**Ziel:** TradeguildPlot nutzt neues PlotAction-System

**2A. getAvailablePlotActions() implementieren:**
```java
public List<PlotAction> getAvailablePlotActions(Player player) {
    List<PlotAction> actions = new ArrayList<>();

    // NamedPlot Actions
    actions.add(new PlotActionSetName(this, providers));

    // StorageContainerPlot Actions
    actions.add(new PlotActionManageStorage(this, providers, storageManager));
    actions.add(new PlotActionViewPrices(this, providers));

    // NpcContainerPlot Actions
    actions.add(new PlotActionManageNpcs(this, providers, plotModule));

    // Owner-Filterung via canExecute() automatisch!
    return actions;
}
```

**2B. executeAction() entfernen** (obsolet mit PlotAction)

**2C. UiActionInfo ersetzen** durch PlotAction

**Ergebnis:** TradeguildPlot vollständig auf GuiBuilder-System migriert

---

#### **Phase 3: InteractionHandler & Commands** 📋 Geplant

**Ziel:** GUI-Erstellung über GuiBuilder

**3A. InteractionHandler refactoren:**
```java
// ALT (HandelsgildeUi):
HandelsgildeUi ui = new HandelsgildeUi(plot, player, providers);
ui.open(player);

// NEU (GuiBuilder):
List<PlotAction> actions = plot.getAvailablePlotActions(player);
PageableGui gui = GuiBuilder.buildFrom(player, plot.getDisplayName(), actions);
gui.open(player);
```

**3B. PlotCommand refactoren** (analog)

**3C. HandelsgildeUi deprecated markieren**

**Ergebnis:** Universales GUI-System für alle Plots

---

#### **Phase 4: HandelsgildeUi Removal** 📋 Geplant

**Ziel:** Alte UI vollständig entfernen

- Alle Referenzen entfernen
- `HandelsgildeUi.java` löschen
- Migration abgeschlossen

**Ergebnis:** Cleanup abgeschlossen

---

#### **Phase 5: SOLID-Refactoring (Andere Module)** 📋 Geplant

**Ziel:** GuiRenderable-Pattern auf andere Module übertragen

**5A. Items-Modul:**
- **Problem:** Nicht erweiterbar, hart-kodiert
- **Lösung:** `CurrencyItem` Interface (Self-Describing)
- **Ziel:** Neue Währungen ohne Code-Änderung

**5B. Economy-Modul:**
- **Problem:** TradeUI nicht GuiRenderable-kompatibel
- **Lösung:** `TradeAction` mit Self-Rendering
- **Ziel:** Konsistente UI-Erstellung

**5C. NPCs-Modul:**
- **Problem:** NPC-Verwaltung mit manuellen UIs
- **Lösung:** `NpcAction extends PlotAction`
- **Ziel:** GuiBuilder für NPC-Konfiguration

**5D. Price-Management:**
- **Problem:** Preise nur für StorageContainerPlot
- **Lösung:** `Priceable` Interface + Universal PriceManager
- **Ziel:** Preise für beliebige Items/Services

**Ergebnis:** SOLID-konformes Design im gesamten Plugin

---

### Sprint 20+: Neue Features (📋 Geplant)

- Quest-System (GuiBuilder-basiert)
- Chat-System (Provider-Pattern)
- Auth-System (Provider-Pattern)
- WebHooks (Event-Streaming)

---

## SOLID-Prinzipien & Universal Patterns

**Erkenntnisse aus Sprint 18: Code muss SOLID und universal sein!**

### Kern-Prinzipien

1. **Single Responsibility Principle (SRP)**
   - PlotAction: Kennt Display + Logik + Permissions (alles für EINE Action)
   - GuiBuilder: Nur GUI-Erstellung aus GuiRenderable-Listen
   - GuiRenderable: Nur Self-Rendering-Interface

2. **Open/Closed Principle (OCP)**
   - **Offen für Erweiterung:** Neue PlotAction → automatisch im GUI
   - **Geschlossen für Änderung:** GuiBuilder ändert sich nicht bei neuen Actions
   - Beispiel: SetNameAction hinzufügen ohne GuiBuilder zu ändern

3. **Liskov Substitution Principle (LSP)**
   - Alle PlotActions sind austauschbar (gleiches Interface)
   - GuiBuilder funktioniert mit JEDER GuiRenderable-Implementierung
   - TradeguildPlot, MarketPlot, etc. alle nutzbar mit GuiBuilder

4. **Interface Segregation Principle (ISP)**
   - UiAction: Nur execute() + canExecute()
   - GuiRenderable: Nur getDisplayItem() + isVisible()
   - MenuAction: Nur getSubActions() + hasSubMenu()
   - PlotAction implementiert alle 3 separat

5. **Dependency Inversion Principle (DIP)**
   - GuiBuilder hängt von Interface ab (GuiRenderable + UiAction)
   - NICHT von Implementierung (PlotAction)
   - Intersection Type: `T extends GuiRenderable & UiAction`

### Universal Patterns (aus Sprint 18)

**Pattern 1: Self-Rendering Objects**
```java
// ❌ FALSCH: Manuelle UI-Konstruktion
public class PlotUi {
    public void buildButton(Plot plot) {
        ItemStack item = new ItemStack(Material.NAME_TAG);
        item.setDisplayName("...");
        // ... manuell für jeden Plot-Typ
    }
}

// ✅ RICHTIG: Self-Rendering
public class PlotActionSetName implements GuiRenderable {
    @Override
    public ItemStack getDisplayItem(Player viewer) {
        // Action kennt ihre Darstellung!
        return buildItem(getIcon(), getDisplayName(), getLore());
    }
}

// Universal verwendbar:
GuiBuilder.buildFrom(player, title, plot.getAvailablePlotActions(player));
```

**Pattern 2: Intersection Types für Constraints**
```java
// ❌ FALSCH: Lose Typisierung
public PageableGui buildFrom(List<Object> actions) {
    // Was wenn Object kein GuiRenderable ist?
}

// ✅ RICHTIG: Intersection Type
public <T extends GuiRenderable & UiAction> PageableGui buildFrom(
    Player viewer,
    String title,
    List<T> actions
) {
    // Compiler garantiert: T IST GuiRenderable UND UiAction!
}
```

**Pattern 3: Composition over Inheritance**
```java
// ❌ FALSCH: Tiefe Vererbungshierarchien
class Action {}
class PlotAction extends Action {}
class NameAction extends PlotAction {}
class SetNameAction extends NameAction {}

// ✅ RICHTIG: Interface-Komposition
interface UiAction { }
interface GuiRenderable { }
interface MenuAction { }

class PlotAction implements UiAction, GuiRenderable, MenuAction {
    // Kombiniert Capabilities ohne tiefe Hierarchie
}
```

**Pattern 4: No Manual Type-Checking**
```java
// ❌ FALSCH: Manuelle Type-Checks
public void openUI(Plot plot) {
    if (plot instanceof TradeguildPlot) {
        openTradeguildUI();
    } else if (plot instanceof MarketPlot) {
        openMarketUI();
    }
    // Neue Plot-Typen = Code-Änderung!
}

// ✅ RICHTIG: Polymorphismus
public void openUI(Plot plot, Player player) {
    // ALLE Plot-Typen funktionieren!
    List<PlotAction> actions = plot.getAvailablePlotActions(player);
    PageableGui gui = GuiBuilder.buildFrom(player, plot.getDisplayName(), actions);
    gui.open(player);
}
```

### Identifizierte Schwachstellen (Sprint 19 TO-DO)

1. **Münzen-System (Items-Modul)**
   - **Problem:** CoinProvider hart-kodiert für Vanilla Coins
   - **SOLID-Verstoß:** OCP (nicht erweiterbar ohne Code-Änderung)
   - **Lösung:** CurrencyItem Interface + Registry Pattern

2. **Price-Management (Plots-Modul)**
   - **Problem:** Nur StorageContainerPlot hat Preise
   - **SOLID-Verstoß:** SRP (Plot + Preis-Logik vermischt)
   - **Lösung:** Priceable Interface + Universal PriceManager

3. **NPC-UIs (NPCs-Modul)**
   - **Problem:** NpcManagementUi manuell konstruiert
   - **SOLID-Verstoß:** DIP (abhängig von konkreter UI-Implementierung)
   - **Lösung:** NpcAction extends PlotAction + GuiBuilder

4. **HandelsgildeUi (Plots-Modul)**
   - **Problem:** Plot-spezifische UI-Klasse
   - **SOLID-Verstoß:** OCP (neue Plot-Typen = neue UI-Klassen)
   - **Lösung:** Vollständig durch GuiBuilder ersetzen

### Universal Design Checklist

Vor jeder neuen Feature-Implementierung fragen:

- [ ] **Ist es universal?** Funktioniert es für ALLE Typen, nicht nur einen?
- [ ] **Ist es erweiterbar?** Neue Implementierung ohne Code-Änderung?
- [ ] **Ist die Hierarchie erkennbar?** Prefix-basierte Klassennamen?
- [ ] **Nutzt es Self-Rendering?** Objekte kennen ihre Darstellung?
- [ ] **Verwendet es Interfaces?** Composition over Inheritance?
- [ ] **Ist es type-safe?** Compiler-Checks statt Runtime-Checks?
- [ ] **Folgt es SOLID?** Alle 5 Prinzipien beachtet?

### Anti-Patterns (erweitert)

**❌ Plot-Typ-spezifische UIs:**
```java
// NIEMALS!
class TradeguildPlotUi extends BasicUi { }
class MarketPlotUi extends BasicUi { }
class WarehousePlotUi extends BasicUi { }
```

**✅ Universal GuiBuilder:**
```java
// IMMER!
GuiBuilder.buildFrom(player, title, plot.getAvailablePlotActions(player));
```

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

**Last Updated:** 2025-11-19
**Version:** 1.0-SNAPSHOT
**Sprint:** 18 (Universal GUI-System ✅) → 19 (Vollständige UI-Migration + SOLID-Refactoring 📋)
**Branch:** claude/fix-storage-price-loop-012sXDfqzLyyPSPX8QC8egq7

**Wichtige Erkenntnisse (Sprint 18):**
- MenuAction ist INTERFACE, keine Klasse!
- PlotAction kann Untermenüs haben (implements MenuAction), muss aber nicht
- GuiBuilder ist universal für ALLE Plot-Typen
- SOLID-Prinzipien müssen auf ALLE Module übertragen werden
- Code muss universal und erweiterbar sein (nicht Plot-Typ-spezifisch)

**Hinweis:** module-merchants und module-adminshops wurden entfernt (obsolet - Funktionalität in NPCs-Modul)
