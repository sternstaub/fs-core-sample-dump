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
/fscore admin gui list                          # Zeigt alle Test-UIs
/fscore admin gui confirm                       # Ja/Nein Dialog
/fscore admin economy getcoin sterne bronze 10  # 10 Bronzesterne holen
/plot price set                                 # Preis festlegen (Owner)
/plot gui                                       # Plot-Verwaltung (Owner/Guest)
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
| [Core](core/) | ✅ Abgeschlossen | Provider-Interfaces, NoOp-Implementierungen, UI-Framework (inkl. TradeUI) |
| [FallenStar Plots](module-plots/) | ✅ Abgeschlossen | Plot-System + Storage + Slot-System + TownyPlotProvider |
| [FallenStar Items](module-items/) | ✅ Abgeschlossen | Vanilla Currency Items + Optional MMOItems |
| [FallenStar Economy](module-economy/) | ✅ Abgeschlossen | Weltwirtschaft + VaultEconomyProvider + Währungssystem + TradeSet-System |
| [FallenStar NPCs](module-npcs/) | 🔨 In Arbeit | NPC-System + CitizensNPCProvider + Händler-NPCs (GuildTrader, PlayerTrader) |

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

**UI-Framework (Core-integriert):**
- ✅ BaseUI Abstraktionsklassen
- ✅ SmallChestUI, LargeChestUI, SignUI, AnvilUI, BookUI
- ✅ UIRegistry für zentrale UI-Verwaltung
- ✅ Guest/Owner View Pattern für Plot-UIs
- ✅ TradeUI (Vanilla Merchant Interface)
- ✅ Testbefehle: `/fscore admin gui <ui-id>`

**Economy-System:**
- ✅ Währungssystem (Basiswährung "Sterne")
- ✅ VaultEconomyProvider mit Withdraw-Funktionalität
- ✅ ItemBasePriceProvider (Vanilla + Custom Items)
- ✅ Data Persistence (Preise überleben Server-Neustarts)
- ✅ Multi-Currency Support (Wechselkurse)

**Trading-System:**
- ✅ TradeSet-System (Ankauf/Verkauf-Preise, Input → Output)
- ✅ TradingEntity-Interface (Provider-Pattern für Händler-NPCs)
- ✅ TradeUI (Dynamisches Vanilla Merchant-Interface)
- ✅ PlotRegistry (Auto-Registration von Handelsgilden via Towny)
- ✅ Virtuelles Händler-Inventar (54 Slots, Base64-Serialisierung)

**Plot-Slots System:**
- ✅ NPC-Slot-Objekte mit SlotType (TRADER, BANKER, CRAFTSMAN, etc.)
- ✅ SlottedPlot Interface für slottable Grundstücke
- ✅ SlottedPlotForMerchants mit Händler-spezifischen Limits
- ✅ Slot-Verwaltung (addSlot, removeSlot, assignNPC)
- ✅ Slot-Status-Tracking (occupied, active, assigned NPC)
- ✅ SlotManagementUI (Händler auf Slots platzieren)
- ✅ TraderSelectionUI (Händler-Auswahl aus Handelsgilden)

**NPC-Reisesystem:**
- ✅ NPCTravelSystem (10s/Chunk Verzögerung, 5 Sterne/Chunk Kosten)
- ✅ TravelTicket (Reise-Details, Status-Tracking, Fortschritts-Berechnung)
- ✅ Restart-Handling (Aktive Reisen überleben Server-Neustart)
- ✅ NPCSkinPool (Zufällige Skins für 5 NPC-Typen)

**Plot-Namen-Feature:**
- ✅ NamedPlot-Interface (Custom-Namen für Grundstücke)
- ✅ PlotNameManager (Zentrale Verwaltung, Persistierung)
- ✅ PlotNameInputUI (Namen-Eingabe via Chat)

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
**Sprint:** 11-12 (Trading-System & Händler-Infrastruktur) - ✅ Abgeschlossen

**Fertiggestellt:**
- ✅ Architektur-Design & Provider-System
- ✅ Core-Plugin (Sprint 1-2 - Interfaces + NoOp + UI-Framework inkl. TradeUI)
- ✅ FallenStar Plots (Sprint 3-4 - Plot-System + Storage + Slot-System)
- ✅ FallenStar Items (Sprint 5-6 - Vanilla Coins + MMOItems)
- ✅ UI-Refactoring (Sprint 7-8 - UI-Modul in Core integriert)
- ✅ FallenStar Economy (Sprint 9-10 - Weltwirtschaft + Vault + Währungssystem)
- ✅ Trading-System (Sprint 11-12 - TradeSet, TradingEntity, PlotRegistry, NPC-Reisen)
- ✅ Testbefehl-Struktur (`/fscore admin [gui/items/plots/economy]`)

**Aktueller Sprint:**
- 🔨 FallenStar NPCs (Sprint 13-14 - NPC-System + Citizens-Integration)

**Wichtige Architektur-Änderungen:**
- ✅ Storage-Modul in Plots-Modul integriert
- ✅ UI-Modul in Core-Plugin integriert (TradeUI migriert)
- ✅ WorldAnchors-Modul entfernt → Plot-Slots System
- ✅ MMOItems ist jetzt OPTIONAL (Graceful Degradation)
- ✅ Vanilla Currency Items unabhängig von MMOItems
- ✅ UI-Framework mit Test-UI-System
- ✅ Admin-Command-Handler-Registry (kein Reflection mehr!)
- ✅ VaultEconomyProvider mit Withdraw-Funktionalität
- ✅ Data Persistence Pattern (loadFromConfig/saveToConfig)

---

## 🎮 Testbefehle

**UI-Tests:**
```bash
/fscore admin gui list        # Zeigt alle registrierten Test-UIs
/fscore admin gui confirm     # Öffnet Confirmation UI (Ja/Nein Dialog)
/fscore admin gui trade       # Öffnet Simple Trade UI (Vanilla Demo)
```

**Economy-Tests:**
```bash
/fscore admin economy getcoin <währung> [tier] [anzahl]    # Kostenlose Münzen
/fscore admin economy withdraw <währung> [tier] [anzahl]   # Vault-basierte Auszahlung
# Beispiele:
#   /fscore admin economy getcoin sterne bronze 10
#   /fscore admin economy withdraw sterne silver 5
```

**Plot-Tests:**
```bash
/plot price set              # Preis für Item festlegen (Owner)
/plot price list             # Alle Preise anzeigen (Public)
/plot gui                    # Öffnet Plot-Verwaltungs-UI (Owner/Guest View)
```

**Item-Tests:** (Placeholder)
```bash
/fscore admin items list      # Zeigt alle Items
/fscore admin items browse    # Item-Browser (nur mit MMOItems)
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

### Economy-System
```java
// Währungen registrieren
CurrencyItemSet sterne = CurrencyItemSet.createBaseCurrency();
currencyManager.registerCurrency(sterne);

// Münzen auszahlen (kostenlos)
currencyManager.payoutCoins(player, "sterne", CurrencyTier.BRONZE, 10);

// Vault-basierter Withdraw
BigDecimal withdrawn = currencyManager.withdrawCoins(player, "sterne", CurrencyTier.SILVER, 5);
// Zieht 50 Sterne vom Vault-Konto ab und gibt 5 Silbersterne

// Item-Preise verwalten
priceProvider.registerVanillaPrice(Material.DIAMOND, BigDecimal.valueOf(100));
BigDecimal price = priceProvider.getVanillaPriceOrDefault(Material.DIAMOND);
```

### Plot-Slots System
```java
// NPC-Slots auf Grundstücken
PlotSlot slot = new PlotSlot(location, PlotSlot.SlotType.TRADER);
merchantPlot.addSlot(slot);

// NPC auf Slot platzieren
slot.assignNPC(npcUuid);

// Slot-Status prüfen
if (slot.isOccupied()) {
    UUID npcId = slot.getAssignedNPC().orElse(null);
}

// Slot-Limits prüfen
int freeSlots = merchantPlot.getFreeSlots();
int maxTraders = merchantPlot.getMaxTraderSlots(); // Default: 5
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

// Guest/Owner View Pattern
HandelsgildeUI ui = new HandelsgildeUI(providers, priceCommand, plot, isOwner);
ui.open(player); // Zeigt unterschiedliche Ansicht basierend auf Besitzrechten
```

### Graceful Degradation
```java
// Items-Modul läuft MIT und OHNE MMOItems
if (mmoItemsAvailable) {
    // Full Mode: Custom Items + Vanilla Coins
} else {
    // Vanilla Mode: Nur Coins (kein Crash!)
}

// Economy-Modul läuft MIT und OHNE Vault
if (economyProvider.isAvailable()) {
    // Vault-basierte Transaktionen
} else {
    // Nur Item-basierte Wirtschaft
}
```

---

## 🔗 Links

- **GitHub:** https://github.com/sternstaub/fs-core-sample-dump
- **Dokumentation:** [CLAUDE.md](CLAUDE.md) für vollständige Architektur-Details
- **Sprint-Planung:** Siehe CLAUDE.md → Sprint-Based Development

---

**Für Details siehe [REPOSITORY_INDEX.md](REPOSITORY_INDEX.md) und [CLAUDE.md](CLAUDE.md)**
