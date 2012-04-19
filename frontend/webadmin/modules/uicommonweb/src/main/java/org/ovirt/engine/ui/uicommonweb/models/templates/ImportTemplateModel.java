package org.ovirt.engine.ui.uicommonweb.models.templates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage_domain_static;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.DiskImageList;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.ObservableCollection;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.IIsObjectInSetup;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;

@SuppressWarnings("unused")
public class ImportTemplateModel extends ListWithDetailsModel implements IIsObjectInSetup
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
            OnPropertyChanged(new PropertyChangedEventArgs("NameAndDescription")); //$NON-NLS-1$
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

    private HashMap<Guid, VmTemplate> alreadyInSystem;

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

    @Override
    public void setItems(final Iterable value)
    {
        String vmt_guidKey = "_VMT_ID =";
        String orKey = " or ";
        StringBuilder searchPattern = new StringBuilder();
        searchPattern.append("Template: ");

        List<VmTemplate> list = (List<VmTemplate>) value;
        for (int i = 0; i < list.size(); i++) {
            VmTemplate vmTemplate = list.get(i);

            searchPattern.append(vmt_guidKey);
            searchPattern.append(vmTemplate.getId().toString());
            if (i < list.size() - 1) {
                searchPattern.append(orKey);
            }
        }

        Frontend.RunQuery(VdcQueryType.Search,
                new SearchParameters(searchPattern.toString(), SearchType.VmTemplate),
                new AsyncQuery(this, new INewAsyncCallback() {

                    @Override
                    public void OnSuccess(Object model, Object returnValue) {
                        List<VmTemplate> vmtList =
                                (List<VmTemplate>) ((VdcQueryReturnValue) returnValue).getReturnValue();
                        alreadyInSystem = new HashMap<Guid, VmTemplate>();
                        for (VmTemplate vmt : vmtList) {
                            alreadyInSystem.put(vmt.getId(), vmt);
                        }

                        setSuperItems(value);
                    }
                }));

    }

    protected void setSuperItems(Iterable value) {
        super.setItems(value);
        List<VmTemplate> list = (List<VmTemplate>) getItems();
        for (VmTemplate vmTemplate : list) {
            getDiskStorageMap().put(vmTemplate.getId(), new HashMap<Guid, Guid>());
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
        return "ImportTemplateModel"; //$NON-NLS-1$
    }

    public void setExtendedItems(ArrayList<Map.Entry<VmTemplate, DiskImageList>> arrayList) {
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

    @Override
    public boolean isObjectInSetup(Object vmTemplate) {
        if (alreadyInSystem == null) {
            return false;
        }
        return alreadyInSystem.containsKey(((VmTemplate) vmTemplate).getId());
    }
}
