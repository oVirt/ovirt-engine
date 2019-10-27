package org.ovirt.engine.core.bll.kubevirt;

import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.RemoveStorageDomainParameters;
import org.ovirt.engine.core.common.action.RemoveVdsParameters;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.TagDao;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.dao.VmStatisticsDao;
import org.ovirt.engine.core.dao.VmTemplateDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@ApplicationScoped
public class ForceClusterResourcesRemover {

    @Inject
    private VdsStaticDao vdsStaticDao;
    @Inject
    private VmStatisticsDao vmStatisticsDao;
    @Inject
    private VmDynamicDao vmDynamicDao;
    @Inject
    private VmStaticDao vmStaticDao;
    @Inject
    private SnapshotDao snapshotDao;
    @Inject
    private TagDao tagDao;
    @Inject
    private VmTemplateDao vmTemplateDao;
    @Inject
    private BackendInternal backendInternal;
    @Inject
    private ClusterDao clusterDao;

    public void forceRemove(Guid clusterId) {
        TransactionSupport.executeInNewTransaction(() -> {
            List<Guid> vms = vmStaticDao.getAllByCluster(clusterId)
                    .stream()
                    .map(VmBase::getId)
                    .collect(Collectors.toList());
            vms.stream().map(snapshotDao::getAll).flatMap(List::stream).forEach(s -> snapshotDao.remove(s.getId()));
            vmStatisticsDao.removeAll(vms);
            vmDynamicDao.removeAll(vms);
            vms.stream()
                    .map(id -> new Pair<>(id, tagDao.getAllForVm(id.toString())))
                    .forEach(v -> v.getSecond()
                            .stream()
                            .forEach(t -> tagDao.detachVmFromTag(t.getTagId(), v.getFirst())));
            vms.stream().forEach(vmStaticDao::remove);

            vmTemplateDao.getAllForCluster(clusterId).stream().forEach(t -> vmTemplateDao.remove(t.getId()));
            return null;
        });

        List<Guid> hosts = vdsStaticDao.getAllForCluster(clusterId)
                .stream()
                .map(VdsStatic::getId)
                .collect(Collectors.toList());
        hosts.forEach(h -> backendInternal.runInternalAction(ActionType.RemoveVds, new RemoveVdsParameters(h)));

        backendInternal.runInternalAction(ActionType.RemoveStorageDomain,
                new RemoveStorageDomainParameters(clusterId));

        TransactionSupport.executeInNewTransaction(() -> {
            clusterDao.remove(clusterId);
            return null;
        });

        // TODO: Consider removal of disks and pools once supported
    }
}
