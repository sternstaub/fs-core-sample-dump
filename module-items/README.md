# FallenStar Items Module

**Vanilla Currency Items + Optional MMOItems Integration**

---

## 📋 Übersicht

Das Items-Modul stellt ein **Vanilla-First** Währungssystem bereit und integriert optional mit MMOItems für erweiterte Custom-Item-Funktionalität.

**Wichtig:** Das Modul funktioniert **MIT und OHNE** MMOItems!

---

## ✅ Features

### Vanilla Currency System "Sterne" (IMMER verfügbar)
- **Bronzestern** (COPPER_INGOT, CMD: 1, Wert: 1)
- **Silberstern** (IRON_INGOT, CMD: 2, Wert: 10)
- **Goldstern** (GOLD_INGOT, CMD: 3, Wert: 100)
- PDC-basierte Item-Identifikation (`fallenstar:item_id`)
- Custom Model Data für Resource Pack Support
- Währungswert-Berechnung (1:10:100 Ratio)
- **Konfigurierbare Materialien** (config.yml)

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

// Bronzesterne erstellen (50 Stück = 50 Wert)
manager.createItem("bronze_stern", 50).ifPresent(coins -> {
    player.getInventory().addItem(coins);
});

// Silbersterne erstellen (10 Stück = 100 Wert)
manager.createItem("silver_stern", 10).ifPresent(coins -> {
    player.getInventory().addItem(coins);
});

// Goldsterne erstellen (1 Stück = 100 Wert)
manager.createItem("gold_stern", 1).ifPresent(coins -> {
    player.getInventory().addItem(coins);
});
```

### Currency Items erkennen

```java
// Prüfen ob Item ein Special Item (inkl. Währung) ist
if (manager.isSpecialItem(itemInHand)) {
    // Item-ID ermitteln
    manager.getSpecialItemId(itemInHand).ifPresent(itemId -> {
        // itemId = "bronze_stern", "silver_stern" oder "gold_stern"
        player.sendMessage("Item: " + itemId);

        // Bei Währungs-Items kann der Wert berechnet werden
        if (itemId.endsWith("_stern")) {
            int amount = itemInHand.getAmount();
            int singleValue = getSingleCoinValue(itemId); // 1, 10 oder 100
            int totalValue = amount * singleValue;
            player.sendMessage("Gesamtwert: " + totalValue + " Sterne");
        }
    });
}

private int getSingleCoinValue(String itemId) {
    return switch (itemId) {
        case "bronze_stern" -> 1;
        case "silver_stern" -> 10;
        case "gold_stern" -> 100;
        default -> 0;
    };
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

## ⚙️ Konfiguration

### Währungs-Materialien anpassen

Die Materialien für die Währungs-Tiers können in der `config.yml` angepasst werden:

```yaml
# config.yml
currency:
  # Material-Typen für Währungs-Tiers
  # WICHTIG: Materialien müssen in Minecraft 1.21.1 verfügbar sein!
  bronze-tier-material: COPPER_INGOT
  silver-tier-material: IRON_INGOT
  gold-tier-material: GOLD_INGOT
```

**Wichtige Hinweise:**
- ✅ Materialien müssen in Minecraft 1.21.1 existieren
- ✅ Material-Namen müssen UPPERCASE sein (z.B. `COPPER_INGOT`, nicht `copper_ingot`)
- ❌ `COPPER_NUGGET` gibt es erst ab Minecraft 1.21.9!
- ⚠️ Bei ungültigem Material wird automatisch der Default verwendet

**Beispiel-Konfigurationen:**

```yaml
# Nuggets (nur ab MC 1.21.9!)
currency:
  bronze-tier-material: COPPER_NUGGET
  silver-tier-material: IRON_NUGGET
  gold-tier-material: GOLD_NUGGET

# Alternative Items (z.B. Edelsteine)
currency:
  bronze-tier-material: EMERALD
  silver-tier-material: DIAMOND
  gold-tier-material: NETHER_STAR

# Kreative Varianten
currency:
  bronze-tier-material: BRICK
  silver-tier-material: QUARTZ
  gold-tier-material: AMETHYST_SHARD
```

**Achtung:**
- Nach Änderung der Materialien muss der Server neu gestartet werden
- Bestehende Währungs-Items behalten ihr altes Material
- Custom Model Data bleibt erhalten (1 = Bronze, 2 = Silber, 3 = Gold)

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

## 📊 Basiswährung "Sterne" - Definitionen

| Name | Item-ID | Material (Default) | Custom Model Data | Wert | Beschreibung |
|------|---------|----------|-------------------|------|--------------|
| **Bronzestern** | `bronze_stern` | `COPPER_INGOT` | `1` | 1 | Basiswährung (1er Münze) |
| **Silberstern** | `silver_stern` | `IRON_INGOT` | `2` | 10 | Handelswährung (10er Münze) |
| **Goldstern** | `gold_stern` | `GOLD_INGOT` | `3` | 100 | Edelwährung (100er Münze) |

**Hinweis:** Materialien können in der `config.yml` geändert werden (siehe Konfiguration unten).

### Custom Model Data für Resource Packs

**Custom Model Data (CMD)** ermöglicht benutzerdefinierte Item-Texturen ohne Mod!

#### Resource Pack Integration

Erstelle einen Resource Pack mit folgenden Override-Einträgen:

**`assets/minecraft/models/item/copper_ingot.json`:**
```json
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "minecraft:item/copper_ingot"
  },
  "overrides": [
    {
      "predicate": { "custom_model_data": 1 },
      "model": "fallenstar:item/bronze_stern"
    }
  ]
}
```

**`assets/minecraft/models/item/iron_ingot.json`:**
```json
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "minecraft:item/iron_ingot"
  },
  "overrides": [
    {
      "predicate": { "custom_model_data": 2 },
      "model": "fallenstar:item/silver_stern"
    }
  ]
}
```

**`assets/minecraft/models/item/gold_ingot.json`:**
```json
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "minecraft:item/gold_ingot"
  },
  "overrides": [
    {
      "predicate": { "custom_model_data": 3 },
      "model": "fallenstar:item/gold_stern"
    }
  ]
}
```

Dann erstelle die benutzerdefinierten Modelle in:
- `assets/fallenstar/models/item/bronze_stern.json`
- `assets/fallenstar/models/item/silver_stern.json`
- `assets/fallenstar/models/item/gold_stern.json`

Und die Texturen in:
- `assets/fallenstar/textures/item/bronze_stern.png` (16x16 PNG)
- `assets/fallenstar/textures/item/silver_stern.png` (16x16 PNG)
- `assets/fallenstar/textures/item/gold_stern.png` (16x16 PNG)

#### Hinweise für Resource Pack Ersteller:
- **CMD-Werte NICHT ändern** - hardcoded im Code
- Texturen sollten münzähnlich sein
- Empfohlene Größe: 16x16 Pixel
- Format: PNG mit Transparenz
- Farbschema: Bronze (kupferfarben), Silber (grau-weiß), Gold (golden-gelb)

---

## 💡 Best Practices

### 1. Optional-Pattern verwenden
```java
// ✅ RICHTIG
manager.createItem("bronze_stern", 10).ifPresent(coins -> {
    // Verwende coins
});

// ❌ FALSCH
ItemStack coins = manager.createItem("bronze_stern", 10).get(); // NoSuchElementException!
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
