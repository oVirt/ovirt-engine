package org.ovirt.engine.core.utils.ejb;

/**
 * Enum that defines the possible proxy types for bean
 *
 *
 */
public enum BeanProxyType {
    LOCAL("/local"); // Local proxy

    private String _value;

    private BeanProxyType(String value) {
        _value = value;
    }

    public String toString() {
        return _value;
    }
}
