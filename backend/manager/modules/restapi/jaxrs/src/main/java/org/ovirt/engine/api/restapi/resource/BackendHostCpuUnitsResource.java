package org.ovirt.engine.api.restapi.resource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.api.model.HostCpuUnit;
import org.ovirt.engine.api.model.HostCpuUnits;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.model.Vms;
import org.ovirt.engine.api.resource.HostCpuUnitsResource;
import org.ovirt.engine.api.restapi.types.Mapper;
import org.ovirt.engine.api.restapi.types.VmMapper;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsCpuUnit;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.IdsQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.utils.CpuPinningHelper;
import org.ovirt.engine.core.compat.Guid;

public class BackendHostCpuUnitsResource extends AbstractBackendCollectionResource<HostCpuUnit, Object> implements HostCpuUnitsResource {

    private Guid hostId;

    public BackendHostCpuUnitsResource(Guid hostId) {
        super(HostCpuUnit.class, Object.class);
        this.hostId = hostId;
    }

    @Override
    public HostCpuUnits list() {
        VDS host = getHost();

        if (host.getCpuTopology() == null) {
            return new HostCpuUnits();
        }

        List<VM> vms = getMonitoredVms();

        HostCpuUnits hostCpuUnits = new HostCpuUnits();
        Mapper<VdsCpuUnit, HostCpuUnit> mapper = getMapper(VdsCpuUnit.class, HostCpuUnit.class);
        for (VdsCpuUnit vdsCpuUnit : host.getCpuTopology()) {
            hostCpuUnits.getHostCpuUnits().add(mapper.map(vdsCpuUnit, null));
        }

        addPinnedVms(hostCpuUnits.getHostCpuUnits(), vms);
        addVdsmCpusAffinity(hostCpuUnits.getHostCpuUnits(), host.getVdsmCpusAffinity());

        hostCpuUnits.getHostCpuUnits().forEach(this::addLinks);
        return hostCpuUnits;
    }

    private void addPinnedVms(List<HostCpuUnit> hostCpuUnits, List<VM> vms) {
        for (VM vm : vms) {
            String vmPinning = vm.getVmPinning();
            if (vmPinning == null) {
                continue;
            }
            Set<Integer> vmPinnedCpus = CpuPinningHelper.getAllPinnedPCpus(vmPinning);
            hostCpuUnits.stream()
                    .filter(hostCpuUnit -> vmPinnedCpus.contains(hostCpuUnit.getCpuId()))
                    .forEach(hostCpuUnit -> {
                        Vm v = new Vm();
                        v.setId(vm.getId().toString());
                        v.setName(vm.getName());
                        v.setCpuPinningPolicy(VmMapper.map(vm.getCpuPinningPolicy()));

                        if (hostCpuUnit.getVms() == null) {
                            hostCpuUnit.setVms(new Vms());
                        }
                        hostCpuUnit.getVms().getVms().add(v);
                    });
        }
    }

    private void addVdsmCpusAffinity(List<HostCpuUnit> hostCpuUnits, String affinityString) {
        Integer affinity = parseAffinity(affinityString);
        hostCpuUnits.stream()
                .filter(hostCpuUnit -> hostCpuUnit.getCpuId() == affinity)
                .forEach(hostCpuUnit -> hostCpuUnit.setRunsVdsm(true));
    }

    private int parseAffinity(String affinity) {
        try {
            return Integer.parseInt(affinity);
        } catch (Exception e) {
            return -1;
        }
    }

    private VDS getHost() {
        QueryReturnValue queryReturnValue = runQuery(QueryType.GetVdsByVdsId, new IdQueryParameters(hostId));
        if (!queryReturnValue.getSucceeded()) {
            try {
                backendFailure(queryReturnValue.getExceptionString());
            } catch (BackendFailureException ex) {
                handleError(ex, false);
            }
        }

        VDS host = queryReturnValue.getReturnValue();
        return host;
    }

    private List<VM> getMonitoredVms() {
        QueryReturnValue queryReturnValue =
                runQuery(QueryType.GetAllVmsRunningForMultipleVds, new IdsQueryParameters(Arrays.asList(hostId)));
        if (!queryReturnValue.getSucceeded()) {
            try {
                backendFailure(queryReturnValue.getExceptionString());
            } catch (BackendFailureException ex) {
                handleError(ex, false);
            }
        }

        Map<Guid, List<VM>> vmsMap = queryReturnValue.getReturnValue();
        List<VM> vms = vmsMap.get(hostId);
        return vms == null ? Collections.emptyList() : vms;
    }
}
