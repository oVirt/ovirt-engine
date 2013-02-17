package org.ovirt.engine.core.bll.provider;


public interface ProviderProxy {

    /**
     * Test the connection to the provider.<br>
     * If the connection is unsuccessful, an exception will be thrown.
     */
    void testConnection();
}
