# TODO: Interaction-System Integration

## ✅ Implementiert

### Core-System
- [x] Interaction-Interfaces (Interactable, UiTarget, etc.)
- [x] InteractionRegistry + InteractionHandler
- [x] Plot-Interface-Hierarchie (Plot, BasePlot, Traits)
- [x] TradeguildPlot mit allen Traits
- [x] GuildTraderNpcEntity (UiTarget für NPCs)

## 🔨 Ausstehend

### 1. TradeguildPlot-Instanz-Verwaltung

**Problem:**
- PlotRegistry speichert nur Plot-IDs (Strings), nicht Plot-Objekte
- TradeguildPlot-Instanzen müssen irgendwo verwaltet werden

**Lösungsansatz:**
```java
// In PlotModule:
private Map<String, TradeguildPlot> tradeguildPlots = new ConcurrentHashMap<>();

private void registerTradeguildPlotInInteractionRegistry(Plot basePlot) {
    // Prüfe ob MERCHANT_GUILD
    if (plotRegistry.isRegistered(basePlot, PlotType.MERCHANT_GUILD)) {
        // Erstelle TradeguildPlot
        TradeguildPlot tradePlot = new TradeguildPlot(
            basePlot.getUuid(),
            basePlot.getIdentifier(),
            basePlot.getLocation(),
            basePlot.getNativePlot()
        );

        // In InteractionRegistry registrieren
        corePlugin.getInteractionRegistry().registerPlot(basePlot.getLocation(), tradePlot);

        // Cache für spätere Verwendung
        tradeguildPlots.put(basePlot.getIdentifier(), tradePlot);
    }
}
```

### 2. TradeguildPlot-Persistenz

**Benötigt:**
- Laden von Custom-Namen aus Towny MetaData / DataStore
- Laden von Storage-Daten (Items, Preise)
- Laden von NPC-IDs
- Laden von Slot-Daten

**Lösungsansatz:**
```java
// TradeguildPlot.java
public void loadFromDataStore(DataStore dataStore) {
    String key = "tradeguild." + getUuid();

    // Lade Custom-Name
    dataStore.load(key + ".name", String.class)
        .thenAccept(opt -> opt.ifPresent(name -> this.customName = name));

    // Lade Storage
    dataStore.load(key + ".storage", Map.class)
        .thenAccept(opt -> opt.ifPresent(data -> this.storage.putAll(data)));

    // ...
}

public void saveToDataStore(DataStore dataStore) {
    String key = "tradeguild." + getUuid();

    dataStore.save(key + ".name", customName);
    dataStore.save(key + ".storage", storage);
    // ...
}
```

### 3. NPCModule Integration

**Benötigt:**
- GuildTraderNpcEntity-Instanzen in InteractionRegistry registrieren
- Beim NPC-Spawn: `interactionRegistry.registerEntity(npcId, entity)`
- Beim NPC-Despawn: `interactionRegistry.unregisterEntity(npcId)`

**Lösungsansatz:**
```java
// In NPCModule oder NPCManager
public UUID spawnGuildTrader(Plot plot) {
    UUID npcId = npcManager.spawnNPC(...);

    // Erstelle Entity
    GuildTraderNpcEntity entity = new GuildTraderNpcEntity(
        npcId, plot, guildTraderType
    );

    // In InteractionRegistry registrieren
    corePlugin.getInteractionRegistry().registerEntity(npcId, entity);

    return npcId;
}
```

### 4. UI-Implementierungen

**Ausstehende UIs:**
- PlotMainMenuUi (mit getAvailableActions)
- StoragePriceUi (Preise verwalten)
- NpcManagementUi (NPCs spawnen/entfernen)
- PlotStorageUi (Lager-Übersicht)
- PlotNameInputUi (AnvilUi für Namen-Eingabe)

**Beispiel PlotMainMenuUi:**
```java
public class PlotMainMenuUi extends LargeChestUi {
    private final TradeguildPlot plot;

    public PlotMainMenuUi(TradeguildPlot plot, Player player) {
        super("§6" + plot.getDisplayName());
        this.plot = plot;

        // Auto-generate buttons from available actions
        List<UiActionInfo> actions = plot.getAvailableActions(player, UiContext.MAIN_MENU);
        for (UiActionInfo action : actions) {
            action.slot().ifPresent(slot -> {
                setItem(slot, createButton(action), p -> {
                    plot.executeAction(p, action.id());
                });
            });
        }
    }
}
```

### 5. PlotProvider-Erweiterung

**Benötigt:**
- `isOwner(Plot, Player)` für Owner-Checks
- `getPlotAs(Location, Class<T>)` für Type-Safe Casting

**Beispiel:**
```java
public interface PlotProvider {
    // ...

    /**
     * Prüft ob Spieler Owner eines Plots ist.
     */
    boolean isOwner(Plot plot, Player player);

    /**
     * Gibt Plot als spezifischen Typ zurück.
     *
     * @return Optional mit gecastetem Plot, oder empty wenn falscher Typ
     */
    default <T extends Plot> Optional<T> getPlotAs(Location location, Class<T> type) {
        Plot plot = getPlot(location);
        if (type.isInstance(plot)) {
            return Optional.of(type.cast(plot));
        }
        return Optional.empty();
    }
}
```

## 📝 Migrations-Schritte

### Phase 1: Instanz-Verwaltung
1. TradeguildPlot-Factory erstellen
2. PlotModule: TradeguildPlot-Cache implementieren
3. PlotRegistryListener: TradeguildPlot bei MERCHANT_GUILD-Registration erstellen
4. InteractionRegistry-Integration für alle TradeguildPlots

### Phase 2: Persistenz
1. TradeguildPlot: loadFromDataStore/saveToDataStore implementieren
2. PlotModule: Laden aller TradeguildPlots beim Start
3. PlotModule: Speichern bei onDisable
4. Auto-Save nach jeder Änderung

### Phase 3: NPC-Integration
1. NPCModule: InteractionRegistry-Zugriff
2. GuildTraderNPC: registerEntity bei Spawn
3. GuildTraderNPC: unregisterEntity bei Despawn
4. Migration alter NPCs zu neuen Entities

### Phase 4: UI-Vervollständigung
1. PlotMainMenuUi implementieren
2. StoragePriceUi überarbeiten (UiActionTarget-basiert)
3. NpcManagementUi implementieren
4. PlotStorageUi implementieren

### Phase 5: Testing
1. Build-Test
2. Server-Deploy
3. Click-to-UI-Test (Plots)
4. Click-to-UI-Test (NPCs)
5. Owner/Guest-Unterscheidung testen
6. Persistenz testen

## 💡 Design-Entscheidungen

### Warum TradeguildPlot-Instanzen?
- Plot-Interface ist jetzt Interface, nicht Klasse
- BasePlot ist minimal (nur Basis-Daten)
- TradeguildPlot erweitert BasePlot mit allen Traits
- Traits ermöglichen Composition statt Vererbung

### Warum InteractionRegistry?
- Zentrale Verwaltung aller klickbaren Objekte
- Type-Safe (Interactable-Interface)
- Automatisches Click-Routing via InteractionHandler
- Saubere Trennung: Core = Infrastructure, Module = Business Logic

### Warum UiActionTarget?
- Self-Constructing UIs (Objekte definieren eigene Buttons)
- Context-Aware (verschiedene Aktionen je nach UI-Kontext)
- Permission-Integration
- DRY (keine Code-Duplizierung in UIs)

## 🎯 Nächste Commits

1. **Commit:** TradeguildPlot-Factory + Instanz-Verwaltung
2. **Commit:** PlotModule InteractionRegistry-Integration
3. **Commit:** NPCModule InteractionRegistry-Integration
4. **Commit:** PlotMainMenuUi Implementierung
5. **Commit:** Testing + Bugfixes

---

**Stand:** 2025-11-18
**Branch:** claude/fix-storage-price-loop-012sXDfqzLyyPSPX8QC8egq7
