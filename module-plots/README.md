# FallenStar Plots Module

**Plot-System mit Towny-Integration und erweiterten Features**

Version: 1.0-SNAPSHOT
Sprint: 3-4 (Basis) + 11-12 (Erweiterte Features)

---

## Übersicht

Das Plots-Modul erweitert FallenStar um ein flexibles Grundstücks-Verwaltungssystem mit Towny-Integration:

- **Plot-System** - Grundstücks-Verwaltung über Towny
- **Storage-System** - Plot-gebundene Lagerung von Items
- **PlotRegistry** - Auto-Registration spezieller Grundstückstypen
- **Slot-System** - NPC-Platzierung auf Grundstücken
- **Virtuelles Inventar** - Persistentes Händler-Inventar
- **NPC-Reisesystem** - NPCs reisen zwischen Grundstücken
- **NPC-Skin-Pool** - Zufällige Skins für NPC-Typen
- **Plot-Namen** - Benutzerdefinierte Namen für Grundstücke

---

## Features

### ✅ Implementiert (Sprint 3-4)

#### 1. Plot-Provider (Towny-Integration)
- **TownyPlotProvider**: Wrapper für Towny-API
  - Plot-Informationen abrufen
  - Owner-Checks
  - Plot-Typ-Erkennung
  - Graceful Degradation (NoOpPlotProvider als Fallback)

#### 2. Storage-System
- **PlotStorageData**: Plot-gebundene Material-Speicherung
  - Konfigurierbare Materialien pro Plot
  - Empfangskiste (Receiver Chest)
  - Persistent via Config

- **PlotStorageManager**: Verwaltung aller Plot-Storages
  - Storage für Grundstück abrufen
  - Neues Storage erstellen
  - Config-basierte Persistierung

#### 3. Commands
- `/plot storage setreceiver` - Empfangskiste setzen (Owner-only)
- `/plot storage scan` - Storage-Materialien scannen (Owner-only)
- `/plot storage view` - Storage-Materialien anzeigen

### ✅ Implementiert (Sprint 11-12)

#### 4. PlotRegistry
**Zentrale Registry für spezielle Grundstückstypen**

Features:
- Auto-Registration via Towny-Events
- Auto-Deregistration bei Plot-Typ-Änderung/Löschung
- 4 Plot-Typen: MERCHANT_GUILD, EMBASSY, BANK, WORKSHOP
- Persistent in Config gespeichert

Verwendung:
```java
// Plot als Handelsgilde registrieren
plotRegistry.registerPlot(plot, PlotType.MERCHANT_GUILD);

// Alle Handelsgilden abrufen
List<String> guilds = plotRegistry.getPlotIdsByType(PlotType.MERCHANT_GUILD);
```

Towny-Integration:
- Automatische Registration bei TownBlockSettingsChangedEvent
- Automatische De-Registration bei DeleteTownEvent

#### 5. Virtuelles Händler-Inventar
**Persistentes 54-Slot-Inventar für Spielerhändler**

Features:
- Plot-gebunden (nicht weltbasiert)
- 54 Slots (LargeChest-Größe)
- Base64-Serialisierung für ItemStacks
- Persistent in Config

Verwendung:
```java
// Inventar für Spieler und Plot erstellen
VirtualTraderInventory inv = inventoryManager.getOrCreateInventory(playerId, plot);

// Inventar öffnen (Bearbeitung)
inv.open(player);

// Inventar laden/speichern
inv.loadFromConfig(config);
inv.saveToConfig(config);
```

Manager:
- VirtualTraderInventoryManager verwaltet alle Inventare
- Thread-safe mit ConcurrentHashMap
- Automatisches Speichern bei Änderungen

#### 6. Slot-Verwaltungs-GUI
**UI zum Platzieren von Händlern auf Slots**

Features:
- Zeigt alle verfügbaren Slots auf Grundstück
- Händler auf Slots platzieren
- Händler von Slots entfernen
- Neue Slots kaufen (Kosten konfigurierbar)

Workflow:
1. Spieler öffnet `/plot gui` auf Grundstück
2. Klickt auf "Händler-Slots verwalten"
3. Sieht Liste freier Slots
4. Klickt auf Slot → Händler-Auswahl-UI
5. Wählt Händler aus PlotRegistry-Handelsgilden
6. Händler reist zum Slot (NPC-Reisesystem)

UIs:
- **SlotManagementUI**: Slot-Übersicht und Verwaltung
- **TraderSelectionUI**: Händler-Auswahl aus Handelsgilden

#### 7. NPC-Reisesystem
**NPCs reisen zwischen Grundstücken mit Verzögerung und Kosten**

Features:
- Verzögerung: 10 Sekunden pro Chunk-Entfernung
- Kosten: 5 Sterne pro Chunk-Entfernung
- Manhattan-Distance-Berechnung (X + Z Chunks)
- Restart-Safe: Aktive Reisen überleben Server-Neustarts

Verwendung:
```java
// NPC-Reise starten
TravelTicket ticket = travelSystem.startTravel(npcId, fromPlot, toSlot);

// Kosten berechnen
BigDecimal cost = travelSystem.calculateTravelCost(fromLoc, toLoc);

// Dauer berechnen
int seconds = travelSystem.calculateTravelTime(fromLoc, toLoc);
```

Restart-Handling:
- Aktive Reisen in Config gespeichert
- Bei Server-Start: Laufende Reisen fortsetzen oder abschließen
- Abgeschlossene Reisen: NPC direkt ans Ziel teleportieren

#### 8. NPC-Skin-Pool
**Zufällige Skins für verschiedene NPC-Typen**

Features:
- 5 NPC-Typen: TRADER, BANKER, AMBASSADOR, CRAFTSMAN, TRAVELING
- Admin setzt Skin-Pool pro Typ
- Zufällige Skin-Auswahl bei NPC-Erstellung
- Default-Skins (MHF_Villager, etc.)

Verwendung:
```java
// Skin hinzufügen
skinPool.addSkin(NPCType.TRADER, "Notch");

// Zufälligen Skin abrufen
String skin = skinPool.getRandomSkin(NPCType.TRADER);

// Bei NPC-Erstellung verwenden
npc.data().set(NPC.PLAYER_SKIN_UUID_METADATA, skin);
```

Default-Skins:
- **TRADER**: MHF_Villager, MHF_Alex, MHF_Steve
- **BANKER**: MHF_Villager, Notch
- **AMBASSADOR**: jeb_, Dinnerbone
- **CRAFTSMAN**: MHF_ArrowUp, MHF_ArrowDown
- **TRAVELING**: MHF_Villager

#### 9. Plot-Namen-Feature
**Benutzerdefinierte Namen für Grundstücke**

Features:
- Custom-Namen setzen (max. 32 Zeichen)
- Validierung: Nur Buchstaben, Zahlen, Leerzeichen, -, _
- Fallback zu Default-Namen ("Plot #123")
- Persistent in Config

Interface:
```java
public interface NamedPlot extends Plot {
    Optional<String> getCustomName();
    void setCustomName(String name);
    void clearCustomName();
    String getDisplayName();  // Custom oder Default
}
```

Verwendung:
```java
// Namen setzen
NamedPlot plot = (NamedPlot) plotProvider.getPlot(location);
plot.setCustomName("Meine Handelsgilde");

// Namen abrufen
String displayName = plot.getDisplayName();  // "Meine Handelsgilde"

// Namen löschen
plot.clearCustomName();
```

Integration:
- **Owner GUI**: Button zum Namen setzen
- **Plot-Listen**: Custom-Namen in Listen anzeigen
- **PlotInfo-Command**: Custom-Namen in `/plot info`

### 📋 Geplant (Sprint 13-14)

- Citizens-Integration (NPCProvider)
- Konkrete NPC-Implementierungen (GuildTraderNPC, PlayerTraderNPC, BankerNPC)
- NPC-Spawning auf Slots mit Citizens
- PlotSlot-Klasse vervollständigen

---

## Architektur

### Module Dependencies

```
Plots-Modul
├── FallenStar-Core (ProviderRegistry, ProvidersReadyEvent, TradingEntity)
├── FallenStar-Items (SpecialItemManager für Münzen)
└── Towny (Plot-API)
```

### Paket-Struktur

```
de.fallenstar.plot/
├── PlotsModule.java                # Main Plugin Class
├── provider/
│   └── TownyPlotProvider.java      # Towny-Integration
├── command/
│   ├── PlotStorageCommand.java     # Storage-Befehle
│   └── PlotPriceCommand.java       # Preis-Befehle
├── manager/
│   └── PlotStorageManager.java     # Storage-Manager
├── model/
│   ├── PlotStorageData.java        # Storage-Datenmodell
│   └── NamedPlot.java              # Plot-Namen Interface
├── registry/
│   ├── PlotRegistry.java           # Plot-Typ-Registry
│   └── PlotRegistryListener.java   # Towny-Event-Listener
├── trader/
│   ├── VirtualTraderInventory.java         # Virtuelles Inventar
│   └── VirtualTraderInventoryManager.java  # Inventar-Manager
├── ui/
│   ├── SlotManagementUI.java       # Slot-Verwaltungs-GUI
│   ├── TraderSelectionUI.java      # Händler-Auswahl-GUI
│   └── PlotNameInputUI.java        # Namen-Eingabe-UI
├── npc/
│   ├── NPCTravelSystem.java        # NPC-Reisesystem
│   ├── TravelTicket.java           # Reise-Ticket
│   └── NPCSkinPool.java            # Skin-Pool
└── listener/
    └── PriceSetListener.java       # Plot-Preis-Events
```

---

## Verwendung

### PlotRegistry

**Plot registrieren:**
```java
PlotRegistry registry = plotsModule.getPlotRegistry();

// Manuell registrieren
registry.registerPlot(plot, PlotRegistry.PlotType.MERCHANT_GUILD);

// Alle Handelsgilden abrufen
List<String> guilds = registry.getPlotIdsByType(PlotType.MERCHANT_GUILD);

// Plot-Typ prüfen
Optional<PlotType> type = registry.getPlotType(plot);
```

**Automatische Registration (via Towny):**
- Plot-Typ auf COMMERCIAL setzen → Auto-Registration als MERCHANT_GUILD
- Plot löschen → Auto-Deregistration

### Virtuelles Händler-Inventar

**Inventar erstellen und verwalten:**
```java
VirtualTraderInventoryManager manager = plotsModule.getInventoryManager();

// Inventar für Spieler und Plot erstellen
VirtualTraderInventory inv = manager.getOrCreateInventory(playerId, plot);

// Inventar öffnen (GUI)
inv.open(player);

// Contents abrufen
ItemStack[] items = inv.getContents();

// Contents setzen
inv.setContents(newItems);
```

**Speichern/Laden:**
```java
// Beim Modul-Start laden
inventoryManager.loadFromConfig(getConfig());

// Nach jeder Änderung speichern
inventoryManager.saveToConfig(getConfig());
saveConfig();
```

### NPC-Reisesystem

**Reise starten:**
```java
NPCTravelSystem travelSystem = plotsModule.getTravelSystem();

// Reise starten
TravelTicket ticket = travelSystem.startTravel(
    npcUuid,      // NPC ID
    fromPlot,     // Start-Plot
    toSlot        // Ziel-Slot
);

// Kosten und Dauer abrufen
BigDecimal cost = ticket.getCost();
int seconds = ticket.getDurationSeconds();

// Verbleibende Zeit prüfen
int remaining = ticket.getRemainingSeconds();
boolean done = ticket.isComplete();
```

**Restart-Handling:**
```java
// In onEnable()
travelSystem.loadActiveTravel(getConfig());

// In onDisable()
travelSystem.saveActiveTravel(getConfig());
saveConfig();
```

### NPC-Skin-Pool

**Skins verwalten:**
```java
NPCSkinPool skinPool = plotsModule.getSkinPool();

// Skin hinzufügen
skinPool.addSkin(NPCType.TRADER, "Notch");
skinPool.addSkin(NPCType.TRADER, "jeb_");

// Zufälligen Skin abrufen
String randomSkin = skinPool.getRandomSkin(NPCType.TRADER);

// Alle Skins für Typ
List<String> skins = skinPool.getSkins(NPCType.TRADER);
```

**Config-Persistierung:**
```yaml
# config.yml
skin-pools:
  TRADER:
    - "Notch"
    - "jeb_"
    - "MHF_Villager"
  BANKER:
    - "MHF_Villager"
```

### Plot-Namen

**Namen setzen:**
```java
PlotNameManager nameManager = plotsModule.getNameManager();

// Namen setzen
nameManager.setCustomName(plotId, "Meine Handelsgilde");

// Namen abrufen
Optional<String> name = nameManager.getCustomName(plotId);

// Namen löschen
nameManager.clearCustomName(plotId);
```

**UI-Integration:**
```java
// In HandelsgildeUI (Owner-View)
ItemStack nameButton = new ItemStack(Material.NAME_TAG);
// ... (Button konfigurieren)

setItem(slot, nameButton, player -> {
    PlotNameInputUI.openNameInput(player, plot, nameManager, name -> {
        player.sendMessage("§aName gesetzt: " + name);
    });
});
```

---

## Konfiguration

### plugin.yml

```yaml
name: FallenStar-Plots
version: 1.0-SNAPSHOT
main: de.fallenstar.plot.PlotsModule
api-version: 1.21

# Hard Dependencies
depend: [FallenStar-Core, Towny]

# Optional Dependencies
softdepend: [FallenStar-Items]

commands:
  plot:
    description: Plot-Verwaltung
    usage: /plot <subcommand>
    permission: fallenstar.plot.use
```

### config.yml

```yaml
# Plot-Registry
plot-registry:
  auto-register: true
  types:
    - MERCHANT_GUILD
    - EMBASSY
    - BANK
    - WORKSHOP

# Virtuelles Händler-Inventar
trader-inventories:
  player-uuid-123:
    plot-id: "plot-456"
    contents: "base64-encoded-items..."

# NPC-Reisesystem
travel-system:
  seconds-per-chunk: 10
  cost-per-chunk: 5
  active-travels:
    npc-uuid-789:
      from:
        world: "world"
        x: 100
        y: 64
        z: 200
      to:
        world: "world"
        x: 500
        y: 64
        z: 600
      start-time: 1234567890
      duration: 200
      cost: 50.0

# NPC-Skin-Pool
skin-pools:
  TRADER:
    - "MHF_Villager"
    - "MHF_Alex"
    - "MHF_Steve"
  BANKER:
    - "MHF_Villager"
    - "Notch"
  AMBASSADOR:
    - "jeb_"
    - "Dinnerbone"
  CRAFTSMAN:
    - "MHF_ArrowUp"
    - "MHF_ArrowDown"
  TRAVELING:
    - "MHF_Villager"

# Plot-Namen
custom-names:
  plot-id-123: "Meine Handelsgilde"
  plot-id-456: "Zentral-Markt"

# Plot-Storage (existierendes Feature)
plot-storage:
  plot-id-789:
    materials:
      - DIAMOND
      - GOLD_INGOT
      - IRON_INGOT
    receiver-chest:
      world: "world"
      x: 100
      y: 64
      z: 200
```

---

## API-Nutzung (für andere Module)

### PlotRegistry

```java
// In deinem Modul
Plugin plotsPlugin = Bukkit.getPluginManager().getPlugin("FallenStar-Plots");
Method method = plotsPlugin.getClass().getMethod("getPlotRegistry");
PlotRegistry registry = (PlotRegistry) method.invoke(plotsPlugin);

// Plot registrieren
registry.registerPlot(myPlot, PlotType.MERCHANT_GUILD);
```

### VirtualTraderInventoryManager

```java
// Zugriff auf Inventar-Manager
VirtualTraderInventoryManager manager = /* ... via Reflection */;

// Inventar erstellen
VirtualTraderInventory inv = manager.getOrCreateInventory(playerId, plot);
```

### NPCTravelSystem

```java
// NPC-Reise starten
NPCTravelSystem travelSystem = /* ... */;
TravelTicket ticket = travelSystem.startTravel(npcId, fromPlot, toSlot);

// Kosten berechnen
BigDecimal cost = travelSystem.calculateTravelCost(start, end);
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

    <!-- Items Module (für Münzen) -->
    <dependency>
        <groupId>de.fallenstar</groupId>
        <artifactId>module-items</artifactId>
        <version>${project.version}</version>
        <scope>provided</scope>
    </dependency>

    <!-- Towny API -->
    <dependency>
        <groupId>com.palmergames.bukkit.towny</groupId>
        <artifactId>towny</artifactId>
        <version>0.100.0.0</version>
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
cd module-plots
mvn clean package
```

Output: `target/FallenStar-Plots-1.0-SNAPSHOT.jar`

### Installation

1. FallenStar-Core installieren
2. Towny installieren
3. FallenStar-Plots installieren
4. Optional: FallenStar-Items (für Münzen)
5. Server starten

### Testing

**Commands testen:**
```bash
# Storage-Befehle
/plot storage setreceiver  # Empfangskiste setzen
/plot storage scan         # Storage scannen
/plot storage view         # Storage anzeigen

# Plot-Namen (via GUI)
/plot gui  # Öffne Verwaltungs-GUI

# Logs prüfen
[INFO] Plots-Modul wird gestartet...
[INFO] ✓ PlotRegistry initialisiert
[INFO] ✓ VirtualTraderInventoryManager initialisiert
[INFO] ✓ NPCTravelSystem initialisiert
[INFO] ✓ NPCSkinPool initialisiert
[INFO] ✓ PlotNameManager initialisiert
```

---

## Erweiterungen (Roadmap)

### Sprint 13-14: NPCs
- Citizens-Integration (NPCProvider)
- GuildTraderNPC (Gildenhändler)
- PlayerTraderNPC (Spielerhändler)
- TravelingMerchantNPC (Fahrender Händler)
- WorldBankerNPC (Weltbankier)

### Sprint 15-16: Erweiterte Features
- Slot-System Implementierung (SlottedPlot, PlotSlot)
- NPC-Spawning auf Slots
- Slot-Limits (5 Trader, 2 Banker, 3 Craftsman)

---

## Bekannte Einschränkungen

1. **Towny-Abhängigkeit**: Aktuell nur Towny-Integration (keine Factions)
2. **NPC-Typen nicht implementiert**: Nur Interfaces und Skin-Pool, keine konkreten Citizens-NPCs (kommt in Sprint 13-14)
3. **Slot-System teilweise implementiert**: UI und Travel-System vorhanden, aber PlotSlot-Klasse noch rudimentär

---

## Lizenz

© 2025 FallenStar Development Team

---

## Support

- GitHub Issues: `https://github.com/sternstaub/fs-core-sample-dump/issues`
- Wiki: `https://github.com/sternstaub/fs-core-sample-dump/wiki`

---

**Status:** ✅ Sprint 3-4 abgeschlossen, ✅ Sprint 11-12 erweiterte Features implementiert
**Nächster Sprint:** Sprint 13-14 - NPCs (Citizens-Integration)

