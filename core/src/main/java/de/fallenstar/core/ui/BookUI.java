package de.fallenstar.core.ui;

import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Book UI - Multi-Page Text-Output.
 *
 * Verwendet für:
 * - Dokumentation
 * - Lange Texte
 * - Tutorials
 * - Hilfe-Seiten
 *
 * Der Spieler öffnet ein virtuelles Buch das durchgeblättert
 * werden kann. Jede Seite kann Text mit Adventure-API-Formatierung
 * enthalten.
 *
 * @author FallenStar
 * @version 1.0
 */
public abstract class BookUI extends BaseUI {

    protected List<Component> pages;

    /**
     * Konstruktor für BookUI.
     *
     * @param title Titel des Buchs
     */
    public BookUI(String title) {
        super(title);
        this.pages = new ArrayList<>();
    }

    /**
     * Fügt eine Seite zum Buch hinzu.
     *
     * @param page Seiten-Inhalt als Component
     */
    public void addPage(Component page) {
        pages.add(page);
    }

    /**
     * Fügt mehrere Seiten zum Buch hinzu.
     *
     * @param pages Seiten-Inhalte als Components
     */
    public void addPages(List<Component> pages) {
        this.pages.addAll(pages);
    }

    /**
     * Setzt die Seiten des Buchs.
     *
     * @param pages Liste von Seiten-Components
     */
    public void setPages(List<Component> pages) {
        this.pages = new ArrayList<>(pages);
    }

    /**
     * Gibt alle Seiten zurück.
     *
     * @return Liste von Seiten-Components
     */
    public List<Component> getPages() {
        return new ArrayList<>(pages);
    }

    /**
     * Öffnet das Book-UI für einen Spieler.
     *
     * Verwendet Adventure API's Book.openBook() Methode.
     *
     * @param player Spieler für den das UI geöffnet wird
     */
    @Override
    public void open(Player player) {
        if (pages.isEmpty()) {
            return;
        }

        Book book = Book.book(
                Component.text(title),
                Component.text("FallenStar"),
                pages
        );

        player.openBook(book);
    }

    /**
     * Schließt das Book-UI für einen Spieler.
     *
     * @param player Spieler für den das UI geschlossen wird
     */
    @Override
    public void close(Player player) {
        // Buch wird automatisch geschlossen
    }
}
