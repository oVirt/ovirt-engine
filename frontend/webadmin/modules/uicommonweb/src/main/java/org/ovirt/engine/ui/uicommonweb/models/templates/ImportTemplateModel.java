package org.ovirt.engine.ui.uicommonweb.models.templates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.IEventListener;
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
import org.ovirt.engine.ui.uicommonweb.validation.RegexValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

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

    private EntityModel cloneAllTemplates;
    private EntityModel cloneAllTemplates_message;
    private EntityModel cloneOnlyDuplicateTemplates;
    private EntityModel cloneTemplatesSuffix;

    public EntityModel getCloneAllTemplates() {
        return cloneAllTemplates;
    }

    public void setCloneAllTemplates(EntityModel cloneAllTemplates) {
        this.cloneAllTemplates = cloneAllTemplates;
    }

    public EntityModel getCloneAllTemplates_message() {
        return cloneAllTemplates_message;
    }

    public void setCloneAllTemplates_message(EntityModel cloneAllTemplates_message) {
        this.cloneAllTemplates_message = cloneAllTemplates_message;
    }

    public EntityModel getCloneOnlyDuplicateTemplates() {
        return cloneOnlyDuplicateTemplates;
    }

    public void setCloneOnlyDuplicateTemplates(EntityModel cloneOnlyDuplicateTemplates) {
        this.cloneOnlyDuplicateTemplates = cloneOnlyDuplicateTemplates;
    }

    public EntityModel getCloneTemplatesSuffix() {
        return cloneTemplatesSuffix;
    }

    public void setCloneTemplatesSuffix(EntityModel cloneTemplatesSuffix) {
        this.cloneTemplatesSuffix = cloneTemplatesSuffix;
    }

    public ImportTemplateModel()
    {
        setDestinationStorage(new ListModel());
        setCluster(new ListModel());
        setDiskStorageMap(new HashMap<Guid, HashMap<Guid, Guid>>());
        setIsSingleDestStorage(new EntityModel());
        getIsSingleDestStorage().setEntity(true);

        setCloneAllTemplates(new EntityModel());
        getCloneAllTemplates().setEntity(false);
        getCloneAllTemplates().getEntityChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if ((Boolean) getCloneAllTemplates().getEntity()) {
                    getCloneTemplatesSuffix().setIsAvailable(true);
                } else if (!(Boolean) getCloneOnlyDuplicateTemplates().getEntity()) {
                    getCloneTemplatesSuffix().setIsAvailable(false);
                }
            }
        });
        setCloneOnlyDuplicateTemplates(new EntityModel());
        getCloneOnlyDuplicateTemplates().setEntity(false);
        getCloneOnlyDuplicateTemplates().setIsAvailable(false);
        setCloneTemplatesSuffix(new EntityModel());
        getCloneTemplatesSuffix().setEntity("_Copy"); //$NON-NLS-1$
        getCloneTemplatesSuffix().setIsAvailable(false);
        setCloneAllTemplates_message(new EntityModel());
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

        getCloneTemplatesSuffix().setIsValid(true);
        if (getCloneTemplatesSuffix().getIsAvailable()) {
            getCloneTemplatesSuffix().ValidateEntity(new IValidation[] { new NotEmptyValidation() });
            if (!getCloneTemplatesSuffix().getIsValid()) {
                return false;
            }
            List<VmTemplate> list = (List<VmTemplate>) getItems();
            for (VmTemplate template : list) {
                String newTemplateName = template.getname() + getCloneTemplatesSuffix().getEntity();

                String nameExpr;
                String nameMsg;
                nameExpr = "^[0-9a-zA-Z-_]{1," + 49 + "}$"; //$NON-NLS-1$ //$NON-NLS-2$
                nameMsg =
                        ConstantsManager.getInstance()
                                .getMessages()
                                .newNameWithSuffixCannotContainBlankOrSpecialChars(40);
                EntityModel temp = new EntityModel();
                temp.setIsValid(true);
                temp.setEntity(newTemplateName);
                temp.ValidateEntity(
                        new IValidation[] {
                                new NotEmptyValidation(),
                                new RegexValidation(nameExpr, nameMsg)
                        });
                if (!temp.getIsValid()) {
                    getCloneTemplatesSuffix().setInvalidityReasons(temp.getInvalidityReasons());
                    getCloneTemplatesSuffix().setIsValid(false);
                    return false;
                }
            }
        }

        return getDestinationStorage().getIsValid() && getCluster().getIsValid()
                & getCloneTemplatesSuffix().getIsValid();
    }

    @Override
    public void setItems(final Iterable value)
    {
        String vmt_guidKey = "_VMT_ID ="; //$NON-NLS-1$
        String orKey = " or "; //$NON-NLS-1$
        StringBuilder searchPattern = new StringBuilder();
        searchPattern.append("Template: "); //$NON-NLS-1$

        final List<VmTemplate> list = (List<VmTemplate>) value;
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

                        if (alreadyInSystem.size() > 0) {
                            getCloneAllTemplates_message().setEntity(alreadyInSystem.size() + " " //$NON-NLS-1$
                                    + ConstantsManager.getInstance().getConstants().templateAlreadyExistsMsg());
                            if (list.size() == alreadyInSystem.size()) {
                                getCloneAllTemplates().setEntity(true);
                                getCloneAllTemplates().setIsChangable(false);
                                getCloneTemplatesSuffix().setIsAvailable(true);
                            } else {
                                getCloneOnlyDuplicateTemplates().setIsAvailable(true);
                                getCloneOnlyDuplicateTemplates().setEntity(true);
                                getCloneOnlyDuplicateTemplates().setIsChangable(false);
                                getCloneTemplatesSuffix().setIsAvailable(true);
                            }
                        } else {
                            getCloneAllTemplates_message().setEntity(ConstantsManager.getInstance()
                                    .getConstants()
                                    .templateNoExistsMsg());
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
    public boolean isObjectInSetup(Object vmTemplate) {
        if (alreadyInSystem == null) {
            return false;
        }
        return alreadyInSystem.containsKey(((VmTemplate) vmTemplate).getId());
    }
}
