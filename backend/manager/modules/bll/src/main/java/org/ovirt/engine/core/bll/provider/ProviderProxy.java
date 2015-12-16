package org.ovirt.engine.core.bll.provider;

import java.security.cert.Certificate;
import java.util.List;


public interface ProviderProxy<T extends ProviderValidator> {

    /**
     * Test the connection to the provider.<br>
     * If the connection is unsuccessful, an exception will be thrown.
     */
    void testConnection();

    /**
     * Get the certificate chain of the provider.<br>
     * Useful when the provider is secured.
     * @return List of Certificate objects
     */
    List<? extends Certificate> getCertificateChain();

    /**
     * Callback executed when the provider is added.<br>
     * Useful to add provider-specific operations when the provider is added.
     */
    void onAddition();

    /**
     * Callback executed when the provider is modified.<br>
     * Useful to add provider-specific operations when the provider is modified.
     */
    void onModification();

    /**
     * Callback executed when the provider is removed.<br>
     * Useful to add provider-specific operations when the provider is removed.
     */
    void onRemoval();

    /**
     * Gets a specific validator for the provider
     */
    T getProviderValidator();

}
