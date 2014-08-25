package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.spi.DirectoryManager;
import javax.security.auth.login.Configuration;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.extensions.Base;
import org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.dns.DnsSRVLocator.DnsSRVResult;
import org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.ldap.LdapSRVLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {

    private static boolean firstCallToInit = true;
    private static Logger logger = LoggerFactory.getLogger(Utils.class);

    public static void setDefaults(Properties conf, String domain) {
        {
            Properties defaults = new Properties();
            try (Reader reader = new InputStreamReader(Utils.class.getResourceAsStream("defaults.properties"), Charset.forName("UTF-8"))) {
                defaults.load(reader);
                for (Map.Entry<Object, Object> entry : defaults.entrySet()) {
                    putIfAbsent(conf, (String) entry.getKey(), (String) entry.getValue());
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        refreshLdapServers(conf, domain);

    }

    public static void refreshLdapServers(Properties conf, String domain) {
        LdapSRVLocator locator = new LdapSRVLocator();
        List<String> addresses = new ArrayList<>();
        try {
            if (StringUtils.isBlank(conf.getProperty("config.LdapServers"))) {
                // list of LDAP servers is empty, find LDAP servers using DNS SRV records and convert them
                // into the list of URIs
                DnsSRVResult results = locator.getLdapServers(domain);
                if (results == null || results.getNumOfValidAddresses() == 0) {
                    throw new Exception(String.format("No ldap servers  were found for domain %1$s", domain));
                } else {
                    for (int counter = 0; counter < results.getNumOfValidAddresses(); counter++) {
                        addresses.add(results.getAddresses()[counter]);
                    }
                }
            } else {
                // list of LDAP servers was entered, convert them to URIs
                addresses = Arrays.asList(conf.getProperty("config.LdapServers").split(";"));
            }
            List<String> ldapServers = new ArrayList<>();
            for (String address : addresses) {
                String ldapURI = locator.constructURI("ldap", address, "389").toString();
                ldapServers.add(ldapURI);
            }
            conf.setProperty("config.LdapServers", StringUtils.join(ldapServers, ";"));
        } catch (Exception ex) {
            logger.error("Exception has occurred during refreshing ldap servers. Exception message is: {} ",
                    ex.getMessage());
            if (logger.isDebugEnabled()) {
                logger.debug("", ex);
            }
        }
    }

    private static void putIfAbsent(Properties props, String key, String value) {
        if (!props.containsKey(key)) {
            props.put(key, value);
        }
    }

    public static synchronized void handleApplicationInit(String applicationName) throws Exception {
        if (firstCallToInit) {
            if (applicationName.equals(Base.ApplicationNames.OVIRT_ENGINE_AAA_EXTENSION_TOOL)) {
                Configuration.getConfiguration().refresh();
                DirectoryManager.setObjectFactoryBuilder(new DirectoryContextFactoryBuilder());
            }
            firstCallToInit = false;
        }
        System.setProperty("sun.security.krb5.msinterop.kstring", "true");
    }


}
