package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.cluster.NetworkHelper;
import org.ovirt.engine.core.bll.network.macpool.ReadMacPool;
import org.ovirt.engine.core.bll.profiles.CpuProfileHelper;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ChangeVMClusterParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.LabelDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.dao.network.VmNicDao;
import org.ovirt.engine.core.dao.scheduling.AffinityGroupDao;
import org.ovirt.engine.core.utils.ObjectIdentityChecker;
import org.ovirt.engine.core.vdsbroker.ResourceManager;

public class ChangeVMClusterCommand<T extends ChangeVMClusterParameters> extends VmCommand<T> {

    @Inject
    private CpuProfileHelper cpuProfileHelper;
    @Inject
    private MoveMacs moveMacs;
    @Inject
    private ClusterDao clusterDao;
    @Inject
    private NetworkDao networkDao;
    @Inject
    private VmNicDao vmNicDao;
    @Inject
    private ResourceManager resourceManager;
    @Inject
    private AffinityGroupDao affinityGroupDao;
    @Inject
    private LabelDao labelDao;

    @Inject
    private NetworkHelper networkHelper;

    private boolean dedicatedHostWasCleared;

    private Guid sourceMacPoolId;
    private Guid targetMacPoolId;

    //cached value.
    private List<String> macsToMigrate;

    public ChangeVMClusterCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void init() {
        super.init();
        setClusterId(getParameters().getClusterId());

        sourceMacPoolId = getMacPoolId(getVm().getClusterId());
        targetMacPoolId = getMacPoolId(getClusterId());
    }

    private Guid getMacPoolId(Guid clusterId) {
        return Optional.ofNullable(clusterDao.get(clusterId)).map(Cluster::getMacPoolId).orElse(null);
    }

    @Override
    protected boolean validate() {
        if (!canRunActionOnNonManagedVm()) {
            return false;
        }

        if (!isInternalExecution() && !ObjectIdentityChecker.canUpdateField(getVm(), "clusterId", getVm().getStatus())) {
            return failValidation(EngineMessage.VM_STATUS_NOT_VALID_FOR_UPDATE);
        }

        if (macPoolChanged()) {
            ReadMacPool macPoolForTargetCluster = macPoolPerCluster.getMacPoolForCluster(getClusterId());
            if (!validate(moveMacs.canMigrateMacsToAnotherMacPool(macPoolForTargetCluster, getMacsToMigrate()))) {
                return false;
            }
        }

        ChangeVmClusterValidator validator = ChangeVmClusterValidator.create(getVm(),
                getClusterId(),
                getParameters().getVmCustomCompatibilityVersion(),
                getUserId());
        return validate(validator.validate());
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__UPDATE);
        addValidationMessage(EngineMessage.VAR__TYPE__VM__CLUSTER);
    }

    @Override
    protected void executeCommand() {
        boolean clusterRemainedTheSame = getVm().getClusterId().equals(getClusterId());
        if (clusterRemainedTheSame) {
            setSucceeded(true);
            return;
        }

        updateVmInterfaces();

        updateVm(getVm());

        moveMacsToAnotherMacPoolIfNeeded();
        removeVmFromAllAssociatedAffinityGroups(getVmId());
        removeVmFromAllLabels(getVmId());

        setSucceeded(true);
    }

    private void updateVm(VM vm) {
        vm.setClusterId(getClusterId());
        clearDedicatedHosts(vm);
        setCpuProfileFromNewCluster(vm);
        resourceManager.getVmManager(getVmId()).update(vm.getStaticData());
    }

    private void setCpuProfileFromNewCluster(VM vm) {
        cpuProfileHelper.assignFirstCpuProfile(vm.getStaticData(), getUserIdIfExternal().orElse(null));
    }

    private void clearDedicatedHosts(VM vm) {
        if (!vm.getDedicatedVmForVdsList().isEmpty()) {
            vm.setDedicatedVmForVdsList(Collections.emptyList());
            dedicatedHostWasCleared = true;
        }
    }

    private void updateVmInterfaces() {
        List<Network> networks = networkDao.getAllForCluster(getClusterId());
        List<VmNic> interfaces = vmNicDao.getAllForVm(getParameters().getVmId());

        for (final VmNic iface : interfaces) {
            if (iface.getVnicProfileId() != null) {
                final Network network = networkHelper.getNetworkByVnicProfileId(iface.getVnicProfileId());
                boolean networkFoundInCluster =
                        networks.stream().anyMatch(n -> Objects.equals(n.getId(), network.getId()));

                // if network not exists in cluster we remove the network to interface connection
                if (!networkFoundInCluster) {
                    iface.setVnicProfileId(null);
                    vmNicDao.update(iface);
                }
            }
        }
    }

    private void removeVmFromAllAssociatedAffinityGroups(Guid vmId) {
        List<AffinityGroup> allAffinityGroupsByVmId = affinityGroupDao.getAllAffinityGroupsByVmId(vmId);
        if (!allAffinityGroupsByVmId.isEmpty()) {
            String groups = allAffinityGroupsByVmId.stream().map(AffinityGroup::getName).collect(Collectors.joining(" "));
            log.info("Due to cluster change, removing VM from associated affinity group(s): {}", groups);
            affinityGroupDao.setAffinityGroupsForVm(vmId, Collections.emptyList());
        }
    }

    private void removeVmFromAllLabels(Guid vmId) {
        List<Label> labels = labelDao.getAllByEntityIds(Collections.singleton(vmId));
        if (!labels.isEmpty()) {
            String labelNames = labels.stream().map(Label::getName).collect(Collectors.joining(" "));
            log.info("Due to cluster change, removing VM from associated label(s): {}", labelNames);
            labelDao.updateLabelsForVm(vmId, Collections.emptyList());
        }
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
        return VmHandler.getPermissionsNeededToChangeCluster(getParameters().getVmId(), getClusterId());
    }

    void moveMacsToAnotherMacPoolIfNeeded() {
        if (macPoolChanged()) {
            moveMacs.migrateMacsToAnotherMacPool(sourceMacPoolId,
                    targetMacPoolId,
                    getMacsToMigrate(),
                    getContext());
        }
    }

    private boolean macPoolChanged() {
        return !Objects.equals(sourceMacPoolId, targetMacPoolId);
    }

    private List<String> getMacsToMigrate() {
        if (this.macsToMigrate == null) {
            this.macsToMigrate = calculateMacsToMigrate();
        }
        return this.macsToMigrate;
    }

    private List<String> calculateMacsToMigrate() {
        List<VmNic> vmNicsForVm = vmNicDao.getAllForVm(getVm().getId());
        return vmNicsForVm.stream().map(VmNic::getMacAddress).collect(Collectors.toList());
    }
}
