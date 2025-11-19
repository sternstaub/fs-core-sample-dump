# 🏗️ Architektur-Refactoring: Interface-basiertes Design

**Ziel:** Type-Safe, erweiterbar, wiederverwendbar durch maximale Interface-Nutzung

**Status:** Design-Phase
**Version:** 2.0
**Datum:** 2025-11-18

---

## 📋 Inhaltsverzeichnis

1. [Kernprobleme](#kernprobleme)
2. [Design-Prinzipien](#design-prinzipien)
3. [Plot-System Refactoring](#plot-system-refactoring)
4. [UI-System Refactoring](#ui-system-refactoring)
5. [NPC-Container-System](#npc-container-system)
6. [Event-System](#event-system)
7. [Implementierungs-Phasen](#implementierungs-phasen)

---

## 🔴 Kernprobleme

### Problem 1: Plot als Klasse (nicht Interface)

**Aktuell:**
```java
public class Plot {
    private final UUID uuid;
    private final String identifier;
    // ...
}

public class NamedPlot extends Plot { ... }
```

**Warum problematisch:**
- ❌ Kann nicht mehrere "Traits" kombinieren
- ❌ Limitiert Erweiterbarkeit
- ❌ Kein echtes Trait-System (StorageContainerPlot, NpcContainerPlot)

**Lösung:**
```java
// Plot wird zu Interface
public interface Plot {
    UUID getUuid();
    String getIdentifier();
    Location getLocation();
}

// Traits als Interfaces
public interface NamedPlot extends Plot {
    Optional<String> getCustomName();
    void setCustomName(String name);
}

public interface StorageContainerPlot extends Plot {
    PlotStorage getPlotStorage();
}

public interface NpcContainerPlot extends Plot {
    NpcContainer getNpcContainer();
}

// Konkrete Implementation kann mehrere Traits kombinieren
public class TradeguildPlot implements Plot, NamedPlot, StorageContainerPlot, NpcContainerPlot {
    // Kombiniert alle Traits!
}
```

---

### Problem 2: Keine UI-Navigation (Parent-Child)

**Aktuell:**
```java
public class HandelsgildeUi extends GenericUiLargeChest { ... }
public class StoragePriceUi extends GenericUiLargeChest { ... }
// Keine Verbindung zwischen Parent und Child!
```

**Warum problematisch:**
- ❌ Keine Back-Buttons möglich
- ❌ Kein Kontext zwischen UIs
- ❌ Jedes UI muss Daten selbst laden

**Lösung:**
```java
public interface ChildUi<P extends UiParent> {
    P getParent();
    Class<P> getParentUiClass();

    default void openParent(Player player) {
        getParent().open(player);
    }
}

// Verwendung
public class StoragePriceUi extends GenericUiLargeChest
                             implements ChildUi<HandelsgildeUi> {
    private final HandelsgildeUi parent;

    @Override
    public HandelsgildeUi getParent() {
        return parent;
    }

    @Override
    public Class<HandelsgildeUi> getParentUiClass() {
        return HandelsgildeUi.class;
    }
}
```

---

### Problem 3: Duplizierter NPC-Container-Code

**Aktuell:**
```java
// PlotBoundNPCRegistry (Plots-Modul)
Map<UUID, List<UUID>> plotNPCs;

// PlayerBoundNPCRegistry (NPCs-Modul - geplant)
Map<UUID, List<UUID>> playerNPCs;
// → Gleiche Logik, unterschiedliche Container!
```

**Warum problematisch:**
- ❌ Code-Duplikation
- ❌ Inkonsistente APIs
- ❌ Schwer wartbar

**Lösung:**
```java
// Gemeinsames Interface
public interface NpcContainer {
    UUID getContainerId();
    List<UUID> getNPCs();
    void addNPC(UUID npcId, String npcType);
    void removeNPC(UUID npcId);
    int getMaxNPCs();
}

// Plot implementiert NpcContainer
public class TradeguildPlot implements NpcContainerPlot {
    @Override
    public NpcContainer getNpcContainer() {
        return this.npcContainer;
    }
}

// Spieler-Objekt implementiert NpcContainer
public class PlayerNpcOwnership implements NpcContainer {
    private final UUID playerId;
    // ... gleiche Logik wie PlotBoundNPCRegistry!
}
```

---

## 🎯 Design-Prinzipien

### 1. Interface-First

**Regel:** Jede Funktionalität als Interface definieren, dann implementieren.

**Vorteile:**
- ✅ Type-Safe zur Compile-Zeit
- ✅ Einfach testbar (Mock-Implementierungen)
- ✅ Trait-Komposition möglich

**Beispiel:**
```java
// ❌ FALSCH
public class MarketPlot extends Plot {
    // Vererbung limitiert
}

// ✅ RICHTIG
public interface MarketPlot extends Plot, SlottablePlot, NpcContainerPlot {
    // Trait-Komposition
}
```

---

### 2. Trait-Komposition statt Vererbung

**Regel:** Funktionalität als kleine, fokussierte Interfaces ("Traits").

**Vorteile:**
- ✅ Flexibel kombinierbar
- ✅ Single Responsibility per Trait
- ✅ Klare Abhängigkeiten

**Beispiel:**
```java
// Traits
public interface NamedPlot extends Plot { ... }
public interface StorageContainerPlot extends Plot { ... }
public interface NpcContainerPlot extends Plot { ... }
public interface SlottablePlot extends Plot { ... }

// Kombinationen
public class TradeguildPlot implements NamedPlot, StorageContainerPlot,
                                        NpcContainerPlot, SlottablePlot {
    // Hat ALLE Traits!
}

public class ResidencePlot implements NamedPlot {
    // Nur Named, kein Storage/NPCs
}
```

---

### 3. UI-Hierarchie mit Navigation

**Regel:** Child-UIs kennen ihr Parent, ermöglichen Navigation.

**Vorteile:**
- ✅ Back-Buttons automatisch
- ✅ Kontext-Weitergabe
- ✅ Klare UI-Strukturen

**Beispiel:**
```java
// UI-Hierarchie
PlotMainMenuUi (Parent)
  ├─ PlotStorageUi (Child)
  ├─ PlotNpcManagementUi (Child)
  │   └─ NpcPlacementUi (Grandchild)
  └─ PlotPriceManagementUi (Child)
      └─ StoragePriceUi (Grandchild)
```

---

### 4. Generische Container-Abstraktion

**Regel:** Gemeinsame Funktionalität abstrahieren, nicht duplizieren.

**Vorteile:**
- ✅ DRY (Don't Repeat Yourself)
- ✅ Konsistente APIs
- ✅ Einfach wartbar

**Beispiel:**
```java
// Gemeinsam
public interface NpcContainer {
    List<UUID> getNPCs();
    void addNPC(UUID npcId, String npcType);
}

// Spezifisch
public class PlotNpcContainer implements NpcContainer { ... }
public class PlayerNpcOwnership implements NpcContainer { ... }
// Gleiche API, unterschiedliche Persistierung!
```

---

## 🏗️ Plot-System Refactoring

### Phase 1: Plot-Interface-Hierarchie

#### 1.1 Core-Interface: `Plot`

**Location:** `core/src/main/java/de/fallenstar/core/provider/Plot.java`

```java
package de.fallenstar.core.provider;

import org.bukkit.Location;
import java.util.UUID;

/**
 * Basis-Interface für alle Plot-Typen.
 *
 * Definiert die Mindest-Funktionalität die jedes Grundstück bieten muss.
 *
 * @author FallenStar
 * @version 2.0
 */
public interface Plot {

    /**
     * @return Eindeutige UUID des Plots
     */
    UUID getUuid();

    /**
     * @return Lesbarer Identifier (z.B. "TownName_PlotID")
     */
    String getIdentifier();

    /**
     * @return Eine Location innerhalb des Plots
     */
    Location getLocation();

    /**
     * @return Plot-Typ (z.B. "handelsgilde", "marktplatz", "residence")
     */
    String getPlotType();

    /**
     * Gibt das originale Plot-Objekt zurück.
     * Sollte nur von Provider-Implementierungen verwendet werden.
     *
     * @param <T> Typ des nativen Plot-Objekts
     * @return Das native Plot-Objekt (z.B. TownBlock bei Towny)
     */
    <T> T getNativePlot();
}
```

---

#### 1.2 Trait: `NamedPlot`

**Location:** `core/src/main/java/de/fallenstar/core/provider/NamedPlot.java`

```java
package de.fallenstar.core.provider;

import java.util.Optional;

/**
 * Trait für Plots mit benutzerdefinierten Namen.
 *
 * Implementierungen:
 * - TradeguildPlot
 * - MarketPlot
 * - ResidencePlot
 *
 * @author FallenStar
 * @version 2.0
 */
public interface NamedPlot extends Plot {

    /**
     * @return Optional mit Custom-Namen, oder empty wenn nicht gesetzt
     */
    Optional<String> getCustomName();

    /**
     * Setzt den benutzerdefinierten Namen.
     *
     * @param name Der neue Name (max. 32 Zeichen)
     * @throws IllegalArgumentException wenn Name ungültig
     */
    void setCustomName(String name);

    /**
     * Entfernt den Custom-Namen.
     */
    void clearCustomName();

    /**
     * @return Anzeige-Name (Custom oder Default)
     */
    default String getDisplayName() {
        return getCustomName().orElse("Plot #" + getIdentifier());
    }

    /**
     * @return true wenn Custom-Name gesetzt
     */
    default boolean hasCustomName() {
        return getCustomName().isPresent();
    }
}
```

---

#### 1.3 Trait: `StorageContainerPlot`

**Location:** `core/src/main/java/de/fallenstar/core/provider/StorageContainerPlot.java`

```java
package de.fallenstar.core.provider;

/**
 * Trait für Plots mit Storage-System.
 *
 * Plots die dieses Interface implementieren haben Input/Output-Chests.
 *
 * Implementierungen:
 * - TradeguildPlot
 * - WarehousePlot (später)
 *
 * @author FallenStar
 * @version 2.0
 */
public interface StorageContainerPlot extends Plot {

    /**
     * @return PlotStorage-Instanz für dieses Plot
     */
    PlotStorage getPlotStorage();

    /**
     * @return true wenn Storage konfiguriert ist
     */
    default boolean hasStorage() {
        return getPlotStorage() != null;
    }
}
```

---

#### 1.4 Trait: `NpcContainerPlot`

**Location:** `core/src/main/java/de/fallenstar/core/provider/NpcContainerPlot.java`

```java
package de.fallenstar.core.provider;

/**
 * Trait für Plots die NPCs besitzen können.
 *
 * NPCs sind an das Plot gebunden (für Verwaltung/Ownership).
 * Sie können physisch auf anderen Plots stehen (via Slots).
 *
 * Implementierungen:
 * - TradeguildPlot
 * - MarketPlot
 *
 * @author FallenStar
 * @version 2.0
 */
public interface NpcContainerPlot extends Plot {

    /**
     * @return NpcContainer für dieses Plot
     */
    NpcContainer getNpcContainer();

    /**
     * @return true wenn NPCs vorhanden
     */
    default boolean hasNPCs() {
        return getNpcContainer().getNPCCount() > 0;
    }
}
```

---

#### 1.5 Trait: `SlottablePlot`

**Location:** `core/src/main/java/de/fallenstar/core/provider/SlottablePlot.java`

```java
package de.fallenstar.core.provider;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Trait für Plots mit NPC-Slots.
 *
 * Slots ermöglichen es, fremde NPCs temporär zu platzieren.
 * Unterschied zu NpcContainerPlot:
 * - NpcContainerPlot: NPCs GEHÖREN dem Plot
 * - SlottablePlot: NPCs werden GEMIETET/PLATZIERT
 *
 * Implementierungen:
 * - MarketPlot (Marktplätze haben Händler-Slots)
 * - EventPlot (Event-Plätze haben temporäre NPC-Slots)
 *
 * @author FallenStar
 * @version 2.0
 */
public interface SlottablePlot extends Plot {

    /**
     * @return Alle NPC-Slots dieses Plots
     */
    List<NpcSlot> getSlots();

    /**
     * @return Maximale Anzahl an Slots
     */
    int getMaxSlots();

    /**
     * @return Anzahl freier Slots
     */
    default int getFreeSlotCount() {
        return (int) getSlots().stream()
                .filter(slot -> !slot.isOccupied())
                .count();
    }

    /**
     * Findet einen freien Slot.
     *
     * @return Optional mit freiem Slot, oder empty wenn alle belegt
     */
    default Optional<NpcSlot> findFreeSlot() {
        return getSlots().stream()
                .filter(slot -> !slot.isOccupied())
                .findFirst();
    }

    /**
     * Findet Slot für bestimmten NPC.
     *
     * @param npcId UUID des NPCs
     * @return Optional mit Slot, oder empty wenn NPC nicht auf Plot
     */
    default Optional<NpcSlot> findSlotForNPC(UUID npcId) {
        return getSlots().stream()
                .filter(slot -> slot.isOccupied() &&
                               slot.getAssignedNPC().equals(Optional.of(npcId)))
                .findFirst();
    }
}
```

---

#### 1.6 Konkrete Plot-Typen

##### TradeguildPlot (Handelsgilde)

**Location:** `module-plots/src/main/java/de/fallenstar/plot/model/TradeguildPlot.java`

```java
package de.fallenstar.plot.model;

import de.fallenstar.core.provider.*;
import org.bukkit.Location;
import java.util.UUID;
import java.util.Optional;

/**
 * Handelsgilde-Grundstück.
 *
 * Features:
 * - Custom-Namen (NamedPlot)
 * - Storage-System (StorageContainerPlot)
 * - NPCs besitzen (NpcContainerPlot)
 * - Preis-Management (ItemBasePriceProvider via PlotPriceManager)
 *
 * @author FallenStar
 * @version 2.0
 */
public class TradeguildPlot implements NamedPlot, StorageContainerPlot, NpcContainerPlot {

    private final UUID uuid;
    private final String identifier;
    private final Location location;
    private final Object nativePlot;

    // Trait-Implementierungen
    private final PlotNameManager nameManager;
    private final PlotStorage plotStorage;
    private final NpcContainer npcContainer;

    public TradeguildPlot(
            UUID uuid,
            String identifier,
            Location location,
            Object nativePlot,
            PlotNameManager nameManager,
            PlotStorage plotStorage,
            NpcContainer npcContainer
    ) {
        this.uuid = uuid;
        this.identifier = identifier;
        this.location = location;
        this.nativePlot = nativePlot;
        this.nameManager = nameManager;
        this.plotStorage = plotStorage;
        this.npcContainer = npcContainer;
    }

    // Plot Interface
    @Override
    public UUID getUuid() {
        return uuid;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public String getPlotType() {
        return "handelsgilde";
    }

    @Override
    public <T> T getNativePlot() {
        return (T) nativePlot;
    }

    // NamedPlot Interface
    @Override
    public Optional<String> getCustomName() {
        String name = nameManager.getPlotName(this);
        return Optional.ofNullable(name);
    }

    @Override
    public void setCustomName(String name) {
        if (!isValidName(name)) {
            throw new IllegalArgumentException("Ungültiger Name: " + name);
        }
        nameManager.setPlotName(this, name);
    }

    @Override
    public void clearCustomName() {
        nameManager.setPlotName(this, null);
    }

    // StorageContainerPlot Interface
    @Override
    public PlotStorage getPlotStorage() {
        return plotStorage;
    }

    // NpcContainerPlot Interface
    @Override
    public NpcContainer getNpcContainer() {
        return npcContainer;
    }

    // Helper
    private static boolean isValidName(String name) {
        if (name == null || name.isEmpty() || name.length() > 32) {
            return false;
        }
        return name.matches("[a-zA-Z0-9äöüÄÖÜß \\-_]+");
    }
}
```

##### MarketPlot (Marktplatz)

**Location:** `module-plots/src/main/java/de/fallenstar/plot/model/MarketPlot.java`

```java
package de.fallenstar.plot.model;

import de.fallenstar.core.provider.*;
import org.bukkit.Location;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

/**
 * Marktplatz-Grundstück.
 *
 * Features:
 * - Custom-Namen (NamedPlot)
 * - NPC-Slots für fremde Händler (SlottablePlot)
 * - Keine eigenen NPCs (kein NpcContainerPlot)
 * - Kein Storage (kein StorageContainerPlot)
 *
 * @author FallenStar
 * @version 2.0
 */
public class MarketPlot implements NamedPlot, SlottablePlot {

    private final UUID uuid;
    private final String identifier;
    private final Location location;
    private final Object nativePlot;

    private final PlotNameManager nameManager;
    private final List<NpcSlot> slots;
    private final int maxSlots;

    public MarketPlot(
            UUID uuid,
            String identifier,
            Location location,
            Object nativePlot,
            PlotNameManager nameManager,
            List<NpcSlot> slots,
            int maxSlots
    ) {
        this.uuid = uuid;
        this.identifier = identifier;
        this.location = location;
        this.nativePlot = nativePlot;
        this.nameManager = nameManager;
        this.slots = slots;
        this.maxSlots = maxSlots;
    }

    // Plot Interface
    @Override
    public UUID getUuid() {
        return uuid;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public String getPlotType() {
        return "marktplatz";
    }

    @Override
    public <T> T getNativePlot() {
        return (T) nativePlot;
    }

    // NamedPlot Interface
    @Override
    public Optional<String> getCustomName() {
        String name = nameManager.getPlotName(this);
        return Optional.ofNullable(name);
    }

    @Override
    public void setCustomName(String name) {
        if (!isValidName(name)) {
            throw new IllegalArgumentException("Ungültiger Name: " + name);
        }
        nameManager.setPlotName(this, name);
    }

    @Override
    public void clearCustomName() {
        nameManager.setPlotName(this, null);
    }

    // SlottablePlot Interface
    @Override
    public List<NpcSlot> getSlots() {
        return slots;
    }

    @Override
    public int getMaxSlots() {
        return maxSlots;
    }

    // Helper
    private static boolean isValidName(String name) {
        if (name == null || name.isEmpty() || name.length() > 32) {
            return false;
        }
        return name.matches("[a-zA-Z0-9äöüÄÖÜß \\-_]+");
    }
}
```

---

### Phase 2: PlotProvider-Anpassung

**PlotProvider muss typisierte Plots zurückgeben:**

```java
package de.fallenstar.core.provider;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * PlotProvider - Refactored für Interface-basierte Plots.
 *
 * @author FallenStar
 * @version 2.0
 */
public interface PlotProvider {

    /**
     * @return true wenn Provider verfügbar
     */
    boolean isAvailable();

    /**
     * Gibt ein Plot an einer Location zurück.
     *
     * @param location Location
     * @return Plot oder null wenn keine vorhanden
     * @throws ProviderFunctionalityNotFoundException wenn nicht verfügbar
     */
    Plot getPlot(Location location) throws ProviderFunctionalityNotFoundException;

    /**
     * Gibt ein typisiertes Plot zurück (wenn es den Typ hat).
     *
     * @param location Location
     * @param plotType Plot-Typ-Klasse
     * @return Optional mit typisiertem Plot, oder empty
     */
    <T extends Plot> Optional<T> getPlotAs(Location location, Class<T> plotType)
            throws ProviderFunctionalityNotFoundException;

    /**
     * Prüft ob Plot ein bestimmtes Trait hat.
     *
     * @param plot Das Plot
     * @param trait Trait-Interface
     * @return true wenn Plot das Trait implementiert
     */
    default <T extends Plot> boolean hasTrait(Plot plot, Class<T> trait) {
        return trait.isInstance(plot);
    }

    // ... weitere Methoden ...
}
```

**Verwendung:**
```java
// Generic
Plot plot = plotProvider.getPlot(location);

// Typed
Optional<TradeguildPlot> tradeguild = plotProvider.getPlotAs(location, TradeguildPlot.class);
if (tradeguild.isPresent()) {
    // Type-safe access!
    PlotStorage storage = tradeguild.get().getPlotStorage();
}

// Trait-Check
if (plotProvider.hasTrait(plot, StorageContainerPlot.class)) {
    StorageContainerPlot storagePlot = (StorageContainerPlot) plot;
    PlotStorage storage = storagePlot.getPlotStorage();
}
```

---

## 🎨 UI-System Refactoring

### Phase 3: ChildUI-Interface

#### 3.1 Core-Interface: `ChildUi`

**Location:** `core/src/main/java/de/fallenstar/core/ui/ChildUi.java`

```java
package de.fallenstar.core.ui;

import org.bukkit.entity.Player;

/**
 * Interface für Child-UIs die ein Parent-UI haben.
 *
 * Ermöglicht Navigation zurück zum Parent (Back-Button).
 *
 * @param <P> Parent-UI-Typ
 *
 * @author FallenStar
 * @version 2.0
 */
public interface ChildUi<P extends UiParent> {

    /**
     * @return Parent-UI-Instanz
     */
    P getParent();

    /**
     * @return Parent-UI-Klasse (für Type-Checks)
     */
    Class<P> getParentUiClass();

    /**
     * Öffnet das Parent-UI für einen Spieler.
     *
     * @param player Der Spieler
     */
    default void openParent(Player player) {
        P parent = getParent();
        if (parent != null) {
            parent.open(player);
        }
    }

    /**
     * Schließt dieses UI und öffnet Parent.
     *
     * @param player Der Spieler
     */
    default void closeAndOpenParent(Player player) {
        // UI schließen (muss von konkreter UI implementiert werden)
        if (this instanceof BaseUi) {
            ((BaseUi) this).close(player);
        }
        openParent(player);
    }
}
```

---

#### 3.2 Navigation-Button: `BackButton`

**Location:** `core/src/main/java/de/fallenstar/core/ui/element/navigation/BackButton.java`

```java
package de.fallenstar.core.ui.element.navigation;

import de.fallenstar.core.ui.ChildUi;
import de.fallenstar.core.ui.UiParent;
import de.fallenstar.core.ui.element.ClickableUiElement;
import de.fallenstar.core.ui.element.UiAction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Back-Button für ChildUis.
 *
 * Öffnet automatisch das Parent-UI.
 *
 * @author FallenStar
 * @version 2.0
 */
public class BackButton {

    /**
     * Erstellt einen Back-Button für ein ChildUi.
     *
     * @param childUi Das Child-UI
     * @return ClickableUiElement mit Back-Action
     */
    public static <P extends UiParent> ClickableUiElement<BackAction<P>> create(ChildUi<P> childUi) {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(
                Component.text("« Zurück")
                        .color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        );

        item.setItemMeta(meta);

        return new ClickableUiElement.CustomButton<>(
                item,
                new BackAction<>(childUi)
        );
    }

    /**
     * Back-Action (Type-Safe).
     */
    public static final class BackAction<P extends UiParent> implements UiAction {
        private final ChildUi<P> childUi;

        public BackAction(ChildUi<P> childUi) {
            this.childUi = childUi;
        }

        @Override
        public void execute(Player player) {
            childUi.openParent(player);
        }

        @Override
        public String getActionName() {
            return "Back[" + childUi.getParentUiClass().getSimpleName() + "]";
        }
    }
}
```

---

#### 3.3 UI-Hierarchie-Beispiel

##### Parent: `PlotMainMenuUi`

**Location:** `module-plots/src/main/java/de/fallenstar/plot/ui/PlotMainMenuUi.java`

```java
package de.fallenstar.plot.ui;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.ui.container.GenericUiSmallChest;
import de.fallenstar.core.ui.element.navigation.CloseButton;
import de.fallenstar.core.ui.row.BasicUiRowForContent;
import de.fallenstar.core.registry.ProviderRegistry;
import org.bukkit.entity.Player;

/**
 * Haupt-Menü für Plot-Verwaltung.
 *
 * Zeigt verschiedene Verwaltungs-Optionen:
 * - Storage-Verwaltung
 * - NPC-Verwaltung
 * - Preis-Verwaltung
 * - Plot-Informationen
 *
 * @author FallenStar
 * @version 2.0
 */
public class PlotMainMenuUi extends GenericUiSmallChest {

    private final Plot plot;
    private final ProviderRegistry providers;
    private final boolean isOwner;

    public PlotMainMenuUi(Plot plot, ProviderRegistry providers, boolean isOwner) {
        super("§6§l" + getPlotTitle(plot));
        this.plot = plot;
        this.providers = providers;
        this.isOwner = isOwner;

        buildUi();
    }

    private void buildUi() {
        // Row 0: Navigation
        var navigationRow = new BasicUiRowForContent();
        navigationRow.setElement(0, CloseButton.create(this));
        setRow(0, navigationRow);

        // Row 1: Optionen
        var optionsRow = new BasicUiRowForContent();

        // Option: Storage
        if (plot instanceof StorageContainerPlot) {
            optionsRow.setElement(1, createStorageButton());
        }

        // Option: NPCs
        if (plot instanceof NpcContainerPlot && isOwner) {
            optionsRow.setElement(3, createNpcManagementButton());
        }

        // Option: Preise
        if (isOwner) {
            optionsRow.setElement(5, createPriceManagementButton());
        }

        setRow(1, optionsRow);
    }

    private ClickableUiElement<?> createStorageButton() {
        // Öffnet PlotStorageUi (Child)
        // ...
    }

    private ClickableUiElement<?> createNpcManagementButton() {
        // Öffnet PlotNpcManagementUi (Child)
        // ...
    }

    private ClickableUiElement<?> createPriceManagementButton() {
        // Öffnet PlotPriceManagementUi (Child)
        // ...
    }

    private static String getPlotTitle(Plot plot) {
        if (plot instanceof NamedPlot) {
            return ((NamedPlot) plot).getDisplayName();
        }
        return "Plot #" + plot.getIdentifier();
    }
}
```

##### Child: `PlotStorageUi`

**Location:** `module-plots/src/main/java/de/fallenstar/plot/ui/PlotStorageUi.java`

```java
package de.fallenstar.plot.ui;

import de.fallenstar.core.provider.StorageContainerPlot;
import de.fallenstar.core.ui.ChildUi;
import de.fallenstar.core.ui.container.GenericUiLargeChest;
import de.fallenstar.core.ui.element.navigation.BackButton;
import de.fallenstar.core.ui.row.BasicUiRowForContent;

/**
 * Storage-Verwaltungs-UI.
 *
 * Child von PlotMainMenuUi.
 *
 * @author FallenStar
 * @version 2.0
 */
public class PlotStorageUi extends GenericUiLargeChest
                            implements ChildUi<PlotMainMenuUi> {

    private final StorageContainerPlot plot;
    private final PlotMainMenuUi parent;
    private final boolean isOwner;

    public PlotStorageUi(
            StorageContainerPlot plot,
            PlotMainMenuUi parent,
            boolean isOwner
    ) {
        super("§6§lStorage-Verwaltung");
        this.plot = plot;
        this.parent = parent;
        this.isOwner = isOwner;

        buildUi();
    }

    private void buildUi() {
        // Row 0: Navigation mit Back-Button
        var navigationRow = new BasicUiRowForContent();
        navigationRow.setElement(0, BackButton.create(this)); // ← Type-Safe Back-Button!
        setRow(0, navigationRow);

        // ... Rest des UI
    }

    // ChildUi Interface
    @Override
    public PlotMainMenuUi getParent() {
        return parent;
    }

    @Override
    public Class<PlotMainMenuUi> getParentUiClass() {
        return PlotMainMenuUi.class;
    }
}
```

##### Grandchild: `StoragePriceUi`

**Location:** `module-plots/src/main/java/de/fallenstar/plot/ui/StoragePriceUi.java`

```java
package de.fallenstar.plot.ui;

import de.fallenstar.core.ui.ChildUi;
import de.fallenstar.core.ui.container.GenericUiLargeChest;
import de.fallenstar.core.ui.element.navigation.BackButton;
import de.fallenstar.plot.gui.PriceEditorContext;

/**
 * Preis-Editor-UI.
 *
 * Child von PlotPriceManagementUi (welches Child von PlotMainMenuUi ist).
 *
 * @author FallenStar
 * @version 2.0
 */
public class StoragePriceUi extends GenericUiLargeChest
                             implements ChildUi<PlotPriceManagementUi> {

    private final PriceEditorContext context;
    private final PlotPriceManagementUi parent;

    public StoragePriceUi(
            PriceEditorContext context,
            PlotPriceManagementUi parent,
            Consumer<PriceEditorContext> onConfirm,
            Runnable onCancel
    ) {
        super("§6§lPreise festlegen");
        this.context = context;
        this.parent = parent;

        buildUi();
    }

    private void buildUi() {
        // Row 0: Navigation
        var navigationRow = new BasicUiRowForContent();
        navigationRow.setElement(0, BackButton.create(this)); // ← Zurück zu PlotPriceManagementUi
        setRow(0, navigationRow);

        // ... Rest des UI
    }

    // ChildUi Interface
    @Override
    public PlotPriceManagementUi getParent() {
        return parent;
    }

    @Override
    public Class<PlotPriceManagementUi> getParentUiClass() {
        return PlotPriceManagementUi.class;
    }
}
```

**UI-Hierarchie:**
```
PlotMainMenuUi (Root)
  ├─ PlotStorageUi (Child)
  ├─ PlotNpcManagementUi (Child)
  │   └─ NpcPlacementUi (Grandchild)
  └─ PlotPriceManagementUi (Child)
      └─ StoragePriceUi (Grandchild)
          ↑ Back-Button → PlotPriceManagementUi
          ↑ Grandparent: PlotMainMenuUi
```

---

## 🤖 NPC-Container-System

### Phase 4: Generische NPC-Container-Abstraktion

#### 4.1 Core-Interface: `NpcContainer`

**Location:** `core/src/main/java/de/fallenstar/core/provider/NpcContainer.java`

```java
package de.fallenstar.core.provider;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface für Entities die NPCs besitzen können.
 *
 * Implementierungen:
 * - PlotNpcContainer (Plots-Modul)
 * - PlayerNpcOwnership (NPCs-Modul)
 *
 * @author FallenStar
 * @version 2.0
 */
public interface NpcContainer {

    /**
     * @return UUID des Containers (Plot-UUID oder Player-UUID)
     */
    UUID getContainerId();

    /**
     * @return Container-Typ ("plot" oder "player")
     */
    String getContainerType();

    /**
     * @return Liste aller NPC-IDs in diesem Container
     */
    List<UUID> getNPCs();

    /**
     * @return Anzahl der NPCs
     */
    default int getNPCCount() {
        return getNPCs().size();
    }

    /**
     * Fügt einen NPC hinzu.
     *
     * @param npcId UUID des NPCs
     * @param npcType NPC-Typ (z.B. "guildtrader")
     * @return true wenn erfolgreich hinzugefügt
     */
    boolean addNPC(UUID npcId, String npcType);

    /**
     * Entfernt einen NPC.
     *
     * @param npcId UUID des NPCs
     * @return true wenn erfolgreich entfernt
     */
    boolean removeNPC(UUID npcId);

    /**
     * Prüft ob NPC in Container vorhanden.
     *
     * @param npcId UUID des NPCs
     * @return true wenn vorhanden
     */
    default boolean hasNPC(UUID npcId) {
        return getNPCs().contains(npcId);
    }

    /**
     * @return Maximale Anzahl an NPCs für diesen Container
     */
    int getMaxNPCs();

    /**
     * @return true wenn Container voll ist
     */
    default boolean isFull() {
        return getNPCCount() >= getMaxNPCs();
    }

    /**
     * @return Anzahl freier NPC-Plätze
     */
    default int getFreeSlots() {
        return Math.max(0, getMaxNPCs() - getNPCCount());
    }

    /**
     * Gibt NPC-Info für bestimmten NPC zurück.
     *
     * @param npcId UUID des NPCs
     * @return Optional mit NPC-Info
     */
    Optional<NpcInfo> getNpcInfo(UUID npcId);

    /**
     * Speichert Container-Daten.
     */
    void save();

    /**
     * Lädt Container-Daten.
     */
    void load();

    /**
     * NPC-Informationen.
     *
     * @param npcId UUID des NPCs
     * @param npcType NPC-Typ
     * @param addedTime Zeitpunkt der Hinzufügung (Unix timestamp)
     */
    record NpcInfo(UUID npcId, String npcType, long addedTime) {}
}
```

---

#### 4.2 Implementation: `PlotNpcContainer`

**Location:** `module-plots/src/main/java/de/fallenstar/plot/npc/PlotNpcContainer.java`

```java
package de.fallenstar.plot.npc;

import de.fallenstar.core.provider.NpcContainer;
import de.fallenstar.core.provider.Plot;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.logging.Logger;

/**
 * NPC-Container für Plots.
 *
 * Verwaltet NPCs die zu einem Plot gehören.
 *
 * @author FallenStar
 * @version 2.0
 */
public class PlotNpcContainer implements NpcContainer {

    private final Plot plot;
    private final int maxNPCs;
    private final Logger logger;
    private final FileConfiguration config;

    private final Map<UUID, NpcInfo> npcs;

    public PlotNpcContainer(Plot plot, int maxNPCs, FileConfiguration config, Logger logger) {
        this.plot = plot;
        this.maxNPCs = maxNPCs;
        this.config = config;
        this.logger = logger;
        this.npcs = new HashMap<>();
    }

    @Override
    public UUID getContainerId() {
        return plot.getUuid();
    }

    @Override
    public String getContainerType() {
        return "plot";
    }

    @Override
    public List<UUID> getNPCs() {
        return new ArrayList<>(npcs.keySet());
    }

    @Override
    public boolean addNPC(UUID npcId, String npcType) {
        if (isFull()) {
            logger.warning("Cannot add NPC to plot " + plot.getUuid() + " - container full");
            return false;
        }

        NpcInfo info = new NpcInfo(npcId, npcType, System.currentTimeMillis());
        npcs.put(npcId, info);

        save();

        logger.info("Added NPC " + npcId + " (" + npcType + ") to plot " + plot.getUuid());
        return true;
    }

    @Override
    public boolean removeNPC(UUID npcId) {
        NpcInfo removed = npcs.remove(npcId);

        if (removed != null) {
            save();
            logger.info("Removed NPC " + npcId + " from plot " + plot.getUuid());
            return true;
        }

        return false;
    }

    @Override
    public int getMaxNPCs() {
        return maxNPCs;
    }

    @Override
    public Optional<NpcInfo> getNpcInfo(UUID npcId) {
        return Optional.ofNullable(npcs.get(npcId));
    }

    @Override
    public void save() {
        String path = "plot-npc-containers." + plot.getUuid();

        // Clear alte Daten
        config.set(path, null);

        // Speichere NPCs
        List<Map<String, Object>> npcList = new ArrayList<>();
        for (NpcInfo info : npcs.values()) {
            Map<String, Object> npcData = new HashMap<>();
            npcData.put("npc-id", info.npcId().toString());
            npcData.put("type", info.npcType());
            npcData.put("added-time", info.addedTime());
            npcList.add(npcData);
        }

        config.set(path + ".npcs", npcList);
        config.set(path + ".max-npcs", maxNPCs);
    }

    @Override
    public void load() {
        String path = "plot-npc-containers." + plot.getUuid();

        ConfigurationSection section = config.getConfigurationSection(path);
        if (section == null) {
            logger.fine("No NPC container data for plot " + plot.getUuid());
            return;
        }

        List<Map<?, ?>> npcList = section.getMapList("npcs");
        for (Map<?, ?> npcData : npcList) {
            try {
                UUID npcId = UUID.fromString((String) npcData.get("npc-id"));
                String npcType = (String) npcData.get("type");
                long addedTime = ((Number) npcData.get("added-time")).longValue();

                NpcInfo info = new NpcInfo(npcId, npcType, addedTime);
                npcs.put(npcId, info);

            } catch (Exception e) {
                logger.warning("Failed to load NPC from container: " + e.getMessage());
            }
        }

        logger.info("Loaded " + npcs.size() + " NPCs for plot " + plot.getUuid());
    }
}
```

---

#### 4.3 Implementation: `PlayerNpcOwnership`

**Location:** `module-npcs/src/main/java/de/fallenstar/npc/player/PlayerNpcOwnership.java`

```java
package de.fallenstar.npc.player;

import de.fallenstar.core.provider.NpcContainer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.logging.Logger;

/**
 * NPC-Container für Spieler.
 *
 * Verwaltet NPCs die ein Spieler besitzt.
 *
 * @author FallenStar
 * @version 2.0
 */
public class PlayerNpcOwnership implements NpcContainer {

    private final UUID playerId;
    private final int maxNPCs;
    private final Logger logger;
    private final FileConfiguration config;

    private final Map<UUID, NpcInfo> npcs;

    public PlayerNpcOwnership(UUID playerId, int maxNPCs, FileConfiguration config, Logger logger) {
        this.playerId = playerId;
        this.maxNPCs = maxNPCs;
        this.config = config;
        this.logger = logger;
        this.npcs = new HashMap<>();
    }

    @Override
    public UUID getContainerId() {
        return playerId;
    }

    @Override
    public String getContainerType() {
        return "player";
    }

    @Override
    public List<UUID> getNPCs() {
        return new ArrayList<>(npcs.keySet());
    }

    @Override
    public boolean addNPC(UUID npcId, String npcType) {
        if (isFull()) {
            logger.warning("Cannot add NPC to player " + playerId + " - container full");
            return false;
        }

        NpcInfo info = new NpcInfo(npcId, npcType, System.currentTimeMillis());
        npcs.put(npcId, info);

        save();

        logger.info("Added NPC " + npcId + " (" + npcType + ") to player " + playerId);
        return true;
    }

    @Override
    public boolean removeNPC(UUID npcId) {
        NpcInfo removed = npcs.remove(npcId);

        if (removed != null) {
            save();
            logger.info("Removed NPC " + npcId + " from player " + playerId);
            return true;
        }

        return false;
    }

    @Override
    public int getMaxNPCs() {
        return maxNPCs;
    }

    @Override
    public Optional<NpcInfo> getNpcInfo(UUID npcId) {
        return Optional.ofNullable(npcs.get(npcId));
    }

    @Override
    public void save() {
        String path = "player-npc-ownership." + playerId;

        // Clear alte Daten
        config.set(path, null);

        // Speichere NPCs
        List<Map<String, Object>> npcList = new ArrayList<>();
        for (NpcInfo info : npcs.values()) {
            Map<String, Object> npcData = new HashMap<>();
            npcData.put("npc-id", info.npcId().toString());
            npcData.put("type", info.npcType());
            npcData.put("added-time", info.addedTime());
            npcList.add(npcData);
        }

        config.set(path + ".npcs", npcList);
        config.set(path + ".max-npcs", maxNPCs);
    }

    @Override
    public void load() {
        String path = "player-npc-ownership." + playerId;

        ConfigurationSection section = config.getConfigurationSection(path);
        if (section == null) {
            logger.fine("No NPC ownership data for player " + playerId);
            return;
        }

        List<Map<?, ?>> npcList = section.getMapList("npcs");
        for (Map<?, ?> npcData : npcList) {
            try {
                UUID npcId = UUID.fromString((String) npcData.get("npc-id"));
                String npcType = (String) npcData.get("type");
                long addedTime = ((Number) npcData.get("added-time")).longValue();

                NpcInfo info = new NpcInfo(npcId, npcType, addedTime);
                npcs.put(npcId, info);

            } catch (Exception e) {
                logger.warning("Failed to load NPC from ownership: " + e.getMessage());
            }
        }

        logger.info("Loaded " + npcs.size() + " NPCs for player " + playerId);
    }
}
```

**Vorteil:** Beide nutzen die gleiche API (NpcContainer), aber unterschiedliche Persistierung!

---

## 📢 Event-System

### Phase 5: Type-Safe Events

#### 5.1 Plot-Events

**Location:** `core/src/main/java/de/fallenstar/core/event/plot/PlotEvent.java`

```java
package de.fallenstar.core.event.plot;

import de.fallenstar.core.provider.Plot;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Basis-Event für Plot-bezogene Events.
 *
 * @author FallenStar
 * @version 2.0
 */
public abstract class PlotEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Plot plot;

    protected PlotEvent(Plot plot) {
        this.plot = plot;
    }

    public Plot getPlot() {
        return plot;
    }

    /**
     * Type-Safe Plot-Zugriff.
     *
     * @param plotType Plot-Typ-Klasse
     * @return Optional mit typisiertem Plot
     */
    public <T extends Plot> Optional<T> getPlotAs(Class<T> plotType) {
        if (plotType.isInstance(plot)) {
            return Optional.of(plotType.cast(plot));
        }
        return Optional.empty();
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
```

**Konkrete Events:**

```java
// PlotStorageChangedEvent
public class PlotStorageChangedEvent extends PlotEvent {
    private final StorageChangeType changeType;

    public enum StorageChangeType {
        INPUT_ADDED, OUTPUT_ADDED, INPUT_REMOVED, OUTPUT_REMOVED
    }

    public PlotStorageChangedEvent(StorageContainerPlot plot, StorageChangeType changeType) {
        super(plot);
        this.changeType = changeType;
    }

    public StorageChangeType getChangeType() {
        return changeType;
    }

    public StorageContainerPlot getStoragePlot() {
        return (StorageContainerPlot) getPlot();
    }
}

// PlotNpcAddedEvent
public class PlotNpcAddedEvent extends PlotEvent {
    private final UUID npcId;
    private final String npcType;

    public PlotNpcAddedEvent(NpcContainerPlot plot, UUID npcId, String npcType) {
        super(plot);
        this.npcId = npcId;
        this.npcType = npcType;
    }

    public UUID getNpcId() {
        return npcId;
    }

    public String getNpcType() {
        return npcType;
    }

    public NpcContainerPlot getNpcPlot() {
        return (NpcContainerPlot) getPlot();
    }
}

// PlotNpcRemovedEvent
public class PlotNpcRemovedEvent extends PlotEvent {
    private final UUID npcId;

    public PlotNpcRemovedEvent(NpcContainerPlot plot, UUID npcId) {
        super(plot);
        this.npcId = npcId;
    }

    public UUID getNpcId() {
        return npcId;
    }

    public NpcContainerPlot getNpcPlot() {
        return (NpcContainerPlot) getPlot();
    }
}
```

---

#### 5.2 UI-Events

```java
package de.fallenstar.core.event.ui;

import de.fallenstar.core.ui.BaseUi;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event wenn UI geöffnet wird.
 *
 * @author FallenStar
 * @version 2.0
 */
public class UiOpenEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final BaseUi ui;
    private boolean cancelled;

    public UiOpenEvent(Player player, BaseUi ui) {
        this.player = player;
        this.ui = ui;
        this.cancelled = false;
    }

    public Player getPlayer() {
        return player;
    }

    public BaseUi getUi() {
        return ui;
    }

    public <T extends BaseUi> Optional<T> getUiAs(Class<T> uiType) {
        if (uiType.isInstance(ui)) {
            return Optional.of(uiType.cast(ui));
        }
        return Optional.empty();
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}

// UiCloseEvent
public class UiCloseEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final BaseUi ui;

    public UiCloseEvent(Player player, BaseUi ui) {
        this.player = player;
        this.ui = ui;
    }

    // ... getters ...

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
```

---

## 🚀 Implementierungs-Phasen

### Phase 1: Plot-Interface-Refactoring (Sprint 15)

**Ziel:** Plot wird zu Interface-Hierarchie

**Tasks:**
1. ✅ Plot-Interface erstellen (core)
2. ✅ Trait-Interfaces erstellen (NamedPlot, StorageContainerPlot, NpcContainerPlot, SlottablePlot)
3. ✅ Konkrete Plot-Implementierungen (TradeguildPlot, MarketPlot)
4. ✅ PlotProvider anpassen (getPlotAs, hasTrait)
5. ✅ TownyPlotProvider refactoren
6. ✅ Tests schreiben

**Breaking Changes:**
- Plot ist jetzt Interface statt Klasse
- NamedPlot ist Interface statt Subklasse

**Migration:**
```java
// Alt
Plot plot = plotProvider.getPlot(location);
if (plot instanceof NamedPlot) {
    NamedPlot namedPlot = (NamedPlot) plot;
}

// Neu (gleich!)
Plot plot = plotProvider.getPlot(location);
if (plot instanceof NamedPlot) {
    NamedPlot namedPlot = (NamedPlot) plot;
}
// → Code bleibt gleich durch Polymorphie!
```

---

### Phase 2: UI-Navigation-System (Sprint 15-16)

**Ziel:** ChildUI-Interface + Back-Buttons

**Tasks:**
1. ✅ ChildUI-Interface erstellen
2. ✅ BackButton-Element erstellen
3. ✅ Bestehende UIs zu ChildUIs konvertieren
4. ✅ PlotMainMenuUi als Root erstellen
5. ✅ UI-Hierarchie umsetzen
6. ✅ Tests schreiben

**Nicht-Breaking:**
- Alte UIs funktionieren weiter
- Neue UIs können ChildUI implementieren

---

### Phase 3: NPC-Container-Abstraktion (Sprint 16)

**Ziel:** Gemeinsames NpcContainer-Interface

**Tasks:**
1. ✅ NpcContainer-Interface erstellen
2. ✅ PlotNpcContainer implementieren
3. ✅ PlotBoundNPCRegistry refactoren (nutzt PlotNpcContainer)
4. ✅ PlayerNpcOwnership implementieren (nutzt NpcContainer)
5. ✅ Tests schreiben

**Vorteil:**
- PlotBoundNPCRegistry und PlayerNpcOwnership nutzen gleiche API
- Später: NpcContainerManager (generisch für beide)

---

### Phase 4: Event-System-Integration (Sprint 16-17)

**Ziel:** Type-Safe Events

**Tasks:**
1. ✅ PlotEvent-Hierarchie erstellen
2. ✅ UiEvent-Hierarchie erstellen
3. ✅ Events in Manager integrieren
4. ✅ Event-Handler-Beispiele
5. ✅ Dokumentation

---

### Phase 5: Dokumentation & Migration-Guide (Sprint 17)

**Tasks:**
1. ✅ ARCHITECTURE_REFACTORING.md finalisieren
2. ✅ MIGRATION_GUIDE.md schreiben
3. ✅ Code-Beispiele aktualisieren
4. ✅ README.md updaten

---

## 📚 Vorteile des neuen Designs

### ✅ Type-Safety

```java
// Compiler verhindert Fehler!
Optional<TradeguildPlot> tradeguild = plotProvider.getPlotAs(location, TradeguildPlot.class);

if (tradeguild.isPresent()) {
    // Alle Trait-Methoden verfügbar!
    PlotStorage storage = tradeguild.get().getPlotStorage();
    NpcContainer npcs = tradeguild.get().getNpcContainer();
    String name = tradeguild.get().getDisplayName();
}
```

### ✅ Trait-Komposition

```java
// Flexibel kombinierbar!
public class TradeguildPlot implements
        NamedPlot,              // Custom-Namen
        StorageContainerPlot,   // Storage-System
        NpcContainerPlot {      // NPC-Verwaltung
    // Hat ALLE Traits!
}
```

### ✅ UI-Navigation

```java
// Automatische Back-Buttons!
public class ChildUi extends GenericUiLargeChest
                      implements ChildUi<ParentUi> {

    private void buildUi() {
        setElement(0, BackButton.create(this)); // ← One-Liner!
    }
}
```

### ✅ Wiederverwendbarkeit

```java
// Gleiche API für Plot UND Player!
public interface NpcContainer {
    List<UUID> getNPCs();
    boolean addNPC(UUID npcId, String npcType);
    // ...
}

// Beide nutzen NpcContainer
PlotNpcContainer plotNpcs = ...;
PlayerNpcOwnership playerNpcs = ...;
// → Gleiche Methoden!
```

### ✅ Testbarkeit

```java
// Mock-Implementierungen trivial!
public class MockPlot implements Plot, NamedPlot {
    // Minimale Implementation für Tests
}
```

---

## 🎯 Zusammenfassung

**Kernverbesserungen:**
1. **Plot als Interface** - Trait-Komposition statt Vererbung
2. **ChildUI-Pattern** - Navigation mit Back-Buttons
3. **NpcContainer-Abstraktion** - Wiederverwendbar für Plot & Player
4. **Type-Safe Events** - Compiler-geprüfte Event-Handler

**Migration:** Phasenweise über 3-4 Sprints, nicht-breaking wo möglich

**Ergebnis:** Type-Safe, erweiterbar, wartbar, DRY-konform

---

**Nächste Schritte:**
1. Review dieses Design-Dokuments
2. Feedback einarbeiten
3. Phase 1 starten (Plot-Interface-Refactoring)

---

## 🎨 ERWEITERUNG: UiActionTarget-Pattern

**User-Feedback:** Objekte sollten ihre eigenen UI-Actions bereitstellen!

**Problem:** Aktuell müssen UIs ihre Buttons manuell erstellen → Code-Duplikation

**Lösung:** UiActionTarget-Interface → Objekte kennen ihre eigenen Operations

---

### UiActionTarget-Interface

**Location:** `core/src/main/java/de/fallenstar/core/ui/UiActionTarget.java`

```java
package de.fallenstar.core.ui;

import de.fallenstar.core.ui.element.UiAction;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Interface für Objekte die UI-Actions bereitstellen können.
 *
 * Ermöglicht es Objekten (Plot, NPC, Item, etc.) ihre eigenen
 * UI-Actions zu definieren. UIs können diese dann abrufen und
 * automatisch Buttons generieren.
 *
 * **Konzept:**
 * - Objekt kennt seine eigenen Operations
 * - UI fragt Objekt: "Was kann ich mit dir machen?"
 * - UI generiert Buttons automatisch
 *
 * **Beispiel:**
 * ```java
 * Plot plot = ...;
 * if (plot instanceof UiActionTarget) {
 *     List<UiActionInfo> actions = ((UiActionTarget) plot)
 *         .getAvailableUiActions(player, UiContext.MAIN_MENU);
 *
 *     // UI generiert Buttons automatisch!
 *     for (UiActionInfo action : actions) {
 *         addButton(action.createButton());
 *     }
 * }
 * ```
 *
 * @author FallenStar
 * @version 2.0
 */
public interface UiActionTarget {

    /**
     * Gibt verfügbare UI-Actions für einen Kontext zurück.
     *
     * @param player Der Spieler (für Permission-Checks)
     * @param context UI-Kontext (MAIN_MENU, SUB_MENU, etc.)
     * @return Liste von UiActionInfo
     */
    List<UiActionInfo> getAvailableUiActions(Player player, UiContext context);

    /**
     * Prüft ob Action verfügbar ist.
     *
     * @param player Der Spieler
     * @param actionId Action-ID
     * @return true wenn verfügbar
     */
    default boolean hasAction(Player player, String actionId) {
        return getAvailableUiActions(player, UiContext.ANY).stream()
                .anyMatch(action -> action.id().equals(actionId));
    }
}
```

---

### UiContext-Enum

**Location:** `core/src/main/java/de/fallenstar/core/ui/UiContext.java`

```java
package de.fallenstar.core.ui;

/**
 * Kontext für UI-Actions.
 *
 * Ermöglicht es, verschiedene Actions für verschiedene
 * UI-Bereiche bereitzustellen.
 *
 * **Lösung für SubMenu-Problem:**
 * - MAIN_MENU: Haupt-Menü Actions (Storage, NPCs, Preise)
 * - STORAGE_MENU: Storage-spezifische Actions (Input/Output konfigurieren)
 * - NPC_MENU: NPC-spezifische Actions (Spawn, Remove, Configure)
 * - PRICE_MENU: Preis-spezifische Actions (Set Price, List Prices)
 *
 * @author FallenStar
 * @version 2.0
 */
public enum UiContext {
    /**
     * Passt zu allen Kontexten (für Checks).
     */
    ANY,

    /**
     * Haupt-Menü eines Objekts.
     */
    MAIN_MENU,

    /**
     * Storage-Verwaltungs-Menü.
     */
    STORAGE_MENU,

    /**
     * NPC-Verwaltungs-Menü.
     */
    NPC_MENU,

    /**
     * Preis-Verwaltungs-Menü.
     */
    PRICE_MENU,

    /**
     * Slot-Verwaltungs-Menü.
     */
    SLOT_MENU,

    /**
     * Info/Statistik-Menü.
     */
    INFO_MENU,

    /**
     * Admin-/Eigentümer-Menü.
     */
    OWNER_MENU,

    /**
     * Gast-Menü (eingeschränkte Rechte).
     */
    GUEST_MENU;

    /**
     * Prüft ob dieser Kontext mit einem anderen matcht.
     *
     * ANY matcht mit allem.
     *
     * @param other Anderer Kontext
     * @return true wenn Match
     */
    public boolean matches(UiContext other) {
        return this == ANY || other == ANY || this == other;
    }
}
```

---

### UiActionInfo-Record

**Location:** `core/src/main/java/de/fallenstar/core/ui/UiActionInfo.java`

```java
package de.fallenstar.core.ui;

import de.fallenstar.core.ui.element.ClickableUiElement;
import de.fallenstar.core.ui.element.UiAction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * Informationen über eine UI-Action.
 *
 * Enthält alles was für die Button-Erstellung nötig ist:
 * - ID (eindeutig)
 * - Display-Name
 * - Beschreibung (Lore)
 * - Icon (Material)
 * - Action (auszuführende Operation)
 * - Kontext (wo die Action verfügbar ist)
 *
 * @author FallenStar
 * @version 2.0
 */
public record UiActionInfo(
        String id,
        String displayName,
        List<String> description,
        Material icon,
        UiAction action,
        UiContext context,
        int priority
) {

    /**
     * Erstellt einen Button für diese Action.
     *
     * @return ClickableUiElement
     */
    public ClickableUiElement<UiAction> createButton() {
        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();

        // Display-Name
        meta.displayName(
                Component.text(displayName)
                        .color(NamedTextColor.GOLD)
                        .decoration(TextDecoration.ITALIC, false)
        );

        // Lore
        List<Component> lore = description.stream()
                .map(line -> Component.text(line)
                        .color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false))
                .toList();
        meta.lore(lore);

        item.setItemMeta(meta);

        return new ClickableUiElement.CustomButton<>(item, action);
    }

    /**
     * Builder für UiActionInfo.
     */
    public static class Builder {
        private String id;
        private String displayName;
        private List<String> description = List.of();
        private Material icon = Material.PAPER;
        private UiAction action;
        private UiContext context = UiContext.MAIN_MENU;
        private int priority = 0;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder description(List<String> description) {
            this.description = description;
            return this;
        }

        public Builder description(String... lines) {
            this.description = List.of(lines);
            return this;
        }

        public Builder icon(Material icon) {
            this.icon = icon;
            return this;
        }

        public Builder action(UiAction action) {
            this.action = action;
            return this;
        }

        public Builder context(UiContext context) {
            this.context = context;
            return this;
        }

        public Builder priority(int priority) {
            this.priority = priority;
            return this;
        }

        public UiActionInfo build() {
            if (id == null || displayName == null || action == null) {
                throw new IllegalStateException("id, displayName und action sind required!");
            }
            return new UiActionInfo(id, displayName, description, icon, action, context, priority);
        }
    }

    /**
     * Erstellt einen neuen Builder.
     */
    public static Builder builder() {
        return new Builder();
    }
}
```

---

### TradeguildPlot mit UiActionTarget

**Location:** `module-plots/src/main/java/de/fallenstar/plot/model/TradeguildPlot.java`

```java
package de.fallenstar.plot.model;

import de.fallenstar.core.provider.*;
import de.fallenstar.core.ui.*;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Handelsgilde mit UiActionTarget-Implementation.
 *
 * @author FallenStar
 * @version 2.0
 */
public class TradeguildPlot implements NamedPlot, StorageContainerPlot,
                                        NpcContainerPlot, UiActionTarget {

    // ... bestehende Fields & Methoden ...

    // UiActionTarget Implementation
    @Override
    public List<UiActionInfo> getAvailableUiActions(Player player, UiContext context) {
        List<UiActionInfo> actions = new ArrayList<>();

        // Prüfe ob Owner
        boolean isOwner = isOwner(player);

        // MAIN_MENU Actions
        if (context.matches(UiContext.MAIN_MENU)) {
            // Storage-Action (für alle sichtbar)
            if (hasStorage()) {
                actions.add(UiActionInfo.builder()
                        .id("storage")
                        .displayName("§6Storage-Verwaltung")
                        .description(
                                "§7Verwalte Input/Output-Chests",
                                "§7",
                                "§eKlicke zum Öffnen"
                        )
                        .icon(Material.CHEST)
                        .action(new OpenStorageMenuAction(this))
                        .context(UiContext.MAIN_MENU)
                        .priority(10)
                        .build());
            }

            // NPC-Action (nur Owner)
            if (isOwner && hasNPCs()) {
                actions.add(UiActionInfo.builder()
                        .id("npcs")
                        .displayName("§6NPC-Verwaltung")
                        .description(
                                "§7Verwalte Gildenhändler",
                                "§7",
                                "§a" + getNpcContainer().getNPCCount() + " NPCs",
                                "§7",
                                "§eKlicke zum Öffnen"
                        )
                        .icon(Material.PLAYER_HEAD)
                        .action(new OpenNpcMenuAction(this))
                        .context(UiContext.MAIN_MENU)
                        .priority(20)
                        .build());
            }

            // Preis-Action (nur Owner)
            if (isOwner) {
                actions.add(UiActionInfo.builder()
                        .id("prices")
                        .displayName("§6Preis-Verwaltung")
                        .description(
                                "§7Setze Ankaufs-/Verkaufspreise",
                                "§7",
                                "§eKlicke zum Öffnen"
                        )
                        .icon(Material.EMERALD)
                        .action(new OpenPriceMenuAction(this))
                        .context(UiContext.MAIN_MENU)
                        .priority(30)
                        .build());
            }

            // Info-Action (für alle)
            actions.add(UiActionInfo.builder()
                    .id("info")
                    .displayName("§7Plot-Informationen")
                    .description(
                            "§7Zeige Statistiken",
                            "§7und Details"
                    )
                    .icon(Material.BOOK)
                    .action(new ViewPlotInfoAction(this))
                    .context(UiContext.MAIN_MENU)
                    .priority(100)
                    .build());
        }

        // STORAGE_MENU Actions (SubMenu!)
        if (context.matches(UiContext.STORAGE_MENU)) {
            if (isOwner) {
                actions.add(UiActionInfo.builder()
                        .id("configure_input")
                        .displayName("§aInput-Chests konfigurieren")
                        .description("§7Setze Input-Truhen")
                        .icon(Material.GREEN_STAINED_GLASS)
                        .action(new ConfigureInputChestsAction(this))
                        .context(UiContext.STORAGE_MENU)
                        .build());

                actions.add(UiActionInfo.builder()
                        .id("configure_output")
                        .displayName("§6Output-Chests konfigurieren")
                        .description("§7Setze Output-Truhen")
                        .icon(Material.ORANGE_STAINED_GLASS)
                        .action(new ConfigureOutputChestsAction(this))
                        .context(UiContext.STORAGE_MENU)
                        .build());
            }

            // View Storage (für alle)
            actions.add(UiActionInfo.builder()
                    .id("view_storage")
                    .displayName("§7Storage ansehen")
                    .description("§7Zeige Inventar")
                    .icon(Material.ENDER_CHEST)
                    .action(new ViewStorageAction(this))
                    .context(UiContext.STORAGE_MENU)
                    .build());
        }

        // NPC_MENU Actions (SubMenu!)
        if (context.matches(UiContext.NPC_MENU)) {
            if (isOwner) {
                actions.add(UiActionInfo.builder()
                        .id("spawn_npc")
                        .displayName("§aNPC spawnen")
                        .description("§7Erstelle neuen Händler")
                        .icon(Material.SPAWNER)
                        .action(new SpawnNpcAction(this))
                        .context(UiContext.NPC_MENU)
                        .build());

                actions.add(UiActionInfo.builder()
                        .id("manage_npcs")
                        .displayName("§6NPCs verwalten")
                        .description("§7Bearbeite bestehende NPCs")
                        .icon(Material.WRITABLE_BOOK)
                        .action(new ManageNpcsAction(this))
                        .context(UiContext.NPC_MENU)
                        .build());
            }
        }

        // PRICE_MENU Actions (SubMenu!)
        if (context.matches(UiContext.PRICE_MENU)) {
            if (isOwner) {
                actions.add(UiActionInfo.builder()
                        .id("set_price")
                        .displayName("§aPreis festlegen")
                        .description(
                                "§7Nimm ein Item in die Hand",
                                "§7und setze den Preis"
                        )
                        .icon(Material.GOLD_INGOT)
                        .action(new SetPriceAction(this))
                        .context(UiContext.PRICE_MENU)
                        .build());

                actions.add(UiActionInfo.builder()
                        .id("list_prices")
                        .displayName("§6Alle Preise anzeigen")
                        .description("§7Zeige Preisliste")
                        .icon(Material.WRITABLE_BOOK)
                        .action(new ListPricesAction(this))
                        .context(UiContext.PRICE_MENU)
                        .build());
            }
        }

        // Sortiere nach Priority
        actions.sort((a, b) -> Integer.compare(a.priority(), b.priority()));

        return actions;
    }

    // Helper
    private boolean isOwner(Player player) {
        // Implementierung...
        return false; // Placeholder
    }
}
```

---

### Self-Constructing UI

**Vorteil:** UI baut sich automatisch basierend auf verfügbaren Actions!

**Location:** `module-plots/src/main/java/de/fallenstar/plot/ui/PlotMainMenuUi.java`

```java
package de.fallenstar.plot.ui;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.ui.UiActionTarget;
import de.fallenstar.core.ui.UiContext;
import de.fallenstar.core.ui.container.GenericUiSmallChest;
import de.fallenstar.core.ui.element.navigation.CloseButton;
import de.fallenstar.core.ui.row.BasicUiRowForContent;
import org.bukkit.entity.Player;

/**
 * Self-Constructing Plot Main Menu.
 *
 * Baut sich automatisch basierend auf verfügbaren UiActions!
 *
 * @author FallenStar
 * @version 2.0
 */
public class PlotMainMenuUi extends GenericUiSmallChest {

    private final Plot plot;
    private final Player viewer;

    public PlotMainMenuUi(Plot plot, Player viewer) {
        super("§6§l" + getPlotTitle(plot));
        this.plot = plot;
        this.viewer = viewer;

        buildUi();
    }

    private void buildUi() {
        // Row 0: Navigation
        var navigationRow = new BasicUiRowForContent();
        navigationRow.setElement(0, CloseButton.create(this));
        setRow(0, navigationRow);

        // Row 1: Actions (automatisch generiert!)
        var actionsRow = new BasicUiRowForContent();

        if (plot instanceof UiActionTarget) {
            var actionTarget = (UiActionTarget) plot;

            // Hole Actions für MAIN_MENU Kontext
            var actions = actionTarget.getAvailableUiActions(viewer, UiContext.MAIN_MENU);

            // Generiere Buttons automatisch!
            int slot = 1;
            for (var actionInfo : actions) {
                actionsRow.setElement(slot++, actionInfo.createButton());
            }
        }

        setRow(1, actionsRow);
    }

    private static String getPlotTitle(Plot plot) {
        if (plot instanceof NamedPlot) {
            return ((NamedPlot) plot).getDisplayName();
        }
        return "Plot #" + plot.getIdentifier();
    }
}
```

**Ergebnis:** UI passt sich automatisch an:
- Owner sieht: Storage, NPCs, Preise, Info
- Gast sieht: Storage (readonly), Info
- Keine manuellen Permission-Checks im UI-Code!

---

### SubMenu mit Context

**Location:** `module-plots/src/main/java/de/fallenstar/plot/ui/PlotStorageUi.java`

```java
package de.fallenstar.plot.ui;

import de.fallenstar.core.provider.StorageContainerPlot;
import de.fallenstar.core.ui.*;
import de.fallenstar.core.ui.container.GenericUiLargeChest;
import de.fallenstar.core.ui.element.navigation.BackButton;
import de.fallenstar.core.ui.row.BasicUiRowForContent;
import org.bukkit.entity.Player;

/**
 * Self-Constructing Storage SubMenu.
 *
 * Nutzt STORAGE_MENU Context!
 *
 * @author FallenStar
 * @version 2.0
 */
public class PlotStorageUi extends GenericUiLargeChest
                            implements ChildUi<PlotMainMenuUi> {

    private final StorageContainerPlot plot;
    private final PlotMainMenuUi parent;
    private final Player viewer;

    public PlotStorageUi(
            StorageContainerPlot plot,
            PlotMainMenuUi parent,
            Player viewer
    ) {
        super("§6§lStorage-Verwaltung");
        this.plot = plot;
        this.parent = parent;
        this.viewer = viewer;

        buildUi();
    }

    private void buildUi() {
        // Row 0: Navigation
        var navigationRow = new BasicUiRowForContent();
        navigationRow.setElement(0, BackButton.create(this));
        setRow(0, navigationRow);

        // Row 1: Storage-spezifische Actions (automatisch!)
        var actionsRow = new BasicUiRowForContent();

        if (plot instanceof UiActionTarget) {
            var actionTarget = (UiActionTarget) plot;

            // Hole Actions für STORAGE_MENU Kontext!
            var actions = actionTarget.getAvailableUiActions(viewer, UiContext.STORAGE_MENU);

            int slot = 1;
            for (var actionInfo : actions) {
                actionsRow.setElement(slot++, actionInfo.createButton());
            }
        }

        setRow(1, actionsRow);

        // Row 2-5: Storage-Inhalt anzeigen
        // ...
    }

    // ChildUi Interface
    @Override
    public PlotMainMenuUi getParent() {
        return parent;
    }

    @Override
    public Class<PlotMainMenuUi> getParentUiClass() {
        return PlotMainMenuUi.class;
    }
}
```

**SubMenu-Problem gelöst!**
- MAIN_MENU Context → Haupt-Actions (Storage, NPCs, Preise)
- STORAGE_MENU Context → Storage-spezifische Actions (Configure Input/Output)
- Unterschiedliche Actions je nach Context!

---

## 🌍 Projekt-weite Anwendung

**Konzept auf ALLE Systeme übertragen:**

### Economy-System mit UiActionTarget

```java
package de.fallenstar.economy.model;

import de.fallenstar.core.ui.*;

/**
 * Currency mit UiActionTarget.
 *
 * @author FallenStar
 * @version 2.0
 */
public class Currency implements UiActionTarget {

    private final String id;
    private final String displayName;
    private final CurrencyTierSet tiers;

    @Override
    public List<UiActionInfo> getAvailableUiActions(Player player, UiContext context) {
        List<UiActionInfo> actions = new ArrayList<>();

        if (context.matches(UiContext.MAIN_MENU)) {
            // Withdraw Action
            actions.add(UiActionInfo.builder()
                    .id("withdraw")
                    .displayName("§aGeld abheben")
                    .description(
                            "§7Hebe Münzen von deinem Konto ab",
                            "§7",
                            "§eKlicke zum Abheben"
                    )
                    .icon(Material.GOLD_INGOT)
                    .action(new WithdrawCurrencyAction(this))
                    .context(UiContext.MAIN_MENU)
                    .build());

            // Deposit Action
            actions.add(UiActionInfo.builder()
                    .id("deposit")
                    .displayName("§6Geld einzahlen")
                    .description("§7Zahle Münzen auf dein Konto ein")
                    .icon(Material.CHEST)
                    .action(new DepositCurrencyAction(this))
                    .context(UiContext.MAIN_MENU)
                    .build());

            // Exchange Action
            actions.add(UiActionInfo.builder()
                    .id("exchange")
                    .displayName("§eWährung tauschen")
                    .description("§7Tausche in andere Währungen")
                    .icon(Material.EMERALD)
                    .action(new ExchangeCurrencyAction(this))
                    .context(UiContext.MAIN_MENU)
                    .build());
        }

        return actions;
    }
}
```

---

### Item-System mit UiActionTarget

```java
package de.fallenstar.item.model;

import de.fallenstar.core.ui.*;

/**
 * CustomItem mit UiActionTarget.
 *
 * @author FallenStar
 * @version 2.0
 */
public class CustomItem implements UiActionTarget {

    private final String itemId;
    private final String displayName;
    private final ItemStack itemStack;

    @Override
    public List<UiActionInfo> getAvailableUiActions(Player player, UiContext context) {
        List<UiActionInfo> actions = new ArrayList<>();

        if (context.matches(UiContext.MAIN_MENU)) {
            // Give Action (Admin only)
            if (player.hasPermission("fallenstar.admin.items")) {
                actions.add(UiActionInfo.builder()
                        .id("give")
                        .displayName("§aItem erhalten")
                        .description("§7Gibt dir dieses Item")
                        .icon(Material.DIAMOND)
                        .action(new GiveItemAction(this))
                        .build());
            }

            // View Stats Action
            actions.add(UiActionInfo.builder()
                    .id("stats")
                    .displayName("§7Item-Statistiken")
                    .description("§7Zeige Details und Stats")
                    .icon(Material.BOOK)
                    .action(new ViewItemStatsAction(this))
                    .build());

            // Craft Action (wenn craftbar)
            if (isCraftable()) {
                actions.add(UiActionInfo.builder()
                        .id("craft")
                        .displayName("§6Craften")
                        .description("§7Zeige Crafting-Rezept")
                        .icon(Material.CRAFTING_TABLE)
                        .action(new ViewCraftingRecipeAction(this))
                        .build());
            }
        }

        return actions;
    }

    private boolean isCraftable() {
        // Check ob Item Crafting-Rezept hat
        return false;
    }
}
```

---

### NPC-System mit UiActionTarget

```java
package de.fallenstar.npc.model;

import de.fallenstar.core.ui.*;

/**
 * NPC mit UiActionTarget.
 *
 * @author FallenStar
 * @version 2.0
 */
public class GuildTraderNPC implements NPCType, TradingEntity, UiActionTarget {

    private final UUID npcId;
    private final Plot owningPlot;

    @Override
    public List<UiActionInfo> getAvailableUiActions(Player player, UiContext context) {
        List<UiActionInfo> actions = new ArrayList<>();

        boolean isOwner = isOwner(player);

        if (context.matches(UiContext.MAIN_MENU)) {
            // Trade Action (für alle)
            actions.add(UiActionInfo.builder()
                    .id("trade")
                    .displayName("§6Handeln")
                    .description(
                            "§7Öffne Handelsmenü",
                            "§7",
                            "§eKlicke zum Handeln"
                    )
                    .icon(Material.EMERALD)
                    .action(new OpenTradeUiAction(this))
                    .priority(10)
                    .build());

            // Configure Action (nur Owner)
            if (isOwner) {
                actions.add(UiActionInfo.builder()
                        .id("configure")
                        .displayName("§aNPC konfigurieren")
                        .description("§7Verwalte Händler-Einstellungen")
                        .icon(Material.WRITABLE_BOOK)
                        .action(new ConfigureNpcAction(this))
                        .priority(20)
                        .build());

                actions.add(UiActionInfo.builder()
                        .id("remove")
                        .displayName("§cNPC entfernen")
                        .description("§7Entferne diesen Händler")
                        .icon(Material.BARRIER)
                        .action(new RemoveNpcAction(this))
                        .priority(30)
                        .build());
            }

            // Info Action (für alle)
            actions.add(UiActionInfo.builder()
                    .id("info")
                    .displayName("§7Händler-Info")
                    .description("§7Zeige Statistiken")
                    .icon(Material.BOOK)
                    .action(new ViewNpcInfoAction(this))
                    .priority(100)
                    .build());
        }

        return actions;
    }

    private boolean isOwner(Player player) {
        // Check ob Player Owner des owningPlot ist
        return false;
    }
}
```

---

## 🎯 Konsistentes Pattern: Überall gleich!

**Jedes System nutzt UiActionTarget:**

```java
// Plot
Plot plot = ...;
if (plot instanceof UiActionTarget) {
    var actions = ((UiActionTarget) plot).getAvailableUiActions(player, UiContext.MAIN_MENU);
}

// Currency
Currency currency = ...;
if (currency instanceof UiActionTarget) {
    var actions = ((UiActionTarget) currency).getAvailableUiActions(player, UiContext.MAIN_MENU);
}

// Item
CustomItem item = ...;
if (item instanceof UiActionTarget) {
    var actions = ((UiActionTarget) item).getAvailableUiActions(player, UiContext.MAIN_MENU);
}

// NPC
GuildTraderNPC npc = ...;
if (npc instanceof UiActionTarget) {
    var actions = ((UiActionTarget) npc).getAvailableUiActions(player, UiContext.MAIN_MENU);
}

// → ÜBERALL GLEICH!
```

---

## 📚 Vorteile UiActionTarget-Pattern

### ✅ DRY (Don't Repeat Yourself)

```java
// ❌ VORHER: Code-Duplikation
public class PlotMainMenuUi {
    private void buildUi() {
        // Manuell Storage-Button erstellen
        var storageButton = new ClickableUiElement<>(...);

        // Manuell NPC-Button erstellen
        var npcButton = new ClickableUiElement<>(...);

        // Manuell Permission-Checks
        if (isOwner) {
            // ...
        }
    }
}

// ✅ NACHHER: Automatisch
public class PlotMainMenuUi {
    private void buildUi() {
        // Plot kennt seine Actions!
        var actions = plot.getAvailableUiActions(player, UiContext.MAIN_MENU);

        // Automatisch Buttons generieren
        for (var action : actions) {
            addButton(action.createButton());
        }
    }
}
```

### ✅ Self-Documenting Code

```java
// Objekt dokumentiert sich selbst!
public class TradeguildPlot implements UiActionTarget {
    @Override
    public List<UiActionInfo> getAvailableUiActions(...) {
        // Hier sieht man ALLE möglichen Operations!
        // - Storage-Verwaltung
        // - NPC-Verwaltung
        // - Preis-Verwaltung
        // - Info anzeigen
    }
}
```

### ✅ Einfach erweiterbar

```java
// Neue Action hinzufügen? Nur an EINER Stelle!
public class TradeguildPlot implements UiActionTarget {
    @Override
    public List<UiActionInfo> getAvailableUiActions(...) {
        actions.add(UiActionInfo.builder()
                .id("new_feature")
                .displayName("§aNeues Feature")
                .action(new NewFeatureAction(this))
                .build());
        // → Erscheint automatisch in ALLEN UIs!
    }
}
```

### ✅ Konsistent projekt-weit

```java
// Plot, Currency, Item, NPC - ALLE nutzen UiActionTarget!
// → Einheitliche API
// → Einheitliches Verhalten
// → Leicht zu lernen
```

---

## 🎮 ERWEITERUNG 2: Interaction-System

**User-Feedback:** "Ein Plot ist ein UiActionTarget? Wäre cool wenn man den NPC anklickt und das UI öffnet sich!"

**Problem:** Wie werden UIs geöffnet? Wie werden Clicks geroutet?

**Lösung:** Interaction-System mit Interactable, UiTarget, InteractionHandler

---

### Das fehlende Puzzle-Stück

**Bisher hatten wir:**
- ✅ Plot hat UiActionTarget (kennt seine Actions)
- ✅ UI kann sich selbst bauen (Self-Constructing)
- ❌ **Aber:** Wie wird das UI **geöffnet**?

**Jetzt fehlt:**
1. **Click-Detection:** Wie wissen wir, dass Plot/NPC angeklickt wurde?
2. **Click-Routing:** Woher weiß System welches Objekt angeklickt wurde?
3. **UI-Opening:** Wer öffnet das UI?
4. **Permission-Check:** Wer darf interagieren?

---

### 1. Interactable-Interface (Basis!)

**Location:** `core/src/main/java/de/fallenstar/core/interaction/Interactable.java`

```java
package de.fallenstar.core.interaction;

import org.bukkit.entity.Player;

/**
 * Interface für interaktive Objekte.
 *
 * Ermöglicht es Objekten (Plot, NPC, Block, Item) auf
 * Spieler-Interaktionen zu reagieren.
 *
 * **Konzept:**
 * - Objekt definiert wie es interagiert wird
 * - Objekt entscheidet ob Interaktion erlaubt ist
 * - Objekt führt Interaktion aus
 *
 * @author FallenStar
 * @version 2.0
 */
public interface Interactable {

    /**
     * Wird aufgerufen wenn Spieler mit Objekt interagiert.
     *
     * @param player Der Spieler
     * @param context Interaktions-Kontext
     * @return true wenn Interaktion verarbeitet wurde (cancelt Event)
     */
    boolean onInteract(Player player, InteractionContext context);

    /**
     * Prüft ob Spieler mit diesem Objekt interagieren darf.
     *
     * @param player Der Spieler
     * @return true wenn Interaktion erlaubt
     */
    default boolean canInteract(Player player) {
        return true; // Default: alle dürfen
    }

    /**
     * Gibt den Interaktions-Typ zurück.
     *
     * @return InteractionType
     */
    InteractionType getInteractionType();
}
```

---

### 2. InteractionContext (Kontext-Informationen)

**Location:** `core/src/main/java/de/fallenstar/core/interaction/InteractionContext.java`

```java
package de.fallenstar.core.interaction;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Kontext einer Interaktion.
 *
 * Enthält alle relevanten Informationen über die Interaktion:
 * - Typ (Block, Entity, Item)
 * - Modifikatoren (Shift, Links/Rechts-Klick)
 * - Angeklicktes Objekt
 *
 * @author FallenStar
 * @version 2.0
 */
public record InteractionContext(
        InteractionType type,
        boolean isSneaking,
        boolean isLeftClick,
        Block clickedBlock,
        Entity clickedEntity,
        ItemStack itemInHand
) {

    /**
     * Erstellt Context aus PlayerInteractEvent.
     */
    public static InteractionContext fromEvent(PlayerInteractEvent event, InteractionType type) {
        return new InteractionContext(
                type,
                event.getPlayer().isSneaking(),
                event.getAction().isLeftClick(),
                event.getClickedBlock(),
                null,
                event.getItem()
        );
    }

    /**
     * Erstellt Context aus Entity-Click.
     */
    public static InteractionContext fromEntityClick(Entity entity, boolean isSneaking) {
        return new InteractionContext(
                InteractionType.ENTITY,
                isSneaking,
                false,
                null,
                entity,
                null
        );
    }

    /**
     * @return true wenn Shift+Rechtsklick (Admin-Mode)
     */
    public boolean isAdminInteraction() {
        return isSneaking && !isLeftClick;
    }
}
```

---

### 3. InteractionType-Enum

**Location:** `core/src/main/java/de/fallenstar/core/interaction/InteractionType.java`

```java
package de.fallenstar.core.interaction;

/**
 * Typ der Interaktion.
 *
 * @author FallenStar
 * @version 2.0
 */
public enum InteractionType {
    /**
     * Block angeklickt.
     */
    BLOCK,

    /**
     * Entity angeklickt (z.B. NPC).
     */
    ENTITY,

    /**
     * Item in Inventory angeklickt.
     */
    ITEM,

    /**
     * Plot-Area betreten/interagiert.
     */
    PLOT,

    /**
     * Welt-Interaktion (Air-Click, etc.).
     */
    WORLD
}
```

---

### 4. UiTarget-Interface (UI-Öffnung!)

**Location:** `core/src/main/java/de/fallenstar/core/ui/UiTarget.java`

```java
package de.fallenstar.core.ui;

import de.fallenstar.core.interaction.Interactable;
import de.fallenstar.core.interaction.InteractionContext;
import org.bukkit.entity.Player;

import java.util.Optional;

/**
 * Interface für Objekte die ein UI öffnen können.
 *
 * **Kombiniert drei Konzepte:**
 * 1. Interactable - Wie wird interagiert? (Rechtsklick, etc.)
 * 2. UiActionTarget - Welche Actions sind verfügbar?
 * 3. UiTarget - Welches UI wird geöffnet?
 *
 * **Workflow:**
 * ```
 * Player rechtsklickt Plot
 *   → InteractionHandler ruft onInteract() auf
 *   → onInteract() ruft openUi() auf
 *   → openUi() ruft createUi() auf
 *   → UI wird geöffnet
 * ```
 *
 * @author FallenStar
 * @version 2.0
 */
public interface UiTarget extends Interactable, UiActionTarget {

    /**
     * Erstellt das Haupt-UI für dieses Objekt.
     *
     * @param player Der Spieler
     * @param context Interaktions-Kontext
     * @return Optional mit UI, oder empty wenn kein UI verfügbar
     */
    Optional<BaseUi> createUi(Player player, InteractionContext context);

    /**
     * Öffnet das UI für einen Spieler.
     *
     * @param player Der Spieler
     * @param context Interaktions-Kontext
     * @return true wenn UI geöffnet wurde
     */
    default boolean openUi(Player player, InteractionContext context) {
        Optional<BaseUi> ui = createUi(player, context);

        if (ui.isEmpty()) {
            return false;
        }

        ui.get().open(player);
        return true;
    }

    /**
     * Default-Implementation von Interactable.onInteract().
     *
     * Öffnet automatisch das UI bei Interaktion:
     * - Shift+Rechtsklick → Admin-UI (falls verfügbar)
     * - Normal-Rechtsklick → Standard-UI
     */
    @Override
    default boolean onInteract(Player player, InteractionContext context) {
        // Shift+Rechtsklick → Admin-UI (wenn Admin-Permission)
        if (context.isAdminInteraction() && player.hasPermission("fallenstar.admin")) {
            return openAdminUi(player, context);
        }

        // Normal-Rechtsklick → Standard-UI
        return openUi(player, context);
    }

    /**
     * Öffnet Admin-UI (falls verfügbar).
     *
     * Override in konkreten Implementierungen für erweiterte Admin-Funktionen.
     *
     * @param player Der Spieler (muss Admin sein)
     * @param context Interaktions-Kontext
     * @return true wenn Admin-UI geöffnet wurde
     */
    default boolean openAdminUi(Player player, InteractionContext context) {
        // Default: kein Admin-UI → öffne normales UI
        return openUi(player, context);
    }
}
```

---

### 5. InteractionHandler (Click-Routing!)

**Location:** `core/src/main/java/de/fallenstar/core/interaction/InteractionHandler.java`

```java
package de.fallenstar.core.interaction;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Optional;
import java.util.logging.Logger;

/**
 * Handler für Spieler-Interaktionen.
 *
 * Routet Clicks zu den richtigen Interactable-Objekten:
 * - Block/Air-Clicks → Plots
 * - Entity-Clicks → NPCs
 * - Item-Clicks → Custom Items
 *
 * @author FallenStar
 * @version 2.0
 */
public class InteractionHandler implements Listener {

    private final InteractionRegistry registry;
    private final Logger logger;

    public InteractionHandler(InteractionRegistry registry, Logger logger) {
        this.registry = registry;
        this.logger = logger;
    }

    /**
     * Handhabt Block-/Air-Interaktionen (Plot-Clicks).
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // Prüfe ob Spieler auf Plot steht
        Location location = player.getLocation();
        Optional<Interactable> plotOpt = registry.getInteractableAtLocation(location);

        if (plotOpt.isEmpty()) {
            return; // Kein interaktives Objekt
        }

        Interactable interactable = plotOpt.get();

        // Permission-Check
        if (!interactable.canInteract(player)) {
            player.sendMessage("§cDu darfst nicht mit diesem Objekt interagieren!");
            return;
        }

        // Erstelle Context
        InteractionContext context = InteractionContext.fromEvent(
                event,
                interactable.getInteractionType()
        );

        // Interaktion ausführen
        boolean handled = interactable.onInteract(player, context);

        if (handled) {
            event.setCancelled(true); // Verhindere normale Interaktion
            logger.fine("Handled interaction with " + interactable.getClass().getSimpleName());
        }
    }

    /**
     * Handhabt Entity-Interaktionen (NPC-Clicks).
     */
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();

        // Hole Interactable für Entity (z.B. NPC)
        Optional<Interactable> npcOpt = registry.getInteractableForEntity(entity.getUniqueId());

        if (npcOpt.isEmpty()) {
            return; // Keine interaktive Entity
        }

        Interactable interactable = npcOpt.get();

        // Permission-Check
        if (!interactable.canInteract(player)) {
            player.sendMessage("§cDu darfst nicht mit diesem NPC interagieren!");
            event.setCancelled(true);
            return;
        }

        // Erstelle Context
        InteractionContext context = InteractionContext.fromEntityClick(
                entity,
                player.isSneaking()
        );

        // Interaktion ausführen
        boolean handled = interactable.onInteract(player, context);

        if (handled) {
            event.setCancelled(true);
            logger.fine("Handled entity interaction with " + interactable.getClass().getSimpleName());
        }
    }
}
```

---

### 6. InteractionRegistry (Objekt-Verwaltung!)

**Location:** `core/src/main/java/de/fallenstar/core/interaction/InteractionRegistry.java`

```java
package de.fallenstar.core.interaction;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.provider.PlotProvider;
import org.bukkit.Location;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Registry für Interactable-Objekte.
 *
 * Verwaltet Zuordnung:
 * - Location → Plot
 * - Entity-UUID → NPC
 * - Item-ID → Custom Item
 *
 * @author FallenStar
 * @version 2.0
 */
public class InteractionRegistry {

    private final PlotProvider plotProvider;
    private final Logger logger;

    // Entity-UUID → Interactable (für NPCs)
    private final Map<UUID, Interactable> entityInteractables;

    // Item-ID → Interactable (für Custom Items)
    private final Map<String, Interactable> itemInteractables;

    public InteractionRegistry(PlotProvider plotProvider, Logger logger) {
        this.plotProvider = plotProvider;
        this.logger = logger;
        this.entityInteractables = new ConcurrentHashMap<>();
        this.itemInteractables = new ConcurrentHashMap<>();
    }

    /**
     * Gibt Interactable an Location zurück (z.B. Plot).
     *
     * @param location Location
     * @return Optional mit Interactable
     */
    public Optional<Interactable> getInteractableAtLocation(Location location) {
        if (plotProvider == null || !plotProvider.isAvailable()) {
            return Optional.empty();
        }

        try {
            Plot plot = plotProvider.getPlot(location);

            if (plot instanceof Interactable) {
                return Optional.of((Interactable) plot);
            }

        } catch (Exception e) {
            // Kein Plot an Location
            logger.fine("No plot at location " + location);
        }

        return Optional.empty();
    }

    /**
     * Gibt Interactable für Entity zurück (z.B. NPC).
     *
     * @param entityId Entity-UUID
     * @return Optional mit Interactable
     */
    public Optional<Interactable> getInteractableForEntity(UUID entityId) {
        return Optional.ofNullable(entityInteractables.get(entityId));
    }

    /**
     * Registriert einen NPC.
     *
     * @param entityId Entity-UUID (Citizens-NPC-UUID)
     * @param npc NPC (muss Interactable implementieren)
     */
    public void registerNPC(UUID entityId, Interactable npc) {
        entityInteractables.put(entityId, npc);
        logger.info("Registered NPC " + entityId + " (" + npc.getClass().getSimpleName() + ")");
    }

    /**
     * Unregistriert einen NPC.
     *
     * @param entityId Entity-UUID
     */
    public void unregisterNPC(UUID entityId) {
        Interactable removed = entityInteractables.remove(entityId);
        if (removed != null) {
            logger.info("Unregistered NPC " + entityId);
        }
    }

    /**
     * Gibt Interactable für Item zurück.
     *
     * @param itemId Item-ID
     * @return Optional mit Interactable
     */
    public Optional<Interactable> getInteractableForItem(String itemId) {
        return Optional.ofNullable(itemInteractables.get(itemId));
    }

    /**
     * Registriert ein Custom Item.
     *
     * @param itemId Item-ID
     * @param item Item (muss Interactable implementieren)
     */
    public void registerItem(String itemId, Interactable item) {
        itemInteractables.put(itemId, item);
        logger.info("Registered item " + itemId);
    }

    /**
     * @return Anzahl registrierter NPCs
     */
    public int getNPCCount() {
        return entityInteractables.size();
    }

    /**
     * @return Anzahl registrierter Items
     */
    public int getItemCount() {
        return itemInteractables.size();
    }
}
```

---

### 7. TradeguildPlot mit UiTarget

**Location:** `module-plots/src/main/java/de/fallenstar/plot/model/TradeguildPlot.java`

```java
package de.fallenstar.plot.model;

import de.fallenstar.core.provider.*;
import de.fallenstar.core.ui.*;
import de.fallenstar.core.interaction.*;
import de.fallenstar.plot.ui.PlotMainMenuUi;
import de.fallenstar.plot.ui.PlotAdminUi;
import org.bukkit.entity.Player;

import java.util.Optional;

/**
 * TradeguildPlot mit vollständiger UI-Integration.
 *
 * Features:
 * - NamedPlot (Custom-Namen)
 * - StorageContainerPlot (Storage-System)
 * - NpcContainerPlot (NPC-Verwaltung)
 * - UiTarget (UI-Interaktion) ← NEU!
 *
 * **Interaktion:**
 * - Rechtsklick auf Plot → Öffnet PlotMainMenuUi
 * - Shift+Rechtsklick → Öffnet PlotAdminUi (nur Admin)
 *
 * @author FallenStar
 * @version 2.0
 */
public class TradeguildPlot implements
        NamedPlot,
        StorageContainerPlot,
        NpcContainerPlot,
        UiTarget {  // ← UiTarget statt nur UiActionTarget!

    // ... bestehende Fields ...

    // UiTarget Implementation
    @Override
    public Optional<BaseUi> createUi(Player player, InteractionContext context) {
        // Erstelle PlotMainMenuUi
        PlotMainMenuUi ui = new PlotMainMenuUi(this, player);
        return Optional.of(ui);
    }

    @Override
    public InteractionType getInteractionType() {
        return InteractionType.PLOT;
    }

    @Override
    public boolean canInteract(Player player) {
        // Jeder kann Plot-UI öffnen
        // Permissions werden in UiActions geprüft
        return true;
    }

    @Override
    public boolean openAdminUi(Player player, InteractionContext context) {
        // Admin-Permission-Check
        if (!player.hasPermission("fallenstar.admin.plots")) {
            player.sendMessage("§cKeine Admin-Berechtigung!");
            return false;
        }

        // Öffne erweiterte Admin-UI
        PlotAdminUi adminUi = new PlotAdminUi(this, player);
        adminUi.open(player);

        player.sendMessage("§a§lAdmin-UI geöffnet!");
        return true;
    }

    // UiActionTarget Implementation (wie vorher)
    @Override
    public List<UiActionInfo> getAvailableUiActions(Player player, UiContext context) {
        // ... wie vorher implementiert ...
    }
}
```

---

### 8. GuildTraderNPC mit UiTarget

**Location:** `module-npcs/src/main/java/de/fallenstar/npc/npctype/GuildTraderNPC.java`

```java
package de.fallenstar.npc.npctype;

import de.fallenstar.core.ui.*;
import de.fallenstar.core.interaction.*;
import de.fallenstar.npc.ui.*;

/**
 * GuildTraderNPC mit UI-Integration.
 *
 * **Interaktion:**
 * - Rechtsklick → Trade-UI (Gäste) oder Management-UI (Owner)
 * - Shift+Rechtsklick → Erweiterte Optionen
 *
 * @author FallenStar
 * @version 2.0
 */
public class GuildTraderNPC implements
        NPCType,
        TradingEntity,
        UiTarget {  // ← UiTarget!

    private final UUID npcId;
    private final Plot owningPlot;

    // UiTarget Implementation
    @Override
    public Optional<BaseUi> createUi(Player player, InteractionContext context) {
        boolean isOwner = isOwner(player);

        if (isOwner) {
            // Owner → Management-UI (Configure, Remove, etc.)
            return Optional.of(new NpcManagementUi(this, player));
        } else {
            // Gast → Trade-UI (Handeln)
            return Optional.of(new NpcTradeUi(this, player));
        }
    }

    @Override
    public InteractionType getInteractionType() {
        return InteractionType.ENTITY;
    }

    @Override
    public boolean canInteract(Player player) {
        // Jeder kann mit NPC interagieren
        return true;
    }

    // UiActionTarget Implementation (wie vorher)
    @Override
    public List<UiActionInfo> getAvailableUiActions(Player player, UiContext context) {
        // ... wie vorher implementiert ...
    }

    private boolean isOwner(Player player) {
        // Check ob Player Owner des owningPlot ist
        // TODO: Implementierung
        return false;
    }
}
```

---

### 9. Permission-Integration in UiActionInfo

**Erweiterte UiActionInfo:**

```java
public record UiActionInfo(
        String id,
        String displayName,
        List<String> description,
        Material icon,
        UiAction action,
        UiContext context,
        int priority,
        String requiredPermission  // ← NEU!
) {

    /**
     * Prüft ob Spieler diese Action ausführen darf.
     *
     * @param player Der Spieler
     * @return true wenn erlaubt
     */
    public boolean canExecute(Player player) {
        if (requiredPermission == null) {
            return true; // Keine Permission erforderlich
        }

        return player.hasPermission(requiredPermission);
    }

    /**
     * Builder (erweitert).
     */
    public static class Builder {
        private String requiredPermission = null;

        public Builder requiredPermission(String permission) {
            this.requiredPermission = permission;
            return this;
        }

        // ... rest bleibt gleich ...
    }
}
```

**Self-Constructing UI mit Permission-Filter:**

```java
public class PlotMainMenuUi extends GenericUiSmallChest {

    private void buildUi() {
        if (plot instanceof UiActionTarget) {
            var actionTarget = (UiActionTarget) plot;

            // Hole Actions
            var actions = actionTarget.getAvailableUiActions(viewer, UiContext.MAIN_MENU);

            int slot = 1;
            for (var actionInfo : actions) {
                // Permission-Check!
                if (!actionInfo.canExecute(viewer)) {
                    continue; // Überspringe Action
                }

                actionsRow.setElement(slot++, actionInfo.createButton());
            }
        }
    }
}
```

---

### 10. Validated Actions

**Location:** `core/src/main/java/de/fallenstar/core/ui/element/ValidatedUiAction.java`

```java
package de.fallenstar.core.ui.element;

import org.bukkit.entity.Player;

/**
 * UiAction mit Validation.
 *
 * Validiert vor Ausführung ob Action erlaubt ist:
 * - Permissions
 * - State-Checks (Plot existiert noch?)
 * - Business-Logic (genug Geld? etc.)
 *
 * @author FallenStar
 * @version 2.0
 */
public interface ValidatedUiAction extends UiAction {

    /**
     * Validiert ob Action ausgeführt werden darf.
     *
     * @param player Der Spieler
     * @return Validierungs-Ergebnis
     */
    ValidationResult validate(Player player);

    /**
     * Führt Action aus (nur wenn Validation erfolgreich).
     */
    @Override
    default void execute(Player player) {
        ValidationResult result = validate(player);

        if (!result.isValid()) {
            player.sendMessage("§c" + result.errorMessage());
            return;
        }

        // Validation erfolgreich → Action ausführen
        executeValidated(player);
    }

    /**
     * Führt validierte Action aus.
     *
     * Wird nur aufgerufen wenn validate() erfolgreich war.
     *
     * @param player Der Spieler
     */
    void executeValidated(Player player);

    /**
     * Validierungs-Ergebnis.
     */
    record ValidationResult(boolean isValid, String errorMessage) {
        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult failure(String message) {
            return new ValidationResult(false, message);
        }
    }
}
```

**Beispiel-Verwendung:**

```java
public class OpenPriceMenuAction implements ValidatedUiAction {
    private final TradeguildPlot plot;

    @Override
    public ValidationResult validate(Player player) {
        // Check 1: Plot existiert noch?
        if (plot == null) {
            return ValidationResult.failure("Plot nicht gefunden!");
        }

        // Check 2: Spieler ist Owner?
        if (!plot.isOwner(player)) {
            return ValidationResult.failure("Nur der Besitzer kann Preise verwalten!");
        }

        // Check 3: Economy-System verfügbar?
        if (!isEconomyAvailable()) {
            return ValidationResult.failure("Economy-System nicht verfügbar!");
        }

        return ValidationResult.success();
    }

    @Override
    public void executeValidated(Player player) {
        // Alle Checks bestanden → Öffne Price-Menu
        PlotPriceManagementUi ui = new PlotPriceManagementUi(plot, player);
        ui.open(player);
    }

    @Override
    public String getActionName() {
        return "OpenPriceMenu";
    }

    private boolean isEconomyAvailable() {
        // TODO: Check
        return true;
    }
}
```

---

## 🎯 Vollständiger Workflow

### Beispiel 1: User klickt auf Handelsgilde

```
1. Player rechtsklickt in Handelsgilde-Area
   ↓
2. PlayerInteractEvent gefeuert
   ↓
3. InteractionHandler.onPlayerInteract() fängt Event ab
   ↓
4. InteractionRegistry.getInteractableAtLocation(location)
   ├─ PlotProvider.getPlot(location) → TradeguildPlot
   └─ TradeguildPlot instanceof Interactable → true
   ↓
5. TradeguildPlot.canInteract(player) → true
   ↓
6. InteractionContext erstellt (PLOT, Sneaking=false)
   ↓
7. TradeguildPlot.onInteract(player, context)
   ├─ Nicht Sneaking → UiTarget.openUi() (Default-Implementation)
   └─ TradeguildPlot.createUi(player, context)
       └─ new PlotMainMenuUi(this, player)
   ↓
8. PlotMainMenuUi.buildUi()
   ├─ plot.getAvailableUiActions(player, MAIN_MENU)
   │   ├─ Storage-Action (für alle)
   │   ├─ NPC-Action (nur Owner)
   │   ├─ Preis-Action (nur Owner)
   │   └─ Info-Action (für alle)
   ├─ Permission-Filter (canExecute() für jede Action)
   └─ Automatische Button-Generierung
   ↓
9. UI.open(player)
   ↓
10. Player sieht UI mit personalisierten Buttons!
```

**Ergebnis:**
- **Owner sieht:** Storage, NPCs, Preise, Info
- **Gast sieht:** Storage, Info

---

### Beispiel 2: User klickt auf GuildTraderNPC

```
1. Player rechtsklickt auf Citizens-NPC
   ↓
2. PlayerInteractEntityEvent gefeuert
   ↓
3. InteractionHandler.onPlayerInteractEntity() fängt Event ab
   ↓
4. InteractionRegistry.getInteractableForEntity(npcUUID)
   └─ GuildTraderNPC gefunden
   ↓
5. GuildTraderNPC.canInteract(player) → true
   ↓
6. InteractionContext erstellt (ENTITY, Sneaking=false)
   ↓
7. GuildTraderNPC.onInteract(player, context)
   ├─ UiTarget.openUi() (Default-Implementation)
   └─ GuildTraderNPC.createUi(player, context)
       ├─ isOwner(player) → false
       └─ new NpcTradeUi(this, player)  // Gast-UI!
   ↓
8. NpcTradeUi öffnet sich
   ├─ Generiert TradeSets aus Plot-Storage + Preisen
   └─ Zeigt Vanilla Merchant Interface
   ↓
9. Player kann handeln!
```

**Ergebnis:**
- **Owner:** Sieht NpcManagementUi (Configure, Remove)
- **Gast:** Sieht NpcTradeUi (Handeln)

---

### Beispiel 3: Admin Shift+Rechtsklickt Plot

```
1. Player Shift+Rechtsklick auf Plot
   ↓
2. InteractionContext (PLOT, Sneaking=true)
   ↓
3. TradeguildPlot.onInteract(player, context)
   ├─ context.isAdminInteraction() → true
   ├─ player.hasPermission("fallenstar.admin") → true
   └─ TradeguildPlot.openAdminUi(player, context)
   ↓
4. PlotAdminUi öffnet sich
   ├─ Erweiterte Funktionen
   ├─ Debug-Informationen
   └─ Admin-Tools
   ↓
5. Admin sieht erweiterte Optionen!
```

---

## 📚 Vorteile Interaction-System

### ✅ Zentrales Click-Routing

```java
// EINE Handler-Klasse für ALLE Interaktionen!
public class InteractionHandler {
    // Block-Clicks → Plots
    // Entity-Clicks → NPCs
    // Item-Clicks → Custom Items
}
```

### ✅ Type-Safe Interactions

```java
// Compiler-Check: Objekt MUSS Interactable implementieren!
public class TradeguildPlot implements UiTarget {
    // MUSS onInteract(), createUi(), etc. implementieren
}
```

### ✅ Automatische UI-Öffnung

```java
// Default-Implementation in UiTarget!
default boolean onInteract(...) {
    return openUi(player, context);
}
// → Jedes UiTarget öffnet automatisch sein UI!
```

### ✅ Context-Aware

```java
// Verschiedene UIs je nach Kontext
if (context.isSneaking()) {
    openAdminUi();  // Admin-Modus
} else {
    openUi();       // Normal-Modus
}
```

### ✅ Permission-Integration

```java
// Automatisches Permission-Filtering
actions.stream()
    .filter(action -> action.canExecute(player))
    .forEach(action -> addButton(action));
```

---

## 🚀 Implementierungs-Phasen (aktualisiert)

### Phase 1: Plot-Interface-Refactoring (Sprint 15)
- ✅ Plot-Interface-Hierarchie
- ✅ Trait-Interfaces (NamedPlot, StorageContainerPlot, NpcContainerPlot, SlottablePlot)
- ✅ Konkrete Implementierungen (TradeguildPlot, MarketPlot)
- ✅ PlotProvider-Anpassungen (getPlotAs, hasTrait)

### Phase 2: UI-Navigation-System (Sprint 15-16)
- ✅ ChildUI-Interface
- ✅ BackButton
- ✅ UI-Hierarchie
- ✅ PlotMainMenuUi (Root)
- ✅ SubMenus (PlotStorageUi, PlotNpcManagementUi, etc.)

### Phase 3: UiActionTarget-Pattern (Sprint 16)
- ✅ UiActionTarget-Interface
- ✅ UiContext-Enum (MAIN_MENU, STORAGE_MENU, etc.)
- ✅ UiActionInfo-Record (mit Builder)
- ✅ Plot-Implementierung
- ✅ Self-Constructing UIs

### Phase 4: Interaction-System (Sprint 16) **NEU!**
- ✅ Interactable-Interface (Basis)
- ✅ InteractionContext (Kontext-Informationen)
- ✅ InteractionType-Enum
- ✅ UiTarget-Interface (kombiniert Interactable + UiActionTarget)
- ✅ InteractionHandler (Click-Routing)
- ✅ InteractionRegistry (Objekt-Verwaltung)
- ✅ Permission-Integration in UiActionInfo
- ✅ ValidatedUiAction-Interface

### Phase 5: Plot + NPC mit UiTarget (Sprint 16-17)
- ✅ TradeguildPlot implementiert UiTarget
- ✅ GuildTraderNPC implementiert UiTarget
- ✅ Admin-UI-Funktionalität (Shift+Rechtsklick)
- ✅ Automatische UI-Öffnung bei Rechtsklick

### Phase 6: NPC-Container-Abstraktion (Sprint 17)
- ✅ NpcContainer-Interface
- ✅ PlotNpcContainer
- ✅ PlayerNpcOwnership
- ✅ Registry-Integration

### Phase 7: Projekt-weite Anwendung (Sprint 17-18)
- ✅ Economy mit UiActionTarget
- ✅ Items mit UiActionTarget
- ✅ NPCs mit UiActionTarget
- ✅ Storage mit UiActionTarget
- ✅ Alle Systeme nutzen Interaction-System

### Phase 8: Event-System-Integration (Sprint 18)
- ✅ PlotEvent-Hierarchie
- ✅ UiEvent-Hierarchie
- ✅ InteractionEvent (InteractionStartEvent, InteractionSuccessEvent)
- ✅ Action-Events (ActionExecutedEvent, ActionFailedEvent)

---

## 🎯 Zusammenfassung (aktualisiert)

**Kernverbesserungen:**
1. **Plot als Interface** - Trait-Komposition statt Vererbung
2. **ChildUI-Pattern** - Navigation mit Back-Buttons
3. **UiActionTarget-Pattern** - Self-Constructing UIs **[NEU!]**
4. **NpcContainer-Abstraktion** - Wiederverwendbar für Plot & Player
5. **Type-Safe Events** - Compiler-geprüfte Event-Handler
6. **Konsistenz projekt-weit** - Alle Systeme nutzen gleiche Patterns **[NEU!]**

**Migration:** Phasenweise über 4-5 Sprints, nicht-breaking wo möglich

**Ergebnis:**
- Type-Safe, erweiterbar, wartbar, DRY-konform
- **Self-Documenting** (Objekte kennen ihre Operations) **[NEU!]**
- **Konsistent** (Plot, Economy, Items, NPCs - alle gleich) **[NEU!]**

---

**Fragen/Anmerkungen:** Bitte als GitHub Issues oder in CLAUDE.md eintragen

---

**Autoren:** FallenStar Team + Claude AI
**Datum:** 2025-11-18
**Version:** 2.1 (Design-Phase - Extended mit UiActionTarget)
