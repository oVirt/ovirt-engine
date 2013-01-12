package org.ovirt.engine.core.bll.adbroker;

import java.io.File;

import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

import sun.security.krb5.Config;
import sun.security.krb5.KrbException;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.DependsOn;
import javax.ejb.Startup;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;

/**
 * Manage the container's Kerberos initialization.
 *
 */
// Here we use a Singleton bean
// The @Startup annotation is to make sure the bean is initialized on startup.
// @ConcurrencyManagement - we use bean managed concurrency:
// Singletons that use bean-managed concurrency allow full concurrent access to all the
// business and timeout methods in the singleton.
// The developer of the singleton is responsible for ensuring that the state of the singleton is synchronized across all clients.
// The @DependsOn annotation is in order to make sure it is started after the Backend bean is initialized
@SuppressWarnings("restriction")
@Singleton
@Startup
@DependsOn("Backend")
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class KerberosManager implements KerberosManagerSericeManagmentMBean {

    private static Log log = LogFactory.getLog(KerberosManager.class);

    private boolean isKerberosAuth() {
        boolean isKerberosAuth = false;
        String authMethod = org.ovirt.engine.core.common.config.Config.<String> GetValue(ConfigValues.AuthenticationMethod);
        String domainName = org.ovirt.engine.core.common.config.Config.<String> GetValue(ConfigValues.DomainName);
        String ldapSecurityAuthentication = org.ovirt.engine.core.common.config.Config.<String> GetValue(ConfigValues.LDAPSecurityAuthentication);

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

    @PostConstruct
    public void postConstruct() {
        create();
    }

    /**
     * This method is called upon the bean creation as part
     * of the management Service bean lifecycle.
     */
    public void create() {
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
        System.setProperty("sun.security.krb5.msinterop.kstring","true");
    }

    @SuppressWarnings("restriction")
    @Override
    public void refresh() throws KrbException {
        if (!isKerberosAuth()) {
            return;
        }
        log.info("Refreshing kerberos configuration");
        Config.refresh();

    }

}
