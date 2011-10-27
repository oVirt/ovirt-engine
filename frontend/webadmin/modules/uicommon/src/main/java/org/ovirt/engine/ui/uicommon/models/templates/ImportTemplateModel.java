package org.ovirt.engine.ui.uicommon.models.templates;
import java.util.Collections;
import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.vdscommands.*;
import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.common.action.*;
import org.ovirt.engine.ui.frontend.*;
import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;
import org.ovirt.engine.core.common.*;

import org.ovirt.engine.ui.uicommon.validation.*;
import org.ovirt.engine.core.common.businessentities.*;

import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;

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
			java.util.Map.Entry<VmTemplate, java.util.ArrayList<DiskImage>> selectedItem = (java.util.Map.Entry<VmTemplate, java.util.ArrayList<DiskImage>>)getSelectedItem();
			VmTemplate template = selectedItem.getKey();
			setNameAndDescription(StringFormat.format("%1$s%2$s", template.getname(), !StringHelper.isNullOrEmpty(template.getdescription()) ? " [" + template.getdescription() + "]" : ""));
		}
		else
		{
			setNameAndDescription("");
		}

	}
}