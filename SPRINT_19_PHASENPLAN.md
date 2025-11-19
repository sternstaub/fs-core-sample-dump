# Sprint 19 - Phasenplan
**Vollständige UI-Migration + SOLID-Refactoring**

**Datum:** 2025-11-19
**Branch:** claude/fix-storage-price-loop-012sXDfqzLyyPSPX8QC8egq7

---

## Übersicht

Sprint 19 wird in **4 Hauptphasen** unterteilt, die sukzessive abgearbeitet werden:

1. **Phase 1:** PlotActions Migration (Foundation)
2. **Phase 2:** TradeguildPlot Integration (Core UI-System)
3. **Phase 3:** SOLID-Refactoring Items + Economy
4. **Phase 4:** SOLID-Refactoring NPCs + Cleanup

**Nach jeder Phase:**
- ✅ Unit Tests schreiben/erweitern
- ✅ Commit + Push
- ✅ Kurze Zusammenfassung

---

## Phase 1: PlotActions Migration (Foundation)
**Ziel:** Alle fehlenden PlotActions implementieren

### Tasks:

#### 1.1 PlotActionManageStorage
- [ ] Klasse erstellen: `/module-plots/action/PlotActionManageStorage.java`
- [ ] Icon: Material.CHEST
- [ ] DisplayName: "§6Lager verwalten"
- [ ] Lore: Anzahl Items, Kapazität
- [ ] executeAction(): Öffnet PlotStorageUi
- [ ] requiresOwnership(): true

#### 1.2 PlotActionManagePrices
- [ ] Klasse erstellen: `/module-plots/action/PlotActionManagePrices.java`
- [ ] Icon: Material.GOLD_INGOT
- [ ] DisplayName: "§ePreise verwalten"
- [ ] Lore: "Setze Ankauf/Verkauf-Preise"
- [ ] executeAction(): Öffnet StoragePriceUi
- [ ] requiresOwnership(): true

#### 1.3 PlotActionViewPrices
- [ ] Klasse erstellen: `/module-plots/action/PlotActionViewPrices.java`
- [ ] Icon: Material.EMERALD
- [ ] DisplayName: "§aPreisliste"
- [ ] Lore: "Zeige aktuelle Preise"
- [ ] executeAction(): Öffnet Read-Only Preisliste
- [ ] requiresOwnership(): false (Gäste dürfen Preise sehen!)

#### 1.4 PlotActionTeleport
- [ ] Klasse erstellen: `/module-plots/action/PlotActionTeleport.java`
- [ ] Icon: Material.ENDER_PEARL
- [ ] DisplayName: "§dTeleportieren"
- [ ] Lore: "Teleport zum Plot-Spawn"
- [ ] executeAction(): player.teleport(plot.getSpawnLocation())
- [ ] requiresOwnership(): false

#### 1.5 PlotActionInfo
- [ ] Klasse erstellen: `/module-plots/action/PlotActionInfo.java`
- [ ] Icon: Material.BOOK
- [ ] DisplayName: "§bPlot-Informationen"
- [ ] Lore: Owner, Größe, Typ
- [ ] executeAction(): Öffnet PlotInfoUi
- [ ] requiresOwnership(): false

### Unit Tests Phase 1:

**Test-Klasse:** `/module-plots/test/.../PlotActionTest.java`

```java
@Test
void testManageStorageAction_ownerCanExecute() {
    // Given: Owner + PlotActionManageStorage
    // When: canExecute(owner)
    // Then: true
}

@Test
void testManageStorageAction_guestCannotExecute() {
    // Given: Guest + PlotActionManageStorage
    // When: canExecute(guest)
    // Then: false
}

@Test
void testViewPricesAction_guestCanExecute() {
    // Given: Guest + PlotActionViewPrices
    // When: canExecute(guest)
    // Then: true (Gäste dürfen Preise sehen!)
}

@Test
void testAllActions_haveValidDisplayItem() {
    // Given: Alle PlotActions
    // When: getDisplayItem(player)
    // Then: ItemStack != null, DisplayName != null
}

@Test
void testAllActions_permissionLoreAdded() {
    // Given: PlotAction mit !canExecute()
    // When: getDisplayItem(player)
    // Then: Lore enthält "§c§l✗ Keine Berechtigung"
}
```

### Erwartetes Ergebnis Phase 1:
- ✅ 5 neue PlotActions vollständig implementiert
- ✅ Alle mit GuiRenderable-Logik
- ✅ Unit Tests (5 Tests, alle grün)
- ✅ Commit: "Feature: PlotActions Migration - Storage, Prices, Teleport, Info (Phase 1)"

---

## Phase 2: TradeguildPlot Integration (Core UI-System)
**Ziel:** GuiBuilder in TradeguildPlot integrieren, HandelsgildeUi ersetzen

### Tasks:

#### 2.1 TradeguildPlot.getAvailablePlotActions()
- [ ] Methode hinzufügen: `List<PlotAction> getAvailablePlotActions(Player player, ProviderRegistry providers)`
- [ ] Kombiniert alle Trait-Actions:
  ```java
  List<PlotAction> actions = new ArrayList<>();
  // NamedPlot
  actions.add(new PlotActionSetName(this, providers));
  // StorageContainerPlot
  actions.add(new PlotActionManageStorage(this, providers, plotModule));
  actions.add(new PlotActionManagePrices(this, providers, plotModule));
  actions.add(new PlotActionViewPrices(this, providers, plotModule));
  // NpcContainerPlot
  actions.add(new PlotActionManageNpcs(this, providers, plotModule));
  // Generic
  actions.add(new PlotActionTeleport(this, providers));
  actions.add(new PlotActionInfo(this, providers));
  return actions;
  ```
- [ ] Filterung erfolgt via canExecute() in GuiBuilder!

#### 2.2 TradeguildPlot.createUi() refactoren
- [ ] Ersetze GenericInteractionMenuUi durch GuiBuilder:
  ```java
  @Override
  public Optional<BaseUi> createUi(Player player, InteractionContext context) {
      List<PlotAction> actions = getAvailablePlotActions(player, context.getProviders());
      PageableBasicUi gui = GuiBuilder.buildFrom(player, "§6Handelsgilde - " + getDisplayName(), actions);
      return Optional.of(gui);
  }
  ```

#### 2.3 PlotCommand refactoren
- [ ] openHandelsgildeUI() nutzt bereits UiTarget-Pattern
- [ ] Verifizieren dass GuiBuilder automatisch verwendet wird
- [ ] Testen mit Owner + Guest

#### 2.4 HandelsgildeUi entfernen
- [ ] Deprecated-Annotation hinzufügen (falls noch nicht)
- [ ] Javadoc: Migration-Guide zu GuiBuilder
- [ ] Klasse NICHT löschen (für User-Referenz)
- [ ] README.md aktualisieren: HandelsgildeUi obsolet

### Unit Tests Phase 2:

**Test-Klasse:** `/module-plots/test/.../TradeguildPlotTest.java`

```java
@Test
void testGetAvailablePlotActions_ownerGetsAllActions() {
    // Given: Owner + TradeguildPlot
    // When: getAvailablePlotActions(owner, providers)
    // Then: 7 Actions (SetName, ManageStorage, ManagePrices, ViewPrices, ManageNpcs, Teleport, Info)
}

@Test
void testGetAvailablePlotActions_guestGetsFilteredActions() {
    // Given: Guest + TradeguildPlot
    // When: getAvailablePlotActions(guest, providers)
    // Then: Nur Actions mit canExecute(guest) == true
}

@Test
void testCreateUi_returnsGuiBuilderUi() {
    // Given: TradeguildPlot + Player
    // When: createUi(player, context)
    // Then: PageableBasicUi mit PlotActions
}

@Test
void testCreateUi_ownerSeesManageActions() {
    // Given: Owner
    // When: createUi(owner, context)
    // Then: GUI enthält ManageStorage, ManagePrices
}

@Test
void testCreateUi_guestSeesViewActions() {
    // Given: Guest
    // When: createUi(guest, context)
    // Then: GUI enthält ViewPrices, Info, Teleport
}
```

### Erwartetes Ergebnis Phase 2:
- ✅ TradeguildPlot verwendet GuiBuilder
- ✅ HandelsgildeUi deprecated (dokumentiert)
- ✅ Owner + Guest UIs funktionieren unterschiedlich
- ✅ Unit Tests (5 Tests, alle grün)
- ✅ Commit: "Refactor: TradeguildPlot mit GuiBuilder - HandelsgildeUi deprecated (Phase 2)"

---

## Phase 3: SOLID-Refactoring Items + Economy
**Ziel:** Münzen-System und Price-Management universell machen

### Tasks:

#### 3.1 CurrencyItem Interface (Items-Modul)
- [ ] Interface erstellen: `/core/provider/CurrencyItem.java`
  ```java
  public interface CurrencyItem {
      String getCurrencyName();
      Material getIcon();
      BigDecimal getValue();
      ItemStack createItem(int amount);
      boolean isCurrencyItem(ItemStack item);
  }
  ```

#### 3.2 CurrencyRegistry (Core)
- [ ] Klasse erstellen: `/core/registry/CurrencyRegistry.java`
- [ ] Map<String, CurrencyItem> currencies
- [ ] registerCurrency(String id, CurrencyItem item)
- [ ] Optional<CurrencyItem> getCurrency(String id)
- [ ] List<CurrencyItem> getAllCurrencies()

#### 3.3 VanillaCoinItem (Items-Modul)
- [ ] Klasse erstellen: implements CurrencyItem
- [ ] Wraps existing Coin logic
- [ ] getCurrencyName(): "Sterne"
- [ ] getIcon(): Material.NETHER_STAR
- [ ] getValue(): BigDecimal.ONE

#### 3.4 CoinProvider refactoren
- [ ] Nutzt CurrencyRegistry statt hart-kodiert
- [ ] provideCurrency(Player, String currencyId, int amount)
- [ ] Abwärtskompatibel mit altem API

#### 3.5 Priceable Interface (Core)
- [ ] Interface erstellen: `/core/provider/Priceable.java`
  ```java
  public interface Priceable {
      Optional<BigDecimal> getBuyPrice(Material material);
      Optional<BigDecimal> getSellPrice(Material material);
      void setBuyPrice(Material material, BigDecimal price);
      void setSellPrice(Material material, BigDecimal price);
  }
  ```

#### 3.6 Universal PriceManager (Plots-Modul)
- [ ] Klasse erstellen: `/module-plots/manager/PriceManager.java`
- [ ] Implementiert Priceable
- [ ] Map<UUID, Map<Material, PlotPriceData>> plotPrices
- [ ] Persistiert in Config/DataStore
- [ ] NICHT mehr nur für StorageContainerPlot!

#### 3.7 StorageContainerPlot refactoren
- [ ] Nutzt PriceManager statt eigene Preis-Logik
- [ ] getPriceManager() → PriceManager
- [ ] Abwärtskompatibel mit altem API

### Unit Tests Phase 3:

**Test-Klasse:** `/core/test/.../CurrencyRegistryTest.java`

```java
@Test
void testRegisterCurrency_canRetrieve() {
    // Given: CurrencyRegistry + VanillaCoinItem
    // When: registerCurrency("sterne", vanillaCoin)
    // Then: getCurrency("sterne") returns vanillaCoin
}

@Test
void testGetAllCurrencies_returnsAll() {
    // Given: 3 registered currencies
    // When: getAllCurrencies()
    // Then: List with 3 items
}
```

**Test-Klasse:** `/module-plots/test/.../PriceManagerTest.java`

```java
@Test
void testSetBuyPrice_canRetrieve() {
    // Given: PriceManager
    // When: setBuyPrice(DIAMOND, 100)
    // Then: getBuyPrice(DIAMOND) == 100
}

@Test
void testSetSellPrice_canRetrieve() {
    // Given: PriceManager
    // When: setSellPrice(DIAMOND, 80)
    // Then: getSellPrice(DIAMOND) == 80
}

@Test
void testPrices_persistAcrossReloads() {
    // Given: PriceManager mit Preisen
    // When: saveToConfig() + loadFromConfig()
    // Then: Preise bleiben erhalten
}
```

### Erwartetes Ergebnis Phase 3:
- ✅ CurrencyItem Interface + Registry (erweiterbar!)
- ✅ Priceable Interface + Universal PriceManager
- ✅ CoinProvider refactored (abwärtskompatibel)
- ✅ Unit Tests (5 Tests, alle grün)
- ✅ Commit: "Refactor: CurrencyItem + PriceManager universell (SOLID) (Phase 3)"

---

## Phase 4: SOLID-Refactoring NPCs + Cleanup
**Ziel:** NPC-UIs mit GuiBuilder, finale Cleanup

### Tasks:

#### 4.1 NpcAction Hierarchie (NPCs-Modul)
- [ ] Abstract class erstellen: `NpcAction extends PlotAction`
- [ ] protected final NPC npc (zusätzlich zu plot)
- [ ] Spezifische Actions:
  - NpcActionConfigure (Owner)
  - NpcActionTrade (Guest)
  - NpcActionRemove (Owner)

#### 4.2 NpcManagementUi refactoren
- [ ] Ersetze manuelle Konstruktion durch GuiBuilder
- [ ] getNpcActions(Player player) → List<NpcAction>
- [ ] GuiBuilder.buildFrom(player, title, npcActions)

#### 4.3 PlayerNpcManagementUi refactoren
- [ ] Analog zu NpcManagementUi
- [ ] Nutzt GuiBuilder statt manuelle Items

#### 4.4 TradeAction (Economy-Modul)
- [ ] Optional: TradeAction implements GuiRenderable
- [ ] Falls Zeit: TradeUI konsistent mit GuiBuilder
- [ ] Falls keine Zeit: Issue für Sprint 20

#### 4.5 Finale Cleanup
- [ ] README.md aktualisieren (alle Module)
- [ ] CLAUDE.md: Sprint 19 als ✅ markieren
- [ ] Deprecated-Klassen dokumentieren
- [ ] Migration-Guides vervollständigen

### Unit Tests Phase 4:

**Test-Klasse:** `/module-npcs/test/.../NpcActionTest.java`

```java
@Test
void testNpcActionConfigure_ownerCanExecute() {
    // Given: Owner + NpcActionConfigure
    // When: canExecute(owner)
    // Then: true
}

@Test
void testNpcActionTrade_guestCanExecute() {
    // Given: Guest + NpcActionTrade
    // When: canExecute(guest)
    // Then: true
}

@Test
void testNpcActions_haveValidDisplayItems() {
    // Given: Alle NpcActions
    // When: getDisplayItem(player)
    // Then: ItemStack != null
}
```

### Erwartetes Ergebnis Phase 4:
- ✅ NPC-UIs verwenden GuiBuilder
- ✅ Alle UIs konsistent (PlotActions, NpcActions)
- ✅ Dokumentation aktualisiert
- ✅ Unit Tests (3 Tests, alle grün)
- ✅ Commit: "Refactor: NPC-UIs mit GuiBuilder + Sprint 19 Cleanup (Phase 4)"

---

## Zusammenfassung

### Erwartete Commits:
1. `Phase 1: PlotActions Migration - Storage, Prices, Teleport, Info`
2. `Phase 2: TradeguildPlot mit GuiBuilder - HandelsgildeUi deprecated`
3. `Phase 3: CurrencyItem + PriceManager universell (SOLID)`
4. `Phase 4: NPC-UIs mit GuiBuilder + Sprint 19 Cleanup`
5. `Docs: Sprint 19 abgeschlossen - Alle UIs universal + SOLID`

### Erwartete Unit Tests:
- **Phase 1:** 5 Tests (PlotActions)
- **Phase 2:** 5 Tests (TradeguildPlot + GUI)
- **Phase 3:** 5 Tests (Currency + Prices)
- **Phase 4:** 3 Tests (NPC-Actions)
- **Gesamt:** ~18 neue Tests

### Erwartete Dateien:
**Neu:**
- PlotActionManageStorage.java
- PlotActionManagePrices.java
- PlotActionViewPrices.java
- PlotActionTeleport.java
- PlotActionInfo.java
- CurrencyItem.java (Interface)
- CurrencyRegistry.java
- VanillaCoinItem.java
- Priceable.java (Interface)
- PriceManager.java
- NpcAction.java (Abstract)
- NpcActionConfigure.java
- NpcActionTrade.java
- NpcActionRemove.java

**Refactored:**
- TradeguildPlot.java
- HandelsgildeUi.java (deprecated)
- CoinProvider.java
- StorageContainerPlot.java
- NpcManagementUi.java
- PlayerNpcManagementUi.java

**Dokumentation:**
- CLAUDE.md (Sprint 19 ✅)
- README.md (alle Module)
- Migration-Guides

---

## Status-Tracking

### Phase 1: ⏳ In Arbeit
- [ ] PlotActionManageStorage
- [ ] PlotActionManagePrices
- [ ] PlotActionViewPrices
- [ ] PlotActionTeleport
- [ ] PlotActionInfo
- [ ] Unit Tests Phase 1
- [ ] Commit Phase 1

### Phase 2: 📋 Geplant
- [ ] TradeguildPlot.getAvailablePlotActions()
- [ ] TradeguildPlot.createUi() refactoren
- [ ] HandelsgildeUi deprecated
- [ ] Unit Tests Phase 2
- [ ] Commit Phase 2

### Phase 3: 📋 Geplant
- [ ] CurrencyItem Interface
- [ ] CurrencyRegistry
- [ ] Priceable Interface
- [ ] PriceManager
- [ ] Unit Tests Phase 3
- [ ] Commit Phase 3

### Phase 4: 📋 Geplant
- [ ] NpcAction Hierarchie
- [ ] NPC-UIs refactoren
- [ ] Cleanup
- [ ] Unit Tests Phase 4
- [ ] Commit Phase 4

---

**Start:** 2025-11-19
**Geschätzte Dauer:** 4-6 Stunden (alle Phasen)
**AI arbeitet autonom während User andere Dinge erledigt**
