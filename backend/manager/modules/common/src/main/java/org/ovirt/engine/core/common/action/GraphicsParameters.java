package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.GraphicsDevice;

public class GraphicsParameters extends ActionParametersBase {

    private boolean vm = true;
    private GraphicsDevice dev;

    public GraphicsParameters() {
    }

    public GraphicsParameters(GraphicsDevice dev) {
        this.dev = dev;
    }

    public boolean isVm() {
        return vm;
    }

    public GraphicsParameters setVm(boolean vm) {
        this.vm = vm;
        return this;
    }

    public GraphicsDevice getDev() {
        return dev;
    }

}
