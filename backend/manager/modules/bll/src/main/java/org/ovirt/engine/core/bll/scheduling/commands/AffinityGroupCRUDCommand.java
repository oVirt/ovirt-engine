package org.ovirt.engine.core.bll.scheduling.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.AffinityValidator;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.common.scheduling.parameters.AffinityGroupCRUDParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.LabelDao;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.dao.scheduling.AffinityGroupDao;

public abstract class AffinityGroupCRUDCommand <T extends AffinityGroupCRUDParameters> extends CommandBase<T> {

    protected static final String Entity_VM = "VM";
    protected static final String Entity_VDS = "VDS";
    protected static final String Entity_LABEL = "LABEL";

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private VmStaticDao vmStaticDao;
    @Inject
    private VdsStaticDao vdsStaticDao;
    @Inject
    private AffinityGroupDao affinityGroupDao;
    @Inject
    private LabelDao labelDao;

    AffinityGroup affinityGroup = null;

    public AffinityGroupCRUDCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void init() {
        super.init();
        if (getAffinityGroup() != null) {
            setClusterId(getAffinityGroup().getClusterId());
            addCustomValue("affinityGroupName", getAffinityGroup().getName());
        }
    }

    protected boolean validateParameters() {
        if (getCluster() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_INVALID_CLUSTER_FOR_AFFINITY_GROUP);
        }

        return validateVms() &&
                validateHosts() &&
                validateLabels() &&
                affinityGroupsWithoutConflict(getParameters().getAffinityGroup());

    }

    private boolean validateVms() {
        List<Guid> vmIds = getParameters().getAffinityGroup().getVmIds();
        if (vmIds.isEmpty()) {
            return true;
        }

        Map<Guid, VmStatic> vms = vmStaticDao.getByIds(vmIds).stream()
                .collect(Collectors.toMap(VmBase::getId, vm -> vm));

        Set<Guid> vmSet = new HashSet<>();
        for (Guid vmId : vmIds) {
            VmStatic vmStatic = vms.get(vmId);
            if (vmStatic == null) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_INVALID_ENTITY_FOR_AFFINITY_GROUP, String
                        .format("$entity %s", Entity_VM));
            }
            if (!Objects.equals(vmStatic.getClusterId(), getClusterId())) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_ENTITY_NOT_IN_AFFINITY_GROUP_CLUSTER, String
                        .format("$entity %s", Entity_VM));
            }
            if (vmSet.contains(vmStatic.getId())) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_DUPLICATE_ENTITY_IN_AFFINITY_GROUP, String
                        .format("$entity %s", Entity_VM));
            }

            vmSet.add(vmStatic.getId());
        }
        return true;
    }

    private boolean validateHosts() {
        List<Guid> hostIds = getParameters().getAffinityGroup().getVdsIds();
        if (hostIds.isEmpty()) {
            return true;
        }

        Map<Guid, VdsStatic> hosts = vdsStaticDao.getByIds(hostIds).stream()
                .collect(Collectors.toMap(VdsStatic::getId, host -> host));

        Set<Guid> vdsSet = new HashSet<>();
        for (Guid vdsId : hostIds) {
            VdsStatic vdsStatic = hosts.get(vdsId);
            if (vdsStatic == null) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_INVALID_ENTITY_FOR_AFFINITY_GROUP, String
                        .format("$entity %s", Entity_VDS));
            }
            if (!Objects.equals(vdsStatic.getClusterId(), getClusterId())) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_ENTITY_NOT_IN_AFFINITY_GROUP_CLUSTER, String
                        .format("$entity %s", Entity_VDS));
            }
            if (vdsSet.contains(vdsStatic.getId())) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_DUPLICATE_ENTITY_IN_AFFINITY_GROUP, String
                        .format("$entity %s", Entity_VDS));
            }

            vdsSet.add(vdsStatic.getId());
        }

        return true;
    }

    private boolean validateLabels() {
        List<Guid> vmLabels = getParameters().getAffinityGroup().getVmLabels();
        List<Guid> hostLabels = getParameters().getAffinityGroup().getHostLabels();

        Set<Guid> allLabelIds = new HashSet<>(vmLabels);
        allLabelIds.addAll(hostLabels);

        Map<Guid, Label> labels = labelDao.getAllByIds(allLabelIds).stream()
                .collect(Collectors.toMap(Label::getId, label -> label));

        if (vmLabels.stream().map(labels::get).anyMatch(Objects::isNull) ||
                hostLabels.stream().map(labels::get).anyMatch(Objects::isNull)) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_INVALID_LABEL_FOR_AFFINITY_GROUP);
        }

        // TODO - check label cluster ID, once labels have cluster ID

        Set<Guid> uniqueVmLabels = new HashSet<>(vmLabels);
        Set<Guid> uniqueHostLabels = new HashSet<>(hostLabels);
        if (uniqueVmLabels.size() < vmLabels.size() || uniqueHostLabels.size() < hostLabels.size()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_DUPLICATE_LABEL_IN_AFFINITY_GROUP);
        }

        return true;
    }

    private boolean affinityGroupsWithoutConflict(AffinityGroup affinityGroup) {
        List<AffinityGroup> affinityGroups =
                affinityGroupDao.getAllAffinityGroupsWithFlatLabelsByClusterId(affinityGroup.getClusterId());

        List<Guid> labelIds = new ArrayList<>(affinityGroup.getVmLabels());
        labelIds.addAll(affinityGroup.getHostLabels());

        Map<Guid, Label> labels = labelDao.getAllByIds(labelIds).stream()
                .collect(Collectors.toMap(Label::getId, label -> label));

        AffinityGroup affinityGroupCopy = new AffinityGroup(affinityGroup);
        AffinityValidator.unpackAffinityGroupLabels(affinityGroupCopy, labels);

        // Replace the existing affinity group by the updated copy
        affinityGroups.removeIf(g -> g.getId().equals(affinityGroupCopy.getId()));
        affinityGroups.add(affinityGroupCopy);

        AffinityValidator.Result result = AffinityValidator.checkAffinityGroupConflicts(affinityGroups);
        if (result.getValidationResult().isValid()) {
            result.getLoggingMethod().accept(this, auditLogDirector);
        }
        return validate(result.getValidationResult());
    }

    protected AffinityGroup getAffinityGroup() {
        if (affinityGroup == null) {
            affinityGroup = affinityGroupDao.get(getParameters().getAffinityGroupId());
        }
        return affinityGroup;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(getClusterId(),
                VdcObjectType.Cluster,
                getActionType().getActionGroup()));
    }

    @Override
    public Guid getClusterId() {
        return getAffinityGroup().getClusterId();
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__TYPE__AFFINITY_GROUP);
    }
}
