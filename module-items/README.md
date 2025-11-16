# FallenStar Items Module

**MMOItems Integration Module (Sprint 5-6)**

## Übersicht

Das Items-Modul ist ein Wrapper für das MMOItems-Plugin und stellt eine einheitliche Abstraktion für Custom-Items im FallenStar-System bereit.

## Features

✅ **MMOItems-API-Wrapper**
- Vollständige Implementierung des ItemProvider-Interfaces
- Unterstützung für alle MMOItems-Types
- Error-Handling und Logging

✅ **Spezial-Items**
- Münz-Items für Economy-System
- UI-Button-Items für UI-Modul
- Kategorisierung über MMOItems-Tags

✅ **Admin-Commands**
- `/fsitems currency <player> <type> <amount>` - Gebe Münzen
- `/fsitems info` - Zeigt Custom-Item-Info
- `/fsitems reload` - Lädt Config neu

## Dependencies

- **FallenStar-Core** (erforderlich)
- **MMOItems** (erforderlich)

## Installation

1. Stelle sicher, dass FallenStar-Core installiert ist
2. Stelle sicher, dass MMOItems installiert ist
3. Kopiere `FallenStar-Items-1.0.jar` nach `plugins/`
4. Starte Server neu

## Verwendung

### Für Admins

```bash
# Gebe 100 Gold-Münzen an Spieler
/fsitems currency <Spieler> gold 100

# Info über gehaltenes Item
/fsitems info
```

### Für Entwickler

```java
// Hole ItemProvider vom Core
ItemProvider items = core.getProviderRegistry().getItemProvider();

// Erstelle Custom-Item
Optional<ItemStack> sword = items.createItem("SWORD", "FLAMING_BLADE", 1);

// Erstelle Münz-Item
SpecialItemManager special = itemsModule.getSpecialItemManager();
Optional<ItemStack> coins = special.createCurrency("gold", 50);
```

## Spezial-Items

### Münzen (Currency)

- **bronze** - Bronze-Münze (Wert: 1)
- **silver** - Silber-Münze (Wert: 10)
- **gold** - Gold-Münze (Wert: 100)

### UI-Buttons

- **next** - Weiter-Button
- **back** - Zurück-Button
- **confirm** - Bestätigen-Button
- **cancel** - Abbrechen-Button
- **info** - Info-Button

## Konfiguration

Siehe `config.yml` für Item-IDs und Werte.

---

**Version:** 1.0-SNAPSHOT  
**Sprint:** 5-6  
**Status:** Implementiert ✅
