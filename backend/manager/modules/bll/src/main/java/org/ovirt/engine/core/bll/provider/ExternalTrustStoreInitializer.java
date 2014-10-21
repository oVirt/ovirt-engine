package org.ovirt.engine.core.bll.provider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;

import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExternalTrustStoreInitializer {

    private static final Logger log = LoggerFactory.getLogger(ExternalTrustStoreInitializer.class);

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
                log.error("Creation of the external trust store failed: {}", e.getMessage());
                log.debug("Exception", e);
            }
        }
    }

    public static KeyStore getTrustStore() {
        try (InputStream in = new FileInputStream(getTrustStorePath())) {
            // TODO: do not use password of other store
            String password = EngineLocalConfig.getInstance().getPKITrustStorePassword();
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(in, password.toCharArray());
            return ks;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
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
