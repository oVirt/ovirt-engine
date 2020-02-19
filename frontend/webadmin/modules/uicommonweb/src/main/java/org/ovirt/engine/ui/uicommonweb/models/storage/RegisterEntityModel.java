package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.network.ExternalVnicProfileMapping;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportEntityData;
import org.ovirt.engine.ui.uicommonweb.models.vms.register.VnicProfileMappingEntity;
import org.ovirt.engine.ui.uicommonweb.models.vms.register.VnicProfileMappingModel;
import org.ovirt.engine.ui.uicommonweb.validation.I18NNameValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.ValidationResult;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public abstract class RegisterEntityModel<T, E extends ImportEntityData<T>> extends Model {

    public static final String VNIC_PROFILE_MAPPING_COMMAND = "vnicProfileMapping"; //$NON-NLS-1$

    private UICommand vnicProfileMappingCommand;
    private VnicProfileMappingModel vnicProfileMappingModel;
    // holds the source-to-target profile mappings between sessions of the {@link VnicProfileMappingModel}
    private Map<Cluster, Set<VnicProfileMappingEntity>> externalVnicProfilesPerTargetCluster;
    private UICommand cancelCommand;
    private ListModel<E> entities;
    private ListModel<Cluster> cluster;
    private EntityModel<Map<Guid, List<Quota>>> clusterQuotasMap;
    private EntityModel<Map<Guid, Quota>> diskQuotaMap;
    private ListModel<Quota> storageQuota;
    private Guid storageDomainId;
    private StoragePool storagePool;
    public boolean isMappingChangeConfirmed;
    private List<T> entitiesFromDB;

    public RegisterEntityModel() {
        setEntities(new ListModel<E>());
        setCluster(new ListModel<Cluster>());
        setClusterQuotasMap(new EntityModel<>());
        getClusterQuotasMap().setEntity(new HashMap<>());
        setDiskQuotaMap(new EntityModel<>());
        getDiskQuotaMap().setEntity(new HashMap<>());
        setStorageQuota(new ListModel<>());
        setExternalVnicProfilesPerTargetCluster(new HashMap<>());
        setMappingChangeConfirmed(false);
    }

    protected abstract SearchType getSearchType();

    protected abstract String createSearchPattern(Collection<E> entities);

    protected abstract void updateExistingEntities(List<T> entities, Guid storagePoolId);

    protected abstract void onSave();

    protected void onSave(ActionType actionType, List<ActionParametersBase> parameters) {
        startProgress();
        Frontend.getInstance().runMultipleAction(actionType, parameters,
                result -> {
                    stopProgress();
                    cancel();
                }, this);
    }

    public Collection<ExternalVnicProfileMapping> cloneExternalVnicProfiles(Cluster cluster) {
        if (!isMappingChangeConfirmed) {
            return Collections.emptyList();
        }
        return externalVnicProfilesPerTargetCluster.get(cluster).stream()
                .map(VnicProfileMappingEntity::convertExternalVnicProfileMapping)
                .collect(Collectors.toList());
    }

    @Override
    public void initialize() {
        super.initialize();
        // Create and set commands
        UICommand saveCommand = UICommand.createDefaultOkUiCommand("OnSave", this); //$NON-NLS-1$
        getCommands().add(saveCommand);
        getCommands().add(getCancelCommand());

        updateClusters();
    }

    private void updateClusters() {
        AsyncDataProvider.getInstance().getDataCentersByStorageDomain(new AsyncQuery<>(storagePools -> {
            storagePool = storagePools.size() > 0 ? storagePools.get(0) : null;
            if (storagePool == null) {
                return;
            }

            AsyncDataProvider.getInstance().getClusterByServiceList(new AsyncQuery<>(clusters -> {
                for (ImportEntityData<T> entityData : entities.getItems()) {
                    List<Cluster> filteredClusters =
                            AsyncDataProvider.getInstance().filterByArchitecture(clusters, entityData.getArchType());
                    entityData.getCluster().setItems(filteredClusters);
                    entityData.getCluster().setSelectedItem(Linq.firstOrNull(filteredClusters));
                }

                getCluster().setItems(clusters);
                getCluster().setSelectedItem(Linq.firstOrNull(clusters));

                updateClusterQuota(clusters);
                updateStorageQuota();
                updateExistingEntitiesFromDB(storagePool.getId());
            }), storagePool.getId(), true, false);

        }), storageDomainId);
    }

    private void updateExistingEntitiesFromDB(Guid storagePoolId) {
        Frontend.getInstance().runQuery(QueryType.Search,
                new SearchParameters(createSearchPattern(entities.getItems()), getSearchType()),
                new AsyncQuery<QueryReturnValue>(returnValue -> {
                    entitiesFromDB = returnValue.getReturnValue();
                    updateExistingEntities(entitiesFromDB, storagePoolId);
                }));
    }

    private void updateStorageQuota() {
        if (!isQuotaEnabled()) {
            return;
        }

        AsyncDataProvider.getInstance().getAllRelevantQuotasForStorageSorted(new AsyncQuery<>(
                quotas -> {
                    quotas = (quotas != null) ? quotas : new ArrayList<Quota>();

                    getStorageQuota().setItems(quotas);
                    getStorageQuota().setSelectedItem(Linq.firstOrNull(quotas));
                }), storageDomainId, null);
    }

    private void updateClusterQuota(List<Cluster> clusters) {
        if (!isQuotaEnabled()) {
            return;
        }

        List<QueryType> queries = new ArrayList<>();
        List<QueryParametersBase> params = new ArrayList<>();
        for (Cluster cluster : clusters) {
            queries.add(QueryType.GetAllRelevantQuotasForCluster);
            params.add(new IdQueryParameters(cluster.getId()));
        }

        Frontend.getInstance().runMultipleQueries(queries, params, result -> {
            Map<Guid, List<Quota>> clusterQuotasMap = new HashMap<>();
            for (int i = 0; i < result.getReturnValues().size(); i++) {
                List<Quota> quotas = result.getReturnValues().get(i).getReturnValue();
                Guid clusterId = ((IdQueryParameters) result.getParameters().get(i)).getId();

                clusterQuotasMap.put(clusterId, quotas);
            }
            getClusterQuotasMap().setEntity(clusterQuotasMap);
        });
    }

    public void selectQuotaByName(String name, ListModel<Quota> listModel) {
        for (Quota quota : listModel.getItems()) {
            if (quota.getQuotaName().equals(name)) {
                listModel.setSelectedItem(quota);
                break;
            }
        }
    }

    public List<String> getQuotaNames(List<Quota> quotas) {
        List<String> names = new ArrayList<>();
        if (quotas != null) {
            for (Quota quota : quotas) {
                names.add(quota.getQuotaName());
            }
        }
        return names;
    }

    public Quota getQuotaByName(String name, List<Quota> quotas) {
        for (Quota quota : quotas) {
            if (quota.getQuotaName().equals(name)) {
                return quota;
            }
        }
        return null;
    }

    protected void updateDiskQuotas(List<Disk> disks) {
        for (Disk disk : disks) {
            Quota quota = getDiskQuotaMap().getEntity().get(disk.getId());
            if (quota == null) {
                quota = getStorageQuota().getSelectedItem();
            }
            if (quota != null) {
                ((DiskImage) disk).setQuotaId(quota.getId());
            }
        }
    }

    protected boolean validate() {
        updateExistingEntities(entitiesFromDB, storagePool.getId());
        return validateNames();
    }

    private boolean validateNames() {
        for (E entity : entities.getItems()) {
            EntityModel<String> nameEntity = validateName(entity);
            if (!nameEntity.getIsValid()) {
                entity.setError(ConstantsManager.getInstance().getConstants().invalidName());
                entity.setInvalidityReasons(nameEntity.getInvalidityReasons());
                entity.setIsValid(nameEntity.getIsValid());
                entities.setSelectedItem(entity);
                onPropertyChanged(new PropertyChangedEventArgs("InvalidVm")); //$NON-NLS-1$
                return false;
            }
        }
        return true;
    }

    private EntityModel<String> validateName(E entityData) {
        final int maxNameLength = AsyncDataProvider.getInstance().getMaxVmNameLength();
        EntityModel<String> name = new EntityModel<>(entityData.getName());
        name.validateEntity(
                new IValidation[] {
                        new NotEmptyValidation(),
                        new LengthValidation(maxNameLength),
                        new I18NNameValidation(),
                        uniqueNameValidation(entityData)
                });

        return name;
    }

    private IValidation uniqueNameValidation(E entityData) {
        return value -> (entityData.isNameExistsInSystem() || !isNameUnique(entityData)) ?
                ValidationResult.fail(ConstantsManager.getInstance()
                        .getConstants()
                        .nameMustBeUniqueInvalidReason())
                : ValidationResult.ok();
    }

    private boolean isNameUnique(E data) {
        return getEntities().getItems()
                .stream()
                .noneMatch(item -> data != item && data.getName().equals(item.getName()));
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if ("OnSave".equals(command.getName())) { //$NON-NLS-1$
            onSave();
        }
        if (VNIC_PROFILE_MAPPING_COMMAND.equals(command.getName())) {
            onVnicProfileMappingCommand();
        }
    }

    private void onVnicProfileMappingCommand() {
        Map<Cluster, Set<VnicProfileMappingEntity>> result = updateExternalVnicProfilesPerTargetCluster();
        setExternalVnicProfilesPerTargetCluster(result);
        // pass the updated profile mappings to the model so it can display updated values in the dialog
        vnicProfileMappingModel = new VnicProfileMappingModel(this, externalVnicProfilesPerTargetCluster);
        // while initializing the model the current vnic profiles are fetched from the backend and the target profiles
        // are adjusted accordingly
        vnicProfileMappingModel.initialize();
        setWindow(vnicProfileMappingModel);
    }

    /**
     * on first call: create new mappings with no target profile for each interface of the entities being registered.
     * on subsequent calls: repopulate with existing mappings and user selections
     */
    private Map<Cluster, Set<VnicProfileMappingEntity>> updateExternalVnicProfilesPerTargetCluster() {
        Map<Cluster, Set<VnicProfileMappingEntity>> updated = new HashMap<>();

        // each importEntityData holds a VM or VmTemplate instance
        // the update may be for several VMs or VmTemplates over various clusters
        for (E importEntityData : getEntities().getItems()) {
            Cluster cluster = importEntityData.getCluster().getSelectedItem();

            // get the set of profiles being updated for the cluster or create new
            Set<VnicProfileMappingEntity> clusterVnicProfileMappings;
            if (updated.containsKey(cluster)) {
                clusterVnicProfileMappings = updated.get(cluster);
            } else {
                clusterVnicProfileMappings = new HashSet<>();
                updated.put(cluster, clusterVnicProfileMappings);
            }
            // get the previous set of profiles for the cluster or create new
            Set<VnicProfileMappingEntity> previousClusterVnicProfileMappings;
            if (externalVnicProfilesPerTargetCluster.containsKey(cluster)) {
                previousClusterVnicProfileMappings = externalVnicProfilesPerTargetCluster.get(cluster);
            } else {
                previousClusterVnicProfileMappings = new HashSet<>();
            }
            // create or set mappings according to currently existing interfaces and previous user selections
            Set<VnicProfileMappingEntity> vmVnicProfiles = getNewVnicProfileMappings(getInterfaces(importEntityData), previousClusterVnicProfileMappings);
            clusterVnicProfileMappings.addAll(vmVnicProfiles);
        }
        return updated;
    }

    protected abstract List<VmNetworkInterface> getInterfaces(E importEntityData);

    /**
     * create a {@link VnicProfileMappingEntity} with no target profile for each pre-loaded interface of the registered entity
     * if a previous mapping for the same source profile is not found.
     *
     * @param interfaces the interfaces of the registered entity initially loaded by the frontend when the register action started
     * @param previousClusterVnicProfileMappings the mappings passed to {@link VnicProfileMappingModel}, with user selections of target profiles
     * @return a set of mappings for all interfaces of registered entity
     */
    private Set<VnicProfileMappingEntity> getNewVnicProfileMappings(List<VmNetworkInterface> interfaces, Set<VnicProfileMappingEntity> previousClusterVnicProfileMappings) {
        Set<VnicProfileMappingEntity> result = new HashSet<>();
        for (VmNetworkInterface vnic : interfaces) {
            VnicProfileMappingEntity newMapping = new VnicProfileMappingEntity(vnic.getNetworkName(), vnic.getVnicProfileName(), null);
            VnicProfileMappingEntity mapping = previousClusterVnicProfileMappings.stream().filter(x -> x.isSameSourceProfile(newMapping)).findFirst().orElse(newMapping);
            // warning: the Set.add() uses the equals of {@link VnicProfileMappingEntity} which only compares the source profile
            result.add(mapping);
        }
        return result;
    }

    public UICommand getVnicProfileMappingCommand() {
        return vnicProfileMappingCommand;
    }

    protected void addVnicProfileMappingCommand() {
        getCommands().add(createVnicProfileMappingCommand());
    }

    private UICommand createVnicProfileMappingCommand() {
        vnicProfileMappingCommand = new UICommand(VNIC_PROFILE_MAPPING_COMMAND, this);
        vnicProfileMappingCommand.setTitle(ConstantsManager.getInstance().getConstants().vnicProfilesMapping());
        return vnicProfileMappingCommand;
    }

    protected void cancel() {
        getCancelCommand().execute();
    }

    @Override
    public UICommand getCancelCommand() {
        return cancelCommand;
    }

    public void setCancelCommand(UICommand cancelCommand) {
        this.cancelCommand = cancelCommand;
    }

    public ListModel<E> getEntities() {
        return entities;
    }

    public void setEntities(ListModel<E> entities) {
        this.entities = entities;
    }

    public ListModel<Cluster> getCluster() {
        return cluster;
    }

    private void setCluster(ListModel<Cluster> value) {
        cluster = value;
    }

    public Guid getStorageDomainId() {
        return storageDomainId;
    }

    public void setStorageDomainId(Guid storageDomainId) {
        this.storageDomainId = storageDomainId;
    }

    public EntityModel<Map<Guid, List<Quota>>> getClusterQuotasMap() {
        return clusterQuotasMap;
    }

    public void setClusterQuotasMap(EntityModel<Map<Guid, List<Quota>>> clusterQuotasMap) {
        this.clusterQuotasMap = clusterQuotasMap;
    }

    public EntityModel<Map<Guid, Quota>> getDiskQuotaMap() {
        return diskQuotaMap;
    }

    public void setDiskQuotaMap(EntityModel<Map<Guid, Quota>> diskQuotaMap) {
        this.diskQuotaMap = diskQuotaMap;
    }

    public ListModel<Quota> getStorageQuota() {
        return storageQuota;
    }

    public void setStorageQuota(ListModel<Quota> storageQuota) {
        this.storageQuota = storageQuota;
    }

    public boolean isQuotaEnabled() {
        return storagePool.getQuotaEnforcementType() != QuotaEnforcementTypeEnum.DISABLED;
    }

    public void setExternalVnicProfilesPerTargetCluster(Map<Cluster, Set<VnicProfileMappingEntity>> externalVnicProfilesPerTargetCluster) {
        this.externalVnicProfilesPerTargetCluster = externalVnicProfilesPerTargetCluster;
    }

    public void setMappingChangeConfirmed(boolean mappingChangeConfirmed) {
        isMappingChangeConfirmed = mappingChangeConfirmed;
    }
}
