package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.common.action.ImportVmParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.ExternalVnicProfileMapping;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportEntityData;
import org.ovirt.engine.ui.uicommonweb.models.vms.register.VnicProfileMappingModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;

public class RegisterVmModel extends RegisterEntityModel<VM> {

    public static final String VNIC_PROFILE_MAPPING_COMMAND = "vnicProfileMapping"; //$NON-NLS-1$

    private VnicProfileMappingModel vnicProfileMappingModel;
    private UICommand vnicProfileMappingCommand;
    private Map<Cluster, Set<ExternalVnicProfileMapping>> externalVnicProfilesPerTargetCluster;

    public RegisterVmModel() {
        super();

        this.externalVnicProfilesPerTargetCluster = new HashMap<>();

        addVnicProfileMappingCommand();
    }

    private void addVnicProfileMappingCommand() {
        final UICommand vnicProfileMappingCommand = createVnicProfileMappingCommand();
        getCommands().add(vnicProfileMappingCommand);
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
        for (ImportEntityData<VM> vmImportEntityData : getEntities().getItems()) {
            final Cluster cluster = vmImportEntityData.getCluster().getSelectedItem();
            final Set<ExternalVnicProfileMapping> clusterVnicProfileMappings;
            if (externalVnicProfilesPerTargetCluster.containsKey(cluster)) {
                clusterVnicProfileMappings = externalVnicProfilesPerTargetCluster.get(cluster);
            } else {
                clusterVnicProfileMappings = new HashSet<>();
                externalVnicProfilesPerTargetCluster.put(cluster, clusterVnicProfileMappings);
            }
            final Set<ExternalVnicProfileMapping> vmVnicProfiles =
                    getNewVnicProfileMappings(vmImportEntityData, clusterVnicProfileMappings);
            clusterVnicProfileMappings.addAll(vmVnicProfiles);
        }
    }

    private Set<ExternalVnicProfileMapping> getNewVnicProfileMappings(ImportEntityData<VM> vmImportEntityData,
            Set<ExternalVnicProfileMapping> clusterVnicProfileMappings) {
        final Set<ExternalVnicProfileMapping> result = new HashSet<>();
        for (VmNetworkInterface vnic : vmImportEntityData.getEntity().getInterfaces()) {
            final ExternalVnicProfileMapping newMapping =
                    new ExternalVnicProfileMapping(vnic.getNetworkName(), vnic.getVnicProfileName(), null);
            if (!clusterVnicProfileMappings.contains(newMapping)) {
                result.add(newMapping);
            }
        }
        return result;
    }

    protected void onSave() {
        ArrayList<VdcActionParametersBase> parameters = new ArrayList<>();
        for (ImportEntityData<VM> entityData : getEntities().getItems()) {
            VM vm = entityData.getEntity();
            Cluster cluster = entityData.getCluster().getSelectedItem();

            final Collection<ExternalVnicProfileMapping> externalVnicProfileMappings =
                    externalVnicProfilesPerTargetCluster.get(cluster);
            ImportVmParameters params = new ImportVmParameters(
                    externalVnicProfileMappings,
                    false);
            params.setContainerId(vm.getId());
            params.setStorageDomainId(getStorageDomainId());
            params.setImagesExistOnTargetStorageDomain(true);
            params.setClusterId(cluster != null ? cluster.getId() : null);

            if (isQuotaEnabled()) {
                Quota quota = entityData.getClusterQuota().getSelectedItem();
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

    public UICommand getVnicProfileMappingCommand() {
        return vnicProfileMappingCommand;
    }
}
