package org.ovirt.engine.core.cryptotool;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.ovirt.engine.core.uutils.cli.parser.ArgumentsParser;
import org.ovirt.engine.core.uutils.crypto.EnvelopeEncryptDecrypt;
import org.ovirt.engine.core.uutils.crypto.EnvelopePBE;

public class Main {

    private static final Map<String, String> substitutions = new HashMap<>();

    private static class ExitException extends RuntimeException {
        private int exitCode;

        public ExitException() {
            this(0);
        }

        public ExitException(int exitCode) {
            this(null, exitCode);
        }

        public ExitException(String msg, int exitCode) {
            super(msg);
            this.exitCode = exitCode;
        }

        public int getExitCode() {
            return exitCode;
        }
    }

    @FunctionalInterface
    private interface Logic {
        void execute(Map<String, Object> argMap) throws IOException, GeneralSecurityException;
    }

    private enum Action {
        PBE_ENCODE(
            argMap -> System.out.println(
                EnvelopePBE.encode(
                    (String)argMap.get("algorithm"),
                    (Integer)argMap.get("key-size"),
                    (Integer)argMap.get("iterations"),
                    null,
                    getPassword("Password: ", (String)argMap.get("password"))
                )
            )
        ),
        PBE_CHECK(
            argMap -> {
                if (
                    !EnvelopePBE.check(
                        new String(readStream(System.in), StandardCharsets.UTF_8),
                        getPassword("Password: ", (String)argMap.get("password"))
                    )
                ) {
                    System.err.println("FAILED");
                    throw new ExitException("Check failed", 1);
                }
            }
        ),
        ENC_ENCODE(
            argMap -> {
                try(InputStream in = new FileInputStream((String)argMap.get("certificate"))) {
                    System.out.println(
                        EnvelopeEncryptDecrypt.encrypt(
                            (String)argMap.get("algorithm"),
                            (Integer)argMap.get("key-size"),
                            CertificateFactory.getInstance("X.509").generateCertificate(in),
                            (Integer)argMap.get("block-size"),
                            readStream(System.in)
                        )
                    );
                }
            }
        ),
        ENC_DECODE(
            argMap -> {
                String keystorePassword = getPassword("Key store password: ", (String)argMap.get("keystore-password"));
                System.out.write(
                    EnvelopeEncryptDecrypt.decrypt(
                        getPrivateKeyEntry(
                            getKeyStore(
                                (String)argMap.get("keystore-type"),
                                (String)argMap.get("keystore"),
                                keystorePassword
                            ),
                            (String)argMap.get("keystore-alias"),
                                argMap.get("key-password") != null ?
                                getPassword("Key password: ", (String)argMap.get("key-password")) :
                                keystorePassword
                        ),
                        new String(readStream(System.in), StandardCharsets.UTF_8)
                    )
                );
            }
        );

        private Logic logic;

        private Action(Logic logic) {
            this.logic = logic;
        }

        void execute(Properties props, List<String> cmdArgs) throws IOException, GeneralSecurityException {
            ArgumentsParser parser = new ArgumentsParser(props, cmdArgs.remove(0));
            parser.getSubstitutions().putAll(substitutions);
            parser.parse(cmdArgs);
            Map<String, Object> argMap = parser.getParsedArgs();

            if((Boolean)argMap.get("help")) {
                System.out.format("Usage: %s", parser.getUsage());
                throw new ExitException("Help", 0);
            }
            if(!parser.getErrors().isEmpty()) {
                for(Throwable t : parser.getErrors()) {
                    System.err.format("FATAL: %s%n", t.getMessage());
                }
                throw new ExitException("Parsing error", 1);
            }
            logic.execute(argMap);
        }
    };

    private static String PROGRAM_NAME = System.getProperty("org.ovirt.engine.cryptotool.core.programName");
    private static String PACKAGE_NAME = System.getProperty("org.ovirt.engine.cryptotool.core.packageName");
    private static String PACKAGE_VERSION = System.getProperty("org.ovirt.engine.cryptotool.core.packageVersion");
    private static String PACKAGE_DISPLAY_NAME = System.getProperty("org.ovirt.engine.cryptotool.core.packageDisplayName");

    private static KeyStore getKeyStore(String storeType, String store, String password) throws IOException, GeneralSecurityException {
        KeyStore ks = KeyStore.getInstance(storeType);
        try (InputStream is = new FileInputStream(store)) {
            ks.load(is, password.toCharArray());
        }
        return ks;
    }

    private static KeyStore.PrivateKeyEntry getPrivateKeyEntry(KeyStore ks, String alias, String password) throws IOException, GeneralSecurityException {
        KeyStore.PrivateKeyEntry entry = (KeyStore.PrivateKeyEntry)ks.getEntry(alias, new KeyStore.PasswordProtection(password.toCharArray()));
        if (entry == null) {
            throw new IllegalArgumentException(String.format("Keystore alias '%s' is missing", alias));
        }
        return entry;
    }

    private static byte[] readStream(InputStream in) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int n;
        while ((n = in.read(buf)) != -1) {
            os.write(buf, 0, n);
        }
        return os.toByteArray();
    }

    private static String getPassword(String prompt, String what) throws IOException {
        String[] keyValue = what.split(":", 2);
        String type = keyValue[0];
        String value = keyValue[1];

        String password = null;
        if ("pass".equals(type)) {
            password = value;
        } else if ("file".equals(type)) {
            try(
                InputStream is = new FileInputStream(value);
                Reader reader = new InputStreamReader(is);
                BufferedReader breader = new BufferedReader(reader);
            ) {
                password = breader.readLine();
            }
        } else if ("env".equals(type)) {
            password = System.getenv(value);
        } else if ("interactive".equals(type)) {
            if (System.console() == null) {
                throw new RuntimeException("Console is not available, interactive password prompt is impossible");
            }
            System.console().printf("%s", prompt);
            char[] passwordChars = System.console().readPassword();
            if (passwordChars == null) {
                throw new RuntimeException("Cannot read password");
            }
            password = new String(passwordChars);
        } else {
            throw new IllegalArgumentException(String.format("Invalid type: '%s'", type));
        }

        if (password == null) {
            throw new IllegalArgumentException("No password");
        }

        return password;
    }

    public static void main(String... args) throws IOException {
        int exitStatus = 1;
        List<String> cmdArgs = new ArrayList<>(Arrays.asList(args));
        substitutions.put("@PROGRAM_NAME@", PROGRAM_NAME);

        try {
            Properties props = new Properties();
            try (
                InputStream in = Main.class.getResourceAsStream("arguments.properties");
                Reader reader = new InputStreamReader(in);
            ) {
                props.load(reader);
            }
            ArgumentsParser parser = new ArgumentsParser(props, "core");
            parser.getSubstitutions().putAll(substitutions);
            parser.parse(cmdArgs);
            Map<String, Object> argMap = parser.getParsedArgs();

            if((Boolean)argMap.get("help")) {
                System.out.format("Usage: %s", parser.getUsage());
                throw new ExitException("Help", 0);
            } else if((Boolean)argMap.get("version")) {
                System.out.format("%s-%s (%s)%n", PACKAGE_NAME, PACKAGE_VERSION, PACKAGE_DISPLAY_NAME);
                throw new ExitException("Version", 0);
            }
            if(!parser.getErrors().isEmpty()) {
                for(Throwable t : parser.getErrors()) {
                    System.err.format("FATAL: %s%n", t.getMessage());
                }
                throw new ExitException("Parsing error", 1);
            }

            if (cmdArgs.size() < 1) {
                System.err.println("Action not provided");
                throw new ExitException("Action not provided", 1);
            }

            Action action;
            try {
                action = Action.valueOf(cmdArgs.get(0).toUpperCase().replace("-", "_"));
            } catch(IllegalArgumentException e) {
                System.err.printf("Invalid action '%s'%n", cmdArgs.get(0));
                throw new ExitException("Invalid action", 1);
            }

            action.execute(props, cmdArgs);
            exitStatus = 0;
        } catch (ExitException e) {
            exitStatus = e.getExitCode();
        } catch (Exception e) {
            System.err.format("FATAL: %s%n", e.getMessage());
            e.printStackTrace(System.err);
        }

        System.exit(exitStatus);
    }

}
