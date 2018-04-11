package org.ovirt.engine.core.vdsbroker.gluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerService;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterService;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServiceStatus;
import org.ovirt.engine.core.common.constants.gluster.GlusterConstants;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.gluster.GlusterServiceDao;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturn;

/**
 * The return type to receive a list of gluster related services.
 */
public class GlusterServicesReturn extends StatusReturn {
    private static final String SERVICES = "services";
    private static final String NAME = "name";
    private static final String PID = "pid";
    private static final String STATUS = "status";
    private static final String MESSAGE = "message";

    private Guid serverId;
    private static final Map<String, GlusterService> servicesMap = getServicesMap();
    private List<GlusterServerService> services;

    @SuppressWarnings("unchecked")
    public GlusterServicesReturn(Guid serverId, Map<String, Object> innerMap) {
        super(innerMap);
        this.serverId = serverId;

        if (getStatus().code != GlusterConstants.CODE_SUCCESS) {
            return;
        }

        services = new ArrayList<>();
        for (Object service : (Object[]) innerMap.get(SERVICES)) {
            services.add(getService((Map<String, Object>) service));
        }
    }

    private GlusterServerService getService(Map<String, Object> serviceMap) {
        GlusterServerService serverService = new GlusterServerService();
        serverService.setServiceName((String) serviceMap.get(NAME));
        String pid = (String) serviceMap.get(PID);
        serverService.setPid(StringUtils.isEmpty(pid) ? null : Integer.parseInt(pid));
        serverService.setStatus(GlusterServiceStatus.valueOf((String) serviceMap.get(STATUS)));
        serverService.setMessage((String) serviceMap.get(MESSAGE));
        serverService.setServerId(serverId);

        GlusterService service = servicesMap.get(serverService.getServiceName());
        if (service != null) {
            serverService.setServiceId(service.getId());
            serverService.setServiceType(service.getServiceType());
        }

        return serverService;
    }

    private static Map<String, GlusterService> getServicesMap() {
        Map<String, GlusterService> serviceNames = new HashMap<>();

        List<GlusterService> services = Injector.get(GlusterServiceDao.class).getAll();
        for (GlusterService service : services) {
            serviceNames.put(service.getServiceName(), service);
        }

        return serviceNames;
    }

    public List<GlusterServerService> getServices() {
        return services;
    }
}
