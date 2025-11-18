package de.fallenstar.plot.npc.model;

/**
 * Typen von NPCs die auf Plots gespawnt werden können.
 *
 * Diese Enum wird für die **Persistierung** (Config/YAML) verwendet.
 * Für polymorphe NPC-Implementierungen siehe {@link de.fallenstar.npc.npctype.NPCType}.
 *
 * Jeder Typ hat unterschiedliche Funktionalität und Requirements.
 *
 * @author FallenStar
 * @version 1.0
 */
public enum PlotNPCType {

    /**
     * Gildenhändler (NPC auf Handelsgilde-Plot).
     *
     * - Nutzt SourcePlot-Storage für Inventar
     * - Verkauft Items zu Plot-Preisen
     * - Automatisches Inventar-Management
     */
    GUILD_TRADER("Gildenhändler"),

    /**
     * Spielerhändler (Spieler-gesteuerter NPC).
     *
     * - Spieler kauft Händler-Slot
     * - Spieler wählt SourcePlot (eigenes Grundstück)
     * - Spieler konfiguriert eigene Preise
     * - Manuelles Inventar-Management
     */
    PLAYER_TRADER("Spielerhändler"),

    /**
     * Lokaler Bankier (Bank-NPC mit eigenem Münzbestand).
     *
     * - Gehört zu einer Bank (Plot)
     * - Eigener Münzbestand (kann zur Neige gehen)
     * - Währungsumtausch (Sterne ↔ Lokale Währung)
     * - Nutzt Plot-Storage für Münzreserven
     */
    LOCAL_BANKER("Lokaler Bankier"),

    /**
     * Weltbankier (Globaler Bank-NPC).
     *
     * - Globale Bank ohne Limits
     * - Sterne ↔ Vault-Guthaben
     * - Verfügbar auf Admin-Plots
     */
    WORLD_BANKER("Weltbankier"),

    /**
     * Botschafter-NPC (Schnellreise-System).
     *
     * - Teleportiert zu anderen Botschaftern
     * - Entgelt konfigurierbar (Default: 100 Sterne)
     * - Preis wird vom Plot-Besitzer festgelegt
     */
    AMBASSADOR("Botschafter"),

    /**
     * Handwerker-NPC (Crafting/Repair).
     *
     * - Bietet Crafting-Services
     * - Repariert Items
     * - Enchanting (optional)
     */
    CRAFTSMAN("Handwerker");

    private final String displayName;

    PlotNPCType(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @return Anzeigename des NPC-Typs
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @return Ob dieser NPC-Typ einen SourcePlot benötigt
     */
    public boolean requiresSourcePlot() {
        return this == GUILD_TRADER || this == PLAYER_TRADER || this == LOCAL_BANKER;
    }

    /**
     * @return Ob dieser NPC-Typ von Spielern gekauft werden kann
     */
    public boolean isPurchasable() {
        return this == PLAYER_TRADER;
    }
}
