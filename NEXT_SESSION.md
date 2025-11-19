# Vorbereitung für nächste Session

**Datum:** Für Session nach 2025-11-19
**Aktueller Sprint:** 18 ✅ → 19 📋
**Branch:** claude/fix-storage-price-loop-012sXDfqzLyyPSPX8QC8egq7

---

## Sprint 18 - Zusammenfassung (Abgeschlossen ✅)

### Was wurde erreicht:

1. **GuiRenderable Interface** (Core)
   - Self-Rendering Actions: Actions kennen ihre Darstellung
   - `getDisplayItem(Player viewer)` - Automatisches ItemStack mit Permission-Lore
   - `isVisible(Player viewer)` - Dynamisches Filtern von Actions

2. **PlotAction implements GuiRenderable**
   - Abstrakte Methoden: `getIcon()`, `getDisplayName()`, `getLore()`
   - Automatische Permission-Lore bei !canExecute()
   - Owner-Requirements werden angezeigt

3. **GuiBuilder** (Universal für ALLE Plot-Typen!)
   - `buildFrom()` mit Intersection Type `T extends GuiRenderable & UiAction`
   - Automatische Konvertierung: Action-Liste → PageableBasicUi
   - Ersetzt manuelle UI-Konstruktionen

4. **MenuAction-Konzept geklärt**
   - **WICHTIG:** MenuAction ist INTERFACE, KEINE Klasse!
   - PlotAction implements MenuAction (KANN Untermenü haben, MUSS NICHT)
   - Hierarchische Submenus via getSubActions()

5. **Konkrete Actions implementiert**
   - SetNameAction: Material.NAME_TAG, zeigt aktuellen Namen
   - ManageNpcsAction: Material.VILLAGER_SPAWN_EGG

### Architektur-Evolution (Komplett):

```
Phase 1 (Sprint 15): UiActionInfo + switch(actionId)
  ❌ Duplikation zwischen Display und Logik

Phase 2 (Sprint 17): PlotAction + Command Pattern
  ✅ Actions kapseln Logik + Permissions
  ❌ Display noch in UiActionInfo

Phase 3 (Sprint 18): GuiRenderable + GuiBuilder
  ✅ Actions kapseln Display + Logik + Permissions
  ✅ Universal GuiBuilder für ALLE Plots
  ✅ SOLID-konform
```

### Commits:
- `9c01fa5` - GuiRenderable Interface + PlotAction Self-Rendering
- `95f0ad8` - Universal GUI-System mit GuiBuilder (Sprint 18 abgeschlossen)

---

## Sprint 19 - Planung (📋 Für nächste Session)

### Hauptziel:
**Vollständige UI-Migration + SOLID-Refactoring ALLER Module**

### Kritische Prinzipien (aus Sprint 18 gelernt):

1. **SOLID über alles** - Jede Klasse eine Verantwortung
2. **Universalität** - Code soll für ALLE Typen funktionieren (nicht nur einen)
3. **Self-Rendering** - Objekte kennen ihre Darstellung
4. **Keine manuellen UI-Konstruktionen** - GuiBuilder für alles
5. **Vererbungshierarchie erkennbar** - Prefix-basierte Namen

### Prioritäten:

#### 1. UI-Migration (Höchste Priorität)

**Neue PlotActions erstellen:**
- [ ] PlotActionManageStorage (Storage-Verwaltung)
- [ ] PlotActionManagePrices (Preis-Verwaltung)
- [ ] PlotActionViewPrices (Preisliste)
- [ ] PlotActionTeleport (Teleport-Action)
- [ ] PlotActionInfo (Plot-Info anzeigen)

**TradeguildPlot refactoren:**
- [ ] `getAvailablePlotActions()` implementieren
- [ ] Kombiniert alle Trait-PlotActions
- [ ] Owner/Guest-Filterung via canExecute()
- [ ] Ersetzt getMainMenuActions()

**PlotCommand/InteractionHandler:**
- [ ] Nutzt GuiBuilder statt HandelsgildeUi
- [ ] Universal für alle Plot-Typen
- [ ] HandelsgildeUi vollständig entfernen (deprecated → removal)

#### 2. SOLID-Refactoring (Identifizierte Schwachstellen)

**Münzen-System (Items-Modul):**
- **Problem:** CoinProvider hart-kodiert für Vanilla Coins
- **SOLID-Verstoß:** OCP (nicht erweiterbar ohne Code-Änderung)
- **Lösung:** CurrencyItem Interface + Registry Pattern
- [ ] CurrencyItem Interface erstellen
- [ ] CurrencyRegistry implementieren
- [ ] CoinProvider refactoren

**Price-Management (Plots-Modul):**
- **Problem:** Nur StorageContainerPlot hat Preise
- **SOLID-Verstoß:** SRP (Plot + Preis-Logik vermischt)
- **Lösung:** Priceable Interface + Universal PriceManager
- [ ] Priceable Interface erstellen
- [ ] Universal PriceManager implementieren
- [ ] StorageContainerPlot refactoren

**NPC-UIs (NPCs-Modul):**
- **Problem:** NpcManagementUi manuell konstruiert
- **SOLID-Verstoß:** DIP (abhängig von konkreter UI-Implementierung)
- **Lösung:** NpcAction extends PlotAction + GuiBuilder
- [ ] NpcAction-Hierarchie erstellen
- [ ] NpcManagementUi durch GuiBuilder ersetzen
- [ ] PlayerNpcManagementUi refactoren

**Trade-System (Economy-Modul):**
- **Problem:** TradeUI nicht GuiRenderable-kompatibel
- **SOLID-Verstoß:** Manuelle UI-Konstruktion
- **Lösung:** TradeAction mit Self-Rendering
- [ ] TradeAction implementieren
- [ ] TradeUI konsistent mit GuiBuilder

#### 3. Dokumentation

- [ ] SOLID-Prinzipien Sektion in CLAUDE.md (✅ bereits erledigt)
- [ ] Universal-Patterns Dokumentation (✅ bereits erledigt)
- [ ] Anti-Patterns erweitern (✅ bereits erledigt)
- [ ] Migration-Guide für bestehende UIs

---

## Wichtige Erkenntnisse (NICHT vergessen!)

### MenuAction ist INTERFACE, keine Klasse!

```java
// ✅ RICHTIG:
public abstract class PlotAction implements UiAction, MenuAction, GuiRenderable {
    // JEDE PlotAction KANN ein Untermenü haben (muss aber nicht)
    // Default: getSubActions() gibt leere Liste zurück
}

// ❌ FALSCH:
public class SubMenuAction extends PlotAction {
    // NICHT nötig! MenuAction ist Interface!
}
```

### GuiBuilder ist universal!

```java
// ✅ Funktioniert für ALLE Plot-Typen:
PageableGui gui = GuiBuilder.buildFrom(
    player,
    "§6Plot-Verwaltung",
    plot.getAvailablePlotActions(player)
);

// ❌ NIEMALS Plot-spezifische UIs:
class TradeguildPlotUi extends BasicUi { } // Gegen OCP!
```

### SOLID vor Features!

**Vor JEDER neuen Implementierung fragen:**
- [ ] Ist es universal? (Funktioniert für ALLE Typen?)
- [ ] Ist es erweiterbar? (Neue Implementierung ohne Code-Änderung?)
- [ ] Ist die Hierarchie erkennbar? (Prefix-basierte Namen?)
- [ ] Nutzt es Self-Rendering? (Objekte kennen Darstellung?)
- [ ] Verwendet es Interfaces? (Composition over Inheritance?)
- [ ] Ist es type-safe? (Compiler-Checks statt Runtime?)
- [ ] Folgt es SOLID? (Alle 5 Prinzipien?)

---

## Anti-Patterns (VERMEIDEN!)

### ❌ Plot-Typ-spezifische UIs
```java
class TradeguildPlotUi { }
class MarketPlotUi { }
// Neue Plot-Typen = neue UI-Klassen → FALSCH!
```

### ❌ Manuelle Type-Checks
```java
if (plot instanceof TradeguildPlot) {
    openTradeguildUI();
} else if (plot instanceof MarketPlot) {
    openMarketUI();
}
// Polymorphismus nutzen stattdessen!
```

### ❌ Hart-kodierte Dependencies
```java
CoinProvider coinProvider = new VanillaCoinProvider();
// Registry Pattern nutzen stattdessen!
```

### ❌ Reflection
```java
Method m = plugin.getClass().getMethod("someMethod");
m.invoke(plugin);
// Provider-Pattern oder Direct Dependency nutzen!
```

---

## Nächste Schritte (für AI-Assistant)

1. **Start:** TradeguildPlot.getAvailablePlotActions() implementieren
2. **Dann:** Weitere PlotActions erstellen (Storage, Prices, etc.)
3. **Dann:** PlotCommand refactoren (GuiBuilder statt HandelsgildeUi)
4. **Dann:** HandelsgildeUi entfernen
5. **Parallel:** SOLID-Refactoring der identifizierten Schwachstellen

### Empfohlene Reihenfolge:

1. UI-Migration (sichtbare Verbesserung für User)
2. Münzen-System (oft verwendet, sollte erweiterbar sein)
3. Price-Management (wichtig für Trading)
4. NPC-UIs (konsistent mit neuen Patterns)
5. Trade-System (abschließende Konsistenz)

---

## Fragen für User (am Anfang der Session)

1. Soll ich mit UI-Migration oder SOLID-Refactoring starten?
2. Welches Modul hat höchste Priorität? (Plots, Items, NPCs, Economy?)
3. Gibt es neue Anforderungen die Sprint 19 beeinflussen?
4. Build-Status: Sind Tests nach Sprint 18 noch grün?

---

**Status:** Sprint 18 vollständig abgeschlossen ✅
**Bereit für:** Sprint 19 (Vollständige UI-Migration + SOLID-Refactoring)
**Dokumentation:** CLAUDE.md vollständig aktualisiert mit SOLID-Prinzipien
