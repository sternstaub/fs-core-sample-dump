# Roadmap: /plot gui Command

**Status:** 📋 Geplant
**Priorität:** Medium
**Abhängigkeiten:** UI-Modul (optional, keine harte Abhängigkeit)

---

## Übersicht

Der `/plot gui` Befehl soll eine benutzerfreundliche UI-basierte Verwaltung für Plots bereitstellen, ohne dass das Plot-Modul eine harte Abhängigkeit zum UI-Modul hat.

## Architektur-Ansatz

### Option 1: Event-basierte Kommunikation (Empfohlen)

```java
// PlotModule feuert Event
PlotGUIRequestEvent event = new PlotGUIRequestEvent(player, plot);
Bukkit.getPluginManager().callEvent(event);

// UI-Modul (falls geladen) reagiert darauf
@EventHandler
public void onPlotGUIRequest(PlotGUIRequestEvent event) {
    // Öffne Plot-GUI
    PlotManagementUI ui = new PlotManagementUI(...);
    ui.open(event.getPlayer());
}
```

**Vorteile:**
- Keine harte Abhängigkeit
- UI-Modul kann dynamisch geladen/entladen werden
- Sauber entkoppelt

### Option 2: Provider-Pattern (Alternativ)

```java
// Core: UIProvider-Interface erweitern
public interface UIProvider {
    void openPlotGUI(Player player, Plot plot);
}

// UI-Modul registriert Implementierung
// Plot-Modul nutzt UIProvider falls verfügbar
```

## Geplante Features

### Phase 1: Basis-GUI
- **Plot-Informationen anzeigen**
  - Typ, Owner, Permissions
  - Größe, Koordinaten
  - Einstellungen

- **Quick-Actions**
  - Storage-Übersicht öffnen
  - NPC-Management
  - Permissions-Editor

### Phase 2: Storage-Integration
- **Material-Liste als GUI**
  - Sortierung, Filterung
  - Material-Details per Klick
  - Empfangskiste setzen

### Phase 3: NPC-Management-GUI
- **NPC-Liste**
  - Spawnen, Entfernen
  - NPC-Konfiguration
  - Dialog-Editor

### Phase 4: Handelsgilde-Features
- **Preisübersicht** (ItemBasePriceProvider)
- **Händler-NPCs konfigurieren**
- **Handelsangebote erstellen**

## Implementierungs-Schritte

1. **Event-Klasse erstellen** (`PlotGUIRequestEvent` in Core)
2. **UI-Modul erweitern** mit Plot-GUI-Handler
3. **PlotManagementUI implementieren** (UI-Modul)
4. **Fallback-Mechanik** in PlotCommand (zeigt Text-Info wenn kein UI)
5. **Permissions hinzufügen** (`fallenstar.plot.gui`)

## Abhängigkeiten

- **Optional:** UI-Modul (für grafische Verwaltung)
- **Fallback:** Text-basierte Ausgabe via `/plot info`
- **Integration:** Storage-System, NPC-System

## Zeitplan

- **Sprint 13-14:** Event-Infrastruktur + Basis-GUI
- **Sprint 15-16:** Storage-Integration
- **Sprint 17-18:** NPC-Management
- **Sprint 19-20:** Handelsgilde-Features

## Code-Beispiel (zukünftig)

```java
// In PlotCommand.java
case "gui" -> {
    // Feuere Event (UI-Modul reagiert falls geladen)
    PlotGUIRequestEvent event = new PlotGUIRequestEvent(player, currentPlot);
    Bukkit.getPluginManager().callEvent(event);

    if (!event.isHandled()) {
        // Fallback: Text-basierte Info
        player.sendMessage("§eUI-Modul nicht verfügbar - nutze §6/plot info");
        return infoCommand.execute(player, subArgs);
    }
    return true;
}
```

## Notizen

- **Keine harte Abhängigkeit** zum UI-Modul
- **Graceful Degradation** wenn UI nicht verfügbar
- **Event-basiert** für maximale Flexibilität
- **Modular** - Features können schrittweise aktiviert werden

---

**Last Updated:** 2025-11-16
**Author:** FallenStar Development Team
