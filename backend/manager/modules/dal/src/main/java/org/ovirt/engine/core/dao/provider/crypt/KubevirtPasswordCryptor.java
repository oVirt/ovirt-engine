package org.ovirt.engine.core.dao.provider.crypt;

import java.nio.charset.StandardCharsets;

import org.ovirt.engine.core.utils.crypt.EngineEncryptionUtils;
import org.ovirt.engine.core.uutils.crypto.EnvelopeEncryptDecrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KubevirtPasswordCryptor implements PasswordCryptor {
    private static final Logger log = LoggerFactory.getLogger(KubevirtPasswordCryptor.class);

    @Override
    public String encryptPassword(String password) {
        try {
            return EnvelopeEncryptDecrypt.encrypt(
                    "AES/OFB/PKCS5Padding",
                    256,
                    EngineEncryptionUtils.getCertificate(),
                    100,
                    password.getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            log.debug("Failed to encrypt kubevirt provider token", e);
        }

        return password;
    }

    @Override
    public String decryptPassword(String password) {
        try {
            return new String(EnvelopeEncryptDecrypt.decrypt(EngineEncryptionUtils.getPrivateKeyEntry(), password),
                    StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.debug("Failed to decrypt kubevirt provider token", e);
        }

        return password;
    }
}
