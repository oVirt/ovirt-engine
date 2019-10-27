package org.ovirt.engine.core.dao.provider.crypt;

import org.ovirt.engine.core.dal.dbbroker.DbFacadeUtils;

public class DefaultPasswordCryptor implements PasswordCryptor {
    @Override
    public String encryptPassword(String password) {
        return DbFacadeUtils.encryptPassword(password);
    }

    @Override
    public String decryptPassword(String password) {
        return DbFacadeUtils.decryptPassword(password);
    }
}
