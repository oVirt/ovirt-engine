package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.ovirt.engine.core.common.action.ImportVmParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.profiles.CpuProfile;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.HasEntity;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageDiskListModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.FrontendMultipleQueryAsyncResult;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleQueryAsyncCallback;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.inject.Inject;

public class ImportVmFromExportDomainModel extends ListWithDetailsModel {
    public static final String ON_DISK_LOAD = "OnDiskLoad"; //$NON-NLS-1$

    ArchitectureType targetArchitecture;
    private final VmImportDiskListModel importDiskListModel;
    private StoragePool storagePool;
    private boolean hasQuota;
    private final Map<Guid, List<Disk>> missingTemplateDiskMap = new HashMap<>();
    protected ArrayList<StorageDomain> filteredStorageDomains;
    private Map<Guid, ArrayList<Quota>> storageQuotaMap;
    private final Map<Guid, List<Disk>> templateDiskMap = new HashMap<>();
    private final Map<Guid, ImportDiskData> diskImportDataMap = new HashMap<>();

    public StoragePool getStoragePool() {
        return storagePool;
    }

    public void setStoragePool(StoragePool storagePool) {
        this.storagePool = storagePool;
    }

    public ArchitectureType getTargetArchitecture() {
        return targetArchitecture;
    }

    public void setTargetArchitecture(ArchitectureType targetArchitecture) {
        this.targetArchitecture = targetArchitecture;
    }

    private final StorageDiskListModel storageDiskListModel;

    public StorageDiskListModel getStorage() {
        return storageDiskListModel;
    }

    private final ClusterListModel<Void> cluster;

    public ClusterListModel<Void> getCluster() {
        return cluster;
    }

    private ListModel<CpuProfile> cpuProfiles;

    public ListModel<CpuProfile> getCpuProfiles() {
        return cpuProfiles;
    }

    private void setCpuProfiles(ListModel<CpuProfile> value) {
        cpuProfiles = value;
    }

// TODO: should the above be a CpuProfileListModel?
//    private final CpuProfileListModel cpuProfiles;
//
//    public CpuProfileListModel getCpuProfiles() {
//        return cpuProfiles;
//    }

    private final QuotaListModel<Void> clusterQuota;

    public QuotaListModel<Void> getClusterQuota() {
        return clusterQuota;
    }

    private UICommand closeCommand;

    public void setCloseCommand(UICommand closeCommand) {
        this.closeCommand = closeCommand;
    }

    public UICommand getCloseCommand() {
        return closeCommand;
    }

    private final IEventListener<EventArgs> clusterChangedListener = new IEventListener<EventArgs>() {

        @Override
        public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
            if (hasQuota) {
                Frontend.getInstance().runQuery(VdcQueryType.GetAllRelevantQuotasForVdsGroup,
                    new IdQueryParameters(getCluster().getSelectedItem().getId()),
                    new AsyncQuery(ImportVmFromExportDomainModel.this,
                            new INewAsyncCallback() {

                                @Override
                                public void onSuccess(Object model, Object returnValue) {
                                    ImportVmFromExportDomainModel importVmModel = (ImportVmFromExportDomainModel) model;
                                    ArrayList<Quota> quotaList = ((VdcQueryReturnValue) returnValue).getReturnValue();
                                    importVmModel.getClusterQuota().setItems(quotaList);
                                    if (quotaList.isEmpty()
                                            && QuotaEnforcementTypeEnum.HARD_ENFORCEMENT.equals(storagePool.getQuotaEnforcementType())) {
                                        setMessage(ConstantsManager.getInstance()
                                                .getConstants()
                                                .missingQuotaClusterEnforceMode());
                                    } else if (getMessage() != null
                                            && getMessage().equals(ConstantsManager.getInstance()
                                                    .getConstants()
                                                    .missingQuotaClusterEnforceMode())) {
                                        setMessage("");
                                    }
                                }
                            }));
            }
            fetchCpuProfiles(getCluster().getSelectedItem().getId());
        }
    };

    @Override
    public void setSelectedItem(Object value) {
        super.setSelectedItem(value);
        onEntityChanged();
    }

    @Inject
    public ImportVmFromExportDomainModel(final VmImportDiskListModel vmImportDiskListModel,
            final StorageDiskListModel storageDomain, final ClusterListModel<Void> cluster, final QuotaListModel clusterQuota,
            final VmImportGeneralModel vmImportGeneralModel, final VmImportInterfaceListModel vmImportInterfaceListModel,
            final VmImportAppListModel vmImportAppListModel) {
        importDiskListModel = vmImportDiskListModel;
        storageDiskListModel = storageDomain;
        this.cluster = cluster;
        this.clusterQuota = clusterQuota;
        setCpuProfiles(new ListModel<CpuProfile>());
        setDetailList(vmImportGeneralModel, vmImportInterfaceListModel, vmImportAppListModel);
    }

    private void setDetailList(final VmImportGeneralModel vmImportGeneralModel,
            final VmImportInterfaceListModel vmImportInterfaceListModel,
            final VmImportAppListModel vmImportAppListModel) {
        getClusterQuota().setIsAvailable(false);
        List<HasEntity> list = new ArrayList<>();
        list.add(vmImportGeneralModel);
        list.add(vmImportInterfaceListModel);
        list.add(importDiskListModel);
        list.add(vmImportAppListModel);
        setDetailModels(list);
    }

    public void init(List items, final Guid storageDomainId) {
        setItems(items, storageDomainId);
    }

    protected void doInit(final Guid storageDomainId) {
       // get Storage pool
       AsyncDataProvider.getInstance().getDataCentersByStorageDomain(new AsyncQuery(this, new INewAsyncCallback() {

           @Override
           public void onSuccess(Object model, Object returnValue) {
               List<StoragePool> pools = (List<StoragePool>) returnValue;
               if (pools == null || pools.size() != 1) {
                   return;
               }

               StoragePool dataCenter = pools.get(0);
               setStoragePool(dataCenter);
               // show quota
               if (dataCenter != null && dataCenter.getQuotaEnforcementType() != QuotaEnforcementTypeEnum.DISABLED) {
                   hasQuota = true;
               }
               if (hasQuota) {
                   getClusterQuota().setIsAvailable(true);
               }
               getCluster().getSelectedItemChangedEvent().addListener(clusterChangedListener);
               // get cluster
               if (dataCenter != null) {
                   getCluster().setItems(null);
                   AsyncDataProvider.getInstance().getClusterByServiceList(new AsyncQuery(ImportVmFromExportDomainModel.this, new INewAsyncCallback() {
                       @Override
                       public void onSuccess(Object model, Object returnValue) {
                           ArrayList<VDSGroup> clusters = (ArrayList<VDSGroup>) returnValue;

                           ImportVmFromExportDomainModel importModel = (ImportVmFromExportDomainModel) model;
                           ArchitectureType targetArch = importModel.getTargetArchitecture();

                           if (targetArch != null) {
                               List<VDSGroup> filteredClusters = AsyncDataProvider.getInstance().filterByArchitecture(clusters,
                                       targetArch);
                               getCluster().setItems(filteredClusters);
                               getCluster().setSelectedItem(Linq.firstOrDefault(filteredClusters));
                           } else {
                               getCluster().setItems(clusters);
                               getCluster().setSelectedItem(Linq.firstOrDefault(clusters));
                           }

                           getStorage().setItems(null);
                           // get storage domains
                           AsyncDataProvider.getInstance().getStorageDomainList(new AsyncQuery(ImportVmFromExportDomainModel.this,
                                   new INewAsyncCallback() {

                                       @Override
                                       public void onSuccess(Object model, Object returnValue) {
                                           ArrayList<StorageDomain> storageDomains =
                                                   (ArrayList<StorageDomain>) returnValue;
                                           // filter storage domains
                                           filteredStorageDomains = new ArrayList<>();
                                           for (StorageDomain domain : storageDomains) {
                                               if (Linq.isDataActiveStorageDomain(domain)) {
                                                   filteredStorageDomains.add(domain);
                                               }
                                           }

                                           getStorage().setItems((ArrayList) filteredStorageDomains);
                                           if (hasQuota) {
                                               initQuotaForStorageDomains();
                                           } else {
                                               initDisksStorageDomainsList();
                                           }
                                       }

                                   }),
                                   getStoragePool().getId());
                       }

                   }),
                           dataCenter.getId(), true, false);
               }
           }
       }),
               storageDomainId);
    }

    private void fetchCpuProfiles(Guid clusterId) {
        Frontend.getInstance().runQuery(VdcQueryType.GetCpuProfilesByClusterId,
                new IdQueryParameters(clusterId),
                new AsyncQuery(new INewAsyncCallback() {

                    @Override
                    public void onSuccess(Object model, Object returnValue) {
                        List<CpuProfile> cpuProfiles =
                                ((VdcQueryReturnValue) returnValue).getReturnValue();
                        getCpuProfiles().setItems(cpuProfiles);
                    }
                }));
    }

    private void initQuotaForStorageDomains() {
        ArrayList<VdcQueryType> queryTypeList = new ArrayList<>();
        ArrayList<VdcQueryParametersBase> queryParamsList =
                new ArrayList<>();
        for (StorageDomain storage : filteredStorageDomains) {
            queryTypeList.add(VdcQueryType.GetAllRelevantQuotasForStorage);
            queryParamsList.add(new IdQueryParameters(storage.getId()));
        }
        storageQuotaMap = new HashMap<>();
        Frontend.getInstance().runMultipleQueries(queryTypeList,
                queryParamsList,
                new IFrontendMultipleQueryAsyncCallback() {
                    @Override
                    public void executed(FrontendMultipleQueryAsyncResult result) {
                        List<VdcQueryReturnValue> returnValueList =
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
                    }
                });
    }

    private void checkIfDefaultStorageApplicableForAllDisks() {
        boolean isDefaultStorageApplicableForAllDisks = true;
        StorageDomain defaultStorage = (StorageDomain) getStorage().getSelectedItem();
        for (ImportDiskData importData : diskImportDataMap.values()) {
            if (defaultStorage != null && !importData.getStorageDomains().contains(defaultStorage)) {
                isDefaultStorageApplicableForAllDisks = false;
                break;
            } else {
                importData.setSelectedStorageDomain(defaultStorage);
            }
        }

        if ((getMessage() == null || getMessage().isEmpty())
                && !isDefaultStorageApplicableForAllDisks) {
            setMessage(ConstantsManager.getInstance().getConstants().importNotApplicableForDefaultStorage());
        }
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
                            && (getDiskImportData(disk.getId()).getSelectedStorageDomain()
                                    .getStorageType().isBlockDomain())) {
                        ((ImportVmData) item).setWarning(ConstantsManager.getInstance().getConstants()
                                .importSparseDiskToBlockDeviceMustCollapseSnapshots());
                        ((ImportVmData) item).getCollapseSnapshots().setEntity(true);
                        ((ImportVmData) item).getCollapseSnapshots()
                                .setChangeProhibitionReason(ConstantsManager.getInstance()
                                        .getConstants()
                                        .importSparseDiskToBlockDeviceMustCollapseSnapshots());
                        ((ImportVmData) item).getCollapseSnapshots().setIsChangable(false);

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
            ArrayList<VdcQueryType> queryTypeList = new ArrayList<>();
            final ArrayList<VdcQueryParametersBase> queryParamsList = new ArrayList<>();
            for (Guid templateId : templateDiskMap.keySet()) {
                queryTypeList.add(VdcQueryType.GetVmTemplatesDisks);
                queryParamsList.add(new IdQueryParameters(templateId));
            }
            Frontend.getInstance().runMultipleQueries(queryTypeList, queryParamsList, new IFrontendMultipleQueryAsyncCallback() {
                @Override
                public void executed(FrontendMultipleQueryAsyncResult result) {
                    List<VdcQueryReturnValue> returnValueList = result.getReturnValues();
                    Map<Guid, ArrayList<StorageDomain>> templateDisksStorageDomains =
                            new HashMap<>();
                    for (VdcQueryReturnValue returnValue : returnValueList) {
                        for (DiskImage diskImage : (ArrayList<DiskImage>) returnValue.getReturnValue()) {
                            templateDisksStorageDomains.put(diskImage.getImageId(),
                                    getStorageDomainsByIds(diskImage.getStorageIds()));
                        }
                    }

                    for (Entry<Guid, List<Disk>> guidListEntry : templateDiskMap.entrySet()) {
                        for (Disk disk : guidListEntry.getValue()) {
                            DiskImage diskImage = (DiskImage) disk;
                            if (diskImage.getParentId() != null && !Guid.Empty.equals(diskImage.getParentId())) {
                                ArrayList<StorageDomain> storageDomains =
                                        templateDisksStorageDomains.get(diskImage.getParentId());
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
                new GetAllFromExportDomainQueryParameters(storagePool.getId(), ((StorageDomain) getEntity())
                        .getId());
        Frontend.getInstance().runQuery(VdcQueryType.GetTemplatesFromExportDomain, tempVar, new AsyncQuery(ImportVmFromExportDomainModel.this,
                new INewAsyncCallback() {

                    @Override
                    public void onSuccess(Object model, Object returnValue) {
                        Map<VmTemplate, List<DiskImage>> dictionary =
                                (HashMap<VmTemplate, List<DiskImage>>) ((VdcQueryReturnValue) returnValue).getReturnValue();
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
                    }
                }));

    }

    protected void postInitDisks() {
        onDataLoad();

        checkDestFormatCompatibility();
        stopProgress();
        getStorage().getSelectedItemChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                onDataLoad();
            }
        });
    }

    public void onDataLoad() {
        checkIfDefaultStorageApplicableForAllDisks();
        onPropertyChanged(new PropertyChangedEventArgs(ON_DISK_LOAD));
    }

    private ArrayList<StorageDomain> getStorageDomainsByIds(ArrayList<Guid> getstorage_ids) {
        ArrayList<StorageDomain> domains = new ArrayList<>();
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
        ImportDiskData importData = diskImportDataMap.get(diskId);
        if (importData != null) {
            if (storageDiskListModel.getSelectedItem() == null) {
                importData.setSelectedStorageDomain((StorageDomain) storageDiskListModel.getSelectedItem());
            }
        }
        return importData;
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
    protected Object provideDetailModelEntity(Object selectedItem) {
        return selectedItem;
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
        getStorage().validateSelectedItem(
                new IValidation[] { new NotEmptyValidation() });
        getCluster().validateSelectedItem(
                new IValidation[] { new NotEmptyValidation() });

        return getStorage().getIsValid()
                && getCluster().getIsValid()
                && getClusterQuota().getIsValid();
    }

    public void setItems(final Collection value, final Guid storageDomainId)
    {
        String vm_guidKey = "ID ="; //$NON-NLS-1$
        String orKey = " or "; //$NON-NLS-1$
        StringBuilder searchPattern = new StringBuilder();
        searchPattern.append("VM: "); //$NON-NLS-1$

        final List<VM> list = (List<VM>) value;
        for (int i = 0; i < list.size(); i++) {
            VM vm = list.get(i);

            searchPattern.append(vm_guidKey);
            searchPattern.append(vm.getId().toString());
            if (i < list.size() - 1) {
                searchPattern.append(orKey);
            }
        }

        Frontend.getInstance().runQuery(VdcQueryType.Search,
                new SearchParameters(searchPattern.toString(), SearchType.VM),
                new AsyncQuery(this, new INewAsyncCallback() {

                    @Override
                    public void onSuccess(Object model, Object returnValue) {
                        List<VM> vmList =
                                ((VdcQueryReturnValue) returnValue).getReturnValue();

                        List<ImportVmData> vmDataList = new ArrayList<>();

                        for (VM vm : (Iterable<VM>) value) {
                            ImportVmData vmData = new ImportVmData(vm);
                            boolean vmExistsInSystem = vmList.contains(vm);
                            vmData.setExistsInSystem(vmExistsInSystem);
                            if (vmExistsInSystem) {
                                vmData.getClone().setEntity(true);
                                vmData.getClone().setChangeProhibitionReason(ConstantsManager.getInstance()
                                        .getConstants()
                                        .importVMThatExistsInSystemMustClone());
                                vmData.getClone().setIsChangable(false);
                            }
                            vmDataList.add(vmData);
                        }
                        ImportVmFromExportDomainModel.super.setItems(vmDataList);
                        doInit(storageDomainId);
                    }
                }));

    }

    public void setSuperItems(Collection value) {
        super.setItems(value);
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

    private void showCloseMessage(String message) {
        setMessage(message);
        getCommands().clear();
        getCommands().add(getCloseCommand());
        stopProgress();
    }

    public void importVms(IFrontendMultipleActionAsyncCallback callback,
            Map<Guid, Object> cloneObjectMap) {
        startProgress(null);
        Frontend.getInstance().runMultipleAction(
                VdcActionType.ImportVm,
                buildImportVmParameters(cloneObjectMap),
                callback,
                this);
    }

    private List<VdcActionParametersBase> buildImportVmParameters(Map<Guid, Object> cloneObjectMap) {
        List<VdcActionParametersBase> prms = new ArrayList<>();

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
                disk.setvolumeFormat(
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
                if (!cloneObjectMap.containsKey(vm.getId())) {
                    continue;
                }
                prm.setImportAsNewEntity(true);
                prm.setCopyCollapse(true);
                prm.getVm().setName(((ImportVmData) cloneObjectMap.get(vm.getId())).getVm().getName());
            }

            prms.add(prm);

        }

        return prms;
    }
}
