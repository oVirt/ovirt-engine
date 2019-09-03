package org.ovirt.engine.core.bll.validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dao.LabelDao;
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
    @Inject
    private LabelDao labelDao;

    public Result validateAffinityUpdateForVm(Guid clusterId, Guid vmId, Collection<AffinityGroup> affinityGroups, Collection<Label> labels) {
        return validateAffinityUpdate(clusterId, vmId, affinityGroups, labels, AffinityGroup::getVmIds, Label::getVms);
    }

    public Result validateAffinityUpdateForHost(Guid clusterId, Guid hostId, Collection<AffinityGroup> affinityGroups, Collection<Label> labels) {
        return validateAffinityUpdate(clusterId, hostId, affinityGroups, labels, AffinityGroup::getVdsIds, Label::getHosts);
    }

    private Result validateAffinityUpdate(Guid clusterId,
            Guid entityId,
            Collection<AffinityGroup> affinityGroups,
            Collection<Label> labels,
            Function<AffinityGroup, Collection<Guid>> entityIdsExtractorFromGroup,
            Function<Label, Collection<Guid>> entityIdsExtractorFromLabel) {

        if (CollectionUtils.isEmpty(affinityGroups) && CollectionUtils.isEmpty(labels)) {
            return Result.VALID;
        }

        Set<Guid> affinityGroupIds = affinityGroups == null ? null :
                affinityGroups.stream()
                        .map(AffinityGroup::getId)
                        .collect(Collectors.toSet());

        Set<Guid> labelIds = labels == null ? null :
                labels.stream()
                        .map(Label::getId)
                        .collect(Collectors.toSet());

        if (affinityGroupIds != null && affinityGroupIds.stream().anyMatch(Guid::isNullOrEmpty)) {
            return Result.newFailed(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_INVALID_AFFINITY_GROUP_ID));
        }

        if (labelIds != null && labelIds.stream().anyMatch(Guid::isNullOrEmpty)) {
            return Result.newFailed(new ValidationResult(EngineMessage.AFFINITY_LABEL_NOT_EXISTS));
        }

        Map<Guid, Label> labelMap = labelDao.getAllByClusterId(clusterId).stream()
                .collect(Collectors.toMap(Label::getId, label -> label));

        List<AffinityGroup> groups = affinityGroupDao.getAllAffinityGroupsByClusterId(clusterId);

        Pair<Boolean, Boolean> labelModResult = modifyLabelsOrGroups(labelMap.values(), entityId, labelIds, entityIdsExtractorFromLabel);
        Pair<Boolean, Boolean> groupModResult = modifyLabelsOrGroups(groups, entityId, affinityGroupIds, entityIdsExtractorFromGroup);

        if (!labelModResult.getFirst() && !groupModResult.getFirst()) {
            return Result.VALID;
        }

        // Not all labels are from the cluster
        if (!labelModResult.getSecond()) {
            return Result.newFailed(new ValidationResult(EngineMessage.AFFINITY_LABEL_NOT_FROM_SELECTED_CLUSTER));
        }

        // Not all groups are from the cluster
        if (!groupModResult.getSecond()) {
            return Result.newFailed(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_AFFINITY_GROUPS_NOT_FROM_SELECTED_CLUSTER));
        }

        groups.forEach(group -> unpackAffinityGroupLabels(group, labelMap));

        return checkAffinityGroupConflicts(groups);
    }

    private<T extends BusinessEntity<Guid>> Pair<Boolean, Boolean> modifyLabelsOrGroups(Collection<T> elements,
            Guid entityId,
            Set<Guid> elementsWithEntity,
            Function<T, Collection<Guid>> entityIdsExtractor) {

        boolean collectionChanged = false;
        boolean allElementsFound = true;

        if (elementsWithEntity == null) {
            return new Pair<>(collectionChanged, allElementsFound);
        }

        int elementsFound = 0;
        for (T element : elements) {
            Collection<Guid> entityIds = entityIdsExtractor.apply(element);

            if (elementsWithEntity.contains(element.getId())) {
                ++elementsFound;
                if (!entityIds.contains(entityId)) {
                    entityIds.add(entityId);
                    collectionChanged = true;
                }
            } else {
                boolean removed = entityIds.remove(entityId);
                if (removed) {
                    collectionChanged = true;
                }
            }
        }

        allElementsFound = elementsFound == elementsWithEntity.size();
        return new Pair<>(collectionChanged, allElementsFound);
    }

    public static void unpackAffinityGroupLabels(AffinityGroup group, Map<Guid, Label> labels) {
        Set<Guid> vmIds = unpackAffinityGroupVmsFromLabels(group, labels);
        Set<Guid> hostIds = unpackAffinityGroupHostsFromLabels(group, labels);

        group.setVmIds(new ArrayList<>(vmIds));
        group.setVdsIds(new ArrayList<>(hostIds));
        group.setVmLabels(Collections.emptyList());
        group.setHostLabels(Collections.emptyList());
    }

    public static Set<Guid> unpackAffinityGroupVmsFromLabels(AffinityGroup group, Map<Guid, Label> labels) {
        Set<Guid> vmIds = group.getVmLabels().stream()
                .map(labels::get)
                .flatMap(label -> label.getVms().stream())
                .collect(Collectors.toSet());

        vmIds.addAll(group.getVmIds());
        return vmIds;
    }

    public static Set<Guid> unpackAffinityGroupHostsFromLabels(AffinityGroup group, Map<Guid, Label> labels) {
        Set<Guid> hostIds = group.getHostLabels().stream()
                .map(labels::get)
                .flatMap(label -> label.getHosts().stream())
                .collect(Collectors.toSet());

        hostIds.addAll(group.getVdsIds());
        return hostIds;
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
