package org.ovirt.engine.core.bll.scheduling.commands;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.scheduling.arem.AffinityRulesUtils;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.common.scheduling.parameters.AffinityGroupCRUDParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.dao.scheduling.AffinityGroupDao;

public abstract class AffinityGroupCRUDCommand extends CommandBase<AffinityGroupCRUDParameters> {

    private static final String Entity_VM = "VM";
    private static final String Entity_VDS = "VDS";

    @Inject
    private VmStaticDao vmStaticDao;
    @Inject
    private VdsStaticDao vdsStaticDao;
    @Inject
    private AffinityGroupDao affinityGroupDao;

    AffinityGroup affinityGroup = null;

    public AffinityGroupCRUDCommand(AffinityGroupCRUDParameters parameters, CommandContext cmdContext) {
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
        if (getParameters().getAffinityGroup().getVmIds() != null) {
            VmStatic vmStatic = null;
            Set<Guid> vmSet = new HashSet<>();
            for (Guid vmId : getParameters().getAffinityGroup().getVmIds()) {
                vmStatic = vmStaticDao.get(vmId);
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
                } else {
                    vmSet.add(vmStatic.getId());
                }
            }
        }
        if (getParameters().getAffinityGroup().getVdsIds() != null) {
            VdsStatic vdsStatic = null;
            Set<Guid> vdsSet = new HashSet<>();
            for (Guid vdsId : getParameters().getAffinityGroup().getVdsIds()) {
                vdsStatic = vdsStaticDao.get(vdsId);
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
                } else {
                    vdsSet.add(vdsStatic.getId());
                }
            }
        }

        return affinityGroupsWithoutConflict(getParameters().getAffinityGroup());
    }

    private boolean affinityGroupsWithoutConflict(AffinityGroup affinityGroup) {
        List<AffinityGroup> affinityGroups =
                affinityGroupDao.getAllAffinityGroupsByClusterId(affinityGroup.getClusterId());

        // Replace the existing affinity group by the updated copy
        for (Iterator<AffinityGroup> it = affinityGroups.iterator(); it.hasNext(); ) {
            AffinityGroup g = it.next();
            if (g.getId().equals(affinityGroup.getId())) {
                it.remove();
            }
        }
        affinityGroups.add(affinityGroup);

        List<AffinityRulesUtils.AffinityGroupConflicts> conflicts = AffinityRulesUtils
                .checkForAffinityGroupHostsConflict(affinityGroups);

        for (AffinityRulesUtils.AffinityGroupConflicts conflict : conflicts) {

            String affinityGroupsNames = AffinityRulesUtils.getAffinityGroupsNames(conflict.getAffinityGroups());
            String hosts = conflict.getHosts().stream()
                    .map(id -> id.toString())
                    .collect(Collectors.joining(","));
            String vms = conflict.getVms().stream()
                    .map(id -> id.toString())
                    .collect(Collectors.joining(","));

            if (conflict.getType().canBeSaved()) {
                addCustomValue("AffinityGroups", affinityGroupsNames);
                addCustomValue("Hosts", hosts);
                addCustomValue("Vms", vms);
                auditLogDirector.log(this, conflict.getAuditLogType());
            } else {
                if (conflict.isVmToVmAffinity()) {
                    return failValidation(EngineMessage.ACTION_TYPE_FAILED_AFFINITY_RULES_COLLISION,
                            String.format("$UnifiedAffinityGroups %1$s", vms),
                            String.format("$negativeAR %1$s", affinityGroupsNames),
                            String.format("$Vms %1$s", conflict.getNegativeVms().stream()
                                    .map(id -> id.toString())
                                    .collect(Collectors.joining(",")))
                    );
                } else {
                    return failValidation(EngineMessage.ACTION_TYPE_FAILED_AFFINITY_HOSTS_RULES_COLLISION,
                            String.format("AffinityGroups: %1$s", affinityGroupsNames),
                            String.format("Hosts: %1$s", hosts),
                            String.format("Vms: %1$s", vms)
                    );
                }
            }
        }
        return true;
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
