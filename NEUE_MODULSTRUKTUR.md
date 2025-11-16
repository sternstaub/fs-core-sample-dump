# Aktualisierte Modulstruktur für CLAUDE.md

## Zu ersetzender Abschnitt: "Multi-Module Maven Layout"

Dieser Abschnitt ersetzt Zeilen 104-231 in CLAUDE.md.

```
fs-core-sample-dump/
│
├── pom.xml                          # Parent POM (manages all modules)
│
├── core/                            # Core Plugin (Sprint 1-2) ✅
│   ├── pom.xml
│   ├── src/main/
│   │   ├── java/de/fallenstar/core/
│   │   │   ├── FallenStarCore.java           # Main plugin class
│   │   │   ├── provider/                      # Provider interfaces
│   │   │   │   ├── PlotProvider.java
│   │   │   │   ├── EconomyProvider.java
│   │   │   │   ├── NPCProvider.java
│   │   │   │   ├── ItemProvider.java
│   │   │   │   ├── ChatProvider.java
│   │   │   │   ├── UIProvider.java            # 📋 NEU: UI-Provider-Interface
│   │   │   │   ├── AuthProvider.java          # 📋 NEU: Auth-Provider-Interface
│   │   │   │   ├── NetworkProvider.java
│   │   │   │   ├── Plot.java                  # Data model
│   │   │   │   └── impl/                      # NUR NoOp-Implementierungen!
│   │   │   │       ├── NoOpPlotProvider.java
│   │   │   │       ├── NoOpEconomyProvider.java
│   │   │   │       ├── NoOpNPCProvider.java
│   │   │   │       ├── NoOpItemProvider.java
│   │   │   │       ├── NoOpChatProvider.java
│   │   │   │       ├── NoOpUIProvider.java    # 📋 NEU
│   │   │   │       ├── NoOpAuthProvider.java  # 📋 NEU
│   │   │   │       └── NativeTextUIProvider.java  # 📋 NEU: Native Fallback
│   │   │   ├── registry/
│   │   │   │   └── ProviderRegistry.java      # Auto-detects providers
│   │   │   ├── exception/
│   │   │   │   └── ProviderFunctionalityNotFoundException.java
│   │   │   ├── event/
│   │   │   │   └── ProvidersReadyEvent.java
│   │   │   ├── ui/                            # 📋 NEU: UI-Kontext-Klassen
│   │   │   │   ├── context/
│   │   │   │   │   ├── TradeContext.java
│   │   │   │   │   ├── DialogContext.java
│   │   │   │   │   ├── StorageContext.java
│   │   │   │   │   └── TownContext.java
│   │   │   │   └── components/
│   │   │   │       ├── Menu.java
│   │   │   │       ├── Dialog.java
│   │   │   │       └── Form.java
│   │   │   └── database/
│   │   │       ├── DataStore.java             # Interface
│   │   │       └── impl/                      # (missing implementations)
│   │   └── resources/
│   │       ├── plugin.yml
│   │       └── config.yml
│
├── module-plots/                    # FallenStar Plots (Sprint 3-4) ✅
│   ├── pom.xml                      # Plot-System + Storage-Integration
│   ├── src/main/java/de/fallenstar/plots/
│   │   ├── PlotsModule.java                   # Main class
│   │   ├── provider/                          # Provider-Implementierungen
│   │   │   └── TownyPlotProvider.java         # Towny-Integration
│   │   ├── command/                           # Plot-Befehle
│   │   ├── manager/                           # Plot- und Storage-Manager
│   │   ├── model/                             # Plot-Datenmodelle
│   │   ├── listener/                          # Event-Handler
│   │   └── storage/                           # ✅ Storage-System (ex-module-storage)
│   │       ├── command/                       # Storage-Befehle
│   │       │   ├── StorageInfoCommand.java
│   │       │   ├── StorageListCommand.java
│   │       │   ├── StorageSetReceiverCommand.java
│   │       │   └── StorageViewCommand.java    # 📋 NEU: /plot storage view
│   │       ├── manager/                       # Storage-Manager
│   │       │   ├── StorageManager.java
│   │       │   └── ChestScanService.java
│   │       ├── model/                         # Storage-Datenmodelle
│   │       │   ├── PlotStorage.java
│   │       │   ├── ChestData.java
│   │       │   └── StoredMaterial.java
│   │       ├── provider/                      # PlotStorageProvider
│   │       │   └── PlotStorageProvider.java
│   │       └── listener/                      # Storage-Events
│   │           └── ChestInteractListener.java
│   └── src/main/resources/
│       ├── plugin.yml
│       └── config.yml
│
├── module-ui/                       # FallenStar UI (Sprint 5-6) 📋 NEU
│   ├── pom.xml                      # Natives UI-Rendering (Text, Chat, Inventory)
│   ├── src/main/java/de/fallenstar/ui/
│   │   ├── UIModule.java                      # Main class
│   │   ├── provider/                          # Provider-Implementierungen
│   │   │   └── NativeUIProvider.java          # Native UI-Implementierung
│   │   ├── renderer/                          # UI-Renderer
│   │   │   ├── ChatRenderer.java              # Chat-basierte UIs
│   │   │   ├── BookRenderer.java              # Book-basierte UIs
│   │   │   ├── InventoryRenderer.java         # Inventory-basierte UIs
│   │   │   └── SignRenderer.java              # Sign-basierte UIs
│   │   └── components/                        # UI-Komponenten
│   │       ├── Menu.java
│   │       ├── Dialog.java
│   │       └── Form.java
│   └── src/main/resources/
│       ├── plugin.yml
│       └── config.yml
│
├── module-items/                    # FallenStar Items (Sprint 7-8) 📋
│   ├── pom.xml                      # Custom Items, MMOItems-Integration, UI-Integration
│   ├── src/main/java/de/fallenstar/items/
│   │   ├── ItemsModule.java                   # Main class
│   │   ├── provider/                          # Provider-Implementierungen
│   │   │   └── MMOItemsItemProvider.java      # MMOItems-Integration
│   │   ├── command/                           # Item-Befehle
│   │   ├── manager/                           # Item-Manager
│   │   ├── model/                             # Item-Modelle
│   │   ├── factory/                           # Item-Factory
│   │   └── ui/                                # UI-Integration (Crafting-Menüs)
│   └── src/main/resources/
│       ├── plugin.yml
│       └── config.yml
│
├── module-economy/                  # FallenStar Economy (Sprint 9-10) 📋
│   ├── pom.xml                      # Weltwirtschaft, Münzgeld, Preise, UI-Integration
│   ├── src/main/java/de/fallenstar/economy/
│   │   ├── EconomyModule.java                 # Main class
│   │   ├── provider/                          # Provider-Implementierungen
│   │   │   └── VaultEconomyProvider.java      # Vault-Integration
│   │   ├── command/                           # Wirtschafts-Befehle
│   │   ├── manager/                           # Wirtschafts-Manager
│   │   ├── model/                             # Wirtschafts-Modelle
│   │   ├── pricing/                           # Preisberechnungen
│   │   └── ui/                                # UI-Integration (Handels-Menüs)
│   └── src/main/resources/
│       ├── plugin.yml
│       └── config.yml
│
├── module-worldanchors/             # FallenStar WorldAnchors (Sprint 11-12) 📋
│   ├── pom.xml                      # Schnellreisen, POIs, Wegpunkte
│   ├── src/main/java/de/fallenstar/worldanchors/
│   │   ├── WorldAnchorsModule.java            # Main class
│   │   ├── command/                           # Reise-Befehle
│   │   ├── manager/                           # Reise-Manager
│   │   ├── model/                             # POI-Modelle
│   │   └── task/                              # Reise-Tasks
│   └── src/main/resources/
│       ├── plugin.yml
│       └── config.yml
│
├── module-npcs/                     # FallenStar NPCs (Sprint 13-14) 🔨
│   ├── pom.xml                      # NPC-System (Denizen-Ersatz, UI-Integration)
│   ├── src/main/java/de/fallenstar/npcs/
│   │   ├── NPCsModule.java                    # Main class
│   │   ├── provider/                          # Provider-Implementierungen
│   │   │   └── CitizensNPCProvider.java       # Citizens-Integration
│   │   ├── command/                           # NPC-Befehle (/plot npc)
│   │   │   ├── NPCCreateCommand.java
│   │   │   ├── NPCRemoveCommand.java
│   │   │   └── NPCEditCommand.java
│   │   ├── manager/                           # NPC-Manager
│   │   ├── model/                             # NPC-Modelle
│   │   ├── dialog/                            # 📋 NEU: Dialog-Engine (Denizen-Ersatz)
│   │   │   ├── DialogManager.java
│   │   │   ├── DialogTree.java
│   │   │   └── DialogOption.java
│   │   └── gui/                               # NPC-Interaktionen (via UI-Provider)
│   └── src/main/resources/
│       ├── plugin.yml
│       └── config.yml
│
├── module-chat/                     # FallenStar Chat (Sprint 15-16) 📋 NEU
│   ├── pom.xml                      # Matrix-Bridge, globaler Chat
│   ├── src/main/java/de/fallenstar/chat/
│   │   ├── ChatModule.java                    # Main class
│   │   ├── provider/                          # Provider-Implementierungen
│   │   │   └── MatrixChatProvider.java        # Matrix-Integration
│   │   ├── bridge/                            # Chat-Bridge
│   │   │   ├── MatrixBridge.java              # Matrix-Implementierung
│   │   │   └── ChatBridge.java                # Abstrakte Bridge
│   │   ├── command/                           # Chat-Befehle
│   │   │   ├── ChatCommand.java
│   │   │   └── GlobalChatCommand.java
│   │   ├── listener/                          # Chat-Events
│   │   │   └── ChatListener.java
│   │   └── manager/                           # Chat-Manager
│   │       └── ChatManager.java
│   └── src/main/resources/
│       ├── plugin.yml
│       └── config.yml
│
├── module-auth/                     # FallenStar Auth (Sprint 17-18) 📋 NEU
│   ├── pom.xml                      # Keycloak-Integration, SSO
│   ├── src/main/java/de/fallenstar/auth/
│   │   ├── AuthModule.java                    # Main class
│   │   ├── provider/                          # Provider-Implementierungen
│   │   │   └── KeycloakAuthProvider.java      # Keycloak-Integration
│   │   ├── manager/                           # Auth-Manager
│   │   │   ├── AuthManager.java
│   │   │   └── SessionManager.java
│   │   ├── listener/                          # Login/Logout-Events
│   │   │   ├── LoginListener.java
│   │   │   └── LogoutListener.java
│   │   └── model/                             # Auth-Modelle
│   │       ├── AuthSession.java
│   │       └── AuthToken.java
│   └── src/main/resources/
│       ├── plugin.yml
│       └── config.yml
│
├── module-webhooks/                 # FallenStar WebHooks (Sprint 19-20) 📋 NEU
│   ├── pom.xml                      # Wiki/Forum-Integration
│   ├── src/main/java/de/fallenstar/webhooks/
│   │   ├── WebHooksModule.java                # Main class
│   │   ├── webhook/                           # WebHook-Implementierungen
│   │   │   ├── WikiWebHook.java               # Wiki-Integration
│   │   │   ├── ForumWebHook.java              # Forum-Integration
│   │   │   └── GenericWebHook.java            # Generischer WebHook
│   │   ├── manager/                           # WebHook-Manager
│   │   │   └── WebHookManager.java
│   │   ├── listener/                          # Game-Events
│   │   │   └── GameEventListener.java
│   │   └── model/                             # WebHook-Modelle
│   │       ├── WebHookConfig.java
│   │       └── WebHookPayload.java
│   └── src/main/resources/
│       ├── plugin.yml
│       └── config.yml
│
├── module-adminshops/               # FallenStar AdminShops (optional)
│   └── (zukünftig)
│
├── module-merchants/                # FallenStar Merchants (optional)
│   └── (zukünftig)
│
└── Documentation Files (*.md)
```

## Aktualisierter Module Dependency Graph

```
Core (UI Provider Interface + Native Fallback + alle Interfaces)
 ↑
 ├── UI               (Natives UI-Rendering, registriert NativeUIProvider)
 ├── Plots            (Plot-System + Storage, Towny → TownyPlotProvider)
 ├── Items            (Custom Items, MMOItems → MMOItemsItemProvider, nutzt UIProvider)
 ├── Economy          (Weltwirtschaft, Vault → VaultEconomyProvider, nutzt UIProvider)
 ├── WorldAnchors     (Schnellreisen, POIs, Wegpunkte)
 ├── NPCs             (NPC-System, Denizen-Ersatz, nutzt UIProvider + PlotProvider)
 ├── Chat             (Matrix-Bridge, MatrixChatProvider)
 ├── Auth             (Keycloak, KeycloakAuthProvider)
 └── WebHooks         (Wiki/Forum-Integration)
```

## Aktualisierte Sprint-Planung

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

**Gesamt:** 20 Sprints (40 Wochen)

## Wichtige Architektur-Änderungen

### 1. Storage-Modul entfernt ❌

- **module-storage/** wurde komplett entfernt
- Alle Funktionalität ist jetzt in **module-plots/storage/**
- Redundante Dateien gelöscht

### 2. UI-Provider-System hinzugefügt ✅

- **UIProvider.java** Interface in Core
- **NativeTextUIProvider.java** native Fallback-Implementierung in Core
- **NoOpUIProvider.java** NoOp-Implementierung
- **module-ui/** für natives Rendering

### 3. Neue Provider-Interfaces ✅

- **UIProvider** - UI-Rendering
- **AuthProvider** - Authentifizierung

### 4. Neue Module 🆕

- **module-ui** (Sprint 5-6) - Natives UI-System
- **module-chat** (Sprint 15-16) - Matrix-Bridge
- **module-auth** (Sprint 17-18) - Keycloak
- **module-webhooks** (Sprint 19-20) - Wiki/Forum

### 5. Denizen-Ersatz 🔄

- NPCs-Modul bekommt eigene Dialog-Engine
- Nutzt UI-Provider für Interaktionen
- /plot npc Befehle aus altem Core übernehmen
- Keine Denizen-Abhängigkeit mehr

### 6. Sprint-Umplanung 📅

- Von 12 auf 20 Sprints erweitert
- UI-Modul vor Items eingeschoben
- Items von Sprint 5-6 → 7-8
- Economy von Sprint 7-8 → 9-10
- WorldAnchors von Sprint 9-10 → 11-12
- NPCs von Sprint 11-12 → 13-14
- Neue Module: Chat (15-16), Auth (17-18), WebHooks (19-20)
