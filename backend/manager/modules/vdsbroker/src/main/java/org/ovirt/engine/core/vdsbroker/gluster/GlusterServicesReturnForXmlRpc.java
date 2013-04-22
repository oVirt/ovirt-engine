package org.ovirt.engine.core.vdsbroker.gluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterServiceStatus;
import org.ovirt.engine.core.common.businessentities.gluster.ServiceInfo;
import org.ovirt.engine.core.common.constants.gluster.GlusterConstants;
import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturnForXmlRpc;

/**
 * The XmlRpc return type to receive a list of gluster related services.
 */
public class GlusterServicesReturnForXmlRpc extends StatusReturnForXmlRpc {
    private static final String SERVICES = "services";
    private static final String NAME = "name";
    private static final String PID = "pid";
    private static final String STATUS = "status";
    private static final String MESSAGE = "message";

    private List<ServiceInfo> services;

    @SuppressWarnings("unchecked")
    public GlusterServicesReturnForXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);

        if (mStatus.mCode != GlusterConstants.CODE_SUCCESS) {
            return;
        }

        services = new ArrayList<ServiceInfo>();
        for (Object service : (Object[]) innerMap.get(SERVICES)) {
            services.add(getService((Map<String, Object>) service));
        }
    }

    private ServiceInfo getService(Map<String, Object> serviceMap) {
        ServiceInfo service = new ServiceInfo();
        service.setServiceName((String) serviceMap.get(NAME));
        service.setPid(Integer.parseInt((String) serviceMap.get(PID)));
        service.setStatus(GlusterServiceStatus.valueOf((String) serviceMap.get(STATUS)));
        service.setMessage((String) serviceMap.get(MESSAGE));

        return service;
    }

    public List<ServiceInfo> getServices() {
        return services;
    }
}
