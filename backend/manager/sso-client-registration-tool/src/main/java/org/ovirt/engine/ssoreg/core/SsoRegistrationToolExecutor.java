package org.ovirt.engine.ssoreg.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.apache.commons.codec.binary.Base64;
import org.ovirt.engine.core.uutils.cli.parser.ArgumentsParser;
import org.ovirt.engine.core.uutils.crypto.EnvelopePBE;
import org.ovirt.engine.ssoreg.db.DBUtils;
import org.slf4j.LoggerFactory;

public class SsoRegistrationToolExecutor {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(SsoRegistrationToolExecutor.class);
    private static final Logger OVIRT_LOGGER = Logger.getLogger("org.ovirt");
    private static String PROGRAM_NAME = System.getProperty("org.ovirt.engine.ssoreg.core.programName");
    private static String PACKAGE_NAME = System.getProperty("org.ovirt.engine.ssoreg.core.packageName");
    private static String PACKAGE_VERSION = System.getProperty("org.ovirt.engine.ssoreg.core.packageVersion");
    private static String PACKAGE_DISPLAY_NAME = System.getProperty("org.ovirt.engine.ssoreg.core.packageDisplayName");
    private static String ENGINE_ETC = System.getProperty("org.ovirt.engine.ssoreg.core.engineEtc");
    private static SecureRandom secureRandom = new SecureRandom();

    public static void main(String... args) {
        int exitStatus = 1;
        List<String> cmdArgs = new ArrayList<>(Arrays.asList(args));
        try {
            final Map<String, String> contextSubstitutions = new HashMap<>();
            contextSubstitutions.put("@ENGINE_ETC@", ENGINE_ETC);
            contextSubstitutions.put("@PROGRAM_NAME@", PROGRAM_NAME);

            setupLogger();
            ArgumentsParser parser;

            final Map<String, String> substitutions = new HashMap<>(contextSubstitutions);

            try (InputStream stream = SsoRegistrationToolExecutor.class.getResourceAsStream("arguments.properties")) {
                parser = new ArgumentsParser(stream, "core");
                parser.getSubstitutions().putAll(substitutions);
            }
            parser.parse(cmdArgs);
            Map<String, Object> argMap = parser.getParsedArgs();
            setupLogger(argMap);

            log.debug("Version: {}-{} ({})", PACKAGE_NAME, PACKAGE_VERSION, PACKAGE_DISPLAY_NAME);

            if ((Boolean) argMap.get("help")) {
                System.out.format("Usage: %s", parser.getUsage());
                throw new ExitException("Help", 0);
            } else if ((Boolean) argMap.get("version")) {
                System.out.format("%s-%s (%s)%n", PACKAGE_NAME, PACKAGE_VERSION, PACKAGE_DISPLAY_NAME);
                throw new ExitException("Version", 0);
            }

            if (!parser.getErrors().isEmpty()) {
                for (Throwable t : parser.getErrors()) {
                    log.error(t.getMessage());
                    log.debug(t.getMessage(), t);
                }
                throw new ExitException("Parsing error", 1);
            }

            DBUtils dbUtils = new DBUtils();

            log.info("=========================================================================");
            log.info("================== oVirt Sso Client Registration Tool ===================");
            log.info("=========================================================================");

            String clientId = getProvidedParameter(argMap,
                    "client-id",
                    id -> !id.isBlank(),
                    "must not be null or blank");

            String certificateFile = getProvidedParameter(argMap,
                    "client-ca-location",
                    location -> new File(location).exists(),
                    "file not found at given path");

            String callbackPrefix = getProvidedParameter(argMap,
                    "callback-prefix-url",
                    prefix -> prefix.startsWith("http") || prefix.startsWith("https"),
                    "must start with either http or https");

            Boolean encryptedUserInfo = Boolean.TRUE;
            if (argMap.containsKey("encrypted-userinfo")) {
                encryptedUserInfo = getProvidedParameter(argMap, "encrypted-userinfo");
            }

            String tmpConfFileName = null;
            if (argMap.containsKey("conf-file-name")) {
                tmpConfFileName = getProvidedParameter(argMap,
                        "conf-file-name",
                        name -> !name.isBlank(),
                        "temporary configuration file name must not be blank");
            }

            String clientSecret = generateClientSecret();
            String encodedClientSecret = encode(argMap, clientSecret);
            dbUtils.unregisterClient(clientId);
            dbUtils.registerClient(clientId, encodedClientSecret, certificateFile, callbackPrefix, encryptedUserInfo);
            String tmpFile = createTmpSsoClientConfFile(clientId,
                    clientSecret,
                    certificateFile,
                    callbackPrefix,
                    encryptedUserInfo,
                    tmpConfFileName);
            System.out.println("Client registration completed successfully");
            System.out.format("Client secret has been written to file %s%n", tmpFile);
            log.info("========================================================================");
            log.info("========================= Execution Completed ==========================");
            log.info("========================================================================");

            exitStatus = 0;
        } catch (ExitException e) {
            log.debug(e.getMessage(), e);
            exitStatus = e.getExitCode();
        } catch (Throwable t) {
            log.error(t.getMessage() != null ? t.getMessage() : t.getClass().getName());
            log.debug("Exception:", t);
        }
        log.debug("Exiting with status '{}'", exitStatus);
        System.exit(exitStatus);
    }

    private static String generateClientSecret() {
        byte[] s = new byte[32];
        secureRandom.nextBytes(s);
        return new Base64(0, new byte[0], true).encodeToString(s);
    }

    private static String encode(Map<String, Object> args, String clientSecret)
            throws IOException, GeneralSecurityException {
        return EnvelopePBE.encode((String) args.get("encoding-algorithm"),
                Integer.parseInt((String) args.get("encoding-keysize")),
                Integer.parseInt((String) args.get("encoding-iterations")),
                null,
                clientSecret);
    }

    private static void setupLogger() {
        String logLevel = System.getenv("OVIRT_LOGGING_LEVEL");
        OVIRT_LOGGER.setLevel(
                logLevel != null ? Level.parse(logLevel) : Level.INFO);
    }

    private static void setupLogger(Map<String, Object> args) throws IOException {
        Logger log = Logger.getLogger("");
        String logfile = (String) args.get("log-file");
        if (logfile != null) {
            FileHandler fh = new FileHandler(
                    new File(SsoLocalConfig.getInstance().getLogDir(), logfile).getAbsolutePath(),
                    true);
            fh.setFormatter(new SimpleFormatter());
            log.addHandler(fh);
        }
        OVIRT_LOGGER.setLevel((Level) args.get("log-level"));
    }

    private static String createTmpSsoClientConfFile(String clientId,
            String clientSecret,
            String certificateFile,
            String callbackPrefix,
            Boolean encryptedUserInfo,
            String tempConfigurationFileName) throws FileNotFoundException {
        File tmpFile = new File(tempConfigurationFileName);

        try (PrintWriter pw = new PrintWriter(new FileOutputStream(tmpFile))) {
            pw.println(String.format("SSO_CLIENT_ID=%s", clientId));
            pw.println(String.format("SSO_CLIENT_SECRET=%s", clientSecret));
            pw.println(String.format("SSO_CLIENT_CERTIFICATE_FILE=%s", certificateFile));
            pw.println(String.format("SSO_CLIENT_CALLBACK_PREFIX=%s", callbackPrefix));
            pw.println(String.format("SSO_CLIENT_ENCRYPTED_USERINFO=%b", encryptedUserInfo));
        }
        return tmpFile.getAbsolutePath();
    }

    private static <T> T getProvidedParameter(Map<String, Object> argMap,
            String paramName,
            Predicate<T> isValidPredicate,
            String invalidParamMessage) {

        if (!argMap.containsKey(paramName)) {
            throw new IllegalArgumentException("Parameter required but not found: " + paramName);
        }

        T paramValue = (T) argMap.get(paramName);
        if (!isValidPredicate.test(paramValue)) {
            throw new IllegalArgumentException(paramName + "=" + paramValue + ": " + invalidParamMessage);
        }
        log.info("Provided parameter: {}={}", paramName, paramValue);
        return paramValue;
    }

    private static <T> T getProvidedParameter(Map<String, Object> argMap, String paramName) {
        return getProvidedParameter(argMap, paramName, arg -> true, "");
    }
}
