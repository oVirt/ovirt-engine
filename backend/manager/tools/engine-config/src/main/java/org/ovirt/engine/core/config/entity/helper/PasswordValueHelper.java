package org.ovirt.engine.core.config.entity.helper;

import java.security.GeneralSecurityException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ovirt.engine.core.config.EngineConfig;
import org.ovirt.engine.core.config.EngineConfigCLIParser;
import org.ovirt.engine.core.config.EngineConfigLogic;
import org.ovirt.engine.core.config.db.ConfigDAO;
import org.ovirt.engine.core.config.entity.ConfigKey;
import org.ovirt.engine.core.config.entity.ConfigKeyFactory;
import org.ovirt.engine.core.engineencryptutils.EncryptionUtils;

public class PasswordValueHelper implements ValueHelper {
    private static ConfigDAO configDAO;
    private static String certAlias;
    private static String keyStoreURL;
    private static String keyStorePass;
    private static final Logger log = Logger.getLogger(PasswordValueHelper.class);
    public static final String INTERACTIVE_MODE = "Interactive";
    private EngineConfigCLIParser parser;

    static {
        try {
            configDAO = EngineConfig.getInstance().getEngineConfigLogic().getConfigDAO();
            ConfigKeyFactory keyFactory = ConfigKeyFactory.getInstance();
            certAlias =
                configDAO.getKey(keyFactory.generateBlankConfigKey("CertAlias", "String"))
                .getValue();
            keyStoreURL =
                configDAO.getKey(keyFactory.generateBlankConfigKey("keystoreUrl", "String"))
                .getValue();
            keyStorePass =
                configDAO.getKey(keyFactory.generateBlankConfigKey("keystorePass", "String"))
                .getValue();
        } catch (Exception e) {
            log.debug(e.getMessage());
        }
    }

    String encrypt(String value) throws Exception {
        return EncryptionUtils.encrypt(value, keyStoreURL, keyStorePass, certAlias);
    }

    String decrypt(String value) throws Exception {
        return EncryptionUtils.decrypt(value, keyStoreURL, keyStorePass, certAlias);
    }

    @Override
    public String getValue(String value) throws GeneralSecurityException {
        /*
         * The idea of this method would normally be to decrypt and return
         * the decrypted value. Due to security reasons, we do not wish to return
         * the real value. Just and indication if we have a value in the DB or not.
         * So we if there's no value we return "Empty".
         * If there's a value we try to decrypt. On success we return "Set",
         * On failure we return an error.
         */
        String returnedValue = "Empty";
        if (value != null && !value.equals("")){
            try {
                decrypt(value);
                returnedValue = "Set";
            } catch (Exception e) {
                String msg = "Failed to decrypt the current value";
                Logger.getLogger(EngineConfig.class).debug(msg, e);
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
            if (StringUtils.isNotBlank(value) && value.equals(INTERACTIVE_MODE)) {
                password = EngineConfigLogic.startPasswordDialog(null);
            } else {
                password = EngineConfigLogic.getPassFromFile(parser.getAdminPassFile());
            }
            returnedValue = encrypt(password);
        } catch (Exception e) {
            String msg = "Failed to encrypt the current value";
            Logger.getLogger(EngineConfig.class).debug(msg, e);
            throw new GeneralSecurityException(msg);
        }

        return returnedValue;
    }

    @Override
    public boolean validate(ConfigKey key, String value) {
        // The only valid value is "Interactive"
        if (StringUtils.isNotBlank(value) && value.equals(INTERACTIVE_MODE)) {
            return true;
        }
        if (parser.getAdminPassFile() != null) {
            return true;
        }
        return false;
    }

    @Override
    public void setParser(EngineConfigCLIParser parser) {
        this.parser = parser;
    }

}
