package org.ovirt.engine.core.bll.scheduling.queries;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.validator.AffinityValidator;
import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.LabelDao;
import org.ovirt.engine.core.dao.VmDao;

public abstract class AffinityGroupsQueryBase<T extends QueryParametersBase> extends QueriesCommandBase<T> {

    @Inject
    private VmDao vmDao;
    @Inject
    private LabelDao labelDao;

    public AffinityGroupsQueryBase(T parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    protected void checkBrokenGroups(List<AffinityGroup> groups) {
        Set<Guid> allLabelIds = groups.stream()
                .flatMap(ag -> Stream.concat(ag.getVmLabels().stream(), ag.getHostLabels().stream()))
                .collect(Collectors.toSet());

        Map<Guid, Label> labelsMap = labelDao.getAllByIds(allLabelIds).stream()
                .collect(Collectors.toMap(Label::getId, label -> label));

        Set<Guid> allVmIds = groups.stream()
                .flatMap(ag -> ag.getVmIds().stream())
                .collect(Collectors.toSet());

        allVmIds.addAll(labelsMap.values().stream()
            .flatMap(label -> label.getVms().stream())
            .collect(Collectors.toList()));

        Map<Guid, VM> runningVmsMap = vmDao.getVmsByIds(allVmIds).stream()
                .filter(vm -> vm.getRunOnVds() != null)
                .collect(Collectors.toMap(VM::getId, vm -> vm));

        for (AffinityGroup group : groups) {
            Set<Guid> vmIds = AffinityValidator.unpackAffinityGroupVmsFromLabels(group, labelsMap);
            List<VM> runningVms =  vmIds.stream()
                    .map(runningVmsMap::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            Set<Guid> hostsWithVms = runningVms.stream()
                    .map(VM::getRunOnVds)
                    .collect(Collectors.toSet());

            Set<Guid> hostIds = AffinityValidator.unpackAffinityGroupHostsFromLabels(group, labelsMap);
            group.setBroken(
                    (group.isVmPositive() && hostsWithVms.size() > 1) ||
                    (group.isVmNegative() && hostsWithVms.size() < runningVms.size()) ||
                    (group.isVdsPositive() && !hostIds.containsAll(hostsWithVms)) ||
                    (group.isVdsNegative() && hostIds.stream().anyMatch(hostsWithVms::contains))
            );
        }
    }
}
