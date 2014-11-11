package org.ovirt.engine.core.bll.network.cluster;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.VdsGroupCommandBase;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.validator.NetworkValidator;
import org.ovirt.engine.core.common.action.AttachNetworkToVdsGroupParameter;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.errors.VdcBllMessages;

@InternalCommandAttribute
public class DetachNetworkFromClusterInternalCommand<T extends AttachNetworkToVdsGroupParameter>
        extends VdsGroupCommandBase<T> {

    public DetachNetworkFromClusterInternalCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        NetworkClusterHelper helper = new NetworkClusterHelper(getParameters().getNetworkCluster());
        helper.removeNetworkAndReassignRoles();

        setSucceeded(true);
    }

    @Override
    protected boolean canDoAction() {
        DetachNetworkValidator validator =
                new DetachNetworkValidator(getNetwork(), getParameters().getNetworkCluster());
        return validate(validator.notManagementNetwork())
                && validate(validator.clusterNetworkNotUsedByVms())
                && validate(validator.clusterNetworkNotUsedByTemplates());
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__DETACH);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__NETWORK);
    }

    private Network getNetwork() {
        return getParameters().getNetwork();
    }

    private class DetachNetworkValidator extends NetworkValidator {

        private NetworkCluster networkCluster;

        public DetachNetworkValidator(Network network, NetworkCluster networkCluster) {
            super(network);
            this.networkCluster = networkCluster;
        }

        public ValidationResult clusterNetworkNotUsedByVms() {
            return networkNotUsed(getVmStaticDAO().getAllByGroupAndNetworkName(networkCluster.getClusterId(),
                    network.getName()),
                    VdcBllMessages.VAR__ENTITIES__VMS,
                    VdcBllMessages.VAR__ENTITIES__VM);
        }

        public ValidationResult clusterNetworkNotUsedByTemplates() {
            List<VmTemplate> templatesUsingNetwork = new ArrayList<>();
            for (VmTemplate template : getVmTemplateDAO().getAllForVdsGroup(networkCluster.getClusterId())) {
                for (VmNetworkInterface nic : getVmNetworkInterfaceDao().getAllForTemplate(template.getId())) {
                    if (network.getName().equals(nic.getNetworkName())) {
                        templatesUsingNetwork.add(template);
                    }
                }
            }
            return networkNotUsed(templatesUsingNetwork,
                    VdcBllMessages.VAR__ENTITIES__VM_TEMPLATES,
                    VdcBllMessages.VAR__ENTITIES__VM_TEMPLATE);
        }
    }
}
