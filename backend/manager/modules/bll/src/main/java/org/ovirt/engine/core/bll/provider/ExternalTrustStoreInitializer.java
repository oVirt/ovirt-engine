package org.ovirt.engine.core.bll.provider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;

import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class ExternalTrustStoreInitializer {

    private static Log log = LogFactory.getLog(ExternalTrustStoreInitializer.class);
    private static final String FILE_URL_PREFIX = "file://";

    public static String getTrustStorePath() {
        File varDir = EngineLocalConfig.getInstance().getVarDir();
        return varDir + "/" + "external_truststore";
    }

    public static URL getTrustStoreUrl() throws MalformedURLException {
        return new URL(FILE_URL_PREFIX + getTrustStorePath());
    }

    public static String getTrustStorePassword() {
        return EngineLocalConfig.getInstance().getPKITrustStorePassword();
    }

    public static void init() {
        File trustStoreFile = new File(getTrustStorePath());
        if (!trustStoreFile.exists()) {
            try (OutputStream out = new FileOutputStream(trustStoreFile)){
                KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                // Passing null stream will create a new empty trust store
                trustStore.load(null, getTrustStorePassword().toCharArray());
                trustStore.store(out, getTrustStorePassword().toCharArray());
            } catch (Exception e) {
                log.error("Creation of the external trust store failed.",e);
            }
        }
    }
}
