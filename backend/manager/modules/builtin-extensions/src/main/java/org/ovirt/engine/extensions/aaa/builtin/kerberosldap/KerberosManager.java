package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage the container's Kerberos initialization.
 *
 */
public class KerberosManager {

    private static final Logger log = LoggerFactory.getLogger(KerberosManager.class);
    private static volatile KerberosManager instance = null;


    public static KerberosManager getInstance() {
        if (instance == null) {
            synchronized (KerberosManager.class) {
                if (instance == null) {
                    instance = new KerberosManager();
                }
            }
        }
        return instance;
    }

    private KerberosManager() {
        String engineEtc = System.getenv("ENGINE_ETC");
        if (engineEtc == null) {
            engineEtc = "/etc/ovirt-engine";
        }

        File krb5File = new File(engineEtc, "krb5.conf");
        if (!krb5File.exists()) {
            String msg = String.format("Failed loading kerberos settings from File %1$s.", krb5File.getAbsolutePath());
            log.error(msg);
            throw new RuntimeException(msg);
        }

        System.setProperty("java.security.krb5.conf", krb5File.getAbsolutePath());
        System.setProperty("sun.security.krb5.msinterop.kstring", "true");
    }

}
