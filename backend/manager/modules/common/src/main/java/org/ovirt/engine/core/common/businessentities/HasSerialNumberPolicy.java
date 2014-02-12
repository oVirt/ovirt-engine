package org.ovirt.engine.core.common.businessentities;

public interface HasSerialNumberPolicy {
    SerialNumberPolicy getSerialNumberPolicy();
    void setSerialNumberPolicy(SerialNumberPolicy serialNumberPolicy);

    String getCustomSerialNumber();
    void setCustomSerialNumber(String customSerialNumber);
}
