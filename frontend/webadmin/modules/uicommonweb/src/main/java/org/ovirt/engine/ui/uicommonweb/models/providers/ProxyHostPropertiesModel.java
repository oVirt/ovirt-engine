package org.ovirt.engine.ui.uicommonweb.models.providers;

import java.util.Collections;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;

public abstract class ProxyHostPropertiesModel extends Model {

    private Guid lastStoragePoolId;
    private Guid lastProxyHostId;

    public void disableProxyHost() {
        getProxyHost().setItems(Collections.singleton(null));
        getProxyHost().setIsChangeable(false);
    }

    public Guid getLastStoragePoolId() {
        return lastStoragePoolId;
    }

    public Guid getLastProxyHostId() {
        return lastProxyHostId;
    }

    public void setLastStoragePoolId(Guid lastStoragePoolId) {
        this.lastStoragePoolId = lastStoragePoolId;
    }

    public void setLastProxyHostId(Guid lastProxyHostId) {
        this.lastProxyHostId = lastProxyHostId;
    }

    public ListModel<VDS> getProxyHost() {
        return new ListModel<>();
    }
}
