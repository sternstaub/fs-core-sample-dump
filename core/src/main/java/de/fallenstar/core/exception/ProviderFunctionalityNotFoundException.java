package de.fallenstar.core.exception;

/**
 * Exception die geworfen wird wenn eine Provider-Funktionalität
 * nicht verfügbar ist.
 * 
 * Module können diese Exception fangen und graceful degradieren,
 * d.h. Features deaktivieren oder Fallback-Verhalten nutzen.
 * 
 * Beispiel:
 * <pre>
 * try {
 *     Plot plot = plotProvider.getPlot(location);
 *     // Feature mit Plot-Support
 * } catch (ProviderFunctionalityNotFoundException e) {
 *     // Feature ohne Plot-Support oder Fallback
 *     logger.warning("Plot feature disabled: " + e.getMessage());
 * }
 * </pre>
 * 
 * @author FallenStar
 * @version 1.0
 */
public class ProviderFunctionalityNotFoundException extends Exception {
    
    private final String providerName;
    private final String functionalityName;
    
    /**
     * Erstellt eine neue Exception.
     * 
     * @param providerName Name des Providers (z.B. "PlotProvider")
     * @param functionalityName Name der fehlenden Funktionalität
     */
    public ProviderFunctionalityNotFoundException(String providerName, 
                                                   String functionalityName) {
        super(String.format("Provider '%s' does not support functionality '%s'", 
                           providerName, functionalityName));
        this.providerName = providerName;
        this.functionalityName = functionalityName;
    }
    
    /**
     * Erstellt eine neue Exception mit zusätzlicher Message.
     * 
     * @param providerName Name des Providers
     * @param functionalityName Name der fehlenden Funktionalität
     * @param additionalInfo Zusätzliche Informationen
     */
    public ProviderFunctionalityNotFoundException(String providerName,
                                                   String functionalityName,
                                                   String additionalInfo) {
        super(String.format("Provider '%s' does not support functionality '%s': %s",
                           providerName, functionalityName, additionalInfo));
        this.providerName = providerName;
        this.functionalityName = functionalityName;
    }
    
    /**
     * @return Name des Providers der die Funktionalität nicht unterstützt
     */
    public String getProviderName() {
        return providerName;
    }
    
    /**
     * @return Name der fehlenden Funktionalität
     */
    public String getFunctionalityName() {
        return functionalityName;
    }
}
