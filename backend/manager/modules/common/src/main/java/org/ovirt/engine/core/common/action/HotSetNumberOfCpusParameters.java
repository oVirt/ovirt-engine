package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.VmStatic;

public class HotSetNumberOfCpusParameters extends VmManagementParametersBase {

    private static final long serialVersionUID = 3471288629004073208L;

    private PlugAction plugAction;

    public HotSetNumberOfCpusParameters() {
    }

    public HotSetNumberOfCpusParameters(VmStatic vmStatic, PlugAction plugAction) {
        super(vmStatic);
        this.plugAction = plugAction;
    }

    public PlugAction getPlugAction() {
        return plugAction;
    }
}
