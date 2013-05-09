package org.ovirt.engine.core.common.businessentities;

public enum AttestationResultEnum {
    UNTRUSTED(0),
    TRUSTED(1),
    UNKNOWN(2),
    TIMEOUT(3),
    UNINITIALIZED(4);

    private AttestationResultEnum(int value) {
    }
}

