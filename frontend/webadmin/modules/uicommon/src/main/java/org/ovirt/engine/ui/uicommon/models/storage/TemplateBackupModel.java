package org.ovirt.engine.ui.uicommon.models.storage;
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

import org.ovirt.engine.ui.uicommon.models.templates.*;
import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.common.businessentities.*;

import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;

@SuppressWarnings("unused")
public class TemplateBackupModel extends ManageBackupModel implements ITaskTarget
{

	public java.util.Map.Entry<VmTemplate, java.util.ArrayList<DiskImage>> getSelectedItem()
	{
		return (java.util.Map.Entry<VmTemplate, java.util.ArrayList<DiskImage>>)super.getSelectedItem();
	}
	public void setSelectedItem(java.util.Map.Entry<VmTemplate, java.util.ArrayList<DiskImage>> value)
	{
		super.setSelectedItem(value);
	}


	public TemplateBackupModel()
	{
		setTitle("Template Import");
	}

	@Override
	protected void remove()
	{
		super.remove();

		if (getWindow() != null)
		{
			return;
		}

		ConfirmationModel model = new ConfirmationModel();
		setWindow(model);
		model.setTitle("Remove Backed up Template(s)");
		model.setHashName("remove_backed_up_template");
		model.setMessage("Template(s)");
		//model.Items = SelectedItems.Cast<KeyValuePair<VmTemplate, List<DiskImage>>>().Select(a => a.getKey().name);

		java.util.ArrayList<String> items = new java.util.ArrayList<String>();
		for (Object a : getSelectedItems())
		{
			java.util.Map.Entry<VmTemplate, java.util.ArrayList<DiskImage>> item = (java.util.Map.Entry<VmTemplate, java.util.ArrayList<DiskImage>>)a;
			VmTemplate template = item.getKey();
			items.add(template.getname());
		}
		model.setItems(items);

		model.setNote("Note: The deleted items might still appear on the sub-tab, since the remove operation might be long. Use the Refresh button, to get the updated status.");

		UICommand tempVar = new UICommand("OnRemove", this);
		tempVar.setTitle("OK");
		tempVar.setIsDefault(true);
		model.getCommands().add(tempVar);
		UICommand tempVar2 = new UICommand("Cancel", this);
		tempVar2.setTitle("Cancel");
		tempVar2.setIsCancel(true);
		model.getCommands().add(tempVar2);
	}

	private void OnRemove()
	{
		storage_pool pool = DataProvider.GetFirstStoragePoolByStorageDomain(getEntity().getId());
		//Frontend.RunMultipleActions(VdcActionType.RemoveVmTemplateFromImportExport,
		//	SelectedItems.Cast<KeyValuePair<VmTemplate, List<DiskImage>>>()
		//	.Select(a => (VdcActionParametersBase)new VmTemplateImportExportParameters(a.getKey().vmt_guid, Entity.id, pool.id))
		//	.ToList()
		//);
		java.util.ArrayList<VdcActionParametersBase> prms = new java.util.ArrayList<VdcActionParametersBase>();
		for (Object a : getSelectedItems())
		{
			java.util.Map.Entry<VmTemplate, java.util.ArrayList<DiskImage>> item = (java.util.Map.Entry<VmTemplate, java.util.ArrayList<DiskImage>>)a;
			VmTemplate template = item.getKey();
			prms.add(new VmTemplateImportExportParameters(template.getId(), getEntity().getId(), pool.getId()));
		}

		Frontend.RunMultipleAction(VdcActionType.RemoveVmTemplateFromImportExport, prms);


		Cancel();
		OnEntityChanged();
	}

	@Override
	protected void Restore()
	{
		super.Restore();

		if (getWindow() != null)
		{
			return;
		}

		ImportTemplateModel model = new ImportTemplateModel();
		setWindow(model);
		model.setTitle("Import Template(s)");
		model.setHashName("import_template");
		java.util.ArrayList<VDSGroup> clusters = DataProvider.GetClusterListByStorageDomain(getEntity().getId());

		model.getCluster().setItems(clusters);
		model.getCluster().setSelectedItem(Linq.FirstOrDefault(clusters));



		model.setSourceStorage(getEntity().getStorageStaticData());
		model.setStoragePool(DataProvider.GetFirstStoragePoolByStorageDomain(getEntity().getStorageStaticData().getId()));

		//var destStorages = DataProvider.GetDataDomainsListByDomain(Entity.id)
		//    .Where(a => (a.storage_domain_type == StorageDomainType.Data || a.storage_domain_type == StorageDomainType.Master)
		//                && a.status.HasValue && a.status.Value == StorageDomainStatus.Active)
		//    .ToList();

		java.util.ArrayList<storage_domains> destStorages = new java.util.ArrayList<storage_domains>();
		for (storage_domains domain : DataProvider.GetDataDomainsListByDomain(getEntity().getId()))
		{
			if ((domain.getstorage_domain_type() == StorageDomainType.Data || domain.getstorage_domain_type() == StorageDomainType.Master) && domain.getstatus() != null && domain.getstatus() == StorageDomainStatus.Active)
			{
				destStorages.add(domain);
			}
		}


		model.getDestinationStorage().setItems(destStorages);
		model.getDestinationStorage().setSelectedItem(Linq.FirstOrDefault(destStorages));

		model.setItems(getSelectedItems());

		if (destStorages.isEmpty())
		{
			model.getDestinationStorage().setIsChangable(false);
			model.getDestinationStorage().getChangeProhibitionReasons().add("Cannot import Template.");

			model.setMessage("There is no Data Storage Domain to import the Template into. Please attach a Data Storage Domain to the Template's Data Center.");

			UICommand tempVar = new UICommand("Cancel", this);
			tempVar.setTitle("Close");
			tempVar.setIsDefault(true);
			tempVar.setIsCancel(true);
			model.getCommands().add(tempVar);
		}
		else
		{
			UICommand tempVar2 = new UICommand("OnRestore", this);
			tempVar2.setTitle("OK");
			tempVar2.setIsDefault(true);
			model.getCommands().add(tempVar2);
			UICommand tempVar3 = new UICommand("Cancel", this);
			tempVar3.setTitle("Cancel");
			tempVar3.setIsCancel(true);
			model.getCommands().add(tempVar3);
		}
	}

	private void OnRestore()
	{
		ImportTemplateModel model = (ImportTemplateModel)getWindow();

		if (model.getProgress() != null)
		{
			return;
		}

		if (!model.Validate())
		{
			return;
		}

		//List<VdcReturnValueBase> ret = Frontend.RunMultipleActions(VdcActionType.ImportVmTemplate,
		//	SelectedItems.Cast<KeyValuePair<VmTemplate, List<DiskImage>>>()
		//	.Select(a => (VdcActionParametersBase)new ImprotVmTemplateParameters(model.StoragePool.id,
		//		model.SourceStorage.id,
		//		model.DestinationStorage.ValueAs<storage_domains>().id,
		//		model.Cluster.ValueAs<VDSGroup>().ID,
		//		a.getKey())
		//	)
		//	.ToList()
		//);
		java.util.ArrayList<VdcActionParametersBase> prms = new java.util.ArrayList<VdcActionParametersBase>();
		for (Object a : getSelectedItems())
		{
			java.util.Map.Entry<VmTemplate, java.util.ArrayList<DiskImage>> item = (java.util.Map.Entry<VmTemplate, java.util.ArrayList<DiskImage>>)a;
			prms.add(new ImprotVmTemplateParameters(model.getStoragePool().getId(), model.getSourceStorage().getId(), ((storage_domains)model.getDestinationStorage().getSelectedItem()).getId(), ((VDSGroup)model.getCluster().getSelectedItem()).getId(), item.getKey()));
		}

		model.StartProgress(null);

		Frontend.RunMultipleAction(VdcActionType.ImportVmTemplate, prms,
		new IFrontendMultipleActionAsyncCallback() {
			@Override
			public void Executed(FrontendMultipleActionAsyncResult  result) {

			TemplateBackupModel templateBackupModel = (TemplateBackupModel) result.getState();
			templateBackupModel.getWindow().StopProgress();
			templateBackupModel.Cancel();
			java.util.ArrayList<VdcReturnValueBase> retVals = (java.util.ArrayList<VdcReturnValueBase>)result.getReturnValue();
			if (retVals != null && templateBackupModel.getSelectedItems().size() > retVals.size())
			{
				ConfirmationModel confirmModel = new ConfirmationModel();
				templateBackupModel.setConfirmWindow(confirmModel);
				confirmModel.setTitle("Import Template(s)");
				confirmModel.setHashName("import_template");
				String importedTemplates = "";
				int i = 0;
				for (Object a : templateBackupModel.getSelectedItems())
				{
					java.util.Map.Entry<VmTemplate, java.util.ArrayList<DiskImage>> item = (java.util.Map.Entry<VmTemplate, java.util.ArrayList<DiskImage>>)a;
					VmTemplate template = item.getKey();
					if (Linq.FindVdcReturnValueByDescription(retVals, template.getname()) == null)
					{
						importedTemplates += template.getname() + (++i != templateBackupModel.getSelectedItems().size() ? ", " : "");
					}
				}
				StringHelper.trimEnd(importedTemplates.trim(), ',');
				confirmModel.setMessage(StringFormat.format("Import process has begun for Template(s): %1$s.\nYou can check import status in the 'Events' tab of the specific destination storage domain, or in the main 'Events' tab", importedTemplates));
				UICommand tempVar = new UICommand("CancelConfirm", templateBackupModel);
				tempVar.setTitle("Close");
				tempVar.setIsDefault(true);
				tempVar.setIsCancel(true);
				confirmModel.getCommands().add(tempVar);
			}

			}
		}, this);



	}

	@Override
	protected void EntityPropertyChanged(Object sender, PropertyChangedEventArgs e)
	{
		super.EntityPropertyChanged(sender, e);

		if (e.PropertyName.equals("storage_domain_shared_status"))
		{
			getSearchCommand().Execute();
		}
	}

	@Override
	protected void SyncSearch()
	{
		super.SyncSearch();

		if (getEntity() == null || getEntity().getstorage_domain_type() != StorageDomainType.ImportExport || getEntity().getstorage_domain_shared_status() != StorageDomainSharedStatus.Active)
		{
			setItems(null);
			setIsRefreshing(false);
		}
		else
		{
			setIsRefreshing(true);
			Task.Create(this, 1).Run();
		}
	}

	@Override
	protected void AsyncSearch()
	{
		super.AsyncSearch();
		SyncSearch();
	}

	@Override
	public void ExecuteCommand(UICommand command)
	{
		super.ExecuteCommand(command);

		if (StringHelper.stringsEqual(command.getName(), "OnRemove"))
		{
			OnRemove();
		}
		else if (StringHelper.stringsEqual(command.getName(), "OnRestore"))
		{
			OnRestore();
		}
	}

	private VdcQueryReturnValue returnValue;
	private Guid storageDomainId = Guid.Empty;

	public void run(TaskContext context)
	{
		if (getEntity() != null)
		{
			switch ((Integer)context.getState())
			{
				case 1:
					storageDomainId = getEntity().getId();

					storage_pool pool = DataProvider.GetFirstStoragePoolByStorageDomain(storageDomainId);

					GetAllFromExportDomainQueryParamenters tempVar = new GetAllFromExportDomainQueryParamenters(pool.getId(), storageDomainId);
					tempVar.setGetAll(true);
					returnValue = Frontend.RunQuery(VdcQueryType.GetTemplatesFromExportDomain, tempVar);

					context.InvokeUIThread(this, 2);
					break;

				case 2:
					//if user didn't change the entity meanwhile, update Items, else dont touch it
					if (storageDomainId.equals(getEntity().getId()))
					{
						if (returnValue != null && returnValue.getSucceeded())
						{
							//Items = ((Dictionary<VmTemplate, List<DiskImage>>)returnValue.ReturnValue).ToList();
							java.util.ArrayList<java.util.Map.Entry<VmTemplate, java.util.ArrayList<DiskImage>>> items = new java.util.ArrayList<java.util.Map.Entry<VmTemplate, java.util.ArrayList<DiskImage>>>();
							java.util.HashMap<VmTemplate, java.util.ArrayList<DiskImage>> list = (java.util.HashMap<VmTemplate, java.util.ArrayList<DiskImage>>)returnValue.getReturnValue();

							for (java.util.Map.Entry<VmTemplate, java.util.ArrayList<DiskImage>> item : list.entrySet())
							{
								items.add(item);
							}
							setItems(items);
						}
						else
						{
							setItems(new java.util.ArrayList<java.util.Map.Entry<VmTemplate, java.util.ArrayList<DiskImage>>>());
						}
						setIsRefreshing(false);
					}
					returnValue = null;
					storageDomainId = Guid.Empty;
					break;
			}
		}
	}
}