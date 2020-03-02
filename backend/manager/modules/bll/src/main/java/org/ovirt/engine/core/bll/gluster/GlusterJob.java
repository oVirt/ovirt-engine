package org.ovirt.engine.core.bll.gluster;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.scheduling.OvirtGlusterSchedulingService;
import org.ovirt.engine.core.bll.utils.GlusterAuditLogUtil;
import org.ovirt.engine.core.bll.utils.GlusterUtil;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerInfo;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.StepDao;
import org.ovirt.engine.core.dao.StorageDomainDRDao;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VdsDynamicDao;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.VdsStatisticsDao;
import org.ovirt.engine.core.dao.gluster.GlusterBrickDao;
import org.ovirt.engine.core.dao.gluster.GlusterClusterServiceDao;
import org.ovirt.engine.core.dao.gluster.GlusterGeoRepDao;
import org.ovirt.engine.core.dao.gluster.GlusterHooksDao;
import org.ovirt.engine.core.dao.gluster.GlusterOptionDao;
import org.ovirt.engine.core.dao.gluster.GlusterServerDao;
import org.ovirt.engine.core.dao.gluster.GlusterServerServiceDao;
import org.ovirt.engine.core.dao.gluster.GlusterServiceDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;
import org.ovirt.engine.core.dao.gluster.StorageDeviceDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.lock.LockManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class GlusterJob {

    @Inject
    protected VdsDao vdsDao;

    @Inject
    protected VdsStaticDao vdsStaticDao;

    @Inject
    protected VdsDynamicDao vdsDynamicDao;

    @Inject
    protected VdsStatisticsDao vdsStatisticsDao;

    @Inject
    protected ClusterDao clusterDao;

    @Inject
    protected NetworkDao networkDao;

    @Inject
    protected InterfaceDao interfaceDao;

    @Inject
    protected StepDao stepDao;

    @Inject
    protected GlusterVolumeDao volumeDao;

    @Inject
    protected GlusterOptionDao optionDao;

    @Inject
    protected GlusterBrickDao brickDao;

    @Inject
    protected GlusterHooksDao hooksDao;

    @Inject
    protected GlusterServiceDao serviceDao;

    @Inject
    protected GlusterServerServiceDao serverServiceDao;

    @Inject
    protected GlusterClusterServiceDao clusterServiceDao;

    @Inject
    protected GlusterServerDao serverDao;

    @Inject
    protected GlusterGeoRepDao geoRepDao;

    @Inject
    protected StorageDeviceDao storageDeviceDao;

    @Inject
    protected StorageDomainStaticDao storageDomainStaticDao;

    @Inject
    protected StorageDomainDRDao storageDomainDRDao;

    @Inject
    private LockManager lockManager;

    @Inject
    private VDSBrokerFrontend resourceManager;

    @Inject
    protected GlusterUtil glusterUtil;

    @Inject
    protected GlusterAuditLogUtil logUtil;

    @Inject
    private OvirtGlusterSchedulingService scheduler;

    protected final Logger log = LoggerFactory.getLogger(getClass());

    public abstract Collection<GlusterJobSchedulingDetails> getSchedulingDetails();

    public void schedule() {
        getSchedulingDetails().forEach(j -> scheduler.scheduleAFixedDelayJob(
                this, j.getMethodName(), new Class[0], new Class[0], j.getDelay(), j.getDelay(), TimeUnit.SECONDS
        ));
    }

    @SuppressWarnings("unchecked")
    protected List<GlusterServerInfo> fetchServers(VDS upServer) {
        VDSReturnValue result = runVdsCommand(VDSCommandType.GlusterServersList, new VdsIdVDSCommandParametersBase(upServer.getId()));

        return result.getSucceeded() ? (List<GlusterServerInfo>) result.getReturnValue() : null;
    }

    protected VDSReturnValue runVdsCommand(VDSCommandType commandType, VDSParametersBase params) {
        return resourceManager.runVdsCommand(commandType, params);
    }

    /**
     * Acquires a lock on the cluster with given id and locking group {@link LockingGroup#GLUSTER}
     *
     * @param clusterId
     *            ID of the cluster on which the lock is to be acquired
     */
    protected void acquireLock(Guid clusterId) {
        lockManager.acquireLockWait(getEngineLock(clusterId));
    }

    /**
     * Releases the lock held on the cluster having given id and locking group {@link LockingGroup#GLUSTER}
     *
     * @param clusterId
     *            ID of the cluster on which the lock is to be released
     */
    protected void releaseLock(Guid clusterId) {
        lockManager.releaseLock(getEngineLock(clusterId));
    }

    /**
     * Returns an {@link EngineLock} instance that represents a lock on a cluster with given id and the locking group
     * {@link LockingGroup#GLUSTER}
     */
    private EngineLock getEngineLock(Guid clusterId) {
        return new EngineLock(Collections.singletonMap(clusterId.toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.GLUSTER, EngineMessage.ACTION_TYPE_FAILED_GLUSTER_OPERATION_INPROGRESS)), null);
    }

    protected EngineLock acquireGeoRepSessionLock(Guid id) {
        EngineLock lock = new EngineLock(Collections.singletonMap(id.toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.GLUSTER_GEOREP,
                        EngineMessage.ACTION_TYPE_FAILED_GEOREP_SESSION_LOCKED)), null);
        lockManager.acquireLockWait(lock);
        return lock;
    }

    protected EngineLock acquireVolumeSnapshotLock(Guid id) {
        EngineLock lock = new EngineLock(Collections.singletonMap(id.toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.GLUSTER_SNAPSHOT,
                        EngineMessage.ACTION_TYPE_FAILED_VOLUME_SNAPSHOT_LOCKED)), null);
        lockManager.acquireLockWait(lock);
        return lock;
    }

    protected static int getRefreshRate(ConfigValues refreshRateConfig) {
        return Config.<Integer> getValue(refreshRateConfig);
    }
}
