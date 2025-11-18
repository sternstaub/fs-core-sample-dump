package de.fallenstar.items.ui;

import de.fallenstar.core.ui.LargeChestUi;
import de.fallenstar.items.provider.MMOItemsItemProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * UI zum Durchstöbern aller verfügbaren Custom-Items.
 *
 * Features:
 * - Zeigt alle Custom-Items kategorisiert
 * - Klick: Item erhalten (nur Test-Zwecke)
 * - Zeigt Item-Kategorien und Preise
 *
 * @author FallenStar
 * @version 1.0
 */
public class ItemBrowserUi extends LargeChestUi {

    private final MMOItemsItemProvider itemProvider;
    private String currentCategory = null;
    private int currentPage = 0;
    private static final int ITEMS_PER_PAGE = 45; // 5 Zeilen à 9 Slots

    public ItemBrowserUi(MMOItemsItemProvider itemProvider) {
        super("§6Custom Items Browser");
        this.itemProvider = itemProvider;
    }

    @Override
    public void open(Player player) {
        // Beim ersten Öffnen zeige Kategorien
        showCategories(player);
    }

    /**
     * Zeigt alle Kategorien an.
     */
    public void showCategories(Player player) {
        this.currentCategory = null;
        this.currentPage = 0;

        clearItems();
        setTitle("§6Item Browser - Kategorien");

        List<String> categories;
        try {
            categories = itemProvider.getCategories();
        } catch (Exception e) {
            player.sendMessage(Component.text("✗ Fehler beim Laden der Kategorien", NamedTextColor.RED));
            return;
        }

        int slot = 0;
        for (String category : categories) {
            if (slot >= ITEMS_PER_PAGE) break;

            List<String> itemIds;
            try {
                itemIds = itemProvider.getItemsByCategory(category);
            } catch (Exception e) {
                continue;
            }

            ItemStack icon = new ItemStack(Material.CHEST);
            ItemMeta meta = icon.getItemMeta();
            meta.displayName(Component.text(category, NamedTextColor.GOLD));
            meta.lore(List.of(
                    Component.text("Items: " + itemIds.size(), NamedTextColor.GRAY),
                    Component.empty(),
                    Component.text("» Klicke zum Öffnen", NamedTextColor.YELLOW)
            ));
            icon.setItemMeta(meta);

            setItem(slot++, icon, p -> {
                showCategory(p, category);
            });
        }

        // Zurück-Button
        addBackButton(player);

        super.open(player);
    }

    /**
     * Zeigt Items einer Kategorie an.
     */
    public void showCategory(Player player, String category) {
        this.currentCategory = category;
        this.currentPage = 0;

        loadCategoryPage(player);
    }

    /**
     * Lädt eine Seite einer Kategorie.
     */
    private void loadCategoryPage(Player player) {
        clearItems();
        setTitle("§6" + currentCategory + " (Seite " + (currentPage + 1) + ")");

        List<String> itemIds;
        try {
            itemIds = itemProvider.getItemsByCategory(currentCategory);
        } catch (Exception e) {
            player.sendMessage(Component.text("✗ Fehler beim Laden der Items", NamedTextColor.RED));
            return;
        }

        int startIndex = currentPage * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, itemIds.size());

        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            String itemId = itemIds.get(i);
            final int finalSlot = slot; // Final Variable für Lambda

            try {
                itemProvider.getItemType(itemId).ifPresent(type -> {
                    try {
                        itemProvider.createItem(type, itemId, 1).ifPresent(itemStack -> {
                            // Erweitere Lore mit zusätzlichen Infos
                            ItemMeta meta = itemStack.getItemMeta();
                            List<Component> lore = meta.hasLore() ? new ArrayList<>(meta.lore()) : new ArrayList<>();
                            lore.add(Component.empty());
                            lore.add(Component.text("Type: " + type, NamedTextColor.GRAY));
                            lore.add(Component.text("ID: " + itemId, NamedTextColor.GRAY));

                            // Preis anzeigen
                            try {
                                itemProvider.getSuggestedPrice(itemId).ifPresent(price -> {
                                    if (price > 0) {
                                        lore.add(Component.text("Preis: " + price + " Münzen", NamedTextColor.GOLD));
                                    } else {
                                        lore.add(Component.text("Nicht handelbar", NamedTextColor.RED));
                                    }
                                });
                            } catch (Exception e) {
                                // Preis nicht verfügbar
                            }

                            lore.add(Component.empty());
                            lore.add(Component.text("» Klicke zum Erhalten (Test)", NamedTextColor.YELLOW));
                            meta.lore(lore);
                            itemStack.setItemMeta(meta);

                            setItem(finalSlot, itemStack, p -> {
                                giveItem(p, type, itemId);
                            });
                        });
                    } catch (Exception e) {
                        // Item konnte nicht erstellt werden
                    }
                });
            } catch (Exception e) {
                // ItemType konnte nicht geladen werden
            }

            slot++;
        }

        // Navigation
        addNavigationButtons(player, itemIds.size());

        super.open(player);
    }

    /**
     * Gibt ein Item an den Spieler.
     */
    private void giveItem(Player player, String type, String itemId) {
        try {
            itemProvider.createItem(type, itemId, 1).ifPresent(item -> {
                player.getInventory().addItem(item);
                player.sendMessage(Component.text("✓ Item erhalten: " + itemId, NamedTextColor.GREEN));
            });
        } catch (Exception e) {
            player.sendMessage(Component.text("✗ Fehler beim Erstellen des Items", NamedTextColor.RED));
        }
    }

    /**
     * Fügt Navigations-Buttons hinzu.
     */
    private void addNavigationButtons(Player player, int totalItems) {
        int totalPages = (int) Math.ceil((double) totalItems / ITEMS_PER_PAGE);

        // Zurück zur Kategorie-Übersicht (Slot 45)
        ItemStack backIcon = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backIcon.getItemMeta();
        backMeta.displayName(Component.text("« Zurück zu Kategorien", NamedTextColor.YELLOW));
        backIcon.setItemMeta(backMeta);
        setItem(45, backIcon, p -> showCategories(p));

        // Vorherige Seite (Slot 48)
        if (currentPage > 0) {
            ItemStack prevIcon = new ItemStack(Material.SPECTRAL_ARROW);
            ItemMeta prevMeta = prevIcon.getItemMeta();
            prevMeta.displayName(Component.text("« Vorherige Seite", NamedTextColor.AQUA));
            prevIcon.setItemMeta(prevMeta);
            setItem(48, prevIcon, p -> {
                currentPage--;
                loadCategoryPage(p);
            });
        }

        // Info (Slot 49)
        ItemStack infoIcon = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoIcon.getItemMeta();
        infoMeta.displayName(Component.text("Seite " + (currentPage + 1) + "/" + totalPages, NamedTextColor.WHITE));
        infoMeta.lore(List.of(
                Component.text("Gesamt: " + totalItems + " Items", NamedTextColor.GRAY)
        ));
        infoIcon.setItemMeta(infoMeta);
        setItem(49, infoIcon, null); // Kein Click-Handler

        // Nächste Seite (Slot 50)
        if ((currentPage + 1) < totalPages) {
            ItemStack nextIcon = new ItemStack(Material.SPECTRAL_ARROW);
            ItemMeta nextMeta = nextIcon.getItemMeta();
            nextMeta.displayName(Component.text("Nächste Seite »", NamedTextColor.AQUA));
            nextIcon.setItemMeta(nextMeta);
            setItem(50, nextIcon, p -> {
                currentPage++;
                loadCategoryPage(p);
            });
        }

        // Schließen (Slot 53)
        ItemStack closeIcon = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeIcon.getItemMeta();
        closeMeta.displayName(Component.text("Schließen", NamedTextColor.RED));
        closeIcon.setItemMeta(closeMeta);
        setItem(53, closeIcon, this::close);
    }

    /**
     * Fügt Zurück-Button hinzu (nur bei Kategorie-Übersicht).
     */
    private void addBackButton(Player player) {
        ItemStack closeIcon = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeIcon.getItemMeta();
        closeMeta.displayName(Component.text("Schließen", NamedTextColor.RED));
        closeIcon.setItemMeta(closeMeta);
        setItem(49, closeIcon, this::close);
    }
}
