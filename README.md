# FallenStar Paper Core - Sample Repository

**Vollständiges Sample-Code-Repository für das modulare Plugin-System**

---

## 🎯 Schnellstart

```bash
# Vollständige Repository-Struktur anzeigen
cat REPOSITORY_INDEX.md

# Alle Module bauen
mvn clean package

# Testbefehle im Spiel
/fscore admin gui list       # Zeigt alle Test-UIs
/fscore admin gui confirm    # Ja/Nein Dialog
/fscore admin gui trade      # Trading Demo
```

---

## 📚 Dokumentation

- **[REPOSITORY_INDEX.md](REPOSITORY_INDEX.md)** - Vollständige Dateistruktur
- **[CLAUDE.md](CLAUDE.md)** - KI-Assistant Guide (Sprint-Planung, Architektur)
- **[QUICKSTART.md](QUICKSTART.md)** - 5-Minuten-Einstieg
- **[SETUP_COMPLETE.md](SETUP_COMPLETE.md)** - Was ist fertig, was fehlt
- **[CONTRIBUTING.md](CONTRIBUTING.md)** - Entwicklungsrichtlinien
- **[core/README.md](core/README.md)** - Core Plugin Dokumentation

---

## 📦 Module

| Modul | Status | Beschreibung |
|-------|--------|--------------|
| [Core](core/) | ✅ Abgeschlossen | Provider-Interfaces, NoOp-Implementierungen, UI-Framework |
| [FallenStar Plots](module-plots/) | ✅ Abgeschlossen | Plot-System + Storage + TownyPlotProvider |
| [FallenStar Items](module-items/) | ✅ Abgeschlossen | Vanilla Currency Items + Optional MMOItems |
| [FallenStar UI](module-ui/) | ✅ Abgeschlossen | ConfirmationUI, SimpleTradeUI, UIButtonManager |
| [FallenStar Economy](module-economy/) | 📋 Geplant | Weltwirtschaft + VaultEconomyProvider |
| [FallenStar WorldAnchors](module-worldanchors/) | 📋 Geplant | Schnellreisen, POIs, Wegpunkte |
| [FallenStar NPCs](module-npcs/) | 📋 Geplant | NPC-System + CitizensNPCProvider |

---

## 🚀 Features

**Provider-basierte Architektur:**
- ✅ Abstraktion von Dependencies (Towny, Vault, Citizens, MMOItems)
- ✅ Graceful Degradation bei fehlenden Plugins
- ✅ Optional<T> statt Exception-based Control Flow
- ✅ Core enthält NUR Interfaces + NoOp-Implementierungen

**Modulares Design:**
- ✅ Unabhängige Module (nur Core-Dependency)
- ✅ Klare Interfaces
- ✅ Keine direkten Plugin-Dependencies
- ✅ Provider-Implementierungen in Modulen

**Vanilla-First Approach:**
- ✅ Vanilla Currency Items (Bronze/Silver/Gold Coins)
- ✅ PDC-basierte Item-Identifikation
- ✅ Custom Model Data Support
- ✅ Funktioniert OHNE externe Plugins

**UI-Framework:**
- ✅ BaseUI Abstraktionsklassen
- ✅ SmallChestUI, LargeChestUI, SignUI, AnvilUI, BookUI
- ✅ UIRegistry für zentrale UI-Verwaltung
- ✅ Testbefehle: `/fscore admin gui <ui-id>`

**KI-optimierte Entwicklung:**
- ✅ Sprint-basierte Planung (20 Sprints)
- ✅ Fokussierte Arbeitspakete
- ✅ Klare Deliverables
- ✅ CLAUDE.md für KI-Assistenten

---

## 🛠️ Technology Stack

- **Paper API:** 1.21.1
- **Java:** 21
- **Build Tool:** Maven (Multi-Module)
- **Datenbank:** SQLite / MySQL

**Optionale Dependencies:**
- Towny (PlotProvider) - für Plot-System
- Vault (EconomyProvider) - für Wirtschaft
- Citizens (NPCProvider) - für NPCs
- MMOItems (ItemProvider) - für Custom Items (OPTIONAL!)

**Hinweis:** Alle Module funktionieren mit Graceful Degradation - fehlende Dependencies führen zu reduzierter Funktionalität statt Crashes.

---

## 📊 Projekt-Status

**Phase:** 🚀 Aktive Entwicklung
**Version:** 1.0-SNAPSHOT
**Sprint:** 7-8 (UI-Modul) - ✅ Abgeschlossen

**Fertiggestellt:**
- ✅ Architektur-Design & Provider-System
- ✅ Core-Plugin (Interfaces + NoOp + UI-Framework)
- ✅ FallenStar Plots (Sprint 3-4 - Plot-System + Storage)
- ✅ FallenStar Items (Sprint 5-6 - Vanilla Coins + MMOItems)
- ✅ FallenStar UI (Sprint 7-8 - ConfirmationUI + SimpleTradeUI)
- ✅ Testbefehl-Struktur (`/fscore admin [gui/items/plots]`)

**Nächster Sprint:**
- 📋 FallenStar Economy (Sprint 9-10 - Weltwirtschaft + Vault)

**Wichtige Architektur-Änderungen:**
- ✅ Storage-Modul in Plots-Modul integriert
- ✅ MMOItems ist jetzt OPTIONAL (Graceful Degradation)
- ✅ Vanilla Currency Items unabhängig von MMOItems
- ✅ UI-Framework mit Test-UI-System
- ✅ Admin-Command-Struktur für Modul-Tests

---

## 🎮 Testbefehle

**UI-Tests:**
```bash
/fscore admin gui list        # Zeigt alle registrierten Test-UIs
/fscore admin gui confirm     # Öffnet Confirmation UI (Ja/Nein Dialog)
/fscore admin gui trade       # Öffnet Simple Trade UI (Vanilla Demo)
```

**Item-Tests:** (Placeholder)
```bash
/fscore admin items list      # Zeigt alle Items
/fscore admin items browse    # Item-Browser (nur mit MMOItems)
```

**Plot-Tests:** (Placeholder)
```bash
/fscore admin plots info      # Plot-Info am Standort
/fscore admin plots storage view  # Zeigt Storage-Materialien
```

---

## 💎 Highlights

### Vanilla Currency System
```java
// Bronze/Silver/Gold Coins - OHNE MMOItems!
SpecialItemManager manager = ...;
Optional<ItemStack> bronzeCoin = manager.createCurrency("bronze", 10);
Optional<ItemStack> silverCoin = manager.createCurrency("silver", 5);
Optional<ItemStack> goldCoin = manager.createCurrency("gold", 1);

// PDC-basierte Identifikation
boolean isCurrency = manager.isCurrencyItem(itemStack);
int value = manager.getCurrencyValue(itemStack); // Berechnet Gesamtwert
```

### UI-System
```java
// ConfirmationUI - Generisches Ja/Nein Dialog
ConfirmationUI ui = ConfirmationUI.createSimple(
    buttonManager,
    "Möchtest du fortfahren?",
    player -> player.sendMessage("Bestätigt!")
);
ui.open(player);

// Automatische Registrierung in UIRegistry
uiRegistry.registerUI("my-ui", "Display Name", "Description", () -> new MyUI());
```

### Graceful Degradation
```java
// Items-Modul läuft MIT und OHNE MMOItems
if (mmoItemsAvailable) {
    // Full Mode: Custom Items + Vanilla Coins
} else {
    // Vanilla Mode: Nur Coins (kein Crash!)
}
```

---

## 🔗 Links

- **GitHub:** https://github.com/sternstaub/fs-core-sample-dump
- **Dokumentation:** [CLAUDE.md](CLAUDE.md) für vollständige Architektur-Details
- **Sprint-Planung:** Siehe CLAUDE.md → Sprint-Based Development

---

**Für Details siehe [REPOSITORY_INDEX.md](REPOSITORY_INDEX.md) und [CLAUDE.md](CLAUDE.md)**
