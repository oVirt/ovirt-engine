package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import java.io.File;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

/**
 * Manage the container's Kerberos initialization.
 *
 */
public class KerberosManager {

    private static Log log = LogFactory.getLog(KerberosManager.class);
    private static volatile KerberosManager instance = null;

    private boolean isKerberosAuth() {
        boolean isKerberosAuth = false;
        String authMethod = org.ovirt.engine.core.common.config.Config.<String> getValue(ConfigValues.AuthenticationMethod);
        String domainName = org.ovirt.engine.core.common.config.Config.<String> getValue(ConfigValues.DomainName);
        String ldapSecurityAuthentication = org.ovirt.engine.core.common.config.Config.<String> getValue(ConfigValues.LDAPSecurityAuthentication);

        if (authMethod.equalsIgnoreCase("LDAP")) {
            // If there are domains then we need to load the Kerberos configuration in case the LDAP security
            // authentication entry contains
            // GSSAPI explicitly, or implicitly (if empty)
            if (!domainName.isEmpty()) {
                if (ldapSecurityAuthentication.isEmpty() || ldapSecurityAuthentication.toUpperCase().contains("GSSAPI")) {
                    isKerberosAuth = true;
                }
            }
        }
        return isKerberosAuth;
    }

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
        if (!isKerberosAuth()) {
            return;
        }
        String engineEtc = System.getenv("ENGINE_ETC");
        if (engineEtc == null) {
            engineEtc = "/etc/ovirt-engine";
        }
        File krb5File = new File(engineEtc, "krb5.conf");
        if (krb5File.exists()) {
            if (log.isDebugEnabled()) {
                log.debug("Loading kerberos settings from " + krb5File.getAbsolutePath());
            }
            System.setProperty("java.security.krb5.conf",
                    krb5File.getAbsolutePath());
        } else {
            log.error("Failed loading kerberos setting. File " + krb5File + " not found.");
        }
        System.setProperty("sun.security.krb5.msinterop.kstring", "true");
    }

}
