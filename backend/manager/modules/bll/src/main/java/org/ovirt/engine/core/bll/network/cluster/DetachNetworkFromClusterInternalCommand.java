package org.ovirt.engine.core.bll.network.cluster;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.ClusterCommandBase;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.ValidateSupportsTransaction;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.validator.NetworkValidator;
import org.ovirt.engine.core.common.action.AttachNetworkToClusterParameter;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.dao.VmTemplateDao;
import org.ovirt.engine.core.dao.gluster.GlusterBrickDao;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;

@InternalCommandAttribute
@ValidateSupportsTransaction
public class DetachNetworkFromClusterInternalCommand<T extends AttachNetworkToClusterParameter>
        extends ClusterCommandBase<T> {

    @Inject
    private NetworkClusterHelper networkClusterHelper;
    @Inject
    private NetworkHelper networkHelper;
    @Inject
    private VmStaticDao vmStaticDao;
    @Inject
    private VmTemplateDao vmTemplateDao;
    @Inject
    private VmNetworkInterfaceDao vmNetworkInterfaceDao;
    @Inject
    private GlusterBrickDao glusterBrickDao;

    public DetachNetworkFromClusterInternalCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        networkClusterHelper.removeNetworkAndReassignRoles(getParameters().getNetworkCluster());
        networkHelper.setVdsmNamesInVdsInterfaces(getNetwork(), getClusterId());

        setSucceeded(true);
    }

    @Override
    protected boolean validate() {
        DetachNetworkValidator validator =
                new DetachNetworkValidator(getNetwork(), getParameters().getNetworkCluster());
        return validate(validator.notManagementNetwork())
                && validate(validator.clusterNetworkNotUsedByVms())
                && validate(validator.clusterNetworkNotUsedByTemplates())
                && validate(validator.clusterNetworkNotUsedByBricks());
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__DETACH);
        addValidationMessage(EngineMessage.VAR__TYPE__NETWORK);
    }

    private Network getNetwork() {
        return getParameters().getNetwork();
    }

    private class DetachNetworkValidator extends NetworkValidator {

        private final NetworkCluster networkCluster;

        public DetachNetworkValidator(Network network, NetworkCluster networkCluster) {
            super(network);
            this.networkCluster = networkCluster;
        }

        public ValidationResult clusterNetworkNotUsedByVms() {
            return new PluralMessages(EngineMessage.VAR__ENTITIES__VM, EngineMessage.VAR__ENTITIES__VMS)
                .getNetworkInUse(getEntitiesNames(vmStaticDao.getAllByGroupAndNetworkName(networkCluster.getClusterId(),
                    network.getName())));
        }

        public ValidationResult clusterNetworkNotUsedByTemplates() {
            List<VmTemplate> templatesUsingNetwork = new ArrayList<>();
            for (VmTemplate template : vmTemplateDao.getAllForCluster(networkCluster.getClusterId())) {
                for (VmNetworkInterface nic : vmNetworkInterfaceDao.getAllForTemplate(template.getId())) {
                    if (network.getName().equals(nic.getNetworkName())) {
                        templatesUsingNetwork.add(template);
                    }
                }
            }
            return new PluralMessages(EngineMessage.VAR__ENTITIES__VM_TEMPLATE,
                EngineMessage.VAR__ENTITIES__VM_TEMPLATES)
                .getNetworkInUse(getEntitiesNames(templatesUsingNetwork));
        }

        public ValidationResult clusterNetworkNotUsedByBricks() {
            return new PluralMessages(EngineMessage.VAR__ENTITIES__GLUSTER_BRICK,
                EngineMessage.VAR__ENTITIES__GLUSTER_BRICKS)
                .getNetworkInUse(getEntitiesNames(glusterBrickDao.getAllByClusterAndNetworkId(networkCluster.getClusterId(),
                    network.getId())));
        }
    }
}
