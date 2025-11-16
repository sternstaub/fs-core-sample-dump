# FallenStar Items Module

**Sprint 5-6** - MMOItems Integration Module ✅

## Überblick

Das Items-Modul ist ein Wrapper für MMOItems 6.10+, der eine einheitliche Abstraktionsebene für Custom-Items im FallenStar-System bereitstellt.

### Hauptmerkmale

- ✅ **Reflection-basierte MMOItems-Integration** (keine MMOPlugin-Dependency!)
- ✅ **Type-basierte Kategorisierung** (Ersatz für entfernte `getTags()`-API)
- ✅ **Automatische Preisberechnung** basierend auf Item-Types
- ✅ **Spezial-Items**: Währungs-Items (Münzen), UI-Buttons
- ✅ **Test-UIs**: ItemBrowserUI, TestTradeUI
- ✅ **UIRegistry-Integration** für `/fscore admin gui`

## Status

**✅ ERFOLGREICH DEPLOYED**

Server-Logs bestätigen erfolgreiche Initialisierung:
```
[FallenStar-Items] ✓ MMOItems API initialized via reflection
[FallenStar-Items] ✓ MMOItemsItemProvider initialized
[FallenStar-Items] ✓ Test-UIs registered
[FallenStar-Items] ✓ Items Module enabled!
```

## Features

### Commands
- `/items browse` - Item-Browser UI
- `/items info` - Item-Info anzeigen
- `/items reload` - Cache invalidieren

### UIs (via /fscore admin gui)
- **Item Browser** - Kategorie-basierter Item-Browser
- **Trade Test UI** - Vanilla Trading Demo

### Spezial-Items
- Währungen: bronze, silver, gold (BRONZE_COIN, SILVER_COIN, GOLD_COIN)
- UI-Buttons: next, back, confirm, cancel, info

## Build Info

**Dependencies:**
- MMOItems-API 6.10.1-SNAPSHOT (Phoenix)
- MythicLib-dist 1.6.2-SNAPSHOT

**Reflection-Pattern:** Vermeidet MMOPlugin-Dependency zur Compile-Zeit!

---

**Version:** 1.0-SNAPSHOT  
**Status:** ✅ Production Ready  
**Last Updated:** 2025-11-16
