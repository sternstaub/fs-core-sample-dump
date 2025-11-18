# ARCHITECTURE EXTENSION: Distributor-Pattern + Generic Interaction Menu

## 🎯 Neue Design-Patterns

### 1. Generic Interaction Menu

**Problem:**
- NPCs haben unterschiedliche Funktionen (Trading, Quests, etc.)
- Funktionen sollen erweiterbar sein
- Kein Spaghetti-Code in onClick()

**Lösung:**
```java
public interface NpcGenericInteractionMenu {
    /**
     * Gibt verfügbare Untermenüs für einen NPC zurück.
     */
    List<UiActionInfo> getAvailableSubMenus(Player player, UUID npcId);

    /**
     * Öffnet ein Untermenü.
     */
    boolean openSubMenu(Player player, UUID npcId, String subMenuId);
}

// Verwendung:
GuildTraderNPC implements UiTarget {
    @Override
    public List<UiActionInfo> getAvailableActions(Player player, UiContext context) {
        if (context == UiContext.MAIN_MENU) {
            return List.of(
                UiActionInfo.builder()
                    .id("trade")
                    .displayName("§6Handeln")
                    .icon(Material.GOLD_INGOT)
                    .build(),
                UiActionInfo.builder()
                    .id("quests")
                    .displayName("§eQuests")
                    .icon(Material.BOOK)
                    .build()
            );
        }
        return List.of();
    }

    @Override
    public boolean executeAction(Player player, String actionId) {
        return switch (actionId) {
            case "trade" -> openTradeUI(player);
            case "quests" -> openQuestUI(player);
            default -> false;
        };
    }
}
```

**Vorteile:**
- ✅ Erweiterbar (neue Funktionen = neue Actions)
- ✅ Kein if/else-Spaghetti
- ✅ Self-Documenting (getAvailableActions zeigt alle Optionen)
- ✅ Context-Aware

---

### 2. Distributor-Pattern

**Problem:**
- Manuelle Slot-Zuweisung ist mühsam
- Quests müssen manuell NPCs zugewiesen werden
- Keine automatische Verteilung

**Lösung: Distributor + Distributable**

#### 2.1 Distributor Interface

```java
/**
 * Interface für Objekte die Inhalte distribuieren können.
 *
 * @param <T> Typ des Distributable
 */
public interface Distributor<T extends Distributable> {
    /**
     * Distribuiert ein Objekt.
     *
     * @param distributable Das zu distribuierende Objekt
     * @return true wenn erfolgreich, false wenn voll/fehlgeschlagen
     */
    boolean distribute(T distributable);

    /**
     * Entfernt ein distribuiertes Objekt.
     *
     * @param distributable Das Objekt
     * @return true wenn erfolgreich entfernt
     */
    boolean undistribute(T distributable);

    /**
     * Gibt die maximale Kapazität zurück.
     *
     * @return Maximale Anzahl
     */
    int getCapacity();

    /**
     * Gibt die aktuelle Belegung zurück.
     *
     * @return Anzahl distribuierter Objekte
     */
    int getCurrentCount();

    /**
     * Prüft ob noch Kapazität verfügbar ist.
     *
     * @return true wenn Platz frei
     */
    default boolean hasCapacity() {
        return getCurrentCount() < getCapacity();
    }

    /**
     * Gibt alle distribuierten Objekte zurück.
     *
     * @return Liste von Distributables
     */
    List<T> getDistributed();
}
```

#### 2.2 Distributable Interface

```java
/**
 * Interface für Objekte die distribuiert werden können.
 */
public interface Distributable {
    /**
     * Gibt die ID des Distributable zurück.
     *
     * @return UUID
     */
    UUID getId();

    /**
     * Gibt den Typ zurück.
     *
     * @return Typ-String
     */
    String getType();

    /**
     * Callback wenn distribuiert wurde.
     *
     * @param distributor Der Distributor
     */
    default void onDistributed(Distributor<?> distributor) {
        // Optional: Override für custom Logic
    }

    /**
     * Callback wenn de-distribuiert wurde.
     */
    default void onUndistributed() {
        // Optional: Override für custom Logic
    }
}
```

#### 2.3 NpcDistributor

```java
/**
 * Distributor für NPCs auf Slots.
 */
public interface NpcDistributor extends Distributor<DistributableNpc> {
    /**
     * Distribuiert einen NPC auf einen freien Slot.
     *
     * Algorithmus:
     * 1. Prüfe hasCapacity()
     * 2. Finde freien Slot
     * 3. Platziere NPC
     * 4. Rufe npc.onDistributed() auf
     *
     * @param npc Der NPC
     * @return true wenn erfolgreich
     */
    @Override
    boolean distribute(DistributableNpc npc);

    /**
     * Gibt den Slot für einen NPC zurück.
     *
     * @param npc Der NPC
     * @return Optional mit Slot-Nummer
     */
    Optional<Integer> getSlotForNpc(DistributableNpc npc);
}
```

#### 2.4 QuestDistributor

```java
/**
 * Distributor für Quests an NPCs.
 */
public interface QuestDistributor extends Distributor<DistributableQuest> {
    /**
     * Distribuiert eine Quest an einen zufälligen NPC.
     *
     * Algorithmus:
     * 1. Hole alle NPCs (QuestContainer)
     * 2. Filtere NPCs mit Kapazität
     * 3. Wähle zufälligen NPC
     * 4. Weise Quest zu
     * 5. Rufe quest.onDistributed() auf
     *
     * @param quest Die Quest
     * @return true wenn erfolgreich
     */
    @Override
    boolean distribute(DistributableQuest quest);

    /**
     * Gibt alle Quest-Container zurück.
     *
     * Nullable: Kann empty list zurückgeben wenn keine NPCs vorhanden.
     *
     * @return Liste von QuestContainern (kann leer sein!)
     */
    List<QuestContainer> getQuestContainers();
}
```

#### 2.5 DistributableNpc

```java
/**
 * NPC der auf Slots distribuiert werden kann.
 */
public interface DistributableNpc extends Distributable {
    /**
     * Gibt die Entity-UUID zurück (für NPC-Spawn).
     *
     * @return Entity UUID oder null wenn noch nicht gespawned
     */
    UUID getEntityId();

    /**
     * Spawnt den NPC an einer Location.
     *
     * @param location Location
     * @return Entity UUID
     */
    UUID spawn(Location location);

    /**
     * Despawnt den NPC.
     */
    void despawn();

    /**
     * Gibt den NPC-Typ zurück.
     *
     * @return NPC-Typ String (z.B. "guild_trader", "quest_giver")
     */
    String getNpcType();
}
```

#### 2.6 DistributableQuest

```java
/**
 * Quest die an NPCs distribuiert werden kann.
 */
public interface DistributableQuest extends Distributable {
    /**
     * Gibt den Quest-Titel zurück.
     *
     * @return Titel
     */
    String getTitle();

    /**
     * Gibt die Quest-Stufe zurück.
     *
     * @return Level
     */
    int getLevel();

    /**
     * Gibt den aktuellen Quest-Container zurück.
     *
     * Nullable: Kann null sein wenn Quest nicht distribuiert ist!
     *
     * @return QuestContainer oder null
     */
    QuestContainer getCurrentContainer();

    /**
     * Setzt den Quest-Container.
     *
     * @param container Der Container
     */
    void setCurrentContainer(QuestContainer container);
}
```

#### 2.7 QuestContainer

```java
/**
 * Container der Quests halten kann (z.B. NPC).
 */
public interface QuestContainer {
    /**
     * Gibt die maximale Anzahl Quests zurück.
     *
     * @return Max Quests
     */
    int getMaxQuests();

    /**
     * Gibt aktuelle Quests zurück.
     *
     * @return Liste von Quests
     */
    List<DistributableQuest> getQuests();

    /**
     * Fügt eine Quest hinzu.
     *
     * @param quest Die Quest
     * @return true wenn erfolgreich
     */
    boolean addQuest(DistributableQuest quest);

    /**
     * Entfernt eine Quest.
     *
     * @param quest Die Quest
     * @return true wenn erfolgreich
     */
    boolean removeQuest(DistributableQuest quest);

    /**
     * Prüft ob noch Kapazität für Quests vorhanden ist.
     *
     * @return true wenn Platz frei
     */
    default boolean hasQuestCapacity() {
        return getQuests().size() < getMaxQuests();
    }
}
```

---

### 3. TradeguildPlot mit Distributor-Support

```java
public class TradeguildPlot extends BasePlot
    implements NamedPlot, StorageContainerPlot, NpcContainerPlot,
               SlottablePlot, UiTarget,
               NpcDistributor, QuestDistributor {  // NEU!

    // NpcDistributor Implementation
    @Override
    public boolean distribute(DistributableNpc npc) {
        // Finde freien Slot
        List<Integer> freeSlots = getFreeSlots();
        if (freeSlots.isEmpty()) {
            return false; // Kein Platz
        }

        // Wähle ersten freien Slot
        int slot = freeSlots.get(0);

        // Spawne NPC
        Location spawnLoc = calculateSlotLocation(slot);
        UUID entityId = npc.spawn(spawnLoc);

        // Platziere in Slot
        placeNpcInSlot(slot, entityId);

        // Registriere in NpcContainer
        registerNpc(entityId);

        // Callback
        npc.onDistributed(this);

        return true;
    }

    @Override
    public int getCapacity() {
        return getMaxSlots();
    }

    @Override
    public int getCurrentCount() {
        return getOccupiedSlotCount();
    }

    @Override
    public List<DistributableNpc> getDistributed() {
        // Hole alle NPCs in Slots
        return getOccupiedSlots().stream()
            .map(this::getNpcInSlot)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(this::getDistributableNpc)
            .toList();
    }

    // QuestDistributor Implementation
    @Override
    public boolean distribute(DistributableQuest quest) {
        // Hole alle NPCs die Quests halten können
        List<QuestContainer> containers = getQuestContainers();

        if (containers.isEmpty()) {
            return false; // Keine NPCs vorhanden
        }

        // Filtere Container mit Kapazität
        List<QuestContainer> available = containers.stream()
            .filter(QuestContainer::hasQuestCapacity)
            .toList();

        if (available.isEmpty()) {
            return false; // Alle NPCs voll
        }

        // Wähle zufälligen Container
        QuestContainer container = available.get(
            ThreadLocalRandom.current().nextInt(available.size())
        );

        // Weise Quest zu
        boolean success = container.addQuest(quest);
        if (success) {
            quest.setCurrentContainer(container);
            quest.onDistributed(this);
        }

        return success;
    }

    @Override
    public List<QuestContainer> getQuestContainers() {
        // Hole alle NPCs die QuestContainer sind
        return getNpcIds().stream()
            .map(this::getNpcEntity)
            .filter(npc -> npc instanceof QuestContainer)
            .map(npc -> (QuestContainer) npc)
            .toList();
    }
}
```

---

### 4. Verwendungs-Beispiele

#### Beispiel 1: NPC automatisch auf Plot platzieren

```java
// Erstelle NPC
DistributableNpc trader = new GuildTraderDistributable(
    "guild_trader",
    guildTraderType
);

// Distribuiere auf Handelsgilde
TradeguildPlot plot = getPlot(...);
boolean success = plot.distribute(trader);

if (success) {
    player.sendMessage("§aHändler wurde automatisch platziert!");
} else {
    player.sendMessage("§cKeine freien Slots verfügbar!");
}
```

#### Beispiel 2: Quest automatisch an NPC verteilen

```java
// Erstelle Quest
DistributableQuest quest = new SimpleQuest(
    "Sammle 10 Äpfel",
    1  // Level
);

// Distribuiere auf Handelsgilde
boolean success = plot.distribute(quest);

if (success) {
    QuestContainer container = quest.getCurrentContainer();
    player.sendMessage("§aQuest wurde " + container.getName() + " zugewiesen!");
} else {
    player.sendMessage("§cKeine NPCs mit freien Quest-Slots!");
}
```

#### Beispiel 3: Generic Interaction Menu

```java
// Spieler klickt auf Gildenhändler
// → InteractionHandler fängt ab
// → entity.onInteract(player, context)

@Override
public boolean onInteract(Player player, InteractionContext context) {
    // Öffne Generic Interaction Menu
    List<UiActionInfo> actions = getAvailableActions(player, UiContext.MAIN_MENU);

    if (actions.isEmpty()) {
        // Fallback: Direkt zu Trade
        openTradeUI(player);
    } else {
        // Generisches Menü mit allen Optionen
        NpcInteractionMenuUi menu = new NpcInteractionMenuUi(this, player);
        menu.open(player);
    }

    return true;
}
```

---

## 📋 Aktualisierte Implementierungs-Phasen

### Phase 1: Instanz-Verwaltung + Distributor-Core
- TradeguildPlot-Factory
- TradeguildPlot-Cache
- InteractionRegistry-Integration
- **NEU:** Distributor-Interfaces implementieren
- **NEU:** NpcDistributor + QuestDistributor in TradeguildPlot

### Phase 2: Persistenz
- DataStore-Integration
- Auto-Load/Auto-Save
- **NEU:** Distributor-State speichern

### Phase 3: NPC-Integration + Distributable-NPCs
- NPCModule: InteractionRegistry
- GuildTraderNpcEntity registrieren
- **NEU:** DistributableNpc-Implementierung
- **NEU:** Automatische Distribution testen

### Phase 4: UI-Implementierungen + Generic Menu
- PlotMainMenuUi
- **NEU:** NpcInteractionMenuUi (Generic Interaction Menu)
- StoragePriceUi
- NpcManagementUi
- **NEU:** QuestManagementUi

### Phase 5: Quest-System (Optional, später)
- DistributableQuest-Implementierung
- QuestContainer-Implementierung
- Quest-Distribution testen

### Phase 6: Testing
- Build + Deploy
- Click-to-UI
- NPC-Distribution
- Quest-Distribution

---

## 💡 Vorteile des Distributor-Patterns

✅ **Automatische Verteilung:** Kein manuelles Slot-Management
✅ **Skalierbar:** Neue Distributable-Typen leicht hinzufügbar
✅ **Kapazitäts-Management:** Automatische Prüfung auf freie Slots
✅ **Erweiterbar:** Neue Distributoren einfach implementierbar
✅ **Type-Safe:** Compiler prüft Distributor<T> Typen
✅ **Wiederverwendbar:** Distributor-Pattern für viele Use-Cases

---

## 🎯 Nächste Schritte

1. **Core-Interfaces erstellen:**
   - Distributor.java
   - Distributable.java
   - NpcDistributor.java
   - QuestDistributor.java
   - DistributableNpc.java
   - DistributableQuest.java
   - QuestContainer.java

2. **TradeguildPlot erweitern:**
   - Implements NpcDistributor
   - Implements QuestDistributor
   - distribute() Methoden implementieren

3. **GuildTraderNpcEntity erweitern:**
   - Implements DistributableNpc
   - Implements QuestContainer

4. **Phase 1-6 durchführen**

---

**Stand:** 2025-11-18
**Branch:** claude/fix-storage-price-loop-012sXDfqzLyyPSPX8QC8egq7
**Status:** Bereit für Implementierung
