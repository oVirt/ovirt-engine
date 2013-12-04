package org.ovirt.engine.core.bll.network.dc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.RenamedEntityInfoProvider;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.network.NetworkConfigurator;
import org.ovirt.engine.core.bll.network.cluster.NetworkClusterHelper;
import org.ovirt.engine.core.bll.utils.VersionSupport;
import org.ovirt.engine.core.bll.validator.NetworkValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.action.SetupNetworksParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute
public class UpdateNetworkCommand<T extends AddNetworkStoragePoolParameters> extends NetworkCommon<T> implements RenamedEntityInfoProvider{
    private Network oldNetwork;

    public UpdateNetworkCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                getNetworkDAO().update(getNetwork());

                for (NetworkCluster clusterAttachment : getNetworkClusterDAO().getAllForNetwork(getNetwork().getId())) {
                    NetworkClusterHelper.setStatus(clusterAttachment.getClusterId(), getNetwork());
                }

                if (networkChangedToNonVmNetwork()) {
                    removeVnicProfiles();
                }

                return null;
            }
        });

        if (applyChangesToHostsRequired()) {
            applyNetworkChangesToHosts();
        }

        setSucceeded(true);
    }

    protected boolean setupNetworkSupported() {
        return VersionSupport.isActionSupported(VdcActionType.SetupNetworks,
                getStoragePool().getcompatibility_version());
    }

    private boolean applyChangesToHostsRequired() {
        return !getNetwork().isExternal() && setupNetworkSupported();
    }

    private void applyNetworkChangesToHosts() {
        SyncNetworkParametersBuilder builder = new SyncNetworkParametersBuilder();
        ArrayList<VdcActionParametersBase> parameters = builder.buildParameters(getNetwork());

        if (!parameters.isEmpty()) {
            getBackend().runInternalMultipleActions(VdcActionType.PersistentSetupNetworks, parameters);
        }
    }

    private boolean networkChangedToNonVmNetwork() {
        return getOldNetwork().isVmNetwork() && !getNetwork().isVmNetwork();
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__UPDATE);
    }

    @Override
    protected boolean canDoAction() {
        if (onlyPermittedFieldsChanged()) {
            return true;
        }

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
                && validate(validatorOld.networkNotUsedByRunningVms())
                && validate(validatorOld.nonVmNetworkNotUsedByVms(getNetwork()))
                && validate(validatorOld.nonVmNetworkNotUsedByTemplates(getNetwork()))
                && validate(validatorOld.notRenamingUsedNetwork(getNetworkName()))
                && (oldAndNewNetworkIsNotExternal()
                || validate(validatorOld.externalNetworkDetailsUnchanged(getNetwork())));
    }

    /**
     * @return <code>true</code> iff only the description or comment field were changed, otherwise <code>false</code>.
     */
    private boolean onlyPermittedFieldsChanged() {
        Network oldNetwork = getOldNetwork();
        Network newNetwork = getNetwork();

        if (oldNetwork == null || newNetwork == null) {
            return false;
        }

        return Objects.equals(oldNetwork.getName(), newNetwork.getName()) &&
                Objects.equals(oldNetwork.getDataCenterId(), newNetwork.getDataCenterId()) &&
                Objects.equals(oldNetwork.getId(), newNetwork.getId()) &&
                Objects.equals(oldNetwork.getMtu(), newNetwork.getMtu()) &&
                Objects.equals(oldNetwork.getName(), newNetwork.getName()) &&
                Objects.equals(oldNetwork.getProvidedBy(), newNetwork.getProvidedBy()) &&
                Objects.equals(oldNetwork.getStp(), newNetwork.getStp()) &&
                Objects.equals(oldNetwork.getVlanId(), newNetwork.getVlanId()) &&
                Objects.equals(oldNetwork.isVmNetwork(), newNetwork.isVmNetwork());
    }

    private boolean oldAndNewNetworkIsNotExternal() {
        return !getOldNetwork().isExternal() && !getNetwork().isExternal();
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

    protected static class UpdateNetworkValidator extends NetworkValidator {

        public UpdateNetworkValidator(Network network) {
            super(network);
        }

        public ValidationResult notRenamingUsedNetwork(String networkName) {
            if (StringUtils.equals(network.getName(), networkName)) {
                return ValidationResult.VALID;
            }

            ValidationResult result = networkNotUsedByHosts();
            if (!result.isValid()) {
                return result;
            }

            result = networkNotUsedByVms();
            if (!result.isValid()) {
                return result;
            }

            return networkNotUsedByTemplates();
        }

        public ValidationResult nonVmNetworkNotUsedByVms(Network updatedNetwork) {
            if (networkChangedToNonVmNetwork(updatedNetwork)) {
                return networkNotUsed(getVms(), VdcBllMessages.VAR__ENTITIES__VMS);
            }

            return ValidationResult.VALID;
        }

        private boolean networkChangedToNonVmNetwork(Network updatedNetwork) {
            return network.isVmNetwork() && !updatedNetwork.isVmNetwork();
        }

        public ValidationResult networkNotUsedByRunningVms() {
            List<VM> runningVms = new ArrayList<>();

            for (VM vm : getVms()) {
                if (vm.isRunningOrPaused()) {
                    runningVms.add(vm);
                }
            }

            return networkNotUsed(runningVms, VdcBllMessages.VAR__ENTITIES__VMS);
        }

        public ValidationResult nonVmNetworkNotUsedByTemplates(Network updatedNetwork) {
            if (networkChangedToNonVmNetwork(updatedNetwork)) {
                return networkNotUsed(getTemplates(), VdcBllMessages.VAR__ENTITIES__VMS);
            }

            return ValidationResult.VALID;
        }

        public ValidationResult notRenamingManagementNetwork(Network newNetwork) {
            String managementNetwork = NetworkUtils.getEngineNetwork();
            return network.getName().equals(managementNetwork) &&
                    !newNetwork.getName().equals(managementNetwork)
                    ? new ValidationResult(VdcBllMessages.NETWORK_CAN_NOT_REMOVE_DEFAULT_NETWORK)
                    : ValidationResult.VALID;
        }

        /**
         * Check that the external network details that can't be updated were not updated.<br>
         * The check is undefined if both the validator's network and the new network are internal networks.
         *
         * @param newNetwork
         *            The new network definition to check.
         * @return A valid result iff the details that shouldn't be changed remained unchanged, An error otherwise.
         */
        public ValidationResult externalNetworkDetailsUnchanged(Network newNetwork) {
            return ObjectUtils.equals(network.getVlanId(), newNetwork.getVlanId())
                    && network.getMtu() == newNetwork.getMtu()
                    && network.getStp() == newNetwork.getStp()
                    && network.isVmNetwork() == newNetwork.isVmNetwork()
                    && ObjectUtils.equals(network.getProvidedBy(), newNetwork.getProvidedBy())
                    ? ValidationResult.VALID
                    : new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_EXTERNAL_NETWORK_DETAILS_CANNOT_BE_EDITED);
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

    private class SyncNetworkParametersBuilder {

        private SetupNetworksParameters createSetupNetworksParameters(Guid hostId) {
            VDS host = new VDS();
            host.setId(hostId);
            NetworkConfigurator configurator = new NetworkConfigurator(host);
            List<VdsNetworkInterface> nics = configurator.filterBondsWithoutSlaves(getHostInterfaces(hostId));
            return configurator.createSetupNetworkParams(nics);
        }

        private List<VdsNetworkInterface> getHostInterfaces(Guid hostId) {
            return getDbFacade().getInterfaceDao().getAllInterfacesForVds(hostId);
        }

        protected ArrayList<VdcActionParametersBase> buildParameters(Network network) {
            ArrayList<VdcActionParametersBase> parameters = new ArrayList<>();
            List<VdsNetworkInterface> nics =
                    getDbFacade().getInterfaceDao().getVdsInterfacesByNetworkId(getNetwork().getId());

            Set<Guid> hostIdsToSync = new HashSet<>();
            for (VdsNetworkInterface nic : nics) {
                if (!NetworkUtils.isNetworkInSync(nic, getNetwork())) {
                    hostIdsToSync.add(nic.getVdsId());
                }
            }

            for (Guid hostId : hostIdsToSync) {
                SetupNetworksParameters setupNetworkParams = createSetupNetworksParameters(hostId);
                setupNetworkParams.setNetworksToSync(Collections.singletonList(getNetworkName()));
                parameters.add(setupNetworkParams);
            }

            return parameters;
        }
    }
}
