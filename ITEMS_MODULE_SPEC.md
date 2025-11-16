# Items-Modul Spezifikation

**FallenStar Paper Core - Items-Modul (Sprint 5-6)**

**Version:** 1.0
**Autor:** FallenStar Team
**Datum:** 2025-11-16

---

## Übersicht

Das Items-Modul ist ein Wrapper-Modul für das **MMOItems-Plugin** und stellt eine einheitliche Abstraktionsschicht für Custom-Items bereit. Es implementiert das `ItemProvider`-Interface aus dem Core-Plugin und ermöglicht:

- Verwendung von Custom Items in **Trading-UIs**
- Integration von Custom Items in **Economy-System**
- Custom Items in **NPC-Dialogen** und **Quests**
- Zentrale Verwaltung aller Item-Typen

---

## Architektur

### Provider-Pattern

```
Core Plugin (ItemProvider Interface)
    ↓
Items-Modul (MMOItemsItemProvider Implementation)
    ↓
MMOItems Plugin (externe API)
```

**Wichtig:**
- **Core** enthält nur das Interface
- **Items-Modul** enthält die MMOItems-Implementierung
- Andere Module nutzen **nur** das ItemProvider-Interface

---

## ItemProvider-Interface

### Kern-Funktionalität

#### 1. **Item-Erstellung**

```java
// Einfache Item-Erstellung (Amount = 1)
Optional<ItemStack> createItem(String itemId)

// Mit spezifischer Anzahl
Optional<ItemStack> createItem(String itemId, int amount)

// Mit Type und ID (MMOItems-spezifisch)
Optional<ItemStack> createItem(String type, String itemId, int amount)
```

**Beispiele:**
```java
ItemProvider items = providerRegistry.getItemProvider();

// SWORD mit ID "FLAMING_BLADE"
Optional<ItemStack> sword = items.createItem("SWORD", "FLAMING_BLADE", 1);

// CONSUMABLE mit ID "HEALTH_POTION", Stack von 16
Optional<ItemStack> potions = items.createItem("CONSUMABLE", "HEALTH_POTION", 16);

// Vereinfacht (ohne Type)
Optional<ItemStack> item = items.createItem("MYTHIC_SWORD", 1);
```

#### 2. **Item-Identifikation**

```java
// Item-ID eines ItemStacks abrufen
Optional<String> getItemId(ItemStack itemStack)

// Prüfen ob Item ein Custom-Item ist
boolean isCustomItem(ItemStack itemStack)

// Item-Type abrufen
Optional<String> getItemType(String itemId)
Optional<String> getItemType(ItemStack itemStack)
```

**Beispiele:**
```java
ItemStack playerItem = player.getInventory().getItemInMainHand();

if (items.isCustomItem(playerItem)) {
    Optional<String> id = items.getItemId(playerItem);
    Optional<String> type = items.getItemType(playerItem);

    player.sendMessage("Custom Item: " + id.orElse("Unknown"));
    player.sendMessage("Type: " + type.orElse("Unknown"));
}
```

#### 3. **Item-Discovery**

```java
// Alle verfügbaren Types (SWORD, BOW, ARMOR, etc.)
List<String> getAllTypes()

// Alle Items eines Types
List<String> getItemsByType(String type)

// Alle registrierten Item-IDs
List<String> getAllItemIds()

// Prüfen ob Item existiert
boolean itemExists(String itemId)
boolean itemExists(String type, String itemId)
```

**Beispiele:**
```java
// Alle Waffen-Typen auflisten
List<String> types = items.getAllTypes();
for (String type : types) {
    List<String> itemsOfType = items.getItemsByType(type);
    System.out.println(type + ": " + itemsOfType.size() + " items");
}

// Prüfen ob spezifisches Item existiert
if (items.itemExists("SWORD", "EXCALIBUR")) {
    // Item kann erstellt werden
}
```

#### 4. **Kategorisierung**

```java
// Kategorie eines Items (z.B. "WEAPONS", "ARMOR")
Optional<String> getItemCategory(String itemId)

// Alle verfügbaren Kategorien
List<String> getCategories()

// Items einer Kategorie
List<String> getItemsByCategory(String category)
```

**Hinweis:** Kategorien sind MMOItems-intern definiert und unterscheiden sich von Types.

#### 5. **Economy-Integration**

```java
// Preisempfehlung für ein Item (basierend auf Stats, Seltenheit)
Optional<Double> getSuggestedPrice(String itemId)
```

**Beispiel:**
```java
Optional<Double> price = items.getSuggestedPrice("LEGENDARY_SWORD");
if (price.isPresent()) {
    player.sendMessage("Empfohlener Preis: " + price.get() + " Münzen");
}
```

---

## MMOItems-API-Wrapper

### MMOItemsItemProvider Implementation

**Datei:** `module-items/src/main/java/de/fallenstar/items/provider/MMOItemsItemProvider.java`

```java
public class MMOItemsItemProvider implements ItemProvider {

    private final MMOItems mmoItemsPlugin;
    private final Logger logger;

    public MMOItemsItemProvider(Logger logger) {
        this.logger = logger;
        this.mmoItemsPlugin = (MMOItems) Bukkit.getPluginManager().getPlugin("MMOItems");
    }

    @Override
    public boolean isAvailable() {
        return mmoItemsPlugin != null && mmoItemsPlugin.isEnabled();
    }

    @Override
    public Optional<ItemStack> createItem(String type, String itemId, int amount) {
        try {
            // MMOItems API verwenden
            Type mmoType = Type.get(type);
            if (mmoType == null) {
                logger.warning("Unknown MMOItems type: " + type);
                return Optional.empty();
            }

            MMOItem mmoItem = MMOItems.plugin.getTemplates().getTemplate(mmoType, itemId);
            if (mmoItem == null) {
                logger.warning("Unknown MMOItems item: " + type + ":" + itemId);
                return Optional.empty();
            }

            ItemStack item = mmoItem.newBuilder().build().newBuilder().build();
            item.setAmount(amount);
            return Optional.of(item);

        } catch (Exception e) {
            logger.severe("Failed to create MMOItem " + type + ":" + itemId + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> getItemId(ItemStack itemStack) {
        try {
            NBTItem nbtItem = NBTItem.get(itemStack);
            return Optional.ofNullable(nbtItem.getString("MMOITEMS_ITEM_ID"));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean isCustomItem(ItemStack itemStack) {
        return NBTItem.get(itemStack).hasTag("MMOITEMS_ITEM_ID");
    }

    @Override
    public List<String> getAllTypes() {
        return Type.values().stream()
            .map(Type::getId)
            .collect(Collectors.toList());
    }

    @Override
    public List<String> getItemsByType(String type) {
        Type mmoType = Type.get(type);
        if (mmoType == null) {
            return Collections.emptyList();
        }

        return MMOItems.plugin.getTemplates().getTemplates(mmoType).stream()
            .map(MMOItem::getId)
            .collect(Collectors.toList());
    }

    // ... weitere Methoden
}
```

---

## Verwendung in anderen Modulen

### Beispiel: Trading-UI

```java
public class TradeUI extends BaseUI {

    private final ItemProvider itemProvider;
    private final List<TradeEntry> trades;

    public TradeUI(ItemProvider itemProvider) {
        super("Händler");
        this.itemProvider = itemProvider;
        this.trades = new ArrayList<>();
    }

    /**
     * Fügt einen Trade mit Custom Items hinzu.
     */
    public void addCustomItemTrade(String inputType, String inputId,
                                   String outputType, String outputId) {
        Optional<ItemStack> input = itemProvider.createItem(inputType, inputId, 1);
        Optional<ItemStack> output = itemProvider.createItem(outputType, outputId, 1);

        if (input.isPresent() && output.isPresent()) {
            trades.add(new TradeEntry(input.get(), null, output.get()));
        }
    }

    /**
     * Trade-Entry für vanilla Trading UI.
     */
    public record TradeEntry(
        ItemStack input1,
        ItemStack input2,  // nullable
        ItemStack output
    ) {}
}
```

### Beispiel: Economy-Modul

```java
public class ShopManager {

    private final ItemProvider itemProvider;
    private final EconomyProvider economyProvider;

    public void sellItemToPlayer(Player player, String itemType, String itemId) {
        // Hole Preisempfehlung
        Optional<Double> suggestedPrice = itemProvider.getSuggestedPrice(itemId);

        if (suggestedPrice.isEmpty()) {
            player.sendMessage("§cPreis konnte nicht ermittelt werden!");
            return;
        }

        double price = suggestedPrice.get();

        // Prüfe Guthaben
        if (economyProvider.getBalance(player.getUniqueId()) < price) {
            player.sendMessage("§cNicht genug Geld! Benötigt: " + price);
            return;
        }

        // Erstelle Item
        Optional<ItemStack> item = itemProvider.createItem(itemType, itemId, 1);

        if (item.isEmpty()) {
            player.sendMessage("§cItem konnte nicht erstellt werden!");
            return;
        }

        // Transaktion durchführen
        economyProvider.withdraw(player.getUniqueId(), price);
        player.getInventory().addItem(item.get());
        player.sendMessage("§aItem gekauft für " + price + " Münzen!");
    }
}
```

---

## Modul-Struktur

```
module-items/
├── pom.xml
├── README.md
└── src/main/
    ├── java/de/fallenstar/items/
    │   ├── ItemsModule.java                # Main Plugin Class
    │   ├── provider/
    │   │   └── MMOItemsItemProvider.java   # MMOItems-Wrapper
    │   ├── command/
    │   │   ├── ItemsCommand.java           # /fsitems Befehle
    │   │   └── ItemBrowserCommand.java     # Item-Browser-UI
    │   ├── manager/
    │   │   ├── ItemCacheManager.java       # Item-Cache für Performance
    │   │   └── ItemMetadataManager.java    # Metadata-Verwaltung
    │   └── ui/
    │       ├── ItemBrowserUI.java          # UI zum Durchstöbern aller Items
    │       └── ItemInfoUI.java             # Detailansicht für Items
    └── resources/
        ├── plugin.yml
        └── config.yml
```

---

## Dependencies (pom.xml)

```xml
<dependencies>
    <!-- Core Dependency (REQUIRED) -->
    <dependency>
        <groupId>de.fallenstar</groupId>
        <artifactId>core</artifactId>
        <version>${project.version}</version>
        <scope>provided</scope>
    </dependency>

    <!-- Paper API -->
    <dependency>
        <groupId>io.papermc.paper</groupId>
        <artifactId>paper-api</artifactId>
    </dependency>

    <!-- MMOItems API -->
    <dependency>
        <groupId>net.Indyuce</groupId>
        <artifactId>MMOItems-API</artifactId>
        <version>6.9.5</version>
        <scope>provided</scope>
    </dependency>
</dependencies>

<repositories>
    <!-- Phoenix Repository für MMOItems -->
    <repository>
        <id>phoenix</id>
        <url>https://nexus.phoenixdevt.fr/repository/maven-public/</url>
    </repository>
</repositories>
```

---

## plugin.yml

```yaml
name: FallenStar-Items
version: 1.0
main: de.fallenstar.items.ItemsModule
api-version: 1.21
author: FallenStar
description: Custom Items Module - MMOItems Integration

# WICHTIG: Core und MMOItems müssen geladen sein
depend:
  - FallenStar-Core
  - MMOItems

# Commands
commands:
  fsitems:
    description: Items Module commands
    usage: /fsitems <browse|info|reload>
    permission: fallenstar.items.admin
    aliases: [items, fsi]

# Permissions
permissions:
  fallenstar.items.admin:
    description: Access to items admin commands
    default: op
```

---

## Initialisierung im Modul

```java
public class ItemsModule extends JavaPlugin implements Listener {

    private ProviderRegistry providers;
    private MMOItemsItemProvider itemProvider;

    @Override
    public void onEnable() {
        getLogger().info("FallenStar Items Module loading...");
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onProvidersReady(ProvidersReadyEvent event) {
        this.providers = event.getRegistry();

        // Prüfe ob MMOItems verfügbar ist
        if (!checkMMOItemsAvailable()) {
            getLogger().severe("MMOItems plugin not found! Disabling module...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Erstelle MMOItems-Provider
        itemProvider = new MMOItemsItemProvider(getLogger());

        // Registriere Provider im Core
        // WICHTIG: Core nutzt automatisch unseren Provider
        getLogger().info("✓ MMOItemsItemProvider initialized");

        // Registriere Commands
        registerCommands();

        getLogger().info("✓ Items Module enabled!");
    }

    private boolean checkMMOItemsAvailable() {
        Plugin mmoItems = getServer().getPluginManager().getPlugin("MMOItems");
        return mmoItems != null && mmoItems.isEnabled();
    }

    private void registerCommands() {
        ItemsCommand cmd = new ItemsCommand(this);
        getCommand("fsitems").setExecutor(cmd);
        getCommand("fsitems").setTabCompleter(cmd);
    }

    public MMOItemsItemProvider getItemProvider() {
        return itemProvider;
    }
}
```

---

## Provider-Registrierung im Core

**Automatische Detection in ProviderRegistry:**

```java
// In ProviderRegistry.java (Core)
public void detectAndRegister() {
    // ... andere Provider ...

    // ItemProvider Detection
    if (isPluginEnabled("FallenStar-Items")) {
        // Items-Modul lädt automatisch MMOItemsItemProvider
        Plugin itemsModule = Bukkit.getPluginManager().getPlugin("FallenStar-Items");

        if (itemsModule != null) {
            try {
                Object provider = itemsModule.getClass().getMethod("getItemProvider").invoke(itemsModule);
                itemProvider = (ItemProvider) provider;
                logger.info("✓ Registered MMOItemsItemProvider from Items Module");
            } catch (Exception e) {
                logger.warning("Failed to load ItemProvider from module: " + e.getMessage());
                itemProvider = new NoOpItemProvider();
            }
        }
    } else {
        itemProvider = new NoOpItemProvider();
        logger.warning("✗ No Items module loaded, using NoOpItemProvider");
    }
}
```

---

## Verwendungs-Beispiele

### 1. **Test-Trade-UI mit Custom Items**

```java
public class TestTradeUI extends TradeUI {

    public TestTradeUI(ItemProvider itemProvider) {
        super(itemProvider);

        // Trade 1: CONSUMABLE "HEALTH_POTION" → MATERIAL "GLOWSTONE"
        addCustomItemTrade("CONSUMABLE", "HEALTH_POTION",
                          "MATERIAL", "GLOWSTONE");

        // Trade 2: SWORD "IRON_BLADE" + Vanilla Item → SWORD "STEEL_BLADE"
        Optional<ItemStack> input1 = itemProvider.createItem("SWORD", "IRON_BLADE", 1);
        ItemStack input2 = new ItemStack(Material.DIAMOND, 3);
        Optional<ItemStack> output = itemProvider.createItem("SWORD", "STEEL_BLADE", 1);

        if (input1.isPresent() && output.isPresent()) {
            addTrade(new TradeEntry(input1.get(), input2, output.get()));
        }
    }
}
```

### 2. **Item-Browser-UI**

```java
public class ItemBrowserUI extends LargeChestUI {

    private final ItemProvider itemProvider;
    private String currentType;

    public ItemBrowserUI(ItemProvider itemProvider) {
        super("§6Item Browser");
        this.itemProvider = itemProvider;
        loadAllTypes();
    }

    private void loadAllTypes() {
        List<String> types = itemProvider.getAllTypes();

        int slot = 0;
        for (String type : types) {
            // Zeige jeden Type als Button
            ItemStack typeIcon = createTypeIcon(type);

            setItem(slot++, typeIcon, player -> {
                openTypeView(player, type);
            });
        }
    }

    private void openTypeView(Player player, String type) {
        // Öffne neue UI mit allen Items dieses Types
        ItemTypeViewUI typeView = new ItemTypeViewUI(itemProvider, type);
        typeView.open(player);
    }
}
```

### 3. **Item-Vergleich in Crafting**

```java
public class CraftingManager {

    private final ItemProvider itemProvider;

    public boolean canCraft(Player player, String recipeId) {
        Recipe recipe = getRecipe(recipeId);

        for (ItemStack required : recipe.getInputs()) {
            if (itemProvider.isCustomItem(required)) {
                // Prüfe Custom-Item-Match
                Optional<String> requiredId = itemProvider.getItemId(required);

                if (requiredId.isEmpty() || !hasCustomItem(player, requiredId.get())) {
                    return false;
                }
            } else {
                // Vanilla-Item-Check
                if (!player.getInventory().contains(required.getType(), required.getAmount())) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean hasCustomItem(Player player, String itemId) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) continue;

            if (itemProvider.isCustomItem(item)) {
                Optional<String> id = itemProvider.getItemId(item);
                if (id.isPresent() && id.get().equals(itemId)) {
                    return true;
                }
            }
        }
        return false;
    }
}
```

---

## Spezial-Items für System-Funktionen

Das Items-Modul verwaltet nicht nur normale Custom-Items, sondern auch **Spezial-Items** für System-Funktionen:

### 1. **Münz-Items** (Economy-Integration)

Münzen werden als Custom-Items erstellt und im Economy-Modul verwendet:

**MMOItems-Typ:** `CURRENCY` oder `MATERIAL`

**Kategorien:**
- `CURRENCY_COIN` - Standard-Münzen
- `CURRENCY_SPECIAL` - Spezial-Währungen

**Beispiel-Items:**
```java
// Bronze-Münze (Typ: MATERIAL, ID: BRONZE_COIN)
items.createItem("MATERIAL", "BRONZE_COIN", 1)

// Silber-Münze
items.createItem("MATERIAL", "SILVER_COIN", 1)

// Gold-Münze
items.createItem("MATERIAL", "GOLD_COIN", 1)

// Edelstein (Spezial-Währung)
items.createItem("MATERIAL", "EMERALD_GEM", 1)
```

**Economy-Modul Integration:**
```java
// Economy-Modul holt Münz-Items vom ItemProvider
ItemStack coins = itemProvider.createItem("MATERIAL", "GOLD_COIN", amount).orElse(null);
player.getInventory().addItem(coins);
```

**Kategorisierung:**
```java
// Alle Münz-Items abrufen
List<String> currencyItems = itemProvider.getItemsByCategory("CURRENCY_COIN");
// → ["BRONZE_COIN", "SILVER_COIN", "GOLD_COIN"]
```

---

### 2. **UI-Button-Items** (UI-Modul Integration)

Items die als anklickbare Buttons in UIs verwendet werden:

**MMOItems-Typ:** `MISC` oder `ACCESSORY`

**Kategorien:**
- `UI_BUTTON` - Allgemeine UI-Buttons
- `UI_NAVIGATION` - Navigations-Buttons
- `UI_ACTION` - Aktions-Buttons

**Beispiel-Items:**
```java
// Weiter-Button (Pfeil nach rechts)
items.createItem("MISC", "UI_BUTTON_NEXT", 1)

// Zurück-Button (Pfeil nach links)
items.createItem("MISC", "UI_BUTTON_BACK", 1)

// Bestätigen-Button (grüner Haken)
items.createItem("MISC", "UI_BUTTON_CONFIRM", 1)

// Abbrechen-Button (rotes X)
items.createItem("MISC", "UI_BUTTON_CANCEL", 1)

// Info-Button (Buch)
items.createItem("MISC", "UI_BUTTON_INFO", 1)

// Botschafts-Icon (Fahne)
items.createItem("MISC", "UI_ICON_EMBASSY", 1)

// Händler-Icon (Smaragd)
items.createItem("MISC", "UI_ICON_TRADER", 1)
```

**UI-Modul Integration:**
```java
public class AmbassadorUI extends SmallChestUI {

    private final ItemProvider itemProvider;

    public AmbassadorUI(ItemProvider itemProvider) {
        super("§6Botschafter");
        this.itemProvider = itemProvider;

        // Verwende UI-Button-Item als Icon
        ItemStack embassyIcon = itemProvider.createItem("MISC", "UI_ICON_EMBASSY", 1)
            .orElse(new ItemStack(Material.WHITE_BANNER));

        setItem(13, embassyIcon, player -> {
            // Botschafts-Aktion
        });

        // Zurück-Button
        ItemStack backButton = itemProvider.createItem("MISC", "UI_BUTTON_BACK", 1)
            .orElse(new ItemStack(Material.ARROW));

        setItem(18, backButton, player -> close(player));
    }
}
```

---

### 3. **Spezial-Item-Verwaltung**

**Manager-Klasse für Spezial-Items:**

```java
public class SpecialItemManager {

    private final ItemProvider itemProvider;
    private final Map<String, String> currencyItemIds;
    private final Map<String, String> uiButtonItemIds;

    public SpecialItemManager(ItemProvider itemProvider) {
        this.itemProvider = itemProvider;
        this.currencyItemIds = new HashMap<>();
        this.uiButtonItemIds = new HashMap<>();

        // Initialisiere Münz-Items
        initializeCurrencyItems();

        // Initialisiere UI-Buttons
        initializeUIButtons();
    }

    private void initializeCurrencyItems() {
        currencyItemIds.put("bronze", "BRONZE_COIN");
        currencyItemIds.put("silver", "SILVER_COIN");
        currencyItemIds.put("gold", "GOLD_COIN");
    }

    private void initializeUIButtons() {
        uiButtonItemIds.put("next", "UI_BUTTON_NEXT");
        uiButtonItemIds.put("back", "UI_BUTTON_BACK");
        uiButtonItemIds.put("confirm", "UI_BUTTON_CONFIRM");
        uiButtonItemIds.put("cancel", "UI_BUTTON_CANCEL");
    }

    /**
     * Erstellt ein Münz-Item.
     */
    public Optional<ItemStack> createCurrency(String currencyType, int amount) {
        String itemId = currencyItemIds.get(currencyType);
        if (itemId == null) {
            return Optional.empty();
        }
        return itemProvider.createItem("MATERIAL", itemId, amount);
    }

    /**
     * Erstellt ein UI-Button-Item.
     */
    public Optional<ItemStack> createUIButton(String buttonType) {
        String itemId = uiButtonItemIds.get(buttonType);
        if (itemId == null) {
            return Optional.empty();
        }
        return itemProvider.createItem("MISC", itemId, 1);
    }

    /**
     * Prüft ob ein ItemStack ein Münz-Item ist.
     */
    public boolean isCurrencyItem(ItemStack itemStack) {
        if (!itemProvider.isCustomItem(itemStack)) {
            return false;
        }

        Optional<String> itemId = itemProvider.getItemId(itemStack);
        return itemId.isPresent() && currencyItemIds.containsValue(itemId.get());
    }

    /**
     * Gibt den Währungstyp eines Münz-Items zurück.
     */
    public Optional<String> getCurrencyType(ItemStack itemStack) {
        Optional<String> itemId = itemProvider.getItemId(itemStack);
        if (itemId.isEmpty()) {
            return Optional.empty();
        }

        return currencyItemIds.entrySet().stream()
            .filter(entry -> entry.getValue().equals(itemId.get()))
            .map(Map.Entry::getKey)
            .findFirst();
    }
}
```

---

### 4. **MMOItems-Kategorien für Spezial-Items**

**In MMOItems konfigurieren:**

```yaml
# MMOItems/item/MATERIAL.yml
BRONZE_COIN:
  material: GOLD_NUGGET
  display-name: '&6Bronze-Münze'
  lore:
  - '&7Grundwährung des Reiches'
  - '&7Wert: 1'
  custom-model-data: 1001
  item-particles:
    type: SIMPLE
    particle: GOLD
  disable-crafting: true
  disable-smelting: true
  tier: COMMON
  # WICHTIG: Kategorie für Spezial-Items
  tags:
  - CURRENCY_COIN
  - SYSTEM_ITEM

SILVER_COIN:
  material: IRON_INGOT
  display-name: '&fSilber-Münze'
  lore:
  - '&7Handelswährung'
  - '&7Wert: 10 Bronze'
  custom-model-data: 1002
  disable-crafting: true
  disable-smelting: true
  tier: UNCOMMON
  tags:
  - CURRENCY_COIN
  - SYSTEM_ITEM

# MMOItems/item/MISC.yml
UI_BUTTON_NEXT:
  material: ARROW
  display-name: '&aWeiter →'
  lore:
  - '&7Zur nächsten Seite'
  custom-model-data: 2001
  hide-enchants: true
  disable-interaction: true
  tags:
  - UI_BUTTON
  - UI_NAVIGATION
  - SYSTEM_ITEM
```

---

### 5. **Spezial-Item-Commands**

```java
// In ItemsCommand.java
public void handleCreateCurrency(CommandSender sender, String[] args) {
    // /fsitems currency <player> <type> <amount>
    if (args.length < 4) {
        sender.sendMessage("§cUsage: /fsitems currency <player> <type> <amount>");
        return;
    }

    Player target = Bukkit.getPlayer(args[1]);
    String currencyType = args[2];
    int amount = Integer.parseInt(args[3]);

    Optional<ItemStack> currency = specialItemManager.createCurrency(currencyType, amount);

    if (currency.isPresent()) {
        target.getInventory().addItem(currency.get());
        sender.sendMessage("§a" + amount + "x " + currencyType + " Münzen an " + target.getName() + " gegeben");
    } else {
        sender.sendMessage("§cUnbekannter Währungstyp: " + currencyType);
    }
}
```

---

## Features des Items-Moduls

### ✅ Implementierte Features (Sprint 5-6)

1. **MMOItems-API-Wrapper**
   - Vollständige Implementierung aller ItemProvider-Methoden
   - Error-Handling und Logging
   - Performance-Optimierung mit Caching

2. **Item-Browser-UI**
   - `/fsitems browse` - Durchstöbern aller Custom-Items
   - Filterung nach Type, Kategorie, Seltenheit
   - Item-Detailansicht mit Stats

3. **Admin-Commands**
   - `/fsitems give <player> <type> <id> [amount]` - Item geben
   - `/fsitems info` - Info über gehaltenes Item
   - `/fsitems reload` - Provider neu laden

4. **Test-Trading-UI**
   - Registrierung im UIRegistry für Tests
   - 2 Test-Trades (Cobblestone → Stone, etc.)
   - Demonstration von Custom-Item-Integration

5. **Provider-Integration**
   - Automatische Registrierung im Core
   - Graceful Degradation wenn MMOItems fehlt
   - Event-basierte Initialisierung

### 📋 Geplante Features (Spätere Sprints)

- **Item-Crafting-System** (Sprint 7-8)
- **Item-Upgrading** (Sprint 9-10)
- **Item-Tier-System-Integration** (Sprint 11-12)
- **Quest-Item-Integration** (Sprint 13-14)

---

## Zusammenfassung

Das **Items-Modul** ist ein essentieller Bestandteil des FallenStar-Systems:

- ✅ **Wrapper für MMOItems-API** - Einheitliche Abstraktion
- ✅ **Provider-Pattern** - Saubere Architektur
- ✅ **Trading-Integration** - Custom Items in Händler-UIs
- ✅ **Economy-Ready** - Preisempfehlungen, Kategorisierung
- ✅ **UI-System-Integration** - Item-Browser, Test-UIs
- ✅ **Erweiterbar** - Basis für weitere Features

**Nächster Schritt:** Implementierung in **Sprint 5-6** nach Abschluss der UI-Framework-Vorbereitung.

---

**Letzte Aktualisierung:** 2025-11-16
**Version:** 1.0
**Status:** Spezifikation abgeschlossen, bereit für Implementierung
