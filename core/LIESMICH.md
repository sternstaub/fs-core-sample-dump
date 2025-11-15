# FallenStar Core Plugin

**Basis des modularen Plugin-Systems - stellt Provider-Abstraktion und APIs bereit**

---

## 🎯 Zweck

Das Core-Plugin ist verantwortlich für:
- Provider-Registry und Auto-Detection
- DataStore-Abstraktion
- Event-System (ProvidersReadyEvent)
- KEINE Business-Logic - nur Infrastruktur

---

## 📦 Komponenten

### Provider-Interfaces

In `src/main/java/de/fallenstar/core/provider/`

- **PlotProvider** - Abstraktion für Towny, Factions, etc.
- **EconomyProvider** - Abstraktion für Vault
- **NPCProvider** - Abstraktion für Citizens, ZNPC
- **ItemProvider** - Abstraktion für MMOItems, ItemsAdder
- **ChatProvider** - Abstraktion für Matrix, Discord
- **NetworkProvider** - Abstraktion für Velocity, BungeeCord

### Provider-Registry

`ProviderRegistry` erkennt verfügbare Plugins automatisch und registriert entsprechende Provider.

```java
public void detectAndRegister() {
    if (isPluginEnabled("Towny")) {
        plotProvider = new TownyPlotProvider();
    } else {
        plotProvider = new NoOpPlotProvider();
    }
    // ...
}
```

### Exception-System

`ProviderFunctionalityNotFoundException` signalisiert fehlende Features.

Module fangen diese ab und degradieren sanft:

```java
try {
    Plot plot = plotProvider.getPlot(location);
    // Feature verfügbar
} catch (ProviderFunctionalityNotFoundException e) {
    // Feature deaktivieren oder Fallback
}
```

---

## 🚀 Verwendung

### Für Modul-Entwickler

1. **Auf ProvidersReadyEvent warten**

```java
@EventHandler
public void onProvidersReady(ProvidersReadyEvent event) {
    ProviderRegistry registry = event.getRegistry();
    // Provider nutzen
}
```

2. **Provider verwenden**

```java
PlotProvider plotProvider = registry.getPlotProvider();

if (plotProvider.isAvailable()) {
    Plot plot = plotProvider.getPlot(location);
    // ...
}
```

3. **Exceptions behandeln**

```java
try {
    // Provider-Operation
} catch (ProviderFunctionalityNotFoundException e) {
    // Sanfte Degradierung
}
```

---

## 📝 Konfiguration

**config.yml:**

```yaml
database:
  type: sqlite  # sqlite, mysql, redis

providers:
  plot:
    enabled: true
    preferred: towny
  
  economy:
    enabled: true
  
  npc:
    enabled: true
    preferred: citizens
```

---

## 🔧 Entwicklung

### Neuen Provider hinzufügen

1. Interface in `provider/` erstellen
2. NoOp-Implementation in `provider/impl/` erstellen
3. In `ProviderRegistry.detectAndRegister()` hinzufügen
4. Konkrete Implementation erstellen (optional)

---

## 📚 API

**Provider-Registry holen:**

```java
FallenStarCore core = (FallenStarCore) Bukkit.getPluginManager()
    .getPlugin("FallenStar-Core");
ProviderRegistry registry = core.getProviderRegistry();
```

**DataStore holen:**

```java
DataStore dataStore = core.getDataStore();
CompletableFuture<Optional<MyData>> future = 
    dataStore.load("namespace", "key", MyData.class);
```

---

## 📊 Status

**Sprint:** 1-2  
**Status:** ⚙️ In Entwicklung  
**Version:** 1.0-SNAPSHOT

---

Für mehr Informationen siehe [Haupt-README](../LIESMICH.md)
