package org.ovirt.engine.core.bll.provider.network.openstack;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.woorea.openstack.quantum.model.Port;

@JsonRootName(value = "port")
public class PortForUpdate extends Port {

    private static final long serialVersionUID = 8605678666920132080L;

    @Override
    @JsonInclude
    public List<String> getSecurityGroups() {
        return super.getSecurityGroups();
    }
}
