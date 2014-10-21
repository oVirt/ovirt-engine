package org.ovirt.engine.core.bll;

import org.apache.commons.lang.ObjectUtils;
import org.ovirt.engine.core.bll.network.cluster.NetworkHelper;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ChangeVMClusterParameters;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.ObjectIdentityChecker;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

import java.util.List;

public class ChangeVMClusterCommand<T extends ChangeVMClusterParameters> extends VmCommand<T> {

    private boolean dedicatedHostWasCleared;

    public ChangeVMClusterCommand(T params) {
        super(params);
    }

    @Override
    protected boolean canDoAction() {
        if (!canRunActionOnNonManagedVm()) {
            return false;
        }

        if (!isInternalExecution() && !ObjectIdentityChecker.CanUpdateField(getVm(), "vdsGroupId", getVm().getStatus())) {
            addCanDoActionMessage(VdcBllMessages.VM_STATUS_NOT_VALID_FOR_UPDATE);
            return false;
        }

        ChangeVmClusterValidator validator = new ChangeVmClusterValidator(this, getParameters().getClusterId());
        return validator.validate();
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__UPDATE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM__CLUSTER);
    }

    @Override
    protected void executeCommand() {
        // check that the cluster are not the same
        VM vm = getVm();
        if (vm.getVdsGroupId().equals(getParameters().getClusterId())) {
            setSucceeded(true);
            return;
        }

        // update vm interfaces
        List<Network> networks = getNetworkDAO().getAllForCluster(getParameters().getClusterId());
        List<VmNic> interfaces = getVmNicDao().getAllForVm(getParameters().getVmId());

        for (final VmNic iface : interfaces) {
            if (iface.getVnicProfileId() != null) {
                final Network network = NetworkHelper.getNetworkByVnicProfileId(iface.getVnicProfileId());
                Network net = LinqUtils.firstOrNull(networks, new Predicate<Network>() {
                    @Override
                    public boolean eval(Network n) {
                        return ObjectUtils.equals(n.getId(), network.getId());
                    }
                });

                // if network not exists in cluster we remove the network to
                // interface connection
                if (net == null) {
                    iface.setVnicProfileId(null);
                    getVmNicDao().update(iface);
                }
            }
        }

        if (vm.getDedicatedVmForVds() != null) {
            vm.setDedicatedVmForVds(null);
            dedicatedHostWasCleared = true;
        }

        // Since CPU profile is coupled to cluster, when changing a cluster
        // the 'old' CPU profile is invalid. The update VM command is called straight after
        // will validate a right profile for VM and its cluster
        vm.setCpuProfileId(null);
        vm.setVdsGroupId(getParameters().getClusterId());
        DbFacade.getInstance().getVmStaticDao().update(vm.getStaticData());

        // change vm cluster should remove the vm from all associated affinity groups
        List<AffinityGroup> allAffinityGroupsByVmId =
                DbFacade.getInstance().getAffinityGroupDao().getAllAffinityGroupsByVmId(vm.getId());
        if (!allAffinityGroupsByVmId.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (AffinityGroup affinityGroup : allAffinityGroupsByVmId) {
                sb.append(affinityGroup.getName() + " ");
            }
            log.infoFormat("Due to cluster change, removing VM from associated affinity group(s): {0}", sb.toString());
            DbFacade.getInstance().getAffinityGroupDao().removeVmFromAffinityGroups(vm.getId());
        }
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ?
                dedicatedHostWasCleared ?
                        AuditLogType.USER_UPDATE_VM_CLUSTER_DEFAULT_HOST_CLEARED
                        : AuditLogType.USER_UPDATE_VM
                : AuditLogType.USER_FAILED_UPDATE_VM;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return VmHandler.getPermissionsNeededToChangeCluster(getParameters().getVmId(), getParameters().getClusterId());
    }
}
