# FallenStar Core Plugin

**Foundation of the modular plugin system - provides provider abstraction and APIs**

---

## 🎯 Purpose

The Core plugin is responsible for:
- Provider registry and auto-detection
- DataStore abstraction
- Event system (ProvidersReadyEvent)
- NO business logic - only infrastructure

---

## 📦 Components

### Provider Interfaces

Located in `src/main/java/de/fallenstar/core/provider/`

- **PlotProvider** - Abstraction for Towny, Factions, etc.
- **EconomyProvider** - Abstraction for Vault
- **NPCProvider** - Abstraction for Citizens, ZNPC
- **ItemProvider** - Abstraction for MMOItems, ItemsAdder
- **TradingEntity** - Interface for trading NPCs and shops (Sprint 11-12)
- **ChatProvider** - Abstraction for Matrix, Discord
- **NetworkProvider** - Abstraction for Velocity, BungeeCord

### Provider Registry

`ProviderRegistry` auto-detects available plugins and registers appropriate providers.

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

### Exception System

`ProviderFunctionalityNotFoundException` signals missing features.

Modules catch this and gracefully degrade:

```java
try {
    Plot plot = plotProvider.getPlot(location);
    // Feature available
} catch (ProviderFunctionalityNotFoundException e) {
    // Feature disabled or fallback
}
```

---

## 🚀 Usage

### For Module Developers

1. **Wait for ProvidersReadyEvent**

```java
@EventHandler
public void onProvidersReady(ProvidersReadyEvent event) {
    ProviderRegistry registry = event.getRegistry();
    // Access providers
}
```

2. **Use Providers**

```java
PlotProvider plotProvider = registry.getPlotProvider();

if (plotProvider.isAvailable()) {
    Plot plot = plotProvider.getPlot(location);
    // ...
}
```

3. **Handle Exceptions**

```java
try {
    // Provider operation
} catch (ProviderFunctionalityNotFoundException e) {
    // Graceful degradation
}
```

---

## 📝 Configuration

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

## 🔧 Development

### Adding a New Provider

1. Create interface in `provider/`
2. Create NoOp implementation in `provider/impl/`
3. Add to `ProviderRegistry.detectAndRegister()`
4. Create concrete implementation (optional)

---

## 📚 API

**Get Provider Registry:**

```java
FallenStarCore core = (FallenStarCore) Bukkit.getPluginManager().getPlugin("FallenStar-Core");
ProviderRegistry registry = core.getProviderRegistry();
```

**Get DataStore:**

```java
DataStore dataStore = core.getDataStore();
CompletableFuture<Optional<MyData>> future = dataStore.load("namespace", "key", MyData.class);
```

---

## 📊 Status

**Sprint:** 1-2  
**Status:** ⚙️ In Development  
**Version:** 1.0-SNAPSHOT

---

For more information, see the main [README](../README.md)
