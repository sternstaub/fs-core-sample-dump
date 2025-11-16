# FallenStar Core - Restrukturierungsplan

**Datum:** 2025-11-16
**Status:** In Planung
**Umfang:** Vollständige Überarbeitung der Modulstruktur und Integration alter Core-Funktionalitäten

---

## Zusammenfassung

Dieser Plan beschreibt die vollständige Restrukturierung des FallenStar-Plugin-Systems mit folgenden Zielen:

1. **Storage-Modul entfernen** (redundant, Funktionalität in Plots verschoben)
2. **Alte Core-Funktionalitäten migrieren** (aus fallenstar-old-core)
3. **Neue Module hinzufügen** (UI, Chat, Auth, WebHooks)
4. **UI-Provider-System implementieren** (mit nativem Fallback)
5. **Denizen-Abhängigkeit entfernen** (durch natives System ersetzen)

---

## 1. Storage-Modul Redundanz-Analyse

### Aktueller Status

**✅ BESTÄTIGT: module-storage ist vollständig redundant**

Die Storage-Funktionalität wurde erfolgreich nach `module-plots` verschoben:

```
module-plots/src/main/java/de/fallenstar/plot/storage/
├── command/
│   ├── StorageInfoCommand.java
│   ├── StorageListCommand.java
│   └── StorageSetReceiverCommand.java
├── listener/
│   └── ChestInteractListener.java
├── manager/
│   ├── ChestScanService.java
│   └── StorageManager.java
├── model/
│   ├── ChestData.java
│   ├── PlotStorage.java
│   └── StoredMaterial.java
└── provider/
    └── PlotStorageProvider.java
```

**Alle Dateien existieren identisch in beiden Modulen!**

### Zu entfernende Dateien

```
module-storage/                     # KOMPLETT ENTFERNEN
├── pom.xml
├── README.md
└── src/main/java/de/fallenstar/storage/
    ├── StorageModule.java          # Einzige exklusive Datei
    ├── command/                     # Duplikat von plots
    ├── listener/                    # Duplikat von plots
    ├── manager/                     # Duplikat von plots
    ├── model/                       # Duplikat von plots
    └── storageprovider/             # Duplikat von plots
```

### Anzupassende Dateien

1. **pom.xml** (Root) - `<module>module-storage</module>` entfernen
2. **REPOSITORY_INDEX.md** - Storage-Modul entfernen
3. **CLAUDE.md** - Modulstruktur aktualisieren
4. **README.md** - Modulübersicht aktualisieren

---

## 2. Alte Core-Funktionalitäten (fallenstar-old-core)

### Quelle
**Repository:** https://gitlab.fallenstar.de/team/fallenstar-old-core

### Zu migrierende Funktionalitäten

| Funktionalität | Ziel-Modul | Beschreibung | Status |
|----------------|------------|--------------|--------|
| **Matrix-Bridge** | Chat-Modul | Globaler Chat mit Matrix-Integration | 📋 Neu |
| **Grundstücks-NPCs** | NPCs-Modul | Denizen-basiert, wird nativ ersetzt | 🔨 In Arbeit |
| **Keycloak-Integration** | Auth-Modul | Authentifizierung und Autorisierung | 📋 Neu |
| **Wiki/Forum-Integration** | WebHooks-Modul | Integration externer Dienste | 📋 Neu |

### Denizen-Ersatz

**Problem:** Der Botschafter-NPC wird aktuell über Denizen implementiert.

**Lösung:** Natives NPC-System mit UI-Integration:
- NPCs-Modul implementiert eigenes Interaktionssystem
- UI-Modul stellt Dialoge und Menüs bereit
- Citizens-Integration für NPC-Rendering (optional)
- Keine Denizen-Abhängigkeit mehr

**Implementierung:**
- `/plot npc` Befehle aus altem Core übernehmen
- Native Dialog-Engine im NPCs-Modul
- UI-Provider für Interaktionsmenüs

---

## 3. UI-Provider-System

### Konzept

Ein neues Provider-Interface für User Interfaces mit nativem textbasiertem Fallback.

### UI Provider Interface (Core)

**Datei:** `core/src/main/java/de/fallenstar/core/provider/UIProvider.java`

```java
/**
 * Provider-Interface für User Interfaces.
 *
 * Implementierungen:
 * - NativeUIProvider (FS UI-Modul, textbasiert)
 * - ClientModUIProvider (zukünftig, für schöne UIs)
 * - NoOpUIProvider (Fallback)
 */
public interface UIProvider {
    boolean isAvailable();

    // Basis-UI-Typen
    void showTradeUI(Player player, TradeContext context);
    void showTownManagementUI(Player player, TownContext context);
    void showNPCDialogUI(Player player, DialogContext context);
    void showStorageUI(Player player, StorageContext context);
    void showQuestUI(Player player, QuestContext context);

    // Generische UI-Komponenten
    void showMenu(Player player, Menu menu);
    void showDialog(Player player, Dialog dialog);
    void showForm(Player player, Form form);
}
```

### Native UI Implementierung (Core)

**Datei:** `core/src/main/java/de/fallenstar/core/provider/impl/NativeTextUIProvider.java`

```java
/**
 * Native textbasierte UI-Implementierung.
 *
 * Verwendet Chat-Nachrichten, Books, Signs und Inventories
 * für UI-Rendering ohne Client-Mod.
 *
 * Dient als Fallback und Standard-Implementierung.
 */
public class NativeTextUIProvider implements UIProvider {
    @Override
    public boolean isAvailable() {
        return true; // Immer verfügbar
    }

    @Override
    public void showTradeUI(Player player, TradeContext context) {
        // Text-basiertes Handelsmenü via Chat + Inventory
    }

    // ...weitere Implementierungen
}
```

### NoOp Implementation (Core)

**Datei:** `core/src/main/java/de/fallenstar/core/provider/impl/NoOpUIProvider.java`

```java
/**
 * NoOp Implementation des UIProviders.
 *
 * Wird verwendet wenn kein UI-System verfügbar ist.
 * Alle Methoden werfen ProviderFunctionalityNotFoundException.
 */
public class NoOpUIProvider implements UIProvider {
    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public void showTradeUI(Player player, TradeContext context)
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            "UIProvider", "showTradeUI",
            "No UI system available"
        );
    }

    // ...weitere NoOp-Methoden
}
```

---

## 4. Neue Modulstruktur

### Übersicht

```
fs-core-sample-dump/
├── core/                           # Core + UI Provider Interface
├── module-plots/                   # Plots + Storage (integriert)
├── module-ui/                      # UI-Modul (natives Rendering) ⬅ NEU
├── module-items/                   # Custom Items
├── module-economy/                 # Weltwirtschaft
├── module-worldanchors/            # Schnellreisen
├── module-npcs/                    # NPCs (mit UI-Integration)
├── module-chat/                    # Matrix-Bridge, globaler Chat ⬅ NEU
├── module-auth/                    # Keycloak-Integration ⬅ NEU
├── module-webhooks/                # Wiki/Forum-Integration ⬅ NEU
├── module-adminshops/              # Admin-Shops (optional)
└── module-merchants/               # Händler-System (optional)
```

### Module-Abhängigkeiten

```
Core (UI Provider Interface + Native Fallback)
 ↑
 ├── UI-Modul         (natives UI-Rendering)
 ├── Plots            (Plot-System + Storage)
 ├── Items            (Custom Items, nutzt UI)
 ├── Economy          (Weltwirtschaft, nutzt UI)
 ├── WorldAnchors     (Schnellreisen, nutzt UI)
 ├── NPCs             (NPC-System, nutzt UI + Plot)
 ├── Chat             (Matrix-Bridge)
 ├── Auth             (Keycloak)
 └── WebHooks         (Wiki/Forum)
```

---

## 5. Neue Sprint-Planung (20 Sprints)

| Sprint | Module | Duration | Status | Beschreibung |
|--------|--------|----------|--------|--------------|
| **1-2** | **Core + UI Provider Interface** | 2 Wochen | ✅ / 📋 | Core abgeschlossen, UI Provider Interface hinzufügen |
| **3-4** | **Plots (inkl. Storage)** | 2 Wochen | ✅ | Plot-System + Storage-Integration (fertig) |
| **5-6** | **UI-Modul** | 2 Wochen | 📋 | Natives UI-Rendering (Text, Chat, Inventory, Books) |
| **7-8** | **Items** | 2 Wochen | 📋 | Custom Items mit UI-Integration |
| **9-10** | **Economy** | 2 Wochen | 📋 | Weltwirtschaft mit UI-Integration |
| **11-12** | **WorldAnchors** | 2 Wochen | 📋 | Schnellreisen, POIs, Wegpunkte |
| **13-14** | **NPCs** | 2 Wochen | 🔨 | NPC-System mit UI, Denizen-Ersatz |
| **15-16** | **Chat** | 2 Wochen | 📋 | Matrix-Bridge, globaler Chat |
| **17-18** | **Auth** | 2 Wochen | 📋 | Keycloak-Integration |
| **19-20** | **WebHooks** | 2 Wochen | 📋 | Wiki/Forum-Integration |

**Legende:**
- ✅ Abgeschlossen
- 🔨 In Arbeit
- 📋 Geplant

### Begründung der Reihenfolge

1. **UI vor Items:** Items-Modul nutzt UI für Crafting-Menüs, Item-Auswahl
2. **UI vor NPCs:** NPCs nutzt UI für Dialoge, Handelsmenüs
3. **NPCs nach UI:** Denizen-Ersatz braucht UI-System
4. **Chat/Auth/WebHooks am Ende:** Unabhängige Integration externer Dienste

---

## 6. Neue Module (Details)

### 6.1 UI-Modul (Sprint 5-6)

**Zweck:** Natives UI-Rendering ohne Client-Mod

**Paketstruktur:**
```
module-ui/
├── pom.xml
├── README.md
└── src/main/java/de/fallenstar/ui/
    ├── UIModule.java               # Main class
    ├── provider/
    │   └── NativeUIProvider.java   # Konkrete UI-Implementierung
    ├── renderer/
    │   ├── ChatRenderer.java       # Chat-basierte UIs
    │   ├── BookRenderer.java       # Book-basierte UIs
    │   ├── InventoryRenderer.java  # Inventory-basierte UIs
    │   └── SignRenderer.java       # Sign-basierte UIs
    ├── components/
    │   ├── Menu.java               # Menü-Komponente
    │   ├── Dialog.java             # Dialog-Komponente
    │   ├── Form.java               # Formular-Komponente
    │   └── Button.java             # Button-Komponente
    └── context/
        ├── TradeContext.java       # Handels-UI-Kontext
        ├── TownContext.java        # Stadtverwaltungs-UI-Kontext
        ├── DialogContext.java      # Dialog-UI-Kontext
        └── StorageContext.java     # Storage-UI-Kontext
```

**UI-Typen:**
- **Handels-UI:** Für Economy, Merchants, AdminShops
- **Stadtverwaltungs-UI:** Für Plots, Towny-Integration
- **Dialog-UI:** Für NPCs, Quests
- **Storage-UI:** Für Plot-Storage, Inventare
- **Quest-UI:** Für zukünftiges Quest-System

**Rendering-Methoden:**
- **Chat:** Clickable Text Components, Hover-Events
- **Inventory:** Custom Inventories mit Items
- **Books:** Geschriebene Bücher für längere Texte
- **Signs:** Sign-Editor für kurze Eingaben

**Zukünftige Erweiterung:**
- Client-Mod für schöne UIs (später)
- Custom Packet-basierte UIs (später)

### 6.2 Chat-Modul (Sprint 15-16)

**Zweck:** Matrix-Bridge für globalen Chat

**Paketstruktur:**
```
module-chat/
├── pom.xml
├── README.md
└── src/main/java/de/fallenstar/chat/
    ├── ChatModule.java             # Main class
    ├── provider/
    │   └── MatrixChatProvider.java # Matrix-Integration
    ├── bridge/
    │   ├── MatrixBridge.java       # Matrix-Bridge-Implementierung
    │   └── ChatBridge.java         # Abstrakte Bridge-Klasse
    ├── command/
    │   ├── ChatCommand.java        # /chat Befehl
    │   └── GlobalChatCommand.java  # /global Befehl
    ├── listener/
    │   └── ChatListener.java       # Chat-Events
    └── manager/
        └── ChatManager.java        # Chat-Manager
```

**Funktionalitäten:**
- Globaler Chat über Matrix
- Cross-Server-Chat (via Matrix)
- Private Nachrichten
- Chat-Kanäle
- Moderation
- Chat-Formatierung

**Integration:**
- Matrix-SDK für Java
- Webhook-Support für Discord (optional)
- IRC-Bridge (optional)

### 6.3 Auth-Modul (Sprint 17-18)

**Zweck:** Keycloak-Integration für Authentifizierung

**Paketstruktur:**
```
module-auth/
├── pom.xml
├── README.md
└── src/main/java/de/fallenstar/auth/
    ├── AuthModule.java             # Main class
    ├── provider/
    │   └── KeycloakAuthProvider.java # Keycloak-Integration
    ├── manager/
    │   ├── AuthManager.java        # Auth-Manager
    │   └── SessionManager.java     # Session-Manager
    ├── listener/
    │   ├── LoginListener.java      # Login-Events
    │   └── LogoutListener.java     # Logout-Events
    └── model/
        ├── AuthSession.java        # Session-Modell
        └── AuthToken.java          # Token-Modell
```

**Funktionalitäten:**
- Keycloak-basierte Authentifizierung
- SSO (Single Sign-On)
- Rollen- und Rechteverwaltung
- Session-Management
- Token-basierte Auth
- Multi-Faktor-Authentifizierung (optional)

**Integration:**
- Keycloak-Java-Adapter
- OAuth2/OpenID Connect
- JWT-Tokens

### 6.4 WebHooks-Modul (Sprint 19-20)

**Zweck:** Wiki/Forum-Integration via WebHooks

**Paketstruktur:**
```
module-webhooks/
├── pom.xml
├── README.md
└── src/main/java/de/fallenstar/webhooks/
    ├── WebHooksModule.java         # Main class
    ├── webhook/
    │   ├── WikiWebHook.java        # Wiki-WebHook
    │   ├── ForumWebHook.java       # Forum-WebHook
    │   └── GenericWebHook.java     # Generischer WebHook
    ├── manager/
    │   └── WebHookManager.java     # WebHook-Manager
    ├── listener/
    │   └── GameEventListener.java  # Spiel-Events für WebHooks
    └── model/
        ├── WebHookConfig.java      # WebHook-Konfiguration
        └── WebHookPayload.java     # WebHook-Payload
```

**Funktionalitäten:**
- Wiki-Integration (MediaWiki, DokuWiki, etc.)
- Forum-Integration (Discourse, phpBB, etc.)
- Event-Benachrichtigungen
- Bi-direktionale Synchronisation
- Webhook-basierte API

**Integration:**
- HTTP-Client für Webhooks
- JSON-Parsing
- Event-Mapping

---

## 7. Fehlender Befehl: /plot storage view

### Implementierung in module-plots

**Datei:** `module-plots/src/main/java/de/fallenstar/plot/storage/command/StorageViewCommand.java`

```java
package de.fallenstar.plot.storage.command;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.provider.PlotProvider;
import de.fallenstar.plot.storage.model.PlotStorage;
import de.fallenstar.plot.storage.model.StoredMaterial;
import de.fallenstar.plot.storage.provider.PlotStorageProvider;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Befehl zum Anzeigen des Storage-Inhalts eines Plots.
 *
 * Syntax: /plot storage view [plot-id]
 *
 * Zeigt alle gespeicherten Materialien mit Mengen an.
 *
 * @author FallenStar
 * @version 1.0
 */
public class StorageViewCommand implements CommandExecutor {

    private final PlotStorageProvider storageProvider;
    private final PlotProvider plotProvider;

    public StorageViewCommand(PlotStorageProvider storageProvider,
                              PlotProvider plotProvider) {
        this.storageProvider = storageProvider;
        this.plotProvider = plotProvider;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cNur Spieler können diesen Befehl nutzen!");
            return true;
        }

        // Plot ermitteln
        Plot plot;
        try {
            if (args.length >= 1) {
                // Plot-ID angegeben
                plot = plotProvider.getPlotById(args[0]);
            } else {
                // Aktuelles Plot
                plot = plotProvider.getPlot(player.getLocation());
            }
        } catch (Exception e) {
            player.sendMessage("§cPlot nicht gefunden!");
            return true;
        }

        // Storage-Daten abrufen
        PlotStorage storage = storageProvider.getPlotStorage(plot);

        // Header
        player.sendMessage("§6=== Storage-Übersicht: " + plot.getName() + " ===");
        player.sendMessage("");

        // Material-Liste
        if (storage.getAllMaterials().isEmpty()) {
            player.sendMessage("§7Keine Materialien gespeichert.");
        } else {
            player.sendMessage("§eGespeicherte Materialien:");
            for (StoredMaterial material : storage.getAllMaterials()) {
                player.sendMessage(String.format(
                    "  §f%s: §a%d §7(in %d Truhen)",
                    material.getMaterial().name(),
                    material.getTotalAmount(),
                    material.getChestCount()
                ));
            }
        }

        player.sendMessage("");

        // Statistiken
        player.sendMessage(String.format(
            "§7Gesamt: §e%d §7verschiedene Materialien in §e%d §7Truhen",
            storage.getMaterialCount(),
            storage.getChestCount()
        ));

        // Empfangskiste
        if (storage.getReceiverChest() != null) {
            player.sendMessage("§7Empfangskiste: §a✓ gesetzt");
        } else {
            player.sendMessage("§7Empfangskiste: §c✗ nicht gesetzt");
        }

        return true;
    }
}
```

**Registrierung in PlotsModule:**

```java
// In PlotsModule.java
private void registerCommands() {
    // ...existing commands...

    // Storage View Command
    PluginCommand storageViewCmd = getCommand("plot");
    if (storageViewCmd != null) {
        // Subcommand-Handler erweitern
        storageViewCmd.setExecutor(new PlotCommandHandler(
            // ...
            new StorageViewCommand(storageProvider, plotProvider)
        ));
    }
}
```

---

## 8. Umsetzungsschritte

### Phase 1: Cleanup (Sofort)

1. ✅ Storage-Redundanz bestätigt
2. ⬜ `module-storage/` Verzeichnis löschen
3. ⬜ `pom.xml` (Root) aktualisieren
4. ⬜ `REPOSITORY_INDEX.md` aktualisieren
5. ⬜ `CLAUDE.md` aktualisieren
6. ⬜ Build testen: `mvn clean package`
7. ⬜ Git Commit: "Refactoring: Storage-Modul entfernt (redundant, in Plots integriert)"

### Phase 2: UI Provider Interface (Sprint 1-2 Erweiterung)

1. ⬜ `UIProvider.java` Interface erstellen
2. ⬜ `NativeTextUIProvider.java` in Core implementieren
3. ⬜ `NoOpUIProvider.java` erstellen
4. ⬜ `ProviderRegistry` erweitern
5. ⬜ Context-Klassen erstellen (TradeContext, etc.)
6. ⬜ Build testen
7. ⬜ Git Commit: "Feature: UI Provider Interface in Core hinzugefügt"

### Phase 3: Storage View Command (Sofort)

1. ⬜ `StorageViewCommand.java` implementieren
2. ⬜ Command in `PlotsModule` registrieren
3. ⬜ Testen auf Server
4. ⬜ Git Commit: "Feature: /plot storage view Befehl implementiert"

### Phase 4: Dokumentation Update (Sofort)

1. ⬜ `CLAUDE.md` vollständig aktualisieren
2. ⬜ Neue Modulstruktur dokumentieren
3. ⬜ Sprint-Planung aktualisieren
4. ⬜ UI-Provider-System dokumentieren
5. ⬜ Git Commit: "Docs: Vollständige Restrukturierungs-Dokumentation"

### Phase 5: UI-Modul (Sprint 5-6)

1. ⬜ `module-ui/` erstellen
2. ⬜ `UIModule.java` implementieren
3. ⬜ Native UI-Renderer implementieren
4. ⬜ UI-Komponenten erstellen
5. ⬜ Testen
6. ⬜ Git Commit: "Feature: UI-Modul mit nativem Rendering"

### Phase 6: Weitere Module (Sprint 7-20)

- Sprint 7-8: Items-Modul
- Sprint 9-10: Economy-Modul
- Sprint 11-12: WorldAnchors-Modul
- Sprint 13-14: NPCs-Modul (Denizen-Ersatz)
- Sprint 15-16: Chat-Modul (Matrix-Bridge)
- Sprint 17-18: Auth-Modul (Keycloak)
- Sprint 19-20: WebHooks-Modul (Wiki/Forum)

---

## 9. Technische Details

### UI Provider Auto-Detection

```java
// In ProviderRegistry.java
public void detectAndRegister() {
    // ...existing providers...

    // UI Provider Detection
    if (isPluginEnabled("FallenStar-UI")) {
        uiProvider = getForeignProvider(NativeUIProvider.class);
        logger.info("✓ Registered NativeUIProvider (FallenStar-UI)");
    } else {
        // Fallback auf Core's native Implementation
        uiProvider = new NativeTextUIProvider();
        logger.info("✓ Using NativeTextUIProvider (Core Fallback)");
    }
}
```

### Denizen-Ersatz Konzept

**Alt (Denizen):**
```yaml
# Denizen-Script für Botschafter-NPC
botschafter_script:
  type: interact
  steps:
    1:
      click trigger:
        script:
        - narrate "Willkommen!"
        - menu open trade_menu
```

**Neu (Natives System):**
```java
// Native NPC-Dialog via UI-Provider
public class AmbassadorNPC {
    private final UIProvider uiProvider;

    public void onInteract(Player player, NPC npc) {
        DialogContext context = DialogContext.builder()
            .npc(npc)
            .message("Willkommen!")
            .options(List.of(
                new DialogOption("Handel", () -> showTradeMenu(player)),
                new DialogOption("Quest", () -> showQuestMenu(player)),
                new DialogOption("Info", () -> showInfoDialog(player))
            ))
            .build();

        uiProvider.showNPCDialogUI(player, context);
    }
}
```

### Matrix-Bridge Architektur

```java
// Chat-Modul Integration mit Matrix
public class MatrixBridge implements ChatBridge {
    private MatrixClient matrixClient;

    @Override
    public void sendMessage(String channel, String message) {
        // Sende Nachricht an Matrix-Room
        matrixClient.sendMessage(getRoomId(channel), message);
    }

    @Override
    public void onMatrixMessage(MatrixMessage message) {
        // Empfange Nachricht von Matrix und sende ins Spiel
        String ingameMessage = formatMessage(message);
        Bukkit.broadcastMessage(ingameMessage);
    }
}
```

---

## 10. Zusammenfassung

### Kernänderungen

1. **✅ Storage-Modul entfernen** - Redundant, in Plots integriert
2. **📋 UI-Provider-System** - Interface in Core, natives Rendering
3. **📋 4 neue Module** - UI, Chat, Auth, WebHooks
4. **📋 Denizen ersetzen** - Natives NPC-System mit UI
5. **📋 20-Sprint-Planung** - Strukturierte Entwicklung über 40 Wochen

### Vorteile

- **Modular:** Klare Trennung der Funktionalitäten
- **Unabhängig:** Keine Denizen-Abhängigkeit mehr
- **Erweiterbar:** UI-System kann durch Client-Mod erweitert werden
- **Integriert:** Alte Core-Funktionalitäten sauber migriert
- **Provider-basiert:** Konsistente Architektur

### Nächste Schritte

1. **Sofort:** Storage-Modul entfernen
2. **Sofort:** `/plot storage view` implementieren
3. **Sprint 1-2:** UI Provider Interface zu Core hinzufügen
4. **Sprint 5-6:** UI-Modul implementieren
5. **Danach:** Weitere Module nach Plan

---

**Erstellt:** 2025-11-16
**Autor:** Claude (AI Assistant)
**Version:** 1.0
**Status:** Bereit zur Umsetzung
