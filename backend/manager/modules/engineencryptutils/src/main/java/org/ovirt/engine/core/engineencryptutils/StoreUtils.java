package org.ovirt.engine.core.engineencryptutils;

import java.util.Vector;
import java.util.HashMap;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.Key;

public class StoreUtils {

    private static class CLIParse {
        private Vector argv = new Vector();
        private HashMap argsMap = new HashMap();
        private int argvIndex = 0;

        public CLIParse(String[] args) {
            for (int i = 0; i < args.length; i++) {
                if (args[i].startsWith("-")) {
                    int ix = args[i].indexOf("=");
                    String key = (ix > 0) ? args[i].substring(1, ix) : args[i].substring(1);
                    String value = (ix > 0) ? args[i].substring(ix + 1) : "";
                    argsMap.put(key.toLowerCase(), value);
                } else {
                    argv.addElement(args[i]);
                }
            }
        }

        public boolean hasArg(String arg) {
            return argsMap.containsKey(arg.toLowerCase());
        }

        public String getArg(String arg) {
            return (String) argsMap.get(arg.toLowerCase());
        }

        public String nextParam() {
            String strReturn = null;
            if (argvIndex < argv.size()) {
                strReturn = (String) argv.elementAt(argvIndex++);
            }
            return strReturn;
        }
    }

    private static byte[] pvk(String keystore, String password, String alias) {
        byte[] bReturn = null;
        try {
            KeyStore ks = KeyStore.getInstance("jks");
            ks.load(new FileInputStream(keystore), password.toCharArray());
            Key key = ks.getKey(alias, password.toCharArray());
            bReturn = key.getEncoded();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bReturn;
    }

    private static void printUsage() {
        System.out.println("Usage:");
        System.out.println("EncryptionUtils -[enc|dec] -store=path/to/keystore-file -pass=keystore-pass -string='string to be enc/decrypted' [-alias='cert alias']");
    }

    private static boolean validate(CLIParse parser) {
        boolean fOK = true;
        if ((!parser.hasArg("enc")) && (!parser.hasArg("dec")) && (!parser.hasArg("pvk"))) {
            System.out.println("What do you wish me to do? -please specify -enc or -dec.");
            fOK = false;
        }

        if ((parser.hasArg("enc") || parser.hasArg("dec")) &&
                (!parser.hasArg("string"))) {
            System.out.println("Can't find a string to work with :( -please specify -string='something'.");
            fOK = false;
        }

        if ((!parser.hasArg("store")) || (!new File(parser.getArg("store")).exists())) {
            System.out.println("Can't find a keystore to work with :( -please specify -store with the correct keystore path.");
            fOK = false;
        }

        if (!parser.hasArg("pass")) {
            System.out.println("Can't find a keystore pass :( -please specify -pass with the correct keystore password.");
            fOK = false;
        }

        if (!fOK) {
            printUsage();
        }

        return fOK;
    }

    public static void main(String[] args) {
        try {
            CLIParse parser = new CLIParse(args);
            if (parser.hasArg("?") || parser.hasArg("help") || args.length == 0) {
                printUsage();
                return;
            }

            if (!validate(parser)) {
                return;
            }

            String alias = "engine";
            if (parser.hasArg("alias")) {
                alias = parser.getArg("alias");
            }

            if (parser.hasArg("enc")) {
                System.out.println(
                        EncryptionUtils.encrypt(
                                parser.getArg("string"),
                                parser.getArg("store"),
                                parser.getArg("pass"),
                                alias
                                ).trim().replace("\r\n", "")
                        );
            } else if (parser.hasArg("dec")) {
                System.out.println(
                        EncryptionUtils.decrypt(
                                parser.getArg("string"),
                                parser.getArg("store"),
                                parser.getArg("pass"),
                                alias
                                )
                        );
            } else if (parser.hasArg("pvk")) {
                System.out.write(
                        pvk(
                                parser.getArg("store"),
                                parser.getArg("pass"),
                                alias
                        )
                        );
            }
        } catch (Exception e) {
            System.out.println("Operation failed!");
        }
    }
}
