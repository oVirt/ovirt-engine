package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;

public class GraphicsParameters extends VdcActionParametersBase {

    private boolean vm = true;
    private GraphicsDevice dev;

    public GraphicsParameters(GraphicsDevice dev) {
        this.dev = dev;
    }

    public boolean isVm() {
        return vm;
    }

    public void setVm(boolean vm) {
        this.vm = vm;
    }

    public GraphicsDevice getDev() {
        return dev;
    }

    public void setDev(GraphicsDevice dev) {
        this.dev = dev;
    }
}
