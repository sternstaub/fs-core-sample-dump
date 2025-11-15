# FallenStar Paper Samples - Schnellstart-Anleitung

## 🚀 5-Minuten-Übersicht

### What is this?

Complete sample repository for a modular Minecraft plugin system with:
- **Provider-based architecture** (abstracts Towny, Vault, Citizens, etc.)
- **Multiple independent modules** (Storage, Merchants, Travel, AdminShops)
- **AI-optimized development** (sprint-based, clear deliverables)

### Repository Structure

```
FallenStar-Paper-Samples/
├── core/               ← Provider system (START HERE)
├── module-storage/     ← Chest management
├── module-merchants/   ← NPC trading
├── module-travel/      ← Traveling merchants
├── module-adminshops/  ← Template-based shops
└── docs/              ← Documentation
```

---

## 📖 Read These First

**3 Essential Files:**

1. **README.md** ← You are here
2. **REPOSITORY_INDEX.md** ← Complete structure
3. **core/README.md** ← Provider system explained

**Then:**
- CONTRIBUTING.md ← Development guidelines
- SETUP_COMPLETE.md ← What's done, what's needed

---

## 🎯 For Developers

### Understand the Architecture (10 min)

```bash
# 1. Read main README
cat README.md

# 2. Understand provider system
cat core/README.md

# 3. See complete structure
cat REPOSITORY_INDEX.md
```

### Explore Sample Code (15 min)

```bash
# Core Provider Interfaces
cat core/src/main/java/de/fallenstar/core/provider/PlotProvider.java

# Provider Registry (Auto-Detection)
cat core/src/main/java/de/fallenstar/core/registry/ProviderRegistry.java

# Module Example
cat module-storage/src/main/java/de/fallenstar/storage/StorageModule.java
```

### Start Development

**Sprint 1: Core Plugin**

```bash
cd core/

# Create missing providers:
# - NoOpEconomyProvider.java
# - NoOpNPCProvider.java
# - VaultEconomyProvider.java
# - CitizensNPCProvider.java
# - SQLiteDataStore.java

# Test:
mvn clean package
# Copy JAR to server, test provider detection
```

---

## 🏗️ For Project Managers

### Sprint Overview

| Sprint | Module | Duration | Deliverable |
|--------|--------|----------|-------------|
| 1-2 | Core | 2 weeks | Provider system working |
| 3 | Storage | 1 week | Chest management |
| 4-5 | Merchants | 2 weeks | NPC trading |
| 6-7 | AdminShops | 2 weeks | Template shops |
| 8-9 | Travel | 2 weeks | Traveling merchants |
| 10 | All | 1 week | Polish & testing |

**Total:** 10-12 weeks

### See docs/DEVELOPMENT_ROADMAP.md for details

---

## 🤖 For AI-Assisted Development

### Per Chat Session

**Load Context:**
```
1. Sprint goal (from DEVELOPMENT_ROADMAP.md)
2. Relevant interfaces
3. Example implementations
```

**Focus:**
- One module at a time
- Clear deliverables
- Test after each feature

**Output:**
- Working code
- Tests
- Documentation
- Summary for next chat

### Example Chat

```
"Sprint 1: Implement NoOpEconomyProvider

Context:
- EconomyProvider interface (attached)
- NoOpPlotProvider as example (attached)

Deliverable:
- NoOpEconomyProvider.java following the same pattern
- Javadoc comments
- Follows CONTRIBUTING.md guidelines"
```

---

## 📦 What's Included

### ✅ Code (18 files)

**Core Plugin:**
- 7 Provider interfaces
- 2 Provider implementations (Towny, NoOp)
- 5 Core classes
- 2 Config files

**Storage Module:**
- Module main class
- Register command example

### ✅ Documentation (9 files)

- Main README
- Repository index
- Contributing guide
- Setup complete guide
- Core README
- 4 Module READMEs

### ✅ Build Files

- Parent POM
- .gitignore
- setup.sh

---

## 🎓 Key Concepts

### Provider Pattern

**Problem:** Direct plugin dependencies are rigid

```java
// ❌ BAD: Direct dependency
import com.palmergames.bukkit.towny.*;
TownBlock block = TownyAPI.getTownBlock(loc);
```

**Solution:** Provider abstraction

```java
// ✅ GOOD: Provider interface
PlotProvider provider = registry.getPlotProvider();
if (provider.isAvailable()) {
    Plot plot = provider.getPlot(loc);
}
```

### Graceful Degradation

**When plugin missing:**

```java
// NoOp Provider throws exception
public Plot getPlot(Location loc) 
    throws ProviderFunctionalityNotFoundException {
    throw new ProviderFunctionalityNotFoundException(/*...*/);
}

// Module handles it gracefully
try {
    Plot plot = provider.getPlot(loc);
    // Plot-based feature
} catch (ProviderFunctionalityNotFoundException e) {
    // Fallback or disable feature
}
```

### Modular Architecture

```
Core (Foundation)
 ↑
Storage ← Merchants ← TravelSystem
          ↑
          AdminShops
```

**Rules:**
- Modules only depend upward
- No circular dependencies
- Clean interfaces

---

## 🔧 Common Tasks

### Build Everything

```bash
mvn clean package
```

### Build Single Module

```bash
cd core/
mvn clean package
```

### Test on Server

```bash
cp core/target/*.jar /server/plugins/
cp module-*/target/*.jar /server/plugins/
```

### Add New Provider

1. Create interface in `core/provider/`
2. Create NoOp in `core/provider/impl/`
3. Add to `ProviderRegistry`
4. Create concrete impl (optional)

---

## ❓ FAQ

**Q: Where do I start?**  
A: Read README.md, then core/README.md, then start Sprint 1

**Q: Can I use this for my server?**  
A: Yes! This is sample code to build upon

**Q: What if I don't have Towny/Vault/etc?**  
A: That's fine! NoOp providers will be used automatically

**Q: Do I need all modules?**  
A: No! Use only what you need. Start with Core + Storage

**Q: How do I add my own module?**  
A: Copy module-storage structure, follow the pattern

**Q: Where's the complete documentation?**  
A: Check REPOSITORY_INDEX.md for all files

---

## 📞 Support

**Documentation:**
- REPOSITORY_INDEX.md - Complete structure
- CONTRIBUTING.md - Development guidelines
- core/README.md - Provider system
- SETUP_COMPLETE.md - Status & todos

**Sample Code:**
- All files are heavily commented
- Follow existing patterns
- Javadoc on all public methods

---

## ✨ Ready?

**Next Step:** Read [REPOSITORY_INDEX.md](REPOSITORY_INDEX.md) for complete overview

**Then:** Start Sprint 1 (Core Implementation)

**Good luck!** 🚀
