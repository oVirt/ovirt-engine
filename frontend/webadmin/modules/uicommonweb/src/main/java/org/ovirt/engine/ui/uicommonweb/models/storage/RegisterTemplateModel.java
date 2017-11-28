package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ImportVmTemplateFromConfParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.ExternalVnicProfileMapping;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportTemplateData;
import org.ovirt.engine.ui.uicommonweb.models.vms.register.VnicProfileMappingEntity;
import org.ovirt.engine.ui.uicommonweb.models.vms.register.VnicProfileMappingModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class RegisterTemplateModel extends RegisterEntityModel<VmTemplate, ImportTemplateData> {

    public static final String VNIC_PROFILE_MAPPING_COMMAND = "vnicProfileMapping"; //$NON-NLS-1$

    private VnicProfileMappingModel vnicProfileMappingModel;
    private UICommand vnicProfileMappingCommand;
    private Map<Cluster, Set<VnicProfileMappingEntity>> externalVnicProfilesPerTargetCluster;

    public RegisterTemplateModel() {
        this.externalVnicProfilesPerTargetCluster = new HashMap<>();
    }

    @Override
    public void initialize() {
        addVnicProfileMappingCommand();
        super.initialize();
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
                this,
                externalVnicProfilesPerTargetCluster);
        vnicProfileMappingModel.initialize();
    }

    private void updateExternalVnicProfilesPerTargetCluster() {
        final Map<Cluster, Set<VnicProfileMappingEntity>> result = new HashMap<>();
        for (ImportTemplateData importTemplateData : getEntities().getItems()) {
            final Cluster cluster = importTemplateData.getCluster().getSelectedItem();
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
                    getNewVnicProfileMappings(importTemplateData, previousClusterVnicProfileMappings);
            clusterVnicProfileMappings.addAll(vmVnicProfiles);
        }
        externalVnicProfilesPerTargetCluster = result;
    }

    private Set<VnicProfileMappingEntity> getNewVnicProfileMappings(ImportTemplateData importTemplateData,
            Set<VnicProfileMappingEntity> previousClusterVnicProfileMappings) {
        final Set<VnicProfileMappingEntity> result = new HashSet<>();
        for (VmNetworkInterface vnic : importTemplateData.getEntity().getInterfaces()) {
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
        ArrayList<ActionParametersBase> parameters = new ArrayList<>();
        for (ImportTemplateData entityData : getEntities().getItems()) {
            VmTemplate vmTemplate = entityData.getEntity();
            Cluster cluster = entityData.getCluster().getSelectedItem();

            ImportVmTemplateFromConfParameters params = new ImportVmTemplateFromConfParameters();
            params.setExternalVnicProfileMappings(getExternalVnicProfileMappings(cluster));
            params.setContainerId(vmTemplate.getId());
            params.setStorageDomainId(getStorageDomainId());
            params.setImagesExistOnTargetStorageDomain(true);
            params.setClusterId(cluster != null ? cluster.getId() : null);

            if (isQuotaEnabled()) {
                Quota quota = entityData.getClusterQuota().getSelectedItem();
                params.setQuotaId(quota != null ? quota.getId() : null);
                params.setDiskTemplateMap(vmTemplate.getDiskTemplateMap());
                updateDiskQuotas(new ArrayList<Disk>(params.getDiskTemplateMap().values()));
            }

            parameters.add(params);
        }

        startProgress();
        Frontend.getInstance().runMultipleAction(ActionType.ImportVmTemplateFromConfiguration, parameters,
                result -> {
                    stopProgress();
                    cancel();
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
