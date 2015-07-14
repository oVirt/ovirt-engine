package org.ovirt.engine.core.bll.provider.network.openstack;

import java.util.List;

import org.codehaus.jackson.map.annotate.JsonRootName;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import com.woorea.openstack.quantum.model.Port;

@JsonRootName(value = "port")
public class PortForUpdate extends Port {

    private static final long serialVersionUID = 8605678666920132080L;

    @Override
    @JsonSerialize(include = Inclusion.ALWAYS)
    public List<String> getSecurityGroups() {
        return super.getSecurityGroups();
    }
}
