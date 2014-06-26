package org.ovirt.engine.core.bll.network.host;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.network.AddNetworksByLabelParametersBuilder;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.LabelNicParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.NetworkUtils;

public class LabelNicCommand<T extends LabelNicParameters> extends CommandBase<T> {

    private VdsNetworkInterface nic;
    private List<Network> labeledNetworks;
    private List<VdsNetworkInterface> hostNics;

    public LabelNicCommand(T parameters) {
        super(parameters);
        setVdsId(getNic() == null ? null : getNic().getVdsId());
    }

    @Override
    protected void executeCommand() {
        VdcReturnValueBase result =
                runInternalAction(VdcActionType.PersistentSetupNetworks,
                        new AddNetworksByLabelParametersBuilder(getContext()).buildParameters(getNic(),
                                getLabel(),
                                getClusterNetworksByLabel()), cloneContextAndDetachFromParent());
        if (result.getSucceeded()) {
            getReturnValue().setActionReturnValue(getLabel());
        } else {
            propagateFailure(result);
        }

        setSucceeded(result.getSucceeded());
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__ADD);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__LABEL);
    }

    @Override
    protected boolean canDoAction() {
        if (getNic() == null) {
            return failCanDoAction(VdcBllMessages.HOST_NETWORK_INTERFACE_NOT_EXIST);
        }

        if (NetworkUtils.isLabeled(getNic()) && getNic().getLabels().contains(getLabel())) {
            return failCanDoAction(VdcBllMessages.INTERFACE_ALREADY_LABELED);
        }

        if (!ValidationUtils.validateInputs(getValidationGroups(), getNic()).isEmpty()) {
            return failCanDoAction(VdcBllMessages.IMPROPER_INTERFACE_IS_LABELED);
        }

        if (Boolean.TRUE.equals(getNic().getBonded())) {
            int slavesCount = 0;
            for (VdsNetworkInterface nic : getHostInterfaces()) {
                if (StringUtils.equals(getNic().getName(), nic.getBondName())) {
                    slavesCount++;
                    if (slavesCount == 2) {
                        break;
                    }
                }
            }

            if (slavesCount < 2) {
                return failCanDoAction(VdcBllMessages.IMPROPER_BOND_IS_LABELED);
            }
        }

        for (VdsNetworkInterface nic : getHostInterfaces()) {
            if (!StringUtils.equals(nic.getName(), getNicName()) && NetworkUtils.isLabeled(nic)
                    && nic.getLabels().contains(getLabel())) {
                return failCanDoAction(VdcBllMessages.OTHER_INTERFACE_ALREADY_LABELED, "$LabeledNic " + nic.getName());
            }
        }

        List<String> assignedNetworks = validateNetworksNotAssignedToIncorrectNics();
        if (!assignedNetworks.isEmpty()) {
            return failCanDoAction(VdcBllMessages.LABELED_NETWORK_ATTACHED_TO_WRONG_INTERFACE, "$AssignedNetworks "
                    + StringUtils.join(assignedNetworks, ", "));
        }

        return true;
    }

    private List<VdsNetworkInterface> getHostInterfaces() {
        if (hostNics == null) {
            hostNics = getDbFacade().getInterfaceDao().getAllInterfacesForVds(getVdsId());
        }

        return hostNics;
    }

    public List<String> validateNetworksNotAssignedToIncorrectNics() {
        Map<String, VdsNetworkInterface> nicsByNetworkName = Entities.hostInterfacesByNetworkName(getHostInterfaces());
        List<String> badlyAssignedNetworks = new ArrayList<>();
        for (Network network : getClusterNetworksByLabel()) {
            if (nicsByNetworkName.containsKey(network.getName())) {
                VdsNetworkInterface assignedNic = nicsByNetworkName.get(network.getName());
                if (!StringUtils.equals(getNicName(), NetworkUtils.stripVlan(assignedNic))) {
                    badlyAssignedNetworks.add(network.getName());
                }
            }
        }

        return badlyAssignedNetworks;
    }

    private List<Network> getClusterNetworksByLabel() {
        if (labeledNetworks == null) {
            labeledNetworks = getNetworkDAO().getAllByLabelForCluster(getLabel(), getVds().getVdsGroupId());
        }

        return labeledNetworks;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.LABEL_NIC : AuditLogType.LABEL_NIC_FAILED;
    }

    private VdsNetworkInterface getNic() {
        if (nic == null) {
            nic = getDbFacade().getInterfaceDao().get(getParameters().getNicId());
        }

        return nic;
    }

    public String getNicName() {
        return getNic().getName();
    }

    public String getLabel() {
        return getParameters().getLabel();
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        Guid hostId = getNic() == null ? null : getNic().getVdsId();
        return Collections.singletonList(new PermissionSubject(hostId,
                VdcObjectType.VDS,
                getActionType().getActionGroup()));
    }
}
