package org.ovirt.engine.core.config.entity.helper;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.config.EngineConfigCLIParser;
import org.ovirt.engine.core.config.EngineConfigLogic;
import org.ovirt.engine.core.config.entity.ConfigKey;
import org.ovirt.engine.core.tools.ToolConsole;
import org.ovirt.engine.core.utils.crypt.EngineEncryptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PasswordValueHelper implements ValueHelper {
    // The log:
    private static final Logger log = LoggerFactory.getLogger(PasswordValueHelper.class);

    // The console:
    private static final ToolConsole console = ToolConsole.getInstance();

    public static final String INTERACTIVE_MODE = "Interactive";
    private EngineConfigCLIParser parser;

    String encrypt(String value) throws Exception {
        return EngineEncryptionUtils.encrypt(value);
    }

    String decrypt(String value) throws Exception {
        return EngineEncryptionUtils.decrypt(value);
    }

    @Override
    public String getValue(String value) throws GeneralSecurityException {
        /*
         * The idea of this method would normally be to decrypt and return
         * the decrypted value. Due to security reasons, we do not wish to return
         * the real value. Just an indication if we have a value in the DB or not.
         * So if there's no value we return "Empty".
         * If there's a value we try to decrypt. On success we return "Set",
         * On failure we return an error.
         */
        String returnedValue = "Empty";
        if (value != null && !value.equals("")){
            try {
                decrypt(value);
                returnedValue = "Set";
            } catch (Exception exception) {
                String msg = "Failed to decrypt the current value.";
                console.writeLine(msg);
                log.error("Exception", exception);
                throw new GeneralSecurityException(msg);
            }
        }

        return returnedValue;
    }

    /**
     * this method is ignoring the value! if the value is "Interactive" it will open a console input for the password
     * else it will look for the password from a file
     *
     * @return The user's encrypted password
     */
    @Override
    public String setValue(String value) throws GeneralSecurityException {
        String returnedValue = null;
        String password = null;

        try {
            password = extractPasswordValue(value);
            if (StringUtils.isBlank(password)) {
                return StringUtils.EMPTY;
            }
            returnedValue = encrypt(password);
        } catch(Throwable exception) {
            String msg = "Failed to encrypt the current value.";
            console.writeLine(msg);
            log.error(msg, exception);
            throw new GeneralSecurityException(msg);
        }

        return returnedValue;
    }

    public String extractPasswordValue(String value) throws IOException {
        String password = null;
        if (StringUtils.isNotBlank(value) && value.equalsIgnoreCase(INTERACTIVE_MODE)) {
            password = EngineConfigLogic.startPasswordDialog(null);
            String passwordConfirm = EngineConfigLogic.startPasswordDialog(null, "Please reenter password");
            if (!StringUtils.equals(password, passwordConfirm)) {
                console.writeLine("Passwords don't match.");
                return extractPasswordValue(value);
            }
        } else {
                password =
                    EngineConfigLogic.getPassFromFile(StringUtils.isNotBlank(value) ? value
                            : parser.getAdminPassFile());
        }
        return password;
    }

    @Override
    public ValidationResult validate(ConfigKey key, String value) {
        // check if value is file path
        if (StringUtils.isNotBlank(value) && new File(value).exists()) {
            return new ValidationResult(true);
        }
        // The only valid value is "Interactive"
        if (StringUtils.isNotBlank(value) && value.equalsIgnoreCase(INTERACTIVE_MODE)) {
            return new ValidationResult(true);
        }
        // or if we have the password in --admin-pass-file
        if (StringUtils.isNotBlank(parser.getAdminPassFile())) {
            if (new File(parser.getAdminPassFile()).exists()) {
                return new ValidationResult(true);
            } else {
                return new ValidationResult(false, String.format("The file %1$s does not exist. Cannot change password for %2$s",
                                parser.getAdminPassFile(), key.getKey()));
            }
        }
        return new ValidationResult(false,
                String.format("It appears you tried to change a password for %1$s in a wrong way. "
                        +
                        "You should use interactive mode or provide a file cotaining the password. Run the utility with --help for more information.",
                        key.getKey()));
    }

    @Override
    public void setParser(EngineConfigCLIParser parser) {
        this.parser = parser;
    }

    @Override
    public String getHelpNote(ConfigKey key) {
        return String.format("%n%n%n" +
                             "### Notes:%n" +
                             "### 1. Passwords: password can be set in interactive mode ie:%n" +
                             "###        engine-config -s %1$s=interactive%n" +
                             "###    or via file with one of the following options:%n" +
                             "###        engine-config -s %1$s --admin-pass-file=/tmp/mypass%n" +
                             "###        engine-config -s %1$s=/tmp/mypass%n" +
                             "### 2. In order for your change(s) to take effect,%n" +
                             "###    restart the oVirt engine service (using: 'service ovirt-engine restart').%n" +
                "################################################################################%n%n", key.getKey());
    }
}
