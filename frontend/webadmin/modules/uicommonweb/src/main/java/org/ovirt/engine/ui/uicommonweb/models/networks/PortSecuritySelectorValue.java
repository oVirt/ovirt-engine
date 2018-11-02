package org.ovirt.engine.ui.uicommonweb.models.networks;

import org.ovirt.engine.ui.uicompat.ConstantsManager;

public enum PortSecuritySelectorValue {
    ENABLED(ConstantsManager.getInstance()
            .getConstants().portSecurityEnabled(), true),
    DISABLED(ConstantsManager.getInstance()
            .getConstants().portSecurityDisabled(), false),
    UNDEFINED(ConstantsManager.getInstance()
            .getConstants().portSecurityUndefined(), null);

    private String description;

    private Boolean value;

    private PortSecuritySelectorValue(String description, Boolean value) {
        this.description = description;
        this.value = value;
    }

    @Override
    public String toString() {
        return description;
    }

    public Boolean getValue() {
        return value;
    }
}
