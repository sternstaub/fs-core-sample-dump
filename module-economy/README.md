# FallenStar Economy Module

**Weltwirtschaft, Währungen, Münzsystem mit Vault-Integration**

Version: 1.0-SNAPSHOT
Sprint: 9-10 (Economy-Modul)

---

## Übersicht

Das Economy-Modul erweitert FallenStar um ein flexibles, erweiterbares Wirtschaftssystem:

- **Mehrere Währungen** mit individuellen Wechselkursen
- **Münzsystem** (Bronze/Silber/Gold - 1er/10er/100er Münzen)
- **Vault-Integration** für Economy-API-Kompatibilität
- **Basiswährung "Sterne"** (1:1 Wechselkurs)
- **Extensible** - neue Währungen einfach hinzufügbar

---

## Features

### ✅ Implementiert (Sprint 9-10)

#### 1. Währungssystem
- **CurrencyItemSet**: Währungs-Modell mit Exchange Rates
  - Bronze-Tier (1er Münze, Wert: 1)
  - Silber-Tier (10er Münze, Wert: 10)
  - Gold-Tier (100er Münze, Wert: 100)
  - Wechselkurs zur Basiswährung

#### 2. Basiswährung "Sterne"
- Bronzestern (Gold Nugget, Custom Model Data: 1)
- Silberstern (Gold Nugget, Custom Model Data: 2)
- Goldstern (Gold Nugget, Custom Model Data: 3)
- Wechselkurs: 1:1 (Referenzwährung)

#### 3. CurrencyManager
- Währungen registrieren/verwalten
- Münzen auszahlen: `payoutCoins(player, currency, tier, amount)`
- Wechselkurs-Berechnungen
- Integration mit SpecialItemManager (Items-Modul)

#### 4. Admin-Befehle
- `/fscore admin economy getcoin <währung> [tier] [anzahl]`
  - Beispiel: `/fscore admin economy getcoin sterne gold 10`
  - Tab-Completion für alle Parameter
  - Tier: bronze, silver, gold
  - Menge: 1-64

### 📋 Geplant (zukünftige Sprints)

- Vault Economy Provider
- Spieler-Balances verwalten
- Transaktionen (Einzahlen/Abheben)
- Preisberechnungen
- Währungskonvertierung
- Shop-Integration
- Quest-Rewards

---

## Architektur

### Module Dependencies

```
Economy-Modul
├── FallenStar-Core (ProviderRegistry, ProvidersReadyEvent)
├── FallenStar-Items (SpecialItemManager für Münz-Items)
└── Vault (Economy-API)
```

### Paket-Struktur

```
de.fallenstar.economy/
├── EconomyModule.java              # Main Plugin Class
├── manager/
│   └── CurrencyManager.java        # Währungsverwaltung
├── model/
│   └── CurrencyItemSet.java        # Währungs-Modell (Record)
└── provider/                       # (geplant)
    └── VaultEconomyProvider.java   # Vault-Integration
```

---

## Verwendung

### Basiswährung "Sterne"

Die Basiswährung wird automatisch beim Start registriert:

```java
// In EconomyModule.java
private void registerBaseCurrency() {
    CurrencyItemSet sterne = CurrencyItemSet.createBaseCurrency();
    currencyManager.registerCurrency(sterne);
}
```

**Items:**
- `bronze_stern` - Bronzestern (1er)
- `silver_stern` - Silberstern (10er)
- `gold_stern` - Goldstern (100er)

### Münzen auszahlen

```java
CurrencyManager manager = economyModule.getCurrencyManager();

// 10x Bronze-Sterne ausgeben
manager.payoutCoins(player, "sterne", CurrencyTier.BRONZE, 10);

// 5x Silber-Sterne ausgeben
manager.payoutCoins(player, "sterne", CurrencyTier.SILVER, 5);

// 1x Gold-Stern ausgeben
manager.payoutCoins(player, "sterne", CurrencyTier.GOLD, 1);
```

### Admin-Befehle

**Münzen an sich selbst geben:**
```
/fscore admin economy getcoin sterne bronze 10
/fscore admin economy getcoin sterne silver 5
/fscore admin economy getcoin sterne gold
```

**Hilfe anzeigen:**
```
/fscore admin economy
```

---

## Konfiguration

### plugin.yml

```yaml
name: FallenStar-Economy
version: 1.0-SNAPSHOT
main: de.fallenstar.economy.EconomyModule
api-version: 1.21

# Hard Dependencies
depend: [FallenStar-Core, FallenStar-Items, Vault]

commands:
  fseconomy:
    description: Economy Admin-Befehle
    usage: /fseconomy <subcommand>
    permission: fallenstar.economy.admin
    aliases: [fsecon, economy]
```

### config.yml (geplant)

```yaml
# Währungskonfiguration
currencies:
  sterne:
    enabled: true
    exchange_rate: 1.0  # Basiswährung
    bronze_item: "bronze_stern"
    silver_item: "silver_stern"
    gold_item: "gold_stern"

  # Weitere Währungen können hier hinzugefügt werden
  dukaten:
    enabled: false
    exchange_rate: 1.2  # 1 Dukat = 1.2 Sterne
    bronze_item: "bronze_dukat"
    silver_item: "silver_dukat"
    gold_item: "gold_dukat"
```

---

## API-Nutzung (für andere Module)

### Währung registrieren

```java
// Neue Währung "Dukaten" registrieren
CurrencyItemSet dukaten = new CurrencyItemSet(
    "dukaten",                      // ID
    "Dukaten",                      // Display Name
    "bronze_dukat",                 // Bronze-Item ID
    "silver_dukat",                 // Silber-Item ID
    "gold_dukat",                   // Gold-Item ID
    new BigDecimal("1.2")           // Wechselkurs (1 Dukat = 1.2 Sterne)
);

currencyManager.registerCurrency(dukaten);
```

### Wechselkurs-Berechnungen

```java
CurrencyItemSet dukaten = /* ... */;

// 100 Dukaten in Sterne konvertieren
BigDecimal sterne = dukaten.toBaseCurrency(new BigDecimal("100"));
// Ergebnis: 120 Sterne (100 * 1.2)

// 120 Sterne in Dukaten konvertieren
BigDecimal dukatenAmount = dukaten.fromBaseCurrency(new BigDecimal("120"));
// Ergebnis: 100 Dukaten (120 / 1.2)
```

### Tier-Werte

```java
int bronzeValue = CurrencyTier.BRONZE.getTierValue();  // 1
int silverValue = CurrencyTier.SILVER.getTierValue();  // 10
int goldValue = CurrencyTier.GOLD.getTierValue();      // 100
```

---

## Technische Details

### CurrencyItemSet (Record)

Immutable Datenstruktur für Währungen:

```java
public record CurrencyItemSet(
    String currencyId,              // Eindeutige ID
    String displayName,             // Anzeigename
    String bronzeItemId,            // SpecialItem-ID (Bronze)
    String silverItemId,            // SpecialItem-ID (Silber)
    String goldItemId,              // SpecialItem-ID (Gold)
    BigDecimal exchangeRate         // Wechselkurs
) {
    // Factory-Methode für Basiswährung
    public static CurrencyItemSet createBaseCurrency() { /* ... */ }

    // Hilfsmethoden
    public boolean isBaseCurrency() { /* ... */ }
    public BigDecimal toBaseCurrency(BigDecimal amount) { /* ... */ }
    public BigDecimal fromBaseCurrency(BigDecimal baseAmount) { /* ... */ }
    public String getItemId(CurrencyTier tier) { /* ... */ }
}
```

### CurrencyManager

Zentrale Verwaltung aller Währungen:

```java
public class CurrencyManager {
    private final Map<String, CurrencyItemSet> currencies;
    private final SpecialItemManager itemManager;
    private CurrencyItemSet baseCurrency;

    public void registerCurrency(CurrencyItemSet currency) { /* ... */ }
    public boolean payoutCoins(Player player, String currencyId,
                                CurrencyTier tier, int amount) { /* ... */ }
    public Optional<CurrencyItemSet> getCurrency(String currencyId) { /* ... */ }
    public CurrencyItemSet getBaseCurrency() { /* ... */ }
}
```

### Integration mit Items-Modul

```java
// CurrencyManager nutzt SpecialItemManager für Item-Erstellung
Optional<ItemStack> coins = itemManager.createItem(
    currency.getItemId(tier),
    amount
);

if (coins.isPresent()) {
    player.getInventory().addItem(coins.get());
}
```

---

## Dependencies

### Maven

```xml
<dependencies>
    <!-- Core Module -->
    <dependency>
        <groupId>de.fallenstar</groupId>
        <artifactId>fallenstar-core</artifactId>
        <version>${project.version}</version>
        <scope>provided</scope>
    </dependency>

    <!-- Items Module -->
    <dependency>
        <groupId>de.fallenstar</groupId>
        <artifactId>module-items</artifactId>
        <version>${project.version}</version>
        <scope>provided</scope>
    </dependency>

    <!-- Vault API -->
    <dependency>
        <groupId>com.github.MilkBowl</groupId>
        <artifactId>VaultAPI</artifactId>
        <version>1.7.1</version>
        <scope>provided</scope>
    </dependency>

    <!-- Paper API -->
    <dependency>
        <groupId>io.papermc.paper</groupId>
        <artifactId>paper-api</artifactId>
        <version>1.21.1-R0.1-SNAPSHOT</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

---

## Entwicklung

### Build

```bash
cd module-economy
mvn clean package
```

Output: `target/FallenStar-Economy-1.0-SNAPSHOT.jar`

### Installation

1. FallenStar-Core installieren
2. FallenStar-Items installieren
3. Vault installieren
4. FallenStar-Economy installieren
5. Server starten

### Testing

```bash
# Münzen ausgeben (im Spiel)
/fscore admin economy getcoin sterne bronze 10
/fscore admin economy getcoin sterne silver 5
/fscore admin economy getcoin sterne gold

# Logs prüfen
[INFO] Economy-Modul wird gestartet...
[INFO] ✓ Alle Dependencies verfügbar
[INFO] ✓ Manager initialisiert
[INFO] ✓ Basiswährung registriert: Sterne (Wechselkurs: 1)
[INFO] ✓ Economy-Modul erfolgreich initialisiert!
[INFO]   - Registrierte Währungen: 1
```

---

## Erweiterungen (Roadmap)

### Sprint 11-12: Vault-Integration
- VaultEconomyProvider implementieren
- Balance-System (getBalance, setBalance)
- Transaktionen (deposit, withdraw)

### Sprint 13-14: Preissystem
- Dynamische Preisberechnungen
- Material-basierte Preise
- Region-basierte Preise

### Sprint 15-16: Shop-System
- Admin-Shops (unendliche Vorräte)
- Player-Shops (begrenzte Vorräte)
- Shop-UIs (Integration mit UI-Modul)

---

## Bekannte Einschränkungen

1. **Nur Auszahlung**: Aktuell können nur Münzen ausgezahlt werden, nicht eingezogen
2. **Keine Balance-Persistierung**: Balances werden noch nicht gespeichert
3. **Keine Vault-Integration**: Provider noch nicht implementiert
4. **Keine GUI**: Economy-UIs kommen in späteren Sprints

---

## Lizenz

© 2025 FallenStar Development Team

---

## Support

- GitHub Issues: `https://github.com/sternstaub/fs-core-sample-dump/issues`
- Wiki: `https://github.com/sternstaub/fs-core-sample-dump/wiki`

---

**Status:** ✅ Basis-Implementierung abgeschlossen (Sprint 9-10)
**Nächster Sprint:** Sprint 11-12 - WorldAnchors (Schnellreisen, POIs, Wegpunkte)
