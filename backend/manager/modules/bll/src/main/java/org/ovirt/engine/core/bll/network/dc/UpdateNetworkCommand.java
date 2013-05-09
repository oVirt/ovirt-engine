package org.ovirt.engine.core.bll.network.dc;

import java.util.List;

import org.ovirt.engine.core.bll.RenamedEntityInfoProvider;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.network.cluster.NetworkClusterHelper;
import org.ovirt.engine.core.bll.validator.NetworkValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;

public class UpdateNetworkCommand<T extends AddNetworkStoragePoolParameters> extends NetworkCommon<T> implements RenamedEntityInfoProvider{
    private Network oldNetwork;

    public UpdateNetworkCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        getNetworkDAO().update(getNetwork());

        for (NetworkCluster clusterAttachment : getNetworkClusterDAO().getAllForNetwork(getNetwork().getId())) {
            NetworkClusterHelper.setStatus(clusterAttachment.getClusterId(), getNetwork());
        }
        setSucceeded(true);
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__UPDATE);
    }

    @Override
    protected boolean canDoAction() {
        NetworkValidator validatorNew = new NetworkValidator(getNetwork());
        UpdateNetworkValidator validatorOld = new UpdateNetworkValidator(getOldNetwork());
        return validate(validatorNew.dataCenterExists())
                && validate(validatorNew.vmNetworkSetCorrectly())
                && validate(validatorNew.stpForVmNetworkOnly())
                && validate(validatorNew.mtuValid())
                && validate(validatorNew.networkPrefixValid())
                && validate(validatorNew.vlanIdNotUsed())
                && validate(validatorOld.networkIsSet())
                && validate(validatorOld.notRenamingManagementNetwork(getNetwork()))
                && validate(validatorNew.networkNameNotUsed())
                && validate(validatorOld.networkNotUsedByVms())
                && validate(validatorOld.networkNotUsedByTemplates());
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.NETWORK_UPDATE_NETWORK : AuditLogType.NETWORK_UPDATE_NETWORK_FAILED;
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(UpdateEntity.class);
        return super.getValidationGroups();
    }

    private Network getOldNetwork() {
        if (oldNetwork == null) {
            oldNetwork = getNetworkDAO().get(getNetwork().getId());
        }
        return oldNetwork;
    }

    private class UpdateNetworkValidator extends NetworkValidator {

        public UpdateNetworkValidator(Network network) {
            super(network);
        }

        public ValidationResult notRenamingManagementNetwork(Network newNetwork) {
            String managementNetwork = Config.<String> GetValue(ConfigValues.ManagementNetwork);
            return network.getName().equals(managementNetwork) &&
                    !newNetwork.getName().equals(managementNetwork)
                    ? new ValidationResult(VdcBllMessages.NETWORK_CAN_NOT_REMOVE_DEFAULT_NETWORK)
                    : ValidationResult.VALID;
        }

    }

    @Override
    public String getEntityType() {
        return VdcObjectType.Network.getVdcObjectTranslation();
    }

    @Override
    public String getEntityOldName() {
        return getOldNetwork().getName();
    }

    @Override
    public String getEntityNewName() {
        return getParameters().getNetwork().getName();
    }

    @Override
    public void setEntityId(AuditLogableBase logable) {

    }
}
