package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.VmNicValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ChangeVMClusterParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.ObjectIdentityChecker;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

@SuppressWarnings("serial")
public class ChangeVMClusterCommand<T extends ChangeVMClusterParameters> extends VmCommand<T> {

    private VDSGroup targetCluster;
    private boolean dedicatedHostWasCleared;

    public ChangeVMClusterCommand(T params) {
        super(params);
    }

    @Override
    protected boolean canDoAction() {
        // Set parameters for messeging.
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__UPDATE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM__CLUSTER);

        VM vm = getVm();
        if (vm == null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_EXIST);
            return false;
        } else {
            if (ObjectIdentityChecker.CanUpdateField(vm, "vds_group_id", vm.getStatus())) {
                targetCluster = DbFacade.getInstance().getVdsGroupDao().get(getParameters().getClusterId());
                if (targetCluster == null) {
                    addCanDoActionMessage(VdcBllMessages.VM_CLUSTER_IS_NOT_VALID);
                    return false;
                }

                // Check that the target cluster is in the same data center.
                if (!targetCluster.getStoragePoolId().equals(vm.getStoragePoolId())) {
                    addCanDoActionMessage(VdcBllMessages.VM_CANNOT_MOVE_TO_CLUSTER_IN_OTHER_STORAGE_POOL);
                    return false;
                }

                List<VmNetworkInterface> interfaces = DbFacade.getInstance().getVmNetworkInterfaceDao()
                .getAllForVm(getParameters().getVmId());

                Version clusterCompatibilityVersion = targetCluster.getcompatibility_version();
                if (!validateDestinationClusterContainsNetworks(interfaces)
                        || !validateNicsLinkedCorrectly(interfaces, clusterCompatibilityVersion)) {
                    return false;
                }

                // Check if VM static parameters are compatible for new cluster.
                boolean isCpuSocketsValid = AddVmCommand.CheckCpuSockets(
                                                                         vm.getStaticData().getnum_of_sockets(),
                                                                         vm.getStaticData().getcpu_per_socket(),
                                                                         clusterCompatibilityVersion.getValue(),
                                                                         getReturnValue().getCanDoActionMessages());
                if (!isCpuSocketsValid) {
                    return false;
                }

                // Check that the USB policy is legal
                if (!VmHandler.isUsbPolicyLegal(vm.getUsbPolicy(), vm.getOs(), targetCluster, getReturnValue().getCanDoActionMessages())) {
                    return false;
                }
            } else {
                addCanDoActionMessage(VdcBllMessages.VM_STATUS_NOT_VALID_FOR_UPDATE);
                return false;
            }
        }
        return true;
    }

    /**
     * Checks that when unlinking is not supported in the destination cluster and a NIC has unlinked network, it's not
     * valid.
     *
     * @param interfaces
     *            The NICs to check.
     * @param clusterCompatibilityVersion
     *            The destination cluster's compatibility version.
     * @return Whether the NICs are linked correctly (with regards to the destination cluster).
     */
    private boolean validateNicsLinkedCorrectly(List<VmNetworkInterface> interfaces,
            Version clusterCompatibilityVersion) {
        for (VmNetworkInterface iface : interfaces) {
            VmNicValidator nicValidator = new VmNicValidator(iface, clusterCompatibilityVersion);
            if (!validate(nicValidator.linkedCorrectly())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks that the destination cluster has all the networks that the given NICs require.<br>
     * No network on a NIC is allowed (it's checked when running VM).
     *
     * @param interfaces
     *            The NICs to check networks on.
     * @return Whether the destination cluster has all networks configured or not.
     */
    private boolean validateDestinationClusterContainsNetworks(List<VmNetworkInterface> interfaces) {
        List<Network> networks =
                DbFacade.getInstance().getNetworkDao().getAllForCluster(getParameters().getClusterId());
        StringBuilder missingNets = new StringBuilder();
        for (VmNetworkInterface iface : interfaces) {
            String netName = iface.getNetworkName();
            if (netName != null) {
                boolean exists = false;
                for (Network net : networks) {
                    if (net.getname().equals(netName)) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    if (missingNets.length() > 0) {
                        missingNets.append(", ");
                    }
                    missingNets.append(netName);
                }
            }
        }
        if (missingNets.length() > 0) {
            addCanDoActionMessage(VdcBllMessages.MOVE_VM_CLUSTER_MISSING_NETWORK);
            addCanDoActionMessage(String.format("$networks %1$s", missingNets.toString()));
            return false;
        }

        return true;
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
        List<Network> networks = DbFacade.getInstance().getNetworkDao()
                .getAllForCluster(getParameters().getClusterId());
        List<VmNetworkInterface> interfaces = DbFacade.getInstance().getVmNetworkInterfaceDao()
                .getAllForVm(getParameters().getVmId());

        for (final VmNetworkInterface iface : interfaces) {
            if (iface.getNetworkName() != null) {
                Network net = LinqUtils.firstOrNull(networks, new Predicate<Network>() {
                    @Override
                    public boolean eval(Network n) {
                        return iface.getNetworkName().equals(n.getname());
                    }
                });
                // if network not exists in cluster we remove the network to
                // interface connection
                if (net == null) {
                    iface.setNetworkName(null);
                    DbFacade.getInstance().getVmNetworkInterfaceDao().update(iface);
                }
            }
        }

        if (vm.getDedicatedVmForVds() != null) {
            vm.setDedicatedVmForVds(null);
            dedicatedHostWasCleared = true;
        }

        vm.setVdsGroupId(getParameters().getClusterId());
        DbFacade.getInstance().getVmStaticDao().update(vm.getStaticData());
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
        List<PermissionSubject> permissionList = new ArrayList<PermissionSubject>();
        // In addition to having EDIT_VM_PROPERTIES on the VM, you must have CREATE_VM on the cluster
        permissionList.add(new PermissionSubject(getParameters().getVmId(), VdcObjectType.VM, getActionType().getActionGroup()));
        permissionList.add(new PermissionSubject(getParameters().getClusterId(), VdcObjectType.VdsGroups, ActionGroup.CREATE_VM));
        return permissionList;
    }
}
