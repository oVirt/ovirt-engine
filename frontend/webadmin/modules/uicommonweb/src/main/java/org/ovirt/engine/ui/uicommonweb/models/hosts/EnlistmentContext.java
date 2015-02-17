package org.ovirt.engine.ui.uicommonweb.models.hosts;

import org.ovirt.engine.core.compat.Guid;

@SuppressWarnings("unused")
public class EnlistmentContext {

    private HostListModel<?> model;

    public HostListModel<?> getModel() {
        return model;
    }

    private Guid dataCenterId;

    public Guid getDataCenterId() {
        return dataCenterId;
    }

    public void setDataCenterId(Guid value) {
        dataCenterId = value;
    }

    private Guid clusterId;

    public Guid getClusterId() {
        return clusterId;
    }

    public void setClusterId(Guid value) {
        clusterId = value;
    }

    private Guid oldClusterId;

    public Guid getOldClusterId() {
        return oldClusterId;
    }

    public void setOldClusterId(Guid value) {
        oldClusterId = value;
    }

    public EnlistmentContext(HostListModel<?> model) {
        this.model = model;
    }
}
