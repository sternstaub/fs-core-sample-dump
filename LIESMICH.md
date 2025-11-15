# FallenStar Paper Core - Sample Repository

**Vollständiges Sample-Code-Repository für das modulare Plugin-System**

---

## 🎯 Schnellstart

```bash
# Vollständige Repository-Struktur anzeigen
cat VERZEICHNIS.md

# Alle Module bauen
mvn clean package
```

---

## 📚 Dokumentation

- **[VERZEICHNIS.md](VERZEICHNIS.md)** - Vollständige Dateistruktur
- **[SCHNELLSTART.md](SCHNELLSTART.md)** - 5-Minuten-Einstieg
- **[FERTIGSTELLUNG.md](FERTIGSTELLUNG.md)** - Was ist fertig, was fehlt
- **[CONTRIBUTING.md](CONTRIBUTING.md)** - Entwicklungsrichtlinien (EN)

---

## 📦 Module

| Modul | Status | Beschreibung |
|-------|--------|--------------|
| [Core](core/) | ⚙️ Basis | Provider-System, APIs |
| [Storage](module-storage/) | 📦 Sprint 3 | Truhen-Verwaltung |
| [Merchants](module-merchants/) | 🤝 Sprint 4-5 | NPC-Handel |
| [TravelSystem](module-travel/) | 🚢 Sprint 8-9 | Reisende Händler |
| [AdminShops](module-adminshops/) | 🏪 Sprint 6-7 | Template-basierte Shops |

---

## 🚀 Features

**Provider-basierte Architektur:**
- ✅ Abstraktion von Dependencies (Towny, Vault, Citizens)
- ✅ Sanfter Abbau bei fehlenden Plugins
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

## 🛠️ Technologie-Stack

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

## 📋 Repository-Inhalt

### ✅ Code (18 Dateien)

**Core Plugin:**
- 7 Provider-Interfaces
- 2 Provider-Implementierungen (Towny, NoOp)
- 5 Core-Klassen
- 2 Config-Dateien

**Storage Modul:**
- Modul-Hauptklasse
- Register-Command-Beispiel

### ✅ Dokumentation

- Deutsche Haupt-Docs
- Englische Referenz-Docs
- Core README
- Module READMEs

### ✅ Build-Dateien

- Parent POM
- .gitignore

---

## 🎯 Nächste Schritte

### 1. Dokumentation lesen

```bash
# Schnellstart (5 Minuten)
cat SCHNELLSTART.md

# Vollständiges Verzeichnis
cat VERZEICHNIS.md

# Core verstehen
cat core/LIESMICH.md
```

### 2. Code erkunden

```bash
# Provider-Interfaces anschauen
ls -la core/src/main/java/de/fallenstar/core/provider/

# Beispiel-Modul
cat module-storage/src/main/java/de/fallenstar/storage/StorageModule.java
```

### 3. Mit Entwicklung beginnen

**Sprint 1: Core Plugin**
```bash
cd core/

# Fehlende Dateien implementieren:
# - NoOpEconomyProvider.java
# - NoOpNPCProvider.java
# - VaultEconomyProvider.java
# - CitizensNPCProvider.java
# - SQLiteDataStore.java
```

---

## 🏗️ Architektur-Übersicht

### Provider-Pattern

**Problem:** Direkte Plugin-Dependencies sind starr

```java
// ❌ SCHLECHT: Direkte Dependency
import com.palmergames.bukkit.towny.*;
TownBlock block = TownyAPI.getTownBlock(loc);
```

**Lösung:** Provider-Abstraktion

```java
// ✅ GUT: Provider-Interface
PlotProvider provider = registry.getPlotProvider();
if (provider.isAvailable()) {
    Plot plot = provider.getPlot(loc);
}
```

### Graceful Degradation

**Bei fehlendem Plugin:**

```java
// NoOp-Provider wirft Exception
public Plot getPlot(Location loc) 
    throws ProviderFunctionalityNotFoundException {
    throw new ProviderFunctionalityNotFoundException(/*...*/);
}

// Modul behandelt es elegant
try {
    Plot plot = provider.getPlot(loc);
    // Plot-basiertes Feature
} catch (ProviderFunctionalityNotFoundException e) {
    // Fallback oder Feature deaktivieren
}
```

### Modulare Architektur

```
Core (Basis)
 ↑
Storage ← Merchants ← TravelSystem
          ↑
          AdminShops
```

**Regeln:**
- Module hängen nur nach oben ab
- Keine zirkulären Dependencies
- Saubere Interfaces

---

## 🔧 Häufige Aufgaben

### Alles bauen

```bash
mvn clean package
```

### Einzelnes Modul bauen

```bash
cd core/
mvn clean package
```

### Auf Server testen

```bash
cp core/target/*.jar /pfad/zum/server/plugins/
cp module-*/target/*.jar /pfad/zum/server/plugins/
```

---

## ❓ FAQ

**F: Wo fange ich an?**  
A: Lies SCHNELLSTART.md, dann VERZEICHNIS.md, dann beginne mit Sprint 1

**F: Kann ich das für meinen Server verwenden?**  
A: Ja! Das ist Sample-Code zum Weiterbauen

**F: Was wenn ich Towny/Vault/etc. nicht habe?**  
A: Kein Problem! NoOp-Provider werden automatisch verwendet

**F: Brauche ich alle Module?**  
A: Nein! Nutze nur was du brauchst. Start mit Core + Storage

**F: Wie füge ich ein eigenes Modul hinzu?**  
A: Kopiere die module-storage Struktur, folge dem Pattern

---

## 📞 Hilfe & Support

**Dokumentation:**
- VERZEICHNIS.md - Vollständige Struktur
- FERTIGSTELLUNG.md - Status & TODOs
- core/LIESMICH.md - Provider-System
- CONTRIBUTING.md - Entwicklungsrichtlinien

**Sample-Code:**
- Alle Dateien sind umfassend kommentiert
- Folge existierenden Patterns
- Javadoc auf allen public methods

---

**Für detaillierte Informationen siehe [VERZEICHNIS.md](VERZEICHNIS.md)**

**Viel Erfolg! 🚀**
