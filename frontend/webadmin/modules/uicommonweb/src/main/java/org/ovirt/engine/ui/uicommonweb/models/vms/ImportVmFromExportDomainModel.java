package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ImportVmParameters;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.profiles.CpuProfile;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncCallback;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaListModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.inject.Inject;

public class ImportVmFromExportDomainModel extends ImportVmModel {
    public static final String ON_DISK_LOAD = "OnDiskLoad"; //$NON-NLS-1$

    private final VmImportDiskListModel importDiskListModel;
    private final Map<Guid, List<Disk>> missingTemplateDiskMap = new HashMap<>();
    protected ArrayList<StorageDomain> filteredStorageDomains;
    private Map<Guid, ArrayList<Quota>> storageQuotaMap;
    private final Map<Guid, List<Disk>> templateDiskMap = new HashMap<>();
    private final Map<Guid, ImportDiskData> diskImportDataMap = new HashMap<>();

    @Override
    public void setSelectedItem(Object value) {
        super.setSelectedItem(value);
        onEntityChanged();
    }

    @Inject
    public ImportVmFromExportDomainModel(final VmImportDiskListModel vmImportDiskListModel,
            final ClusterListModel<Void> cluster, final QuotaListModel clusterQuota,
            final VmImportGeneralModel vmImportGeneralModel, final VmImportInterfaceListModel vmImportInterfaceListModel,
            final VmImportAppListModel vmImportAppListModel) {
        super(cluster, clusterQuota);
        importDiskListModel = vmImportDiskListModel;
        setDetailList(vmImportGeneralModel, vmImportInterfaceListModel, importDiskListModel, vmImportAppListModel);
    }

    protected void doInit() {
        StoragePool dataCenter = getStoragePool();
        if (dataCenter == null) {
            return;
        }
        getClusterQuota().setIsAvailable(dataCenter.getQuotaEnforcementType() != QuotaEnforcementTypeEnum.DISABLED);
        getCluster().getSelectedItemChangedEvent().addListener(clusterChangedListener);
        // get cluster
        getCluster().setItems(null);
        AsyncDataProvider.getInstance().getClusterByServiceList(new AsyncQuery<>(clusters -> {
            ArchitectureType targetArch = getTargetArchitecture();

            if (targetArch != null) {
                List<Cluster> filteredClusters = AsyncDataProvider.getInstance().filterByArchitecture(clusters,
                        targetArch);
                getCluster().setItems(filteredClusters);
                getCluster().setSelectedItem(Linq.firstOrNull(filteredClusters));
            } else {
                getCluster().setItems(clusters);
                getCluster().setSelectedItem(Linq.firstOrNull(clusters));
            }

            // get storage domains
            AsyncDataProvider.getInstance().getStorageDomainList(new AsyncQuery<>(
                            storageDomains -> {
                                // filter storage domains
                                filteredStorageDomains = new ArrayList<>();
                                for (StorageDomain domain : storageDomains) {
                                    if (Linq.isDataActiveStorageDomain(domain)) {
                                        filteredStorageDomains.add(domain);
                                    }
                                }

                                if (getClusterQuota().getIsAvailable()) {
                                    initQuotaForStorageDomains();
                                } else {
                                    initDisksStorageDomainsList();
                                }
                            }),
                    getStoragePool().getId());
        }), dataCenter.getId(), true, false);
    }

    private void initQuotaForStorageDomains() {
        ArrayList<QueryType> queryTypeList = new ArrayList<>();
        ArrayList<QueryParametersBase> queryParamsList =
                new ArrayList<>();
        for (StorageDomain storage : filteredStorageDomains) {
            queryTypeList.add(QueryType.GetAllRelevantQuotasForStorage);
            queryParamsList.add(new IdQueryParameters(storage.getId()));
        }
        storageQuotaMap = new HashMap<>();
        Frontend.getInstance().runMultipleQueries(queryTypeList,
                queryParamsList,
                result -> {
                    List<QueryReturnValue> returnValueList =
                            result.getReturnValues();
                    boolean noQuota = true;
                    for (int i = 0; i < filteredStorageDomains.size(); i++) {
                        ArrayList<Quota> quotaList = returnValueList.get(i)
                                .getReturnValue();
                        if (noQuota
                                && !quotaList.isEmpty()) {
                            noQuota = false;
                        }
                        storageQuotaMap.put(
                                filteredStorageDomains.get(i).getId(),
                                quotaList);
                    }
                    if (noQuota
                            && QuotaEnforcementTypeEnum.HARD_ENFORCEMENT.equals(storagePool.getQuotaEnforcementType())) {
                        showCloseMessage(ConstantsManager.getInstance()
                                .getConstants()
                                .missingQuotaStorageEnforceMode());
                    }
                    initDisksStorageDomainsList();
                });
    }

    protected void checkDestFormatCompatibility() {
        for (Object item : getItems()) {
            VM vm = ((ImportVmData) item).getVm();
            if (vm.getDiskMap() != null) {
                for (Map.Entry<Guid, Disk> pair : vm.getDiskMap().entrySet()) {
                    DiskImage disk = (DiskImage) pair.getValue();
                    if (disk.getVolumeType() == VolumeType.Sparse
                            && disk.getVolumeFormat() == VolumeFormat.RAW
                            && getDiskImportData(disk.getId()) != null
                            && getDiskImportData(disk.getId()).getSelectedStorageDomain()
                                    .getStorageType().isBlockDomain()) {
                        ((ImportVmData) item).setWarning(ConstantsManager.getInstance().getConstants()
                                .importSparseDiskToBlockDeviceMustCollapseSnapshots());
                        ((ImportVmData) item).getCollapseSnapshots().setEntity(true);
                        ((ImportVmData) item).getCollapseSnapshots().setIsChangeable(false);
                        ((ImportVmData) item).getCollapseSnapshots()
                                .setChangeProhibitionReason(ConstantsManager.getInstance()
                                        .getConstants()
                                        .importSparseDiskToBlockDeviceMustCollapseSnapshots());

                        onPropertyChanged(new PropertyChangedEventArgs(ON_DISK_LOAD));
                    }
                }
            }
        }
    }

    protected void initDisksStorageDomainsList() {
        for (Object item : getItems()) {
            ImportVmData importVmData = (ImportVmData) item;
            VM vm = importVmData.getVm();
            if (!Guid.Empty.equals(vm.getVmtGuid())) {
                if (!templateDiskMap.containsKey(vm.getVmtGuid())) {
                    templateDiskMap.put(vm.getVmtGuid(), new ArrayList<Disk>());
                }
                templateDiskMap.get(vm.getVmtGuid()).addAll(extractRootDisks(vm));
            }

            for (Disk disk : vm.getDiskMap().values()) {
                DiskImage diskImage = (DiskImage) disk;
                addDiskImportData(diskImage.getId(),
                        filteredStorageDomains,
                        diskImage.getVolumeType(),
                        importVmData.getCollapseSnapshots());
            }
        }
        if (!templateDiskMap.isEmpty()) {
            ArrayList<QueryType> queryTypeList = new ArrayList<>();
            final ArrayList<QueryParametersBase> queryParamsList = new ArrayList<>();
            for (Guid templateId : templateDiskMap.keySet()) {
                queryTypeList.add(QueryType.GetVmTemplatesDisks);
                queryParamsList.add(new IdQueryParameters(templateId));
            }
            Frontend.getInstance().runMultipleQueries(queryTypeList, queryParamsList, result -> {
                List<QueryReturnValue> returnValueList = result.getReturnValues();
                Map<Guid, List<StorageDomain>> templateDisksStorageDomains = new HashMap<>();
                for (QueryReturnValue returnValue : returnValueList) {
                    for (DiskImage diskImage : (ArrayList<DiskImage>) returnValue.getReturnValue()) {
                        templateDisksStorageDomains.put(diskImage.getImageId(),
                                getStorageDomainsByIds(diskImage.getStorageIds()));
                    }
                }

                for (Entry<Guid, List<Disk>> guidListEntry : templateDiskMap.entrySet()) {
                    for (Disk disk : guidListEntry.getValue()) {
                        DiskImage diskImage = (DiskImage) disk;
                        if (diskImage.getParentId() != null && !Guid.Empty.equals(diskImage.getParentId())) {
                            List<StorageDomain> storageDomains = templateDisksStorageDomains.get(diskImage.getParentId());
                            if (storageDomains == null) {
                                missingTemplateDiskMap.put(guidListEntry.getKey(), guidListEntry.getValue());
                            }
                        }
                    }
                }
                if (!missingTemplateDiskMap.keySet().isEmpty()) {
                    getTemplatesFromExportDomain();
                } else {
                    postInitDisks();
                }
            });
        } else {
            postInitDisks();
        }

    }

    private Collection<Disk> extractRootDisks(VM vm) {
        Set<Disk> rootDisks = new HashSet<>();

        for (DiskImage candidate : vm.getImages()) {
            if (isRoot(candidate, vm.getImages())) {
                rootDisks.add(candidate);
            }
        }

        return rootDisks;
    }

    private boolean isRoot(DiskImage candidate, List<DiskImage> images) {
        for (DiskImage image : images) {
            if (candidate.getParentId().equals(image.getImageId())) {
//                if the candidate has a parent then it is not a root
                return false;
            }
        }
//        if we did not find a parent of a candidate then it is a root
        return true;
    }

    protected void getTemplatesFromExportDomain() {
        GetAllFromExportDomainQueryParameters tempVar =
                new GetAllFromExportDomainQueryParameters(storagePool.getId(), (Guid) getEntity());
        Frontend.getInstance().runQuery(QueryType.GetTemplatesFromExportDomain, tempVar,
                new AsyncQuery<QueryReturnValue>(returnValue -> {
                    Map<VmTemplate, List<DiskImage>> dictionary = (HashMap<VmTemplate, List<DiskImage>>) returnValue.getReturnValue();
                    Map<Guid, Guid> tempMap = new HashMap<>();
                    for (Entry<VmTemplate, List<DiskImage>> entry : dictionary.entrySet()) {
                        tempMap.put(entry.getKey().getId(), null);
                    }
                    for (Entry<Guid, List<Disk>> missingTemplateEntry : missingTemplateDiskMap.entrySet()) {
                        if (tempMap.containsKey(missingTemplateEntry.getKey())) {
                            for (Disk disk : missingTemplateEntry.getValue()) {
                                addDiskImportData(disk.getId(),
                                        filteredStorageDomains,
                                        ((DiskImage) disk).getVolumeType(),
                                        new EntityModel(true));
                            }
                        } else {
                            showCloseMessage(ConstantsManager.getInstance()
                                    .getConstants()
                                    .errorTemplateCannotBeFoundMessage());
                            return;
                        }
                    }
                    ImportVmFromExportDomainModel.this.setMessage(ConstantsManager.getInstance()
                            .getConstants()
                            .importMissingStorages());

                    for (ImportVmData vmData : (List<ImportVmData>) getItems()) {
                        if (!Guid.Empty.equals(vmData.getVm().getVmtGuid())
                                && missingTemplateDiskMap.containsKey(vmData.getVm().getVmtGuid())) {
                            vmData.setTemplateExistsInSetup(false);
                        }
                    }

                    postInitDisks();
                }));

    }

    protected void postInitDisks() {
        onDataLoad();

        checkDestFormatCompatibility();
        stopProgress();
    }

    public void onDataLoad() {
        onPropertyChanged(new PropertyChangedEventArgs(ON_DISK_LOAD));
    }

    private List<StorageDomain> getStorageDomainsByIds(List<Guid> getstorage_ids) {
        List<StorageDomain> domains = new ArrayList<>();
        for (Guid storageDomainId : getstorage_ids) {
            for (StorageDomain storageDomain : filteredStorageDomains) {
                if (storageDomainId.equals(storageDomain.getId())) {
                    domains.add(storageDomain);
                    break;
                }
            }
        }
        return domains;
    }

    public ImportDiskData getDiskImportData(Guid diskId) {
        return diskImportDataMap.get(diskId);
    }

    protected void addDiskImportData(Guid diskId,
            ArrayList<StorageDomain> storageDomains,
            VolumeType volumeType, EntityModel collapseSnapshots) {
        ImportDiskData data = new ImportDiskData();
        data.setCollapseSnapshot(collapseSnapshots);
        data.setAllStorageDomains(filteredStorageDomains);
        data.setStorageDomains(storageDomains);
        data.setVolumeType(volumeType);
        data.setStorageQuotaList(storageQuotaMap);
        diskImportDataMap.put(diskId, data);

    }

    @Override
    protected void activeDetailModelChanged() {
        super.activeDetailModelChanged();
    }

    public boolean validate() {
        if (QuotaEnforcementTypeEnum.HARD_ENFORCEMENT.equals(storagePool.getQuotaEnforcementType())) {
            getClusterQuota().validateSelectedItem(
                    new IValidation[] { new NotEmptyValidation() });
            for (ImportDiskData item : diskImportDataMap.values()) {
                if (item.getSelectedQuota() == null) {
                    setMessage(ConstantsManager.getInstance().getConstants().missingQuotaStorageEnforceMode());
                    return false;
                }
            }
            if (getMessage() != null
                    && getMessage().equals(ConstantsManager.getInstance()
                            .getConstants()
                            .missingQuotaStorageEnforceMode())) {
                setMessage("");
            }
        }
        getCluster().validateSelectedItem(
                new IValidation[] { new NotEmptyValidation() });

        return validateNames()
                && getCluster().getIsValid()
                && getClusterQuota().getIsValid();
    }

    protected void withDataCenterLoaded(Guid storageDomainId, final AsyncCallback<List<StoragePool>> callback) {
        // get Storage pool
        AsyncDataProvider.getInstance().getDataCentersByStorageDomain(new AsyncQuery<>(pools -> {
            if (pools == null || pools.size() != 1) {
                return;
            }

            StoragePool dataCenter = pools.get(0);
            setStoragePool(dataCenter);
            callback.onSuccess(pools);
        }), storageDomainId);
    }

    public void init(final List<VM> externalVms, final Guid storageDomainId) {
        withDataCenterLoaded(storageDomainId, returnValue -> setItems(r -> doInit(), externalVms));
    }

    @Override
    protected String getListName() {
        return "ImportVmModel"; //$NON-NLS-1$
    }

    public SearchableListModel getImportDiskListModel() {
        return importDiskListModel;
    }

    public boolean isQuotaEnabled() {
        return getStoragePool() != null
                && getStoragePool().getQuotaEnforcementType() != QuotaEnforcementTypeEnum.DISABLED;
    }

    public void executeImport(IFrontendMultipleActionAsyncCallback callback) {
        startProgress();
        Frontend.getInstance().runMultipleAction(
                ActionType.ImportVm,
                buildImportVmParameters(),
                callback,
                this);
    }


    private List<ActionParametersBase> buildImportVmParameters() {
        List<ActionParametersBase> prms = new ArrayList<>();

        for (Object item : getItems()) {
            VM vm = ((ImportVmData) item).getVm();

            ImportVmParameters prm = new ImportVmParameters(vm, (Guid) getEntity(),
                    Guid.Empty, getStoragePool().getId(),
                    getCluster().getSelectedItem().getId());

            if (getClusterQuota().getSelectedItem() != null &&
                    getClusterQuota().getIsAvailable()) {
                prm.setQuotaId(getClusterQuota().getSelectedItem().getId());
            }

            CpuProfile cpuProfile = getCpuProfiles().getSelectedItem();
            if (cpuProfile != null) {
                prm.setCpuProfileId(cpuProfile.getId());
            }

            prm.setForceOverride(true);
            prm.setCopyCollapse(((ImportVmData) item).getCollapseSnapshots().getEntity());

            Map<Guid, Guid> map = new HashMap<>();
            for (Map.Entry<Guid, Disk> entry : vm.getDiskMap().entrySet()) {
                DiskImage disk = (DiskImage) entry.getValue();
                map.put(disk.getId(), getDiskImportData(disk.getId()).getSelectedStorageDomain().getId());
                disk.setVolumeFormat(
                        AsyncDataProvider.getInstance().getDiskVolumeFormat(
                                getDiskImportData(disk.getId()).getSelectedVolumeType(),
                                getDiskImportData(
                                        disk.getId()).getSelectedStorageDomain().getStorageType()));
                disk.setVolumeType(getDiskImportData(disk.getId()).getSelectedVolumeType());

                if (getDiskImportData(disk.getId()).getSelectedQuota() != null) {
                    disk.setQuotaId(
                            getDiskImportData(disk.getId()).getSelectedQuota().getId());
                }
            }

            prm.setImageToDestinationDomainMap(map);

            if (((ImportVmData) item).isExistsInSystem() ||
                    ((ImportVmData) item).getClone().getEntity()) {
                prm.setImportAsNewEntity(true);
                prm.setCopyCollapse(true);
            }
            prms.add(prm);
        }

        return prms;
    }
}
