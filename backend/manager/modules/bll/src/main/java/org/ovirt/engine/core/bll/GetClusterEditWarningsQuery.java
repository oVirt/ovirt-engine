package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.ClusterEditWarnings;
import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.interfaces.BackendLocal;
import org.ovirt.engine.core.common.queries.ClusterEditParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VmDao;

public class GetClusterEditWarningsQuery<P extends ClusterEditParameters> extends QueriesCommandBase<P> {

    @Inject
    private VdsDao vdsDao;

    @Inject
    private VmDao vmDao;

    @Inject
    @Any
    private Instance<ClusterEditChecker<VDS>> hostCheckers;

    @Inject
    @Any
    private Instance<ClusterEditChecker<VM>> vmCheckers;

    @Inject
    private BackendLocal backend;

    public GetClusterEditWarningsQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        final VDSGroup oldCluster = backend.runQuery(VdcQueryType.GetVdsGroupById, getParameters()).getReturnValue();
        VDSGroup newCluster = getParameters().getNewCluster();

        List<ClusterEditWarnings.Warning> hostWarnings = getProblematicEntities(oldCluster, newCluster, hostCheckers, new ClusterEntityResolver<VDS>() {
            @Override
            public List<VDS> getClusterEntities(VDSGroup cluster) {
                return vdsDao.getAllForVdsGroup(cluster.getId());
            }
        });

        List<ClusterEditWarnings.Warning> vmWarnings = new ArrayList<>();
        if (oldCluster.supportsVirtService() && newCluster.supportsVirtService()) {
            vmWarnings = getProblematicEntities(oldCluster, newCluster, vmCheckers, new ClusterEntityResolver<VM>() {
                @Override
                public List<VM> getClusterEntities(VDSGroup cluster) {
                    return vmDao.getAllForVdsGroup(cluster.getId());
                }
            });
        }

        setReturnValue(new ClusterEditWarnings(hostWarnings, vmWarnings));
    }

    private static <T extends Nameable> List<ClusterEditWarnings.Warning> getProblematicEntities(
            VDSGroup oldCluster,
            VDSGroup newCluster,
            Iterable<ClusterEditChecker<T>> checkers,
            ClusterEntityResolver<T> entityResolver) {

        List<ClusterEditWarnings.Warning> warnings = new ArrayList<>();

        List<ClusterEditChecker<T>> checks = getApplicableChecks(oldCluster, newCluster, checkers);
        if (!checks.isEmpty()) {
            List<T> entities = entityResolver.getClusterEntities(oldCluster);
            for (ClusterEditChecker<T> checker : checks) {
                ClusterEditWarnings.Warning warning = new ClusterEditWarnings.Warning(checker.getMainMessage());
                for (T entity : entities) {
                    if (!checker.check(entity)) {
                        warning.getDetailsByName().put(entity.getName(), checker.getDetailMessage(entity));
                    }
                }
                if (!warning.isEmpty()) {
                    warnings.add(warning);
                }
            }
        }

        return warnings;
    }

    private static <T> List<ClusterEditChecker<T>> getApplicableChecks(
            VDSGroup oldCluster,
            VDSGroup newCluster,
            Iterable<ClusterEditChecker<T>> checkers) {

        List<ClusterEditChecker<T>> result = new ArrayList<>();
        for (ClusterEditChecker<T> checker : checkers) {
            if (checker.isApplicable(oldCluster, newCluster)) {
                result.add(checker);
            }
        }

        return result;
    }

    private interface ClusterEntityResolver<T> {
        List<T> getClusterEntities(VDSGroup cluster);
    }
}
