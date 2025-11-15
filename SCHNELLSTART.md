# FallenStar Paper Samples - Schnellstart-Anleitung

## 🚀 5-Minuten-Übersicht

### Was ist das?

Vollständiges Sample-Repository für ein modulares Minecraft-Plugin-System mit:
- **Provider-basierter Architektur** (abstrahiert Towny, Vault, Citizens, etc.)
- **Mehreren unabhängigen Modulen** (Storage, Merchants, Travel, AdminShops)
- **KI-optimierter Entwicklung** (sprint-basiert, klare Deliverables)

### Repository-Struktur

```
FallenStar-Paper-Samples/
├── core/               ← Provider-System (HIER STARTEN)
├── module-storage/     ← Truhen-Verwaltung
├── module-merchants/   ← NPC-Handel
├── module-travel/      ← Reisende Händler
├── module-adminshops/  ← Template-basierte Shops
└── docs/              ← Dokumentation
```

---

## 📖 Diese Dateien zuerst lesen

**3 essentielle Dateien:**

1. **LIESMICH.md** ← Du bist hier
2. **VERZEICHNIS.md** ← Vollständige Struktur
3. **core/LIESMICH.md** ← Provider-System erklärt

**Dann:**
- CONTRIBUTING.md ← Entwicklungsrichtlinien (EN)
- FERTIGSTELLUNG.md ← Was ist fertig, was fehlt

---

## 🎯 Für Entwickler

### Architektur verstehen (10 min)

```bash
# 1. Haupt-README lesen
cat LIESMICH.md

# 2. Provider-System verstehen
cat core/LIESMICH.md

# 3. Vollständige Struktur ansehen
cat VERZEICHNIS.md
```

### Sample-Code erkunden (15 min)

```bash
# Core Provider-Interfaces
cat core/src/main/java/de/fallenstar/core/provider/PlotProvider.java

# Provider-Registry (Auto-Detection)
cat core/src/main/java/de/fallenstar/core/registry/ProviderRegistry.java

# Modul-Beispiel
cat module-storage/src/main/java/de/fallenstar/storage/StorageModule.java
```

### Mit Entwicklung beginnen

**Sprint 1: Core Plugin**

```bash
cd core/

# Fehlende Provider erstellen:
# - NoOpEconomyProvider.java
# - NoOpNPCProvider.java
# - VaultEconomyProvider.java
# - CitizensNPCProvider.java
# - SQLiteDataStore.java

# Testen:
mvn clean package
# JAR auf Server kopieren, Provider-Detection testen
```

---

## 🏗️ Für Projekt-Manager

### Sprint-Übersicht

| Sprint | Modul | Dauer | Deliverable |
|--------|-------|-------|-------------|
| 1-2 | Core | 2 Wochen | Provider-System funktioniert |
| 3 | Storage | 1 Woche | Truhen-Verwaltung |
| 4-5 | Merchants | 2 Wochen | NPC-Handel |
| 6-7 | AdminShops | 2 Wochen | Template-Shops |
| 8-9 | Travel | 2 Wochen | Reisende Händler |
| 10 | Alle | 1 Woche | Polish & Testing |

**Gesamt:** 10-12 Wochen

### Siehe docs/DEVELOPMENT_ROADMAP.md für Details

---

## 🤖 Für KI-gestützte Entwicklung

### Pro Chat-Session

**Kontext laden:**
```
1. Sprint-Ziel (aus DEVELOPMENT_ROADMAP.md)
2. Relevante Interfaces
3. Beispiel-Implementierungen
```

**Fokus:**
- Ein Modul zur Zeit
- Klare Deliverables
- Nach jedem Feature testen

**Output:**
- Funktionierender Code
- Tests
- Dokumentation
- Summary für nächsten Chat

### Beispiel-Chat

```
"Sprint 1: NoOpEconomyProvider implementieren

Kontext:
- EconomyProvider Interface (angehängt)
- NoOpPlotProvider als Beispiel (angehängt)

Deliverable:
- NoOpEconomyProvider.java nach gleichem Pattern
- Javadoc-Kommentare
- Folgt CONTRIBUTING.md Richtlinien"
```

---

## 📦 Was ist enthalten

### ✅ Code (18 Dateien)

**Core Plugin:**
- 7 Provider-Interfaces
- 2 Provider-Implementierungen (Towny, NoOp)
- 5 Core-Klassen
- 2 Config-Dateien

**Storage Modul:**
- Modul-Hauptklasse
- Register-Command-Beispiel

### ✅ Dokumentation (8 Dateien)

- Deutsche Haupt-Docs (LIESMICH.md, SCHNELLSTART.md, etc.)
- Englische Referenz (README.md, QUICKSTART.md, etc.)
- Core-Dokumentation
- Modul-Dokumentation

### ✅ Build-Dateien

- Parent POM
- .gitignore

---

## 🎓 Schlüsselkonzepte

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
cp core/target/*.jar /server/plugins/
cp module-*/target/*.jar /server/plugins/
```

### Neuen Provider hinzufügen

1. Interface in `core/provider/` erstellen
2. NoOp in `core/provider/impl/` erstellen
3. In `ProviderRegistry` hinzufügen
4. Konkrete Implementation erstellen (optional)

---

## ❓ FAQ

**F: Wo fange ich an?**  
A: Lies LIESMICH.md, dann core/LIESMICH.md, dann starte Sprint 1

**F: Kann ich das für meinen Server verwenden?**  
A: Ja! Das ist Sample-Code zum Weiterbauen

**F: Was wenn ich Towny/Vault/etc. nicht habe?**  
A: Kein Problem! NoOp-Provider werden automatisch verwendet

**F: Brauche ich alle Module?**  
A: Nein! Nutze nur was du brauchst. Start mit Core + Storage

**F: Wie füge ich ein eigenes Modul hinzu?**  
A: Kopiere die module-storage Struktur, folge dem Pattern

**F: Wo ist die vollständige Dokumentation?**  
A: Schau in VERZEICHNIS.md für alle Dateien

---

## 📞 Support

**Dokumentation:**
- VERZEICHNIS.md - Vollständige Struktur
- CONTRIBUTING.md - Entwicklungsrichtlinien
- core/LIESMICH.md - Provider-System
- FERTIGSTELLUNG.md - Status & TODOs

**Sample-Code:**
- Alle Dateien sind umfassend kommentiert
- Folge existierenden Patterns
- Javadoc auf allen public methods

---

## ✨ Bereit?

**Nächster Schritt:** Lies [VERZEICHNIS.md](VERZEICHNIS.md) für vollständige Übersicht

**Dann:** Starte Sprint 1 (Core-Implementierung)

**Viel Erfolg!** 🚀
