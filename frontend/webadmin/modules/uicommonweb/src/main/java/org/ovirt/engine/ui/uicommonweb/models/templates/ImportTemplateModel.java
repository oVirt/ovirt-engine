package org.ovirt.engine.ui.uicommonweb.models.templates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage_domain_static;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.queries.DiskImageList;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.ObservableCollection;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;

@SuppressWarnings("unused")
public class ImportTemplateModel extends ListWithDetailsModel
{

    private storage_domain_static privateSourceStorage;

    public storage_domain_static getSourceStorage()
    {
        return privateSourceStorage;
    }

    public void setSourceStorage(storage_domain_static value)
    {
        privateSourceStorage = value;
    }

    private storage_pool privateStoragePool;

    public storage_pool getStoragePool()
    {
        return privateStoragePool;
    }

    public void setStoragePool(storage_pool value)
    {
        privateStoragePool = value;
    }

    private ListModel privateDestinationStorage;

    public ListModel getDestinationStorage()
    {
        return privateDestinationStorage;
    }

    private void setDestinationStorage(ListModel value)
    {
        privateDestinationStorage = value;
    }

    private ListModel privateCluster;

    public ListModel getCluster()
    {
        return privateCluster;
    }

    private void setCluster(ListModel value)
    {
        privateCluster = value;
    }

    private String nameAndDescription;
    private TemplateImportDiskListModel templateImportDiskListModel;

    public String getNameAndDescription()
    {
        return nameAndDescription;
    }

    public void setNameAndDescription(String value)
    {
        if (!StringHelper.stringsEqual(nameAndDescription, value))
        {
            nameAndDescription = value;
            OnPropertyChanged(new PropertyChangedEventArgs("NameAndDescription"));
        }
    }

    private EntityModel privateIsSingleDestStorage;

    public EntityModel getIsSingleDestStorage() {
        return privateIsSingleDestStorage;
    }

    public void setIsSingleDestStorage(EntityModel value) {
        privateIsSingleDestStorage = value;
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

    public ImportTemplateModel()
    {
        setDestinationStorage(new ListModel());
        setCluster(new ListModel());
        setDiskStorageMap(new HashMap<Guid, HashMap<Guid, Guid>>());
        setIsSingleDestStorage(new EntityModel());
        getIsSingleDestStorage().setEntity(true);
    }

    @Override
    protected void InitDetailModels()
    {
        super.InitDetailModels();

        ObservableCollection<EntityModel> list = new ObservableCollection<EntityModel>();
        list.add(new TemplateGeneralModel());
        list.add(new TemplateImportInterfaceListModel());
        this.templateImportDiskListModel = new TemplateImportDiskListModel();
        list.add(templateImportDiskListModel);
        setDetailModels(list);
    }

    public boolean Validate()
    {
        getDestinationStorage().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });

        getCluster().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });

        return getDestinationStorage().getIsValid() && getCluster().getIsValid();
    }

    public void setItems(Iterable value)
    {
        super.setItems(value);

        for (Object vmTemplate : getItems()) {
            getDiskStorageMap().put(((VmTemplate) vmTemplate).getId(), new HashMap<Guid, Guid>());
        }

        initDiskStorageMap();
    }

    private void initDiskStorageMap() {
        ArrayList<storage_domains> allDestStorages = (ArrayList<storage_domains>) getDestinationStorage().getItems();
        for (Object item : getItems()) {
            VmTemplate vmTemplate = (VmTemplate) item;
            for (DiskImage disk : vmTemplate.getDiskList()) {
                Guid storageId = !allDestStorages.isEmpty() ? allDestStorages.get(0).getId() : new Guid();
                addToDiskStorageMap(vmTemplate.getId(), disk, storageId);
            }
        }
    }

    @Override
    protected void OnSelectedItemChanged()
    {
        super.OnSelectedItemChanged();
    }

    @Override
    protected String getListName() {
        return "ImportTemplateModel";
    }

    public void setExtendedItems(ArrayList<Entry<VmTemplate, DiskImageList>> arrayList) {
        templateImportDiskListModel.setExtendedItems(arrayList);
    }

    public void DestinationStorage_SelectedItemChanged(DiskImage disk, String storageDomainName) {
        VmTemplate item = (VmTemplate) getSelectedItem();
        addToDiskStorageMap(item.getId(), disk, getStorageDomainByName(storageDomainName).getId());
    }

    public void addToDiskStorageMap(Guid vmId, DiskImage disk, Guid storageId) {
        HashMap<Guid, Guid> vmDiskStorageMap = getDiskStorageMap().get(vmId);
        vmDiskStorageMap.put(disk.getImageId(), storageId);
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
}
