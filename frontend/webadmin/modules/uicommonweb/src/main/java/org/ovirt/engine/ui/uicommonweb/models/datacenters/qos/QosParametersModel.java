package org.ovirt.engine.ui.uicommonweb.models.datacenters.qos;

import org.ovirt.engine.core.common.businessentities.qos.QosBase;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

public abstract class QosParametersModel<T extends QosBase> extends EntityModel<T> {

    public abstract void init(T qos);

    public abstract void flush(T qos);

    public abstract boolean validate();

}
