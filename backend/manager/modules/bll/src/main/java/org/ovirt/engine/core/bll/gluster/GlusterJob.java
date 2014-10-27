package org.ovirt.engine.core.bll.gluster;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.utils.ClusterUtils;
import org.ovirt.engine.core.bll.utils.GlusterUtil;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerInfo;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.gluster.GlusterAuditLogUtil;
import org.ovirt.engine.core.dao.StepDao;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.dao.VdsDynamicDAO;
import org.ovirt.engine.core.dao.VdsGroupDAO;
import org.ovirt.engine.core.dao.VdsStaticDAO;
import org.ovirt.engine.core.dao.VdsStatisticsDAO;
import org.ovirt.engine.core.dao.gluster.GlusterBrickDao;
import org.ovirt.engine.core.dao.gluster.GlusterClusterServiceDao;
import org.ovirt.engine.core.dao.gluster.GlusterGeoRepDao;
import org.ovirt.engine.core.dao.gluster.GlusterHooksDao;
import org.ovirt.engine.core.dao.gluster.GlusterOptionDao;
import org.ovirt.engine.core.dao.gluster.GlusterServerDao;
import org.ovirt.engine.core.dao.gluster.GlusterServerServiceDao;
import org.ovirt.engine.core.dao.gluster.GlusterServiceDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.lock.LockManager;
import org.ovirt.engine.core.utils.lock.LockManagerFactory;

public abstract class GlusterJob {

    private final LockManager lockManager = LockManagerFactory.getLockManager();
    protected GlusterAuditLogUtil logUtil = GlusterAuditLogUtil.getInstance();

    /**
     * Required so that the log util can be mocked in the JUnit test
     */
    protected void setLogUtil(GlusterAuditLogUtil logUtil) {
        this.logUtil = logUtil;
    }


    @SuppressWarnings("unchecked")
    protected List<GlusterServerInfo> fetchServers(VDS upServer) {
        VDSReturnValue result =
                Backend.getInstance()
                        .getResourceManager()
                        .RunVdsCommand(VDSCommandType.GlusterServersList,
                                new VdsIdVDSCommandParametersBase(upServer.getId()));

        return result.getSucceeded() ? (List<GlusterServerInfo>) result.getReturnValue() : null;
    }

    protected VDSReturnValue runVdsCommand(VDSCommandType commandType, VDSParametersBase params) {
        return Backend.getInstance().getResourceManager().RunVdsCommand(commandType, params);
    }

    protected ClusterUtils getClusterUtils() {
        return ClusterUtils.getInstance();
    }

    protected VdsStatisticsDAO getVdsStatisticsDao() {
        return DbFacade.getInstance().getVdsStatisticsDao();
    }

    protected VdsStaticDAO getVdsStaticDao() {
        return DbFacade.getInstance().getVdsStaticDao();
    }

    protected VdsDynamicDAO getVdsDynamicDao() {
        return DbFacade.getInstance().getVdsDynamicDao();
    }

    protected InterfaceDao getInterfaceDao() {
        return DbFacade.getInstance().getInterfaceDao();
    }

    protected VdsGroupDAO getClusterDao() {
        return DbFacade.getInstance().getVdsGroupDao();
    }

    protected VdsDAO getVdsDao() {
        return DbFacade.getInstance().getVdsDao();
    }

    protected GlusterVolumeDao getVolumeDao() {
        return DbFacade.getInstance().getGlusterVolumeDao();
    }

    protected GlusterOptionDao getOptionDao() {
        return DbFacade.getInstance().getGlusterOptionDao();
    }

    protected GlusterBrickDao getBrickDao() {
        return DbFacade.getInstance().getGlusterBrickDao();
    }

    protected GlusterHooksDao getHooksDao() {
        return DbFacade.getInstance().getGlusterHooksDao();
    }

    protected GlusterServiceDao getGlusterServiceDao() {
        return DbFacade.getInstance().getGlusterServiceDao();
    }

    protected GlusterServerServiceDao getGlusterServerServiceDao() {
        return DbFacade.getInstance().getGlusterServerServiceDao();
    }

    protected GlusterClusterServiceDao getGlusterClusterServiceDao() {
        return DbFacade.getInstance().getGlusterClusterServiceDao();
    }

    protected GlusterServerDao getGlusterServerDao() {
        return DbFacade.getInstance().getGlusterServerDao();
    }

    protected StepDao getStepDao() {
        return DbFacade.getInstance().getStepDao();
    }

    protected GlusterGeoRepDao getGeoRepDao() {
        return DbFacade.getInstance().getGlusterGeoRepDao();
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
     *
     * @param clusterId
     * @return
     */
    private EngineLock getEngineLock(Guid clusterId) {
        return new EngineLock(Collections.singletonMap(clusterId.toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.GLUSTER, VdcBllMessages.ACTION_TYPE_FAILED_GLUSTER_OPERATION_INPROGRESS)), null);
    }

    protected EngineLock acquireGeoRepSessionLock(Guid id) {
        EngineLock lock = new EngineLock(Collections.singletonMap(id.toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.GLUSTER_GEOREP,
                        VdcBllMessages.ACTION_TYPE_FAILED_GEOREP_SESSION_LOCKED)), null);
        LockManagerFactory.getLockManager().acquireLockWait(lock);
        return lock;
    }

    protected GlusterUtil getGlusterUtil() {
        return GlusterUtil.getInstance();
    }
}
