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

**Fragen/Anmerkungen:** Bitte als GitHub Issues oder in CLAUDE.md eintragen

---

**Autoren:** FallenStar Team + Claude AI
**Datum:** 2025-11-18
**Version:** 2.0 (Design-Phase)
