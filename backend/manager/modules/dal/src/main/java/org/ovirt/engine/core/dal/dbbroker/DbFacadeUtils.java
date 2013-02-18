package org.ovirt.engine.core.dal.dbbroker;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigCommon;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.utils.crypt.EncryptionUtils;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class DbFacadeUtils {
    private static final Log log = LogFactory.getLog(DbFacadeUtils.class);

    public static Date fromDate(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return new Date(timestamp.getTime());
    }

    public static Object asSingleResult(List<?> list) {
        return list.isEmpty() ? null : list.get(0);
    }

    public static String encryptPassword(String password) {
        if (StringUtils.isEmpty(password)) {
            return password;
        }
        String keyFile = Config.resolveKeyStorePath();
        String passwd = Config.<String> GetValue(ConfigValues.keystorePass, ConfigCommon.defaultConfigurationVersion);
        String alias = Config.<String> GetValue(ConfigValues.CertAlias, ConfigCommon.defaultConfigurationVersion);
        try {
            return EncryptionUtils.encrypt(password, keyFile, passwd, alias);
        } catch (Exception e) {
            throw new SecurityException(e);
        }
    }

    public static String decryptPassword(String password) {
        if (StringUtils.isEmpty(password)) {
            return password;
        }
        String keyFile = Config.resolveKeyStorePath();
        String passwd = Config.<String> GetValue(ConfigValues.keystorePass, ConfigCommon.defaultConfigurationVersion);
        String alias = Config.<String> GetValue(ConfigValues.CertAlias, ConfigCommon.defaultConfigurationVersion);
        try {
            return EncryptionUtils.decrypt(password, keyFile, passwd, alias);
        } catch (Exception e) {
            log.debugFormat("Failed to decrypt password, error message: {0}", e.getMessage());
            return password;
        }
    }
}
