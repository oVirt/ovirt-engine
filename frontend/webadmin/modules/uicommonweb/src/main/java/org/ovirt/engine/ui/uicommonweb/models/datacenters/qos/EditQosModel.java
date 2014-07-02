package org.ovirt.engine.ui.uicommonweb.models.datacenters.qos;

import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.qos.QosBase;
import org.ovirt.engine.ui.uicommonweb.models.Model;

public abstract class EditQosModel<T extends QosBase, P extends QosParametersModel<T>> extends NewQosModel<T, P> {

    public EditQosModel(T qos, Model sourceModel, StoragePool dataCenter) {
        super(sourceModel, dataCenter);
        init(qos);
    }

    @Override
    public void init(T qos) {
        setQos(qos);
        getName().setEntity(qos.getName());
        getDescription().setEntity(qos.getDescription());
    }
}

