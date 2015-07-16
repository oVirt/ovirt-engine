package org.ovirt.engine.core.bll.network.cluster;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CanDoActionSupportsTransaction;
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
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dao.VmDao;

@InternalCommandAttribute
@CanDoActionSupportsTransaction
public class DetachNetworkFromClusterInternalCommand<T extends AttachNetworkToVdsGroupParameter>
        extends VdsGroupCommandBase<T> {

    @Inject
    private VmDao vmDao;

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
                new DetachNetworkValidator(vmDao, getNetwork(), getParameters().getNetworkCluster());
        return validate(validator.notManagementNetwork())
                && validate(validator.clusterNetworkNotUsedByVms())
                && validate(validator.clusterNetworkNotUsedByTemplates())
                && validate(validator.clusterNetworkNotUsedByBricks());
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(EngineMessage.VAR__ACTION__DETACH);
        addCanDoActionMessage(EngineMessage.VAR__TYPE__NETWORK);
    }

    private Network getNetwork() {
        return getParameters().getNetwork();
    }

    private class DetachNetworkValidator extends NetworkValidator {

        private final NetworkCluster networkCluster;

        public DetachNetworkValidator(VmDao vmDao, Network network, NetworkCluster networkCluster) {
            super(vmDao, network);
            this.networkCluster = networkCluster;
        }

        public ValidationResult clusterNetworkNotUsedByVms() {
            return networkNotUsed(getVmStaticDao().getAllByGroupAndNetworkName(networkCluster.getClusterId(),
                    network.getName()),
                    EngineMessage.VAR__ENTITIES__VMS,
                    EngineMessage.VAR__ENTITIES__VM);
        }

        public ValidationResult clusterNetworkNotUsedByTemplates() {
            List<VmTemplate> templatesUsingNetwork = new ArrayList<>();
            for (VmTemplate template : getVmTemplateDao().getAllForVdsGroup(networkCluster.getClusterId())) {
                for (VmNetworkInterface nic : getVmNetworkInterfaceDao().getAllForTemplate(template.getId())) {
                    if (network.getName().equals(nic.getNetworkName())) {
                        templatesUsingNetwork.add(template);
                    }
                }
            }
            return networkNotUsed(templatesUsingNetwork,
                    EngineMessage.VAR__ENTITIES__VM_TEMPLATES,
                    EngineMessage.VAR__ENTITIES__VM_TEMPLATE);
        }

        public ValidationResult clusterNetworkNotUsedByBricks() {
            return networkNotUsed(getGlusterBrickDao().getAllByClusterAndNetworkId(networkCluster.getClusterId(),
                    network.getId()),
                    EngineMessage.VAR__ENTITIES__GLUSTER_BRICKS,
                    EngineMessage.VAR__ENTITIES__GLUSTER_BRICK);
        }
    }
}
