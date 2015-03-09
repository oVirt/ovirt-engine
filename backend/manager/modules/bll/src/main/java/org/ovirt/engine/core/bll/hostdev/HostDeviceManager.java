package org.ovirt.engine.core.bll.hostdev;

import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.HostDeviceDao;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.dao.VdsDynamicDAO;
import org.ovirt.engine.core.dao.VmDeviceDAO;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.lock.LockManager;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

@ApplicationScoped
public class HostDeviceManager {

    @Inject
    private VdsDynamicDAO hostDynamicDao;

    @Inject
    private LockManager lockManager;

    @Inject
    private VmDeviceDAO vmDeviceDao;

    @Inject
    private HostDeviceDao hostDeviceDao;

    @Inject
    private VdsDAO vdsDao;

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

    public boolean checkVmNeedsHostDevices(VM vm) {
        return supportsHostDevicePassthrough(vm) &&
                vmDeviceDao.existsVmDeviceByVmIdAndType(vm.getId(), VmDeviceGeneralType.HOSTDEV);
    }

    public boolean checkVmHostDeviceAvailability(VM vm, Guid vdsId) {
        // if vm's cluster doesn't support hostdev, it's requirements for host devices are trivially fulfilled
        return !supportsHostDevicePassthrough(vm) || hostDeviceDao.checkVmHostDeviceAvailability(vm.getId(), vdsId);
    }

    public void allocateVmHostDevices(Guid vmId) {
        hostDeviceDao.markHostDevicesUsedByVmId(vmId);
    }

    public void freeVmHostDevices(Guid vmId) {
        hostDeviceDao.freeHostDevicesUsedByVmId(vmId);
    }

    public void acquireHostDevicesLock(Guid vdsId) {
        lockManager.acquireLockWait(new EngineLock(getExclusiveLockForHostDevices(vdsId)));
    }

    public void releaseHostDevicesLock(Guid vdsId) {
        lockManager.releaseLock(new EngineLock(getExclusiveLockForHostDevices(vdsId)));
    }

    private static Map<String, Pair<String, String>> getExclusiveLockForHostDevices(Guid vdsId) {
        return Collections.singletonMap(
                vdsId.toString(),
                LockMessagesMatchUtil.makeLockingPair(
                        LockingGroup.HOST_DEVICES,
                        VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED));
    }

    private static boolean supportsHostDevicePassthrough(VM vm) {
        return FeatureSupported.hostDevicePassthrough(vm.getVdsGroupCompatibilityVersion());
    }
}
