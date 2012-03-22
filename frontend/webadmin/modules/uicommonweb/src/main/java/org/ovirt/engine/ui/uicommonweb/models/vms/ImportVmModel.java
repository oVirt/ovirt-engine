package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.businessentities.storage_domain_static;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.ObservableCollection;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;

@SuppressWarnings("unused")
public class ImportVmModel extends ListWithDetailsModel {
    boolean sameSelectedDestinationStorage = false;
    ArrayList<storage_domains> uniqueDestStorages;
    ArrayList<storage_domains> allDestStorages;
    storage_domains selectedDestinationStorage;
    HashMap<Guid, ArrayList<Guid>> templateGuidUniqueStorageDomainDic;
    HashMap<Guid, ArrayList<Guid>> templateGuidAllStorageDomainDic;
    HashMap<Guid, ArrayList<DiskImage>> templateGuidDiskImagesDic;
    VmImportDiskListModel importDiskListModel;
    ArrayList<storage_domains> allStorageDomains;

    int uniqueDomains;
    Guid templateGuid;

    private storage_domain_static privateSourceStorage;

    public storage_domain_static getSourceStorage() {
        return privateSourceStorage;
    }

    public void setSourceStorage(storage_domain_static value) {
        privateSourceStorage = value;
    }

    private storage_pool privateStoragePool;

    public storage_pool getStoragePool() {
        return privateStoragePool;
    }

    public void setStoragePool(storage_pool value) {
        privateStoragePool = value;
    }

    private ListModel privateDestinationStorage;

    public ListModel getDestinationStorage() {
        return privateDestinationStorage;
    }

    private void setDestinationStorage(ListModel value) {
        privateDestinationStorage = value;
    }

    private ListModel privateAllDestinationStorage;

    public ListModel getAllDestinationStorage() {
        return privateAllDestinationStorage;
    }

    private void setAllDestinationStorage(ListModel value) {
        privateAllDestinationStorage = value;
    }

    private ListModel privateCluster;

    public ListModel getCluster() {
        return privateCluster;
    }

    private void setCluster(ListModel value) {
        privateCluster = value;
    }

    private ListModel privateSystemDiskFormat;

    public ListModel getSystemDiskFormat() {
        return privateSystemDiskFormat;
    }

    private void setSystemDiskFormat(ListModel value) {
        privateSystemDiskFormat = value;
    }

    private ListModel privateDataDiskFormat;

    public ListModel getDataDiskFormat() {
        return privateDataDiskFormat;
    }

    private void setDataDiskFormat(ListModel value) {
        privateDataDiskFormat = value;
    }

    private EntityModel collapseSnapshots;

    public EntityModel getCollapseSnapshots() {
        return collapseSnapshots;
    }

    public void setCollapseSnapshots(EntityModel value) {
        this.collapseSnapshots = value;
    }

    private boolean isMissingStorages;

    public boolean getIsMissingStorages() {
        return isMissingStorages;
    }

    public void setIsMissingStorages(boolean value) {
        if (isMissingStorages != value) {
            isMissingStorages = value;
            OnPropertyChanged(new PropertyChangedEventArgs("IsMissingStorages"));
        }
    }

    private String nameAndDescription;
    private AsyncQuery onCollapseSnapshotsChangedFinish;

    public String getNameAndDescription() {
        return nameAndDescription;
    }

    public void setNameAndDescription(String value) {
        if (!StringHelper.stringsEqual(nameAndDescription, value)) {
            nameAndDescription = value;
            OnPropertyChanged(new PropertyChangedEventArgs("NameAndDescription"));
        }
    }

    private java.util.List<VM> problematicItems;

    public java.util.List<VM> getProblematicItems() {
        return problematicItems;
    }

    public void setProblematicItems(java.util.List<VM> value) {
        if (problematicItems != value) {
            problematicItems = value;
            OnPropertyChanged(new PropertyChangedEventArgs("ProblematicItems"));
        }
    }

    private EntityModel privateIsSingleDestStorage;

    public EntityModel getIsSingleDestStorage() {
        return privateIsSingleDestStorage;
    }

    public void setIsSingleDestStorage(EntityModel value) {
        privateIsSingleDestStorage = value;
    }

    private ListModel privateStorageDoamins;

    public ListModel getStorageDoamins() {
        return privateStorageDoamins;
    }

    private void setStorageDoamins(ListModel value) {
        privateStorageDoamins = value;
    }

    private HashMap<Guid, HashMap<Guid, Guid>> privateDiskStorageMap;

    public HashMap<Guid, HashMap<Guid, Guid>> getDiskStorageMap()
    {
        return privateDiskStorageMap;
    }

    public void setDiskStorageMap(HashMap<Guid, HashMap<Guid, Guid>> value)
    {
        privateDiskStorageMap = value;
    }

    @Override
    public void setSelectedItem(Object value) {
        super.setSelectedItem(value);
        OnEntityChanged();
    }

    public ImportVmModel() {
        setProblematicItems(new ArrayList<VM>());
        setCollapseSnapshots(new EntityModel());
        getCollapseSnapshots().setEntity(false);
        getCollapseSnapshots().getPropertyChangedEvent().addListener(
                new IEventListener() {

                    @Override
                    public void eventRaised(Event ev, Object sender,
                            EventArgs args) {
                        OnCollapseSnapshotsChanged();
                    }
                });

        setDestinationStorage(new ListModel());
        getDestinationStorage().getSelectedItemChangedEvent().addListener(
                new IEventListener() {
                    @Override
                    public void eventRaised(Event ev, Object sender,
                            EventArgs args) {
                        DestinationStorage_SelectedItemChanged();
                    }
                });
        setCluster(new ListModel());
        setSystemDiskFormat(new ListModel());
        setDataDiskFormat(new ListModel());
        setDiskStorageMap(new HashMap<Guid, HashMap<Guid, Guid>>());
        setIsSingleDestStorage(new EntityModel());
        getIsSingleDestStorage().setEntity(true);
        setAllDestinationStorage(new ListModel());
    }

    public void OnCollapseSnapshotsChanged(AsyncQuery _asyncQuery) {
        this.onCollapseSnapshotsChangedFinish = _asyncQuery;
        OnCollapseSnapshotsChanged();
    }

    public void initStorageDomains() {
        templateGuidUniqueStorageDomainDic = new java.util.HashMap<Guid, ArrayList<Guid>>();
        templateGuidAllStorageDomainDic = new java.util.HashMap<Guid, ArrayList<Guid>>();
        templateGuidDiskImagesDic = new java.util.HashMap<Guid, ArrayList<DiskImage>>();

        uniqueDomains = 0;
        for (Object item : getItems()) {
            VM vm = (VM) item;
            templateGuid = vm.getvmt_guid();

            if (templateGuid.equals(NGuid.Empty)) {
                uniqueDomains++;
                templateGuidUniqueStorageDomainDic.put(templateGuid, null);
                templateGuidAllStorageDomainDic.put(templateGuid, null);
            } else {
                AsyncDataProvider.GetTemplateDiskList(new AsyncQuery(this,
                        new INewAsyncCallback() {
                            @Override
                            public void OnSuccess(Object target, Object returnValue) {
                                ImportVmModel importVmModel = (ImportVmModel) target;
                                ArrayList<DiskImage> disks = (ArrayList<DiskImage>) returnValue;

                                ArrayList<ArrayList<Guid>> allSourceStorages = new ArrayList<ArrayList<Guid>>();

                                for (DiskImage disk : disks) {
                                    allSourceStorages.add(disk.getstorage_ids());
                                }

                                ArrayList<Guid> intersectStorageDomains = Linq.Intersection(allSourceStorages);
                                ArrayList<Guid> unionStorageDomains = Linq.Union(allSourceStorages);

                                uniqueDomains++;
                                templateGuidUniqueStorageDomainDic.put(importVmModel.templateGuid,
                                        intersectStorageDomains);
                                templateGuidAllStorageDomainDic.put(importVmModel.templateGuid,
                                        unionStorageDomains);
                                templateGuidDiskImagesDic.put(importVmModel.templateGuid, disks);

                                importVmModel.postInitStorageDomains();
                            }
                        }),
                        templateGuid);
            }
        }
        postInitStorageDomains();
    }

    protected void postInitStorageDomains() {
        if (templateGuidUniqueStorageDomainDic.size() != uniqueDomains) {
            return;
        }

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.Model = this;
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object returnValue) {
                allStorageDomains = (ArrayList<storage_domains>) returnValue;
                OnCollapseSnapshotsChanged();
                initDiskStorageMap();
            }
        };
        AsyncDataProvider.GetDataDomainsListByDomain(_asyncQuery, this.getSourceStorage().getId());
    }

    private void initDiskStorageMap() {
        for (Object item : getItems()) {
            VM vm = (VM) item;
            for (DiskImage disk : vm.getDiskMap().values()) {
                if (NGuid.Empty.equals(vm.getvmt_guid())) {
                    Guid storageId = !allDestStorages.isEmpty() ?
                            allDestStorages.get(0).getId() : new Guid();
                    addToDiskStorageMap(vm.getId(), disk, storageId);
                }
                else {
                    ArrayList<Guid> storageIds =
                            templateGuidUniqueStorageDomainDic.get(vm.getvmt_guid());
                    Guid storageId = storageIds != null ?
                            templateGuidUniqueStorageDomainDic.get(vm.getvmt_guid()).get(0) : new Guid();
                    addToDiskStorageMap(vm.getId(), disk, storageId);
                }
            }
        }
    }

    public void OnCollapseSnapshotsChanged() {
        if (this.getItems() == null || allStorageDomains == null) {
            return;
        }
        selectedDestinationStorage = null;
        sameSelectedDestinationStorage = false;
        uniqueDestStorages = new ArrayList<storage_domains>();
        allDestStorages = new ArrayList<storage_domains>();
        setIsMissingStorages(false);

        if (getDestinationStorage().getSelectedItem() != null) {
            selectedDestinationStorage = (storage_domains) getDestinationStorage().getSelectedItem();
        }

        for (storage_domains domain : allStorageDomains) {
            boolean addStorage = false;

            if (Linq.IsDataActiveStorageDomain(domain)) {
                allDestStorages.add(domain);

                if (((Boolean) getCollapseSnapshots().getEntity()).equals(true)) {
                    addStorage = true;
                }
                else {
                    for (Map.Entry<Guid, ArrayList<Guid>> keyValuePair : templateGuidUniqueStorageDomainDic.entrySet())
                    {
                        if (NGuid.Empty.equals(keyValuePair.getKey())) {
                            addStorage = true;
                        } else {
                            addStorage = false;
                            for (Guid storageDomainId : keyValuePair.getValue()) {
                                if (storageDomainId.equals(domain.getId()))
                                {
                                    addStorage = true;
                                    break;
                                }
                            }
                        }
                        if (addStorage == false) {
                            break;
                        }
                    }
                }
            }
            else {
                for (Map.Entry<Guid, ArrayList<Guid>> keyValuePair : templateGuidAllStorageDomainDic.entrySet())
                {
                    if (!NGuid.Empty.equals(keyValuePair.getKey())) {
                        for (Guid storageDomainId : keyValuePair.getValue()) {
                            if (storageDomainId.equals(domain.getId()))
                            {
                                setIsMissingStorages(true);
                                break;
                            }
                        }
                    }
                }
            }

            if (addStorage) {
                uniqueDestStorages.add(domain);
                if (sameSelectedDestinationStorage == false && domain.equals(selectedDestinationStorage)) {
                    sameSelectedDestinationStorage = true;
                    selectedDestinationStorage = domain;
                }
            }
        }

        getAllDestinationStorage().setItems(allDestStorages);
        getDestinationStorage().setItems(uniqueDestStorages);
        if (sameSelectedDestinationStorage) {
            getDestinationStorage().setSelectedItem(selectedDestinationStorage);
        } else {
            getDestinationStorage().setSelectedItem(Linq.FirstOrDefault(uniqueDestStorages));
        }

        if (getDetailModels() != null
                && getActiveDetailModel() instanceof VmImportDiskListModel) {
            VmImportDiskListModel detailModel = (VmImportDiskListModel) getActiveDetailModel();
            detailModel
                    .setCollapseSnapshots((Boolean) getCollapseSnapshots()
                            .getEntity());
        }
        if (onCollapseSnapshotsChangedFinish != null) {
            onCollapseSnapshotsChangedFinish.asyncCallback.OnSuccess(
                    onCollapseSnapshotsChangedFinish.getModel(), null);
            onCollapseSnapshotsChangedFinish = null;
        }
    }

    @Override
    protected void ActiveDetailModelChanged() {
        super.ActiveDetailModelChanged();
        OnCollapseSnapshotsChanged();
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
        getDestinationStorage().ValidateSelectedItem(
                new IValidation[] { new NotEmptyValidation() });
        getCluster().ValidateSelectedItem(
                new IValidation[] { new NotEmptyValidation() });

        return getDestinationStorage().getIsValid()
                && getCluster().getIsValid();
    }

    @Override
    protected void OnSelectedItemChanged() {
        super.OnSelectedItemChanged();

        if (getSelectedItem() != null) {
            VM vm = (VM) getSelectedItem();
            setNameAndDescription(StringFormat.format("%1$s%2$s",
                    vm.getvm_name(),
                    !StringHelper.isNullOrEmpty(vm.getvm_description()) ? " ["
                            + vm.getvm_description() + "]" : ""));
        } else {
            setNameAndDescription("");
        }

    }

    public void setItems(Iterable value)
    {
        super.setItems(value);

        for (Object vm : getItems()) {
            getDiskStorageMap().put(((VM) vm).getId(), new HashMap<Guid, Guid>());
        }

        initStorageDomains();
    }

    @Override
    protected String getListName() {
        return "ImportVmModel";
    }

    public void setSelectedVMsCount(int size) {
        importDiskListModel.setSelectedVMsCount(((List) getItems()).size());
    }

    storage_domains currStorageDomain = null;

    private void DestinationStorage_SelectedItemChanged() {
        storage_domains selectedStorageDomain = (storage_domains) getDestinationStorage().getSelectedItem();
        List destinationStorageDomains = ((List) getDestinationStorage().getItems());
        if (selectedStorageDomain == null && !destinationStorageDomains.isEmpty()) {
            selectedStorageDomain = (storage_domains) destinationStorageDomains.get(0);
        }
        if (currStorageDomain == null || selectedStorageDomain == null
                || !currStorageDomain.getQueryableId().equals(selectedStorageDomain.getQueryableId())) {
            currStorageDomain = selectedStorageDomain;
            UpdateImportWarnings();
        }
    }

    public void DestinationStorage_SelectedItemChanged(DiskImage disk, String storageDomainName) {
        VM item = (VM) getSelectedItem();
        addToDiskStorageMap(item.getId(), disk, getStorageDomainByName(storageDomainName).getId());
    }

    public void addToDiskStorageMap(Guid vmId, DiskImage disk, Guid storageId) {
        HashMap<Guid, Guid> vmDiskStorageMap = getDiskStorageMap().get(vmId);
        vmDiskStorageMap.put(disk.getId(), storageId);
    }

    private storage_domains getStorageDomainByName(String storageDomainName) {
        storage_domains storage = null;
        for (Object storageDomain : getDestinationStorage().getItems()) {
            storage = (storage_domains) storageDomain;
            if (storageDomainName.equals(storage.getstorage_name())) {
                break;
            }
        }
        return storage;
    }

    @Override
    protected void ItemsChanged() {
        super.ItemsChanged();
        UpdateImportWarnings();
    }

    public VmImportDiskListModel getImportDiskListModel() {
        return importDiskListModel;
    }

    private void UpdateImportWarnings() {
        // Clear problematic state.
        getProblematicItems().clear();

        if (getItems() == null) {
            return;
        }

        storage_domains destinationStorage = (storage_domains) getDestinationStorage()
                .getSelectedItem();
        for (Object item : getItems()) {
            VM vm = (VM) item;

            if (vm.getDiskMap() != null) {
                for (java.util.Map.Entry<String, DiskImage> pair : vm
                        .getDiskMap().entrySet()) {
                    DiskImage disk = pair.getValue();

                    if (disk.getvolume_type() == VolumeType.Sparse
                            && disk.getvolume_format() == VolumeFormat.RAW
                            && destinationStorage != null
                            && (destinationStorage.getstorage_type() == StorageType.ISCSI || destinationStorage
                                    .getstorage_type() == StorageType.FCP)) {
                        getProblematicItems().add(vm);
                    }
                }
            }
        }

        // Decide what to do with the CollapseSnapshots option.
        if (problematicItems.size() > 0) {
            if (problematicItems.size() == Linq.Count(getItems())) {
                // All items are problematic.
                getCollapseSnapshots().setIsChangable(false);
                getCollapseSnapshots().setEntity(true);
                getCollapseSnapshots()
                        .setMessage(
                                "Note that all snapshots will be collapsed due to different storage types");
            } else {
                // Some items are problematic.
                getCollapseSnapshots()
                        .setMessage(
                                "Use a separate import operation for the marked VMs or\nApply \"Collapse Snapshots\" for all VMs");
            }
        } else {
            // No problematic items.
            getCollapseSnapshots().setIsChangable(true);
            getCollapseSnapshots().setMessage(null);
        }
    }

    public void VolumeType_SelectedItemChanged(DiskImage disk,
            VolumeType tempVolumeType) {
        for (Object item : getItems()) {
            VM vm = (VM) item;
            java.util.HashMap<String, DiskImageBase> diskDictionary = new java.util.HashMap<String, DiskImageBase>();

            for (java.util.Map.Entry<String, DiskImage> a : vm.getDiskMap()
                    .entrySet()) {
                if (a.getValue().getQueryableId().equals(disk.getQueryableId())) {
                    a.getValue().setvolume_type(tempVolumeType);
                    break;
                }
            }
        }
    }

    public ArrayList<String> getAvailableStorageDomainsByDiskId(Guid diskId) {
        ArrayList<String> storageDomains = null;
        ArrayList<Guid> storageDomainsIds = getImportDiskListModel().getAvailableStorageDomainsByDiskId(diskId);

        if (storageDomainsIds != null) {
            storageDomains = new ArrayList<String>();
            for (Guid storageId : storageDomainsIds) {
                if (Linq.IsActiveStorageDomain(getStorageById(storageId))) {
                    storageDomains.add(getStorageNameById(storageId));
                }
            }
        }

        if (storageDomains != null) {
            Collections.sort(storageDomains);
        }

        return storageDomains;
    }

    public String getStorageNameById(NGuid storageId) {
        String storageName = "";
        for (Object storageDomain : getAllDestinationStorage().getItems()) {
            storage_domains storage = (storage_domains) storageDomain;
            if (storage.getId().equals(storageId)) {
                storageName = storage.getstorage_name();
            }
        }
        return storageName;
    }

    public storage_domains getStorageById(Guid storageId) {
        for (storage_domains storage : allDestStorages) {
            if (storage.getId().equals(storageId)) {
                return storage;
            }
        }
        return null;
    }
}
