package org.ovirt.engine.core.bll.network.dc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.RenamedEntityInfoProvider;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.common.predicates.VmNetworkCanBeUpdatedPredicate;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.NetworkParametersBuilder;
import org.ovirt.engine.core.bll.network.AddNetworkParametersBuilder;
import org.ovirt.engine.core.bll.network.RemoveNetworkParametersBuilder;
import org.ovirt.engine.core.bll.network.cluster.ManagementNetworkUtil;
import org.ovirt.engine.core.bll.network.cluster.NetworkClusterHelper;
import org.ovirt.engine.core.bll.network.cluster.NetworkHelper;
import org.ovirt.engine.core.bll.validator.NetworkValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.action.PersistentSetupNetworksParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute
public class UpdateNetworkCommand<T extends AddNetworkStoragePoolParameters> extends NetworkModification<T> implements RenamedEntityInfoProvider {

    @Inject
    private ManagementNetworkUtil managementNetworkUtil;

    private Network oldNetwork;

    public UpdateNetworkCommand(T parameters) {
        super(parameters);
    }

    public UpdateNetworkCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
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

        if (!getNetwork().isExternal()) {
            if (NetworkHelper.setupNetworkSupported(getStoragePool().getCompatibilityVersion())) {
                applyNetworkChangesToHosts();
            } else if (!onlyPermittedFieldsChanged() || !allowedNetworkLabelManipulation()) {
                List<VdsNetworkInterface> nics =
                        getDbFacade().getInterfaceDao().getVdsInterfacesByNetworkId(getNetwork().getId());
                if (!nics.isEmpty()) {
                    AuditLogDirector.log(this, AuditLogType.MULTI_UPDATE_NETWORK_NOT_POSSIBLE);
                }
            }
        }

        setSucceeded(true);
    }

    private void applyNetworkChangesToHosts() {
        SyncNetworkParametersBuilder builder = new SyncNetworkParametersBuilder(getContext());
        ArrayList<VdcActionParametersBase> parameters = builder.buildParameters(getNetwork(), getOldNetwork());

        if (!parameters.isEmpty()) {
            NetworkParametersBuilder.updateParametersSequencing(parameters);
            runInternalMultipleActions(VdcActionType.PersistentSetupNetworks, parameters);
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
        if (onlyPermittedFieldsChanged() && allowedNetworkLabelManipulation()) {
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
                && validate(validatorNew.qosExistsInDc())
                && validate(validatorOld.networkIsSet())
                && validate(validatorOld.notChangingDataCenterId(getNetwork().getDataCenterId()))
                && validate(validatorNew.networkNameNotUsed())
                && validate(validatorOld.networkNotUsedByRunningVms())
                && validate(validatorOld.nonVmNetworkNotUsedByVms(getNetwork()))
                && validate(validatorOld.nonVmNetworkNotUsedByTemplates(getNetwork()))
                && validate(validatorOld.notRenamingUsedNetwork(getNetworkName()))
                && validate(validatorOld.notRenamingLabel(getNetwork().getLabel()))
                && (oldAndNewNetworkIsNotExternal()
                || validate(validatorOld.externalNetworkDetailsUnchanged(getNetwork())));
    }

    private boolean allowedNetworkLabelManipulation() {
        boolean labelNotChanged = !labelChanged();

        return !getNetwork().isExternal() && (labelNotChanged || labelAdded());
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

        public ValidationResult notRenamingLabel(String newLabel) {
            String oldLabel = network.getLabel();
            if (oldLabel == null || newLabel == null || oldLabel.equals(newLabel)) {
                return ValidationResult.VALID;
            }

            List<VdsNetworkInterface> nics =
                    getDbFacade().getInterfaceDao().getVdsInterfacesByNetworkId(network.getId());
            for (VdsNetworkInterface nic : nics) {
                VdsNetworkInterface labeledNic = nic;
                if (NetworkUtils.isVlan(nic)) {
                    labeledNic = getBaseInterface(nic);
                }

                if (NetworkUtils.isLabeled(labeledNic) && labeledNic.getLabels().contains(oldLabel)) {
                    return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_NETWORK_LABEL_RENAMING_NOT_SUPPORTED);
                }
            }

            return ValidationResult.VALID;
        }

        private VdsNetworkInterface getBaseInterface(VdsNetworkInterface vlan) {
            List<VdsNetworkInterface> hostNics =
                    getDbFacade().getInterfaceDao().getAllInterfacesForVds(vlan.getVdsId());

            for (VdsNetworkInterface hostNic : hostNics) {
                if (NetworkUtils.interfaceBasedOn(vlan, hostNic.getName())) {
                    return hostNic;
                }
            }

            throw new VdcBLLException(VdcBllErrors.LABELED_NETWORK_INTERFACE_NOT_FOUND);
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
                return networkNotUsedByVms();
            }

            return ValidationResult.VALID;
        }

        private boolean networkChangedToNonVmNetwork(Network updatedNetwork) {
            return network.isVmNetwork() && !updatedNetwork.isVmNetwork();
        }

        public ValidationResult networkNotUsedByRunningVms() {
            List<VM> runningVms = new ArrayList<>();
            List<VmNetworkInterface> vnics = getDbFacade().getVmNetworkInterfaceDao().getAllForNetwork(network.getId());
            Map<Guid, List<VmNetworkInterface>> vnicsByVmId = Entities.vmInterfacesByVmId(vnics);

            for (VM vm : getVms()) {
                if (vm.isRunningOrPaused()) {
                    for (VmNetworkInterface nic : vnicsByVmId.get(vm.getId())) {
                        if (VmNetworkCanBeUpdatedPredicate.getInstance().eval(nic)) {
                            runningVms.add(vm);
                            break;
                        }
                    }
                }
            }

            return networkNotUsed(runningVms, VdcBllMessages.VAR__ENTITIES__VMS, VdcBllMessages.VAR__ENTITIES__VM);
        }

        public ValidationResult nonVmNetworkNotUsedByTemplates(Network updatedNetwork) {
            if (networkChangedToNonVmNetwork(updatedNetwork)) {
                return networkNotUsedByTemplates();
            }

            return ValidationResult.VALID;
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

        public ValidationResult notChangingDataCenterId(Guid dataCenterId) {
            final Guid oldDataCenterId = network.getDataCenterId();
            return ValidationResult.failWith(VdcBllMessages.ACTION_TYPE_FAILED_DATA_CENTER_ID_CANNOT_BE_CHANGED)
                    .when(!oldDataCenterId.equals(dataCenterId));
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

    private class SyncNetworkParametersBuilder extends NetworkParametersBuilder {

        public SyncNetworkParametersBuilder(CommandContext commandContext) {
            super(commandContext);
        }

        private ArrayList<VdcActionParametersBase> buildParameters(Network network, Network oldNetwork) {
            ArrayList<VdcActionParametersBase> parameters = new ArrayList<>();
            List<VdsNetworkInterface> nics =
                    getDbFacade().getInterfaceDao().getVdsInterfacesByNetworkId(network.getId());

            // sync network on nics if the label wasn't changed
            if (!labelChanged()) {
                createSyncNetworkParameters(parameters, nics);
                return parameters;
            }

            // add network to labeled interfaces and sync network on the rest
            if (labelAdded() || labelRenamed()) {
                List<VdsNetworkInterface> labeledNics = getLabeledNics(network);
                Map<Guid, VdsNetworkInterface> hostToNic = mapHostToNic(nics);
                List<VdsNetworkInterface> nicsForAdd = new ArrayList<>();
                Set<VdsNetworkInterface> nicsForSync = new HashSet<>();

                // nics to add network
                for (VdsNetworkInterface labeledNic : labeledNics) {
                    VdsNetworkInterface nic = hostToNic.get(labeledNic.getVdsId());

                    // add network to labeled nic if network not configured on host
                    if (nic == null) {
                        nicsForAdd.add(labeledNic);
                    } else {
                        // sync the network
                        nicsForSync.add(nic);
                    }
                }

                // add the unlabeled nics to be synced
                for (VdsNetworkInterface nic : nics) {
                    if (!nicsForSync.contains(nic)) {
                        nicsForSync.add(nic);
                    }
                }

                parameters.addAll(createAddNetworkParameters(nicsForAdd));
                createSyncNetworkParameters(parameters, nicsForSync);
                return parameters;
            }

            // remove network from labeled interfaces
            if (labelRemoved()) {
                List<VdsNetworkInterface> labeledNics = getLabeledNics(oldNetwork);
                Map<Guid, VdsNetworkInterface> hostToNic = mapHostToNic(nics);
                List<VdsNetworkInterface> nicsForRemove = new ArrayList<>();
                Set<VdsNetworkInterface> nicsForSync = new HashSet<>();

                // nics to remove the network from
                for (VdsNetworkInterface labeledNic : labeledNics) {
                    VdsNetworkInterface nic = hostToNic.get(labeledNic.getVdsId());

                    // remove the network from labeled nic
                    if (nic != null) {
                        nicsForRemove.add(labeledNic);
                    }
                }

                // add the unlabeled nics to be synced
                for (VdsNetworkInterface nic : nics) {
                    if (!nicsForSync.contains(nic)) {
                        nicsForSync.add(nic);
                    }
                }

                parameters.addAll(createRemoveNetworkParameters(nicsForRemove));
                createSyncNetworkParameters(parameters, nicsForSync);
                return parameters;
            }

            return parameters;
        }

        private ArrayList<VdcActionParametersBase> createAddNetworkParameters(List<VdsNetworkInterface> nicsForAdd) {
            AddNetworkParametersBuilder builder = new AddNetworkParametersBuilder(getNetwork(), getContext());
            return builder.buildParameters(nicsForAdd);
        }

        private ArrayList<VdcActionParametersBase> createRemoveNetworkParameters(List<VdsNetworkInterface> nicsForRemove) {
            RemoveNetworkParametersBuilder builder =
                    new RemoveNetworkParametersBuilder(getOldNetwork(), getContext(), managementNetworkUtil);
            return builder.buildParameters(nicsForRemove);
        }

        private Map<Guid, VdsNetworkInterface> mapHostToNic(List<VdsNetworkInterface> nics) {
            Map<Guid, VdsNetworkInterface> hostToNic = new HashMap<>(nics.size());
            for (VdsNetworkInterface nic : nics) {
                hostToNic.put(nic.getVdsId(), nic);
            }
            return hostToNic;
        }

        private List<VdsNetworkInterface> getLabeledNics(Network network) {
            List<NetworkCluster> clusters = getNetworkClusterDAO().getAllForNetwork(network.getId());
            List<VdsNetworkInterface> labeledNics = new ArrayList<>();
            for (NetworkCluster networkCluster : clusters) {
                labeledNics.addAll(getDbFacade().getInterfaceDao()
                        .getAllInterfacesByLabelForCluster(networkCluster.getClusterId(), network.getLabel()));
            }
            return labeledNics;
        }

        private void createSyncNetworkParameters(ArrayList<VdcActionParametersBase> parameters,
                Collection<VdsNetworkInterface> nics) {

            Set<Guid> hostIdsToSync = new HashSet<>();
            for (VdsNetworkInterface nic : nics) {
                if (!NetworkUtils.isNetworkInSync(nic,
                        getNetwork(),
                        getDbFacade().getHostNetworkQosDao().get(getNetwork().getQosId()))) {
                    hostIdsToSync.add(nic.getVdsId());
                }
            }

            for (Guid hostId : hostIdsToSync) {
                PersistentSetupNetworksParameters setupNetworkParams = createSetupNetworksParameters(hostId);
                setupNetworkParams.setNetworkNames(getNetworkName());
                setupNetworkParams.setNetworksToSync(Collections.singletonList(getNetworkName()));
                parameters.add(setupNetworkParams);
            }
        }

    }

    private boolean labelChanged() {
        return !Objects.equals(getNetwork().getLabel(), getOldNetwork().getLabel());
    }

    private boolean labelAdded() {
        return !NetworkUtils.isLabeled(getOldNetwork()) && NetworkUtils.isLabeled(getNetwork());
    }

    private boolean labelRemoved() {
        return NetworkUtils.isLabeled(getOldNetwork()) && !NetworkUtils.isLabeled(getNetwork());
    }

    private boolean labelRenamed() {
        return NetworkUtils.isLabeled(getOldNetwork()) && NetworkUtils.isLabeled(getNetwork()) && labelChanged();
    }
}
