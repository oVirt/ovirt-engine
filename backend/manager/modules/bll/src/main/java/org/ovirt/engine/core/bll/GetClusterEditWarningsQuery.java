package org.ovirt.engine.core.bll;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

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

        List<ClusterEditWarnings.Warning> hostWarnings = getProblematicEntities(oldCluster, newCluster, hostCheckers,
                cluster -> vdsDao.getAllForVdsGroup(cluster.getId()));

        List<ClusterEditWarnings.Warning> vmWarnings = getProblematicEntities(oldCluster, newCluster, vmCheckers,
                cluster -> vmDao.getAllForVdsGroup(cluster.getId()));

        setReturnValue(new ClusterEditWarnings(hostWarnings, vmWarnings));
    }

    private static <T extends Nameable> List<ClusterEditWarnings.Warning> getProblematicEntities(
            VDSGroup oldCluster,
            VDSGroup newCluster,
            Iterable<ClusterEditChecker<T>> checkers,
            ClusterEntityResolver<T> entityResolver) {

        List<ClusterEditWarnings.Warning> warnings = new ArrayList<>();

        List<ClusterEditChecker<T>> applicableChecks = stream(checkers.spliterator(), false)
                .filter(checker -> checker.isApplicable(oldCluster, newCluster))
                .collect(toList());

        if (!applicableChecks.isEmpty()) {
            List<T> entities = entityResolver.getClusterEntities(oldCluster);
            for (ClusterEditChecker<T> checker : applicableChecks) {
                ClusterEditWarnings.Warning warning = new ClusterEditWarnings.Warning(checker.getMainMessage());
                entities.stream().filter(entity -> !checker.check(entity)).forEach(entity ->
                        warning.getDetailsByName().put(entity.getName(), checker.getDetailMessage(entity)));
                if (!warning.isEmpty()) {
                    warnings.add(warning);
                }
            }
        }

        return warnings;
    }

    @FunctionalInterface
    private interface ClusterEntityResolver<T> {
        List<T> getClusterEntities(VDSGroup cluster);
    }
}
