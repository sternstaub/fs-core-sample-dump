package de.fallenstar.plot.ui;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.provider.PlotProvider;
import de.fallenstar.core.ui.SmallChestUi;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI für Default-Plots (ohne spezielle Plot-Typen).
 *
 * Zeigt Plot-Informationen an:
 * - Plot-Name (Custom oder Default)
 * - Plot-Besitzer
 * - Plot-Identifier
 * - Plot-Location
 *
 * **Verwendung:**
 * - Spieler klickt auf Default-Plot
 * - `/plot gui` öffnet PlotInfoUi
 * - Owner kann Plot-Namen setzen
 *
 * **Layout: SmallChestUi (27 Slots)**
 * - Zeile 0 (0-8): Header + Close
 * - Zeile 1 (9-17): Plot-Info-Buttons
 * - Zeile 2 (18-26): Owner-Optionen (falls Owner)
 *
 * @author FallenStar
 * @version 1.0
 */
public class PlotInfoUi extends SmallChestUi {

    private final Plot plot;
    private final PlotProvider plotProvider;
    private final de.fallenstar.plot.manager.PlotNameManager plotNameManager;
    private final boolean isOwner;

    /**
     * Erstellt ein PlotInfoUi.
     *
     * @param plot Der Plot
     * @param plotProvider PlotProvider (für Owner-Check)
     * @param plotNameManager PlotNameManager (für Namen-Verwaltung)
     * @param player Der Spieler
     */
    public PlotInfoUi(Plot plot, PlotProvider plotProvider, de.fallenstar.plot.manager.PlotNameManager plotNameManager, Player player) {
        super("§6Plot-Informationen");
        this.plot = plot;
        this.plotProvider = plotProvider;
        this.plotNameManager = plotNameManager;

        // Owner-Check
        boolean owner = false;
        try {
            owner = plotProvider.isOwner(plot, player);
        } catch (Exception e) {
            // Fehler → kein Owner
        }
        this.isOwner = owner;

        buildUI(player);
    }

    /**
     * Baut das UI auf.
     */
    private void buildUI(Player player) {
        // Header
        buildHeader();

        // Plot-Info
        buildPlotInfo();

        // Owner-Optionen (falls Owner)
        if (isOwner) {
            buildOwnerOptions();
        }

        // Close-Button
        buildCloseButton();
    }

    /**
     * Baut den Header.
     */
    private void buildHeader() {
        // Slot 4: Plot-Icon
        ItemStack plotIcon = new ItemStack(Material.OAK_SIGN);
        ItemMeta meta = plotIcon.getItemMeta();

        // Name des Plots
        String displayName = plotNameManager.getDisplayName(plot);

        meta.displayName(Component.text(displayName).color(NamedTextColor.GOLD));

        List<String> lore = new ArrayList<>();
        lore.add("§7Grundstücks-Informationen");
        lore.add("");
        lore.add(isOwner ? "§a§lDein Grundstück" : "§7Besucher-Ansicht");

        meta.lore(lore.stream().map(s -> Component.text(s)).toList());
        plotIcon.setItemMeta(meta);

        setItem(4, plotIcon);
    }

    /**
     * Baut die Plot-Info-Buttons.
     */
    private void buildPlotInfo() {
        // Slot 10: Besitzer-Info
        ItemStack ownerItem = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta ownerMeta = ownerItem.getItemMeta();
        ownerMeta.displayName(Component.text("§e§lBesitzer").color(NamedTextColor.YELLOW));

        List<String> ownerLore = new ArrayList<>();
        try {
            String ownerName = plotProvider.getOwnerName(plot);
            ownerLore.add("§7" + ownerName);
        } catch (Exception e) {
            ownerLore.add("§7Unbekannt");
        }

        ownerMeta.lore(ownerLore.stream().map(s -> Component.text(s)).toList());
        ownerItem.setItemMeta(ownerMeta);
        setItem(10, ownerItem);

        // Slot 12: Plot-ID
        ItemStack idItem = new ItemStack(Material.NAME_TAG);
        ItemMeta idMeta = idItem.getItemMeta();
        idMeta.displayName(Component.text("§e§lPlot-ID").color(NamedTextColor.YELLOW));

        List<String> idLore = new ArrayList<>();
        idLore.add("§7" + plot.getIdentifier());

        idMeta.lore(idLore.stream().map(s -> Component.text(s)).toList());
        idItem.setItemMeta(idMeta);
        setItem(12, idItem);

        // Slot 14: Location
        ItemStack locItem = new ItemStack(Material.COMPASS);
        ItemMeta locMeta = locItem.getItemMeta();
        locMeta.displayName(Component.text("§e§lPosition").color(NamedTextColor.YELLOW));

        List<String> locLore = new ArrayList<>();
        locLore.add("§7Welt: §f" + plot.getLocation().getWorld().getName());
        locLore.add("§7X: §f" + plot.getLocation().getBlockX());
        locLore.add("§7Y: §f" + plot.getLocation().getBlockY());
        locLore.add("§7Z: §f" + plot.getLocation().getBlockZ());

        locMeta.lore(locLore.stream().map(s -> Component.text(s)).toList());
        locItem.setItemMeta(locMeta);
        setItem(14, locItem);
    }

    /**
     * Baut die Owner-Optionen.
     */
    private void buildOwnerOptions() {
        // Slot 20: Plot-Namen setzen
        ItemStack nameItem = new ItemStack(Material.NAME_TAG);
        ItemMeta nameMeta = nameItem.getItemMeta();
        nameMeta.displayName(Component.text("§e§lPlot-Namen setzen").color(NamedTextColor.YELLOW));

        List<String> nameLore = new ArrayList<>();
        String currentName = plotNameManager.getPlotName(plot);
        if (currentName != null && !currentName.trim().isEmpty()) {
            nameLore.add("§7Aktuell: §f" + currentName);
        } else {
            nameLore.add("§7Aktuell: §cKein Name gesetzt");
        }
        nameLore.add("");
        nameLore.add("§a§lKlicke zum Ändern");

        nameMeta.lore(nameLore.stream().map(s -> Component.text(s)).toList());
        nameItem.setItemMeta(nameMeta);

        setItem(20, nameItem, player -> {
            if (!isOwner) {
                player.sendMessage("§c✗ Du musst der Owner sein um den Namen zu ändern!");
                close(player);
                return;
            }

            // Öffne Namen-Eingabe
            close(player);
            PlotNameInputUi.openNameInput(player, plot, plotNameManager, name -> {
                player.sendMessage("§a✓ Plot-Name gesetzt: §e" + name);
            });
        });

        // Slot 22: Plot-Typ-Info
        ItemStack typeItem = new ItemStack(Material.BOOK);
        ItemMeta typeMeta = typeItem.getItemMeta();
        typeMeta.displayName(Component.text("§e§lPlot-Typ").color(NamedTextColor.YELLOW));

        List<String> typeLore = new ArrayList<>();
        typeLore.add("§7Standard-Grundstück");
        typeLore.add("");
        typeLore.add("§7Spezielle Plot-Typen:");
        typeLore.add("§8• Handelsgilde (COMMERCIAL)");
        typeLore.add("§8• Botschaft (EMBASSY)");
        typeLore.add("§8• Bank (BANK)");
        typeLore.add("§8• Werkstatt (ARENA)");

        typeMeta.lore(typeLore.stream().map(s -> Component.text(s)).toList());
        typeItem.setItemMeta(typeMeta);
        setItem(22, typeItem);
    }

    /**
     * Baut den Close-Button.
     */
    private void buildCloseButton() {
        ItemStack closeItem = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeItem.getItemMeta();
        closeMeta.displayName(Component.text("§c§lSchließen").color(NamedTextColor.RED));

        closeItem.setItemMeta(closeMeta);
        setItem(26, closeItem, this::close);
    }
}
