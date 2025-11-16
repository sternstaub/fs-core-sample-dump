package de.fallenstar.ui.ui;

import de.fallenstar.core.ui.BaseUI;
import de.fallenstar.ui.manager.UIButtonManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;

import java.util.ArrayList;
import java.util.List;

/**
 * Einfaches Trade-UI mit Vanilla Villager-Interface.
 *
 * Verwendet das native Minecraft Merchant-Interface (Villager-Handel),
 * um echte Trading-Funktionalität bereitzustellen.
 *
 * Features:
 * - Echtes Villager-Interface (wie NPC-Händler)
 * - Input 1 + Input 2 (optional) → Output
 * - 6 Demo-Trades mit Vanilla-Items
 * - Unbegrenzte Trade-Wiederholungen (maxUses = 999)
 * - Automatische Inventar-Prüfung und Item-Entfernung durch Bukkit
 *
 * Trades:
 * 1. 10 Gold + 5 Redstone → Diamant
 * 2. 20 Eisen → 5 Gold
 * 3. 32 Kohle + 16 Holz → 8 Fackeln
 * 4. 5 Diamanten → Verzauberter Bogen
 * 5. 64 Weizen + 32 Karotten → 16 Goldene Karotten
 * 6. 16 Smaragde → Elytra
 *
 * @author FallenStar
 * @version 2.0 (Vanilla Merchant Interface)
 */
public class SimpleTradeUI extends BaseUI {

    private final UIButtonManager buttonManager;

    /**
     * Konstruktor für SimpleTradeUI.
     *
     * @param buttonManager UIButtonManager-Instanz (für zukünftige Erweiterungen)
     */
    public SimpleTradeUI(UIButtonManager buttonManager) {
        super("§6§lTest-Händler");
        this.buttonManager = buttonManager;
    }

    /**
     * Öffnet das Merchant-Interface für einen Spieler.
     *
     * @param player Spieler für den das Interface geöffnet wird
     */
    @Override
    public void open(Player player) {
        // Erstelle Merchant (Villager-Interface)
        Merchant merchant = Bukkit.createMerchant(Component.text(title));

        // Erstelle Trade-Rezepte
        List<MerchantRecipe> recipes = createTradeRecipes();

        // Füge alle Rezepte zum Merchant hinzu
        merchant.setRecipes(recipes);

        // Öffne Merchant-Interface
        player.openMerchant(merchant, true);

        // Erfolgs-Nachricht
        player.sendMessage(Component.text("✓ Händler geöffnet - Verwende deine Items zum Handeln!", NamedTextColor.GREEN));
    }

    /**
     * Schließt das Merchant-Interface für einen Spieler.
     *
     * @param player Spieler
     */
    @Override
    public void close(Player player) {
        player.closeInventory();
    }

    /**
     * Erstellt alle Trade-Rezepte.
     *
     * @return Liste von MerchantRecipe
     */
    private List<MerchantRecipe> createTradeRecipes() {
        List<MerchantRecipe> recipes = new ArrayList<>();

        // Trade 1: 10 Gold + 5 Redstone → Diamant
        recipes.add(createRecipe(
                new ItemStack(Material.DIAMOND, 1),
                new ItemStack(Material.GOLD_INGOT, 10),
                new ItemStack(Material.REDSTONE, 5)
        ));

        // Trade 2: 20 Eisen → 5 Gold
        recipes.add(createRecipe(
                new ItemStack(Material.GOLD_INGOT, 5),
                new ItemStack(Material.IRON_INGOT, 20),
                null
        ));

        // Trade 3: 32 Kohle + 16 Holz → 8 Fackeln
        recipes.add(createRecipe(
                new ItemStack(Material.TORCH, 8),
                new ItemStack(Material.COAL, 32),
                new ItemStack(Material.OAK_LOG, 16)
        ));

        // Trade 4: 5 Diamanten → Verzauberter Bogen
        ItemStack bow = new ItemStack(Material.BOW, 1);
        // TODO: Echte Verzauberungen hinzufügen
        recipes.add(createRecipe(
                bow,
                new ItemStack(Material.DIAMOND, 5),
                null
        ));

        // Trade 5: 64 Weizen + 32 Karotten → 16 Goldene Karotten
        recipes.add(createRecipe(
                new ItemStack(Material.GOLDEN_CARROT, 16),
                new ItemStack(Material.WHEAT, 64),
                new ItemStack(Material.CARROT, 32)
        ));

        // Trade 6: 16 Smaragde → Elytra
        recipes.add(createRecipe(
                new ItemStack(Material.ELYTRA, 1),
                new ItemStack(Material.EMERALD, 16),
                null
        ));

        return recipes;
    }

    /**
     * Erstellt ein einzelnes Trade-Rezept.
     *
     * @param result Output-Item
     * @param input1 Input 1 (erforderlich)
     * @param input2 Input 2 (optional, kann null sein)
     * @return MerchantRecipe
     */
    private MerchantRecipe createRecipe(ItemStack result, ItemStack input1, ItemStack input2) {
        // Erstelle Rezept mit Output und unbegrenzten Uses
        MerchantRecipe recipe = new MerchantRecipe(result, 999); // maxUses = 999 (quasi unbegrenzt)

        // Füge Inputs hinzu
        recipe.addIngredient(input1);
        if (input2 != null) {
            recipe.addIngredient(input2);
        }

        // Optional: Gebe XP für Trades (0 = kein XP)
        recipe.setExperienceReward(false);

        return recipe;
    }
}
