package org.ovirt.engine.core.bll;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.scheduling.SchedulingManager;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.queries.GetValidHostsForVmsParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ClusterDao;

public class GetValidHostsForVmsQuery<P extends GetValidHostsForVmsParameters> extends QueriesCommandBase<P> {

    @Inject
    private ClusterDao clusterDao;
    @Inject
    private SchedulingManager schedulingManager;

    public GetValidHostsForVmsQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        List<VDS> validHosts = getValidHosts();
        getQueryReturnValue().setReturnValue(validHosts);
    }

    private List<VDS> getValidHosts() {
        Guid clusterId = getParameters().getClusterId();
        if (Guid.Empty.equals(clusterId)) {
            clusterId = getParameters().getVms().get(0).getClusterId();
        }

        Cluster cluster = clusterDao.get(clusterId);

        List<VM> vms = getParameters().getVms();
        List<Guid> blackList = getParameters().getBlackList();
        List<Guid> whiteList = getParameters().getWhiteList();
        List<String> messages = getParameters().getMessages();

        Map<Guid, VDS> hostMap = new HashMap<>();

        List<Set<Guid>> hostsLists = vms.stream()
            .map(vm -> schedulingManager.canSchedule(cluster, vm, blackList, whiteList, messages))
            .map(hosts -> addToMap(hostMap, hosts))
            .map(this::getIdSet)
            .collect(Collectors.toList());

        Set<Guid> validHostIds = hostsLists.isEmpty() ? new HashSet<>() : new HashSet<>(hostsLists.get(0));

        if (hostsLists.size() > 1) {
            validHostIds = hostsLists.stream()
                    .skip(1)
                    .collect(() -> new HashSet<>(hostsLists.get(0)), Set::retainAll, Set::retainAll);
        }

        return validHostIds.stream()
                .map(hostMap::get)
                .collect(Collectors.toList());
    }

    private Set<Guid> getIdSet(List<VDS> hosts) {
        return hosts.stream()
                .map(VDS::getId)
                .collect(Collectors.toSet());
    }

    private List<VDS> addToMap(Map<Guid, VDS> map, List<VDS> hosts) {
        hosts.forEach(host -> map.put(host.getId(), host));
        return hosts;
    }

    @Override
    protected boolean validateInputs() {
        if (!super.validateInputs()) {
            return false;
        }

        if (getParameters().getVms().size() == 0) {
            getQueryReturnValue().setExceptionString(EngineMessage.VM_AT_LEAST_ONE_SPECIFIED.name());
            getQueryReturnValue().setSucceeded(false);
            return false;
        }

        return true;
    }
}
