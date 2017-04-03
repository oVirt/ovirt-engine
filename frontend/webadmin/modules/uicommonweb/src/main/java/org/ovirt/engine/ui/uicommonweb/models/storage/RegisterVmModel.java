package org.ovirt.engine.ui.uicommonweb.models.storage;

import static org.ovirt.engine.ui.uicommonweb.Linq.where;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.ovirt.engine.core.common.action.ImportVmParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.ExternalVnicProfileMapping;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncCallback;
import org.ovirt.engine.ui.frontend.ErrorTranslator;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.vms.register.RegisterVmData;
import org.ovirt.engine.ui.uicommonweb.models.vms.register.VnicProfileMappingEntity;
import org.ovirt.engine.ui.uicommonweb.models.vms.register.VnicProfileMappingModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;

public class RegisterVmModel extends RegisterEntityModel<VM, RegisterVmData> {

    public static final String VNIC_PROFILE_MAPPING_COMMAND = "vnicProfileMapping"; //$NON-NLS-1$
    private static final String NEWLINE = "\n"; //$NON-NLS-1$

    private static final Predicate<RegisterVmData> REASSIGN_MACS_PREDICATE = rvd -> rvd.getReassignMacs().getEntity();
    private static final Predicate<RegisterVmData> NOT_REASSIGN_MACS_PREDICATE = REASSIGN_MACS_PREDICATE.negate();

    private VnicProfileMappingModel vnicProfileMappingModel;
    private UICommand vnicProfileMappingCommand;
    private Map<Cluster, Set<VnicProfileMappingEntity>> externalVnicProfilesPerTargetCluster;

    public RegisterVmModel() {
        super();

        this.externalVnicProfilesPerTargetCluster = new HashMap<>();
    }

    @Override
    public void initialize() {
        addVnicProfileMappingCommand();

        getCluster().getItemsChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                postClustersInit();
            }
        });

        super.initialize();
    }

    private void addVnicProfileMappingCommand() {
        final UICommand vnicProfileMappingCommand = createVnicProfileMappingCommand();
        getCommands().add(vnicProfileMappingCommand);
    }

    private void postClustersInit() {

        validateAllMacs();

        for (final RegisterVmData registerVmData : getEntities().getItems()) {
            final IEventListener<EventArgs> validateMacsListener = new IEventListener<EventArgs>() {
                @Override
                public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                    validateVmMacs(Collections.singletonList(registerVmData));
                }
            };
            registerVmData.getReassignMacs().getEntityChangedEvent().addListener(validateMacsListener);
            registerVmData.getCluster().getSelectedItemChangedEvent().addListener(validateMacsListener);
        }
    }

    private void validateAllMacs() {
        validateVmMacs(getEntities().getItems());
    }

    private void validateVmMacs(final Collection<RegisterVmData> registerVmData) {
        final Collection<RegisterVmData> vmsToBeValidated = getVmsForMacValidation(registerVmData);
        clearVmMacsWarningOnReassignedVms(registerVmData);
        if (vmsToBeValidated.isEmpty()) {
            return;
        }

        final Map<Guid, List<VM>> vmsByClusterId = mapVmsByTargetClusterId(vmsToBeValidated);
        AsyncDataProvider.getInstance().validateVmMacs(
                new AsyncQuery<>(
                        new AsyncCallback<Map<Guid, List<List<String>>>>() {
                            @Override
                            public void onSuccess(Map<Guid, List<List<String>>> returnValue) {
                                processVmMacsValidationResult(vmsToBeValidated, returnValue);
                            }
                        }),
                vmsByClusterId);
    }

    private void processVmMacsValidationResult(
            Collection<RegisterVmData> vmsToBeValidated,
            Map<Guid, List<List<String>>> validationResult) {
        for (RegisterVmData vmData : vmsToBeValidated) {
            final List<List<String>> vmMessages = validationResult.get(vmData.getVm().getId());
            if (vmMessages == null) {
                continue;
            }
            final String translatedMessage = translateMessages(vmMessages);
            updateVmWithMacsValidationResult(vmData, translatedMessage);
        }
    }

    private void updateVmWithMacsValidationResult(RegisterVmData vmData, String translatedMessage) {
        if (translatedMessage.isEmpty()) {
            clearVmMacsWarning(vmData);
        } else {
            setVmMacsWarning(vmData, translatedMessage, Boolean.TRUE);
        }
    }

    private void setVmMacsWarning(RegisterVmData vmData, String message, Boolean isValid) {
        vmData.setWarning(message);
        vmData.getBadMacsExist().setEntity(isValid);
    }

    private void clearVmMacsWarning(RegisterVmData vmData) {
        setVmMacsWarning(vmData, null, Boolean.FALSE);
    }

    private void clearVmMacsWarningOnReassignedVms(Collection<RegisterVmData> registerVmData) {
        final Collection<RegisterVmData> vmsToBeReassigned = where(registerVmData, REASSIGN_MACS_PREDICATE);
        for (RegisterVmData vmData : vmsToBeReassigned) {
            clearVmMacsWarning(vmData);
        }
    }

    private Collection<RegisterVmData> getVmsForMacValidation(Collection<RegisterVmData> registerVmData) {
        return where(registerVmData, NOT_REASSIGN_MACS_PREDICATE);
    }

    private Map<Guid, List<VM>> mapVmsByTargetClusterId(Collection<RegisterVmData> vms) {
        final Map<Guid, List<VM>> result = new HashMap<>();
        for (RegisterVmData vmData : vms) {
            final Guid clusterId = vmData.getCluster().getSelectedItem().getId();
            final List<VM> clusterVms;
            if (result.containsKey(clusterId)) {
                clusterVms = result.get(clusterId);
            } else {
                clusterVms = new ArrayList<>();
                result.put(clusterId, clusterVms);
            }
            clusterVms.add(vmData.getVm());
        }
        return result;
    }

    private String translateMessages(List<List<String>> vmMessages) {
        StringBuilder stringBuilder = new StringBuilder();
        final ErrorTranslator appErrorsTranslator = Frontend.getInstance().getAppErrorsTranslator();
        for (List<String> vmMessage : vmMessages) {
            if (vmMessage.isEmpty()) {
                continue;
            }
            for (String translatedMessage : appErrorsTranslator.translateErrorText(new ArrayList<>(vmMessage))) {
                stringBuilder.append(translatedMessage);
            }
            stringBuilder.append(NEWLINE);
        }
        return stringBuilder.toString();
    }

    private UICommand createVnicProfileMappingCommand() {
        vnicProfileMappingCommand = new UICommand(VNIC_PROFILE_MAPPING_COMMAND, this);
        vnicProfileMappingCommand.setTitle(ConstantsManager.getInstance().getConstants().vnicProfilesMapping());
        return vnicProfileMappingCommand;
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getVnicProfileMappingCommand()) {
            onVnicProfileMap();
        }
    }

    private void onVnicProfileMap() {
        if (getWindow() != null) {
            return;
        }

        createVnicProfileMappingsModel();

        setWindow(vnicProfileMappingModel);
    }

    private void createVnicProfileMappingsModel() {
        updateExternalVnicProfilesPerTargetCluster();
        vnicProfileMappingModel = new VnicProfileMappingModel(
                RegisterVmModel.this,
                externalVnicProfilesPerTargetCluster);
        vnicProfileMappingModel.initialize();
    }

    private void updateExternalVnicProfilesPerTargetCluster() {
        final Map<Cluster, Set<VnicProfileMappingEntity>> result = new HashMap<>();
        for (RegisterVmData registerVmData : getEntities().getItems()) {
            final Cluster cluster = registerVmData.getCluster().getSelectedItem();
            final Set<VnicProfileMappingEntity> clusterVnicProfileMappings;
            if (result.containsKey(cluster)) {
                clusterVnicProfileMappings = result.get(cluster);
            } else {
                clusterVnicProfileMappings = new HashSet<>();
                result.put(cluster, clusterVnicProfileMappings);
            }
            final Set<VnicProfileMappingEntity> previousClusterVnicProfileMappings;
            if (externalVnicProfilesPerTargetCluster.containsKey(cluster)) {
                previousClusterVnicProfileMappings = externalVnicProfilesPerTargetCluster.get(cluster);
            } else {
                previousClusterVnicProfileMappings = new HashSet<>();
            }
            final Set<VnicProfileMappingEntity> vmVnicProfiles =
                    getNewVnicProfileMappings(registerVmData, previousClusterVnicProfileMappings);
            clusterVnicProfileMappings.addAll(vmVnicProfiles);
        }
        externalVnicProfilesPerTargetCluster = result;
    }

    private Set<VnicProfileMappingEntity> getNewVnicProfileMappings(RegisterVmData registerVmData,
            Set<VnicProfileMappingEntity> previousClusterVnicProfileMappings) {
        final Set<VnicProfileMappingEntity> result = new HashSet<>();
        for (VmNetworkInterface vnic : registerVmData.getEntity().getInterfaces()) {
            final VnicProfileMappingEntity newMapping =
                    new VnicProfileMappingEntity(vnic.getNetworkName(), vnic.getVnicProfileName(), null);
            final VnicProfileMappingEntity mapping =
                    previousClusterVnicProfileMappings
                            .stream()
                            .filter(x -> x.equals(newMapping))
                            .findFirst()
                            .orElse(newMapping);
            result.add(mapping);
        }
        return result;
    }

    protected void onSave() {
        ArrayList<VdcActionParametersBase> parameters = new ArrayList<>();
        for (RegisterVmData registerVmData : getEntities().getItems()) {
            VM vm = registerVmData.getEntity();
            Cluster cluster = registerVmData.getCluster().getSelectedItem();

            ImportVmParameters params = new ImportVmParameters(
                    getExternalVnicProfileMappings(cluster),
                    registerVmData.getReassignMacs().getEntity());
            params.setContainerId(vm.getId());
            params.setStorageDomainId(getStorageDomainId());
            params.setImagesExistOnTargetStorageDomain(true);
            params.setClusterId(cluster != null ? cluster.getId() : null);

            if (isQuotaEnabled()) {
                Quota quota = registerVmData.getClusterQuota().getSelectedItem();
                params.setQuotaId(quota != null ? quota.getId() : null);
                params.setDiskMap(vm.getDiskMap());
                updateDiskQuotas(new ArrayList<>(params.getDiskMap().values()));
            }

            parameters.add(params);
        }

        startProgress();
        Frontend.getInstance().runMultipleAction(VdcActionType.ImportVmFromConfiguration, parameters, new IFrontendMultipleActionAsyncCallback() {
            @Override
            public void executed(FrontendMultipleActionAsyncResult result) {
                stopProgress();
                cancel();
            }
        }, this);
    }

    private Collection<ExternalVnicProfileMapping> getExternalVnicProfileMappings(Cluster cluster) {
        final Set<VnicProfileMappingEntity> vnicProfileMappingEntities = getClusterVnicProfileMappingEntities(cluster);
        final Collection<ExternalVnicProfileMapping> result = new ArrayList<>(vnicProfileMappingEntities.size());
        for (VnicProfileMappingEntity vnicProfileMappingEntity : vnicProfileMappingEntities) {
            result.add(vnicProfileMappingEntity.getExternalVnicProfileMapping());
        }
        return result;
    }

    private Set<VnicProfileMappingEntity> getClusterVnicProfileMappingEntities(Cluster cluster) {
        final Set<VnicProfileMappingEntity> result = externalVnicProfilesPerTargetCluster.get(cluster);
        if (result == null) {
            return new HashSet<>();
        } else {
            return result;
        }
    }

    public UICommand getVnicProfileMappingCommand() {
        return vnicProfileMappingCommand;
    }
}
