# FallenStar Items Module

**Vanilla Currency Items + Optional MMOItems Integration**

---

## 📋 Übersicht

Das Items-Modul stellt ein **Vanilla-First** Währungssystem bereit und integriert optional mit MMOItems für erweiterte Custom-Item-Funktionalität.

**Wichtig:** Das Modul funktioniert **MIT und OHNE** MMOItems!

---

## ✅ Features

### Vanilla Currency System (IMMER verfügbar)
- **Bronze Coin** (GOLD_NUGGET, CMD: 1, Wert: 1)
- **Silver Coin** (GOLD_NUGGET, CMD: 2, Wert: 10)
- **Gold Coin** (GOLD_INGOT, CMD: 1, Wert: 100)
- PDC-basierte Item-Identifikation (`fallenstar:item_id`)
- Custom Model Data für Texturpacks
- Währungswert-Berechnung

### Optional: MMOItems Integration
- MMOItemsItemProvider (nur wenn MMOItems installiert)
- ItemBrowserUI (Kategorie-basierter Item-Browser)
- TestTradeUI (Händler-Demo mit Custom Items)
- Reflection-basierter API-Zugriff (MMOItems 6.10+)

### Graceful Degradation
```
MIT MMOItems:    Full Mode (Custom Items + Vanilla Coins)
OHNE MMOItems:   Vanilla Mode (nur Coins)
```

---

## 🏗️ Architektur

### SpecialItemManager v2.0
**Vanilla ItemStack-Erzeugung ohne MMOItems-Dependency!**

```java
public class SpecialItemManager {
    // Erstellt Vanilla ItemStacks mit:
    // - Custom Model Data
    // - PDC für Item-ID
    // - Custom Display Name + Lore

    public Optional<ItemStack> createCurrency(String type, int amount);
    public boolean isCurrencyItem(ItemStack item);
    public Optional<String> getCurrencyType(ItemStack item);
    public int getCurrencyValue(ItemStack item);
}
```

**Beispiel:**
```java
SpecialItemManager manager = new SpecialItemManager(plugin, logger);

// Bronze Coin erstellen
Optional<ItemStack> coin = manager.createCurrency("bronze", 10);

// Item-Typ prüfen
if (manager.isCurrencyItem(itemStack)) {
    int value = manager.getCurrencyValue(itemStack);
    // value = 10 (10 Bronze Coins = 10 Wert)
}
```

### MMOItemsItemProvider (OPTIONAL)
**Nur aktiv wenn MMOItems installiert!**

```java
public class MMOItemsItemProvider implements ItemProvider {
    // Reflection-basierter Zugriff auf MMOItems API
    // Vermeidet MMOPlugin-Dependency-Problem

    public Optional<ItemStack> createItem(String type, String id, int amount);
    public boolean isCustomItem(ItemStack item);
    public Optional<String> getItemId(ItemStack item);
}
```

---

## 📦 Dependencies

**Required:**
- FallenStar-Core (provided)
- Paper API 1.21.1 (provided)

**Optional:**
- MMOItems 6.10+ (softdepend)
- MythicLib 1.6.2+ (required by MMOItems)

**Hinweis:** MMOItems ist **SNAPSHOT** - nur für optionale Features!

---

## 🚀 Verwendung

### Currency Items erstellen

```java
// In deinem Plugin/Modul
ItemsModule itemsModule = (ItemsModule) Bukkit.getPluginManager().getPlugin("FallenStar-Items");
SpecialItemManager manager = itemsModule.getSpecialItemManager();

// Bronze Coins erstellen
manager.createCurrency("bronze", 50).ifPresent(coins -> {
    player.getInventory().addItem(coins);
});

// Silver Coins erstellen
manager.createCurrency("silver", 10).ifPresent(coins -> {
    player.getInventory().addItem(coins);
});

// Gold Coins erstellen
manager.createCurrency("gold", 1).ifPresent(coins -> {
    player.getInventory().addItem(coins);
});
```

### Currency Items erkennen

```java
// Prüfen ob Item eine Währung ist
if (manager.isCurrencyItem(itemInHand)) {
    // Währungstyp ermitteln
    manager.getCurrencyType(itemInHand).ifPresent(type -> {
        // type = "bronze", "silver" oder "gold"
        int value = manager.getCurrencyValue(itemInHand);
        player.sendMessage("Wert: " + value);
    });
}
```

### MMOItems-Features (optional)

```java
// Nur wenn MMOItems verfügbar
if (itemsModule.getItemProvider() != null) {
    MMOItemsItemProvider provider = itemsModule.getItemProvider();

    // Custom Item erstellen
    provider.createItem("SWORD", "IRON_BLADE", 1).ifPresent(sword -> {
        player.getInventory().addItem(sword);
    });
}
```

---

## 🎮 Testbefehle

```bash
# Admin-Befehle (erfordert Permission: fallenstar.core.admin)
/fscore admin items list         # Zeigt alle Items (Placeholder)
/fscore admin items browse       # Öffnet Item-Browser (nur mit MMOItems)

# UI-Tests
/fscore admin gui items-browser      # Item-Browser UI (nur mit MMOItems)
/fscore admin gui items-trade-test   # Trade UI Demo (nur mit MMOItems)
```

---

## 📊 Währungs-Definitionen

| Währung | Material | CMD | Wert | Beschreibung |
|---------|----------|-----|------|--------------|
| **Bronze** | GOLD_NUGGET | 1 | 1 | Grundwährung |
| **Silver** | GOLD_NUGGET | 2 | 10 | Handelswährung |
| **Gold** | GOLD_INGOT | 1 | 100 | Edelwährung |

**Custom Model Data (CMD)** ermöglicht Texturpack-Integration!

---

## 💡 Best Practices

### 1. Optional-Pattern verwenden
```java
// ✅ RICHTIG
manager.createCurrency("bronze", 10).ifPresent(coins -> {
    // Verwende coins
});

// ❌ FALSCH
ItemStack coins = manager.createCurrency("bronze", 10).get(); // NoSuchElementException!
```

### 2. Graceful Degradation
```java
// Prüfe ob MMOItems verfügbar
if (itemsModule.getItemProvider() != null) {
    // Verwende MMOItems-Features
} else {
    // Fallback auf Vanilla
}
```

### 3. Currency-Wert berechnen
```java
// Berechne Gesamtwert aller Coins im Inventar
int totalValue = 0;
for (ItemStack item : player.getInventory().getContents()) {
    if (item != null && manager.isCurrencyItem(item)) {
        totalValue += manager.getCurrencyValue(item);
    }
}
```

---

## 🏆 Vorteile

### Vanilla-First Approach:
- ✅ Funktioniert OHNE externe Plugins
- ✅ Keine SNAPSHOT-Dependencies für Core-Features
- ✅ PDC-basierte Identifikation (persistent)
- ✅ Custom Model Data Support

### MMOItems Optional:
- ✅ Kein Crash wenn MMOItems fehlt
- ✅ Reflection-Wrapper für API-Stabilität
- ✅ Erweiterte Features nur wenn verfügbar

### Performance:
- ✅ Kein Reflection für Vanilla Coins
- ✅ PDC-Lookup schneller als NBT-Parsing
- ✅ Currency-Definition-Cache

---

## 🐛 Troubleshooting

### "MMOItems not found - running in Vanilla-only mode"
**Ursache:** MMOItems Plugin nicht installiert
**Lösung:** Installiere MMOItems oder verwende nur Vanilla Coins

### "Failed to create currency item"
**Ursache:** Ungültiger Currency-Type
**Lösung:** Verwende nur "bronze", "silver" oder "gold"

### "Currency items not working in UI"
**Ursache:** Vergessen `SpecialItemManager` zu übergeben
**Lösung:** Prüfe UI-Konstruktor-Parameter

---

## 🔗 Siehe auch

- **[Core README](../core/README.md)** - Provider-Interfaces
- **[CLAUDE.md](../CLAUDE.md)** - Sprint 5-6 Details
- **[UI Module README](../module-ui/README.md)** - UI-Integration

---

**Version:** 2.0
**Status:** ✅ Abgeschlossen (Sprint 5-6 + 7-8 Refactoring)
**Letzte Änderung:** 2025-11-16 (MMOItems optional gemacht)
