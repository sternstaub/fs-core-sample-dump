package de.fallenstar.core.provider;

import de.fallenstar.core.exception.ProviderFunctionalityNotFoundException;
import org.bukkit.inventory.ItemStack;
import java.util.List;
import java.util.Optional;

/**
 * Provider-Interface für Custom-Item-Systeme.
 * 
 * Implementierungen:
 * - MMOItemsProvider (MMOItems-Plugin)
 * - ItemsAdderProvider (ItemsAdder-Plugin)
 * - NoOpItemProvider (Vanilla-Items only)
 * 
 * Ermöglicht:
 * - Custom-Items in Händler-Angeboten
 * - Item-Kategorisierung
 * - Item-Metadata-Zugriff
 * 
 * @author FallenStar
 * @version 1.0
 */
public interface ItemProvider {
    
    /**
     * Prüft ob dieser Provider verfügbar ist.
     * 
     * @return true wenn Custom-Item-System vorhanden
     */
    boolean isAvailable();
    
    /**
     * Erstellt ein Custom-Item anhand seiner ID.
     * 
     * @param itemId Item-ID (z.B. "MYTHIC_SWORD", "legendary_armor")
     * @return Optional<ItemStack> - leer wenn Item nicht existiert
     * @throws ProviderFunctionalityNotFoundException wenn nicht verfügbar
     */
    Optional<ItemStack> createItem(String itemId) 
            throws ProviderFunctionalityNotFoundException;
    
    /**
     * Gibt die Item-ID eines ItemStacks zurück.
     * 
     * @param itemStack Das Item
     * @return Optional<String> - Item-ID oder leer wenn kein Custom-Item
     * @throws ProviderFunctionalityNotFoundException wenn nicht verfügbar
     */
    Optional<String> getItemId(ItemStack itemStack) 
            throws ProviderFunctionalityNotFoundException;
    
    /**
     * Prüft ob ein ItemStack ein Custom-Item ist.
     * 
     * @param itemStack Das Item
     * @return true wenn Custom-Item
     * @throws ProviderFunctionalityNotFoundException wenn nicht verfügbar
     */
    boolean isCustomItem(ItemStack itemStack) 
            throws ProviderFunctionalityNotFoundException;
    
    /**
     * Gibt alle Items einer Kategorie zurück.
     * 
     * Beispiel: "WEAPONS", "ARMOR", "CONSUMABLES"
     * 
     * @param category Kategorie-Name
     * @return Liste von Item-IDs in dieser Kategorie
     * @throws ProviderFunctionalityNotFoundException wenn nicht verfügbar
     */
    List<String> getItemsByCategory(String category) 
            throws ProviderFunctionalityNotFoundException;
    
    /**
     * Gibt alle verfügbaren Kategorien zurück.
     * 
     * @return Liste von Kategorie-Namen
     * @throws ProviderFunctionalityNotFoundException wenn nicht verfügbar
     */
    List<String> getCategories() 
            throws ProviderFunctionalityNotFoundException;
    
    /**
     * Gibt die Kategorie eines Items zurück.
     * 
     * @param itemId Item-ID
     * @return Optional<String> - Kategorie oder leer
     * @throws ProviderFunctionalityNotFoundException wenn nicht verfügbar
     */
    Optional<String> getItemCategory(String itemId) 
            throws ProviderFunctionalityNotFoundException;
    
    /**
     * Gibt einen Richtwert für den Preis eines Items zurück.
     * 
     * Basiert auf Item-Stats, Seltenheit, etc.
     * Nur als Vorschlag - finale Preise werden von Admins/System festgelegt.
     * 
     * @param itemId Item-ID
     * @return Optional<Double> - Empfohlener Preis oder leer
     * @throws ProviderFunctionalityNotFoundException wenn nicht verfügbar
     */
    Optional<Double> getSuggestedPrice(String itemId) 
            throws ProviderFunctionalityNotFoundException;
}
