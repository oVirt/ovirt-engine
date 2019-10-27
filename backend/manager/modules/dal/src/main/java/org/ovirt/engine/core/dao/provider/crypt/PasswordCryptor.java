package org.ovirt.engine.core.dao.provider.crypt;

public interface PasswordCryptor {

    String encryptPassword(String password);

    String decryptPassword(String password);
}
