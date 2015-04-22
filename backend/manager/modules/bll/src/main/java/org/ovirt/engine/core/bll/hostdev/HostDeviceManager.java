package org.ovirt.engine.core.bll.hostdev;

import java.util.ArrayList;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDynamicDAO;
import org.ovirt.engine.core.dao.HostDeviceDao;

@ApplicationScoped
public class HostDeviceManager {

    @Inject
    private VdsDynamicDAO hostDynamicDao;

    @Inject
    private HostDeviceDao hostDeviceDao;

    @Inject
    private BackendInternal backend;

    public void init() {
        ArrayList<VdcActionParametersBase> parameters = new ArrayList<>();
        // It is sufficient to refresh only the devices of 'UP' hosts since other hosts
        // will have their devices refreshed in InitVdsOnUpCommand
        for (Guid hostId : hostDynamicDao.getIdsOfHostsWithStatus(VDSStatus.Up)) {
            parameters.add(new VdsActionParameters(hostId));
        }

        backend.runInternalMultipleActions(VdcActionType.RefreshHostDevices, parameters);
        hostDeviceDao.cleanDownVms();
    }
}
