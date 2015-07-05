package org.ovirt.engine.core.bll.scheduling.commands;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.common.scheduling.parameters.AffinityGroupCRUDParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.scheduling.AffinityGroupDao;

public abstract class AffinityGroupCRUDCommand extends CommandBase<AffinityGroupCRUDParameters> {

    AffinityGroup affinityGroup = null;

    public AffinityGroupCRUDCommand(AffinityGroupCRUDParameters parameters) {
        super(parameters);
        if (getAffinityGroup() != null) {
            setVdsGroupId(getAffinityGroup().getClusterId());
            addCustomValue("affinityGroupName", getAffinityGroup().getName());
        }
    }

    protected boolean validateParameters() {
        if (getVdsGroup() == null) {
            return failCanDoAction(EngineMessage.ACTION_TYPE_FAILED_INVALID_CLUSTER_FOR_AFFINITY_GROUP);
        }
        if (getParameters().getAffinityGroup().getEntityIds() != null) {
            VmStatic vmStatic = null;
            Set<Guid> vmSet = new HashSet<>();
            for (Guid vmId : getParameters().getAffinityGroup().getEntityIds()) {
                vmStatic = getVmStaticDao().get(vmId);
                if (vmStatic == null) {
                    return failCanDoAction(EngineMessage.ACTION_TYPE_FAILED_INVALID_VM_FOR_AFFINITY_GROUP);
                }
                if (!Objects.equals(vmStatic.getVdsGroupId(), getVdsGroupId())) {
                    return failCanDoAction(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_IN_AFFINITY_GROUP_CLUSTER);
                }
                if (vmSet.contains(vmStatic.getId())) {
                    return failCanDoAction(EngineMessage.ACTION_TYPE_FAILED_DUPLICTE_VM_IN_AFFINITY_GROUP);
                } else {
                    vmSet.add(vmStatic.getId());
                }
            }
        }

        return hasAffinityCollisions(getParameters().getAffinityGroup());
    }

    private boolean hasAffinityCollisions(AffinityGroup affinityGroup) {
        List<AffinityGroup> affinityGroups = getAffinityGroupDao().getAllAffinityGroupsByClusterId(affinityGroup.getClusterId());
        affinityGroups.add(affinityGroup);
        return validateUnifiedAffinityGroups(affinityGroups);
    }

    /**
     * Unified affinity groups are affinity groups after being merged for logical reasons. From now on
     * will be called UAG in the following algorithm:
     * # UAG = {{vm} for each vm} - Each vm is in a separate set that contains itself only.
     * # For each positive affinity group(Sorted by group id):
     * ## Merge VM sets from the group in UAG(Sorted by vm id).
     * # For each negative affinity group(Sorted by group id):
     * ## For each group in UAG(Sorted by first vm uuid):
     * ### if size of the intersection of the group from UAG and the negative group is > 1:
     * #### throw exception “Affinity group contradiction detected” (With associated groups).
     */
    private boolean validateUnifiedAffinityGroups(List<AffinityGroup> affinityGroups) {
        Set<Set<Guid>> uag = new HashSet();

        /*
        * UAG = {{vm} for each vm in any affinity group(Either negative or positive)}
        * (UAG stands for Unified Affinity Groups).
        */
        for(AffinityGroup ag: affinityGroups) {
            Set<Guid> temp = new HashSet<>();
            temp.addAll(ag.getEntityIds());
            uag.add(temp);
        }

        /**
         * # For each positive affinity group(Sorted by group id) - ag:
         * # create empty Set<Vm> mergedSet
         * ## For each vm in ag:
         * ### remove the group contains vm from uag
         * ### merge the group with mergedSet
         * # add mergedSet back to uag.
         */
        for(AffinityGroup ag : affinityGroups) {
            if(ag.isPositive()) {
                Set<Guid> mergedSet = new HashSet<>();

                for(Guid id : ag.getEntityIds()) {
                    Set<Guid> vmGroup = popVmGroupByGuid(uag, id);
                    mergedSet.addAll(vmGroup);
                }

                uag.add(mergedSet);
            }
        }

        //Checking negative affinity group collisions
        for(AffinityGroup ag : affinityGroups) {
            if(ag.isPositive()) {
                continue;
            }

            for (Set<Guid> positiveGroup : uag) {
                Set<Guid> intersection = new HashSet<>(ag.getEntityIds());
                intersection.retainAll(positiveGroup);

                if(intersection.size() > 1) {
                    return failCanDoAction(EngineMessage.ACTION_TYPE_FAILED_AFFINITY_RULES_COLLISION,
                            String.format("$UnifiedAffinityGroups %s", positiveGroup.toString()),
                            String.format("$negativeAR %s", ag.getEntityIds().toString()));
                }
            }
        }

        return true;
    }

    private Set<Guid> popVmGroupByGuid(Set<Set<Guid>> uag, Guid id) {

        for(Iterator<Set<Guid>> iterator = uag.iterator(); iterator.hasNext();) {
            Set<Guid> s = iterator.next();

            if(s.contains(id)) {
                iterator.remove();
                return s;
            }
        }

        return Collections.<Guid>emptySet();
    }

    protected AffinityGroup getAffinityGroup() {
        if (affinityGroup == null) {
            affinityGroup = getAffinityGroupDao().get(getParameters().getAffinityGroupId());
        }
        return affinityGroup;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(getClusterId(),
                VdcObjectType.VdsGroups,
                getActionType().getActionGroup()));
    }

    protected Guid getClusterId() {
        return getAffinityGroup().getClusterId();
    }

    protected AffinityGroupDao getAffinityGroupDao() {
        return DbFacade.getInstance().getAffinityGroupDao();
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(EngineMessage.VAR__TYPE__AFFINITY_GROUP);
    }
}
