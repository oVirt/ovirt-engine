package org.ovirt.engine.ui.uicommonweb.models.templates;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage_domain_static;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.compat.ObservableCollection;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringFormat;
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

    public ImportTemplateModel()
    {
        setDestinationStorage(new ListModel());
        setCluster(new ListModel());
    }

    @Override
    protected void InitDetailModels()
    {
        super.InitDetailModels();

        ObservableCollection<EntityModel> list = new ObservableCollection<EntityModel>();
        list.add(new TemplateGeneralModel());
        list.add(new TemplateImportInterfaceListModel());
        list.add(new TemplateImportDiskListModel());
        setDetailModels(list);
    }

    public boolean Validate()
    {
        getDestinationStorage().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });

        getCluster().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });

        return getDestinationStorage().getIsValid() && getCluster().getIsValid();
    }

    @Override
    protected void OnSelectedItemChanged()
    {
        super.OnSelectedItemChanged();
        if (getSelectedItem() != null)
        {
            java.util.Map.Entry<VmTemplate, java.util.ArrayList<DiskImage>> selectedItem =
                    (java.util.Map.Entry<VmTemplate, java.util.ArrayList<DiskImage>>) getSelectedItem();
            VmTemplate template = selectedItem.getKey();
            setNameAndDescription(StringFormat.format("%1$s%2$s",
                    template.getname(),
                    !StringHelper.isNullOrEmpty(template.getdescription()) ? " [" + template.getdescription() + "]"
                            : ""));
        }
        else
        {
            setNameAndDescription("");
        }

    }

    @Override
    protected String getListName() {
        return "ImportTemplateModel";
    }
}
