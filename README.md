# FallenStar Paper Core - Sample Repository

**Vollständiges Sample-Code-Repository für das modulare Plugin-System**

---

## 🎯 Schnellstart

```bash
# Vollständige Repository-Struktur anzeigen
cat REPOSITORY_INDEX.md

# Alle Module bauen
mvn clean package
```

---

## 📚 Dokumentation

- **[REPOSITORY_INDEX.md](REPOSITORY_INDEX.md)** - Vollständige Dateistruktur
- **[QUICKSTART.md](QUICKSTART.md)** - 5-Minuten-Einstieg
- **[SETUP_COMPLETE.md](SETUP_COMPLETE.md)** - Was ist fertig, was fehlt
- **[CONTRIBUTING.md](CONTRIBUTING.md)** - Entwicklungsrichtlinien
- **[core/README.md](core/README.md)** - Core Plugin Dokumentation

---

## 📦 Module

| Modul | Status | Beschreibung |
|-------|--------|--------------|
| [Core](core/) | ✅ Abgeschlossen | Provider-Interfaces, NoOp-Implementierungen |
| [FallenStar Plots](module-plots/) | ✅ Abgeschlossen | Plot-System + Storage + TownyPlotProvider |
| [FallenStar Items](module-items/) | 📋 Geplant | Custom Items + MMOItemsItemProvider |
| [FallenStar Economy](module-economy/) | 📋 Geplant | Weltwirtschaft + VaultEconomyProvider |
| [FallenStar WorldAnchors](module-worldanchors/) | 📋 Geplant | Schnellreisen, POIs, Wegpunkte |
| [FallenStar NPCs](module-npcs/) | 🔨 In Arbeit | NPC-System + CitizensNPCProvider |

---

## 🚀 Features

**Provider-basierte Architektur:**
- ✅ Abstraktion von Dependencies (Towny, Vault, Citizens)
- ✅ Graceful Degradation bei fehlenden Plugins
- ✅ Exception-basiertes Feature-Handling

**Modulares Design:**
- ✅ Unabhängige Module
- ✅ Klare Interfaces
- ✅ Keine direkten Plugin-Dependencies

**KI-optimierte Entwicklung:**
- ✅ Sprint-basierte Planung
- ✅ Fokussierte Arbeitspakete
- ✅ Klare Deliverables

---

## 🛠️ Technology Stack

- **Paper API:** 1.21.1
- **Java:** 21
- **Build Tool:** Maven
- **Datenbank:** SQLite / MySQL

**Optionale Dependencies:**
- Towny (PlotProvider)
- Vault (EconomyProvider)
- Citizens (NPCProvider)
- MMOItems (ItemProvider)

---

## 📊 Projekt-Status

**Phase:** 🚀 Aktive Entwicklung
**Version:** 1.0-SNAPSHOT

**Fertiggestellt:**
- ✅ Architektur-Design & Provider-System (Core nur Interfaces!)
- ✅ Core-Plugin (Interfaces + NoOp-Implementierungen)
- ✅ FallenStar Plots (inkl. Storage + TownyPlotProvider)
- ✅ Dokumentation & Sprint-Planung

**In Arbeit:**
- 🔨 FallenStar NPCs (Sprint 11-12)

**Geplant:**
- 📋 FallenStar Items (Sprint 5-6)
- 📋 FallenStar Economy (Sprint 7-8)
- 📋 FallenStar WorldAnchors (Sprint 9-10)

---

**Für Details siehe [REPOSITORY_INDEX.md](REPOSITORY_INDEX.md)**
