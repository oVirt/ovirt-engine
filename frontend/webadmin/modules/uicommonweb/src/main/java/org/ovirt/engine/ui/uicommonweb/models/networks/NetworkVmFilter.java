package org.ovirt.engine.ui.uicommonweb.models.networks;

import org.ovirt.engine.ui.uicommonweb.ViewFilter;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public enum NetworkVmFilter implements ViewFilter<NetworkVmFilter> {
    running(ConstantsManager.getInstance().getConstants().runningVm()),
    notRunning(ConstantsManager.getInstance().getConstants().notRunningVm());

    private String text;

    NetworkVmFilter(String text) {
        this.text = text;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public NetworkVmFilter getValue() {
        return this;
    }
}
