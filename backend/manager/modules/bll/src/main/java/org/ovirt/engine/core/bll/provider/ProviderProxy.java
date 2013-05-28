package org.ovirt.engine.core.bll.provider;

import java.security.cert.Certificate;
import java.util.List;


public interface ProviderProxy {

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
}
