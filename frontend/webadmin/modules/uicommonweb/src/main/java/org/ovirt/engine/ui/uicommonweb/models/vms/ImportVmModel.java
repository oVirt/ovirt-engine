package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.DiskImageList;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParameters;
import org.ovirt.engine.core.common.queries.GetAllRelevantQuotasForStorageParameters;
import org.ovirt.engine.core.common.queries.GetAllRelevantQuotasForVdsGroupParameters;
import org.ovirt.engine.core.common.queries.GetVmTemplatesDisksParameters;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.ObservableCollection;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendMultipleQueryAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleQueryAsyncCallback;

@SuppressWarnings("unused")
public class ImportVmModel extends ListWithDetailsModel implements IIsObjectInSetup {
    public static final String ON_DISK_LOAD = "OnDiskLoad"; //$NON-NLS-1$

    private VmImportDiskListModel importDiskListModel;
    private storage_pool storagePool;
    private boolean hasQuota;
    private final Map<Guid, List<Disk>> missingTemplateDiskMap = new HashMap<Guid, List<Disk>>();
    protected ArrayList<storage_domains> filteredStorageDomains;
    private HashMap<Guid, VM> alreadyInSystemVmMap;
    private Map<Guid, ArrayList<Quota>> storageQuotaMap;
    private final Map<Guid, List<Disk>> templateDiskMap = new HashMap<Guid, List<Disk>>();
    private final Map<Guid, ImportData> diskImportDataMap = new HashMap<Guid, ImportData>();

    private EntityModel cloneAll;

    public EntityModel getCloneAll() {
        return cloneAll;
    }

    public void setCloneAll(EntityModel cloneAll) {
        this.cloneAll = cloneAll;
    }

    public storage_pool getStoragePool() {
        return storagePool;
    }

    public void setStoragePool(storage_pool storagePool) {
        this.storagePool = storagePool;
    }

    private ListModel storage;

    public ListModel getStorage() {
        return storage;
    }

    public void setStorage(ListModel storage) {
        this.storage = storage;
    }

    private ListModel cluster;

    public ListModel getCluster() {
        return cluster;
    }

    private void setCluster(ListModel value) {
        cluster = value;
    }

    private ListModel clusterQuota;

    public ListModel getClusterQuota() {
        return clusterQuota;
    }

    public void setClusterQuota(ListModel clusterQuota) {
        this.clusterQuota = clusterQuota;
    }

    private EntityModel collapseSnapshots;

    public EntityModel getCollapseSnapshots() {
        return collapseSnapshots;
    }

    public void setCollapseSnapshots(EntityModel value) {
        this.collapseSnapshots = value;
    }

    protected List<VM> disksToConvert = new ArrayList<VM>();

    public List<VM> getDisksToConvert() {
        return disksToConvert;
    }

    private UICommand closeCommand;

    public void setCloseCommand(UICommand closeCommand) {
        this.closeCommand = closeCommand;
    }

    public UICommand getCloseCommand() {
        return closeCommand;
    }

    private final IEventListener quotaClusterListener = new IEventListener() {

        @Override
        public void eventRaised(Event ev, Object sender, EventArgs args) {
            Frontend.RunQuery(VdcQueryType.GetAllRelevantQuotasForVdsGroup,
                    new GetAllRelevantQuotasForVdsGroupParameters(((VDSGroup) getCluster().getSelectedItem()).getId()),
                    new AsyncQuery(ImportVmModel.this,
                            new INewAsyncCallback() {

                                @Override
                                public void OnSuccess(Object model, Object returnValue) {
                                    ImportVmModel importVmModel = (ImportVmModel) model;
                                    ArrayList<Quota> quotaList =
                                            (ArrayList<Quota>) ((VdcQueryReturnValue) returnValue).getReturnValue();
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
    };

    @Override
    public void setSelectedItem(Object value) {
        super.setSelectedItem(value);
        OnEntityChanged();
    }

    public ImportVmModel() {
        setCollapseSnapshots(new EntityModel());
        getCollapseSnapshots().setEntity(false);

        setStorage(new ListModel());
        setCluster(new ListModel());
        setClusterQuota(new ListModel());
        getClusterQuota().setIsAvailable(false);
        setCloneAll(new EntityModel());
        getCloneAll().setEntity(false);
    }

    public void init(Guid storageDomainId) {
        // get Storage pool
        AsyncDataProvider.GetDataCentersByStorageDomain(new AsyncQuery(this, new INewAsyncCallback() {

            @Override
            public void OnSuccess(Object model, Object returnValue) {
                ArrayList<storage_pool> pools = (ArrayList<storage_pool>) returnValue;
                storage_pool dataCenter = null;
                if (pools != null && pools.size() == 1) {
                    dataCenter = pools.get(0);
                } else {
                    //TODO internal client ERROR
                }
                setStoragePool(dataCenter);
                // show quota
                if (dataCenter.getQuotaEnforcementType() != QuotaEnforcementTypeEnum.DISABLED) {
                    hasQuota = true;
                }
                if (hasQuota) {
                    getClusterQuota().setIsAvailable(true);
                    getCluster().getSelectedItemChangedEvent().addListener(quotaClusterListener);
                }
                // get cluster
                AsyncDataProvider.GetClusterList(new AsyncQuery(ImportVmModel.this, new INewAsyncCallback() {

                    @Override
                    public void OnSuccess(Object model, Object returnValue) {
                        ArrayList<VDSGroup> clusters = (ArrayList<VDSGroup>) returnValue;
                        getCluster().setItems(clusters);
                        getCluster().setSelectedItem(Linq.FirstOrDefault(clusters));
                        // get storage domains
                        AsyncDataProvider.GetStorageDomainList(new AsyncQuery(ImportVmModel.this,
                                new INewAsyncCallback() {

                                    @Override
                                    public void OnSuccess(Object model, Object returnValue) {
                                        ArrayList<storage_domains> storageDomains =
                                                (ArrayList<storage_domains>) returnValue;
                                        // filter storage domains
                                        filteredStorageDomains =
                                                new ArrayList<storage_domains>();
                                        for (storage_domains domain : storageDomains) {
                                            if (Linq.IsDataActiveStorageDomain(domain)) {
                                                filteredStorageDomains.add(domain);
                                            }
                                        }

                                        getStorage().setItems(filteredStorageDomains);
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
                        dataCenter.getId());
            }
        }),
                storageDomainId);
    }

    private void initQuotaForStorageDomains() {
        ArrayList<VdcQueryType> queryTypeList = new ArrayList<VdcQueryType>();
        ArrayList<VdcQueryParametersBase> queryParamsList =
                new ArrayList<VdcQueryParametersBase>();
        for (storage_domains storage : filteredStorageDomains) {
            queryTypeList.add(VdcQueryType.GetAllRelevantQuotasForStorage);
            queryParamsList.add(new GetAllRelevantQuotasForStorageParameters(storage.getId()));
        }
        storageQuotaMap = new HashMap<Guid, ArrayList<Quota>>();
        Frontend.RunMultipleQueries(queryTypeList,
                queryParamsList,
                new IFrontendMultipleQueryAsyncCallback() {
                    @Override
                    public void Executed(FrontendMultipleQueryAsyncResult result) {
                        List<VdcQueryReturnValue> returnValueList =
                                result.getReturnValues();
                        boolean noQuota = true;
                        for (int i = 0; i < filteredStorageDomains.size(); i++) {
                            ArrayList<Quota> quotaList = (ArrayList<Quota>) returnValueList.get(i)
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
        storage_domains defaultStorage = (storage_domains) getStorage().getSelectedItem();
        for (Guid diskGuid : diskImportDataMap.keySet()) {
            ImportData importData = diskImportDataMap.get(diskGuid);
            storage_domains selectedStorage = importData.getSelectedStorageDomain();

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
            VM vm = (VM) item;
            if (vm.getDiskMap() != null) {
                for (Map.Entry<Guid, Disk> pair : vm.getDiskMap().entrySet()) {
                    DiskImage disk = (DiskImage) pair.getValue();
                    if (disk.getvolume_type() == VolumeType.Sparse
                            && disk.getvolume_format() == VolumeFormat.RAW
                            && getDiskImportData(disk.getId()) != null
                            && (getDiskImportData(disk.getId()).getSelectedStorageDomain()
                                    .getstorage_type().isBlockDomain())) {
                        getDisksToConvert().add(vm);
                        break;
                    }
                }
            }
        }
        if (getDisksToConvert().size() > 0) {
            // Some items are problematic.
            getCollapseSnapshots().setEntity(true);
            getCollapseSnapshots().setIsChangable(false);
            setMessage(ConstantsManager.getInstance()
                    .getConstants()
                    .useSeparateImportOperationForMarkedVMsMsg());
            OnPropertyChanged(new PropertyChangedEventArgs(ON_DISK_LOAD));
        }
    }

    protected void initDisksStorageDomainsList() {
        for (Object item : getItems()) {
            VM vm = (VM) item;
            if (!NGuid.Empty.equals(vm.getVmtGuid())) {
                if (!templateDiskMap.containsKey(vm.getVmtGuid())) {
                    templateDiskMap.put(vm.getVmtGuid(), new ArrayList<Disk>());
                }
                templateDiskMap.get(vm.getVmtGuid()).addAll(vm.getDiskMap().values());
            } else {
                for (Disk disk : vm.getDiskMap().values()) {
                    DiskImage diskImage = (DiskImage) disk;
                    setDiskImportData(diskImage.getId(),
                            filteredStorageDomains, diskImage.getvolume_type());
                }
            }
        }
        if (!templateDiskMap.isEmpty()) {
            ArrayList<VdcQueryType> queryTypeList = new ArrayList<VdcQueryType>();
            ArrayList<VdcQueryParametersBase> queryParamsList = new ArrayList<VdcQueryParametersBase>();
            for (Guid templateId : templateDiskMap.keySet()) {
                queryTypeList.add(VdcQueryType.GetVmTemplatesDisks);
                queryParamsList.add(new GetVmTemplatesDisksParameters(templateId));
            }
            Frontend.RunMultipleQueries(queryTypeList, queryParamsList, new IFrontendMultipleQueryAsyncCallback() {

                @Override
                public void Executed(FrontendMultipleQueryAsyncResult result) {
                    List<VdcQueryReturnValue> returnValueList = result.getReturnValues();
                    Map<Guid, ArrayList<storage_domains>> templateDisksStorageDomains =
                            new HashMap<Guid, ArrayList<storage_domains>>();
                    for (int i = 0; i < returnValueList.size(); i++) {
                        Guid templateGuid = ((GetVmTemplatesDisksParameters) result.getParameters().get(i)).getId();
                        ArrayList<DiskImage> disks = (ArrayList<DiskImage>) returnValueList.get(i).getReturnValue();
                        for (DiskImage diskImage : disks) {
                            templateDisksStorageDomains.put(diskImage.getImageId(),
                                    getStorageDomainsByIds(diskImage.getstorage_ids()));
                        }
                    }
                    for (Guid templateId : templateDiskMap.keySet()) {
                        for (Disk disk : templateDiskMap.get(templateId)) {
                            DiskImage diskImage = (DiskImage) disk;
                            if (diskImage.getParentId() == null && NGuid.Empty.equals(diskImage.getParentId())) {
                                setDiskImportData(disk.getId(),
                                        filteredStorageDomains, diskImage.getvolume_type());
                            } else {
                                ArrayList<storage_domains> storageDomains =
                                        templateDisksStorageDomains.get(diskImage.getParentId());

                                if (storageDomains == null) {
                                    // Missing template disk
                                    missingTemplateDiskMap.put(templateId, templateDiskMap.get(templateId));
                                } else {
                                    setDiskImportData(disk.getId(), storageDomains, diskImage.getvolume_type());
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

    protected void getTemplatesFromExportDomain() {
        GetAllFromExportDomainQueryParameters tempVar =
                new GetAllFromExportDomainQueryParameters(storagePool.getId(), ((storage_domains) getEntity())
                        .getId());
        tempVar.setGetAll(true);
        Frontend.RunQuery(VdcQueryType.GetTemplatesFromExportDomain, tempVar, new AsyncQuery(ImportVmModel.this,
                new INewAsyncCallback() {

                    @Override
                    public void OnSuccess(Object model, Object returnValue) {
                        Map<VmTemplate, DiskImageList> dictionary =
                                (HashMap<VmTemplate, DiskImageList>) ((VdcQueryReturnValue) returnValue).getReturnValue();
                        Map<Guid, Guid> tempMap = new HashMap<Guid, Guid>();
                        for (Entry<VmTemplate, DiskImageList> entry : dictionary.entrySet()) {
                            tempMap.put(entry.getKey().getId(), null);
                        }
                        for (Guid templateId : missingTemplateDiskMap.keySet()) {
                            if (tempMap.containsKey(templateId)) {
                                for (Disk disk : missingTemplateDiskMap.get(templateId)) {
                                    setDiskImportData(disk.getId(),
                                            filteredStorageDomains, ((DiskImage) disk).getvolume_type());
                                }
                            } else {
                                showCloseMessage(ConstantsManager.getInstance()
                                        .getConstants()
                                        .errorTemplateCannotBeFoundMessage());
                                return;
                            }
                        }
                        ImportVmModel.this.setMessage(ConstantsManager.getInstance()
                                .getConstants()
                                .importMissingStorages());
                        getCollapseSnapshots().setEntity(true);
                        getCollapseSnapshots().setIsChangable(false);
                        postInitDisks();
                    }
                }));

    }

    protected void postInitDisks() {
        onDataLoad();

        checkDestFormatCompatibility();
        StopProgress();
        getStorage().getSelectedItemChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                onDataLoad();
            }
        });
        getCollapseSnapshots().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                OnPropertyChanged(new PropertyChangedEventArgs(ON_DISK_LOAD));
            }
        });
    }

    public void onDataLoad() {
        checkIfDefaultStorageApplicableForAllDisks();
        OnPropertyChanged(new PropertyChangedEventArgs(ON_DISK_LOAD));
    }

    private ArrayList<storage_domains> getStorageDomainsByIds(ArrayList<Guid> getstorage_ids) {
        ArrayList<storage_domains> domains = new ArrayList<storage_domains>();
        for (Guid storageDomainId : getstorage_ids) {
            for (storage_domains storageDomain : filteredStorageDomains) {
                if (storageDomainId.equals(storageDomain.getId())) {
                    domains.add(storageDomain);
                    break;
                }
            }
        }
        return domains;
    }

    public ImportData getDiskImportData(Guid diskId) {
        ImportData importData = diskImportDataMap.get(diskId);
        if (importData != null) {
            if (storage.getSelectedItem() == null) {
                importData.setSelectedStorageDomain((storage_domains) storage.getSelectedItem());
            }
        }
        return importData;
    }

    protected void setDiskImportData(Guid diskId,
            ArrayList<storage_domains> storageDomains,
            VolumeType volumeType) {
        ImportData data = new ImportData();
        diskImportDataMap.put(diskId, data);
        data.setAllStorageDomains(filteredStorageDomains);
        data.setCollapseSnapshot(getCollapseSnapshots());
        data.setStorageDomains(storageDomains);
        data.setVolumeType(volumeType);
        data.setStorageQuotaList(storageQuotaMap);
    }

    @Override
    protected void ActiveDetailModelChanged() {
        super.ActiveDetailModelChanged();
    }

    @Override
    protected void InitDetailModels() {
        super.InitDetailModels();

        importDiskListModel = new VmImportDiskListModel();

        ObservableCollection<EntityModel> list = new ObservableCollection<EntityModel>();
        list.add(new VmGeneralModel());
        list.add(new VmImportInterfaceListModel());
        list.add(importDiskListModel);
        list.add(new VmAppListModel());
        setDetailModels(list);
    }

    public boolean Validate() {
        if (QuotaEnforcementTypeEnum.HARD_ENFORCEMENT.equals(storagePool.getQuotaEnforcementType())) {
            getClusterQuota().ValidateSelectedItem(
                    new IValidation[] { new NotEmptyValidation() });
            for (ImportData item : diskImportDataMap.values()) {
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
        getStorage().ValidateSelectedItem(
                new IValidation[] { new NotEmptyValidation() });
        getCluster().ValidateSelectedItem(
                new IValidation[] { new NotEmptyValidation() });

        return getStorage().getIsValid()
                && getCluster().getIsValid()
                && getClusterQuota().getIsValid();
    }

    @Override
    public void setItems(final Iterable value)
    {
        String vm_guidKey = "_VM_ID ="; //$NON-NLS-1$
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

        Frontend.RunQuery(VdcQueryType.Search,
                new SearchParameters(searchPattern.toString(), SearchType.VM),
                new AsyncQuery(this, new INewAsyncCallback() {

                    @Override
                    public void OnSuccess(Object model, Object returnValue) {
                        List<VM> vmList =
                                (List<VM>) ((VdcQueryReturnValue) returnValue).getReturnValue();
                        alreadyInSystemVmMap = new HashMap<Guid, VM>();
                        for (VM vm : vmList) {
                            alreadyInSystemVmMap.put(vm.getId(), vm);
                        }
                        if (vmList.size() == list.size()) {
                            getCloneAll().setEntity(true);
                            getCloneAll().setIsChangable(false);
                        }
                        ImportVmModel.super.setItems(value);
                    }
                }));

    }

    public void setSuperItems(Iterable value) {
        super.setItems(value);
    }

    @Override
    protected String getListName() {
        return "ImportVmModel"; //$NON-NLS-1$
    }

    public SearchableListModel getImportDiskListModel() {
        return importDiskListModel;
    }

    @Override
    public boolean isObjectInSetup(Object vm) {
        if (alreadyInSystemVmMap == null) {
            return false;
        }
        return alreadyInSystemVmMap.containsKey(((VM) vm).getId());
    }

    public boolean isQuotaEnabled() {
        return getStoragePool() != null
                && getStoragePool().getQuotaEnforcementType() != QuotaEnforcementTypeEnum.DISABLED;
    }

    private void showCloseMessage(String message) {
        setMessage(message);
        getCommands().clear();
        getCommands().add(getCloseCommand());
        StopProgress();
    }
}
