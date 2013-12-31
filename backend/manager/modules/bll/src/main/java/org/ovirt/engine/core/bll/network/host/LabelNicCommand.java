package org.ovirt.engine.core.bll.network.host;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.network.NetworkParametersBuilder;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.LabelNicParameters;
import org.ovirt.engine.core.common.action.SetupNetworksParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.NetworkUtils;

public class LabelNicCommand<T extends LabelNicParameters> extends CommandBase<T> {

    private VdsNetworkInterface nic;

    public LabelNicCommand(T parameters) {
        super(parameters);
        setVdsId(getNic() == null ? null : getNic().getVdsId());
    }

    @Override
    protected void executeCommand() {
        VdcReturnValueBase result =
                getBackend().runInternalAction(VdcActionType.PersistentSetupNetworks,
                        new AddNetworksByLabelParametersBuilder().buildParameters(getNic(), getLabel()));
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

        if (getNic().getLabels() != null && getNic().getLabels().contains(getParameters().getLabel())) {
            return failCanDoAction(VdcBllMessages.INTERFACE_ALREADY_LABELED);
        }

        if (!ValidationUtils.validateInputs(getValidationGroups(), getNic()).isEmpty()) {
            return failCanDoAction(VdcBllMessages.IMPROPER_INTERFACE_IS_LABELED);
        }

        return true;
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

    private class AddNetworksByLabelParametersBuilder extends NetworkParametersBuilder {

        public SetupNetworksParameters buildParameters(VdsNetworkInterface nic, String label) {
            SetupNetworksParameters parameters = createSetupNetworksParameters(nic.getVdsId());
            List<Network> labeledNetworks = getNetworkDAO().getAllByLabelForCluster(label, getVds().getVdsGroupId());
            Set<Network> networkToAdd = getNetworksToConfigure(parameters.getInterfaces(), labeledNetworks);
            VdsNetworkInterface nicToConfigure = getNicToConfigure(parameters.getInterfaces(), nic.getId());
            if (nicToConfigure == null) {
                throw new VdcBLLException(VdcBllErrors.LABELED_NETWORK_INTERFACE_NOT_FOUND);
            }

            // add label to nic to be passed to setup-networks
            labelConfiguredNic(label, nicToConfigure);

            // configure networks on the nic
            parameters.getInterfaces().addAll(configureNetworks(nicToConfigure, networkToAdd));
            return parameters;
        }

        public void labelConfiguredNic(String label, VdsNetworkInterface nicToConfigure) {
            if (nicToConfigure.getLabels() == null) {
                nicToConfigure.setLabels(new HashSet<String>());
            }

            nicToConfigure.getLabels().add(label);
        }

        public Set<Network> getNetworksToConfigure(List<VdsNetworkInterface> nics, List<Network> labeledNetworks) {
            Map<String, VdsNetworkInterface> nicsByNetworkName = Entities.hostInterfacesByNetworkName(nics);
            Set<Network> networkToAdd = new HashSet<>();

            for (Network network : labeledNetworks) {
                if (!nicsByNetworkName.containsKey(network.getName())) {
                    networkToAdd.add(network);
                }
            }

            return networkToAdd;
        }

        /**
         * Configure the networks on a specific nic and/or returns a list of vlans as new added interfaces configured
         * with vlan networks
         *
         * @param nic
         *            the underlying interface to configure
         * @param networks
         *            the networks to configure on the nic
         * @return a list of vlan devices or an empty list
         */
        public List<VdsNetworkInterface> configureNetworks(VdsNetworkInterface nic, Set<Network> networks) {
            List<VdsNetworkInterface> vlans = new ArrayList<>();
            for (Network network : networks) {
                if (NetworkUtils.isVlan(network)) {
                    vlans.add(createVlanDevice(nic, network));
                } else if (StringUtils.isEmpty(nic.getNetworkName())) {
                    nic.setNetworkName(network.getName());
                } else {
                    throw new VdcBLLException(VdcBllErrors.NETWORK_LABEL_CONFLICT);
                }
            }

            return vlans;
        }
    }
}
