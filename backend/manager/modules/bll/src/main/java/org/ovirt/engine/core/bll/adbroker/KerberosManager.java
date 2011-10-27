package org.ovirt.engine.core.bll.adbroker;

import java.io.File;

import org.jboss.ejb3.annotation.Depends;
import org.jboss.ejb3.annotation.Management;
import org.jboss.ejb3.annotation.Service;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;

import sun.security.krb5.Config;
import sun.security.krb5.KrbException;

/**
 * Manage the container's Kerberos initialization.
 *
 */
@SuppressWarnings("restriction")
@Service
@Depends("jboss.j2ee:ear=engine.ear,jar=engine-bll.jar,name=Backend,service=EJB3")
@Management(KerberosManagerSericeManagmentMBean.class)
public class KerberosManager implements KerberosManagerSericeManagmentMBean {

    private static LogCompat log = LogFactoryCompat.getLog(KerberosManager.class);

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

    /**
     * This method is called upon the bean creation as part
     * of the management Service bean lifecycle.
     */
    public void create() {
        if (!isKerberosAuth()) {
            return;
        }
        String serverHomeDir = System.getProperty("jboss.server.home.dir");
        File krb5File = new File(serverHomeDir, "conf/krb5.conf");
        if (krb5File.exists()) {
            if (log.isDebugEnabled()) {
                log.debug("Loading kerberos settings from " + krb5File.getAbsolutePath());
            }
            System.setProperty("java.security.krb5.conf",
                    krb5File.getAbsolutePath());
        } else {
            log.error("Failed loading kerberos setting. File " + krb5File + " not found.");
        }

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
