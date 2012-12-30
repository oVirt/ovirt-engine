package org.ovirt.engine.core.utils.kerberos;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.log4j.Logger;
import org.ovirt.engine.core.utils.CLIParser;
import org.ovirt.engine.core.utils.dns.DnsSRVLocator.DnsSRVResult;

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
    private CLIParser cliParser;
    private final String usage =
            " Usage: \n\t-domains=<comma seperated list>\n\t-destinationFile<file>" +
                    "\n\t-m mixed mode. Will add a flag to support AD in 2003/2008 mixed mode will be added";
    private boolean useDnsLookup;
    private final static Logger log = Logger.getLogger(KrbConfCreator.class);

    public KrbConfCreator(String... args) throws Exception {
        parseOptions(args);
        loadSourceFile();
        extractRealmsFromDomains();
    }

    public KrbConfCreator(String domains, boolean useDnsLookup) throws Exception {
        this.useDnsLookup = useDnsLookup;
        loadSourceFile();
        extractRealmsFromDomains(domains);
    }

    /**
     * @param args
     *            arguments will be parsed as follow -domains="domainA,domainB," domains list
     *            -destinationFile="path/to/destination/file -e encryption mode. When exist a flag to support AD in
     *            2003/2008 mixed mode
     * @return boolean true if to continue to next stage
     */
    private void parseOptions(String[] args) {
        cliParser = new CLIParser(args);
        if (!cliParser.hasArg(Arguments.domains.name()) ||
                !cliParser.hasArg(Arguments.krb5_conf_path.name())) {
            System.out.println("Missing arguments\nusage: " + usage);
            System.exit(1);

        }
    }

    private void loadSourceFile() throws FileNotFoundException {
        String template = "krb5.conf.template";
        sourceFile = KrbConfCreator.class.getClassLoader().getResourceAsStream(template);
        if (sourceFile == null) {
            throw new FileNotFoundException(template + " was not found");
        }
        log.debug("loaded template kr5.conf file " + template);
    }

    public StringBuffer parse() throws AuthenticationException {
        String mixedMode = "no";
        if (cliParser.hasArg(Arguments.mixed_mode.name())) {
            mixedMode = cliParser.getArg(Arguments.mixed_mode.name());
        }
        return parse(mixedMode);
    }

    public StringBuffer parse(String mixedMode) throws AuthenticationException {
        StringBuffer sb = new StringBuffer();
        Scanner scanner = new Scanner(sourceFile);

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
            if (!useDnsLookup && line.matches(DOMAIN_REALM_SECTION)) {
                line = appendDomainRealms(realms);
                log.debug("setting domain realm");
            }

            sb.append(line + seperator);
        }

        return sb;
    }

    private void extractRealmsFromDomains() {
        String domains = cliParser.getArg(Arguments.domains.name());
        extractRealmsFromDomains(domains);
        String[] realmArray = domains.split(",", -1);
        List<String> realms = new ArrayList<String>();
        for (String realm : realmArray) {
            realms.add(realm.toUpperCase());
        }
        this.realms = realms;
    }

    private void extractRealmsFromDomains(String domains) {
        String[] realmArray = domains.split(",", -1);
        List<String> realms = new ArrayList<String>();
        for (String realm : realmArray) {
            realms.add(realm.toUpperCase().trim());
        }
        this.realms = realms;
    }

    public void toFile(StringBuffer sb) throws FileNotFoundException, IOException {
        File outputConfig;
        if (cliParser.hasArg(Arguments.krb5_conf_path.name())) {
            outputConfig = new File(cliParser.getArg(Arguments.krb5_conf_path.name()));
        } else {
            outputConfig =
                    new File(System.getProperty("java.io.tmpdir") + File.separator + InstallerConstants.KRB_FILE_NAME);
        }
        toFile(outputConfig.getAbsolutePath(), sb);
    }

    public void toFile(String krb5ConfPath, StringBuffer sb) throws FileNotFoundException, IOException {

        FileOutputStream fos = new FileOutputStream(krb5ConfPath);
        fos.write(sb.toString().getBytes());
        fos.close();
    }

    private String appendRealms(List<String> realms) throws AuthenticationException {
        StringBuffer text = new StringBuffer(" [realms]");
        KDCLocator locator = new KDCLocator();
        for (String realm : realms) {
            DnsSRVResult kdc;
            try {
                kdc = locator.getKdc(KDCLocator.TCP, realm);
                String[] addresses = kdc.getAddresses();
                if (addresses.length == 0) { // kdc not found for this realm
                    throw new IllegalArgumentException(InstallerConstants.ERROR_PREFIX
                            + " there are no KDCs for for realm " + realm +
                            ". Realm name may not be valid");
                }
                text.append(seperator + "\t" + realm + " = {" + seperator); // output REALM = {
                for (String address : addresses) {
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
        for (String realm : realms) {
            text.append("\t" + realm.toLowerCase() + " = " + realm.toUpperCase() + "\n");
        }
        return text.toString();
    }

    private String getProblematicRealmExceptionMsg(String realm) {
        return (realm != null) ? " Problematic domain is: " + realm.toLowerCase() : "";
    }

    public static void main(String[] args) throws FileNotFoundException {
        try {
            KrbConfCreator kerbParser = new KrbConfCreator(args);
            StringBuffer buffer = kerbParser.parse();
            kerbParser.toFile(buffer);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            if (e instanceof AuthenticationException) {
                System.exit(((AuthenticationException) e).getAuthResult().getExitCode());
            }
            System.exit(1);
        }
    }

    private enum Arguments {
        domains,
        krb5_conf_path,
        mixed_mode
    }
}
