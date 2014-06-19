package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.spi.DirectoryManager;
import javax.security.auth.login.Configuration;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.extensions.Base;
import org.ovirt.engine.core.ldap.LdapSRVLocator;
import org.ovirt.engine.core.utils.dns.DnsSRVLocator.DnsSRVResult;

public class Utils {

    private static boolean firstCallToInit = true;

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
        if (StringUtils.isBlank(conf.getProperty("config.LdapServers"))) {
            try {

                LdapSRVLocator locator = new LdapSRVLocator();
                DnsSRVResult results = locator.getLdapServers(domain);
                if (results == null || results.getNumOfValidAddresses() == 0) {
                    throw new Exception(String.format("No ldap servers  were found for domain %1$s", domain));
                } else {
                    List<String> ldapServers = new ArrayList<>();
                    for (int counter = 0; counter < results.getNumOfValidAddresses(); counter++) {
                        String address = results.getAddresses()[counter];
                        String ldapURI = locator.constructURI("ldap", address, "389").toString();
                        ldapServers.add(ldapURI);
                    }
                    conf.setProperty("config.LdapServers", StringUtils.join(ldapServers, ","));
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
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
