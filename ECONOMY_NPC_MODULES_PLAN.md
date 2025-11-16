# Economy- und NPC-Module - Kurzplanung

## Economy-Modul (Sprint 9-10)

### Abhängigkeiten
- **ItemProvider** (Items-Modul) - Für Münz-Items
- **UI-Modul** - Für Händler-UIs
- **EconomyProvider** (Vault-Integration)

### Kernfunktionen

#### 1. **Münz-basiertes Wirtschaftssystem**
```java
// Münz-Items vom ItemProvider
ItemStack goldCoins = itemProvider.createItem("MATERIAL", "GOLD_COIN", 10);

// Wirtschafts-Transaktionen
economyManager.payWithCoins(player, 100); // Zahlt mit Münz-Items
economyManager.giveCoins(player, 50);     // Gibt Münz-Items
```

#### 2. **Trade Sets** (Händler-Angebote)
```java
public class TradeSet {
    private String id;                    // "blacksmith_tier1"
    private String displayName;           // "Schmied - Grundausrüstung"
    private List<TradeEntry> trades;     // Liste von Angeboten

    public record TradeEntry(
        ItemStack input1,       // z.B. Gold-Münzen
        ItemStack input2,       // Optional: zweiter Input
        ItemStack output,       // z.B. Eisenschwert
        int maxUses            // Wie oft handelbar
    ) {}
}
```

#### 3. **Händler-UI-Integration**
```java
public class TraderUI extends LargeChestUI {

    public TraderUI(TradeSet tradeSet, ItemProvider itemProvider) {
        super("§6" + tradeSet.getDisplayName());

        int slot = 0;
        for (TradeEntry trade : tradeSet.getTrades()) {
            setItem(slot++, trade.output(), player -> {
                attemptTrade(player, trade);
            });
        }
    }

    private void attemptTrade(Player player, TradeEntry trade) {
        // Prüfe ob Spieler Inputs hat
        if (hasItems(player, trade.input1(), trade.input2())) {
            // Entferne Inputs
            removeItems(player, trade.input1(), trade.input2());
            // Gebe Output
            player.getInventory().addItem(trade.output());
        }
    }
}
```

#### 4. **Trade-Set-Konfiguration**
```yaml
# config/tradesets/blacksmith.yml
blacksmith_tier1:
  display-name: "Schmied - Grundausrüstung"
  trades:
    - input1:
        type: MATERIAL
        id: GOLD_COIN
        amount: 50
      output:
        type: SWORD
        id: IRON_BLADE
        amount: 1
      max-uses: 10

    - input1:
        type: MATERIAL
        id: GOLD_COIN
        amount: 30
      input2:
        material: DIAMOND
        amount: 1
      output:
        type: SWORD
        id: STEEL_BLADE
        amount: 1
      max-uses: 5
```

---

## NPC-Modul (Sprint 13-14)

### Abhängigkeiten
- **NPCProvider** (Citizens-Integration)
- **PlotProvider** (Grundstücks-Zugriff)
- **UI-Modul** - Für Botschafter-UI
- **ItemProvider** - Für UI-Icons

### Kernfunktionen

#### 1. **Botschafter-NPC**
```java
public class AmbassadorNPC {
    private NPC citizensNPC;              // Citizens-NPC
    private Plot embassyPlot;             // Botschafts-Grundstück
    private String townName;              // Stadt des Botschafters
    private Location teleportLocation;    // Ziel-Teleport-Punkt
}
```

#### 2. **Botschafter-UI (erweitert)**
```java
public class AmbassadorUI extends SmallChestUI {

    private final NPCProvider npcProvider;
    private final PlotProvider plotProvider;
    private final ItemProvider itemProvider;

    public AmbassadorUI(/* providers */) {
        super("§6Botschafter - Schnellreisen");
        loadAmbassadorNPCs();
    }

    private void loadAmbassadorNPCs() {
        // Hole alle Botschafter-NPCs
        List<AmbassadorNPC> ambassadors = npcProvider
            .getNPCsByTag("AMBASSADOR");

        int slot = 0;
        for (AmbassadorNPC ambassador : ambassadors) {
            // Icon für Botschaft
            ItemStack icon = itemProvider
                .createItem("MISC", "UI_ICON_EMBASSY", 1)
                .orElse(new ItemStack(Material.WHITE_BANNER));

            // Setze Lore mit Stadt-Info
            ItemMeta meta = icon.getItemMeta();
            meta.setDisplayName("§6" + ambassador.getTownName());
            meta.setLore(List.of(
                "§7Klicke um dich zur Botschaft",
                "§7von §e" + ambassador.getTownName() + " §7zu teleportieren"
            ));
            icon.setItemMeta(meta);

            setItem(slot++, icon, player -> {
                teleportToEmbassy(player, ambassador);
            });
        }
    }

    private void teleportToEmbassy(Player player, AmbassadorNPC ambassador) {
        // Teleportiere zum Botschafts-Grundstück
        player.teleport(ambassador.getTeleportLocation());
        player.sendMessage("§aTeleportiert zur Botschaft von " + ambassador.getTownName());
        close(player);
    }
}
```

#### 3. **NPC-Registry-Integration**
```java
public class AmbassadorRegistry {

    private final Map<String, AmbassadorNPC> ambassadors;
    private final NPCProvider npcProvider;
    private final PlotProvider plotProvider;

    public void registerAmbassador(String townName, NPC npc, Plot embassyPlot) {
        AmbassadorNPC ambassador = new AmbassadorNPC(npc, embassyPlot, townName);
        ambassadors.put(townName, ambassador);

        // Setze NPC-Tag für spätere Suche
        npcProvider.setNPCTag(npc, "AMBASSADOR");
        npcProvider.setNPCTag(npc, "TOWN_" + townName.toUpperCase());
    }

    public List<AmbassadorNPC> getAllAmbassadors() {
        return new ArrayList<>(ambassadors.values());
    }

    public Optional<AmbassadorNPC> getAmbassador(String townName) {
        return Optional.ofNullable(ambassadors.get(townName));
    }
}
```

#### 4. **Botschafter-Command**
```java
// /fsnpc ambassador <create|remove|teleport>
public class AmbassadorCommand {

    public void handleCreate(Player player, String townName) {
        // Erstelle NPC an Spieler-Position
        NPC npc = npcProvider.createNPC(player.getLocation(), "§6Botschafter von " + townName);

        // Hole Plot an Position
        Plot plot = plotProvider.getPlot(player.getLocation());

        // Registriere als Botschafter
        ambassadorRegistry.registerAmbassador(townName, npc, plot);

        player.sendMessage("§aBotschafter für " + townName + " erstellt!");
    }
}
```

---

## Sprint-Abhängigkeiten (Übersicht)

```
Sprint 5-6: Items-Modul
    ↓ (ItemProvider verfügbar)
Sprint 7-8: UI-Modul
    ↓ (UI + ItemProvider verfügbar)
Sprint 9-10: Economy-Modul
    ↓ (Economy + UI + ItemProvider verfügbar)
Sprint 11-12: WorldAnchors
    ↓
Sprint 13-14: NPCs-Modul (Botschafter nutzt alle vorherigen Module)
```

**Wichtig:** Jedes Modul baut auf den vorherigen auf!

---

**Letzte Aktualisierung:** 2025-11-16
