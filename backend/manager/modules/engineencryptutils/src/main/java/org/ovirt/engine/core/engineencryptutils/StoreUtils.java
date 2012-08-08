package org.ovirt.engine.core.engineencryptutils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.Vector;

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
        FileInputStream input = null;
        try {
            KeyStore ks = KeyStore.getInstance("jks");
            input = new FileInputStream(keystore);
            ks.load(input, password.toCharArray());
            Key key = ks.getKey(alias, password.toCharArray());
            bReturn = key.getEncoded();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    //ignore
                }
            }
        }
        return bReturn;
    }

    private static String pubkey2ssh(String keystore, String password, String alias) {
        String bReturn = null;
        FileInputStream input = null;
        try {
            // Load the key store:
            KeyStore ks = KeyStore.getInstance("jks");
            input = new FileInputStream(keystore);
            ks.load(input, password.toCharArray());

            // Find the public key:
            Key key = ks.getKey(alias, password.toCharArray());
            if (key instanceof PrivateKey) {
                Certificate cert = ks.getCertificate(alias);
                key = cert.getPublicKey();
            }

            // Generate and return the SSH key string:
            return OpenSSHUtils.getKeyString((PublicKey) key, alias);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    //ignore
                }
            }
        }
        return bReturn;
    }

    private static void printUsage() {
        System.out.println("Usage:");
        System.out.println("StoreUtils -[enc|dec] -store=path/to/keystore-file -pass=keystore-pass -string='string to be enc/decrypted' [-alias='cert alias']");
        System.out.println("StoreUtils -pubkey2ssh -store=path/to/keystore-file -pass=keystore-pass -alias='cert alias'");
    }

    private static boolean validate(CLIParse parser) {
        boolean fOK = true;
        if ((!parser.hasArg("enc")) && (!parser.hasArg("dec")) && (!parser.hasArg("pvk")) && (!parser.hasArg("pubkey2ssh"))) {
            System.out.println("What do you wish me to do? -please specify -enc, -dec or -pubkey2ssh");
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
        int exitCode = 0;
        try {
            CLIParse parser = new CLIParse(args);
            if (parser.hasArg("?") || parser.hasArg("help") || args.length == 0) {
                printUsage();
                return;
            }

            if (!validate(parser)) {
                exitCode = 1;
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
            } else if (parser.hasArg("pubkey2ssh")) {
                System.out.print(
                        pubkey2ssh(
                                parser.getArg("store"),
                                parser.getArg("pass"),
                                alias
                        )
                        );
            }
        } catch (Exception e) {
            System.out.println("Operation failed!");
            exitCode = 1;
        }
        finally {
            System.exit(exitCode);
        }
    }
}
