package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.cluster.NetworkHelper;
import org.ovirt.engine.core.bll.profiles.CpuProfileHelper;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ChangeVMClusterParameters;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.utils.ObjectIdentityChecker;

public class ChangeVMClusterCommand<T extends ChangeVMClusterParameters> extends VmCommand<T> {

    private boolean dedicatedHostWasCleared;

    @Inject
    private CpuProfileHelper cpuProfileHelper;

    public ChangeVMClusterCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected boolean validate() {
        if (!canRunActionOnNonManagedVm()) {
            return false;
        }

        if (!isInternalExecution() && !ObjectIdentityChecker.canUpdateField(getVm(), "clusterId", getVm().getStatus())) {
            addValidationMessage(EngineMessage.VM_STATUS_NOT_VALID_FOR_UPDATE);
            return false;
        }

        ChangeVmClusterValidator validator = ChangeVmClusterValidator.create(this,
                getParameters().getClusterId(),
                getParameters().getVmCustomCompatibilityVersion());
        return validator.validate();
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__UPDATE);
        addValidationMessage(EngineMessage.VAR__TYPE__VM__CLUSTER);
    }

    @Override
    protected void executeCommand() {
        // check that the cluster are not the same
        VM vm = getVm();
        if (vm.getClusterId().equals(getParameters().getClusterId())) {
            setSucceeded(true);
            return;
        }

        // update vm interfaces
        List<Network> networks = networkDao.getAllForCluster(getParameters().getClusterId());
        List<VmNic> interfaces = vmNicDao.getAllForVm(getParameters().getVmId());

        for (final VmNic iface : interfaces) {
            if (iface.getVnicProfileId() != null) {
                final Network network = NetworkHelper.getNetworkByVnicProfileId(iface.getVnicProfileId());
                boolean networkFoundInCluster =
                        networks.stream().anyMatch(n -> Objects.equals(n.getId(), network.getId()));

                // if network not exists in cluster we remove the network to
                // interface connection
                if (!networkFoundInCluster) {
                    iface.setVnicProfileId(null);
                    vmNicDao.update(iface);
                }
            }
        }

        if (vm.getDedicatedVmForVdsList().size() > 0) {
            vm.setDedicatedVmForVdsList(Collections.emptyList());
            dedicatedHostWasCleared = true;
        }

        vm.setClusterId(getParameters().getClusterId());

        // Set cpu profile from the new cluster
        cpuProfileHelper.assignFirstCpuProfile(vm.getStaticData(), getUserId());

        vmStaticDao.update(vm.getStaticData());

        // change vm cluster should remove the vm from all associated affinity groups
        List<AffinityGroup> allAffinityGroupsByVmId = affinityGroupDao.getAllAffinityGroupsByVmId(vm.getId());
        if (!allAffinityGroupsByVmId.isEmpty()) {
            String groups = allAffinityGroupsByVmId.stream().map(AffinityGroup::getName).collect(Collectors.joining(" "));
            log.info("Due to cluster change, removing VM from associated affinity group(s): {}", groups);
            affinityGroupDao.removeVmFromAffinityGroups(vm.getId());
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
