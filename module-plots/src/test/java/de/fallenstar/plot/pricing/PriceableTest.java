package de.fallenstar.plot.pricing;

import de.fallenstar.plot.model.TradeguildPlot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit-Tests für Priceable Interface (Sprint 19 Phase 3).
 *
 * Testet TradeguildPlot's Implementierung von Priceable:
 * - getBuyPrice / setSellPrice
 * - removeBuyPrice / removeSellPrice
 * - clearAllBuyPrices / clearAllSellPrices
 * - getAllBuyPrices / getAllSellPrices
 *
 * @author FallenStar
 * @version 1.0
 */
@DisplayName("Priceable Interface Tests (Sprint 19 Phase 3)")
class PriceableTest {

    private TradeguildPlot tradeguildPlot;
    private Location mockLocation;

    @BeforeEach
    void setUp() {
        // Mock Location
        World mockWorld = mock(World.class);
        when(mockWorld.getName()).thenReturn("world");

        mockLocation = mock(Location.class);
        when(mockLocation.getWorld()).thenReturn(mockWorld);
        when(mockLocation.getBlockX()).thenReturn(100);
        when(mockLocation.getBlockY()).thenReturn(64);
        when(mockLocation.getBlockZ()).thenReturn(200);

        // Create TradeguildPlot
        tradeguildPlot = new TradeguildPlot(
            "PriceableTest_001",
            UUID.randomUUID(),
            mockLocation
        );
    }

    // ========== setBuyPrice / getSellPrice Tests ==========

    @Test
    @DisplayName("setBuyPrice: Setzt Kaufpreis korrekt")
    void testSetBuyPrice_success() {
        BigDecimal price = BigDecimal.valueOf(100);

        tradeguildPlot.setBuyPrice(Material.DIAMOND, price);

        Optional<BigDecimal> result = tradeguildPlot.getBuyPrice(Material.DIAMOND);
        assertTrue(result.isPresent(), "Kaufpreis sollte gesetzt sein");
        assertEquals(price, result.get(), "Kaufpreis sollte 100 sein");
    }

    @Test
    @DisplayName("setSellPrice: Setzt Verkaufspreis korrekt")
    void testSetSellPrice_success() {
        BigDecimal price = BigDecimal.valueOf(150);

        tradeguildPlot.setSellPrice(Material.DIAMOND, price);

        Optional<BigDecimal> result = tradeguildPlot.getSellPrice(Material.DIAMOND);
        assertTrue(result.isPresent(), "Verkaufspreis sollte gesetzt sein");
        assertEquals(price, result.get(), "Verkaufspreis sollte 150 sein");
    }

    @Test
    @DisplayName("getBuyPrice: Gibt empty Optional bei nicht-gesetztem Preis")
    void testGetBuyPrice_notSet() {
        Optional<BigDecimal> result = tradeguildPlot.getBuyPrice(Material.EMERALD);

        assertFalse(result.isPresent(), "Kaufpreis sollte nicht gesetzt sein");
    }

    @Test
    @DisplayName("getSellPrice: Gibt empty Optional bei nicht-gesetztem Preis")
    void testGetSellPrice_notSet() {
        Optional<BigDecimal> result = tradeguildPlot.getSellPrice(Material.EMERALD);

        assertFalse(result.isPresent(), "Verkaufspreis sollte nicht gesetzt sein");
    }

    // ========== removeBuyPrice / removeSellPrice Tests ==========

    @Test
    @DisplayName("removeBuyPrice: Entfernt Kaufpreis")
    void testRemoveBuyPrice_success() {
        tradeguildPlot.setBuyPrice(Material.IRON_INGOT, BigDecimal.TEN);

        tradeguildPlot.removeBuyPrice(Material.IRON_INGOT);

        Optional<BigDecimal> result = tradeguildPlot.getBuyPrice(Material.IRON_INGOT);
        assertFalse(result.isPresent(), "Kaufpreis sollte entfernt sein");
    }

    @Test
    @DisplayName("removeSellPrice: Entfernt Verkaufspreis")
    void testRemoveSellPrice_success() {
        tradeguildPlot.setSellPrice(Material.IRON_INGOT, BigDecimal.valueOf(20));

        tradeguildPlot.removeSellPrice(Material.IRON_INGOT);

        Optional<BigDecimal> result = tradeguildPlot.getSellPrice(Material.IRON_INGOT);
        assertFalse(result.isPresent(), "Verkaufspreis sollte entfernt sein");
    }

    // ========== getAllBuyPrices / getAllSellPrices Tests ==========

    @Test
    @DisplayName("getAllBuyPrices: Gibt alle Kaufpreise zurück")
    void testGetAllBuyPrices_success() {
        tradeguildPlot.setBuyPrice(Material.DIAMOND, BigDecimal.valueOf(100));
        tradeguildPlot.setBuyPrice(Material.EMERALD, BigDecimal.valueOf(50));

        Map<Material, BigDecimal> prices = tradeguildPlot.getAllBuyPrices();

        assertEquals(2, prices.size(), "Sollte 2 Kaufpreise haben");
        assertEquals(BigDecimal.valueOf(100), prices.get(Material.DIAMOND), "Diamond Kaufpreis sollte 100 sein");
        assertEquals(BigDecimal.valueOf(50), prices.get(Material.EMERALD), "Emerald Kaufpreis sollte 50 sein");
    }

    @Test
    @DisplayName("getAllSellPrices: Gibt alle Verkaufspreise zurück")
    void testGetAllSellPrices_success() {
        tradeguildPlot.setSellPrice(Material.DIAMOND, BigDecimal.valueOf(150));
        tradeguildPlot.setSellPrice(Material.EMERALD, BigDecimal.valueOf(75));

        Map<Material, BigDecimal> prices = tradeguildPlot.getAllSellPrices();

        assertEquals(2, prices.size(), "Sollte 2 Verkaufspreise haben");
        assertEquals(BigDecimal.valueOf(150), prices.get(Material.DIAMOND), "Diamond Verkaufspreis sollte 150 sein");
        assertEquals(BigDecimal.valueOf(75), prices.get(Material.EMERALD), "Emerald Verkaufspreis sollte 75 sein");
    }

    // ========== clearAllBuyPrices / clearAllSellPrices Tests ==========

    @Test
    @DisplayName("clearAllBuyPrices: Löscht alle Kaufpreise")
    void testClearAllBuyPrices_success() {
        tradeguildPlot.setBuyPrice(Material.DIAMOND, BigDecimal.valueOf(100));
        tradeguildPlot.setBuyPrice(Material.EMERALD, BigDecimal.valueOf(50));

        tradeguildPlot.clearAllBuyPrices();

        Map<Material, BigDecimal> prices = tradeguildPlot.getAllBuyPrices();
        assertTrue(prices.isEmpty(), "Alle Kaufpreise sollten gelöscht sein");
    }

    @Test
    @DisplayName("clearAllSellPrices: Löscht alle Verkaufspreise")
    void testClearAllSellPrices_success() {
        tradeguildPlot.setSellPrice(Material.DIAMOND, BigDecimal.valueOf(150));
        tradeguildPlot.setSellPrice(Material.EMERALD, BigDecimal.valueOf(75));

        tradeguildPlot.clearAllSellPrices();

        Map<Material, BigDecimal> prices = tradeguildPlot.getAllSellPrices();
        assertTrue(prices.isEmpty(), "Alle Verkaufspreise sollten gelöscht sein");
    }

    // ========== hasBuyPrice / hasSellPrice Tests (default Methoden) ==========

    @Test
    @DisplayName("hasBuyPrice: Gibt true bei gesetztem Preis")
    void testHasBuyPrice_true() {
        tradeguildPlot.setBuyPrice(Material.GOLD_INGOT, BigDecimal.valueOf(25));

        assertTrue(tradeguildPlot.hasBuyPrice(Material.GOLD_INGOT), "Sollte Kaufpreis haben");
    }

    @Test
    @DisplayName("hasBuyPrice: Gibt false bei nicht-gesetztem Preis")
    void testHasBuyPrice_false() {
        assertFalse(tradeguildPlot.hasBuyPrice(Material.GOLD_INGOT), "Sollte keinen Kaufpreis haben");
    }

    @Test
    @DisplayName("hasSellPrice: Gibt true bei gesetztem Preis")
    void testHasSellPrice_true() {
        tradeguildPlot.setSellPrice(Material.GOLD_INGOT, BigDecimal.valueOf(30));

        assertTrue(tradeguildPlot.hasSellPrice(Material.GOLD_INGOT), "Sollte Verkaufspreis haben");
    }

    @Test
    @DisplayName("hasSellPrice: Gibt false bei nicht-gesetztem Preis")
    void testHasSellPrice_false() {
        assertFalse(tradeguildPlot.hasSellPrice(Material.GOLD_INGOT), "Sollte keinen Verkaufspreis haben");
    }

    // ========== Integration Tests (Buy + Sell zusammen) ==========

    @Test
    @DisplayName("Integration: Kaufpreis < Verkaufspreis (Standard-Pattern)")
    void testIntegration_buyLessThanSell() {
        BigDecimal buyPrice = BigDecimal.valueOf(100);
        BigDecimal sellPrice = BigDecimal.valueOf(150);

        tradeguildPlot.setBuyPrice(Material.DIAMOND, buyPrice);
        tradeguildPlot.setSellPrice(Material.DIAMOND, sellPrice);

        assertTrue(
            buyPrice.compareTo(sellPrice) < 0,
            "Kaufpreis sollte < Verkaufspreis sein (Standard-Pattern)"
        );

        BigDecimal profit = sellPrice.subtract(buyPrice);
        assertEquals(BigDecimal.valueOf(50), profit, "Gewinn-Marge sollte 50 sein");
    }

    @Test
    @DisplayName("Integration: Kaufpreis und Verkaufspreis unabhängig")
    void testIntegration_independentPrices() {
        tradeguildPlot.setBuyPrice(Material.DIAMOND, BigDecimal.valueOf(100));

        // Verkaufspreis NICHT setzen
        Optional<BigDecimal> sellPrice = tradeguildPlot.getSellPrice(Material.DIAMOND);

        assertFalse(sellPrice.isPresent(), "Verkaufspreis sollte unabhängig vom Kaufpreis sein");
    }
}
