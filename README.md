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
| [Core](core/) | ⚙️ Foundation | Provider-System, APIs |
| [Storage](module-storage/) | 📦 Sprint 3 | Truhen-Verwaltung |
| [Merchants](module-merchants/) | 🤝 Sprint 4-5 | NPC-Handel |
| [TravelSystem](module-travel/) | 🚢 Sprint 8-9 | Reisende Händler |
| [AdminShops](module-adminshops/) | 🏪 Sprint 6-7 | Template-basierte Shops |

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

**Phase:** 📝 Planung / Sample-Entwicklung  
**Version:** 1.0-SNAPSHOT

**Fertiggestellt:**
- ✅ Architektur-Design
- ✅ Provider-System
- ✅ Sample-Code (Core + Storage)
- ✅ Dokumentation
- ✅ Sprint-Planung

**In Arbeit:**
- 🔨 Core-Implementierung (Sprint 1)

---

**Für Details siehe [REPOSITORY_INDEX.md](REPOSITORY_INDEX.md)**
