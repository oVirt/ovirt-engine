package org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.kerberos;

import static org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.kerberos.KrbConfCreator.DEFAULT_TKT_ENCTYPES_ARCFOUR_HMAC_MD5;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.util.Scanner;

public class DetectMixedMode {

    /**
     * Open a krb5.conf file and search a property defining the encryption types used for kerberos ticket negotiation.
     * The property is "default_tkt_enctypes = arcfour-hmac-md5".
     *
     * @param krbConfPath
     * @return true - If the property is set and unremarked. false - if the property is remarked or absent
     * @throws FileNotFoundException
     */
    public boolean detect(String krbConfPath) throws FileNotFoundException {
        System.out.println("Searching " + krbConfPath + "\n for property " + DEFAULT_TKT_ENCTYPES_ARCFOUR_HMAC_MD5);
        try (Scanner scanner = new Scanner(new FileInputStream(krbConfPath), Charset.forName("UTF-8").toString())) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.matches(DEFAULT_TKT_ENCTYPES_ARCFOUR_HMAC_MD5)) {
                // Bingo! mixed-mode flag is there.
                    return true;
                } else if (line.matches("#+" + DEFAULT_TKT_ENCTYPES_ARCFOUR_HMAC_MD5)) {
                // Bingo! mixed-mode is remarked, so we have the answer. Leaving the loop.
                    break;
                }
            }
        }
        return false;
    }
}
