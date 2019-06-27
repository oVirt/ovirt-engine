package org.ovirt.engine.core.bll.validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.collections.CollectionUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.scheduling.arem.AffinityRulesUtils;
import org.ovirt.engine.core.bll.scheduling.arem.AffinityRulesUtils.AffinityGroupConflicts;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dao.scheduling.AffinityGroupDao;

@Singleton
public class AffinityValidator {
    public static class Result {
        private final ValidationResult validationResult;
        private final BiConsumer<AuditLogable, AuditLogDirector> loggingMethod;

        private Result(ValidationResult validationResult,
                BiConsumer<AuditLogable, AuditLogDirector> loggingMethod) {
            this.validationResult = validationResult;
            this.loggingMethod = loggingMethod;
        }

        public ValidationResult getValidationResult() {
            return validationResult;
        }

        public BiConsumer<AuditLogable, AuditLogDirector> getLoggingMethod() {
            return loggingMethod;
        }

        public static Result newValid(BiConsumer<AuditLogable, AuditLogDirector> loggingMethod) {
            return new Result(ValidationResult.VALID, loggingMethod);
        }

        public static Result newFailed(ValidationResult validationResult) {
            return new Result(validationResult, null);
        }

        public static final Result VALID = newValid((a, b) -> {});
    }

    @Inject
    private AffinityGroupDao affinityGroupDao;

    public Result validateAffinityUpdateForVm(Guid clusterId, Guid vmId, Collection<AffinityGroup> affinityGroups) {
        return validateAffinityUpdate(clusterId, vmId, affinityGroups, AffinityGroup::getVmIds);
    }

    public Result validateAffinityUpdateForHost(Guid clusterId, Guid hostId, Collection<AffinityGroup> affinityGroups) {
        return validateAffinityUpdate(clusterId, hostId, affinityGroups, AffinityGroup::getVdsIds);
    }

    private Result validateAffinityUpdate(Guid clusterId,
            Guid entityId,
            Collection<AffinityGroup> affinityGroups,
            Function<AffinityGroup, List<Guid>> entityIdsExtractor) {

        if (CollectionUtils.isEmpty(affinityGroups)) {
            return Result.VALID;
        }

        Set<Guid> affinityGroupIds =  affinityGroups.stream()
                        .map(AffinityGroup::getId)
                        .collect(Collectors.toSet());

        if (affinityGroupIds.stream().anyMatch(Guid::isNullOrEmpty)) {
            return Result.newFailed(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_INVALID_AFFINITY_GROUP_ID));
        }

        int groupsFromClusterCount = 0;
        boolean groupsChanged = false;
        List<AffinityGroup> groups = affinityGroupDao.getAllAffinityGroupsByClusterId(clusterId);
        for (AffinityGroup group : groups) {
            List<Guid> entityIds = entityIdsExtractor.apply(group);

            if (affinityGroupIds.contains(group.getId())) {
                ++groupsFromClusterCount;
                if (!entityIds.contains(entityId)) {
                    entityIds.add(entityId);
                    groupsChanged = true;
                }
            } else {
                boolean removed = entityIds.remove(entityId);
                if (removed) {
                    groupsChanged = true;
                }
            }
        }

        if (!groupsChanged) {
            return Result.VALID;
        }

        // Not all groups are from the cluster
        if (groupsFromClusterCount < affinityGroupIds.size()) {
            return Result.newFailed(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_AFFINITY_GROUPS_NOT_FROM_SELECTED_CLUSTER));
        }

        return checkAffinityGroupConflicts(groups);
    }

    public static Result checkAffinityGroupConflicts(List<AffinityGroup> groups) {
        if (groups.isEmpty()) {
            return Result.VALID;
        }

        List<BiConsumer<AuditLogable, AuditLogDirector>> loggingMethods = new ArrayList<>();

        List<AffinityGroupConflicts> conflicts = AffinityRulesUtils.checkForAffinityGroupHostsConflict(groups);
        for (AffinityGroupConflicts conflict : conflicts) {
            String affinityGroupsNames = conflict.getAffinityGroups().stream()
                    .map(AffinityGroup::getName)
                    .collect(Collectors.joining(","));

            String hosts = conflict.getHosts().stream()
                    .map(Guid::toString)
                    .collect(Collectors.joining(","));

            String vms = conflict.getVms().stream()
                    .map(Guid::toString)
                    .collect(Collectors.joining(","));

            if (conflict.getType().canBeSaved()) {
                loggingMethods.add((loggable, auditLogDirector) -> {
                    loggable.addCustomValue("AffinityGroups", affinityGroupsNames);
                    loggable.addCustomValue("Hosts", hosts);
                    loggable.addCustomValue("Vms", vms);
                    auditLogDirector.log(loggable, conflict.getAuditLogType());
                });
                continue;
            }

            if (conflict.isVmToVmAffinity()) {
                return Result.newFailed(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_AFFINITY_RULES_COLLISION,
                        String.format("$UnifiedAffinityGroups %1$s", vms),
                        String.format("$negativeAR %1$s", affinityGroupsNames),
                        String.format("$Vms %1$s", conflict.getNegativeVms().stream()
                                .map(Guid::toString)
                                .collect(Collectors.joining(",")))));
            } else {
                return Result.newFailed(new ValidationResult(
                        Arrays.asList(EngineMessage.ACTION_TYPE_FAILED_AFFINITY_HOSTS_RULES_COLLISION,
                                EngineMessage.AFFINITY_GROUPS_LIST,
                                EngineMessage.HOSTS_LIST,
                                EngineMessage.VMS_LIST),
                        String.format("$affinityGroups %1$s", affinityGroupsNames),
                        String.format("$hostsList %1$s", hosts),
                        String.format("$vmsList %1$s", vms)
                ));
            }
        }

        return Result.newValid((auditLogable, auditLogDirector) ->
                loggingMethods.forEach(method -> method.accept(auditLogable, auditLogDirector)));
    }
}
