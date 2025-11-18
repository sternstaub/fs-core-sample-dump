# FallenStar NPCs Module

**NPC-System mit Citizens-Integration für Plot-gebundene NPCs**

Version: 1.0-SNAPSHOT
Sprint: 13-14 (In Arbeit)

---

## Übersicht

Das NPCs-Modul erweitert FallenStar um ein vollständiges NPC-System mit Citizens-Integration:

- **Citizens-Integration** - CitizensNPCProvider für NPC-Spawning
- **Plot-gebundene NPCs** - NPCs sind an Grundstücke gebunden
- **Mehrere NPC-Typen** - Botschafter, Gildenhändler, Spielerhändler, Weltbankier
- **NPCManager** - Zentrale NPC-Verwaltung
- **Graceful Degradation** - Funktioniert mit und ohne Citizens

---

## Features

### ✅ Implementiert (Sprint 13-14)

#### 1. CitizensNPCProvider
- **Citizens-Integration**: Implementiert NPCProvider-Interface aus Core
  - `spawnNPC(Location, String, String)` - NPC spawnen
  - `removeNPC(UUID)` - NPC entfernen
  - `isAvailable()` - Citizens-Verfügbarkeit prüfen
  - Graceful Degradation bei fehlendem Citizens

#### 2. NPC-Typen (NPCType Interface)
- **NPCType Interface**: Gemeinsames Interface für alle NPC-Typen
  - `getTypeName()` - Typ-Identifier
  - `getDisplayName()` - Anzeigename
  - `getSkin()` - Skin (Spielername oder Textur)
  - `onClick(Player, UUID)` - Interaktions-Logik
  - `isAvailable()` - Verfügbarkeits-Check

#### 3. Konkrete NPC-Implementierungen

**AmbassadorNPC** (Botschafter):
- Teleport zu anderen Towns gegen Bezahlung
- Erfordert TownProvider (Towny)
- Kosten: Abhängig von Entfernung
- UI: Town-Auswahl-Menü

**GuildTraderNPC** (Gildenhändler):
- Plot-gebundener Händler
- Nutzt TradeSet-System aus Economy-Modul
- PlotRegistry-Integration für Handelsgilden
- Virtuelles Inventar (VirtualTraderInventory)

**PlayerTraderNPC** (Spielerhändler):
- Spieler-eigener Händler
- Individuelles Inventar
- Eigene Trade-Angebote
- Plot-gebunden

**WorldBankerNPC** (Weltbankier):
- Währungsumtausch
- Economy-Integration
- Wechselkurs-Berechnungen

#### 4. Manager

**NPCManager**:
- Zentrale NPC-Verwaltung
- NPC-Spawning über CitizensNPCProvider
- NPC-Typ-Registry
- Interaktions-Event-Handling

**GuildTraderManager**:
- Spezielle Verwaltung für Gildenhändler
- Trade-Offer-Management
- PlotRegistry-Integration

#### 5. Admin-Befehle
- `/fscore admin npc spawn <type>` - NPC spawnen
- `/fscore admin npc remove <npc-id>` - NPC entfernen
- `/fscore admin npc list` - Alle NPCs auflisten
- `/fscore admin npc info <npc-id>` - NPC-Informationen

### 📋 Geplant (zukünftige Verbesserungen)

- Persistent NPC Storage (Config-basiert)
- NPC-Respawning nach Server-Restart
- Erweiterte NPC-Konfiguration
- Custom NPC-Traits (Citizens-Traits)
- NPC-Patrol-System
- Dialog-System

---

## Architektur

### Module Dependencies

```
NPCs-Modul
├── FallenStar-Core (NPCProvider, ProviderRegistry, PlotTypeRegistry)
├── FallenStar-Plots (PlotRegistry, VirtualTraderInventory)
├── FallenStar-Economy (TradeSet, CurrencyManager)
├── Citizens (NPC-Spawning und Management)
└── Towny (optional für AmbassadorNPC)
```

### Paket-Struktur

```
de.fallenstar.npc/
├── NPCModule.java                  # Main Plugin Class
├── provider/
│   └── CitizensNPCProvider.java    # Citizens-Integration
├── npctype/
│   ├── NPCType.java                # Interface für NPC-Typen
│   ├── AmbassadorNPC.java          # Botschafter-Implementierung
│   ├── GuildTraderNPC.java         # Gildenhändler-Implementierung
│   ├── PlayerTraderNPC.java        # Spielerhändler-Implementierung
│   └── WorldBankerNPC.java         # Bankier-Implementierung
├── manager/
│   ├── NPCManager.java             # Zentrale NPC-Verwaltung
│   └── GuildTraderManager.java     # Gildenhändler-Manager
└── command/
    └── NPCAdminHandler.java        # Admin-Befehle
```

---

## Verwendung

### NPC spawnen

```java
NPCManager npcManager = npcModule.getNPCManager();

// Botschafter spawnen
AmbassadorNPC ambassador = new AmbassadorNPC(providers);
UUID npcId = npcManager.spawnNPC(
    location,
    ambassador.getTypeName(),
    ambassador.getDisplayName(),
    ambassador.getSkin()
);

// Gildenhändler spawnen
GuildTraderNPC trader = new GuildTraderNPC(providers, guildPlot);
UUID traderId = npcManager.spawnNPC(
    location,
    trader.getTypeName(),
    trader.getDisplayName(),
    trader.getSkin()
);
```

### NPC-Interaktion

```java
// NPCManager registriert automatisch Click-Listener

// In NPCType-Implementierung:
@Override
public void onClick(Player player, UUID npcId) {
    // Botschafter: Öffne Town-Auswahl-UI
    // Händler: Öffne Trade-UI
    // Bankier: Öffne Währungsumtausch-UI
}
```

### GuildTrader-Manager

```java
GuildTraderManager traderManager = npcModule.getGuildTraderManager();

// Trade-Angebote hinzufügen
TradeSet offer = new TradeSet(/* ... */);
traderManager.addTradeOffer(guildPlot, offer);

// Alle Angebote für Plot
List<TradeSet> offers = traderManager.getTradeOffers(guildPlot);
```

---

## Konfiguration

### plugin.yml

```yaml
name: FallenStar-NPCs
version: 1.0-SNAPSHOT
main: de.fallenstar.npc.NPCModule
api-version: 1.21

# Hard Dependencies
depend: [FallenStar-Core]

# Optional Dependencies
softdepend: [Citizens, FallenStar-Plots, FallenStar-Economy, Towny]

commands:
  npc:
    description: NPC-Verwaltung
    usage: /npc <subcommand>
    permission: fallenstar.npc.use
```

### config.yml (geplant)

```yaml
# NPC-System
npc-system:
  enabled: true
  auto-respawn: true  # NPCs nach Restart neu spawnen

# NPC-Typen Aktivierung
npc-types:
  ambassador:
    enabled: true
    requires-town-provider: true

  guild-trader:
    enabled: true
    requires-plot-registry: true

  player-trader:
    enabled: true

  world-banker:
    enabled: true
    requires-economy: true

# Botschafter-Kosten
ambassador:
  base-cost: 100  # Basis-Kosten in Sternen
  cost-per-chunk: 5  # Kosten pro Chunk Entfernung

# Default-Skins (falls nicht in NPCSkinPool)
default-skins:
  ambassador: "jeb_"
  guild-trader: "MHF_Villager"
  player-trader: "MHF_Alex"
  world-banker: "Notch"

# Persistent NPCs (wird automatisch gefüllt)
spawned-npcs: []
```

---

## NPC-Typen Details

### AmbassadorNPC (Botschafter)
**Zweck:** Teleportiert Spieler zu anderen Towns gegen Bezahlung

**Funktionen:**
- Town-Auswahl-UI
- Entfernungs-Berechnung (Manhattan Distance)
- Kosten-Berechnung (base + per-chunk)
- Teleport-Durchführung

**Voraussetzungen:**
- TownProvider (Towny)
- EconomyProvider (für Bezahlung)

**Interaktion:**
```
Spieler klickt NPC → Town-Liste öffnet sich → Wählt Town aus →
Kosten werden angezeigt → Bei Bestätigung: Zahlung + Teleport
```

---

### GuildTraderNPC (Gildenhändler)
**Zweck:** Zentraler Händler für Handelsgilden

**Funktionen:**
- TradeSet-basiertes Trading
- PlotRegistry-Integration
- Gemeinsames Guild-Inventar
- VirtualTraderInventory

**Voraussetzungen:**
- PlotRegistry (Plot als MERCHANT_GUILD registriert)
- TradeSet-System (Economy-Modul)

**Interaktion:**
```
Spieler klickt NPC → Trade-UI öffnet sich (Vanilla Merchant) →
Handelt mit Items basierend auf TradeSets
```

---

### PlayerTraderNPC (Spielerhändler)
**Zweck:** Individuelle Händler-NPCs für Spieler

**Funktionen:**
- Spieler-eigenes Inventar (VirtualTraderInventory)
- Individuelle Trade-Angebote
- Plot-gebunden (nur auf eigenem Plot)

**Voraussetzungen:**
- VirtualTraderInventory (Plots-Modul)
- TradeSet-System (Economy-Modul)

**Interaktion:**
```
Owner: Klick → Inventar-Management-UI
Kunde: Klick → Trade-UI mit Spieler-Angeboten
```

---

### WorldBankerNPC (Weltbankier)
**Zweck:** Währungsumtausch zwischen verschiedenen Währungen

**Funktionen:**
- Wechselkurs-Berechnungen
- Multi-Currency-Support
- Economy-Integration

**Voraussetzungen:**
- CurrencyManager (Economy-Modul)
- Mehrere registrierte Währungen

**Interaktion:**
```
Spieler klickt NPC → Währungsumtausch-UI →
Wählt Quell- und Zielwährung → Wechselkurs wird angezeigt →
Bei Bestätigung: Items werden getauscht
```

---

## API-Nutzung (für andere Module)

### NPC spawnen

```java
NPCModule npcModule = (NPCModule) Bukkit.getPluginManager().getPlugin("FallenStar-NPCs");
NPCManager manager = npcModule.getNPCManager();

// NPC spawnen
UUID npcId = manager.spawnNPC(location, "ambassador", "Botschafter", "jeb_");
```

### NPC-Typ registrieren (Custom NPC)

```java
// Eigenen NPC-Typ implementieren
public class MyCustomNPC implements NPCType {
    @Override
    public String getTypeName() { return "custom"; }

    @Override
    public void onClick(Player player, UUID npcId) {
        player.sendMessage("Custom NPC!");
    }

    // ... weitere Methoden
}

// Registrieren
MyCustomNPC customNpc = new MyCustomNPC();
manager.registerNPCType(customNpc);
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

    <!-- Plots Module (für VirtualTraderInventory) -->
    <dependency>
        <groupId>de.fallenstar</groupId>
        <artifactId>module-plots</artifactId>
        <version>${project.version}</version>
        <scope>provided</scope>
    </dependency>

    <!-- Economy Module (für TradeSet) -->
    <dependency>
        <groupId>de.fallenstar</groupId>
        <artifactId>module-economy</artifactId>
        <version>${project.version}</version>
        <scope>provided</scope>
    </dependency>

    <!-- Citizens API -->
    <dependency>
        <groupId>net.citizensnpcs</groupId>
        <artifactId>citizens-main</artifactId>
        <version>2.0.33-SNAPSHOT</version>
        <scope>provided</scope>
    </dependency>

    <!-- Towny API (optional für AmbassadorNPC) -->
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
cd module-npcs
mvn clean package
```

Output: `target/FallenStar-NPCs-1.0-SNAPSHOT.jar`

### Installation

1. FallenStar-Core installieren
2. Citizens installieren
3. FallenStar-Plots installieren (optional)
4. FallenStar-Economy installieren (optional)
5. Towny installieren (optional)
6. FallenStar-NPCs installieren
7. Server starten

### Testing

**NPC spawnen (im Spiel):**
```bash
/fscore admin npc spawn ambassador
/fscore admin npc spawn guild-trader
/fscore admin npc spawn player-trader
/fscore admin npc spawn world-banker
```

**Logs prüfen:**
```
[INFO] NPCs-Modul wird gestartet...
[INFO] ✓ Citizens gefunden - NPCProvider verfügbar
[INFO] ✓ NPCManager initialisiert
[INFO] ✓ 4 NPC-Typen registriert
[INFO] ✓ NPCs-Modul erfolgreich initialisiert!
```

---

## Integration mit anderen Modulen

### Plots-Modul
- **VirtualTraderInventory**: Händler-Inventar für PlayerTraderNPC
- **PlotRegistry**: Handelsgilden-Registrierung für GuildTraderNPC
- **NPCSkinPool**: Zufällige Skins für NPC-Typen

### Economy-Modul
- **TradeSet**: Trading-System für Händler-NPCs
- **CurrencyManager**: Währungsumtausch für WorldBankerNPC
- **Kosten-Berechnung**: Ambassador-Teleport-Kosten

### Core
- **NPCProvider**: Interface-Implementierung (CitizensNPCProvider)
- **AdminCommandRegistry**: Admin-Befehle registrieren
- **PlotTypeRegistry**: Plot-Typ-Abfragen

---

## Bekannte Einschränkungen

1. **Citizens-Abhängigkeit**: NPCs funktionieren nur mit Citizens (kein Fallback)
2. **Kein Persistent Storage**: NPCs überleben Server-Restart noch nicht
3. **Ambassador ohne Towny**: AmbassadorNPC funktioniert nur mit Towny
4. **NPC-Respawning**: Noch nicht implementiert
5. **Custom Traits**: Citizens-Traits werden noch nicht genutzt

---

## Roadmap

### Sprint 13-14: ✅ Basis-Implementierung (Aktuell)
- CitizensNPCProvider implementieren
- NPCType Interface
- Konkrete NPC-Typen (Ambassador, GuildTrader, PlayerTrader, WorldBanker)
- NPCManager und GuildTraderManager
- Admin-Befehle

### Zukünftige Verbesserungen:
- Persistent NPC Storage (Config)
- Auto-Respawning nach Restart
- Erweiterte NPC-Konfiguration
- Dialog-System
- NPC-Patrol-Routen
- Custom Citizens-Traits

---

## Lizenz

© 2025 FallenStar Development Team

---

## Support

- GitHub Issues: `https://github.com/sternstaub/fs-core-sample-dump/issues`
- Wiki: `https://github.com/sternstaub/fs-core-sample-dump/wiki`

---

**Status:** 🔨 Sprint 13-14 - In Arbeit
**Dependencies:** Core (required), Citizens (required), Plots (optional), Economy (optional), Towny (optional)
**Nächster Sprint:** Sprint 14-15 - Merchants-Modul (baut auf NPCs auf)
