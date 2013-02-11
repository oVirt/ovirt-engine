package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.common.action.MigrateVmToServerParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.linq.Function;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

/**
 * Base class for load balancing algorithms. Load balance flow is the same for
 * all algorithms: Initialization all Vdss lists(under utilized, over utilized
 * and ready for migration), Over utilized servers treatment and under utilized
 * servers treatment.
 */
public abstract class VdsLoadBalancingAlgorithm {
    public VdsLoadBalancingAlgorithm(VDSGroup group) {
        setVdsGroup(group);
    }

    private VDSGroup privateVdsGroup;

    public VDSGroup getVdsGroup() {
        return privateVdsGroup;
    }

    private void setVdsGroup(VDSGroup value) {
        privateVdsGroup = value;
    }

    /**
     * This property contain list of all servers from db, used in load balancing
     * algorithm. Server must be up and without any desktop during migration
     */
    private List<VDS> privateAllRelevantVdss;

    public List<VDS> getAllRelevantVdss() {
        return privateAllRelevantVdss;
    }

    private void setAllRelevantVdss(List<VDS> value) {
        privateAllRelevantVdss = value;
    }

    /**
     * List of all over utilized servers
     */
    private Map<Guid, VDS> privateOverUtilizedServers;

    protected Map<Guid, VDS> getOverUtilizedServers() {
        return privateOverUtilizedServers;
    }

    protected void setOverUtilizedServers(Map<Guid, VDS> value) {
        privateOverUtilizedServers = value;
    }

    /**
     * List of all over normal servers, can be used to migrating desktops to it
     */
    private Map<Guid, VDS> privateReadyToMigrationServers;

    protected Map<Guid, VDS> getReadyToMigrationServers() {
        return privateReadyToMigrationServers;
    }

    protected void setReadyToMigrationServers(Map<Guid, VDS> value) {
        privateReadyToMigrationServers = value;
    }

    /**
     * List of all under utilized servers. This servers can be empty to power
     * saving
     */
    private Map<Guid, VDS> privateUnderUtilizedServers;

    protected Map<Guid, VDS> getUnderUtilizedServers() {
        return privateUnderUtilizedServers;
    }

    protected void setUnderUtilizedServers(Map<Guid, VDS> value) {
        privateUnderUtilizedServers = value;
    }

    /**
     * Factory method to create necessary load balancing algorithm
     *
     * @return
     */
    public static VdsLoadBalancingAlgorithm CreateVdsLoadBalancingAlgorithm(VDSGroup group) {
        // return new VmCountVdsLoadBalancingAlgorithm();
        return new VdsCpuVdsLoadBalancingAlgorithm(group);
    }

    public void LoadBalance() {
        setAllRelevantVdss(DbFacade.getInstance().getVdsDao().getAllForVdsGroupWithoutMigrating(getVdsGroup().getId()));
        log.infoFormat("VdsLoadBalancer: number of relevant vdss (no migration, no pending): {0}.",
                getAllRelevantVdss().size());
        InitOverUtilizedList();
        InitReadyToMigrationList();
        InitUnderUtilizedList();
        if (getOverUtilizedServers().size() != 0
                && (getReadyToMigrationServers().size() != 0 || getUnderUtilizedServers().size() != 0)) {
            ProceedOverUtilizedServers();
        }
        if (getUnderUtilizedServers().size() > 0
                && (getReadyToMigrationServers().size() > 0 || getUnderUtilizedServers().size() > 1)) {
            ProceedUnderUtilizedServers();
        }
    }

    protected abstract void InitOverUtilizedList();

    protected abstract void InitReadyToMigrationList();

    protected abstract void InitUnderUtilizedList();

    private void ProceedOverUtilizedServers() {
        List<Guid> overUtilizedServersIds = LinqUtils.foreach(getOverUtilizedServers().values(),
                new Function<VDS, Guid>() {
                    @Override
                    public Guid eval(VDS vds) {
                        return vds.getId();
                    }
                });
        for (Guid vdsId : overUtilizedServersIds) {
            VDS vds = getOverUtilizedServers().get(vdsId);
            log.infoFormat("VdsLoadBalancer: Server {0} decided as overutilized", vds.getVdsName());
            java.util.List<VM> vms = getMigrableVmsRunningOnVds(vdsId);
            if (vms.size() != 0) {

                /**
                 * Get random desktop from under utilized server and try to
                 * migrate it to other server
                 */
                VM vm = getBestVmToMigrate(vms, vdsId);
                Map<Guid, VDS> currentList = getReadyToMigrationServers();
                /**
                 * Try to find server in Ready to Migration list for migrate
                 * desktop to
                 */
                List<VDS> candidates = GetMigrationCandidates(currentList, vm);
                VDS destinationVds = null;
                if (candidates.isEmpty()) {
                    /**
                     * No available server in ReadyToMigrationList Try to find
                     * server from UnderUtilized list for migrate desktop to
                     */
                    currentList = getUnderUtilizedServers();
                    candidates = GetMigrationCandidates(currentList, vm);
                    if (!candidates.isEmpty()) {
                        destinationVds = candidates.get(candidates.size() - 1);
                    }
                } else {
                    destinationVds = candidates.get(0);
                }
                if (destinationVds == null) {
                    log.infoFormat(
                            "VdsLoadBalancer: Server {0} detected as overutilized. Failed to found another server to migrate its vms",
                            vds.getVdsName());
                } else {
                    Guid destinationVdsId = destinationVds.getId();
                    /**
                     * Migrate vm from OverUtilezed server
                     */
                    MigrateVmToServerParameters parameters =
                        new MigrateVmToServerParameters(false, vm.getId(), destinationVdsId);
                    Backend.getInstance().runInternalAction(VdcActionType.MigrateVmToServer,
                            parameters,
                            ExecutionHandler.createInternalJobContext());
                    /**
                     * Remove server from list
                     */
                    currentList.remove(destinationVdsId);
                    log.infoFormat("VdsLoadBalancer: Desktop {0} migrated from overutilized server {1} to server {2}",
                            vm.getName(), vds.getVdsName(), destinationVds.getVdsName());

                }
            } else {
                log.info("VdsLoadBalancer: No vms found to migrate on this server");
            }
        }
    }

    private void ProceedUnderUtilizedServers() {
        List<Guid> underUtilizedServersIds = LinqUtils.foreach(getUnderUtilizedServers().values(),
                new Function<VDS, Guid>() {
                    @Override
                    public Guid eval(VDS vds) {
                        return vds.getId();
                    }
                });
        Set<Guid> processed = new HashSet<Guid>();
        for (Guid vdsId : underUtilizedServersIds) {
            if (!processed.contains(vdsId)) {
                VDS vds = getUnderUtilizedServers().get(vdsId);
                java.util.List<VM> vms = getMigrableVmsRunningOnVds(vdsId);
                if (vms.size() != 0) {
                    VM vm = getBestVmToMigrate(vms, vdsId);
                    /**
                     * Get random desktop from under utilized server and try to
                     * migrate it to other server
                     */
                    Map<Guid, VDS> currentList = getReadyToMigrationServers();
                    List<VDS> candidates = GetMigrationCandidates(currentList, vm);
                    VDS destinationVds = null;
                    if (candidates.isEmpty()) {
                        /**
                         * Ready to Migrate servers not contain server to
                         * migrate current desktop, Try to find other
                         * UnderUtilized server with maximum count of running
                         * desktops
                         */
                        currentList = getUnderUtilizedServers();
                        final Guid vdsId1 = vdsId;
                        candidates = LinqUtils.filter(GetMigrationCandidates(currentList, vm), new Predicate<VDS>() {
                            @Override
                            public boolean eval(VDS a) {
                                return !a.getId().equals(vdsId1);
                            }
                        });
                        if (!candidates.isEmpty()) {
                            destinationVds = Collections.max(candidates, new Comparator<VDS>() {
                                @Override
                                public int compare(VDS o1, VDS o2) {
                                    return o1.getVmCount() - o2.getVmCount();
                                }
                            });
                        }
                    } else {
                        destinationVds = candidates.get(0);
                    }
                    if (destinationVds == null) {
                        log.infoFormat(
                                "Server {0} detected as underutilized. Failed to found another server to migrate its vms",
                                vds.getVdsName());
                    } else {
                        Guid destinationVdsId = destinationVds.getId();
                        MigrateVmToServerParameters parameters =
                            new MigrateVmToServerParameters(false, vm.getId(), destinationVdsId);
                        Backend.getInstance().runInternalAction(VdcActionType.MigrateVmToServer,
                                parameters,
                                ExecutionHandler.createInternalJobContext());
                        currentList.remove(destinationVdsId);
                        log.infoFormat(
                                "VdsLoadBalancer: Desktop {0} migrated from underutilized server {1} to server {2}",
                                vm.getName(), vds.getVdsName(), destinationVds.getVdsName());
                        processed.add(destinationVdsId);
                    }
                } else {
                    log.infoFormat("VdsLoadBalancer: No vms found to migrate on this server {0}", vds.getVdsName());
                }
                getUnderUtilizedServers().remove(vdsId); // remove the smallest
                                                         // underutilized vds
                                                         // which was already
                                                         // processed, in
                                                         // order to not
                                                         // passed vm on it
            }
        }
    }

    private java.util.List<VM> getMigrableVmsRunningOnVds(Guid vdsId) {
        List<VM> vmsFromDB = DbFacade.getInstance().getVmDao().getAllRunningForVds(vdsId);

        List<VM> vms = LinqUtils.filter(vmsFromDB, new Predicate<VM>() {
            @Override
            public boolean eval(VM v) {
                return v.getMigrationSupport() == MigrationSupport.MIGRATABLE;
            }
        });

        return vms;
    }

    private List<VDS> GetMigrationCandidates(Map<Guid, VDS> list, final VM vm) {
        return LinqUtils.filter(list.values(), new Predicate<VDS>() {
            @Override
            public boolean eval(VDS p) {
                return (p.getVdsGroupId().equals(vm.getVdsGroupId())
                        && RunVmCommandBase.hasMemoryToRunVM(p, vm)
                        && RunVmCommandBase.hasCpuToRunVM(p, vm));
            }
        });
    }

    protected abstract VM getBestVmToMigrate(List<VM> vms, Guid vdsId);

    private static Log log = LogFactory.getLog(VdsLoadBalancingAlgorithm.class);
}
