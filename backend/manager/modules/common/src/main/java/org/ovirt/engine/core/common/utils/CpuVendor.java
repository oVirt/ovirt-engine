package org.ovirt.engine.core.common.utils;

import java.io.Serializable;
import java.util.Set;

public enum CpuVendor implements Serializable {

    INTEL("vmx"),
    AMD("svm"),
    IBM("powernv"),
    IBMS390("sie");

    private final String flag;

    CpuVendor(String flag) {
        this.flag = flag;
    }

    public String getFlag() {
        return flag;
    }

    public static CpuVendor fromFlags(Set<String> flags) {
        for (CpuVendor vendor: CpuVendor.values()) {
            if (flags.contains(vendor.flag)) {
                return vendor;
            }
        }
        return null;
    }
}
