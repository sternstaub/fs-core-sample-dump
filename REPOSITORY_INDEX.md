# FallenStar Paper Samples - Complete Repository Index

## 📦 Vollständige Repository-Struktur

```
FallenStar-Paper-Samples/
│
├── README.md                           ← Haupt-README (erstellt)
├── CONTRIBUTING.md                     ← Contribution Guidelines (erstellt)
├── pom.xml                            ← Parent POM (vorhanden)
├── .gitignore
│
├── docs/                              ← Dokumentation
│   ├── ARCHITECTURE.md                ← System-Design
│   ├── DEVELOPMENT_ROADMAP.md         ← Sprint-Planung
│   ├── PROJECT_SUMMARY.md             ← Quick Overview
│   ├── PROVIDER_GUIDE.md              ← Provider-System Guide
│   └── MODULE_DEVELOPMENT.md          ← Modul-Entwicklung Guide
│
├── core/                              ← Core Plugin
│   ├── README.md                      ← Core-Dokumentation
│   ├── pom.xml                        ← Core POM
│   └── src/main/
│       ├── java/de/fallenstar/core/
│       │   ├── FallenStarCore.java    ← Haupt-Klasse
│       │   │
│       │   ├── provider/              ← Provider Interfaces
│       │   │   ├── PlotProvider.java
│       │   │   ├── Plot.java
│       │   │   ├── EconomyProvider.java
│       │   │   ├── NPCProvider.java
│       │   │   ├── ItemProvider.java
│       │   │   ├── ChatProvider.java
│       │   │   ├── NetworkProvider.java
│       │   │   │
│       │   │   └── impl/              ← Concrete Implementations
│       │   │       ├── TownyPlotProvider.java
│       │   │       ├── NoOpPlotProvider.java
│       │   │       ├── NoOpEconomyProvider.java
│       │   │       ├── NoOpNPCProvider.java
│       │   │       ├── NoOpItemProvider.java
│       │   │       ├── VaultEconomyProvider.java
│       │   │       └── CitizensNPCProvider.java
│       │   │
│       │   ├── registry/
│       │   │   └── ProviderRegistry.java
│       │   │
│       │   ├── exception/
│       │   │   └── ProviderFunctionalityNotFoundException.java
│       │   │
│       │   ├── event/
│       │   │   └── ProvidersReadyEvent.java
│       │   │
│       │   └── database/
│       │       ├── DataStore.java
│       │       └── impl/
│       │           ├── SQLiteDataStore.java
│       │           └── MySQLDataStore.java
│       │
│       └── resources/
│           ├── plugin.yml
│           └── config.yml
│
├── module-storage/                    ← Storage Module
│   ├── README.md
│   ├── pom.xml
│   └── src/main/
│       ├── java/de/fallenstar/storage/
│       │   ├── StorageModule.java     ← Haupt-Klasse
│       │   │
│       │   ├── command/               ← Commands
│       │   │   ├── StorageRegisterCommand.java
│       │   │   ├── StorageListCommand.java
│       │   │   └── StorageInfoCommand.java
│       │   │
│       │   ├── manager/               ← Business Logic
│       │   │   ├── ChestManager.java
│       │   │   └── MaterialTracker.java
│       │   │
│       │   ├── model/                 ← Data Models
│       │   │   ├── RegisteredChest.java
│       │   │   └── StorageData.java
│       │   │
│       │   └── listener/              ← Event Handlers
│       │       └── ChestInteractListener.java
│       │
│       └── resources/
│           ├── plugin.yml
│           └── config.yml
│
├── module-merchants/                  ← Merchants Module
│   ├── README.md
│   ├── pom.xml
│   └── src/main/
│       ├── java/de/fallenstar/merchants/
│       │   ├── MerchantsModule.java
│       │   │
│       │   ├── command/
│       │   │   ├── MerchantCreateCommand.java
│       │   │   ├── MerchantOfferCommand.java
│       │   │   └── MerchantRemoveCommand.java
│       │   │
│       │   ├── manager/
│       │   │   ├── MerchantManager.java
│       │   │   └── TradeManager.java
│       │   │
│       │   ├── model/
│       │   │   ├── Merchant.java
│       │   │   └── TradeOffer.java
│       │   │
│       │   └── gui/
│       │       └── TradeGUI.java
│       │
│       └── resources/
│           ├── plugin.yml
│           └── config.yml
│
├── module-travel/                     ← TravelSystem Module
│   ├── README.md
│   ├── pom.xml
│   └── src/main/
│       ├── java/de/fallenstar/travel/
│       │   ├── TravelModule.java
│       │   │
│       │   ├── command/
│       │   │   ├── ContractCreateCommand.java
│       │   │   └── ContractListCommand.java
│       │   │
│       │   ├── manager/
│       │   │   ├── ContractManager.java
│       │   │   └── TravelManager.java
│       │   │
│       │   ├── model/
│       │   │   ├── MerchantContract.java
│       │   │   └── TravelState.java
│       │   │
│       │   └── task/
│       │       └── TravelTask.java
│       │
│       └── resources/
│           ├── plugin.yml
│           └── config.yml
│
└── module-adminshops/                 ← AdminShops Module
    ├── README.md
    ├── pom.xml
    └── src/main/
        ├── java/de/fallenstar/adminshops/
        │   ├── AdminShopsModule.java
        │   │
        │   ├── command/
        │   │   ├── AdminShopCreateCommand.java
        │   │   └── AdminShopReloadCommand.java
        │   │
        │   ├── manager/
        │   │   ├── TemplateManager.java
        │   │   └── PricingEngine.java
        │   │
        │   ├── model/
        │   │   ├── ShopTemplate.java
        │   │   └── AdminShop.java
        │   │
        │   └── config/
        │       └── TemplateLoader.java
        │
        └── resources/
            ├── plugin.yml
            ├── config.yml
            └── templates/
                ├── potion-merchant.yml
                └── weapon-merchant.yml
```

---

## 🚀 Setup-Anleitung

### 1. Repository klonen

```bash
git clone <repo-url>
cd FallenStar-Paper-Samples
```

### 2. Build

```bash
mvn clean package
```

Dies erstellt:
- `core/target/FallenStar-Core-1.0.jar`
- `module-storage/target/FallenStar-Storage-1.0.jar`
- `module-merchants/target/FallenStar-Merchants-1.0.jar`
- etc.

### 3. Installation

```bash
cp */target/*.jar /path/to/server/plugins/
```

---

## 📝 Fehlende Dateien erstellen

### Für lokale Entwicklung

Alle Sample-Java-Files sind bereits in diesem Repository vorhanden.
Die unten aufgeführten Files sollten für eine vollständige Implementation erstellt werden:

#### Core Module - Fehlende Implementations

```java
// core/src/main/java/de/fallenstar/core/provider/impl/

NoOpEconomyProvider.java
NoOpNPCProvider.java
NoOpItemProvider.java
VaultEconomyProvider.java
CitizensNPCProvider.java
```

#### Core Module - Database Implementations

```java
// core/src/main/java/de/fallenstar/core/database/impl/

SQLiteDataStore.java
MySQLDataStore.java
```

#### Storage Module - Zusätzliche Klassen

```java
// module-storage/src/main/java/de/fallenstar/storage/

command/StorageListCommand.java
command/StorageInfoCommand.java
manager/ChestManager.java
manager/MaterialTracker.java
model/RegisteredChest.java
model/StorageData.java
listener/ChestInteractListener.java
```

#### Merchants Module - Vollständige Implementation

```java
// module-merchants/src/main/java/de/fallenstar/merchants/

command/MerchantCreateCommand.java
command/MerchantOfferCommand.java
command/MerchantRemoveCommand.java
manager/MerchantManager.java
manager/TradeManager.java
model/Merchant.java
model/TradeOffer.java
gui/TradeGUI.java
```

#### Travel Module - Vollständige Implementation

```java
// module-travel/src/main/java/de/fallenstar/travel/

command/ContractCreateCommand.java
command/ContractListCommand.java
manager/ContractManager.java
manager/TravelManager.java
model/MerchantContract.java
model/TravelState.java (Enum)
task/TravelTask.java
```

#### AdminShops Module - Vollständige Implementation

```java
// module-adminshops/src/main/java/de/fallenstar/adminshops/

command/AdminShopCreateCommand.java
command/AdminShopReloadCommand.java
manager/TemplateManager.java
manager/PricingEngine.java
model/ShopTemplate.java
model/AdminShop.java
config/TemplateLoader.java
```

---

## 📚 Vorhanden im Repository

### ✅ Bereits erstellt

**Core Files:**
- ✅ PlotProvider.java
- ✅ Plot.java
- ✅ EconomyProvider.java
- ✅ NPCProvider.java
- ✅ ItemProvider.java
- ✅ ChatProvider.java
- ✅ NetworkProvider.java
- ✅ TownyPlotProvider.java
- ✅ NoOpPlotProvider.java
- ✅ FallenStarCore.java
- ✅ ProviderRegistry.java
- ✅ ProviderFunctionalityNotFoundException.java
- ✅ ProvidersReadyEvent.java
- ✅ DataStore.java

**Storage Module:**
- ✅ StorageModule.java
- ✅ StorageRegisterCommand.java

**Documentation:**
- ✅ README.md (Root)
- ✅ CONTRIBUTING.md
- ✅ ARCHITECTURE.md
- ✅ DEVELOPMENT_ROADMAP.md
- ✅ PROJECT_SUMMARY.md

**Build Files:**
- ✅ pom.xml (Parent)
- ✅ plugin.yml (Core)
- ✅ config.yml (Core)

---

## 🎯 Nächste Schritte

### Für Sprint 1 (Core Implementation)

1. **Core-Dateien reviewen:**
   - Provider-Interfaces verstehen
   - ProviderRegistry-Logic nachvollziehen
   - Exception-Handling-Pattern verstehen

2. **Fehlende NoOp-Provider erstellen:**
   - Folge dem Pattern von NoOpPlotProvider
   - Alle werfen ProviderFunctionalityNotFoundException

3. **DataStore-Implementationen:**
   - SQLiteDataStore für MVP
   - MySQLDataStore für Production (später)

4. **Testing:**
   - Core-Plugin kompiliert
   - Provider werden erkannt
   - ProvidersReadyEvent wird gefeuert

### Für Sprint 3 (Storage Module)

1. **Storage-Commands implementieren:**
   - Register, List, Info
   - Folge dem Pattern von StorageRegisterCommand

2. **Manager-Klassen:**
   - ChestManager für Truhen-Verwaltung
   - MaterialTracker für Counting

3. **Event-Listener:**
   - ChestInteractListener für Sneak+Rechtsklick

4. **Testing:**
   - Truhen registrieren funktioniert
   - Material-Tracking korrekt
   - Persistence über Restart

---

## 💡 Entwicklungs-Tipps

### Templates nutzen

Jede vorhandene Klasse ist ein Template:
- `StorageModule.java` → Template für alle Module
- `StorageRegisterCommand.java` → Template für Commands
- `NoOpPlotProvider.java` → Template für NoOp-Provider

### Pattern wiederholen

Die Architektur ist konsistent:
```
Module/
├── {Module}Main.java        (wie StorageModule)
├── command/                  (Commands)
├── manager/                  (Business Logic)
├── model/                    (Data Classes)
└── listener/ oder gui/      (Feature-specific)
```

### Dokumentation

Jede neue Klasse braucht:
- Javadoc-Header
- Method-Dokumentation
- Inline-Kommentare für komplexe Logik

---

## 📊 Status

**Repository-Struktur:** ✅ Vollständig definiert  
**Core Samples:** ✅ 80% erstellt  
**Module Samples:** ⚠️ 30% erstellt (Storage Module)  
**Dokumentation:** ✅ 90% erstellt  
**Build-Files:** ✅ Vollständig

**Nächster Schritt:** Core-Plugin vollständig implementieren (Sprint 1)

---

**Dieses Repository ist ein lebendiges Template. Kopiere, modifiziere, erweitere!** 🚀
