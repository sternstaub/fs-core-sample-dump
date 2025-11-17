# FallenStar UI Module

**Konkrete UI-Implementierungen für das FallenStar-System**

---

## 📋 Übersicht

Das UI-Modul implementiert konkrete UI-Klassen basierend auf dem Core UI-Framework. Es stellt wiederverwendbare UIs bereit, die von anderen Modulen genutzt werden können.

**Sprint 7-8** - ✅ Abgeschlossen
**Sprint 11-12** - ✅ TradeUI hinzugefügt

---

## ✅ Implementierte UIs

### 1. ConfirmationUI
**Generisches Ja/Nein Bestätigungs-Dialog**

- Layout: 9 Slots (SmallChestUI)
- Grüne Wolle (Ja) - Slot 3
- Rote Wolle (Nein) - Slot 5
- Barriere (Schließen) - Slot 8 (oben rechts)

**Features:**
- Factory-Methoden für einfache Erstellung
- Customizable Callbacks
- UI-Button Integration

**Verwendung:**
```java
ConfirmationUI ui = ConfirmationUI.createSimple(
    buttonManager,
    "Möchtest du diese Aktion ausführen?",
    player -> player.sendMessage("Bestätigt!")
);
ui.open(player);
```

### 2. SimpleTradeUI
**Vanilla Trading Demo**

- Layout: 54 Slots (LargeChestUI)
- 6 Trade-Angebote mit Input1 + Input2 → Output
- Demo-Implementierung mit Vanilla-Items
- Testdaten für verschiedene Trades

**Features:**
- Input/Output Visualisierung
- Click-Handler für Trades
- Vanilla-only (keine MMOItems-Dependency)

**Trades:**
1. 10 Gold + 5 Redstone → Diamant
2. 20 Eisen → 5 Gold
3. 32 Kohle + 16 Holz → 8 Fackeln
4. 5 Diamanten → Verzauberter Bogen
5. 64 Weizen + 32 Karotten → 16 Goldene Karotten
6. 16 Smaragde → Elytra

### 3. TradeUI (Sprint 11-12)
**Dynamisches Trading-Interface für TradingEntities**

- Nutzt Vanilla Merchant Interface
- Dynamische TradeSets von TradingEntity
- Reflection-basierter TradeSet-Zugriff (Economy-Modul)
- Inventar-Validierung gegen TradingEntity.getTradeInventory()

**Features:**
- Universell für alle TradingEntity-Typen
- Automatische MerchantRecipe-Konvertierung
- Ankauf/Verkauf-Logik aus TradeSet
- Max-Uses Unterstützung

**Verwendung:**
```java
// Öffne Trading-UI für beliebige TradingEntity
TradingEntity trader = getGuildTrader();  // Gildenhändler
TradeUI.openTradeUI(player, trader);

// Funktioniert mit allen Typen:
// - GUILD_TRADER (Gildenhändler)
// - PLAYER_TRADER (Spielerhändler)
// - TRAVELING_MERCHANT (Fahrender Händler)
// - WORLD_BANKER (Weltbankier)
```

**Unterstützte TradingEntity-Typen:**
1. **GUILD_TRADER** - Gildenhändler (nutzt Plot-Storage)
2. **PLAYER_TRADER** - Spielerhändler (nutzt virtuelles Inventar)
3. **TRAVELING_MERCHANT** - Fahrender Händler (eigenes Inventar)
4. **WORLD_BANKER** - Weltbankier (unbegrenztes Inventar)

---

## 🛠️ UIButtonManager

**Verwaltet wiederverwendbare UI-Button Items**

### Button-Typen:
- **CONFIRM** - Grüne Wolle (Bestätigen)
- **CANCEL** - Rote Wolle (Abbrechen)
- **CLOSE** - Barriere (Schließen)
- **INFO** - Buch (Information)
- **NEXT** - Pfeil (Weiter »)
- **PREVIOUS** - Pfeil (« Zurück)
- **BACK** - Spectral Arrow (↶ Zurück)

### Verwendung:

```java
UIButtonManager buttonManager = new UIButtonManager();
buttonManager.initialize();

// Standard-Button erstellen
Optional<ItemStack> confirmButton = buttonManager.createConfirmButton();
Optional<ItemStack> cancelButton = buttonManager.createCancelButton();
Optional<ItemStack> closeButton = buttonManager.createCloseButton();

// Button mit Custom-Text
Optional<ItemStack> customConfirm = buttonManager.createButton(
    ButtonType.CONFIRM,
    Component.text("Ja, sicher!", NamedTextColor.GREEN),
    List.of(Component.text("Klicke um zu bestätigen"))
);
```

---

## 🎮 Testbefehle

```bash
# Alle UIs auflisten
/fscore admin gui list

# ConfirmationUI öffnen
/fscore admin gui confirm

# SimpleTradeUI öffnen (Vanilla-Demo)
/fscore admin gui trade

# TradeUI - wird über TradingEntity geöffnet (kein direkter Testbefehl)
# Beispiel: Rechtsklick auf Gildenhändler-NPC
```

---

## 🏗️ Architektur

### UIModule
**Haupt-Modul-Klasse**

```java
public class UIModule extends JavaPlugin implements Listener {
    private UIButtonManager buttonManager;
    private UIRegistry uiRegistry;

    @EventHandler
    public void onProvidersReady(ProvidersReadyEvent event) {
        // Initialisiere Manager
        initializeManagers();

        // Registriere UIs in UIRegistry
        registerUIs();
    }
}
```

### UI-Registrierung

```java
// UIs werden beim Start automatisch registriert
uiRegistry.registerUI(
    "confirm",                                  // UI-ID
    "Bestätigungs-Dialog (Ja/Nein)",          // Display Name
    "Generisches Ja/Nein Confirmation UI",    // Beschreibung
    () -> ConfirmationUI.createSimple(...)    // Factory
);
```

---

## 📦 Dependencies

**Required:**
- FallenStar-Core (provided) - UI-Framework, UIRegistry
- Paper API 1.21.1 (provided)

**Optional:**
- Keine!

---

## 💡 Best Practices

### 1. UI-Button Manager verwenden

```java
// ✅ RICHTIG - Wiederverwendbare Buttons
buttonManager.createConfirmButton().ifPresent(button -> {
    setItem(slot, button, clickHandler);
});

// ❌ FALSCH - Buttons manuell erstellen
ItemStack button = new ItemStack(Material.GREEN_WOOL);
// ... viel Boilerplate-Code
```

### 2. Factory-Methoden nutzen

```java
// ✅ RICHTIG - Factory für einfache Fälle
ConfirmationUI ui = ConfirmationUI.createSimple(
    buttonManager, message, onConfirm
);

// ✅ AUCH RICHTIG - Konstruktor für komplexe Fälle
ConfirmationUI ui = new ConfirmationUI(
    buttonManager, title, message, onConfirm, onCancel
);
```

### 3. UI-Registrierung

```java
// UIs in UIRegistry registrieren für Testbarkeit
uiRegistry.registerUI(
    "my-custom-ui",
    "Display Name",
    "Beschreibung",
    () -> new MyCustomUI(dependencies...)
);

// Dann über /fscore admin gui my-custom-ui testbar!
```

---

## 🎨 Eigene UIs erstellen

### Beispiel: Custom UI

```java
public class MyCustomUI extends SmallChestUI {
    private final UIButtonManager buttonManager;

    public MyCustomUI(UIButtonManager buttonManager) {
        super("§aMein Custom UI");
        this.buttonManager = buttonManager;
    }

    @Override
    public void open(Player player) {
        clearItems();

        // Dein UI-Layout hier
        buttonManager.createInfoButton(List.of(
            Component.text("Willkommen!")
        )).ifPresent(info -> {
            setItem(4, info, null);
        });

        // Close-Button
        buttonManager.createCloseButton().ifPresent(close -> {
            setItem(8, close, this::close);
        });

        super.open(player);
    }
}
```

### UI registrieren

```java
// In deinem Modul (onProvidersReady):
UIRegistry uiRegistry = core.getUIRegistry();
UIButtonManager buttonManager = uiModule.getButtonManager();

uiRegistry.registerUI(
    "my-custom-ui",
    "Mein Custom UI",
    "Beschreibung...",
    () -> new MyCustomUI(buttonManager)
);
```

---

## 🏆 Features

### Vanilla-First:
- ✅ Keine externen Dependencies
- ✅ Funktioniert out-of-the-box
- ✅ Leichtgewichtig

### Wiederverwendbar:
- ✅ UIButtonManager für konsistente Buttons
- ✅ Factory-Pattern für einfache Erstellung
- ✅ Event-Driven (Consumer<Player> Callbacks)

### Testbar:
- ✅ UIRegistry-Integration
- ✅ `/fscore admin gui` Testbefehle
- ✅ Schnelles Prototyping

---

## 📊 UI-Layout Referenz

### ConfirmationUI Layout (9 Slots)
```
[ ] [ ] [📝] [✓] [ ] [✗] [ ] [ ] [🚫]
 0   1   2    3   4   5   6   7   8
        Msg  Ja       Nein         Close
```

### SimpleTradeUI Layout (54 Slots)
```
Reihe 1: [═══════════ Header ═══════════]
Reihe 2: [Trade 1] [Trade 2] ...
Reihe 3-5: Trade-Angebote (Input1 + Input2 → Output)
Reihe 6: [═══════════ Footer ═══════════]
          ... [Close] ...
```

---

## 🔗 Integration mit anderen Modulen

### Items-Modul
```java
// UI-Modul nutzt SpecialItemManager für Currency-Items
SpecialItemManager manager = itemsModule.getSpecialItemManager();
manager.createCurrency("bronze", 10).ifPresent(coins -> {
    // Verwende in UI
});
```

### Economy-Modul (geplant)
```java
// Trading-UIs können EconomyProvider nutzen
if (economyProvider.isAvailable()) {
    // Echte Währungs-Transaktionen
}
```

---

## 🐛 Troubleshooting

### "UI schließt sofort nach Öffnen"
**Ursache:** Keine Items gesetzt oder `super.open()` nicht aufgerufen
**Lösung:** Prüfe dass `super.open(player)` am Ende von `open()` steht

### "Buttons haben kein Icon"
**Ursache:** `UIButtonManager.initialize()` nicht aufgerufen
**Lösung:** Rufe `initialize()` beim Start auf

### "Click-Handler funktioniert nicht"
**Ursache:** UI nicht als Event-Listener registriert
**Lösung:** BaseUI implementiert Listener automatisch - prüfe `setItem()` Call

---

## 🔗 Siehe auch

- **[Core README](../core/README.md)** - UI-Framework Dokumentation
- **[Items README](../module-items/README.md)** - Currency Items
- **[CLAUDE.md](../CLAUDE.md)** - Sprint 7-8 Details

---

**Version:** 1.0
**Status:** ✅ Abgeschlossen (Sprint 7-8)
**Letzte Änderung:** 2025-11-16
