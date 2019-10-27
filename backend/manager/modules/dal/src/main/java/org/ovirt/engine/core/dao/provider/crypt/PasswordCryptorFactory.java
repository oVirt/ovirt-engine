package org.ovirt.engine.core.dao.provider.crypt;

import org.ovirt.engine.core.common.businessentities.ProviderType;

public class PasswordCryptorFactory {

    public static PasswordCryptor create(ProviderType type) {
        if (type == ProviderType.KUBEVIRT) {
            return new KubevirtPasswordCryptor();
        } else {
            return new DefaultPasswordCryptor();
        }
    }
}
