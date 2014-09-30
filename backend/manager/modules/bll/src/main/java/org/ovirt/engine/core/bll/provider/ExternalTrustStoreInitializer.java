package org.ovirt.engine.core.bll.provider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.security.KeyStore;

import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class ExternalTrustStoreInitializer {

    private static final Log log = LogFactory.getLog(ExternalTrustStoreInitializer.class);

    private static String getTrustStorePath() {
        File varDir = EngineLocalConfig.getInstance().getVarDir();
        return varDir + "/" + "external_truststore";
    }

    public static void init() {
        File trustStoreFile = new File(getTrustStorePath());
        if (!trustStoreFile.exists()) {
            try (OutputStream out = new FileOutputStream(trustStoreFile)){
                String password = EngineLocalConfig.getInstance().getPKITrustStorePassword();
                KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                // Passing null stream will create a new empty trust store
                trustStore.load(null, password.toCharArray());
                trustStore.store(out, password.toCharArray());
            } catch (Exception e) {
                log.error("Creation of the external trust store failed.", e);
            }
        }
    }

    public static KeyStore getTrustStore() {
        KeyStore ks = null;
        try {
            ks = EngineLocalConfig.getInstance().getExternalProvidersTrustStore().exists() ?
                            KeyStore.getInstance(EngineLocalConfig.getInstance().getExternalProvidersTrustStoreType())
                            : null;
            if (ks != null) {
                try (FileInputStream ksFileInputStream =
                        new FileInputStream(EngineLocalConfig.getInstance().getExternalProvidersTrustStore())) {
                        ks.load(ksFileInputStream, EngineLocalConfig.getInstance()
                            .getExternalProvidersTrustStorePassword()
                            .toCharArray());
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return ks;

    }

    public static void setTrustStore(KeyStore keystore) {
        try (OutputStream out = new FileOutputStream(getTrustStorePath())) {
            // TODO: do not use password of other store
            String password = EngineLocalConfig.getInstance().getPKITrustStorePassword();
            keystore.store(out, password.toCharArray());
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
