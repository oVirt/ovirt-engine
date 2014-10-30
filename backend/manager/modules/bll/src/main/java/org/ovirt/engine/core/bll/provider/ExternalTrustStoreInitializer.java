package org.ovirt.engine.core.bll.provider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;

import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExternalTrustStoreInitializer {

    private static final Logger log = LoggerFactory.getLogger(ExternalTrustStoreInitializer.class);

    public static KeyStore getTrustStore() {
        try {
            KeyStore ks = KeyStore.getInstance(EngineLocalConfig.getInstance().getExternalProvidersTrustStoreType());
            if (!EngineLocalConfig.getInstance().getExternalProvidersTrustStore().exists()) {
                ks.load(null);
            } else {
                try (FileInputStream ksFileInputStream =
                        new FileInputStream(EngineLocalConfig.getInstance().getExternalProvidersTrustStore())) {
                    ks.load(ksFileInputStream, EngineLocalConfig.getInstance()
                            .getExternalProvidersTrustStorePassword()
                            .toCharArray());
                }
            }
            return ks;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void addCertificate(Certificate cert) throws CertificateEncodingException, KeyStoreException {
        KeyStore keystore = getTrustStore();
        keystore.setCertificateEntry(Integer.toString(cert.hashCode()), cert);

        File trustStoreFile = EngineLocalConfig.getInstance().getExternalProvidersTrustStore();
        File tempFile = null;
        try {
            tempFile = File.createTempFile("keystore", ".tmp", trustStoreFile.getParentFile());
            try (FileOutputStream out = new FileOutputStream(tempFile)) {
                keystore.store(out, EngineLocalConfig.getInstance()
                        .getExternalProvidersTrustStorePassword()
                        .toCharArray());
            }
            if (!tempFile.renameTo(trustStoreFile.getAbsoluteFile())) {
                throw new RuntimeException(String.format("Failed to save trust store to file %1$s",
                        trustStoreFile.getAbsolutePath()));
            }
            tempFile = null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (tempFile != null && !tempFile.delete()) {
                log.error("Cannot delete '{}'", tempFile.getAbsolutePath());
            }
        }
    }
}
