package org.ovirt.engine.core.bll.scheduling.queries;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDao;

public abstract class AffinityGroupsQueryBase<T extends QueryParametersBase> extends QueriesCommandBase<T> {

    @Inject
    private VmDao vmDao;

    public AffinityGroupsQueryBase(T parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    protected void checkBrokenGroups(List<AffinityGroup> groups) {
        Set<Guid> allVmIds = groups.stream()
                .flatMap(ag -> ag.getVmIds().stream())
                .collect(Collectors.toSet());

        Map<Guid, VM> runningVmsMap = vmDao.getVmsByIds(allVmIds).stream()
                .filter(vm -> vm.getRunOnVds() != null)
                .collect(Collectors.toMap(VM::getId, vm -> vm));

        for (AffinityGroup group : groups) {
            List<VM> runningVms =  group.getVmIds().stream()
                    .map(runningVmsMap::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            Set<Guid> hostsWithVms = runningVms.stream()
                    .map(VM::getRunOnVds)
                    .collect(Collectors.toSet());

            group.setBroken(
                    (group.isVmPositive() && hostsWithVms.size() > 1) ||
                            (group.isVmNegative() && hostsWithVms.size() < runningVms.size()) ||
                            (group.isVdsPositive() && !group.getVdsIds().containsAll(hostsWithVms)) ||
                            (group.isVdsNegative() && group.getVdsIds().stream().anyMatch(hostsWithVms::contains))
            );
        }
    }
}
