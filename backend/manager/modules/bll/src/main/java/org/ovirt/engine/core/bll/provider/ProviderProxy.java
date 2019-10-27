package org.ovirt.engine.core.bll.provider;

import java.security.cert.Certificate;
import java.util.List;

import org.ovirt.engine.core.bll.context.CommandContext;

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
    default List<? extends Certificate> getCertificateChain() {
        return null;
    }

    /**
     * Callback executed when the provider is added.<br>
     * Useful to add provider-specific operations when the provider is added.
     */
    default void onAddition() {
    }

    /**
     * Callback executed when the provider is modified.<br>
     * Useful to add provider-specific operations when the provider is modified.
     */
    default void onModification() {
    }

    /**
     * Callback executed when the provider is removed.<br>
     * Useful to add provider-specific operations when the provider is removed.
     */
    default void onRemoval() {
    }

    /**
     * Gets a specific validator for the provider
     */
    T getProviderValidator();

    /**
     * Sets a context for the provider proxy backend actions
     */
    default void setCommandContext(CommandContext context) {
    }
}
