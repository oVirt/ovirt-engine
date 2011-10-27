package org.ovirt.engine.core.utils.kerberos;

import static org.ovirt.engine.core.utils.kerberos.KrbConfCreator.DEFAULT_TKT_ENCTYPES_ARCFOUR_HMAC_MD5;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
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
        InputStream sourceFile = new FileInputStream(krbConfPath);
        System.out.println("Searching " + krbConfPath + "\n for property " + DEFAULT_TKT_ENCTYPES_ARCFOUR_HMAC_MD5);
        Scanner scanner = new Scanner(sourceFile);

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
        return false;
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Error: Missing krb5.conf file argument");
            System.out.println("Usage: " + DetectMixedMode.class.getName() + " krb5.conf");
            System.exit(1);
        }
        DetectMixedMode d = new DetectMixedMode();
        String krbConfPath = args[0];
        try {
            System.out.print(d.detect(krbConfPath) ? "yes" : "no");
        } catch (FileNotFoundException e) {
            System.out.println("Error: file " + krbConfPath + " not found");
            System.exit(1);
        }
    }
}
