package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;

import javax.validation.Valid;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "AddNetworkStoragePoolParameters")
public class AddNetworkStoragePoolParameters extends StoragePoolParametersBase {
    private static final long serialVersionUID = -7392121807419409051L;
    @Valid
    @XmlElement(name = "Network")
    private network _network;

    public AddNetworkStoragePoolParameters(Guid storagePoolId, network net) {
        super(storagePoolId);
        _network = net;
    }

    public network getNetwork() {
        return _network;
    }

    public AddNetworkStoragePoolParameters() {
    }
}
