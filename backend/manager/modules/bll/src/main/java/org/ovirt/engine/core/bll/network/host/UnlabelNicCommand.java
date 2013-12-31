package org.ovirt.engine.core.bll.network.host;

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
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.NetworkUtils;

public class UnlabelNicCommand<T extends LabelNicParameters> extends CommandBase<T> {

    private VdsNetworkInterface nic;

    public UnlabelNicCommand(T parameters) {
        super(parameters);
        setVdsId(getNic() == null ? null : getNic().getVdsId());
    }

    @Override
    protected void executeCommand() {
        VdcReturnValueBase result =
                getBackend().runInternalAction(VdcActionType.PersistentSetupNetworks,
                        new RemoveNetworksByLabelParametersBuilder().buildParameters(getNic(), getLabel()));

        if (!result.getSucceeded()) {
            propagateFailure(result);
        }

        setSucceeded(result.getSucceeded());
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__REMOVE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__LABEL);
    }

    @Override
    protected boolean canDoAction() {
        if (getNic() == null) {
            return failCanDoAction(VdcBllMessages.HOST_NETWORK_INTERFACE_NOT_EXIST);
        }

        if (getNic().getLabels() == null || !getNic().getLabels().contains(getParameters().getLabel())) {
            return failCanDoAction(VdcBllMessages.INTERFACE_NOT_LABELED);
        }

        return true;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.UNLABEL_NIC : AuditLogType.UNLABEL_NIC_FAILED;
    }

    private VdsNetworkInterface getNic() {
        if (nic == null) {
            nic = getDbFacade().getInterfaceDao().get(getParameters().getNicId());
        }

        return nic;
    }

    public String getNickName() {
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

    private class RemoveNetworksByLabelParametersBuilder extends NetworkParametersBuilder {

        public SetupNetworksParameters buildParameters(VdsNetworkInterface nic, String label) {
            SetupNetworksParameters parameters = createSetupNetworksParameters(nic.getVdsId());
            List<Network> labeledNetworks = getNetworkDAO().getAllByLabelForCluster(label, getVds().getVdsGroupId());
            VdsNetworkInterface nicToConfigure = getNicToConfigure(parameters.getInterfaces(), nic.getId());
            if (nicToConfigure == null) {
                throw new VdcBLLException(VdcBllErrors.LABELED_NETWORK_INTERFACE_NOT_FOUND);
            }

            Set<VdsNetworkInterface> nicsToRemove =
                    getNicsToRemove(parameters.getInterfaces(), labeledNetworks, nicToConfigure);

            // remove the label from the nic to be passed to setup-networks
            unlabelConfiguredNic(label, nicToConfigure);

            // remove the networks from all of the nics
            parameters.getInterfaces().removeAll(nicsToRemove);
            return parameters;
        }

        private Set<VdsNetworkInterface> getNicsToRemove(List<VdsNetworkInterface> nics,
                List<Network> labeledNetworks,
                VdsNetworkInterface underlyingNic) {
            Map<String, VdsNetworkInterface> nicsByNetworkName = Entities.hostInterfacesByNetworkName(nics);
            Set<VdsNetworkInterface> nicsToRemove = new HashSet<>();

            for (Network network : labeledNetworks) {
                VdsNetworkInterface nic = nicsByNetworkName.get(network.getName());
                if (nic != null) {
                    if (StringUtils.equals(nic.getName(), underlyingNic.getName())) {
                        underlyingNic.setNetworkName(null);
                    } else if (StringUtils.equals(NetworkUtils.stripVlan(nic.getName()), underlyingNic.getName())) {
                        nicsToRemove.add(nic);
                    }
                }
            }

            return nicsToRemove;
        }

        private void unlabelConfiguredNic(String label, VdsNetworkInterface nicToConfigure) {
            if (nicToConfigure.getLabels() != null) {
                nicToConfigure.getLabels().remove(getLabel());
            }
        }
    }
}
