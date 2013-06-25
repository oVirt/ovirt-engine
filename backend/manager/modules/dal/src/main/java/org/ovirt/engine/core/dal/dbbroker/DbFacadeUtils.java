package org.ovirt.engine.core.dal.dbbroker;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.ovirt.engine.core.utils.crypt.EngineEncryptionUtils;
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
        try {
            return EngineEncryptionUtils.encrypt(password);
        } catch (Exception e) {
            throw new SecurityException(e);
        }
    }

    public static String decryptPassword(String password) {
        try {
            return EngineEncryptionUtils.decrypt(password);
        } catch (Exception e) {
            log.debugFormat("Failed to decrypt password, error message: {0}", e.getMessage());
            return password;
        }
    }
}
