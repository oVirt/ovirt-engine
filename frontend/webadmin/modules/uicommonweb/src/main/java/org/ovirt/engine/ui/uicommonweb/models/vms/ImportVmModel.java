package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
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
import org.ovirt.engine.ui.uicommonweb.DataProvider;
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
    java.util.ArrayList<storage_domains> destStorages;
    storage_domains selectedDestinationStorage;
    java.util.HashMap<Guid, java.util.ArrayList<storage_domains>> templateGuidStorageDomainDic;
    VmImportDiskListModel importDiskListModel;
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
    }

    public void OnCollapseSnapshotsChanged(AsyncQuery _asyncQuery) {
        this.onCollapseSnapshotsChangedFinish = _asyncQuery;
        OnCollapseSnapshotsChanged();
    }

    public void OnCollapseSnapshotsChanged() {
        if (this.getItems() == null) {
            return;
        }
        selectedDestinationStorage = null;
        sameSelectedDestinationStorage = false;
        if (getDestinationStorage().getSelectedItem() != null) {
            selectedDestinationStorage = (storage_domains) getDestinationStorage()
                    .getSelectedItem();
        }
        destStorages = new java.util.ArrayList<storage_domains>();
        templateGuidStorageDomainDic = new java.util.HashMap<Guid, java.util.ArrayList<storage_domains>>();
        for (Object item : getItems()) {
            VM vm = (VM) item;
            Guid Guid = vm.getvmt_guid();
            if (templateGuidStorageDomainDic.containsKey(Guid)) {
                continue;
            }
            if (Guid.equals(NGuid.Empty)) {
                templateGuidStorageDomainDic.put(Guid, null);
            } else {
                templateGuidStorageDomainDic.put(Guid,
                        DataProvider.GetStorageDomainListByTemplate(Guid));
            }
        }
        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.Model = this;
        _asyncQuery.asyncCallback = new INewAsyncCallback() {

            @Override
            public void OnSuccess(Object model, Object returnValue) {
                ArrayList<storage_domains> list = (ArrayList<storage_domains>) returnValue;
                for (storage_domains domain : list) {
                    boolean addStorage = false;
                    if ((domain.getstorage_domain_type() == StorageDomainType.Data || domain
                            .getstorage_domain_type() == StorageDomainType.Master)
                            && domain.getstatus() != null
                            && domain.getstatus() == StorageDomainStatus.Active) {
                        for (java.util.Map.Entry<Guid, java.util.ArrayList<storage_domains>> keyValuePair : templateGuidStorageDomainDic
                                .entrySet()) {
                            if (NGuid.Empty.equals(keyValuePair.getKey())) {
                                addStorage = true;
                            } else {
                                addStorage = false;
                                for (storage_domains storageDomain : keyValuePair
                                        .getValue()) {
                                    if (storageDomain.getid().equals(
                                            domain.getid())
                                            || ((Boolean) getCollapseSnapshots()
                                                    .getEntity()).equals(true)) {
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
                    if (addStorage) {
                        destStorages.add(domain);
                        if (sameSelectedDestinationStorage == false
                                && domain.equals(selectedDestinationStorage)) {
                            sameSelectedDestinationStorage = true;
                            selectedDestinationStorage = domain;
                        }
                    }
                }
                getDestinationStorage().setItems(destStorages);
                if (sameSelectedDestinationStorage) {
                    getDestinationStorage().setSelectedItem(
                            selectedDestinationStorage);
                } else {
                    getDestinationStorage().setSelectedItem(
                            Linq.FirstOrDefault(destStorages));
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
        };
        AsyncDataProvider.GetDataDomainsListByDomain(_asyncQuery, this
                .getSourceStorage().getId());
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

    @Override
    protected String getListName() {
        return "ImportVmModel";
    }

    public void setSelectedVMsCount(int size) {
        importDiskListModel.setSelectedVMsCount(((java.util.List) getItems())
                .size());
    }

    storage_domains currStorageDomain = null;

    private void DestinationStorage_SelectedItemChanged() {
        storage_domains selectedStorageDomain = (storage_domains) getDestinationStorage().getSelectedItem();
        if (selectedStorageDomain == null) {
            selectedStorageDomain = (storage_domains) ((List) getDestinationStorage().getItems()).get(0);
        }
        if (currStorageDomain == null
                || !currStorageDomain.getQueryableId().equals(selectedStorageDomain.getQueryableId())) {
            currStorageDomain = selectedStorageDomain;
            UpdateImportWarnings();
        }
    }

    @Override
    protected void ItemsChanged() {
        super.ItemsChanged();
        UpdateImportWarnings();
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

            // vm.DiskMap.Each(a => prm.DiskInfoList.Add(a.getKey(), a.Value));
            for (java.util.Map.Entry<String, DiskImage> a : vm.getDiskMap()
                    .entrySet()) {
                if (a.getValue().getQueryableId().equals(disk.getQueryableId())) {
                    a.getValue().setvolume_type(tempVolumeType);
                    break;
                }
            }
        }
    }
}
