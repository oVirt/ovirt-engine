package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.lock.LockManager;
import org.slf4j.Logger;

@Singleton
public class HostLocking {

    @Inject
    private LockManager lockManager;

    public EngineLock acquireMonitorLock(VDS host, String lockReleaseMessage, Logger log) {
        Guid hostId = host.getId();
        Map<String, Pair<String, String>> exclusiveLocks =
                Collections.singletonMap(hostId.toString(), new Pair<>(LockingGroup.VDS_INIT.name(), ""));

        String closingMessage = calculateClosingMessage(lockReleaseMessage, host);
        EngineLock monitoringLock = new HostEngineLock(exclusiveLocks, null, closingMessage, log);
        log.info("Before acquiring lock in order to prevent monitoring for host '{}' from data-center '{}'",
                host.getName(),
                host.getStoragePoolName());
        lockManager.acquireLockWait(monitoringLock);
        log.info("Lock acquired, from now a monitoring of host will be skipped for host '{}' from data-center '{}'",
                host.getName(),
                host.getStoragePoolName());

        return monitoringLock;
    }

    private static String calculateClosingMessage(String commandName, VDS host) {
        return String.format("%s finished. Lock released. Monitoring can run now for host '%s' from data-center '%s'",
                commandName,
                host.getName(),
                host.getStoragePoolName());
    }

    public Map<String, Pair<String, String>> getSetupNetworksLock(Guid hostId) {
        return Collections.singletonMap(LockingGroup.HOST_NETWORK.name() + hostId.toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.HOST_NETWORK,
                        EngineMessage.ACTION_TYPE_FAILED_SETUP_NETWORKS_OR_REFRESH_IN_PROGRESS));
    }

    public Map<String, Pair<String, String>> getPowerManagementLock(Guid vdsId) {
        return Collections.singletonMap(vdsId.toString(), LockMessagesMatchUtil.makeLockingPair(
                LockingGroup.VDS_FENCE,
                EngineMessage.POWER_MANAGEMENT_ACTION_ON_ENTITY_ALREADY_IN_PROGRESS));
    }

    public Map<String, Pair<String, String>> getVdsPoolAndStorageConnectionsLock(Guid vdsId) {
        return Collections.singletonMap(LockingGroup.VDS_POOL_AND_STORAGE_CONNECTIONS.name() + vdsId.toString(),
                LockMessagesMatchUtil.makeLockingPair(
                    LockingGroup.VDS_POOL_AND_STORAGE_CONNECTIONS,
                    EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
    }

    public void acquireHostDevicesLock(Guid vdsId) {
        lockManager.acquireLockWait(new EngineLock(getExclusiveLockForHostDevices(vdsId)));
    }

    public void releaseHostDevicesLock(Guid vdsId) {
        lockManager.releaseLock(new EngineLock(getExclusiveLockForHostDevices(vdsId)));
    }

    private Map<String, Pair<String, String>> getExclusiveLockForHostDevices(Guid vdsId) {
        return Collections.singletonMap(
                vdsId.toString(),
                LockMessagesMatchUtil.makeLockingPair(
                        LockingGroup.HOST_DEVICES,
                        EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
    }

    private static class HostEngineLock extends EngineLock implements AutoCloseable {
        public final String closingMessage;
        private final Logger log;

        public HostEngineLock(Map<String, Pair<String, String>> exclusiveLocks,
                Map<String, Pair<String, String>> sharedLocks, String closingMessage, Logger log) {
            super(exclusiveLocks, sharedLocks);
            this.closingMessage = closingMessage;
            this.log = log;
        }

        @Override
        public void close() {
            super.close();
            log.info(closingMessage);
        }
    }
}
