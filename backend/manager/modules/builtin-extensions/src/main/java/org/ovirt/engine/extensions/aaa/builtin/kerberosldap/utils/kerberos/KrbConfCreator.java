package org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.kerberos;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.dns.DnsSRVLocator.DnsSRVResult;

/**
 * A tool to create a krb5.conf file from a template using the supplied domain list. For each domain an SRV DNS request
 * will be made to extract the relevant KDC's. The (configurable) output file is a valid krb5.conf to by the ENGINE
 * server.
 */
public class KrbConfCreator {
    protected static final String REALMS_SECTION = "#realms";
    protected static final String DOMAIN_REALM_SECTION = "#domain_realm";
    public static final String DEFAULT_TKT_ENCTYPES_ARCFOUR_HMAC_MD5 = "default_tkt_enctypes = arcfour-hmac-md5";
    public static final String DNS_LOOKUP_REALM = "dns_lookup_realm";
    public static final String DNS_LOOKUP_KDC = "dns_lookup_kdc";
    private static final String DEFAULT_REALM = "default_realm";
    public final static String seperator = System.getProperty("line.separator");
    protected InputStream sourceFile;
    private List<String> realms;
    private boolean useDnsLookup;
    private Map<String, List<String>> ldapServersPerGSSAPIDomains;
    private String domainRealmMappingFile;

    private final static Logger log = Logger.getLogger(KrbConfCreator.class);

    public KrbConfCreator(String domains,
            boolean useDnsLookup,
            Map<String, List<String>> ldapServersPerGSSAPIDomains,
            String domainRealmMappingFile) throws Exception {
        this.useDnsLookup = useDnsLookup && ( ldapServersPerGSSAPIDomains == null || ldapServersPerGSSAPIDomains.size() == 0 );
        this.ldapServersPerGSSAPIDomains = ldapServersPerGSSAPIDomains;
        this.domainRealmMappingFile = domainRealmMappingFile;
        loadSourceFile();
        extractRealmsFromDomains(domains);
    }

    private void loadSourceFile() throws FileNotFoundException {
        String template = "krb5.conf.template";
        sourceFile = KrbConfCreator.class.getClassLoader().getResourceAsStream(template);
        if (sourceFile == null) {
            throw new FileNotFoundException(template + " was not found");
        }
        log.debug("loaded template kr5.conf file " + template);
    }

    public StringBuffer parse(String mixedMode) throws AuthenticationException {
        StringBuffer sb = new StringBuffer();
        Scanner scanner = new Scanner(sourceFile, Charset.forName("UTF-8").toString());

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();

            // set the first domain in the list to be the default REALM
            if (line.matches("#" + DEFAULT_REALM + ".*")) {
                line = DEFAULT_REALM + " = " + realms.get(0);
            }

            if (line.matches("#" + DNS_LOOKUP_KDC + ".*")) {
                line = DNS_LOOKUP_KDC + " = " + Boolean.toString(useDnsLookup);
            }
            if (line.matches("#" + DNS_LOOKUP_REALM + ".*")) {
                line = DNS_LOOKUP_REALM + " = " + Boolean.toString(useDnsLookup);
            }


            // Active directory in 2003 mode hack + IPA
            if (line.matches(".*" + DEFAULT_TKT_ENCTYPES_ARCFOUR_HMAC_MD5)) {
                if (mixedMode != null && (mixedMode.equalsIgnoreCase("y") || mixedMode.equalsIgnoreCase("yes"))) {
                    line = line.replace("#", "");
                    log.debug("setting default_tkt_enctypes ");
                }
            }

            // populate realms by domains
            if (!useDnsLookup && line.matches(REALMS_SECTION)) {
                line = appendRealms(realms);
                log.debug("setting realms");
            }

            // populate domain realms by domains
            //In case useDnsLookup is true
            if (!(useDnsLookup  && realms.size() == 1) && line.matches(DOMAIN_REALM_SECTION)) {
                line = appendDomainRealms(realms);
                log.debug("setting domain realm");
            }

            sb.append(line + seperator);
        }

        return sb;
    }

    private void extractRealmsFromDomains(String domains) {
        String[] realmArray = domains.split(",", -1);
        List<String> realms = new ArrayList<String>();
        for (String realm : realmArray) {
            realms.add(realm.toUpperCase().trim());
        }
        this.realms = realms;
    }

    public void toFile(String krb5ConfPath, StringBuffer sb) throws FileNotFoundException, IOException {
        try (FileOutputStream fos = new FileOutputStream(krb5ConfPath)) {
            fos.write(sb.toString().getBytes());
        }
    }

    private String appendRealms(List<String> realms) throws AuthenticationException {
        StringBuffer text = new StringBuffer(" [realms]");
        KDCLocator locator = new KDCLocator();
        for (String realm : realms) {
            try {
                List<String> ldapServers = ldapServersPerGSSAPIDomains.get(realm.toLowerCase());
                if (ldapServers == null || ldapServers.size() == 0) {
                    DnsSRVResult kdc = locator.getKdc(KDCLocator.TCP, realm);
                    String[] addresses = kdc.getAddresses();
                    if (addresses.length == 0) { // kdc not found for this realm
                        throw new IllegalArgumentException(InstallerConstants.ERROR_PREFIX
                                + " there are no KDCs for for realm " + realm +
                                ". Realm name may not be valid");
                    }
                    ldapServers = Arrays.asList(addresses);
                }

                text.append(seperator + "\t" + realm + " = {" + seperator); // output REALM = {
                for (String address : ldapServers) {
                    text.append("\t\tkdc = " + address + seperator); // output kdc = address
                }
                text.append("\t}" + seperator); // append line}

            } catch (Exception ex) {
                AuthenticationResult result = KerberosUtils.convertDNSException(ex);
                System.out.println(InstallerConstants.ERROR_PREFIX + result.getDetailedMessage() + "."
                        + getProblematicRealmExceptionMsg(realm));
                throw new AuthenticationException(result);
            }
        }
        return text.toString();
    }

    // The domain realm section is the following section
    // [domain_realm]
    // .example.com = EXAMPLE.COM
    // .second.example.com = SECOND.EXAMPLE.COM
    private String appendDomainRealms(List<String> realms) throws AuthenticationException {
        StringBuffer text = new StringBuffer(" [domain_realm]\n");
        if (!domainRealmMappingFileExits()) {
            for (String realm : realms) {
                text.append("\t" + realm.toLowerCase() + " = " + realm.toUpperCase() + "\n");
            }
        } else {
            // Fill in [domain_realm] section from the provided file at engine-manage-domains.conf
            // This can be useful in case the realm is not an upper case of the domain
            try (BufferedReader reader =
                    new BufferedReader(new InputStreamReader(new FileInputStream(domainRealmMappingFile),
                            java.nio.charset.Charset.defaultCharset().displayName()))) {
                while (true) {
                    String readLine = reader.readLine();
                    if (readLine == null) {
                        break;
                    }
                    text.append("\t").append(readLine).append("\n");
                }

            } catch (FileNotFoundException e) {
                // This exception should not really happen as we check that the file exists at
                // domainRealmMappingFileExits()

            } catch (IOException e) {
            }
        }
        return text.toString();
    }

    private boolean domainRealmMappingFileExits() {
        if (StringUtils.isEmpty(domainRealmMappingFile)) {
            return false;
        }
        File f = new File(domainRealmMappingFile);
        return f.exists();
    }

    private String getProblematicRealmExceptionMsg(String realm) {
        return (realm != null) ? " Problematic domain is: " + realm.toLowerCase() : "";
    }
}
